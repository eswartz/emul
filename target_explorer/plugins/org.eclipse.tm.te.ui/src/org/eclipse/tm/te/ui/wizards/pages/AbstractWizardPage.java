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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.te.ui.utils.SWTControlUtil;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * An abstract common wizard page implementation.
 * <p>
 * This wizard page implementation is adding control history management
 * and link the page with the context help system.
 */
public abstract class AbstractWizardPage extends WizardPage {
	// The context help id of the wizard page
	private String contextHelpId = null;

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
		this.contextHelpId = contextHelpId;
	}

	/**
	 * Returns the wizard page context help id.
	 *
	 * @return The context help id or <code>null</code> if none.
	 */
	protected final String getContextHelpId() {
		return contextHelpId;
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
	 * Convenience method to create a "invisible" label for creating an
	 * empty space between controls.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param span The horizontal span.
	 * @param toolkit The form toolkit or <code>null</code>.
	 *
	 * @return
	 */
	protected Label createEmptySpace(Composite parent, int span, FormToolkit toolkit) {
		Assert.isNotNull(parent);

		Label emptySpace = toolkit != null ? toolkit.createLabel(parent, null) : new Label(parent, SWT.NONE);

		GridData layoutData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		layoutData.horizontalSpan = span;
		layoutData.widthHint = 0; layoutData.heightHint = SWTControlUtil.convertHeightInCharsToPixels(emptySpace, 1);

		emptySpace.setLayoutData(layoutData);

		return emptySpace;
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
}
