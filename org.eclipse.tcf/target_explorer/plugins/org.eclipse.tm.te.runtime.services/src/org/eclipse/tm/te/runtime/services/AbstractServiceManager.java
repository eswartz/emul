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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.te.runtime.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.services.interfaces.IService;

/**
 * Abstract service manager implementation.
 */
public abstract class AbstractServiceManager<V extends IService> {

	// map for all services per id
	private Map<String, List<ServiceProxy>> services = new HashMap<String, List<ServiceProxy>>();

	/**
	 * Proxy to provide lazy loading of contributing plug-ins.
	 */
	protected class ServiceProxy {

		private IConfigurationElement configElement = null;
		public String clazz;
		private V service = null;
		private List<Class<? extends V>> serviceTypes = new ArrayList<Class<? extends V>>();

		/**
		 * Constructor.
		 */
		protected ServiceProxy(IConfigurationElement configElement) {
			Assert.isNotNull(configElement);
			this.configElement = configElement;

			// Read the class attribute. If null, check for the class sub element
			clazz = configElement.getAttribute("class"); //$NON-NLS-1$
			if (clazz == null) {
				IConfigurationElement[] children = configElement.getChildren("class"); //$NON-NLS-1$
				// Single element definition assumed (see extension point schema)
				if (children.length > 0) {
					clazz = children[0].getAttribute("class"); //$NON-NLS-1$
				}
			}
		}

		/**
		 * Add a type to the proxy. Types are used unless the proxy is instantiated to provide lazy
		 * loading of services. After instantiated, a service will be identified only by its type
		 * and implementing or extending interfaces or super-types.
		 *
		 * @param serviceType The type to add.
		 */
		public void addType(Class<? extends V> serviceType) {
			Assert.isNotNull(serviceType);
			if (service == null && serviceTypes != null && !serviceTypes.contains(serviceType)) {
				serviceTypes.add(serviceType);
			}
		}

		/**
		 * Return the real service instance for this proxy.
		 */
		@SuppressWarnings("unchecked")
		protected V getService(boolean unique) {
			if ((service == null || unique) && configElement != null) {
				try {
					// Create the service class instance via the configuration element
					Object service = configElement.createExecutableExtension("class"); //$NON-NLS-1$
					if (service instanceof IService) {
						if (unique) {
							return (V) service;
						}
						else if (service instanceof IService) {
							this.service = (V)service;
						}
						else {
							IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "Service '" + service.getClass().getName() + "' not of type IService."); //$NON-NLS-1$ //$NON-NLS-2$
							Platform.getLog(CoreBundleActivator.getContext().getBundle())
							                .log(status);
						}
					}
				}
				catch (CoreException e) {
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "Cannot create service '" + clazz + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
					Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
				}
				if (serviceTypes != null) {
					serviceTypes.clear();
				}
				serviceTypes = null;
			}
			return service;
		}

