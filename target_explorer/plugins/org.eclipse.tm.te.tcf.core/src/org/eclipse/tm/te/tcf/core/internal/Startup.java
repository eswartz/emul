/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.core.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.core.activator.CoreBundleActivator;


/**
 * Class loaded by the TCF core framework when the framework is fired up. The static
 * constructor of the class will trigger whatever is necessary in this case.
 * <p>
 * <b>Note:</b> This will effectively trigger {@link CoreBundleActivator#start(org.osgi.framework.BundleContext)}
 * to be called.
 */
public class Startup {
	// Atomic boolean to store the started state of the TCF core framework
	/* default */ final static AtomicBoolean STARTED = new AtomicBoolean(false);

	static {
		// We might get here on shutdown as well, and if TCF has not
		// been loaded, than we will run into an NPE. Lets double check.
		if (Protocol.getEventQueue() != null) {
			// Initialize the framework status by scheduling a simple
			// runnable to execute and be invoked once the framework
			// is fully up and usable.
			Protocol.invokeLater(new Runnable() {
				public void run() {
					// Core framework is scheduling the runnables, means it is started.
					setStarted(true);
				}
			});
		}
	}

	/**
	 * Set the core framework started state to the given state.
	 *
	 * @param started <code>True</code> when the framework is started, <code>false</code> otherwise.
	 */
	public static final void setStarted(boolean started) {
		STARTED.set(started);

		// Start/Stop should be called in the TCF protocol dispatch thread
		if (Protocol.getEventQueue() != null) {
			Protocol.invokeLater(new Runnable() {
				public void run() {
					// Catch IllegalStateException: TCF event dispatcher has shut down
					try {
						if (STARTED.get()) Tcf.start(); else Tcf.stop();
					} catch (IllegalStateException e) {
						if (!STARTED.get() && "TCF event dispatcher has shut down".equals(e.getLocalizedMessage())) { //$NON-NLS-1$
							// ignore the exception on shutdown
						} else {
							// re-throw in any other case
							throw e;
						}
					}
				}
			});
		}
	}

	/**
	 * Returns if or if not the core framework has been started.
	 *
	 * @return <code>True</code> when the framework is started, <code>false</code> otherwise.
	 */
	public static final boolean isStarted() {
		return STARTED.get();
	}
}
