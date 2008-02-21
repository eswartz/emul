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
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.services.ILineNumbers.CodeArea;


public class TCFNodeStackFrame extends TCFNode {

    private IStackTrace.StackTraceContext stack_trace_context;
    private ILineNumbers.CodeArea code_area;
    private BigInteger code_address;

    private final int frame_no;
    private final TCFChildrenRegisters children_regs;

    TCFNodeStackFrame(TCFNode parent, String id, TCFChildrenRegisters children_regs) {
        super(parent, id);
        this.frame_no = 0;
        this.children_regs = children_regs;
    }

    TCFNodeStackFrame(TCFNode parent, String id, int frame_no) {
        super(parent, id);
        this.frame_no = frame_no;
        children_regs = new TCFChildrenRegisters(this);
    }

    int getFrameNo() {
        return frame_no;
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
        if (stack_trace_context != null) {
            Number addr = stack_trace_context.getReturnAddress();
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
        int offset = 0;
        TCFNode[] arr = null;
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(result.getPresentationContext().getId())) {
            arr = children_regs.toArray();
        }
        else {
            arr = null;
        }
        if (arr != null) {
            Arrays.sort(arr);
            for (TCFNode n : arr) {
                if (offset >= result.getOffset() && offset < result.getOffset() + result.getLength()) {
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
        result.setImageDescriptor(getImageDescriptor(getImageName()), 0);
        String label = id;
        Number n = null;
        if (frame_no == 0 && parent.getAddress() != null) {
            n = new BigInteger(parent.getAddress());
        }
        else if (stack_trace_context != null) {
            n = stack_trace_context.getReturnAddress();
        }
        if (n == null) {
            label = "...";
        }
        else {
            label = makeHexAddrString(n);
            if (code_area != null && code_area.file != null) {
                label += ": " + code_area.file + ", line " + (code_area.start_line + 1);
            }
        }
        result.setLabel(label, 0);
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
        super.invalidateNode();
        code_address = null;
        code_area = null;
        makeModelDelta(IModelDelta.STATE);
    }

    void onSuspended() {
        super.invalidateNode();
        stack_trace_context = null;
        children_regs.onSuspended();
        makeModelDelta(IModelDelta.STATE);
    }

    @Override
    public void invalidateNode() {
        super.invalidateNode();
        stack_trace_context = null;
        code_address = null;
        code_area = null;
        children_regs.invalidate();
    }

    @Override
    protected boolean validateNodeData() {
        assert pending_command == null;
        if (node_error != null) return true;
        if (frame_no == 0) {
            if (!children_regs.valid) {
                assert children_regs.node == parent;
                // Need to validate parent for children_regs to be valid.
                ArrayList<TCFNode> nodes = new ArrayList<TCFNode>();
                nodes.add(parent);
                if (!validateNodes(nodes)) return false;
            }
            return validateSourceMapping();
        }
        if (!children_regs.valid && !children_regs.validate()) return false;
        if (stack_trace_context != null) return true;
        IStackTrace st = model.getLaunch().getService(IStackTrace.class);
        pending_command = st.getContext(new String[]{ id }, new IStackTrace.DoneGetContext() {
            public void doneGetContext(IToken token, Exception error, IStackTrace.StackTraceContext[] context) {
                if (pending_command != token) return;
                pending_command = null;
                if (error != null) {
                    node_error = error;
                }
                else {
                    stack_trace_context = context[0];
                }
                if (!validateSourceMapping()) return;
                validateNode();
            }
        });
        return false;
    }

    private boolean validateSourceMapping() {
        BigInteger n = null;
        ILineNumbers ln = model.getLaunch().getService(ILineNumbers.class);
        if (node_error == null && ln != null) {
            String s = getAddress();
            if (s != null) n = new BigInteger(s);
        }
        if (n != null && n.equals(code_address)) return true;
        if (n == null) {
            code_area = null;
            code_address = null;
            return true;
        }
        final BigInteger n0 = n;
        final BigInteger n1 = n0.add(BigInteger.valueOf(1));
        pending_command = ln.mapToSource(parent.id, n0, n1, new ILineNumbers.DoneMapToSource() {
            public void doneMapToSource(IToken token, Exception error, CodeArea[] areas) {
                if (pending_command != token) return;
                pending_command = null;
                code_area = null;
                if (error != null) {
                    node_error = error;
                }
                else if (areas != null && areas.length > 0) {
                    for (ILineNumbers.CodeArea area : areas) {
                        if (code_area == null || area.start_line < code_area.start_line) {
                            code_area = area;
                        }
                    }
                }
                code_address = n0;
                validateNode();
            }
        });
        return false;
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
