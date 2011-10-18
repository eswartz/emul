/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.ui.terminals.interfaces.IConnectorType;

/**
 * Terminal connector type extension point manager implementation.
 */
public class ConnectorManager extends AbstractExtensionPointManager<IConnectorType> {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstanceHolder {
		public static ConnectorManager instance = new ConnectorManager();
	}

	/**
	 * Returns the singleton instance for the terminal connector type extension point manager.
	 */
	public static ConnectorManager getInstance() {
		return LazyInstanceHolder.instance;
	}

	/**
	 * Constructor.
	 */
	ConnectorManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tm.te.ui.terminals.connectorTypes"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "connectorType"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed terminal connector types.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed terminal connector type.
	 *
	 * @return The list of contributed terminal connector types, or an empty array.
	 */
	public IConnectorType[] getConnectorTypes(boolean unique) {
		List<IConnectorType> contributions = new ArrayList<IConnectorType>();
		Collection<ExecutableExtensionProxy<IConnectorType>> connectorTypes = getExtensions().values();
		for (ExecutableExtensionProxy<IConnectorType> connectorType : connectorTypes) {
			IConnectorType instance = unique ? connectorType.newInstance() : connectorType.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new IConnectorType[contributions.size()]);
	}

	/**
	 * Returns the terminal connector type identified by its unique id. If no terminal
	 * connector type with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the terminal connector type or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the terminal connector type contribution.
	 *
	 * @return The terminal connector type instance or <code>null</code>.
	 */
	public IConnectorType getConnectorType(String id, boolean unique) {
		IConnectorType contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<IConnectorType> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}
}
