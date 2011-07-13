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

import java.math.BigInteger;

/**
 * Target Explorer: Validator for hex values.
 */
public class HexValidator extends RegexValidator {

	/**
	 * Allows entering decimal numbers.
	 */
	public static final int ATTR_ALLOW_DECIMAL = 2;

	/**
	 * Enables negative decimal numbers. Has effect only if {@link #ATTR_ALLOW_DECIMAL}
	 * is set as well.
	 */
	public static final int ATTR_ALLOW_NEGATIVE_DECIMAL = 4;

	/**
	 * Enables negative hexadecimal numbers.
	 */
	public static final int ATTR_ALLOW_NEGATIVE_HEX = 8;
	// next attribute should start with 2^4

	// keys for error messages
	public static final String ERROR_INVALID_VALUE_RANGE = "HexValidator_Error_InvalidValueRange"; //$NON-NLS-1$

	// regular expressions
	protected static final String HEX_REGEX = "@NEGATIVE@(0(x|X)[0-9a-fA-F]{@BYTES_MIN@,@BYTES_MAX@})|0"; //$NON-NLS-1$
	protected static final String NUMBER_REGEX = "@NEGATIVE@([0-9]*)"; //$NON-NLS-1$

	private int minBytes = 0;
	private int maxBytes = 8;

	private boolean isHex = false;
	private boolean isDecimal = false;

	/**
	 * Constructor.
	 * @param attributes
	 */
	public HexValidator(int attributes, int minBytes, int maxBytes) {
		super(attributes, getRegEx(attributes, minBytes, maxBytes));

		this.minBytes = minBytes;
		this.maxBytes = maxBytes;
	}

	/**
	 * Constructor.
	 * @param attributes
	 */
	public HexValidator(int attributes, int bytes) {
		super(attributes, getRegEx(attributes, bytes, bytes));

		this.minBytes = bytes;
		this.maxBytes = bytes;
	}

	/*
	 * Static method to generate regular expression for constructor super call.
	 */
	private static String getRegEx(int attributes, int minBytes, int maxBytes) {
		String regex = (isAttribute(ATTR_ALLOW_DECIMAL, attributes)
										? NUMBER_REGEX.replaceAll("@NEGATIVE@", isAttribute(ATTR_ALLOW_NEGATIVE_DECIMAL, attributes) ? "-?" : "") + "|"   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
										: "") + HEX_REGEX;  //$NON-NLS-1$
		regex = regex.replaceAll("@NEGATIVE@", isAttribute(ATTR_ALLOW_NEGATIVE_HEX, attributes) ? "-?" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		regex = regex.replaceAll("@BYTES_MIN@", "" + minBytes); //$NON-NLS-1$ //$NON-NLS-2$
		regex = regex.replaceAll("@BYTES_MAX@", "" + maxBytes); //$NON-NLS-1$ //$NON-NLS-2$
		return regex;
	}

	/**
	 * Adjust the values for minimum bytes and maximum byte allowed.
	 *
	 * @param minBytes The minimum number of bytes. Must be non-negative.
	 * @param maxBytes The maximum number of bytes. Must be non-negative.
	 */
	public void setBounds(int minBytes, int maxBytes) {
		assert minBytes >= 0 && maxBytes >= 0 && minBytes <= maxBytes;
		setRegularExpression(getRegEx(getAttributes(), minBytes, maxBytes));
		this.minBytes = minBytes;
		this.maxBytes = maxBytes;
	}

	/**
	 * Returns if the validator is currently working in hex mode.
	 *
	 * @return <code>true</code> if in hex mode, <code>false</code> otherwise.
	 */
	public boolean isHex() {
		return isHex;
	}

	/**
	 * Returns if the validator is currently working in decimal mode.
	 *
	 * @return <code>true</code> if in decimal mode, <code>false</code> otherwise.
	 */
	public boolean isDecimal() {
		return isDecimal;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.RegexValidator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String newText) {
		isHex = false;
		isDecimal = false;

		boolean valid = super.isValid(newText);

		if (valid) {
			String hexRegex = HEX_REGEX;
			hexRegex = hexRegex.replaceAll("@NEGATIVE@", isAttribute(ATTR_ALLOW_NEGATIVE_HEX) ? "-?" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			hexRegex = hexRegex.replaceAll("@BYTES_MIN@", "" + minBytes); //$NON-NLS-1$ //$NON-NLS-2$
			hexRegex = hexRegex.replaceAll("@BYTES_MAX@", "" + maxBytes); //$NON-NLS-1$ //$NON-NLS-2$

			String numberRegex = NUMBER_REGEX;
			numberRegex = numberRegex.replaceAll("@NEGATIVE@", isAttribute(ATTR_ALLOW_NEGATIVE_DECIMAL) ? "-?" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			isHex = newText.matches(hexRegex);
			isDecimal = newText.matches(numberRegex);
		}

		if (valid && isAttribute(ATTR_ALLOW_DECIMAL) && isDecimal()) {
			BigInteger min = minBytes > 1 ? BigInteger.valueOf(16).pow(minBytes-1) : BigInteger.ZERO;
			BigInteger max = BigInteger.valueOf(16).pow(maxBytes);

			BigInteger value = !"".equals(newText) ? decode(newText) : BigInteger.ZERO; //$NON-NLS-1$
			if (value == null || value.abs().compareTo(min) < 0 || value.abs().compareTo(max) > 0) {
				setMessage(getMessageText(ERROR_INVALID_VALUE_RANGE), getMessageTextType(ERROR_INVALID_VALUE_RANGE, ERROR));
				valid = getMessageType() != ERROR;
			}
		}

		return valid;
	}

	/**
	 * Decodes a given string into a <code>BigInteger</code> representation.
	 *
	 * @param value The value to decode. Must not be <code>null</code>!
	 * @return The big integer representation or <code>null</code> if the decoding failed.
	 */
	public final static BigInteger decode(String value) {
		assert value != null;
		BigInteger result = null;
		if (value != null) {
			try {
				if (value.trim().toUpperCase().startsWith("0X")) { //$NON-NLS-1$
					// we have to cut away the leading 0x.
					result = new BigInteger(value.substring(2), 16);
				} else {
					result = new BigInteger(value, 10);
				}
			} catch (NumberFormatException e) { /* ignored on purpose */
			}
		}

		return result;
	}
}
