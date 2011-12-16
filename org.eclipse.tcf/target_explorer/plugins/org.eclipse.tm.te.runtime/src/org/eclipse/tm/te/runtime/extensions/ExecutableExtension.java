/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.extensions;

import java.util.Hashtable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.te.runtime.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;
import org.eclipse.tm.te.runtime.nls.Messages;

/**
 * Executable extension implementation.
 */
public class ExecutableExtension extends PlatformObject implements IExecutableExtension {
	// The mandatory id of the extension
	private String id = null;
	// The label of the extension
	private String label = null;
	// The description of the extension
	private String description = null;

	/**
	 * Clone the initialization data to the given executable extension instance.
	 *
	 * @param other The destination executable extension instance. Must not be <code>null</code>.
	 */
	public void cloneInitializationData(ExecutableExtension other) {
		Assert.isNotNull(other);
		other.id = id;
		other.label = label;
		other.description = description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		if (config != null) doSetInitializationData(config, propertyName, data);
	}

	/**
	 * Executes the {@link #setInitializationData(IConfigurationElement, String, Object)}.
	 *
	 * @param config The configuration element. Must not be <code>null</code>.
	 * @param propertyName The name of an attribute of the configuration element used on the <code>createExecutableExtension(String)<code> call.
	 *                     This argument can be used in the cases where a single configuration element is used to define multiple
	 *                     executable extensions.
 	 * @param data Adapter data in the form of a String, a {@link Hashtable}, or <code>null</code>.
 	 *
	 * @throws CoreException - if error(s) detected during initialization processing
	 */
	public void doSetInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		Assert.isNotNull(config);

		// Initialize the id field by reading the <id> extension attribute.
		// Throws an exception if the id is empty or null.
		id = config != null ? config.getAttribute("id") : null; //$NON-NLS-1$
		if (id == null || (id != null && "".equals(id.trim()))) { //$NON-NLS-1$
			throw createMissingMandatoryAttributeException("id", config.getContributor().getName()); //$NON-NLS-1$
		}

		// Try the "label" attribute first
		label = config != null ? config.getAttribute("label") : null; //$NON-NLS-1$
		// If "label" is not found or empty, try the "name" attribute as fallback
		if (label == null || "".equals(label.trim())) { //$NON-NLS-1$
			label = config != null ? config.getAttribute("name") : null; //$NON-NLS-1$
		}

		// Read the description text from the "<description>" child element
		IConfigurationElement[] children = config != null ? config.getChildren("description") : null; //$NON-NLS-1$
		// Only one description element is allow. All other will be ignored
		if (children != null && children.length > 0) {
			IConfigurationElement element = children[0];
			description = element.getValue();
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

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension#getLabel()
	 */
	@Override
	public String getLabel() {
		return label != null ? label.trim() : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension#getDescription()
	 */
	@Override
	public String getDescription() {
		return description != null ? description.trim() : ""; //$NON-NLS-1$
	}
}
