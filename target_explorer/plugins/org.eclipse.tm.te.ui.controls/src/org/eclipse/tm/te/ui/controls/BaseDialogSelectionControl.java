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

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;

/**
 * Target Explorer: Base implementation of a control allowing to select
 * the content of the edit field control from a dialog. The dialog is
 * associated to the base edit browse text controls button.
 */
public class BaseDialogSelectionControl extends BaseEditBrowseTextControl {
	private Dialog dialogControl;
	private String dialogTitle;

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent page this control is embedded in.
	 *                   Might be <code>null</code> if the control is not associated with a page.
	 */
	public BaseDialogSelectionControl(IDialogPage parentPage) {
		super(parentPage);
		setIsGroup(true);
		setHasHistory(true);
		setDialogTitle(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#isAdjustEditFieldControlWidthHint()
	 */
	@Override
	protected boolean isAdjustEditFieldControlWidthHint() {
		return true;
	}

	/**
	 * Sets the dialogs title string. If the title string is <code>null</code>, the
	 * dialogs title string is set to an empty string.
	 *
	 * @param title The dialogs title or <code>null</code>.
	 */
	public void setDialogTitle(String title) {
		if (title == null) {
			dialogTitle = ""; //$NON-NLS-1$
		} else {
			dialogTitle = title;
		}
	}

	/**
	 * Returns the dialogs title string.
	 *
	 * @return The dialogs string or an empty string.
	 */
	public String getDialogTitle() {
		return dialogTitle;
	}

	/**
	 * Returns the dialog control.
	 *
	 * @return The dialog control or <code>null</code> if the control has not been created yet.
	 */
	public Dialog getDialogControl() {
		return dialogControl;
	}

	/**
	 * The method is called to create the dialog control. Subclasses may override this method
	 * to create their own dialog control. The default implementation returns <code>null</code>.
	 *
	 * @param parent The parent control for the button control to create. Must not be <code>null</code>!
	 * @return The created button control.
	 */
	protected Dialog doCreateDialogControl(Composite parent) {
		assert parent != null;
		return null;
	}

	/**
	 * Configure the controls associated dialog before the dialogs is opened. Subclasses may use
	 * this hook to configure the controls associated dialog for their specific needs.
	 *
	 * @param dialog The standard file dialog to configure. Must not be <code>null</code>!
	 */
	protected void configureDialogControl(Dialog dialog) {
		assert dialog != null;
		if (dialog != null) {
			dialog.setText(getDialogTitle());
		}
	}

	/**
	 * Opens the given dialog and wait till the user pressed either OK or cancel. In
	 * case the user pressed OK and have selected a element within the dialog, the
	 * selected element is returned as string. In case the user canceled the dialog,
	 * the method returns <code>null</code>. The default implementation opens nothing
	 * and returns <code>null</code>!
	 *
	 * @param dialog The dialog to open. Must not be <code>null</code>.
	 * @return The selected element or <code>null</code>.
	 */
	protected String doOpenDialogControl(Dialog dialog) {
		assert dialog != null;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#onButtonControlSelected()
	 */
	@Override
	protected void onButtonControlSelected() {
		// create and configure the controls associated dialog
		dialogControl = doCreateDialogControl(getParentControl());
		configureDialogControl(dialogControl);

		// open the dialog and get the user selected element
		String selectedElement = doOpenDialogControl(dialogControl);
		// apply the selected element in case the user pressed OK.
		if (selectedElement != null) {
			doApplyElementFromDialogControl(selectedElement);
		}

		// finally, validate the control
		isValid();
	}

	/**
	 * Apply the selected element returned from the controls associated dialog to the
	 * control. The default implementation applies the given element as is to the edit field
	 * control. Subclasses may override this method to run additional logic just before
	 * applying the selected element to the control.
	 *
	 * @param selectedElement The selected element from that controls associated dialog. Must not be <code>null</code>.
	 */
	protected void doApplyElementFromDialogControl(String selectedElement) {
		assert selectedElement != null;
		setEditFieldControlText(selectedElement);
	}
}
