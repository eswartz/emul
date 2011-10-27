/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CAddressBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CFunctionBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CLineBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CWatchpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.tm.internal.tcf.debug.model.ITCFBreakpointListener;
import org.eclipse.tm.internal.tcf.debug.model.ITCFConstants;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpoint;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpointsModel;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpointsStatus;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModelManager;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;

/**
 * This class monitors breakpoints status on TCF debug targets and calls ICBreakpoint.incrementInstallCount() or
 * ICBreakpoint.decrementInstallCount() when breakpoint status changes.
 */
@SuppressWarnings("restriction")
class TCFBreakpointStatusListener {

    /** Ref count attribute for foreign breakpoints */
    private static final String ATTR_REFCOUNT = "org.eclipse.tm.tcf.cdt.refcount";
    /** TCF breakpoint ID attribute */
    private static final String ATTR_TCF_ID = ITCFConstants.ID_TCF_DEBUG_MODEL + '.' + IBreakpoints.PROP_ID;

    private class BreakpointListener implements ITCFBreakpointListener {

        private final TCFBreakpointsStatus status;
        private final Map<String,ICBreakpoint> installed = new HashMap<String,ICBreakpoint>();
        private final Set<String> foreign = new HashSet<String>();
        private final Set<String> deleted = new HashSet<String>();

        BreakpointListener(TCFLaunch launch) {
            status = launch.getBreakpointsStatus();
            status.addListener(this);
            bp_listeners.put(launch, this);
            for (String id : status.getStatusIDs()) breakpointStatusChanged(id);
        }

        public void breakpointStatusChanged(String id) {
            IBreakpoint bp = bp_model.getBreakpoint(id);
            updateInstallCount(id, bp);
            if (bp == null) createOrUpdateBreakpoint(id);
        }

        private void updateInstallCount(String id, IBreakpoint bp) {
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
                if (ok && installed.get(id) == null) {
                    installed.put(id, cbp);
                    incrementInstallCount(cbp);
                }
                if (!ok && installed.get(id) == cbp) {
                    installed.remove(id);
                    decrementInstallCount(cbp);
                }
            }
        }

        public void breakpointRemoved(String id) {
            ICBreakpoint cbp = installed.remove(id);
            if (cbp != null) {
                decrementInstallCount(cbp);
            }
            if (foreign.remove(id)) {
                deleteTransientBreakpoint(id);
            }
        }

        public void breakpointChanged(String id) {
            createOrUpdateBreakpoint(id);
        }

        void dispose() {
            for (ICBreakpoint cbp : installed.values()) {
                decrementInstallCount(cbp);
            }
            installed.clear();
            for (String id : foreign) {
                deleteTransientBreakpoint(id);
            }
            foreign.clear();
        }

