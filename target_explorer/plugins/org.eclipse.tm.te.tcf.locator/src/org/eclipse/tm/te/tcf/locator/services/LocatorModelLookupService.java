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
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelLookupService;


/**
 * Default locator model lookup service implementation.
 */
public class LocatorModelLookupService extends AbstractLocatorModelService implements ILocatorModelLookupService {

	/**
	 * Constructor.
	 *
	 * @param parentModel The parent locator model instance. Must not be <code>null</code>.
	 */
	public LocatorModelLookupService(ILocatorModel parentModel) {
		super(parentModel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.services.ILocatorModelLookupService#lkupPeerModelById(java.lang.String)
	 */
	public IPeerModel lkupPeerModelById(String id) {
		Assert.isNotNull(id);
		Assert.isTrue(Protocol.isDispatchThread());

		IPeerModel node = null;
		for (IPeerModel candidate : getLocatorModel().getPeers()) {
			IPeer peer = candidate.getPeer();
			if (id.equals(peer.getID())) {
				node = candidate;
				break;
			}
		}

		return node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupPeerModelByAgentId(java.lang.String)
	 */
	public IPeerModel[] lkupPeerModelByAgentId(String agentId) {
		Assert.isNotNull(agentId);
		Assert.isTrue(Protocol.isDispatchThread());

		List<IPeerModel> nodes = new ArrayList<IPeerModel>();
		for (IPeerModel candidate : getLocatorModel().getPeers()) {
			IPeer peer = candidate.getPeer();
			if (agentId.equals(peer.getAgentID())) {
				nodes.add(candidate);
			}
		}

		return nodes.toArray(new IPeerModel[nodes.size()]);
	}
}
