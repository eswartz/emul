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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.services.IMemory;
import com.windriver.tcf.api.services.IRunControl;

/**
 * This class is used to maintain a dynamic list of both executable contexts and memory spaces
 * that are children of a given parent context. The job is slightly complicated by necessity
 * to merge results from two independent services. 
 */
@SuppressWarnings("serial")
public class TCFChildrenExecContext extends TCFChildren {
    
    private Map<String,TCFNode> mem_children;
    private Map<String,TCFNode> run_children;
    
    // Track disposed IDs to detect violations of the communication protocol
    private LinkedHashMap<String,String> disposed_ids = new LinkedHashMap<String,String>() {
        protected boolean removeEldestEntry(Map.Entry<String,String> eldest) {
            return size() > 128;
        }
    };

    TCFChildrenExecContext(TCFNode node) {
        super(node);
    }
    
    @Override
    void dispose() {
        HashSet<TCFNode> s = new HashSet<TCFNode>();
        s.addAll(children.values());
        if (mem_children != null) s.addAll(mem_children.values());
        if (run_children != null) s.addAll(run_children.values());
        for (TCFNode n : s) n.dispose();
        mem_children = null;
        run_children = null;
    }

    @Override
    void dispose(String id) {
        super.dispose(id);
        if (mem_children != null) mem_children.remove(id);
        if (run_children != null) run_children.remove(id);
        disposed_ids.put(id, id);
    }
    
    @Override
    boolean validate() {
        assert !node.disposed;
        Map<String,TCFNode> new_children = new HashMap<String,TCFNode>();
        if (!validateMemoryChildren(new_children)) return false;
        if (!validateRunControlChildren(new_children)) return false;
        doneValidate(new_children);
        return true;
    }
    
    @Override
    void invalidate() {
        HashSet<TCFNode> s = new HashSet<TCFNode>();
        s.addAll(children.values());
        if (mem_children != null) s.addAll(mem_children.values());
        if (run_children != null) s.addAll(run_children.values());
        for (TCFNode n : s) n.invalidateNode();
        mem_children = null;
        run_children = null;
        valid = false;
    }

    void onContextAdded(IRunControl.RunControlContext context) {
        assert !node.disposed;
        if (run_children != null) {
            String id = context.getID();
            TCFNodeExecContext n = (TCFNodeExecContext)node.model.getNode(id);
            if (n == null) {
                n = new TCFNodeExecContext(node, id);
                n.setRunContext(context);
                n.makeModelDelta(IModelDelta.INSERTED);
            }
            else {
                n.setRunContext(context);
                n.makeModelDelta(IModelDelta.STATE);
            }
            children.put(id, n);
            run_children.put(id, n);
        }
        else { 
            node.invalidateNode();
            node.makeModelDelta(IModelDelta.CONTENT);
        }
    }

    void onContextAdded(IMemory.MemoryContext context) {
        assert !node.disposed;
        if (mem_children != null) {
            String id = context.getID();
            TCFNodeExecContext n = (TCFNodeExecContext)node.model.getNode(id);
            if (n == null) {
                n = new TCFNodeExecContext(node, id);
                n.setMemoryContext(context);
                n.makeModelDelta(IModelDelta.INSERTED);
            }
            else {
                n.setMemoryContext(context);
                n.makeModelDelta(IModelDelta.STATE);
            }
            children.put(id, n);
            mem_children.put(id, n);
        }
        else { 
            node.invalidateNode();
            node.makeModelDelta(IModelDelta.CONTENT);
        }
    }

    private boolean validateMemoryChildren(final Map<String,TCFNode> new_children) {
        if (mem_children != null) {
            new_children.putAll(mem_children);
            return true;
        }
        IMemory mem = node.model.getLaunch().getService(IMemory.class);
        if (mem == null) {
            mem_children = new HashMap<String,TCFNode>();
            return true;
        }
        assert node.pending_command == null;
        node.pending_command = mem.getChildren(node.id, new IMemory.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (node.pending_command != token) return;
                node.pending_command = null;
                mem_children = new_children;
                mem_children.clear();
                if (error != null) {
                    node.node_error = error;
                }
                else {
                    for (String id : contexts) {
                        assert disposed_ids.get(id) == null;
                        TCFNode n = node.model.getNode(id);
                        if (n == null) n = new TCFNodeExecContext(node, id);
                        mem_children.put(id, n);
                    }
                }
                node.validateNode();
            }
        });
        return false;
    }

    private boolean validateRunControlChildren(final Map<String,TCFNode> new_children) {
        if (run_children != null) {
            new_children.putAll(run_children);
            return true;
        }
        IRunControl run = node.model.getLaunch().getService(IRunControl.class);
        if (run == null) {
            run_children = new HashMap<String,TCFNode>();
            return true;
        }
        assert node.pending_command == null;
        node.pending_command = run.getChildren(node.id, new IRunControl.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (node.pending_command != token) return;
                node.pending_command = null;
                run_children = new_children;
                run_children.clear();
                if (error != null) {
                    node.node_error = error;
                }
                else {
                    for (String id : contexts) {
                        assert disposed_ids.get(id) == null;
                        TCFNode n = node.model.getNode(id);
                        if (n == null) n = new TCFNodeExecContext(node, id);
                        run_children.put(id, n);
                    }
                }
                node.validateNode();
            }
        });
        return false;
    }
}
