/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.terminals.core.nls;

import org.eclipse.osgi.util.NLS;

/**
 * Target Explorer TCF terminals extensions core plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tm.te.tcf.terminals.core.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String TerminalsLauncher_error_channelConnectFailed;
	public static String TerminalsLauncher_error_channelNotConnected;
	public static String TerminalsLauncher_error_missingProcessPath;
	public static String TerminalsLauncher_error_missingRequiredService;
	public static String TerminalsLauncher_error_illegalNullArgument;
	public static String TerminalsLauncher_error_terminalLaunchFailed;
	public static String TerminalsLauncher_error_terminalExitFailed;
	public static String TerminalsLauncher_error_possibleCause;
	public static String TerminalsLauncher_error_possibleCauseUnknown;
	public static String TerminalsLauncher_cause_subscribeFailed;
	public static String TerminalsLauncher_cause_startFailed;
	public static String TerminalsLauncher_cause_ioexception;

	public static String TerminalsStreamReaderRunnable_error_readFailed;
	public static String TerminalsStreamWriterRunnable_error_writeFailed;
	public static String TerminalsStreamReaderRunnable_error_appendFailed;
}
