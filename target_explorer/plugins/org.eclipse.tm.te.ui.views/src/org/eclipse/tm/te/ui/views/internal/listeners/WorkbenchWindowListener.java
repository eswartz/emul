/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal.listeners;

import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The window listener implementation. Takes care of the
 *                  management of the global listeners per workbench window.
 */
public class WorkbenchWindowListener implements IWindowListener {
	// The global part listener instance
	private final WorkbenchPartListener partListener = new WorkbenchPartListener();


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowActivated(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowClosed(IWorkbenchWindow window) {
		// On close, remove all global listeners from the window
		if (window != null && window.getPartService() != null) {
			window.getPartService().removePartListener(partListener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowOpened(IWorkbenchWindow window) {
		// On open, register all global listener to the window
		if (window != null && window.getPartService() != null) {
			// Get the part service
			IPartService service = window.getPartService();
			// Unregister the part listener, just in case
			service.removePartListener(partListener);
			// Register the part listener
			service.addPartListener(partListener);
			// Signal the active part to the part listener after registration
			partListener.partActivated(window.getActivePage().getActivePartReference());
		}
	}

}
