/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.interfaces;


/**
 * Target Explorer: Executable extension public interface declaration.
 */
public interface IExecutableExtension extends org.eclipse.core.runtime.IExecutableExtension {

	/**
	 * Returns the unique id of the extension. The returned
	 * id must be never <code>null</code> or an empty string.
	 *
	 * @return The unique id.
	 */
	public String getId();

	/**
	 * Returns the label or UI name of the extension.
	 *
	 * @return The label or UI name. An empty string if not set.
	 */
	public String getLabel();

	/**
	 * Returns the description of the extension.
	 *
	 * @return The description or an empty string.
	 */
	public String getDescription();
}
