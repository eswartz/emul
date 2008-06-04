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
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tm.tcf.util.TCFDataCache;

public class TCFNodeStackFrame extends TCFNode {

    private int frame_no;
    private final TCFChildrenRegisters children_regs;
    private final TCFDataCache<IStackTrace.StackTraceContext> stack_trace_context;
    private final TCFDataCache<TCFSourceRef> line_info;
    
    private TCFNodeStackFrame(final TCFNodeExecContext parent, final String id, final int frame_no, TCFChildrenRegisters regs) {
        super(parent, id);
        this.frame_no = frame_no;
        if (regs == null) regs = new TCFChildrenRegisters(this);
        this.children_regs = regs;
        IChannel channel = model.getLaunch().getChannel();
        stack_trace_context = new TCFDataCache<IStackTrace.StackTraceContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                assert command == null;
                if (frame_no == 0) {
                    set(null, null, null);
                    return true;
                }
                IStackTrace st = model.getLaunch().getService(IStackTrace.class);
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
                BigInteger n = null;
                if (frame_no == 0) {
                    if (!parent.validateNode(this)) return false;
                }
                else {
                    if (!stack_trace_context.validate()) {
                        stack_trace_context.wait(this);
                        return false;
                    }
                }
                String s = getAddress();
                if (s != null) n = new BigInteger(s);
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

    TCFNodeStackFrame(TCFNodeExecContext parent, String id, TCFChildrenRegisters children_regs) {
        this(parent, id, 0, children_regs);
    }

    TCFNodeStackFrame(TCFNodeExecContext parent, String id, int frame_no) {
        this(parent, id, frame_no, null);
    }

    int getFrameNo() {
        assert Protocol.isDispatchThread();
        return frame_no;
    }
    
    void setFrameNo(int frame_no) {
        assert this.frame_no != 0 && frame_no != 0;
        this.frame_no = frame_no;
    }
    
    TCFDataCache<TCFSourceRef> getLineInfo() {
        return line_info;
    }

    @Override
    void dispose() {
        if (frame_no != 0) children_regs.dispose();
        super.dispose();
    }

    @Override
    void dispose(String id) {
        if (frame_no != 0) children_regs.dispose(id);
    }

    @Override
    public IRunControl.RunControlContext getRunContext() {
        return parent.getRunContext();
    }

    @Override
    public IMemory.MemoryContext getMemoryContext() {
        return parent.getMemoryContext();
    }

    @Override
    public boolean isRunning() {
        return parent.isRunning();
    }

    @Override
    public boolean isSuspended() {
        return parent.isSuspended();
    }

    @Override
    public String getAddress() {
        assert Protocol.isDispatchThread();
        if (frame_no == 0) return parent.getAddress();
        if (!stack_trace_context.isValid()) return null;
        IStackTrace.StackTraceContext ctx = stack_trace_context.getData();
        if (ctx != null) {
            Number addr = ctx.getReturnAddress();
            if (addr != null) return addr.toString();
        }
        return null;
    }
    
    @Override
    protected void getData(IChildrenCountUpdate result) {
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(result.getPresentationContext().getId())) {
            result.setChildCount(children_regs.size());
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
        else {
            arr = null;
        }
        if (arr != null) {
            Arrays.sort(arr);
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
    }

    @Override
    protected void getData(IHasChildrenUpdate result) {
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(result.getPresentationContext().getId())) {
            result.setHasChilren(children_regs.size() > 0);
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
        if (error != null) {
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
        IMemory.MemoryContext m = getMemoryContext();
        int sz = (m != null ? m.getAddressSize() : 4) * 2;
        int l = sz - s.length();
        if (l < 0) l = 0;
        if (l > 16) l = 16;
        return "0x0000000000000000".substring(0, 2 + l) + s;
    }

    void onSourceMappingChange() {
        line_info.reset();
        makeModelDelta(IModelDelta.STATE);
    }

    void onSuspended() {
        stack_trace_context.reset();
        line_info.reset();
        children_regs.onSuspended();
        makeModelDelta(IModelDelta.STATE);
    }
    
    void onRegistersChanged() {
        children_regs.onRegistersChanged();
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    @Override
    public void invalidateNode() {
        stack_trace_context.reset();
        line_info.reset();
        children_regs.reset();
    }

    @Override
    public boolean validateNode(Runnable done) {
        if (frame_no == 0 && !parent.validateNode(done)) return false;
        stack_trace_context.validate();
        children_regs.validate();
        if (!stack_trace_context.isValid()) {
            stack_trace_context.wait(done);
            return false;
        }
        if (!children_regs.isValid()) {
            children_regs.wait(done);
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
        if (isRunning()) return "icons/full/obj16/stckframe_running_obj.gif";
        return "icons/full/obj16/stckframe_obj.gif";
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
