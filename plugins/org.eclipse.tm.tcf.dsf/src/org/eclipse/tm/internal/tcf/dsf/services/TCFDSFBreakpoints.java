/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.dsf.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.tm.internal.tcf.debug.model.ITCFBreakpointListener;
import org.eclipse.tm.internal.tcf.debug.model.ITCFConstants;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpointsModel;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.dsf.Activator;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.osgi.framework.BundleContext;

public class TCFDSFBreakpoints extends AbstractDsfService implements org.eclipse.dd.dsf.debug.service.IBreakpoints {

    private class BreakpointDMC extends AbstractDMContext implements IBreakpointDMContext {

        final String id;
        final IBreakpoint bp;
        final TCFDataCache<Map<String,Object>> status;
        final Set<IBreakpointsTargetDMContext> targets;

        boolean disposed;

        public BreakpointDMC(IDsfService service, final String id, IBreakpoint bp) {
            super(service, new IDMContext[0]);
            this.id = id;
            this.bp = bp;
            cache.put(id, this);
            status = new TCFDataCache<Map<String,Object>>(channel) {
                @Override
                public boolean startDataRetrieval() {
                    assert command == null;
                    assert !disposed;
                    if (tcf_bpt_service == null) {
                        reset(null);
                        return true;
                    }
                    command = tcf_bpt_service.getStatus(id, new org.eclipse.tm.tcf.services.IBreakpoints.DoneGetStatus() {
                        public void doneGetStatus(IToken token, Exception err, Map<String,Object> status) {
                            set(token, err, status);
                        }
                    });
                    return false;
                }
            };
            targets = new HashSet<IBreakpointsTargetDMContext>();
        }

        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) && ((BreakpointDMC)other).id.equals(id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        void dispose() {
            assert !disposed;
            cache.remove(id);
            for (IBreakpointsTargetDMContext t : targets.toArray(
                    new IBreakpointsTargetDMContext[targets.size()])) onRemoved(t);
            assert targets.isEmpty();
            disposed = true;
        }

        void onAdded(final IBreakpointsTargetDMContext t) {
            targets.add(t);
            IBreakpointsAddedEvent e = new IBreakpointsAddedEvent() {
                public IBreakpointsTargetDMContext getDMContext() {
                    return t;
                }
                public IBreakpointDMContext[] getBreakpoints() {
                    return new IBreakpointDMContext[]{ BreakpointDMC.this };
                }
            };
            getSession().dispatchEvent(e, getProperties());
        }

        void onUpdated(final IBreakpointsTargetDMContext t) {
            assert targets.contains(t);
            IBreakpointsUpdatedEvent e = new IBreakpointsUpdatedEvent() {
                public IBreakpointsTargetDMContext getDMContext() {
                    return t;
                }
                public IBreakpointDMContext[] getBreakpoints() {
                    return new IBreakpointDMContext[]{ BreakpointDMC.this };
                }
            };
            getSession().dispatchEvent(e, getProperties());
        }

        void onRemoved(final IBreakpointsTargetDMContext t) {
            targets.remove(t);
            IBreakpointsRemovedEvent e = new IBreakpointsRemovedEvent() {
                public IBreakpointsTargetDMContext getDMContext() {
                    return t;
                }
                public IBreakpointDMContext[] getBreakpoints() {
                    return new IBreakpointDMContext[]{ BreakpointDMC.this };
                }
            };
            getSession().dispatchEvent(e, getProperties());
        }
    }

    private class BreakpointData implements IBreakpointDMData {

        final IBreakpoint bp;
        final Map<String,Object> attrs;
        final Map<String,Object> status;
        final String file;

        @SuppressWarnings("unchecked")
        BreakpointData(IBreakpoint bp, Map<String,Object> status) throws CoreException, IOException {
            this.bp = bp;
            this.status = status;
            attrs = bp.getMarker().getAttributes();
            IResource resource = bp.getMarker().getResource();
            if (resource == ResourcesPlugin.getWorkspace().getRoot()) {
                file = null;
            }
            else {
                IPath p = resource.getRawLocation();
                if (p == null) file = null;
                else file = p.toFile().getCanonicalPath();
            }
        }

