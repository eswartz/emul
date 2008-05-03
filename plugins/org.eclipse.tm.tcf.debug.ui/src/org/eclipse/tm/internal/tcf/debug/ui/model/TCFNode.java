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

import java.util.Collection;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;


/**
 * TCFNode is base class for all TCF debug model elements.
 */
public abstract class TCFNode extends PlatformObject implements Comparable<TCFNode> {

    protected final String id;
    protected final TCFNode parent;
    protected final TCFModel model;

    protected boolean disposed;

    /**
     * Constructor for a root node. There should be exactly one root in the model.
     * @param model
     */
    protected TCFNode(TCFModel model) {
        id = null;
        parent = null;
        this.model = model;
    }

    /**
     * Constructor for a node other then root. Node ID must be unique.
     * @param parent - parent node.
     * @param id - node ID.
     */
    protected TCFNode(TCFNode parent, String id) {
        assert Protocol.isDispatchThread();
        this.parent = parent;
        this.id = id;
        model = parent.model;
        model.addNode(id, this);
    }

    /**
     * Dispose this node. The node is removed from the model.
     * Subclasses should override the method to dispose children nodes, if any.
     */
    void dispose() {
        assert !disposed;
        if (parent != null) parent.dispose(id);
        invalidateNode();
        model.removeNode(id);
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

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == ILaunch.class) return model.getLaunch();
        if (adapter == IModelProxyFactory.class) return model;
        if (adapter == IElementLabelProvider.class) return model;
        if (adapter == IElementContentProvider.class) return model;
        if (adapter == IColumnPresentationFactory.class) return model;
        Object o = model.getCommand(adapter);
        if (o != null) return o;
        //System.err.println(adapter.getName());
        return super.getAdapter(adapter);
    }

    public final TCFNode getParent() {
        assert Protocol.isDispatchThread();
        return parent;
    }

    public IRunControl.RunControlContext getRunContext() {
        return null;
    }

    public IMemory.MemoryContext getMemoryContext() {
        return null;
    }

    public boolean isRunning() {
        return false;
    }

    public boolean isSuspended() {
        return false;
    }

    /**
     * Return address of this node.
     * For executable contexts and stack frames address is current PC.
     * @return
     */
    public String getAddress() {
        return null;
    }
    
    boolean isNodeContentVisibleInContext(IPresentationContext p) {
        return true;
    }

    /**
     * Retrieve children count for a presentation context.
     * @param result - children count update request.
     */
    final void update(final IChildrenCountUpdate result) {
        new TCFRunnable(model.getDisplay(), result) {
            public void run() {
                if (result.isCanceled()) return;
                IChannel channel = model.getLaunch().getChannel();
                if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                    if (!validateNode(this)) return;
                    getData(result);
                }
                else {
                    result.setChildCount(0);
                }
                result.setStatus(Status.OK_STATUS);
                done();
            }
        };
    }

    /**
     * Retrieve children for a presentation context.
     * @param result - children update request.
     */
    final void update(final IChildrenUpdate result) {
        new TCFRunnable(model.getDisplay(), result) {
            public void run() {
                if (result.isCanceled()) return;
                IChannel channel = model.getLaunch().getChannel();
                if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                    if (!validateNode(this)) return;
                    getData(result);
                }
                result.setStatus(Status.OK_STATUS);
                done();
            }
        };
    }

    /**
     * Check if node has children in a presentation context.
     * @param result - "has children" update request.
     */
    final void update(final IHasChildrenUpdate result) {
        new TCFRunnable(model.getDisplay(), result) {
            public void run() {
                if (result.isCanceled()) return;
                IChannel channel = model.getLaunch().getChannel();
                if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                    if (!validateNode(this)) return;
                    getData(result);
                }
                else {
                    result.setHasChilren(false);
                }
                result.setStatus(Status.OK_STATUS);
                done();
            }
        };
    }

    /**
     * Retrieve node label for a presentation context.
     * @param result - label update request.
     */
    final void update(final ILabelUpdate result) {
        new TCFRunnable(model.getDisplay(), result) {
            public void run() {
                if (result.isCanceled()) return;
                IChannel channel = model.getLaunch().getChannel();
                if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                    if (!validateNode(this)) return;
                    getData(result);
                }
                else {
                    result.setLabel("...", 0);
                }
                result.setStatus(Status.OK_STATUS);
                done();
            }
        };
    }
    
    /**
     * Retrieve children count for a presentation context.
     * The node is validated before calling this method,
     * so the method should return cached data.
     * The method is always called on TCF dispatch thread.
     * @param result - children count update request.
     */
    protected void getData(IChildrenCountUpdate result) {
        result.setChildCount(0);
    }
    
    /**
     * Retrieve children for a presentation context.
     * The node is validated before calling this method,
     * so the method should return cached data.
     * The method is always called on TCF dispatch thread.
     * @param result - children update request.
     */
    protected void getData(IChildrenUpdate result) {
    }
    
    /**
     * Check if the node has children in a presentation context.
     * The node is validated before calling this method,
     * so the method should return cached data.
     * The method is always called on TCF dispatch thread.
     * @param result - "has children" update request.
     */
    protected void getData(IHasChildrenUpdate result) {
        result.setHasChilren(false);
    }
    
    /**
     * Retrieve node label for a presentation context.
     * The node is validated before calling this method,
     * so the method should return cached data.
     * The method is always called on TCF dispatch thread.
     * @param result - label update request.
     */
    protected void getData(ILabelUpdate result) {
        result.setImageDescriptor(ImageCache.getImageDescriptor(getImageName()), 0);
        result.setLabel(id, 0);
    }
    
    /**
     * Create and post ModelDelta for changes in this node.
     * @param flags - description of what has changed: IModelDelta.ADDED, IModelDelta.REMOVED, etc.
     */
    final void makeModelDelta(int flags) {
        for (TCFModelProxy p : model.getModelProxyList()) {
            int f = flags & getRelevantModelDeltaFlags(p.getPresentationContext());
            if (f != 0) makeModelDelta(p, f);
        }
    }
    
    /**
     * Return bit set of model delta flags relevant for this node in given presentation context.
     * Sub-classes are supposed to override this method.
     * @param p - presentation context
     * @return bit set of model delta flags
     */
    int getRelevantModelDeltaFlags(IPresentationContext p) {
        return IModelDelta.CONTENT | IModelDelta.STATE;
    }

    /**
     * Create and post ModelDelta for changes in this node, relevant for given presentation context.
     * @param p - target presentation context.
     * @param flags - description of what has changed: IModelDelta.ADDED, IModelDelta.REMOVED, etc.
     * @return - ModelDelta that describes node changes.
     */
    ModelDelta makeModelDelta(TCFModelProxy p, int flags) {
        ModelDelta delta = p.getDelta(this);
        if (delta == null) {
            ModelDelta parent_delta = parent.makeModelDelta(p, IModelDelta.NO_CHANGE);
            delta = parent_delta.addNode(this, flags);
            p.addDelta(this, delta);
        }
        else {
            delta.setFlags(delta.getFlags() | flags);
        }
        return delta;
    }

    /*--------------------------------------------------------------------------------------*/
    /* Node data retrieval state machine                                                    */
    
    /**
     * Invalidate the node - flush all cached data.
     * Subclasses should override this method to flush any additional data.
     */
    public abstract void invalidateNode();
    
    /**
     * Validate node - retrieve and put into a cache missing data from remote peer.
     * The method should initiate retrieval of all data needed by TCFNode.update() methods.
     * Validation is done asynchronously. If the node is already valid,
     * the method should return true. Otherwise, it returns false,
     * adds 'done' into 'wait_list', and later call-backs from 'wait_list' are invoked.
     * Note: activation of call-back does not mean all data is retrieved,
     * it only means that node state changed, client should call validateNode() again,
     * until the method returns true.
     * @param done - call-back object to call when node state changes. 
     * @return true if the node is already valid, false if validation is started.
     */
    public abstract boolean validateNode(Runnable done);
    
    /**
     * Subclasses can use this method to validate a collection of nodes.
     * Validation of multiple nodes is expensive and should be avoided
     * when possible.
     * 
     * Validation is performed in background, and 'done' call-back is
     * activated when nodes state changes.
     *  
     * @param nodes
     * @return true if all nodes are already valid, false if validation is started.
     */
    protected boolean validateNodes(Collection<TCFNode> nodes, Runnable done) {
        TCFNode pending = null;
        for (TCFNode n : nodes) {
            if (!n.validateNode(null)) pending = n;
        }
        if (pending != null && !pending.validateNode(done)) return false;
        return true;
    }
        
    /*--------------------------------------------------------------------------------------*/
    /* Misc                                                                                 */

    protected String getImageName() {
        return null;
    }

    public int compareTo(TCFNode n) {
        return id.compareTo(n.id);
    }

    public String toString() {
        String s = "[" + Integer.toHexString(hashCode()) + "] " + id;
        if (disposed) s += ", disposed";
        return s;
    }
}
