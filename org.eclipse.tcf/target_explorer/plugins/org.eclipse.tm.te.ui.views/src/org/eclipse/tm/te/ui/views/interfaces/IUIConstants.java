/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.interfaces;

/**
 * Target Explorer UI constants.
 */
public interface IUIConstants {

	/**
	 * The target explorer view id.
	 */
	public static final String ID_EXPLORER = "org.eclipse.tm.te.ui.views.TargetExplorer"; //$NON-NLS-1$

	/**
	 * The target explorer editor id.
	 */
	public static final String ID_EDITOR = "org.eclipse.tm.te.ui.view.Editor"; //$NON-NLS-1$

	// ***** Define the constants for the Target Explorer view root mode *****

	/**
	 * Root nodes are working sets.
	 */
	public static final int MODE_WORKING_SETS = 0;

	/**
	 * Root nodes are whatever is contributed to the view.
	 */
	public static final int MODE_NORMAL = 1;
}
