/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.model.interfaces;


/**
 * Interface to be implemented by model nodes which can be connected.
 */
public interface IConnectable {

	/**
	 * Property: Connect state.
	 */
	public static final String PROPERTY_CONNECT_STATE = "connectstate.transient"; //$NON-NLS-1$

	/**
	 * Property: Connect sub state.
	 */
	public static final String PROPERTY_CONNECT_SUB_STATE = "connectSubState.transient"; //$NON-NLS-1$

	public static final int STATE_UNREACHABLE = -1;
	public static final int STATE_UNCONNECTED = 0;
	public static final int STATE_DISCONNECTING = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_CONNECTED = 3;

	public static final int SUB_STATE_NORMAL = 0;
	public static final int SUB_STATE_CONNECT_SEND = 1;
	public static final int SUB_STATE_DISCONNECT_SEND = 2;
	public static final int SUB_STATE_REBOOT_MANUAL = 3;

	public static final int USER_SUB_STATE_BASE = 10;

	/**
	 * Returns whether or not this context is connectable.
	 *
	 * @return <code>true</code> if this context is connectable.
	 */
	public boolean isConnectable();

	/**
	 * Returns the connect state of this context.
	 *
	 * @return The connect state.
	 */
	public int getConnectState();

	/**
	 * Set the connect state of for this context.
	 *
	 * @param connectState The new connect state.
	 * @return <code>true</code> if the state has changed.
	 */
	public boolean setConnectState(int connectState);

	/**
	 * Check the current state.
	 *
	 * @param connectState The connect state to check.
	 * @return <code>true</code> if the current connect state equals the given one.
	 */
	public boolean isConnectState(int connectState);

	/**
	 * Returns the connect sub state of this context.
	 *
	 * @return The connect sub state.
	 */
	public int getConnectSubState();

	/**
	 * Set the connect sub state of for this context.
	 *
	 * @param connectSubState The connect sub state to check.
	 * @return <code>true</code> if the state has changed.
	 */
	public boolean setConnectSubState(int connectSubState);

	/**
	 * Check the current sub state.
	 *
	 * @param connectSubState The connect sub state to check.
	 * @return <code>true</code> if the current connect sub state equals the given one.
	 */
	public boolean isConnectSubState(int connectSubState);
}
