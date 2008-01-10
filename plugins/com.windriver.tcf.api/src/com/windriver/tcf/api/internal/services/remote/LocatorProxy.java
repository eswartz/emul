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
package com.windriver.tcf.api.internal.services.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.windriver.tcf.api.core.Command;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IPeer;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.protocol.JSON;
import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.services.ILocator;

public class LocatorProxy implements ILocator {
    
    private final IChannel channel;
    private final Map<String,IPeer> peers = new HashMap<String,IPeer>();
    private final Collection<LocatorListener> listeners = new ArrayList<LocatorListener>();
    
    private class Peer implements IPeer {
        
        private final Map<String, String> attrs;
        
        Peer(Map<String,String> attrs) {
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
            IChannel c = channel.getRemotePeer().openChannel();
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
                    IPeer peer = new Peer((Map<String,String>)args[0]);
                    peers.put(peer.getID(), peer);
                    for (Iterator<LocatorListener> i = listeners.iterator(); i.hasNext();) {
                        i.next().peerAdded(peer);
                    }
                }
                else if (name.equals("peerChanged")) {
                    assert args.length == 1;
                    Map<String,String> m = (Map<String,String>)args[0];
                    if (m == null) throw new Error("Locator service: invalid peerChanged event - no peer ID");
                    IPeer peer = peers.get(m.get(IPeer.ATTR_ID));
                    if (peer == null) throw new Error("Invalid peerChanged event: unknown peer ID");
                    for (Iterator<LocatorListener> i = listeners.iterator(); i.hasNext();) {
                        i.next().peerChanged(peer);
                    }
                }
                else if (name.equals("peerRemoved")) {
                    assert args.length == 1;
                    String id = (String)args[0];
                    IPeer peer = peers.get(id);
                    if (peer == null) throw new Error("Locator service: invalid peerRemoved event - unknown peer ID");
                    for (Iterator<LocatorListener> i = listeners.iterator(); i.hasNext();) {
                        i.next().peerRemoved(id);
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
                    assert args.length == 2;
                    error = toError(args[0], args[1]);
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
    }

    public void removeListener(LocatorListener listener) {
        listeners.remove(listener);
    }
}
