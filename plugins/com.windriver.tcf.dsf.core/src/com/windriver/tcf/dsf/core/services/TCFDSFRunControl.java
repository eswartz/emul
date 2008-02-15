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

import java.math.BigInteger;
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
import org.eclipse.dd.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.osgi.framework.BundleContext;

import com.windriver.debug.tcf.core.model.ITCFConstants;
import com.windriver.tcf.dsf.core.Activator;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.services.IRunControl;
import com.windriver.tcf.api.services.IRunControl.RunControlContext;

public class TCFDSFRunControl extends AbstractDsfService implements org.eclipse.dd.dsf.debug.service.IRunControl {

    public static class SuspendedEvent extends AbstractDMEvent<IExecutionDMContext> implements ISuspendedDMEvent {

        private final StateChangeReason reason;

        public SuspendedEvent(IExecutionDMContext dmc, String reason) {
            super(dmc);
            this.reason = toStateChangeReason(reason);
        }

        public StateChangeReason getReason() {
            return reason;
        }
    }

    public static class ResumedEvent extends AbstractDMEvent<IExecutionDMContext> implements IResumedDMEvent {

        public ResumedEvent(IExecutionDMContext dmc) {
            super(dmc);
        }

        public StateChangeReason getReason() {
            return StateChangeReason.USER_REQUEST;
        }
    }

    public class ContainerSuspendedEvent extends AbstractDMEvent<IExecutionDMContext> implements IContainerSuspendedDMEvent {

        private final String trigger_id;
        private final StateChangeReason reason;

        public ContainerSuspendedEvent(IExecutionDMContext dmc, String trigger_id, String reason) {
            super(dmc);
            this.trigger_id = trigger_id;
            this.reason = toStateChangeReason(reason);
        }

        public IExecutionDMContext getTriggeringContext() {
            return cache.get(trigger_id);
        }

        public StateChangeReason getReason() {
            return reason;
        }
    }

    public static class ContainerResumedEvent extends AbstractDMEvent<IExecutionDMContext> implements IContainerResumedDMEvent {

        public ContainerResumedEvent(IExecutionDMContext dmc) {
            super(dmc);
        }

        public StateChangeReason getReason() {
            return StateChangeReason.USER_REQUEST;
        }
    }

    public static class StartedEvent extends AbstractDMEvent<IContainerDMContext> implements IStartedDMEvent {

        private final IExecutionDMContext exe;

        public StartedEvent(IContainerDMContext dmc, IExecutionDMContext exe) {
            super(dmc);
            this.exe = exe;
        }

        public IExecutionDMContext getExecutionContext() {
            return exe;
        }
    }

    public static class ChangedEvent extends AbstractDMEvent<IExecutionDMContext> {

        public ChangedEvent(IExecutionDMContext dmc) {
            super(dmc);
        }
    }

    public static class ExitedEvent extends AbstractDMEvent<IContainerDMContext> implements IExitedDMEvent {

        private final IExecutionDMContext exe;

        public ExitedEvent(IContainerDMContext dmc, IExecutionDMContext exe) {
            super(dmc);
            this.exe = exe;
        }

        public IExecutionDMContext getExecutionContext() {
            return exe;
        }
    }

