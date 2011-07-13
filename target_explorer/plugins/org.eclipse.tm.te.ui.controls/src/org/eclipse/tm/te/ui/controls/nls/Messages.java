/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls.nls;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

/**
 * Target ExploreR: Common Controls plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tm.te.ui.controls.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	/**
	 * Returns if or if not this NLS manager contains a constant for
	 * the given externalized strings key.
	 *
	 * @param key The externalized strings key or <code>null</code>.
	 * @return <code>True</code> if a constant for the given key exists, <code>false</code> otherwise.
	 */
	public static boolean hasString(String key) {
		if (key != null) {
			try {
				Field field = Messages.class.getDeclaredField(key);
				return field != null;
			} catch (NoSuchFieldException e) { /* ignored on purpose */ }
		}

		return false;
	}

	/**
	 * Returns the corresponding string for the given externalized strings
	 * key or <code>null</code> if the key does not exist.
	 *
	 * @param key The externalized strings key or <code>null</code>.
	 * @return The corresponding string or <code>null</code>.
	 */
	public static String getString(String key) {
		if (key != null) {
			try {
				Field field = Messages.class.getDeclaredField(key);
				if (field != null) {
					return (String)field.get(null);
				}
			} catch (Exception e) { /* ignored on purpose */ }
		}

		return null;
	}

	// **** Declare externalized string id's down here *****

	public static String BaseEditBrowseTextControl_button_label;
	public static String BaseEditBrowseTextControl_validationJob_name;

	public static String DirectorySelectionControl_title;
	public static String DirectorySelectionControl_group_label;
	public static String DirectorySelectionControl_editfield_label;

	public static String FileSelectionControl_title_open;
	public static String FileSelectionControl_title_save;
	public static String FileSelectionControl_group_label;
	public static String FileSelectionControl_editfield_label;

	public static String RemoteHostAddressControl_label;
	public static String RemoteHostAddressControl_information_checkNameAddressUserInformation;
	public static String RemoteHostAddressControl_information_checkNameAddressField;
	public static String RemoteHostAddressControl_information_checkNameAddressFieldOk;
	public static String RemoteHostAddressControl_information_missingTargetNameAddress;
	public static String RemoteHostAddressControl_error_invalidTargetNameAddress;
	public static String RemoteHostAddressControl_error_invalidTargetIpAddress;
	public static String RemoteHostAddressControl_error_targetNameNotResolveable;

	public static String NameOrIPValidator_Information_MissingNameOrIP;
	public static String NameOrIPValidator_Information_MissingName;
	public static String NameOrIPValidator_Information_MissingIP;
	public static String NameOrIPValidator_Information_CheckName;
	public static String NameOrIPValidator_Error_InvalidNameOrIP;
	public static String NameOrIPValidator_Error_InvalidName;
	public static String NameOrIPValidator_Error_InvalidIP;

	public static String PortNumberValidator_Information_MissingPortNumber;
	public static String PortNumberValidator_Error_InvalidPortNumber;
	public static String PortNumberValidator_Error_PortNumberNotInRange;

	public static String FileNameValidator_Information_MissingName;
	public static String FileNameValidator_Error_InvalidName;
	public static String FileNameValidator_Error_IsDirectory;
	public static String FileNameValidator_Error_MustExist;
	public static String FileNameValidator_Error_ReadOnly;
	public static String FileNameValidator_Error_NoAccess;
	public static String FileNameValidator_Error_IsRelativ;
	public static String FileNameValidator_Error_IsAbsolut;
	public static String FileNameValidator_Error_HasSpaces;

	public static String DirectoryNameValidator_Information_MissingName;
	public static String DirectoryNameValidator_Error_IsFile;
	public static String DirectoryNameValidator_Error_MustExist;
	public static String DirectoryNameValidator_Error_ReadOnly;
	public static String DirectoryNameValidator_Error_NoAccess;
	public static String DirectoryNameValidator_Error_IsRelativ;
	public static String DirectoryNameValidator_Error_IsAbsolut;

	public static String RegexValidator_Information_MissingValue;
	public static String RegexValidator_Error_InvalidValue;

	public static String HexValidator_Error_InvalidValueRange;

	public static String WorkspaceContainerValidator_Information_MissingValue;
	public static String WorkspaceContainerValidator_Error_InvalidValue;

	public static String NumberValidator_Error_InvalidRange;
}
