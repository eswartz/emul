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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.util.TCFDataCache;


public class TCFChildrenStackTrace extends TCFChildren {

    private final TCFNodeExecContext node;

    private String top_frame_id;

    TCFChildrenStackTrace(TCFNodeExecContext node) {
        super(node.channel, 16);
        this.node = node;
    }

    void dispose(String id) {
        super.dispose(id);
        // Register nodes are special case:
        // they have executable node as parent,
        // but they are referenced as children of stack frame
        for (TCFNode n : getNodes()) n.dispose(id);
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

    void onMemoryMapChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onMemoryMapChanged();
    }

    void onRegisterValueChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onRegisterValueChanged();
    }

    void onResumed() {
        reset(null);
    }

    public TCFNodeStackFrame getTopFrame() {
        assert isValid();
        return (TCFNodeStackFrame)node.model.getNode(top_frame_id);
    }

    @Override
    public void set(IToken token, Throwable error, Map<String,TCFNode> data) {
        for (TCFNode n : getNodes()) {
            if (data == null || data.get(n.id) == null) ((TCFNodeStackFrame)n).setFrameNo(-1);
        }
        super.set(token, error, data);
    }

    private void addEmulatedTopFrame(HashMap<String,TCFNode> data) {
        top_frame_id = node.id + "-TF";
        TCFNodeStackFrame n = (TCFNodeStackFrame)node.model.getNode(top_frame_id);
        if (n == null) n = new TCFNodeStackFrame(node, top_frame_id, true);
        data.put(n.id, n);
    }

    @Override
    protected boolean startDataRetrieval() {
        final HashMap<String,TCFNode> data = new HashMap<String,TCFNode>();
        TCFDataCache<TCFContextState> state = node.getState();
        if (!state.validate(this)) return false;
        Throwable state_error = state.getError();
        TCFContextState state_data = state.getData();
        if (state_error != null || state_data == null || !state_data.is_suspended) {
            set(null, state_error, data);
            return true;
        }
        IStackTrace st = node.model.getLaunch().getService(IStackTrace.class);
        if (st == null) {
            addEmulatedTopFrame(data);
            set(null, null, data);
            return true;
        }
        assert command == null;
        command = st.getChildren(node.id, new IStackTrace.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (command == token) {
                    if (error == null && contexts != null) {
                        int cnt = contexts.length;
                        for (String id : contexts) {
                            cnt--;
                            TCFNodeStackFrame n = (TCFNodeStackFrame)node.model.getNode(id);
                            if (n == null) n = new TCFNodeStackFrame(node, id, false);
                            assert n.parent == node;
                            n.setFrameNo(cnt);
                            data.put(id, n);
                            if (cnt == 0) top_frame_id = id;
                        }
                    }
                    if (data.size() == 0) addEmulatedTopFrame(data);
                }
                set(token, error, data);
            }
        });
        return false;
    }
}
