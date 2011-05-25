/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;

/**
 * The file system model implementation.
 * <p>
 * The file system model provides access to the file system
 * model root node per peer id.
 */
public final class FSModel extends PlatformObject {
	/**
	 * The file system model root node cache. The keys
	 * are the peer id's.
	 */
	private final Map<String, FSTreeNode> roots = new HashMap<String, FSTreeNode>();

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
	}
}
