/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.services.local;

import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.internal.tcf.core.LocalPeer;
import org.eclipse.tm.internal.tcf.core.RemotePeer;
import org.eclipse.tm.internal.tcf.core.ServiceManager;
import org.eclipse.tm.internal.tcf.core.TransportManager;
import org.eclipse.tm.tcf.core.AbstractChannel;
import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IServiceProvider;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;


/**
 * Locator service uses transport layer to search
 * for peers and to collect and maintain up-to-date
 * data about peer’s attributes.
 */
// TODO: research usage of DNS-SD (DNS Service Discovery) to discover TCF peers
public class LocatorService implements ILocator {

    private static final int DISCOVEY_PORT = 1534;
    private static final int MAX_PACKET_SIZE = 9000 - 40 - 8;
    private static final int PREF_PACKET_SIZE = 1500 - 40 - 8;

    private static LocatorService locator;
    private static final Map<String,IPeer> peers = new HashMap<String,IPeer>();
    private static final ArrayList<LocatorListener> listeners = new ArrayList<LocatorListener>();
    private static final HashMap<String,Throwable> error_log = new HashMap<String,Throwable>();

    private final HashSet<SubNet> subnets = new HashSet<SubNet>();
    private final ArrayList<Slave> slaves = new ArrayList<Slave>();
    private final byte[] inp_buf = new byte[MAX_PACKET_SIZE];
    private final byte[] out_buf = new byte[MAX_PACKET_SIZE];

    private InetAddress loopback_addr;

    private static class SubNet {
        final int prefix_length;
        final InetAddress address;
        final InetAddress broadcast;

        long last_slaves_req_time;
        boolean send_all_ok;

        SubNet(int prefix_length, InetAddress address, InetAddress broadcast) {
            this.prefix_length = prefix_length;
            this.address = address;
            this.broadcast = broadcast;
        }

        boolean contains(InetAddress addr) {
            if (addr == null || address == null) return false;
            byte[] a1 = addr.getAddress();
            byte[] a2 = address.getAddress();
            if (a1.length != a2.length) return false;
            int i = 0;
            int l = prefix_length <= a1.length * 8 ? prefix_length : a1.length * 8;
            while (i + 8 <= l) {
                int n = i / 8;
                if (a1[n] != a2[n]) return false;
                i += 8;
            }
            while (i < l) {
                int n = i / 8;
                int m = 1 << (7 - i % 8);
                if ((a1[n] & m) != (a2[n] & m)) return false;
                i++;
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SubNet)) return false;
            SubNet x = (SubNet)o;
            return
                prefix_length == x.prefix_length &&
                broadcast.equals(x.broadcast) &&
                address.equals(x.address);
        }

        @Override
        public int hashCode() {
            return address.hashCode();
        }

