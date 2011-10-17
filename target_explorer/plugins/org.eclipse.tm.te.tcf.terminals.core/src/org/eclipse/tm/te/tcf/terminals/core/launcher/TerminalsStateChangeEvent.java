/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.terminals.core.launcher;

import org.eclipse.tm.te.runtime.events.ChangeEvent;

/**
 * Remote terminals state change event implementation.
 */
public class TerminalsStateChangeEvent extends ChangeEvent {
    private static final long serialVersionUID = -2119193125420935279L;

	/**
	 * Event id signaling if a remote terminal got created.
	 */
	public static final String EVENT_PROCESS_CREATED = "created"; //$NON-NLS-1$

	/**
	 * Event id signaling if a remote terminal terminated.
	 */
	public static final String EVENT_PROCESS_TERMINATED = "terminated"; //$NON-NLS-1$

	/**
	 * Event id signaling that the communication with the terminal got lost, probably because of a
	 * channel closed event. Any listener waiting for a terminal termination event should react on
	 * this event and stop waiting.
	 */
	public static final String EVENT_LOST_COMMUNICATION = "lostCommunication"; //$NON-NLS-1$

	// Exit code of the remote process
	private final int exitCode;

	/**
	 * Constructor.
	 *
	 * @param source The event source. Must not be <code>null</code>.
	 * @param eventId The event id. Must not be <code>null</code>.
	 * @param oldValue The old value or <code>null</code>.
	 * @param newValue The new value or <code>null</code>.
	 */
	public TerminalsStateChangeEvent(Object source, Object eventId, Object oldValue, Object newValue) {
		this(source, eventId, oldValue, newValue, -1);
	}

	/**
	 * Constructor.
	 *
	 * @param source The event source. Must not be <code>null</code>.
	 * @param eventId The event id. Must not be <code>null</code>.
	 * @param oldValue The old value or <code>null</code>.
	 * @param newValue The new value or <code>null</code>.
	 * @param exitCode The process exit code or <code>-1</code> if not applicable.
	 */
	public TerminalsStateChangeEvent(Object source, Object eventId, Object oldValue, Object newValue, int exitCode) {
		super(source, eventId, oldValue, newValue);
		this.exitCode = exitCode;
	}

	/**
	 * Returns the remote process exit code. The exit code is only applicable if the set event id is
	 * {@link #EVENT_PROCESS_TERMINATED}.
	 *
	 * @return The remote process exit code or <code>-1</code> if not applicable.
	 */
	public final int getExitCode() {
		return exitCode;
	}
}
