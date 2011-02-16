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
package org.eclipse.tcf.target.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tcf.internal.target.core.Activator;
import org.eclipse.tcf.target.core.TargetEvent.EventType;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;

public abstract class AbstractTarget extends PlatformObject implements ITarget {
	
	protected LinkedList<IPeer> peers = new LinkedList<IPeer>();
	protected LinkedList<ITargetListener> listeners = new LinkedList<ITargetListener>();
	protected LinkedList<ITargetRequest> outstandingRequests = new LinkedList<ITargetRequest>();
	private Map<String, Object> localProperties = new HashMap<String, Object>();
	
	private IChannel channel;

	@Override
	public boolean isRunning() {
		return !peers.isEmpty();
	}
	
	@Override
	public Map<String, Object> getLocalProperties() {
		return localProperties;
	}
	
	@Override
	public void addTargetListener(ITargetListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeLaunchListener(ITargetListener listener) {
		listeners.remove(listener);
	}
	
	protected abstract void launch();
	
	protected IChannel getChannel() {
		if (channel != null)
			return channel;
		
		if (peers.isEmpty())
			return null;
			
		channel = peers.getFirst().openChannel();
		// Add listener to inform when we're being closed
		channel.addChannelListener(new IChannel.IChannelListener() {
			@Override
			public void onChannelOpened() {
			}
			
			@Override
			public void onChannelClosed(Throwable error) {
				fireEvent(new TargetEvent(EventType.DELETED, AbstractTarget.this));
			}
			
			@Override
			public void congestionLevel(int level) {
			}
		});
		return channel;
	}

	@Override
	public String getShortName() {
		if (peers.isEmpty())
			return "<unknown>";
		return peers.getFirst().getName();
	}
	
	@Override
	public String getName() {
		String name = getShortName();
		String agentId = getAgentId();
		if (agentId != null)
			name += " {" + agentId + "}";
		return name;
	}
	
	private String getAgentId() {
		if (peers.isEmpty())
			return null;
		
		return peers.getFirst().getAgentID();
	}
	
	@Override
	public void handleTargetRequest(final ITargetRequest request) {
		assert Protocol.isDispatchThread();
		if (!peers.isEmpty()) {
			// have peers, ready to go.
			final IChannel channel = getChannel();
			switch (channel.getState()) {
			case IChannel.STATE_OPENING:
				channel.addChannelListener(new IChannel.IChannelListener() {
					@Override
					public void onChannelOpened() {
						request.execute(channel);
						channel.removeChannelListener(this);
					}
					
					@Override
					public void onChannelClosed(Throwable error) {
						request.channelUnavailable(Activator.createStatus(IStatus.ERROR, error));
					}
					
					@Override
					public void congestionLevel(int level) {
					}
				});
				break;
			case IChannel.STATE_OPEN:
				request.execute(channel);
				break;
			case IChannel.STATE_CLOSED:
				request.channelUnavailable(Activator.createStatus(IStatus.ERROR, new Error("Channel closed.")));
				break;
			}
			return;
		}
		
		// Need to launch
		boolean launching = !outstandingRequests.isEmpty();
		outstandingRequests.add(request);
		if (!launching)
			launch();
	}
	
	protected void fireEvent(TargetEvent event) {
		for (ITargetListener listener : listeners)
			listener.handleEvent(event);
	}
	
	@Override
	public boolean handleNewPeer(IPeer peer) {
		boolean launching = false;
		if (peers.isEmpty())
			launching = true;
		else {
			// Make sure agent id matches
			if (!getAgentId().equals(peer.getAgentID()))
				return false;
			
			// Replace any old peer object with the new one
			for (Iterator<IPeer> i = peers.iterator(); i.hasNext();) {
				IPeer p = i.next();
				if (p.getID().equals(peer.getID()))
					i.remove();
			}
		}
		
		peers.add(peer);
		
		if (launching) {
			// start things up now that we have a peer
			fireEvent(new TargetEvent(EventType.LAUNCHED, this));
			for (ITargetRequest request : outstandingRequests)
				handleTargetRequest(request);
			outstandingRequests.clear();
		}
		
		return true;
	}
	
	@Override
	public boolean handleRemovePeer(String id) {
		for (Iterator<IPeer> i = peers.iterator(); i.hasNext();) {
			if (id.equals(i.next().getID())) {
				i.remove();
			}
		}
		
		// Always return false, only discovered targets should return true.
		return false;
	}

}
