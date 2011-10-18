/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * Interface to be implemented by classes providing a remote terminals launcher.
 */
public interface ITerminalsLauncher extends IAdaptable {

	/**
	 * Property denoting the terminal PTY type.
	 * <p>
	 * <b>Note:</b> If not explicitly specified, the terminal type defaults to &quot;ansi&quot;.
	 * <p>
	 * The property type is {@link String}.
	 */
	public static String PROP_TERMINAL_TYPE = "terminals.type"; //$NON-NLS-1$

	/**
	 * Property denoting the terminal encoding.
	 * <p>
	 * The property type is {@link String}.
	 */
	public static String PROP_TERMINAL_ENCODING = "terminals.cwd"; //$NON-NLS-1$

	/**
	 * Property denoting the terminal environment.
	 * <p>
	 * The property type is {@link Map}&lt; {@link String}, {@link String} &gt;.
	 */
	public static String PROP_TERMINAL_ENV = "terminals.env"; //$NON-NLS-1$

	/**
	 * Property denoting if the terminal is redirecting it's output to an file.
	 * <p>
	 * The property type is {@link String}.
	 */
	public static String PROP_TERMINAL_OUTPUT_REDIRECT_TO_FILE = "terminal.redirectToFile"; //$NON-NLS-1$

	/**
	 * Property denoting the full name of the connection the launcher got invoked for.
	 * <p>
	 * The property type is {@link String}.
	 */
	public static String PROP_CONNECTION_NAME = "connection.name"; //$NON-NLS-1$

	/**
	 * Launch a remote terminal defined by the given launch properties at the target specified by the
	 * given peer.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @param params The remote terminal properties. Must not be <code>null</code>.
	 * @param callback The callback or <code>null</code>.
	 */
	public void launch(IPeer peer, IPropertiesContainer properties, ICallback callback);

	/**
	 * Disposes the remote terminals launcher instance.
	 */
	public void dispose();

	/**
	 * Exit the launched terminal (if still running).
	 */
	public void exit();
}
