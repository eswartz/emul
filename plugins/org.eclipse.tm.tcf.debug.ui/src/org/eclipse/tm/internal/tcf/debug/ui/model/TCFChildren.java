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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.util.TCFDataCache;

/**
 * TCFChildren is a concrete type of TCF data cache that is used to cache a list of children. 
 */
public abstract class TCFChildren extends TCFDataCache<Map<String,TCFNode>> {
    
    private final int pool_margin;
    private final Map<String,TCFNode> node_pool = new LinkedHashMap<String,TCFNode>(32, 0.75f, true);

    TCFChildren(IChannel channel) {
        super(channel);
        pool_margin = 0;
    }
    
    TCFChildren(IChannel channel, int pool_margin) {
        super(channel);
        this.pool_margin = pool_margin;
    }
    
    void dispose() {
        if (node_pool.isEmpty()) return;
        TCFNode a[] = node_pool.values().toArray(new TCFNode[node_pool.size()]);
        for (int i = 0; i < a.length; i++) a[i].dispose();
        assert node_pool.isEmpty();
    }
    
    void dispose(String id) {
        node_pool.remove(id);
        if (isValid()) {
            Map<String,TCFNode> map = getData();
            if (map != null) map.remove(id);
        }
    }
    
    private void flush(Map<String,TCFNode> data) {
        node_pool.putAll(data);
        if (node_pool.size() > data.size() + pool_margin) {
            String[] arr = node_pool.keySet().toArray(new String[node_pool.size()]);
            for (String id : arr) {
                if (data.get(id) == null) {
                    node_pool.get(id).dispose();
                    if (node_pool.size() <= data.size() + pool_margin) break;
                }
            }
        }
    }
    
    public void set(IToken token, Throwable error, Map<String,TCFNode> data) {
        if (data != null) {
            super.set(token, error, data);
            flush(data);
        }
        else {
            super.set(token, error, new HashMap<String,TCFNode>());
        }
    }
    
    /**
     * Set given data to the cache, mark cache as valid, cancel any pending data retrieval.
     * @param data - up-to-date data to store in the cache, null means empty collection of nodes.
     */
    public void reset(Map<String,TCFNode> data) {
        if (data != null) {
            super.reset(data);
            flush(data);
        }
        else {
            super.reset(new HashMap<String,TCFNode>());
        }
    }
    
    /**
     * Add a node to collection of children.
     * @param n - a node.
     */
    void add(TCFNode n) {
        node_pool.put(n.id, n);
        if (isValid()) getData().put(n.id, n);
    }
    
    /** Return collection of all nodes, including current children as well as
     * currently unused nodes from the pool.
     * To get only current children use getData() method.
     * @return Collection of nodes.
     */
    Collection<TCFNode> getNodes() {
        return node_pool.values();
    }
    
    /**
     * Return current number of children.
     * The cache must be valid for the method to work.
     * @return number of children.
     */
    int size() {
        assert isValid();
        return getData().size();
    }
    
    /**
     * Return index of given child node.
     * @param n - a child node
     * @return - node index or -1 if node is not found in children list
     */
    int getIndexOf(TCFNode n) {
        TCFNode[] arr = toArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == n) return i;
        }
        return -1;
    }
    
    /**
     * Return current children nodes as an array.
     * @return array of nodes.
     */
    TCFNode[] toArray() {
        assert isValid();
        Map<String,TCFNode> data = getData();
        TCFNode[] arr = data.values().toArray(new TCFNode[data.size()]);
        Arrays.sort(arr);
        return arr;
    }
}
