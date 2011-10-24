/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.services;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.te.runtime.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.services.interfaces.IService;
import org.eclipse.tm.te.runtime.services.nls.Messages;
import org.osgi.framework.Bundle;

/**
 * Common service manager implementation, handling the extension point
 * <code>org.eclipse.tm.te.runtime.services</code>.
 */
public class ServiceManager extends AbstractServiceManager<IService> {
	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static ServiceManager instance = new ServiceManager();
	}

	/**
	 * Constructor.
	 */
	ServiceManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the service manager.
	 */
	public static ServiceManager getInstance() {
		return LazyInstance.instance;
	}

	/**
	 * Get a global unbound service that implements at least the needed service type.
	 *
	 * If an interface type is given, the service with the highest implementation is returned.
	 * This may result in a random selection depending on the extension registration order,
	 * especially when a service interface is implemented two times in different hierarchy paths.
	 *
	 * If a class type is given, if available, the service of exactly that class is returned.
	 * Otherwise the highest implementation is returned.
	 *
	 * @param serviceType The service type the service should at least implement or extend.
	 * @return The service or <code>null</code>.
	 */
	public IService getService(Class<? extends IService> serviceType, boolean unique) {
		return super.getService("", serviceType, unique); //$NON-NLS-1$
	}

	/**
	 * Get a global unbound service that implements at least the needed service type.
	 *
	 * If an interface type is given, the service with the highest implementation is returned.
	 * This may result in a random selection depending on the extension registration order,
	 * especially when a service interface is implemented two times in different hierarchy paths.
	 *
	 * If a class type is given, if available, the service of exactly that class is returned.
	 * Otherwise the highest implementation is returned.
	 *
	 * @param serviceType The service type the service should at least implement or extend.
	 * @return The service or <code>null</code>.
	 */
	public IService getService(Class<? extends IService> serviceType) {
		return super.getService("", serviceType); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.services.AbstractServiceManager#loadServices()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void loadServices() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.tm.te.runtime.services.services"); //$NON-NLS-1$
		if (ep != null) {
			IExtension[] extensions = ep.getExtensions();
			if (extensions != null) {
				for (IExtension extension : extensions) {
					IConfigurationElement[] configElements = extension.getConfigurationElements();
					if (configElements != null) {
						for (IConfigurationElement configElement : configElements) {
							// Determine the unique id to bind the service contributions to.
							String id = null;

							if ("connectionTypeServices".equals(configElement.getName())) { //$NON-NLS-1$
								id = configElement.getAttribute("connectionTypeId"); //$NON-NLS-1$

								// For a connection type service declaration, the connection type id is mandatory
								if (id == null || "".equals(id)) { //$NON-NLS-1$
									IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(),
																NLS.bind(Messages.ServiceManager_warning_skippedConnectionTypeService, configElement.getDeclaringExtension().getNamespaceIdentifier()));
									Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
									continue;
								}
							}
							else if ("genericServices".equals(configElement.getName())) { //$NON-NLS-1$
								id = configElement.getAttribute("id"); //$NON-NLS-1$
							}

							// Normalize the id
							if (id == null) id = ""; //$NON-NLS-1$

							// Get the service contributions
							IConfigurationElement[] services = configElement.getChildren("service"); //$NON-NLS-1$
							// Process the service contributions
							for (IConfigurationElement service : services) {
								ServiceProxy proxy = getServiceProxy(service);
								IConfigurationElement[] serviceTypes = service.getChildren("serviceType"); //$NON-NLS-1$
								if (serviceTypes != null && serviceTypes.length > 0) {
									for (IConfigurationElement serviceType : serviceTypes) {
										try {
											String type = serviceType.getAttribute("class"); //$NON-NLS-1$
											String bundleId = serviceType.getAttribute("bundleId"); //$NON-NLS-1$

											// If a bundle id got specified, use the specified bundle to load the service class
											Bundle bundle = bundleId != null ? bundle = Platform.getBundle(bundleId) : null;
											// If we don't have a bundle to load from yet, fallback to the declaring bundle
											if (bundle == null) bundle = Platform.getBundle(configElement.getDeclaringExtension().getNamespaceIdentifier());
											// And finally, use our own bundle to load the class.
											// This fallback is expected to never be used.
											if (bundle == null) bundle = CoreBundleActivator.getContext().getBundle();

											// Try to load the service type class now.
											Class<?> typeClass = bundle != null ? bundle.loadClass(type) : Class.forName(type);
											proxy.addType((Class<IService>)typeClass);
										}
										catch (Exception e) {
											IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(),
																		NLS.bind(Messages.ServiceManager_warning_failedToLoadServiceType, serviceType.getAttribute("class"), service.getAttribute("class")), e); //$NON-NLS-1$ //$NON-NLS-2$
											Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
										}
									}
								}
								if (!addService(id, proxy)) {
									IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(),
																NLS.bind(Messages.ServiceManager_warning_failedToBindService, proxy.clazz, id), null);
									Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
								}
							}
						}
					}
				}
			}
		}
	}

}
