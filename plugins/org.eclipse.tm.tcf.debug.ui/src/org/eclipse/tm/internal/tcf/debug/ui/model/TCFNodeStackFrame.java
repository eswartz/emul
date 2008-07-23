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
import java.util.Map;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tm.tcf.util.TCFDataCache;

public class TCFNodeStackFrame extends TCFNode {

    private int frame_no;
    private final TCFChildrenRegisters children_regs;
    private final TCFChildrenLocalVariables children_vars;
    private final TCFDataCache<IStackTrace.StackTraceContext> stack_trace_context;
    private final TCFDataCache<TCFSourceRef> line_info;
    
    TCFNodeStackFrame(final TCFNodeExecContext parent, final String id) {
        super(parent, id);
        children_regs = new TCFChildrenRegisters(this);
        children_vars = new TCFChildrenLocalVariables(this);
        IChannel channel = model.getLaunch().getChannel();
        stack_trace_context = new TCFDataCache<IStackTrace.StackTraceContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                if (!parent.isSuspended()) {
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
                if (!stack_trace_context.validate()) {
                    stack_trace_context.wait(this);
                    return false;
                }
                BigInteger n = getAddress();
                if (n == null) {
                    set(null, null, null);
                    return true;
                }
                TCFSourceRef l = line_info_cache.get(n);
                if (l != null) {
                    set(null, null, l);
                    return true;
                }
                ILineNumbers ln = model.getLaunch().getService(ILineNumbers.class);
                if (ln == null) {
                    l = new TCFSourceRef();
                    l.address = n;
                    set(null, null, l);
                    return true;
                }
                final BigInteger n0 = n;
                final BigInteger n1 = n0.add(BigInteger.valueOf(1));
                command = ln.mapToSource(parent.id, n0, n1, new ILineNumbers.DoneMapToSource() {
                    public void doneMapToSource(IToken token, Exception error, CodeArea[] areas) {
                        TCFSourceRef l = new TCFSourceRef();
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
    }

    public int getFrameNo() {
        assert Protocol.isDispatchThread();
        return frame_no;
    }
    
    void setFrameNo(int frame_no) {
        this.frame_no = frame_no;
    }
    
    public TCFDataCache<TCFSourceRef> getLineInfo() {
        return line_info;
    }

    @Override
    void dispose() {
        children_regs.dispose();
        children_vars.dispose();
        super.dispose();
    }

    @Override
    void dispose(String id) {
        children_regs.dispose(id);
        children_vars.dispose(id);
    }
    
    public TCFDataCache<IStackTrace.StackTraceContext> getStackTraceContext() {
        return stack_trace_context;
    }

    @Override
    public BigInteger getAddress() {
        assert Protocol.isDispatchThread();
        if (!stack_trace_context.isValid()) return null;
        IStackTrace.StackTraceContext ctx = stack_trace_context.getData();
        if (ctx != null) {
            Number n = ctx.getInstructionAddress();
            if (n instanceof BigInteger) return (BigInteger)n;
            if (n != null) return new BigInteger(n.toString());
        }
        if (frame_no == 0) return parent.getAddress();
        return null;
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
    
    @Override
    public int getNodeIndex(IPresentationContext p, TCFNode n) {
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(p.getId())) {
            if (!children_regs.isValid()) return -1;
            return children_regs.getIndexOf(n);
        }
        else if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(p.getId())) {
            if (!children_vars.isValid()) return -1;
            return children_vars.getIndexOf(n);
        }
        else {
            return 0;
        }
    }
    
    @Override
    public int getChildrenCount(IPresentationContext p) {
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(p.getId())) {
            if (!children_regs.isValid()) return -1;
            return children_regs.size();
        }
        else if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(p.getId())) {
            if (!children_vars.isValid()) return -1;
            return children_vars.size();
        }
        else {
            return 0;
        }
    }

    @Override
    protected void getData(IChildrenCountUpdate result) {
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(result.getPresentationContext().getId())) {
            result.setChildCount(children_regs.size());
        }
        else if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(result.getPresentationContext().getId())) {
            result.setChildCount(children_vars.size());
        }
        else {
            result.setChildCount(0);
        }
    }

    @Override
    protected void getData(IChildrenUpdate result) {
        TCFNode[] arr = null;
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(result.getPresentationContext().getId())) {
            arr = children_regs.toArray();
        }
        else if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(result.getPresentationContext().getId())) {
            arr = children_vars.toArray();
        }
        else {
            arr = new TCFNode[0];
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
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(result.getPresentationContext().getId())) {
            result.setHasChilren(children_regs.size() > 0);
        }
        else if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(result.getPresentationContext().getId())) {
            result.setHasChilren(children_vars.size() > 0);
        }
        else {
            result.setHasChilren(false);
        }
    }

    @Override
    protected void getData(ILabelUpdate result) {
        result.setImageDescriptor(ImageCache.getImageDescriptor(getImageName()), 0);
        Throwable error = stack_trace_context.getError();
        if (error == null) error = line_info.getError();
        if (error != null && ((TCFNodeExecContext)parent).isSuspended()) {
            result.setForeground(new RGB(255, 0, 0), 0);
            result.setLabel(error.getClass().getName() + ": " + error.getMessage(), 0);
        }
        else {
            TCFSourceRef l = line_info.getData();
            if (l == null) {
                result.setLabel("...", 0);
            }
            else {
                String label = makeHexAddrString(l.address);
                if (l.area != null && l.area.file != null) {
                    label += ": " + l.area.file + ", line " + l.area.start_line;
                }
                result.setLabel(label, 0);
            }
        }
    }

    private String makeHexAddrString(Number n) {
        BigInteger i = null;
        if (n instanceof BigInteger) i = (BigInteger)n;
        else i = new BigInteger(n.toString());
        String s = i.toString(16);
        IMemory.MemoryContext m = ((TCFNodeExecContext)parent).getMemoryContext();
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
        stack_trace_context.reset();
        line_info.reset();
        children_regs.onSuspended();
        children_vars.onSuspended();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }
    
    void onRegistersChanged() {
        children_regs.onRegistersChanged();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    @Override
    public void invalidateNode() {
        stack_trace_context.reset();
        line_info.reset();
        children_regs.reset();
        children_vars.reset();
    }

    @Override
    public boolean validateNode(Runnable done) {
        stack_trace_context.validate();
        children_regs.validate();
        children_vars.validate();
        if (!stack_trace_context.isValid()) {
            stack_trace_context.wait(done);
            return false;
        }
        if (!children_regs.isValid()) {
            children_regs.wait(done);
            return false;
        }
        if (!children_vars.isValid()) {
            children_vars.wait(done);
            return false;
        }
        if (!line_info.validate()) {
            line_info.wait(done);
            return false;
        }
        return true;
    }

    @Override
    protected String getImageName() {
        if (((TCFNodeExecContext)parent).isRunning()) return ImageCache.IMG_STACK_FRAME_RUNNING;
        return ImageCache.IMG_STACK_FRAME_SUSPENDED;
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
