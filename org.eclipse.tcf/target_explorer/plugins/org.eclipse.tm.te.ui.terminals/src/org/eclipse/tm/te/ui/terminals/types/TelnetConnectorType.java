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

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.internal.terminal.telnet.TelnetSettings;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.te.ui.terminals.internal.SettingsStore;

/**
 * Telnet terminal connector type implementation.
 */
@SuppressWarnings("restriction")
public class TelnetConnectorType extends AbstractConnectorType {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.terminals.interfaces.IConnectorType#createTerminalConnector(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
    @Override
	public ITerminalConnector createTerminalConnector(IPropertiesContainer properties) {
    	Assert.isNotNull(properties);

    	// Check for the terminal connector id
    	String connectorId = properties.getStringProperty(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);

		// Extract the telnet properties
		String host = properties.getStringProperty(ITerminalsConnectorConstants.PROP_IP_HOST);
		String port = properties.getStringProperty(ITerminalsConnectorConstants.PROP_IP_PORT);
		String timeout = properties.getStringProperty(ITerminalsConnectorConstants.PROP_TIMEOUT);

		int portOffset = 0;
		if (properties.getProperty(ITerminalsConnectorConstants.PROP_IP_PORT_OFFSET) != null) {
			portOffset = properties.getIntProperty(ITerminalsConnectorConstants.PROP_IP_PORT_OFFSET);
			if (portOffset < 0) portOffset = 0;
		}

		return host != null && port != null ? createTelnetConnector(connectorId, new String[] { host, port, timeout }, portOffset) : null;
	}

	/**
	 * Creates a terminal connector object based on the given telnet server attributes.
	 * <p>
	 * The telnet server attributes must contain at least 2 elements:
	 * <ul>
	 * <li>attributes[0] --> telnet server host name</li>
	 * <li>attributes[1] --> telnet port</li>
	 * <li>attributes[2] --> timeout (optional)</li>
	 * </ul>
	 *
	 * @param connectorId The terminal connector id or <code>null</code>.
	 * @param attributes The telnet server attributes. Must not be <code>null</code> and must have at least two elements.
	 * @param portOffset Offset to add to the port.
	 *
	 * @return The terminal connector object instance or <code>null</code>.
	 */
	protected ITerminalConnector createTelnetConnector(String connectorId, String[] attributes, int portOffset) {
		Assert.isNotNull(attributes);
		Assert.isTrue(attributes.length >= 2);

		if (connectorId == null) connectorId = "org.eclipse.tm.internal.terminal.telnet.TelnetConnector"; //$NON-NLS-1$

		final String serverName = attributes[0];
		final String serverPort = Integer.toString(Integer.decode(attributes[1]).intValue() + portOffset);
		final String timeout = attributes.length >= 3 ? attributes[2] : null;

		// Construct the terminal settings store
		ISettingsStore store = new SettingsStore();

		// Construct the telnet settings
		TelnetSettings telnetSettings = new TelnetSettings();
		telnetSettings.setHost(serverName);
		telnetSettings.setNetworkPort(serverPort);
		if (timeout != null) {
			telnetSettings.setTimeout(timeout);
		}
		// And save the settings to the store
		telnetSettings.save(store);

		// Construct the terminal connector instance
		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector(connectorId);
		if (connector != null) {
			// Apply default settings
			connector.makeSettingsPage();
			// And load the real settings
			connector.load(store);
		}

		return connector;
	}
}
