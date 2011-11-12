/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.stepper.extensions;

import java.util.Date;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.te.runtime.callback.Callback;
import org.eclipse.tm.te.runtime.concurrent.util.ExecutorsUtil;
import org.eclipse.tm.te.runtime.interfaces.ISharedConstants;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.stepper.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContext;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStep;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepExecutor;
import org.eclipse.tm.te.runtime.stepper.interfaces.IExtendedContextStep;
import org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tm.te.runtime.stepper.interfaces.tracing.ITraceIds;
import org.eclipse.tm.te.runtime.utils.ProgressHelper;
import org.eclipse.tm.te.runtime.utils.StatusHelper;

/**
 * Step executor implementation.
 * <p>
 * The step executor is responsible for initiating the execution of a single step. The executor
 * creates and associated the step callback and blocks the execution till the executed step invoked
 * the callback.
 * <p>
 * The step executor is passing any status thrown by the executed step to the parent stepper
 * instance for handling.
 * <p>
 * If the step to execute is of type {@link IExtendedContextStep}, the step executor is calling
 * {@link IExtendedContextStep#initializeFrom(org.eclipse.tm.te.runtime.stepper.interfaces.IContext, Object, org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId, IProgressMonitor)} and
 * {@link IExtendedContextStep#validateExecute(org.eclipse.tm.te.runtime.stepper.interfaces.IContext, Object, org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId, IProgressMonitor)} before calling
 * {@link IContextStep#execute(org.eclipse.tm.te.runtime.stepper.interfaces.IContext, Object, org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId, IProgressMonitor, org.eclipse.tm.te.runtime.interfaces.callback.ICallback)}.
 * <p>
 * The methods will be called within the current step executor thread.
 * <p>
 * The stepper implementation can be traced and profiled by setting the debug options:
 * <ul>
 * <li><i>org.eclipse.tm.te.runtime.stepper/trace/stepping</i></li>
 * <li><i>org.eclipse.tm.te.runtime.stepper/profile/stepping</i></li>
 * </ul>
 */
public abstract class AbstractContextStepExecutor implements IContextStepExecutor {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepExecutor#execute(org.eclipse.tm.te.runtime.stepper.interfaces.IContextStep, org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.tm.te.runtime.stepper.interfaces.IContext, org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public final void execute(IContextStep step, IFullQualifiedId id, final IContext context, final IPropertiesContainer data, IProgressMonitor progress) throws CoreException {
		Assert.isNotNull(step);
		Assert.isNotNull(id);
		Assert.isNotNull(context);
		Assert.isNotNull(data);
		Assert.isNotNull(progress);

		long startTime = System.currentTimeMillis();

