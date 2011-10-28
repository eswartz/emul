/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.async.interfaces;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;

/**
 * Describes a common interface of an encapsulated executable.
 * <p>
 * All required steps combined to execute the action must be executed by the implementor of the
 * executable. The executable itself has no UI or job control. It is only supposed to use the
 * provided progress monitors to give feedback and report errors using exceptions and the callback
 * mechanism.
 */
public interface IAsyncExecutable {

	/**
	 * Execute the necessary steps to complete the executable.
	 * <p>
	 * If the executable has been finished, the specified callback <b>must</b> be called!
	 *
	 * @param callback The callback to invoke or <code>null</code>
	 */
	public void execute(ICallback callback);

	/**
	 * Execute the necessary steps to complete the executable.
	 * <p>
	 * User feedback can be provided through the specified progress monitor, if not <code>null</code>.
	 * <p>
	 * If the executable has been finished, the specified callback <b>must</b> be called!
	 *
	 * @param progress The progress monitor or <code>null</code>.
	 * @param ticksToUse The ticks to use from the progress monitor.
	 * @param callback The callback to invoke or <code>null</code>
	 */
	public void execute(IProgressMonitor progress, int ticksToUse, ICallback callback);
}
