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

import java.util.HashMap;
import java.util.Map;

import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.services.IStackTrace;

public class TCFChildrenStackTrace extends TCFChildren {

    private final TCFChildrenRegisters children_regs;

    private final Map<String,TCFNodeStackFrame> frames_cache =
        new HashMap<String,TCFNodeStackFrame>();

    TCFChildrenStackTrace(TCFNode node, TCFChildrenRegisters children_regs) {
        super(node);
        this.children_regs = children_regs;
    }
    
    @Override
    void dispose() {
        TCFNode arr[] = frames_cache.values().toArray(new TCFNode[frames_cache.size()]);
        for (int i = 0; i < arr.length; i++) arr[i].dispose();
        assert frames_cache.isEmpty();
        assert children.isEmpty();
    }

    @Override
    void dispose(String id) {
        super.dispose(id);
        frames_cache.remove(id);
    }

    void onSourceMappingChange() {
        for (TCFNodeStackFrame n : frames_cache.values()) n.onSourceMappingChange();
    }

    void onSuspended() {
        for (TCFNodeStackFrame n : frames_cache.values()) n.onSuspended();
        valid = false;
    }

    void onResumed() {
        valid = false;
    }

    @Override
    boolean validate() {
        final Map<String,TCFNode> new_children = new HashMap<String,TCFNode>();
        if (!node.isSuspended()) {
            doneValidate(new_children);
            return true;
        }
        String nm = node.id + "-TF";
        TCFNodeStackFrame n = frames_cache.get(nm);
        if (n == null) n = (TCFNodeStackFrame)node.model.getNode(nm);
        if (n == null) n = new TCFNodeStackFrame(node, nm, children_regs);
        new_children.put(n.id, n);
        frames_cache.put(n.id, n);
        IStackTrace st = node.model.getLaunch().getService(IStackTrace.class);
        if (st == null) {
            doneValidate(new_children);
            return true;
        }
        assert node.pending_command == null;
        node.pending_command = st.getChildren(node.id, new IStackTrace.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (node.pending_command != token) return;
                node.pending_command = null;
                if (error != null) {
                    node.node_error = error;
                }
                else {
                    int cnt = contexts.length;
                    for (String id : contexts) {
                        TCFNodeStackFrame n = frames_cache.get(id);
                        if (n == null) n = (TCFNodeStackFrame)node.model.getNode(id);
                        if (n == null || n.getFrameNo() != cnt) {
                            if (n != null) n.dispose();
                            n = new TCFNodeStackFrame(node, id, cnt);
                        }
                        assert n.getFrameNo() == cnt;
                        assert n.id.equals(id);
                        assert n.parent == node;
                        new_children.put(id, n);
                        frames_cache.put(id, n);
                        cnt--;
                    }
                    if (frames_cache.size() > new_children.size() + 32) {
                        // Trim frame cache
                        TCFNode arr[] = frames_cache.values().toArray(new TCFNode[frames_cache.size()]);
                        for (int i = 0; i < arr.length; i++) {
                            if (new_children.get(arr[i].id) == null) arr[i].dispose();
                        }
                    }
                }
                doneValidate(new_children);
                node.validateNode();
            }
        });
        return false;
    }
}
