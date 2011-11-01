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
 * Verify listener for text widgets to receive host names or IP addresses.
 */
public class NameOrIPVerifyListener extends RegexVerifyListener {

	// regular expressions
	// characters that can be set at the beginning
	private static final String NAME_START_REGEX = "[a-zA-Z]"; //$NON-NLS-1$
	// characters that can be set after the starting character
	private static final String NAME_FOLLOW_REGEX = "[a-zA-Z0-9]"; //$NON-NLS-1$
	// allowed separators in fragments
	private static final String NAME_SEPERATOR_REGEX = "(\\-|_)"; //$NON-NLS-1$
	// complete name fragment
	public static final String NAME_FRAGMENT_REGEX =
		"(" + NAME_START_REGEX + NAME_FOLLOW_REGEX + "*" +  //$NON-NLS-1$ //$NON-NLS-2$
		"(" + NAME_SEPERATOR_REGEX + NAME_FOLLOW_REGEX + "+)*)"; //$NON-NLS-1$ //$NON-NLS-2$
	// open name fragment during typing
	private static final String OPEN_NAME_FRAGMENT_REGEX =
		"(" + NAME_FRAGMENT_REGEX + NAME_SEPERATOR_REGEX + "?)"; //$NON-NLS-1$ //$NON-NLS-2$
	// multiple
	public static final String OPEN_NAME_REGEX =
		"((" + NAME_FRAGMENT_REGEX + "\\.)*" + OPEN_NAME_FRAGMENT_REGEX + "?)?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static final String IP_FRAGMENT_REGEX = "([0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])"; //$NON-NLS-1$
	public static final String OPEN_IP_REGEX = "((" + IP_FRAGMENT_REGEX + "?\\.){0,3}" + IP_FRAGMENT_REGEX + "?)?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	public static final String OPEN_TIPC_REGEX = "((" + IP_FRAGMENT_REGEX + "?\\.){0,2}" + IP_FRAGMENT_REGEX + "?)?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// arguments
	public static final int ATTR_NAME = 1;
	public static final int ATTR_IP = 2;
	public static final int ATTR_TIPC = 4;

	/**
	 * Constructor
	 * @param decimal true if decimal value is allowed
	 * @param hex true if hex value (0x####) is allowed
	 */
	public NameOrIPVerifyListener(int attributes) {
		super(attributes, getRegEx(attributes));
	}

	/*
	 * Static method to generate regular expression for constructor super call.
	 */
	private static String getRegEx(int attributes) {
		String regex = null;

		if (isAttribute(ATTR_TIPC, attributes)) {
			regex = OPEN_TIPC_REGEX;
		}
		else {
			if (isAttribute(ATTR_NAME, attributes) || !isAttribute(ATTR_IP, attributes)) {
				regex = OPEN_NAME_REGEX;
			}
			if (isAttribute(ATTR_IP, attributes) || !isAttribute(ATTR_NAME, attributes)) {
				regex = (regex != null) ? (regex + "|" + OPEN_IP_REGEX) : OPEN_IP_REGEX;	//$NON-NLS-1$
			}
		}

		return regex;
	}
}
