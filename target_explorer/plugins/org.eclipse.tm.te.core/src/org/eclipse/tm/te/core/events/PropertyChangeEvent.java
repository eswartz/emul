/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.events;

import java.util.EventObject;

import org.eclipse.tm.te.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.core.interfaces.tracing.ITraceIds;

/**
 * Property change event implementation.
 */
public class PropertyChangeEvent extends EventObject {
	private static final long serialVersionUID = -7859159130977760588L;

	private Object eventId;
	private Object oldValue;
	private Object newValue;

	/**
	 * Constructor.
	 *
	 * @param source The source object. Must not be <code>null</code>.
	 * @param eventId The event id. Must not be <code>null</code>.
	 * @param oldValue The old value.
	 * @param newValue The new value.
	 *
	 * @exception IllegalArgumentException if eventId == null.
	 */
	public PropertyChangeEvent(Object source, Object eventId, Object oldValue, Object newValue) {
		super(source);

		if (eventId == null)
		    throw new IllegalArgumentException("null eventId"); //$NON-NLS-1$

		this.eventId = eventId;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * Returns the event id.
	 *
	 * @return The event id.
	 */
	public final Object getEventId() {
		return eventId;
	}

	/**
	 * Returns the old value.
	 *
	 * @return The old value or <code>null</code>.
	 */
	public final Object getOldValue() {
		return oldValue;
	}

	/**
	 * Returns the new value.
	 *
	 * @return The new value or <code>null</code>.
	 */
	public final Object getNewValue() {
		return newValue;
	}

	/*
	 * Formats a value due to its type.
	 */
	private Object formatValue(Object value) {
		Object formattedValue = value;
		if (value != null && value.getClass().isArray()) {
			StringBuilder str = new StringBuilder();
			str.append("{"); //$NON-NLS-1$
			for (int i = 0; i < ((Object[]) value).length; i++) {
				if (i > 0) str.append(","); //$NON-NLS-1$
				str.append(formatValue(((Object[]) value)[i]));
			}
			str.append("}"); //$NON-NLS-1$
			formattedValue = str.toString();
		}
		return formattedValue;
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
		toString.append("," + prefix + "oldValue="); //$NON-NLS-1$ //$NON-NLS-2$
		toString.append(formatValue(oldValue));
		toString.append("," + prefix + "newValue="); //$NON-NLS-1$ //$NON-NLS-2$
		toString.append(formatValue(newValue));
		toString.append("}"); //$NON-NLS-1$

		return toString.toString();
   }
}
