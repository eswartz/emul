/*******************************************************************************
 * Copyright (c) 2007-2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.tm.tcf.core.AbstractChannel;
import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.core.ChannelTCP;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.ITransportProvider;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;


public class TransportManager {

    private static final Collection<AbstractChannel> channels =
        new LinkedList<AbstractChannel>();
    private static final Collection<Protocol.ChannelOpenListener> listeners = 
        new LinkedList<Protocol.ChannelOpenListener>();
    private static final HashMap<String,ITransportProvider> transports =
        new HashMap<String,ITransportProvider>();
    
    static {
        addTransportProvider(new ITransportProvider() {

            public String getName() {
                return "TCP";
            }

            public IChannel openChannel(IPeer peer) {
                assert getName().equals(peer.getTransportName());
                Map<String,String> attrs = peer.getAttributes();
                String host = attrs.get(IPeer.ATTR_IP_HOST);
                String port = attrs.get(IPeer.ATTR_IP_PORT);
                if (host == null) throw new Error("No host name");
                if (port == null) throw new Error("No port number");
                return new ChannelTCP(peer, host, Integer.parseInt(port), false);
            }
        });
        
        addTransportProvider(new ITransportProvider() {

            public String getName() {
                return "SSL";
            }

            public IChannel openChannel(IPeer peer) {
                assert getName().equals(peer.getTransportName());
                Map<String,String> attrs = peer.getAttributes();
                String host = attrs.get(IPeer.ATTR_IP_HOST);
                String port = attrs.get(IPeer.ATTR_IP_PORT);
                if (host == null) throw new Error("No host name");
                if (port == null) throw new Error("No port number");
                return new ChannelTCP(peer, host, Integer.parseInt(port), true);
            }
        });
        
        addTransportProvider(new ITransportProvider() {

            public String getName() {
                return "Loop";
            }

            public IChannel openChannel(IPeer peer) {
                assert getName().equals(peer.getTransportName());
                return new ChannelLoop(peer);
            }
        });
    }
    
    public static void addTransportProvider(ITransportProvider transport) {
        String name = transport.getName();
        assert name != null;
        if (transports.get(name) != null) throw new Error("Already registered: " + name);
        transports.put(name, transport);
    }
    
    public static void removeTransportProvider(ITransportProvider transport) {
        String name = transport.getName();
        assert name != null;
        if (transports.get(name) == transport) transports.remove(name);
    }
    
    public static IChannel openChannel(IPeer peer) {
        String name = peer.getTransportName();
        if (name == null) throw new Error("No transport name");
        ITransportProvider transport = transports.get(name);
        if (transport == null) throw new Error("Unknown transport name: " + name);
        return transport.openChannel(peer);
    }

    public static void channelOpened(final AbstractChannel channel) {
        assert !channels.contains(channel);
        channels.add(channel);
        Protocol.ChannelOpenListener[] array = listeners.toArray(new Protocol.ChannelOpenListener[listeners.size()]);
        for (Protocol.ChannelOpenListener l : array) {
            try {
                l.onChannelOpen(channel);
            }
            catch (Throwable x) {
                Protocol.log("Exception in channel listener", x);
            }
        }
    }

    public static void channelClosed(final AbstractChannel channel, final Throwable x) {
        assert channels.contains(channel);
        channels.remove(channel);
    }
    
    public static IChannel[] getOpenChannels() {
        return channels.toArray(new IChannel[channels.size()]);
    }
    
    public static void addChanelOpenListener(Protocol.ChannelOpenListener listener) {
        assert listener != null;
        listeners.add(listener);
    }

    public static void removeChanelOpenListener(Protocol.ChannelOpenListener listener) {
        listeners.remove(listener);
    }

    public static void peerDisposed(AbstractPeer peer) {
        Exception error = null;
        Collection<AbstractChannel> bf = new ArrayList<AbstractChannel>(channels);
        for (Iterator<AbstractChannel> i = bf.iterator(); i.hasNext();) {
            AbstractChannel c = i.next();
            if (c.getRemotePeer() != peer) continue;
            if (error == null) error = new Exception("Peer is disposed");
            c.terminate(error);
        }
    }

    /**
     * Transmit TCF event message.
     * The message is sent to all open communication channels – broadcasted.
     * 
     * This is internal API, TCF clients should use {@code org.eclipse.tm.tcf.protocol.Protocol}.
     */
    public static void sendEvent(String service_name, String event_name, byte[] data) {
        for (Iterator<AbstractChannel> i = channels.iterator(); i.hasNext();) {
            AbstractChannel channel = i.next();
            IService s = channel.getLocalService(service_name);
            if (s != null) channel.sendEvent(s, event_name, data);
        }
    }
    
    /**
     * Call back after TCF messages sent by this host up to this moment are delivered
     * to their intended targets. This method is intended for synchronization of messages
     * across multiple channels.
     * 
     * Note: Cross channel synchronization can reduce performance and throughput.
     * Most clients don't need cross channel synchronization and should not call this method. 
     *  
     * @param done will be executed by dispatch thread after communication 
     * messages are delivered to corresponding targets.
     * 
     * This is internal API, TCF clients should use {@code org.eclipse.tm.tcf.protocol.Protocol}.
     */
    public static void sync(final Runnable done) {
        final Set<IToken> set = new HashSet<IToken>();
        ILocator.DoneSync done_sync = new ILocator.DoneSync() {
            public void doneSync(IToken token) {
                assert set.contains(token);
                set.remove(token);
                if (set.isEmpty()) done.run();
            }
        };
        for (Iterator<AbstractChannel> i = channels.iterator(); i.hasNext();) {
            AbstractChannel channel = i.next();
            ILocator s = channel.getRemoteService(ILocator.class);
            if (s != null) set.add(s.sync(done_sync));
        }
        if (set.isEmpty()) Protocol.invokeLater(done);
    }
}
