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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.ServiceDMContext;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.osgi.framework.BundleContext;

import com.windriver.tcf.dsf.core.Activator;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.protocol.Protocol;
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
            return model.get(trigger_id);
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
                ExecutionDMC n = model.get(id);
                if (n != null) n.onContextResumed();
            }
            for (String id : context_ids) {
                ExecutionDMC n = model.get(id);
                if (n != null && n.ctx.isContainer()) {
                    getSession().dispatchEvent(new ContainerResumedEvent(n), getProperties());
                }
            }
        }

        public void containerSuspended(String trigger_id, String pc,
                String reason, Map<String, Object> params,
                String[] suspended_ids) {
            if (trigger_id != null) {
                ExecutionDMC n = model.get(trigger_id);
                if (n != null) n.onContextSuspended(pc, reason, params);
            }
            for (String id : suspended_ids) {
                if (id.equals(trigger_id)) continue;
                ExecutionDMC n = model.get(id);
                if (n != null) n.onContainerSuspended(reason);
            }
            for (String id : suspended_ids) {
                ExecutionDMC n = model.get(id);
                if (n != null && n.ctx.isContainer()) {
                    getSession().dispatchEvent(new ContainerSuspendedEvent(n, trigger_id, reason), getProperties());
                }
            }
        }

        public void contextAdded(RunControlContext[] contexts) {
            for (RunControlContext ctx : contexts) {
                ExecutionDMC n = model.get(ctx.getParentID());
                if (n != null) n.onContextAdded(ctx);
            }
        }

        public void contextChanged(RunControlContext[] contexts) {
            for (RunControlContext ctx : contexts) {
                ExecutionDMC n = model.get(ctx.getID());
                if (n != null) n.onContextChanged(ctx);
            }
        }

        public void contextException(String id, String msg) {
            ExecutionDMC n = model.get(id);
            if (n != null) n.onContextException(msg);
        }

        public void contextRemoved(String[] context_ids) {
            for (String id : context_ids) {
                ExecutionDMC n = model.get(id);
                if (n != null) n.onContextRemoved();
            }
        }

        public void contextResumed(String id) {
            ExecutionDMC n = model.get(id);
            if (n != null) n.onContextResumed();
        }

        public void contextSuspended(String id, String pc, String reason, Map<String, Object> params) {
            ExecutionDMC n = model.get(id);
            if (n != null) n.onContextSuspended(pc, reason, params);
        }
    };
    
    private interface IDataRequest {
        void cancel();
        void done();
    }

    private static final int 
        VALID_CHILDREN = 4,
        VALID_CONTEXT = 8,
        VALID_STATE = 16,
        VALID_ALL = VALID_CHILDREN | VALID_CONTEXT | VALID_STATE;
    
    private class ExecutionDMC extends TCFDSFExecutionDMC {
        
        final String id;
        final ExecutionDMC parent;
        
        final SortedMap<String,ExecutionDMC> children = new TreeMap<String,ExecutionDMC>();
        final Map<String,ExecutionDMC> children_next = new HashMap<String,ExecutionDMC>();
        final Collection<IDataRequest> node_wait_list = new ArrayList<IDataRequest>();
    
        int valid;
        Throwable error;
        boolean disposed;
        IToken command;
        
        RunControlContext ctx;
        int is_stepping;
        int is_resuming;
        boolean is_suspended;
        boolean is_running;
        String suspend_pc;
        String suspend_reason;
        String exception_msg;
        Map<String,Object> suspend_params;
    
        public ExecutionDMC(ExecutionDMC parent, String id) {
            super(TCFDSFRunControl.this, parent == null ? null : new IDMContext[] { parent });
            this.parent = parent;
            this.id = id;
        }

        @Override
        public String toString() {
            return baseToString() + ".context[" + id + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public boolean equals(Object obj) {
            return super.baseEquals(obj) && ((ExecutionDMC)obj).id.equals(id);
        }
    
        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String getTcfContextId() {
            return id;
        }
        
        void dispose() {
            assert !disposed;
            ExecutionDMC arr[] = children.values().toArray(new ExecutionDMC[children.size()]);
            for (int i = 0; i < arr.length; i++) arr[i].dispose();
            assert children.isEmpty();
            if (parent != null) {
                parent.children.remove(id);
                parent.children_next.remove(id);
            }
            model.remove(id);
            disposed = true;
        }
        
        void invalidateDMC(int flags) {
            // cancel current data retrieval command
            if (command != null) {
                command.cancel();
                command = null;
            }

            // cancel waiting requests
            if (!node_wait_list.isEmpty()) {
                IDataRequest[] arr = node_wait_list.toArray(new IDataRequest[node_wait_list.size()]);
                node_wait_list.clear();
                for (IDataRequest r : arr) r.cancel();
            }

            if ((flags & VALID_STATE) != 0) {
                is_suspended = false;
                is_running = false;
            }

            if ((flags & VALID_CHILDREN) != 0) {
                children_next.clear();
                for (ExecutionDMC n : children.values()) n.invalidateDMC(VALID_ALL);
            }
            
            if (flags == VALID_ALL) { 
                error = null;
            }
            
            valid &= ~flags;
        }

        boolean validateDMC(IDataRequest done) {
            assert Protocol.isDispatchThread();
            assert (valid & ~VALID_ALL) == 0;
            assert parent == null || parent.children.get(id) == model.get(id);
            if (channel.getState() != IChannel.STATE_OPEN) {
                children_next.clear();
                error = null;
                command = null;
                valid = VALID_ALL;
            }
            if (command != null) {
                if (done != null) node_wait_list.add(done);
                return false;
            }
            if (parent != null && parent.error != null) {
                valid = VALID_ALL;
            }
            if ((valid & VALID_CONTEXT) == 0 && !validateRunControlContext(done)) return false;
            if ((valid & VALID_STATE) == 0 && !validateRunControlState(done)) return false;
            if ((valid & VALID_CHILDREN) == 0 && !validateRunControlChildren(done)) return false;
            assert valid == VALID_ALL;
            assert command == null;
            ExecutionDMC[] a = children.values().toArray(new ExecutionDMC[children.size()]);
            for (ExecutionDMC n : a) {
                if (children_next.get(n.id) == null) n.dispose();
            }
            for (ExecutionDMC n : children_next.values()) {
                if (children.get(n.id) == null) {
                    children.put(n.id, n);
                    model.put(n.id, n);
                }
            }
            if (!node_wait_list.isEmpty()) {
                IDataRequest[] arr = node_wait_list.toArray(new IDataRequest[node_wait_list.size()]);
                node_wait_list.clear();
                for (IDataRequest r : arr) r.done();
            }
            assert valid == VALID_ALL;
            return true;
        }

        private boolean validateRunControlChildren(IDataRequest done) {
            assert command == null;
            if (tcf_run_service == null) {
                valid |= VALID_CHILDREN;
                return true;
            }
            if (done != null) node_wait_list.add(done);
            command = tcf_run_service.getChildren(id, new IRunControl.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                    if (command != token) return;
                    command = null;
                    if (error != null) {
                        ExecutionDMC.this.error = error;
                    }
                    else {
                        for (int i = 0; i < contexts.length; i++) {
                            String id = contexts[i];
                            ExecutionDMC node = model.get(id);
                            if (node == null) node = new ExecutionDMC(ExecutionDMC.this, id);
                            children_next.put(id, node);
                        }
                    }
                    valid |= VALID_CHILDREN;
                    validateDMC(null);
                }
            });
            return false;
        }

        private boolean validateRunControlContext(IDataRequest done) {
            assert command == null;
            if (tcf_run_service == null) {
                valid |= VALID_CONTEXT;
                return true;
            }
            if (done != null) node_wait_list.add(done);
            command = tcf_run_service.getContext(id, new IRunControl.DoneGetContext() {
                public void doneGetContext(IToken token, Exception error, IRunControl.RunControlContext ctx) {
                    if (command != token) return;
                    command = null;
                    if (error != null) {
                        ExecutionDMC.this.error = error;
                    }
                    else {
                        ExecutionDMC.this.ctx = ctx;
                    }
                    valid |= VALID_CONTEXT;
                    validateDMC(null);
                }
            });
            return false;
        }

        private boolean validateRunControlState(IDataRequest done) {
            assert command == null;
            if (ctx == null) {
                valid |= VALID_STATE;
                return true;
            }
            if (error != null || !ctx.hasState()) {
                is_running = false;
                is_suspended = false;
                suspend_pc = null;
                suspend_reason = null;
                suspend_params = null;
                valid |= VALID_STATE;
                return true;
            }
            if (done != null) node_wait_list.add(done);
            command = ctx.getState(new IRunControl.DoneGetState() {
                public void doneGetState(IToken token, Exception error, boolean suspend, String pc, String reason, Map<String,Object> params) {
                    if (token != command) return;
                    command = null;
                    if (error != null) {
                        is_running = false;
                        is_suspended = false;
                        suspend_pc = null;
                        suspend_reason = null;
                        suspend_params = null;
                        ExecutionDMC.this.error = error;
                    }
                    else {
                        is_running = !suspend;
                        is_suspended = suspend;
                        if (suspend) {
                            suspend_pc = pc;
                            suspend_reason = reason;
                            suspend_params = params;
                        }
                        else {
                            suspend_pc = null;
                            suspend_reason = null;
                            suspend_params = null;
                        }
                    }
                    valid |= VALID_STATE;
                    validateDMC(null);
                }
            });
            return false;
        }
        
        /*--------------------------------------------------------------------------------------*/
        /* Events                                                                               */

        void onContextAdded(IRunControl.RunControlContext context) {
            String id = context.getID();
            assert !disposed;
            assert children.get(id) == null;
            ExecutionDMC n = new ExecutionDMC(this, id);
            n.ctx = context;
            n.valid |= VALID_CONTEXT;
            children.put(id, n);
            model.put(id, n);
            getSession().dispatchEvent(new StartedEvent(this, n), getProperties());
        }

        void onContextChanged(IRunControl.RunControlContext context) {
            assert !disposed;
            ctx = context;
            invalidateDMC(VALID_CHILDREN);
            getSession().dispatchEvent(new ChangedEvent(this), getProperties());
        }

        void onContextRemoved() {
            assert !disposed;
            dispose();
            getSession().dispatchEvent(new ExitedEvent(parent, this), getProperties());
        }

        void onContainerSuspended(String reason) {
            assert !disposed;
            if (ctx == null) return;
            if (!ctx.hasState()) return;
            invalidateDMC(VALID_STATE);
            getSession().dispatchEvent(new SuspendedEvent(this, reason), getProperties());
        }

        void onContextSuspended(String pc, String reason, Map<String,Object> params) {
            assert !disposed;
            if (ctx == null) return;
            assert ctx.hasState();
            is_suspended = true;
            suspend_pc = pc;
            suspend_reason = reason;
            suspend_params = params;
            is_running = false;
            valid |= VALID_STATE;
            getSession().dispatchEvent(new SuspendedEvent(this, reason), getProperties());
        }

        void onContextResumed() {
            assert !disposed;
            if (ctx == null) return;
            assert ctx.hasState();
            exception_msg = null;
            is_suspended = false;
            suspend_pc = null;
            suspend_reason = null;
            suspend_params = null;
            is_running = true;
            valid |= VALID_STATE;
            getSession().dispatchEvent(new ResumedEvent(this), getProperties());
        }

        void onContextException(String msg) {
            assert !disposed;
            exception_msg = msg;
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
    private final Map<String,ExecutionDMC> model = new HashMap<String,ExecutionDMC>();
    private IDMContext service_dmc;

    public TCFDSFRunControl(DsfSession session, IChannel channel) {
        super(session);
        this.channel = channel;
        tcf_run_service = channel.getRemoteService(com.windriver.tcf.api.services.IRunControl.class);
        if (tcf_run_service != null) tcf_run_service.addListener(run_listener);
        service_dmc = new ServiceDMContext(this, "#run_control");
    }

    @Override
    protected BundleContext getBundleContext() {
        return Activator.getBundleContext();
    }

    @SuppressWarnings("unchecked")
    public void getModelData(IDMContext dmc, final DataRequestMonitor<?> rm) {
        if (dmc instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)dmc;
            IDataRequest done = new IDataRequest() {

                public void cancel() {
                    rm.setCanceled(true);
                    rm.done();
                }

                @SuppressWarnings("unchecked")
                public void done() {
                    if (ctx.error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Data error", ctx.error)); //$NON-NLS-1$
                    }
                    else {
                        ExecutionData dt = new ExecutionData(toStateChangeReason(ctx.suspend_reason));
                        ((DataRequestMonitor<IExecutionDMData>)rm).setData(dt);
                    }
                    rm.done();
                }
            };
            if (ctx.validateDMC(done)) done.done();
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
    
    public IContainerDMContext getContainerDMC() {
        // TODO: getContainerDMC()
        assert false;
        return null;
    }

    public boolean canInstructionStep(IDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC x = (ExecutionDMC)context;
            return x.ctx.canResume(com.windriver.tcf.api.services.IRunControl.RM_STEP_INTO);
        }
        return false;
    }

    public boolean canResume(IDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC x = (ExecutionDMC)context;
            return x.ctx.canResume(com.windriver.tcf.api.services.IRunControl.RM_RESUME);
        }
        return false;
    }

    public boolean canStep(IDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC x = (ExecutionDMC)context;
            return x.ctx.canResume(com.windriver.tcf.api.services.IRunControl.RM_STEP_OVER);
        }
        return false;
    }

    public boolean canSuspend(IDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC x = (ExecutionDMC)context;
            return x.ctx.canSuspend();
        }
        return false;
    }

    public void getExecutionContexts(IContainerDMContext context, final DataRequestMonitor<IExecutionDMContext[]> rm) {
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC ctx = (ExecutionDMC)context;
            IDataRequest done = new IDataRequest() {

                public void cancel() {
                    rm.setCanceled(true);
                    rm.done();
                }

                @SuppressWarnings("unchecked")
                public void done() {
                    if (ctx.error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Data error", ctx.error)); //$NON-NLS-1$
                    }
                    else {
                        rm.setData(ctx.children.values().toArray(new ExecutionDMC[ctx.children.size()]));
                    }
                    rm.done();
                }
            };
            if (ctx.validateDMC(done)) done.done();
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void step(IDMContext context, StepType stepType, final RequestMonitor rm) {
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC x = (ExecutionDMC)context;
            int md = -1;
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
            if (md < 0) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        NOT_SUPPORTED, "Invalid step type", null)); //$NON-NLS-1$
                rm.done();
            }
            else {
                x.ctx.resume(md, 1, new com.windriver.tcf.api.services.IRunControl.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null) {
                            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                    REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                        }
                        x.is_stepping--;
                        rm.done();
                    }
                });
                x.is_stepping++;
            }
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void instructionStep(IDMContext context, StepType stepType, final RequestMonitor rm) {
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC x = (ExecutionDMC)context;
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
                x.ctx.resume(md, 1, new com.windriver.tcf.api.services.IRunControl.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null) {
                            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                    REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                        }
                        x.is_stepping--;
                        rm.done();
                    }
                });
                x.is_stepping++;
            }
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
            final ExecutionDMC x = (ExecutionDMC)context;
            x.ctx.resume(com.windriver.tcf.api.services.IRunControl.RM_RESUME, 1,
                    new com.windriver.tcf.api.services.IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                    }
                    x.is_resuming--;
                    rm.done();
                }
            });
            x.is_resuming++;
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void suspend(IDMContext context, final RequestMonitor rm) {
        if (context instanceof ExecutionDMC) {
            final ExecutionDMC x = (ExecutionDMC)context;
            x.ctx.suspend(new com.windriver.tcf.api.services.IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
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

    public boolean isSuspended(IDMContext context) {
        if (context instanceof ExecutionDMC) {
            ExecutionDMC x = (ExecutionDMC)context;
            return x.is_suspended && x.is_resuming == 0 && x.is_stepping == 0;
        }
        return false;
    }

    public void getExecutionData(IExecutionDMContext dmc, DataRequestMonitor<IExecutionDMData> rm) {
        // TODO Auto-generated method stub
        assert false;
    }
}
