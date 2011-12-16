/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.events;

import java.util.EventObject;

import org.eclipse.tm.te.runtime.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.interfaces.tracing.ITraceIds;

/**
 * Event used to signal the disposal of an element.
 */
public class DisposedEvent extends EventObject {
	private static final long serialVersionUID = -8900361742097122798L;

	private final Object data;

	/**
	 * Constructor.
	 *
	 * @param source The event source. Must not be <code>null</code>.
	 *               <p>
	 *               The event source is expected to be of type {@link CTabItem}.
	 *
	 * @param data The custom data object or <code>null</code>.
	 */
	public DisposedEvent(Object source, Object data) {
		super(source);
		this.data = data;
	}

	/**
	 * Returns the custom data object associated with the disposed terminal console.
	 *
	 * @return The custom data object or <code>null</code>.
	 */
	public final Object getData() {
		return data;
	}

	/* (non-Javadoc)
	 * @see java.util.EventObject#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer(getClass().getName());

		String prefix = ""; //$NON-NLS-1$
		// if debugging the event, formating them a little bit better readable.
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(1, ITraceIds.TRACE_EVENTS))
			prefix = "\n\t\t"; //$NON-NLS-1$

		toString.append(prefix + "{source="); //$NON-NLS-1$
		toString.append(source);
		toString.append("}"); //$NON-NLS-1$

		return toString.toString();
	}

}
