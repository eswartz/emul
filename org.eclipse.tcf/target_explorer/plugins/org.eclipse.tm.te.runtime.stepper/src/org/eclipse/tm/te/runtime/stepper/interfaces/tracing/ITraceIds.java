/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.stepper.interfaces.tracing;

/**
 * Stepper Runtime plug-in trace slot identifiers.
 */
public interface ITraceIds {

	/**
	 * If activated, trace information about step execution is printed out.
	 */
	public static final String TRACE_STEPPING = "trace/stepping"; //$NON-NLS-1$

	/**
	 * If activated, profile information about step execution is printed out.
	 */
	public static final String PROFILE_STEPPING = "profile/stepping"; //$NON-NLS-1$
}
