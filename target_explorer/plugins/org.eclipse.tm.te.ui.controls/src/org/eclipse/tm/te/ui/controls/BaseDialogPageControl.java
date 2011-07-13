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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.tm.te.ui.controls.interfaces.IRunnableContextProvider;


/**
 * Target Explorer: Common UI control to be embedded within a dialog page.
 */
public class BaseDialogPageControl extends BaseControl implements IRunnableContextProvider {
	/**
	 * Reference to the parent page if the control is embedded within a page.
	 */
	private final IDialogPage parentPage;

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

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.interfaces.IRunnableContextProvider#getRunnableContext()
	 */
	public IRunnableContext getRunnableContext() {
		return getParentPage() instanceof IRunnableContextProvider ? ((IRunnableContextProvider)getParentPage()).getRunnableContext() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseControl#doGetParentSection(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	@Override
	protected IDialogSettings doGetParentSection(IDialogSettings settings) {
		assert settings != null;

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
