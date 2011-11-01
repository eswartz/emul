/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * Utility methods to deal with views.
 */
public class ViewsUtil {

	/**
	 * Asynchronously refresh the view identified by the given id.
	 *
	 * @param id The view id. Must not be <code>null</code>.
	 */
	public static void refresh(final String id) {
		Assert.isNotNull(id);

		// Create the runnable
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// Check the active workbench window and active page instances
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
					// Get the view reference
					IViewReference reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(id);
					// Get the view part from the reference, but do not restore it
					IWorkbenchPart part = reference != null ? reference.getPart(false) : null;
					// If the part is a common navigator, get the common viewer
					Viewer viewer = part instanceof CommonNavigator ? ((CommonNavigator)part).getCommonViewer() : null;
					// If not a common navigator, try to adapt to the viewer
					if (viewer == null) viewer = part != null ? (Viewer)part.getAdapter(Viewer.class) : null;
					// Refresh the viewer
					if (viewer != null) viewer.refresh();
				}
			}
		};

		// Execute asynchronously
		if (PlatformUI.isWorkbenchRunning()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
		}
	}

	/**
	 * Asynchronously set the given selection to the view identified by the given id.
	 *
	 * @param id The view id. Must not be <code>null</code>.
	 * @param selection The selection or <code>null</code>.
	 */
	public static void setSelection(final String id, final ISelection selection) {
		Assert.isNotNull(id);

		// Create the runnable
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// Check the active workbench window and active page instances
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
					// Get the view reference
					IViewReference reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(id);
					// Get the view part from the reference, but do not restore it
					IWorkbenchPart part = reference != null ? reference.getPart(false) : null;
					// Get the selection provider
					ISelectionProvider selectionProvider = part != null && part.getSite() != null ? part.getSite().getSelectionProvider() : null;
					// And apply the selection
					if (selectionProvider != null) selectionProvider.setSelection(selection);
				}
			}
		};

		// Execute asynchronously
		if (PlatformUI.isWorkbenchRunning()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
		}
	}
}
