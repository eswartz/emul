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
package com.windriver.debug.tcf.ui.model;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.ui.IDebugUIConstants;

import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.services.IMemory;
import com.windriver.tcf.api.services.IRunControl;

public class TCFNodeExecContext extends TCFNode {
    
    private final TCFChildren children_exec;
    private final TCFChildren children_stack;
    private final TCFChildren children_regs;
    
    private IMemory.MemoryContext mem_context;
    private IRunControl.RunControlContext run_context;

    private boolean suspended;
    private String suspended_pc;
    private String suspended_reason;
    @SuppressWarnings("unused")
    private Map<String,Object> suspended_params;
    private boolean running;
    private boolean terminated;
    @SuppressWarnings("unused")
    private String exception_msg;
    
    private boolean valid_mem_ctx;
    private boolean valid_run_ctx;
    private boolean valid_state;

    TCFNodeExecContext(TCFNode parent, String id) {
        super(parent, id);
        children_exec = new TCFChildrenExecContext(this);
        children_regs = new TCFChildrenRegisters(this);
        children_stack = new TCFChildrenStackTrace(this, children_regs);
    }

    @Override
    void dispose() {
        children_exec.dispose();
        children_stack.dispose();
        children_regs.dispose();
        super.dispose();
    }
    
    @Override
    void dispose(String id) {
        children_exec.dispose(id);
        children_stack.dispose(id);
        children_regs.dispose(id);
    }

    @Override
    public IRunControl.RunControlContext getRunContext() {
        assert Protocol.isDispatchThread();
        return run_context;
    }

    @Override
    public IMemory.MemoryContext getMemoryContext() {
        assert Protocol.isDispatchThread();
        return mem_context;
    }

    @Override
    public boolean isRunning() {
        assert Protocol.isDispatchThread();
        return running;
    }

    @Override
    public boolean isSuspended() {
        assert Protocol.isDispatchThread();
        return suspended;
    }

    @Override
    public String getAddress() {
        assert Protocol.isDispatchThread();
        return suspended_pc;
    }

    @Override
    protected void getData(IChildrenCountUpdate result) {
        if (run_context != null && run_context.hasState()) {
            if (IDebugUIConstants.ID_REGISTER_VIEW.equals(result.getPresentationContext().getId())) {
                result.setChildCount(children_regs.size());
            }
            else {
                result.setChildCount(children_stack.size());
            }
        }
        else {
            result.setChildCount(children_exec.size());
        }
    }
    
    @Override
    protected void getData(IChildrenUpdate result) {
        int offset = 0;
        TCFNode[] arr = null;
        if (run_context != null && run_context.hasState()) {
            if (IDebugUIConstants.ID_REGISTER_VIEW.equals(result.getPresentationContext().getId())) {
                arr = children_regs.toArray();
            }
            else {
                arr = children_stack.toArray();
            }
        }
        else {
            arr = children_exec.toArray();
        }
        Arrays.sort(arr);
        for (TCFNode n : arr) {
            if (offset >= result.getOffset() && offset < result.getOffset() + result.getLength()) {
                result.setChild(n, offset);
            }
            offset++;
        }
    }
    
    @Override
    protected void getData(IHasChildrenUpdate result) {
        if (run_context != null && run_context.hasState()) {
            if (IDebugUIConstants.ID_REGISTER_VIEW.equals(result.getPresentationContext().getId())) {
                result.setHasChilren(children_regs.size() > 0);
            }
            else {
                result.setHasChilren(children_stack.size() > 0);
            }
        }
        else {
            result.setHasChilren(children_exec.size() > 0);
        }
    }
    
    @Override
    protected void getData(ILabelUpdate result) {
        result.setImageDescriptor(getImageDescriptor(getImageName()), 0);
        String label = id;
        if (run_context != null) {
            if (run_context.hasState()) {
                if (running) {
                    label += " (Running)";
                }
                else if (suspended) {
                    if (suspended_reason != null) {
                        label += " (" + suspended_reason + ")";
                    }
                    else {
                        label += " (Suspended)";
                    }
                }
            }
            String file = (String)run_context.getProperties().get("File");
            if (file != null) label += " " + file;
        }
        result.setLabel(label, 0);
    }

