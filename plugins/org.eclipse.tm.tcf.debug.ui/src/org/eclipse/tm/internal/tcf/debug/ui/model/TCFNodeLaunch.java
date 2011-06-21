/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;


public class TCFNodeLaunch extends TCFNode implements ISymbolOwner {

    private final TCFChildrenExecContext children;
    private final TCFChildren filtered_children;
    private final Map<String,TCFNodeSymbol> symbols = new HashMap<String,TCFNodeSymbol>();

    TCFNodeLaunch(final TCFModel model) {
        super(model);
        children = new TCFChildrenExecContext(this);
        filtered_children = new TCFChildren(this) {
            @Override
            protected boolean startDataRetrieval() {
                Set<String> filter = launch.getContextFilter();
                if (filter == null) {
                    if (!children.validate(this)) return false;
                    set(null, children.getError(), children.getData());
                    return true;
                }
                Map<String,TCFNode> nodes = new HashMap<String,TCFNode>();
                for (String id : filter) {
                    if (!model.createNode(id, this)) return false;
                    if (isValid()) {
                        // Ignore invalid IDs
                        reset();
                    }
                    else {
                        nodes.put(id, model.getNode(id));
                    }
                }
                set(null, null, nodes);
                return true;
            }
            @Override
            public void dispose() {
                getNodes().clear();
                super.dispose();
            }
        };
    }

    @Override
    void dispose() {
        ArrayList<TCFNodeSymbol> l = new ArrayList<TCFNodeSymbol>(symbols.values());
        for (TCFNodeSymbol s : l) s.dispose();
        assert symbols.size() == 0;
        super.dispose();
    }

    @Override
    protected boolean getData(IChildrenCountUpdate result, Runnable done) {
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
            if (!filtered_children.validate(done)) return false;
            result.setChildCount(filtered_children.size());
        }
        else {
            result.setChildCount(0);
        }
        return true;
    }

    @Override
    protected boolean getData(IChildrenUpdate result, Runnable done) {
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
            return filtered_children.getData(result, done);
        }
        return true;
    }

    @Override
    protected boolean getData(IHasChildrenUpdate result, Runnable done) {
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(result.getPresentationContext().getId())) {
            if (!filtered_children.validate(done)) return false;
            result.setHasChilren(filtered_children.size() > 0);
        }
        else {
            result.setHasChilren(false);
        }
        return true;
    }

    void onContextAdded(IRunControl.RunControlContext context) {
        children.onContextAdded(context);
    }

    void onContextAdded(IMemory.MemoryContext context) {
        children.onContextAdded(context);
    }

    void onAnyContextSuspendedOrChanged() {
        for (TCFNodeSymbol s : symbols.values()) s.onMemoryMapChanged();
    }

    void onAnyContextAddedOrRemoved() {
        filtered_children.reset();
    }

    public void addSymbol(TCFNodeSymbol s) {
        assert symbols.get(s.id) == null;
        symbols.put(s.id, s);
    }

    public void removeSymbol(TCFNodeSymbol s) {
        assert symbols.get(s.id) == s;
        symbols.remove(s.id);
    }

    public TCFChildren getChildren() {
        return children;
    }

    public TCFChildren getFilteredChildren() {
        return filtered_children;
    }
}
