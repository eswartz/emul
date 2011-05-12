/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.wizards.pages;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * An abstract common wizard page implementation.
 * <p>
 * This wizard page implementation is adding control history management
 * and link the page with the context help system.
 */
public abstract class AbstractWizardPage extends WizardPage {
	// The context help id of the wizard page
	private String fContextHelpId = null;

	/**
	 * Constructor.
	 *
	 * @param pageName The page name. Must not be <code>null</code>.
	 */
	public AbstractWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Constructor.
	 *
	 * @param pageName The page name. Must not be <code>null</code>.
	 * @param title The wizard page title or <code>null</code>.
	 * @param titleImage The wizard page title image or <code>null</code>.
	 */
	public AbstractWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/**
	 * Set the wizard page context help id.
	 * <p>
	 * If set to non <code>null</code>, than the help id is associated
	 * with the pages control once subclasses calls {@link #setControl(org.eclipse.swt.widgets.Control)}.
	 *
	 * @param contextHelpId The context help id or <code>null</code> if none.
	 */
	protected final void setContextHelpId(String contextHelpId) {
		fContextHelpId = contextHelpId;
	}

	/**
	 * Returns the wizard page context help id.
	 *
	 * @return The context help id or <code>null</code> if none.
	 */
	protected final String getContextHelpId() {
		return fContextHelpId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#performHelp()
	 */
	@Override
	public void performHelp() {
		String contextHelpId = getContextHelpId();
		if (contextHelpId != null) {
			PlatformUI.getWorkbench().getHelpSystem().displayHelp(contextHelpId);
		}
	}

	/**
	 * Saves the widget history of all UI elements of the page.
	 */
	public void saveWidgetValues() {
	}

	/**
	 * Restores the widget history of all UI elements of the page.
	 */
	public void restoreWidgetValues() {
	}

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
	public void validatePage() {
	}

}
