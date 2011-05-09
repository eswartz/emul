# *******************************************************************************
# * Copyright (c) 2011 Wind River Systems, Inc. and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Wind River Systems - initial API and implementation
# *******************************************************************************

"""
Locator service uses transport layer to search
for peers and to collect and maintain up-to-date
data about peer’s attributes.
"""

import threading, time, exceptions, socket, cStringIO
from tcf.services import locator
from tcf.util import logging
from tcf.channel import fromJSONSequence, toJSONSequence
from tcf.channel.ChannelProxy import ChannelProxy
from tcf import protocol, peer, errors

# Flag indicating whether tracing of the the discovery activity is enabled.
__TRACE_DISCOVERY = False

class SubNet(object):
    def __init__(self, prefix_length, address, broadcast):
        self.prefix_length = prefix_length
        self.address = address
        self.broadcast = broadcast

    def contains(self, addr):
        if addr is None or self.address is None: return False
        a1 = addr.getAddress()
        a2 = self.address.getAddress()
        if len(a1) != len(a2): return False
        i = 0
        if self.prefix_length <= len(a1) * 8:
            l = self.prefix_length
        else:
            l = len(a1) * 8
        while i + 8 <= l:
            n = i / 8
            if a1[n] != a2[n]: return False
            i += 8
        while i < l:
            n = i / 8
            m = 1 << (7 - i % 8)
            if (a1[n] & m) != (a2[n] & m): return False
            i += 1
        return True

    def __eq__(self, o):
        if not isinstance(o, SubNet): return False
        return self.prefix_length == o.prefix_length and \
            self.broadcast == o.broadcast and \
            self.address == o.address

    def __hash__(self):
        return hash(self.address)

    def __str__(self):
        return "%s/%d" % (self.address.getHostAddress(), self.prefix_length)

class Slave(object):
    # Time of last packet receiver from self slave
    last_packet_time = 0

    #Time of last REQ_SLAVES packet received from self slave
    last_req_slaves_time = 0

    def __init__(self, address, port):
        self.address = address
        self.port = port

    def __str__(self):
        return "%s/%d" % (self.address.getHostAddress(), self.port)

class AddressCacheItem(object):
    address = None
    time_stamp = 0
    used = False
    def __init__(self, host):
        self.host = host

class InetAddress(object):
    "Mimicking Java InetAddress class"
    def __init__(self, host, addr):
        self.host = host
        self.addr = addr
    def getHostAddress(self):
        return self.addr

class InputPacket(object):
    """
    Wrapper for final class DatagramPacket so its toString() can present
    the value in the debugger in a readable fashion.
    """
    def __init__(self, data, addr):
        self.data = data
        self.addr = addr

    def getLength(self):
        return len(self.data)

    def getData(self):
        return self.data

    def getPort(self):
        return self.addr[1]

    def getAddress(self):
        return self.addr[0]

    def __str__(self):
        return "[address=%s,port=%d,data=\"%s\"]" % (self.getAddress(), self.getPort(), self.data)

DISCOVEY_PORT = 1534
MAX_PACKET_SIZE = 9000 - 40 - 8
PREF_PACKET_SIZE = 1500 - 40 - 8

