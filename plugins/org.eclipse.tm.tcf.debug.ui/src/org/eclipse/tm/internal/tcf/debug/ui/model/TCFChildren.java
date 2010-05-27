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

    private static final TCFNode[] EMPTY_NODE_ARRAY = new TCFNode[0];

    private TCFNode[] array;

    TCFChildren(IChannel channel) {
        super(channel);
        pool_margin = 0;
    }

    TCFChildren(IChannel channel, int pool_margin) {
        super(channel);
        this.pool_margin = pool_margin;
    }

    /**
     * Dispose the cache and all nodes in the nodes pool.
     */
    @Override
    public void dispose() {
        assert !isDisposed();
        if (!node_pool.isEmpty()) {
            TCFNode a[] = node_pool.values().toArray(new TCFNode[node_pool.size()]);
            for (int i = 0; i < a.length; i++) a[i].dispose();
            assert node_pool.isEmpty();
        }
        super.dispose();
    }

    /**
     * Remove a node from cache.
     * The method is called every time a node is disposed.
     * @param id - node ID
     */
    void dispose(String id) {
        node_pool.remove(id);
        if (isValid()) {
            array = null;
            Map<String,TCFNode> data = getData();
            if (data != null) data.remove(id);
        }
    }

    private void addToPool(Map<String,TCFNode> data) {
        assert !isDisposed();
        for (TCFNode n : data.values()) {
            assert data.get(n.id) == n;
            node_pool.put(n.id, n);
        }
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

    /**
     * End cache pending state.
     * @param token - pending command handle.
     * @param error - data retrieval error or null
     * @param data - up-to-date map of children nodes
     */
    @Override
    public void set(IToken token, Throwable error, Map<String,TCFNode> data) {
        array = null;
        if (isDisposed()) {
            // A command can return data after the cache element has been disposed.
            // Just ignore the data in such case.
            super.set(token, null, null);
            assert node_pool.isEmpty();
        }
        else if (data != null) {
            super.set(token, error, data);
            addToPool(data);
        }
        else {
            super.set(token, error, new HashMap<String,TCFNode>());
        }
    }

    /**
     * Set given data to the cache, mark cache as valid, cancel any pending data retrieval.
     * @param data - up-to-date data to store in the cache, null means empty collection of nodes.
     */
    @Override
    public void reset(Map<String,TCFNode> data) {
        assert !isDisposed();
        array = null;
        if (data != null) {
            super.reset(data);
            addToPool(data);
        }
        else {
            super.reset(new HashMap<String,TCFNode>());
        }
    }

    /**
     * Invalidate the cache. If retrieval is in progress - let it continue.
     */
    @Override
    public void reset() {
        super.reset();
        array = null;
    }

    /**
     * Force cache to invalid state, cancel pending data retrieval if any.
     */
    @Override
    public void cancel() {
        super.cancel();
        array = null;
    }

    /**
     * Add a node to collection of children.
     * @param n - a node.
     */
    void add(TCFNode n) {
        assert !isDisposed();
        assert node_pool.get(n.id) == null;
        node_pool.put(n.id, n);
        if (isValid()) {
            array = null;
            Map<String,TCFNode> data = getData();
            if (data != null) data.put(n.id, n);
        }
    }

    /**
     * Return collection of all nodes, including current children as well as
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
        Map<String,TCFNode> data = getData();
        return data == null ? 0 : data.size();
    }

    /**
     * Return current children nodes as an array.
     * @return array of nodes.
     */
    TCFNode[] toArray() {
        assert isValid();
        if (array != null) return array;
        Map<String,TCFNode> data = getData();
        if (data == null) return array = EMPTY_NODE_ARRAY;
        array = data.values().toArray(new TCFNode[data.size()]);
        Arrays.sort(array);
        return array;
    }
}
