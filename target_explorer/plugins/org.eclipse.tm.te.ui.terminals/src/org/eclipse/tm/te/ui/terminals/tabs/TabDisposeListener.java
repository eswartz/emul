/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.tabs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.te.runtime.events.DisposedEvent;
import org.eclipse.tm.te.runtime.events.EventManager;

/**
 * Terminals tab default dispose listener implementation.
 */
@SuppressWarnings("restriction")
public class TabDisposeListener implements DisposeListener {
	private final TabFolderManager parentTabFolderManager;

	/**
	 * Constructor.
	 *
	 * @param parentTabFolderManager The parent tab folder manager. Must be not <code>null</code>
	 */
	public TabDisposeListener(TabFolderManager parentTabFolderManager) {
		Assert.isNotNull(parentTabFolderManager);
		this.parentTabFolderManager = parentTabFolderManager;
	}

	/**
	 * Returns the parent terminal console tab folder manager instance.
	 *
	 * @return The parent terminal console tab folder manager instance.
	 */
	protected final TabFolderManager getParentTabFolderManager() {
		return parentTabFolderManager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	@Override
	public void widgetDisposed(DisposeEvent e) {
		// If a tab item gets disposed, we have to dispose the terminal as well
		if (e.getSource() instanceof CTabItem) {
			// Get the terminal control (if any) from the tab item
			Object candidate = ((CTabItem)e.getSource()).getData();
			if (candidate instanceof ITerminalViewControl) ((ITerminalViewControl)candidate).disposeTerminal();

			// If all items got removed, we have to switch back to the empty page control
			if (parentTabFolderManager.getTabFolder() != null && parentTabFolderManager.getTabFolder().getItemCount() == 0) {
				parentTabFolderManager.getParentView().switchToEmptyPageControl();
			}
			// Fire selection changed event
			parentTabFolderManager.fireSelectionChanged();
			// Fire the terminal console disposed event
			EventManager.getInstance().fireEvent(new DisposedEvent(e.getSource(), ((CTabItem)e.getSource()).getData("customData"))); //$NON-NLS-1$
		}
	}
}
