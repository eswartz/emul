/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.core.interfaces.listeners;

import org.eclipse.tm.tcf.protocol.IChannel;

/**
 * Interface for clients to implement that wishes to listen
 * channel state changes, like opening and closing of a channel.
 */
public interface IChannelStateChangeListener {

	/**
	 * Invoked if the channel state has changed.
	 *
	 * @param channel The channel which changed state.
	 * @param state The new state.
	 */
	public void stateChanged(IChannel channel, int state);
}
