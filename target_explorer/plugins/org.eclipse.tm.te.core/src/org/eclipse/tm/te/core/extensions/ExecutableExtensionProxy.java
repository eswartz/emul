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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.te.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.core.nls.Messages;


/**
 * Target Explorer: Executable extension proxy implementation.
 */
public class ExecutableExtensionProxy<V> {
	// The extension instance. Created on first access
	private V instance;
	// The configuration element
	private final IConfigurationElement element;
	// The unique id of the extension.
	private String id;

	/**
	 * Constructor.
	 *
	 * @param element The configuration element. Must not be <code>null</code>.
	 * @throws CoreException In case the configuration element attribute <i>id</i> is <code>null</code> or empty.
	 */
	public ExecutableExtensionProxy(IConfigurationElement element) throws CoreException {
		Assert.isNotNull(element);
		this.element = element;

		// Extract the extension attributes
		id = element.getAttribute("id"); //$NON-NLS-1$
		if (id == null || id.trim().length() == 0) {
			throw new CoreException(new Status(IStatus.ERROR,
					CoreBundleActivator.getUniqueIdentifier(),
					0,
					NLS.bind(Messages.Extension_error_missingRequiredAttribute, "id", element.getContributor().getName()), //$NON-NLS-1$
					null));
		}

		instance = null;
	}

	/**
	 * Constructor.
	 *
	 * @param id The id for this instance.
	 * @param instance The instance to add to proxy.
	 */
	public ExecutableExtensionProxy(String id, V instance) {
		Assert.isNotNull(id);
		Assert.isNotNull(instance);
		this.id = id;
		this.instance = instance;
		this.element = null;
	}

	/**
	 * Returns the extensions unique id.
	 *
	 * @return The unique id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the configuration element for this extension.
	 *
	 * @return The configuration element.
	 */
	public IConfigurationElement getConfigurationElement() {
		return element;
	}

	/**
	 * Reset the extension instance to <code>null</code> and force the
	 * creation of a new extension instance on the next {@link #getInstance()}
	 * method invocation.
	 *
	 * @return The current extension instance or <code>null</code> if none.
	 */
	public V reset() {
		V oldExtension = instance;
		instance = null;
		return oldExtension;
	}

	/**
	 * Returns the extension class instance. The contributing
	 * plug-in will be activated if not yet activated anyway.
	 *
	 * @return The extension class instance or <code>null</code> if the instantiation fails.
	 */
	public V getInstance() {
		if (instance == null) instance = newInstance();
		return instance;
	}

	/**
	 * Returns always a new extension class instance which is different
	 * to what {@link #getInstance()} would return.
	 *
	 * @return A new extension class instance or <code>null</code> if the instantiation fails.
	 */
	@SuppressWarnings("unchecked")
	public V newInstance() {
		IConfigurationElement element = getConfigurationElement();
		Assert.isNotNull(element);
		// The "class" to load can be specified either as attribute or as child element
		if (element != null && (element.getAttribute("class") != null || element.getChildren("class").length > 0)) { //$NON-NLS-1$ //$NON-NLS-2$
			try {
				return (V)element.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (Exception e) {
				// Possible exceptions: CoreException, ClassCastException.
				Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(new Status(IStatus.ERROR,
						CoreBundleActivator.getUniqueIdentifier(),
						NLS.bind(Messages.Extension_error_invalidExtensionPoint, element.getDeclaringExtension().getUniqueIdentifier()), e));
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// Proxie's are equal if they have encapsulate an element
		// with the same unique id
		if (obj instanceof ExecutableExtensionProxy<?>) {
			return getId().equals(((ExecutableExtensionProxy<?>)obj).getId());
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// The hash code of a proxy is the one from the id
		return getId().hashCode();
	}
}
