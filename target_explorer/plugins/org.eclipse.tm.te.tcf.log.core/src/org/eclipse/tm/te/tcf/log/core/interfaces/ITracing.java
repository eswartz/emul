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
 * Target Explorer: TCF logging tracing identifiers.
 */
public interface ITracing {

	/**
	 * If enabled, prints information about the logging channel trace listener method invocations.
	 */
	public static String ID_TRACE_CHANNEL_TRACE_LISTENER = "trace/channelTraceListener"; //$NON-NLS-1$
}