    private final com.windriver.tcf.api.services.IRunControl.RunControlListener run_listener =
        new com.windriver.tcf.api.services.IRunControl.RunControlListener() {

        public void containerResumed(String[] context_ids) {
            for (String id : context_ids) {
                ExecutionDMC n = cache.get(id);
                if (n != null) n.onContextResumed();
            }
            for (String id : context_ids) {
                ExecutionDMC n = cache.get(id);
                if (n != null && n.context.isValid()) {
                    RunControlContext c = n.context.getData();
                    if (c.isContainer()) {
                        getSession().dispatchEvent(new ContainerResumedEvent(n), getProperties());
                    }
                }
            }
        }

        public void containerSuspended(String trigger_id, String pc,
                String reason, Map<String, Object> params,
                String[] suspended_ids) {
            if (trigger_id != null) {
                ExecutionDMC n = cache.get(trigger_id);
                if (n != null) n.onContextSuspended(pc, reason, params);
            }
            for (String id : suspended_ids) {
                if (id.equals(trigger_id)) continue;
                ExecutionDMC n = cache.get(id);
                if (n != null) n.onContainerSuspended(reason);
            }
            for (String id : suspended_ids) {
                ExecutionDMC n = cache.get(id);
                if (n != null && n.context.isValid()) {
                    RunControlContext c = n.context.getData();
                    if (c.isContainer()) {
                        getSession().dispatchEvent(new ContainerSuspendedEvent(n, trigger_id, reason), getProperties());
                    }
                }
            }
        }

        public void contextAdded(RunControlContext[] contexts) {
            for (RunControlContext ctx : contexts) {
                ExecutionDMC n = cache.get(ctx.getParentID());
                if (n != null) n.onContextAdded(ctx);
            }
        }

        public void contextChanged(RunControlContext[] contexts) {
            for (RunControlContext ctx : contexts) {
                ExecutionDMC n = cache.get(ctx.getID());
                if (n != null) n.onContextChanged(ctx);
            }
        }

        public void contextException(String id, String msg) {
            ExecutionDMC n = cache.get(id);
            if (n != null) n.onContextException(msg);
        }

        public void contextRemoved(String[] context_ids) {
            for (String id : context_ids) {
                ExecutionDMC n = cache.get(id);
                if (n != null) n.onContextRemoved();
            }
        }

        public void contextResumed(String id) {
            ExecutionDMC n = cache.get(id);
            if (n != null) n.onContextResumed();
        }

        public void contextSuspended(String id, String pc, String reason, Map<String, Object> params) {
            ExecutionDMC n = cache.get(id);
            if (n != null) n.onContextSuspended(pc, reason, params);
        }
    };
    
    private static class ExecutionState {
        boolean is_suspended;
        boolean is_running;
        String suspend_pc;
        String suspend_reason;
        Map<String,Object> suspend_params;
    }

    private class ExecutionDMC extends TCFDSFExecutionDMC {

        final String id;
        final ExecutionDMC parent;
        final IMemoryBlockRetrievalExtension mem_retrieval;

        boolean disposed;
        int is_stepping;
        int is_resuming;
        
        final TCFDataCache<RunControlContext> context; 
        final TCFDataCache<Map<String,ExecutionDMC>> children; 
        final TCFDataCache<ExecutionState> state; 

