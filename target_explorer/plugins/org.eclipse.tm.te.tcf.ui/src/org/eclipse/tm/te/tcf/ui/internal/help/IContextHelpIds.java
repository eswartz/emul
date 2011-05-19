/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.help;

import org.eclipse.tm.te.tcf.ui.activator.UIPlugin;

/**
 * Target Explorer: Context help id definitions.
 */
public interface IContextHelpIds {

	/**
	 * TCF data source UI plug-in common context help id prefix.
	 */
	public final static String PREFIX = UIPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$

	/**
	 * New TCF target wizard main page.
	 */
	public final static String NEW_TARGET_WIZARD_PAGE = PREFIX + "NewTargetWizardPage"; //$NON-NLS-1$

	/**
	 * Target Explorer details editor page: Node properties
	 */
	public final static String NODE_PROPERTIES_EDITOR_PAGE = PREFIX + "NodePropertiesEditorPage"; //$NON-NLS-1$
}
