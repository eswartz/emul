/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.util.TCFDataCache;

public class TCFNodeSymbol extends TCFNode {

    private final TCFData<ISymbols.Symbol> context;
    private final TCFData<String[]> children;

    private int update_policy;
    private ISymbolOwner owner;

    private TCFNodeSymbol prev;
    private TCFNodeSymbol next;

    private static final int MAX_SYMBOL_COUNT = 64;
    private static TCFNodeSymbol sym_list;
    private static int sym_count;
    private static boolean gc_posted;

    protected TCFNodeSymbol(final TCFNode parent, final String id) {
        super(parent, id);
        context = new TCFData<ISymbols.Symbol>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                ISymbols syms = launch.getService(ISymbols.class);
                if (id == null || syms == null) {
                    set(null, null, null);
                    return true;
                }
                command = syms.getContext(id, new ISymbols.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, ISymbols.Symbol sym) {
                        set(token, error, sym);
                        if (error != null || sym == null) setUpdatePolicy(null, 0);
                        else setUpdatePolicy(sym.getOwnerID(), sym.getUpdatePolicy());
                    }
                });
                return false;
            }
        };
        children = new TCFData<String[]>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                ISymbols syms = launch.getService(ISymbols.class);
                if (id == null || syms == null) {
                    set(null, null, null);
                    return true;
                }
                command = syms.getChildren(id, new ISymbols.DoneGetChildren() {
                    public void doneGetChildren(IToken token, Exception error, String[] ids) {
                        set(token, error, ids);
                    }
                });
                return false;
            }
        };
        setUpdatePolicy(null, 0);
        if (sym_list == null) {
            prev = next = this;
        }
        else {
            prev = sym_list;
            next = sym_list.next;
            prev.next = next.prev = this;
        }
        sym_list = this;
        if (!gc_posted) {
            // Garbage collection: dispose unused symbols
            gc_posted = true;
            Protocol.invokeLater(5000, new Runnable() {
                public void run() {
                    gc_posted = false;
                    int cnt = sym_count / 16;
                    while (sym_count > MAX_SYMBOL_COUNT) {
                        TCFNodeSymbol s = sym_list.next;
                        if (s.context.isPending()) break;
                        if (s.children.isPending()) break;
                        s.dispose();
                        if (cnt == 0) break;
                        cnt--;
                    }
                    if (sym_count > 0) {
                        gc_posted = true;
                        Protocol.invokeLater(5000, this);
                    }
                }
            });
        }
        sym_count++;
    }

    @Override
    public void dispose() {
        assert !isDisposed();
        if (owner != null) {
            owner.removeSymbol(this);
            owner = null;
        }
        if (sym_list == this) sym_list = prev;
        if (sym_list == this) {
            sym_list = null;
        }
        else {
            prev.next = next;
            next.prev = prev;
        }
        prev = next = null;
        sym_count--;
        assert (sym_count == 0) == (sym_list == null);
        super.dispose();
    }

    public TCFDataCache<ISymbols.Symbol> getContext() {
        if (sym_list != this) {
            prev.next = next;
            next.prev = prev;
            prev = sym_list;
            next = sym_list.next;
            prev.next = next.prev = this;
            sym_list = this;
        }
        return context;
    }

    public TCFDataCache<String[]> getChildren() {
        if (sym_list != this) {
            prev.next = next;
            next.prev = prev;
            prev = sym_list;
            next = sym_list.next;
            prev.next = next.prev = this;
            sym_list = this;
        }
        return children;
    }

    private void setUpdatePolicy(String id, int policy) {
        update_policy = policy;
        if (!isDisposed()) {
            TCFNode n = model.getNode(id);
            if (!(n instanceof ISymbolOwner)) n = parent;
            if (n != owner) {
                if (owner != null) owner.removeSymbol(this);
                owner = (ISymbolOwner)n;
                owner.addSymbol(this);
            }
        }
    }

    void onMemoryMapChanged() {
        context.reset();
        children.reset();
    }

    void onExeStateChange() {
        if (update_policy == ISymbols.UPDATE_ON_MEMORY_MAP_CHANGES) return;
        context.reset();
        children.reset();
    }
}
