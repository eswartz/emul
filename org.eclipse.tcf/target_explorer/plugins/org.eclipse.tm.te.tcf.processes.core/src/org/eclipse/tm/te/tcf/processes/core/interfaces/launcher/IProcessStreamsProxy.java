/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.core.interfaces.launcher;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;

/**
 * An interface to be implemented by clients to receive the remote process I/O instead of the
 * standard terminal console.
 */
public interface IProcessStreamsProxy {

	/**
	 * Connects the stream proxy with the output stream of the remote process.
	 *
	 * @param stream The stream connected to the remote process output stream, or <code>null</code>.
	 */
	public void connectOutputStreamMonitor(InputStream stream);

	/**
	 * Connects the stream proxy with the input stream of the remote process.
	 *
	 * @param stream The stream connected to the remote process input stream, or <code>null</code>.
	 */
	public void connectInputStreamMonitor(OutputStream stream);

	/**
	 * Connects the stream proxy with the error stream of the remote process.
	 *
	 * @param stream The stream connected to the remote process error stream, or <code>null</code>.
	 */
	public void connectErrorStreamMonitor(InputStream stream);

	/**
	 * Dispose the streams proxy instance.
	 *
	 * @param callback The callback to invoke or <code>null</code>.
	 */
	public void dispose(ICallback callback);
}
