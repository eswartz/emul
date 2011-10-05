/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFFunctionRef;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IMemoryMap;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.ui.IViewPart;

public class TCFNodeExecContext extends TCFNode implements ISymbolOwner {

    private final TCFChildrenExecContext children_exec;
    private final TCFChildrenStackTrace children_stack;
    private final TCFChildrenRegisters children_regs;
    private final TCFChildrenExpressions children_exps;
    private final TCFChildrenHoverExpressions children_hover_exps;
    private final TCFChildrenLogExpressions children_log_exps;
    private final TCFChildrenModules children_modules;

    private final TCFData<IMemory.MemoryContext> mem_context;
    private final TCFData<IRunControl.RunControlContext> run_context;
    private final TCFData<MemoryRegion[]> memory_map;
    private final TCFData<IProcesses.ProcessContext> prs_context;
    private final TCFData<TCFContextState> state;
    private final TCFData<BigInteger> address; // Current PC as BigInteger
    private final TCFData<Collection<Map<String,Object>>> signal_list;
    private final TCFData<SignalMask[]> signal_mask;
    private final TCFData<TCFNodeExecContext> memory_node;
    private final TCFData<TCFNodeExecContext> symbols_node;
    private final TCFData<String> full_name;

    private LinkedHashMap<BigInteger,TCFDataCache<TCFSourceRef>> line_info_lookup_cache;
    private LinkedHashMap<BigInteger,TCFDataCache<TCFFunctionRef>> func_info_lookup_cache;
    private LookupCacheTimer lookup_cache_timer;

    private int mem_seq_no;
    private int exe_seq_no;

    /*
     * LookupCacheTimer is executed periodically to dispose least-recently
     * accessed entries in line_info_lookup_cache and func_info_lookup_cache.
     * The timer disposes itself when both caches become empty.
     */
    private class LookupCacheTimer implements Runnable {

        LookupCacheTimer() {
            Protocol.invokeLater(4000, this);
        }

        public void run() {
            if (isDisposed()) return;
            if (line_info_lookup_cache != null) {
                BigInteger addr = line_info_lookup_cache.keySet().iterator().next();
                TCFDataCache<TCFSourceRef> cache = line_info_lookup_cache.get(addr);
                if (!cache.isPending()) {
                    line_info_lookup_cache.remove(addr).dispose();
                    if (line_info_lookup_cache.size() == 0) line_info_lookup_cache = null;
                }
            }
            if (func_info_lookup_cache != null) {
                BigInteger addr = func_info_lookup_cache.keySet().iterator().next();
                TCFDataCache<TCFFunctionRef> cache = func_info_lookup_cache.get(addr);
                if (!cache.isPending()) {
                    func_info_lookup_cache.remove(addr).dispose();
                    if (func_info_lookup_cache.size() == 0) func_info_lookup_cache = null;
                }
            }
            if (line_info_lookup_cache == null && func_info_lookup_cache == null) {
                lookup_cache_timer = null;
            }
            else {
                Protocol.invokeLater(2500, this);
            }
        }
    }

    public static class ChildrenStateInfo {
        public boolean running;
        public boolean suspended;
        public boolean not_active;
        public boolean breakpoint;
    }

    private final Map<String,TCFNodeSymbol> symbols = new HashMap<String,TCFNodeSymbol>();

    private int resumed_cnt;
    private boolean resume_pending;
    private boolean resumed_by_action;
    private TCFNode[] last_stack_trace;
    private TCFNode[] last_children_list;
    private String last_label;
    private ImageDescriptor last_image;
    private ChildrenStateInfo last_children_state_info;
    private boolean delayed_children_list_delta;

