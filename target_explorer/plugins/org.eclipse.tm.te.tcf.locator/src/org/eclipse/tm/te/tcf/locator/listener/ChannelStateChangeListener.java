/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.listener;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.core.interfaces.listeners.IChannelStateChangeListener;
import org.eclipse.tm.te.tcf.locator.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.locator.interfaces.ITracing;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelLookupService;


/**
 * Channel state change listener implementation.
 */
public class ChannelStateChangeListener implements IChannelStateChangeListener {
	// Reference to the parent model
	private final ILocatorModel model;

	/**
	 * Constructor.
	 *
	 * @param model The parent locator model. Must not be <code>null</code>.
	 */
	public ChannelStateChangeListener(ILocatorModel model) {
		assert model != null;
		this.model = model;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.tcf.core.interfaces.listeners.IChannelStateChangeListener#stateChanged(org.eclipse.tm.tcf.protocol.IChannel, int)
	 */
	public void stateChanged(IChannel channel, int state) {
		assert Protocol.isDispatchThread() && channel != null;

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_CHANNEL_STATE_CHANGE_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("ChannelStateChangeListener.stateChanged( " + channel + ", " + (state == IChannel.STATE_OPEN ? "OPEN" : "CLOSED") + " )", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
														ITracing.ID_TRACE_CHANNEL_STATE_CHANGE_LISTENER, this);
		}

		switch (state) {
			case IChannel.STATE_OPEN:
				IPeer peer = channel.getRemotePeer();
				// Find the corresponding model node
				IPeerModel node = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(peer.getID());
				if (node != null) {
					// Increase the channel reference counter by 1
					int counter = node.getIntProperty(IPeerModelProperties.PROP_CHANNEL_REF_COUNTER);
					if (counter < 0) counter = 0;
					counter++;
					node.setProperty(IPeerModelProperties.PROP_CHANNEL_REF_COUNTER, counter);
					if (counter > 0) node.setProperty(IPeerModelProperties.PROP_STATE, IPeerModelProperties.STATE_CONNECTED);
				}
				break;
			case IChannel.STATE_CLOSED:
				peer = channel.getRemotePeer();
				// Find the corresponding model node
				node = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(peer.getID());
				if (node != null) {
					// Decrease the channel reference counter by 1
					int counter = node.getIntProperty(IPeerModelProperties.PROP_CHANNEL_REF_COUNTER);
					counter--;
					if (counter < 0) counter = 0;
					node.setProperty(IPeerModelProperties.PROP_CHANNEL_REF_COUNTER, counter);
					if (counter == 0) node.setProperty(IPeerModelProperties.PROP_STATE, IPeerModelProperties.STATE_REACHABLE);
				}
				break;
		}
	}
}
