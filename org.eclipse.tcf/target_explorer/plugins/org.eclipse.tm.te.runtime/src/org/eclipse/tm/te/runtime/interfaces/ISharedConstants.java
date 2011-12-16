/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.interfaces;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Shared constants.
 */
public interface ISharedConstants {

	/**
	 *  Local time format presenting full time format including milliseconds.
	 */
	public final static DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS"); //$NON-NLS-1$
}
