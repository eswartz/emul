/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.callback;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.te.runtime.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.interfaces.IConditionTester;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.progress.ProgressHelper;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;

/**
 * Default implementation of the <code>ICallback</code> interface.
 */
public class Callback extends PropertiesContainer implements ICallback {

	protected static final String PROPERTY_PARENT_CALLBACK = "parentCallback"; //$NON-NLS-1$
	protected static final String PROPERTY_PROGRESS_MONITOR = "progressMonitor"; //$NON-NLS-1$
	protected static final String PROPERTY_PROGRESS_TICKS = "progressTicks"; //$NON-NLS-1$
	protected static final String PROPERTY_IS_DONE = "isDone"; //$NON-NLS-1$
	protected static final String PROPERTY_STATUS = "status"; //$NON-NLS-1$

	/**
	 * Property: Asynchronous operations can store a result to the callback
	 *           object they invoke once the operation has been finished.
	 */
	protected final String PROPERTY_RESULT = "result"; //$NON-NLS-1$


	private static final String[] PROPERTY_KEYS_NOT_TO_COPY = {
													PROPERTY_PARENT_CALLBACK, PROPERTY_PROGRESS_MONITOR,
													PROPERTY_PROGRESS_TICKS, PROPERTY_IS_DONE, PROPERTY_STATUS
												};
	private static final List<String> PROPERTY_KEYS_NOT_TO_COPY_LIST = Arrays.asList(PROPERTY_KEYS_NOT_TO_COPY);

	/**
	 * Condition tester for ExecutorsUtil to check whether the callback is done
	 * or the {@link IProgressMonitor} is canceled.
	 */
	private class CallbackDoneConditionTester implements IConditionTester {
		final ICallback callback;
		final IProgressMonitor monitor;

