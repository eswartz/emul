/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls.net;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl;
import org.eclipse.tm.te.ui.controls.nls.Messages;
import org.eclipse.tm.te.ui.controls.validator.PortNumberValidator;
import org.eclipse.tm.te.ui.controls.validator.PortNumberVerifyListener;
import org.eclipse.tm.te.ui.controls.validator.Validator;

/**
 * Basic remote host port control.
 */
public class RemoteHostPortControl extends BaseEditBrowseTextControl {

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent dialog page this control is embedded in.
	 *                   Might be <code>null</code> if the control is not associated with a page.
	 */
	public RemoteHostPortControl(IDialogPage parentPage) {
		super(parentPage);
		setIsGroup(false);
		setHasHistory(false);
		setHideBrowseButton(true);
		setEditFieldLabel(Messages.RemoteHostPortControl_label);
		setAdjustBackgroundColor(parentPage != null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#doCreateEditFieldValidator()
	 */
	@Override
	protected Validator doCreateEditFieldValidator() {
		return new PortNumberValidator(PortNumberValidator.ATTR_DECIMAL | PortNumberValidator.ATTR_HEX);
	}

	private VerifyListener verifyListener;

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#doGetEditFieldControlVerifyListener()
	 */
	@Override
	protected VerifyListener doGetEditFieldControlVerifyListener() {
		if (verifyListener == null) {
			verifyListener =
				new PortNumberVerifyListener(PortNumberVerifyListener.ATTR_DECIMAL | PortNumberVerifyListener.ATTR_HEX);
		}
		return verifyListener;
	}

}
