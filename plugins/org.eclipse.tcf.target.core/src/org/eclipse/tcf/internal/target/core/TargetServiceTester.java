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

import java.util.Collection;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.util.TCFTask;

public class TargetServiceTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, final Object[] args, Object expectedValue) {
		if (!(receiver instanceof ITarget))
			return false;
		final ITarget target = (ITarget)receiver;
		
		if ("targetServiceRunning".equals(property))
			// If target isn't running, return false
			if (!target.isRunning())
				return false;
		
		return new TCFTask<Boolean>() {
			@Override
			public void run() {
				target.handleTargetRequest(new ITarget.ITargetRequest() {
					@Override
					public void execute(IChannel channel) {
						Collection<String> remoteServices = channel.getRemoteServices();
						for (String remoteService : remoteServices) {
							for (Object arg : args) {
								if (remoteService.equals(arg)) {
									done(Boolean.TRUE);
									return;
								}
							}
						}
						done(Boolean.FALSE);
					}
					
					@Override
					public void channelUnavailable(IStatus error) {
						done(Boolean.FALSE);
					}
				});
			}
		}.getE().booleanValue();
	}

}
