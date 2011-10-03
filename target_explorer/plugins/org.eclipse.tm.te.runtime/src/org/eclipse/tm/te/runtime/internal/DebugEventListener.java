/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.internal;

import java.util.EventObject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tm.te.runtime.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.interfaces.events.IEventListener;
import org.eclipse.tm.te.runtime.interfaces.tracing.ITraceIds;

/**
 * Event listener for internal debugging purpose.
 */
public class DebugEventListener implements IEventListener {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
	 */
	@Override
	public void eventFired(EventObject event) {
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_EVENTS))
			CoreBundleActivator.getTraceHandler().trace("thread=[" + Thread.currentThread().getName() + "]\n\t" + event.toString(), //$NON-NLS-1$ //$NON-NLS-2$
																0, ITraceIds.TRACE_EVENTS, IStatus.INFO, this);
	}
}
