/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.interfaces;

import java.util.Map;

/**
 * Locator model scanner.
 */
public interface IScanner {

	/**
	 * Scanner configuration property: The time in millisecond between the scanner runs.
	 */
	public static String PROP_SCHEDULE = "schedule"; //$NON-NLS-1$

	/**
	 * Set or modify the current scanner configuration.
	 *
	 * @param configuration The new scanner configuration. Must be not <code>null</code>.
	 */
	public void setConfiguration(Map<String, Object> configuration);

	/**
	 * Returns the current scanner configuration.
	 *
	 * @return The current scanner configuration.
	 */
	public Map<String, Object> getConfiguration();

	/**
	 * Terminate the scanner.
	 */
	public void terminate();

	/**
	 * Returns if or if not the discovery model scanner has been terminated.
	 *
	 * @return <code>True</code> if the discovery model scanner is terminated, <code>false</code> if still active.
	 */
	public boolean isTerminated();
}
