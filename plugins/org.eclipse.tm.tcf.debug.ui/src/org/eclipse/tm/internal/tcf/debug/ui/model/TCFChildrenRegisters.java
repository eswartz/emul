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

    private final TCFNode node;

    TCFChildrenRegisters(TCFNode node) {
        super(node.model.getLaunch().getChannel(), 32);
        this.node = node;
    }

    /**
     * Invalidate register values only, keep cached register attributes.
     */
    void onSuspended() {
        for (TCFNode n : getNodes()) ((TCFNodeRegister)n).onSuspended();
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
        command = regs.getChildren(node.id, new IRegisters.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                Map<String,TCFNode> data = null;
                if (command == token && error == null) {
                    data = new HashMap<String,TCFNode>();
                    for (String id : contexts) {
                        TCFNode n = node.model.getNode(id);
                        if (n == null) n = new TCFNodeRegister(node, id);
                        data.put(id, n);
                    }
                }
                set(token, error, data);
            }
        });
        return false;
    }
}
