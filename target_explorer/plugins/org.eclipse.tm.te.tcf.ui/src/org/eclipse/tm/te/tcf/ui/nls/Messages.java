/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.nls;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

/**
 * Target Explorer: TCF UI Plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tm.te.tcf.ui.nls.Messages"; //$NON-NLS-1$

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

	public static String NodePropertiesContentProvider_peerNode_sectionTitle;

	public static String NodePropertiesLabelProvider_state;
	public static String NodePropertiesLabelProvider_state__1;
	public static String NodePropertiesLabelProvider_state_0;
	public static String NodePropertiesLabelProvider_state_1;
	public static String NodePropertiesLabelProvider_state_2;
	public static String NodePropertiesLabelProvider_state_3;

	public static String NodePropertiesLabelProvider_lastScannerError;

	public static String NodePropertiesLabelProvider_services_local;
	public static String NodePropertiesLabelProvider_services_remote;

	public static String NewTargetWizard_windowTitle;
	public static String NewTargetWizard_error_savePeer;

	public static String NewTargetWizardPage_title;
	public static String NewTargetWizardPage_description;
	public static String NewTargetWizardPage_section_transportType;
	public static String NewTargetWizardPage_section_attributes;

	public static String PeerIdControl_label;
	public static String PeerNameControl_label;

	public static String TransportTypeControl_label;
	public static String TransportTypeControl_tcpType_label;
	public static String TransportTypeControl_sslType_label;
	public static String TransportTypeControl_pipeType_label;
	public static String TransportTypeControl_customType_label;

	public static String MyRemoteHostAddressControl_label;
	public static String MyRemoteHostAddressControl_information_missingTargetNameAddress;
	public static String MyRemoteHostAddressControl_error_invalidTargetNameAddress;
	public static String MyRemoteHostAddressControl_error_invalidTargetIpAddress;
	public static String MyRemoteHostAddressControl_error_targetNameNotResolveable;
	public static String MyRemoteHostAddressControl_information_checkNameAddressUserInformation;

	public static String PipeNameControl_label;
	public static String PipeNameControl_information_missingValue;
	public static String PipeNameControl_error_invalidValue;

	public static String CustomTransportNameControl_label;
	public static String CustomTransportNameControl_information_missingValue;
	public static String CustomTransportNameControl_error_invalidValue;

	public static String PeerAttributesTablePart_button_new;
	public static String PeerAttributesTablePart_button_edit;
	public static String PeerAttributesTablePart_button_remove;
	public static String PeerAttributesTablePart_column_name;
	public static String PeerAttributesTablePart_column_value;
	public static String PeerAttributesTablePart_add_dialogTitle;
	public static String PeerAttributesTablePart_add_title;
	public static String PeerAttributesTablePart_add_message;
	public static String PeerAttributesTablePart_edit_dialogTitle;
	public static String PeerAttributesTablePart_edit_title;
	public static String PeerAttributesTablePart_edit_message;
}
