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
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.util.TCFDataCache;

public class TCFNodeStackFrame extends TCFNode {

    private int frame_no;
    private final boolean emulated;
    private final TCFChildrenRegisters children_regs;
    private final TCFChildrenLocalVariables children_vars;
    private final TCFChildrenExpressions children_exps;
    private final TCFDataCache<IStackTrace.StackTraceContext> stack_trace_context;
    private final TCFDataCache<TCFSourceRef> line_info;
    private final TCFDataCache<BigInteger> address;

    TCFNodeStackFrame(final TCFNodeExecContext parent, final String id, final boolean emulated) {
        super(parent, id);
        this.emulated = emulated;
        children_regs = new TCFChildrenRegisters(this);
        children_vars = new TCFChildrenLocalVariables(this);
        children_exps = new TCFChildrenExpressions(this);
        stack_trace_context = new TCFDataCache<IStackTrace.StackTraceContext>(channel) {
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
                IStackTrace st = model.getLaunch().getService(IStackTrace.class);
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
        final Map<BigInteger,TCFSourceRef> line_info_cache = parent.getLineInfoCache();
        line_info = new TCFDataCache<TCFSourceRef>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!stack_trace_context.validate(this)) return false;
                if (!address.validate(this)) return false;
                BigInteger n = address.getData();
                if (n == null) {
                    set(null, address.getError(), null);
                    return true;
                }
                IMemory.MemoryContext mem_ctx = null;
                TCFNode p = parent;
                while (p != null) {
                    if (p instanceof TCFNodeExecContext) {
                        TCFDataCache<IMemory.MemoryContext> cache = ((TCFNodeExecContext)p).getMemoryContext();
                        if (!cache.validate(this)) return false;
                        mem_ctx = cache.getData();
                        if (mem_ctx != null) break;
                    }
                    p = p.parent;
                }
                TCFSourceRef l = line_info_cache.get(n);
                if (l != null) {
                    l.context = mem_ctx;
                    set(null, null, l);
                    return true;
                }
                ILineNumbers ln = model.getLaunch().getService(ILineNumbers.class);
                if (ln == null) {
                    l = new TCFSourceRef();
                    l.context = mem_ctx;
                    l.address = n;
                    set(null, null, l);
                    return true;
                }
                final BigInteger n0 = n;
                final BigInteger n1 = n0.add(BigInteger.valueOf(1));
                final IMemory.MemoryContext ctx = mem_ctx;
                command = ln.mapToSource(parent.id, n0, n1, new ILineNumbers.DoneMapToSource() {
                    public void doneMapToSource(IToken token, Exception error, ILineNumbers.CodeArea[] areas) {
                        TCFSourceRef l = new TCFSourceRef();
                        l.context = ctx;
                        l.address = n0;
                        if (error == null && areas != null && areas.length > 0) {
                            for (ILineNumbers.CodeArea area : areas) {
                                if (l.area == null || area.start_line < l.area.start_line) {
                                    l.area = area;
                                }
                            }
                        }
                        l.error = error;
                        set(token, null, l);
                        if (error == null) line_info_cache.put(l.address, l);
                    }
                });
                return false;
            }
        };
        address = new TCFDataCache<BigInteger>(channel) {
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
                        set(null, null, new BigInteger(n.toString()));
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

    public int getFrameNo() {
        assert Protocol.isDispatchThread();
        return frame_no;
    }

    void setFrameNo(int frame_no) {
        this.frame_no = frame_no;
    }

    @Override
    void dispose() {
        stack_trace_context.dispose();
        line_info.dispose();
        address.dispose();
        children_regs.dispose();
        children_vars.dispose();
        children_exps.dispose();
        super.dispose();
    }

    @Override
    void dispose(String id) {
        children_regs.dispose(id);
        children_vars.dispose(id);
        children_exps.dispose(id);
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

    public BigInteger getReturnAddress() {
        assert Protocol.isDispatchThread();
        if (!stack_trace_context.isValid()) return null;
        IStackTrace.StackTraceContext ctx = stack_trace_context.getData();
        if (ctx != null) {
            Number n = ctx.getReturnAddress();
            if (n instanceof BigInteger) return (BigInteger)n;
            if (n != null) return new BigInteger(n.toString());
        }
        return null;
    }

    public boolean isEmulated() {
        return emulated;
    }

    private TCFChildren getChildren(IPresentationContext ctx) {
        String id = ctx.getId();
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(id)) return children_regs;
        if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(id)) return children_vars;
        if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(id)) return children_exps;
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
        TCFNode[] arr = null;
        TCFChildren c = getChildren(result.getPresentationContext());
        if (c != null) {
            if (!c.validate(done)) return false;
            arr = c.toArray();
        }
        else {
            return true;
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
    protected boolean getData(ILabelUpdate result, Runnable done) {
        TCFChildrenStackTrace stack_trace_cache = ((TCFNodeExecContext)parent).getStackTrace();
        if (!stack_trace_cache.validate(done)) return false;
        if (stack_trace_cache.getData().get(id) == null) {
            result.setLabel("", 0);
        }
        else {
            TCFDataCache<TCFContextState> state_cache = ((TCFNodeExecContext)parent).getState();
            if (!state_cache.validate(done)) return false;
            Throwable error = state_cache.getError();
            if (error == null) error = stack_trace_cache.getError();
            if (error == null) {
                TCFDataCache<?> pending = null;
                if (!stack_trace_context.validate()) pending = stack_trace_context;
                if (!line_info.validate()) pending = line_info;
                if (pending != null) {
                    pending.wait(done);
                    return false;
                }
                if (error == null) error = stack_trace_context.getError();
                if (error == null) error = line_info.getError();
            }
            TCFContextState state_data = state_cache.getData();
            String image_name =  state_data != null && state_data.is_suspended ?
                    ImageCache.IMG_STACK_FRAME_SUSPENDED :
                    ImageCache.IMG_STACK_FRAME_RUNNING;
            if (error != null) {
                if (state_data == null || state_data.is_suspended) {
                    result.setForeground(new RGB(255, 0, 0), 0);
                    result.setLabel(TCFModel.getErrorMessage(error, false), 0);
                }
                else {
                    result.setLabel("...", 0);
                }
            }
            else {
                TCFSourceRef l = line_info.getData();
                if (l == null) {
                    result.setLabel("...", 0);
                }
                else {
                    String module = getModuleName(l.address, done);
                    if (module == null) return false;
                    String label = makeHexAddrString(l.context, l.address) + module;
                    if (l.area != null && l.area.file != null) {
                        label += ": " + l.area.file + ", line " + l.area.start_line;
                    }
                    result.setLabel(label, 0);
                }
            }
            result.setImageDescriptor(ImageCache.getImageDescriptor(image_name), 0);
        }
        return true;
    }

    private String getModuleName(BigInteger pc, Runnable done) {
        TCFDataCache<IRunControl.RunControlContext> parent_dc = ((TCFNodeExecContext)parent).getRunContext();
        if (!parent_dc.validate(done)) return null;
        IRunControl.RunControlContext parent_ctx = parent_dc.getData();
        if (parent_ctx == null) return "";
        String prs_id = parent_ctx.getProcessID();
        if (prs_id == null) return "";
        TCFNodeExecContext prs_node = (TCFNodeExecContext)model.getNode(prs_id);
        TCFDataCache<TCFNodeExecContext.MemoryRegion[]> map_dc = prs_node.getMemoryMap();
        if (!map_dc.validate(done)) return null;
        TCFNodeExecContext.MemoryRegion[] map = map_dc.getData();
        if (map == null) return "";
        for (TCFNodeExecContext.MemoryRegion r : map) {
            String fnm = r.region.getFileName();
            if (fnm != null && r.contains(pc)) {
                fnm = fnm.replace('\\', '/');
                int x = fnm.lastIndexOf('/');
                if (x >= 0) fnm = fnm.substring(x + 1);
                return " [" + fnm + "]";
            }
        }
        return "";
    }

    private String makeHexAddrString(IMemory.MemoryContext m, BigInteger n) {
        String s = n.toString(16);
        int sz = (m != null ? m.getAddressSize() : 4) * 2;
        int l = sz - s.length();
        if (l < 0) l = 0;
        if (l > 16) l = 16;
        return "0x0000000000000000".substring(0, 2 + l) + s;
    }

    void onSourceMappingChange() {
        line_info.reset();
        addModelDelta(IModelDelta.STATE);
    }

    void onSuspended() {
        stack_trace_context.cancel();
        line_info.cancel();
        address.cancel();
        children_regs.onSuspended();
        children_vars.onSuspended();
        children_exps.onSuspended();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onMemoryMapChanged() {
        addModelDelta(IModelDelta.STATE);
    }

    void onRegistersChanged() {
        children_regs.onRegistersChanged();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    void onRegisterValueChanged() {
        stack_trace_context.cancel();
        line_info.cancel();
        address.cancel();
        addModelDelta(IModelDelta.STATE);
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
