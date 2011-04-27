/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.interfaces.preferences;

/**
 * The locator model bundle preference key identifiers..
 */
public interface IPreferenceKeys {
	/**
	 * Common prefix for all core preference keys
	 */
	public final String PREFIX = "tcf.locator.core."; //$NON-NLS-1$

	/**
	 * If set to <code>true</code>, peers having the same agent id are filtered.
	 */
	public final String PREF_FILTER_BY_AGENT_ID = PREFIX + "model.filter.agentid"; //$NON-NLS-1$
}
