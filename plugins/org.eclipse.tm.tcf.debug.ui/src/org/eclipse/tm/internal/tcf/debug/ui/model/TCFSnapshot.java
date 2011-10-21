/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * TCFSnapshot is used to create snapshots of debug views presentation data.
 * Such snapshots are used to implement various view update policies.
 */
class TCFSnapshot {

    private final IPresentationContext ctx;

    private final HashMap<TCFNode,PresentationData> cache = new HashMap<TCFNode,PresentationData>();

    private final String[] columns;
    private final RGB rgb_stalled = new RGB(128, 128, 128);

    private class PresentationData implements IChildrenCountUpdate, IChildrenUpdate, ILabelUpdate, Runnable {

        IViewerUpdate update;
        Runnable done;
        boolean canceled;
        IStatus status;

        String[] label;
        FontData[] font_data;
        ImageDescriptor[] image_desc;
        RGB[] fg_color;
        RGB[] bg_color;
        boolean label_done;

        TCFNode[] children;
        boolean children_done;

        boolean stalled;

        private final ArrayList<Runnable> waiting_list = new ArrayList<Runnable>();

        public IPresentationContext getPresentationContext() {
            return ctx;
        }

        public Object getElement() {
            return update.getElement();
        }

        public TreePath getElementPath() {
            return update.getElementPath();
        }

        public Object getViewerInput() {
            return update.getViewerInput();
        }

        public void setStatus(IStatus status) {
            this.status = status;
        }

        public IStatus getStatus() {
            return status;
        }

        public void done() {
            assert false;
        }

        public void cancel() {
            canceled = true;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public String[] getColumnIds() {
            return columns;
        }

        public void setLabel(String text, int col) {
            if (!label_done) {
                if (label == null) {
                    int cnt = columns == null ? 1 : columns.length;
                    label = new String[cnt];
                }
                label[col] = text;
            }
            else {
                if (col >= label.length) stalled = true;
                else if (label[col] != text) {
                    if (label[col] == null || text == null || !text.equals(label[col])) {
                        stalled = true;
                    }
                }
            }
        }

        public void setFontData(FontData fnt, int col) {
            if (!label_done) {
                if (font_data == null) {
                    int cnt = columns == null ? 1 : columns.length;
                    font_data = new FontData[cnt];
                }
                font_data[col] = fnt;
            }
        }

        public void setImageDescriptor(ImageDescriptor image, int col) {
            if (!label_done) {
                if (image_desc == null) {
                    int cnt = columns == null ? 1 : columns.length;
                    image_desc = new ImageDescriptor[cnt];
                }
                image_desc[col] = image;
            }
        }

        public void setForeground(RGB rgb, int col) {
            if (!label_done) {
                if (fg_color == null) {
                    int cnt = columns == null ? 1 : columns.length;
                    fg_color = new RGB[cnt];
                }
                fg_color[col] = rgb;
            }
        }

        public void setBackground(RGB rgb, int col) {
            if (!label_done) {
                if (bg_color == null) {
                    int cnt = columns == null ? 1 : columns.length;
                    bg_color = new RGB[cnt];
                }
                bg_color[col] = rgb;
            }
        }

        public int getOffset() {
            return 0;
        }

        public int getLength() {
            return children.length;
        }

        public void setChild(Object child, int offset) {
            if (!children_done) {
                children[offset] = (TCFNode)child;
            }
        }

        public void setChildCount(int cnt) {
            if (!children_done) {
                children = new TCFNode[cnt];
            }
        }

        public void run() {
            Runnable d = done;
            update = null;
            done = null;
            for (Runnable r : waiting_list) Protocol.invokeLater(r);
            waiting_list.clear();
            d.run();
        }
    }

    private PresentationData data;

    TCFSnapshot(IPresentationContext ctx) {
        this.ctx = ctx;
        columns = ctx.getColumns();
    }

    void dispose() {
        for (PresentationData d : cache.values()) {
            for (Runnable r : d.waiting_list) Protocol.invokeLater(r);
        }
        cache.clear();
    }

