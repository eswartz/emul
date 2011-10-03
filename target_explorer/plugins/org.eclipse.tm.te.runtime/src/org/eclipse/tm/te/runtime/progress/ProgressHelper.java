/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.progress;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.tm.te.runtime.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.callback.Callback;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.interfaces.tracing.ITraceIds;

/**
 * Helper implementation to deal with progress monitors and callbacks.
 */
public final class ProgressHelper {

	public static final int PROGRESS_DONE = 0;
	public static final int PROGRESS_NONE = -1;

	/**
	 * Checks if there was an error or the operation was canceled.
	 *
	 * @param caller
	 *            The caller or <code>null</code>.
	 * @param status
	 *            The status. <code>null</code> if status should not be checked.
	 * @param progress
	 *            The progress monitor. <code>null</code> if cancel should not
	 *            be checked.
	 * @param callback
	 *            The callback to call on cancel or error.
	 *
	 * @return <code>false</code> if everything is OK.
	 */
	public static final boolean isCancelOrError(Object caller, IStatus status, IProgressMonitor progress, ICallback callback) {
		if (status == null) status = Status.OK_STATUS;

		if (!status.isOK() || (progress != null && progress.isCanceled())) {
			if (status.getSeverity() == IStatus.CANCEL || (progress != null && progress.isCanceled())) {
				status = new Status(IStatus.CANCEL, status.getPlugin(),
									status.getCode(), status.getMessage(),
									new OperationCanceledException());
			} else if (status.getSeverity() == IStatus.ERROR) {
				Throwable e = status.getException();
				try {
					throw e;
				} catch (Throwable thrown) {
					e = thrown;
				}
				CoreBundleActivator.getTraceHandler().trace(
						status.getMessage(),
						1,
						ITraceIds.TRACE_CALLBACKS,
						status.getSeverity(),
						caller != null ? caller.getClass() : ProgressHelper.class);
				status = new Status(IStatus.ERROR, status.getPlugin(), status.getCode(), status.getMessage(), e);
			}

			if (callback != null) {
				if (caller instanceof ICallback) {
					Callback.copyProperties((ICallback) caller, callback);
				}
				callback.done(caller, status);
			}
			return true;
		}
		return false;
	}

	/**
	 * Checks if the operation was canceled.
	 *
	 * @param caller
	 *            The caller or <code>null</code>.
	 * @param progress
	 *            The progress monitor. Must not be <code>null</code>
	 * @param callback
	 *            The callback to call on cancel or error.
	 *
	 * @return <code>false</code> if everything is OK.
	 */
	public static final boolean isCancel(Object caller, IProgressMonitor progress, ICallback callback) {
		Assert.isNotNull(progress);
		return isCancel(caller, null, progress, callback);
	}

	/**
	 * Checks if the operation was canceled.
	 *
	 * @param caller
	 *            The caller or <code>null</code>.
	 * @param status
	 *            The status. Must not be <code>null</code>.
	 * @param callback
	 *            The callback to call on cancel or error.
	 *
	 * @return <code>false</code> if everything is OK.
	 */
	public static final boolean isCancel(Object caller, IStatus status, ICallback callback) {
		Assert.isNotNull(status);
		return isCancel(caller, status, null, callback);
	}

	/**
	 * Checks if the operation was canceled.
	 *
	 * @param caller
	 *            The caller or <code>null</code>.
	 * @param status
	 *            The status. <code>null</code> if status should not be checked.
	 * @param progress
	 *            The progress monitor. <code>null</code> if cancel should not
	 *            be checked.
	 * @param callback
	 *            The callback to call on cancel or error.
	 *
	 * @return <code>false</code> if everything is OK.
	 */
	public static final boolean isCancel(Object caller, IStatus status, IProgressMonitor progress, ICallback callback) {
		if (status == null) status = Status.OK_STATUS;

		if (status.getSeverity() == IStatus.CANCEL || (progress != null && progress.isCanceled())) {
			status = new Status(IStatus.CANCEL, status.getPlugin(),
					status.getCode(), status.getMessage(),
					new OperationCanceledException());

			if (callback != null) {
				if (caller instanceof ICallback) {
					Callback.copyProperties((ICallback) caller, callback);
				}
				callback.done(caller, status);
			}
			return true;
		}
		return false;
	}

