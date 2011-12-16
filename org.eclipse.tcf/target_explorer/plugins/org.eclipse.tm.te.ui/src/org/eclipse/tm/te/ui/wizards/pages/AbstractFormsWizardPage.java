/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.wizards.pages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.te.ui.forms.CustomFormToolkit;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Abstract wizard page using forms.
 */
public abstract class AbstractFormsWizardPage extends AbstractWizardPage {
	// The forms toolkit instance
	private CustomFormToolkit toolkit = null;

	/**
	 * Constructor.
	 *
	 * @param pageName The page name. Must not be <code>null</code>.
	 */
	public AbstractFormsWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Constructor.
	 *
	 * @param pageName The page name. Must not be <code>null</code>.
	 * @param title The wizard page title or <code>null</code>.
	 * @param titleImage The wizard page title image or <code>null</code>.
	 */
	public AbstractFormsWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		toolkit = null;
		super.dispose();
	}

	/**
	 * Creates the forms toolkit to use.
	 *
	 * @param display The display. Must not be <code>null</code>.
	 * @return The forms toolkit instance. Must never be <code>null</code>.
	 */
	protected CustomFormToolkit createFormToolkit(Display display) {
		Assert.isNotNull(display);
		return new CustomFormToolkit(new FormToolkit(display));
	}

	/**
	 * Returns the forms toolkit to use.
	 * <p>
	 * If {@link #createControl(Composite)} hasn't been called yet, or
	 * {@link #dispose()} has been called, the method will return
	 * <code>null</code>.
	 *
	 * @return The forms toolkit instance or <code>null</code>.
	 */
	public final CustomFormToolkit getFormToolkit() {
		return toolkit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		// Create the form toolkit
		toolkit = createFormToolkit(parent.getDisplay());
		Assert.isNotNull(toolkit);
	}

}
