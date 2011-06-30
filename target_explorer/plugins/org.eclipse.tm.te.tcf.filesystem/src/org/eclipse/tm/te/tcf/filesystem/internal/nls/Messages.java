/*********************************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River)		- initial API and implementation
 * William Chen (Wind River)	- [345384]Provide property pages for remote file system nodes
 *********************************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.nls;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

/**
 * Target Explorer: File System plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages"; //$NON-NLS-1$

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

	public static String AdvancedAttributesDialog_Archive;
	public static String AdvancedAttributesDialog_ArchiveIndex;
	public static String AdvancedAttributesDialog_Compress;
	public static String AdvancedAttributesDialog_Compressed;
	public static String AdvancedAttributesDialog_CompressEncrypt;
	public static String AdvancedAttributesDialog_Device;
	public static String AdvancedAttributesDialog_Directory;
	public static String AdvancedAttributesDialog_Encrypt;
	public static String AdvancedAttributesDialog_Encrypted;
	public static String AdvancedAttributesDialog_FileArchive;
	public static String AdvancedAttributesDialog_FileBanner;
	public static String AdvancedAttributesDialog_FolderArchive;
	public static String AdvancedAttributesDialog_FolderBanner;
	public static String AdvancedAttributesDialog_Hidden;
	public static String AdvancedAttributesDialog_Indexed;
	public static String AdvancedAttributesDialog_IndexFile;
	public static String AdvancedAttributesDialog_IndexFolder;
	public static String AdvancedAttributesDialog_Normal;
	public static String AdvancedAttributesDialog_Offline;
	public static String AdvancedAttributesDialog_ReadOnly;
	public static String AdvancedAttributesDialog_Reparse;
	public static String AdvancedAttributesDialog_ShellTitle;
	public static String AdvancedAttributesDialog_Sparse;
	public static String AdvancedAttributesDialog_System;
	public static String AdvancedAttributesDialog_Temporary;
	public static String AdvancedAttributesDialog_Virtual;

	public static String InformationPage_Accessed;
	public static String InformationPage_Advanced;
	public static String InformationPage_Attributes;
	public static String InformationPage_Computer;
	public static String InformationPage_File;
	public static String InformationPage_Folder;
	public static String InformationPage_Hidden;
	public static String InformationPage_Location;
	public static String InformationPage_Modified;
	public static String InformationPage_Name;
	public static String InformationPage_ReadOnly;
	public static String InformationPage_Size;
	public static String InformationPage_Type;

	public static String FSExplorerTreeControl_section_title;

	public static String FSTreeControl_column_name_label;
	public static String FSTreeControl_column_size_label;
	public static String FSTreeControl_column_modified_label;

	public static String FSOpenFileDialog_title;

	public static String OpenWithMenu_DefaultEditor;

	public static String PermissionsGroup_Executable;
	public static String PermissionsGroup_GroupPermissions;
	public static String PermissionsGroup_OtherPermissions;
	public static String PermissionsGroup_Readable;
	public static String PermissionsGroup_UserPermissions;
	public static String PermissionsGroup_Writable;
}
