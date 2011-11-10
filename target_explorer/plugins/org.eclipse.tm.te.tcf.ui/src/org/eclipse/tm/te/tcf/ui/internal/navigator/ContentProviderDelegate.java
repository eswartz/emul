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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.locator.interfaces.IModelListener;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tm.te.tcf.ui.model.Model;
import org.eclipse.tm.te.ui.views.interfaces.IRoot;
import org.eclipse.ui.navigator.CommonViewer;


/**
 * Content provider delegate implementation.
 */
public class ContentProviderDelegate implements ITreeContentProvider {
	private final static Object[] NO_ELEMENTS = new Object[0];

	// The locator model listener instance
	private IModelListener modelListener = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] children = NO_ELEMENTS;

		// If the parent element is null or IRoot, than we assume
		// the locator model as parent element.
		if (parentElement == null || parentElement instanceof IRoot) {
			parentElement = Model.getModel();
		}
		// If it is the locator model, get the peers
		if (parentElement instanceof ILocatorModel) {
			children = ((ILocatorModel)parentElement).getPeers();
		}

		return children;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		// If it is a peer model node, return the parent locator model
		if (element instanceof IPeerModel) {
			return ((IPeerModel)element).getModel();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		boolean hasChildren = false;

		if (element instanceof ILocatorModel) {
			hasChildren = ((ILocatorModel)element).getPeers().length > 0;
		}

		return hasChildren;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		Model.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		final ILocatorModel model = Model.getModel();

		// Create and attach the model listener if not yet done
		if (modelListener == null && model != null && viewer instanceof CommonViewer) {
			modelListener = new ModelListener(model, (CommonViewer)viewer);
			model.addListener(modelListener);
		}

		// Refresh the model
		if (model != null && newInput instanceof IRoot) {
			// If the TCF framework is not running yet, run the refresh asynchronous
			if (Tcf.isRunning() && Protocol.isDispatchThread()) {
				model.getService(ILocatorModelRefreshService.class).refresh();
			} else {
				Protocol.invokeLater(new Runnable() {
					@Override
					public void run() {
						model.getService(ILocatorModelRefreshService.class).refresh();
					}
				});
			}
		}
	}
}
