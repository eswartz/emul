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
import java.util.Arrays;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;


public class TCFNodeLaunch extends TCFNode {

    private final TCFChildrenExecContext children;

    TCFNodeLaunch(final TCFModel model) {
        super(model);
        children = new TCFChildrenExecContext(this);
        // Set initial selection in Debug View
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (!children.validate()) {
                    children.wait(this);
                    return;
                }
                ArrayList<TCFNodeStackFrame> frames = new ArrayList<TCFNodeStackFrame>();
                TCFNode[] arr = children.toArray();
                Arrays.sort(arr);
                for (TCFNode n : arr) {
                    if (!searchTopFrame((TCFNodeExecContext)n, frames, this)) return;
                    if (frames.size() > 0) {
                        model.setDebugViewSelection(frames.get(0).id, true);
                        return;
                    }
                }
                if (arr.length > 0) {
                    model.setDebugViewSelection(arr[0].id, true);
                }
            }
        });
    }

    private boolean searchTopFrame(TCFNodeExecContext e, ArrayList<TCFNodeStackFrame> frames, Runnable r) {
        if (!e.validateNode(r)) return false;
        TCFNodeStackFrame f = e.getTopFrame();
        if (f != null && !f.disposed) {
            frames.add(f);
            return true;
        }
        TCFChildrenExecContext c = e.getChildren();
        if (!c.validate()) {
            c.wait(r);
            return false;
        }
        TCFNode[] arr = c.toArray();
        Arrays.sort(arr);
        for (TCFNode n : arr) {
            if (!searchTopFrame((TCFNodeExecContext)n, frames, r)) return false;
            if (frames.size() > 0) break;
        }
        return true;
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
    public int getNodeIndex(IPresentationContext p, TCFNode n) {
        if (!children.isValid()) return -1;
        return children.getIndexOf(n);
    }

    @Override
    public int getChildrenCount(IPresentationContext p) {
        if (!children.isValid()) return -1;
        return children.size();
    }

    @Override
    protected void getData(IChildrenCountUpdate result) {
        result.setChildCount(children.size());
    }

    @Override
    protected void getData(IChildrenUpdate result) {
        TCFNode[] arr = children.toArray();
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

    @Override
    protected void getData(IHasChildrenUpdate result) {
        result.setHasChilren(children.size() > 0);
    }

    void onContextAdded(IRunControl.RunControlContext context) {
        children.onContextAdded(context);
    }

    void onContextAdded(IMemory.MemoryContext context) {
        children.onContextAdded(context);
    }

    int getContextCount() {
        assert children.isValid();
        return children.size();
    }

    @Override
    public boolean validateNode(Runnable done) {
        if (!children.validate()) {
            children.wait(done);
            return false;
        }
        return true;
    }
}
