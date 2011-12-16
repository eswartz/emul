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
 * Validator using regular expression.
 */
public class RegexValidator extends Validator {

	// keys for info messages
	public static final String INFO_MISSING_VALUE = "RegexValidator_Information_MissingValue"; //$NON-NLS-1$

	// keys for error messages
	public static final String ERROR_INVALID_VALUE = "RegexValidator_Error_InvalidValue"; //$NON-NLS-1$

	// arguments
	private String regex;

	/**
	 * Constructor.
	 * @param attributes
	 */
	public RegexValidator(int attributes, String regex) {
		super(attributes);
		this.regex = regex;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.Validator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String newText) {
		init();

		// info message when value is empty and mandatory
		if (newText == null || newText.trim().length() == 0) {
			if (isAttribute(ATTR_MANDATORY)) {
				setMessage(getMessageText(INFO_MISSING_VALUE), getMessageTextType(INFO_MISSING_VALUE, INFORMATION));
				return false;
			}
			return true;
		}

		if (!newText.matches(regex)) {
			setMessage(getMessageText(ERROR_INVALID_VALUE), getMessageTextType(ERROR_INVALID_VALUE, ERROR));
			return getMessageType() != ERROR;
		}

		return true;
	}

	/**
	 * Returns the regular expression.
	 * @return
	 */
	protected String getRegularExpression() {
		return regex;
	}

	/**
	 * Set the regular expression.
	 * @param regex
	 */
	protected void setRegularExpression(String regex) {
		if (regex != null && regex.length() > 0) {
			this.regex = regex;
		}
		else {
			this.regex = ".*"; //$NON-NLS-1$
		}
	}
}
