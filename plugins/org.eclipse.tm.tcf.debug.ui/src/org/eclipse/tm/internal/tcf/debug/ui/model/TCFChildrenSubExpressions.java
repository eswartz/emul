/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
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

import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.util.TCFDataCache;

public class TCFChildrenSubExpressions extends TCFChildren {

    private final TCFNode node;
    private final int par_level;
    private final int par_offs;
    private final int par_size;

    TCFChildrenSubExpressions(TCFNode node, int par_level, int par_offs, int par_size) {
        super(node.channel, 128);
        this.node = node;
        this.par_level = par_level;
        this.par_offs = par_offs;
        this.par_size = par_size;
    }

    void onSuspended() {
        reset();
        for (TCFNode n : getNodes()) {
            if (n instanceof TCFNodeExpression) ((TCFNodeExpression)n).onSuspended();
        }
    }

    void onCastToTypeChanged() {
        cancel();
        TCFNode a[] = getNodes().toArray(new TCFNode[getNodes().size()]);
        for (int i = 0; i < a.length; i++) a[i].dispose();
    }

    private TCFNodeExpression findField(TCFDataCache<ISymbols.Symbol> field, boolean deref) {
        assert field != null;
        for (TCFNode n : getNodes()) {
            TCFNodeExpression e = (TCFNodeExpression)n;
            if (field == e.getField() && e.isDeref() == deref) return e;
        }
        return null;
    }

    private HashMap<String,TCFNode> findFields(ISymbols.Symbol type, String[] children, boolean deref) {
        int cnt = 0;
        HashMap<String,TCFNode> data = new HashMap<String,TCFNode>();
        for (String id : children) {
            TCFDataCache<ISymbols.Symbol> field = node.getModel().getSymbolInfoCache(id);
            TCFNodeExpression n = findField(field, deref);
            if (n == null) n = new TCFNodeExpression(node, null, field, null, -1, deref);
            n.setSortPosition(cnt++);
            data.put(n.id, n);
        }
        return data;
    }

    private TCFNodeExpression findIndex(int index, boolean deref) {
        assert index >= 0;
        for (TCFNode n : getNodes()) {
            TCFNodeExpression e = (TCFNodeExpression)n;
            if (e.getIndex() == index && e.isDeref() == deref) return e;
        }
        return null;
    }

    private TCFNodeArrayPartition findPartition(int offs, int size) {
        assert offs >= 0;
        for (TCFNode n : getNodes()) {
            TCFNodeArrayPartition e = (TCFNodeArrayPartition)n;
            if (e.getOffset() == offs && e.getSize() == size) return e;
        }
        return null;
    }

    @Override
    protected boolean startDataRetrieval() {
        assert !isDisposed();
        TCFNode exp = node;
        while (exp != null) {
            if (exp instanceof TCFNodeExpression) break;
            exp = exp.parent;
        }
        TCFDataCache<ISymbols.Symbol> type_cache = ((TCFNodeExpression)exp).getType();
        if (!type_cache.validate(this)) return false;
        ISymbols.Symbol type_data = type_cache.getData();
        if (type_data == null) {
            set(null, null, new HashMap<String,TCFNode>());
            return true;
        }
        ISymbols.TypeClass type_class = type_data.getTypeClass();
        if (par_level > 0 && type_class != ISymbols.TypeClass.array) {
            set(null, null, new HashMap<String,TCFNode>());
            return true;
        }
        if (type_class == ISymbols.TypeClass.composite) {
            TCFDataCache<String[]> children_cache = node.model.getSymbolChildrenCache(type_data.getID());
            if (children_cache == null) {
                set(null, null, new HashMap<String,TCFNode>());
                return true;
            }
            if (!children_cache.validate(this)) return false;
            String[] children_data = children_cache.getData();
            Map<String,TCFNode> data = null;
            if (children_data != null) data = findFields(type_data, children_data, false);
            set(null, children_cache.getError(), data);
            return true;
        }
        if (type_class == ISymbols.TypeClass.array) {
            Map<String,TCFNode> data = new HashMap<String,TCFNode>();
            int offs = par_level > 0 ? par_offs : 0;
            int size = par_level > 0 ? par_size : type_data.getLength();
            if (size <= 100) {
                for (int i = offs; i < offs + size; i++) {
                    TCFNodeExpression n = findIndex(i, false);
                    if (n == null) n = new TCFNodeExpression(node, null, null, null, i, false);
                    n.setSortPosition(i);
                    data.put(n.id, n);
                }
            }
            else {
                int next_size = 100;
                while (size / next_size > 100) next_size *= 100;
                for (int i = offs; i < offs + size; i += next_size) {
                    int sz = next_size;
                    if (i + sz > offs + size) sz = offs + size - i;
                    TCFNodeArrayPartition n = findPartition(i, sz);
                    if (n == null) n = new TCFNodeArrayPartition(node, par_level + 1, i, sz);
                    data.put(n.id, n);
                }
            }
            set(null, null, data);
            return true;
        }
        if (type_class == ISymbols.TypeClass.pointer) {
            Map<String,TCFNode> data = new HashMap<String,TCFNode>();
            TCFDataCache<IExpressions.Value> value = ((TCFNodeExpression)exp).getValue();
            if (!value.validate(this)) return false;
            IExpressions.Value v = value.getData();
            if (v != null && !isNull(v.getValue())) {
                TCFDataCache<ISymbols.Symbol> base_type_cache = node.model.getSymbolInfoCache(type_data.getBaseTypeID());
                if (base_type_cache != null) {
                    if (!base_type_cache.validate(this)) return false;
                    ISymbols.Symbol base_type_data = base_type_cache.getData();
                    if (base_type_data == null || base_type_data.getSize() != 0) {
                        if (base_type_data.getTypeClass() == ISymbols.TypeClass.composite) {
                            TCFDataCache<String[]> children_cache = node.model.getSymbolChildrenCache(base_type_data.getID());
                            if (children_cache != null) {
                                if (!children_cache.validate(this)) return false;
                                String[] children_data = children_cache.getData();
                                if (children_data != null) data = findFields(type_data, children_data, true);
                            }
                        }
                        else {
                            TCFNodeExpression n = findIndex(0, true);
                            if (n == null) n = new TCFNodeExpression(node, null, null, null, 0, true);
                            n.setSortPosition(0);
                            data.put(n.id, n);
                        }
                    }
                }
            }
            set(null, null, data);
            return true;
        }
        set(null, null, new HashMap<String,TCFNode>());
        return true;
    }

    private boolean isNull(byte[] data) {
        if (data == null) return true;
        for (byte b : data) {
            if (b != 0) return false;
        }
        return true;
    }
}
