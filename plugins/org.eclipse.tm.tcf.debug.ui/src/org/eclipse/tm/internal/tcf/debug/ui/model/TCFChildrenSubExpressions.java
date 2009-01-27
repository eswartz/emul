/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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
import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.util.TCFDataCache;

public class TCFChildrenSubExpressions extends TCFChildren {
    
    private final TCFNode node;
    private final int par_level;
    private final int par_offs;
    private final int par_size;

    TCFChildrenSubExpressions(TCFNode node, int par_level, int par_offs, int par_size) {
        super(node.model.getLaunch().getChannel(), 64);
        this.node = node;
        this.par_level = par_level;
        this.par_offs = par_offs;
        this.par_size = par_size;
    }

    void onSuspended() {
        for (TCFNode n : getNodes()) {
            if (n instanceof TCFNodeExpression) ((TCFNodeExpression)n).onSuspended();
        }
    }
    
    private TCFNodeExpression findField(String id) {
        assert id != null;
        for (TCFNode n : getNodes()) {
            TCFNodeExpression e = (TCFNodeExpression)n;
            if (id.equals(e.getFieldID())) return e;
        }
        return null;
    }
    
    private TCFNodeExpression findIndex(int index) {
        assert index >= 0;
        for (TCFNode n : getNodes()) {
            TCFNodeExpression e = (TCFNodeExpression)n;
            if (e.getIndex() == index) return e;
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
        final TCFDataCache<ISymbols.Symbol> type = ((TCFNodeExpression)exp).getType();
        if (!type.validate()) {
            type.wait(this);
            return false;
        }
        final ISymbols syms = node.model.getLaunch().getService(ISymbols.class);
        final ISymbols.Symbol type_sym = type.getData();
        if (syms == null || type_sym == null) {
            set(null, null, new HashMap<String,TCFNode>());
            return true;
        }
        ISymbols.TypeClass type_class = type_sym.getTypeClass();
        if (par_level > 0 && type_class != ISymbols.TypeClass.array) {
            set(null, null, new HashMap<String,TCFNode>());
            return true;
        }
        if (type_class == ISymbols.TypeClass.composite) {
            command = syms.getChildren(type_sym.getID(), new ISymbols.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                    Map<String,TCFNode> data = null;
                    if (command == token && error == null) {
                        int cnt = 0;
                        data = new HashMap<String,TCFNode>();
                        for (String id : contexts) {
                            TCFNodeExpression n = findField(id);
                            if (n == null) n = new TCFNodeExpression(node, null, id, null, -1);
                            n.setSortPosition(cnt++);
                            data.put(n.id, n);
                        }
                    }
                    set(token, error, data);
                }
            });
            return false;
        }
        if (type_class == ISymbols.TypeClass.array) {
            Map<String,TCFNode> data = new HashMap<String,TCFNode>();
            int offs = par_level > 0 ? par_offs : 0;
            int size = par_level > 0 ? par_size : type_sym.getLength();
            if (size <= 100) {
                for (int i = offs; i < offs + size; i++) {
                    TCFNodeExpression n = findIndex(i);
                    if (n == null) n = new TCFNodeExpression(node, null, null, null, i);
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
            if (!value.validate()) {
                value.wait(this);
                return false;
            }
            IExpressions.Value v = value.getData();
            if (v != null && !isNull(v.getValue())) {
                TCFDataCache<ISymbols.Symbol> base_type = node.model.getSymbolInfoCache(
                        type_sym.getExeContextID(), type_sym.getBaseTypeID());
                if (!base_type.validate()) {
                    base_type.wait(this);
                    return false;
                }
                ISymbols.Symbol base_type_sym = base_type.getData();
                if (base_type_sym == null || base_type_sym.getSize() != 0) {
                    TCFNodeExpression n = findIndex(0);
                    if (n == null) n = new TCFNodeExpression(node, null, null, null, 0);
                    n.setSortPosition(0);
                    data.put(n.id, n);
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
