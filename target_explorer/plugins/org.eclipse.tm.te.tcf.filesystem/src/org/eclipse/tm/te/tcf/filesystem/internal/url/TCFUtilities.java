/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.url;

import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.te.tcf.filesystem.internal.exceptions.TCFChannelException;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;

/**
 * A utilities class that provides common TCF operations.
 */
public class TCFUtilities {

	/**
	 * Open a channel connected to the target represented by the peer.
	 *
	 * @return The channel or null if the operation fails.
	 */
	public static IChannel openChannel(final IPeer peer) throws TCFChannelException {
		final Rendezvous rendezvous = new Rendezvous();
		final TCFChannelException[] errors = new TCFChannelException[1];
		final IChannel[] channels = new IChannel[1];
		channels[0] = peer.openChannel();
		channels[0].addChannelListener(new IChannelListener() {
			@Override
			public void onChannelOpened() {
				rendezvous.arrive();
			}

			@Override
			public void onChannelClosed(Throwable error) {
				if (error != null) {
					String message = NLS.bind(Messages.TCFUtilities_OpeningFailureMessage,
							new Object[]{peer.getID(), error.getLocalizedMessage()});
					errors[0] = new TCFChannelException(message, error);
					rendezvous.arrive();
				}
			}

			@Override
			public void congestionLevel(int level) {
			}
		});
		try {
			rendezvous.waiting(5000L);
		} catch (InterruptedException e) {
			String message = NLS.bind(Messages.TCFUtilities_OpeningFailureMessage,
					new Object[]{peer.getID(), e.getLocalizedMessage()});
			errors[0] = new TCFChannelException(message, e);
		}
		if(errors[0] != null){
			throw errors[0];
		}
		return channels[0];
	}
}
