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
public class TCFNode extends PlatformObject
implements IMemoryBlockRetrievalExtension, Comparable<TCFNode> {

    protected final String id;
    protected final TCFNode parent;
    protected final TCFModel model;

    protected boolean disposed;

    protected TCFNode(TCFNode parent, String id) {
        assert Protocol.isDispatchThread();
        this.parent = parent;
        this.id = id;
        model = parent.model;
    }

    protected TCFNode(TCFModel model) {
        id = null;
        parent = null;
        this.model = model;
    }

    /**
     * Dispose this node. The node is removed from the model.
     */
    void dispose() {
        assert !disposed;
        if (parent != null) parent.dispose(id);
        model.removeNode(id);
        disposed = true;
    }
    
    /**
     * A child node is being disposed.
     * The child should be removed from this node children lists.
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
                        result.setBackground(new RGB(255, 0, 0), 0);
                        result.setLabel(node_error.getClass().getName() +
                            ": " + node_error.getMessage(), 0);
                    }
                    else {
                        getData(result);
                    }
                }
                else {
                    result.setLabel("[Disposed]", 0);
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
     * @param flags - description of what has changed: CF_CONTEXT, CF_CHILDREN or CF_ALL.
     * @return - ModelDelta that describes node changes.
     */
    ModelDelta makeModelDelta(int flags) {
        int count = -1;
        //if (node_valid == CF_ALL) count = children.size();
        ModelDelta delta = model.getDelta(this);
        int index = -1;
        /*
        if (parent.node_valid == CF_ALL) {
            index = 0;
            for (Iterator<TCFNode> i = parent.children.values().iterator(); i.hasNext();) {
                if (i.next() == this) break;
                index++;
            }
        }
        */
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

    void onContextAdded(IRunControl.RunControlContext context) {
        assert !disposed;
        // TODO: Bug in Eclipse: IModelDelta.INSERTED fails if this is root node
        invalidateNode(CF_CHILDREN);
        makeModelDelta(IModelDelta.CONTENT);
    }

    void onContextAdded(IMemory.MemoryContext context) {
        assert !disposed;
        // TODO: Bug in Eclipse: IModelDelta.INSERTED fails if this is root node
        invalidateNode(CF_CHILDREN);
        makeModelDelta(IModelDelta.CONTENT);
    }

    /*--------------------------------------------------------------------------------------*/
    /* Node data retrieval state machine                                                    */

    protected static final int 
    	CF_CHILDREN     = 0x0001,
        CF_CONTEXT      = 0x0002,
        CF_ALL          = CF_CHILDREN | CF_CONTEXT;

    protected int node_valid;
    protected Throwable node_error;
    protected IToken data_command;
    protected final Collection<TCFRunnable> wait_list = new ArrayList<TCFRunnable>();
    
    /**
     * Invalidate the node - flush all cached data.
     */
    public void invalidateNode() {
        invalidateNode(CF_ALL);
    }

    protected void invalidateNode(int flags) {
    	// flags - set of CF_*
    	
        // cancel current data retrieval command
        if (data_command != null) {
            data_command.cancel();
            data_command = null;
        }

        // cancel waiting monitors
        if (!wait_list.isEmpty()) {
            TCFRunnable[] arr = wait_list.toArray(new TCFRunnable[wait_list.size()]);
            for (TCFRunnable r : arr) r.cancel();
            wait_list.clear();
        }

        if (flags == CF_ALL) { 
            node_error = null;
        }
        
        node_valid &= ~flags;
    }

    /**
     * Validate node - retrieve and put into a cache missing data from remote peer.
     * Validation is done asynchronously.
     * @param done - call back, it is called when validation is done.
     * @return true if the node is valid, false if validation is started.
     */
    public boolean validateNode(TCFRunnable done) {
        assert Protocol.isDispatchThread();
        assert (node_valid & ~CF_ALL) == 0;
        if (data_command != null) {
            if (done != null) wait_list.add(done);
            return false;
        }
        else if (model.getLaunch().getChannel() == null) {
            node_error = null;
            node_valid = CF_ALL;
        }
        else {
            if ((node_valid & CF_CONTEXT) == 0 && !validateContext(done)) return false;
            if ((node_valid & CF_CHILDREN) == 0 && !validateChildren(done)) return false;
        }
        assert node_valid == CF_ALL;
        if (!wait_list.isEmpty()) {
            Runnable[] arr = wait_list.toArray(new Runnable[wait_list.size()]);
            wait_list.clear();
            for (int i = 0; i < arr.length; i++) arr[i].run();
        }
        return true;
    }
    
    protected boolean validateContext(TCFRunnable done) {
        node_valid |= CF_CONTEXT;
        return true;
    }

    protected boolean validateChildren(TCFRunnable done) {
        node_valid |= CF_CHILDREN;
        return true;
    }

    /*--------------------------------------------------------------------------------------*/
    /* Memory Block Retrieval                                                                                 */

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
