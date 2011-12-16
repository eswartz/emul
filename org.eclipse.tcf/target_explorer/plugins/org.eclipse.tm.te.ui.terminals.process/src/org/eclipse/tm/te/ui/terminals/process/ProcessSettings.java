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
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * Process connector settings implementation.
 */
@SuppressWarnings("restriction")
public class ProcessSettings {
	// Reference to the process image
	private String image;
	// Reference to the process arguments (space separated string)
	private String arguments;
	// Reference to the process object
	private Process process;
	// Reference to the pseudo terminal object
	private PTY pty;
	// Flag to control the local echo (defaults to true if
	// the PTY is not supported on the current host platform)
	private boolean localEcho = !PTY.isSupported();
	// The line separator setting
	private String lineSeparator = null;

	/**
	 * Sets the process image.
	 *
	 * @param image The process image or <code>null</code>.
	 */
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * Returns the process image.
	 *
	 * @return The process image or <code>null</code>.
	 */
	public String getImage() {
		return image;
	}

	/**
	 * Sets the process arguments.
	 * <p>
	 * The arguments are space separated. The caller is responsible for
	 * correct quoting.
	 *
	 * @param arguments The process arguments or <code>null</code>.
	 */
	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	/**
	 * Returns the process arguments.
	 *
	 * @return The process arguments as space separated list or <code>null</code>.
	 */
	public String getArguments() {
		return arguments;
	}

	/**
	 * Sets the process object.
	 *
	 * @param image The process object or <code>null</code>.
	 */
	public void setProcess(Process process) {
		this.process = process;
	}

	/**
	 * Returns the process object.
	 *
	 * @return The process object or <code>null</code>.
	 */
	public Process getProcess() {
		return process;
	}

	/**
	 * Sets the pseudo terminal object.
	 *
	 * @param pty The pseudo terminal or <code>null</code>.
	 */
	public void setPTY(PTY pty) {
		this.pty = pty;
		// If the PTY is set to "null", the local echo will be set to "true"
		if (pty == null) setLocalEcho(true);
	}

	/**
	 * Returns the pseudo terminal object.
	 *
	 * @return The pseudo terminal or <code>null</code>.
	 */
	public PTY getPTY() {
		return pty;
	}

	/**
	 * Sets if the process requires a local echo from the
	 * terminal widget.
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
	 * Sets the process line separator.
	 *
	 * @param separator The process line separator <code>null</code>.
	 */
	public void setLineSeparator(String separator) {
		this.lineSeparator = separator;
	}

	/**
	 * Returns the process line separator.
	 *
	 * @return The process line separator or <code>null</code>.
	 */
	public String getLineSeparator() {
		return lineSeparator;
	}

	/**
	 * Loads the process settings from the given settings store.
	 *
	 * @param store The settings store. Must not be <code>null</code>.
	 */
	public void load(ISettingsStore store) {
		Assert.isNotNull(store);
		image = store.get("Path", null);//$NON-NLS-1$
		arguments = store.get("Arguments", null); //$NON-NLS-1$
		localEcho = Boolean.parseBoolean(store.get("LocalEcho", Boolean.FALSE.toString())); //$NON-NLS-1$
		lineSeparator = store.get("LineSeparator", null); //$NON-NLS-1$
		if (store instanceof IPropertiesContainer) {
			process = (Process)((IPropertiesContainer)store).getProperty("Process"); //$NON-NLS-1$
			pty = (PTY)((IPropertiesContainer)store).getProperty("PTY"); //$NON-NLS-1$
		}
	}

	/**
	 * Saves the process settings to the given settings store.
	 *
	 * @param store The settings store. Must not be <code>null</code>.
	 */
	public void save(ISettingsStore store) {
		Assert.isNotNull(store);
		store.put("Path", image);//$NON-NLS-1$
		store.put("Arguments", arguments); //$NON-NLS-1$
		store.put("LocalEcho", Boolean.toString(localEcho)); //$NON-NLS-1$
		store.put("LineSeparator", lineSeparator); //$NON-NLS-1$
		if (store instanceof IPropertiesContainer) {
			((IPropertiesContainer)store).setProperty("Process", process); //$NON-NLS-1$
			((IPropertiesContainer)store).setProperty("PTY", pty); //$NON-NLS-1$
		}
	}
}
