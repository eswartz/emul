/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls.validator;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


/**
 * Input validator for files or directories.
 */
public class DirectoryNameValidator extends Validator {

	// keys for info messages file
	public static final String INFO_MISSING_DIR_NAME = "DirectoryNameValidator_Information_MissingName"; //$NON-NLS-1$

	// keys for error messages directory
	public static final String ERROR_IS_FILE = "DirectoryNameValidator_Error_IsFile"; //$NON-NLS-1$
	public static final String ERROR_MUST_EXIST = "DirectoryNameValidator_Error_MustExist"; //$NON-NLS-1$
	public static final String ERROR_READ_ONLY = "DirectoryNameValidator_Error_ReadOnly"; //$NON-NLS-1$
	public static final String ERROR_NO_ACCESS = "DirectoryNameValidator_Error_NoAccess"; //$NON-NLS-1$
	public static final String ERROR_IS_RELATIV = "DirectoryNameValidator_Error_IsRelativ"; //$NON-NLS-1$
	public static final String ERROR_IS_ABSOLUT = "DirectoryNameValidator_Error_IsAbsolut"; //$NON-NLS-1$

	// arguments
	public static final int ATTR_MUST_EXIST = 2;
	public static final int ATTR_CAN_READ = 4;
	public static final int ATTR_CAN_WRITE = 8;
	// if both attributes not set accept relative and absolute directories
	public static final int ATTR_ABSOLUT = 16;
	public static final int ATTR_RELATIV = 32;
	/**
	 * Set this attribute if the path to validate is not on a local file system.
	 * Setting this attribute does unset ATTR_MUST_EXIST, ATTR_CAN_READ and ATTR_CAN_WRITE.
	 */
	public static final int ATTR_REMOTE_PATH = 64;
	// next attribute should start with 2^7

	// value attributes
	private boolean isDir;
	private boolean exists;
	private boolean canRead;
	private boolean canWrite;
	private boolean absolute;

	/**
	 * Constructor
	 * @attributes attributes The validator attributes.
	 */
	public DirectoryNameValidator(int attributes) {
		super(attributes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.Validator#init()
	 */
	@Override
	protected void init() {
		super.init();
		isDir = false;
		exists = false;
		canRead = false;
		canWrite = false;
		absolute = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.Validator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String newFile) {
		init();

		// info message when value is empty and mandatory
		if (newFile == null || newFile.trim().length() == 0) {
			if (isAttribute(ATTR_MANDATORY)) {
				setMessage(getMessageText(INFO_MISSING_DIR_NAME), getMessageTextType(INFO_MISSING_DIR_NAME, INFORMATION));
				return false;
			}
			return true;
		}

		newFile	= newFile.trim();

		IPath path = new Path(newFile);
		File dir = path.toFile();

		// If the path is an remote path, use the IPath object to validate
		// if or if not it is an absolute path
		absolute = isAttribute(ATTR_REMOTE_PATH) ? path.isAbsolute() : dir.isAbsolute();

		// Validate all other attributes via the File object
		exists = dir.exists();
		isDir = dir.isDirectory() || !exists;

		// To determine canRead and canWrite, we have to find the first existing parent directory
		File parentDir = dir.getParentFile();
		while (parentDir != null && !parentDir.exists()) {
			parentDir = parentDir.getParentFile();
		}

		canRead = dir.canRead() || (!exists && parentDir != null && parentDir.canWrite());
		canWrite = dir.canWrite() || (!exists && parentDir != null && parentDir.canWrite());

		if (!isAttribute(ATTR_REMOTE_PATH) && isAttribute(ATTR_MUST_EXIST) && !exists) {
			setMessage(getMessageText(ERROR_MUST_EXIST), getMessageTextType(ERROR_MUST_EXIST, ERROR));
		}
		else if (isAttribute(ATTR_ABSOLUT) && !isAttribute(ATTR_RELATIV) && !absolute) {
			setMessage(getMessageText(ERROR_IS_RELATIV), getMessageTextType(ERROR_IS_RELATIV, ERROR));
		}
		else if (isAttribute(ATTR_RELATIV) && !isAttribute(ATTR_ABSOLUT) && absolute) {
			setMessage(getMessageText(ERROR_IS_ABSOLUT), getMessageTextType(ERROR_IS_ABSOLUT, ERROR));
		}
		else if (exists && !isDir) {
			setMessage(getMessageText(ERROR_IS_FILE), getMessageTextType(ERROR_IS_FILE, ERROR));
		}
		else if (!isAttribute(ATTR_REMOTE_PATH) && isAttribute(ATTR_CAN_READ) && !canRead) {
			setMessage(getMessageText(ERROR_NO_ACCESS), getMessageTextType(ERROR_NO_ACCESS, ERROR));
		}
		else if (!isAttribute(ATTR_REMOTE_PATH) && isAttribute(ATTR_CAN_WRITE) && !canWrite) {
			setMessage(getMessageText(ERROR_READ_ONLY), getMessageTextType(ERROR_READ_ONLY, ERROR));
		}

		return getMessageType() != ERROR;
	}

	/**
	 * Validated value is a directory.
	 * @return
	 */
	public boolean isDirectory() {
		return isDir;
	}

	/**
	 * Validated directory exists.
	 * @return
	 */
	public boolean exists() {
		return exists;
	}

	/**
	 * Validated directory can be read.
	 * @return
	 */
	public boolean canRead() {
		return canRead;
	}

	/**
	 * Validated directory be written.
	 * @return
	 */
	public boolean canWrite() {
		return canWrite;
	}

	/**
	 * Validated directory path is absolute.
	 * @return
	 */
	public boolean isAbsolute() {
		return absolute;
	}

	/**
	 * Validated directory path is relative.
	 * @return
	 */
	public boolean isRelative() {
		return !absolute;
	}
}
