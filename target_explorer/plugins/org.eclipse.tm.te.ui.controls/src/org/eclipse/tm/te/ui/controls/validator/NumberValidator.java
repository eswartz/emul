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

import java.text.MessageFormat;

/**
 * Positive integer number validator for text widgets.
 */
public class NumberValidator extends RegexValidator {

	// keys for error messages
	public static final String ERROR_INVALID_RANGE = "NumberValidator_Error_InvalidRange"; //$NON-NLS-1$

	// regular expressions
	protected static final String NUMBER_REGEX = "([0-9]*)"; //$NON-NLS-1$

	private int min = 0;
	private int max = Integer.MAX_VALUE;

	/**
	 * Constructor
	 */
	public NumberValidator() {
		this(-1, -1);
	}

	/**
	 * Constructor
	 *
	 * @param min The lower boundary of the allowed input range.
	 * 			  If less than zero the value is set to 0.
	 * @param max The upper boundary of the allowed input range.
	 * 			  If less than zero the value is set to INTEGER.MAX_VALUE.
	 */
	public NumberValidator(int min, int max) {
		this(NO_ATTR, NUMBER_REGEX, min, max);
	}

	/**
	 * Constructor
	 *
	 * @param attributes The verify listener attributes.
	 * @param regEx The regular expression.
	 * @param min The minimum allowed input.
	 * 			  If less than zero the value is set to 0.
	 * @param max The maximum allowed input.
	 * 			  If less than zero the value is set to INTEGER.MAX_VALUE.
	 */
	public NumberValidator(int attributes, String regEx, int min, int max) {
		super(attributes, regEx);
		setBounds(min, max);
	}

	/**
	 * Sets the lower and upper value boundary.
	 *
	 * @param min The minimum allowed input.
	 * 			  If less than zero the value is set to 0.
	 * @param max The maximum allowed input.
	 * 			  If less than zero the value is set to INTEGER.MAX_VALUE.
	 */
	public void setBounds(int min, int max) {
		min = (min >= 0) ? min : 0;
		max = (max >= 0) ? max : Integer.MAX_VALUE;
		this.min = Math.min(min, max);
		this.max = Math.max(min, max);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.RegexValidator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String newText) {
		boolean valid = super.isValid(newText);

		// If the value is a valid number matching the regex, check if
		// the range is valid
		if (valid) {
			try {
				// Decode the string into an integer object
				int value = Integer.decode(newText).intValue();
				if (value < min || value > max) {
					// Value is out of range -> set error message
					setMessage(MessageFormat.format(getMessageText(ERROR_INVALID_RANGE), Integer.valueOf(min), Integer.valueOf(max)), getMessageTextType(ERROR_INVALID_RANGE, ERROR));
					valid = getMessageType() != ERROR;
				}
			} catch (NumberFormatException e) { /* ignored on purpose */ }
		}

		return valid;
	}
}
