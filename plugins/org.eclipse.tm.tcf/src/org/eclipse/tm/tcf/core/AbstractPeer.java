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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.tm.internal.tcf.core.ReadOnlyMap;
import org.eclipse.tm.internal.tcf.core.Transport;
import org.eclipse.tm.internal.tcf.services.local.LocatorService;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator.LocatorListener;


public abstract class AbstractPeer implements IPeer {

    private final Map<String, String> ro_attrs;
    private final Map<String, String> rw_attrs;
    
    public AbstractPeer(Map<String, String> attrs) {
        assert Protocol.isDispatchThread();
        if (attrs != null) {
            rw_attrs = new HashMap<String, String>(attrs);
        }
        else {
            rw_attrs = new HashMap<String, String>();
        }
        ro_attrs = new ReadOnlyMap<String, String>(rw_attrs);
        assert getID() != null;
        LocatorService.addPeer(this);
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
        Collection<LocatorListener> listeners = LocatorService.getListeners();
        if (!equ) {
            rw_attrs.clear();
            rw_attrs.putAll(attrs);
            for (LocatorListener l : listeners) l.peerChanged(this);
        }
        else {
            for (LocatorListener l : listeners) l.peerHeartBeat(attrs.get(ATTR_ID));
        }
    }

    public void dispose() {
        assert Protocol.isDispatchThread();
        Transport.peerDisposed(this);
        LocatorService.removePeer(this);
    }

    public Map<String, String> getAttributes() {
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
        return Transport.openChannel(this);
    }
}
