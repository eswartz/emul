/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.locator.ScannerRunnable;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
import org.eclipse.tm.te.tcf.locator.nodes.LocatorModel;
import org.eclipse.tm.te.tcf.locator.nodes.PeerModel;


/**
 * Default locator model refresh service implementation.
 */
public class LocatorModelRefreshService extends AbstractLocatorModelService implements ILocatorModelRefreshService {

	/**
	 * Constructor.
	 *
	 * @param parentModel The parent locator model instance. Must be not <code>null</code>.
	 */
	public LocatorModelRefreshService(ILocatorModel parentModel) {
		super(parentModel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.services.ILocatorModelRefreshService#refresh()
	 */
	public void refresh() {
		assert Protocol.isDispatchThread();

		// Get the parent locator model
		ILocatorModel model = getLocatorModel();

		// If the parent model is already disposed, the service will drop out immediately
		if (model.isDisposed()) return;

		// If the TCF framework isn't initialized yet, the service will drop out immediately
		if (!Tcf.isRunning()) return;

		// Get the list of old children (update node instances where possible)
		final List<IPeerModel> oldChildren = new ArrayList<IPeerModel>(Arrays.asList(model.getPeers()));

		// Get the locator service
		ILocator locatorService = Protocol.getLocator();
		if (locatorService != null) {
			// Check for the locator listener to be created and registered
			if (model instanceof LocatorModel) ((LocatorModel)model).checkLocatorListener();
			// Get the map of peers known to the locator service.
			Map<String, IPeer> peers = locatorService.getPeers();
			for (String peerId : peers.keySet()) {
				// Get the peer instance for the current peer id
				IPeer peer = peers.get(peerId);
				// Try to find an existing peer node first
				IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(peerId);
				// And create a new one if we cannot find it
				if (peerNode == null) peerNode = new PeerModel(model, peer);
				else oldChildren.remove(peerNode);
				// Validate the peer node before adding
				if (peerNode != null) peerNode = model.validatePeerNodeForAdd(peerNode);
				if (peerNode != null) {
					// Add the peer node to model
					model.getService(ILocatorModelUpdateService.class).add(peerNode);
					// And schedule for immediate status update
					Runnable runnable = new ScannerRunnable(model.getScanner(), peerNode);
					Protocol.invokeLater(runnable);
				}
			}
		}

		// If there are remaining old children, remove them from the model (non-recursive)
		for (IPeerModel oldChild : oldChildren) model.getService(ILocatorModelUpdateService.class).remove(oldChild);

		// Create and fire the notification event if non null
//		if (dirty) {
//			IWRNotificationEvent event = model.getFactory().newPropertyChangeEvent(model, IContainerModelNode.PROPERTY_CHANGED, null, null);
//			if (event != null) WRNotificationManager.getInstance().fireEvent(event);
//		}
	}

}
