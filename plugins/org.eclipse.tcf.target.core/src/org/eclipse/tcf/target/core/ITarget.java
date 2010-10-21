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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;

/**
 * Object that represents a target, or more accurately an agent running on a target.
 * 
 * Extends IAdaptable to allow for extensions in the targets view.
 * 
 * @author Doug Schaefer
 *
 */
public interface ITarget extends IAdaptable {

	/**
	 * The UI name for the target.
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * A short name for the target, typically the peer name.
	 * 
	 * @return short name
	 */
	String getShortName();

	/**
	 * Execute the request, launching the target and opening a channel if necessary.
	 * @param request
	 */
	void handleTargetRequest(ITargetRequest request);
	
	/**
	 * Target request.
	 */
	interface ITargetRequest {
		/**
		 * Execute the requested with the target's channel.
		 * 
		 * @param channel
		 */
		void execute(IChannel channel);
		
		/**
		 * Failed to open a channel to execute the request.
		 * 
		 * @param error that occured
		 */
		void channelUnavailable(IStatus error);
	}
	
	/**
	 * Is target running.
	 * 
	 * @return is running
	 */
	boolean isRunning();

	/**
	 * Add target listener.
	 * 
	 * @param listener
	 */
	void addTargetListener(ITargetListener listener);
	
	/**
	 * Remove target listener
	 * 
	 * @param listener
	 */
	void removeLaunchListener(ITargetListener listener);
	
	/**
	 * Return true if this peer is associated with this target.
	 * 
	 * @param peer
	 * @return is my peer
	 */
	boolean handleNewPeer(IPeer peer);

	/**
	 * Peer has been removed. Return true if there are no more peers for this target.
	 * 
	 * @param peer
	 * @return no more peers
	 */
	boolean handleRemovePeer(String id);

}
