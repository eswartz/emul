/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
import org.eclipse.dd.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.tm.internal.tcf.debug.actions.TCFActionStepInto;
import org.eclipse.tm.internal.tcf.debug.actions.TCFActionStepOut;
import org.eclipse.tm.internal.tcf.debug.actions.TCFActionStepOver;
import org.eclipse.tm.internal.tcf.debug.model.ITCFConstants;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.dsf.Activator;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tm.tcf.services.IStackTrace.StackTraceContext;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.osgi.framework.BundleContext;


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

        public StateChangeReason getReason() {
            return reason;
        }

        public IExecutionDMContext[] getTriggeringContexts() {
            IExecutionDMContext ctx = trigger_id == null ? null : cache.get(trigger_id);
            return ctx == null ? new IExecutionDMContext[0] : new IExecutionDMContext[]{ ctx };
        }
    }

    public class ContainerResumedEvent extends AbstractDMEvent<IExecutionDMContext> implements IContainerResumedDMEvent {

        private final String trigger_id;

        public ContainerResumedEvent(IExecutionDMContext dmc, String trigger_id) {
            super(dmc);
            this.trigger_id = trigger_id;
        }

        public StateChangeReason getReason() {
            return StateChangeReason.USER_REQUEST;
        }

        public IExecutionDMContext[] getTriggeringContexts() {
            IExecutionDMContext ctx = trigger_id == null ? null : cache.get(trigger_id);
            return ctx == null ? new IExecutionDMContext[0] : new IExecutionDMContext[]{ ctx };
        }
    }

    public static class StartedEvent extends AbstractDMEvent<IExecutionDMContext> implements IStartedDMEvent {

        public StartedEvent(IExecutionDMContext dmc) {
            super(dmc);
        }
    }

    public static class ChangedEvent extends AbstractDMEvent<IExecutionDMContext> {

        public ChangedEvent(IExecutionDMContext dmc) {
            super(dmc);
        }
    }

    public static class ExitedEvent extends AbstractDMEvent<IExecutionDMContext> implements IExitedDMEvent {

        public ExitedEvent(IContainerDMContext dmc) {
            super(dmc);
        }
    }

    private final org.eclipse.tm.tcf.services.IRunControl.RunControlListener run_listener =
        new org.eclipse.tm.tcf.services.IRunControl.RunControlListener() {

        public void containerResumed(String[] context_ids) {
            for (String id : context_ids) {
                ExecutionDMC n = cache.get(id);
                if (n != null) n.onContextResumed();
            }
            for (String id : context_ids) {
                ExecutionDMC n = cache.get(id);
                if (n != null && n.run_control_context_cache.isValid()) {
                    RunControlContext c = n.run_control_context_cache.getData();
                    if (c.isContainer()) {
                        getSession().dispatchEvent(new ContainerResumedEvent(n, null), getProperties());
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
                if (n != null && n.run_control_context_cache.isValid()) {
                    RunControlContext c = n.run_control_context_cache.getData();
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

    private class ExecutionDMC extends TCFDSFExecutionDMC {

        final String id;
        final ExecutionDMC parent;
        final IMemoryBlockRetrievalExtension mem_retrieval;

        boolean disposed;
        int is_stepping;
        int is_resuming;

        public ExecutionDMC(ExecutionDMC parent, final String id) {
            super(channel, TCFDSFRunControl.this, parent == null ?
                    new IDMContext[0] : new IDMContext[] { parent });
            this.parent = parent;
            this.id = id;
            DsfMemoryBlockRetrieval mr = null;
            try {
                mr = new DsfMemoryBlockRetrieval(ITCFConstants.ID_TCF_DEBUG_MODEL, config, getSession());
            }
            catch (DebugException e) {
                e.printStackTrace();
            };
            mem_retrieval = mr;
        }

        @Override
        protected TCFDSFExecutionDMC addChild(String id) {
            ExecutionDMC n = cache.get(id);
            if (n == null) {
                n = new ExecutionDMC(ExecutionDMC.this, id);
                cache.put(id, n);
            }
            return n;
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
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public void dispose() {
            assert !disposed;
            run_control_context_cache.cancel();
            run_control_children_cache.cancel();
            run_control_state_cache.cancel();
            if (run_control_children_cache.isValid()) {
                Map<String,TCFDSFExecutionDMC> m = run_control_children_cache.getData();
                if (m != null) {
                    for (TCFDSFExecutionDMC n : m.values()) n.dispose();
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
            n.run_control_context_cache.reset(c);
            if (run_control_children_cache.isValid()) {
                Map<String,TCFDSFExecutionDMC> m = run_control_children_cache.getData();
                if (m != null) m.put(id, n);
            }
            cache.put(id, n);
            getSession().dispatchEvent(new StartedEvent(n), getProperties());
        }

        void onContextChanged(IRunControl.RunControlContext c) {
            assert !disposed;
            run_control_context_cache.reset(c);
            getSession().dispatchEvent(new ChangedEvent(this), getProperties());
        }

        void onContextRemoved() {
            assert !disposed;
            if (parent != null && parent.run_control_children_cache.isValid()) {
                Map<String,TCFDSFExecutionDMC> m = parent.run_control_children_cache.getData();
                if (m != null) m.remove(id);
            }
            dispose();
            getSession().dispatchEvent(new ExitedEvent(this), getProperties());
        }

        void onContainerSuspended(String reason) {
            assert !disposed;
            if (!run_control_context_cache.isValid()) return;
            RunControlContext rc = run_control_context_cache.getData();
            if (rc == null) return;
            if (!rc.hasState()) return;
            run_control_state_cache.reset();
            getSession().dispatchEvent(new SuspendedEvent(this, reason), getProperties());
        }

        void onContextSuspended(String pc, String reason, Map<String,Object> params) {
            assert !disposed;
            assert !run_control_context_cache.isValid() || run_control_context_cache.getData().hasState();
            TCFContextState st = new TCFContextState();
            st.is_suspended = true;
            st.suspend_pc = pc;
            st.suspend_reason = reason;
            st.suspend_params = params;
            run_control_state_cache.reset(st);
            getSession().dispatchEvent(new SuspendedEvent(this, reason), getProperties());
        }

        void onContextResumed() {
            assert !disposed;
            assert !run_control_context_cache.isValid() || run_control_context_cache.getData().hasState();
            TCFContextState st = new TCFContextState();
            run_control_state_cache.reset(st);
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
        if (s.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_USER_REQUEST)) return StateChangeReason.USER_REQUEST;
        if (s.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_STEP)) return StateChangeReason.STEP;
        if (s.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_BREAKPOINT)) return StateChangeReason.BREAKPOINT;
        if (s.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_EXCEPTION)) return StateChangeReason.EXCEPTION;
        if (s.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_CONTAINER)) return StateChangeReason.CONTAINER;
        if (s.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_WATCHPOINT)) return StateChangeReason.WATCHPOINT;
        if (s.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_SIGNAL)) return StateChangeReason.SIGNAL;
        if (s.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_SHAREDLIB)) return StateChangeReason.SHAREDLIB;
        if (s.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_ERROR)) return StateChangeReason.ERROR;
        return StateChangeReason.UNKNOWN;
    }

    private final ILaunchConfiguration config;
    private final TCFLaunch launch;
    private final IChannel channel;
    private final org.eclipse.tm.tcf.services.IRunControl tcf_run_service;
    private final Map<String,ExecutionDMC> cache = new HashMap<String,ExecutionDMC>();
    private final ExecutionDMC root_dmc;

    public TCFDSFRunControl(ILaunchConfiguration config, TCFLaunch launch,
            DsfSession session, IChannel channel, final RequestMonitor monitor) {
        super(session);
        this.config = config;
        this.launch = launch;
        this.channel = channel;
        tcf_run_service = channel.getRemoteService(org.eclipse.tm.tcf.services.IRunControl.class);
        if (tcf_run_service != null) tcf_run_service.addListener(run_listener);
        root_dmc = new ExecutionDMC(null, null);
        cache.put(null, root_dmc);
        initialize(new RequestMonitor(getExecutor(), monitor) {
            @Override
            protected void handleSuccess() {
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
            if (!ctx.run_control_context_cache.validate()) {
                ctx.run_control_context_cache.wait(new Runnable() {
                    public void run() {
                        getModelData(ctx, rm);
                    }
                });
                return;
            }
            if (ctx.run_control_context_cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", ctx.run_control_context_cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            if (ctx.run_control_context_cache.getData() == null) {
                ExecutionData dt = new ExecutionData(StateChangeReason.UNKNOWN);
                ((DataRequestMonitor<IExecutionDMData>)rm).setData(dt);
                rm.done();
                return;
            }
            if (!ctx.run_control_context_cache.getData().hasState()) {
                ExecutionData dt = new ExecutionData(StateChangeReason.UNKNOWN);
                ((DataRequestMonitor<IExecutionDMData>)rm).setData(dt);
                rm.done();
                return;
            }
            if (!ctx.run_control_state_cache.validate()) {
                ctx.run_control_state_cache.wait(new Runnable() {
                    public void run() {
                        getModelData(ctx, rm);
                    }
                });
                return;
            }
            if (ctx.run_control_state_cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", ctx.run_control_state_cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            if (ctx.run_control_state_cache.getData() == null) {
                ExecutionData dt = new ExecutionData(StateChangeReason.UNKNOWN);
                ((DataRequestMonitor<IExecutionDMData>)rm).setData(dt);
                rm.done();
                return;
            }
            ExecutionData dt = new ExecutionData(toStateChangeReason(ctx.run_control_state_cache.getData().suspend_reason));
            ((DataRequestMonitor<IExecutionDMData>)rm).setData(dt);
            rm.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public boolean isValid() {
        return true;
    }

    public void canResume(final IExecutionDMContext context, final DataRequestMonitor<Boolean> rm) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)context;
            if (!ctx.run_control_context_cache.validate()) {
                ctx.run_control_context_cache.wait(new Runnable() {
                    public void run() {
                        canResume(context, rm);
                    }
                });
            }
            else {
                RunControlContext c = ctx.run_control_context_cache.getData();
                rm.setData(c != null && c.canResume(org.eclipse.tm.tcf.services.IRunControl.RM_RESUME));
                rm.done();
            }
        }
        else {
            rm.setData(false);
            rm.done();
        }
    }

    public void canStep(final IExecutionDMContext context, final StepType step_type, final DataRequestMonitor<Boolean> rm) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)context;
            if (!ctx.run_control_context_cache.validate()) {
                ctx.run_control_context_cache.wait(new Runnable() {
                    public void run() {
                        canStep(context, step_type, rm);
                    }
                });
            }
            else {
                RunControlContext c = ctx.run_control_context_cache.getData();
                int md = toTCFStepType(step_type);
                boolean b = c != null && c.canResume(md);
                if (!b && c != null) {
                    // Check if can emulate desired step type
                    // TODO: check breakpoints service - it is needed to emulate step commands
                    switch (md) {
                    case org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OVER_LINE:
                        b = c.canResume(org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OVER) ||
                            c.canResume(org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO);
                        break;
                    case org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OVER:
                        b = c.canResume(org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO);
                        break;
                    case org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO_LINE:
                        b = c.canResume(org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO);
                        break;
                    case org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OUT:
                        b = c.canResume(org.eclipse.tm.tcf.services.IRunControl.RM_RESUME);
                        break;
                    }
                }
                rm.setData(b);
                rm.done();
            }
        }
        else {
            rm.setData(false);
            rm.done();
        }
    }

    public void canSuspend(final IExecutionDMContext context, final DataRequestMonitor<Boolean> rm) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)context;
            if (!ctx.run_control_context_cache.validate()) {
                ctx.run_control_context_cache.wait(new Runnable() {
                    public void run() {
                        canSuspend(context, rm);
                    }
                });
            }
            else {
                RunControlContext c = ctx.run_control_context_cache.getData();
                rm.setData(c != null && c.canSuspend());
                rm.done();
            }
        }
        else {
            rm.setData(false);
            rm.done();
        }
    }

    public TCFDSFExecutionDMC getContext(String id) {
        return cache.get(id);
    }

    public void getContainerContexts(IContainerDMContext context, final DataRequestMonitor<IExecutionDMContext[]> rm) {
        getContexts(context, rm, false, false);
    }

    public void getExecutionContexts(IContainerDMContext context, final DataRequestMonitor<IExecutionDMContext[]> rm) {
        getContexts(context, rm, false, true);
    }

    public void getAllContexts(IContainerDMContext context, final DataRequestMonitor<IExecutionDMContext[]> rm) {
        getContexts(context, rm, true, true);
    }

    public void getContexts(IContainerDMContext context,
            final DataRequestMonitor<IExecutionDMContext[]> rm,
            final boolean all, final boolean has_state) {
        if (context == null) context = root_dmc;
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)context;
            TCFDataCache<Map<String,TCFDSFExecutionDMC>> cache = ctx.run_control_children_cache;
            if (!cache.validate()) {
                cache.wait(new Runnable() {
                    public void run() {
                        getContexts(ctx, rm, all, has_state);
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
            final Set<Runnable> reqs = new HashSet<Runnable>();
            for (TCFDSFExecutionDMC e : cache.getData().values()) {
                if (!e.run_control_context_cache.validate()) {
                    Runnable req = new Runnable() {
                        public void run() {
                            if (reqs.remove(this) && reqs.isEmpty()) getContexts(ctx, rm, all, has_state);
                        }
                    };
                    reqs.add(req);
                    e.run_control_context_cache.wait(req);
                }
                // TODO DSF service design does not support lazy retrieval of context state (because isSuspened() is not async)
                else if (!e.run_control_state_cache.validate()) {
                    Runnable req = new Runnable() {
                        public void run() {
                            if (reqs.remove(this) && reqs.isEmpty()) getContexts(ctx, rm, all, has_state);
                        }
                    };
                    reqs.add(req);
                    e.run_control_state_cache.wait(req);
                }
            }
            if (reqs.isEmpty()) {
                ArrayList<TCFDSFExecutionDMC> l = new ArrayList<TCFDSFExecutionDMC>();
                for (TCFDSFExecutionDMC e : cache.getData().values()) {
                    assert e.run_control_context_cache.isValid();
                    RunControlContext c = e.run_control_context_cache.getData();
                    if (all || has_state == c.hasState()) l.add(e);
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

    public int toTCFStepType(StepType step_type) {
        switch (step_type) {
        case STEP_OVER:
            return org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OVER_LINE;
        case STEP_INTO:
            return org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO_LINE;
        case STEP_RETURN:
            return org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OUT;
        case INSTRUCTION_STEP_OVER:
            return org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OVER;
        case INSTRUCTION_STEP_INTO:
            return org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO;
        case INSTRUCTION_STEP_RETUTRN:
            return org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OUT;
        }
        return -1;
    }

    private class StepIntoAction extends TCFActionStepInto {

        private final ExecutionDMC ctx;
        private final RequestMonitor monitor;

        private TCFDSFStack.TCFFrameDMC frame;

        StepIntoAction(TCFLaunch launch, ExecutionDMC ctx, RequestMonitor monitor, boolean src_step) {
            super(launch, ctx.run_control_context_cache.getData(), src_step);
            this.ctx = ctx;
            this.monitor = monitor;
            ctx.is_stepping++;
        }

        @Override
        protected TCFDataCache<TCFContextState> getContextState() {
            return ctx.run_control_state_cache;
        }

        @Override
        protected TCFDataCache<TCFSourceRef> getLineInfo() {
            if (frame == null) {
                TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
                if (service == null) return null;
                frame = service.getTopFrame(ctx);
                if (frame == null) return null;
            }
            return frame.source_cache;
        }

        @Override
        protected TCFDataCache<StackTraceContext> getStackFrame() {
            if (frame == null) {
                TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
                if (service == null) return null;
                frame = service.getTopFrame(ctx);
                if (frame == null) return null;
            }
            return frame.context_cache;
        }

        @Override
        protected int getStackFrameIndex() {
            if (frame == null) {
                TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
                if (service == null) return 0;
                frame = service.getTopFrame(ctx);
                if (frame == null) return 0;
            }
            return frame.getLevel();
        }

        @Override
        protected TCFDataCache<?> getStackTrace() {
            TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
            if (service == null) return null;
            return service.getFramesCache(ctx, null);
        }

        @Override
        protected void exit(Throwable error) {
            if (exited) return;
            super.exit(error);
            ctx.is_stepping--;
            if (error != null) {
                monitor.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
            }
            monitor.done();
        }
    }

    private class StepOverAction extends TCFActionStepOver {

        private final ExecutionDMC ctx;
        private final RequestMonitor monitor;

        private TCFDSFStack.TCFFrameDMC frame;

        StepOverAction(TCFLaunch launch, ExecutionDMC ctx, RequestMonitor monitor, boolean src_step) {
            super(launch, ctx.run_control_context_cache.getData(), src_step);
            this.ctx = ctx;
            this.monitor = monitor;
            ctx.is_stepping++;
        }

        @Override
        protected TCFDataCache<TCFContextState> getContextState() {
            return ctx.run_control_state_cache;
        }

        @Override
        protected TCFDataCache<TCFSourceRef> getLineInfo() {
            if (frame == null) {
                TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
                if (service == null) return null;
                frame = service.getTopFrame(ctx);
                if (frame == null) return null;
            }
            return frame.source_cache;
        }

        @Override
        protected TCFDataCache<StackTraceContext> getStackFrame() {
            if (frame == null) {
                TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
                if (service == null) return null;
                frame = service.getTopFrame(ctx);
                if (frame == null) return null;
            }
            return frame.context_cache;
        }

        @Override
        protected int getStackFrameIndex() {
            if (frame == null) {
                TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
                if (service == null) return 0;
                frame = service.getTopFrame(ctx);
                if (frame == null) return 0;
            }
            return frame.getLevel();
        }

        @Override
        protected TCFDataCache<?> getStackTrace() {
            TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
            if (service == null) return null;
            return service.getFramesCache(ctx, null);
        }

        @Override
        protected void exit(Throwable error) {
            if (exited) return;
            super.exit(error);
            ctx.is_stepping--;
            if (error != null) {
                monitor.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
            }
            monitor.done();
        }
    }

    private class StepOutAction extends TCFActionStepOut {

        private final ExecutionDMC ctx;
        private final RequestMonitor monitor;

        private TCFDSFStack.TCFFrameDMC frame;

        StepOutAction(TCFLaunch launch, ExecutionDMC ctx, RequestMonitor monitor) {
            super(launch, ctx.run_control_context_cache.getData());
            this.ctx = ctx;
            this.monitor = monitor;
            ctx.is_stepping++;
        }

        @Override
        protected TCFDataCache<TCFContextState> getContextState() {
            return ctx.run_control_state_cache;
        }

        @Override
        protected TCFDataCache<StackTraceContext> getStackFrame() {
            if (frame == null) {
                TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
                if (service == null) return null;
                frame = service.getTopFrame(ctx);
                if (frame == null) return null;
            }
            return frame.context_cache;
        }

        @Override
        protected int getStackFrameIndex() {
            if (frame == null) {
                TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
                if (service == null) return 0;
                frame = service.getTopFrame(ctx);
                if (frame == null) return 0;
            }
            return frame.getLevel();
        }

        @Override
        protected TCFDataCache<?> getStackTrace() {
            TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
            if (service == null) return null;
            return service.getFramesCache(ctx, null);
        }

        @Override
        protected void exit(Throwable error) {
            if (exited) return;
            super.exit(error);
            ctx.is_stepping--;
            if (error != null) {
                monitor.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
            }
            monitor.done();
        }
    }

    public void step(final IExecutionDMContext context, final StepType step_type, final RequestMonitor rm) {
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)context;
            if (!ctx.run_control_context_cache.validate()) {
                ctx.run_control_context_cache.wait(new Runnable() {
                    public void run() {
                        step(context, step_type, rm);
                    }
                });
                return;
            }
            if (ctx.run_control_context_cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", ctx.run_control_context_cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            RunControlContext c = ctx.run_control_context_cache.getData();
            if (c != null) {
                int md = toTCFStepType(step_type);
                if (md < 0) {
                    rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            NOT_SUPPORTED, "Invalid step type", null)); //$NON-NLS-1$
                    rm.done();
                }
                else {
                    switch (md) {
                    case org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO:
                        new StepIntoAction(launch, ctx, rm, false);
                        return;
                    case org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO_LINE:
                        new StepIntoAction(launch, ctx, rm, true);
                        return;
                    case org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OVER:
                        new StepOverAction(launch, ctx, rm, false);
                        return;
                    case org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OVER_LINE:
                        new StepOverAction(launch, ctx, rm, true);
                        return;
                    case org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OUT:
                        new StepOutAction(launch, ctx, rm);
                        return;
                    }
                    rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            NOT_SUPPORTED, "Invalid step type", null)); //$NON-NLS-1$
                    rm.done();
                }
                return;
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

    public boolean isStepping(IExecutionDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC x = (ExecutionDMC)context;
            return x.is_stepping > 0;
        }
        return false;
    }

    public void resume(IExecutionDMContext context, final RequestMonitor rm) {
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.run_control_context_cache.isValid()) {
                RunControlContext c = ctx.run_control_context_cache.getData();
                if (c != null) {
                    c.resume(org.eclipse.tm.tcf.services.IRunControl.RM_RESUME, 1,
                            new org.eclipse.tm.tcf.services.IRunControl.DoneCommand() {
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

    public void suspend(IExecutionDMContext context, final RequestMonitor rm) {
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.run_control_context_cache.isValid()) {
                RunControlContext c = ctx.run_control_context_cache.getData();
                if (c != null) {
                    c.suspend(new org.eclipse.tm.tcf.services.IRunControl.DoneCommand() {
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

    public boolean hasState(IExecutionDMContext context) {
        boolean r = false;
        if (context instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.run_control_context_cache.isValid()) {
                RunControlContext c = ctx.run_control_context_cache.getData();
                return c != null && c.hasState();
            }
        }
        return r;
    }

    public boolean isSuspended(IExecutionDMContext context) {
        boolean r = false;
        if (context instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)context;
            if (ctx.run_control_context_cache.isValid()) {
                RunControlContext c = ctx.run_control_context_cache.getData();
                if (c != null && c.hasState()) {
                    if (ctx.is_resuming == 0 && ctx.is_stepping == 0 && ctx.run_control_state_cache.isValid()) {
                        TCFContextState st = ctx.run_control_state_cache.getData();
                        if (st != null) r = st.is_suspended;
                    }
                }
            }
        }
        return r;
    }

    public void getExecutionData(IExecutionDMContext dmc, DataRequestMonitor<IExecutionDMData> rm) {
        if (dmc instanceof ExecutionDMC) {
            ExecutionDMC ctx = (ExecutionDMC)dmc;
            StateChangeReason r = StateChangeReason.UNKNOWN;
            if (ctx.run_control_state_cache.isValid()) {
                TCFContextState st = ctx.run_control_state_cache.getData();
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