    private String hover_expression;

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
                addr_start = JSON.toBigInteger(addr);
                addr_end = addr_start.add(JSON.toBigInteger(size));
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
        children_exec = new TCFChildrenExecContext(this);
        children_stack = new TCFChildrenStackTrace(this);
        children_regs = new TCFChildrenRegisters(this);
        children_exps = new TCFChildrenExpressions(this);
        children_hover_exps = new TCFChildrenHoverExpressions(this);
        children_log_exps = new TCFChildrenLogExpressions(this);
        children_modules = new TCFChildrenModules(this);
        mem_context = new TCFData<IMemory.MemoryContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                IMemory mem = launch.getService(IMemory.class);
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
        run_context = new TCFData<IRunControl.RunControlContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                IRunControl run = launch.getService(IRunControl.class);
                if (run == null) {
                    set(null, null, null);
                    return true;
                }
                command = run.getContext(id, new IRunControl.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, IRunControl.RunControlContext context) {
                        if (context != null) model.updateContextMap(id, context);
                        set(token, error, context);
                    }
                });
                return false;
            }
        };
        prs_context = new TCFData<IProcesses.ProcessContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                IProcesses prs = launch.getService(IProcesses.class);
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
        memory_map = new TCFData<MemoryRegion[]>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                IMemoryMap mmap = launch.getService(IMemoryMap.class);
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
        state = new TCFData<TCFContextState>(channel) {
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
        address = new TCFData<BigInteger>(channel) {
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
        signal_list = new TCFData<Collection<Map<String,Object>>>(channel) {
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
        signal_mask = new TCFData<SignalMask[]>(channel) {
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
        memory_node = new TCFData<TCFNodeExecContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                String mem_id = null;
                if (!run_context.validate(this)) return false;
                Throwable err = run_context.getError();
                if (err == null) {
                    IRunControl.RunControlContext ctx = run_context.getData();
                    if (ctx != null) mem_id = ctx.getProcessID();
                }
                if (err != null) {
                    set(null, err, null);
                }
                else if (mem_id == null) {
                    set(null, new Exception("Context does not provide memory access"), null);
                }
                else {
                    if (!model.createNode(mem_id, this)) return false;
                    if (!isValid()) set(null, null, (TCFNodeExecContext)model.getNode(mem_id));
                }
                return true;
            }
        };
        symbols_node = new TCFData<TCFNodeExecContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                String syms_id = null;
                if (!run_context.validate(this)) return false;
                Throwable err = run_context.getError();
                if (err == null) {
                    IRunControl.RunControlContext ctx = run_context.getData();
                    if (ctx != null) {
                        syms_id = ctx.getSymbolsGroup();
                        if (syms_id == null) syms_id = ctx.getProcessID();
                    }
                }
                if (err != null) {
                    set(null, err, null);
                }
                else if (syms_id == null) {
                    set(null, new Exception("Context does not support symbol groups"), null);
                }
                else {
                    if (!model.createNode(syms_id, this)) return false;
                    if (!isValid()) set(null, null, (TCFNodeExecContext)model.getNode(syms_id));
                }
                return true;
            }
        };
        full_name = new TCFData<String>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!run_context.validate(this)) return false;
                IRunControl.RunControlContext ctx = run_context.getData();
                String res = null;
                if (ctx != null) {
                    res = ctx.getName();
                    if (res == null) {
                        res = ctx.getID();
                    }
                    else {
                        // Add ancestor names
                        TCFNodeExecContext p = TCFNodeExecContext.this;
                        ArrayList<String> lst = new ArrayList<String>();
                        lst.add(res);
                        while (p.parent instanceof TCFNodeExecContext) {
                            p = (TCFNodeExecContext)p.parent;
                            TCFDataCache<IRunControl.RunControlContext> run_ctx_cache = p.run_context;
                            if (!run_ctx_cache.validate(this)) return false;
                            IRunControl.RunControlContext run_ctx_data = run_ctx_cache.getData();
                            String name = null;
                            if (run_ctx_data != null) name = run_ctx_data.getName();
                            if (name == null) name = "";
                            lst.add(name);
                        }
                        StringBuffer bf = new StringBuffer();
                        for (int i = lst.size(); i > 0; i--) {
                            bf.append('/');
                            bf.append(lst.get(i - 1));
                        }
                        res = bf.toString();
                    }
                }
                set(null, null, res);
                return true;
            }
        };
    }

    @Override
    void dispose() {
        assert !isDisposed();
        ArrayList<TCFNodeSymbol> l = new ArrayList<TCFNodeSymbol>(symbols.values());
        for (TCFNodeSymbol s : l) s.dispose();
        assert symbols.size() == 0;
        super.dispose();
    }

    void setMemSeqNo(int no) {
        mem_seq_no = no;
    }

    void setExeSeqNo(int no) {
        exe_seq_no = no;
    }

    TCFChildren getHoverExpressionCache(String expression) {
        if (expression != hover_expression && (expression == null || !expression.equals(hover_expression))) {
            hover_expression = expression;
            children_hover_exps.cancel();
        }
        return children_hover_exps;
    }

    String getHoverExpression() {
        return hover_expression;
    }

    public TCFChildrenLogExpressions getLogExpressionCache() {
        return children_log_exps;
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

    public TCFDataCache<TCFNodeExecContext> getSymbolsNode() {
        return symbols_node;
    }

    public TCFDataCache<TCFNodeExecContext> getMemoryNode() {
        return memory_node;
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

    public TCFDataCache<TCFSourceRef> getLineInfo(final BigInteger addr) {
        if (isDisposed()) return null;
        TCFDataCache<TCFSourceRef> ref_cache;
        if (line_info_lookup_cache != null) {
            ref_cache = line_info_lookup_cache.get(addr);
            if (ref_cache != null) return ref_cache;
        }
        final ILineNumbers ln = launch.getService(ILineNumbers.class);
        if (ln == null) return null;
        final BigInteger n0 = addr;
        final BigInteger n1 = n0.add(BigInteger.valueOf(1));
        if (line_info_lookup_cache == null) {
            line_info_lookup_cache = new LinkedHashMap<BigInteger,TCFDataCache<TCFSourceRef>>(11, 0.75f, true);
            if (lookup_cache_timer == null) lookup_cache_timer = new LookupCacheTimer();
        }
        line_info_lookup_cache.put(addr, ref_cache = new TCFData<TCFSourceRef>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!memory_node.validate(this)) return false;
                IMemory.MemoryContext mem_data = null;
                TCFNodeExecContext mem = memory_node.getData();
                if (mem != null) {
                    TCFDataCache<IMemory.MemoryContext> mem_cache = mem.mem_context;
                    if (!mem_cache.validate(this)) return false;
                    mem_data = mem_cache.getData();
                }
                final TCFSourceRef ref_data = new TCFSourceRef();
                if (mem_data != null) {
                    ref_data.context_id = mem_data.getID();
                    ref_data.address_size = mem_data.getAddressSize();
                }
                command = ln.mapToSource(id, n0, n1, new ILineNumbers.DoneMapToSource() {
                    public void doneMapToSource(IToken token, Exception error, ILineNumbers.CodeArea[] areas) {
                        ref_data.address = addr;
                        if (error == null && areas != null && areas.length > 0) {
                            for (ILineNumbers.CodeArea area : areas) {
                                BigInteger a0 = JSON.toBigInteger(area.start_address);
                                BigInteger a1 = JSON.toBigInteger(area.end_address);
                                if (n0.compareTo(a0) >= 0 && n0.compareTo(a1) < 0) {
                                    if (ref_data.area == null || area.start_line < ref_data.area.start_line) {
                                        if (area.start_address != a0 || area.end_address != a1) {
                                            area = new ILineNumbers.CodeArea(area.directory, area.file,
                                                    area.start_line, area.start_column,
                                                    area.end_line, area.end_column,
                                                    a0, a1, area.isa,
                                                    area.is_statement, area.basic_block,
                                                    area.prologue_end, area.epilogue_begin);
                                        }
                                        ref_data.area = area;
                                    }
                                }
                            }
                        }
                        ref_data.error = error;
                        set(token, null, ref_data);
                    }
                });
                return false;
            }
        });
        return ref_cache;
    }

    public TCFDataCache<TCFFunctionRef> getFuncInfo(final BigInteger addr) {
        if (isDisposed()) return null;
        TCFDataCache<TCFFunctionRef> ref_cache;
        if (func_info_lookup_cache != null) {
            ref_cache = func_info_lookup_cache.get(addr);
            if (ref_cache != null) return ref_cache;
        }
        final ISymbols syms = launch.getService(ISymbols.class);
        if (syms == null) return null;
        if (func_info_lookup_cache == null) {
            func_info_lookup_cache = new LinkedHashMap<BigInteger,TCFDataCache<TCFFunctionRef>>(11, 0.75f, true);
            if (lookup_cache_timer == null) lookup_cache_timer = new LookupCacheTimer();
        }
        func_info_lookup_cache.put(addr, ref_cache = new TCFData<TCFFunctionRef>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!memory_node.validate(this)) return false;
                IMemory.MemoryContext mem_data = null;
                TCFNodeExecContext mem = memory_node.getData();
                if (mem != null) {
                    TCFDataCache<IMemory.MemoryContext> mem_cache = mem.mem_context;
                    if (!mem_cache.validate(this)) return false;
                    mem_data = mem_cache.getData();
                }
                final TCFFunctionRef ref_data = new TCFFunctionRef();
                if (mem_data != null) {
                    ref_data.context_id = mem_data.getID();
                    ref_data.address_size = mem_data.getAddressSize();
                }
                command = syms.findByAddr(id, addr, new ISymbols.DoneFind() {
                    public void doneFind(IToken token, Exception error, String symbol_id) {
                        ref_data.address = addr;
                        ref_data.error = error;
                        ref_data.symbol_id = symbol_id;
                        set(token, null, ref_data);
                    }
                });
                return false;
            }
        });
        return ref_cache;
    }

    private void clearLookupCaches() {
        if (line_info_lookup_cache != null) {
            Iterator<TCFDataCache<TCFSourceRef>> i = line_info_lookup_cache.values().iterator();
            while (i.hasNext()) {
                TCFDataCache<TCFSourceRef> cache = i.next();
                if (cache.isPending()) continue;
                cache.dispose();
                i.remove();
            }
            if (line_info_lookup_cache.size() == 0) line_info_lookup_cache = null;
        }
        if (func_info_lookup_cache != null) {
            Iterator<TCFDataCache<TCFFunctionRef>> i = func_info_lookup_cache.values().iterator();
            while (i.hasNext()) {
                TCFDataCache<TCFFunctionRef> cache = i.next();
                if (cache.isPending()) continue;
                cache.dispose();
                i.remove();
            }
            if (func_info_lookup_cache.size() == 0) func_info_lookup_cache = null;
        }
    }

    @Override
    public TCFNode getParent(IPresentationContext ctx) {
        assert Protocol.isDispatchThread();
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(ctx.getId())) {
            Set<String> ids = launch.getContextFilter();
            if (ids != null) {
                if (ids.contains(id)) return model.getRootNode();
                if (parent instanceof TCFNodeLaunch) return null;
            }
        }
        return parent;
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

    public TCFChildren getRegisters() {
        return children_regs;
    }

    public TCFChildren getModules() {
        return children_modules;
    }

    public TCFChildren getChildren() {
        return children_exec;
    }

    public TCFNodeStackFrame getLastTopFrame() {
        if (!resume_pending) return null;
        if (last_stack_trace == null || last_stack_trace.length == 0) return null;
        return (TCFNodeStackFrame)last_stack_trace[0];
    }

    public TCFNodeStackFrame getViewBottomFrame() {
        if (last_stack_trace == null || last_stack_trace.length == 0) return null;
        return (TCFNodeStackFrame)last_stack_trace[last_stack_trace.length - 1];
    }

    /**
     * Get context full name - including all ancestor names.
     * Return context ID if the context does not have a name.
     * @return cache item with the context full name.
     */
    public TCFDataCache<String> getFullName() {
        return full_name;
    }

    public void addSymbol(TCFNodeSymbol s) {
        assert symbols.get(s.id) == null;
        symbols.put(s.id, s);
    }

    public void removeSymbol(TCFNodeSymbol s) {
        assert symbols.get(s.id) == s;
        symbols.remove(s.id);
    }

    /**
     * Return true if this context cannot be accessed because it is not active.
     * Not active means the target is suspended but this context is not one that is
     * currently scheduled to run on a target CPU.
     * Some debuggers don't support access to register values and other properties of such contexts.
     */
    public boolean isNotActive() {
        TCFContextState state_data = state.getData();
        if (state_data != null && state_data.suspend_params != null) {
            @SuppressWarnings("unchecked")
            Map<String,Object> attrs = (Map<String,Object>)state_data.suspend_params.get(IRunControl.STATE_PC_ERROR);
            if (attrs != null) {
                Number n = (Number)attrs.get(IErrorReport.ERROR_CODE);
                if (n != null) return n.intValue() == IErrorReport.TCF_ERROR_NOT_ACTIVE;
            }
        }
        return false;
    }

    @Override
    protected boolean getData(IChildrenCountUpdate result, Runnable done) {
        TCFChildren children = null;
        String view_id = result.getPresentationContext().getId();
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) {
                if (resume_pending && last_stack_trace != null) {
                    result.setChildCount(last_stack_trace.length);
                    return true;
                }
                if (!state.validate(done)) return false;
                if (isNotActive()) {
                    last_stack_trace = new TCFNode[0];
                    result.setChildCount(0);
                    return true;
                }
                TCFContextState state_data = state.getData();
                if (state_data != null && !state_data.is_suspended) {
                    result.setChildCount(0);
                    return true;
                }
                children = children_stack;
            }
            else {
                if (!model.getAutoChildrenListUpdates() && last_children_list != null) {
                    result.setChildCount(last_children_list.length);
                    return true;
                }
                children = children_exec;
            }
        }
        else if (IDebugUIConstants.ID_REGISTER_VIEW.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) children = children_regs;
        }
        else if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) children = children_exps;
        }
        else if (TCFModel.ID_EXPRESSION_HOVER.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) children = children_hover_exps;
        }
        else if (IDebugUIConstants.ID_MODULE_VIEW.equals(view_id)) {
            if (!mem_context.validate(done)) return false;
            IMemory.MemoryContext ctx = mem_context.getData();
            if (ctx != null) children = children_modules;
        }
        if (children != null) {
            if (!children.validate(done)) return false;
            if (children == children_stack) last_stack_trace = children_stack.toArray();
            if (children == children_exec) last_children_list = children_exec.toArray();
            result.setChildCount(children.size());
        }
        else {
            result.setChildCount(0);
        }
        return true;
    }

    private void setResultChildren(IChildrenUpdate result, TCFNode[] arr) {
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
    protected boolean getData(IChildrenUpdate result, Runnable done) {
        TCFChildren children = null;
        String view_id = result.getPresentationContext().getId();
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) {
                if (resume_pending && last_stack_trace != null) {
                    setResultChildren(result, last_stack_trace);
                    return true;
                }
                if (!state.validate(done)) return false;
                if (isNotActive()) {
                    last_stack_trace = new TCFNode[0];
                    return true;
                }
                TCFContextState state_data = state.getData();
                if (state_data != null && !state_data.is_suspended) {
                    return true;
                }
                children = children_stack;
            }
            else {
                if (!model.getAutoChildrenListUpdates() && last_children_list != null) {
                    setResultChildren(result, last_children_list);
                    return true;
                }
                children = children_exec;
            }
        }
        else if (IDebugUIConstants.ID_REGISTER_VIEW.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) children = children_regs;
        }
        else if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) children = children_exps;
        }
        else if (TCFModel.ID_EXPRESSION_HOVER.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) children = children_hover_exps;
        }
        else if (IDebugUIConstants.ID_MODULE_VIEW.equals(view_id)) {
            if (!mem_context.validate(done)) return false;
            IMemory.MemoryContext ctx = mem_context.getData();
            if (ctx != null) children = children_modules;
        }
        if (children == null) return true;
        if (children == children_stack) {
            if (!children.validate(done)) return false;
            last_stack_trace = children_stack.toArray();
        }
        if (children == children_exec) {
            if (!children.validate(done)) return false;
            last_children_list = children_exec.toArray();
        }
        return children.getData(result, done);
    }

    @Override
    protected boolean getData(IHasChildrenUpdate result, Runnable done) {
        TCFChildren children = null;
        String view_id = result.getPresentationContext().getId();
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) {
                if (resume_pending && last_stack_trace != null) {
                    result.setHasChilren(last_stack_trace.length > 0);
                    return true;
                }
                if (!state.validate(done)) return false;
                if (isNotActive()) {
                    last_stack_trace = new TCFNode[0];
                    result.setHasChilren(false);
                    return true;
                }
                TCFContextState state_data = state.getData();
                if (state_data != null) {
                    result.setHasChilren(state_data.is_suspended);
                    return true;
                }
                children = children_stack;
            }
            else {
                if (!model.getAutoChildrenListUpdates() && last_children_list != null) {
                    result.setHasChilren(last_children_list.length > 0);
                    return true;
                }
                children = children_exec;
            }
        }
        else if (IDebugUIConstants.ID_REGISTER_VIEW.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) children = children_regs;
        }
        else if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) children = children_exps;
        }
        else if (TCFModel.ID_EXPRESSION_HOVER.equals(view_id)) {
            if (!run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && ctx.hasState()) children = children_hover_exps;
        }
        else if (IDebugUIConstants.ID_MODULE_VIEW.equals(view_id)) {
            if (!mem_context.validate(done)) return false;
            IMemory.MemoryContext ctx = mem_context.getData();
            if (ctx != null) children = children_modules;
        }
        if (children != null) {
            if (!children.validate(done)) return false;
            if (children == children_stack) last_stack_trace = children_stack.toArray();
            if (children == children_exec) last_children_list = children_exec.toArray();
            result.setHasChilren(children.size() > 0);
        }
        else {
            result.setHasChilren(false);
        }
        return true;
    }

    @Override
    protected boolean getData(ILabelUpdate result, Runnable done) {
        result.getViewerInput();
        if (!run_context.validate(done)) return false;
        String image_name = null;
        boolean suspended_by_bp = false;
        StringBuffer label = new StringBuffer();
        Throwable error = run_context.getError();
        if (error != null) {
            result.setForeground(new RGB(255, 0, 0), 0);
            label.append(id);
            label.append(": ");
            label.append(TCFModel.getErrorMessage(error, false));
        }
        else {
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx == null) {
                label.append(id);
            }
            else {
                String nm = ctx.getName();
                if (nm == null && !ctx.hasState()) {
                    String prs = ctx.getProcessID();
                    if (prs != null) {
                        if (!prs_context.validate(done)) return false;
                        IProcesses.ProcessContext pctx = prs_context.getData();
                        if (pctx != null) nm = pctx.getName();
                    }
                }
                label.append(nm != null ? nm : id);
                if (ctx.hasState() && !TCFModel.ID_PINNED_VIEW.equals(result.getPresentationContext().getId())) {
                    // Thread
                    if (resume_pending && resumed_by_action || model.getActiveAction(id) != null) {
                        image_name = ImageCache.IMG_THREAD_RUNNNIG;
                        if (resume_pending && last_label != null) {
                            result.setImageDescriptor(ImageCache.getImageDescriptor(image_name), 0);
                            result.setLabel(last_label, 0);
                            return true;
                        }
                        label.append(" (Running)");
                    }
                    else if (resume_pending && last_label != null && last_image != null) {
                        result.setImageDescriptor(last_image, 0);
                        result.setLabel(last_label, 0);
                        return true;
                    }
                    else {
                        if (!state.validate(done)) return false;
                        TCFContextState state_data = state.getData();
                        if (isNotActive()) {
                            image_name = ImageCache.IMG_THREAD_NOT_ACTIVE;
                            label.append(" (Not active)");
                            if (state_data.suspend_reason != null && !state_data.suspend_reason.equals(IRunControl.REASON_USER_REQUEST)) {
                                label.append(" - ");
                                label.append(state_data.suspend_reason);
                            }
                        }
                        else {
                            if (state_data == null) image_name = ImageCache.IMG_THREAD_UNKNOWN_STATE;
                            else if (state_data.is_suspended) image_name = ImageCache.IMG_THREAD_SUSPENDED;
                            else image_name = ImageCache.IMG_THREAD_RUNNNIG;
                            if (state_data != null) {
                                if (!state_data.is_suspended) {
                                    label.append(" (Running)");
                                }
                                else {
                                    String s = null;
                                    String r = model.getContextActionResult(id);
                                    if (r == null) {
                                        r = state_data.suspend_reason;
                                        if (state_data.suspend_params != null) {
                                            s = (String)state_data.suspend_params.get(IRunControl.STATE_SIGNAL_DESCRIPTION);
                                            if (s == null) s = (String)state_data.suspend_params.get(IRunControl.STATE_SIGNAL_NAME);
                                        }
                                    }
                                    suspended_by_bp = IRunControl.REASON_BREAKPOINT.equals(r);
                                    if (r == null) r = "Suspended";
                                    if (s != null) r += ": " + s;
                                    label.append(" (");
                                    label.append(r);
                                    if (state_data.suspend_params != null) {
                                        String prs = (String)state_data.suspend_params.get("Context");
                                        if (prs != null) {
                                            label.append(", ");
                                            label.append(prs);
                                        }
                                        String cpu = (String)state_data.suspend_params.get("CPU");
                                        if (cpu != null) {
                                            label.append(", ");
                                            label.append(cpu);
                                        }
                                    }
                                    label.append(")");
                                }
                            }
                        }
                    }
                    last_children_state_info = null;
                }
                else {
                    // Thread container (process)
                    ChildrenStateInfo i = new ChildrenStateInfo();
                    if (!hasSuspendedChildren(i, done)) return false;
                    if (i.suspended) image_name = ImageCache.IMG_PROCESS_SUSPENDED;
                    else image_name = ImageCache.IMG_PROCESS_RUNNING;
                    suspended_by_bp = i.breakpoint;
                    last_children_state_info = i;
                }
            }
        }
        last_image = ImageCache.getImageDescriptor(image_name);
        if (suspended_by_bp) last_image = ImageCache.addOverlay(last_image, ImageCache.IMG_BREAKPOINT_OVERLAY);
        result.setImageDescriptor(last_image, 0);
        result.setLabel(last_label = label.toString(), 0);
        return true;
    }

    @Override
    protected boolean getData(IViewerInputUpdate result, Runnable done) {
        result.setInputElement(this);
        String view_id = result.getPresentationContext().getId();
        if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(view_id)) {
            if (!children_stack.validate(done)) return false;
            TCFNodeStackFrame frame = children_stack.getTopFrame();
            if (frame != null) result.setInputElement(frame);
        }
        else if (IDebugUIConstants.ID_MODULE_VIEW.equals(view_id)) {
            // TODO: need to post view input delta when memory context changes
            TCFDataCache<TCFNodeExecContext> mem = model.searchMemoryContext(this);
            if (mem == null) return true;
            if (!mem.validate(done)) return false;
            if (mem.getData() == null) return true;
            result.setInputElement(mem.getData());
        }
        return true;
    }

    public void refresh(IViewPart view) {
        last_children_list = null;
        last_children_state_info = null;
        last_stack_trace = null;
        last_label = null;
        last_image = null;
        super.refresh(view);
    }

    void postAllChangedDelta() {
        postContentChangedDelta();
        postStateChangedDelta();
    }

    void postContextAddedDelta() {
        if (parent instanceof TCFNodeExecContext) {
            TCFNodeExecContext exe = (TCFNodeExecContext)parent;
            ChildrenStateInfo info = exe.last_children_state_info;
            if (info != null) {
                if (!model.getAutoChildrenListUpdates()) {
                    // Manual manual updates.
                    return;
                }
                if (!info.suspended && !info.not_active && model.getDelayChildrenListUpdates()) {
                    // Delay content update until a child is suspended.
                    exe.delayed_children_list_delta = true;
                    return;
                }
            }
        }
        for (TCFModelProxy p : model.getModelProxies()) {
            if (IDebugUIConstants.ID_DEBUG_VIEW.equals(p.getPresentationContext().getId())) {
                /* Note: should use IModelDelta.INSERTED but it is broken in Eclipse 3.6 */
                p.addDelta(this, IModelDelta.ADDED);
            }
        }
    }

    private void postContextRemovedDelta() {
        if (parent instanceof TCFNodeExecContext) {
            TCFNodeExecContext exe = (TCFNodeExecContext)parent;
            ChildrenStateInfo info = exe.last_children_state_info;
            if (info != null) {
                if (!model.getAutoChildrenListUpdates()) {
                    // Manual manual updates.
                    return;
                }
                if (!info.suspended && !info.not_active && model.getDelayChildrenListUpdates()) {
                    // Delay content update until a child is suspended.
                    exe.delayed_children_list_delta = true;
                    return;
                }
            }
        }
        for (TCFModelProxy p : model.getModelProxies()) {
            if (IDebugUIConstants.ID_DEBUG_VIEW.equals(p.getPresentationContext().getId())) {
                p.addDelta(this, IModelDelta.REMOVED);
            }
        }
    }

    private void postContentChangedDelta() {
        delayed_children_list_delta = false;
        for (TCFModelProxy p : model.getModelProxies()) {
            int flags = 0;
            String view_id = p.getPresentationContext().getId();
            if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id) &&
                    (launch.getContextActionsCount(id) == 0 ||
                    !model.getDelayStackUpdateUtilLastStep())) {
                flags |= IModelDelta.CONTENT;
            }
            if (IDebugUIConstants.ID_REGISTER_VIEW.equals(view_id) ||
                    IDebugUIConstants.ID_EXPRESSION_VIEW.equals(view_id) ||
                    TCFModel.ID_EXPRESSION_HOVER.equals(view_id)) {
                if (p.getInput() == this) flags |= IModelDelta.CONTENT;
            }
            if (flags == 0) continue;
            p.addDelta(this, flags);
        }
    }

    private void postAllAndParentsChangedDelta() {
        postContentChangedDelta();
        TCFNode n = this;
        while (n instanceof TCFNodeExecContext) {
            TCFNodeExecContext e = (TCFNodeExecContext)n;
            if (e.delayed_children_list_delta) e.postContentChangedDelta();
            e.postStateChangedDelta();
            n = n.parent;
        }
    }

    public void postStateChangedDelta() {
        for (TCFModelProxy p : model.getModelProxies()) {
            if (IDebugUIConstants.ID_DEBUG_VIEW.equals(p.getPresentationContext().getId())) {
                p.addDelta(this, IModelDelta.STATE);
            }
        }
    }

    private void postModulesChangedDelta() {
        for (TCFModelProxy p : model.getModelProxies()) {
            if (IDebugUIConstants.ID_MODULE_VIEW.equals(p.getPresentationContext().getId())) {
                p.addDelta(this, IModelDelta.CONTENT);
            }
        }
    }

    private void postStackChangedDelta() {
        for (TCFModelProxy p : model.getModelProxies()) {
            if (IDebugUIConstants.ID_DEBUG_VIEW.equals(p.getPresentationContext().getId())) {
                p.addDelta(this, IModelDelta.CONTENT);
            }
        }
    }

    void onContextAdded(IRunControl.RunControlContext context) {
        children_exec.onContextAdded(context);
    }

    void onContextChanged(IRunControl.RunControlContext context) {
        assert !isDisposed();
        full_name.reset();
        run_context.reset(context);
        symbols_node.reset();
        memory_node.reset();
        signal_mask.reset();
        if (state.isValid()) {
            TCFContextState s = state.getData();
            if (s == null || s.is_suspended) state.reset();
        }
        children_stack.reset();
        children_stack.onSourceMappingChange();
        children_regs.reset();
        children_exec.onAncestorContextChanged();
        for (TCFNodeSymbol s : symbols.values()) s.onMemoryMapChanged();
        postAllChangedDelta();
    }

    void onAncestorContextChanged() {
        full_name.reset();
    }

    void onContextAdded(IMemory.MemoryContext context) {
        children_exec.onContextAdded(context);
    }

    void onContextChanged(IMemory.MemoryContext context) {
        assert !isDisposed();
        clearLookupCaches();
        mem_context.reset(context);
        for (TCFNodeSymbol s : symbols.values()) s.onMemoryMapChanged();
        postAllChangedDelta();
    }

    void onContextRemoved() {
        assert !isDisposed();
        resumed_cnt++;
        resume_pending = false;
        resumed_by_action = false;
        dispose();
        postContextRemovedDelta();
        launch.removeContextActions(id);
    }

    void onExpressionAddedOrRemoved() {
        children_exps.cancel();
        children_stack.onExpressionAddedOrRemoved();
    }

    void onContainerSuspended() {
        assert !isDisposed();
        if (run_context.isValid()) {
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && !ctx.hasState()) return;
        }
        onContextSuspended(null, null, null);
    }

    void onContainerResumed() {
        assert !isDisposed();
        if (run_context.isValid()) {
            IRunControl.RunControlContext ctx = run_context.getData();
            if (ctx != null && !ctx.hasState()) return;
        }
        onContextResumed();
    }

    void onContextSuspended(String pc, String reason, Map<String,Object> params) {
        assert !isDisposed();
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
        children_stack.onSuspended();
        children_regs.onSuspended();
        children_exps.onSuspended();
        children_hover_exps.onSuspended();
        children_log_exps.onSuspended();
        for (TCFNodeSymbol s : symbols.values()) s.onExeStateChange();
        if (model.getActiveAction(id) == null) {
            boolean update_now = pc != null || resumed_by_action;
            resumed_cnt++;
            resume_pending = false;
            resumed_by_action = false;
            if (update_now) {
                children_stack.postAllChangedDelta();
                postAllAndParentsChangedDelta();
            }
            else {
                final int cnt = ++resumed_cnt;
                Protocol.invokeLater(500, new Runnable() {
                    public void run() {
                        if (cnt != resumed_cnt) return;
                        if (isDisposed()) return;
                        children_stack.postAllChangedDelta();
                        postAllAndParentsChangedDelta();
                    }
                });
            }
        }
    }

    void onContextResumed() {
        assert !isDisposed();
        state.reset(new TCFContextState());
        if (!resume_pending) {
            final int cnt = ++resumed_cnt;
            resume_pending = true;
            resumed_by_action = model.getActiveAction(id) != null;
            if (resumed_by_action) postAllChangedDelta();
            Protocol.invokeLater(400, new Runnable() {
                public void run() {
                    if (cnt != resumed_cnt) return;
                    if (isDisposed()) return;
                    children_stack.onResumed();
                    resume_pending = false;
                    postAllAndParentsChangedDelta();
                    model.onContextRunning();
                }
            });
        }
    }

    void onContextActionDone() {
        if (!state.isValid() || state.getData() == null || state.getData().is_suspended) {
            resumed_cnt++;
            resume_pending = false;
            resumed_by_action = false;
        }
        postAllChangedDelta();
        children_stack.postAllChangedDelta();
    }

    void onContextException(String msg) {
    }

    void onMemoryChanged(Number[] addr, long[] size) {
        assert !isDisposed();
        children_stack.onMemoryChanged();
        children_exps.onMemoryChanged();
        children_hover_exps.onMemoryChanged();
        children_log_exps.onMemoryChanged();
        postContentChangedDelta();
    }

    void onMemoryMapChanged() {
        clearLookupCaches();
        memory_map.reset();
        children_modules.onMemoryMapChanged();
        children_stack.onMemoryMapChanged();
        children_exps.onMemoryMapChanged();
        children_hover_exps.onMemoryMapChanged();
        children_log_exps.onMemoryMapChanged();
        postModulesChangedDelta();
    }

    void onRegistersChanged() {
        children_stack.onRegistersChanged();
        postContentChangedDelta();
    }

    void onRegisterValueChanged() {
        if (state.isValid()) {
            TCFContextState s = state.getData();
            if (s == null || s.is_suspended) state.reset();
        }
        address.reset();
        children_stack.onRegisterValueChanged();
        children_exps.onRegisterValueChanged();
        children_hover_exps.onRegisterValueChanged();
        children_log_exps.onRegisterValueChanged();
        postContentChangedDelta();
    }

    void onPreferencesChanged() {
        if (delayed_children_list_delta && !model.getDelayChildrenListUpdates() ||
                model.getAutoChildrenListUpdates()) postContentChangedDelta();
        children_stack.onPreferencesChanged();
        postStackChangedDelta();
    }

    void riseTraceLimit() {
        children_stack.riseTraceLimit();
        postStackChangedDelta();
    }

    public boolean hasSuspendedChildren(ChildrenStateInfo info, Runnable done) {
        if (!children_exec.validate(done)) return false;
        Map<String,TCFNode> m = children_exec.getData();
        if (m == null || m.size() == 0) return true;
        for (TCFNode n : m.values()) {
            if (!(n instanceof TCFNodeExecContext)) continue;
            TCFNodeExecContext e = (TCFNodeExecContext)n;
            if (!e.run_context.validate(done)) return false;
            IRunControl.RunControlContext ctx = e.run_context.getData();
            if (ctx != null && ctx.hasState()) {
                TCFDataCache<TCFContextState> state_cache = e.getState();
                if (!state_cache.validate(done)) return false;
                TCFContextState state_data = state_cache.getData();
                if (state_data != null) {
                    if (!state_data.is_suspended) {
                        info.running = true;
                    }
                    else if (e.isNotActive()) {
                        info.not_active = true;
                    }
                    else {
                        info.suspended = true;
                        String r = model.getContextActionResult(e.id);
                        if (r == null) r = state_data.suspend_reason;
                        if (IRunControl.REASON_BREAKPOINT.equals(r)) info.breakpoint = true;
                    }
                }
            }
            else {
                if (!e.hasSuspendedChildren(info, done)) return false;
            }
            if (info.breakpoint && info.running) break;
        }
        return true;
    }

    @Override
    public int compareTo(TCFNode n) {
        if (n instanceof TCFNodeExecContext) {
            TCFNodeExecContext f = (TCFNodeExecContext)n;
            if (mem_seq_no < f.mem_seq_no) return -1;
            if (mem_seq_no > f.mem_seq_no) return +1;
            if (exe_seq_no < f.exe_seq_no) return -1;
            if (exe_seq_no > f.exe_seq_no) return +1;
        }
        return id.compareTo(n.id);
    }
}
