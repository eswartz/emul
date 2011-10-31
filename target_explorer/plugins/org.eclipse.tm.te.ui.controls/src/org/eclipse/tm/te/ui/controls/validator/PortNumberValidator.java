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
 * Input validator for port numbers.
 * <p>
 * Valid formats are decimal and, if allowed, hex (0xFFFF).
 */
public class PortNumberValidator extends NumberValidator {

	// regular expressions for validator
	protected static final String PORT_REGEX_DEC = "([0-9]{1,5})"; //$NON-NLS-1$
	protected static final String PORT_REGEX_HEX = "(0(x|X)[0-9a-fA-F]{1,4})"; //$NON-NLS-1$

	public static final int ATTR_HEX = 2;
	public static final int ATTR_DECIMAL = 4;
	// next attribute should start with 2^3

	/**
	 * Constructor
	 */
	public PortNumberValidator() {
		this(ATTR_MANDATORY | ATTR_DECIMAL);
	}

	/**
	 * Constructor
	 * @attributes attributes The validator attributes.
	 */
	public PortNumberValidator(int attributes) {
		this(attributes, 0, 65535);
	}

	/**
	 * Constructor
	 *
	 * @param attributes The verify listener attributes.
	 * @param min The minimum allowed input.
	 * 			  If less than zero the value is set to 0.
	 * @param max The maximum allowed input.
	 * 			  If less than zero the value is set to INTEGERE.MAX_VALUE.
	 */
	public PortNumberValidator(int attributes, int min, int max) {
		super(attributes, getRegEx(attributes), min, max);

		setMessageText(RegexValidator.INFO_MISSING_VALUE, getString("PortNumberValidator_Information_MissingPortNumber")); //$NON-NLS-1$
		setMessageText(RegexValidator.ERROR_INVALID_VALUE, getString("PortNumberValidator_Error_InvalidPortNumber")); //$NON-NLS-1$
		setMessageText(NumberValidator.ERROR_INVALID_RANGE, getString("PortNumberValidator_Error_PortNumberNotInRange")); //$NON-NLS-1$
	}

	/*
	 * Static method to generate regular expression for constructor super call.
	 */
	private static String getRegEx(int attributes) {
		String regex = null;

		if (isAttribute(ATTR_DECIMAL, attributes) || !isAttribute(ATTR_HEX, attributes)) {
			regex = PORT_REGEX_DEC;
		}
		if (isAttribute(ATTR_HEX, attributes) || !isAttribute(ATTR_DECIMAL, attributes)) {
			regex = (regex != null) ? (regex + "|" + PORT_REGEX_HEX) : PORT_REGEX_HEX;	//$NON-NLS-1$
		}

		return regex;
	}
}
