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

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.te.ui.terminals.internal.SettingsStore;
import org.eclipse.tm.te.ui.terminals.streams.StreamsSettings;

/**
 * Streams terminal connector type implementation.
 */
@SuppressWarnings("restriction")
public class StreamsConnectorType extends AbstractConnectorType {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.terminals.interfaces.IConnectorType#createTerminalConnector(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
    @Override
	public ITerminalConnector createTerminalConnector(IPropertiesContainer properties) {
		Assert.isNotNull(properties);

    	// Check for the terminal connector id
    	String connectorId = properties.getStringProperty(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);

		// Extract the streams properties
		OutputStream stdin = (OutputStream)properties.getProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDIN);
		InputStream stdout = (InputStream)properties.getProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDOUT);
		InputStream stderr = (InputStream)properties.getProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDERR);
		boolean localEcho = properties.getBooleanProperty(ITerminalsConnectorConstants.PROP_LOCAL_ECHO);

		return createStreamsConnector(connectorId, stdin, stdout, stderr, localEcho);
	}

	/**
	 * Creates a terminal connector object based on the given stream objects.
	 *
	 * @param connectorId The terminal connector id or <code>null</code>.
	 * @param stdin The stdin stream or <code>null</code>.
	 * @param stdout The stdout stream or <code>null</code>.
	 * @param stderr The stderr stream or <code>null</code>.
	 * @param localEcho <code>True</code> if the terminal widget local echo shall be enabled, <code>false</code> otherwise.
	 *
	 * @return The terminal connector object instance or <code>null</code>.
	 */
	protected ITerminalConnector createStreamsConnector(String connectorId, OutputStream stdin, InputStream stdout, InputStream stderr, boolean localEcho) {
		// Construct the terminal settings store
		ISettingsStore store = new SettingsStore();

		if (connectorId == null) connectorId = "org.eclipse.tm.te.ui.terminals.StreamsConnector"; //$NON-NLS-1$

		// Construct the streams settings
		StreamsSettings streamsSettings = new StreamsSettings();
		streamsSettings.setStdinStream(stdin);
		streamsSettings.setStdoutStream(stdout);
		streamsSettings.setStderrStream(stderr);
		streamsSettings.setLocalEcho(localEcho);
		// And save the settings to the store
		streamsSettings.save(store);

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
