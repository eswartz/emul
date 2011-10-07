/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.streams;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;
import org.eclipse.ui.services.IDisposable;

/**
 * Streams connector implementation.
 */
@SuppressWarnings("restriction")
public abstract class AbstractStreamsConnector extends TerminalConnectorImpl {
	// Reference to the stdin monitor
    private InputStreamMonitor stdInMonitor;
    // Reference to the stdout monitor
    private OutputStreamMonitor stdOutMonitor;
    // Reference to the stderr monitor
    private OutputStreamMonitor stdErrMonitor;

    /**
     * Connect the given streams. The streams connector will wrap each stream
     * with a corresponding terminal stream monitor.
     *
     * @param terminalControl The terminal control. Must not be <code>null</code>.
     * @param stdin The stdin stream or <code>null</code>.
     * @param stdout The stdout stream or <code>null</code>.
     * @param stderr The stderr stream or <code>null</code>.
     */
    protected void connectStreams(ITerminalControl terminalControl, OutputStream stdin, InputStream stdout, InputStream stderr) {
    	Assert.isNotNull(terminalControl);

    	// Create the input stream monitor
    	if (stdin != null) {
    		stdInMonitor = createStdInMonitor(terminalControl, stdin);
    		// Register the connector if it implements IDisposable and stdout/stderr are not monitored
    		if (stdout == null && stderr == null && this instanceof IDisposable) stdInMonitor.addDisposable((IDisposable)this);
    		// Start the monitoring
    		stdInMonitor.startMonitoring();
    	}

    	// Create the output stream monitor
    	if (stdout != null) {
    		stdOutMonitor = createStdOutMonitor(terminalControl, stdout);
    		// Register the connector if it implements IDisposable
    		if (this instanceof IDisposable) stdOutMonitor.addDisposable((IDisposable)this);
    		// Start the monitoring
    		stdOutMonitor.startMonitoring();
    	}

    	// Create the error stream monitor
    	if (stderr != null) {
    		stdErrMonitor = createStdErrMonitor(terminalControl, stderr);
    		// Register the connector if it implements IDisposable and stdout is not monitored
    		if (stdout == null && this instanceof IDisposable) stdErrMonitor.addDisposable((IDisposable)this);
    		// Start the monitoring
    		stdErrMonitor.startMonitoring();
    	}
    }

    /**
     * Creates an stdin monitor for the given terminal control and stdin stream.
     * Subclasses may override to create a specialized stream monitor.
     *
     * @param terminalControl The terminal control. Must not be <code>null</code>.
     * @param stdin The stdin stream or <code>null</code>.
     * @return input stream monitor
     */
    protected InputStreamMonitor createStdInMonitor(ITerminalControl terminalControl, OutputStream stdin) {
        return new InputStreamMonitor(terminalControl, stdin);
    }

    /**
     * Creates an stdout monitor for the given terminal control and stdout stream.
     * Subclasses may override to create a specialized stream monitor.
     *
     * @param terminalControl The terminal control. Must not be <code>null</code>.
     * @param stdout The stdout stream or <code>null</code>.
     * @return output stream monitor
     */
    protected OutputStreamMonitor createStdOutMonitor(ITerminalControl terminalControl, InputStream stdout) {
        return new OutputStreamMonitor(terminalControl, stdout);
    }

    /**
     * Creates an stderr monitor for the given terminal control and stderr stream.
     * Subclasses may override to create a specialized stream monitor.
     *
     * @param terminalControl The terminal control. Must not be <code>null</code>.
     * @param stderr The stderr stream or <code>null</code>.
     * @return output stream monitor
     */
    protected OutputStreamMonitor createStdErrMonitor(ITerminalControl terminalControl, InputStream stderr) {
        return new OutputStreamMonitor(terminalControl, stderr);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#doDisconnect()
     */
    @Override
    protected void doDisconnect() {
    	// Dispose the streams
        if (stdInMonitor != null) { stdInMonitor.dispose(); stdInMonitor = null; }
        if (stdOutMonitor != null) { stdOutMonitor.dispose(); stdOutMonitor = null; }
        if (stdErrMonitor != null) { stdErrMonitor.dispose(); stdErrMonitor = null; }

    	super.doDisconnect();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#getTerminalToRemoteStream()
	 */
	@Override
	public OutputStream getTerminalToRemoteStream() {
		return stdInMonitor;
	}

}
