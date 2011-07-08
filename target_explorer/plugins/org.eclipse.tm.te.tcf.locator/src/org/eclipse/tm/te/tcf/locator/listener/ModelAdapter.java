/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.listener;

import org.eclipse.tm.te.tcf.locator.interfaces.IModelListener;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Default model listener implementation.
 */
public class ModelAdapter implements IModelListener {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.IModelListener#locatorModelChanged(org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel)
	 */
	public void locatorModelChanged(ILocatorModel model) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.IModelListener#locatorModelDisposed(org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel)
	 */
	public void locatorModelDisposed(ILocatorModel model) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.IModelListener#peerModelChanged(org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel, org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel)
	 */
	public void peerModelChanged(ILocatorModel model, IPeerModel peer) {
	}

}
