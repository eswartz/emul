/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.tm.te.ui.controls.nls.Messages;
import org.eclipse.tm.te.ui.controls.validator.DirectoryNameValidator;
import org.eclipse.tm.te.ui.controls.validator.Validator;
import org.osgi.framework.Bundle;


/**
 * Base implementation of a simple directory selection control.
 * <p>
 * The control supports direct editing by the user or browsing for the directory. By
 * default, the control has a history of recently selected directories.
 */
public class DirectorySelectionControl extends BaseDialogSelectionControl {
	private String dialogMessage = ""; //$NON-NLS-1$

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent dialog page this control is embedded in.
	 *                   Might be <code>null</code> if the control is not associated with a page.
	 */
	public DirectorySelectionControl(IDialogPage parentPage) {
		super(parentPage);
		setDialogTitle(Messages.DirectorySelectionControl_title);
		setGroupLabel(Messages.DirectorySelectionControl_group_label);
		setEditFieldLabel(Messages.DirectorySelectionControl_editfield_label);
	}

	/**
	 * Sets the dialogs description message. If the given message is <code>null</code>, the
	 * dialogs description message is set to an empty string.
	 *
	 * @param message The dialogs description message or <code>null</code>.
	 */
	public void setDialogMessage(String message) {
		if (message == null) {
			this.dialogMessage = ""; //$NON-NLS-1$
		} else {
			this.dialogMessage = message;
		}
	}

	/**
	 * Returns the dialogs description message.
	 *
	 * @return The dialogs description message or an empty string.
	 */
	public String getDialogMessage() {
		return dialogMessage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseDialogSelectionControl#doCreateDialogControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Dialog doCreateDialogControl(Composite parent) {
		Assert.isNotNull(parent);

		Dialog dialog = new DirectoryDialog(parent.getShell());
		return dialog;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseDialogSelectionControl#configureDialogControl(org.eclipse.swt.widgets.Dialog)
	 */
	@Override
	protected void configureDialogControl(Dialog dialog) {
		super.configureDialogControl(dialog);

		// We do expect a directory dialog here.
		if (dialog instanceof DirectoryDialog) {
			DirectoryDialog directoryDialog = (DirectoryDialog)dialog;
			// the dialog should open within the directory of the currently selected
			// directory. If no directory has been currently selected, it should open
			// within the last browsed directory.
			String selectedDirectory = doGetSelectedDirectory();
			if (selectedDirectory != null && selectedDirectory.trim().length() > 0) {
				directoryDialog.setFilterPath(selectedDirectory);
			} else if (Platform.getBundle("org.eclipse.core.resources") != null //$NON-NLS-1$
						&& Platform.getBundle("org.eclipse.core.resources").getState() == Bundle.ACTIVE) { //$NON-NLS-1$
				directoryDialog.setFilterPath(org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
			}
			// set the dialogs description message
			directoryDialog.setMessage(getDialogMessage());
		}
	}

	/**
	 * Returns the directory to set as initial directory. This method
	 * is called from {@link #configureDialogControl(Dialog)} in case the dialog
	 * is a {@link DirectoryDialog}.
	 *
	 * @return The initial directory to set to the directory dialog or <code>null</code> if none.
	 */
	protected String doGetSelectedDirectory() {
		return getEditFieldControlText();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#doCreateEditFieldValidator()
	 */
	@Override
	protected Validator doCreateEditFieldValidator() {
		return new DirectoryNameValidator(
			Validator.ATTR_MANDATORY |
			DirectoryNameValidator.ATTR_MUST_EXIST |
			DirectoryNameValidator.ATTR_CAN_READ |
			DirectoryNameValidator.ATTR_CAN_WRITE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#setEditFieldValidator(org.eclipse.tm.te.ui.controls.validator.Validator)
	 */
	@Override
	public void setEditFieldValidator(Validator editFieldValidator) {
		Assert.isTrue(editFieldValidator instanceof DirectoryNameValidator);

		if (editFieldValidator instanceof DirectoryNameValidator) {
			super.setEditFieldValidator(editFieldValidator);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseDialogSelectionControl#doOpenDialogControl(org.eclipse.swt.widgets.Dialog)
	 */
	@Override
	protected String doOpenDialogControl(Dialog dialog) {
		Assert.isNotNull(dialog);

		// We do expect a directory dialog here.
		if (dialog instanceof DirectoryDialog) {
			DirectoryDialog directoryDialog = (DirectoryDialog)dialog;
			return directoryDialog.open();
		}

		return null;
	}
}
