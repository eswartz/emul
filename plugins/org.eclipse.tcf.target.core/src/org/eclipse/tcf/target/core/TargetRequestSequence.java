/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.target.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tcf.target.core.ITarget.ITargetRequest;
import org.eclipse.tm.tcf.protocol.IChannel;

/**
 * @author dschaefer
 *
 */
public abstract class TargetRequestSequence implements ITargetRequest {

	private Step[] steps;
	private int current;
	private IChannel channel;
	
	public abstract class Step {
		public abstract void run(IChannel channel);
		
		public void nextStep() {
			TargetRequestSequence.this.next();
		}
		
	}
	
	public abstract Step[] getSteps();
	
	@Override
	public void execute(IChannel channel) {
		this.channel = channel;
		this.current = 0;
		this.steps = getSteps();
		next();
	}

	private void next() {
		if (current < steps.length)
			steps[current++].run(channel);
	}
	
	@Override
	public void channelUnavailable(IStatus error) {
		// default - do nothing
	}

}
