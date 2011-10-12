/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.process;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.te.ui.terminals.internal.SettingsStore;
import org.eclipse.tm.te.ui.terminals.types.AbstractConnectorType;

/**
 * Streams terminal connector type implementation.
 */
@SuppressWarnings("restriction")
public class ProcessConnectorType extends AbstractConnectorType {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.terminals.interfaces.IConnectorType#createTerminalConnector(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
    @Override
	public ITerminalConnector createTerminalConnector(IPropertiesContainer properties) {
		Assert.isNotNull(properties);

    	// Check for the terminal connector id
    	String connectorId = properties.getStringProperty(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);

		// Extract the process properties
		String path = properties.getStringProperty(ITerminalsConnectorConstants.PROP_PROCESS_PATH);
		String arguments = properties.getStringProperty(ITerminalsConnectorConstants.PROP_PROCESS_ARGS);
		Process process = (Process)properties.getProperty(ITerminalsConnectorConstants.PROP_PROCESS_OBJ);
		PTY pty = (PTY)properties.getProperty(ITerminalsConnectorConstants.PROP_PTY_OBJ);
		boolean localEcho = properties.getBooleanProperty(ITerminalsConnectorConstants.PROP_LOCAL_ECHO);

		return createProcessConnector(connectorId, path, arguments, process, pty, localEcho);
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
}
