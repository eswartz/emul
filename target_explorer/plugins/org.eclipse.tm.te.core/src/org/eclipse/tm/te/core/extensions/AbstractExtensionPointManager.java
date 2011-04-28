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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.te.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.core.nls.Messages;

/**
 * Target Explorer: Abstract extension point manager implementation.
 * 
 * @since 1.0
 */
public abstract class AbstractExtensionPointManager<V> {
    // Flag to mark the extension point manager initialized (extensions loaded).
	private boolean fInitialized = false;
    // The map of loaded extension listed by their unique ids
	private Map<String, ExecutableExtensionProxy<V>> fExtensions = new LinkedHashMap<String, ExecutableExtensionProxy<V>>();

	/**
	 * Constructor.
	 * @since 1.0
	 */
	public AbstractExtensionPointManager() {
	}

	/**
	 * Returns if or if not the extension point manager got initialized already.
	 * <p>
	 * Initialized means that the manager read the extensions for the managed extension point.
	 *
	 * @return <code>True</code> if already initialized, <code>false</code> otherwise.
	 * @since 1.0
	 */
	protected boolean isInitialized() {
		return fInitialized;
	}

	/**
	 * Sets if or if not the extension point manager is initialized.
	 * <p>
	 * Initialized means that the manager has read the extensions for the managed extension point.
	 *
	 * @return <code>True</code> to set the extension point manager is initialized, <code>false</code> otherwise.
	 * @since 1.0
	 */
	protected void setInitialized(boolean initialized) {
		fInitialized = initialized;
	}

	/**
	 * Returns the map of managed extensions. If not loaded before,
	 * this methods trigger the loading of the extensions to the managed
	 * extension point.
	 *
	 * @return The map of extensions.
	 * @since 1.0
	 */
	protected Map<String, ExecutableExtensionProxy<V>> getExtensions() {
		// Load and store the extensions thread-safe!
		synchronized (fExtensions) {
			if (!isInitialized()) { loadExtensions(); setInitialized(true); }
		}
		return fExtensions;
	}

	/**
	 * Returns the extension point id to read. The method
	 * must return never <code>null</code>.
	 *
	 * @return The extension point id.
	 * @since 1.0
	 */
	protected abstract String getExtensionPointId();

	/**
	 * Returns the configuration element name. The method
	 * must return never <code>null</code>.
	 *
	 * @return The configuration element name.
	 * @since 1.0
	 */
	protected abstract String getConfigurationElementName();

	/**
	 * Creates the extension proxy instance.
	 *
	 * @param element The configuration element of the extension. Must not be <code>null</code>.
	 * @return The extension proxy instance.
     *
     * @throws CoreException If the extension proxy instantiation failed.
     * @since 1.0
	 */
	protected ExecutableExtensionProxy<V> doCreateExtensionProxy(IConfigurationElement element) throws CoreException {
		assert element != null;
		return new ExecutableExtensionProxy<V>(element);
	}

	/**
	 * Store the given extension to the given extensions store. Checks if an extension with the same id does exist
	 * already and throws an exception in this case.
	 *
	 * @param extensions The extensions store. Must not be <code>null</code>.
	 * @param candidate The extension. Must not be <code>null</code>.
	 * @param element The configuration element. Must not be <code>null</code>.
	 *
	 * @throws CoreException In case a extension with the same id as the given extension already exist.
	 * @since 1.0
	 */
	protected void doStoreExtensionTo(Map<String, ExecutableExtensionProxy<V>> extensions, ExecutableExtensionProxy<V> candidate, IConfigurationElement element) throws CoreException {
		assert extensions != null && candidate != null && element != null;

		// If no extension with this id had been registered before, register now.
		if (!fExtensions.containsKey(candidate.getId())) {
			fExtensions.put(candidate.getId(), candidate);
		}
		else {
			throw new CoreException(new Status(IStatus.ERROR,
			                    				CoreBundleActivator.getUniqueIdentifier(),
			                    				0,
			                    				NLS.bind(Messages.Extension_error_duplicateExtension, candidate.getId(), element.getContributor().getName()),
			                    				null));
		}
	}

	/**
	 * Loads the extensions for the managed extension point.
	 * @since 1.0
	 */
	protected void loadExtensions() {
		// If already initialized, this method will do nothing.
		if (isInitialized())  return;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(getExtensionPointId());
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (getConfigurationElementName().equals(element.getName())) {
						try {
							ExecutableExtensionProxy<V> candidate = doCreateExtensionProxy(element);
							if (candidate.getId() != null) {
								doStoreExtensionTo(fExtensions, candidate, element);
							} else {
								throw new CoreException(new Status(IStatus.ERROR,
										CoreBundleActivator.getUniqueIdentifier(),
									0,
									NLS.bind(Messages.Extension_error_missingRequiredAttribute, "id", element.getAttribute("label")), //$NON-NLS-1$ //$NON-NLS-2$
									null));
							}
						} catch (CoreException e) {
							Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(new Status(IStatus.ERROR,
									CoreBundleActivator.getUniqueIdentifier(),
									NLS.bind(Messages.Extension_error_invalidExtensionPoint, element.getDeclaringExtension().getUniqueIdentifier()), e));
						}
					}
				}
			}
		}
	}
}
