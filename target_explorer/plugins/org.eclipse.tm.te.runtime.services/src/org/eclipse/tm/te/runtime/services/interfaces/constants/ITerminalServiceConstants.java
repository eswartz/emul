/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.services.interfaces.constants;

/**
 * Defines the constants to be used with the terminal service.
 */
public interface ITerminalServiceConstants {

	/**
	 * Common terminal connector types.
	 */
	public enum ConnectorType { TELNET, SSH, SERIAL, PROCESS, STREAMS }

	/**
	 * Common terminal property: The unique id of the terminal console view to open.
	 */
	public static final String PROP_ID = "id"; //$NON-NLS-1$

	/**
	 * Common terminal property: The title of the terminal console tab to open.
	 */
	public static final String PROP_TITLE = "title"; //$NON-NLS-1$

	/**
	 * Common terminal property: Custom data object to associate with the terminal console tab.
	 */
	public static final String PROP_DATA = "data"; //$NON-NLS-1$

	/**
	 * Common terminal property: Terminal connector type like &quot;telnet&quot; or &quot;ssh&quot;.
	 */
	public static final String PROP_CONNECTOR_TYPE = "connector.type"; //$NON-NLS-1$

	/**
	 * Optional terminal property: Specific terminal connector type ID.  This
	 * property allows client to override the specific connector implementation
	 * for a given type.
	 */
	public static final String PROP_CONNECTOR_TYPE_ID = "connector.type.id"; //$NON-NLS-1$

	/**
	 * Common terminal property: Host name or IP address the terminal server is running.
	 *                           Typical for telnet or ssh terminal consoles.
	 */
	public static final String PROP_IP_HOST = "ip.host"; //$NON-NLS-1$

	/**
	 * Common terminal property: Port at which the terminal server is providing the console input and output.
	 *                           Typical for telnet or ssh terminal consoles.
	 */
	public static final String PROP_IP_PORT = "ip.port"; //$NON-NLS-1$

	/**
	 * Common terminal property: Timeout to be passed to the terminal connector. The specific terminal
	 *                           connector implementation may interpret this value differently. If not
	 *                           set, the terminal connector may use a default value.
	 */
	public static final String PROP_TIMEOUT = "timeout"; //$NON-NLS-1$

	/**
	 * Common terminal property: Process image path. Typical for process terminal consoles.
	 */
	public static final String PROP_PROCESS_PATH = "process.path"; //$NON-NLS-1$

	/**
	 * Common terminal property: Process arguments. Typical for process terminal consoles.
	 */
	public static final String PROP_PROCESS_ARGS = "process.args"; //$NON-NLS-1$

	/**
	 * Common terminal property: Runtime process instance. Typical for process terminal consoles.
	 */
	public static final String PROP_PROCESS_OBJ = "process"; //$NON-NLS-1$

	/**
	 * Common terminal property: Runtime process PTY instance. Typical for process terminal consoles.
	 */
	public static final String PROP_PTY_OBJ = "pty"; //$NON-NLS-1$

	/**
	 * Common terminal property: Flag to control if a local echo is needed from the terminal widget.
	 *                           Typical for process and streams terminal consoles.
	 */
	public static final String PROP_LOCAL_ECHO = "localEcho"; //$NON-NLS-1$

	/**
	 * Common terminal property: Stdin streams instance. Typical for streams terminal consoles.
	 */
	public static final String PROP_STREAMS_STDIN = "streams.stdin"; //$NON-NLS-1$

	/**
	 * Common terminal property: Stdout streams instance. Typical for streams terminal consoles.
	 */
	public static final String PROP_STREAMS_STDOUT = "streams.stdout"; //$NON-NLS-1$

	/**
	 * Common terminal property: Stderr streams instance. Typical for streams terminal consoles.
	 */
	public static final String PROP_STREAMS_STDERR = "streams.stderr"; //$NON-NLS-1$
}
