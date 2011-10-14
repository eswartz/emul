/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.services.interfaces.constants;

/**
 * Line separator constants.
 * <p>
 * Copied from <code>org.eclipse.tm.internal.terminal.local.ILocalTerminalSettings</code>.
 */
public interface ILineSeparatorConstants {

	/**
	 * The line separator setting CR (carriage return only; for example, used by Mac OS 9).
	 */
	public final static String LINE_SEPARATOR_CR = "\\r"; //$NON-NLS-1$

	/**
	 * The line separator setting CRLF (carriage return and line feed; for example, used by
	 * Windows).
	 */
	public final static String LINE_SEPARATOR_CRLF = "\\r\\n"; //$NON-NLS-1$

	/**
	 * The line separator setting LF (line feed only; used by all UNIX-based systems).
	 */
	public final static String LINE_SEPARATOR_LF = "\\n"; //$NON-NLS-1$
}
