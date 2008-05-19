/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.internal.tcf.core.LocalPeer;
import org.eclipse.tm.internal.tcf.core.RemotePeer;
import org.eclipse.tm.tcf.Activator;
import org.eclipse.tm.tcf.core.AbstractChannel;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;


/**
 * Locator service uses transport layer to search
 * for peers and to collect and maintain up-to-date
 * data about peer’s attributes and capabilities (services).
 */
public class LocatorService implements ILocator {

    private static LocatorService locator;
    private static final Map<String,IPeer> peers = new HashMap<String,IPeer>();
    private static final Collection<LocatorListener> listeners = new ArrayList<LocatorListener>();

    private static LocalPeer local_peer;
    
    private DatagramSocket socket;
    
    private Thread output_thread = new Thread() {
        public void run() {
            for (;;) {
                Protocol.invokeAndWait(new Runnable() {
                    public void run() {
                        sendPeerInfoRequest();
                    }
                });
                try {
                    sleep(5 * 1000);
                }
                catch (InterruptedException x) {
                    break;
                }
            }
        }
    };
    
    private Thread input_thread = new Thread() {
        // TODO: implement discovery in slave mode (not needed for Windows)
        public void run() {
            for (;;) {
                try {
                    byte[] buf = new byte[0x1000];
                    final DatagramPacket p = new DatagramPacket(buf, buf.length);
                    socket.receive(p);
                    Protocol.invokeAndWait(new Runnable() {
                        public void run() {
                            handleDatagramPacket(p);
                        }
                    });
                }
                catch (Exception x) {
                    Activator.log("Cannot read from datagram socket", x);
                    break;
                }
            }
        }
    };
    
