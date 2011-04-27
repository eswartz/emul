/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.core.interfaces;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;

/**
 * TCF channel manager public API declaration.
 */
public interface IChannelManager extends IAdaptable {

    /**
     * Client call back interface for openChannel(...).
     */
	interface DoneOpenChannel {
        /**
         * Called when the channel fully opened or failed to open.
         *
         * @param error The error description if operation failed, <code>null</code> if succeeded.
         * @param channel The channel object or <code>null</code>.
         */
		void doneOpenChannel(Throwable error, IChannel channel);
	}

	/**
	 * Opens a new channel to communicate with the given peer.
	 * <p>
	 * The method can be called from any thread context.
	 *
	 * @param peer The peer. Must be not <code>null</code>.
	 * @param done The client callback. Must be not <code>null</code>.
	 */
	public void openChannel(IPeer peer, DoneOpenChannel done);

	/**
	 * Opens a new channel to communicate with the peer described by the
	 * given peer attributes.
	 * <p>
	 * The method can be called from any thread context.
	 *
	 * @param peerAttributes The peer attributes. Must be not <code>null</code>.
	 * @param done The client callback. Must be not <code>null</code>.
	 */
	public void openChannel(Map<String, String> peerAttributes, DoneOpenChannel done);
}
