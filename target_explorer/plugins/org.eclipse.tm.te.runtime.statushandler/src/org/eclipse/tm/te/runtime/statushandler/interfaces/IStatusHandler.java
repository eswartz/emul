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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * Interface to be implemented by status handler contributions.
 */
public interface IStatusHandler extends IExecutableExtension {

	/**
	 * Handle the given status and invoke the callback if finished.
	 * <p>
	 * By design, the method behavior is asynchronous. It's up to the status handle contributor if
	 * the implementation is asynchronous or synchronous. Synchronous implementations must invoke
	 * the callback too if finished.
	 *
	 * @param status The status. Must not be <code>null</code>.
	 * @param data The custom status data object, or <code>null</code> if none.
	 * @param done The callback. Must not be <code>null</code>.
	 */
	public void handleStatus(IStatus status, IPropertiesContainer data, DoneHandleStatus done);

	/**
	 * Client call back interface for handleStatus().
	 */
	interface DoneHandleStatus {
		/**
		 * Called when the status handling is done.
		 *
		 * @param error An error if failed, <code>null</code> if succeeded.
		 * @param data The status handler custom result data object, or <code>null</code> if none.
		 */
		void doneHandleStatus(Throwable error, IPropertiesContainer data);
	}
}