    public LocatorService() {
        locator = this;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            input_thread.setName("TCF Locator Receiver");
            output_thread.setName("TCF Locator Transmitter");
            input_thread.start();
            output_thread.start();
        }
        catch (Exception x) {
            Activator.log("Cannot create datagram socket", x);
        }
    }

    public static LocalPeer getLocalPeer() {
        return local_peer;
    }
    
    public static Collection<LocatorListener> getListeners() {
        return listeners;
    }

    public static void addPeer(IPeer peer) {
        assert peers.get(peer.getID()) == null;
        if (peer instanceof LocalPeer) local_peer = (LocalPeer)peer;
        peers.put(peer.getID(), peer);
        for (LocatorListener l : listeners) l.peerAdded(peer);
    }

    public static void removePeer(IPeer peer) {
        String id = peer.getID();
        assert peers.get(id) == peer;
        peers.remove(id);
        for (LocatorListener l : listeners) l.peerRemoved(id);
    }

    public static void channelStarted(final AbstractChannel channel) {
        channel.addCommandServer(locator, new IChannel.ICommandServer() {
            public void command(IToken token, String name, byte[] data) {
                locator.command(channel, token, name, data);
            }
        });
    }

    private void command(AbstractChannel channel, IToken token, String name, byte[] data) {
        try {
            if (name.equals("redirect")) {
                // String peer_id = (String)JSON.parseSequence(data)[0];
                // TODO: perform local ILocator.redirect
                channel.sendResult(token, JSON.toJSONSequence(new Object[]{
                        new Integer(0), null }));
            }
            else if (name.equals("sync")) {
                channel.sendResult(token, null);
            }
            else if (name.equals("publishPeer")) {
                // TODO: handle "publishPeer" command from discovery master
                channel.sendResult(token, JSON.toJSONSequence(new Object[]{
                        new Integer(0), null, new Integer(0) }));
            }
            else {
                channel.terminate(new Exception("Illegal command: " + name));
            }
        }
        catch (Throwable x) {
            channel.terminate(x);
        }
    }
    
    private void sendPeerInfoRequest() {
        try {
            byte[] buf = new byte[8];
            int i = 0;
            buf[i++] = 'T';
            buf[i++] = 'C';
            buf[i++] = 'F';
            buf[i++] = '1';
            buf[i++] = CONF_REQ_INFO;
            buf[i++] = 0;
            buf[i++] = 0;
            buf[i++] = 0;
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
                NetworkInterface f = e.nextElement();
                /* TODO: Class InterfaceAddress does not exists in Java versions before 1.6.
                 * When support for old Java versions is not needed any more,
                 * the code below should be replaced with:
                 *   for (InterfaceAddress ia : f.getInterfaceAddresses()) {
                 *       socket.send(new DatagramPacket(buf, buf.length, ia.getBroadcast(), 1534));
                 *   }
                 */
                try {
                    Method m0 = f.getClass().getMethod("getInterfaceAddresses");
                    for (Object ia : (List<?>)m0.invoke(f)) {
                        Method m1 = ia.getClass().getMethod("getBroadcast");
                        socket.send(new DatagramPacket(buf, buf.length, (InetAddress)m1.invoke(ia), 1534));
                    }
                }
                catch (Exception x) {
                    Enumeration<InetAddress> n = f.getInetAddresses();
                    while (n.hasMoreElements()) {
                        InetAddress ina = n.nextElement();
                        byte[] adr = ina.getAddress();
                        if (adr.length != 4) {
                            // TODO: Support IPv6
                            // System.out.println("Don't support IPv6: " + ina);
                            continue;
                        }
                        /* Since we don't know actual broadcast address,
                         * lets try different combinations.
                         * Hopefully one of them will work.
                         */
                        int h = adr[0] & 0xff;
                        if (h >= 1 && h <= 127 && h != 38) {
                            adr[3] = (byte)255;
                            socket.send(new DatagramPacket(buf, buf.length,
                                    InetAddress.getByAddress(null, adr), 1534));
                            adr[2] = (byte)255;
                            socket.send(new DatagramPacket(buf, buf.length,
                                    InetAddress.getByAddress(null, adr), 1534));
                            adr[1] = (byte)255;
                        }
                        else if (h >= 128 && h <= 191) {
                            adr[3] = (byte)255;
                            socket.send(new DatagramPacket(buf, buf.length,
                                    InetAddress.getByAddress(null, adr), 1534));
                            adr[2] = (byte)255;
                        }
                        else {
                            adr[3] = (byte)(adr[3] | 0x0f);
                            socket.send(new DatagramPacket(buf, buf.length,
                                    InetAddress.getByAddress(null, adr), 1534));
                            adr[3] = (byte)(adr[3] | 0x01f);
                            socket.send(new DatagramPacket(buf, buf.length,
                                    InetAddress.getByAddress(null, adr), 1534));
                            adr[3] = (byte)(adr[3] | 0x03f);
                            socket.send(new DatagramPacket(buf, buf.length,
                                    InetAddress.getByAddress(null, adr), 1534));
                            adr[3] = (byte)(adr[3] | 0x07f);
                            socket.send(new DatagramPacket(buf, buf.length,
                                    InetAddress.getByAddress(null, adr), 1534));
                            adr[3] = (byte)255;
                        }
                        socket.send(new DatagramPacket(buf, buf.length,
                                InetAddress.getByAddress(null, adr), 1534));
                    }
                }
            }
        }
        catch (Exception x) {
            Activator.log("Cannot send datagram packet", x);
        }
    }
    
    private void handleDatagramPacket(DatagramPacket p) {
        try {
            byte[] buf = p.getData();
            int len = p.getLength();
            if (len < 8) return;
            if (buf[0] != 'T') return;
            if (buf[1] != 'C') return;
            if (buf[2] != 'F') return;
            if (buf[3] != '1') return;
            switch (buf[4]) {
            case CONF_PEER_INFO:
                handlePeerInfoPacket(p);
                break;
            case CONF_REQ_INFO:
                handleReqInfoPacket(p);
                break;
            }
        }
        catch (Throwable x) {
            Activator.log("Invalid datagram packet received", x);
        }
    }
    
    private void handlePeerInfoPacket(DatagramPacket p) throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        String s = new String(p.getData(), 8, p.getLength() - 8, "UTF8");
        int len = s.length();
        int i = 0;
        while (i < len) {
            int i0 = i;
            while (i < len && s.charAt(i) != '=' && s.charAt(i) != 0) i++;
            int i1 = i;
            if (i < len && s.charAt(i) == '=') i++;
            int i2 = i;
            while (i < len && s.charAt(i) != 0) i++;
            int i3 = i;
            if (i < len && s.charAt(i) == 0) i++;
            String key = s.substring(i0, i1);
            String val = s.substring(i2, i3);
            map.put(key, val);
        }
        String id = map.get(IPeer.ATTR_ID);
        if (id == null) throw new Exception("Invalid peer info: no ID");
        IPeer peer = peers.get(id);
        if (peer instanceof RemotePeer) {
            ((RemotePeer)peer).updateAttributes(map);
        }
        else {
            new RemotePeer(map);
        }
    }

    private void handleReqInfoPacket(DatagramPacket p) {
        byte[] buf = p.getData();
        int len = p.getLength();
        // TODO: handleReqInfoPacket()
    }

    /*----------------------------------------------------------------------------------*/

    /*
     * Return local instance of Locator service
     */
    public static LocatorService getLocator() {
        return locator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.tcf.protocol.ILocator#getName()
     */
    public String getName() {
        return NAME;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.tcf.protocol.ILocator#getPeers()
     */
    public Map<String,IPeer> getPeers() {
        return peers;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.tcf.protocol.ILocator#redirect()
     */
    public IToken redirect(String peer_id, DoneRedirect done) {
        throw new Error("Channel redirect cannot be done on local peer");
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.tcf.protocol.ILocator#sync()
     */
    public IToken sync(DoneSync done) {
        throw new Error("Channel sync cannot be done on local peer");
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.tcf.protocol.ILocator#addListener(org.eclipse.tm.tcf.protocol.ILocator.Listener)
     */
    public void addListener(LocatorListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.tcf.protocol.ILocator#removeListener(org.eclipse.tm.tcf.protocol.ILocator.Listener)
     */
    public void removeListener(LocatorListener listener) {
        listeners.remove(listener);
    }
}
