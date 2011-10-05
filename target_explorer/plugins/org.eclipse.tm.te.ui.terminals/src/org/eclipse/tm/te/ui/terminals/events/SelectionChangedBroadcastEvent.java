/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.events;

import java.util.EventObject;

import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.tm.te.runtime.events.EventManager;
import org.eclipse.tm.te.ui.terminals.tabs.TabFolderManager;

/**
 * Terminal console selection changed broadcast event. The event is typically fired
 * by a terminal console tab folder manager to signal a tab switch to all listeners.
 */
public class SelectionChangedBroadcastEvent extends EventObject {
	private static final long serialVersionUID = -4970244776543572896L;

	// The selection changed event to broadcast
	private final SelectionChangedEvent selectionChangedEvent;

	/**
	 * Constructor.
	 *
	 * @param source The event source. Must not be <code>null</code>.
	 * @param selectionChangedEvent The selection changed event or <code>null</code>.
	 */
	public SelectionChangedBroadcastEvent(TabFolderManager source, SelectionChangedEvent selectionChangedEvent) {
		super(source);
		this.selectionChangedEvent = selectionChangedEvent;
	}

	/**
	 * Convenience method to return the source tab folder manager.
	 *
	 * @return The source tab folder manager.
	 */
	public TabFolderManager getSourceTabFolderManager() {
		return (TabFolderManager)getSource();
	}

	/**
	 * Returns the broadcasted selection changed event.
	 *
	 * @return The broadcasted selection changed event or <code>null</code>.
	 */
	public SelectionChangedEvent getSelectionChangedEvent() {
		return selectionChangedEvent;
	}

	/* (non-Javadoc)
	 * @see java.util.EventObject#toString()
	 */
	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder(getClass().getName());

		String prefix = ""; //$NON-NLS-1$
		// if debugging the event, formating them a little bit better readable.
		if (EventManager.isTracingEnabled())
			prefix = "\n\t\t"; //$NON-NLS-1$

		toString.append(prefix + "source="); //$NON-NLS-1$
		toString.append(source);
		toString.append("," + prefix + "selectionChangedEvent="); //$NON-NLS-1$ //$NON-NLS-2$
		toString.append(selectionChangedEvent);
		toString.append("}"); //$NON-NLS-1$

		return toString.toString();
   }
}
