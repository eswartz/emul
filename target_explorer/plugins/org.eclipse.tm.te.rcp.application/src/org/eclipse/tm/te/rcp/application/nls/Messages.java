/**
 * Messages.java
 * Created on Dec 05, 2010
 *
 * Copyright (c) 2010 Wind River Systems Inc.
 *
 * The right to copy, distribute, modify, or otherwise make use
 * of this software may be licensed only pursuant to the terms
 * of an applicable Wind River license agreement.
 */
package org.eclipse.tm.te.rcp.application.nls;

import org.eclipse.osgi.util.NLS;

/**
 * Target Management product bundle externalized strings management.
 *
 * @author uwe.stieber@windriver.com
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tm.te.rcp.internal.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String SystemSettingsChange_title;
	public static String SystemSettingsChange_message;
	public static String SystemSettingsChange_yes;
	public static String SystemSettingsChange_no;

	public static String PromptOnExitDialog_shellTitle;
	public static String PromptOnExitDialog_message0;
	public static String PromptOnExitDialog_message1;
	public static String PromptOnExitDialog_choice;

	public static String ApplicationWorkbenchAdvisor_noPerspective;

}
