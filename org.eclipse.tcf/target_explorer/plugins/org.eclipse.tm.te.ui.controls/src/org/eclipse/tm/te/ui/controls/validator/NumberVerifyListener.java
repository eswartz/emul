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

import org.eclipse.swt.events.VerifyEvent;

/**
 * Verify listener for text widgets to receive numbers.
 */
public class NumberVerifyListener extends RegexVerifyListener {

	// regular expressions
	protected static final String NUMBER_REGEX = "([0-9]*)"; //$NON-NLS-1$

	private int min = 0;
	private int max = Integer.MAX_VALUE;

	/**
	 * Constructor
	 */
	public NumberVerifyListener() {
		this(-1, -1);
	}

	/**
	 * Constructor
	 *
	 * @param min The minimum allowed input.
	 * 			  If less than zero the value is set to 0.
	 * @param max The maximum allowed input.
	 * 			  If less than zero the value is set to INTEGERE.MAX_VALUE.
	 */
	public NumberVerifyListener(int min, int max) {
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
	 * 			  If less than zero the value is set to INTEGERE.MAX_VALUE.
	 */
	public NumberVerifyListener(int attributes, String regEx, int min, int max) {
		super(attributes, regEx);
		min = (min >= 0) ? min : 0;
		max = (max >= 0) ? max : Integer.MAX_VALUE;
		this.min = Math.min(min, max);
		this.max = Math.max(min, max);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.RegexVerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
	 */
	@Override
	public void verifyText(VerifyEvent e) {
		super.verifyText(e);
		String fullText = getFullText(e);
		if (e.doit && fullText != null && fullText.length() > 0 && !fullText.equalsIgnoreCase("0x")) { //$NON-NLS-1$
			try {
				int value = Integer.decode(fullText).intValue();
				if (value < min || value > max) {
					e.doit = false;
				}
			}
			catch (Exception ex) {
				e.doit = false;
			}
		}
	}
}
