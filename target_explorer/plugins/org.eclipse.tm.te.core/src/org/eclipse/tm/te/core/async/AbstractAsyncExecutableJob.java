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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tm.te.core.async.interfaces.IAsyncExecutable;
import org.eclipse.tm.te.runtime.callback.Callback;
import org.eclipse.tm.te.runtime.interfaces.IConditionTester;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.utils.ProgressHelper;

/**
 * Abstract asynchronous executable job implementation.
 */
public abstract class AbstractAsyncExecutableJob extends Job implements IAsyncExecutable {

	/**
	 * Job family id to identify jobs based on {@link AbstractAsyncExecutableJob}.
	 *
	 * @see IJobManager
	 */
	public final String ID = "jobfamily.jobs.asynchronous"; //$NON-NLS-1$

	private int rescheduleDelay = -1;
	private boolean cancelable = true;
	protected boolean finished = false;
	private boolean statusHandled = false;

	private String jobFamily = null;

	private ICallback jobCallback = null;

	/**
	 * Constructor.
	 *
	 * @param name The job name.
	 * @param context The context to refresh.
	 * @param mode The refresh mode.
	 */
	protected AbstractAsyncExecutableJob(String name) {
		super(name);
		setPriority(Job.INTERACTIVE);
		setJobFamily(ID);
	}

	/**
	 * Set the delay if the job should be rescheduled.
	 * @param seconds The reschedule delay or <code>-1</code> if job should not be rescheduled.
	 */
	public final void setRescheduleDelay(int seconds) {
		this.rescheduleDelay = seconds;
	}

	/**
	 * Return the delay in seconds until the job should be rescheduled.
	 */
	public final int getRescheduleDelay() {
		return rescheduleDelay;
	}

	/**
	 * Sets if the result status should be handled by the job manager.
	 * @param handled <code>true</code> if the job manager should not handle the result status.
	 */
	protected void setStatusHandled(boolean handled) {
		this.statusHandled = handled;
	}

	/**
	 * Set if the job can be canceled.
	 * @param cancelable <code>true</code> if the job should be cancelable.
	 */
	public final void setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
	}

	/**
	 * Return <code>true</code> if the job can be canceled.
	 */
	public final boolean isCancelable() {
		return cancelable;
	}

	/**
	 * Set the callback for the job.
	 * @param callback The callback.
	 */
	public final void setJobCallback(ICallback callback) {
		this.jobCallback = callback;
	}

	/**
	 * Return the job callback.
	 */
	public final ICallback getJobCallback() {
		return jobCallback;
	}

	/**
	 * Return <code>true</code> if the job is finished.
	 */
	public final boolean isFinished() {
		return finished;
	}

	/**
	 * Set the job family to identify a job using <code>belongsTo(Object family)</code>.
	 * @param jobFamily The job family or <code>null</code>.
	 */
	public final void setJobFamily(String jobFamily) {
		this.jobFamily = jobFamily;
	}

	/**
	 * Return the job family.
	 */
	public final String getJobFamily() {
		return jobFamily;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */
	@Override
	public boolean belongsTo(Object family) {
		if (family instanceof String && getJobFamily() != null && getJobFamily().startsWith(family.toString())) {
			return true;
		}
		return super.belongsTo(family);
	}

	/**
	 * Number of ticks the job uses to show the progress.
	 *
	 * @return The number of progress ticks.
	 */
	protected abstract int getJobTicks();

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
		finished = false;
		progress = ProgressHelper.getProgressMonitor(progress, ticksToUse);
		int jobTicks = getJobTicks();
		ProgressHelper.beginTask(progress, "", jobTicks); //$NON-NLS-1$
		callback = new Callback(progress, ProgressHelper.PROGRESS_DONE, callback);
		if (getJobCallback() != null && getJobCallback() != callback) {
			callback.addParentCallback(getJobCallback());
		}

		internalExecute(progress, new Callback(progress, ProgressHelper.PROGRESS_DONE, callback) {
			@Override
			protected void internalDone(Object caller, IStatus status) {
				finished = true;
			}
		});
	}

	/**
	 * The job itself.
	 *
	 * @param monitor The progress monitor (never <code>null</code>).
	 * @param callback The callback.
	 */
	protected abstract void internalExecute(final IProgressMonitor monitor, final ICallback callback);

	/**
	 * Hold the execution of {@link #run(IProgressMonitor)} until the asynchronous executable
	 * has completed the execution and invoked the callback.
	 *
	 * @param timeout The timeout in milliseconds. <code>0</code> means wait forever.
	 * @param conditionTester The condition tester which condition must be fulfilled until
	 *                        the execution hold of {@link #run(IProgressMonitor)} can be released.
	 */
	protected abstract void waitAndExecute(long timeout, IConditionTester conditionTester);

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected final IStatus run(final IProgressMonitor monitor) {
		finished = false;
		int jobTicks = getJobTicks();
		ProgressHelper.beginTask(monitor, "", jobTicks); //$NON-NLS-1$
		final Callback callback = new Callback(monitor, ProgressHelper.PROGRESS_DONE, getJobCallback());
		internalExecute(monitor, callback);
		waitAndExecute(0, callback.getDoneConditionTester(isCancelable() ? monitor : null));
		finished = true;

		if (getRescheduleDelay() >= 0 && Platform.isRunning() && (!isCancelable() || !monitor.isCanceled())) {
			schedule(getRescheduleDelay() * 1000);
		}

		return statusHandled ? Status.OK_STATUS : callback.getStatus();
	}
}