	/**
	 * Checks if there was an error.
	 *
	 * @param caller
	 *            The caller or <code>null</code>.
	 * @param status
	 *            The status. <code>null</code> if status should not be checked.
	 * @param callback
	 *            The callback to call on cancel or error.
	 *
	 * @return <code>false</code> if everything is OK.
	 */
	public static final boolean isError(Object caller, IStatus status, ICallback callback) {
		if (status == null) status = Status.OK_STATUS;

		if (!status.isOK() && status.getSeverity() != IStatus.CANCEL) {
			if (status.getSeverity() == IStatus.ERROR) {
				Throwable e = status.getException();
				try {
					throw e;
				} catch (Throwable thrown) {
					e = thrown;
				}
				CoreBundleActivator.getTraceHandler().trace(
						status.getMessage(),
						1,
						ITraceIds.TRACE_CALLBACKS,
						status.getSeverity(),
						caller != null ? caller.getClass() : ProgressHelper.class);
				status = new Status(IStatus.ERROR, status.getPlugin(), status.getCode(), status.getMessage(), e);
			}

			if (callback != null) {
				if (caller instanceof ICallback) {
					Callback.copyProperties((ICallback) caller, callback);
				}
				callback.done(caller, status);
			}
			return true;
		}
		return false;
	}

	/**
	 * Wraps the given progress monitor into a {@link SubProgressMonitor}. If
	 * the given monitor is <code>null</code>, a {@link NullProgressMonitor} is returned.
	 *
	 * @param progress
	 *            The global progress monitor or <code>null</code>.
	 * @param ticksToUse
	 *            The ticks to use.
	 *
	 * @return The progress monitor to use.
	 */
	public static final IProgressMonitor getProgressMonitor(IProgressMonitor progress, int ticksToUse) {
		if (progress != null) {
			progress = new SubProgressMonitor(progress, ticksToUse, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		} else {
			progress = new NullProgressMonitor();
		}
		return progress;
	}

	/**
	 * Start a task.
	 *
	 * @param progress
	 *            The progress monitor or <code>null</code>.
	 * @param name
	 *            The name (label) of the task.
	 * @param ticks
	 *            The ticks for this task.
	 */
	public static final void beginTask(IProgressMonitor progress, String name, int ticks) {
		if (progress != null) {
			progress.beginTask("", ticks); //$NON-NLS-1$
			progress.setTaskName(name);
		}
	}

	/**
	 * Set a new task name.
	 *
	 * @param progress
	 *            The progress monitor or <code>null</code>.
	 * @param taskName
	 *            The name (label) of the task.
	 */
	public static final void setTaskName(IProgressMonitor progress, String taskName) {
		if (progress != null) {
			progress.setTaskName(taskName);
		}
	}

	/**
	 * Set a new sub task name.
	 *
	 * @param progress
	 *            The progress monitor or <code>null</code>.
	 * @param subTask
	 *            The name (label) of the sub task.
	 */
	public static final void setSubTaskName(IProgressMonitor progress,
			String subTaskName) {
		if (progress != null) {
			progress.subTask(subTaskName);
		}
	}

	/**
	 * Add the given amount of worked steps to the progress monitor.
	 * <p>
	 * If the given amount of worked steps is less or equal than 0, the method
	 * will do nothing.
	 *
	 * @param progress
	 *            The progress monitor or <code>null</code>.
	 * @param worked
	 *            The amount of worked steps.
	 */
	public static final void worked(IProgressMonitor progress, int worked) {
		if (progress != null && !progress.isCanceled() && worked > 0) {
			progress.worked(worked);
		}
	}

	/**
	 * Set the progress monitor done.
	 *
	 * @param progress
	 *            The progress monitor or <code>null</code>.
	 */
	public static final void done(IProgressMonitor progress) {
		if (progress != null) {
			progress.setTaskName(""); //$NON-NLS-1$
			progress.subTask(""); //$NON-NLS-1$
			progress.done();
		}
	}

	/**
	 * Set the progress monitor canceled.
	 *
	 * @param progress
	 *            The progress monitor or <code>null</code>.
	 */
	public static final void cancel(IProgressMonitor progress) {
		if (progress != null && !progress.isCanceled()) {
			progress.setCanceled(true);
		}
	}

	/**
	 * Get the canceled state of the progress monitor.
	 *
	 * @param progress
	 *            The progress monitor or <code>null</code>.
	 *
	 * @return <code>True</code> if the progress monitor is not
	 *         <code>null</code> and if the progress monitor is canceled.
	 */
	public static final boolean isCanceled(IProgressMonitor progress) {
		if (progress != null) {
			return progress.isCanceled();
		}
		return false;
	}
}
