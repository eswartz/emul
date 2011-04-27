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

import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.te.tcf.locator.ScannerRunnable;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
import org.eclipse.tm.te.tcf.locator.nodes.PeerModel;


/**
 * Locator listener implementation.
 */
public class LocatorListener implements ILocator.LocatorListener {
	// Reference to the parent model
	private final ILocatorModel fModel;

	/**
	 * Constructor.
	 *
	 * @param model The parent locator model. Must be not <code>null</code>.
	 */
	public LocatorListener(ILocatorModel model) {
		super();

		assert model != null;
		fModel = model;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.ILocator.LocatorListener#peerAdded(org.eclipse.tm.tcf.protocol.IPeer)
	 */
	public void peerAdded(IPeer peer) {
		if (fModel != null && peer != null) {
			// find the corresponding model node to remove (expected to be null)
			IPeerModel peerNode = fModel.getService(ILocatorModelLookupService.class).lkupPeerModelById(peer.getID());
			// If found, remove the old node
			if (peerNode != null) fModel.getService(ILocatorModelUpdateService.class).remove(peerNode);
			// Create a new peer node instance
			peerNode = new PeerModel(fModel, peer);
			// Validate the peer node before adding
			if (peerNode != null) peerNode = fModel.validatePeerNodeForAdd(peerNode);
			// Add the peer node to the model
			if (peerNode != null) {
				fModel.getService(ILocatorModelUpdateService.class).add(peerNode);
				// And schedule for immediate status update
				Runnable runnable = new ScannerRunnable(fModel.getScanner(), peerNode);
				Protocol.invokeLater(runnable);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.ILocator.LocatorListener#peerChanged(org.eclipse.tm.tcf.protocol.IPeer)
	 */
	public void peerChanged(IPeer peer) {
		if (fModel != null && peer != null) {
			// find the corresponding model node to remove
			IPeerModel peerNode = fModel.getService(ILocatorModelLookupService.class).lkupPeerModelById(peer.getID());
			// Update the peer instance
			if (peerNode != null) peerNode.setProperty(IPeerModelProperties.PROP_INSTANCE, peer);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.ILocator.LocatorListener#peerRemoved(java.lang.String)
	 */
	public void peerRemoved(String id) {
		if (fModel != null && id != null) {
			// find the corresponding model node to remove
			IPeerModel peerNode = fModel.getService(ILocatorModelLookupService.class).lkupPeerModelById(id);
			if (peerNode != null) {
				// Remove from the model
				fModel.getService(ILocatorModelUpdateService.class).remove(peerNode);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.ILocator.LocatorListener#peerHeartBeat(java.lang.String)
	 */
	public void peerHeartBeat(String id) {
	}

}
