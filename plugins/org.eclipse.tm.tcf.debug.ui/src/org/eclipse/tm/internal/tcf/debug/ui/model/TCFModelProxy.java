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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * A model proxy represents a model for a specific presentation context and
 * fires deltas to notify listeners of changes in the model.
 * Model proxy listeners are debuggers views.
 */
public class TCFModelProxy extends AbstractModelProxy implements IModelProxy, Runnable {

    private static final TCFNode[] EMPTY_NODE_ARRAY = new TCFNode[0];

    private final TCFModel model;
    private final TCFLaunch launch;
    private final Display display;
    private final Map<TCFNode,Integer> node2flags = new HashMap<TCFNode,Integer>();
    private final Map<TCFNode,TCFNode[]> node2children = new HashMap<TCFNode,TCFNode[]>();
    private final Map<TCFNode,ModelDelta> node2delta = new HashMap<TCFNode,ModelDelta>();
    private final LinkedList<TCFNode> selection = new LinkedList<TCFNode>();

    private boolean posted;
    private boolean installed;
    private boolean disposed;
    private boolean realized;
    private long last_update_time;

    private final Runnable timer = new Runnable() {

        public void run() {
            posted = false;
            long idle_time = System.currentTimeMillis() - last_update_time;
            long min_idle_time = model.getMinViewUpdatesInterval();
            if (model.getViewUpdatesThrottleEnabled()) {
                int congestion = Protocol.getCongestionLevel() + 50;
                if (congestion > 0) min_idle_time += congestion * 10;
            }
            if (model.getChannelThrottleEnabled()) {
                int congestion = model.getChannel().getCongestion() + 50;
                if (congestion > 0) min_idle_time += congestion * 10;
            }
            if (idle_time < min_idle_time - 5) {
                Protocol.invokeLater(min_idle_time - idle_time, this);
                posted = true;
            }
            else {
                TCFModelProxy.this.run();
            }
        }
    };

    private class ViewerUpdate implements IViewerUpdate {

        IStatus status;

        public Object getElement() {
            return null;
        }

        public TreePath getElementPath() {
            return null;
        }

        public IPresentationContext getPresentationContext() {
            return TCFModelProxy.this.getPresentationContext();
        }

        public Object getViewerInput() {
            return TCFModelProxy.this.getInput();
        }

        public void cancel() {
        }

        public void done() {
        }

        public IStatus getStatus() {
            return status;
        }

        public boolean isCanceled() {
            return false;
        }

        public void setStatus(IStatus status) {
            this.status = status;
        }
    }

    private class ChildrenCountUpdate extends ViewerUpdate implements IChildrenCountUpdate {

        int count;

        public void setChildCount(int count) {
            this.count = count;
        }
    }

    private class ChildrenUpdate extends ViewerUpdate implements IChildrenUpdate {

        int length;
        TCFNode[] children;

        void setLength(int length) {
            this.length = length;
            this.children = length == 0 ? EMPTY_NODE_ARRAY : new TCFNode[length];
        }

        public int getLength() {
            return length;
        }

        public int getOffset() {
            return 0;
        }

        public void setChild(Object child, int offset) {
            children[offset] = (TCFNode)child;
        }
    }

    private final IViewerUpdateListener update_listener = new IViewerUpdateListener() {

        public void viewerUpdatesBegin() {
            if (!model.getWaitForViewsUpdateAfterStep()) return;
            launch.addPendingClient(this);
        }

        public void viewerUpdatesComplete() {
            launch.removePendingClient(this);
        }

        public void updateStarted(IViewerUpdate update) {
        }

        public void updateComplete(IViewerUpdate update) {
        }
    };

    private final ChildrenCountUpdate children_count_update = new ChildrenCountUpdate();
    private final ChildrenUpdate children_update = new ChildrenUpdate();

    private TCFNode pending_node;

    TCFModelProxy(TCFModel model) {
        this.model = model;
        launch = model.getLaunch();
        display = model.getDisplay();
    }

