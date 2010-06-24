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

    private final TCFNode node;

    TCFChildrenRegisters(TCFNode node) {
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
        IRegisters regs = node.model.getLaunch().getService(IRegisters.class);
        if (regs == null) {
            set(null, null, new HashMap<String,TCFNode>());
            return true;
        }
        if (node instanceof TCFNodeStackFrame) {
            TCFChildrenStackTrace stack_trace_cache = ((TCFNodeExecContext)node.parent).getStackTrace();
            if (!stack_trace_cache.validate(this)) return false; // node.getFrameNo() is not valid
            final int frame_no = ((TCFNodeStackFrame)node).getFrameNo();
            if (frame_no < 0) {
                set(null, null, new HashMap<String,TCFNode>());
                return true;
            }
        }
        assert command == null;
        command = regs.getChildren(node.id, new IRegisters.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                Map<String,TCFNode> data = null;
                if (command == token && error == null) {
                    int index = 0;
                    data = new HashMap<String,TCFNode>();
                    for (String id : contexts) {
                        TCFNodeRegister n = (TCFNodeRegister)node.model.getNode(id);
                        if (n == null) n = new TCFNodeRegister(node, id);
                        n.setIndex(index++);
                        data.put(id, n);
                    }
                }
                set(token, error, data);
            }
        });
        return false;
    }
}
