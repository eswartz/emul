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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.te.ui.terminals.activator.UIPlugin;
import org.eclipse.tm.te.ui.terminals.nls.Messages;
import org.eclipse.ui.services.IDisposable;

/**
 * Input stream monitor implementation.
 * <p>
 * <b>Note:</b> The input is coming <i>from</i> the terminal. Therefore, the input
 * stream monitor is attached to the stdin stream of the monitored (remote) process.
 */
@SuppressWarnings("restriction")
public class InputStreamMonitor extends OutputStream implements IDisposable {
    // Reference to the parent terminal control
    @SuppressWarnings("unused")
	private final ITerminalControl terminalControl;

	// Reference to the monitored (output) stream
    private final OutputStream stream;

    // Reference to the thread writing the stream
    private Thread thread;

    // Flag to mark the monitor disposed. When disposed,
    // no further data is written from the monitored stream.
    private boolean disposed;

    // A list of object to dispose if this monitor is disposed
    private final List<IDisposable> disposables = new ArrayList<IDisposable>();

    // Queue to buffer the data to write to the output stream
    private final Queue<byte[]> queue = new LinkedList<byte[]>();

    /**
     * Constructor.
     *
     * @param terminalControl The parent terminal control. Must not be <code>null</code>.
     * @param stream The stream. Must not be <code>null</code>.
     */
	public InputStreamMonitor(ITerminalControl terminalControl, OutputStream stream) {
    	super();

    	Assert.isNotNull(terminalControl);
    	this.terminalControl = terminalControl;
    	Assert.isNotNull(stream);
        this.stream = stream;
    }

	/**
	 * Adds the given disposable object to the list. The method will do nothing
	 * if either the disposable object is already part of the list or the monitor
	 * is disposed.
	 *
	 * @param disposable The disposable object. Must not be <code>null</code>.
	 */
	public final void addDisposable(IDisposable disposable) {
		Assert.isNotNull(disposable);
		if (!disposed && !disposables.contains(disposable)) disposables.add(disposable);
	}

	/**
	 * Removes the disposable object from the list.
	 *
	 * @param disposable The disposable object. Must not be <code>null</code>.
	 */
	public final void removeDisposable(IDisposable disposable) {
		Assert.isNotNull(disposable);
		disposables.remove(disposable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		// If already disposed --> return immediately
		if (disposed) return;

		// Mark the monitor disposed
    	disposed = true;

        // Close the stream (ignore exceptions on close)
        try { stream.close(); } catch (IOException e) { /* ignored on purpose */ }
        // And interrupt the thread
        close();

        // Dispose all registered disposable objects
        for (IDisposable disposable : disposables) disposable.dispose();
        // Clear the list
        disposables.clear();
	}

    /**
     * Close the terminal input stream monitor.
     */
    @Override
	public void close() {
    	// Not initialized -> return immediately
    	if (thread == null) return;

    	// Copy the reference
    	final Thread oldThread = thread;
    	// Unlink the monitor from the thread
    	thread = null;
    	// And interrupt the writer thread
    	oldThread.interrupt();
    }

    /**
     * Starts the terminal output stream monitor.
     */
    public void startMonitoring() {
    	// If already initialized -> return immediately
    	if (thread != null) return;

    	// Create a new runnable which is constantly reading from the stream
    	Runnable runnable = new Runnable() {
    		@Override
			public void run() {
    			writeStream();
    		}
    	};

    	// Create the writer thread
    	thread = new Thread(runnable, "Terminal Input Stream Monitor Thread"); //$NON-NLS-1$

    	// Configure the writer thread
        thread.setDaemon(true);

        // Start the processing
        thread.start();
    }


    /**
     * Reads from the queue and writes the read content to the stream.
     */
    protected void writeStream() {
    	// Read from the queue and write to the stream until disposed
        while (thread != null && !disposed) {
            // If the queue is empty, wait until notified
        	if (queue.isEmpty()) {
        		synchronized(queue) {
        			try { queue.wait(); } catch (InterruptedException e) { /* ignored on purpose */ }
        		}
        	}

        	// If the queue is not empty, take the first element
        	// and write the data to the stream
            while (!queue.isEmpty() && !disposed) {
            	// Retrieves the queue head (is null if queue is empty (should never happen))
                byte[] data = queue.poll();
                if (data != null) {
                	try {
                		// Write the data to the stream
                		stream.write(data);
                		// Flush the stream immediately
                		stream.flush();
                	} catch (IOException e) {
                    	// IOException received. If this is happening when already disposed -> ignore
        				if (!disposed) {
        					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
        												NLS.bind(Messages.InputStreamMonitor_error_writingToStream, e.getLocalizedMessage()), e);
        					UIPlugin.getDefault().getLog().log(status);
        				}
                	}
                }
            }
        }

        // Dispose the stream
        dispose();
    }

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
    @Override
    public void write(int b) throws IOException {
        synchronized(queue) {
            queue.add(new byte[] { (byte)b });
            queue.notifyAll();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        // Make sure that the written block is not interlaced with other input.
        synchronized(queue) {
            super.write(b, off, len);
        }
    }
}
