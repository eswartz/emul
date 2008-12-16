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
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.util.TCFDataCache;

public class TCFChildrenSubExpressions extends TCFChildren {
    
    private final TCFNodeExpression node;

    TCFChildrenSubExpressions(final TCFNodeExpression node) {
        super(node.model.getLaunch().getChannel(), 64);
        this.node = node;
    }

    void onSuspended() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onSuspended();
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

    @Override
    protected boolean startDataRetrieval() {
        assert !isDisposed();
        final TCFDataCache<ISymbols.Symbol> type = node.getType();
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
        if (type_class == ISymbols.TypeClass.composite) {
            command = syms.getChildren(type_sym.getID(), new ISymbols.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                    Map<String,TCFNode> data = null;
                    if (command == token && error == null) {
                        data = new HashMap<String,TCFNode>();
                        for (String id : contexts) {
                            TCFNodeExpression n = findField(id);
                            if (n == null) n = new TCFNodeExpression(node, null, id, null, -1);
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
            int length = type_sym.getLength();
            for (int i = 0; i < length && i < 16; i++) {
                TCFNodeExpression n = findIndex(i);
                if (n == null) n = new TCFNodeExpression(node, null, null, null, i);
                data.put(n.id, n);
            }
            set(null, null, data);
            return true;
        }
        if (type_class == ISymbols.TypeClass.pointer) {
            Map<String,TCFNode> data = new HashMap<String,TCFNode>();
            TCFNodeExpression n = findIndex(0);
            if (n == null) n = new TCFNodeExpression(node, null, null, null, 0);
            data.put(n.id, n);
            set(null, null, data);
            return true;
        }
        set(null, null, new HashMap<String,TCFNode>());
        return true;
    }
}
