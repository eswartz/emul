/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.interfaces;

import org.eclipse.tm.te.ui.activator.UIPlugin;

/**
 * Target Explorer: Common UI constants.
 *
 * @author uwe.stieber@windriver.com
 */
public interface IUIConstants {

	/**
	 * The Target Explorer common controls context menu id base part.
	 */
	public static final String ID_CONTROL_MENUS_BASE = UIPlugin.getUniqueIdentifier() + ".controls"; //$NON-NLS-1$
}
