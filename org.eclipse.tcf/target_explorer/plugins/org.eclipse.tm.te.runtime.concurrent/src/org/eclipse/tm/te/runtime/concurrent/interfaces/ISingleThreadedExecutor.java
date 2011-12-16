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

import java.util.concurrent.Executor;

/**
 * Single threaded execution service interface declaration.
 */
public interface ISingleThreadedExecutor extends Executor {

	/**
	 * Returns if or if not the current thread is identical to the executor
	 * thread.
	 *
	 * @return <code>True</code> if the current thread is the executor thread,
	 *         <code>false</code> otherwise.
	 */
	public boolean isExecutorThread();

	/**
	 * Returns if or if not the given thread is identical to the executor
	 * thread.
	 *
	 * @param thread
	 *            The thread or <code>null</code>.
	 * @return <code>True</code> if the current thread is the executor thread,
	 *         <code>false</code> otherwise.
	 */
	public boolean isExecutorThread(Thread thread);
}
