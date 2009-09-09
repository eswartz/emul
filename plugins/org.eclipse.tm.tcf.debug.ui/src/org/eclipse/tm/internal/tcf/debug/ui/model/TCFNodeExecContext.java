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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
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
    private final TCFDataCache<BigInteger> address;

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
                if (!run_context.validate(this)) return false;
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
        address = new TCFDataCache<BigInteger>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!run_context.validate(this)) return false;
                IRunControl.RunControlContext ctx = run_context.getData();
                if (ctx == null || !ctx.hasState()) {
                    set(null, run_context.getError(), null);
                    return true;
                }
                if (!state.validate(this)) return false;
                TCFContextState s = state.getData();
                if (s == null) {
                    set(null, state.getError(), null);
                    return true;
                }
                if (s.suspend_pc == null) {
                    set(null, null, null);
                    return true;
                }
                set(null, null, new BigInteger(s.suspend_pc));
                return true;
            }
        };
    }

    @Override
    void dispose() {
        run_context.reset(null);
        prs_context.reset(null);
        mem_context.reset(null);
        state.reset(null);
        address.reset(null);
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

    public TCFDataCache<BigInteger> getAddress() {
        return address;
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
    protected boolean getData(IChildrenCountUpdate result, Runnable done) {
        if (!run_context.validate(done)) return false;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
            if (!children_stack.validate(done)) return false;
            if (!IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
                TCFNodeStackFrame frame = children_stack.getTopFrame();
                if (frame != null) return frame.getData(result, done);
                result.setChildCount(0);
            }
            else {
                result.setChildCount(children_stack.size());
            }
        }
        else {
            if (!children_exec.validate(done)) return false;
            result.setChildCount(children_exec.size());
        }
        return true;
    }

    @Override
    protected boolean getData(IChildrenUpdate result, Runnable done) {
        TCFNode[] arr = null;
        if (!run_context.validate(done)) return false;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
            if (!children_stack.validate(done)) return false;
            if (!IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
                TCFNodeStackFrame frame = children_stack.getTopFrame();
                if (frame != null) return frame.getData(result, done);
                arr = new TCFNode[0];
            }
            else {
                arr = children_stack.toArray();
            }
        }
        else {
            if (!children_exec.validate(done)) return false;
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
        return true;
    }

    @Override
    protected boolean getData(IHasChildrenUpdate result, Runnable done) {
        if (!run_context.validate(done)) return false;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
            if (!children_stack.validate(done)) return false;
            if (!IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
                TCFNodeStackFrame frame = children_stack.getTopFrame();
                if (frame == null) result.setHasChilren(false);
                else if (!frame.getData(result, done)) return false;
            }
            else {
                result.setHasChilren(children_stack.size() > 0);
            }
        }
        else {
            if (!children_exec.validate(done)) return false;
            result.setHasChilren(children_exec.size() > 0);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean getData(ILabelUpdate result, Runnable done) {
        if (!run_context.validate(done)) return false;
        String image_name = null;
        String label = id;
        Throwable error = run_context.getError();
        if (error != null) {
            result.setForeground(new RGB(255, 0, 0), 0);
            label += ": " + error.getClass().getName() + ": " + error.getMessage();
        }
        else {
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null) {
                if (!state.validate(done)) return false;
                TCFContextState state_data = state.getData();
                if (ctx.hasState()) {
                    // Thread
                    if (state_data != null && state_data.is_terminated) image_name = ImageCache.IMG_THREAD_TERMINATED;
                    else if (state_data != null && state_data.is_suspended) image_name = ImageCache.IMG_THREAD_SUSPENDED;
                    else image_name = ImageCache.IMG_THREAD_RUNNNIG;
                }
                else {
                    // Thread container (process)
                    Boolean b = hasSuspendedChildren(done);
                    if (b == null) return false;
                    if (b.booleanValue()) image_name = ImageCache.IMG_PROCESS_SUSPENDED;
                    else image_name = ImageCache.IMG_PROCESS_RUNNING;
                }
                if (state_data != null && !state_data.is_suspended) {
                    label += " (Running)";
                }
                else if (state_data != null && state_data.is_suspended) {
                    String r = state_data.suspend_reason;
                    if (state_data.suspend_params != null) {
                        Collection<String> ids = (Collection<String>)state_data.suspend_params.get(IRunControl.STATE_BREAKPOINT_IDS);
                        if (ids != null) {
                            for (String id : ids) {
                                String s = model.getLaunch().getContextActionBreakpoint(id);
                                if (s != null) r = s;
                            }
                        }
                    }
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
        result.setImageDescriptor(ImageCache.getImageDescriptor(image_name), 0);
        result.setLabel(label, 0);
        return true;
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
        address.reset();
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
        address.reset();
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
                addModelDelta(IModelDelta.CONTENT);
                if (parent instanceof TCFNodeExecContext) {
                    ((TCFNodeExecContext)parent).onChildResumedOrSuspended();
                }
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

    // Return true if at least one child is suspended
    // The method will fail if node is not validated, see validateChildrenState()
    private Boolean hasSuspendedChildren(Runnable done) {
        if (!children_exec.validate(done)) return null;
        Map<String,TCFNode> m = children_exec.getData();
        if (m == null) return false;
        for (TCFNode n : m.values()) {
            if (!(n instanceof TCFNodeExecContext)) continue;
            TCFNodeExecContext e = (TCFNodeExecContext)n;
            if (!e.run_context.validate(done)) return null;
            IRunControl.RunControlContext ctx = e.run_context.getData();
            if (ctx == null) continue;
            if (ctx.hasState()) {
                TCFDataCache<TCFContextState> state_cache = e.getState();
                if (!state_cache.validate(done)) return null;
                TCFContextState state_data = state_cache.getData();
                if (state_data != null && state_data.is_suspended) return true;
            }
            if (ctx.isContainer()) {
                Boolean b = e.hasSuspendedChildren(done);
                if (b == null) return null;
                if (b.booleanValue()) return true;
            }
        }
        return false;
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
