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

import java.util.Arrays;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;


public class TCFNodeLaunch extends TCFNode {
    
    private final TCFChildrenExecContext children; 

    TCFNodeLaunch(TCFModel model) {
        super(model);
        children = new TCFChildrenExecContext(this);
    }

    @Override
    void dispose() {
        children.dispose();
        super.dispose();
    }

    @Override
    void dispose(String id) {
        children.dispose(id);
    }

    @Override
    protected void getData(IChildrenCountUpdate result) {
        result.setChildCount(children.size());
    }
    
    @Override
    protected void getData(IChildrenUpdate result) {
        int offset = 0;
        TCFNode[] arr = children.toArray();
        Arrays.sort(arr);
        for (TCFNode n : arr) {
            if (offset >= result.getOffset() && offset < result.getOffset() + result.getLength()) {
                result.setChild(n, offset);
            }
            offset++;
        }
    }
    
    @Override
    protected void getData(IHasChildrenUpdate result) {
        result.setHasChilren(children.size() > 0);
    }
    
    @Override
    protected void getData(ILabelUpdate result) {
        result.setImageDescriptor(getImageDescriptor(getImageName()), 0);
        String label = id;
        TCFLaunch launch = model.getLaunch();
        String status = "";
        if (launch.isConnecting()) status = "Connecting";
        else if (launch.isDisconnected()) status = "Disconnected";
        else if (launch.isTerminated()) status = "Terminated";
        Throwable error = launch.getError();
        if (error != null) {
            status += " - " + error;
            result.setForeground(new RGB(255, 0, 0), 0);
        }
        if (status.length() > 0) status = " (" + status + ")";
        label = launch.getLaunchConfiguration().getName() + status;
        result.setLabel(label, 0);
    }
    
    void onContextAdded(IRunControl.RunControlContext context) {
        children.onContextAdded(context);
    }

    void onContextAdded(IMemory.MemoryContext context) {
        children.onContextAdded(context);
    }

    @Override
    ModelDelta makeModelDelta(int flags) {
        int count = -1;
        ModelDelta delta = model.getDelta(this);
        if (delta == null) {
            delta = new ModelDelta(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NO_CHANGE);
            delta = delta.addNode(model.getLaunch(), -1, flags, count);
            model.addDelta(this, delta);
        }
        else {
            assert delta.getChildCount() == count;
            delta.setFlags(delta.getFlags() | flags);
        }
        return delta;
    }

    @Override
    public void invalidateNode() {
        super.invalidateNode();
        children.invalidate();
    }

    @Override
    protected boolean validateNodeData() {
        if (!children.valid && !children.validate()) return false;
        return true;
    }

    @Override
    protected String getImageName() {
        return "icons/full/obj16/ldebug_obj.gif";
    }
}
