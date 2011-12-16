/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.async;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tm.te.core.async.interfaces.IAsyncExecutable;
import org.eclipse.tm.te.runtime.callback.Callback;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;
import org.eclipse.tm.te.runtime.utils.ProgressHelper;

/**
 * Abstract asynchronous executable implementation.
 */
public abstract class AbstractAsyncExecutable extends PropertiesContainer implements IAsyncExecutable {

	private boolean cancelable = true;

	/**
	 * Constructor.
	 */
	public AbstractAsyncExecutable() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.async.interfaces.IAsyncExecutable#execute(org.eclipse.tm.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
    public final void execute(ICallback callback) {
		execute(null, ProgressHelper.PROGRESS_NONE, callback);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.async.interfaces.IAsyncExecutable#execute(org.eclipse.core.runtime.IProgressMonitor, int, org.eclipse.tm.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
    public final void execute(IProgressMonitor progress, int ticksToUse, ICallback callback) {
		progress = ProgressHelper.getProgressMonitor(progress, ticksToUse);
		int jobTicks = getJobTicks();
		ProgressHelper.beginTask(progress, getTaskName(), jobTicks);
		callback = new Callback(progress, ProgressHelper.PROGRESS_DONE, callback);

		internalExecute(progress, callback);
	}

	protected abstract void internalExecute(IProgressMonitor monitor, ICallback callback);

	/**
	 * Return the ticks for this executable.
	 */
	protected int getJobTicks() {
		return 100;
	}

	/**
	 * Return the name of the task.
	 */
	protected String getTaskName() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Return <code>true</code> if the job can be canceled.
	 */
	public final boolean isCancelable() {
		return cancelable;
	}

	/**
	 * Set if the job can be canceled.
	 *
	 * @param cancelable <code>true</code> if the job should be cancelable.
	 */
	public final void setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
	}
}
