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
package com.windriver.tcf.dsf.core.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.ServiceDMContext;
import org.eclipse.dd.dsf.debug.service.INativeProcesses;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.osgi.framework.BundleContext;

import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.services.IProcesses;
import com.windriver.tcf.api.services.IProcesses.ProcessContext;
import com.windriver.tcf.dsf.core.Activator;

public class TCFDSFNativeProcesses extends AbstractDsfService implements INativeProcesses {
    
    private class ProcessDMC extends TCFDSFProcessDMC {
        
        final String id;
        
        ProcessDMC(String id) {
            super(TCFDSFNativeProcesses.this, new IDMContext[0]);
            this.id = id;
        }

        @Override
        public String toString() {
            return baseToString() + ".context[" + id + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public boolean equals(Object obj) {
            return super.baseEquals(obj) && ((ProcessDMC)obj).id.equals(id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
    
    private class ThreadDMC extends TCFDSFThreadDMC {
        
        final String id;
        
        ThreadDMC(String id) {
            super(TCFDSFNativeProcesses.this, new IDMContext[0]);
            this.id = id;
        }

        @Override
        public String toString() {
            return baseToString() + ".context[" + id + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public boolean equals(Object obj) {
            return super.baseEquals(obj) && ((ThreadDMC)obj).id.equals(id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
    
    private static class ProcessData implements IProcessDMData {
        
        private final IProcesses.ProcessContext ctx;
        
        ProcessData(IProcesses.ProcessContext ctx) {
            this.ctx = ctx;
        }

        public IDMContext getDebugContext() {
            // TODO Auto-generated method stub
            assert false;
            return null;
        }

        public String getId() {
            return ctx.getID();
        }

        public String getName() {
            return ctx.getName();
        }

        public boolean isDebuggerAttached() {
            return ctx.isAttached();
        }

        public boolean isValid() {
            return true;
        }
    }
    
    private static class ThreadData implements IThreadDMData {

        private final IProcesses.ProcessContext ctx;
        
        ThreadData(IProcesses.ProcessContext ctx) {
            this.ctx = ctx;
        }

        public IDMContext getDebugContext() {
            // TODO Auto-generated method stub
            assert false;
            return null;
        }

        public String getId() {
            return ctx.getID();
        }

        public String getName() {
            return ctx.getName();
        }

        public boolean isDebuggerAttached() {
            return ctx.isAttached();
        }

        public boolean isValid() {
            return true;
        }
        
    }
    
    final IProcesses service;
    private IDMContext service_dmc;
    
    TCFDSFNativeProcesses(DsfSession session, IChannel channel) {
        super(session);
        service = channel.getRemoteService(IProcesses.class);
        service_dmc = new ServiceDMContext(this, "#native_process");
    }

    @Override
    protected BundleContext getBundleContext() {
        return Activator.getBundleContext();
    }

    public IDMContext getServiceContext() {
        return service_dmc;
    }

    public boolean isValid() {
        return true;
    }

    public void attachDebuggerToProcess(IProcessDMContext ctx, RequestMonitor rm) {
        // TODO Auto-generated method stub
        assert false;
    }

    public void canTerminate(IDMContext ctx, DataRequestMonitor<Boolean> rm) {
        // TODO Auto-generated method stub
        assert false;
    }

    public void terminate(IDMContext ctx, RequestMonitor requestMonitor) {
        // TODO Auto-generated method stub
        assert false;
    }

    public void debugNewProcess(String file, DataRequestMonitor<IProcessDMContext> rm) {
        // TODO Auto-generated method stub
        assert false;
    }

    public void runNewProcess(String file, DataRequestMonitor<IProcessDMContext> rm) {
        // TODO Auto-generated method stub
        assert false;
    }

    public IProcessDMContext getProcessForDebugContext(IDMContext ctx) {
        if (ctx instanceof IProcessDMContext) {
            return (IProcessDMContext)ctx;
        }
        if (ctx instanceof TCFDSFExecutionDMC) {
            String id = ((TCFDSFExecutionDMC)ctx).getTcfContextId();
            return new ProcessDMC(id);
        }
        return null;
    }

    public IThreadDMContext getThreadForDebugContext(IDMContext ctx) {
        if (ctx instanceof IThreadDMContext) {
            return (IThreadDMContext)ctx;
        }
        if (ctx instanceof TCFDSFExecutionDMC) {
            String id = ((TCFDSFExecutionDMC)ctx).getTcfContextId();
            return new ThreadDMC(id);
        }
        return null;
    }

    public void getProcessesBeingDebugged(final DataRequestMonitor<IProcessDMContext[]> rm) {
        final Collection<String> list = new ArrayList<String>();
        final Set<IToken> cmds = new HashSet<IToken>();
        final IProcesses.DoneGetChildren done = new IProcesses.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                if (cmds.isEmpty()) return;
                assert cmds.contains(token);
                cmds.remove(token);
                if (error != null) {
                    for (IToken t : cmds) t.cancel();
                    cmds.clear();
                    rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                    rm.done();
                }
                else {
                    for (String id : context_ids) {
                        list.add(id);
                        cmds.add(service.getChildren(id, true, this));
                    }
                    if (cmds.isEmpty()) createDMContexts(list, rm);
                }
            }
        };
        cmds.add(service.getChildren(null, true, done));
    }

    public void getRunningProcesses(final DataRequestMonitor<IProcessDMContext[]> rm) {
        final Collection<String> list = new ArrayList<String>();
        final Set<IToken> cmds = new HashSet<IToken>();
        final IProcesses.DoneGetChildren done = new IProcesses.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                if (cmds.isEmpty()) return;
                assert cmds.contains(token);
                cmds.remove(token);
                if (error != null) {
                    for (IToken t : cmds) t.cancel();
                    cmds.clear();
                    rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                    rm.done();
                }
                else {
                    for (String id : context_ids) {
                        list.add(id);
                        cmds.add(service.getChildren(id, false, this));
                    }
                    if (cmds.isEmpty()) createDMContexts(list, rm);
                }
            }
        };
        cmds.add(service.getChildren(null, false, done));
    }

    @SuppressWarnings("unchecked")
    public void getModelData(IDMContext dmc, final DataRequestMonitor<?> rm) {
        if (dmc instanceof ProcessDMC) {
            service.getContext(((ProcessDMC)dmc).id, new IProcesses.DoneGetContext() {

                @SuppressWarnings("unchecked")
                public void doneGetContext(IToken token, Exception error, ProcessContext context) {
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Data error", error)); //$NON-NLS-1$
                    }
                    else {
                        ((DataRequestMonitor<IProcessDMData>)rm).setData(new ProcessData(context));
                    }
                    rm.done();
                }
            });
        }
        else if (dmc instanceof ThreadDMC) {
            service.getContext(((ProcessDMC)dmc).id, new IProcesses.DoneGetContext() {
                
                @SuppressWarnings("unchecked")
                public void doneGetContext(IToken token, Exception error, ProcessContext context) {
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Data error", error)); //$NON-NLS-1$
                    }
                    else {
                        ((DataRequestMonitor<IThreadDMData>)rm).setData(new ThreadData(context));
                    }
                    rm.done();
                }
            });
        }
        else if (dmc == service_dmc) {
            ((DataRequestMonitor<TCFDSFNativeProcesses>)rm).setData(this);
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }
    
    private void createDMContexts(Collection<String> ids, DataRequestMonitor<IProcessDMContext[]> rm) {
        assert false;
    }

    public void getProcessData(IProcessDMContext dmc,
            DataRequestMonitor<IProcessDMData> rm) {
        // TODO Auto-generated method stub
        
    }

    public void getThreadData(IThreadDMContext dmc, DataRequestMonitor<IThreadDMData> rm) {
        // TODO Auto-generated method stub
        assert false;
    }
}
