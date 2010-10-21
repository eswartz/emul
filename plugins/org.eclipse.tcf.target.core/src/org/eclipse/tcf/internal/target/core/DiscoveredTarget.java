/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.core;

import org.eclipse.tcf.target.core.AbstractTarget;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tm.tcf.protocol.IPeer;

public class DiscoveredTarget extends AbstractTarget implements ITarget {

	private String agentId;
	private String name;
	
	public DiscoveredTarget(IPeer firstPeer) {
		peers.add(firstPeer);
		
		agentId = firstPeer.getAgentID();
		name = firstPeer.getName();
		
		if (name != null) {
			if (agentId != null)
				name += " (" + agentId + ")";
		} else {
			if (agentId != null)
				name = "AgentId: " + agentId;
			else
				name = "PeerId:" + firstPeer.getID();
		}
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean handleNewPeer(IPeer peer) {
		if (agentId == null)
			return false;
		
		if (agentId.equals(peer.getAgentID())) {
			return super.handleNewPeer(peer);
		}

		return false;
	}
	
	@Override
	public boolean handleRemovePeer(String id) {
		super.handleRemovePeer(id); // ignore result
		
		return peers.isEmpty(); // return true if peers all gone
	}
	
}
