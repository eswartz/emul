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
 * Verify listener for text widgets to receive port numbers.
 */
public class PortNumberVerifyListener extends NumberVerifyListener {

	// regular expression for hex validation
	protected static final String PORT_REGEX_DEC = "([0-9]{0,5})"; //$NON-NLS-1$
	protected static final String PORT_REGEX_HEX = "(0((x|X)[0-9a-fA-F]{0,4})?)"; //$NON-NLS-1$

	// arguments
	public static final int ATTR_DECIMAL = 1;
	public static final int ATTR_HEX = 2;

	/**
	 * Constructor
	 * @param decimal true if decimal value is allowed
	 * @param hex true if hex value (0x####) is allowed
	 */
	public PortNumberVerifyListener(int attributes) {
		super(attributes, getRegEx(attributes), 0, 65535);
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
