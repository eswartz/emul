/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.statushandler.interfaces;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tm.te.core.interfaces.IExecutableExtension;
import org.eclipse.tm.te.core.interfaces.IPropertiesContainer;

/**
 * Target Explorer: Status handler API declaration
 */
public interface IStatusHandler extends IExecutableExtension {

	/**
	 * Handle the given status and invoke the callback if finished.
	 * <p>
	 * By design, the method behavior is asynchronous. It's up to the
	 * status handle contributor if the implementation is asynchronous
	 * or synchronous. Synchronous implementations must invoke the callback
	 * too if finished.
	 * <p>
	 * If a custom status data object is passed in, the same object must be
	 * passed to the done callback. The status handler may add properties
	 * to the data object to return status handler custom results.
	 *
	 *
	 * @param status The status. Must not be <code>null</code>.
	 * @param data Custom status data or <code>null</code>.
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
         * @param data The custom data object passed in to {@link IStatusHandler#handleStatus(IStatus, IPropertiesContainer, DoneHandleStatus)}.
         */
        void doneHandleStatus(Exception error, IPropertiesContainer data);
    }
}
