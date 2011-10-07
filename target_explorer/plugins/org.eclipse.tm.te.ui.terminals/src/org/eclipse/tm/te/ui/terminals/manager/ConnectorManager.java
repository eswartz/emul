/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.manager;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.internal.terminal.telnet.TelnetSettings;
import org.eclipse.tm.te.ui.terminals.internal.SettingsStore;
import org.eclipse.tm.te.ui.terminals.process.ProcessSettings;
import org.eclipse.tm.te.ui.terminals.streams.StreamsSettings;


/**
 * Terminal connector manager implementation.
 */
@SuppressWarnings("restriction")
public class ConnectorManager {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstanceHolder {
		public static ConnectorManager instance = new ConnectorManager();
	}

	/**
	 * Returns the singleton instance for the terminal connector manager.
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

	/**
	 * Creates a terminal connector object based on the given telnet server attributes.
	 * <p>
	 * The telnet server attributes must contain 2 elements:
	 * <ul>
	 * <li>attributes[0] --> telnet server host name</li>
	 * <li>attributes[1] --> telnet port</li>
	 * <li>attributes[2] --> timeout</li>
	 * </ul>
	 *
	 * @param attributes The telnet server attributes. Must not be <code>null</code> and must have at least two elements.
	 * @param portOffset Offset to add to the port.
	 *
	 * @return The terminal connector object instance or <code>null</code>.
	 */
	public ITerminalConnector createTelnetConnector(String[] attributes) {
		return createTelnetConnector(attributes, 0);
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
	 * @param attributes The telnet server attributes. Must not be <code>null</code> and must have at least two elements.
	 * @param portOffset Offset to add to the port.
	 *
	 * @return The terminal connector object instance or <code>null</code>.
	 */
	public ITerminalConnector createTelnetConnector(String[] attributes, int portOffset) {
		Assert.isNotNull(attributes);
		Assert.isTrue(attributes.length >= 2);

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
		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector("org.eclipse.tm.internal.terminal.telnet.TelnetConnector"); //$NON-NLS-1$
		if (connector != null) {
			// Apply default settings
			connector.makeSettingsPage();
			// And load the real settings
			connector.load(store);
		}

		return connector;
	}

	/**
	 * Creates a terminal connector object based on the given process image
	 * and the corresponding process object.
	 *
	 * @param connectorTypeId Optional ID of the specific process connector implementation to use.
	 *                        If <code>null</code>, the default process connector will be used.
	 * @param image The process image path. Must not be <code>null</code>.
	 * @param arguments The process arguments or <code>null</code>.
	 * @param process The process. Must not be <code>null</code>.
	 * @param pty The pseudo terminal or <code>null</code>.
	 * @param localEcho <code>True</code> if the terminal widget local echo shall be enabled, <code>false</code> otherwise.
	 *
	 * @return The terminal connector object instance or <code>null</code>.
	 */
	public ITerminalConnector createProcessConnector(String connectorTypeId, final String image, final String arguments, final Process process, PTY pty, boolean localEcho) {
		Assert.isTrue(image != null || process != null);

		// Normalize the process connector id
		if (connectorTypeId == null) connectorTypeId = "org.eclipse.tm.te.ui.terminals.ProcessConnector"; //$NON-NLS-1$

		// Construct the terminal settings store
		ISettingsStore store = new SettingsStore();

		// Construct the process settings
		ProcessSettings processSettings = new ProcessSettings();
		processSettings.setImage(image);
		processSettings.setArguments(arguments);
		processSettings.setProcess(process);
        processSettings.setPTY(pty);
        processSettings.setLocalEcho(localEcho);
		// And save the settings to the store
		processSettings.save(store);

		// Construct the terminal connector instance
		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector(connectorTypeId);
		if (connector != null) {
			// Apply default settings
			connector.makeSettingsPage();
			// And load the real settings
			connector.load(store);
		}

		return connector;
	}

	/**
	 * Creates a terminal connector object based on the given stream objects.
	 *
	 * @param stdin The stdin stream or <code>null</code>.
	 * @param stdout The stdout stream or <code>null</code>.
	 * @param stderr The stderr stream or <code>null</code>.
	 * @param localEcho <code>True</code> if the terminal widget local echo shall be enabled, <code>false</code> otherwise.
	 *
	 * @return The terminal connector object instance or <code>null</code>.
	 */
	public ITerminalConnector createStreamsConnector(OutputStream stdin, InputStream stdout, InputStream stderr, boolean localEcho) {

		// Construct the terminal settings store
		ISettingsStore store = new SettingsStore();

		// Construct the streams settings
		StreamsSettings streamsSettings = new StreamsSettings();
		streamsSettings.setStdinStream(stdin);
		streamsSettings.setStdoutStream(stdout);
		streamsSettings.setStderrStream(stderr);
		streamsSettings.setLocalEcho(localEcho);
		// And save the settings to the store
		streamsSettings.save(store);

		// Construct the terminal connector instance
		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector("org.eclipse.tm.te.ui.terminals.StreamsConnector"); //$NON-NLS-1$
		if (connector != null) {
			// Apply default settings
			connector.makeSettingsPage();
			// And load the real settings
			connector.load(store);
		}

		return connector;
	}
}
