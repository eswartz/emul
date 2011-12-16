/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.stepper.interfaces;

/**
 * A unique, hierarchically id used by steps, step groups and others.
 */
public interface IFullQualifiedId {

	/**
	 * Get the parent id or <code>null</code> if this is the root.
	 *
	 * @return The parent id or <code>null</code>.
	 */
	public IFullQualifiedId getParentId();

	/**
	 * Creates a new id using this id as the parent.
	 *
	 * @param type The type of the new child.
	 * @param id The id for the new child.
	 * @param secondaryId The secondary id of the new child.
	 * @return The new created full qualified id.
	 */
	public IFullQualifiedId createChildId(String type, String id, String secondaryId);

	/**
	 * Get the type of this id.
	 *
	 * @return The type.
	 */
	public String getType();

	/**
	 * Get the id of this node.
	 *
	 * @return The id.
	 */
	public String getId();

	/**
	 * Get the secondary id of this node or <code>null</code>
	 * if there is no secondary id.
	 *
	 * @return The secondary id or <code>null</code>.
	 */
	public String getSecondaryId();
}
