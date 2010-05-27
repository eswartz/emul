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

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRegisters;


public class TCFChildrenRegisters extends TCFChildren {

    private final TCFNodeStackFrame node;

    TCFChildrenRegisters(TCFNodeStackFrame node) {
        super(node.channel, 128);
        this.node = node;
    }

    void onSuspended() {
        for (TCFNode n : getNodes()) ((TCFNodeRegister)n).onSuspended();
        reset();
    }

    void onRegistersChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeRegister)n).onRegistersChanged();
        reset();
    }

    @Override
    protected boolean startDataRetrieval() {
        TCFChildrenStackTrace stack_trace_cache = ((TCFNodeExecContext)node.parent).getStackTrace();
        if (!stack_trace_cache.validate(this)) return false; // node.getFrameNo() is not valid
        IRegisters regs = node.model.getLaunch().getService(IRegisters.class);
        final int frame_no = node.getFrameNo();
        if (regs == null || frame_no < 0) {
            set(null, null, new HashMap<String,TCFNode>());
            return true;
        }
        assert command == null;
        final TCFNode parent = frame_no == 0 ? node.parent : node;
        command = regs.getChildren(parent.id, new IRegisters.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                Map<String,TCFNode> data = null;
                if (command == token && error == null) {
                    int index = 0;
                    data = new HashMap<String,TCFNode>();
                    for (String id : contexts) {
                        TCFNode n = node.model.getNode(id);
                        if (n == null) n = new TCFNodeRegister(parent, id);
                        assert n.parent == parent;
                        ((TCFNodeRegister)n).setIndex(index++);
                        data.put(id, n);
                    }
                }
                set(token, error, data);
            }
        });
        return false;
    }
}
