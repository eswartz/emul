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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFDataCache;


public class TCFNodeLaunch extends TCFNode implements ISymbolOwner {

    private final TCFChildrenExecContext children;
    private final TCFDataCache<TCFNode[]> filtered_children;
    private final Map<String,TCFNodeSymbol> symbols = new HashMap<String,TCFNodeSymbol>();

    TCFNodeLaunch(final TCFModel model) {
        super(model);
        children = new TCFChildrenExecContext(this);
        filtered_children = new TCFDataCache<TCFNode[]>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                Set<String> filter = model.getLaunch().getContextFilter();
                if (filter == null) {
                    if (!children.validate(this)) return false;
                    set(null, children.getError(), children.toArray());
                    return true;
                }
                Set<TCFNode> nodes = new HashSet<TCFNode>();
                for (String id : filter) {
                    if (!model.createNode(id, this)) return false;
                    if (isValid()) {
                        // Ignore invalid IDs
                        reset();
                    }
                    else {
                        nodes.add(model.getNode(id));
                    }
                }
                TCFNode[] array = nodes.toArray(new TCFNode[nodes.size()]);
                Arrays.sort(array);
                set(null, null, array);
                return true;
            }
        };
        // Set initial selection in Debug View
        Protocol.invokeLater(new Runnable() {
            boolean done;
            public void run() {
                if (done) return;
                ArrayList<TCFNodeExecContext> nodes = new ArrayList<TCFNodeExecContext>();
                if (!searchSuspendedThreads(filtered_children, nodes, this)) return;
                if (nodes.size() == 1) {
                    TCFNodeExecContext n = nodes.get(0);
                    model.setDebugViewSelection(n, "Launch");
                }
                else {
                    for (TCFNodeExecContext n : nodes) {
                        String reason = n.getState().getData().suspend_reason;
                        model.setDebugViewSelection(n, reason);
                    }
                }
                done = true;
            }
        });
    }

    private boolean searchSuspendedThreads(TCFDataCache<TCFNode[]> c, ArrayList<TCFNodeExecContext> nodes, Runnable r) {
        if (!c.validate(r)) return false;
        TCFNode[] arr = c.getData();
        if (arr == null) return true;
        for (TCFNode n : arr) {
            if (!searchSuspendedThreads((TCFNodeExecContext)n, nodes, r)) return false;
        }
        return true;
    }

    private boolean searchSuspendedThreads(TCFChildren c, ArrayList<TCFNodeExecContext> nodes, Runnable r) {
        if (!c.validate(r)) return false;
        for (TCFNode n : c.toArray()) {
            if (!searchSuspendedThreads((TCFNodeExecContext)n, nodes, r)) return false;
        }
        return true;
    }

    private boolean searchSuspendedThreads(TCFNodeExecContext n, ArrayList<TCFNodeExecContext> nodes, Runnable r) {
        TCFDataCache<IRunControl.RunControlContext> run_context = n.getRunContext();
        if (!run_context.validate(r)) return false;
        IRunControl.RunControlContext ctx = run_context.getData();
        if (ctx != null && ctx.hasState()) {
            TCFDataCache<TCFContextState> state = n.getState();
            if (!state.validate(r)) return false;
            TCFContextState s = state.getData();
            if (s != null && s.is_suspended) nodes.add(n);
            return true;
        }
        return searchSuspendedThreads(n.getChildren(), nodes, r);
    }

    @Override
    void dispose() {
        children.dispose();
        ArrayList<TCFNodeSymbol> l = new ArrayList<TCFNodeSymbol>(symbols.values());
        for (TCFNodeSymbol s : l) s.dispose();
        assert symbols.size() == 0;
        super.dispose();
    }

    @Override
    void dispose(String id) {
        children.dispose(id);
    }

    private boolean validateFilteredChildren(Runnable done) {
        if (filtered_children.isValid()) {
            TCFNode[] arr = filtered_children.getData();
            if (arr != null) {
                for (TCFNode n : arr) {
                    if (n.disposed) {
                        filtered_children.reset();
                        break;
                    }
                }
            }
        }
        return filtered_children.validate(done);
    }

    @Override
    protected boolean getData(IChildrenCountUpdate result, Runnable done) {
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
            if (!validateFilteredChildren(done)) return false;
            TCFNode[] arr = filtered_children.getData();
            result.setChildCount(arr == null ? 0 : arr.length);
        }
        else {
            result.setChildCount(0);
        }
        return true;
    }

    @Override
    protected boolean getData(IChildrenUpdate result, Runnable done) {
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
            if (!validateFilteredChildren(done)) return false;
            TCFNode[] arr = filtered_children.getData();
            if (arr != null) {
                int offset = 0;
                int r_offset = result.getOffset();
                int r_length = result.getLength();
                for (TCFNode n : arr) {
                    if (offset >= r_offset && offset < r_offset + r_length) {
                        result.setChild(n, offset);
                    }
                    offset++;
                }
            }
        }
        return true;
    }

    @Override
    protected boolean getData(IHasChildrenUpdate result, Runnable done) {
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
            if (!validateFilteredChildren(done)) return false;
            TCFNode[] arr = filtered_children.getData();
            result.setHasChilren(arr != null && arr.length > 0);
        }
        else {
            result.setHasChilren(false);
        }
        return true;
    }

    void onContextAdded(IRunControl.RunControlContext context) {
        children.onContextAdded(context);
        filtered_children.reset();
    }

    void onContextAdded(IMemory.MemoryContext context) {
        children.onContextAdded(context);
        filtered_children.reset();
    }

    void onAnyContextSuspendedOrChanged() {
        for (TCFNodeSymbol s : symbols.values()) s.onMemoryMapChanged();
    }

    public void addSymbol(TCFNodeSymbol s) {
        assert symbols.get(s.id) == null;
        symbols.put(s.id, s);
    }

    public void removeSymbol(TCFNodeSymbol s) {
        assert symbols.get(s.id) == s;
        symbols.remove(s.id);
    }

    public TCFChildrenExecContext getChildren() {
        return children;
    }
}
