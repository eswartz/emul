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
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IStackTrace;


public class TCFChildrenStackTrace extends TCFChildren {

    private final TCFNodeExecContext node;
    
    private String top_frame_id;

    TCFChildrenStackTrace(TCFNodeExecContext node) {
        super(node.model.getLaunch().getChannel(), 16);
        this.node = node;
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
    
    TCFNodeStackFrame getTopFrame() {
        return (TCFNodeStackFrame)node.model.getNode(top_frame_id);
    }

    @Override
    public void set(IToken token, Throwable error, Map<String,TCFNode> data) {
        for (TCFNode n : getNodes()) {
            if (data == null || data.get(n.id) == null) ((TCFNodeStackFrame)n).setFrameNo(-1);
        }
        super.set(token, error, data);
    }
    
    @Override
    protected boolean startDataRetrieval() {
        final HashMap<String,TCFNode> data = new HashMap<String,TCFNode>();
        if (!node.isSuspended()) {
            set(null, null, data);
            return true;
        }
        IStackTrace st = node.model.getLaunch().getService(IStackTrace.class);
        if (st == null) {
            top_frame_id = node.id + "-TF";
            TCFNodeStackFrame n = (TCFNodeStackFrame)node.model.getNode(top_frame_id);
            if (n == null) n = new TCFNodeStackFrame(node, top_frame_id);
            data.put(n.id, n);
            set(null, null, data);
            return true;
        }
        assert command == null;
        command = st.getChildren(node.id, new IStackTrace.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (command == token && error == null) {
                    int cnt = contexts.length;
                    for (String id : contexts) {
                        cnt--;
                        TCFNodeStackFrame n = (TCFNodeStackFrame)node.model.getNode(id);
                        if (n == null) n = new TCFNodeStackFrame(node, id);
                        assert n.id.equals(id);
                        assert n.parent == node;
                        n.setFrameNo(cnt);
                        data.put(id, n);
                        if (cnt == 0) top_frame_id = id;
                    }
                }
                set(token, error, data);
            }
        });
        return false;
    }
}
