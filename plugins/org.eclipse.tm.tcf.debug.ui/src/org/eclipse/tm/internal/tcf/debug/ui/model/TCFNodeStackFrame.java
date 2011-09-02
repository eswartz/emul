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

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFFunctionRef;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.util.TCFDataCache;

public class TCFNodeStackFrame extends TCFNode {

    private int frame_no;
    private boolean trace_limit;
    private final boolean emulated;
    private final TCFChildrenRegisters children_regs;
    private final TCFChildrenLocalVariables children_vars;
    private final TCFChildrenExpressions children_exps;
    private final TCFChildrenHoverExpressions children_hover_exps;
    private final TCFData<IStackTrace.StackTraceContext> stack_trace_context;
    private final TCFData<TCFSourceRef> line_info;
    private final TCFData<TCFFunctionRef> func_info;
    private final TCFData<BigInteger> address;

    private String hover_expression;

    TCFNodeStackFrame(final TCFNodeExecContext parent, final String id, final boolean emulated) {
        super(parent, id);
        this.emulated = emulated;
        children_regs = new TCFChildrenRegisters(this);
        children_vars = new TCFChildrenLocalVariables(this);
        children_exps = new TCFChildrenExpressions(this);
        children_hover_exps = new TCFChildrenHoverExpressions(this);
        stack_trace_context = new TCFData<IStackTrace.StackTraceContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                if (emulated) {
                    set(null, null, null);
                    return true;
                }
                TCFDataCache<TCFContextState> parent_state_cache = parent.getState();
                if (!parent_state_cache.validate(this)) return false;
                TCFContextState parent_state_data = parent_state_cache.getData();
                if (parent_state_data == null || !parent_state_data.is_suspended) {
                    set(null, null, null);
                    return true;
                }
                TCFChildrenStackTrace stack_trace_cache = parent.getStackTrace();
                if (!stack_trace_cache.validate(this)) return false;
                if (frame_no < 0) {
                    set(null, null, null);
                    return true;
                }
                IStackTrace st = launch.getService(IStackTrace.class);
                if (st == null) {
                    assert frame_no == 0;
                    set(null, null, null);
                    return true;
                }
                command = st.getContext(new String[]{ id }, new IStackTrace.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, IStackTrace.StackTraceContext[] context) {
                        set(token, error, context == null || context.length == 0 ? null : context[0]);
                    }
                });
                return false;
            }
        };
        line_info = new TCFData<TCFSourceRef>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!stack_trace_context.validate(this)) return false;
                if (!address.validate(this)) return false;
                BigInteger n = address.getData();
                if (n == null) {
                    set(null, address.getError(), null);
                    return true;
                }
                if (frame_no > 0) n = n.subtract(BigInteger.valueOf(1));
                TCFDataCache<TCFNodeExecContext> mem_cache = ((TCFNodeExecContext)parent).getMemoryNode();
                if (!mem_cache.validate(this)) return false;
                if (mem_cache.getError() != null || mem_cache.getData() == null) {
                    set(null, mem_cache.getError(), null);
                    return true;
                }
                TCFDataCache<TCFSourceRef> info_cache = mem_cache.getData().getLineInfo(n);
                if (info_cache == null) {
                    set(null, null, null);
                    return true;
                }
                if (!info_cache.validate(this)) return false;
                set(null, info_cache.getError(), info_cache.getData());
                return true;
            }
        };
        func_info = new TCFData<TCFFunctionRef>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!address.validate(this)) return false;
                BigInteger n = address.getData();
                if (n == null) {
                    set(null, address.getError(), null);
                    return true;
                }
                TCFDataCache<TCFNodeExecContext> mem_cache = ((TCFNodeExecContext)parent).getMemoryNode();
                if (!mem_cache.validate(this)) return false;
                if (mem_cache.getError() != null || mem_cache.getData() == null) {
                    set(null, mem_cache.getError(), null);
                    return true;
                }
                TCFDataCache<TCFFunctionRef> info_cache = mem_cache.getData().getFuncInfo(n);
                if (info_cache == null) {
                    set(null, null, null);
                    return true;
                }
                if (!info_cache.validate(this)) return false;
                set(null, info_cache.getError(), info_cache.getData());
                return true;
            }
        };
        address = new TCFData<BigInteger>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!stack_trace_context.validate(this)) return false;
                IStackTrace.StackTraceContext ctx = stack_trace_context.getData();
                if (ctx != null) {
                    Number n = ctx.getInstructionAddress();
                    if (n instanceof BigInteger) {
                        set(null, null, (BigInteger)n);
                        return true;
                    }
                    if (n != null) {
                        set(null, null, JSON.toBigInteger(n));
                        return true;
                    }
                }
                if (frame_no == 0) {
                    TCFDataCache<BigInteger> addr_cache = parent.getAddress();
                    if (!addr_cache.validate(this)) return false;
                    set(null, addr_cache.getError(), addr_cache.getData());
                    return true;
                }
                set(null, stack_trace_context.getError(), null);
                return true;
            }
        };
    }

    /**
     * Get frame position in the parent's stack trace.
     * Top frame position is 0.
     * @return frame position or -1 if the frame is not part of the trace.
     */
    public int getFrameNo() {
        assert Protocol.isDispatchThread();
        return frame_no;
    }

    void setFrameNo(int frame_no) {
        this.frame_no = frame_no;
    }

    void setTraceLimit(boolean trace_limit) {
        this.trace_limit = trace_limit;
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

    public TCFDataCache<TCFSourceRef> getLineInfo() {
        return line_info;
    }

    public TCFDataCache<IStackTrace.StackTraceContext> getStackTraceContext() {
        return stack_trace_context;
    }

    public TCFDataCache<BigInteger> getAddress() {
        return address;
    }

    TCFChildren getRegisters() {
        return children_regs;
    }

    public BigInteger getReturnAddress() {
        assert Protocol.isDispatchThread();
        if (!stack_trace_context.isValid()) return null;
        IStackTrace.StackTraceContext ctx = stack_trace_context.getData();
        if (ctx != null) return JSON.toBigInteger(ctx.getReturnAddress());
        return null;
    }

    public boolean isEmulated() {
        return emulated;
    }

    boolean isTraceLimit() {
        return trace_limit && ((TCFNodeExecContext)parent).getViewBottomFrame() == this;
    }

    void riseTraceLimit() {
        ((TCFNodeExecContext)parent).riseTraceLimit();
    }

    private TCFChildren getChildren(IPresentationContext ctx) {
        String id = ctx.getId();
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(id)) return children_regs;
        if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(id)) return children_vars;
        if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(id)) return children_exps;
        if (TCFModel.ID_EXPRESSION_HOVER.equals(id)) return children_hover_exps;
        return null;
    }

    @Override
    protected boolean getData(IHasChildrenUpdate result, Runnable done) {
        TCFChildren c = getChildren(result.getPresentationContext());
        if (c != null) {
            if (!c.validate(done)) return false;
            result.setHasChilren(c.size() > 0);
        }
        else {
            result.setHasChilren(false);
        }
        return true;
    }

    @Override
    protected boolean getData(IChildrenCountUpdate result, Runnable done) {
        TCFChildren c = getChildren(result.getPresentationContext());
        if (c != null) {
            if (!c.validate(done)) return false;
            result.setChildCount(c.size());
        }
        else {
            result.setChildCount(0);
        }
        return true;
    }

    @Override
    protected boolean getData(IChildrenUpdate result, Runnable done) {
        TCFChildren children = getChildren(result.getPresentationContext());
        if (children == null) return true;
        return children.getData(result, done);
    }

    @Override
    protected boolean getData(ILabelUpdate result, Runnable done) {
        TCFChildrenStackTrace stack_trace_cache = ((TCFNodeExecContext)parent).getStackTrace();
        if (!stack_trace_cache.validate(done)) return false;
        if (stack_trace_cache.getData().get(id) == null) {
            result.setLabel("", 0);
        }
        else if (isTraceLimit()) {
            result.setLabel("<select to see more frames>", 0);
        }
        else {
            boolean show_arg_names = model.getShowFunctionArgNames();
            boolean show_arg_values = model.getShowFunctionArgValues();
            TCFDataCache<TCFContextState> state_cache = ((TCFNodeExecContext)parent).getState();
            TCFDataCache<TCFNodeExecContext> mem_cache = ((TCFNodeExecContext)parent).getMemoryNode();
            TCFDataCache<?> pending = null;
            if (!state_cache.validate()) pending = state_cache;
            if (!mem_cache.validate()) pending = mem_cache;
            if (!stack_trace_context.validate()) pending = stack_trace_context;
            if (!address.validate()) pending = address;
            if (!line_info.validate()) pending = line_info;
            if (!func_info.validate()) pending = func_info;
            if (show_arg_names || show_arg_values) {
                if (!children_vars.validate()) {
                    pending = children_vars;
                }
                else {
                    for (TCFNode n : children_vars.toArray()) {
                        TCFNodeExpression e = (TCFNodeExpression)n;
                        if (!e.getVariable().validate()) pending = e.getVariable();
                    }
                }
            }
            if (pending != null) {
                pending.wait(done);
                return false;
            }
            Throwable error = state_cache.getError();
            if (error == null) error = stack_trace_cache.getError();
            if (error == null) error = stack_trace_context.getError();
            if (error == null) error = address.getError();
            if (error == null) error = line_info.getError();
            BigInteger addr = address.getData();
            TCFSourceRef sref = line_info.getData();
            TCFContextState state = state_cache.getData();
            StringBuffer bf = new StringBuffer();
            if (addr != null) {
                bf.append(makeHexAddrString(sref != null ? sref.address_size : 0, addr));
                TCFNodeExecContext mem_node = mem_cache.getData();
                if (mem_node != null) {
                    TCFDataCache<TCFNodeExecContext.MemoryRegion[]> map_dc = mem_node.getMemoryMap();
                    if (!map_dc.validate(done)) return false;
                    TCFNodeExecContext.MemoryRegion[] map = map_dc.getData();
                    if (map != null) {
                        for (TCFNodeExecContext.MemoryRegion r : map) {
                            String fnm = r.region.getFileName();
                            if (fnm != null && r.contains(addr)) {
                                fnm = fnm.replace('\\', '/');
                                int x = fnm.lastIndexOf('/');
                                if (x >= 0) fnm = fnm.substring(x + 1);
                                bf.append(" [");
                                bf.append(fnm);
                                bf.append("]");
                                break;
                            }
                        }
                    }
                }
            }
            TCFFunctionRef ref = func_info.getData();
            if (ref != null && ref.symbol_id != null) {
                TCFDataCache<ISymbols.Symbol> sym_cache = model.getSymbolInfoCache(ref.symbol_id);
                if (!sym_cache.validate(done)) return false;
                ISymbols.Symbol sym_data = sym_cache.getData();
                if (sym_data != null && sym_data.getName() != null) {
                    bf.append(" ");
                    bf.append(sym_data.getName());
                    bf.append('(');
                    if (show_arg_names || show_arg_values) {
                        if (children_vars.getError() != null) {
                            bf.append('?');
                        }
                        else {
                            int cnt = 0;
                            for (TCFNode n : children_vars.toArray()) {
                                ISymbols.Symbol sym = null;
                                TCFNodeExpression expr_node = (TCFNodeExpression)n;
                                IExpressions.Expression expr_props = expr_node.getVariable().getData();
                                if (expr_props != null) {
                                    TCFDataCache<ISymbols.Symbol> s = model.getSymbolInfoCache(expr_props.getSymbolID());
                                    if (!s.validate(done)) return false;
                                    sym = s.getData();
                                }
                                if (sym == null) continue;
                                if (!sym.getFlag(ISymbols.SYM_FLAG_PARAMETER)) continue;
                                if (cnt > 0) bf.append(',');
                                if (show_arg_names) {
                                    String name = "?";
                                    if (sym != null && sym.getName() != null) name = sym.getName();
                                    bf.append(name);
                                    if (show_arg_values) bf.append('=');
                                }
                                if (show_arg_values) {
                                    String s = expr_node.getValueText(done);
                                    if (s == null) return false;
                                    bf.append(s.length() == 0 ? "?" : s);
                                }
                                cnt++;
                            }
                        }
                    }
                    bf.append(')');
                }
            }
            if (sref != null && sref.area != null && sref.area.file != null) {
                bf.append(": ");
                int l = sref.area.file.length();
                if (l > 32) {
                    bf.append("...");
                    bf.append(sref.area.file.substring(l - 32));
                }
                else {
                    bf.append(sref.area.file);
                }
                bf.append(", line ");
                bf.append(sref.area.start_line);
            }
            if (error != null) {
                if (state == null || state.is_suspended) {
                    result.setForeground(new RGB(255, 0, 0), 0);
                    if (bf.length() > 0) bf.append(": ");
                    bf.append(TCFModel.getErrorMessage(error, false));
                }
                else {
                    result.setLabel("...", 0);
                }
            }
            if (bf.length() == 0) bf.append("...");
            result.setLabel(bf.toString(), 0);
            String image_name =  state != null && state.is_suspended ?
                    ImageCache.IMG_STACK_FRAME_SUSPENDED :
                    ImageCache.IMG_STACK_FRAME_RUNNING;
            result.setImageDescriptor(ImageCache.getImageDescriptor(image_name), 0);
        }
        return true;
    }

    @Override
    protected boolean getData(IViewerInputUpdate result, Runnable done) {
        result.setInputElement(result.getElement());
        String id = result.getPresentationContext().getId();
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(id) || IDebugUIConstants.ID_EXPRESSION_VIEW.equals(id)) {
            TCFNodeExecContext exe = (TCFNodeExecContext)parent;
            TCFChildrenStackTrace stack_trace_cache = exe.getStackTrace();
            if (!stack_trace_cache.validate(done)) return false;
            if (stack_trace_cache.getTopFrame() == this) result.setInputElement(exe);
        }
        else if (IDebugUIConstants.ID_MODULE_VIEW.equals(id)) {
            TCFDataCache<TCFNodeExecContext> mem = model.searchMemoryContext(this);
            if (mem == null) return true;
            if (!mem.validate(done)) return false;
            if (mem.getData() == null) return true;
            result.setInputElement(mem.getData());
        }
        return true;
    }

    private String makeHexAddrString(int addr_size, BigInteger n) {
        String s = n.toString(16);
        int sz = (addr_size != 0 ? addr_size : 4) * 2;
        int l = sz - s.length();
        if (l < 0) l = 0;
        if (l > 16) l = 16;
        return "0x0000000000000000".substring(0, 2 + l) + s;
    }

    void postAllChangedDelta() {
        for (TCFModelProxy p : model.getModelProxies()) {
            int flags = 0;
            String view_id = p.getPresentationContext().getId();
            if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id) &&
                    (launch.getContextActionsCount(parent.id) == 0 ||
                    !model.getDelayStackUpdateUtilLastStep())) {
                flags |= IModelDelta.STATE;
            }
            if (getChildren(p.getPresentationContext()) != null && p.getInput() == this) flags |= IModelDelta.CONTENT;
            if (flags == 0) continue;
            p.addDelta(this, flags);
        }
    }

    private void postStateChangedDelta() {
        for (TCFModelProxy p : model.getModelProxies()) {
            String id = p.getPresentationContext().getId();
            if (IDebugUIConstants.ID_DEBUG_VIEW.equals(id)) {
                p.addDelta(this, IModelDelta.STATE);
            }
        }
    }

    void onExpressionAddedOrRemoved() {
        children_exps.cancel();
    }

    void onSourceMappingChange() {
        line_info.reset();
        postStateChangedDelta();
    }

    void onSuspended() {
        stack_trace_context.cancel();
        line_info.cancel();
        func_info.cancel();
        address.cancel();
        children_regs.onSuspended();
        // Unlike thread registers, stack frame register list must be retrieved on every suspend
        children_regs.reset();
        children_vars.onSuspended();
        children_exps.onSuspended();
        children_hover_exps.onSuspended();
        // delta is posted by the parent node
    }

    void onMemoryMapChanged() {
        line_info.reset();
        func_info.reset();
        children_vars.onMemoryMapChanged();
        children_exps.onMemoryMapChanged();
        postAllChangedDelta();
    }

    void onMemoryChanged() {
        stack_trace_context.cancel();
        line_info.cancel();
        func_info.cancel();
        address.cancel();
        children_vars.onMemoryChanged();
        children_exps.onMemoryChanged();
        children_hover_exps.onMemoryChanged();
        postStateChangedDelta();
    }

    void onRegistersChanged() {
        children_regs.onRegistersChanged();
        postAllChangedDelta();
    }

    void onRegisterValueChanged() {
        stack_trace_context.cancel();
        line_info.cancel();
        func_info.cancel();
        address.cancel();
        children_vars.onRegisterValueChanged();
        children_exps.onRegisterValueChanged();
        children_hover_exps.onRegisterValueChanged();
        postStateChangedDelta();
    }

    @Override
    public int compareTo(TCFNode n) {
        if (n instanceof TCFNodeStackFrame) {
            TCFNodeStackFrame f = (TCFNodeStackFrame)n;
            if (frame_no < f.frame_no) return -1;
            if (frame_no > f.frame_no) return +1;
        }
        return id.compareTo(n.id);
    }
}
