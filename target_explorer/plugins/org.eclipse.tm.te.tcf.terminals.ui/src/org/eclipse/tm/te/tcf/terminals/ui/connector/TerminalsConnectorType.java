/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.terminals.ui.connector;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsLauncher;
import org.eclipse.tm.te.ui.terminals.internal.SettingsStore;
import org.eclipse.tm.te.ui.terminals.types.AbstractConnectorType;

/**
 * Terminals terminal connector type implementation.
 */
@SuppressWarnings("restriction")
public class TerminalsConnectorType extends AbstractConnectorType {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.terminals.interfaces.IConnectorType#createTerminalConnector(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
    @Override
	public ITerminalConnector createTerminalConnector(IPropertiesContainer properties) {
		Assert.isNotNull(properties);

    	// Check for the terminal connector id
    	String connectorId = properties.getStringProperty(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);
		if (connectorId == null) connectorId = "org.eclipse.tm.te.tcf.terminals.ui.TerminalsConnector"; //$NON-NLS-1$

		// Extract the streams properties
		OutputStream stdin = (OutputStream)properties.getProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDIN);
		InputStream stdout = (InputStream)properties.getProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDOUT);
		InputStream stderr = (InputStream)properties.getProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDERR);
		boolean localEcho = properties.getBooleanProperty(ITerminalsConnectorConstants.PROP_LOCAL_ECHO);
		String lineSeparator = properties.getStringProperty(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR);
		ITerminalsLauncher launcher = (ITerminalsLauncher)properties.getProperty(ITerminalsConnectorConstants.PROP_DATA);

		// Construct the terminal settings store
		ISettingsStore store = new SettingsStore();

		// Construct the terminals settings
		TerminalsSettings terminalsSettings = new TerminalsSettings();
		terminalsSettings.setStdinStream(stdin);
		terminalsSettings.setStdoutStream(stdout);
		terminalsSettings.setStderrStream(stderr);
		terminalsSettings.setLocalEcho(localEcho);
		terminalsSettings.setLineSeparator(lineSeparator);
		terminalsSettings.setTerminalsLauncher(launcher);
		// And save the settings to the store
		terminalsSettings.save(store);

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