        @Override
        public String toString() {
            return address.getHostAddress() + "/" + prefix_length;
        }
    }

    private static class Slave {
        final InetAddress address;
        final int port;

        /* Time of last packet receiver from this slave */
        long last_packet_time;

        /* Time of last REQ_SLAVES packet received from this slave */
        long last_req_slaves_time;

        Slave(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public String toString() {
            return address.getHostAddress() + ":" + port;
        }
    }

    private static class AddressCacheItem {
        final String host;
        InetAddress address;
        long time_stamp;
        boolean used;

        AddressCacheItem(String host) {
            this.host = host;
        }
    }

    private static final HashMap<String,AddressCacheItem> addr_cache = new HashMap<String,AddressCacheItem>();
    private static boolean addr_request;

    private static LocalPeer local_peer;

    private DatagramSocket socket;
    private long last_master_packet_time;

    private final Thread timer_thread = new Thread() {
        public void run() {
            while (true) {
                try {
                    sleep(DATA_RETENTION_PERIOD / 4);
                    Protocol.invokeAndWait(new Runnable() {
                        public void run() {
                            refresh_timer();
                        }
                    });
                }
                catch (IllegalStateException x) {
                    // TCF event dispatch is shut down
                    return;
                }
                catch (Throwable x) {
                    log("Unhandled exception in TCF discovery timer thread", x);
                }
            }
        }
    };

    private Thread dns_lookup_thread = new Thread() {
        public void run() {
            while (true) {
                try {
                    long time;
                    HashSet<AddressCacheItem> set = null;
                    synchronized (addr_cache) {
                        if (!addr_request) addr_cache.wait(DATA_RETENTION_PERIOD);
                        time = System.currentTimeMillis();
                        for (Iterator<AddressCacheItem> i = addr_cache.values().iterator(); i.hasNext();) {
                            AddressCacheItem a = i.next();
                            if (a.time_stamp + DATA_RETENTION_PERIOD * 10 < time) {
                                if (a.used) {
                                    if (set == null) set = new HashSet<AddressCacheItem>();
                                    set.add(a);
                                }
                                else {
                                    i.remove();
                                }
                            }
                        }
                        addr_request = false;
                    }
                    if (set != null) {
                        for (AddressCacheItem a : set) {
                            InetAddress addr = null;
                            try {
                                addr = InetAddress.getByName(a.host);
                            }
                            catch (UnknownHostException x) {
                            }
                            synchronized (addr_cache) {
                                a.address = addr;
                                a.time_stamp = time;
                                a.used = false;
                            }
                        }
                    }
                }
                catch (Throwable x) {
                    log("Unhandled exception in TCF discovery DNS lookup thread", x);
                }
            }
        }
    };

    private final Thread input_thread = new Thread() {
        public void run() {
            for (;;) {
                DatagramSocket socket = LocatorService.this.socket;
                try {
                    final DatagramPacket p = new DatagramPacket(inp_buf, inp_buf.length);
                    socket.receive(p);
                    Protocol.invokeAndWait(new Runnable() {
                        public void run() {
                            handleDatagramPacket(p);
                        }
                    });
                }
                catch (IllegalStateException x) {
                    // TCF event dispatch is shutdown
                    return;
                }
                catch (Exception x) {
                    if (socket != LocatorService.this.socket) continue;
                    log("Cannot read from datagram socket at port " + socket.getLocalPort(), x);
                }
            }
        }
    };

    static {
        ServiceManager.addServiceProvider(new IServiceProvider() {

            public IService[] getLocalService(final IChannel channel) {
                channel.addCommandServer(locator, new IChannel.ICommandServer() {
                    public void command(IToken token, String name, byte[] data) {
                        locator.command((AbstractChannel)channel, token, name, data);
                    }
                });
                return new IService[]{ locator };
            }

            public IService getServiceProxy(IChannel channel, String service_name) {
                return null;
            }
        });
    }

    public LocatorService() {
        locator = this;
        try {
            loopback_addr = InetAddress.getByName(null);
            out_buf[0] = 'T';
            out_buf[1] = 'C';
            out_buf[2] = 'F';
            out_buf[3] = CONF_VERSION;
            out_buf[4] = 0;
            out_buf[5] = 0;
            out_buf[6] = 0;
            out_buf[7] = 0;
            try {
                socket = new DatagramSocket(DISCOVEY_PORT);
            }
            catch (SocketException x) {
                socket = new DatagramSocket();
            }
            socket.setBroadcast(true);
            input_thread.setName("TCF Locator Receiver");
            timer_thread.setName("TCF Locator Timer");
            dns_lookup_thread.setName("TCF Locator DNS Lookup");
            input_thread.setDaemon(true);
            timer_thread.setDaemon(true);
            dns_lookup_thread.setDaemon(true);
            input_thread.start();
            timer_thread.start();
            dns_lookup_thread.start();
            listeners.add(new LocatorListener() {

                public void peerAdded(IPeer peer) {
                    sendPeerInfo(peer, null, 0);
                }

                public void peerChanged(IPeer peer) {
                    sendPeerInfo(peer, null, 0);
                }

                public void peerHeartBeat(String id) {
                }

                public void peerRemoved(String id) {
                }
            });
            refreshSubNetList();
            sendPeersRequest(null, 0);
            sendAll(null, 0, null, System.currentTimeMillis());
        }
        catch (Exception x) {
            log("Cannot open UDP socket for TCF discovery protocol", x);
        }
    }

    public static LocalPeer getLocalPeer() {
        return local_peer;
    }

    public static LocatorListener[] getListeners() {
        return listeners.toArray(new LocatorListener[listeners.size()]);
    }

    public static void addPeer(AbstractPeer peer) {
        assert peers.get(peer.getID()) == null;
        if (peer instanceof LocalPeer) local_peer = (LocalPeer)peer;
        peers.put(peer.getID(), peer);
        peer.sendPeerAddedEvent();
    }

    public static void removePeer(AbstractPeer peer) {
        String id = peer.getID();
        assert peers.get(id) == peer;
        peers.remove(id);
        peer.sendPeerRemovedEvent();
    }

    private Map<String,Object> makeErrorReport(int code, String msg) {
        Map<String,Object> err = new HashMap<String,Object>();
        err.put(IErrorReport.ERROR_TIME, new Long(System.currentTimeMillis()));
        err.put(IErrorReport.ERROR_CODE, new Integer(code));
        err.put(IErrorReport.ERROR_FORMAT, msg);
        return err;
    }

    private void command(final AbstractChannel channel, final IToken token, String name, byte[] data) {
        try {
            if (name.equals("redirect")) {
                String peer_id = (String)JSON.parseSequence(data)[0];
                IPeer peer = peers.get(peer_id);
                if (peer == null) {
                    channel.sendResult(token, JSON.toJSONSequence(new Object[]{
                            makeErrorReport(IErrorReport.TCF_ERROR_UNKNOWN_PEER, "Unknown peer ID") }));
                    return;
                }
                channel.sendResult(token, JSON.toJSONSequence(new Object[]{ null }));
                if (peer instanceof LocalPeer) {
                    channel.sendEvent(Protocol.getLocator(), "Hello", JSON.toJSONSequence(
                            new Object[]{ channel.getLocalServices() }));
                    return;
                }
                new ChannelProxy(channel, peer.openChannel());
            }
            else if (name.equals("sync")) {
                channel.sendResult(token, null);
            }
            else if (name.equals("getPeers")) {
                int i = 0;
                Object[] arr = new Object[peers.size()];
                for (IPeer p : peers.values()) arr[i++] = p.getAttributes();
                channel.sendResult(token, JSON.toJSONSequence(new Object[]{ null, arr }));
            }
            else {
                channel.rejectCommand(token);
            }
        }
        catch (Throwable x) {
            channel.terminate(x);
        }
    }

    private void log(String msg, Throwable x) {
        // Don't report same error multiple times to avoid filling up the log file.
        if (error_log.get(msg) == null) {
            error_log.put(msg, x);
            Protocol.log(msg, x);
        }
    }

    private InetAddress getInetAddress(String host) {
        if (host == null || host.length() == 0) return null;
        synchronized (addr_cache) {
            AddressCacheItem i = addr_cache.get(host);
            if (i == null) {
                i = new AddressCacheItem(host);
                char ch = host.charAt(0);
                if (ch == '[' || ch == ':' || ch >= '0' && ch <= '9') {
                    try {
                        i.address = InetAddress.getByName(host);
                    }
                    catch (UnknownHostException e) {
                    }
                    i.time_stamp = System.currentTimeMillis();
                }
                else {
                    /* InetAddress.getByName() can cause long delay - delegate to background thread */
                    addr_request = true;
                    addr_cache.notify();
                }
                addr_cache.put(host, i);
            }
            i.used = true;
            return i.address;
        }
    }

    private void refresh_timer() {
        long time = System.currentTimeMillis();
        /* Cleanup slave table */
        if (slaves.size() > 0) {
            int i = 0;
            while (i < slaves.size()) {
                Slave s = slaves.get(i);
                if (s.last_packet_time + DATA_RETENTION_PERIOD < time) {
                    slaves.remove(i);
                }
                else {
                    i++;
                }
            }
        }
        /* Cleanup peers table */
        ArrayList<RemotePeer> stale_peers = null;
        for (IPeer p : peers.values()) {
            if (p instanceof RemotePeer) {
                RemotePeer r = (RemotePeer)p;
                if (r.getLastUpdateTime() + DATA_RETENTION_PERIOD < time) {
                    if (stale_peers == null) stale_peers = new ArrayList<RemotePeer>();
                    stale_peers.add(r);
                }
            }
        }
        if (stale_peers != null) {
            IChannel[] open_channels = TransportManager.getOpenChannels();
            HashSet<IPeer> connected_peers = new HashSet<IPeer>();
            for (IChannel c : open_channels) connected_peers.add(c.getRemotePeer());
            for (RemotePeer p : stale_peers) {
                if (!connected_peers.contains(p)) p.dispose();
            }
        }
        /* Try to become a master */
        if (socket.getLocalPort() != DISCOVEY_PORT && last_master_packet_time + DATA_RETENTION_PERIOD / 2 <= time) {
            DatagramSocket s0 = socket;
            DatagramSocket s1 = null;
            try {
                s1 = new DatagramSocket(DISCOVEY_PORT);
                s1.setBroadcast(true);
                socket = s1;
                s0.close();
            }
            catch (Throwable x) {
            }
        }
        refreshSubNetList();
        if (socket.getLocalPort() != DISCOVEY_PORT) {
            for (SubNet subnet : subnets) {
                addSlave(subnet.address, socket.getLocalPort(), time);
            }
        }
        sendAll(null, 0, null, time);
    }

    private Slave addSlave(InetAddress addr, int port, long timestamp) {
        for (Slave s : slaves) {
            if (s.port == port && s.address.equals(addr)) {
                if (s.last_packet_time < timestamp) s.last_packet_time = timestamp;
                return s;
            }
        }
        long time = System.currentTimeMillis();
        Slave s = new Slave(addr, port);
        s.last_packet_time = timestamp;
        slaves.add(s);
        sendPeersRequest(addr, port);
        sendAll(addr, port, s, time);
        sendSlaveInfo(s, time);
        return s;
    }

    private void refreshSubNetList() {
        HashSet<SubNet> set = new HashSet<SubNet>();
        try {
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
                NetworkInterface f = e.nextElement();
                /* TODO: Class InterfaceAddress does not exists in Java versions before 1.6.
                 * Fix the code below when support for old Java versions is not needed any more.
                 */
                try {
                    Method m0 = f.getClass().getMethod("getInterfaceAddresses");
                    for (Object ia : (List<?>)m0.invoke(f)) {
                        Method m1 = ia.getClass().getMethod("getNetworkPrefixLength");
                        Method m2 = ia.getClass().getMethod("getAddress");
                        Method m3 = ia.getClass().getMethod("getBroadcast");
                        int network_prefix_len = (Short)m1.invoke(ia);
                        InetAddress address = (InetAddress)m2.invoke(ia);
                        InetAddress broadcast = (InetAddress)m3.invoke(ia);
                        if (network_prefix_len <= 0) {
                            // Windows XP reports network prefix length 0
                            // for loopback interface when IP V6 is enabled.
                            // Is it bug or feature?
                            byte[] buf = address.getAddress();
                            if (buf.length == 4 && buf[0] == 127) {
                                network_prefix_len = 8;
                                buf[1] = buf[2] = buf[3] = (byte)255;
                                broadcast = InetAddress.getByAddress(buf);
                            }
                        }

                        if (address instanceof Inet4Address && network_prefix_len > 32) {
                            /*
                             * Workaround for JVM bug:
                             * InterfaceAddress.getNetworkPrefixLength() does not conform to Javadoc
                             * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6707289
                             *
                             * The bug shows up only when IPv6 is enabled.
                             * The bug is supposed to be fixed in Java 1.7.
                             */
                            // TODO: need a better way to get network prefix length on Java 1.6 VM
                            network_prefix_len = 24;
                        }

                        // TODO: discovery over IPv6

                        /* Create IPv6 broadcast address.
                         * The code does not work - commented out until fixed.
                        if (broadcast == null &&
                                address instanceof Inet6Address &&
                                !address.isAnyLocalAddress() &&
                                !address.isLinkLocalAddress() &&
                                !address.isMulticastAddress() &&
                                !address.isLoopbackAddress()) {
                            byte[] net = address.getAddress();
                            byte[] buf = new byte[16];
                            buf[0] = (byte)0xff; // multicast
                            buf[1] = (byte)0x32; // flags + scope
                            buf[2] = (byte)0x00; // reserved
                            buf[3] = (byte)network_prefix_len;
                            int n = (network_prefix_len + 7) / 8;
                            for (int i = 0; i < n; i++) buf[i + 4] = net[i];
                            broadcast = Inet6Address.getByAddress(null, buf);
                        }
                        */

                        if (network_prefix_len > 0 && address != null && broadcast != null) {
                            set.add(new SubNet(network_prefix_len, address, broadcast));
                        }
                    }
                }
                catch (Exception x) {
                    // Java 1.5 or older
                    // TODO: need a better way to get broadcast addresses on Java 1.5 VM
                    Enumeration<InetAddress> n = f.getInetAddresses();
                    while (n.hasMoreElements()) {
                        InetAddress addr = n.nextElement();
                        byte[] buf = addr.getAddress();
                        if (buf.length != 4) continue;
                        buf[3] = (byte)255;
                        try {
                            set.add(new SubNet(24, addr, InetAddress.getByAddress(buf)));
                        }
                        catch (UnknownHostException y) {
                        }
                    }
                }
            }
        }
        catch (SocketException x) {
            log("Cannot get list of network interfaces", x);
        }
        for (Iterator<SubNet> i = subnets.iterator(); i.hasNext();) {
            SubNet s = i.next();
            if (set.contains(s)) continue;
            i.remove();
        }
        for (Iterator<SubNet> i = set.iterator(); i.hasNext();) {
            SubNet s = i.next();
            if (subnets.contains(s)) continue;
            subnets.add(s);
        }
    }

    private byte[] getUTF8Bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        }
        catch (Exception x) {
            log("UTF-8 character encoder is not available", x);
            return s.getBytes();
        }
    }

    private boolean sendDatagramPacket(SubNet subnet, int size, InetAddress addr, int port) {
        try {
            if (addr == null) {
                addr = subnet.broadcast;
                port = DISCOVEY_PORT;
                for (Slave slave : slaves) {
                    sendDatagramPacket(subnet, size, slave.address, slave.port);
                }
            }
            if (!subnet.contains(addr)) return false;
            if (port == socket.getLocalPort() && addr.equals(subnet.address)) return false;
            socket.send(new DatagramPacket(out_buf, size, addr, port));
        }
        catch (Exception x) {
            log("Cannot send datagram packet to " + addr, x);
            return false;
        }
        return true;
    }

    private void sendPeersRequest(InetAddress addr, int port) {
        out_buf[4] = CONF_REQ_INFO;
        for (SubNet subnet : subnets) {
            sendDatagramPacket(subnet, 8, addr, port);
        }
    }

    private void sendPeerInfo(IPeer peer, InetAddress addr, int port) {
        Map<String,String> attrs = peer.getAttributes();
        InetAddress peer_addr = getInetAddress(attrs.get(IPeer.ATTR_IP_HOST));
        if (peer_addr == null) return;
        if (attrs.get(IPeer.ATTR_IP_PORT) == null) return;
        out_buf[4] = CONF_PEER_INFO;
        int i = 8;

        for (SubNet subnet : subnets) {
            if (peer instanceof RemotePeer) {
                if (socket.getLocalPort() != DISCOVEY_PORT) return;
                if (!subnet.address.equals(loopback_addr) && !subnet.address.equals(peer_addr)) continue;
            }
            if (!subnet.address.equals(loopback_addr)) {
                if (!subnet.contains(peer_addr)) continue;
            }
            if (i == 8) {
                StringBuffer sb = new StringBuffer(out_buf.length);
                for (String key : attrs.keySet()) {
                    sb.append(key);
                    sb.append('=');
                    sb.append(attrs.get(key));
                    sb.append((char)0);
                }
                byte[] bt = getUTF8Bytes(sb.toString());
                if (i + bt.length > out_buf.length) return;
                System.arraycopy(bt, 0, out_buf, i, bt.length);
                i += bt.length;
            }
            if (sendDatagramPacket(subnet, i, addr, port)) subnet.send_all_ok = true;
        }
    }

    private void sendEmptyPacket(InetAddress addr, int port) {
        out_buf[4] = CONF_SLAVES_INFO;
        for (SubNet subnet : subnets) {
            if (subnet.send_all_ok) continue;
            sendDatagramPacket(subnet, 8, addr, port);
        }
    }

    private void sendAll(InetAddress addr, int port, Slave sl, long time) {
        for (SubNet subnet : subnets) subnet.send_all_ok = false;
        for (IPeer peer : peers.values()) sendPeerInfo(peer, addr, port);
        if (addr != null && sl != null && sl.last_req_slaves_time + DATA_RETENTION_PERIOD >= time) {
            sendSlavesInfo(addr, port, time);
        }
        sendEmptyPacket(addr, port);
    }

    private void sendSlavesRequest(SubNet subnet, InetAddress addr, int port) {
        out_buf[4] = CONF_REQ_SLAVES;
        sendDatagramPacket(subnet, 8, addr, port);
    }

    private void sendSlaveInfo(Slave x, long time) {
        out_buf[4] = CONF_SLAVES_INFO;
        for (SubNet subnet : subnets) {
            if (!subnet.contains(x.address)) continue;
            int i = 8;
            String s = x.last_packet_time + ":" + x.port + ":" + x.address.getHostAddress();
            byte[] bt = getUTF8Bytes(s);
            System.arraycopy(bt, 0, out_buf, i, bt.length);
            i += bt.length;
            out_buf[i++] = 0;
            for (Slave y : slaves) {
                if (!subnet.contains(y.address)) continue;
                if (y.last_req_slaves_time + DATA_RETENTION_PERIOD < time) continue;
                sendDatagramPacket(subnet, i, y.address, y.port);
            }
        }
    }

    private void sendSlavesInfo(InetAddress addr, int port, long time) {
        out_buf[4] = CONF_SLAVES_INFO;
        for (SubNet subnet : subnets) {
            if (!subnet.contains(addr)) continue;
            int i = 8;
            for (Slave x : slaves) {
                if (x.last_packet_time + DATA_RETENTION_PERIOD < time) continue;
                if (x.port == port && x.address.equals(addr)) continue;
                if (!subnet.address.equals(loopback_addr)) {
                    if (!subnet.contains(x.address)) continue;
                }
                subnet.send_all_ok = true;
                String s = x.last_packet_time + ":" + x.port + ":" + x.address.getHostAddress();
                byte[] bt = getUTF8Bytes(s);
                if (i > 8 && i + bt.length >= PREF_PACKET_SIZE) {
                    sendDatagramPacket(subnet, i, addr, port);
                    i = 8;
                }
                System.arraycopy(bt, 0, out_buf, i, bt.length);
                i += bt.length;
                out_buf[i++] = 0;
            }
            if (i > 8) sendDatagramPacket(subnet, i, addr, port);
        }
    }

    private boolean isRemote(InetAddress address, int port) {
        if (port != socket.getLocalPort()) return true;
        for (SubNet s : subnets) {
            if (s.address.equals(address)) return false;
        }
        return true;
    }

    private void handleDatagramPacket(DatagramPacket p) {
        try {
            long time = System.currentTimeMillis();
            byte[] buf = p.getData();
            int len = p.getLength();
            if (len < 8) return;
            if (buf[0] != 'T') return;
            if (buf[1] != 'C') return;
            if (buf[2] != 'F') return;
            if (buf[3] != CONF_VERSION) return;
            int remote_port = p.getPort();
            InetAddress remote_address = p.getAddress();
            if (isRemote(remote_address, remote_port)) {
                Slave sl = null;
                if (remote_port != DISCOVEY_PORT) {
                    sl = addSlave(remote_address, remote_port, time);
                }
                switch (buf[4]) {
                case CONF_PEER_INFO:
                    handlePeerInfoPacket(p);
                    break;
                case CONF_REQ_INFO:
                    handleReqInfoPacket(p, sl, time);
                    break;
                case CONF_SLAVES_INFO:
                    handleSlavesInfoPacket(p);
                    break;
                case CONF_REQ_SLAVES:
                    handleReqSlavesPacket(p, sl, time);
                    break;
                }
                for (SubNet subnet : subnets) {
                    if (!subnet.contains(remote_address)) continue;
                    long delay = DATA_RETENTION_PERIOD / 3;
                    if (remote_port != DISCOVEY_PORT) delay = DATA_RETENTION_PERIOD / 3 * 2;
                    else if (!subnet.address.equals(remote_address)) delay = DATA_RETENTION_PERIOD / 2;
                    if (subnet.last_slaves_req_time + delay <= time) {
                        sendSlavesRequest(subnet, remote_address, remote_port);
                        subnet.last_slaves_req_time = time;
                    }
                    if (subnet.address.equals(remote_address) && remote_port == DISCOVEY_PORT) {
                        last_master_packet_time = time;
                    }
                }
            }
        }
        catch (Throwable x) {
            log("Invalid datagram packet received from " + p.getAddress(), x);
        }
    }

    private void handlePeerInfoPacket(DatagramPacket p) {
        try {
            Map<String,String> map = new HashMap<String,String>();
            String s = new String(p.getData(), 8, p.getLength() - 8, "UTF-8");
            int l = s.length();
            int i = 0;
            while (i < l) {
                int i0 = i;
                while (i < l && s.charAt(i) != '=' && s.charAt(i) != 0) i++;
                int i1 = i;
                if (i < l && s.charAt(i) == '=') i++;
                int i2 = i;
                while (i < l && s.charAt(i) != 0) i++;
                int i3 = i;
                if (i < l && s.charAt(i) == 0) i++;
                String key = s.substring(i0, i1);
                String val = s.substring(i2, i3);
                map.put(key, val);
            }
            String id = map.get(IPeer.ATTR_ID);
            if (id == null) throw new Exception("Invalid peer info: no ID");
            InetAddress peer_addr = getInetAddress(map.get(IPeer.ATTR_IP_HOST));
            if (peer_addr == null) return;
            for (SubNet subnet : subnets) {
                if (!subnet.contains(peer_addr)) continue;
                IPeer peer = peers.get(id);
                if (peer instanceof RemotePeer) {
                    ((RemotePeer)peer).updateAttributes(map);
                }
                else if (peer == null) {
                    new RemotePeer(map);
                }
                break;
            }
        }
        catch (Exception x) {
            log("Invalid datagram packet received from " + p.getAddress(), x);
        }
    }

    private void handleReqInfoPacket(DatagramPacket p, Slave sl, long time) {
        sendAll(p.getAddress(), p.getPort(), sl, time);
    }

    private void handleSlavesInfoPacket(DatagramPacket p) {
        try {
            String s = new String(p.getData(), 8, p.getLength() - 8, "UTF-8");
            int l = s.length();
            int i = 0;
            while (i < l) {
                int time0 = i;
                while (i < l&& s.charAt(i) != ':' && s.charAt(i) != 0) i++;
                int time1 = i;
                if (i < l && s.charAt(i) == ':') i++;
                int port0 = i;
                while (i < l&& s.charAt(i) != ':' && s.charAt(i) != 0) i++;
                int port1 = i;
                if (i < l && s.charAt(i) == ':') i++;
                int host0 = i;
                while (i < l && s.charAt(i) != 0) i++;
                int host1 = i;
                if (i < l && s.charAt(i) == 0) i++;
                int port = Integer.parseInt(s.substring(port0, port1));
                if (port != DISCOVEY_PORT) {
                    String host = s.substring(host0, host1);
                    InetAddress addr = getInetAddress(host);
                    if (addr != null) {
                        long time_now = System.currentTimeMillis();
                        long time = time0 != time1 ? Long.parseLong(s.substring(time0, time1)) : time_now;
                        if (time < time_now - 600000 || time > time_now + 600000) {
                            log("Invalid datagram packet received from " + p.getAddress(),
                                    new Exception("Invalid slave info timestamp: " + time));
                        }
                        else {
                            addSlave(addr, port, time);
                        }
                    }
                }
            }
        }
        catch (Exception x) {
            log("Invalid datagram packet received from " + p.getAddress(), x);
        }
    }

    private void handleReqSlavesPacket(DatagramPacket p, Slave sl, long time) {
        if (sl != null) sl.last_req_slaves_time = time;
        sendSlavesInfo(p.getAddress(), p.getPort(),  time);
    }

    /*----------------------------------------------------------------------------------*/

    public static LocatorService getLocator() {
        return locator;
    }

    public String getName() {
        return NAME;
    }

    public Map<String,IPeer> getPeers() {
        assert Protocol.isDispatchThread();
        return peers;
    }

    public IToken redirect(String peer_id, DoneRedirect done) {
        throw new Error("Channel redirect cannot be done on local peer");
    }

    public IToken sync(DoneSync done) {
        throw new Error("Channel sync cannot be done on local peer");
    }

    public void addListener(LocatorListener listener) {
        assert listener != null;
        assert Protocol.isDispatchThread();
        listeners.add(listener);
    }

    public void removeListener(LocatorListener listener) {
        assert Protocol.isDispatchThread();
        listeners.remove(listener);
    }
}
