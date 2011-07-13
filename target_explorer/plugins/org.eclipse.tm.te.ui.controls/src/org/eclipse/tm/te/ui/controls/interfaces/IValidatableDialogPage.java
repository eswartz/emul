/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls.interfaces;

import org.eclipse.jface.wizard.WizardPage;

/**
 * Target Explorer: Public interface for validatable dialog pages.
 */
public interface IValidatableDialogPage {

	/**
	 * Validates the page status. If necessary, set corresponding messages
	 * and message types to signal if some control on the page needs attention.
	 * In case the page is a {@link WizardPage}, {@link WizardPage#setPageComplete(boolean)}
	 * should be used to signal if the wizard cannot be completed with the current
	 * page status.
	 */
	public void validatePage();
}
