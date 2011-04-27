/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.core.internal.listener;

import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.core.internal.interfaces.IChannelOpenListener;
import org.eclipse.tm.te.tcf.core.internal.nls.Messages;
import org.eclipse.tm.te.tcf.core.internal.utils.LogUtils;


/**
 * Internal channel listener. Attached to a TCF channel for tracing purpose.
 */
public class InternalChannelListener implements IChannel.IChannelListener {
	// The reference to the channel
	private final IChannel fChannel;

	/**
	 * Constructor.
	 *
	 * @param channel The channel. Must be not <code>null</code>.
	 */
	public InternalChannelListener(IChannel channel) {
		assert channel != null;
		fChannel = channel;
	}

	/**
	 * Return the associated channel.
	 *
	 * @return The channel instance.
	 */
	protected final IChannel getChannel() {
		return fChannel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IChannel.IChannelListener#congestionLevel(int)
	 */
	public void congestionLevel(int level) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IChannel.IChannelListener#onChannelClosed(java.lang.Throwable)
	 */
	public void onChannelClosed(Throwable error) {
		// Detach the listeners cleanly
		detachListeners(getChannel());

		// Construct the cause message
		String cause = ""; //$NON-NLS-1$
		if (error != null) {
			cause = NLS.bind(Messages.InternalChannelListener_onChannelClosed_cause, error.getLocalizedMessage());
		}

		// Trace the channel closing
		LogUtils.logMessageForChannel(getChannel(), NLS.bind(Messages.InternalChannelListener_onChannelClosed_message, cause), "debug/channels", this); //$NON-NLS-1$

		// Fire the property change event for the channel
		Tcf.fireChannelStateChangeListeners(getChannel(), IChannel.STATE_CLOSED);
	}

	/**
	 * Detach all registered listeners from the given channel.
	 *
	 * @param channel The channel. Must not be <code>null</code>.
	 */
	protected void detachListeners(IChannel channel) {
		assert channel != null;

		// Cleanly remove all listeners from the channel
		channel.removeChannelListener(this);

		// And remove the listener references from the global channel open listener
		IChannelOpenListener openListener = (IChannelOpenListener)Tcf.getAdapter(IChannelOpenListener.class);
		if (openListener != null) openListener.setChannelListener(channel, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IChannel.IChannelListener#onChannelOpened()
	 */
	public void onChannelOpened() {
	}
}
