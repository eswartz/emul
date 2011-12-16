/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.core.internal;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.core.TransientPeer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.core.interfaces.IChannelManager;


/**
 * TCF channel manager implementation.
 */
public final class ChannelManager extends PlatformObject implements IChannelManager {

	/**
	 * Constructor.
	 */
	public ChannelManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.core.interfaces.IChannelManager#openChannel(org.eclipse.tm.tcf.protocol.IPeer, org.eclipse.tm.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)
	 */
	@Override
	public void openChannel(final IPeer peer, final DoneOpenChannel done) {
		if (Protocol.isDispatchThread()) {
			internalOpenChannel(peer, done);
		} else {
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					internalOpenChannel(peer, done);
				}
			});
		}
	}

	/**
	 * Internal implementation of {@link #openChannel(IPeer, org.eclipse.tm.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)}.
	 * <p>
	 * Method must be called within the TCF dispatch thread.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	/* default */ void internalOpenChannel(final IPeer peer, final DoneOpenChannel done) {
		Assert.isNotNull(peer);
		Assert.isNotNull(done);
		Assert.isTrue(Protocol.isDispatchThread());

		// Open the channel
		final IChannel channel = peer.openChannel();
		// Register the channel listener
		if (channel != null) {
			channel.addChannelListener(new IChannel.IChannelListener() {

				@Override
				public void onChannelOpened() {
					// Remove ourself as listener from the channel
					channel.removeChannelListener(this);
					// Channel opening succeeded
					done.doneOpenChannel(null, channel);
				}

				@Override
				public void onChannelClosed(Throwable error) {
					// Remove ourself as listener from the channel
					channel.removeChannelListener(this);
					// Channel opening failed
					done.doneOpenChannel(error, channel);
				}

				@Override
				public void congestionLevel(int level) {
					// ignored
				}
			});
		} else {
			// Channel is null? Something went terrible wrong.
			done.doneOpenChannel(new Exception("Unexpected null return value from IPeer#openChannel()!"), null); //$NON-NLS-1$
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.core.interfaces.IChannelManager#openChannel(java.util.Map, org.eclipse.tm.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)
	 */
	@Override
	public void openChannel(final Map<String, String> peerAttributes, final DoneOpenChannel done) {
		if (Protocol.isDispatchThread()) {
			internalOpenChannel(peerAttributes, done);
		} else {
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					internalOpenChannel(peerAttributes, done);
				}
			});
		}
	}

	/**
	 * Internal implementation of {@link #openChannel(Map, org.eclipse.tm.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)}.
	 * <p>
	 * Method must be called within the TCF dispatch thread.
	 *
	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	/* default */ void internalOpenChannel(final Map<String, String> peerAttributes, final DoneOpenChannel done) {
		Assert.isNotNull(peerAttributes);
		Assert.isNotNull(done);
		Assert.isTrue(Protocol.isDispatchThread());
		internalOpenChannel(getOrCreatePeerInstance(peerAttributes), done);
	}

	/**
	 * Tries to find an existing peer instance or create an new {@link IPeer}
	 * instance if not found.
	 * <p>
	 * <b>Note:</b> This method must be invoked at the TCF dispatch thread.
	 *
	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
	 * @return The peer instance.
	 */
	private IPeer getOrCreatePeerInstance(final Map<String, String> peerAttributes) {
		Assert.isNotNull(peerAttributes);
		Assert.isTrue(Protocol.isDispatchThread());

		// Get the peer id from the properties
		String peerId = peerAttributes.get(IPeer.ATTR_ID);
		Assert.isNotNull(peerId);

		// Check if we shall open the peer transient
		boolean isTransient = peerAttributes.containsKey("transient") ? Boolean.parseBoolean(peerAttributes.remove("transient")) : false; //$NON-NLS-1$ //$NON-NLS-2$

		// Look the peer via the Locator Service.
		IPeer peer = Protocol.getLocator().getPeers().get(peerId);
		// If not peer could be found, create a new one
		if (peer == null) {
			peer = isTransient ? new TransientPeer(peerAttributes) : new AbstractPeer(peerAttributes);
		}

		// Return the peer instance
		return peer;
	}
}
