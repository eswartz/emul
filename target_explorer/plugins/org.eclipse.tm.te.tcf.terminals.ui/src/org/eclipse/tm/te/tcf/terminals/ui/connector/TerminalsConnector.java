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

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ITerminals;
import org.eclipse.tm.te.tcf.terminals.core.launcher.TerminalsLauncher;
import org.eclipse.tm.te.ui.terminals.streams.AbstractStreamsConnector;
import org.eclipse.ui.services.IDisposable;

/**
 * Terminals connector implementation.
 */
@SuppressWarnings("restriction")
public class TerminalsConnector extends AbstractStreamsConnector implements IDisposable {
	// Reference to the terminals settings
	private final TerminalsSettings settings;

	/**
	 * Constructor.
	 */
	public TerminalsConnector() {
		this(new TerminalsSettings());
	}

	/**
	 * Constructor.
	 *
	 * @param settings The streams settings. Must not be <code>null</code>
	 */
	public TerminalsConnector(TerminalsSettings settings) {
		super();

		Assert.isNotNull(settings);
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#connect(org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl)
	 */
	@Override
	public void connect(ITerminalControl control) {
		Assert.isNotNull(control);
		super.connect(control);

		// connect the streams
		connectStreams(control, settings.getStdinStream(), settings.getStdoutStream(), settings.getStderrStream(), settings.isLocalEcho(), settings.getLineSeparator());

		// Set the terminal control state to CONNECTED
		control.setState(TerminalState.CONNECTED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#isLocalEcho()
	 */
	@Override
	public boolean isLocalEcho() {
		return settings.isLocalEcho();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		disconnect();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#doDisconnect()
	 */
	@Override
	public void doDisconnect() {
		// Dispose the streams
		super.doDisconnect();

		// Set the terminal control state to CLOSED.
		fControl.setState(TerminalState.CLOSED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#setTerminalSize(int, int)
	 */
	@Override
	public void setTerminalSize(final int newWidth, final int newHeight) {
		if (fControl.getState() == TerminalState.CONNECTED && settings.getTerminalsLauncher() instanceof TerminalsLauncher) {
			final ITerminals service = ((TerminalsLauncher)settings.getTerminalsLauncher()).getSvcTerminals();
			final ITerminals.TerminalContext context = (ITerminals.TerminalContext)settings.getTerminalsLauncher().getAdapter(ITerminals.TerminalContext.class);
			if (service != null && context != null) {
				Protocol.invokeLater(new Runnable() {
					@Override
					public void run() {
						service.setWinSize(context.getID(), newWidth, newHeight, new ITerminals.DoneCommand() {
							@Override
							public void doneCommand(IToken token, Exception error) {
							}
						});
					}
				});
			}
		}
	}

	// ***** Connector settings handling *****

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#getSettingsSummary()
	 */
	@Override
	public String getSettingsSummary() {
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#load(org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore)
	 */
	@Override
	public void load(ISettingsStore store) {
		settings.load(store);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#save(org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore)
	 */
	@Override
	public void save(ISettingsStore store) {
		settings.save(store);
	}
}
