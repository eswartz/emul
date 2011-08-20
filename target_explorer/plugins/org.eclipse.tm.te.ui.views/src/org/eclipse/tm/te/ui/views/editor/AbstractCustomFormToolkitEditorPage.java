/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.te.ui.forms.CustomFormToolkit;
import org.eclipse.ui.forms.IManagedForm;

/**
 * Target Explorer: Abstract details editor page implementation managing
 *                  an custom form toolkit instance.
 */
public class AbstractCustomFormToolkitEditorPage extends AbstractEditorPage {
	// Reference to the form toolkit instance
	private CustomFormToolkit toolkit = null;

	/**
	 * Returns the custom form toolkit instance.
	 *
	 * @return The custom form toolkit instance or <code>null</code>.
	 */
	protected final CustomFormToolkit getFormToolkit() {
		return toolkit;
	}

	/**
	 * Sets the custom form toolkit instance.
	 *
	 * @param toolkit The custom form toolkit instance or <code>null</code>.
	 */
	protected final void setFormToolkit(CustomFormToolkit toolkit) {
		this.toolkit = toolkit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);

		Assert.isNotNull(managedForm);

		// Create the toolkit instance
		toolkit = new CustomFormToolkit(managedForm.getToolkit());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#dispose()
	 */
	@Override
	public void dispose() {
		if (toolkit != null) { toolkit.dispose(); toolkit = null; }
		super.dispose();
	}
}