    @Override
    ModelDelta makeModelDelta(int flags) {
        if (run_context != null && run_context.isContainer()) flags |= IModelDelta.STATE;
        return super.makeModelDelta(flags);
    }
    
    @Override
    void onContextAdded(IRunControl.RunControlContext context) {
        assert !disposed;
        if (node_valid == CF_ALL) {
            String id = context.getID();
            TCFNodeExecContext n = (TCFNodeExecContext)children_exec.children.get(id);
            if (n == null) {
                n = new TCFNodeExecContext(this, id);
                n.run_context = context;
                n.valid_run_ctx = true;
                children_exec.children.put(id, n);
                model.addNode(id, n);
                n.makeModelDelta(IModelDelta.INSERTED);
            }
            else {
                n.run_context = context;
                n.makeModelDelta(IModelDelta.STATE);
            }
        }
        else {
            invalidateNode(CF_CHILDREN);
            makeModelDelta(IModelDelta.CONTENT);
        }
    }

    void onContextChanged(IRunControl.RunControlContext context) {
        assert !disposed;
        run_context = context;
        invalidateNode(CF_CHILDREN);
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    @Override
    void onContextAdded(IMemory.MemoryContext context) {
        assert !disposed;
        if (node_valid == CF_ALL) {
            String id = context.getID();
            TCFNodeExecContext n = (TCFNodeExecContext)children_exec.children.get(id);
            if (n == null) {
                n = new TCFNodeExecContext(this, id);
                n.mem_context = context;
                n.valid_mem_ctx = true;
                children_exec.children.put(id, n);
                model.addNode(id, n);
                n.makeModelDelta(IModelDelta.INSERTED);
            }
            else {
                n.mem_context = context;
                n.makeModelDelta(IModelDelta.STATE);
            }
        }
        else { 
            invalidateNode(CF_CHILDREN);
            makeModelDelta(IModelDelta.CONTENT);
        }
    }

    void onContextChanged(IMemory.MemoryContext context) {
        assert !disposed;
        mem_context = context;
        invalidateNode(CF_CHILDREN);
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextRemoved() {
        assert !disposed;
        dispose();
        if (parent.node_valid == CF_ALL) {
            makeModelDelta(IModelDelta.REMOVED);
        }
        else {
            parent.invalidateNode(CF_CHILDREN);
            parent.makeModelDelta(IModelDelta.CONTENT);
        }
    }

    void onContainerSuspended() {
        assert !disposed;
        if (run_context == null) return;
        if (!run_context.hasState()) return;
        suspended = false;
        running = false;
        valid_state = false;
        invalidateNode(CF_CHILDREN);
        suspended = true;
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContainerResumed() {
        assert !disposed;
        if (run_context == null) return;
        if (!run_context.hasState()) return;
        suspended = false;
        running = false;
        valid_state = false;
        invalidateNode(CF_CHILDREN);
        suspended = false;
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextSuspended(String pc, String reason, Map<String,Object> params) {
        assert !disposed;
        if (run_context == null) return;
        if (!run_context.hasState()) return;
        invalidateNode(CF_CHILDREN);
        suspended = true;
        suspended_pc = pc;
        suspended_reason = reason;
        suspended_params = params;
        running = false;
        valid_state = true;
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextResumed() {
        assert !disposed;
        if (run_context == null) return;
        if (!run_context.hasState()) return;
        invalidateNode(CF_CHILDREN);
        exception_msg = null;
        terminated = false;
        suspended = false;
        suspended_pc = null;
        suspended_reason = null;
        suspended_params = null;
        running = true;
        valid_state = true;
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextException(String msg) {
        assert !disposed;
        exception_msg = msg;
        makeModelDelta(IModelDelta.STATE);
    }

    void onMemoryChanged(Number[] addr, long[] size) {
        assert !disposed;
    }
    
    @Override
    protected void invalidateNode(int flags) {
        super.invalidateNode(flags);
        if ((flags & CF_CONTEXT) != 0) {
            valid_mem_ctx = false;
            valid_run_ctx = false;
            valid_state = false;
            running = false;
            suspended = false;
        }
        if ((flags & CF_CHILDREN) != 0) {
            children_exec.invalidate();
            children_stack.invalidate();
            children_regs.invalidate();
        }
    }
    
    @Override
    protected boolean validateContext(TCFRunnable done) {
        if (!valid_mem_ctx && !validateMemoryContext(done)) return false;
        if (!valid_run_ctx && !validateRunControlContext(done)) return false;
        if (!valid_state && !validateRunControlState(done)) return false;
        node_valid |= CF_CONTEXT;
        return true;
    }
    
    @Override
    protected boolean validateChildren(TCFRunnable done) {
        if (!children_stack.valid && !children_stack.validate(done)) return false;
        if (!children_regs.valid && !children_regs.validate(done)) return false;
        if (!children_exec.valid && !children_exec.validate(done)) return false;
        node_valid |= CF_CHILDREN;
        return true;
    }

    private boolean validateMemoryContext(TCFRunnable done) {
        assert data_command == null;
        IMemory mem = model.getLaunch().getService(IMemory.class);
        if (mem == null) {
            valid_mem_ctx = true;
            return true;
        }
        if (done != null) wait_list.add(done);
        data_command = mem.getContext(id, new IMemory.DoneGetContext() {
            public void doneGetContext(IToken token, Exception error, IMemory.MemoryContext context) {
                if (data_command != token) return;
                data_command = null;
                if (error != null) {
                    node_error = error;
                }
                else {
                    mem_context = context;
                }
                valid_mem_ctx = true;
                validateNode(null);
            }
        });
        return false;
    }

    private boolean validateRunControlContext(TCFRunnable done) {
        assert data_command == null;
        IRunControl run = model.getLaunch().getService(IRunControl.class);
        if (run == null) {
            valid_run_ctx = true;
            return true;
        }
        if (done != null) wait_list.add(done);
        data_command = run.getContext(id, new IRunControl.DoneGetContext() {
            public void doneGetContext(IToken token, Exception error, IRunControl.RunControlContext context) {
                if (data_command != token) return;
                data_command = null;
                if (error != null) {
                    node_error = error;
                }
                else {
                    run_context = context;
                }
                valid_run_ctx = true;
                validateNode(null);
            }
        });
        return false;
    }

    private boolean validateRunControlState(TCFRunnable done) {
        assert data_command == null;
        if (node_error != null || run_context == null || !run_context.hasState()) {
            suspended = false;
            suspended_pc = null;
            suspended_reason = null;
            suspended_params = null;
            running = false;
            valid_state = true;
            return true;
        }
        if (done != null) wait_list.add(done);
        data_command = run_context.getState(new IRunControl.DoneGetState() {
            public void doneGetState(IToken token, Exception error, boolean suspend, String pc, String reason, Map<String,Object> params) {
                if (token != data_command) return;
                data_command = null;
                if (error != null) {
                    suspended = false;
                    suspended_pc = null;
                    suspended_reason = null;
                    suspended_params = null;
                    node_error = error;
                    running = false;
                }
                else {
                    suspended = suspend;
                    if (suspend) {
                        suspended_pc = pc;
                        suspended_reason = reason;
                        suspended_params = params;
                    }
                    else {
                        suspended_pc = null;
                        suspended_reason = null;
                        suspended_params = null;
                    }
                    running = !suspend;
                }
                valid_state = true;
                validateNode(null);
            }
        });
        return false;
    }
    
    private boolean hasSuspendedChildren() {
        for (TCFNode n : children_exec.children.values()) {
            if (n instanceof TCFNodeExecContext) {
                TCFNodeExecContext e = (TCFNodeExecContext)n;
                if (e.run_context != null) {
                    if (e.run_context.hasState() && e.suspended) return true;
                    if (e.run_context.isContainer() && e.hasSuspendedChildren()) return true;
                }
            }
        }
        return false;
    }
    
    @Override
    protected String getImageName() {
        if (run_context != null && run_context.hasState()) {
            // Thread
            if (terminated) return "icons/full/obj16/threadt_obj.gif";
            if (suspended) return "icons/full/obj16/threads_obj.gif";
            return "icons/full/obj16/thread_obj.gif";
        }
        else if (run_context != null) {
            // Thread container (process)
            if (terminated) return "icons/full/obj16/debugtt_obj.gif";
            if (hasSuspendedChildren()) return "icons/full/obj16/debugts_obj.gif";
            return "icons/full/obj16/debugt_obj.gif";
        }
        return super.getImageName();
    }
}
