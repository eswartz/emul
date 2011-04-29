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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Target Explorer: Representation of a file system tree node.
 * <p>
 * <b>Note:</b> Node construction and child list access is limited to
 * the TCF event dispatch thread.
 */
public final class FSTreeNode extends PlatformObject {
	private final UUID fUniqueId = UUID.randomUUID();

	/**
	 * The tree node name.
	 */
	public String name = null;

	/**
	 * The tree node type.
	 */
	public String type = null;

	/**
	 * The tree node file system attributes
	 */
	public IFileSystem.FileAttrs attr = null;

	/**
	 * The peer node the file system tree node is associated with.
	 */
	public IPeerModel peerNode = null;

	/**
	 * The tree node parent.
	 */
	public FSTreeNode parent = null;

	/**
	 * The tree node children.
	 */
	public List<FSTreeNode> children = new ArrayList<FSTreeNode>();

	/**
	 * Flag to mark once the children of the node got queried
	 */
	public boolean childrenQueried = false;

	/**
	 * Flag to mark once the children query is running
	 */
	public boolean childrenQueryRunning = false;

	/**
	 * Constructor.
	 */
	public FSTreeNode() {
		super();
		assert Protocol.isDispatchThread();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return fUniqueId.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof FSTreeNode) {
			return fUniqueId.equals(((FSTreeNode)obj).fUniqueId);
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}
}
