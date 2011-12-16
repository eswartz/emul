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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.te.core.connection.interfaces.IConnectStrategy;
import org.eclipse.tm.te.core.connection.interfaces.IConnectionType;

/**
 * Connection type bindings extension point manager implementation.
 */
public class ConnectionTypeBindingsManager {

	/**
	 * Immutable class describing a connect strategy binding.
	 */
	private final static class ConnectStrategyBinding {
		public final String id;
		public final String overwrite;

		public ConnectStrategyBinding(String id, String overwrite) {
			this.id = id;
			this.overwrite = overwrite;
		}
	}

	// Flag to remember if the extension point got already read and processed.
	private boolean initialized;

	// The map between connection type id's and their associated connect strategies.
	private final Map<String, List<ConnectStrategyBinding>> typeIdToStrategyId = new LinkedHashMap<String, List<ConnectStrategyBinding>>();

	// The sub extension point manager instances
	private final ConnectionTypeExtensionPointManager connectionTypeManger = new ConnectionTypeExtensionPointManager();

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyConnectionTypeBindingsManager {
		public static ConnectionTypeBindingsManager instance = new ConnectionTypeBindingsManager();
	}

	/**
	 * Returns the singleton instance for the connection type bindings manager.
	 */
	public static ConnectionTypeBindingsManager getInstance() {
		return LazyConnectionTypeBindingsManager.instance;
	}

	/**
	 * Constructor.
	 */
	ConnectionTypeBindingsManager() {
		initialized = false;
		initialize();
	}

	/**
	 * Initialize the connection type bindings manager and triggers to load and read the managed
	 * extension points.
	 */
	public void initialize() {
		if (initialized) {
			return;
		}
		// load and register the connection type bindings
		loadConnectionTypeBindings();
		initialized = true;
	}

	// ***** BEGIN: Section extension point management *****

	/**
	 * Load and register all connection type id shortcuts.
	 */
	private void loadConnectionTypeBindings() {
		// load all target connection type bindings and register them
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint("org.eclipse.tm.te.core.connectionTypeBindings"); //$NON-NLS-1$
		if (point != null) {
			// load target connection type bindings
			IExtension[] typeBindings = point.getExtensions();
			for (IExtension typeBinding : typeBindings) {
				// load configuration elements
				IConfigurationElement[] configElements = typeBinding.getConfigurationElements();
				for (IConfigurationElement configElement : configElements) {
					// handle connection type binding
					if ("connectionTypeBinding".equals(configElement.getName())) { //$NON-NLS-1$
						String connectionTypeId = configElement.getAttribute("connectionTypeId"); //$NON-NLS-1$
						// load connect strategy binding
						IConfigurationElement[] connectStrategy = configElement.getChildren("connectStrategy"); //$NON-NLS-1$
						for (IConfigurationElement element : connectStrategy) {
							String connectStrategyId = element.getAttribute("id"); //$NON-NLS-1$
							String connectStrategyOverwrite = element.getAttribute("overwrite"); //$NON-NLS-1$
							if (connectStrategyId != null && connectStrategyId.length() > 0) {
								registerConnectStrategyBinding(connectionTypeId.trim(), connectStrategyId.trim(), connectStrategyOverwrite != null ? connectStrategyOverwrite.trim() : null);
							}
						}
					}
				}
			}
		}
	}

	// ***** END: Section extension point management *****

	// ***** BEGIN: Section connect strategy management *****

