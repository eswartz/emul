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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.te.core.async.interfaces.IAsyncExecutable;
import org.eclipse.tm.te.runtime.callback.Callback;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.utils.ProgressHelper;

/**
 * Asynchronous executable stepper job.
 * <p>
 * The job executes a list of actions. If any action returns with an error, the whole job will be
 * aborted.
 */
public class AsyncExecutableStepperJob extends AbstractAsyncExecutableJob {
	private final IAsyncExecutable[] actions;

	/**
	 * Constructor.
	 *
	 * @param name The job name. Must not be <code>null</code>
	 * @param actions The actions to execute. Must not be <code>null</code>.
	 */
	public AsyncExecutableStepperJob(String name, IAsyncExecutable[] actions) {
		super(name);

		Assert.isNotNull(actions);
		this.actions = actions;
	}

	/**
	 * Returns the actions to execute.
	 *
	 * @return The actions to execute.
	 */
	protected IAsyncExecutable[] getActions() {
		return actions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.async.AbstractAsyncExecutableJob#getJobTicks()
	 */
	@Override
	protected int getJobTicks() {
		return getActions().length * 100;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.async.AbstractAsyncExecutableJob#internalExecute(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tm.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	protected final void internalExecute(final IProgressMonitor monitor, final ICallback callback) {
		if (getActions() == null || getActions().length == 0) {
			callback.done(this, Status.OK_STATUS);
		}
		else {
			ICallback stepCallback = new Callback() {
				private int index = 0;

				/* (non-Javadoc)
				 * @see org.eclipse.tm.te.runtime.callback.Callback#internalDone(java.lang.Object, org.eclipse.core.runtime.IStatus)
				 */
				@Override
				protected void internalDone(Object caller, IStatus status) {
					if (!ProgressHelper.isCancelOrError(caller, status, getProgressMonitor(), null) && index < getActions().length) {
						setProperty(PROPERTY_IS_DONE, false);
						getActions()[index++].execute(monitor, 100, this);
					}
					else {
						callback.done(caller, status);
					}
				}
			};
			stepCallback.done(this, Status.OK_STATUS);
		}
	}
}
