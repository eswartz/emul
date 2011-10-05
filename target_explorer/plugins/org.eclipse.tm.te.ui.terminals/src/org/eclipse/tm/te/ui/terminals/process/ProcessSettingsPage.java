/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.process;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.te.ui.swt.SWTControlUtil;
import org.eclipse.tm.te.ui.terminals.nls.Messages;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

/**
 * Process connector settings page implementation.
 */
@SuppressWarnings("restriction")
public class ProcessSettingsPage implements ISettingsPage {
	private Text processImageSelectorControl;
	private Button processImageSelectorControlButton;
	private Text processArgumentsControl;
	private Button localEchoSelectorControl;

	private final ProcessSettings settings;

	/**
	 * Constructor.
	 *
	 * @param settings
	 */
	public ProcessSettingsPage(ProcessSettings settings) {
		super();

		Assert.isNotNull(settings);
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// The entry fields shall be properly aligned
		Composite panel = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0; layout.marginHeight = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the process image selector control
		Label label = new Label(panel, SWT.HORIZONTAL);
		label.setText(Messages.ProcessSettingsPage_processImagePathSelectorControl_label);

		// Text field and browse button are aligned it their own panel
		Composite innerPanel = new Composite(panel, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0; layout.marginHeight = 0;
		innerPanel.setLayout(layout);
		innerPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		processImageSelectorControl = new Text(innerPanel, SWT.SINGLE | SWT.BORDER);
		processImageSelectorControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		processImageSelectorControlButton = new Button(innerPanel, SWT.PUSH);
		processImageSelectorControlButton.setText(Messages.ProcessSettingsPage_processImagePathSelectorControl_button);
		processImageSelectorControlButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onBrowseButtonSelected(e);
			}
		});

		// Create the process arguments control
		label = new Label(panel, SWT.HORIZONTAL);
		label.setText(Messages.ProcessSettingsPage_processArgumentsControl_label);

		processArgumentsControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		processArgumentsControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the local echo check box
		localEchoSelectorControl = new Button(composite, SWT.CHECK);
		localEchoSelectorControl.setText(Messages.ProcessSettingsPage_localEchoSelectorControl_label);
		localEchoSelectorControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		localEchoSelectorControl.setSelection(!PTY.isSupported());

		// Initialize the control content
		loadSettings();
	}

	/**
	 * Called once the user pressed the browse button.
	 *
	 * @param e The selection event or <code>null</code>.
	 */
	protected void onBrowseButtonSelected(SelectionEvent e) {
		// Determine the shell
		Shell shell = e != null ? e.widget.getDisplay().getActiveShell() : PlatformUI.getWorkbench().getDisplay().getActiveShell();

		// create a standard file dialog
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText(Messages.ProcessSettingsPage_dialogTitle);

		// the dialog should open within the directory of the currently selected
		// file. If no file has been currently selected, it should open within the
		// last browsed directory.
		String selectedFile = SWTControlUtil.getText(processImageSelectorControl);
		if (selectedFile != null && selectedFile.trim().length() > 0) {
			IPath filePath = new Path(selectedFile);
			// If the selected file points to an directory, use the directory as is
			IPath filterPath = filePath.toFile().isDirectory() ? filePath : filePath.removeLastSegments(1);
			String filterFileName = filePath.toFile().isDirectory() || !filePath.toFile().exists() ? null : filePath.lastSegment();

			if (!filterPath.isEmpty()) {
				dialog.setFilterPath(filterPath.toString());
			}
			if (filterFileName != null) {
				dialog.setFileName(filterFileName);
			}
		} else {
			Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
			if (bundle != null && (bundle.getState() == Bundle.RESOLVED || bundle.getState() == Bundle.ACTIVE)) {
				dialog.setFilterPath(org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
			}
		}

		// Open the dialog
		selectedFile = dialog.open();
		if (selectedFile != null) {
			SWTControlUtil.setText(processImageSelectorControl, selectedFile);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage#saveSettings()
	 */
	@Override
	public void saveSettings() {
		settings.setImage(SWTControlUtil.getText(processImageSelectorControl));
		settings.setArguments(SWTControlUtil.getText(processArgumentsControl));
		settings.setLocalEcho(SWTControlUtil.getSelection(localEchoSelectorControl));
		settings.setProcess(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage#loadSettings()
	 */
	@Override
	public void loadSettings() {
		SWTControlUtil.setText(processImageSelectorControl, settings.getImage());
		SWTControlUtil.setText(processArgumentsControl, settings.getArguments());
		SWTControlUtil.setSelection(localEchoSelectorControl, settings.isLocalEcho());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage#validateSettings()
	 */
	@Override
	public boolean validateSettings() {
		// The settings are considered valid if the selected process image can be read.
		String selectedFile = SWTControlUtil.getText(processImageSelectorControl);
		return selectedFile != null && !"".equals(selectedFile.trim()) && new Path(selectedFile).toFile().canRead(); //$NON-NLS-1$
	}
}
