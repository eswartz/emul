/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.dialogs;

import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.te.ui.interfaces.IContextHelpIds;
import org.eclipse.tm.te.ui.jface.dialogs.CustomTitleAreaDialog;
import org.eclipse.tm.te.ui.nls.Messages;
import org.eclipse.tm.te.ui.swt.SWTControlUtil;
import org.eclipse.ui.PlatformUI;

/**
 * Dialog implementation allowing to enter the data for name/value pairs.
 */
public class NameValuePairDialog extends CustomTitleAreaDialog {

	private String name;
	private String value;

	private String dialogTitle;
	private String title;
	private String message;
	private String[] fieldLabels;
	private String[] initialValues;
	private Set<String> usedNames;

	private Text nameText;
	private Text valueText;

	/**
	 * Constructor.
	 *
	 * @param shell The parent shell or <code>null</code>.
	 * @param dialogTitle The dialog title. Must not be <code>null</code>.
	 * @param title The title. Must not be <code>null</code>.
	 * @param message The dialogs default message. Must not be <code>null</code>.
	 * @param fieldLabels The field labels. Must not be <code>null</code>.
	 * @param initialValues The field initial values. Must not be <code>null</code>.
	 * @param usedNames The list of used names. Must not be <code>null</code>.
	 */
	public NameValuePairDialog(Shell shell, String dialogTitle, String title, String message, String[] fieldLabels, String[] initialValues, Set<String> usedNames) {
		super(shell);

		Assert.isNotNull(dialogTitle);
		Assert.isNotNull(title);
		Assert.isNotNull(message);
		Assert.isNotNull(fieldLabels);
		Assert.isNotNull(initialValues);
		Assert.isNotNull(usedNames);

		this.dialogTitle = dialogTitle;
		this.title = title;
		this.message = message;
		this.fieldLabels = fieldLabels;
		this.initialValues = initialValues;
		this.usedNames = usedNames;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite top = (Composite)super.createDialogArea(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, IContextHelpIds.NAME_VALUE_PAIR_DIALOG);

		setDialogTitle(dialogTitle);
		setTitle(title);
		setDefaultMessage(message, IMessageProvider.NONE);

		Composite panel = new Composite(top, SWT.NONE);
		panel.setLayout(new GridLayout(2, false));
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label label = new Label(panel, SWT.NONE);
		label.setText(fieldLabels[0]);

		nameText = new Text(panel, SWT.BORDER | SWT.SINGLE);
		nameText.setText(initialValues[0]);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.widthHint = 300;
		nameText.setLayoutData(layoutData);
		nameText.addModifyListener(new ModifyListener() {
			@Override
            public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		});

		label = new Label(panel, SWT.NONE);
		label.setText(fieldLabels[1]);

		valueText = new Text(panel, SWT.BORDER | SWT.SINGLE);
		valueText.setText(initialValues[1]);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 300;
		valueText.setLayoutData(layoutData);
		valueText.addModifyListener(new ModifyListener() {
			@Override
            public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		});

		applyDialogFont(panel);
		return panel;
	}

    /**
	 * Return the name/value pair entered in this dialog.
	 * <p>
	 * If the cancel button was hit, both will be <code>null</code>.
	 */
	public String[] getNameValuePair() {
		return new String[] { name, value };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTitleAreaDialog#create()
	 */
	@Override
	public void create() {
		super.create();
		updateButtons();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			name= SWTControlUtil.getText(nameText).trim();
			value = SWTControlUtil.getText(valueText).trim();
		} else {
			name = null;
			value = null;
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Enable the OK button if valid input
	 */
	protected void updateButtons() {
		String name = SWTControlUtil.getText(nameText).trim();
		String value = SWTControlUtil.getText(valueText).trim();

		if (name.trim().length() == 0) {
			setMessage(getErrorMissingName(), IMessageProvider.INFORMATION);
		}
		else if (usedNames.contains(name.trim())) {
			setMessage(NLS.bind(getErrorUsedOrIllegalName(), name), IMessageProvider.ERROR);
		}
		else if (value.trim().length() == 0) {
			setMessage(NLS.bind(getErrorMissingValue(), name), IMessageProvider.INFORMATION);
		}
		else {
			setMessage(message, IMessageProvider.NONE);
		}
		getButton(IDialogConstants.OK_ID).setEnabled(getMessageType() == IMessageProvider.NONE);
	}

	/**
	 * Returns the text to show as missing name error.
	 */
	protected String getErrorMissingName() {
		return Messages.NameValuePairDialog_missingName_error;
	}

	/**
	 * Returns the text to show as used or illegal name error.
	 */
	protected String getErrorUsedOrIllegalName() {
		return Messages.NameValuePairDialog_usedOrIllegalName_error;
	}

	/**
	 * Returns the text to show as missing value error.
	 */
	protected String getErrorMissingValue() {
		return Messages.NameValuePairDialog_missingValue_error;
	}
}
