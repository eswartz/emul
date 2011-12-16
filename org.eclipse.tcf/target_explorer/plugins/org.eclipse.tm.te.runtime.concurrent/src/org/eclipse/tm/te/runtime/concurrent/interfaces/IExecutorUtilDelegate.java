/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.concurrent.interfaces;

import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;

/**
 * Executor utility delegate interface declaration.
 */
public interface IExecutorUtilDelegate extends IExecutableExtension {

	/**
	 * Returns if or if not the current thread is an executor thread handled by
	 * this executor utility wait and dispatch delegate.
	 *
	 * @return <code>True</code> if the current thread is handled,
	 *         <code>false</code> otherwise.
	 */
	public boolean isHandledExecutorThread();

	/**
	 * Reads an event from the handled executors event queue, dispatches it
	 * appropriately, and returns <code>true</code> if there is potentially more
	 * work to do, or <code>false</code> if the caller can sleep until another
	 * event is placed on the event queue.
	 *
	 * @return <code>True</code> if there is potentially more work to do,
	 *         <code>false</code> otherwise.
	 */
	public boolean readAndDispatch();
}
