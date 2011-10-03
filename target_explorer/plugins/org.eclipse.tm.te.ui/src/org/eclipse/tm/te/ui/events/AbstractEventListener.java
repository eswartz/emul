/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.events;

import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.te.runtime.interfaces.events.IEventFireDelegate;
import org.eclipse.tm.te.runtime.interfaces.events.IEventListener;
import org.eclipse.tm.te.ui.swt.DisplayUtil;
import org.eclipse.ui.PlatformUI;

/**
 * Target Explorer: Abstract event listener implementation firing the event
 *                  within the platforms UI thread.
 */
public abstract class AbstractEventListener implements IEventListener, IEventFireDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.events.IEventFireDelegate#fire(java.lang.Runnable)
	 */
	@Override
	public void fire(Runnable runnable) {
		// Do nothing if no valid runnable is passed
		if (runnable == null) return;

		// Executes the runnable asynchronously within the current platform
		// UI thread if the platform display instance is available and not yet disposed.
		if (PlatformUI.isWorkbenchRunning() && PlatformUI.getWorkbench().getDisplay() != null
				&& !PlatformUI.getWorkbench().getDisplay().isDisposed()) {
			// Check if the current thread is the platform UI thread
			if (PlatformUI.getWorkbench().getDisplay().equals(Display.getCurrent())) {
				// We can execute the runnable directly
				runnable.run();
			} else {
				// We have to execute the runnable asynchronously
				DisplayUtil.safeAsyncExec(runnable);
			}
		}
	}

}
