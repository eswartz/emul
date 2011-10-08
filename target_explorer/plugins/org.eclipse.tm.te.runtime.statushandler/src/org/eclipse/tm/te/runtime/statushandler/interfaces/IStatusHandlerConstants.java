/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.statushandler.interfaces;

/**
 * Status handler constants.
 */
public interface IStatusHandlerConstants {

	/**
	 * The id of the default status handler.
	 */
	public final static String ID_DEFAUT_HANDLER = "org.eclipse.tm.te.statushandler.default"; //$NON-NLS-1$

	/**
	 * The status to handle is a question (yes/no) (value 0x100).
	 */
	public final static int QUESTION = 0x100;

	/**
	 * The status to handle is a question (yes/no) with cancel (value 0x200).
	 */
	public final static int YES_NO_CANCEL = 0x200;

	/**
	 * Property: The title of the message dialog.
	 *           The value is expected to be a string.
	 */
	public final static String PROPERTY_TITLE = "title"; //$NON-NLS-1$

	/**
	 * Property: The context help id of the message dialog.
	 *           The value is expected to be a string.
	 */
	public final static String PROPERTY_CONTEXT_HELP_ID = "contextHelpId"; //$NON-NLS-1$

	/**
	 * Property: The preference slot id for the &quot;don't ask again&quot; checkbox.
	 *           The value is expected to be a string.
	 */
	public final static String PROPERTY_DONT_ASK_AGAIN_ID = "dontAskAgainId"; //$NON-NLS-1$

	/**
	 * Property: The caller of the status handler. The value is expected to
	 *           be the caller object or the callers class object.
	 */
	public final static String PROPERTY_CALLER = "caller"; //$NON-NLS-1$
}
