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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.tcf.protocol.Protocol;

public class TCFModelProxy extends AbstractModelProxy implements IModelProxy {

    private final TCFModel model;
    private final Map<TCFNode,Integer> deltas = new HashMap<TCFNode,Integer>();
    private final ArrayList<Runnable> wait_list = new ArrayList<Runnable>();
    
    private boolean pending_deltas;
    private boolean updating;

    private final IViewerUpdateListener update_listener = new IViewerUpdateListener() {
        public void viewerUpdatesComplete() {
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    pending_deltas = false;
                    updating = false;
                    if (wait_list.isEmpty()) return;
                    Runnable[] arr = wait_list.toArray(new Runnable[wait_list.size()]);
                    for (Runnable r : arr) r.run();
                    wait_list.clear();
                }
            });
        }               
        public void viewerUpdatesBegin() {
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    updating = true;
                }
            });
        }
        public void updateStarted(IViewerUpdate update) {
        }
        public void updateComplete(IViewerUpdate update) {
        }
    };        

    TCFModelProxy(TCFModel model) {
        this.model = model;
    }

    public void installed(Viewer viewer) {
        super.installed(viewer);
        model.onProxyInstalled(this);
        ((TreeModelViewer)viewer).addViewerUpdateListener(update_listener);
    }

    public void dispose() {
        ((TreeModelViewer)getViewer()).removeViewerUpdateListener(update_listener);
        model.onProxyDisposed(this);
        super.dispose();
    }
    
    void addDelta(TCFNode node, int flags) {
        Integer delta = deltas.get(node);
        if (delta != null) {
            deltas.put(node, Integer.valueOf(delta.intValue() | flags));
        }
        else {
            deltas.put(node, Integer.valueOf(flags));
        }
    }
    
    Viewer getProxyViewer() {
        return getViewer();
    }
    
    boolean validateViewer(Runnable done) {
        assert Protocol.isDispatchThread();
        if (pending_deltas || updating) {
            wait_list.add(done);
            return false;
        }
        return true;
    }
    
    private ModelDelta makeDelta(ModelDelta root, Map<TCFNode,ModelDelta> map, TCFNode node, int flags) {
        ModelDelta delta = map.get(node);
        if (delta == null) {
            if (node.parent == null) {
                delta = root.addNode(model.getLaunch(), flags);
                map.put(node, delta);
            }
            else {
                delta = makeDelta(root, map, node.parent, 0).addNode(node, flags);
                map.put(node, delta);
            }
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
            if (node.disposed) continue;
            makeDelta(root, map, node, deltas.get(node).intValue());
        }
        deltas.clear();
        if (map.isEmpty()) return;
        fireModelChanged(root);
        pending_deltas = true;
    }
}
