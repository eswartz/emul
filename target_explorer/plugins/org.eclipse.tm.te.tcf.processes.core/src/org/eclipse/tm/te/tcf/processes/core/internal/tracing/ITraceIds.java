/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.core.internal.tracing;

/**
 * Core plug-in trace slot identifiers.
 */
public interface ITraceIds {

	/**
	 * If activated, tracing information about the remote process launcher is printed out.
	 */
	public static final String TRACE_PROCESS_LAUNCHER = "trace/processLauncher"; //$NON-NLS-1$

	/**
	 * If activated, tracing information about the remote processes listener is printed out.
	 */
	public static final String TRACE_PROCESSES_LISTENER = "trace/processesListener"; //$NON-NLS-1$

	/**
	 * If activated, tracing information about the remote processes streams listener is printed out.
	 */
	public static final String TRACE_STREAMS_LISTENER = "trace/streamsListener"; //$NON-NLS-1$
}
