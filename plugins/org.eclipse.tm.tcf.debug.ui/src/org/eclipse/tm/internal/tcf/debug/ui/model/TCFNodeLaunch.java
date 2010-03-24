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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;


public class TCFNodeLaunch extends TCFNode implements ISymbolOwner {

    private final TCFChildrenExecContext children;
    private final Map<String,TCFNodeSymbol> symbols = new HashMap<String,TCFNodeSymbol>();

    TCFNodeLaunch(final TCFModel model) {
        super(model);
        children = new TCFChildrenExecContext(this);
        // Set initial selection in Debug View
        Protocol.invokeLater(new Runnable() {
            boolean done;
            public void run() {
                if (done) return;
                ArrayList<TCFNodeStackFrame> frames = new ArrayList<TCFNodeStackFrame>();
                if (!searchTopFrames(children, frames, this)) return;
                for (TCFNodeStackFrame f : frames) model.setDebugViewSelection(f.id, false);
                done = true;
            }
        });
    }

    private boolean searchTopFrames(TCFChildrenExecContext c, ArrayList<TCFNodeStackFrame> frames, Runnable r) {
        if (!c.validate(r)) return false;
        TCFNode[] arr = c.toArray();
        Arrays.sort(arr);
        for (TCFNode n : arr) {
            if (!searchTopFrames((TCFNodeExecContext)n, frames, r)) return false;
        }
        return true;
    }

    private boolean searchTopFrames(TCFNodeExecContext e, ArrayList<TCFNodeStackFrame> frames, Runnable r) {
        TCFChildrenStackTrace stack_trace = e.getStackTrace();
        if (!stack_trace.validate(r)) return false;
        TCFNodeStackFrame f = stack_trace.getTopFrame();
        if (f != null && !f.disposed) frames.add(f);
        return searchTopFrames(e.getChildren(), frames, r);
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

    @Override
    protected boolean getData(IChildrenCountUpdate result, Runnable done) {
        if (!children.validate(done)) return false;
        result.setChildCount(children.size());
        return true;
    }

    @Override
    protected boolean getData(IChildrenUpdate result, Runnable done) {
        if (!children.validate(done)) return false;
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
        return true;
    }

    @Override
    protected boolean getData(IHasChildrenUpdate result, Runnable done) {
        if (!children.validate(done)) return false;
        result.setHasChilren(children.size() > 0);
        return true;
    }

    void onContextAdded(IRunControl.RunControlContext context) {
        children.onContextAdded(context);
    }

    void onContextAdded(IMemory.MemoryContext context) {
        children.onContextAdded(context);
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
