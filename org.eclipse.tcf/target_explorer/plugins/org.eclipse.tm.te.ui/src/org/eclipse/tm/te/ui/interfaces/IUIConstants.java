/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.interfaces;

import org.eclipse.tm.te.ui.activator.UIPlugin;

/**
 * Common UI constants.
 */
public interface IUIConstants {

	/**
	 * The Target Explorer common controls context menu id base part.
	 */
	public static final String ID_CONTROL_MENUS_BASE = UIPlugin.getUniqueIdentifier() + ".controls"; //$NON-NLS-1$

	/**
	 * The Target Explorer new target wizard selection page context help id.
	 */
	public static final String HELP_NEW_WIZARD_SELECTION_PAGE = UIPlugin.getUniqueIdentifier() + ".NewWizardSelectionPage"; //$NON-NLS-1$
}
