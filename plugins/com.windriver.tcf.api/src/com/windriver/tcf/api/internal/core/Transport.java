/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.tcf.api.internal.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import com.windriver.tcf.api.Activator;
import com.windriver.tcf.api.core.AbstractChannel;
import com.windriver.tcf.api.core.AbstractPeer;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IService;
import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.services.ILocator;

public class Transport {

    private static final Collection<AbstractChannel> channels =
        new LinkedList<AbstractChannel>();
    private static final Collection<Protocol.ChannelOpenListener> listeners = 
        new LinkedList<Protocol.ChannelOpenListener>();

    public static void channelOpened(final AbstractChannel channel) {
        channels.add(channel);
        for (Protocol.ChannelOpenListener l : listeners) {
            try {
                l.onChannelOpen(channel);
            }
            catch (Throwable x) {
                Activator.log("Exception in channel listener", x);
            }
        }
    }

    public static void channelClosed(final AbstractChannel channel, final Throwable x) {
        channels.remove(channel);
    }
    
    public static IChannel[] getOpenChannels() {
        return channels.toArray(new IChannel[channels.size()]);
    }
    
    public static void addChanalOpenListener(Protocol.ChannelOpenListener listener) {
        listeners.add(listener);
    }

    public static void removeChanalOpenListener(Protocol.ChannelOpenListener listener) {
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
     * This is internal API, TCF clients should use {@code com.windriver.tcf.api.protocol.Protocol}.
     */
    public static void sendEvent(String service_name, String event_name, byte[] data) {
        for (Iterator<AbstractChannel> i = channels.iterator(); i.hasNext();) {
            AbstractChannel channel = i.next();
            IService s = channel.getRemoteService(service_name);
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
     * This is internal API, TCF clients should use {@code com.windriver.tcf.api.protocol.Protocol}.
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
