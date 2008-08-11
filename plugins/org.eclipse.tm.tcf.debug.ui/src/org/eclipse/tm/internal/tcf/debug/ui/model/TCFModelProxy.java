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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.tcf.protocol.Protocol;

public class TCFModelProxy extends AbstractModelProxy implements IModelProxy {
    
    private static final int CONTENT_FLAGS =
        IModelDelta.ADDED | IModelDelta.REMOVED |
        IModelDelta.REPLACED | IModelDelta.INSERTED |
        IModelDelta.CONTENT | IModelDelta.STATE;

    private final TCFModel model;
    private final Map<TCFNode,Integer> deltas = new HashMap<TCFNode,Integer>();
    
    TCFModelProxy(TCFModel model) {
        this.model = model;
    }

    public synchronized void installed(Viewer viewer) {
        super.installed(viewer);
        IPresentationContext p = getPresentationContext();
        if (p != null) model.onProxyInstalled(p, this);
    }

    public synchronized void dispose() {
        IPresentationContext p = getPresentationContext();
        if (p != null) model.onProxyDisposed(p);
        super.dispose();
    }
    
    void addDelta(TCFNode node, int flags) {
        if (flags != 0) {
            Integer delta = deltas.get(node);
            if (delta != null) {
                deltas.put(node, Integer.valueOf(delta.intValue() | flags));
            }
            else {
                deltas.put(node, Integer.valueOf(flags));
            }
        }
    }
    
    Viewer getProxyViewer() {
        return getViewer();
    }
    
    private ModelDelta makeDelta(ModelDelta root, Map<TCFNode,ModelDelta> map, TCFNode node, int flags) {
        boolean content_only = (flags & ~CONTENT_FLAGS) == 0;
        ModelDelta delta = map.get(node);
        if (delta == null) {
            IPresentationContext p = getPresentationContext();
            if (node.parent == null) {
                if (content_only && (root.getFlags() & IModelDelta.CONTENT) != 0) return null;
                delta = root.addNode(model.getLaunch(), -1, flags, node.getChildrenCount(p));
            }
            else {
                ModelDelta parent = makeDelta(root, map, node.parent, 0);
                if (parent == null) return null;
                if (content_only && (parent.getFlags() & IModelDelta.CONTENT) != 0) return null;
                delta = parent.addNode(
                        node, node.parent.getNodeIndex(p, node),
                        flags, node.getChildrenCount(p));
            }
            map.put(node, delta);
        }
        else if (flags != 0) {
            delta.setFlags(delta.getFlags() | flags);
        }
        return delta;
    }

    void fireModelChanged() {
        assert Protocol.isDispatchThread();
        if (deltas.isEmpty()) return;
        ModelDelta root = new ModelDelta(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NO_CHANGE);
        Map<TCFNode,ModelDelta> map = new HashMap<TCFNode,ModelDelta>();
        for (TCFNode node : deltas.keySet()) {
            makeDelta(root, map, node, deltas.get(node).intValue());
        }
        deltas.clear();
        if (map.isEmpty()) return;
        fireModelChanged(root);
    }
}
