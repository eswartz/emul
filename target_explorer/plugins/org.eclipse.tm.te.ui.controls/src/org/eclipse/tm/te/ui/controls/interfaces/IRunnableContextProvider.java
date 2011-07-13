/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls.interfaces;

import org.eclipse.jface.operation.IRunnableContext;

/**
 * Target Explorer: Public interface of a runnable context provider.
 */
public interface IRunnableContextProvider {

	/**
	 * Returns the associated runnable context.
	 *
	 * @return The runnable context or <code>null</code> if none.
	 */
	public IRunnableContext getRunnableContext();
}
