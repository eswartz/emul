/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.core.internal.interfaces;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.Protocol.ChannelOpenListener;

/**
 * Enhanced channel open listener interface for internal use.
 */
public interface IChannelOpenListener extends ChannelOpenListener {

	/**
	 * Stores the given channel listener to the internal map. The map
	 * key is the given channel. If the given channel listener is <code>null</code>,
	 * the channel is removed from the internal map.
	 *
	 * @param channel The channel. Must not be <code>null</code>.
	 * @param listener The channel listener or <code>null</code>.
	 */
	public void setChannelListener(IChannel channel, IChannel.IChannelListener listener);
}
