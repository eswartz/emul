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
import com.windriver.tcf.api.services.IRegisters;

public class TCFChildrenRegisters extends TCFChildren {

    private boolean running;

    TCFChildrenRegisters(TCFNode node) {
        super(node);
    }

    /**
     * Invalidate register values only, keep cached register attributes.
     */
    void onSuspended() {
        if (running || node.node_error != null) invalidate();
        for (TCFNode n : children.values()) ((TCFNodeRegister)n).onSuspended();
    }

    @Override
    boolean validate() {
        assert !node.disposed;
        assert !valid;
        final Map<String,TCFNode> new_children = new HashMap<String,TCFNode>();
        running = !node.isSuspended(); 
        if (running) {
            valid = true;
            return true;
        }
        IRegisters regs = node.model.getLaunch().getService(IRegisters.class);
        if (regs == null) {
            doneValidate(new_children);
            return true;
        }
        assert node.pending_command == null;
        node.pending_command = regs.getChildren(node.id, new IRegisters.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (node.pending_command != token) return;
                node.pending_command = null;
                if (error != null) {
                    node.node_error = error;
                }
                else {
                    for (String id : contexts) {
                        TCFNode n = node.model.getNode(id);
                        if (n == null) n = new TCFNodeRegister(node, id);
                        new_children.put(id, n);
                    }
                }
                doneValidate(new_children);
                node.validateNode();
            }
        });
        return false;
    }
}
