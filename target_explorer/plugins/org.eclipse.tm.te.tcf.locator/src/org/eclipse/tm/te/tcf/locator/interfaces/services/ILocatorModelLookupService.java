/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.interfaces.services;

import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * The service to lookup/search in the parent locator model.
 */
public interface ILocatorModelLookupService extends ILocatorModelService {

	/**
	 * Lookup the peer model for the given peer id.
	 *
	 * @param id The peer id. Must not be <code>null</code>.
	 * @return The peer model instance, or <code>null</code> if the peer model cannot be found.
	 */
	public IPeerModel lkupPeerModelById(String id);

	/**
	 * Lookup matching peer model instances for the given agent id.
	 *
	 * @param agentId The agent id. Must not be <code>null</code>.
	 * @return The peer model instances, or an empty list if the given agent id could not be matched.
	 */
	public IPeerModel[] lkupPeerModelByAgentId(String agentId);

	/**
	 * Lookup matching peer model instances which supports the listed local and remote services.
	 *
	 * @param expectedLocalServices The list of local service names to be supported, or <code>null</code>.
	 * @param expectedRemoteServices The list of remote service names to be supported, or <code>null</code>.
	 *
	 * @return The peer model instances, or an empty list if the listed services are not supported by any of the peers.
	 */
	public IPeerModel[] lkupPeerModelBySupportedServices(String[] expectedLocalServices, String[] expectedRemoteServices);
}
