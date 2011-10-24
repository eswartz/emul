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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.te.core.connection.interfaces.IConnectionType;
import org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;

/**
 * Connection type extension point manager implementation.
 * <p>
 * The class is not intended to be subclassed by clients.
 */
public final class ConnectionTypeExtensionPointManager extends AbstractExtensionPointManager<IConnectionType> {

	/**
     * Constructor.
     */
    /* default */ ConnectionTypeExtensionPointManager() {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tm.te.core.connectionTypes"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "connectionType"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed connection types.
	 *
	 * @return The list of contributed connection types, or an empty array.
	 */
	public IConnectionType[] getConnectionTypes() {
		List<IConnectionType> types = new ArrayList<IConnectionType>();
		Collection<ExecutableExtensionProxy<IConnectionType>> proxies = getExtensions().values();
		for (ExecutableExtensionProxy<IConnectionType> proxy : proxies)
			if (proxy.getInstance() != null && !types.contains(proxy.getInstance()))
				types.add(proxy.getInstance());

		return types.toArray(new IConnectionType[types.size()]);
	}

	/**
	 * Returns the connection type identified by its unique id. If no connection
	 * type with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the connection type. Must not be <code>null</code>
	 * @return The connection type or <code>null</code>.
	 */
	public IConnectionType getConnectionType(String id) {
		Assert.isNotNull(id);

		IConnectionType connectionType = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<IConnectionType> proxy = getExtensions().get(id);
			connectionType = proxy.getInstance();
		}

		return connectionType;
	}

}
