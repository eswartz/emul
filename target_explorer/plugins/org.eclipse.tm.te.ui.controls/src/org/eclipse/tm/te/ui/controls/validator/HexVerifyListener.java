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

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.events.VerifyEvent;

/**
 * Target Explorer: Verify listener for text widgets to receive hex values.
 */
public class HexVerifyListener extends RegexVerifyListener {

	// arguments (binary coded)
	public static final int ATTR_TO_UPPER_CASE = 1;
	public static final int ATTR_ALLOW_DECIMAL = 2;

	/**
	 * Enables negative decimal numbers. Has effect on if {@link #ATTR_ALLOW_DECIMAL}
	 * is set as well.
	 */
	public static final int ATTR_ALLOW_NEGATIVE_DECIMAL = 4;

	/**
	 * Enables negative hexadecimal numbers.
	 */
	public static final int ATTR_ALLOW_NEGATIVE_HEX = 8;
	// next attribute should start with 2^4

	// regular expressions
	protected static final String HEX_REGEX = "@NEGATIVE@(0((x|X)[0-9a-fA-F]{0,@BYTES@})?)?"; //$NON-NLS-1$
	protected static final String NUMBER_REGEX = "@NEGATIVE@([0-9]*)"; //$NON-NLS-1$

	/**
	 * Constructor
	 * @param bytes The number of bytes (hex digits) allowed.
	 */
	public HexVerifyListener(int attributes, int bytes) {
		super(attributes, getRegEx(attributes, bytes));
	}

	/*
	 * Static method to generate regular expression for constructor super call.
	 */
	private static String getRegEx(int attributes, int bytes) {
		return (isAttribute(ATTR_ALLOW_DECIMAL, attributes)
									? NUMBER_REGEX.replaceAll("@NEGATIVE@", isAttribute(ATTR_ALLOW_NEGATIVE_DECIMAL, attributes) ? "-?" : "") + "|"    //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									: "") + HEX_REGEX.replaceAll("@BYTES@", "" + bytes) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
														.replaceAll("@NEGATIVE@", isAttribute(ATTR_ALLOW_NEGATIVE_HEX, attributes) ? "-?" : ""); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Adjust the value for maximum byte allowed.
	 *
	 * @param maxBytes The maximum number of bytes. Must be non-negative.
	 */
	public void setBounds(int maxBytes) {
		Assert.isTrue(maxBytes >= 0);
		setRegularExpression(getRegEx(getAttributes(), maxBytes));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.RegexVerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
	 */
	@Override
	public void verifyText(VerifyEvent e) {
		super.verifyText(e);
		if (e.doit && isAttribute(ATTR_TO_UPPER_CASE)) {
			e.text = e.text.toUpperCase().replace('X', 'x');
		}
	}
}
