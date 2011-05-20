/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.navigator;

import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.listener.ModelAdapter;
import org.eclipse.tm.te.ui.views.interfaces.IUIConstants;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;


/**
 * Target Explorer: TCF locator model listener implementation.
 */
public class ModelListener extends ModelAdapter {
	private final ILocatorModel parentModel;

	/**
	 * Constructor.
	 *
	 */
	public ModelListener(ILocatorModel parent) {
		assert parent != null;
		parentModel = parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.listener.ModelAdapter#locatorModelChanged(org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel)
	 */
	@Override
	public void locatorModelChanged(final ILocatorModel model) {
		if (parentModel.equals(model)) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					CommonViewer viewer = getViewer();
					if (viewer != null) viewer.refresh();
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
				public void run() {
					CommonViewer viewer = getViewer();
					if (viewer != null) viewer.refresh(peer);
				}
			});
		}
	}

	/**
	 * Get the common viewer used by the Target Explorer view instance.
	 *
	 * @return The common viewer or <code>null</code>
	 */
	protected CommonViewer getViewer() {
		if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
				&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart part = page.findView(IUIConstants.ID_EXPLORER);
			if (part instanceof CommonNavigator) {
				return ((CommonNavigator)part).getCommonViewer();
			}
		}

		return null;
	}
}
