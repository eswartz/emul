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
import org.eclipse.tm.tcf.services.IRegisters;


public class TCFChildrenRegisters extends TCFChildren {

    private final TCFNodeStackFrame node;

    TCFChildrenRegisters(TCFNodeStackFrame node) {
        super(node.model.getLaunch().getChannel(), 32);
        this.node = node;
    }

    void onSuspended() {
        for (TCFNode n : getNodes()) ((TCFNodeRegister)n).onSuspended();
        reset();
    }

    void onRegistersChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeRegister)n).onRegistersChanged();
    }

    @Override
    protected boolean startDataRetrieval() {
        IRegisters regs = node.model.getLaunch().getService(IRegisters.class);
        if (regs == null) {
            set(null, null, new HashMap<String,TCFNode>());
            return true;
        }
        assert command == null;
        String id = node.getFrameNo() == 0 ? node.parent.id : node.id;
        command = regs.getChildren(id, new IRegisters.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                Map<String,TCFNode> data = null;
                if (command == token && error == null) {
                    data = new HashMap<String,TCFNode>();
                    for (String id : contexts) {
                        TCFNode n = node.model.getNode(id);
                        if (n != null && n.parent != node) {
                            n.dispose();
                            n = null;
                        }
                        if (n == null) n = new TCFNodeRegister(node, id);
                        assert n.parent == node;
                        data.put(id, n);
                    }
                }
                set(token, error, data);
            }
        });
        return false;
    }
}
