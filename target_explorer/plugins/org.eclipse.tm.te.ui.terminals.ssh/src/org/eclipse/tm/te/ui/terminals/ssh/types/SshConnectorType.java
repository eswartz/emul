/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361352] [TERMINALS][SSH] Add SSH terminal support
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.ssh.types;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.internal.terminal.ssh.SshSettings;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.te.ui.terminals.internal.SettingsStore;
import org.eclipse.tm.te.ui.terminals.types.AbstractConnectorType;

/**
 * Ssh terminal connector type implementation.
 */
@SuppressWarnings("restriction")
public class SshConnectorType extends AbstractConnectorType {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.terminals.interfaces.IConnectorType#createTerminalConnector(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
    @Override
	public ITerminalConnector createTerminalConnector(IPropertiesContainer properties) {
    	Assert.isNotNull(properties);

    	// Check for the terminal connector id
    	String connectorId = properties.getStringProperty(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);

		// Extract the ssh properties
		String host = properties.getStringProperty(ITerminalsConnectorConstants.PROP_IP_HOST);
		String port = properties.getStringProperty(ITerminalsConnectorConstants.PROP_IP_PORT);
		String timeout = properties.getStringProperty(ITerminalsConnectorConstants.PROP_TIMEOUT);
		String keepAlive=properties.getStringProperty(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE);
		String password=properties.getStringProperty(ITerminalsConnectorConstants.PROP_SSH_PASSWORD);
		String user=properties.getStringProperty(ITerminalsConnectorConstants.PROP_SSH_USER);

		int portOffset = 0;
		if (properties.getProperty(ITerminalsConnectorConstants.PROP_IP_PORT_OFFSET) != null) {
			portOffset = properties.getIntProperty(ITerminalsConnectorConstants.PROP_IP_PORT_OFFSET);
			if (portOffset < 0) portOffset = 0;
		}

		return host != null && port != null ? createSshConnector(connectorId, new String[] { host, port, timeout, keepAlive, password, user }, portOffset) : null;
	}

	/**
	 * Creates a ssh connector object based on the given ssh server attributes.
	 * <p>
	 * The ssh server attributes must contain at least 2 elements:
	 * <ul>
	 * <li>attributes[0] --> ssh server host name</li>
	 * <li>attributes[1] --> ssh port</li>
	 * <li>attributes[2] --> timeout</li>
	 * <li>attributes[3] --> keep alive</li>
	 * <li>attributes[4] --> ssh password</li>
	 * <li>attributes[5] --> ssh user</li>
	 * </ul>
	 *
	 * @param connectorId The terminal connector id or <code>null</code>.
	 * @param attributes The ssh server attributes. Must not be <code>null</code>.
	 * @param portOffset Offset to add to the port.
	 *
	 * @return The terminal connector object instance or <code>null</code>.
	 */
	protected ITerminalConnector createSshConnector(String connectorId, String[] attributes, int portOffset) {
		Assert.isNotNull(attributes);
		Assert.isTrue(attributes.length == 6);

		if (connectorId == null) connectorId = "org.eclipse.tm.internal.terminal.ssh.SshConnector"; //$NON-NLS-1$

		final String serverName = attributes[0];
		final String serverPort = Integer.toString(Integer.decode(attributes[1]).intValue() + portOffset);
		final String timeout = attributes[2];
		final String keepAlive=attributes[3];
		final String password=attributes[4];
		final String user=attributes[5];

		// Construct the ssh settings store
		ISettingsStore store = new SettingsStore();

		// Construct the telnet settings
		SshSettings sshSettings = new SshSettings();
		sshSettings.setHost(serverName);
		sshSettings.setPort(serverPort);
		sshSettings.setTimeout(timeout);
		sshSettings.setKeepalive(keepAlive);
		sshSettings.setPassword(password);
		sshSettings.setUser(user);

		// And save the settings to the store
		sshSettings.save(store);

		// MWE TODO make sure this is NOT passed outside as this is plain text
		store.put("Password", password);

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
