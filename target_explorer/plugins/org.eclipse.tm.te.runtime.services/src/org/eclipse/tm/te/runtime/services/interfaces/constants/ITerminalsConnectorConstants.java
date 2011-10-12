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
 * Defines the terminals connector constants.
 */
public interface ITerminalsConnectorConstants {

	/**
	 * Property: The unique id of the terminals view to open.
	 */
	public static final String PROP_ID = "id"; //$NON-NLS-1$

	/**
	 * Property: The title of the terminal tab to open.
	 */
	public static final String PROP_TITLE = "title"; //$NON-NLS-1$

	/**
	 * Property: Custom data object to associate with the terminal tab.
	 */
	public static final String PROP_DATA = "data"; //$NON-NLS-1$

	/**
	 * Property: Terminals connector type id.
	 */
	public static final String PROP_CONNECTOR_TYPE_ID = "connector.type.id"; //$NON-NLS-1$

	/**
	 * Property: Specific terminal connector type id. Allows clients to
	 *           override the specifically used terminal connector
	 *           implementation for a given type.
	 */
	public static final String PROP_TERMINAL_CONNECTOR_ID = "tm.terminal.connector.id"; //$NON-NLS-1$

	// ***** Generic terminals connector properties *****

	/**
	 * Property: Timeout to be passed to the terminal connector. The specific terminal
	 *           connector implementation may interpret this value differently. If not
	 *           set, the terminal connector may use a default value.
	 */
	public static final String PROP_TIMEOUT = "timeout"; //$NON-NLS-1$

	/**
	 * Property: Flag to control if a local echo is needed from the terminal widget.
	 *           <p>Typical for process and streams terminals.
	 */
	public static final String PROP_LOCAL_ECHO = "localEcho"; //$NON-NLS-1$

	// ***** IP based terminals connector properties *****

	/**
	 * Property: Host name or IP address the terminal server is running.
	 *           <p>Typical for telnet or ssh terminals.
	 */
	public static final String PROP_IP_HOST = "ip.host"; //$NON-NLS-1$

	/**
	 * Property: Port at which the terminal server is providing the console input and output.
	 *           <p>Typical for telnet or ssh terminals.
	 */
	public static final String PROP_IP_PORT = "ip.port"; //$NON-NLS-1$

	/**
	 * Property: An offset to add to the specified port number.
	 *           <p>Typical for telnet or ssh terminals.
	 */
	public static final String PROP_IP_PORT_OFFSET = "ip.port.offset"; //$NON-NLS-1$

	// ***** Process based terminals connector properties *****

	/**
	 * Property: Process image path.
	 * 			 <p>Typical for process terminals.
	 */
	public static final String PROP_PROCESS_PATH = "process.path"; //$NON-NLS-1$

	/**
	 * Property: Process arguments.
	 *           <p>Typical for process terminals.
	 */
	public static final String PROP_PROCESS_ARGS = "process.args"; //$NON-NLS-1$

	/**
	 * Property: Runtime process instance.
     *           <p>Typical for process terminals.
	 */
	public static final String PROP_PROCESS_OBJ = "process"; //$NON-NLS-1$

	/**
	 * Property: Runtime process PTY instance.
	 *           <p>Typical for process terminals.
	 */
	public static final String PROP_PTY_OBJ = "pty"; //$NON-NLS-1$

	// ***** Streams based terminals connector properties *****

	/**
	 * Property: Stdin streams instance.
	 *           <p>Typical for streams terminals.
	 */
	public static final String PROP_STREAMS_STDIN = "streams.stdin"; //$NON-NLS-1$

	/**
	 * Property: Stdout streams instance.
	 *           <p>Typical for streams terminals.
	 */
	public static final String PROP_STREAMS_STDOUT = "streams.stdout"; //$NON-NLS-1$

	/**
	 * Property: Stderr streams instance.
	 *           <p>Typical for streams terminals.
	 */
	public static final String PROP_STREAMS_STDERR = "streams.stderr"; //$NON-NLS-1$
}
