/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IMemoryMap;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFDataCache;


@SuppressWarnings("serial")
public class TCFNodeExecContext extends TCFNode implements ISymbolOwner {

    private final int seq_no;

    private final TCFChildrenExecContext children_exec;
    private final TCFChildrenStackTrace children_stack;

    private final TCFDataCache<IMemory.MemoryContext> mem_context;
    private final TCFDataCache<IRunControl.RunControlContext> run_context;
    private final TCFDataCache<MemoryRegion[]> memory_map;
    private final TCFDataCache<IProcesses.ProcessContext> prs_context;
    private final TCFDataCache<TCFContextState> state;
    private final TCFDataCache<BigInteger> address; // Current PC as BigInteger
    private final TCFDataCache<Collection<Map<String,Object>>> signal_list;
    private final TCFDataCache<SignalMask[]> signal_mask;

    private final Map<BigInteger,TCFSourceRef> line_info_cache;

    private final Map<String,TCFNodeSymbol> symbols = new HashMap<String,TCFNodeSymbol>();

    private int resumed_cnt;

    private static int seq_cnt;

    /**
     * Wrapper class for IMemoryMap.MemoryRegion.
     * The class help to search memory region by address by
     * providing contains() method.
     */
    public static class MemoryRegion {

        private final BigInteger addr_start;
        private final BigInteger addr_end;

        public final IMemoryMap.MemoryRegion region;

        private MemoryRegion(IMemoryMap.MemoryRegion region) {
            this.region = region;
            Number addr = region.getAddress();
            Number size = region.getSize();
            if (addr == null || size == null) {
                addr_start = null;
                addr_end = null;
            }
            else {
                addr_start = addr instanceof BigInteger ? (BigInteger)addr : new BigInteger(addr.toString());
                addr_end = addr_start.add(size instanceof BigInteger ? (BigInteger)size : new BigInteger(size.toString()));
            }
        }

        public boolean contains(BigInteger addr) {
            return
                addr_start != null && addr_end != null &&
                addr_start.compareTo(addr) <= 0 &&
                addr_end.compareTo(addr) > 0;
        }

        @Override
        public String toString() {
            return region.getProperties().toString();
        }
    }

    public static class SignalMask {

        protected Map<String,Object> props;
        protected boolean dont_stop;
        protected boolean dont_pass;
        protected boolean pending;

        public Number getIndex() {
            return (Number)props.get(IProcesses.SIG_INDEX);
        }

        public Number getCode() {
            return (Number)props.get(IProcesses.SIG_CODE);
        }

        public Map<String,Object> getProperties() {
            return props;
        }

        public boolean isDontStop() {
            return dont_stop;
        }

        public boolean isDontPass() {
            return dont_pass;
        }

        public boolean isPending() {
            return pending;
        }

        @Override
        public String toString() {
            StringBuffer bf = new StringBuffer();
            bf.append("[attrs=");
            bf.append(props.toString());
            if (dont_stop) bf.append(",don't stop");
            if (dont_pass) bf.append(",don't pass");
            if (pending) bf.append(",pending");
            bf.append(']');
            return bf.toString();
        }
    }