		/**
		 * Constructor.
		 *
		 * @param callback
		 *            The callback to check. Must not be <code>null</code>.
		 * @param monitor
		 *            The progress monitor to check.
		 */
		public CallbackDoneConditionTester(ICallback callback, IProgressMonitor monitor) {
			Assert.isNotNull(callback);
			this.callback = callback;
			this.monitor = monitor;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.runtime.interfaces.IConditionTester#isConditionFulfilled()
		 */
		@Override
		public boolean isConditionFulfilled() {
			if (monitor == null) {
				return callback.isDone();
			}
			return monitor.isCanceled() || callback.isDone();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.runtime.interfaces.IConditionTester#cleanup()
		 */
		@Override
		public void cleanup() {
		}
	}

	/**
	 * Constructor.
	 */
	public Callback() {
		this(null);
	}

	/**
	 * Constructor to wrap a parent callback.
	 *
	 * @param parentCallback
	 *            The parent callback.
	 */
	public Callback(ICallback parentCallback) {
		this(null, -1, parentCallback);
	}

	/**
	 * Constructor to handle a progress monitor.
	 *
	 * @param monitor
	 *            The progress monitor.
	 * @param ticksToUse
	 *            The ticks to add when done.
	 */
	public Callback(IProgressMonitor monitor, int ticksToUse) {
		this(monitor, ticksToUse, null);
	}

	/**
	 * Constructor to handle a progress monitor and wrap a parent callback.
	 *
	 * @param monitor
	 *            The progress monitor.
	 * @param ticksToUse
	 *            The ticks to add when done.
	 * @param parentCallback
	 *            The parent callback.
	 */
	public Callback(IProgressMonitor monitor, int ticksToUse, ICallback parentCallback) {
		super();
		setProperty(PROPERTY_PARENT_CALLBACK, parentCallback);
		setProperty(PROPERTY_PROGRESS_MONITOR, monitor);
		setProperty(PROPERTY_PROGRESS_TICKS, ticksToUse);
	}

	/**
	 * Get a condition tester for this callback.
	 *
	 * @param monitor
	 *            The progress monitor or <code>null</code>.
	 * @return The condition tester.
	 */
	public final IConditionTester getDoneConditionTester(IProgressMonitor monitor) {
		return new CallbackDoneConditionTester(this, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.callback.ICallback#done(java.lang.Object, org.eclipse.core.runtime.IStatus)
	 */
	@Override
	public final void done(Object caller, IStatus status) {
		Assert.isNotNull(status);

		if (isDone()) {
			CoreBundleActivator.getTraceHandler().trace("WARNING: callback called twice!!", 1, this); //$NON-NLS-1$
			return;
		}

		status = checkStatusIntegrity(status);

		setProperty(PROPERTY_IS_DONE, true);
		setProperty(PROPERTY_STATUS, status);

		ICallback parentCallback = (ICallback) getProperty(PROPERTY_PARENT_CALLBACK);
		if (parentCallback != null && parentCallback.isDone()) {
			CoreBundleActivator.getTraceHandler().trace("WARNING: parent callback called twice!!", 1, this); //$NON-NLS-1$
		}

		internalDone(caller, status);

		if (isDone()) {
			if (getProperty(PROPERTY_PROGRESS_MONITOR) instanceof IProgressMonitor) {
				IProgressMonitor progress = ((IProgressMonitor) getProperty(PROPERTY_PROGRESS_MONITOR));
				if (!progress.isCanceled() && getStatus().getSeverity() != IStatus.CANCEL) {
					int ticks = getIntProperty(PROPERTY_PROGRESS_TICKS);
					if (ticks > 0) {
						progress.worked(ticks);
					} else if (ticks == ProgressHelper.PROGRESS_DONE) {
						progress.done();
					}
				}
			}

			parentCallback = (ICallback) getProperty(PROPERTY_PARENT_CALLBACK);
			if (parentCallback != null && !parentCallback.isDone()) {
				copyProperties(this, parentCallback);
				if (!ProgressHelper.isCancelOrError(this,
													getStatus(),
													(IProgressMonitor) getProperty(PROPERTY_PROGRESS_MONITOR),
													parentCallback)) {
					parentCallback.done(caller, getStatus());
				}
			}
		}
	}

	/**
	 * Copy the properties from the given source callback to the given
	 * destination callback.
	 *
	 * @param source
	 *            The source callback. Must not be <code>null</code>.
	 * @param destination
	 *            The destination callback. Must not be <code>null</code> and
	 *            not yet done.
	 */
	public static final void copyProperties(ICallback source, ICallback destination) {
		Assert.isNotNull(source);
		Assert.isNotNull(destination);
		Assert.isTrue(!destination.isDone());

		for (String key : source.getProperties().keySet()) {
			if (!PROPERTY_KEYS_NOT_TO_COPY_LIST.contains(key)) {
				destination.setProperty(key, source.getProperty(key));
			}
		}
		Assert.isTrue(!destination.isDone());
	}

	/**
	 * Checks the status integrity.
	 *
	 * @param status
	 *            The status or <code>null</code>.
	 * @return The checked status.
	 */
	private IStatus checkStatusIntegrity(IStatus status) {
		if (status == null) status = Status.OK_STATUS;

		if (status.getSeverity() == IStatus.CANCEL && status.getException() == null) {
			status = new Status(IStatus.CANCEL, status.getPlugin(),
								status.getMessage(), new OperationCanceledException(status.getMessage()));
		}

		if (status.getSeverity() == IStatus.WARNING && status.getException() == null) {
			status = new Status(IStatus.WARNING, status.getPlugin(),
								status.getMessage(), new Exception(status.getMessage()));
		}

		if (status.getSeverity() == IStatus.ERROR && status.getException() == null) {
			status = new Status(IStatus.ERROR, status.getPlugin(),
								status.getMessage(), new Exception(status.getMessage()));
		}

		return status;
	}

	/**
	 * Return the progress monitor or <code>null</code>.
	 */
	protected final IProgressMonitor getProgressMonitor() {
		return (IProgressMonitor) getProperty(PROPERTY_PROGRESS_MONITOR);
	}

	/**
	 * Internal callback done.
	 *
	 * @param caller
	 *            The caller.
	 * @param status
	 *            The status.
	 */
	protected void internalDone(Object caller, IStatus status) {
	}

	/**
	 * Return the result on done.
	 */
	public final IStatus getStatus() {
		IStatus status = (IStatus) getProperty(PROPERTY_STATUS);
		if (status == null && getProperty(PROPERTY_PROGRESS_MONITOR) instanceof IProgressMonitor) {
			IProgressMonitor monitor = (IProgressMonitor) getProperty(PROPERTY_PROGRESS_MONITOR);
			if (monitor.isCanceled()) {
				return checkStatusIntegrity(Status.CANCEL_STATUS);
			}
		}

		return checkStatusIntegrity(status);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.callback.ICallback#isDone()
	 */
	@Override
	public final boolean isDone() {
		return getBooleanProperty(PROPERTY_IS_DONE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.callback.ICallback#addParentCallback(org.eclipse.tm.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void addParentCallback(ICallback callback) {
		if (getProperty(PROPERTY_PARENT_CALLBACK) instanceof ICallback) {
			ICallback parentCallback = (ICallback) getProperty(PROPERTY_PARENT_CALLBACK);
			parentCallback.addParentCallback(callback);
		} else {
			setProperty(PROPERTY_PARENT_CALLBACK, callback);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.callback.ICallback#setResult(java.lang.Object)
	 */
	@Override
	public void setResult(Object result) {
		setProperty(PROPERTY_RESULT, result);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.callback.ICallback#getResult()
	 */
	@Override
	public Object getResult() {
	    return getProperty(PROPERTY_RESULT);
	}
}
