/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;

public class TCFNodeArrayPartition extends TCFNode {

    private final int offs;
    private final int size;
    private final TCFChildrenSubExpressions children;

    TCFNodeArrayPartition(TCFNode parent, int level, int offs, int size) {
        super(parent, "AP" + level + "." + offs + "." + parent.id);
        this.offs = offs;
        this.size = size;
        children = new TCFChildrenSubExpressions(this, level, offs, size);
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

    int getOffset() {
        return offs;
    }

    int getSize() {
        return size;
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
        result.setHasChilren(true);
        return true;
    }

    @Override
    protected boolean getData(ILabelUpdate result, Runnable done) {
        result.setImageDescriptor(ImageCache.getImageDescriptor(ImageCache.IMG_ARRAY_PARTITION), 0);
        String[] cols = result.getColumnIds();
        String name = "[" + offs + ".." + (offs + size - 1) + "]";
        if (cols == null || cols.length <= 1) {
            result.setLabel(name, 0);
        }
        else {
            for (int i = 0; i < cols.length; i++) {
                String c = cols[i];
                if (c.equals(TCFColumnPresentationExpression.COL_NAME)) {
                    result.setLabel(name, i);
                }
                else {
                    result.setLabel("", i);
                }
            }
        }
        return true;
    }

    @Override
    int getRelevantModelDeltaFlags(IPresentationContext p) {
        if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(p.getId()) ||
                IDebugUIConstants.ID_VARIABLE_VIEW.equals(p.getId())) {
            return super.getRelevantModelDeltaFlags(p);
        }
        return 0;
    }

    @Override
    public int compareTo(TCFNode n) {
        TCFNodeArrayPartition p = (TCFNodeArrayPartition)n;
        if (offs < p.offs) return -1;
        if (offs > p.offs) return +1;
        return 0;
    }
}
