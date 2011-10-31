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
 * Verify listener for text widgets to receive floating point numbers.
 */
public class DoubleNumberVerifyListener extends RegexVerifyListener {

	/**
	 * Constructor.
	 */
	public DoubleNumberVerifyListener() {
		super(NO_ATTR, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.RegexVerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
	 */
	@Override
	public void verifyText(VerifyEvent e) {
		super.verifyText(e);
		String fullText = getFullText(e);
		if (e.doit && fullText != null && fullText.length() > 0) {
			// append dummy digit to make it possible to enter '.' and 'e' at all
			if (fullText.endsWith("-") || fullText.endsWith("+") || //$NON-NLS-1$ //$NON-NLS-2$
					fullText.endsWith(".") || fullText.endsWith("e") || fullText.endsWith("E")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				fullText += "0"; //$NON-NLS-1$
			}
			try {
				Double.parseDouble(fullText);
			}
			catch (Exception ex) {
				e.doit = false;
			}
		}
	}
}
