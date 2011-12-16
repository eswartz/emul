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

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * Interface to be implemented by classes providing a remote process launcher.
 */
public interface IProcessLauncher extends IAdaptable {
	/**
	 * Property denoting the process image path. The process image path is the absolute remote path.
	 * <p>
	 * The property type is {@link String}.
	 */
	public static String PROP_PROCESS_PATH = "process.path"; //$NON-NLS-1$

	/**
	 * Property denoting the process image path of a monitored application. The process image path
	 * is the absolute remote path.
	 * <p>
	 * <b>Note:</b> Optional: The monitored process path property might be set if the process path
	 * contains the image path of a monitoring application, like mpatrol. This property influence
	 * the terminal title construction only.
	 * <p>
	 * The property type is {@link String}.
	 */
	public static String PROP_PROCESS_MONITORED_PATH = "process.monitored.path"; //$NON-NLS-1$

	/**
	 * Property denoting the process arguments.
	 * <p>
	 * <b>Note:</b> The arguments are passed as is to the launched remote process. In case of shell
	 * scripts, the client must assure that the first argument ($0) is the absolute process image
	 * path.
	 * <p>
	 * The property type is {@link String}[].
	 */
	public static String PROP_PROCESS_ARGS = "process.args"; //$NON-NLS-1$

	/**
	 * Property denoting the process working directory.
	 * <p>
	 * The property type is {@link String}.
	 */
	public static String PROP_PROCESS_CWD = "process.cwd"; //$NON-NLS-1$

	/**
	 * Property denoting the process environment.
	 * <p>
	 * The property type is {@link Map}&lt; {@link String}, {@link String} &gt;.
	 */
	public static String PROP_PROCESS_ENV = "process.env"; //$NON-NLS-1$

	/**
	 * Property denoting if the process is launched attached or not.
	 * <p>
	 * The property type is {@link Boolean}.
	 */
	public static String PROP_PROCESS_ATTACH = "process.attach"; //$NON-NLS-1$

	/**
	 * Property denoting if the process is associated with an input/output console.
	 * <p>
	 * The property type is {@link Boolean}.
	 */
	public static String PROP_PROCESS_ASSOCIATE_CONSOLE = "process.associateConsole"; //$NON-NLS-1$

	/**
	 * Property denoting if the process is redirecting it's output to an file.
	 * <p>
	 * The property type is {@link String}.
	 */
	public static String PROP_PROCESS_OUTPUT_REDIRECT_TO_FILE = "process.redirectToFile"; //$NON-NLS-1$

	/**
	 * Property denoting the full name of the target connection the launcher got invoked for.
	 * <p>
	 * The property type is {@link String}.
	 */
	public static String PROP_CONNECTION_NAME = "connection.name"; //$NON-NLS-1$

	/**
	 * Launch a remote process defined by the given launch properties at the target specified by the
	 * given peer.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @param params The remote process properties. Must not be <code>null</code>.
	 * @param callback The callback or <code>null</code>.
	 */
	public void launch(IPeer peer, IPropertiesContainer properties, ICallback callback);

	/**
	 * Disposes the remote process launcher instance.
	 */
	public void dispose();

	/**
	 * Terminates the launched remote process (if still running).
	 */
	public void terminate();
}