        private void incrementInstallCount(final ICBreakpoint cbp) {
            Job job = new WorkspaceJob("Increment Install Count") {
                @Override
                public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                    try {
                        cbp.incrementInstallCount();
                    }
                    catch (CoreException e) {
                        // ignore expected race condition with marker deletion
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setRule(cbp.getMarker().getResource());
            job.setPriority(Job.SHORT);
            job.setSystem(true);
            job.schedule();
        }

        private void decrementInstallCount(final ICBreakpoint cbp) {
            Job job = new WorkspaceJob("Decrement Install Count") {
                @Override
                public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                    try {
                        cbp.decrementInstallCount();
                    }
                    catch (CoreException e) {
                        // ignore expected race condition with marker deletion
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setRule(cbp.getMarker().getResource());
            job.setPriority(Job.SHORT);
            job.setSystem(true);
            job.schedule();
        }

        private void createOrUpdateBreakpoint(final String id) {
            Map<String,Object> properties = status.getProperties(id);
            if (properties == null) return;
            if (bp_model.isLocal(properties)) return;
            final boolean create = foreign.add(id);
            final Map<String, Object> markerAttrs = bp_model.toMarkerAttributes(properties);
            markerAttrs.put(IBreakpoint.PERSISTED, Boolean.FALSE);
            markerAttrs.put(IMarker.TRANSIENT, Boolean.TRUE);
            Job job = new WorkspaceJob("Create Breakpoint Marker") {
                @Override
                public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                    if (deleted.remove(id)) return Status.OK_STATUS;
                    IBreakpoint[] bps = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
                    for (IBreakpoint bp : bps) {
                        IMarker marker = bp.getMarker();
                        if (marker == null) continue;
                        if (id.equals(TCFBreakpointsModel.getBreakpointID(bp))) {
                            if (create) {
                                int cnt = marker.getAttribute(ATTR_REFCOUNT, 0) + 1;
                                marker.setAttribute(ATTR_REFCOUNT, cnt);
                            }
                            else {
                                // source handle should not change
                                markerAttrs.remove(ICBreakpoint.SOURCE_HANDLE);
                                updateMarkerAttributes(markerAttrs, marker);
                            }
                            return Status.OK_STATUS;
                        }
                    }
                    if (!create) return Status.OK_STATUS;
                    markerAttrs.put(ATTR_REFCOUNT, 1);
                    IBreakpoint x = null;
                    IResource resource = ResourcesPlugin.getWorkspace().getRoot();
                    if (markerAttrs.get(ICWatchpoint.EXPRESSION) != null) {
                        x = new CWatchpoint(resource, markerAttrs, true);
                    }
                    else if (markerAttrs.get(ICLineBreakpoint.ADDRESS) != null) {
                        x = new CAddressBreakpoint(resource, markerAttrs, true);
                    }
                    else if (markerAttrs.get(ICLineBreakpoint.FUNCTION) != null) {
                        x = new CFunctionBreakpoint(resource, markerAttrs, true);
                    }
                    else if (markerAttrs.get(ICBreakpoint.SOURCE_HANDLE) != null &&
                            markerAttrs.get(IMarker.LINE_NUMBER) != null) {
                        x = new CLineBreakpoint(resource, markerAttrs, true);
                    }
                    else {
                        x = new TCFBreakpoint(resource, markerAttrs);
                    }
                    final IBreakpoint bp = x;
                    Protocol.invokeLater(new Runnable() {
                        public void run() {
                            updateInstallCount(id, bp);
                        }
                    });
                    return Status.OK_STATUS;
                }

                private void updateMarkerAttributes(Map<String, Object> markerAttrs, IMarker marker) throws CoreException {
                    List<String> keys = new ArrayList<String>(markerAttrs.size());
                    List<Object> values = new ArrayList<Object>(markerAttrs.size());
                    Map<?,?> oldAttrs = marker.getAttributes();
                    for (Map.Entry<?,?> entry : markerAttrs.entrySet()) {
                        String key = (String) entry.getKey();
                        Object newVal = entry.getValue();
                        Object oldVal = oldAttrs.remove(key);
                        if (oldVal == null || !oldVal.equals(newVal)) {
                            keys.add(key);
                            values.add(newVal);
                        }
                    }
                    if (keys.size() != 0) {
                        String[] keyArr = (String[]) keys.toArray(new String[keys.size()]);
                        Object[] valueArr = (Object[]) values.toArray(new Object[values.size()]);
                        marker.setAttributes(keyArr, valueArr);
                    }
                }
            };
            job.setRule(getBreakpointAccessRule());
            job.setPriority(Job.SHORT);
            job.setSystem(true);
            job.schedule();
        }

        private void deleteTransientBreakpoint(final String id) {
            Job job = new WorkspaceJob("Destroy Breakpoint Marker") {
                @Override
                public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                    IBreakpoint[] bps = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
                    for (IBreakpoint bp : bps) {
                        if (bp.isPersisted()) continue;
                        IMarker marker = bp.getMarker();
                        if (marker == null) continue;
                        if (id.equals(marker.getAttribute(ATTR_TCF_ID, null))) {
                            int cnt = marker.getAttribute(ATTR_REFCOUNT, 0) - 1;
                            if (cnt > 0) {
                                marker.setAttribute(ATTR_REFCOUNT, cnt);
                            }
                            else {
                                bp.delete();
                            }
                            return Status.OK_STATUS;
                        }
                    }
                    // Since breakpoint object is created by a another background job after reading data from remote peer,
                    // this job can be running before the job that creates the object.
                    // We need to remember ID of the breakpoint that became obsolete before it was fully created.
                    deleted.add(id);
                    return Status.OK_STATUS;
                }
            };
            job.setRule(getBreakpointAccessRule());
            job.setPriority(Job.SHORT);
            job.setSystem(true);
            job.schedule();
        }

        private ISchedulingRule getBreakpointAccessRule() {
            IResource resource = ResourcesPlugin.getWorkspace().getRoot();
            ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRuleFactory().markerRule(resource);
            if (rule == null) {
                // In Eclipse 3.6.2, markerRule() always returns null,
                // causing race condition, a lot of crashes and corrupted data.
                // Using modifyRule() instead.
                rule = ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(resource);
            }
            return rule;
        }
    }

    private final TCFModelManager.ModelManagerListener launch_listener = new TCFModelManager.ModelManagerListener() {

        public void onConnected(TCFLaunch launch, TCFModel model) {
            assert bp_listeners.get(launch) == null;
            if (launch.getBreakpointsStatus() != null) new BreakpointListener(launch);
        }

        public void onDisconnected(TCFLaunch launch, TCFModel model) {
            BreakpointListener l = bp_listeners.remove(launch);
            if (l != null) l.dispose();
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
        // handle already connected launches
        for (ILaunch launch : DebugPlugin.getDefault().getLaunchManager().getLaunches()) {
            if (launch instanceof TCFLaunch) {
                TCFLaunch tcfLaunch = (TCFLaunch) launch;
                if (!tcfLaunch.isDisconnected() && !tcfLaunch.isConnecting()) {
                    launch_listener.onConnected(tcfLaunch, model_manager.getModel(tcfLaunch));
                }
            }
        }
    }

    void dispose() {
        model_manager.removeListener(launch_listener);
        for (BreakpointListener l : bp_listeners.values()) l.dispose();
        bp_listeners.clear();
    }
}
