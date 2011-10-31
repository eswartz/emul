/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.model;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.nodes.LocatorModel;
import org.eclipse.tm.te.tcf.ui.internal.navigator.ModelListener;


/**
 * Helper class to instantiate and initialize the TCF locator model.
 */
public final class Model {
	// Reference to the locator model
	/* default */ static ILocatorModel locatorModel;

	/**
	 * Returns the locator model. If not yet initialized,
	 * initialize the locator model.
	 *
	 * @return The locator model.
	 */
	public static ILocatorModel getModel() {
		// Access to the locator model must happen in the TCF dispatch thread
		if (locatorModel == null) {
			if (Protocol.isDispatchThread()) {
				initialize();
			} else {
				Protocol.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						initialize();
					}
				});
			}
		}
		return locatorModel;
	}

	/**
	 * Initialize the root node. Must be called within the TCF dispatch thread.
	 */
	protected static void initialize() {
		Assert.isTrue(Protocol.isDispatchThread());

		locatorModel = new LocatorModel();
		// Register the model listener
		locatorModel.addListener(new ModelListener(locatorModel));
		// Start the scanner
		locatorModel.startScanner(5000, 120000);
	}

	/**
	 * Dispose the root node.
	 */
	public static void dispose() {
		if (locatorModel == null) return;

		// Access to the locator model must happen in the TCF dispatch thread
		if (Protocol.isDispatchThread()) {
			locatorModel.dispose();
		} else {
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					locatorModel.dispose();
				}
			});
		}

		locatorModel = null;
	}

}
