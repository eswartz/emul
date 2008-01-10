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
package com.windriver.debug.tcf.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.model.IBreakpoint;

import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.services.IBreakpoints;

public class TCFBreakpointsStatus {
    
    private final IBreakpoints service;
    private final Map<String,Map<String,Object>> status = new HashMap<String,Map<String,Object>>();
    private final Set<ITCFBreakpointListener> listeners = new HashSet<ITCFBreakpointListener>();
    
    private static final Map<String,Object> status_not_supported = new HashMap<String,Object>();
    
    static {
        status_not_supported.put(IBreakpoints.STATUS_ERROR, "Not supported");
    }
    
    TCFBreakpointsStatus(TCFLaunch launch) {
        assert Protocol.isDispatchThread();
        service = launch.getChannel().getRemoteService(IBreakpoints.class);
        if (service != null) {
            service.addListener(new IBreakpoints.BreakpointsListener() {

                public void breakpointStatusChanged(String id, Map<String, Object> status) {
                    assert Protocol.isDispatchThread();
                    TCFBreakpointsStatus.this.status.put(id, status);
                    for (Iterator<ITCFBreakpointListener> i = listeners.iterator(); i.hasNext();) {
                        i.next().breakpointStatusChanged(id);
                    }
                }
            });
        }
    }
    
    public Map<String, Object> getStatus(String id) {
        assert id != null;
        assert Protocol.isDispatchThread();
        if (service == null) return status_not_supported;
        return status.get(id);
    }

    public Map<String, Object> getStatus(IBreakpoint bp) {
        if (!bp.getModelIdentifier().equals(ITCFConstants.ID_TCF_DEBUG_MODEL)) return status_not_supported;
        IMarker marker = bp.getMarker();
        if (marker == null) return null;
        return getStatus(marker.getAttribute(
                ITCFConstants.ID_TCF_DEBUG_MODEL + '.' + IBreakpoints.PROP_ID, null));
    }
    
    public void addListener(ITCFBreakpointListener listener) {
        assert Protocol.isDispatchThread();
        listeners.add(listener);
    }
    
    public void removeListener(ITCFBreakpointListener listener) {
        assert Protocol.isDispatchThread();
        listeners.remove(listener);
    }
}
