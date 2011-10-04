/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.te.tcf.filesystem.internal.events.INodeStateListener;
import org.eclipse.tm.te.tcf.filesystem.internal.handlers.CacheManager;

/**
 * The file system model implementation.
 * <p>
 * The file system model provides access to the file system
 * model root node per peer id.
 */
public final class FSModel extends PlatformObject {
	// Shared instance
	private static final FSModel instance = new FSModel();
	/**
	 * Get the shared instance of File System model.
	 * @return The File System Model.
	 */
	public static FSModel getInstance(){
		return instance;
	}
	/**
	 * The file system model root node cache. The keys
	 * are the peer id's.
	 */
	private final Map<String, FSTreeNode> roots;
	// The table mapping the local file to the fileNodes.
	private Map<String, FSTreeNode> fileNodes;
	// The table mapping the URL to the fileNodes.
	private Map<URL, FSTreeNode> urlNodes;
	// Node state listeners.
	private List<INodeStateListener> listeners;

	/**
	 * Create a File System Model.
	 */
	private FSModel() {
		roots = Collections.synchronizedMap(new HashMap<String, FSTreeNode>());
		fileNodes = Collections.synchronizedMap(new HashMap<String, FSTreeNode>());
		urlNodes = Collections.synchronizedMap(new HashMap<URL, FSTreeNode>());
		listeners = Collections.synchronizedList(new ArrayList<INodeStateListener>());
	}
	/**
	 * Returns the file system model root node for the peer identified
	 * by the given peer id.
	 *
	 * @param peerId The peer id. Must not be <code>null</code>.
	 * @return The file system model root node or <code>null</code> if not found.
	 */
	public FSTreeNode getRoot(String peerId) {
		Assert.isNotNull(peerId);
		return roots.get(peerId);
	}

	/**
	 * Stores the given file system model root node for the peer identified
	 * by the given peer id. If the node is <code>null</code>, a previously
	 * stored file system model root node is removed.
	 *
	 * @param peerId The peer id. Must not be <code>null</code>.
	 * @param node The file system model root node or <code>null</code>.
	 */
	public void putRoot(String peerId, FSTreeNode node) {
		Assert.isNotNull(peerId);
		if (node != null) roots.put(peerId, node);
		else roots.remove(peerId);
	}

	/**
	 * Dispose the file system model instance.
	 */
	public void dispose() {
		roots.clear();
		fileNodes.clear();
		urlNodes.clear();
	}

	/**
	 * Called to add an FSTreeNode to the two maps, i.e., the location-node map and
	 * the cache-location map.
	 * @param node The FSTreeNode to be added.
	 */
	public void addNode(FSTreeNode node){
		File cacheFile = CacheManager.getInstance().getCacheFile(node);
		fileNodes.put(cacheFile.getAbsolutePath(), node);
		urlNodes.put(node.getLocationURL(), node);
	}

	/**
	 * Get the FSTreeNode given its local file's path.
	 *
	 * @param path The local file's path.
	 * @return The FSTreeNode
	 */
	public FSTreeNode getTreeNode(String path) {
		return fileNodes.get(path);
	}

	/**
	 * Get the FSTreeNode given its URL location
	 * @param location the FSTreeNode's location
	 * @return the FSTreeNode
	 */
	public FSTreeNode getTreeNode(URL location){
		return urlNodes.get(location);
	}

	/**
	 * Add an INodeStateListener to the File System model if it is not
	 * in the listener list yet.
	 *
	 * @param listener The INodeStateListener to be added.
	 */
	public void addNodeStateListener(INodeStateListener listener){
		if(!listeners.contains(listener)){
			listeners.add(listener);
		}
	}

	/**
	 * Remove the INodeStateListener from the File System model if it
	 * exists in the listener list.
	 *
	 * @param listener The INodeStateListener to be removed.
	 */
	public void removeNodeStateListener(INodeStateListener listener){
		if(listeners.contains(listener)){
			listeners.remove(listener);
		}
	}

	/**
	 * Fire a node state changed event with the specified node.
	 *
	 * @param node The node whose state has changed.
	 */
	public void fireNodeStateChanged(FSTreeNode node){
		synchronized(listeners){
			for(INodeStateListener listener:listeners){
				listener.stateChanged(node);
			}
		}
	}
}
