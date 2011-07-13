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


/**
 * Target Explorer: Input validator for hostnames id IP addresses.
 */
public class NameOrIPValidator extends Validator {

	// regular expressions for validator
	private static final String IP_CHARACTERS_REGEX = "[0-9\\.]+"; //$NON-NLS-1$
	public static final String IP_REGEX = NameOrIPVerifyListener.IP_FRAGMENT_REGEX + "(\\." +  //$NON-NLS-1$
		NameOrIPVerifyListener.IP_FRAGMENT_REGEX + "){3}"; //$NON-NLS-1$

	// RFC 1034 - ftp://ftp.rfc-editor.org/in-notes/std/std13.txt
	private static final String NAME_CHARACTERS_REGEX = "[0-9a-zA-Z\\-_\\.]+"; //$NON-NLS-1$
	public static final String NAME_REGEX = NameOrIPVerifyListener.NAME_FRAGMENT_REGEX + "(\\." +  //$NON-NLS-1$
		NameOrIPVerifyListener.NAME_FRAGMENT_REGEX + ")*"; //$NON-NLS-1$

	// keys for info messages
	public static final String INFO_MISSING_NAME_OR_IP = "NameOrIPValidator_Information_MissingNameOrIP"; //$NON-NLS-1$
	public static final String INFO_MISSING_NAME = "NameOrIPValidator_Information_MissingName"; //$NON-NLS-1$
	public static final String INFO_MISSING_IP = "NameOrIPValidator_Information_MissingIP"; //$NON-NLS-1$
	public static final String INFO_CHECK_NAME = "NameOrIPValidator_Information_CheckName"; //$NON-NLS-1$

	// keys for error messages
	public static final String ERROR_INVALID_NAME_OR_IP = "NameOrIPValidator_Error.InvalidNameOrIP"; //$NON-NLS-1$
	public static final String ERROR_INVALID_IP = "NameOrIPValidator_Error_InvalidIP"; //$NON-NLS-1$
	public static final String ERROR_INVALID_NAME = "NameOrIPValidator_Error_InvalidName"; //$NON-NLS-1$

	// arguments
	public static final int ATTR_NAME = 2;
	public static final int ATTR_IP = 4;
	public static final int ATTR_CHECK_AVAILABLE = 8;
	// next attribute should start with 2^4


	// value attributes
	private boolean isIP;
	private boolean isName;

	/**
	 * Constructor
	 * @attributes attributes The validator attributes.
	 */
	public NameOrIPValidator(int attributes) {
		super(attributes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.Validator#init()
	 */
	@Override
	protected void init() {
		super.init();
		isIP = false;
		isName = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.Validator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String ipOrHostName) {
		init();

		// info message when value is empty
		if (ipOrHostName == null || ipOrHostName.trim().length() == 0) {
			if (isAttribute(ATTR_MANDATORY)) {
				if (isAttribute(ATTR_IP) && !isAttribute(ATTR_NAME)) {
					setMessage(getMessageText(INFO_MISSING_IP), getMessageTextType(INFO_MISSING_IP, INFORMATION));
				}
				else if (isAttribute(ATTR_NAME) && !isAttribute(ATTR_IP)) {
					setMessage(getMessageText(INFO_MISSING_NAME), getMessageTextType(INFO_MISSING_NAME, INFORMATION));
				}
				else {
					setMessage(getMessageText(INFO_MISSING_NAME_OR_IP), getMessageTextType(INFO_MISSING_NAME_OR_IP, INFORMATION));
				}
				return false;
			}
			return true;
		}
		ipOrHostName = ipOrHostName.trim();
		// check IP address when only numeric values and '.' are entered
		if (ipOrHostName.matches(IP_CHARACTERS_REGEX)) {
			isIP = true;
			// error message when IP not correct
			if (!isAttribute(ATTR_IP) && isAttribute(ATTR_NAME)) {
				setMessage(getMessageText(ERROR_INVALID_NAME), getMessageTextType(ERROR_INVALID_NAME, ERROR));
			}
			else if (!ipOrHostName.matches(IP_REGEX)) {
				setMessage(getMessageText(ERROR_INVALID_IP), getMessageTextType(ERROR_INVALID_IP, ERROR));
			}
		}
		else if (ipOrHostName.matches(NAME_CHARACTERS_REGEX)) {
			isName = true;
			if (!isAttribute(ATTR_NAME) && isAttribute(ATTR_IP)) {
				setMessage(getMessageText(ERROR_INVALID_IP), getMessageTextType(ERROR_INVALID_IP, ERROR));
			}
			else if (!ipOrHostName.matches(NAME_REGEX)) {
				setMessage(getMessageText(ERROR_INVALID_NAME), getMessageTextType(ERROR_INVALID_NAME, ERROR));
			}
			else if (isAttribute(ATTR_CHECK_AVAILABLE)){
				// info message when name was entered to check
				setMessage(getMessageText(INFO_CHECK_NAME), getMessageTextType(INFO_CHECK_NAME, INFORMATION));
			}
		}
		else {
			if (isAttribute(ATTR_IP) && !isAttribute(ATTR_NAME)) {
				setMessage(getMessageText(ERROR_INVALID_IP), getMessageTextType(ERROR_INVALID_IP, ERROR));
			}
			else if (isAttribute(ATTR_NAME) && !isAttribute(ATTR_IP)) {
				setMessage(getMessageText(ERROR_INVALID_NAME), getMessageTextType(ERROR_INVALID_NAME, ERROR));
			}
			else {
				setMessage(getMessageText(ERROR_INVALID_NAME_OR_IP), getMessageTextType(ERROR_INVALID_NAME_OR_IP, ERROR));
			}
		}

		return getMessageType() != ERROR;
	}

	/**
	 * Validated value is IP address.
	 * @return
	 */
	public boolean isIP() {
		return isIP;
	}

	/**
	 * Validated value is alphanumeric host name.
	 * @return
	 */
	public boolean isName() {
		return isName;
	}
}
