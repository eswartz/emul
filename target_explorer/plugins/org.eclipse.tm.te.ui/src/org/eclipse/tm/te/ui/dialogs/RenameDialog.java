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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.eclipse.tm.te.ui.jface.dialogs.CustomTitleAreaDialog;

/**
 * Rename dialog implementation.
 */
public class RenameDialog extends CustomTitleAreaDialog {

	private String title;
	/* default */ String defaultMessage;
	/* default */ String usedErrorMessage;
	/* default */ String formatErrorMessage;
	private String label;
	/* default */ String formatRegex;
	private String oldName;
	private String newName;
	/* default */ List<String> usedNames;

	/* default */ Text name;

	/**
	 * Constructor.
	 *
	 * @param parent The parent shell.
	 * @param title The title for the dialog.
	 * @param defaultMessage The default info message for the dialog.
	 * @param usedErrorMessage The error message if the name is already in use.
	 * @param formatErrorMessage The error message if the format of the name is illegal.
	 * @param label The label for the entry field.
	 * @param oldName The original name.
	 * @param formatRegex The format regular expression.
	 * @param usedNames The list of used or reserved names.
	 * @param contextHelpId The context help id.
	 */
	public RenameDialog(Shell parent, String title, String defaultMessage, String usedErrorMessage, String formatErrorMessage,
						String label, String oldName, String formatRegex, String[] usedNames, String contextHelpId) {
		super(parent, contextHelpId);

		this.title = title != null ? title : "Rename"; //$NON-NLS-1$
		this.defaultMessage = defaultMessage != null ? defaultMessage : ""; //$NON-NLS-1$
		this.usedErrorMessage = usedErrorMessage != null ? usedErrorMessage : ""; //$NON-NLS-1$
		this.formatErrorMessage = formatErrorMessage != null ? formatErrorMessage : ""; //$NON-NLS-1$
		this.formatRegex = formatRegex != null ? formatRegex : ".*"; //$NON-NLS-1$
		this.label = label != null ? label : "Name:"; //$NON-NLS-1$
		this.oldName = oldName != null ? oldName : ""; //$NON-NLS-1$
		this.usedNames = usedNames != null ? Arrays.asList(usedNames) : new ArrayList<String>();
		this.newName = this.oldName;
	}

	/*(non-Javadoc)
	 * @see org.eclipse.tm.te.ui.dialogs.CustomTitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setDialogTitle(title);
		setDefaultMessage(defaultMessage, IMessageProvider.INFORMATION);

		//set margins of dialog and apply dialog font
		Composite container = (Composite) super.createDialogArea(parent);
		//we need two columns
		Composite comp = new Composite(container, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(comp, SWT.NONE).setText(label);
		name = new Text(comp, SWT.BORDER | SWT.SINGLE);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		name.setText(oldName);
		name.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				if (usedNames.contains(name.getText())) {
					setButtonEnabled(OK, false);
					setMessage(usedErrorMessage, IMessageProvider.ERROR);
				}
				else if (!name.getText().matches(formatRegex)) {
					setButtonEnabled(OK, false);
					setMessage(formatErrorMessage, IMessageProvider.ERROR);
				}
				else {
					setButtonEnabled(OK, true);
					setMessage(defaultMessage, IMessageProvider.INFORMATION);
				}
			}
		});

		applyDialogFont(container);

		return container;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control =  super.createButtonBar(parent);
		setButtonEnabled(OK, false);
		return control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.dialogs.CustomTitleAreaDialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		newName = name.getText();
		super.okPressed();
	}

	/**
	 * Return the new name after OK was pressed.
	 * Unless OK was pressed, the old name is returned.
	 */
	public String getNewName() {
		return newName;
	}
}
