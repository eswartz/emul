/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.exceptions;

/**
 * TCF file system implementation base exception.
 */
public class TCFException extends Exception {
	private static final long serialVersionUID = -220092425137980661L;

	/**
	 * Constructor.
	 *
	 * @param message
	 *            The exception detail message or <code>null</code>.
	 */
	public TCFException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 *            The exception detail message or <code>null</code>.
	 * @param cause
	 *            The exception cause or <code>null</code>.
	 */
	public TCFException(String message, Throwable cause) {
		super(message, cause);
	}
}
