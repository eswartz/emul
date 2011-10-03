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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.tm.te.ui.controls.interfaces.IRunnableContextProvider;
import org.eclipse.tm.te.ui.wizards.interfaces.IValidatableWizardPage;
import org.eclipse.ui.forms.widgets.FormToolkit;


/**
 * Target Explorer: Common UI control to be embedded within a dialog page.
 */
public class BaseDialogPageControl extends BaseControl implements IRunnableContextProvider {
	// Reference to the parent page
	private final IDialogPage parentPage;
	// Reference to the form toolkit to be used to create the controls.
	private FormToolkit toolkit = null;

	/**
	 * Constructor.
	 *
	 */
	public BaseDialogPageControl() {
		this(null);
	}

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent dialog page this control is embedded in or
	 *                   <code>null</code> if the control is not embedded within
	 *                   a dialog page.
	 */
	public BaseDialogPageControl(IDialogPage parentPage) {
		super();
		this.parentPage = parentPage;
	}

	/**
	 * Returns the parent dialog page if this control is embedded within a page.
	 *
	 * @return The parent dialog page or <code>null</code> if the control is not embedded within a page.
	 */
	public final IDialogPage getParentPage() {
		return parentPage;
	}

	/**
	 * Returns the validatable wizard page instance.
	 * <p>
	 * The default implementation is testing the associated
	 * parent page to implement the {@link IValidatableWizardPage} interface.
	 *
	 * @return The validatable wizard page instance or <code>null</code>.
	 */
	public IValidatableWizardPage getValidatableWizardPage() {
		IDialogPage parentPage = getParentPage();
		if (parentPage instanceof IValidatableWizardPage) {
			return (IValidatableWizardPage)parentPage;
		}
		return null;
	}

	/**
	 * Sets the form toolkit to be used for creating the control widgets.
	 *
	 * @param toolkit The form toolkit instance or <code>null</code>.
	 */
	public final void setFormToolkit(FormToolkit toolkit) {
		this.toolkit = toolkit;
	}

	/**
	 * Returns the form toolkit used for creating the control widgets.
	 *
	 * @return The form toolkit instance or <code>null</code>.
	 */
	public final FormToolkit getFormToolkit() {
		return toolkit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.interfaces.IRunnableContextProvider#getRunnableContext()
	 */
	@Override
	public IRunnableContext getRunnableContext() {
		return getParentPage() instanceof IRunnableContextProvider ? ((IRunnableContextProvider)getParentPage()).getRunnableContext() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseControl#doGetParentSection(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	@Override
	protected IDialogSettings doGetParentSection(IDialogSettings settings) {
		Assert.isNotNull(settings);

		// We are going to create a subsection per parent page containing a subsection per control,
		// if the parent page is set at all
		IDialogSettings subsection = settings;
		if (getParentPage() != null) {
			subsection = settings.getSection(getParentPage().getClass().getName());
			if (subsection == null) {
				subsection = settings.addNewSection(getParentPage().getClass().getName());
			}
		}

		return subsection;
	}
}
