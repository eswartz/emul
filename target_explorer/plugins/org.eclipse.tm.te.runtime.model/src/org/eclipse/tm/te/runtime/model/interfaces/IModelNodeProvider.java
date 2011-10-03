/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.model.interfaces;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Interface to implement from objects which are associated
 * with (data) model nodes and providing access to them.
 */
public interface IModelNodeProvider extends IAdaptable {

	/**
	 * Returns the associated (data) model node.
	 *
	 * @return The (data) model node or <code>null</code>.
	 */
	public IModelNode getModelNode();
}
