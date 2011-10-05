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
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * Streams connector settings implementation.
 */
@SuppressWarnings("restriction")
public class StreamsSettings {
	// Reference to the stdin stream
	private OutputStream stdin;
	// Reference to the stdout stream
	private InputStream stdout;
	// Reference to the stderr stream
	private InputStream stderr;
	// Flag to control the local echo
	private boolean localEcho = true;

	/**
	 * Sets the stdin stream instance.
	 *
	 * @param stdin The stream instance or <code>null</code>.
	 */
	public void setStdinStream(OutputStream stdin) {
		this.stdin = stdin;
	}

	/**
	 * Returns the stdin stream instance.
	 *
	 * @return The stream instance or <code>null</code>.
	 */
	public OutputStream getStdinStream() {
		return stdin;
	}

	/**
	 * Sets the stdout stream instance.
	 *
	 * @param stdout The stream instance or <code>null</code>.
	 */
	public void setStdoutStream(InputStream stdout) {
		this.stdout = stdout;
	}

	/**
	 * Returns the stdout stream instance.
	 *
	 * @return The stream instance or <code>null</code>.
	 */
	public InputStream getStdoutStream() {
		return stdout;
	}

	/**
	 * Sets the stderr stream instance.
	 *
	 * @param stderr The stream instance or <code>null</code>.
	 */
	public void setStderrStream(InputStream stderr) {
		this.stderr = stderr;
	}

	/**
	 * Returns the stderr stream instance.
	 *
	 * @return The stream instance or <code>null</code>.
	 */
	public InputStream getStderrStream() {
		return stderr;
	}

	/**
	 * Sets if the process requires a local echo from the terminal widget.
	 *
	 * @param value Specify <code>true</code> to enable the local echo, <code>false</code> otherwise.
	 */
	public void setLocalEcho(boolean value) {
		this.localEcho = value;
	}

	/**
	 * Returns <code>true</code> if the process requires a local echo
	 * from the terminal widget.
	 *
	 * @return <code>True</code> if local echo is enabled, <code>false</code> otherwise.
	 */
	public boolean isLocalEcho() {
		return localEcho;
	}

	/**
	 * Loads the streams settings from the given settings store.
	 *
	 * @param store The settings store. Must not be <code>null</code>.
	 */
	public void load(ISettingsStore store) {
		Assert.isNotNull(store);
		localEcho = Boolean.parseBoolean(store.get("LocalEcho", Boolean.FALSE.toString())); //$NON-NLS-1$
		if (store instanceof IPropertiesContainer) {
			stdin = (OutputStream)((IPropertiesContainer)store).getProperty("stdin"); //$NON-NLS-1$
			stdout = (InputStream)((IPropertiesContainer)store).getProperty("stdout"); //$NON-NLS-1$
			stderr = (InputStream)((IPropertiesContainer)store).getProperty("stderr"); //$NON-NLS-1$
		}
	}

	/**
	 * Saves the process settings to the given settings store.
	 *
	 * @param store The settings store. Must not be <code>null</code>.
	 */
	public void save(ISettingsStore store) {
		Assert.isNotNull(store);
		store.put("LocalEcho", Boolean.toString(localEcho)); //$NON-NLS-1$
		if (store instanceof IPropertiesContainer) {
			((IPropertiesContainer)store).setProperty("stdin", stdin); //$NON-NLS-1$
			((IPropertiesContainer)store).setProperty("stdout", stdout); //$NON-NLS-1$
			((IPropertiesContainer)store).setProperty("stderr", stderr); //$NON-NLS-1$
		}
	}
}
