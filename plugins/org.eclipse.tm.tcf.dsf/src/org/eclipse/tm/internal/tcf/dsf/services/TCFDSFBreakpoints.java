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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
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
import org.osgi.framework.BundleContext;


// TODO IBreakpointHitEvent

public class TCFDSFBreakpoints extends AbstractDsfService implements org.eclipse.dd.dsf.debug.service.IBreakpoints {
    
    private class BreakpointDMC extends AbstractDMContext implements IBreakpointDMContext {
        
        final String id;
        final IBreakpoint bp;
        final TCFDataCache<Map<String,Object>> status;
        
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
                        data = null;
                        valid = true;
                        return true;
                    }
                    command = tcf_bpt_service.getStatus(id, new org.eclipse.tm.tcf.services.IBreakpoints.DoneGetStatus() {
                        public void doneGetStatus(IToken token, Exception err, Map<String,Object> status) {
                            if (command != token) return;
                            command = null;
                            if (err != null) {
                                data = null;
                                error = err;
                            }
                            else {
                                data = status;
                            }
                            valid = true;
                            validate();
                        }
                    });
                    return false;
                }
            };
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
            disposed = true;
        }
    }
    
    private class BreakpointData implements IBreakpointDMData {
        
        final IBreakpoint bp;
        final BreakpointStatus status;
        
        BreakpointData(IBreakpoint bp, BreakpointStatus status) {
            this.bp = bp;
            this.status = status;
        }

        public IBreakpoint getPlatformBreakpoint() {
            return bp;
        }

        public BreakpointStatus getStatus() {
            return status;
        }
    }
    
    private final ITCFBreakpointListener bp_listener = new ITCFBreakpointListener() {

        public void breakpointStatusChanged(String id) {
            final BreakpointDMC dmc = cache.get(id);
            if (dmc != null) {
                Map<String, Object> map = launch.getBreakpointsStatus().getStatus(dmc.id);
                dmc.status.reset(map);
                IBreakpointDMEvent e = null;
                if (map == null) {
                    e = new IBreakpointUninstalledDMEvent() {
                        public IBreakpointDMContext getDMContext() {
                            return dmc;
                        }
                    };
                }
                else if (map.get(org.eclipse.tm.tcf.services.IBreakpoints.STATUS_ERROR) != null) {
                    e = new IBreakpointInstallFailedDMEvent() {
                        public IBreakpointDMContext getDMContext() {
                            return dmc;
                        }
                    };
                }
                else if (map.get(org.eclipse.tm.tcf.services.IBreakpoints.STATUS_PLANTED) != null) {
                    e = new IBreakpointInstalledDMEvent() {
                        public IBreakpointDMContext getDMContext() {
                            return dmc;
                        }
                    };
                }
                else {
                    e = new IBreakpointUninstalledDMEvent() {
                        public IBreakpointDMContext getDMContext() {
                            return dmc;
                        }
                    };
                }
                getSession().dispatchEvent(e, getProperties());
            }
        }

        public void breakpointRemoved(String id) {
            final BreakpointDMC dmc = cache.get(id);
            if (dmc != null) {
                dmc.dispose();
                IBreakpointDMEvent e = new IBreakpointUninstalledDMEvent() {
                    public IBreakpointDMContext getDMContext() {
                        return dmc;
                    }
                };
                getSession().dispatchEvent(e, getProperties());
            }
        }
    };
    
    private final TCFLaunch launch;
    private final IChannel channel;
    private final org.eclipse.tm.tcf.services.IBreakpoints tcf_bpt_service;
    private final Map<String,BreakpointDMC> cache = new HashMap<String,BreakpointDMC>();
    
    public TCFDSFBreakpoints(DsfSession session, TCFLaunch launch, final RequestMonitor monitor) {
        super(session);
        this.launch = launch; 
        channel = launch.getChannel();
        launch.getBreakpointsStatus().addListener(bp_listener);
        tcf_bpt_service = channel.getRemoteService(org.eclipse.tm.tcf.services.IBreakpoints.class);
        initialize(new RequestMonitor(getExecutor(), monitor) { 
            @Override
            protected void handleOK() {
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

    public void getAllBreakpoints(IDMContext ctx, DataRequestMonitor<IBreakpointDMContext[]> rm) {
        IBreakpointManager bp_manager = DebugPlugin.getDefault().getBreakpointManager();
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

    public void getBreakpoints(IDMContext dmc, IBreakpoint bp, DataRequestMonitor<IBreakpointDMContext[]> rm) {
        TCFBreakpointsModel m = TCFBreakpointsModel.getBreakpointsModel();
        ArrayList<IBreakpointDMContext> l = new ArrayList<IBreakpointDMContext>();
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
        rm.setData(l.toArray(new IBreakpointDMContext[l.size()]));
        rm.done();
    }
    
    public void getBreakpointData(final IDMContext dmc, final DataRequestMonitor<IBreakpointDMData> rm) {
        if (dmc instanceof BreakpointDMC) {
            BreakpointDMC bp = (BreakpointDMC)dmc;
            if (!bp.status.validate()) {
                bp.status.addWaitingRequest(new IDataRequest() {
                    public void cancel() {
                        rm.setStatus(new Status(IStatus.CANCEL, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Canceled", null)); //$NON-NLS-1$
                        rm.setCanceled(true);
                        rm.done();
                    }
                    public void done() {
                        getBreakpointData(dmc, rm);
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
            Map<String,Object> map = bp.status.getData();
            BreakpointStatus status = BreakpointStatus.FILTERED_OUT;
            if (map != null) {
                if (map.get(org.eclipse.tm.tcf.services.IBreakpoints.STATUS_ERROR) != null) {
                    status = BreakpointStatus.FAILED_TO_INSTALL;
                }
                else if (map.get(org.eclipse.tm.tcf.services.IBreakpoints.STATUS_PLANTED) != null) {
                    status = BreakpointStatus.INSTALLED;
                }
            }
            rm.setData(new BreakpointData(bp.bp, status));
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
            getBreakpointData((BreakpointDMC)dmc, (DataRequestMonitor<IBreakpointDMData>)rm);
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null));  //$NON-NLS-1$
            rm.done();
        }
    }
}