        public IBreakpoint getPlatformBreakpoint() {
            return bp;
        }

        public Map<String,Object> getStatus() {
            return status;
        }

        @SuppressWarnings("unchecked")
        public IAddress[] getAddresses() {
            if (status == null) return null;
            Collection<Map<String,Object>> arr = (Collection<Map<String,Object>>)status.get(IBreakpoints.STATUS_INSTANCES);
            if (arr == null) return null;
            int cnt = 0;
            for (Map<String,Object> m : arr) {
                if (m.get(IBreakpoints.INSTANCE_ADDRESS) != null) cnt++;
            }
            IAddress[] res = new IAddress[cnt];
            int pos = 0;
            for (Map<String,Object> m : arr) {
                Number n = (Number)m.get(IBreakpoints.INSTANCE_ADDRESS);
                if (n != null) res[pos++] = new TCFAddress(n);
            }
            return res;
        }

        public String getBreakpointType() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getCondition() {
            return (String)attrs.get(ITCFConstants.ID_TCF_DEBUG_MODEL + '.' + IBreakpoints.PROP_CONDITION);
        }

        public String getExpression() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getFileName() {
            return file;
        }

        public String getFunctionName() {
            // TODO Auto-generated method stub
            return null;
        }

        public int getIgnoreCount() {
            Integer count = (Integer)attrs.get(ITCFConstants.ID_TCF_DEBUG_MODEL + '.' + IBreakpoints.PROP_IGNORECOUNT);
            if (count != null) return count.intValue();
            return 0;
        }

        public int getLineNumber() {
            Integer line = (Integer)attrs.get(IMarker.LINE_NUMBER);
            if (line != null) return line.intValue();
            return 0;
        }

        public boolean isEnabled() {
            Boolean enabled = (Boolean)attrs.get(IBreakpoint.ENABLED);
            return enabled != null && enabled.booleanValue() && bp_manager.isEnabled();
        }
    }

    private final ITCFBreakpointListener bp_listener = new ITCFBreakpointListener() {

        @SuppressWarnings("unchecked")
        public void breakpointStatusChanged(String id) {
            final BreakpointDMC dmc = cache.get(id);
            if (dmc != null) {
                TCFDSFRunControl rc = getServicesTracker().getService(TCFDSFRunControl.class);
                Map<String,Object> map = launch.getBreakpointsStatus().getStatus(dmc.id);
                dmc.status.reset(map);
                Set<IBreakpointsTargetDMContext> add_targets = new HashSet<IBreakpointsTargetDMContext>();
                Set<IBreakpointsTargetDMContext> rem_targets = new HashSet<IBreakpointsTargetDMContext>();
                if (map != null) {
                    Collection<Map<String,Object>> arr = (Collection<Map<String,Object>>)map.get(IBreakpoints.STATUS_INSTANCES);
                    if (arr != null) {
                        for (Map<String,Object> m : arr) {
                            String ctx_id = (String)m.get(IBreakpoints.INSTANCE_CONTEXT);
                            if (ctx_id != null) add_targets.add(rc.getContext(ctx_id));
                        }
                    }
                }
                for (IBreakpointsTargetDMContext t : dmc.targets) {
                    if (add_targets.contains(t)) {
                        dmc.onUpdated(t);
                        add_targets.remove(t);
                    }
                    else {
                        rem_targets.add(t);
                    }
                }
                for (IBreakpointsTargetDMContext t : rem_targets) dmc.onRemoved(t);
                for (IBreakpointsTargetDMContext t : add_targets) dmc.onAdded(t);
            }
        }

        public void breakpointRemoved(String id) {
            final BreakpointDMC dmc = cache.get(id);
            if (dmc != null) dmc.dispose();
        }
    };

    private final TCFLaunch launch;
    private final IChannel channel;
    private final org.eclipse.tm.tcf.services.IBreakpoints tcf_bpt_service;
    private final Map<String,BreakpointDMC> cache = new HashMap<String,BreakpointDMC>();
    private final IBreakpointManager bp_manager = DebugPlugin.getDefault().getBreakpointManager();

