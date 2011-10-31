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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.te.ui.controls.activator.UIPlugin;


/**
 * Verify listener using regular expression to check the input.
 */
public class RegexVerifyListener extends VerifyListener {

	private String regex;

	/**
	 * Constructor
	 * @param regex Regular expression to verify input.
	 */
	public RegexVerifyListener(int attributes, String regex) {
		super(attributes);
		setRegularExpression(regex);
	}

	/**
	 * Generates the full text to check whether the result is valid.
	 * @param e
	 * @return
	 */
	protected String getFullText(VerifyEvent e) {
		StringBuffer fulltext = new StringBuffer();
		if (e.widget instanceof Text) {
			Text text = (Text)e.widget;
			fulltext.append(text.getText());
		}
		else if (e.widget instanceof Combo) {
			Combo combo = (Combo)e.widget;
			fulltext.append(combo.getText());
		}

		try {
			if (e.end > e.start) {
				fulltext.replace(e.start, e.end, e.text);
			} else if (e.end >= 0) {
				fulltext.insert(e.end, e.text);
			}
		} catch (StringIndexOutOfBoundsException exc) {
			IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
			                            exc.getLocalizedMessage() + ", VerifyEvent(" + e.toString() + ")", exc); //$NON-NLS-1$ //$NON-NLS-2$
			UIPlugin.getDefault().getLog().log(status);
		}

		return fulltext.toString();
	}

	/**
	 * Verify the full text.
	 * The result is set in <code>e.doit</code>.
	 */
	@Override
	public void verifyText(VerifyEvent e) {
		e.doit = getFullText(e).matches(regex);
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
