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

import org.eclipse.core.runtime.Assert;
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
 */
public class ExecutableExtension extends PlatformObject {
	// The mandatory id of the extension
	private String id = null;

	// The configuration element
	private IConfigurationElement configElement = null;

	/**
	 * Clone the initialization data to the given executable extension instance.
	 *
	 * @param other The destination executable extension instance. Must not be <code>null</code>.
	 */
	public void cloneInitializationData(ExecutableExtension other) {
		Assert.isNotNull(other);
		other.id = id;
		other.configElement = configElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		// Remember the configuration element
		configElement = config;

		// Initialize the id field by reading the <id> extension attribute.
		// Throws an exception if the id is empty or null.
		id = configElement != null ? configElement.getAttribute("id") : null; //$NON-NLS-1$
		if (id == null || (id != null && "".equals(id.trim()))) { //$NON-NLS-1$
			throw createMissingMandatoryAttributeException("id", config.getContributor().getName()); //$NON-NLS-1$
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
	 */
	protected CoreException createMissingMandatoryAttributeException(String attributeName, String extensionId) {
		Assert.isNotNull(attributeName);
		Assert.isNotNull(extensionId);

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
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the configuration element of the extension. The method
	 * does return <code>null</code> if {@link #setInitializationData(IConfigurationElement, String, Object)}
	 * has not been called yet.
	 *
	 * @return The configuration element or <code>null</code> if none.
	 */
	protected final IConfigurationElement getConfigElement() {
		return configElement;
	}

	/**
	 * Returns the label or UI name of the extension.
	 *
	 * @return The label or UI name. An empty string if not set.
	 */
	public String getLabel() {
		// Try the "label" attribute first
		String label = configElement != null ? configElement.getAttribute("label") : null; //$NON-NLS-1$
		// If "label" is not found or empty, try the "name" attribute as fallback
		if (label == null || (label != null && "".equals(label.trim()))) { //$NON-NLS-1$
			label = configElement != null ? configElement.getAttribute("name") : null; //$NON-NLS-1$
		}
		return label != null ? label.trim() : ""; //$NON-NLS-1$
	}

	/**
	 * Returns the description of the extension.
	 *
	 * @return The description or an empty string.
	 */
	public String getDescription() {
		// Read the description text from the "<description>" child element
		IConfigurationElement[] children = configElement != null ? configElement.getChildren("description") : null; //$NON-NLS-1$
		// Only one description element is allow. All other will be ignored
		if (children != null && children.length > 0) {
			IConfigurationElement description = children[0];
			String value = description.getValue();
			return value != null ? value.trim() : ""; //$NON-NLS-1$
		}

		return ""; //$NON-NLS-1$
	}
}
