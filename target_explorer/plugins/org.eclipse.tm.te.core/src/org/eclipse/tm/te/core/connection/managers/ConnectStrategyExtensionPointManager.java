/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.connection.managers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tm.te.core.connection.interfaces.IConnectStrategy;
import org.eclipse.tm.te.core.connection.strategy.ConnectStrategyStepGroup;
import org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup;

/**
 * Connect strategy extension point manager implementation.
 * <p>
 * The class is not intended to be subclassed by clients.
 */
public class ConnectStrategyExtensionPointManager extends AbstractExtensionPointManager<IConnectStrategy> {

	protected class ConnectStrategyExtensionPointProxy extends ExecutableExtensionProxy<IConnectStrategy> {
		private final Map<String, IContextStepGroup> stepGroups = new LinkedHashMap<String, IContextStepGroup>();

		public ConnectStrategyExtensionPointProxy(IConfigurationElement element) throws CoreException {
			super(element);
			loadGroups(element);
		}

		private void loadGroups(IConfigurationElement element) {
			for (IConfigurationElement stepGroupsElement : element.getChildren("stepGroups")) { //$NON-NLS-1$
				for (IConfigurationElement stepGroupElement : stepGroupsElement.getChildren("stepGroup")) { //$NON-NLS-1$
					IContextStepGroup stepGroup = new ConnectStrategyStepGroup();
					try {
						stepGroup.setInitializationData(stepGroupElement, stepGroupElement.getName(), null);
						stepGroups.put(stepGroup.getId(), stepGroup);
					}
					catch (CoreException e) {
					}
				}
			}
		}

		public IContextStepGroup getStepGroup(String stepGroupId) {
			return stepGroups.get(stepGroupId);
		}
	}

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyConnectStrategyExtensionPointManager {
		public static ConnectStrategyExtensionPointManager instance = new ConnectStrategyExtensionPointManager();
	}

	/**
	 * Returns the singleton instance for the connection type bindings manager.
	 */
	public static ConnectStrategyExtensionPointManager getInstance() {
		return LazyConnectStrategyExtensionPointManager.instance;
	}

	/**
     * Constructor.
     */
    /* default */ ConnectStrategyExtensionPointManager() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
     */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tm.te.core.connectStrategies"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "connectStrategy"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#doCreateExtensionProxy(org.eclipse.core.runtime.IConfigurationElement)
	 */
	@Override
	protected ExecutableExtensionProxy<IConnectStrategy> doCreateExtensionProxy(IConfigurationElement element) throws CoreException {
		return new ConnectStrategyExtensionPointProxy(element);
	}

	/**
	 * Returns the connect strategy instance for the given id.
	 *
	 * @param id The connect strategy id or <code>null</code>.
	 * @return Returns the connect strategy instance or <code>null</code>.
	 */
	public IConnectStrategy getConnectStrategy(String connectStrategyId) {
		if (connectStrategyId == null) {
			return null;
		}

		IConnectStrategy connectStrategy = null;
		if (getExtensions().containsKey(connectStrategyId)) {
			ExecutableExtensionProxy<IConnectStrategy> proxy = getExtensions().get(connectStrategyId);
			// Get the extension instance
			connectStrategy = proxy.getInstance();
		}

		return connectStrategy;
	}

	public IContextStepGroup getStepGroup(String connectStrategyId, String stepGroupId) {
		if (connectStrategyId == null || stepGroupId == null) {
			return null;
		}

		if (getExtensions().containsKey(connectStrategyId)) {
			ConnectStrategyExtensionPointProxy proxy = (ConnectStrategyExtensionPointProxy)getExtensions().get(connectStrategyId);
			return proxy.getStepGroup(stepGroupId);
		}
		return null;
	}
}
