/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls.wizards.pages;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.tm.te.ui.controls.interfaces.IValidatableDialogPage;
import org.eclipse.tm.te.ui.wizards.pages.AbstractWizardPage;

/**
 * An abstract validatable wizard page implementation.
 * <p>
 * This wizard page implementation is adding utility methods
 * for handling page validation.
 */
public abstract class AbstractValidatableWizardPage extends AbstractWizardPage implements IValidatableDialogPage {
	// A used to detect if a validation process is already running.
	// If set to true, validatePage() should return immediately.
	private boolean validationInProgress = false;

	/**
	 * Constructor.
	 *
	 * @param pageName The page name. Must not be <code>null</code>.
	 */
	public AbstractValidatableWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Constructor.
	 *
	 * @param pageName The page name. Must not be <code>null</code>.
	 * @param title The wizard page title or <code>null</code>.
	 * @param titleImage The wizard page title image or <code>null</code>.
	 */
	public AbstractValidatableWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/**
	 * Set the validation in progress state.
	 *
	 * @param state <code>True</code> to mark validation in progress, <code>false</code> otherwise.
	 */
	public final boolean setValidationInProgress(boolean state) {
		boolean changed = false;
		// Apply only if really changed
		if (validationInProgress != state) {
			// Set the new value
			validationInProgress = state;
			onValidationInProgressChanged(validationInProgress);
			changed = true;
		}
		return changed;
	}

	/**
	 * Called from {@link #setValidationInProgress(boolean)} if the value
	 * of the corresponding flag changed. Subclasses may overwrite this
	 * method if additional custom steps shall be executed.
	 * <p>
	 * The default implementation is doing nothing.
	 *
	 * @param newValue The new value of the validation in progress flag. Same as calling {@link #isValidationInProgress()}.
	 */
	protected void onValidationInProgressChanged(boolean newValue) {
	}

	/**
	 * Returns if the current validation in progress state.
	 *
	 * @return <code>True</code> to mark validation in progress, <code>false</code> otherwise.
	 */
	public final boolean isValidationInProgress() {
		return validationInProgress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.interfaces.IValidatableDialogPage#validatePage()
	 */
	public void validatePage() {
		if (isValidationInProgress())  return;
		setValidationInProgress(true);

		setMessage(null);
		setErrorMessage(null);
		setPageComplete(true);

		setValidationInProgress(false);
	}

}
