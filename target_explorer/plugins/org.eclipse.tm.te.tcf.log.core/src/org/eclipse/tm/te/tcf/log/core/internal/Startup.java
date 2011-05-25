/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.log.core.internal;

import org.eclipse.tm.tcf.protocol.Protocol;


/**
 * Class loaded by the TCF core framework when the framework is fired up. The static
 * constructor of the class will trigger the registration of the listeners in order
 * to log the communication from the point the framework started up.
 * <p>
 * <b>Note:</b> This will effectively trigger {@link CoreBundleActivator#start(org.osgi.framework.BundleContext)}
 * to be called.
 */
public class Startup {

	static {
		// We might get here on shutdown as well, and if TCF has not
		// been loaded, than we will run into an NPE. Lets double check.
		if (Protocol.getEventQueue() != null) {
			// Execute the listener registration within the TCF dispatch thread
			Protocol.invokeLater(new Runnable() {
				public void run() {
					LogManager.getInstance().initListeners();
				}
			});
		}
	}
}
