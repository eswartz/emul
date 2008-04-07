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
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.tcf.protocol.Protocol;

public class TCFModelProxy extends AbstractModelProxy implements IModelProxy {

    private final TCFModel model;
    private final Map<TCFNode,ModelDelta> deltas = new HashMap<TCFNode,ModelDelta>();

    TCFModelProxy(TCFModel model) {
        this.model = model;
    }

    public void installed(Viewer viewer) {
        super.installed(viewer);
        model.onProxyInstalled(this);
    }

    public void dispose() {
        model.onProxyDisposed(this);
        super.dispose();
    }
    
    ModelDelta getDelta(TCFNode node) {
        return deltas.get(node);
    }

    void addDelta(TCFNode node, ModelDelta delta) {
        assert deltas.get(node) == null;
        assert delta.getElement() == node || delta.getElement() == model.getLaunch() && node == model.getRootNode();
        deltas.put(node, delta);
    }

    void fireModelChanged() {
        assert Protocol.isDispatchThread();
        ModelDelta delta = deltas.get(model.getRootNode());
        assert (delta == null) == deltas.isEmpty();
        if (delta != null) {
            deltas.clear();
            assert delta.getElement() == model.getLaunch();
            IModelDelta top = delta.getParentDelta();
            assert top.getElement() == DebugPlugin.getDefault().getLaunchManager();
            fireModelChanged(top);
        }
    }
}
