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
import com.windriver.tcf.api.services.IRegisters;

public class TCFChildrenRegisters extends TCFChildren {

    TCFChildrenRegisters(TCFNode node) {
        super(node);
    }

    @Override
    boolean validate(TCFRunnable done) {
        children_next.clear();
        String addr = node.getAddress();
        if (addr == null) {
            doneValidate();
            return true;
        }
        IRegisters regs = node.model.getLaunch().getService(IRegisters.class);
        if (regs == null) {
            doneValidate();
            return true;
        }
        assert node.data_command == null;
        if (done != null) node.wait_list.add(done);
        node.data_command = regs.getChildren(node.id, new IRegisters.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (node.data_command != token) return;
                node.data_command = null;
                if (error != null) {
                    node.node_error = error;
                }
                else {
                    for (String id : contexts) {
                        TCFNode n = node.model.getNode(id);
                        if (n == null) n = new TCFNodeRegister(node, id);
                        children_next.put(id, n);
                    }
                }
                doneValidate();
                node.validateNode(null);
            }
        });
        return false;
    }
}
