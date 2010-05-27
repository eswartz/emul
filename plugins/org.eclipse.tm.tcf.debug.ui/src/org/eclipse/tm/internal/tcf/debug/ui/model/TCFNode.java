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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.Protocol;


/**
 * TCFNode is base class for all TCF debug model elements.
 */
public abstract class TCFNode extends PlatformObject implements Comparable<TCFNode> {

    protected final String id;
    protected final TCFNode parent;
    protected final TCFModel model;
    protected final IChannel channel;

    protected boolean disposed;

    /**
     * Constructor for a root node. There should be exactly one root in the model.
     * @param model
     */
    protected TCFNode(TCFModel model) {
        id = null;
        parent = null;
        channel = model.getChannel();
        this.model = model;
    }

    /**
     * Constructor for a node other then root. Node ID must be unique.
     * @param parent - parent node.
     * @param id - node ID.
     */
    protected TCFNode(TCFNode parent, String id) {
        assert Protocol.isDispatchThread();
        assert parent != null;
        assert id != null;
        assert !parent.disposed;
        this.parent = parent;
        this.id = id;
        model = parent.model;
        model.addNode(id, this);
        channel = model.getChannel();
    }

    /**
     * Dispose this node. The node is removed from the model.
     * Subclasses should override the method to dispose children nodes, if any.
     */
    void dispose() {
        assert !disposed;
        if (parent != null) parent.dispose(id);
        if (id != null) {
            assert model.getNode(id) == this;
            model.removeNode(id);
        }
        disposed = true;
    }

    /**
     * A child node is being disposed.
     * The child should be removed from this node children lists.
     * Base node class does not support any children, so the method is empty.
     * Subclasses should override the method if they can have children.
     * @param id - ID of a node being disposed.
     */
    void dispose(String id) {
    }

    /**
     * Check if node is disposed.
     * @return true if disposed.
     */
    public final boolean isDisposed() {
        return disposed;
    }

    /**
     * Get TCFModel that owns this node.
     * @return TCFModel object
     */
    public TCFModel getModel() {
        return model;
    }

    /**
     * Get IChannel of TCFModel that owns this node.
     * @return IChannel object
     */
    public IChannel getChannel() {
        return channel;
    }

    /**
     * Get TCF ID of the node.
     * @return TCF ID
     */
    public String getID() {
        return id;
    }

    /**
     * Returns an object which is an instance of the given class
     * associated with this object. Returns <code>null</code> if
     * no such object can be found.
     *
     * @param adapter the class to adapt to
     * @return the adapted object or <code>null</code>
     * @see IAdaptable#getAdapter(Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(final Class adapter) {
        if (adapter.isInstance(model)) return model;
        Object o = model.getAdapter(adapter, TCFNode.this);
        if (o != null) return o;
        return super.getAdapter(adapter);
    }

    /**
     * Get parent node.
     * @return parent node or null if the node is a root
     */
    public final TCFNode getParent() {
        assert Protocol.isDispatchThread();
        return parent;
    }

    /**
     * Retrieve children count for a presentation context.
     * @param result - children count update request.
     */
    final void update(final IChildrenCountUpdate result) {
        new TCFRunnable(result) {
            public void run() {
                if (!done) {
                    if (!result.isCanceled()) {
                        if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                            if (!getData(result, this)) return;
                        }
                        else {
                            result.setChildCount(0);
                        }
                        result.setStatus(Status.OK_STATUS);
                    }
                    done();
                }
            }
        };
    }

    /**
     * Retrieve children for a presentation context.
     * @param result - children update request.
     */
    final void update(final IChildrenUpdate result) {
        new TCFRunnable(result) {
            public void run() {
                if (!done) {
                    if (!result.isCanceled()) {
                        if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                            if (!getData(result, this)) return;
                        }
                        result.setStatus(Status.OK_STATUS);
                    }
                    done();
                }
            }
        };
    }

    /**
     * Check if node has children in a presentation context.
     * @param result - "has children" update request.
     */
    final void update(final IHasChildrenUpdate result) {
        new TCFRunnable(result) {
            public void run() {
                if (!done) {
                    if (!result.isCanceled()) {
                        IChannel channel = model.getLaunch().getChannel();
                        if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                            if (!getData(result, this)) return;
                        }
                        else {
                            result.setHasChilren(false);
                        }
                        result.setStatus(Status.OK_STATUS);
                    }
                    done();
                }
            }
        };
    }

    /**
     * Retrieve node label for a presentation context.
     * @param result - label update request.
     */
    final void update(final ILabelUpdate result) {
        new TCFRunnable(result) {
            public void run() {
                if (!done) {
                    if (!result.isCanceled()) {
                        if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                            if (!getData(result, this)) return;
                        }
                        else {
                            result.setLabel("...", 0);
                        }
                        result.setStatus(Status.OK_STATUS);
                    }
                    done();
                }
            }
        };
    }

    /**
     * Retrieve children count for a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param result - children count update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    protected boolean getData(IChildrenCountUpdate result, Runnable done) {
        result.setChildCount(0);
        return true;
    }

    /**
     * Retrieve children for a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param result - children update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    protected boolean getData(IChildrenUpdate result, Runnable done) {
        return true;
    }

    /**
     * Check if the node has children in a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param result - "has children" update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    protected boolean getData(IHasChildrenUpdate result, Runnable done) {
        result.setHasChilren(false);
        return true;
    }

    /**
     * Retrieve node label for a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param result - label update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    protected boolean getData(ILabelUpdate result, Runnable done) {
        result.setLabel(id, 0);
        return true;
    }

    /**
     * Create and post ModelDelta for changes in this node.
     * @param flags - description of what has changed: IModelDelta.ADDED, IModelDelta.REMOVED, etc.
     */
    final void addModelDelta(int flags) {
        model.addDelta(this, flags);
    }

    /**
     * Return bit set of model delta flags relevant for this node in given presentation context.
     * Sub-classes are supposed to override this method.
     * @param p - presentation context
     * @return bit set of model delta flags
     */
    int getRelevantModelDeltaFlags(IPresentationContext p) {
        return IModelDelta.CONTENT | IModelDelta.STATE | IModelDelta.ADDED | IModelDelta.REMOVED;
    }

    /*--------------------------------------------------------------------------------------*/
    /* Misc                                                                                 */

    public int compareTo(TCFNode n) {
        return id.compareTo(n.id);
    }

    public String toString() {
        String s = "[" + Integer.toHexString(hashCode()) + "] " + id;
        if (disposed) s += ", disposed";
        return s;
    }
}
