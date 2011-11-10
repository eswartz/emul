/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.navigator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.listener.ModelAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;


/**
 * TCF locator model listener implementation.
 */
public class ModelListener extends ModelAdapter {
	private final ILocatorModel parentModel;
	/* default */ final CommonViewer viewer;

	/**
	 * Constructor.
	 *
	 * @param parent The parent locator model. Must not be <code>null</code>.
	 * @param viewer The common viewer instance. Must not be <code>null</code>.
	 */
	public ModelListener(ILocatorModel parent, CommonViewer viewer) {
		Assert.isNotNull(parent);
		Assert.isNotNull(viewer);

		this.parentModel = parent;
		this.viewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.listener.ModelAdapter#locatorModelChanged(org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel)
	 */
	@Override
	public void locatorModelChanged(final ILocatorModel model) {
		if (parentModel.equals(model)) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh();
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.listener.ModelAdapter#peerModelChanged(org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel, org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel)
	 */
	@Override
	public void peerModelChanged(final ILocatorModel model, final IPeerModel peer) {
		if (parentModel.equals(model)) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh(peer);
				}
			});
		}
	}
}
