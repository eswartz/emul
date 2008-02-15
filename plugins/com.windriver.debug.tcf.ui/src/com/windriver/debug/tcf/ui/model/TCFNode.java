/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.ui.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.osgi.framework.Bundle;

import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.services.IMemory;
import com.windriver.tcf.api.services.IRunControl;

/**
 * TCFNode is base class for all TCF debug model elements.
 */
public abstract class TCFNode extends PlatformObject
implements IMemoryBlockRetrievalExtension, Comparable<TCFNode> {

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

    /**
     * Retrieve children count for a presentation context.
     * @param result - children count update request.
     */
    final void update(final IChildrenCountUpdate result) {
        new TCFRunnable(model.getDisplay(), result) {
            public void run() {
                if (!disposed && model.getLaunch().getChannel() != null) {
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
                if (!disposed && model.getLaunch().getChannel() != null) {
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
                if (!disposed && model.getLaunch().getChannel() != null) {
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
                if (!disposed) {
                    if (!validateNode(this)) return;
                    if (node_error != null) {
                        result.setForeground(new RGB(255, 0, 0), 0);
                        result.setLabel(node_error.getClass().getName() +
                            ": " + node_error.getMessage(), 0);
                    }
                    else {
                        getData(result);
                    }
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
        result.setImageDescriptor(getImageDescriptor(getImageName()), 0);
        result.setLabel(id, 0);
    }
    
    /**
     * Create ModelDelta for changes in this node.
     * @param flags - description of what has changed: IModelDelta.ADDED, IModelDelta.REMOVED, etc.
     * @return - ModelDelta that describes node changes.
     */
    ModelDelta makeModelDelta(int flags) {
        int count = -1;
        int index = -1;
        ModelDelta delta = model.getDelta(this);
        if (delta == null || delta.getChildCount() != count || delta.getIndex() != index) {
            ModelDelta parent_delta = parent.makeModelDelta(IModelDelta.NO_CHANGE);
            delta = parent_delta.addNode(this, index, flags, count);
            model.addDelta(this, delta);
        }
        else {
            delta.setFlags(delta.getFlags() | flags);
        }
        return delta;
    }

    /*--------------------------------------------------------------------------------------*/
    /* Node data retrieval state machine                                                    */

    protected Throwable node_error;
    protected IToken pending_command;
    private final Collection<TCFRunnable> wait_list = new ArrayList<TCFRunnable>();
    
    /**
     * Invalidate the node - flush all cached data.
     * Subclasses should override this method to flush any additional data.
     * Subclasses should call super.invalidateNode(). 
     */
    public void invalidateNode() {
        // cancel current data retrieval command
        if (pending_command != null) {
            pending_command.cancel();
            pending_command = null;
        }

        // cancel waiting monitors
        if (!wait_list.isEmpty()) {
            for (TCFRunnable r : wait_list) r.cancel();
            wait_list.clear();
        }

        node_error = null;
    }

    /**
     * Validate node - retrieve and put into a cache missing data from remote peer.
     * Validation is done asynchronously. If the node is already valid,
     * the method should return true. Otherwise, it returns false,
     * and later, when the node becomes valid, call-backs from 'wait_list' are invoked. 
     * @return true if the node is already valid, false if validation is started.
     */
    public final boolean validateNode() {
        assert Protocol.isDispatchThread();
        assert !disposed;
        if (pending_command != null) {
            return false;
        }
        else if (model.getLaunch().getChannel() == null) {
            node_error = null;
        }
        else if (node_error == null && !validateNodeData()) {
            return false;
        }
        if (!wait_list.isEmpty()) {
            Runnable[] arr = wait_list.toArray(new Runnable[wait_list.size()]);
            wait_list.clear();
            for (Runnable r : arr) r.run();
        }
        return true;
    }
    
    /**
     * Validate node - retrieve and put into a cache missing data from remote peer.
     * Validation is done asynchronously. If the node is already valid,
     * the method should return true. Otherwise, it returns false,
     * adds 'done' into 'wait_list', and later, when the node becomes valid,
     * call-backs from 'wait_list' are invoked.
     * @param done - call-back object to call when node becomes valid. 
     * @return true if the node is already valid, false if validation is started.
     */
    public final boolean validateNode(TCFRunnable done) {
        assert done != null;
        if (!validateNode()) {
            wait_list.add(done);
            return false;
        }
        return true;
    }
    
    private class ValidateNodes extends TCFRunnable {
        
        int cnt = 0;
        private IToken command;
        
        ValidateNodes(Collection<TCFNode> nodes) {
            for (TCFNode n : nodes) {
                if (!n.validateNode(this)) cnt++;
            }
            if (cnt > 0) {
                pending_command = command = new IToken() {
                    public boolean cancel() {
                        return false;
                    }
                };
            }
        }
        
        public void run() {
            cnt--;
            assert cnt >= 0;
            if (cnt != 0) return;
            if (command != pending_command) return;
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    if (command != pending_command) return;
                    pending_command = null;
                    validateNode();
                }
            });
        }
        
        @Override
        public void cancel() {
            run();
        }
    }

    /**
     * Subclasses can use this method to validate a collection of nodes.
     * Validation of multiple nodes is expensive and should be avoided
     * when possible.
     * 
     * Validation is performed in background, and call-backs from 'wait_list' are
     * activated when validation is done.
     *  
     * @param nodes
     * @return true if all nodes are already valid, false if validation is started.
     */
    protected boolean validateNodes(Collection<TCFNode> nodes) {
        if (nodes.isEmpty()) return true;
        if (pending_command != null) return false;
        return new ValidateNodes(nodes).cnt == 0;
    }
    
    /**
     * Subclasses should override this method to implement data retrieval that
     * is specific for this node.
     * 
     * Data retrieval should be performed in background, and it should call
     * validateNode() when retrieval is done.
     *  
     * @return true if the node is already valid, false if data retrieval is started.
     */
    protected abstract boolean validateNodeData();

    /*--------------------------------------------------------------------------------------*/
    /* Memory Block Retrieval                                                               */

    public IMemoryBlockExtension getExtendedMemoryBlock(String addr, Object ctx) throws DebugException {
        assert ctx == this;
        return getMemoryBlock(addr, 0);
    }

    public IMemoryBlock getMemoryBlock(long addr, long length) throws DebugException {
        return getMemoryBlock(Long.toString(addr), length);
    }

    public boolean supportsStorageRetrieval() {
        return getMemoryContext() != null;
    }

    private IMemoryBlockExtension getMemoryBlock(String addr, long length) throws DebugException {
        assert !Protocol.isDispatchThread();
        // TODO: MemoryBlock
        return null;
    }
    
    /*--------------------------------------------------------------------------------------*/
    /* Misc                                                                                 */

    private static final Map<String,ImageDescriptor> image_cache = new HashMap<String,ImageDescriptor>();

    static ImageDescriptor getImageDescriptor(String name) {
        if (name == null) return null;
        ImageDescriptor descriptor = image_cache.get(name);
        if (descriptor == null) {
            descriptor = ImageDescriptor.getMissingImageDescriptor();
            Bundle bundle = Platform.getBundle("org.eclipse.debug.ui");
            if (bundle != null){
                URL url = FileLocator.find(bundle, new Path(name), null);
                descriptor = ImageDescriptor.createFromURL(url);
            }
            image_cache.put(name, descriptor);
        }
        return descriptor;
    }

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