    public TCFDSFBreakpoints(DsfSession session, TCFLaunch launch, final RequestMonitor monitor) {
        super(session);
        this.launch = launch;
        channel = launch.getChannel();
        launch.getBreakpointsStatus().addListener(bp_listener);
        tcf_bpt_service = channel.getRemoteService(org.eclipse.tm.tcf.services.IBreakpoints.class);
        initialize(new RequestMonitor(getExecutor(), monitor) {
            @Override
            protected void handleSuccess() {
                String[] class_names = {
                        org.eclipse.dd.dsf.debug.service.IBreakpoints.class.getName(),
                        TCFDSFBreakpoints.class.getName()
                };
                register(class_names, new Hashtable<String,String>());
                monitor.done();
            }
        });
    }

    @Override
    public void shutdown(RequestMonitor monitor) {
        unregister();
        super.shutdown(monitor);
    }

    @Override
    protected BundleContext getBundleContext() {
        return Activator.getBundleContext();
    }

    public void getBreakpoints(IBreakpointsTargetDMContext ctx, DataRequestMonitor<IBreakpointDMContext[]> rm) {
        TCFBreakpointsModel m = TCFBreakpointsModel.getBreakpointsModel();
        IBreakpoint[] arr = bp_manager.getBreakpoints(ITCFConstants.ID_TCF_DEBUG_MODEL);
        ArrayList<IBreakpointDMContext> l = new ArrayList<IBreakpointDMContext>();
        if (arr != null && arr.length == 0) {
            for (IBreakpoint bp : arr) {
                if (m.isSupported(channel, bp)) {
                    IMarker marker = bp.getMarker();
                    String id = marker.getAttribute(ITCFConstants.ID_TCF_DEBUG_MODEL +
                            '.' + org.eclipse.tm.tcf.services.IBreakpoints.PROP_ID, (String)null);
                    if (id != null) {
                        BreakpointDMC c = cache.get(id);
                        if (c == null) c = new BreakpointDMC(this, id, bp);
                        l.add(c);
                    }
                }
            }
        }
        rm.setData(l.toArray(new IBreakpointDMContext[l.size()]));
        rm.done();
    }

    public void getBreakpointDMData(final IBreakpointDMContext dmc, final DataRequestMonitor<IBreakpointDMData> rm) {
        if (dmc instanceof BreakpointDMC) {
            BreakpointDMC bp = (BreakpointDMC)dmc;
            if (!bp.status.validate()) {
                bp.status.wait(new Runnable() {
                    public void run() {
                        getBreakpointDMData(dmc, rm);
                    }
                });
                return;
            }
            if (bp.status.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", bp.status.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            try {
                rm.setData(new BreakpointData(bp.bp, bp.status.getData()));
            }
            catch (Exception x) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", x)); //$NON-NLS-1$

            }
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null));  //$NON-NLS-1$
        }
        rm.done();
    }

    @SuppressWarnings("unchecked")
    public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
        if (dmc instanceof BreakpointDMC) {
            getBreakpointDMData((BreakpointDMC)dmc, (DataRequestMonitor<IBreakpointDMData>)rm);
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null));  //$NON-NLS-1$
            rm.done();
        }
    }

    public void insertBreakpoint(IBreakpointsTargetDMContext context, Map<String,Object> attributes,
            DataRequestMonitor<IBreakpointDMContext> rm) {
        // Clients are not allowed to call this method.
        // Use IBreakpointManager instead.
        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                REQUEST_FAILED, "Not allowed", new Error())); //$NON-NLS-1$
        rm.done();
    }

    public void removeBreakpoint(IBreakpointDMContext dmc, RequestMonitor rm) {
        // Clients are not allowed to call this method.
        // Use IBreakpointManager instead.
        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                REQUEST_FAILED, "Not allowed", new Error())); //$NON-NLS-1$
        rm.done();
    }

    public void updateBreakpoint(IBreakpointDMContext dmc, Map<String,Object> delta, RequestMonitor rm) {
        // Clients are not allowed to call this method.
        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                REQUEST_FAILED, "Not allowed", new Error())); //$NON-NLS-1$
        rm.done();
    }
}