    /**
     * Retrieve children count for a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param update - children count update request.
     * @param node - debug model node.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    public boolean getData(IChildrenCountUpdate update, TCFNode node, Runnable done) {
        if (!getChildren(update, node, done)) return false;
        update.setChildCount(data.children.length);
        return true;
    }

    /**
     * Retrieve children for a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param update - children update request.
     * @param node - debug model node.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    public boolean getData(IChildrenUpdate update, TCFNode node, Runnable done) {
        if (!getChildren(update, node, done)) return false;
        int offset = 0;
        int r_offset = update.getOffset();
        int r_length = update.getLength();
        for (TCFNode n : data.children) {
            if (offset >= r_offset && offset < r_offset + r_length) {
                update.setChild(n, offset);
            }
            offset++;
        }
        return true;
    }

    /**
     * Check if the node has children in a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param update - "has children" update request.
     * @param node - debug model node.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    public boolean getData(IHasChildrenUpdate update, TCFNode node, Runnable done) {
        if (!getChildren(update, node, done)) return false;
        update.setHasChilren(data.children.length > 0);
        return true;
    }

    /**
     * Retrieve node label for a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param update - label update request.
     * @param node - debug model node.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    public boolean getData(ILabelUpdate update, TCFNode node, Runnable done) {
        if (!getLabel(update, node, done)) return false;
        String[] ids_update = update.getColumnIds();
        String[] ids_data = columns;
        if (ids_update != ids_data && !Arrays.equals(ids_update, ids_data)) {
            int n = ids_update == null ? 1 : ids_update.length;
            for (int i = 0; i < n; i++) update.setBackground(rgb_stalled, i);
        }
        else {
            if (data.label != null) {
                for (int i = 0; i < data.label.length; i++) {
                    if (data.label[i] != null) update.setLabel(data.label[i], i);
                }
            }
            if (data.font_data != null) {
                for (int i = 0; i < data.font_data.length; i++) {
                    if (data.font_data[i] != null) update.setFontData(data.font_data[i], i);
                }
            }
            if (data.image_desc != null) {
                for (int i = 0; i < data.image_desc.length; i++) {
                    if (data.image_desc[i] != null) update.setImageDescriptor(data.image_desc[i], i);
                }
            }
            if (data.stalled) {
                int n = ids_update == null ? 1 : ids_update.length;
                for (int i = 0; i < n; i++) update.setForeground(rgb_stalled, i);
            }
            else {
                if (data.fg_color != null) {
                    for (int i = 0; i < data.fg_color.length; i++) {
                        if (data.fg_color[i] != null) update.setForeground(data.fg_color[i], i);
                    }
                }
            }
            if (data.bg_color != null) {
                for (int i = 0; i < data.bg_color.length; i++) {
                    if (data.bg_color[i] != null) update.setBackground(data.bg_color[i], i);
                }
            }
        }
        return true;
    }

    private boolean getChildren(IViewerUpdate update, TCFNode node, Runnable done) {
        data = cache.get(node);
        if (data == null) cache.put(node, data = new PresentationData());
        assert data.update != update;
        if (data.children_done) return true;
        if (data.update != null) {
            data.waiting_list.add(done);
            return false;
        }
        data.update = update;
        data.done = done;
        if (data.children == null) {
            if (!node.getData((IChildrenCountUpdate)data, data)) return false;
            assert data.children != null;
        }
        if (!node.getData((IChildrenUpdate)data, data)) return false;
        data.children_done = true;
        data.update = null;
        data.done = null;
        return true;
    }

    private boolean getLabel(IViewerUpdate update, TCFNode node, Runnable done) {
        data = cache.get(node);
        if (data == null) cache.put(node, data = new PresentationData());
        assert data.update != update;
        if (data.label_done && data.stalled) return true;
        if (data.update != null) {
            data.waiting_list.add(done);
            return false;
        }
        data.update = update;
        data.done = done;
        if (!node.getData((ILabelUpdate)data, data)) return false;
        data.label_done = true;
        data.update = null;
        data.done = null;
        return true;
    }
}
