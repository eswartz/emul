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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Input validator for files.
 */
public class FileNameValidator extends Validator {

	// keys for info messages file
	public static final String INFO_MISSING_FILE_NAME = "FileNameValidator_Information_MissingName"; //$NON-NLS-1$

	// keys for error messages file
	public static final String ERROR_INVALID_FILE_NAME = "FileNameValidator_Error_InvalidName"; //$NON-NLS-1$
	public static final String ERROR_IS_DIRECTORY = "FileNameValidator_Error_IsDirectory"; //$NON-NLS-1$
	public static final String ERROR_MUST_EXIST = "FileNameValidator_Error_MustExist"; //$NON-NLS-1$
	public static final String ERROR_READ_ONLY = "FileNameValidator_Error_ReadOnly"; //$NON-NLS-1$
	public static final String ERROR_NO_ACCESS = "FileNameValidator_Error_NoAccess"; //$NON-NLS-1$
	public static final String ERROR_IS_RELATIV = "FileNameValidator_Error_IsRelativ"; //$NON-NLS-1$
	public static final String ERROR_IS_ABSOLUT = "FileNameValidator_Error_IsAbsolut"; //$NON-NLS-1$
	public static final String ERROR_HAS_SPACES = "FileNameValidator_Error_HasSpaces"; //$NON-NLS-1$

	// arguments
	private List<String> fileExtensions;

	public static final int ATTR_MUST_EXIST = 2;
	public static final int ATTR_CAN_READ = 4;
	public static final int ATTR_CAN_WRITE = 8;
	// if both attributes not set accept relative and absolute files
	public static final int ATTR_ABSOLUT = 16;
	public static final int ATTR_RELATIV = 32;
	public static final int ATTR_NO_SPACES = 64;
	// next attribute should start with 2^7

	// value attributes
	private boolean isFile;
	private boolean exists;
	private boolean canRead;
	private boolean canWrite;
	private boolean absolute;
	private boolean spaces;

	/**
	 * Constructor
	 * @param attributes The validator attributes.
	 */
	public FileNameValidator(int attributes) {
		super(attributes);
	}

	/**
	 * Constructor
	 * @param attributes The validator attributes.
	 */
	public FileNameValidator(int attributes, String[] fileExtensions) {
		super(attributes);
		setFileExtensions(fileExtensions);
	}

	/**
	 * Set the valid file extensions for attribute ATTR_FILE.
	 * @param fileExtensions The valid file extensions.
	 */
	public void setFileExtensions(String[] fileExtensions) {
		this.fileExtensions = Arrays.asList(fileExtensions);
	}

	/**
	 * Return the valid file extensions for attribute ATTR_FILE.
	 * @return
	 */
	public String[] getFileExtensions() {
		if (fileExtensions != null) {
			return fileExtensions.toArray(new String[fileExtensions.size()]);
		}
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.Validator#init()
	 */
	@Override
	protected void init() {
		super.init();
		isFile = false;
		exists = false;
		canRead = false;
		canWrite = false;
		absolute = false;
		spaces = false;
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
				setMessage(getMessageText(INFO_MISSING_FILE_NAME), getMessageTextType(INFO_MISSING_FILE_NAME, INFORMATION));
				return false;
			}
			return true;
		}

		newFile	= newFile.trim();
		File file = new File(newFile);
		exists = file.exists();
		isFile = file.isFile() || !exists;
		absolute = file.isAbsolute();

		// To determine canRead and canWrite, we have to find the first existing parent directory
		File parentFile = file.getParentFile();
		while (parentFile != null && !parentFile.exists()) {
			parentFile = parentFile.getParentFile();
		}

		canRead = file.canRead() || (!exists && parentFile != null && parentFile.canRead());
		// *** Note: ***
		//           canWrite() may return false on some special folders on Windows like "My Documents".
		//           This is bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4939819.
		canWrite = file.canWrite() || (!exists && parentFile != null && parentFile.canWrite());
		spaces = file.toString().trim().indexOf(' ') > 0;

		// Highest priority is MUST_EXIST
		if (isAttribute(ATTR_MUST_EXIST) && !exists) {
			setMessage(getMessageText(ERROR_MUST_EXIST), getMessageTextType(ERROR_MUST_EXIST, ERROR));
		}
		// Second test on spaces. If disallowed, the user should be told despite the other conditions
		else if (isAttribute(ATTR_NO_SPACES) && spaces) {
			setMessage(getMessageText(ERROR_HAS_SPACES), getMessageTextType(ERROR_HAS_SPACES, ERROR));
		}
		// Third: all the rest
		else if (isAttribute(ATTR_ABSOLUT) && !isAttribute(ATTR_RELATIV) && !absolute) {
			setMessage(getMessageText(ERROR_IS_RELATIV), getMessageTextType(ERROR_IS_RELATIV, ERROR));
		}
		else if (isAttribute(ATTR_RELATIV) && !isAttribute(ATTR_ABSOLUT) && absolute) {
			setMessage(getMessageText(ERROR_IS_ABSOLUT), getMessageTextType(ERROR_IS_ABSOLUT, ERROR));
		}
		else if (exists && !isFile) {
			setMessage(getMessageText(ERROR_IS_DIRECTORY), getMessageTextType(ERROR_IS_DIRECTORY, ERROR));
		}
		else if (isFile && !hasValidExtension(newFile)) {
			setMessage(getMessageText(ERROR_INVALID_FILE_NAME), getMessageTextType(ERROR_INVALID_FILE_NAME, ERROR));
		}
		else if (isAttribute(ATTR_CAN_READ) && !canRead) {
			setMessage(getMessageText(ERROR_NO_ACCESS), getMessageTextType(ERROR_NO_ACCESS, ERROR));
		}
		else if (isAttribute(ATTR_CAN_WRITE) && !canWrite) {
			setMessage(getMessageText(ERROR_READ_ONLY), getMessageTextType(ERROR_READ_ONLY, ERROR));
		}

		return getMessageType() != ERROR;
	}

	private boolean hasValidExtension(String newFile) {
		if (fileExtensions != null && fileExtensions.size() > 0) {
			Iterator<String> i = fileExtensions.iterator();
			while (i.hasNext()) {
				String ex = i.next().toLowerCase();
				if (newFile.toLowerCase().endsWith(ex) ||
					ex.equals("*") || ex.equals("*.*") || ex.equals(".*")) {  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Validated value is a file.
	 * @return
	 */
	public boolean isFile() {
		return isFile;
	}

	/**
	 * Validated file exists.
	 * @return
	 */
	public boolean exists() {
		return exists;
	}

	/**
	 * Validated file can be read.
	 * @return
	 */
	public boolean canRead() {
		return canRead;
	}

	/**
	 * Validated file can be written.
	 * @return
	 */
	public boolean canWrite() {
		return canWrite;
	}

	/**
	 * Validated file path is absolute.
	 * @return
	 */
	public boolean isAbsolute() {
		return absolute;
	}

	/**
	 * Validated file path is relative.
	 * @return
	 */
	public boolean isRelative() {
		return !absolute;
	}
}