    TCFNodeExecContext(TCFNode parent, final String id) {
        super(parent, id);
        seq_no = seq_cnt++;
        children_exec = new TCFChildrenExecContext(this);
        children_stack = new TCFChildrenStackTrace(this);
        line_info_cache = new LinkedHashMap<BigInteger,TCFSourceRef>() {
            protected boolean removeEldestEntry(Map.Entry<BigInteger,TCFSourceRef> eldest) {
                return size() > 256;
            }
        };
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
        memory_map = new TCFDataCache<MemoryRegion[]>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                IMemoryMap mmap = model.getLaunch().getService(IMemoryMap.class);
                if (mmap == null) {
                    set(null, null, null);
                    return true;
                }
                command = mmap.get(id, new IMemoryMap.DoneGet() {
                    public void doneGet(IToken token, Exception error, IMemoryMap.MemoryRegion[] map) {
                        MemoryRegion[] arr = null;
                        if (map != null) {
                            int i = 0;
                            arr = new MemoryRegion[map.length];
                            for (IMemoryMap.MemoryRegion r : map) arr[i++] = new MemoryRegion(r);
                        }
                        set(token, error, arr);
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
        signal_list = new TCFDataCache<Collection<Map<String,Object>>>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                IProcesses prs = channel.getRemoteService(IProcesses.class);
                if (prs == null) {
                    set(null, null, null);
                    return true;
                }
                command = prs.getSignalList(id, new IProcesses.DoneGetSignalList() {
                    public void doneGetSignalList(IToken token, Exception error, Collection<Map<String, Object>> list) {
                        set(token, error, list);
                    }
                });
                return false;
            }
        };
        signal_mask = new TCFDataCache<SignalMask[]>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!signal_list.validate(this)) return false;
                IProcesses prs = channel.getRemoteService(IProcesses.class);
                final Collection<Map<String,Object>> sigs = signal_list.getData();
                if (prs == null || sigs == null) {
                    set(null, signal_list.getError(), null);
                    return true;
                }
                command = prs.getSignalMask(id, new IProcesses.DoneGetSignalMask() {
                    public void doneGetSignalMask(IToken token, Exception error, int dont_stop, int dont_pass, int pending) {
                        int n = 0;
                        SignalMask[] list = new SignalMask[sigs.size()];
                        for (Map<String,Object> m : sigs) {
                            SignalMask s = list[n++] = new SignalMask();
                            s.props = m;
                            int mask = 1 << s.getIndex().intValue();
                            s.dont_stop = (dont_stop & mask) != 0;
                            s.dont_pass = (dont_pass & mask) != 0;
                            s.pending = (pending & mask) != 0;
                        }
                        set(token, error, list);
                    }
                });
                return false;
            }
        };
    }

    @Override
    void dispose() {
        run_context.dispose();
        prs_context.dispose();
        mem_context.dispose();
        memory_map.dispose();
        state.dispose();
        address.dispose();
        signal_list.dispose();
        signal_mask.dispose();
        children_exec.dispose();
        children_stack.dispose();
        ArrayList<TCFNodeSymbol> l = new ArrayList<TCFNodeSymbol>(symbols.values());
        for (TCFNodeSymbol s : l) s.dispose();
        assert symbols.size() == 0;
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

    public TCFDataCache<MemoryRegion[]> getMemoryMap() {
        return memory_map;
    }

    public TCFDataCache<Collection<Map<String,Object>>> getSignalList() {
        return signal_list;
    }

    public TCFDataCache<SignalMask[]> getSignalMask() {
        return signal_mask;
    }

    public Map<BigInteger,TCFSourceRef> getLineInfoCache() {
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

    public void addSymbol(TCFNodeSymbol s) {
        assert symbols.get(s.id) == null;
        symbols.put(s.id, s);
    }

    public void removeSymbol(TCFNodeSymbol s) {
        assert symbols.get(s.id) == s;
        symbols.remove(s.id);
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

    @Override
    protected boolean getData(ILabelUpdate result, Runnable done) {
        if (!run_context.validate(done)) return false;
        String image_name = null;
        String label = id;
        Throwable error = run_context.getError();
        if (error != null) {
            result.setForeground(new RGB(255, 0, 0), 0);
            label += ": " + TCFModel.getErrorMessage(error, false);
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
                    if (model.isContextActionResultAvailable(id)) r = model.getContextActionResult(id);
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
        signal_mask.reset();
        state.reset();
        children_stack.reset();
        children_stack.onSourceMappingChange();
        for (TCFNodeSymbol s : symbols.values()) s.onMemoryMapChanged();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextAdded(IMemory.MemoryContext context) {
        children_exec.onContextAdded(context);
    }

    void onContextChanged(IMemory.MemoryContext context) {
        assert !disposed;
        mem_context.reset(context);
        for (TCFNodeSymbol s : symbols.values()) s.onMemoryMapChanged();
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
        for (TCFNodeSymbol s : symbols.values()) s.onExeStateChange();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onContextSuspended(String pc, String reason, Map<String,Object> params) {
        assert !disposed;
        if (pc != null) {
            TCFContextState s = new TCFContextState();
            s.is_suspended = true;
            s.suspend_pc = pc;
            s.suspend_reason = reason;
            s.suspend_params = params;
            state.reset(s);
        }
        else {
            state.reset();
        }
        address.reset();
        signal_mask.reset();
        resumed_cnt++;
        children_stack.onSuspended();
        for (TCFNodeSymbol s : symbols.values()) s.onExeStateChange();
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

    void onMemoryMapChanged() {
        memory_map.reset();
        children_exec.onMemoryMapChanged();
        children_stack.onMemoryMapChanged();
    }

    void onRegistersChanged() {
        children_stack.onRegistersChanged();
        addModelDelta(IModelDelta.CONTENT);
    }

    void onRegisterValueChanged() {
        state.reset();
        address.reset();
        children_stack.onRegisterValueChanged();
        addModelDelta(IModelDelta.CONTENT);
    }

    // Return true if at least one child is suspended.
    // Return null if waiting for a cache element.
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
