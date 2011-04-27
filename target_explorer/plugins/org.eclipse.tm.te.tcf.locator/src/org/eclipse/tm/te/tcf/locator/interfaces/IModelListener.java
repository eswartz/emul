/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.interfaces;

import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Interface for clients to implement that wishes to listen
 * to changes to the locator model.
 */
public interface IModelListener {

	/**
	 * Invoked if a peer is added or removed to/from the locator model.
	 *
	 * @param model The changed locator model.
	 */
	public void locatorModelChanged(ILocatorModel model);

	/**
	 * Invoked if the locator model is disposed.
	 *
	 * @param model The disposed locator model.
	 */
	public void locatorModelDisposed(ILocatorModel model);

	/**
	 * Invoked if the peer model properties have changed.
	 *
	 * @param model The parent locator model.
	 * @param peer The changed peer model.
	 */
	public void peerModelChanged(ILocatorModel model, IPeerModel peer);
}
