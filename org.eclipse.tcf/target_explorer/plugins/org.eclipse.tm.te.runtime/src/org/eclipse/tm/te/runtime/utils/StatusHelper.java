/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.utils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.te.runtime.activator.CoreBundleActivator;

/**
 * Helper implementation to deal with status objects and throwable's.
 */
public final class StatusHelper {

	/**
	 * Converts a throwable to an IStatus (OK, CANCEL, ERROR).
	 *
	 * @param error The throwable or <code>null</code>.
	 * @return The status.
	 */
	public static final IStatus getStatus(Throwable error) {
		if (error == null) return Status.OK_STATUS;

		int severity = IStatus.ERROR;
		if (error instanceof CoreException && ((CoreException)error).getStatus() != null) {
			return ((CoreException)error).getStatus();
		}
		else if (error instanceof OperationCanceledException) {
			severity = IStatus.CANCEL;
		}

		String message = error.getLocalizedMessage();
		if (message == null) message = error.getMessage();

		return new Status(severity, CoreBundleActivator.getUniqueIdentifier(), message != null ? message : error.toString(), error);
	}
}
