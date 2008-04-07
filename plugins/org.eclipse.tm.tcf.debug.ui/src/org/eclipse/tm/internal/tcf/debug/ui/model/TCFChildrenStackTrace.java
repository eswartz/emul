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

import java.util.HashMap;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IStackTrace;


public class TCFChildrenStackTrace extends TCFChildren {

    private final TCFNodeExecContext node;
    private final TCFChildrenRegisters children_regs;

    TCFChildrenStackTrace(TCFNodeExecContext node, TCFChildrenRegisters children_regs) {
        super(node.model.getLaunch().getChannel(), 16);
        this.node = node;
        this.children_regs = children_regs;
    }
    
    void onSourceMappingChange() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onSourceMappingChange();
    }

    void onSuspended() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onSuspended();
        reset();
    }
    
    void onRegistersChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onRegistersChanged();
    }

    void onResumed() {
        reset(null);
    }

    @Override
    protected boolean startDataRetrieval() {
        final HashMap<String,TCFNode> data = new HashMap<String,TCFNode>();
        if (!node.isSuspended()) {
            set(null, null, data);
            return true;
        }
        String nm = node.id + "-TF";
        TCFNodeStackFrame n = (TCFNodeStackFrame)node.model.getNode(nm);
        if (n == null) n = new TCFNodeStackFrame(node, nm, children_regs);
        data.put(n.id, n);
        IStackTrace st = node.model.getLaunch().getService(IStackTrace.class);
        if (st == null) {
            set(null, null, data);
            return true;
        }
        assert command == null;
        command = st.getChildren(node.id, new IStackTrace.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (command == token && error == null) {
                    int cnt = contexts.length;
                    for (String id : contexts) {
                        TCFNodeStackFrame n = (TCFNodeStackFrame)node.model.getNode(id);
                        if (n == null || n.getFrameNo() != cnt) {
                            if (n != null) n.dispose();
                            n = new TCFNodeStackFrame(node, id, cnt);
                        }
                        assert n.getFrameNo() != 0;
                        assert n.id.equals(id);
                        assert n.parent == node;
                        n.setFrameNo(cnt);
                        data.put(id, n);
                        cnt--;
                    }
                }
                set(token, error, data);
            }
        });
        return false;
    }
}
