/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal.extensions;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tm.te.core.extensions.ExecutableExtension;

/**
 * Target Explorer: Details editor page binding implementation.
 */
public class EditorPageBinding extends ExecutableExtension {
	private String fPageId;
	private String fInsertBefore;
	private String fInsertAfter;
	private Expression fExpression;

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.views.internal.extensions.ExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		super.setInitializationData(config, propertyName, data);

		if (config != null) {
			// Initialize the page id field by reading the <pageId> extension attribute.
			// Throws an exception if the id is empty or null.
			fPageId = config.getAttribute("pageId"); //$NON-NLS-1$
			if (fPageId == null || fPageId.trim().length() == 0) {
				throw createMissingMandatoryAttributeException("pageId", config.getContributor().getName()); //$NON-NLS-1$
			}

			// Initialize the insertBefore field by reading the <insertBefore> extension attribute if present.
			fInsertBefore = config.getAttribute("insertBefore"); //$NON-NLS-1$
			if (fInsertBefore == null || fInsertBefore.trim().length() == 0) fInsertBefore = ""; //$NON-NLS-1$

			// Initialize the insertAfter field by reading the <insertAfter> extension attribute if present.
			fInsertAfter = config.getAttribute("insertAfter"); //$NON-NLS-1$
			if (fInsertAfter == null || fInsertAfter.trim().length() == 0) fInsertAfter = ""; //$NON-NLS-1$

			IConfigurationElement[] children = config.getChildren();
			if (children != null && children.length > 0) {
				// Should only be one - enablement
				fExpression = ExpressionConverter.getDefault().perform(children[0]);
			}

		}
	}

	/**
	 * Returns the editor page id which is associated with this binding.
	 *
	 * @return The editor page id.
	 */
	public String getPageId() {
		return fPageId;
	}

	/**
	 * Returns the enablement expression which is associated with this binding.
	 *
	 * @return The enablement expression or <code>null</code>.
	 */
	public Expression getEnablement() {
		return fExpression;
	}

	/**
	 * Returns the &quot;insertBefore&quot; property for this binding.
	 *
	 * @return The &quot;insertBefore&quot; property of an empty string.
	 */
	public String getInsertBefore() {
		return fInsertBefore;
	}

	/**
	 * Returns the &quot;insertAfter&quot; property for this binding.
	 *
	 * @return The &quot;insertAfter&quot; property of an empty string.
	 */
	public String getInsertAfter() {
		return fInsertAfter;
	}
}
