/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.events;

import java.util.EventObject;

import org.eclipse.tm.te.runtime.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.interfaces.tracing.ITraceIds;

/**
 * Target Explorer view viewer change event implementation.
 */
public class ViewerContentChangeEvent extends EventObject {
    private static final long serialVersionUID = 7168841012111347036L;

    private String eventId;

    /**
     * Event id used if elements got directly added to the viewer.
     */
    public static final String ADD = "add"; //$NON-NLS-1$

    /**
     * Event id used if elements got directly removed the viewer.
     */
    public static final String REMOVE = "remove"; //$NON-NLS-1$

    /**
     * Event id used if the viewer got refreshed.
     */
    public static final String REFRESH = "refresh"; //$NON-NLS-1$

	/**
	 * Constructor.
	 *
	 * @param source The source object. Must not be <code>null</code>.
	 * @param eventId The event id. Must not be <code>null</code>.
	 *
	 * @exception IllegalArgumentException if eventId == null.
	 */
	public ViewerContentChangeEvent(Object source, String eventId) {
		super(source);

		if (eventId == null) throw new IllegalArgumentException("null eventId"); //$NON-NLS-1$
		this.eventId = eventId;
	}

	/**
	 * Returns the event id.
	 *
	 * @return The event id.
	 */
	public final String getEventId() {
		return eventId;
	}

	/* (non-Javadoc)
	 * @see java.util.EventObject#toString()
	 */
	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder(getClass().getName());

		String prefix = ""; //$NON-NLS-1$
		// if tracing the event, formating them a little bit better readable.
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_EVENTS))
			prefix = "\n\t\t"; //$NON-NLS-1$

		toString.append(prefix + "{eventId="); //$NON-NLS-1$
		toString.append(eventId);
		toString.append("," + prefix + "source="); //$NON-NLS-1$ //$NON-NLS-2$
		toString.append(source);
		toString.append("}"); //$NON-NLS-1$

		return toString.toString();
	}
}
