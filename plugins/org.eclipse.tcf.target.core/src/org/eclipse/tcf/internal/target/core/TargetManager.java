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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tcf.target.core.ITargetListener;
import org.eclipse.tcf.target.core.ITargetManager;
import org.eclipse.tcf.target.core.TargetEvent;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;

public class TargetManager implements ITargetManager, ILocator.LocatorListener, ITargetListener {

	private LinkedList<ITarget> targets = new LinkedList<ITarget>();
	private LinkedList<ITargetListener> listeners = new LinkedList<ITargetListener>();
	private final LocalTarget localTarget;
	
	public TargetManager() {
		// Add in the local target
		localTarget = new LocalTarget();
		targets.add(localTarget);
		localTarget.addTargetListener(this);
		
		// load targets and kick of listener
		Protocol.invokeLater(new Runnable() {
			@Override
			public void run() {
				ILocator locator = Protocol.getLocator();
				Map<String, IPeer> peers = locator.getPeers();
				for (IPeer peer : peers.values())
					peerAdded(peer);
				locator.addListener(TargetManager.this);
			}
		});
	}
	
	@Override
	public ITarget[] getTargets() {
		synchronized (targets) {
			return targets.toArray(new ITarget[targets.size()]);
		}
	}
	
	@Override
	public void handleEvent(TargetEvent event) {
		if (event.getEventType() == TargetEvent.EventType.DELETED)
			if (!targets.remove(event.getTarget()))
				// target wasn't really there, ignore
				return;
	
		fireEvent(event);
	}
	
	@Override
	public void addTargetListener(ITargetListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeTargetListener(ITargetListener listener) {
		listeners.remove(listener);
	}
	
	private void fireEvent(TargetEvent event) {
		for (ITargetListener listener : listeners)
			listener.handleEvent(event);
	}
	
	@Override
	public void peerAdded(IPeer peer) {
		synchronized (targets) {
			for (ITarget target : targets) {
				if (target.handleNewPeer(peer))
					return;
			}

			// New target
			ITarget target = new DiscoveredTarget(peer);
			targets.add(target);
			target.addTargetListener(this);
			fireEvent(new TargetEvent(TargetEvent.EventType.ADDED, target));
		}
	}
	
	@Override
	public void peerChanged(IPeer peer) {
	}
	
	@Override
	public void peerHeartBeat(String id) {
	}
	
	@Override
	public void peerRemoved(String id) {
		synchronized (targets) {
			for (Iterator<ITarget> i = targets.iterator(); i.hasNext();) {
				ITarget target = i.next();
				if (target.handleRemovePeer(id)) {
					i.remove();
					fireEvent(new TargetEvent(TargetEvent.EventType.DELETED, target));
				}
			}
		}
	}
	
	/**
	 * Call when shutting down so we can clean up any resources.
	 * In particular the local target.
	 */
	public void dispose() {
		localTarget.dispose();
	}
}
