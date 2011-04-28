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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.te.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.core.nls.Messages;


/**
 * Target Explorer: Executable extension proxy implementation.
 * 
 * @since 1.0
 */
public class ExecutableExtensionProxy<V> {
	// The extension instance. Created on first access
	private V fInstance;
	// The configuration element
	private final IConfigurationElement fElement;
	// The unique id of the extension.
	private String fId;

	/**
	 * Constructor.
	 *
	 * @param element The configuration element. Must not be <code>null</code>.
	 * @throws CoreException In case the configuration element attribute <i>id</i> is <code>null</code> or empty.
	 * @since 1.0
	 */
	public ExecutableExtensionProxy(IConfigurationElement element) throws CoreException {
		assert element != null;
		fElement = element;

		// Extract the extension attributes
		fId = element.getAttribute("id"); //$NON-NLS-1$
		if (fId == null || fId.trim().length() == 0) {
			throw new CoreException(new Status(IStatus.ERROR,
					CoreBundleActivator.getUniqueIdentifier(),
					0,
					NLS.bind(Messages.Extension_error_missingRequiredAttribute, "id", element.getContributor().getName()), //$NON-NLS-1$
					null));
		}

		fInstance = null;
	}

	/**
	 * Constructor.
	 *
	 * @param id The id for this instance.
	 * @param instance The instance to add to proxy.
	 * @since 1.0
	 */
	public ExecutableExtensionProxy(String id, V instance) {
		assert id!= null && instance != null;
		fId = id;
		fInstance = instance;
		fElement = null;
	}

	/**
	 * Returns the extensions unique id.
	 *
	 * @return The unique id.
	 * @since 1.0
	 */
	public String getId() {
		return fId;
	}

	/**
	 * Returns the configuration element for this extension.
	 *
	 * @return The configuration element.
	 * @since 1.0
	 */
	public IConfigurationElement getConfigurationElement() {
		return fElement;
	}

	/**
	 * Reset the extension instance to <code>null</code> and force the
	 * creation of a new extension instance on the next {@link #getInstance()}
	 * method invocation.
	 *
	 * @return The current extension instance or <code>null</code> if none.
	 * @since 1.0
	 */
	public V reset() {
		V oldExtension = fInstance;
		fInstance = null;
		return oldExtension;
	}

	/**
	 * Returns the extension class instance. The contributing
	 * plug-in will be activated if not yet activated anyway.
	 *
	 * @return The extension class instance or <code>null</code> if the instantiation fails.
	 * @since 1.0
	 */
	public V getInstance() {
		if (fInstance == null) fInstance = newInstance();
		return fInstance;
	}

	/**
	 * Returns always a new extension class instance which is different
	 * to what {@link #getInstance()} would return.
	 *
	 * @return A new extension class instance or <code>null</code> if the instantiation fails.
	 * @since 1.0
	 */
	@SuppressWarnings("unchecked")
	public V newInstance() {
		IConfigurationElement element = getConfigurationElement();
		assert element != null;
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
	 * @since 1.0
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
	 * @since 1.0
	 */
	@Override
	public int hashCode() {
		// The hash code of a proxy is the one from the id
		return getId().hashCode();
	}
}