	/**
	 * Register a binding between a connection type id and a connect strategy id. Bindings
	 * registered for the same connection type will be overwritten using the overwrite attribute. If
	 * the connect strategy id is <code>null</code>, possibly registered bindings are removed.
	 *
	 * @param typeId The connection type id. Must not be <code>null</code> and not empty.
	 * @param strategyId The connect strategy id or <code>null</code>.
	 * @param overwrite The connect strategy id that should be overwritten by this id.
	 */
	public void registerConnectStrategyBinding(String typeId, String strategyId, String overwrite) {
		Assert.isNotNull(typeId);
		Assert.isTrue(typeId.trim().length() > 0);

		if (strategyId != null && strategyId.trim().length() > 0) {
			List<ConnectStrategyBinding> bindings = typeIdToStrategyId.get(typeId.trim());
			if (bindings == null) {
				bindings = new ArrayList<ConnectStrategyBinding>();
				typeIdToStrategyId.put(typeId.trim(), bindings);
			}
			ConnectStrategyBinding binding = new ConnectStrategyBinding(strategyId.trim(), overwrite != null ? overwrite.trim() : null);
			bindings.add(binding);
			Collections.sort(bindings, new Comparator<ConnectStrategyBinding>() {
				@Override
                public int compare(ConnectStrategyBinding o1, ConnectStrategyBinding o2) {
					// handle  multiple id's without overwrite (alphabetical order)
					if (o1.overwrite == null && o2.overwrite == null) {
						return o1.id.compareTo(o2.id);
					}
					// handle overwrite the same id twice (alphabetical order)
					if (o1.overwrite != null && o2.overwrite != null && o1.overwrite.equals(o2.overwrite)) {
						return o1.id.compareTo(o2.id);
					}
					// handle recursive overwrite (alphabetical order)
					if (o1.overwrite != null && o1.overwrite.equals(o2.id) && o2.overwrite != null && o2.overwrite.equals(o1.id)) {
						return o1.id.compareTo(o2.id);
					}
					// o1 overwrites o2
					if (o1.overwrite != null && o1.overwrite.equals(o2.id)) {
						return -1;
					}
					// o2 overwrites o1
					if (o2.overwrite != null && o2.overwrite.equals(o1.id)) {
						return 1;
					}
					// fallback alphabetical order
					return o1.id.compareTo(o2.id);
				}
			});

		} else if (strategyId == null) {
			typeIdToStrategyId.remove(typeId.trim());
		}
	}

	/**
	 * Returns the corresponding connect strategy id for the given connection type id.
	 *
	 * @param typeId The connection type id. Must not be <code>null</code> and not empty!
	 * @return The connect strategy id if registered or <code>null</code>.
	 */
	public String getConnectStrategyId(String typeId) {
		if (typeId != null && typeId.trim().length() > 0) {
			List<ConnectStrategyBinding> bindings = typeIdToStrategyId.get(typeId.trim());
			return bindings != null && !bindings.isEmpty() ? bindings.get(0).id : null;
		}
		return null;
	}

	/**
	 * Returns the corresponding <code>IConnectStrategy</code> for the given connection type id.
	 *
	 * @param typeId The connection type id. Must not be <code>null</code>.
	 * @return The corresponding connect strategy object or <code>null</code>.
	 */
	public IConnectStrategy getConnectStrategy(String typeId) {
		Assert.isNotNull(typeId);
		String connectStrategyId = getConnectStrategyId(typeId);
		return ConnectStrategyExtensionPointManager.getInstance().getConnectStrategy(connectStrategyId);
	}

	// ***** END: Section connect strategy management *****

	// ***** BEGIN: Section connection type management *****

	/**
	 * Returns the list of all contributed connection types.
	 *
	 * @return The list of contributed connection types, or an empty array.
	 */
	public IConnectionType[] getConnectionTypes() {
		return connectionTypeManger.getConnectionTypes();
	}

	/**
	 * Returns the corresponding <code>IConnectionType</code> for the given connection type id.
	 *
	 * @param typeId The connection type id. Must not be <code>null</code>.
	 * @return The corresponding connection type object or <code>null</code>.
	 */
	public IConnectionType getConnectionType(String typeId) {
		Assert.isNotNull(typeId);
		return connectionTypeManger.getConnectionType(typeId);
	}

	/**
	 * Returns the corresponding <code>IConnectionType</code> for the given connectable context object.
	 *
	 * @param context The connectable context object. Must not be <code>null</code>
	 * @return The corresponding target connection type object or <code>null</code>.
	 */
	public IConnectionType getConnectionType(Object context) {
		Assert.isNotNull(context);
		return null;
	}

	/**
	 * Checks if the connection type, specified by the given type id, is enabled.
	 * <p>
	 * A connection type is enabled when at least one enablement evaluates to <code>true</code>.
	 *
	 * @param typeId The connection type id. Must not be <code>null</code>.
	 * @return <code>True</code> if the connection type is enabled, <code>false</code> otherwise.
	 */
	public boolean isConnectionTypeEnabled(String typeId) {
		Assert.isNotNull(typeId);
		return true;
	}

	// ***** END: Section connection type management *****
}
