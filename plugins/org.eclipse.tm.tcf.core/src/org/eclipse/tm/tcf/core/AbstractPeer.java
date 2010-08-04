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
package org.eclipse.tm.tcf.core;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.tm.internal.tcf.core.RemotePeer;
import org.eclipse.tm.internal.tcf.core.TransportManager;
import org.eclipse.tm.internal.tcf.services.local.LocatorService;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.tcf.services.ILocator.LocatorListener;

/**
 * Abstract implementation of IPeer interface.
 * Objects of this class are stored in Locator service peer table.
 * The class implements sending notification events to Locator listeners.
 * See TransientPeer for IPeer objects that are not stored in the Locator table.
 */
public class AbstractPeer extends TransientPeer {

    private long last_heart_beat_time;

    public AbstractPeer(Map<String,String> attrs) {
        super(attrs);
        assert Protocol.isDispatchThread();
        String id = getID();
        assert id != null;
        Map<String,IPeer> peers = LocatorService.getLocator().getPeers();
        if (peers.get(id) instanceof RemotePeer) {
            ((RemotePeer)peers.get(id)).dispose();
        }
        assert peers.get(id) == null;
        peers.put(id, this);
        sendPeerAddedEvent();
    }

    public void dispose() {
        assert Protocol.isDispatchThread();
        String id = getID();
        assert id != null;
        Map<String,IPeer> peers = LocatorService.getLocator().getPeers();
        assert peers.get(id) == this;
        peers.remove(id);
        sendPeerRemovedEvent();
    }

    void onChannelTerminated() {
        // A channel to this peer was terminated:
        // not delaying next heart beat helps client to recover much faster.
        last_heart_beat_time = 0;
    }

    public void updateAttributes(Map<String,String> attrs) {
        boolean equ = true;
        assert attrs.get(ATTR_ID).equals(rw_attrs.get(ATTR_ID));
        for (Iterator<String> i = rw_attrs.keySet().iterator(); i.hasNext();) {
            String key = i.next();
            if (!rw_attrs.get(key).equals(attrs.get(key))) {
                equ = false;
                break;
            }
        }
        for (Iterator<String> i = attrs.keySet().iterator(); i.hasNext();) {
            String key = i.next();
            if (!attrs.get(key).equals(rw_attrs.get(key))) {
                equ = false;
                break;
            }
        }
        long time = System.currentTimeMillis();
        if (!equ) {
            rw_attrs.clear();
            rw_attrs.putAll(attrs);
            for (LocatorListener l : LocatorService.getListeners()) {
                try {
                    l.peerChanged(this);
                }
                catch (Throwable x) {
                    Protocol.log("Unhandled exception in Locator listener", x);
                }
            }
            try {
                Object[] args = { rw_attrs };
                Protocol.sendEvent(ILocator.NAME, "peerChanged", JSON.toJSONSequence(args));
            }
            catch (IOException x) {
                Protocol.log("Locator: failed to send 'peerChanged' event", x);
            }
            last_heart_beat_time = time;
        }
        else if (last_heart_beat_time + ILocator.DATA_RETENTION_PERIOD / 4 < time) {
            for (LocatorListener l : LocatorService.getListeners()) {
                try {
                    l.peerHeartBeat(attrs.get(ATTR_ID));
                }
                catch (Throwable x) {
                    Protocol.log("Unhandled exception in Locator listener", x);
                }
            }
            try {
                Object[] args = { rw_attrs.get(ATTR_ID) };
                Protocol.sendEvent(ILocator.NAME, "peerHeartBeat", JSON.toJSONSequence(args));
            }
            catch (IOException x) {
                Protocol.log("Locator: failed to send 'peerHeartBeat' event", x);
            }
            last_heart_beat_time = time;
        }
    }

    private void sendPeerAddedEvent() {
        for (LocatorListener l : LocatorService.getListeners()) {
            try {
                l.peerAdded(this);
            }
            catch (Throwable x) {
                Protocol.log("Unhandled exception in Locator listener", x);
            }
        }
        try {
            Object[] args = { rw_attrs };
            Protocol.sendEvent(ILocator.NAME, "peerAdded", JSON.toJSONSequence(args));
        }
        catch (IOException x) {
            Protocol.log("Locator: failed to send 'peerAdded' event", x);
        }
        last_heart_beat_time = System.currentTimeMillis();
    }

    private void sendPeerRemovedEvent() {
        for (LocatorListener l : LocatorService.getListeners()) {
            try {
                l.peerRemoved(rw_attrs.get(ATTR_ID));
            }
            catch (Throwable x) {
                Protocol.log("Unhandled exception in Locator listener", x);
            }
        }
        try {
            Object[] args = { rw_attrs.get(ATTR_ID) };
            Protocol.sendEvent(ILocator.NAME, "peerRemoved", JSON.toJSONSequence(args));
        }
        catch (IOException x) {
            Protocol.log("Locator: failed to send 'peerRemoved' event", x);
        }
    }

    public IChannel openChannel() {
        return TransportManager.openChannel(this);
    }
}
