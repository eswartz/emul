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

import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.services.IStackTrace;

public class TCFChildrenStackTrace extends TCFChildren {

    private final TCFChildren children_regs;

    TCFChildrenStackTrace(TCFNode node, TCFChildren children_regs) {
        super(node);
        this.children_regs = children_regs;
    }
    
    @Override
    boolean validate(TCFRunnable done) {
        children_next.clear();
        String addr = node.getAddress();
        if (addr == null) {
            doneValidate();
            return true;
        }
        String nm = node.id + "-TF";
        TCFNode n = children.get(nm);
        if (n == null) n = new TCFNodeStackFrame(node, nm, children_regs);
        children_next.put(n.id, n);
        IStackTrace st = node.model.getLaunch().getService(IStackTrace.class);
        if (st == null) {
            doneValidate();
            return true;
        }
        assert node.data_command == null;
        if (done != null) node.wait_list.add(done);
        node.data_command = st.getChildren(node.id, new IStackTrace.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (node.data_command != token) return;
                node.data_command = null;
                if (error != null) {
                    node.node_error = error;
                }
                else {
                    int cnt = contexts.length;
                    for (String id : contexts) {
                        TCFNode n = node.model.getNode(id);
                        if (n == null || ((TCFNodeStackFrame)n).getFrameNo() != cnt) {
                            n = new TCFNodeStackFrame(node, id, cnt);
                        }
                        assert ((TCFNodeStackFrame)n).getFrameNo() == cnt;
                        assert n.id.equals(id);
                        children_next.put(id, n);
                        cnt--;
                    }
                }
                doneValidate();
                node.validateNode(null);
            }
        });
        return false;
    }
}
