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
 * Target Explorer: Context help id definitions.
 */
public interface IContextHelpIds {

	/**
	 * UI plug-in common context help id prefix.
	 */
	public final static String PREFIX = UIPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$

	/**
	 * New target wizard context help id.
	 */
	public final static String NEW_TARGET_WIZARD = PREFIX + "NewTargetWizard"; //$NON-NLS-1$

	/**
	 * Name/value pair dialog context help id.
	 */
	public final static String NAME_VALUE_PAIR_DIALOG = PREFIX + "NameValuePairDialog"; //$NON-NLS-1$
}
