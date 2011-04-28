/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.te.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.core.nls.Messages;



/**
 * Target Explorer: Executable extension implementation.
 * 
 * @since 1.0
 */
public class ExecutableExtension extends PlatformObject {
	private String fId;
	private String fLabel;
	private String fDescription;

	/**
	 * Constructor.
	 * @since 1.0
	 */
	public ExecutableExtension() {
		super();
		fId = null;
		fLabel = ""; //$NON-NLS-1$
		fDescription = ""; //$NON-NLS-1$
	}

	/**
	 * Clone the initialization data to the given executable extension instance.
	 *
	 * @param other The destination executable extension instance. Must not be <code>null</code>.
	 * @since 1.0
	 */
	public void cloneInitializationData(ExecutableExtension other) {
		assert other != null;
		other.fId = fId;
		other.fLabel = fLabel;
		other.fDescription = fDescription;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		if (config != null) {
			// Initialize the id field by reading the <id> extension attribute.
			// Throws an exception if the id is empty or null.
			fId = config.getAttribute("id"); //$NON-NLS-1$
			if (fId == null || fId.trim().length() == 0) {
				throw createMissingMandatoryAttributeException("id", config.getContributor().getName()); //$NON-NLS-1$
			}

			// Initialize the label field by reading the <label> extension attribute if present.
			fLabel = config.getAttribute("label"); //$NON-NLS-1$
			if (fLabel == null || fLabel.trim().length() == 0) fLabel = ""; //$NON-NLS-1$

			// Initialize the description field by reading the "<description>" extension child element if present.
			IConfigurationElement[] children = config.getChildren("description"); //$NON-NLS-1$
			// Only one description element is allow. All other will be ignored
			if (children.length > 0) {
				IConfigurationElement description = children[0];
				String value = description.getValue();
				fDescription = value != null ? value.trim() : ""; //$NON-NLS-1$
			}
		}
	}

	/**
	 * Creates a new {@link CoreException} to be thrown if a mandatory extension attribute
	 * is missing.
	 *
	 * @param attributeName The attribute name. Must not be <code>null</code>.
	 * @param extensionId The extension id. Must not be <code>null</code>.
	 *
	 * @return The {@link CoreException} instance.
	 * @since 1.0
	 */
	protected CoreException createMissingMandatoryAttributeException(String attributeName, String extensionId) {
		assert attributeName != null && extensionId != null;
		return new CoreException(new Status(IStatus.ERROR,
				CoreBundleActivator.getUniqueIdentifier(),
				0,
				NLS.bind(Messages.Extension_error_missingRequiredAttribute, attributeName, extensionId),
				null));
	}

	/**
	 * Returns the unique id of the extension. The returned
	 * id must be never <code>null</code> or an empty string.
	 *
	 * @return The unique id.
	 * @since 1.0
	 */
	public String getId() {
		return fId;
	}

	/**
	 * Returns the label of the extension.
	 *
	 * @return The label or an empty string.
	 * @since 1.0
	 */
	public String getLabel() {
		return fLabel != null ? fLabel : ""; //$NON-NLS-1$
	}

	/**
	 * Returns the description of the extension.
	 *
	 * @return The description or an empty string.
	 * @since 1.0
	 */
	public String getDescription() {
		return fDescription != null ? fDescription : ""; //$NON-NLS-1$
	}
}
