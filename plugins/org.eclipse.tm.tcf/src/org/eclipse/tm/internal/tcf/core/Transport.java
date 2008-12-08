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
package org.eclipse.tm.internal.tcf.core;

import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;


public class Transport {

    private static final Collection<AbstractChannel> channels =
        new LinkedList<AbstractChannel>();
    private static final Collection<Protocol.ChannelOpenListener> listeners = 
        new LinkedList<Protocol.ChannelOpenListener>();
    
    
    public static IChannel openChannel(IPeer peer) {
        String transport = peer.getTransportName();
        if (transport == null) throw new Error("Unknown transport");
        if (transport.equals("Loop")) {
            return new ChannelLoop(peer);
        }
        if (transport.equals("TCP")) {
            Map<String,String> attrs = peer.getAttributes();
            String host = attrs.get(IPeer.ATTR_IP_HOST);
            String port = attrs.get(IPeer.ATTR_IP_PORT);
            if (host == null) throw new Error("No host name");
            if (port == null) throw new Error("No port number");
            return new ChannelTCP(peer, host, Integer.parseInt(port));
        }
        throw new Error("Unknown transport name: " + transport);
    }

    public static void channelOpened(final AbstractChannel channel) {
        assert !channels.contains(channel);
        channels.add(channel);
        for (Protocol.ChannelOpenListener l : listeners) {
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
        listeners.add(listener);
    }

    public static void removeChanelOpenListener(Protocol.ChannelOpenListener listener) {
        listeners.remove(listener);
    }

    public static void peerDisposed(AbstractPeer peer) {
        Collection<AbstractChannel> bf = new ArrayList<AbstractChannel>(channels);
        for (Iterator<AbstractChannel> i = bf.iterator(); i.hasNext();) {
            AbstractChannel c = i.next();
            if (c.getRemotePeer() != peer) continue;
            c.close();
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
