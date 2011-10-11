/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.process.manager;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.te.ui.terminals.internal.SettingsStore;
import org.eclipse.tm.te.ui.terminals.process.ProcessSettings;


/**
 * Process terminal connector manager implementation.
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
	 * Returns the singleton instance.
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