        public ExecutionDMC(ExecutionDMC parent, final String id) {
            super(TCFDSFRunControl.this, parent == null ?
                    new IDMContext[0] : new IDMContext[] { parent });
            this.parent = parent;
            this.id = id;
            DsfMemoryBlockRetrieval mr = null;
            try {
                mr = new DsfMemoryBlockRetrieval(ITCFConstants.ID_TCF_DEBUG_MODEL, this);
            }
            catch (DebugException e) {
                e.printStackTrace();
            };
            mem_retrieval = mr;
            context = new TCFDataCache<RunControlContext>(channel) {
                @Override
                public boolean startDataRetrieval() {
                    assert command == null;
                    if (id == null || tcf_run_service == null) {
                        data = null;
                        valid = true;
                        return true;
                    }
                    command = tcf_run_service.getContext(id, new IRunControl.DoneGetContext() {
                        public void doneGetContext(IToken token, Exception err, IRunControl.RunControlContext ctx) {
                            if (command != token) return;
                            command = null;
                            if (err != null) {
                                error = err;
                            }
                            else {
                                data = ctx;
                            }
                            valid = true;
                            validate();
                        }
                    });
                    return false;
                }
            };
            children = new TCFDataCache<Map<String,ExecutionDMC>>(channel) {
                @Override
                public boolean startDataRetrieval() {
                    assert command == null;
                    if (tcf_run_service == null) {
                        data = null;
                        valid = true;
                        return true;
                    }
                    command = tcf_run_service.getChildren(id, new IRunControl.DoneGetChildren() {
                        public void doneGetChildren(IToken token, Exception err, String[] contexts) {
                            if (command != token) return;
                            command = null;
                            if (err != null) {
                                data = null;
                                error = err;
                            }
                            else {
                                if (data == null) data = new HashMap<String,ExecutionDMC>();
                                data.clear();
                                for (int i = 0; i < contexts.length; i++) {
                                    String id = contexts[i];
                                    ExecutionDMC n = cache.get(id);
                                    if (n == null) {
                                        n = new ExecutionDMC(ExecutionDMC.this, id);
                                        cache.put(n.id, n);
                                    }
                                    data.put(id, n);
                                }
                            }
                            valid = true;
                            validate();
                        }
                    });
                    return false;
                }
            };
            state = new TCFDataCache<ExecutionState>(channel) {
                @Override
                public boolean startDataRetrieval() {
                    assert command == null;
                    assert context.isValid();
                    RunControlContext c = context.getData();
                    if (c == null || !c.hasState()) {
                        data = null;
                        valid = true;
                        return true;
                    }
                    command = c.getState(new IRunControl.DoneGetState() {
                        public void doneGetState(IToken token, Exception err, boolean suspend, String pc, String reason, Map<String,Object> params) {
                            if (token != command) return;
                            command = null;
                            if (err != null) {
                                data = null;
                                error = err;
                            }
                            else {
                                data = new ExecutionState();
                                data.is_running = !suspend;
                                data.is_suspended = suspend;
                                if (suspend) {
                                    data.suspend_pc = pc;
                                    data.suspend_reason = reason;
                                    data.suspend_params = params;
                                }
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
        public String toString() {
            return baseToString() + ".context[" + id + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.baseEquals(obj)) return false;
            String obj_id = ((ExecutionDMC)obj).id;
            if (obj_id == null) return id == null;
            return obj_id.equals(id);
        }

        @Override
        public int hashCode() {
            if (id == null) return 0;
            return id.hashCode();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class cls) {
            Object obj = null;
            if (cls == IMemoryBlockRetrieval.class) obj = mem_retrieval;
            if (cls == IMemoryBlockRetrievalExtension.class) obj = mem_retrieval;
            if (obj == null) obj = super.getAdapter(cls);
            return obj;
        }

        @Override
        public String getTcfContextId() {
            return id;
        }

        @Override
        public void addStateWaitingRequest(IDataRequest req) {
            state.addWaitingRequest(req);
        }

        @Override
        public TCFAddress getPC() {
            ExecutionState st = state.getData();
            if (st == null) return null;
            if (st.suspend_pc == null) return null;
            return new TCFAddress(new BigInteger(st.suspend_pc));
        }

        @Override
        public boolean validateState() {
            return state.validate();
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
        
        void dispose() {
            assert !disposed;
            context.cancel();
            children.cancel();
            state.cancel();
            if (children.isValid()) {
                Map<String,ExecutionDMC> m = children.getData();
                if (m != null) {
                    for (ExecutionDMC n : m.values()) n.dispose();
                }
            }
            cache.remove(id);
            disposed = true;
        }
        
        /*--------------------------------------------------------------------------------------*/
        /* Events                                                                               */

        void onContextAdded(IRunControl.RunControlContext c) {
            String id = c.getID();
            assert !disposed;
            assert cache.get(id) == null;
            ExecutionDMC n = new ExecutionDMC(this, id);
            n.context.reset(c);
            if (children.isValid()) {
                Map<String,ExecutionDMC> m = children.getData();
                if (m != null) m.put(id, n);
            }
            cache.put(id, n);
            getSession().dispatchEvent(new StartedEvent(this, n), getProperties());
        }

        void onContextChanged(IRunControl.RunControlContext c) {
            assert !disposed;
            context.reset(c);
            getSession().dispatchEvent(new ChangedEvent(this), getProperties());
        }

        void onContextRemoved() {
            assert !disposed;
            if (parent != null && parent.children.isValid()) {
                Map<String,ExecutionDMC> m = parent.children.getData();
                if (m != null) m.remove(id);
            }
            dispose();
            getSession().dispatchEvent(new ExitedEvent(parent, this), getProperties());
        }

        void onContainerSuspended(String reason) {
            assert !disposed;
            if (!context.isValid()) return;
            RunControlContext rc = context.getData();
            if (rc == null) return;
            if (!rc.hasState()) return;
            state.reset();
            getSession().dispatchEvent(new SuspendedEvent(this, reason), getProperties());
        }

        void onContextSuspended(String pc, String reason, Map<String,Object> params) {
            assert !disposed;
            assert !context.isValid() || context.getData().hasState();
            ExecutionState st = new ExecutionState();
            st.is_suspended = true;
            st.suspend_pc = pc;
            st.suspend_reason = reason;
            st.suspend_params = params;
            state.reset(st);
            getSession().dispatchEvent(new SuspendedEvent(this, reason), getProperties());
        }

        void onContextResumed() {
            assert !disposed;
            assert !context.isValid() || context.getData().hasState();
            ExecutionState st = new ExecutionState();
            st.is_running = true;
            state.reset(st);
            getSession().dispatchEvent(new ResumedEvent(this), getProperties());
        }

        void onContextException(String msg) {
            assert !disposed;
            // TODO onContextException handling
        }
    }

    private static class ExecutionData implements IExecutionDMData {

        private final StateChangeReason reason;

        ExecutionData(StateChangeReason reason) {
            this.reason = reason;
        }

        public boolean isValid() {
            return true;
        }

        public StateChangeReason getStateChangeReason() { 
            return reason;
        }
    }

    private static StateChangeReason toStateChangeReason(String s) {
        if (s == null) return StateChangeReason.UNKNOWN;
        if (s.equals(com.windriver.tcf.api.services.IRunControl.REASON_USER_REQUEST)) return StateChangeReason.USER_REQUEST;
        if (s.equals(com.windriver.tcf.api.services.IRunControl.REASON_STEP)) return StateChangeReason.STEP;
        if (s.equals(com.windriver.tcf.api.services.IRunControl.REASON_BREAKPOINT)) return StateChangeReason.BREAKPOINT;
        if (s.equals(com.windriver.tcf.api.services.IRunControl.REASON_EXCEPTION)) return StateChangeReason.EXCEPTION;
        if (s.equals(com.windriver.tcf.api.services.IRunControl.REASON_CONTAINER)) return StateChangeReason.CONTAINER;
        if (s.equals(com.windriver.tcf.api.services.IRunControl.REASON_WATCHPOINT)) return StateChangeReason.WATCHPOINT;
        if (s.equals(com.windriver.tcf.api.services.IRunControl.REASON_SIGNAL)) return StateChangeReason.SIGNAL;
        if (s.equals(com.windriver.tcf.api.services.IRunControl.REASON_SHAREDLIB)) return StateChangeReason.SHAREDLIB;
        if (s.equals(com.windriver.tcf.api.services.IRunControl.REASON_ERROR)) return StateChangeReason.ERROR;
        return StateChangeReason.UNKNOWN;
    }

    private final IChannel channel;
    private final com.windriver.tcf.api.services.IRunControl tcf_run_service;
    private final Map<String,ExecutionDMC> cache = new HashMap<String,ExecutionDMC>();
    private final ExecutionDMC root_dmc;
    private IDMContext service_dmc;

    public TCFDSFRunControl(DsfSession session, IChannel channel, final RequestMonitor monitor) {
        super(session);
        this.channel = channel;
        tcf_run_service = channel.getRemoteService(com.windriver.tcf.api.services.IRunControl.class);
        if (tcf_run_service != null) tcf_run_service.addListener(run_listener);
        service_dmc = new ServiceDMContext(this, "#run_control");
        root_dmc = new ExecutionDMC(null, null);
        cache.put(null, root_dmc);
        initialize(new RequestMonitor(getExecutor(), monitor) { 
            @Override
            protected void handleOK() {
                String[] class_names = {
                        org.eclipse.dd.dsf.debug.service.IRunControl.class.getName(),
                        TCFDSFRunControl.class.getName()
                };
                register(class_names, new Hashtable<String,String>());
                monitor.done();
            }
        });
    }

    @Override 
    public void shutdown(RequestMonitor monitor) {
        if (tcf_run_service != null) tcf_run_service.removeListener(run_listener);
        unregister();
        super.shutdown(monitor);
    }

    @Override
    protected BundleContext getBundleContext() {
        return Activator.getBundleContext();
    }

    @SuppressWarnings("unchecked")
    public void getModelData(IDMContext dmc, final DataRequestMonitor<?> rm) {
        if (dmc instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)dmc;
            if (!ctx.context.validate()) {
                ctx.context.addWaitingRequest(new IDataRequest() {
                    public void cancel() {
                        rm.setStatus(new Status(IStatus.CANCEL, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Canceled", null)); //$NON-NLS-1$
                        rm.setCanceled(true);
                        rm.done();
                    }
                    public void done() {
                        getModelData(ctx, rm);
                    }
                });
                return;
            }
            if (ctx.context.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", ctx.context.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            if (ctx.context.getData() == null) {
                ExecutionData dt = new ExecutionData(StateChangeReason.UNKNOWN);
                ((DataRequestMonitor<IExecutionDMData>)rm).setData(dt);
                rm.done();
                return;
            }
            if (!ctx.context.getData().hasState()) {
                ExecutionData dt = new ExecutionData(StateChangeReason.UNKNOWN);
                ((DataRequestMonitor<IExecutionDMData>)rm).setData(dt);
                rm.done();
                return;
            }
            if (!ctx.state.validate()) {
                ctx.state.addWaitingRequest(new IDataRequest() {
                    public void cancel() {
                        rm.setStatus(new Status(IStatus.CANCEL, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Canceled", null)); //$NON-NLS-1$
                        rm.setCanceled(true);
                        rm.done();
                    }
                    public void done() {
                        getModelData(ctx, rm);
                    }
                });
                return;
            }
            if (ctx.state.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", ctx.state.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            if (ctx.state.getData() == null) {
                ExecutionData dt = new ExecutionData(StateChangeReason.UNKNOWN);
                ((DataRequestMonitor<IExecutionDMData>)rm).setData(dt);
                rm.done();
                return;
            }
            ExecutionData dt = new ExecutionData(toStateChangeReason(ctx.state.getData().suspend_reason));
            ((DataRequestMonitor<IExecutionDMData>)rm).setData(dt);
            rm.done();
        }
        else if (dmc == service_dmc) {
            ((DataRequestMonitor<TCFDSFRunControl>)rm).setData(this);
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public IDMContext getServiceContext() {
        return service_dmc;
    }

    public boolean isValid() {
        return true;
    }

    public boolean canInstructionStep(IDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.context.isValid()) {
                RunControlContext c = ctx.context.getData();
                return c != null && c.canResume(com.windriver.tcf.api.services.IRunControl.RM_STEP_INTO);
            }
        }
        return false;
    }

    public boolean canResume(IDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.context.isValid()) {
                RunControlContext c = ctx.context.getData();
                return c != null && c.canResume(com.windriver.tcf.api.services.IRunControl.RM_RESUME);
            }
        }
        return false;
    }

    public boolean canStep(IDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.context.isValid()) {
                RunControlContext c = ctx.context.getData();
                if (c != null) {
                    if (c.canResume(com.windriver.tcf.api.services.IRunControl.RM_STEP_INTO_LINE)) return true;
                    if (c.canResume(com.windriver.tcf.api.services.IRunControl.RM_STEP_INTO)) return true;
                }
            }
        }
        return false;
    }

    public boolean canSuspend(IDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.context.isValid()) {
                RunControlContext c = ctx.context.getData();
                return c != null && c.canSuspend();
            }
        }
        return false;
    }

    public TCFDSFExecutionDMC getContext(String id) {
        return cache.get(id);
    }

    public void getContainerContexts(IContainerDMContext context, final DataRequestMonitor<IExecutionDMContext[]> rm) {
        getContexts(context, rm, false);
    }

    public void getExecutionContexts(IContainerDMContext context, final DataRequestMonitor<IExecutionDMContext[]> rm) {
        getContexts(context, rm, true);
    }

    public void getContexts(IContainerDMContext context,
            final DataRequestMonitor<IExecutionDMContext[]> rm, final boolean has_state) {
        if (context == null) context = root_dmc;
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)context;
            TCFDataCache<Map<String,ExecutionDMC>> cache = ctx.children;
            if (!cache.validate()) {
                cache.addWaitingRequest(new IDataRequest() {
                    public void cancel() {
                        rm.setStatus(new Status(IStatus.CANCEL, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Canceled", null)); //$NON-NLS-1$
                        rm.setCanceled(true);
                        rm.done();
                    }
                    public void done() {
                        getContexts(ctx, rm, has_state);
                    }
                });
                return;
            }
            if (cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            if (cache.getData() == null) {
                rm.setData(new ExecutionDMC[0]);
                rm.done();
                return;
            }
            final Set<IDataRequest> reqs = new HashSet<IDataRequest>();
            for (ExecutionDMC e : cache.getData().values()) {
                if (!e.context.validate()) {
                    IDataRequest req = new IDataRequest() {
                        public void cancel() {
                            if (reqs.remove(this) && reqs.isEmpty()) getContexts(ctx, rm, has_state);
                        }
                        public void done() {
                            if (reqs.remove(this) && reqs.isEmpty()) getContexts(ctx, rm, has_state);
                        }
                    };
                    reqs.add(req);
                    e.context.addWaitingRequest(req);
                }
                // TODO DSF service design does not support lazy retrieval of context state (because isSuspened() is not async)
                else if (!e.state.validate()) {
                    IDataRequest req = new IDataRequest() {
                        public void cancel() {
                            if (reqs.remove(this) && reqs.isEmpty()) getContexts(ctx, rm, has_state);
                        }
                        public void done() {
                            if (reqs.remove(this) && reqs.isEmpty()) getContexts(ctx, rm, has_state);
                        }
                    };
                    reqs.add(req);
                    e.state.addWaitingRequest(req);
                }
            }
            if (reqs.isEmpty()) {
                ArrayList<ExecutionDMC> l = new ArrayList<ExecutionDMC>();
                for (ExecutionDMC e : cache.getData().values()) {
                    assert e.context.isValid();
                    RunControlContext c = e.context.getData();
                    if (c.hasState() == has_state) l.add(e);
                }
                rm.setData(l.toArray(new ExecutionDMC[l.size()]));
                rm.done();
            }
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }
    
    public Collection<TCFDSFExecutionDMC> getCachedContexts() {
        ArrayList<TCFDSFExecutionDMC> l = new ArrayList<TCFDSFExecutionDMC>();
        for (ExecutionDMC dmc : cache.values()) l.add(dmc);
        return l;
    }

    public void step(IDMContext context, StepType stepType, final RequestMonitor rm) {
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.context.isValid()) {
                RunControlContext c = ctx.context.getData();
                if (c != null) {
                    int md = -1;
                    if (c.canResume(com.windriver.tcf.api.services.IRunControl.RM_STEP_INTO_LINE)) {
                        switch (stepType) {
                        case STEP_OVER:
                            md = com.windriver.tcf.api.services.IRunControl.RM_STEP_OVER_LINE;
                            break;
                        case STEP_INTO:
                            md = com.windriver.tcf.api.services.IRunControl.RM_STEP_INTO_LINE;
                            break;
                        case STEP_RETURN:
                            md = com.windriver.tcf.api.services.IRunControl.RM_STEP_OUT;
                            break;
                        }
                    }
                    else {
                        switch (stepType) {
                        case STEP_OVER:
                            md = com.windriver.tcf.api.services.IRunControl.RM_STEP_OVER;
                            break;
                        case STEP_INTO:
                            md = com.windriver.tcf.api.services.IRunControl.RM_STEP_INTO;
                            break;
                        case STEP_RETURN:
                            md = com.windriver.tcf.api.services.IRunControl.RM_STEP_OUT;
                            break;
                        }
                    }
                    if (md < 0) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                NOT_SUPPORTED, "Invalid step type", null)); //$NON-NLS-1$
                        rm.done();
                    }
                    else {
                        c.resume(md, 1, new com.windriver.tcf.api.services.IRunControl.DoneCommand() {
                            public void doneCommand(IToken token, Exception error) {
                                if (rm.isCanceled()) return;
                                if (error != null) {
                                    rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                            REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                                }
                                ctx.is_stepping--;
                                rm.done();
                            }
                        });
                        ctx.is_stepping++;
                    }
                    return;
                }
            }
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void instructionStep(IDMContext context, StepType stepType, final RequestMonitor rm) {
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.context.isValid()) {
                RunControlContext c = ctx.context.getData();
                if (c != null) {
                    int md = -1;
                    switch (stepType) {
                    case STEP_OVER:
                        md = com.windriver.tcf.api.services.IRunControl.RM_STEP_OVER;
                        break;
                    case STEP_INTO:
                        md = com.windriver.tcf.api.services.IRunControl.RM_STEP_INTO;
                        break;
                    case STEP_RETURN:
                        md = com.windriver.tcf.api.services.IRunControl.RM_STEP_OUT;
                        break;
                    }
                    if (md < 0) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                NOT_SUPPORTED, "Invalid step type", null)); //$NON-NLS-1$
                        rm.done();
                    }
                    else {
                        c.resume(md, 1, new com.windriver.tcf.api.services.IRunControl.DoneCommand() {
                            public void doneCommand(IToken token, Exception error) {
                                if (rm.isCanceled()) return;
                                if (error != null) {
                                    rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                            REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                                }
                                ctx.is_stepping--;
                                rm.done();
                            }
                        });
                        ctx.is_stepping++;
                    }
                    return;
                }
            }
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public boolean isStepping(IDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC x = (ExecutionDMC)context;
            return x.is_stepping > 0;
        }
        return false;
    }

    public void resume(IDMContext context, final RequestMonitor rm) {
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.context.isValid()) {
                RunControlContext c = ctx.context.getData();
                if (c != null) {
                    c.resume(com.windriver.tcf.api.services.IRunControl.RM_RESUME, 1,
                            new com.windriver.tcf.api.services.IRunControl.DoneCommand() {
                        public void doneCommand(IToken token, Exception error) {
                            if (rm.isCanceled()) return;
                            if (error != null) {
                                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                        REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                            }
                            ctx.is_resuming--;
                            rm.done();
                        }
                    });
                    ctx.is_resuming++;
                    return;
                }
            }
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void suspend(IDMContext context, final RequestMonitor rm) {
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.context.isValid()) {
                RunControlContext c = ctx.context.getData();
                if (c != null) {
                    c.suspend(new com.windriver.tcf.api.services.IRunControl.DoneCommand() {
                        public void doneCommand(IToken token, Exception error) {
                            if (rm.isCanceled()) return;
                            if (error != null) {
                                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                        REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                            }
                            rm.done();
                        }
                    });
                    return;
                }
            }
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public boolean isSuspended(IDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)context;
            boolean r = false;
            if (ctx.context.isValid()) {
                RunControlContext c = ctx.context.getData();
                if (c != null && c.hasState()) {
                    if (ctx.is_resuming == 0 && ctx.is_stepping == 0 && ctx.state.isValid()) {
                        ExecutionState st = ctx.state.getData();
                        if (st != null) r = st.is_suspended;
                    }
                }
                else if (ctx.children.isValid()) {
                    Map<String,ExecutionDMC> m = ctx.children.getData();
                    if (m != null) {
                        for (ExecutionDMC e : m.values()) {
                            if (isSuspended(e)) r = true;
                        }
                    }
                }
            }
            return r;
        }
        return false;
    }

    public void getExecutionData(IExecutionDMContext dmc, DataRequestMonitor<IExecutionDMData> rm) {
        if (dmc instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)dmc;
            StateChangeReason r = StateChangeReason.UNKNOWN;
            if (ctx.state.isValid()) {
                ExecutionState st = ctx.state.getData();
                if (st != null && st.suspend_reason != null) {
                    r = toStateChangeReason(st.suspend_reason); 
                }
            }
            rm.setData(new ExecutionData(r));
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Given context: " + dmc + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        rm.done();
    }
}
