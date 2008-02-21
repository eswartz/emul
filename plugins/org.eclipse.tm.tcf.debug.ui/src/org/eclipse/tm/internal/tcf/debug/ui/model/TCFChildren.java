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

public class TCFChildren {

    final TCFNode node;
    final Map<String,TCFNode> children = new HashMap<String,TCFNode>();
    
    protected boolean valid;
    
    TCFChildren(TCFNode node) {
        this.node = node;
    }
    
    void dispose() {
        TCFNode a[] = children.values().toArray(new TCFNode[children.size()]);
        for (int i = 0; i < a.length; i++) a[i].dispose();
        assert children.isEmpty();
    }
    
    void dispose(String id) {
        children.remove(id);
    }
    
    void doneValidate(Map<String,TCFNode> new_children) {
        assert !node.disposed;
        assert !valid;
        valid = true;
        if (children.size() > 0) {
            TCFNode[] a = children.values().toArray(new TCFNode[children.size()]);
            for (TCFNode n : a) if (new_children.get(n.id) != n) n.dispose();
        }
        for (TCFNode n : new_children.values()) {
            assert n.parent == node;
            children.put(n.id, n);
        }
        assert children.size() == new_children.size();
    }
    
    boolean validate() {
        doneValidate(new HashMap<String,TCFNode>());
        return true;
    }
    
    void invalidate() {
        assert !node.disposed;
        TCFNode[] a = children.values().toArray(new TCFNode[children.size()]);
        for (int i = 0; i < a.length; i++) a[i].invalidateNode();
        valid = false;
    }
    
    int size() {
        return children.size();
    }
    
    TCFNode[] toArray() {
        return children.values().toArray(new TCFNode[children.size()]);
    }
}
