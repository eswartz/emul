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
package org.eclipse.tm.internal.tcf.debug.model;

import java.util.Map;

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
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.tm.internal.tcf.debug.Activator;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;


public class TCFBreakpoint extends Breakpoint {

    public static final String MARKER_TYPE = "org.eclipse.tm.tcf.debug.breakpoint.marker";

    private static final String[] attr_names = {
        TCFBreakpointsModel.ATTR_ADDRESS, "address",
        TCFBreakpointsModel.ATTR_FUNCTION, "location",
        TCFBreakpointsModel.ATTR_EXPRESSION, "expression",
        TCFBreakpointsModel.ATTR_CONDITION, "condition",
        TCFBreakpointsModel.ATTR_CONTEXTNAMES, "scope (names)",
        TCFBreakpointsModel.ATTR_CONTEXTIDS, "scope (IDs)",
        TCFBreakpointsModel.ATTR_EXE_PATHS, "scope (modules)",
        TCFBreakpointsModel.ATTR_STOP_GROUP, "stop group",
    };

    private static long last_id = 0;

    private static String createNewID() {
        assert Protocol.isDispatchThread();
        long id = System.currentTimeMillis();
        if (id <= last_id) id = last_id + 1;
        last_id = id;
        return Long.toHexString(id);
    }

    public static TCFBreakpoint createFromMarkerAttributes(Map<String,Object> attrs) throws CoreException {
        assert !Protocol.isDispatchThread();
        assert attrs.get(TCFBreakpointsModel.ATTR_ID) != null;
        TCFBreakpoint bp = new TCFBreakpoint();
        IResource resource = ResourcesPlugin.getWorkspace().getRoot();
        IMarker marker = resource.createMarker(MARKER_TYPE);
        bp.setMarker(marker);
        marker.setAttributes(attrs);
        DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(bp);
        return bp;
    }

    public static TCFBreakpoint createFromTCFProperties(Map<String,Object> props) {
        assert Protocol.isDispatchThread();
        if (props.get(IBreakpoints.PROP_ID) == null) props.put(IBreakpoints.PROP_ID, createNewID());
        final TCFBreakpoint bp = new TCFBreakpoint();
        final Map<String,Object> m = Activator.getBreakpointsModel().toMarkerAttributes(props);
        final IResource resource = ResourcesPlugin.getWorkspace().getRoot();
        final ISchedulingRule rule = bp.getMarkerRule(resource);
        Job job = new WorkspaceJob("Add Breakpoint") {  //$NON-NLS-1$
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                IMarker marker = resource.createMarker(MARKER_TYPE);
                bp.setMarker(marker);
                marker.setAttributes(m);
                DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(bp);
                return Status.OK_STATUS;
            }
        };
        job.setRule(rule);
        job.setPriority(Job.SHORT);
        job.setSystem(true);
        job.schedule();
        return bp;
    }

    public TCFBreakpoint() {
    }

    @Override
    public void setEnabled(boolean b) throws CoreException {
        if (!Activator.getBreakpointsModel().isLocal(getMarker())) return;
        super.setEnabled(b);
    }

    public String getModelIdentifier() {
        return ITCFConstants.ID_TCF_DEBUG_MODEL;
    }

    public String getText() {
        IMarker marker = getMarker();
        if (marker == null) return null;
        StringBuffer bf = new StringBuffer();
        for (int i = 0; i < attr_names.length; i += 2) {
            String s = marker.getAttribute(attr_names[i], null);
            if (s == null || s.length() == 0) continue;
            bf.append('[');
            bf.append(attr_names[i + 1]);
            bf.append(": ");
            bf.append(s);
            bf.append(']');
        }
        if (bf.length() == 0) {
            String id = marker.getAttribute(
                    ITCFConstants.ID_TCF_DEBUG_MODEL + '.' + IBreakpoints.PROP_ID, null);
            bf.append(id);
        }
        return bf.toString();
    }

    public void notifyStatusChaged() throws CoreException {
        IMarker marker = getMarker();
        if (marker == null) return;
        int cnt = 0;
        String status = marker.getAttribute(TCFBreakpointsModel.ATTR_STATUS, null);
        if (status != null) cnt = Integer.parseInt(status);
        setAttribute(TCFBreakpointsModel.ATTR_STATUS, Integer.toString(cnt + 1));
    }
}
