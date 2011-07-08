/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.interfaces.nodes;

import org.eclipse.tm.tcf.protocol.IPeer;

/**
 * Default set of custom peer properties.
 */
public interface IPeerModelProperties {

	/**
	 * Property: The peer instance. Object stored here must be
	 *           castable to {@link IPeer}.
	 */
	public static final String PROP_INSTANCE = "instance"; //$NON-NLS-1$

	/**
	 * Property: The list of known local service names.
	 */
	public static final String PROP_LOCAL_SERVICES = "services.local"; //$NON-NLS-1$

	/**
	 * Property: The list of known remote service names.
	 */
	public static final String PROP_REMOTE_SERVICES = "services.remote"; //$NON-NLS-1$

	/**
	 * Property: The peer state.
	 */
	public static String PROP_STATE = "state"; //$NON-NLS-1$

	/**
	 * Peer state: Not determined yet (unknown).
	 */
	public static int STATE_UNKNOWN = -1;

	/**
	 * Peer state: Peer is reachable, no active communication channel is open.
	 */
	public static int STATE_REACHABLE = 0;

	/**
	 * Peer state: Peer is reachable and an active communication channel is opened.
	 */
	public static int STATE_CONNECTED = 1;

	/**
	 * Peer state: Peer is not reachable. Connection attempt timed out.
	 */
	public static int STATE_NOT_REACHABLE = 2;

	/**
	 * Peer state: Peer is not reachable. Connection attempt terminated with error.
	 */
	public static int STATE_ERROR = 3;

	/**
	 * Property: The peer connect timeout (for socket based transports)
	 */
	public static String PROP_CONNECT_TIMEOUT = "connectTimeout"; //$NON-NLS-1$

	/**
	 * Property: Reference counter tracking the active channels for this peer.
	 */
	public static String PROP_CHANNEL_REF_COUNTER = "channelRefCounter.silent"; //$NON-NLS-1$

	/**
	 * Property: The last error the scanner encounter trying to open a channel to this peer.
	 */
	public static String PROP_LAST_SCANNER_ERROR = "lastScannerError"; //$NON-NLS-1$
}
