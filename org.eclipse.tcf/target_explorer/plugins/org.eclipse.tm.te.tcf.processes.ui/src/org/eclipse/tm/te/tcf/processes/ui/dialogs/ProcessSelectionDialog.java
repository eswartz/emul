/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.tcf.processes.ui.controls.ProcessesTreeControl;
import org.eclipse.tm.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tm.te.ui.forms.CustomFormToolkit;
import org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Process selection dialog.
 */
public class ProcessSelectionDialog extends CustomTrayDialog {
	// Reference to the subcontrol
	private final ProcessesTreeControl control;
	// Reference to the current selection within the file system tree
	private ISelection selection;

	protected class ProcessSelectionTreeControl extends ProcessesTreeControl {

		/**
		 * Constructor.
		 */
		public ProcessSelectionTreeControl() {
			super();
		}

		/**
		 * Constructor.
		 *
		 * @param parentPage The parent form page this control is embedded in or <code>null</code>
		 *            if the control is not embedded within a form page.
		 */
		public ProcessSelectionTreeControl(FormPage parentPage) {
			super(parentPage);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.tcf.processes.ui.controls.ProcessesTreeControl#hasColumns()
		 */
		@Override
		protected boolean hasColumns() {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.tcf.processes.ui.controls.ProcessesTreeControl#doCreateTreeViewerSelectionChangedListener(org.eclipse.jface.viewers.TreeViewer)
		 */
		@Override
		protected ISelectionChangedListener doCreateTreeViewerSelectionChangedListener(TreeViewer viewer) {
			return new ProcessSelectionTreeControlSelectionChangedListener();
		}
	}

	protected class ProcessSelectionTreeControlSelectionChangedListener implements ISelectionChangedListener {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		@Override
		@SuppressWarnings("synthetic-access")
		public void selectionChanged(SelectionChangedEvent event) {
			selection = event.getSelection();
			updateButtons();
		}
	}

	/**
	 * Constructor.
	 *
	 * @param shell The parent shell or <code>null</code>.
	 */
	public ProcessSelectionDialog(Shell shell) {
		this(shell, null);
	}

	/**
	 * Constructor.
	 *
	 * @param shell The parent shell or <code>null</code>.
	 * @param contextHelpId The dialog context help id or <code>null</code>.
	 */
	public ProcessSelectionDialog(Shell shell, String contextHelpId) {
		this(null, shell, contextHelpId);
	}

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent form page this control is embedded in or <code>null</code> if
	 *            the control is not embedded within a form page.
	 * @param shell The parent shell or <code>null</code>.
	 * @param contextHelpId The dialog context help id or <code>null</code>.
	 */
	public ProcessSelectionDialog(FormPage parentPage, Shell shell, String contextHelpId) {
		super(shell, contextHelpId);

		control = new ProcessSelectionTreeControl(parentPage);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		setDialogTitle(Messages.ProcessSelectionDialog_title);

		Composite panel = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		panel.setLayout(layout);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = convertHeightInCharsToPixels(25);
		layoutData.widthHint = convertWidthInCharsToPixels(50);
		panel.setLayoutData(layoutData);

		CustomFormToolkit toolkit = null;
		if (control.getParentPart() instanceof IFormPage && ((IFormPage) control.getParentPart()).getManagedForm() != null) {
			toolkit = new CustomFormToolkit(((IFormPage) control.getParentPart()).getManagedForm().getToolkit());
		}
		if (toolkit == null) toolkit = new CustomFormToolkit(new FormToolkit(getShell().getDisplay()));

		control.setupFormPanel(panel, toolkit);

		return composite;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog#close()
	 */
	@Override
	public boolean close() {
		if (control != null) {
			control.dispose();
		}

		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		updateButtons();
		return control;
	}

	/**
	 * Update the button enablement.
	 */
	protected void updateButtons() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) okButton.setEnabled(selection != null && !selection.isEmpty());
	}

	/**
	 * Returns the current file system control selection.
	 *
	 * @return
	 */
	public ISelection getSelection() {
		return selection;
	}
}
