/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.editor;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.te.core.nls.Messages;
import org.eclipse.tm.te.ui.views.activator.UIPlugin;
import org.eclipse.tm.te.ui.views.interfaces.IEditorPage;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.editor.FormPage;


/**
 * Target Explorer: Abstract details editor page implementation.
 */
public abstract class AbstractEditorPage extends FormPage implements IEditorPage {
	// The unique page id
	private String fId;

	/**
	 * Constructor.
	 */
	public AbstractEditorPage() {
		super("", ""); // //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		super.setInitializationData(config, propertyName, data);

		if (config != null) {
			// Initialize the id field by reading the <id> extension attribute.
			// Throws an exception if the id is empty or null.
			fId = config.getAttribute("id"); //$NON-NLS-1$
			if (fId == null || fId.trim().length() == 0) {
				IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
				                            NLS.bind(Messages.Extension_error_missingRequiredAttribute, "id", config.getContributor().getName())); //$NON-NLS-1$
				UIPlugin.getDefault().getLog().log(status);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#getId()
	 */
	@Override
	public String getId() {
		return fId;
	}

	/**
	 * Returns the node associated with the current editor input.
	 *
	 * @return The node or <code>null</code>.
	 */
	public Object getEditorInputNode() {
		IEditorInput input = getEditorInput();
		return input != null ? input.getAdapter(Object.class) : null;
	}
}
