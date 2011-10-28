/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.wizards.controls;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.tm.te.tcf.ui.nls.Messages;
import org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl;

/**
 * Peer name control implementation.
 */
public class PeerNameControl extends BaseEditBrowseTextControl {

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent dialog page this control is embedded in.
	 *                   Might be <code>null</code> if the control is not associated with a page.
	 */
	public PeerNameControl(IDialogPage parentPage) {
		super(parentPage);

		setIsGroup(false);
		setHasHistory(false);
		setHideBrowseButton(true);
		setEditFieldLabel(Messages.PeerNameControl_label);
		setAdjustBackgroundColor(parentPage != null);
	}

}