# TODO: research usage of DNS-SD (DNS Service Discovery) to discover TCF peers
class LocatorService(locator.LocatorService):
    locator = None
    peers = {} # str->Peer
    listeners = [] # list of LocatorListener
    error_log = set() # set of str
    _error_log_lock = threading.RLock()
    
    addr_cache = {} # str->AddressCacheItem
    _addr_cache_lock = threading.Condition()
    addr_request = False
    local_peer = None
    last_master_packet_time = 0
    
    def __init__(self):
        self.subnets = set()
        self.slaves = []
        self.inp_buf = bytearray(MAX_PACKET_SIZE)
        self.out_buf = bytearray(MAX_PACKET_SIZE)
        service = self
        LocatorService.locator = self
        LocatorService.local_peer = peer.LocalPeer()
        class TimerThread(threading.Thread):
            def __init__(self, callable):
                self._callable = callable
            def __call__(self):
                while True:
                    try:
                        time.sleep(locator.DATA_RETENTION_PERIOD / 4 / 1000.)
                        protocol.invokeAndWait(self._callable)
                    except RuntimeError:
                        # TCF event dispatch is shut down
                        return
                    except exceptions.Exception as x:
                        service.log("Unhandled exception in TCF discovery timer thread", x)
        self.timer_thread = TimerThread(self.__refresh_timer)
        class DNSLookupThread(threading.Thread):
            def __call__(self):
                while True:
                    try:
                        itemSet = None
                        with LocatorService._addr_cache_lock:
                            if not LocatorService.addr_request:
                                LocatorService._addr_cache_lock.wait(locator.DATA_RETENTION_PERIOD)
                            msec = int(time.time())
                            for a in LocatorService.addr_cache.values():
                                if a.time_stamp + locator.DATA_RETENTION_PERIOD * 10 < msec:
                                    if a.used:
                                        if itemSet is None: itemSet = set()
                                        itemSet.add(a)
                                    else:
                                        LocatorService.addr_cache.remove(a)
                            LocatorService.addr_request = False
                        if itemSet is not None:
                            for a in itemSet:
                                addr = None
                                try:
                                    addr = socket.gethostbyname(a.host)
                                except socket.gaierror:
                                    pass
                                with LocatorService._addr_cache_lock:
                                    if addr is None:
                                        a.address = None
                                    else:
                                        a.address = InetAddress(a.host, addr)
                                    a.time_stamp = time
                                    a.used = False
                    except exceptions.BaseException as x:
                        service.log("Unhandled exception in TCF discovery DNS lookup thread", x)
        self.dns_lookup_thread = DNSLookupThread()
        class InputThread(threading.Thread):
            def __call__(self):
                try:
                    while True:
                        sock = service.socket
                        try:
                            data, addr = sock.recvfrom(MAX_PACKET_SIZE)
                            p = InputPacket(data, addr)
                            protocol.invokeAndWait(self._callable, p)
                        except RuntimeError:
                            # TCF event dispatch is shutdown
                            return
                        except exceptions.Exception as x:
                            if sock != service.socket: continue
                            port = sock.getsockname()[1]
                            service.log("Cannot read from datagram socket at port %d" % port, x)
                            time.sleep(2)
                except exceptions.BaseException as x:
                    service.log("Unhandled exception in socket reading thread", x)
        self.input_thread = InputThread()
        try:
            self.loopback_addr = socket.gethostname()
            self.out_buf[0:8] = 'TCF%s\0\0\0\0' % locator.CONF_VERSION
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            try:
                self.socket.bind(('', DISCOVEY_PORT))
                if __TRACE_DISCOVERY:
                    logging.trace("Became the master agent (bound to port %d)" % self.socket.getsockname()[1])
            except socket.error as x:
                self.socket.bind(('', 0))
                if __TRACE_DISCOVERY:
                    logging.trace("Became a slave agent (bound to port %d)" + self.socket.getsockname()[1])
            self.socket.setsockopt(socket.SOL_UDP, socket.SO_BROADCAST, 1)
            self.input_thread.setName("TCF Locator Receiver")
            self.timer_thread.setName("TCF Locator Timer")
            self.dns_lookup_thread.setName("TCF Locator DNS Lookup")
            self.input_thread.setDaemon(True)
            self.timer_thread.setDaemon(True)
            self.dns_lookup_thread.setDaemon(True)
            self.input_thread.start()
            self.timer_thread.start()
            self.dns_lookup_thread.start()
            class LocatorListener(locator.LocatorListener):
                def peerAdded(self, peer):
                    service._sendPeerInfo(peer, None, 0)
                def peerChanged(self, peer):
                    service._sendPeerInfo(peer, None, 0)
            self.listeners.add(LocatorListener())
            self.__refreshSubNetList()
            self.__sendPeersRequest(None, 0)
            self.__sendAll(None, 0, None, int(time.time()))
        except exceptions.Exception as x:
            self.log("Cannot open UDP socket for TCF discovery protocol", x)

    @classmethod
    def getLocalPeer(cls):
        return cls.local_peer

    @classmethod
    def getListeners(cls):
        return cls.listeners[:]

    def __makeErrorReport(self, code, msg):
        err = {}
        err[errors.ERROR_TIME] = int(time.time())
        err[errors.ERROR_CODE] = code
        err[errors.ERROR_FORMAT] = msg
        return err

    def __command(self, channel, token, name, data):
        try:
            if name == "redirect":
                peer_id = fromJSONSequence(data)[0]
                _peer = self.peers.get(peer_id)
                if _peer is None:
                    error = self.__makeErrorReport(errors.TCF_ERROR_UNKNOWN_PEER, "Unknown peer ID")
                    channel.sendResult(token, toJSONSequence((error,)))
                    return
                channel.sendResult(token, toJSONSequence((None,)))
                if isinstance(_peer, peer.LocalPeer):
                    channel.sendEvent(protocol.getLocator(), "Hello", toJSONSequence((channel.getLocalServices(),)))
                    return
                ChannelProxy(channel, _peer.openChannel())
            elif name == "sync":
                channel.sendResult(token, None)
            elif name == "getPeers":
                arr = []
                for p in self.peers.values():
                    arr.append(p.getAttributes())
                channel.sendResult(token, toJSONSequence((None, arr)))
            else:
                channel.rejectCommand(token)
        except exceptions.BaseException as x:
            channel.terminate(x)

    def _log(self, msg, x):
        # Don't report same error multiple times to avoid filling up the log file.
        with self._error_log_lock:
            if msg in self.error_log: return
            self.error_log.add(msg)
        protocol.log(msg, x)

    def _getInetAddress(self, host):
        if not host: return None
        with self._addr_cache_lock:
            i = self.addr_cache.get(host)
            if i is None:
                i = AddressCacheItem(host)
                ch = host[0]
                if ch == '[' or ch == ':' or ch >= '0' and ch <= '9':
                    try:
                        addr = socket.gethostbyname(host)
                        i.address = InetAddress(host, addr)
                    except socket.gaierror:
                        pass
                    i.time_stamp = int(time.time())
                else:
                    # socket.gethostbyname() can cause long delay - delegate to background thread
                    LocatorService.addr_request = True
                    self._addr_cache_lock.notify()
                self.addr_cache.put(host, i)
            i.used = True
            return i.address

    def __refresh_timer(self):
        tm = int(time.time())
        # Cleanup slave table
        if self.slaves:
            i = 0
            while i < len(self.slaves):
                s = self.slaves[i]
                if s.last_packet_time + locator.DATA_RETENTION_PERIOD < tm:
                    del self.slaves
                else:
                    i += 1

        # Cleanup peers table
        stale_peers = None
        for p in self.peers.values():
            if isinstance(p, peer.RemotePeer):
                if p.getLastUpdateTime() + locator.DATA_RETENTION_PERIOD < time:
                    if stale_peers == None: stale_peers = []
                    stale_peers.append(p)
        if stale_peers is not None:
            for p in stale_peers: p.dispose()

        # Try to become a master
        port = self.socket.getsockname()[1]
        if port != DISCOVEY_PORT and \
                self.last_master_packet_time + locator.DATA_RETENTION_PERIOD / 2 <= tm:
            s0 = self.socket
            s1 = None
            try:
                s1 = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
                s1.bind(DISCOVEY_PORT)
                s1.setsockopt(socket.SOL_UDP, socket.SO_BROADCAST, 1)
                self.socket = s1
                s0.close()
            except:
                pass
        self.__refreshSubNetList()
        if port != DISCOVEY_PORT:
            for subnet in self.subnets:
                self.__addSlave(subnet.address, port, tm, tm)
        self.__sendAll(None, 0, None, tm)

    def __addSlave(self, addr, port, timestamp, time_now):
        for s in self.slaves:
            if s.port == port and s.address == addr:
                if s.last_packet_time < timestamp: s.last_packet_time = timestamp
                return s
        s = Slave(addr, port)
        s.last_packet_time = timestamp
        self.slaves.append(s)
        self.__sendPeersRequest(addr, port)
        self.__sendAll(addr, port, s, time_now)
        self.__sendSlaveInfo(s, time_now)
        return s

    def __refreshSubNetList(self):
        subNetSet = set()
        try:
            self.__getSubNetList(subNetSet)
        except exceptions.BaseException as x:
            self.log("Cannot get list of network interfaces", x)
        for s in self.subnets:
            if s in subNetSet: continue
            self.subnets.remove(s)
        for s in subNetSet:
            if s in self.subnets: continue
            self.subnets.append(s)
        if __TRACE_DISCOVERY:
            str = cStringIO.StringIO()
            str.write("Refreshed subnet list:")
            for subnet in self.subnets:
                str.write("\n\t* address=%s, broadcast=%s" + (subnet.address, subnet.broadcast))
            logging.trace(str.getvalue())

    def __getSubNetList(self, set):
        name, aliases, addresses = socket.gethostbyname_ex(socket.gethostname())
        for address in addresses:
            rawaddr = socket.inet_aton(address)
            if len(rawaddr) != 4: continue
            rawaddr = rawaddr[:3] + '\0xFF'
            broadcast = socket.inet_ntoa(rawaddr)
            set.append(SubNet(24, address, broadcast))

    def __getUTF8Bytes(self, s):
        return s.encode("UTF-8")

    # Used for tracing
    packetTypes = [
        None,
        "CONF_REQ_INFO",
        "CONF_PEER_INFO",
        "CONF_REQ_SLAVES",
        "CONF_SLAVES_INFO"
    ]

    def __sendDatagramPacket(self, subnet, size, addr, port):
        try:
            if addr is None:
                addr = subnet.broadcast
                port = DISCOVEY_PORT
                for slave in self.slaves:
                    self.__sendDatagramPacket(subnet, size, slave.address, slave.port)
            if not subnet.contains(addr): return False
            if port == self.socket.getsockname()[1] and addr == subnet.address: return False
            self.socket.send(str(self.out_buf[:size]), (addr, port))

            if __TRACE_DISCOVERY:
                map = None
                if self.out_buf[4] == locator.CONF_PEER_INFO:
                    map = self.__parsePeerAttributes(self.out_buf, 8)
                self.__traceDiscoveryPacket(False, self.packetTypes[self.out_buf[4]], map, addr, port)
        except exceptions.BaseException as x:
            self.log("Cannot send datagram packet to %s" % addr, x)
            return False
        return True

    def __parsePeerAttributes(self, data, size):
        """
        Parse peer attributes in CONF_INFO_PEER packet data
        
        @param data
                   the packet section that contain the peer attributes
        @param size
                   the number of bytes in [data] that contain peer attributes
        @return a map containing the attributes
        """
        map = {}
        s = data[8:size - 8].decode("UTF-8")
        l = len(s)
        i = 0
        while i < l:
            i0 = i
            while i < l and s[i] != '=' and s[i] != '\0': i += 1
            i1 = i
            if i < l and s[i] == '=': i += 1
            i2 = i
            while i < l and s[i] != '\0': i += 1
            i3 = i
            if i < l and s[i] == '\0': i += 1
            key = s[i0:i1]
            val = s[i2:i3]
            map.put(key, val)
        return map

    def __sendPeersRequest(self, addr, port):
        self.out_buf[4] = locator.CONF_REQ_INFO
        for subnet in self.subnets:
            self.__sendDatagramPacket(subnet, 8, addr, port)

    def __sendPeerInfo(self, _peer, addr, port):
        attrs = _peer.getAttributes()
        peer_addr = self.__getInetAddress(attrs.get(peer.ATTR_IP_HOST))
        if peer_addr is None: return
        if attrs.get(peer.ATTR_IP_PORT) is None: return
        self.out_buf[4] = CONF_PEER_INFO
        i = 8

        for subnet in self.subnets:
            if isinstance(_peer, peer.RemotePeer):
                if self.socket.getsockname()[1] != DISCOVEY_PORT: return
                if (!subnet.address.equals(loopback_addr) && !subnet.address.equals(peer_addr)) continue
            }
            if (!subnet.address.equals(loopback_addr)) {
                if (!subnet.contains(peer_addr)) continue
            }
            if (i == 8) {
                StringBuffer sb = new StringBuffer(out_buf.length)
                for (str key : attrs.keySet()) {
                    sb.append(key)
                    sb.append('=')
                    sb.append(attrs.get(key))
                    sb.append((char)0)
                }
                byte[] bt = getUTF8Bytes(sb.toString())
                if (i + bt.length > out_buf.length) return
                System.arraycopy(bt, 0, out_buf, i, bt.length)
                i += bt.length
            }
            if (sendDatagramPacket(subnet, i, addr, port)) subnet.send_all_ok = True
        }
    }

    private void sendEmptyPacket(InetAddress addr, int port) {
        out_buf[4] = CONF_SLAVES_INFO
        for (SubNet subnet : subnets) {
            if (subnet.send_all_ok) continue
            sendDatagramPacket(subnet, 8, addr, port)
        }
    }

    private void sendAll(InetAddress addr, int port, Slave sl, long time) {
        for (SubNet subnet : subnets) subnet.send_all_ok = False
        for (IPeer peer : peers.values()) sendPeerInfo(peer, addr, port)
        if (addr != None && sl != None && sl.last_req_slaves_time + DATA_RETENTION_PERIOD >= time) {
            sendSlavesInfo(addr, port, time)
        }
        sendEmptyPacket(addr, port)
    }

    private void sendSlavesRequest(SubNet subnet, InetAddress addr, int port) {
        out_buf[4] = CONF_REQ_SLAVES
        sendDatagramPacket(subnet, 8, addr, port)
    }

    private void sendSlaveInfo(Slave x, long time) {
        int ttl = (int)(x.last_packet_time + DATA_RETENTION_PERIOD - time)
        if (ttl <= 0) return
        out_buf[4] = CONF_SLAVES_INFO
        for (SubNet subnet : subnets) {
            if (!subnet.contains(x.address)) continue
            int i = 8
            str s = ttl + ":" + x.port + ":" + x.address.getHostAddress()
            byte[] bt = getUTF8Bytes(s)
            System.arraycopy(bt, 0, out_buf, i, bt.length)
            i += bt.length
            out_buf[i++] = 0
            for (Slave y : slaves) {
                if (!subnet.contains(y.address)) continue
                if (y.last_req_slaves_time + DATA_RETENTION_PERIOD < time) continue
                sendDatagramPacket(subnet, i, y.address, y.port)
            }
        }
    }

    private void sendSlavesInfo(InetAddress addr, int port, long time) {
        out_buf[4] = CONF_SLAVES_INFO
        for (SubNet subnet : subnets) {
            if (!subnet.contains(addr)) continue
            int i = 8
            for (Slave x : slaves) {
                int ttl = (int)(x.last_packet_time + DATA_RETENTION_PERIOD - time)
                if (ttl <= 0) continue
                if (x.port == port && x.address.equals(addr)) continue
                if (!subnet.address.equals(loopback_addr)) {
                    if (!subnet.contains(x.address)) continue
                }
                subnet.send_all_ok = True
                str s = x.last_packet_time + ":" + x.port + ":" + x.address.getHostAddress()
                byte[] bt = getUTF8Bytes(s)
                if (i > 8 && i + bt.length >= PREF_PACKET_SIZE) {
                    sendDatagramPacket(subnet, i, addr, port)
                    i = 8
                }
                System.arraycopy(bt, 0, out_buf, i, bt.length)
                i += bt.length
                out_buf[i++] = 0
            }
            if (i > 8) sendDatagramPacket(subnet, i, addr, port)
        }
    }

    private boolean isRemote(InetAddress address, int port) {
        if (port != socket.getLocalPort()) return True
        for (SubNet s : subnets) {
            if (s.address.equals(address)) return False
        }
        return True
    }

    private void handleDatagramPacket(InputPacket p) {
        try {
            long time = System.currentTimeMillis()
            byte[] buf = p.getData()
            int len = p.getLength()
            if (len < 8) return
            if (buf[0] != 'T') return
            if (buf[1] != 'C') return
            if (buf[2] != 'F') return
            if (buf[3] != CONF_VERSION) return
            int remote_port = p.getPort()
            InetAddress remote_address = p.getAddress()
            if (isRemote(remote_address, remote_port)) {
                Slave sl = None
                if (remote_port != DISCOVEY_PORT) {
                    sl = addSlave(remote_address, remote_port, time, time)
                }
                switch (buf[4]) {
                case CONF_PEER_INFO:
                    handlePeerInfoPacket(p)
                    break
                case CONF_REQ_INFO:
                    handleReqInfoPacket(p, sl, time)
                    break
                case CONF_SLAVES_INFO:
                    handleSlavesInfoPacket(p, time)
                    break
                case CONF_REQ_SLAVES:
                    handleReqSlavesPacket(p, sl, time)
                    break
                }
                for (SubNet subnet : subnets) {
                    if (!subnet.contains(remote_address)) continue
                    long delay = DATA_RETENTION_PERIOD / 3
                    if (remote_port != DISCOVEY_PORT) delay = DATA_RETENTION_PERIOD / 32
                    else if (!subnet.address.equals(remote_address)) delay = DATA_RETENTION_PERIOD / 2
                    if (subnet.last_slaves_req_time + delay <= time) {
                        sendSlavesRequest(subnet, remote_address, remote_port)
                        subnet.last_slaves_req_time = time
                    }
                    if (subnet.address.equals(remote_address) && remote_port == DISCOVEY_PORT) {
                        last_master_packet_time = time
                    }
                }
            }
        }
        catch (Throwable x) {
            log("Invalid datagram packet received from " + p.getAddress() + "/" + p.getPort(), x)
        }
    }

    private void handlePeerInfoPacket(InputPacket p) {
        try {
            Map<str,str> map = parsePeerAtrributes(p.getData(), p.getLength())
            if (TRACE_DISCOVERY) traceDiscoveryPacket(True, "CONF_PEER_INFO", map, p)
            str id = map.get(IPeer.ATTR_ID)
            if (id == None) throw new Exception("Invalid peer info: no ID")
            boolean ok = True
            str host = map.get(IPeer.ATTR_IP_HOST)
            if (host != None) {
                ok = False
                InetAddress peer_addr = getInetAddress(host)
                if (peer_addr != None) {
                    for (SubNet subnet : subnets) {
                        if (subnet.contains(peer_addr)) {
                            ok = True
                            break
                        }
                    }
                }
            }
            if (ok) {
                IPeer peer = peers.get(id)
                if (peer instanceof RemotePeer) {
                    ((RemotePeer)peer).updateAttributes(map)
                }
                else if (peer == None) {
                    new RemotePeer(map)
                }
            }
        }
        catch (Exception x) {
            log("Invalid datagram packet received from " + p.getAddress() + "/" + p.getPort(), x)
        }
    }

    private void handleReqInfoPacket(InputPacket p, Slave sl, long time) {
        if (TRACE_DISCOVERY) {
            traceDiscoveryPacket(True, "CONF_REQ_INFO", None, p)
        }
        sendAll(p.getAddress(), p.getPort(), sl, time)
    }

    private void handleSlavesInfoPacket(InputPacket p, long time_now) {
        try {
            Map<str,str> trace_map = None # used for tracing only
            int slave_index = 0        # used for tracing only
            if (TRACE_DISCOVERY) {
                trace_map = new HashMap<str,str>(3)
            }

            str s = new str(p.getData(), 8, p.getLength() - 8, "UTF-8")
            int l = s.length()
            int i = 0
            while (i < l) {
                int time0 = i
                while (i < l&& s.charAt(i) != ':' && s.charAt(i) != 0) i++
                int time1 = i
                if (i < l && s.charAt(i) == ':') i++
                int port0 = i
                while (i < l&& s.charAt(i) != ':' && s.charAt(i) != 0) i++
                int port1 = i
                if (i < l && s.charAt(i) == ':') i++
                int host0 = i
                while (i < l && s.charAt(i) != 0) i++
                int host1 = i
                if (i < l && s.charAt(i) == 0) i++
                int port = Integer.parseInt(s.substring(port0, port1))
                str timestamp = s.substring(time0, time1)
                str host = s.substring(host0, host1)
                if (TRACE_DISCOVERY) {
                    trace_map.put("slave[" + slave_index++ + ']', timestamp + ':' + port + ':' + host)
                }
                if (port != DISCOVEY_PORT) {
                    InetAddress addr = getInetAddress(host)
                    if (addr != None) {
                        long delta = 10006030 # 30 minutes
                        long time_val = timestamp.length() > 0 ? Long.parseLong(timestamp) : time_now
                        if (time_val < 3600000) {
                            """Time stamp is "time to live" in milliseconds"""
                            time_val = time_now + time_val / 1000 - DATA_RETENTION_PERIOD
                        }
                        else if (time_val < time_now / 1000 + 50000000) {
                            """Time stamp is in seconds"""
                            time_val= 1000
                        }
                        else {
                            """Time stamp is in milliseconds"""
                        }
                        if (time_val < time_now - delta || time_val > time_now + delta) {
                            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                            str msg =
                                "Invalid slave info timestamp: " + timestamp +
                                " -> " + fmt.format(new Date(time_val))
                            log("Invalid datagram packet received from " +
                                    p.getAddress() + "/" + p.getPort(),
                                    new Exception(msg))
                            time_val = time_now - DATA_RETENTION_PERIOD / 2
                        }
                        addSlave(addr, port, time_val, time_now)
                    }
                }
            }
            if (TRACE_DISCOVERY) {
                traceDiscoveryPacket(True, "CONF_SLAVES_INFO", trace_map, p)
            }
        }
        catch (Exception x) {
            log("Invalid datagram packet received from " + p.getAddress() + "/" + p.getPort(), x)
        }
    }

    private void handleReqSlavesPacket(InputPacket p, Slave sl, long time) {
        if (TRACE_DISCOVERY) {
            traceDiscoveryPacket(True, "CONF_REQ_SLAVES", None, p)
        }
        if (sl != None) sl.last_req_slaves_time = time
        sendSlavesInfo(p.getAddress(), p.getPort(),  time)
    }

    """----------------------------------------------------------------------------------"""

    public static LocatorService getLocator() {
        return locator
    }

    public str getName() {
        return NAME
    }

    public Map<str,IPeer> getPeers() {
        assert Protocol.isDispatchThread()
        return peers
    }

    public IToken redirect(str peer_id, DoneRedirect done) {
        throw new Error("Channel redirect cannot be done on local peer")
    }

    public IToken redirect(Map<str,str> peer, DoneRedirect done) {
        throw new Error("Channel redirect cannot be done on local peer")
    }

    public IToken sync(DoneSync done) {
        throw new Error("Channel sync cannot be done on local peer")
    }

    public void addListener(LocatorListener listener) {
        assert listener != None
        assert Protocol.isDispatchThread()
        listeners.add(listener)
    }

    public void removeListener(LocatorListener listener) {
        assert Protocol.isDispatchThread()
        listeners.remove(listener)
    }

    """
    Log that a TCF Discovery packet has be sent or received. The trace is
    sent to stdout. This should be called only if the tracing has been turned
    on via java property definitions.
    
    @param received
               True if the packet was sent, otherwise it was received
    @param type
               a string specifying the type of packet, e.g., "CONF_PEER_INFO"
    @param attrs
               a set of attributes relevant to the type of packet (typically
               a peer's attributes)
    @param addr
               the network address the packet is being sent to
    @param port
               the port the packet is being sent to
    """
    private static void traceDiscoveryPacket(boolean received, str type, Map<str,str> attrs, InetAddress addr, int port) {
        assert TRACE_DISCOVERY
        StringBuilder str = new StringBuilder(type + (received ? " received from " : " sent to ") +  addr + "/" + port)
        if (attrs != None) {
            Iterator<Entry<str, str>> iter = attrs.entrySet().iterator()
            while (iter.hasNext()) {
                Entry<str, str> entry = iter.next()
                str.append("\n\t" + entry.getKey() + '=' + entry.getValue())
            }
        }
        logging.trace(str.toString())
    }

    """
    Convenience variant that takes a DatagramPacket for specifying
    the target address and port.
    """
    private static void traceDiscoveryPacket(boolean received, str type, Map<str,str> attrs, InputPacket packet) {
        traceDiscoveryPacket(received, type, attrs, packet.getAddress(), packet.getPort())
    }
}


static {
    ServiceManager.addServiceProvider(new IServiceProvider() {

        public IService[] getLocalService(final IChannel channel) {
            channel.addCommandServer(locator, new IChannel.ICommandServer() {
                public void command(IToken token, str name, byte[] data) {
                    locator.command((AbstractChannel)channel, token, name, data)
                }
            })
            return new IService[]{ locator }
        }

        public IService getServiceProxy(IChannel channel, str service_name) {
            return None
        }
    })
}

