/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
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
	 * Lookup the peer model for the given peer id.
	 *
	 * @param agentId The agent id. Must not be <code>null</code>.
	 * @return The peer model instance, or <code>null</code> if the peer model cannot be found.
	 */
	public IPeerModel lkupPeerModelByAgentId(String agentId);
}
