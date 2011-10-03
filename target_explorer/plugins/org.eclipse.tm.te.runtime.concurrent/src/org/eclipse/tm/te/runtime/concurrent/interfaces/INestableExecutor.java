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
 * Nestable execution interface declaration.
 */
public interface INestableExecutor extends Executor {

	/**
	 * Returns the maximum allowed nesting depth. If this methods returns an
	 * integer value <= 0, nesting is disabled.
	 *
	 * @return The maximum allowed nesting depth or 0 to disable nesting.
	 */
	public int getMaxDepth();

	/**
	 * Reads the next command from the task queue and execute it if the maximum
	 * allowed nesting depth has not been exceeded. If the maximum nesting depth
	 * has been reached, the method will throw an {@link IllegalStateException}.
	 *
	 * @return <code>True</code> if there is potentially more work to do, or
	 *         <code>false</code> if the caller can sleep until another event is
	 *         placed on the task queue.
	 */
	public boolean readAndExecute();
}
