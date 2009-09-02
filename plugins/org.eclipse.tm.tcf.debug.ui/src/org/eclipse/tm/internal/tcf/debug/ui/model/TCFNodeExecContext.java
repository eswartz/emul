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
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFDataCache;


@SuppressWarnings("serial")
public class TCFNodeExecContext extends TCFNode {

    private final int seq_no;

    private final TCFChildrenExecContext children_exec;
    private final TCFChildrenStackTrace children_stack;

    private final TCFDataCache<IMemory.MemoryContext> mem_context;
    private final TCFDataCache<IRunControl.RunControlContext> run_context;
    private final TCFDataCache<IProcesses.ProcessContext> prs_context;
    private final TCFDataCache<TCFContextState> state;

    private final Map<BigInteger,TCFSourceRef> line_info_cache;

    private int resumed_cnt;

    private static int seq_cnt;

    TCFNodeExecContext(TCFNode parent, final String id) {
        super(parent, id);
        seq_no = seq_cnt++;
        children_exec = new TCFChildrenExecContext(this);
        children_stack = new TCFChildrenStackTrace(this);
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
        prs_context = new TCFDataCache<IProcesses.ProcessContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                IProcesses prs = model.getLaunch().getService(IProcesses.class);
                if (prs == null) {
                    set(null, null, null);
                    return true;
                }
                command = prs.getContext(id, new IProcesses.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, IProcesses.ProcessContext context) {
                        set(token, error, context);
                    }
                });
                return false;
            }
        };
        state = new TCFDataCache<TCFContextState>(channel) {
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
                        TCFContextState s = new TCFContextState();
                        s.is_suspended = suspended;
                        s.suspend_pc = pc;
                        s.suspend_reason = reason;
                        s.suspend_params = params;
                        set(token, error, s);
                    }
                });
                return false;
            }
        };
    }

    @Override
    void dispose() {
        run_context.reset(null);
        prs_context.reset(null);
        mem_context.reset(null);
        state.reset(null);
        children_exec.dispose();
        children_stack.dispose();
        super.dispose();
    }

    @Override
    void dispose(String id) {
        children_exec.dispose(id);
        children_stack.dispose(id);
    }

    void setRunContext(IRunControl.RunControlContext ctx) {
        run_context.reset(ctx);
    }

    void setProcessContext(IProcesses.ProcessContext ctx) {
        prs_context.reset(ctx);
    }

    void setMemoryContext(IMemory.MemoryContext ctx) {
        mem_context.reset(ctx);
    }

    Map<BigInteger,TCFSourceRef> getLineInfoCache() {
        return line_info_cache;
    }

    public TCFDataCache<IRunControl.RunControlContext> getRunContext() {
        return run_context;
    }

    public TCFDataCache<IProcesses.ProcessContext> getProcessContext() {
        return prs_context;
    }

    public TCFDataCache<IMemory.MemoryContext> getMemoryContext() {
        return mem_context;
    }

    public boolean isRunning() {
        assert Protocol.isDispatchThread();
        if (!run_context.isValid()) return false;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx == null || !ctx.hasState()) return false;
        if (!state.isValid()) return false;
        TCFContextState s = state.getData();
        return s != null && !s.is_suspended;
    }

    public boolean isSuspended() {
        assert Protocol.isDispatchThread();
        if (!run_context.isValid()) return false;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx == null || !ctx.hasState()) return false;
        if (!state.isValid()) return false;
        TCFContextState s = state.getData();
        return s != null && s.is_suspended;
    }

    @Override
    public BigInteger getAddress() {
        assert Protocol.isDispatchThread();
        if (!run_context.isValid()) return null;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx == null || !ctx.hasState()) return null;
        if (!state.isValid()) return null;
        TCFContextState s = state.getData();
        if (s == null) return null;
        if (s.suspend_pc == null) return null;
        return new BigInteger(s.suspend_pc);
    }

    public TCFNodeStackFrame getTopFrame() {
        assert Protocol.isDispatchThread();
        if (!children_stack.isValid()) return null;
        return children_stack.getTopFrame();
    }

    public TCFDataCache<TCFContextState> getState() {
        return state;
    }

    public TCFChildrenStackTrace getStackTrace() {
        return children_stack;
    }

    public TCFChildrenExecContext getChildren() {
        return children_exec;
    }

    @Override
    public int getNodeIndex(IPresentationContext p, TCFNode n) {
        if (!run_context.isValid()) return -1;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
            if (!children_stack.isValid()) return -1;
            if (!IDebugUIConstants.ID_DEBUG_VIEW.equals(p.getId())) {
                TCFNodeStackFrame frame = children_stack.getTopFrame();
                if (frame == null) return -1;
                return frame.getNodeIndex(p, n);
            }
            return children_stack.getIndexOf(n);
        }
        if (!children_exec.isValid()) return -1;
        return children_exec.getIndexOf(n);
    }

    @Override
    public int getChildrenCount(IPresentationContext p) {
        if (!run_context.isValid()) return -1;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
            if (!children_stack.isValid()) return -1;
            if (!IDebugUIConstants.ID_DEBUG_VIEW.equals(p.getId())) {
                TCFNodeStackFrame frame = children_stack.getTopFrame();
                if (frame == null) return -1;
                return frame.getChildrenCount(p);
            }
            return children_stack.size();
        }
        if (!children_exec.isValid()) return -1;
        return children_exec.size();
    }

    @Override
    protected void getData(IChildrenCountUpdate result) {
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
            if (!IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
                TCFNodeStackFrame frame = children_stack.getTopFrame();
                if (frame == null) result.setChildCount(0);
                else frame.getData(result);
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
        TCFNode[] arr = null;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
            if (!IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
                TCFNodeStackFrame frame = children_stack.getTopFrame();
                if (frame == null) {
                    arr = new TCFNode[0];
                }
                else {
                    frame.getData(result);
                    return;
                }
            }
            else {
                arr = children_stack.toArray();
            }
        }
        else {
            arr = children_exec.toArray();
        }
        int offset = 0;
        int r_offset = result.getOffset();
        int r_length = result.getLength();
        for (TCFNode n : arr) {
            if (offset >= r_offset && offset < r_offset + r_length) {
                result.setChild(n, offset);
            }
            offset++;
        }
    }

    @Override
    protected void getData(IHasChildrenUpdate result) {
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
            if (!IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
                TCFNodeStackFrame frame = children_stack.getTopFrame();
                if (frame == null) result.setHasChilren(false);
                else frame.getData(result);
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
                    String r = state.getData().suspend_reason;
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
        children_stack.onSourceMappingChange();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextAdded(IMemory.MemoryContext context) {
        children_exec.onContextAdded(context);
    }

    void onContextChanged(IMemory.MemoryContext context) {
        assert !disposed;
        mem_context.reset(context);
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextRemoved() {
        assert !disposed;
        resumed_cnt++;
        dispose();
        addModelDelta(IModelDelta.REMOVED);
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
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContainerResumed() {
        assert !disposed;
        if (run_context.isValid()) {
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx == null) return;
            if (!ctx.hasState()) return;
        }
        state.reset();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextSuspended(String pc, String reason, Map<String,Object> params) {
        assert !disposed;
        TCFContextState s = new TCFContextState();
        s.is_suspended = true;
        s.suspend_pc = pc;
        s.suspend_reason = reason;
        s.suspend_params = params;
        state.reset(s);
        children_stack.onSuspended();
        resumed_cnt++;
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextResumed() {
        assert !disposed;
        state.reset(new TCFContextState());
        addModelDelta(IModelDelta.STATE);
        final int cnt = ++resumed_cnt;
        Protocol.invokeLater(250, new Runnable() {
            public void run() {
                if (cnt != resumed_cnt) return;
                if (disposed) return;
                children_stack.onResumed();
                if (!validateNode(this)) return;
                addModelDelta(IModelDelta.CONTENT);
                if (parent instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)parent).onChildResumedOrSuspended();
                }
                model.fireModelChanged();
            }
        });
    }

    void onChildResumedOrSuspended() {
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.isContainer()) addModelDelta(IModelDelta.STATE);
        if (parent instanceof TCFNodeExecContext) ((TCFNodeExecContext)parent).onChildResumedOrSuspended();
    }

    void onContextException(String msg) {
    }

    void onMemoryChanged(Number[] addr, long[] size) {
        assert !disposed;
    }

    void onRegistersChanged() {
        children_stack.onRegistersChanged();
        addModelDelta(IModelDelta.CONTENT);
    }

    @Override
    public boolean validateNode(Runnable done) {
        assert !disposed;
        TCFDataCache<?> pending = null;

        if (!mem_context.validate()) pending = mem_context;
        if (!run_context.validate()) pending = run_context;
        if (!prs_context.validate()) pending = prs_context;
        if (pending != null) {
            pending.wait(done);
            return false;
        }

        if (!state.validate()) pending = state;
        if (!children_exec.validate()) pending = children_exec;
        if (!children_stack.validate()) pending = children_stack;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && !ctx.hasState()) {
            // Container need to validate children for
            // hasSuspendedChildren() method to return valid value.
            TCFDataCache<?> dt = validateChildrenState();
            if (dt != null) pending = dt;
        }
        if (pending != null) {
            pending.wait(done);
            return false;
        }

        TCFNodeStackFrame frame = children_stack.getTopFrame();
        if (frame != null && !frame.validateNode(done)) return false;

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
            TCFContextState s = state.getData();
            if (s != null && s.is_terminated) return ImageCache.IMG_THREAD_TERMINATED;
            if (s != null && s.is_suspended) return ImageCache.IMG_THREAD_SUSPENDED;
            return ImageCache.IMG_THREAD_RUNNNIG;
        }
        else if (ctx != null) {
            // Thread container (process)
            //if (terminated) return ImageCache.IMG_PROCESS_TERMINATED;
            if (hasSuspendedChildren()) return ImageCache.IMG_PROCESS_SUSPENDED;
            return ImageCache.IMG_PROCESS_RUNNING;
        }
        return super.getImageName();
    }

    @Override
    public int compareTo(TCFNode n) {
        if (n instanceof TCFNodeExecContext) {
            TCFNodeExecContext f = (TCFNodeExecContext)n;
            if (seq_no < f.seq_no) return -1;
            if (seq_no > f.seq_no) return +1;
        }
        return id.compareTo(n.id);
    }
}
