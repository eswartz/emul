/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.tcf.debug.model.ITCFBreakpointListener;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpointsModel;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpointsStatus;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModelManager;
import org.eclipse.tm.tcf.services.IBreakpoints;

/**
 * This class monitors breakpoints status on TCF debug targets and calls ICBreakpoint.incrementInstallCount() or
 * ICBreakpoint.decrementInstallCount() when breakpoint status changes.
 */
class TCFBreakpointStatusListener {
    
    private class BreakpointListener implements ITCFBreakpointListener {
        
        private final TCFBreakpointsStatus status;
        private final Set<ICBreakpoint> installed;
        
        BreakpointListener(TCFLaunch launch) {
            status = launch.getBreakpointsStatus();
            status.addListener(this);
            bp_listeners.put(launch, this);
            installed = new HashSet<ICBreakpoint>();
            for (String id : status.getStatusIDs()) breakpointStatusChanged(id);
        }
        
        public void breakpointStatusChanged(String id) {
            IBreakpoint bp = bp_model.getBreakpoint(id);
            if (bp instanceof ICBreakpoint) {
                boolean ok = false;
                ICBreakpoint cbp = (ICBreakpoint)bp;
                Map<String,Object> map = status.getStatus(id);
                if (map != null) {
                    @SuppressWarnings("unchecked")
                    Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)map.get(IBreakpoints.STATUS_INSTANCES);
                    if (list != null) {
                        for (Map<String,Object> m : list) {
                            if (m.get(IBreakpoints.INSTANCE_ERROR) == null) ok = true;
                        }
                    }
                }
                if (ok) {
                    if (installed.add(cbp)) incrementInstallCount(cbp);
                }
                else {
                    if (installed.remove(cbp)) decrementInstallCount(cbp);
                }
            }
        }

        public void breakpointRemoved(String id) {
            IBreakpoint bp = bp_model.getBreakpoint(id);
            if (bp instanceof ICBreakpoint) {
                ICBreakpoint cbp = (ICBreakpoint)bp;
                if (installed.remove(cbp)) decrementInstallCount(cbp);
            }
        }
        
        void dispose() {
            for (ICBreakpoint cbp : installed) decrementInstallCount(cbp);
            installed.clear();
        }
        
        private void incrementInstallCount(final ICBreakpoint cbp) {
            asyncExec(new Runnable() {
                public void run() {
                    try {
                        cbp.incrementInstallCount();
                    }
                    catch (Exception x) {
                        Activator.log(x);
                    }
                }
            });
        }
        
        private void decrementInstallCount(final ICBreakpoint cbp) {
            asyncExec(new Runnable() {
                public void run() {
                    try {
                        cbp.decrementInstallCount();
                    }
                    catch (Exception x) {
                        Activator.log(x);
                    }
                }
            });
        }
        
        private void asyncExec(Runnable r) {
            synchronized (Device.class) {
                Display display = Display.getDefault();
                if (display != null && !display.isDisposed()) {
                    display.asyncExec(r);
                }
            }
        }
    }
    
    private final TCFModelManager.ModelManagerListener launch_listener = new TCFModelManager.ModelManagerListener() {

        public void onConnected(TCFLaunch launch, TCFModel model) {
            assert bp_listeners.get(launch) == null;
            new BreakpointListener(launch);
        }

        public void onDisconnected(TCFLaunch launch, TCFModel model) {
            bp_listeners.remove(launch).dispose();
        }
    };
    
    private final TCFModelManager model_manager;
    private final TCFBreakpointsModel bp_model;
    private final Map<TCFLaunch,BreakpointListener> bp_listeners;;

    TCFBreakpointStatusListener() {
        bp_model = TCFBreakpointsModel.getBreakpointsModel();
        model_manager = TCFModelManager.getModelManager();
        model_manager.addListener(launch_listener);
        bp_listeners = new HashMap<TCFLaunch,BreakpointListener>();
    }
    
    void dispose() {
        model_manager.removeListener(launch_listener);
        for (BreakpointListener l : bp_listeners.values()) l.dispose();
        bp_listeners.clear();
    }
}
