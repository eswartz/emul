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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.internal.tcf.core.ReadOnlyMap;
import org.eclipse.tm.internal.tcf.core.Transport;
import org.eclipse.tm.internal.tcf.services.local.LocatorService;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;


public abstract class AbstractPeer implements IPeer {

    private final Map<String, String> ro_attrs;
    private final Map<String, String> rw_attrs;
    
    public AbstractPeer(Map<String, String> attrs) {
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

    protected Map<String, String> getAttributesStorage() {
        assert Protocol.isDispatchThread();
        return rw_attrs;
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
}
