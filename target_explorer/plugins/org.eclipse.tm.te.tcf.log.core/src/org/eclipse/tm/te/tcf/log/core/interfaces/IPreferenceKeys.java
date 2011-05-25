/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.log.core.interfaces;


/**
 * Target Explorer: TCF logging bundle preference key identifiers.
 */
public interface IPreferenceKeys {
	/**
	 * Common prefix for all core preference keys
	 */
	public final String PREFIX = "tcf.log.core."; //$NON-NLS-1$

	/**
	 * If set to <code>true</code>, back-end communication is logged.
	 */
	public final String PREF_LOGGING_ENABLED = PREFIX + "enabled"; //$NON-NLS-1$

	/**
	 * If set to <code>true</code>, locator heart beat events are logged.
	 */
	public final String PREF_SHOW_HEARTBEATS = PREFIX + "show.heartbeats"; //$NON-NLS-1$

	/**
	 * The maximum number of bytes the log files are allowed to grow to, in bytes.
	 * Defaults to 5MB.
	 */
	public final String PREF_MAX_FILE_SIZE = PREFIX + "limits.fileSize"; //$NON-NLS-1$

	/**
	 * The maximum number of files kept in the cycle.
	 * Defaults to 5.
	 */
	public final String PREF_MAX_FILES_IN_CYCLE = PREFIX + "limits.inCycle"; //$NON-NLS-1$
}
