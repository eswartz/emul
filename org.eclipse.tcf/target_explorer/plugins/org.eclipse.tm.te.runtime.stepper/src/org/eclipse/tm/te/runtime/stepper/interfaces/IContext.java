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

import org.eclipse.core.runtime.IAdaptable;

/**
 * Interface to be implemented by objects representing a context for a step.
 */
public interface IContext extends IAdaptable {

	/**
	 * Returns the context id.
	 *
	 * @return The context id or <code>null</code>.
	 */
	public String getContextId();

	/**
	 * Returns a name/label to be used within the UI to represent this context
	 * to the user.
	 *
	 * @return The name or <code>null</code>.
	 */
	public String getContextName();

	/**
	 * Returns a possible multi-line string providing detail information
	 * about the context which shall be included in failure messages.
	 *
	 * @return The context information or <code>null</code>.
	 */
	public String getContextInfo();
}
