/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.interfaces.tracing;

/**
 * Target Explorer: Runtime plug-in trace slot identifiers.
 */
public interface ITraceIds {

	/**
	 * If activated, trace information about event dispatching is printed out.
	 */
	public static final String TRACE_EVENTS = "trace/events"; //$NON-NLS-1$

	/**
	 * If activated, trace information about asynchronous callbacks is printed out.
	 */
	public static final String TRACE_CALLBACKS = "trace/callbacks"; //$NON-NLS-1$
}
