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
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;


@SuppressWarnings("serial")
public class TCFNodeExecContext extends TCFNode {

    private final TCFChildrenExecContext children_exec;
    private final TCFChildrenStackTrace children_stack;
    private final TCFChildrenRegisters children_regs;

    private final TCFDataCache<IMemory.MemoryContext> mem_context;
    private final TCFDataCache<IRunControl.RunControlContext> run_context;
    private final TCFDataCache<ContextState> state;

    private final Map<BigInteger,TCFSourceRef> line_info_cache;

    private static class ContextState {
        boolean suspended;
        String suspended_pc;
        String suspended_reason;
        boolean terminated;
    }

    private int resumed_cnt;

    TCFNodeExecContext(TCFNode parent, final String id) {
        super(parent, id);
        children_exec = new TCFChildrenExecContext(this);
        children_regs = new TCFChildrenRegisters(this);
        children_stack = new TCFChildrenStackTrace(this, children_regs);
        line_info_cache = new LinkedHashMap<BigInteger,TCFSourceRef>() {
            @SuppressWarnings("unchecked")
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > 256;
            }
        };
        IChannel channel = model.getLaunch().getChannel();
        mem_context = new TCFDataCache<IMemory.MemoryContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                IMemory mem = model.getLaunch().getService(IMemory.class);
                if (mem == null) {
                    set(null, null, null);
                    return true;
                }
                command = mem.getContext(id, new IMemory.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, IMemory.MemoryContext context) {
                        set(token, error, context);
                    }
                });
                return false;
            }
        };
        run_context = new TCFDataCache<IRunControl.RunControlContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                IRunControl run = model.getLaunch().getService(IRunControl.class);
                if (run == null) {
                    set(null, null, null);
                    return true;
                }
                command = run.getContext(id, new IRunControl.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, IRunControl.RunControlContext context) {
                        set(token, error, context);
                    }
                });
                return false;
            }
        };
        state = new TCFDataCache<ContextState>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                if (!run_context.validate()) {
                    run_context.wait(this);
                    return false;
                }
                IRunControl.RunControlContext ctx = run_context.getData();
                if (ctx == null || !ctx.hasState()) {
                    set(null, null, null);
                    return true;
                }
                command = ctx.getState(new IRunControl.DoneGetState() {
                    public void doneGetState(IToken token, Exception error, boolean suspended, String pc, String reason, Map<String,Object> params) {
                        ContextState s = new ContextState();
                        s.suspended = suspended;
                        s.suspended_pc = pc;
                        s.suspended_reason = reason;
                        set(token, error, s);
                    }
                });
                return false;
            }
        };
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

    void setRunContext(IRunControl.RunControlContext ctx) {
        run_context.reset(ctx);
    }

    void setMemoryContext(IMemory.MemoryContext ctx) {
        mem_context.reset(ctx);
    }
    
    Map<BigInteger,TCFSourceRef> getLineInfoCache() {
        return line_info_cache;
    }

    @Override
    public IRunControl.RunControlContext getRunContext() {
        assert Protocol.isDispatchThread();
        if (!run_context.isValid()) return null;
        return run_context.getData();
    }

    @Override
    public IMemory.MemoryContext getMemoryContext() {
        assert Protocol.isDispatchThread();
        if (!mem_context.isValid()) return null;
        return mem_context.getData();
    }

    @Override
    public boolean isRunning() {
        assert Protocol.isDispatchThread();
        if (!run_context.isValid()) return false;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx == null || !ctx.hasState()) return false;
        if (!state.isValid()) return false;
        ContextState s = state.getData();
        return s != null && !s.suspended;
    }

    @Override
    public boolean isSuspended() {
        assert Protocol.isDispatchThread();
        if (!run_context.isValid()) return false;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx == null || !ctx.hasState()) return false;
        if (!state.isValid()) return false;
        ContextState s = state.getData();
        return s != null && s.suspended;
    }

    @Override
    public String getAddress() {
        assert Protocol.isDispatchThread();
        if (!run_context.isValid()) return null;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx == null || !ctx.hasState()) return null;
        if (!state.isValid()) return null;
        ContextState s = state.getData();
        if (s == null) return null;
        return s.suspended_pc;
    }

    @Override
    protected void getData(IChildrenCountUpdate result) {
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
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
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
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
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
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
        result.setImageDescriptor(ImageCache.getImageDescriptor(getImageName()), 0);
        String label = id;
        Throwable error = run_context.getError();
        if (error != null) {
            result.setForeground(new RGB(255, 0, 0), 0);
            label += ": " + error.getClass().getName() + ": " + error.getMessage();
        }
        else {
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null) {
                if (isRunning()) {
                    label += " (Running)";
                }
                else if (isSuspended()) {
                    String r = state.getData().suspended_reason;
                    if (r != null) {
                        label += " (" + r + ")";
                    }
                    else {
                        label += " (Suspended)";
                    }
                }
                String file = (String)ctx.getProperties().get("File");
                if (file != null) label += " " + file;
            }
        }
        result.setLabel(label, 0);
    }

    void onContextAdded(IRunControl.RunControlContext context) {
        children_exec.onContextAdded(context);
    }

    void onContextChanged(IRunControl.RunControlContext context) {
        assert !disposed;
        run_context.reset(context);
        resumed_cnt++;
        children_stack.onSourceMappingChange();
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextAdded(IMemory.MemoryContext context) {
        children_exec.onContextAdded(context);
    }

    void onContextChanged(IMemory.MemoryContext context) {
        assert !disposed;
        mem_context.reset(context);
        resumed_cnt++;
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextRemoved() {
        assert !disposed;
        resumed_cnt++;
        dispose();
        parent.makeModelDelta(IModelDelta.CONTENT);
    }

    void onContainerSuspended() {
        assert !disposed;
        if (run_context.isValid()) {
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx == null) return;
            if (!ctx.hasState()) return;
        }
        state.reset();
        children_stack.onSuspended();
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContainerResumed() {
        assert !disposed;
        if (run_context.isValid()) {
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx == null) return;
            if (!ctx.hasState()) return;
        }
        state.reset();
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextSuspended(String pc, String reason, Map<String,Object> params) {
        assert !disposed;
        ContextState s = new ContextState();
        s.suspended = true;
        s.suspended_pc = pc;
        s.suspended_reason = reason;
        state.reset(s);
        children_stack.onSuspended();
        resumed_cnt++;
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextResumed() {
        assert !disposed;
        state.reset(new ContextState());
        makeModelDelta(IModelDelta.STATE);
        final int cnt = ++resumed_cnt;
        model.invokeLater(250, new Runnable() {
            public void run() {
                if (cnt != resumed_cnt) return;
                if (disposed) return;
                children_stack.onResumed();
                if (!validateNode(this)) return;
                makeModelDelta(IModelDelta.CONTENT);
                if (parent instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)parent).onChildResumedOrSuspended();
                }
                model.fireModelChanged();
            }
        });
    }
    
    void onChildResumedOrSuspended() {
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.isContainer()) makeModelDelta(IModelDelta.STATE);
        if (parent instanceof TCFNodeExecContext) ((TCFNodeExecContext)parent).onChildResumedOrSuspended();
    }

    void onContextException(String msg) {
    }

    void onMemoryChanged(Number[] addr, long[] size) {
        assert !disposed;
    }

    void onRegistersChanged() {
        children_stack.onRegistersChanged();
        makeModelDelta(IModelDelta.CONTENT);
    }

    @Override
    public void invalidateNode() {
        run_context.reset();
        mem_context.reset();
        state.reset();
        children_exec.reset();
        children_stack.reset();
        children_regs.reset();
    }
    
    @Override
    public boolean validateNode(Runnable done) {
        assert !disposed;
        mem_context.validate();
        run_context.validate();
        if (!mem_context.isValid()) {
            mem_context.wait(done);
            return false;
        }
        if (!run_context.isValid()) {
            run_context.wait(done);
            return false;
        }
        state.validate();
        children_exec.validate();
        if (!state.isValid()) {
            state.wait(done);
            return false;
        }
        if (!children_exec.isValid()) {
            children_exec.wait(done);
            return false;
        }
        children_regs.validate();
        children_stack.validate();

        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && !ctx.hasState()) {
            // Container need to validate children for
            // hasSuspendedChildren() method to return valid value.
            TCFDataCache<?> dt = validateChildrenState();
            if (dt != null) {
                dt.wait(done);
                return false;
            }
        }
        
        if (!children_regs.isValid()) {
            children_regs.wait(done);
            return false;
        }
        if (!children_stack.isValid()) {
            children_stack.wait(done);
            return false;
        }
        return true;
    }
    
    // Validate children state for hasSuspendedChildren()
    // Return TCFDataCache to wait for if validation is pending.
    private TCFDataCache<?> validateChildrenState() {
        if (!children_exec.validate()) return children_exec;
        TCFDataCache<?> pending = null;
        for (TCFNode n : children_exec.getData().values()) {
            if (!(n instanceof TCFNodeExecContext)) continue;
            TCFNodeExecContext e = (TCFNodeExecContext)n;
            if (!e.run_context.validate()) {
                pending = e.run_context;
                continue;
            }
            IRunControl.RunControlContext ctx = e.run_context.getData();
            if (ctx == null) continue;
            if (ctx.hasState() && !e.state.validate()) pending = e.state;
            if (ctx.isContainer()) pending = e.validateChildrenState();
        }
        return pending;
    }

    // Return true if at least one child is suspended
    // The method will fail if node is not validated, see validateChildrenState()
    private boolean hasSuspendedChildren() {
        Map<String,TCFNode> m = children_exec.getData();
        if (m == null) return false;
        for (TCFNode n : m.values()) {
            if (!(n instanceof TCFNodeExecContext)) continue;
            TCFNodeExecContext e = (TCFNodeExecContext)n;
            IRunControl.RunControlContext ctx = e.run_context.getData();
            if (ctx == null) continue;
            if (ctx.hasState() && e.isSuspended()) return true;
            if (ctx.isContainer() && e.hasSuspendedChildren()) return true;
        }
        return false;
    }

    @Override
    protected String getImageName() {
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
            // Thread
            ContextState s = state.getData();
            if (s != null && s.terminated) return "icons/full/obj16/threadt_obj.gif";
            if (s != null && s.suspended) return "icons/full/obj16/threads_obj.gif";
            return "icons/full/obj16/thread_obj.gif";
        }
        else if (ctx != null) {
            // Thread container (process)
            //if (terminated) return "icons/full/obj16/debugtt_obj.gif";
            if (hasSuspendedChildren()) return "icons/full/obj16/debugts_obj.gif";
            return "icons/full/obj16/debugt_obj.gif";
        }
        return super.getImageName();
    }
}
