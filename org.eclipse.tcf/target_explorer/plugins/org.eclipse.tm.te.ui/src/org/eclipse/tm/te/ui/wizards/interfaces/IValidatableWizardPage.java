/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.wizards.interfaces;

import org.eclipse.jface.wizard.WizardPage;

/**
 * Public interface for validatable wizard pages.
 */
public interface IValidatableWizardPage {

	/**
	 * Validates the page status.
	 * <p>
	 * If necessary, set corresponding messages and message types to signal if some
	 * control on the page needs attention.
	 * <p>
	 * Depending on the outcome of the page data validation, call {@link WizardPage#setPageComplete(boolean)}
	 * with either <code>true</code> or <code>false</code> to signal if the wizard
	 * can finish given the current page data or not.
	 */
	public void validatePage();
}
