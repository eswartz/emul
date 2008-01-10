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

public class TCFChildren {

    final TCFNode node;
    final Map<String,TCFNode> children = new HashMap<String,TCFNode>();
    final Map<String,TCFNode> children_next = new HashMap<String,TCFNode>();
    
    protected boolean valid;
    
    TCFChildren(TCFNode node) {
        this.node = node;
    }
    
    void dispose() {
        TCFNode arr[] = children.values().toArray(new TCFNode[children.size()]);
        for (int i = 0; i < arr.length; i++) arr[i].dispose();
        assert children.isEmpty();
    }
    
    void dispose(String id) {
        children.remove(id);
    }
    
    void doneValidate() {
        valid = true;
        TCFNode[] a = children.values().toArray(new TCFNode[children.size()]);
        for (TCFNode n : a) {
            if (children_next.get(n.id) != n) n.dispose();
        }
        for (TCFNode n : children_next.values()) {
            if (children.get(n.id) == null) {
                children.put(n.id, n);
                n.model.addNode(n.id, n);
            }
            assert children.get(n.id) == n;
        }
        assert children.size() == children_next.size();
    }
    
    boolean validate(TCFRunnable done) {
        doneValidate();
        return true;
    }
    
    void invalidate() {
        children_next.clear();
        TCFNode[] a = children.values().toArray(new TCFNode[children.size()]);
        for (int i = 0; i < a.length; i++) a[i].invalidateNode(TCFNode.CF_ALL);
        valid = false;
    }
    
    int size() {
        return children.size();
    }
    
    TCFNode[] toArray() {
        return children.values().toArray(new TCFNode[children.size()]);
    }
}
