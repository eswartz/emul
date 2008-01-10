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
import com.windriver.tcf.api.services.IMemory;
import com.windriver.tcf.api.services.IRunControl;

public class TCFChildrenExecContext extends TCFChildren {
    
    private boolean mem_valid;
    private boolean run_valid;

    TCFChildrenExecContext(TCFNode node) {
        super(node);
    }
    
    @Override
    boolean validate(TCFRunnable done) {
        if (!mem_valid && !validateMemoryChildren(done)) return false;
        if (!run_valid && !validateRunControlChildren(done)) return false;
        doneValidate();
        return true;
    }
    
    @Override
    void invalidate() {
        mem_valid = false;
        run_valid = false;
        super.invalidate();
    }

    private boolean validateMemoryChildren(TCFRunnable done) {
        assert node.data_command == null;
        IMemory mem = node.model.getLaunch().getService(IMemory.class);
        if (mem == null) {
            mem_valid = true;
            return true;
        }
        if (done != null) node.wait_list.add(done);
        node.data_command = mem.getChildren(node.id, new IMemory.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (node.data_command != token) return;
                node.data_command = null;
                if (error != null) {
                    node.node_error = error;
                }
                else {
                    for (int i = 0; i < contexts.length; i++) {
                        String id = contexts[i];
                        TCFNode n = node.model.getNode(id);
                        if (n == null) n = new TCFNodeExecContext(node, id);
                        children_next.put(id, n);
                    }
                }
                mem_valid = true;
                node.validateNode(null);
            }
        });
        return false;
    }

    private boolean validateRunControlChildren(TCFRunnable done) {
        assert node.data_command == null;
        IRunControl run = node.model.getLaunch().getService(IRunControl.class);
        if (run == null) {
            run_valid = true;
            return true;
        }
        if (done != null) node.wait_list.add(done);
        node.data_command = run.getChildren(node.id, new IRunControl.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (node.data_command != token) return;
                node.data_command = null;
                if (error != null) {
                    node.node_error = error;
                }
                else {
                    for (String id : contexts) {
                        TCFNode n = node.model.getNode(id);
                        if (n == null) n = new TCFNodeExecContext(node, id);
                        children_next.put(id, n);
                    }
                }
                run_valid = true;
                node.validateNode(null);
            }
        });
        return false;
    }
}
