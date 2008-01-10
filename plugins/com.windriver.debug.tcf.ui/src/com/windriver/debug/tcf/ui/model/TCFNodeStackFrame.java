/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.ui.model;

import java.math.BigInteger;
import java.util.Arrays;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;

import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.services.ILineNumbers;
import com.windriver.tcf.api.services.IMemory;
import com.windriver.tcf.api.services.IRunControl;
import com.windriver.tcf.api.services.IStackTrace;
import com.windriver.tcf.api.services.ILineNumbers.CodeArea;

public class TCFNodeStackFrame extends TCFNode {

    private IStackTrace.StackTraceContext stack_trace_context;
    private ILineNumbers.CodeArea code_area;

    private final int frame_no;
    private final TCFChildren children_regs;

    TCFNodeStackFrame(TCFNode parent, String id, TCFChildren children_regs) {
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
        children_regs.dispose();
        super.dispose();
    }

    @Override
    void dispose(String id) {
        children_regs.dispose(id);
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
            return stack_trace_context.getReturnAddress().toString();
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

    @Override
    protected void invalidateNode(int flags) {
        super.invalidateNode(flags);
        if ((flags & CF_CHILDREN) != 0) {
            children_regs.invalidate();
        }
    }

    @Override
    protected boolean validateContext(TCFRunnable done) {
        assert data_command == null;
        if (frame_no == 0) {
            node_valid |= CF_CONTEXT;
            return true;
        }
        IStackTrace st = model.getLaunch().getService(IStackTrace.class);
        if (done != null) wait_list.add(done);
        data_command = st.getContext(new String[]{ id }, new IStackTrace.DoneGetContext() {
            public void doneGetContext(IToken token, Exception error, IStackTrace.StackTraceContext[] context) {
                if (data_command != token) return;
                data_command = null;
                if (error != null) {
                    node_error = error;
                }
                else {
                    stack_trace_context = context[0];
                }
                BigInteger n = null;
                ILineNumbers ln = model.getLaunch().getService(ILineNumbers.class);
                if (node_error == null && ln != null) {
                    String s = getAddress();
                    if (s != null) n = new BigInteger(s);
                }
                code_area = null;
                if (n == null) {
                    node_valid |= CF_CONTEXT;
                    validateNode(null);
                }
                else {
                    BigInteger m = n.add(BigInteger.valueOf(1));
                    data_command = ln.mapToSource(parent.id, n, m, new ILineNumbers.DoneMapToSource() {
                        public void doneMapToSource(IToken token, Exception error, CodeArea[] areas) {
                            if (data_command != token) return;
                            data_command = null;
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
                            node_valid |= CF_CONTEXT;
                            validateNode(null);
                        }
                    });
                }
            }
        });
        return false;
    }

    @Override
    protected boolean validateChildren(TCFRunnable done) {
        if (!children_regs.valid && !children_regs.validate(done)) return false;
        node_valid |= CF_CHILDREN;
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
