/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.te.core.concurrent.interfaces.IExecutor;
import org.eclipse.tm.te.core.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.core.extensions.ExecutableExtensionProxy;


/**
 * Class is providing the entry points to create or query the executor service
 * instances.
 */
public final class Executors {

	/**
	 * Execution service extension point manager.
	 */
	protected static class ExecutorServiceExtensionPointManager extends AbstractExtensionPointManager<IExecutor> {

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.tm.te.core.extensions.AbstractExtensionPointManager#
		 * getExtensionPointId()
		 */
		@Override
		protected String getExtensionPointId() {
			return "org.eclipse.tm.te.core.concurrent.executorServices"; //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.tm.te.core.extensions.AbstractExtensionPointManager#
		 * getConfigurationElementName()
		 */
		@Override
		protected String getConfigurationElementName() {
			return "executorService"; //$NON-NLS-1$
		}

		/**
		 * Returns the list of all contributed executors.
		 *
		 * @return The list of contributed executors, or an empty array.
		 */
		public IExecutor[] getExecutors() {
			List<IExecutor> contributions = new ArrayList<IExecutor>();
			Collection<ExecutableExtensionProxy<IExecutor>> proxies = getExtensions().values();
			for (ExecutableExtensionProxy<IExecutor> proxy : proxies)
				if (proxy.getInstance() != null
						&& !contributions.contains(proxy.getInstance()))
					contributions.add(proxy.getInstance());

			return contributions.toArray(new IExecutor[contributions.size()]);
		}

		/**
		 * Returns the executor identified by its unique id. If no executor with
		 * the specified id is registered, <code>null</code> is returned.
		 *
		 * @param id
		 *            The unique id of the executor. Must not be
		 *            <code>null</code>
		 * @param newInstance
		 *            Specify <code>true</code> to get a new executor service
		 *            instance, <code>false</code> otherwise.
		 *
		 * @return The executor instance or <code>null</code>.
		 */
		public IExecutor getExecutor(String id, boolean newInstance) {
			Assert.isNotNull(id);

			IExecutor executorService = null;
			if (getExtensions().containsKey(id)) {
				ExecutableExtensionProxy<IExecutor> proxy = getExtensions().get(id);
				// Get the extension instance
				executorService = newInstance ? proxy.newInstance() : proxy.getInstance();
			}

			return executorService;
		}
	}

	// Reference to the executor service extension point manager
	private final static ExecutorServiceExtensionPointManager EXTENSION_POINT_MANAGER = new ExecutorServiceExtensionPointManager();

	/**
	 * Constructor.
	 * <p>
	 * <b>Note:</b> The class cannot be instantiated.
	 */
	private Executors() {
	}

	/**
	 * Creates an instance of the executor registered with the specified id. If
	 * no executor is registered under the given id, the method will return
	 * <code>null</code>.
	 *
	 * @param id
	 *            The id of the executor. Must not be <code>null</code>.
	 * @return The new executor instance or <code>null</code>.
	 */
	public static IExecutor newExecutor(String id) {
		Assert.isNotNull(id);
		return EXTENSION_POINT_MANAGER.getExecutor(id, true);
	}

	/**
	 * Returns the shared instance of the executor registered with the specified
	 * id. If the shared instance hasn't been created yet, the instance will be
	 * created and saved. Subsequent calls to this method with the same id will
	 * return always the same executor instance. If no executor is registered
	 * under the given id, the method will return <code>null</code>.
	 *
	 * @param id
	 *            The id of the executor. Must not be <code>null</code>.
	 * @return The new executor instance or <code>null</code>.
	 */
	public static IExecutor getSharedExecutor(String id) {
		Assert.isNotNull(id);
		return EXTENSION_POINT_MANAGER.getExecutor(id, false);
	}

	/**
	 * Returns the shared instances of all registered executors.
	 *
	 * @return All executor instances or an empty array.
	 */
	public static IExecutor[] getAllSharedExecutors() {
		return EXTENSION_POINT_MANAGER.getExecutors();
	}
}