		/**
		 * Check whether this proxy holds a service that is suitable for the given type.
		 *
		 * @param serviceType The service type
		 * @return
		 */
		protected boolean isMatching(Class<? extends V> serviceType) {
			if (service != null) {
				return serviceType.isInstance(service);
			}
			else if (configElement != null) {
				if (serviceType.getClass().getName().equals(clazz)) {
					return true;
				}
				for (Class<? extends V> type : serviceTypes) {
					if (type.equals(serviceType)) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean equals(V service) {
			return clazz.equals(service.getClass());
		}

		public boolean equals(ServiceProxy proxy) {
			return clazz.equals(proxy.clazz);
		}
	}

	/**
	 * Constructor.
	 */
	protected AbstractServiceManager() {
		loadServices();
	}

	/**
	 * @param element
	 * @return
	 */
	protected ServiceProxy getServiceProxy(IConfigurationElement element) {
		return new ServiceProxy(element);
	}

	/**
	 * Returns all id's of the registered services.
	 *
	 * @return The list of id's of the registered services.
	 */
	public String[] getIds() {
		return services.keySet().toArray(new String[services.keySet().size()]);
	}

	/**
	 * Get a service for the id that implements at least the needed service type. If an interface
	 * type is given, the service with the highest implementation is returned. This may result in a
	 * random selection depending on the extension registration order, especially when a service
	 * interface is implemented two times in different hierarchy paths. If a class type is given, if
	 * available, the service of exactly that class is returned. Otherwise the highest
	 * implementation is returned.
	 *
	 * @param id The id for which a service is needed.
	 * @param serviceType The service type the service should at least implement or extend.
	 * @return The service or <code>null</code>.
	 */
	public V getService(String id, Class<? extends V> serviceType) {
		return getService(id, serviceType, false);
	}

	/**
	 * Get a service for the id that implements at least the needed service type. If an interface
	 * type is given, the service with the highest implementation is returned. This may result in a
	 * random selection depending on the extension registration order, especially when a service
	 * interface is implemented two times in different hierarchy paths. If a class type is given, if
	 * available, the service of exactly that class is returned. Otherwise the highest
	 * implementation is returned.
	 *
	 * @param id The id for which a service is needed.
	 * @param serviceType The service type the service should at least implement or extend.
	 * @param unique <code>true</code> if a new instance of the service is needed.
	 *
	 * @return The service or <code>null</code>.
	 */
	public V getService(String id, Class<? extends V> serviceType, boolean unique) {
		Assert.isNotNull(serviceType);
		if (id == null) {
			id = ""; //$NON-NLS-1$
		}
		List<ServiceProxy> proxies = services.get(id);
		if (proxies != null && !proxies.isEmpty()) {
			List<ServiceProxy> candidates = new ArrayList<ServiceProxy>();
			boolean isInterface = serviceType.isInterface();
			for (ServiceProxy proxy : proxies) {
				if (proxy.isMatching(serviceType)) {
					if (!isInterface && proxy.equals(serviceType)) {
						V service = proxy.getService(unique);
						service.setId(id);
						return service;
					}
					candidates.add(proxy);
				}
			}
			V service = null;
			if (!candidates.isEmpty()) {
				service = candidates.get(0).getService(unique);
				service.setId(id);
			}

			return service;
		}
		return null;
	}

	/**
	 * Get a service list for the id that implements at least the needed service type.
	 *
	 * @param id The id for which a service is needed.
	 * @param serviceType The service type the service should at least implement or extend.
	 * @param unique <code>true</code> if a new instance of the service is needed.
	 * @return The service list or empty list.
	 */
	public IService[] getServices(String id, Class<? extends V> serviceType, boolean unique) {
		Assert.isNotNull(serviceType);
		if (id == null) {
			id = ""; //$NON-NLS-1$
		}
		List<ServiceProxy> proxies = services.get(id);
		List<IService> services = new ArrayList<IService>();
		if (proxies != null && !proxies.isEmpty()) {
			List<ServiceProxy> candidates = new ArrayList<ServiceProxy>();
			for (ServiceProxy proxy : proxies) {
				if (proxy.isMatching(serviceType)) {
					candidates.add(proxy);
				}
			}
			for (ServiceProxy serviceProxy : candidates) {
				IService service = serviceProxy.getService(unique);
				service.setId(id);
				services.add(service);
			}
		}
		return services.toArray(new IService[services.size()]);
	}

	/*
	 * Add a service proxy to the list of available services.
	 */
	protected boolean addService(String id, ServiceProxy proxy) {
		Assert.isNotNull(services);
		Assert.isNotNull(id);
		Assert.isNotNull(proxy);

		List<ServiceProxy> proxies = services.get(id);
		if (proxies == null) {
			proxies = new ArrayList<ServiceProxy>();
			services.put(id, proxies);
		}
		Assert.isNotNull(proxies);
		if (proxies.isEmpty() || !proxies.contains(proxy)) {
			return proxies.add(proxy);
		}
		return false;
	}

	/**
	 * Loads the contributed services into proxies (lazy loading!!) and adds them to this manager;
	 */
	protected abstract void loadServices();
}
