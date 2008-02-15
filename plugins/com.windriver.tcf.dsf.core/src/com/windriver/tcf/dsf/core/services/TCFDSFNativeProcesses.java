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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.ServiceDMContext;
import org.eclipse.dd.dsf.debug.service.INativeProcesses;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.osgi.framework.BundleContext;

import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.services.IProcesses;
import com.windriver.tcf.api.services.IRunControl;
import com.windriver.tcf.api.services.IProcesses.ProcessContext;
import com.windriver.tcf.api.services.IRunControl.RunControlContext;
import com.windriver.tcf.dsf.core.Activator;

public class TCFDSFNativeProcesses extends AbstractDsfService implements INativeProcesses {

    private class ProcessDMC extends TCFDSFProcessDMC implements IProcessDMContext {

        final String id;

        ProcessDMC(String id, IDMContext parent) {
            super(TCFDSFNativeProcesses.this, parent != null ? new IDMContext[]{ parent } : new IDMContext[0]);
            this.id = id;
        }

        @Override
        public String toString() {
            return baseToString() + ".context[" + id + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.baseEquals(obj)) return false;
            String obj_id = ((ProcessDMC)obj).id;
            if (obj_id == null) return id == null;
            return obj_id.equals(id);
        }

        @Override
        public int hashCode() {
            if (id == null) return 0;
            return id.hashCode();
        }
    }

    private class ThreadDMC extends TCFDSFThreadDMC implements IThreadDMContext {

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
            if (!super.baseEquals(obj)) return false;
            String obj_id = ((ThreadDMC)obj).id;
            if (obj_id == null) return id == null;
            return obj_id.equals(id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    private class ProcessData implements IProcessDMData {

        private final IProcesses.ProcessContext ctx;

        ProcessData(IProcesses.ProcessContext ctx) {
            this.ctx = ctx;
        }

        public IDMContext getDebugContext() {
            return getServicesTracker().getService(TCFDSFRunControl.class).getContext(ctx.getID());
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

    private class ThreadData implements IThreadDMData {

        private final TCFDSFExecutionDMC ctx;

        ThreadData(TCFDSFExecutionDMC ctx) {
            this.ctx = ctx;
        }

        public IDMContext getDebugContext() {
            return ctx;
        }

        public String getId() {
            if (ctx == null) return null;
            return ctx.getTcfContextId();
        }

        public String getName() {
            // TODO thread name
            return "";
        }

        public boolean isDebuggerAttached() {
            return true;
        }

        public boolean isValid() {
            return true;
        }
    }
    
    private class ProcessStartedEvent extends AbstractDMEvent<IDMContext> implements IProcessStartedEvent {
        
        private final IProcessDMContext prs;

        ProcessStartedEvent(IDMContext dmc, IProcessDMContext prs) {
            super(dmc);
            this.prs = prs;
        }
        
        public IProcessDMContext getProcess() {
            return prs;
        }
    }

    private class ProcessExitedEvent extends AbstractDMEvent<IDMContext> implements IProcessExitedEvent {
        
        private final IProcessDMContext prs;

        ProcessExitedEvent(IDMContext dmc, IProcessDMContext prs) {
            super(dmc);
            this.prs = prs;
        }
        
        public IProcessDMContext getProcess() {
            return prs;
        }
    }
    
    private final com.windriver.tcf.api.services.IRunControl.RunControlListener run_listener =
        new com.windriver.tcf.api.services.IRunControl.RunControlListener() {

            public void containerResumed(String[] context_ids) {
            }

            public void containerSuspended(String context, String pc,
                    String reason, Map<String, Object> params,
                    String[] suspended_ids) {
            }

            public void contextAdded(RunControlContext[] contexts) {
                for (RunControlContext ctx : contexts) {
                    String id = ctx.getID();
                    if (id.equals(ctx.getProperties().get(IRunControl.PROP_PROCESS_ID))) {
                        ProcessDMC dmc = new ProcessDMC(id, root_dmc);
                        process_cache.put(id, dmc);
                        getSession().dispatchEvent(new ProcessStartedEvent(root_dmc, dmc), getProperties());
                    }
                    else {
                        ThreadDMC dmc = new ThreadDMC(id);
                        thread_cache.put(id, dmc);
                    }
                }
            }

            public void contextChanged(RunControlContext[] contexts) {
            }

            public void contextException(String context, String msg) {
            }

            public void contextRemoved(String[] context_ids) {
                for (String id : context_ids) {
                    ProcessDMC dmc = process_cache.remove(id);
                    if (dmc != null) {
                        getSession().dispatchEvent(new ProcessExitedEvent(root_dmc, dmc), getProperties());
                    }
                    else {
                        thread_cache.remove(id);
                    }
                }
            }

            public void contextResumed(String context) {
            }

            public void contextSuspended(String context, String pc,
                    String reason, Map<String, Object> params) {
            }
    };

    private final IProcesses tcf_prs_service;
    private final com.windriver.tcf.api.services.IRunControl tcf_run_service;
    private final Map<String,ProcessDMC> process_cache = new HashMap<String,ProcessDMC>(); // all attached processes
    private final Map<String,ThreadDMC> thread_cache = new HashMap<String,ThreadDMC>(); // only some of attached threads
    private final ProcessDMC root_dmc = new ProcessDMC(null, null);
    private IDMContext service_dmc;

    public TCFDSFNativeProcesses(DsfSession session, IChannel channel, final RequestMonitor monitor) {
        super(session);
        tcf_prs_service = channel.getRemoteService(IProcesses.class);
        tcf_run_service = channel.getRemoteService(com.windriver.tcf.api.services.IRunControl.class);
        if (tcf_run_service != null) tcf_run_service.addListener(run_listener);
        service_dmc = new ServiceDMContext(this, "#native_process");
        initialize(new RequestMonitor(getExecutor(), monitor) { 
            @Override
            protected void handleOK() {
                String[] class_names = {
                        INativeProcesses.class.getName(),
                        TCFDSFNativeProcesses.class.getName()
                };
                register(class_names, new Hashtable<String,String>());
                monitor.done();
            }
        });
    }

    @Override 
    public void initialize(final RequestMonitor monitor) {
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
                    monitor.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                    monitor.done();
                }
                else {
                    for (String id : context_ids) {
                        list.add(id);
                        cmds.add(tcf_prs_service.getChildren(id, true, this));
                    }
                    if (cmds.isEmpty()) {
                        for (String id : list) {
                            assert id != null;
                            if (process_cache.get(id) != null) continue;
                            process_cache.put(id, new ProcessDMC(id, root_dmc));
                        }
                        process_cache.put(null, root_dmc);
                        TCFDSFNativeProcesses.super.initialize(monitor);
                    }
                }
            }
        };
        cmds.add(tcf_prs_service.getChildren(null, true, done));
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

    public IDMContext getServiceContext() {
        return service_dmc;
    }

    public boolean isValid() {
        return true;
    }
    
    public void attachDebuggerToProcess(IProcessDMContext ctx, final RequestMonitor rm) {
        if (tcf_prs_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    REQUEST_FAILED, "Service not available", null)); //$NON-NLS-1$
            rm.done();
        }
        else if (ctx instanceof ProcessDMC) {
            final ProcessDMC p = (ProcessDMC)ctx;
            tcf_prs_service.getContext(p.id, new IProcesses.DoneGetContext() {
                public void doneGetContext(IToken token, Exception error, ProcessContext context) {
                    if (rm.isCanceled()) return;
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Cannot read processs attributes", error)); //$NON-NLS-1$
                        rm.done();
                    }
                    else if (context == null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Invalid processs ID", error)); //$NON-NLS-1$
                        rm.done();
                    }
                    else {
                        context.attach(new IProcesses.DoneCommand() {
                            public void doneCommand(IToken token, Exception error) {
                                if (rm.isCanceled()) return;
                                if (error != null) {
                                    rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                            REQUEST_FAILED, "Cannot attach a process", error)); //$NON-NLS-1$
                                }
                                assert error != null || process_cache.get(p.id) != null;
                                rm.done();
                            }
                        });
                    }
                }
            });
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void canTerminate(IDMContext ctx, final DataRequestMonitor<Boolean> rm) {
        rm.setData(false);
        if (tcf_prs_service == null) {
            rm.done();
        }
        else if (ctx instanceof ProcessDMC) {
            ProcessDMC p = (ProcessDMC)ctx;
            tcf_prs_service.getContext(p.id, new IProcesses.DoneGetContext() {
                public void doneGetContext(IToken token, Exception error, ProcessContext context) {
                    if (rm.isCanceled()) return;
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Cannot read processs attributes", error)); //$NON-NLS-1$
                    }
                    else if (context == null) {
                        rm.setData(false);
                    }
                    else {
                        rm.setData(context.canTerminate());
                    }
                    rm.done();
                }
            });
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void terminate(IDMContext ctx, final RequestMonitor rm) {
        if (tcf_prs_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    REQUEST_FAILED, "Service not available", null)); //$NON-NLS-1$
            rm.done();
        }
        else if (ctx instanceof ProcessDMC) {
            ProcessDMC p = (ProcessDMC)ctx;
            tcf_prs_service.getContext(p.id, new IProcesses.DoneGetContext() {
                public void doneGetContext(IToken token, Exception error, ProcessContext context) {
                    if (rm.isCanceled()) return;
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Cannot read processs attributes", error)); //$NON-NLS-1$
                        rm.done();
                    }
                    else if (context == null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Invalid processs ID", error)); //$NON-NLS-1$
                        rm.done();
                    }
                    else {
                        context.terminate(new IProcesses.DoneCommand() {
                            public void doneCommand(IToken token, Exception error) {
                                if (rm.isCanceled()) return;
                                if (error != null) {
                                    rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                            REQUEST_FAILED, "Cannot terminate a process", error)); //$NON-NLS-1$
                                }
                                rm.done();
                            }
                        });
                    }
                }
            });
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void debugNewProcess(String file, final DataRequestMonitor<IProcessDMContext> rm) {
        if (tcf_prs_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    REQUEST_FAILED, "Service not available", null)); //$NON-NLS-1$
            rm.done();
        }
        else {
            tcf_prs_service.start(null, file, null, null, true, new IProcesses.DoneStart() {
                public void doneStart(IToken token, Exception error, ProcessContext process) {
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Cannot start a process", error)); //$NON-NLS-1$
                    }
                    else {
                        ProcessDMC dmc = process_cache.get(process.getID());
                        assert dmc != null; 
                        rm.setData(dmc);
                    }
                    rm.done();
                }
            });
        }
    }

    public void runNewProcess(String file, final DataRequestMonitor<IProcessDMContext> rm) {
        if (tcf_prs_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    REQUEST_FAILED, "Service not available", null)); //$NON-NLS-1$
            rm.done();
        }
        else {
            tcf_prs_service.start(null, file, null, null, false, new IProcesses.DoneStart() {
                public void doneStart(IToken token, Exception error, ProcessContext process) {
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Cannot start a process", error)); //$NON-NLS-1$
                    }
                    else {
                        assert process_cache.get(process.getID()) == null;
                        rm.setData(new ProcessDMC(process.getID(), root_dmc));
                    }
                    rm.done();
                }
            });
        }
    }

    public IProcessDMContext getProcessForDebugContext(IDMContext ctx) {
        if (ctx instanceof IProcessDMContext) {
            return (IProcessDMContext)ctx;
        }
        if (ctx instanceof TCFDSFExecutionDMC) {
            String id = ((TCFDSFExecutionDMC)ctx).getTcfContextId();
            return process_cache.get(id);
        }
        return null;
    }

    public IThreadDMContext getThreadForDebugContext(IDMContext ctx) {
        if (ctx instanceof IThreadDMContext) {
            return (IThreadDMContext)ctx;
        }
        if (ctx instanceof TCFDSFExecutionDMC) {
            String id = ((TCFDSFExecutionDMC)ctx).getTcfContextId();
            ThreadDMC dmc = thread_cache.get(id);
            if (dmc == null) dmc = new ThreadDMC(id); 
            return dmc;
        }
        return null;
    }

    public void getProcessesBeingDebugged(DataRequestMonitor<IProcessDMContext[]> rm) {
        rm.setData(process_cache.values().toArray(new IProcessDMContext[process_cache.size()]));
        rm.done();
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
                        cmds.add(tcf_prs_service.getChildren(id, false, this));
                    }
                    if (cmds.isEmpty()) {
                        int cnt = 0;
                        IProcessDMContext[] data = new IProcessDMContext[list.size()];
                        for (String id : list) {
                            assert id != null;
                            data[cnt] = process_cache.get(id);
                            if (data[cnt] == null) data[cnt] = new ProcessDMC(id, root_dmc);
                            cnt++;
                        }
                        rm.setData(data);
                        rm.done();
                    }
                }
            }
        };
        cmds.add(tcf_prs_service.getChildren(null, false, done));
    }

    @SuppressWarnings("unchecked")
    public void getModelData(IDMContext dmc, final DataRequestMonitor<?> rm) {
        if (dmc instanceof ProcessDMC) {
            getProcessData((ProcessDMC)dmc, (DataRequestMonitor<IProcessDMData>)rm);
        }
        else if (dmc instanceof ThreadDMC) {
            getThreadData((ThreadDMC)dmc, (DataRequestMonitor<IThreadDMData>)rm);
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

    public void getProcessData(IProcessDMContext dmc, final DataRequestMonitor<IProcessDMData> rm) {
        if (dmc instanceof ProcessDMC) {
            tcf_prs_service.getContext(((ProcessDMC)dmc).id, new IProcesses.DoneGetContext() {

                @SuppressWarnings("unchecked")
                public void doneGetContext(IToken token, Exception error, ProcessContext context) {
                    if (rm.isCanceled()) return;
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Cannot read processs attributes", error)); //$NON-NLS-1$
                    }
                    else if (context == null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Invalid processs ID", error)); //$NON-NLS-1$
                    }
                    else {
                        rm.setData(new ProcessData(context));
                    }
                    rm.done();
                }
            });
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void getThreadData(IThreadDMContext dmc, final DataRequestMonitor<IThreadDMData> rm) {
        if (dmc instanceof ThreadDMC) {
            String id = ((ThreadDMC)dmc).id;
            TCFDSFRunControl rc = getServicesTracker().getService(TCFDSFRunControl.class);
            rm.setData(new ThreadData(rc.getContext(id)));
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }
}
