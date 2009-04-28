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
package org.eclipse.tm.tcf.core;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
 */
public class AbstractPeer implements IPeer {

    private final Map<String, String> ro_attrs;
    private final Map<String, String> rw_attrs;
    
    private long last_heart_beat_time;
    
    public AbstractPeer(Map<String,String> attrs) {
        assert Protocol.isDispatchThread();
        if (attrs != null) {
            rw_attrs = new HashMap<String, String>(attrs);
        }
        else {
            rw_attrs = new HashMap<String, String>();
        }
        ro_attrs = Collections.unmodifiableMap(rw_attrs);
        assert getID() != null;
        LocatorService.addPeer(this);
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
    
    public void sendPeerAddedEvent() {
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

    public void sendPeerRemovedEvent() {
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

    public void dispose() {
        assert Protocol.isDispatchThread();
        TransportManager.peerDisposed(this);
        LocatorService.removePeer(this);
    }

    public Map<String,String> getAttributes() {
        assert Protocol.isDispatchThread();
        return ro_attrs;
    }

    public String getID() {
        assert Protocol.isDispatchThread();
        return ro_attrs.get(ATTR_ID);
    }

    public String getName() {
        assert Protocol.isDispatchThread();
        return ro_attrs.get(ATTR_NAME);
    }

    public String getOSName() {
        assert Protocol.isDispatchThread();
        return ro_attrs.get(ATTR_OS_NAME);
    }

    public String getTransportName() {
        assert Protocol.isDispatchThread();
        return ro_attrs.get(ATTR_TRANSPORT_NAME);
    }
    
    public IChannel openChannel() {
        return TransportManager.openChannel(this);
    }
}
