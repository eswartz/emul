/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.log.core.internal.listener;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.te.tcf.core.interfaces.listeners.IChannelStateChangeListener;

/**
 * Target Explorer: TCF logging channel state listener implementation.
 */
public class ChannelStateChangeListener implements IChannelStateChangeListener {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.core.interfaces.listeners.IChannelStateChangeListener#stateChanged(org.eclipse.tm.tcf.protocol.IChannel, int)
	 */
	public void stateChanged(IChannel channel, int state) {
		Assert.isNotNull(channel);

		switch(state) {
			case IChannel.STATE_OPEN:
				ChannelTraceListenerManager.getInstance().onChannelOpened(channel);
				break;
			case IChannel.STATE_CLOSED:
				ChannelTraceListenerManager.getInstance().onChannelClosed(channel);
				break;
		}
	}
}
