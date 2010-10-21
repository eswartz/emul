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

import java.util.Iterator;
import java.util.LinkedList;

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
	
	private IChannel channel;

	@Override
	public boolean isRunning() {
		return !peers.isEmpty();
	}
	
	@Override
	public void addTargetListener(ITargetListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeLaunchListener(ITargetListener listener) {
		listeners.remove(listener);
	}
	
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
	public void handleTargetRequest(final ITargetRequest request) {
		assert Protocol.isDispatchThread();
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
		else
			for (Iterator<IPeer> i = peers.iterator(); i.hasNext();) {
				IPeer p = i.next();
				if (p.getID().equals(peer.getID()))
					// Replace the old peer object with the new one
					i.remove();
			}
		
		peers.add(peer);
		
		if (launching)
			fireEvent(new TargetEvent(EventType.LAUNCHED, this));
		
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
