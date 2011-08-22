/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.interfaces.nodes;

import java.util.List;


/**
 * A container (data) model node.
 * <p>
 * The container can have both container model node and model node
 * children. Container model nodes can be used as synchronization
 * object for the Eclipse jobs API.
 */
public interface IContainerModelNode extends IModelNode {
	/**
	 * Property change notification: Specific child node has been added.
	 */
	public static final String NOTIFY_ADDED = "added";  //$NON-NLS-1$

	/**
	 * Property change notification: Specific child node has been removed.
	 */
	public static final String NOTIFY_REMOVED = "removed";  //$NON-NLS-1$

	/**
	 * Property change notification: Unspecified child nodes may have changed, added or removed.
	 */
	public static final String NOTIFY_CHANGED = "changed"; //$NON-NLS-1$

	/**
	 * Adds the given child node to the list of children.
	 *
	 * @param child The child node to append. Must not be <code>null</code>!
	 */
	public boolean add(IModelNode child);

	/**
	 * Removes the given node from the list of children.
	 *
	 * @param node The node to remove or <code>null</code>.
	 * @param recursive If <code>true</code> and the node is a container model node, the children
	 *                  of the container model node will be removed recursively.
	 *
	 * @return <code>true</code> if the list of children contained the given node, <code>false</code> otherwise.
	 */
	public boolean remove(IModelNode node, boolean recursive);

	/**
	 * Remove all child nodes recursively.
	 */
	public boolean clear();

	/**
	 * Remove all child nodes with a special type.
	 *
	 * @param nodeType The node type.
	 * @return <code>True</code> if child nodes got removed from the mode, <code>false</code> if not.
	 */
	public <T> boolean removeAll(Class<T> nodeType);

	/**
	 * Returns the child nodes.
	 */
	public IModelNode[] getChildren();

	/**
	 * Returns all child nodes with a special type.
	 *
	 * @param nodeType The node type.
	 * @return The list of nodes or an empty list.
	 */
	public <T> List<T> getChildren(Class<T> nodeType);

	/**
	 * Returns true if node may have children.
	 */
	boolean hasChildren();

	/**
	 * Returns the current count of child nodes.
	 */
	public int size();

	/**
	 * Returns if or if not the given model node is a child of this container.
	 *
	 * @param node The model node.
	 * @return <code>true</code> if the given model node is a child of this container, <code>false</code> otherwise.
	 */
	public boolean contains(IModelNode node);
}
