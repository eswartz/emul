/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.ui.internal.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.te.core.utils.text.StringUtil;
import org.eclipse.tm.te.tcf.filesystem.controls.FSTreeContentProvider;
import org.eclipse.tm.te.tcf.filesystem.dialogs.FSOpenFileDialog;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tm.te.tcf.processes.core.interfaces.launcher.IProcessLauncher;
import org.eclipse.tm.te.tcf.processes.ui.internal.help.IContextHelpIds;
import org.eclipse.tm.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * Launch object at selected peer dialog.
 */
public class LaunchObjectDialog extends CustomTrayDialog {
	private Text imagePath;
	private Button imagePathBrowse;
	private Text arguments;

	private Map<String, Object> launchAttributes = null;
	/* default */final IEditorPart part;

	/**
	 * Constructor.
	 *
	 * @param parent The parent shell used to view the dialog.
	 */
	public LaunchObjectDialog(Shell parent) {
		this(null, parent);
	}

	/**
	 * Constructor.
	 *
	 * @param parent The parent shell used to view the dialog.
	 */
	public LaunchObjectDialog(IEditorPart part, Shell parent) {
		this(part, parent, IContextHelpIds.LAUNCH_OBJECT_DIALOG);
	}

	/**
	 * Constructor.
	 *
	 * @param part The parent editor part or <code>null</code>.
	 * @param parent The parent shell used to view the dialog.
	 * @param contextHelpId The dialog context help id or <code>null</code>.
	 */
	public LaunchObjectDialog(IEditorPart part, Shell parent, String contextHelpId) {
		super(parent, contextHelpId);
		this.part = part;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		configureTitles();

		Composite panel = new Composite(composite, SWT.NONE);
		panel.setLayout(new GridLayout(3, false));
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Add the controls
		Label label = new Label(panel, SWT.NONE);
		label.setText(Messages.LaunchObjectSelectionControl_editfield_label);

		imagePath = new Text(panel, SWT.SINGLE | SWT.BORDER);
		imagePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		imagePath.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateDialog();
			}
		});

		imagePathBrowse = new Button(panel, SWT.PUSH);
		imagePathBrowse.setText(org.eclipse.tm.te.ui.nls.Messages.EditBrowseTextControl_button_label);
		imagePathBrowse.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void widgetSelected(SelectionEvent e) {
				FSOpenFileDialog dialog = new FSOpenFileDialog(part instanceof FormPage ? (FormPage) part : null, getShell(), null);
				if (dialog.open() == Window.OK) {
					ISelection selection = dialog.getSelection();
					if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
						Object candidate = ((IStructuredSelection) selection).getFirstElement();
						if (candidate instanceof FSTreeNode) {
							String absPath = FSTreeContentProvider.getEntryAbsoluteName((FSTreeNode) candidate);
							if (absPath != null) {
								imagePath.setText(absPath);
							}
						}
					}
				}
			}
		});

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.LaunchObjectArgumentsControl_editfield_label);

		arguments = new Text(panel, SWT.SINGLE | SWT.BORDER);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		arguments.setLayoutData(layoutData);
		arguments.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateDialog();
			}
		});

		// Setup the control content
		setupContent();

		// Adjust the font
		applyDialogFont(composite);

		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		validateDialog();
		return control;
	}

	/**
	 * Configure the dialog title and the title area content. The method is called from
	 * {@link #createDialogArea(Composite)}.
	 */
	protected void configureTitles() {
		setDialogTitle(Messages.LaunchObjectSelectionControl_title);
	}

	/**
	 * Setup the control content.
	 */
	protected void setupContent() {
		restoreWidgetValues();
	}

	/**
	 * Validates the dialog.
	 */
	protected void validateDialog() {

		boolean valid = !"".equals(imagePath.getText()); //$NON-NLS-1$

		if (getButton(IDialogConstants.OK_ID) != null) getButton(IDialogConstants.OK_ID).setEnabled(valid);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		// Dispose the launch attributes
		launchAttributes = null;

		super.cancelPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		saveWidgetValues();

		// Create a new launch attributes map
		launchAttributes = new HashMap<String, Object>();
		// Update with the current control content
		updateLaunchAttributes(launchAttributes);

		super.okPressed();
	}

	/**
	 * Updates the given attributes map with the current control content.
	 */
	protected void updateLaunchAttributes(Map<String, Object> launchAttributes) {
		Assert.isNotNull(launchAttributes);

		launchAttributes.put(IProcessLauncher.PROP_PROCESS_PATH, imagePath.getText());

		String argumentsString = arguments.getText();
		String[] args = argumentsString != null && !"".equals(argumentsString.trim()) ? StringUtil.tokenize(argumentsString, 0, true) : null; //$NON-NLS-1$
		launchAttributes.put(IProcessLauncher.PROP_PROCESS_ARGS, args);
	}

	/**
	 * Returns the launch attributes.
	 *
	 * @return The launch attributes or <code>null</code> if canceled.
	 */
	public final Map<String, Object> getLaunchAttributes() {
		return launchAttributes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog#dispose()
	 */
	@Override
	protected void dispose() {
		super.dispose();
	}

	/**
	 * Saves the widget history to the dialog settings.
	 */
	protected void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			settings.put(IProcessLauncher.PROP_PROCESS_PATH, imagePath.getText());
			settings.put(IProcessLauncher.PROP_PROCESS_ARGS, arguments.getText());
		}
	}

	/**
	 * Restores the widget history from the dialog settings.
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String path = settings.get(IProcessLauncher.PROP_PROCESS_PATH);
			if (path != null) imagePath.setText(path);
			String args = settings.get(IProcessLauncher.PROP_PROCESS_ARGS);
			if (args != null) arguments.setText(args);
		}
	}

}
