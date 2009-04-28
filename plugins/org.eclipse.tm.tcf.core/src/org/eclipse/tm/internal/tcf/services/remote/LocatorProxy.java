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
package org.eclipse.tm.internal.tcf.services.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;


public class LocatorProxy implements ILocator {
    
    private final IChannel channel;
    private final Map<String,IPeer> peers = new HashMap<String,IPeer>();
    private final Collection<LocatorListener> listeners = new ArrayList<LocatorListener>();
    
    private boolean get_peers_done = false;
    
    private class Peer implements IPeer {
        
        private final IPeer parent;
        
        private final Map<String, String> attrs;
        
        Peer(IPeer parent, Map<String,String> attrs) {
            this.parent = parent;
            this.attrs = attrs;
        }

        public Map<String, String> getAttributes() {
            assert Protocol.isDispatchThread();
            return attrs;
        }

        public String getID() {
            assert Protocol.isDispatchThread();
            return attrs.get(ATTR_ID);
        }

        public String getName() {
            assert Protocol.isDispatchThread();
            return attrs.get(ATTR_NAME);
        }

        public String getOSName() {
            assert Protocol.isDispatchThread();
            return attrs.get(ATTR_OS_NAME);
        }

        public String getTransportName() {
            assert Protocol.isDispatchThread();
            return attrs.get(ATTR_TRANSPORT_NAME);
        }

        public IChannel openChannel() {
            assert Protocol.isDispatchThread();
            IChannel c = parent.openChannel();
            c.redirect(getID());
            return c;
        }
    };

    private final IChannel.IEventListener event_listener = new IChannel.IEventListener() {

        @SuppressWarnings("unchecked")
        public void event(String name, byte[] data) {
            try {
                Object[] args = JSON.parseSequence(data);
                if (name.equals("peerAdded")) {
                    assert args.length == 1;
                    IPeer peer = new Peer(channel.getRemotePeer(), (Map<String,String>)args[0]);
                    if (peers.get(peer.getID()) != null) {
                        Protocol.log("Invalid peerAdded event", new Error());
                        return;
                    }
                    peers.put(peer.getID(), peer);
                    for (LocatorListener l : listeners.toArray(new LocatorListener[listeners.size()])) {
                        try {
                            l.peerAdded(peer);
                        }
                        catch (Throwable x) {
                            Protocol.log("Unhandled exception in Locator listener", x);
                        }
                    }
                }
                else if (name.equals("peerChanged")) {
                    assert args.length == 1;
                    Map<String,String> m = (Map<String,String>)args[0];
                    if (m == null) throw new Error("Locator service: invalid peerChanged event - no peer ID");
                    IPeer peer = peers.get(m.get(IPeer.ATTR_ID));
                    if (peer == null) return;
                    peers.put(peer.getID(), peer);
                    for (LocatorListener l : listeners.toArray(new LocatorListener[listeners.size()])) {
                        try {
                            l.peerChanged(peer);
                        }
                        catch (Throwable x) {
                            Protocol.log("Unhandled exception in Locator listener", x);
                        }
                    }
                }
                else if (name.equals("peerRemoved")) {
                    assert args.length == 1;
                    String id = (String)args[0];
                    IPeer peer = peers.remove(id);
                    if (peer == null) return;
                    for (LocatorListener l : listeners.toArray(new LocatorListener[listeners.size()])) {
                        try {
                            l.peerRemoved(id);
                        }
                        catch (Throwable x) {
                            Protocol.log("Unhandled exception in Locator listener", x);
                        }
                    }
                }
                else if (name.equals("peerHeartBeat")) {
                    assert args.length == 1;
                    String id = (String)args[0];
                    IPeer peer = peers.get(id);
                    if (peer == null) return;
                    for (LocatorListener l : listeners.toArray(new LocatorListener[listeners.size()])) {
                        try {
                            l.peerHeartBeat(id);
                        }
                        catch (Throwable x) {
                            Protocol.log("Unhandled exception in Locator listener", x);
                        }
                    }
                }
                else {
                    throw new IOException("Locator service: unknown event: " + name);
                }
            }
            catch (Throwable x) {
                channel.terminate(x);
            }
        }
    };
    
    public LocatorProxy(IChannel channel) {
        this.channel = channel;
        channel.addEventListener(this, event_listener);
    }

    public String getName() {
        return NAME;
    }

    public Map<String,IPeer> getPeers() {
        return peers;
    }

    public IToken redirect(String peer_id, final DoneRedirect done) {
        return new Command(channel, this, "redirect", new Object[]{ peer_id }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                done.doneRedirect(token, error);
            }
        }.token;
    }

    public IToken sync(final DoneSync done) {
        return new Command(channel, this, "sync", null) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error != null) channel.terminate(error);
                done.doneSync(token);
            }
        }.token;
    }

    public void addListener(LocatorListener listener) {
        listeners.add(listener);
        if (!get_peers_done) {
            new Command(channel, this, "getPeers", null) {
                @SuppressWarnings("unchecked")
                @Override
                public void done(Exception error, Object[] args) {
                    if (error == null) {
                        assert args.length == 2;
                        error = toError(args[0]);
                    }
                    if (error != null) {
                        Protocol.log("Locator error", error);
                        return;
                    }
                    Collection<?> c = (Collection<?>)args[1];
                    if (c != null) {
                        for (Object o : c) {
                            Map<String,String> m = (Map<String,String>)o;
                            String id = m.get(IPeer.ATTR_ID);
                            if (peers.get(id) != null) continue;
                            IPeer peer = new Peer(channel.getRemotePeer(), m);
                            peers.put(id, peer);
                            for (LocatorListener l : listeners.toArray(new LocatorListener[listeners.size()])) {
                                try {
                                    l.peerAdded(peer);
                                }
                                catch (Throwable x) {
                                    Protocol.log("Unhandled exception in Locator listener", x);
                                }
                            }
                        }
                    }
                }
            };
            get_peers_done = true;
        }
    }

    public void removeListener(LocatorListener listener) {
        listeners.remove(listener);
    }
}
