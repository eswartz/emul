/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.dialogs;

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
import org.eclipse.tm.te.tcf.filesystem.controls.FSTreeControl;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tm.te.ui.dialogs.CustomTrayDialog;
import org.eclipse.tm.te.ui.forms.CustomFormToolkit;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;


/**
 * Target Explorer: File system open file dialog.
 */
public class FSOpenFileDialog extends CustomTrayDialog {
	// Reference to the subcontrol
	private final FSTreeControl fControl;
	// Reference to the current selection within the file system tree
	private ISelection fSelection;

	protected class FSOpenFileTreeControl extends FSTreeControl {

		/**
		 * Constructor.
		 */
		public FSOpenFileTreeControl() {
			super();
		}

		/**
		 * Constructor.
		 *
		 * @param parentPage The parent form page this control is embedded in or
		 *                   <code>null</code> if the control is not embedded within
		 *                   a form page.
		 */
		public FSOpenFileTreeControl(FormPage parentPage) {
			super(parentPage);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.tcf.vtl.tcf.ui.internal.controls.trees.fs.FSTreeControl#hasColumns()
		 */
		@Override
		protected boolean hasColumns() {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.tcf.vtl.tcf.ui.internal.controls.trees.fs.FSTreeControl#doCreateTreeViewerSelectionChangedListener(org.eclipse.jface.viewers.TreeViewer)
		 */
		@Override
		protected ISelectionChangedListener doCreateTreeViewerSelectionChangedListener(TreeViewer viewer) {
			return new FSOpenFileTreeControlSelectionChangedListener();
		}
	}

	protected class FSOpenFileTreeControlSelectionChangedListener implements ISelectionChangedListener{
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		@SuppressWarnings("synthetic-access")
		public void selectionChanged(SelectionChangedEvent event) {
			fSelection = event.getSelection();
			updateButtons();
		}
	}

	/**
	 * Constructor.
	 *
	 * @param shell The parent shell or <code>null</code>.
	 */
	public FSOpenFileDialog(Shell shell) {
		this(shell, null);
	}

	/**
	 * Constructor.
	 *
	 * @param shell The parent shell or <code>null</code>.
	 * @param contextHelpId The dialog context help id or <code>null</code>.
	 */
	public FSOpenFileDialog(Shell shell, String contextHelpId) {
		this(null, shell, contextHelpId);
	}

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent form page this control is embedded in or
	 *                   <code>null</code> if the control is not embedded within
	 *                   a form page.
	 * @param shell The parent shell or <code>null</code>.
	 * @param contextHelpId The dialog context help id or <code>null</code>.
	 */
	public FSOpenFileDialog(FormPage parentPage, Shell shell, String contextHelpId) {
		super(shell, contextHelpId);

		fControl = new FSOpenFileTreeControl(parentPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ide.common.ui.dialogs.WRUnifiedTrayDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);

		setDialogTitle(Messages.FSOpenFileDialog_title);

		Composite panel = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0; layout.marginHeight = 0;
		panel.setLayout(layout);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = convertHeightInCharsToPixels(25);
		layoutData.widthHint = convertWidthInCharsToPixels(50);
		panel.setLayoutData(layoutData);

		CustomFormToolkit toolkit = null;
		if (fControl.getParentPart() instanceof IFormPage && ((IFormPage)fControl.getParentPart()).getManagedForm() != null) {
			toolkit = new CustomFormToolkit(((IFormPage)fControl.getParentPart()).getManagedForm().getToolkit());
		}
		if (toolkit == null) toolkit = new CustomFormToolkit(new FormToolkit(getShell().getDisplay()));

		fControl.setupFormPanel(panel, toolkit);

		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ide.common.ui.dialogs.WRUnifiedTrayDialog#close()
	 */
	@Override
	public boolean close() {
		if (fControl != null) {
			fControl.dispose();
		}

		return super.close();
	}

	/* (non-Javadoc)
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
		if (okButton != null) okButton.setEnabled(fSelection != null && !fSelection.isEmpty());
	}

	/**
	 * Returns the current file system control selection.
	 * @return
	 */
	public ISelection getSelection() {
		return fSelection;
	}
}
