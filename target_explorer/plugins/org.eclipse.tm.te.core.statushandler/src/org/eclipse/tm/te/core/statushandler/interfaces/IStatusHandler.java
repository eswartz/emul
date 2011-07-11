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

/**
 * Target Explorer: Status handler API declaration
 */
public interface IStatusHandler extends IExecutableExtension {

	/**
	 * Handle the given status and invoke the callback if finished.
	 * <p>
	 * The method semantic is asynchronous by design. It's up to the
	 * status handle contributor if the implementation is asynchronous
	 * or synchronous. Synchronous implementations must invoke the callback
	 * too if finished.
	 *
	 * @param status The status. Must not be <code>null</code>.
	 * @param done The callback. Must not be <code>null</code>.
	 */
	public void handleStatus(IStatus status, DoneHandleStatus done);

    /**
     * Client call back interface for handleStatus().
     */
    interface DoneHandleStatus {
        /**
         * Called when the status handling is done.
         *
         * @param error – error description if operation failed, <code>null</code> if succeeded.
         */
        void doneHandleStatus(Exception error);
    }
}