		CoreBundleActivator.getTraceHandler().trace("AbstractContextStepExecutor#execute: *** START (" + step.getLabel() + ")", //$NON-NLS-1$ //$NON-NLS-2$
													0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
		CoreBundleActivator.getTraceHandler().trace(" [" + ISharedConstants.TIME_FORMAT.format(new Date(startTime)) + "]" //$NON-NLS-1$ //$NON-NLS-2$
													+ " ***", //$NON-NLS-1$
													0, ITraceIds.PROFILE_STEPPING, IStatus.WARNING, this);

		int ticksToUse = (step instanceof IExtendedContextStep) ? ((IExtendedContextStep)step).getTotalWork(context, data) : IProgressMonitor.UNKNOWN;
		progress = ProgressHelper.getProgressMonitor(progress, ticksToUse);
		ProgressHelper.beginTask(progress, step.getLabel(), ticksToUse);

		// Create the handler (and the callback) for the current step
		final Callback callback = new Callback();

		// Catch any exception that might occur during execution.
		// Errors are passed through by definition.
		try {
			// Execute the step. Spawn to the dispatch thread if necessary.
			if (step instanceof IExtendedContextStep) {
				IExtendedContextStep extendedStep = (IExtendedContextStep)step;

				// IExtendedContextStep provides protocol for initialization and validation.
				extendedStep.initializeFrom(context, data, id, progress);

				// step is initialized -> now validate for execution.
				// If the step if not valid for execution, validateExecute is throwing an exception.
				extendedStep.validateExecute(context, data, id, progress);
			}

			step.execute(context, data, id, progress, callback);

			// Wait till the step finished, an execution occurred or the
			// user hit cancel on the progress monitor.
			ExecutorsUtil.waitAndExecute(0, callback.getDoneConditionTester(null));

			// Check the status of the step
			normalizeStatus(step, id, context, data, callback.getStatus());
		}
		catch (Exception e) {
			CoreBundleActivator.getTraceHandler().trace("AbstractContextStepExecutor#execute: Exception catched: class ='" + e.getClass().getName() + "'" //$NON-NLS-1$ //$NON-NLS-2$
														+ ", message = '" + e.getLocalizedMessage() + "'"  //$NON-NLS-1$ //$NON-NLS-2$
														+ ", cause = "  //$NON-NLS-1$
														+ (e instanceof CoreException ? ((CoreException)e).getStatus().getException() : e.getCause()),
														0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);

			// If the exception is a CoreException by itself, just re-throw
			if (e instanceof CoreException) {
				// Check if the message does need normalization
				if (isExceptionMessageFormatted(e.getLocalizedMessage())) {
					throw (CoreException)e;
				}
				// We have to normalize the status message first
				normalizeStatus(step, id, context, data, ((CoreException)e).getStatus());
			} else {
				// all other exceptions are repackaged within a CoreException
				normalizeStatus(step, id, context, data, StatusHelper.getStatus(e));
			}
		}
		finally {
			if (!progress.isCanceled()) {
				progress.done();
			}

			// Give the step a chance for cleanup
			if (step instanceof IExtendedContextStep) {
				((IExtendedContextStep)step).cleanup(context, data, id, progress);
			}

			long endTime = System.currentTimeMillis();
			CoreBundleActivator.getTraceHandler().trace("AbstractContextStepExecutor#execute: *** DONE (" + step.getLabel() + ")", //$NON-NLS-1$ //$NON-NLS-2$
														0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
			CoreBundleActivator.getTraceHandler().trace(" [" + ISharedConstants.TIME_FORMAT.format(new Date(endTime)) //$NON-NLS-1$
														+ " , delay = " + (endTime - startTime) + " ms]" //$NON-NLS-1$ //$NON-NLS-2$
														+ " ***", //$NON-NLS-1$
														0, ITraceIds.PROFILE_STEPPING, IStatus.WARNING, this);
		}
	}

	private void normalizeStatus(IContextStep step, IFullQualifiedId id, IContext context , IPropertiesContainer data, IStatus status) throws CoreException {
		Assert.isNotNull(context);
		Assert.isNotNull(data);
		Assert.isNotNull(id);
		Assert.isNotNull(step);

		if (status == null || status.isOK()) {
			return;
		}

		switch (status.getSeverity()) {
			case IStatus.CANCEL:
				throw new OperationCanceledException(status.getMessage());
			default:
				String message = formatMessage(status.getMessage(), status.getSeverity(), step, id, context, data);
				status = new Status(status.getSeverity(), status.getPlugin(), status.getCode(), message != null ? message : status.getMessage(), status.getException());
				throw new CoreException(status);
		}
	}

	/**
	 * Checks if the given message is already formatted to get displayed to the user.
	 *
	 * @param message The message. Must not be <code>null</code>.
	 * @return <code>True</code> if the message is already formatted to get displayed to the user, <code>false</code> otherwise.
	 */
	protected abstract boolean isExceptionMessageFormatted(String message);

	/**
	 * Format the message depending on the severity.
	 *
	 * @param message The message to format.
	 * @param severity The message severity.
	 * @param step The step.
	 * @param id The full qualified step id.
	 * @param context The target context.
	 * @param data The step data.
	 * @return Formatted message.
	 */
	protected abstract String formatMessage(String message, int severity, IContextStep step, IFullQualifiedId id, IContext context, IPropertiesContainer data);

}
