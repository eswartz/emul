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

import java.util.UUID;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A common (data) model node.
 * <p>
 * Each model node is capable of dealing with generic properties, provides
 * it's own image and label for representation within the UI and can be used
 * as synchronization object for the Eclipse job model.
 */
public interface IModelNode extends IPropertiesContainer, ISchedulingRule {

	/**
	 * Property: Model node name. May be used to represent the node
	 *           in UI widgets.
	 */
	public static final String PROPERTY_NAME = "name";  //$NON-NLS-1$

	/**
	 * Property: Model node visible state. May be used to hide the node
	 *           in UI widgets like lists, tables or trees.
	 */
	public static final String PROPERTY_IS_VISIBLE = "isVisible"; //$NON-NLS-1$

	/**
	 * Property: Type. May be used to group nodes or set an node type id.
	 */
	public static final String PROPERTY_TYPE = "type"; //$NON-NLS-1$

	/**
	 * Property: Type label. May be used to represent the group or node
	 *           type in UI widgets.
	 */
	public static final String PROPERTY_TYPE_LABEL = "typeLabel"; //$NON-NLS-1$

	/**
	 * Property: Model node error text. May be used to decorate the
	 *           node in UI widgets with the error text.
	 */
	public static final String PROPERTY_ERROR = "error"; //$NON-NLS-1$

	/**
	 * Returns the parent node.
	 * @return The parent
	 */
	public IContainerModelNode getParent();

	/**
	 * Returns the first parent node that implements the given type
	 * or <code>null</code> if no matching parent can be found.
	 *
	 * @param nodeType The interface/class the parent needs to implement/extend.
	 * @return The parent or <code>null</code>.
	 */
	public IContainerModelNode getParent(Class<?> nodeType);

	/**
	 * Associated the given container model node as parent. The parent node
	 * can be set only once.
	 *
	 * @param parent The parent container model node.
	 * @throws <code>IllegalStateException</code> if the node had been associated already with a parent.
	 */
	public void setParent(IContainerModelNode parent) throws IllegalStateException;

	/**
	 * Moves the model node to the specified new parent container. If the model
	 * node is associated with a parent container, the node will be removed from
	 * the old parent container node non-recursive.
	 * <p>
	 * <b>Note:</b> The method will trigger 2 change events, a {@link IContainerModelNode#NOTIFY_REMOVED}
	 * notification for the old parent (if any) and a {@link IContainerModelNode#NOTIFY_ADDED} notification
	 * for the new parent container.
	 *
	 * @param newParent The new parent container. Must not be <code>null</code>.
	 * @throws IllegalStateException if the move of the node failed.
	 */
	public void move(IContainerModelNode newParent) throws IllegalStateException;

	/**
	 * Return <code>true</code>, if this model node should be visible.
	 */
	public boolean isVisible();

	/**
	 * Returns the text label to be shown within the UI for this node.
	 */
	public String getName();

	/**
	 * Returns the image id of the image to show within the UI for
	 * this node. If this node can be adapted to {@linkplain ImageRegistry.class},
	 * the image is retrieved from the adapter.
	 *
	 * @return The image id or <code>null</code>.
	 */
	public String getImageId();

	/**
	 * Returns the current error or null if no error to show in the status line of the UI.
	 */
	public String getError();

	/**
	 * Returns up to descriptive strings to show in the status line of the UI.
	 * If getErrorText() returns a not null value, this strings are ignored.
	 */
	public String[] getDescription();

	/**
	 * Set the nodes dirty state.
	 * <p>
	 * If a node is dirty, it should be refreshed.
	 *
	 * @param dirty The dirty state.
	 */
	public void setDirty(boolean dirty);

	/**
	 * Returns the nodes dirty state.
	 *
	 * @return <code>True</code> if this node is dirty and needs to be refreshed, <code>false</code> otherwise.
	 */
	public boolean isDirty();

	/**
	 * Set the nodes pending state.
	 *
	 * @param pending The pending state.
	 */
	public void setPending(boolean pending);

	/**
	 * Returns the nodes pending state.
	 *
	 * @return <code>True</code> if an (asynchronous) operation (i.e. refresh) is running for this node.
	 */
	public boolean isPending();

	/**
	 * Lookup a model node by it's unique identifier.
	 *
	 * @param uuid The unique identifier. Must not be <code>null</code>.
	 * @return The model node matching the given unique identifier, or <code>null</code>.
	 */
	public IModelNode find(UUID uuid);
}
