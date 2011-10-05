/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River) - [345384] Provide property pages for remote file system nodes
 *******************************************************************************/
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

	public static String CacheManager_Bytes;
	public static String CacheManager_DowloadingFile;
	public static String CacheManager_DownloadingError;
	public static String CacheManager_DownloadingProgress;
	public static String CacheManager_KBs;
	public static String CacheManager_MBs;
	public static String CacheManager_UploadingProgress;
	public static String CacheManager_UploadNFiles;
	public static String CacheManager_UploadSingleFile;
	public static String CmmitHandler_Cancel;
	public static String CmmitHandler_CommitAnyway;
	public static String CmmitHandler_ErrorTitle;
	public static String CmmitHandler_FileDeleted;
	public static String CmmitHandler_Merge;
	public static String CmmitHandler_StateChangedDialogTitle;
	public static String CmmitHandler_StateChangedMessage;
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
	public static String LocalTypedElement_SavingFile;

	public static String OpenFileHandler_Cancel;
	public static String OpenFileHandler_ConflictingMessage;
	public static String OpenFileHandler_ConflictingTitle;
	public static String OpenFileHandler_Merge;
	public static String OpenFileHandler_OpenAnyway;
	public static String OpenFileHandler_OpeningBinaryNotSupported;
	public static String OpenFileHandler_Warning;
	public static String PermissionsGroup_Executable;
	public static String PermissionsGroup_GroupPermissions;
	public static String PermissionsGroup_OtherPermissions;
	public static String PermissionsGroup_Readable;
	public static String PermissionsGroup_UserPermissions;
	public static String PermissionsGroup_Writable;
	public static String RemoteTypedElement_GettingRemoteContent;
	public static String SaveAllListener_Cancel;
	public static String SaveAllListener_Merge;
	public static String SaveAllListener_SaveAnyway;
	public static String SaveAllListener_SingularMessage;
	public static String SaveAllListener_StateChangedDialogTitle;
	public static String SaveListener_Cancel;
	public static String SaveListener_Merge;
	public static String SaveListener_SaveAnyway;
	public static String SaveListener_StateChangedDialogTitle;
	public static String SaveListener_StateChangedMessage;
	public static String StateManager_CannotGetFileStateMessage2;
	public static String StateManager_CannotGetFileStatMessage;
	public static String StateManager_CannotSetFileStateMessage;
	public static String StateManager_CannotSetFileStateMessage2;
	public static String StateManager_CommitFailureTitle;
	public static String StateManager_RefreshFailureTitle;
	public static String StateManager_TCFNotProvideFSMessage;
	public static String StateManager_TCFNotProvideFSMessage2;
	public static String StateManager_UpdateFailureTitle;
	public static String TcfInputStream_CloseTimeout;
	public static String TcfInputStream_NoDataAvailable;
	public static String TcfInputStream_NoFileReturned;
	public static String TcfInputStream_NoFSServiceAvailable;
	public static String TcfInputStream_OpenFileTimeout;
	public static String TcfInputStream_OpenTCFTimeout;
	public static String TcfInputStream_ReadTimeout;
	public static String TcfInputStream_StreamClosed;
	public static String TcfOutputStream_StreamClosed;
	public static String TcfOutputStream_WriteTimeout;
	public static String TcfURLConnection_CloseFileTimeout;
	public static String TcfURLConnection_NoFileHandleReturned;
	public static String TcfURLConnection_NoFSServiceAvailable;
	public static String TcfURLConnection_NoSuchTcfAgent;
	public static String TcfURLConnection_OpenFileTimeout;
	public static String TcfURLConnection_OpenTCFChannelTimeout;
	public static String TCFUtilities_OpeningFailureMessage;
	public static String TCFUtilities_OpeningFailureTitle;
	public static String UpdateHandler_Cancel;
	public static String UpdateHandler_Merge;
	public static String UpdateHandler_StateChangedDialogTitle;
	public static String UpdateHandler_StateChangedMessage;
	public static String UpdateHandler_UpdateAnyway;
	public static String UserManager_CannotGetUserAccountMessage;
	public static String UserManager_CannotGetUserAccountMessage2;
	public static String UserManager_TCFNotProvideFSMessage;
	public static String UserManager_UserAccountTitle;
}
