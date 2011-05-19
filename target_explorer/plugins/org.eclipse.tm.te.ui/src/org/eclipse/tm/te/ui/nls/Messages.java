/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.nls;

import org.eclipse.osgi.util.NLS;

/**
 * Target Explorer: Common UI plugin externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tm.te.ui.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String NewWizard_dialog_title;

	public static String NewWizardSelectionPage_title;
	public static String NewWizardSelectionPage_description;
	public static String NewWizardSelectionPage_wizards;
	public static String NewWizardSelectionPage_createWizardFailed;

	public static String AbstractAction_UnsatisfiedLinkError_title;
	public static String AbstractAction_UnsatisfiedLinkError_message;

	public static String NodePropertiesTableControl_section_title;
	public static String NodePropertiesTableControl_section_title_noSelection;
	public static String NodePropertiesTableControl_column_name_label;
	public static String NodePropertiesTableControl_column_value_label;

	public static String PendingOperation_label;

	public static String EditBrowseTextControl_button_label;
}
