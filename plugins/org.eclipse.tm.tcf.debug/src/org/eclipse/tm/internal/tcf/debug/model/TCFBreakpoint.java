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

import java.math.BigInteger;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.tm.internal.tcf.debug.Activator;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;


public class TCFBreakpoint extends Breakpoint {

    public static final String MARKER_TYPE = "org.eclipse.tm.tcf.debug.breakpoint.marker";

    private static long last_id = 0;

    private static String createNewID() {
        assert Protocol.isDispatchThread();
        long id = System.currentTimeMillis();
        if (id <= last_id) id = last_id + 1;
        last_id = id;
        return Long.toHexString(id);
    }

    private String text;

    public TCFBreakpoint() {
    }

    public TCFBreakpoint(final IResource resource, Map<String,Object> props) throws DebugException {
        props.put(IBreakpoints.PROP_ID, createNewID());
        final Map<String,Object> m = Activator.getBreakpointsModel().toMarkerAttributes(props);
        final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                IMarker marker = resource.createMarker(MARKER_TYPE);
                setMarker(marker);
                marker.setAttributes(m);
                DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(TCFBreakpoint.this);
            }
        };
        final ISchedulingRule rule = getMarkerRule(resource);
        Job job = new Job("Add Breakpoint") {  //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    TCFBreakpoint.this.run(getMarkerRule(resource), runnable);
                }
                catch (CoreException e) {
                    return e.getStatus();
                }
                return Status.OK_STATUS;
            }
        };
        job.setRule(rule);
        job.schedule();
    }

    public String getModelIdentifier() {
        return ITCFConstants.ID_TCF_DEBUG_MODEL;
    }

    public String getText() {
        if (text == null) {
            IMarker marker = getMarker();
            if (marker == null) return null;
            StringBuffer bf = new StringBuffer();
            String address = marker.getAttribute(
                    ITCFConstants.ID_TCF_DEBUG_MODEL + '.' + IBreakpoints.PROP_LOCATION, null);
            if (address != null && address.length() > 0) {
                bf.append("PC = ");
                BigInteger n = new BigInteger(address, 10);
                String s = n.toString(16);
                int l = Math.min(s.length(), 8);
                bf.append("0x00000000".substring(0, 10 - l));
                bf.append(s);
            }
            else {
                String id = marker.getAttribute(
                        ITCFConstants.ID_TCF_DEBUG_MODEL + '.' + IBreakpoints.PROP_ID, null);
                bf.append("BP");
                bf.append(id);
            }
            text = bf.toString();
        }
        return text;
    }
}