    public void installed(Viewer viewer) {
        super.installed(viewer);
        ((ITreeModelViewer)viewer).addViewerUpdateListener(update_listener);
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                assert !installed;
                assert !disposed;
                model.onProxyInstalled(TCFModelProxy.this);
                installed = true;
            }
        });
    }

    public void dispose() {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                assert !disposed;
                if (installed) model.onProxyDisposed(TCFModelProxy.this);
                disposed = true;
            }
        });
        ((ITreeModelViewer)getViewer()).removeViewerUpdateListener(update_listener);
        launch.removePendingClient(update_listener);
        super.dispose();
    }

    /**
     * Add model change information (delta) to a buffer of pending deltas.
     * Implementation will coalesce and post deltas to the view.
     * @param node - a model node that changed.
     * @param flags - flags that describe the change, see IModelDelta
     */
    void addDelta(TCFNode node, int flags) {
        assert Protocol.isDispatchThread();
        assert installed && !disposed;
        if (flags == 0) return;
        Integer delta = node2flags.get(node);
        if (delta != null) flags |= delta.intValue();
        node2flags.put(node, flags);
        post();
    }

    /**
     * Request node to be expanded in the view.
     * @param node - a model node that will become expanded.
     */
    void expand(TCFNode node) {
        Object input = getInput();
        IPresentationContext ctx = getPresentationContext();
        while (node != null && node != input) {
            addDelta(node, IModelDelta.EXPAND);
            node = node.getParent(ctx);
        }
        post();
    }

    /**
     * Request view selection to be set to given node.
     * @param node - a model node that will become new selection.
     */
    void setSelection(TCFNode node) {
        selection.add(node);
        expand(node.getParent(getPresentationContext()));
    }

    /**
     * Get current value of the view input.
     * @return view input object.
     */
    Object getInput() {
        return getViewer().getInput();
    }

    public void post() {
        assert Protocol.isDispatchThread();
        assert installed && !disposed;
        if (!posted) {
            long idle_time = System.currentTimeMillis() - last_update_time;
            Protocol.invokeLater(model.getMinViewUpdatesInterval() - idle_time, timer);
            if (model.getWaitForViewsUpdateAfterStep()) launch.addPendingClient(this);
            posted = true;
        }
    }

    private TCFNode[] getNodeChildren(TCFNode node) {
        TCFNode[] res = node2children.get(node);
        if (res == null) {
            if (node.isDisposed()) {
                res = EMPTY_NODE_ARRAY;
            }
            else if (!node.getData(children_count_update, null)) {
                pending_node = node;
                res = EMPTY_NODE_ARRAY;
            }
            else {
                children_update.setLength(children_count_update.count);
                if (!node.getData(children_update, null)) {
                    assert false;
                    pending_node = node;
                    res = EMPTY_NODE_ARRAY;
                }
                else {
                    res = children_update.children;
                }
            }
            node2children.put(node, res);
        }
        return res;
    }

    private int getNodeIndex(TCFNode node) {
        TCFNode p = node.getParent(getPresentationContext());
        if (p == null) return -1;
        TCFNode[] arr = getNodeChildren(p);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == node) return i;
        }
        return -1;
    }

    private ModelDelta makeDelta(ModelDelta root, TCFNode node, TCFNode selection) {
        ModelDelta delta = node2delta.get(node);
        if (delta == null) {
            if (node == root.getElement()) {
                delta = root;
            }
            else {
                int flags = 0;
                Integer flags_obj = node2flags.get(node);
                if (flags_obj != null) flags = flags_obj.intValue();
                if (node == selection) {
                    // Bug in Eclipse 3.6.1: SELECT delta has no effect without STATE
                    flags |= IModelDelta.REVEAL | IModelDelta.SELECT | IModelDelta.STATE;
                }
                if (node.parent == null) {
                    // The node is TCF launch node
                    if (root.getElement() instanceof TCFNode) return null;
                    int children = -1;
                    if (selection != null && selection != node || (flags & IModelDelta.EXPAND) != 0) {
                        children = getNodeChildren(node).length;
                    }
                    delta = root.addNode(launch, -1, flags, children);
                }
                else {
                    TCFNode parent = node.getParent(getPresentationContext());
                    if (parent == null) return null;
                    ModelDelta up = makeDelta(root, parent, selection);
                    if (up == null) return null;
                    int index = -1;
                    int children = -1;
                    if (selection != null || (flags & IModelDelta.INSERTED) != 0 || (flags & IModelDelta.EXPAND) != 0) {
                        index = getNodeIndex(node);
                    }
                    if (selection != null && selection != node || (flags & IModelDelta.EXPAND) != 0) {
                        children = getNodeChildren(node).length;
                    }
                    delta = up.addNode(node, index, flags, children);
                }
                node2delta.put(node, delta);
            }
        }
        int flags = delta.getFlags();
        if ((flags & IModelDelta.REMOVED) != 0) return null;
        if ((flags & IModelDelta.CONTENT) != 0 && (flags & IModelDelta.EXPAND) == 0) return null;
        return delta;
    }

    private void asyncExec(Runnable r) {
        synchronized (Device.class) {
            if (!display.isDisposed()) {
                display.asyncExec(r);
            }
        }
    }

    private void postDelta(final ModelDelta root) {
        assert pending_node == null;
        launch.removePendingClient(this);
        asyncExec(new Runnable() {
            public void run() {
                fireModelChanged(root);
            }
        });
    }

    public void run() {
        assert Protocol.isDispatchThread();
        if (disposed) return;
        if (node2flags.isEmpty() && selection.isEmpty()) return;
        if (!realized) {
            if (getPresentationContext().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW)) {
                // Wait until launch manager done creating our launch item in the Debug view.
                // Deltas do NOT work without the launch item.
                asyncExec(new Runnable() {
                    boolean found;
                    public void run() {
                        found = ((ITreeModelContentProviderTarget)getViewer()).findElementIndex(TreePath.EMPTY, launch) >= 0;
                        Protocol.invokeLater(new Runnable() {
                            public void run() {
                                if (found) realized = true;
                                else last_update_time = System.currentTimeMillis();
                                post();
                            }
                        });
                    }
                });
                return;
            }
            else {
                realized = true;
            }
        }
        Object input = getInput();
        int flags = 0;
        if (input instanceof TCFNode) {
            // Optimize away STATE delta on a view input node
            TCFNode node = (TCFNode)input;
            Integer i = node2flags.get(node);
            if (i != null) {
                flags = i;
                if ((flags & IModelDelta.STATE) != 0) {
                    flags &= ~IModelDelta.STATE;
                    if (flags == 0) {
                        node2flags.remove(node);
                        if (node2flags.isEmpty() && selection.isEmpty()) return;
                    }
                    else {
                        node2flags.put(node, flags);
                    }
                }
            }
        }
        pending_node = null;
        node2children.clear();
        node2delta.clear();
        ModelDelta root = new ModelDelta(input, flags);
        for (TCFNode node : node2flags.keySet()) makeDelta(root, node, null);
        if (pending_node == null) {
            node2flags.clear();
            if ((root.getFlags() != 0 || node2delta.size() > 0) && (root.getFlags() & IModelDelta.REMOVED) == 0) {
                postDelta(root);
            }
            node2delta.clear();
            last_update_time = System.currentTimeMillis();
            while (!selection.isEmpty()) {
                TCFNode node = selection.getFirst();
                root = new ModelDelta(input, IModelDelta.NO_CHANGE);
                makeDelta(root, node, node);
                node2delta.clear();
                if (pending_node != null) break;
                postDelta(root);
                selection.remove(node);
            }
        }

        if (pending_node != null && pending_node.getData(children_count_update, this)) {
            assert false;
            Protocol.invokeLater(this);
        }
        node2children.clear();
    }
}
