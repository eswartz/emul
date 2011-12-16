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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.te.runtime.callback.Callback;
import org.eclipse.tm.te.runtime.concurrent.util.ExecutorsUtil;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tm.te.runtime.interfaces.ISharedConstants;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.stepper.StepperAttributeUtil;
import org.eclipse.tm.te.runtime.stepper.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContext;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextManipulator;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStep;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepExecutor;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroupIterator;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroupable;
import org.eclipse.tm.te.runtime.stepper.interfaces.IExtendedContextStep;
import org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tm.te.runtime.stepper.interfaces.IStepper;
import org.eclipse.tm.te.runtime.stepper.interfaces.IVariantDelegate;
import org.eclipse.tm.te.runtime.stepper.interfaces.tracing.ITraceIds;
import org.eclipse.tm.te.runtime.stepper.nls.Messages;
import org.eclipse.tm.te.runtime.utils.ProgressHelper;
import org.eclipse.tm.te.runtime.utils.StatusHelper;

/**
 * An abstract stepper implementation.
 */
public abstract class AbstractContextStepper extends ExecutableExtension implements IStepper, IContextManipulator {

	private boolean initialized = false;
	private boolean finished = false;
	private IPropertiesContainer data = null;
	private IFullQualifiedId fullQualifiedId = null;
	private IProgressMonitor monitor = null;
	private String activeContextId = null;
	private IContext activeContext = null;
	private boolean cancelable = true;

	protected class ExecutedContextStep {
		final IFullQualifiedId id;
		final IContextStep step;

		public ExecutedContextStep(IFullQualifiedId id, IContextStep step) {
			this.id = id;
			this.step = step;
		}
	}

	/**
	 * Constructor.
	 */
	public AbstractContextStepper() {
		super();
	}

	/**
	 * Returns the name of what is executed by the stepper.
	 */
	protected abstract String getName();

	/**
	 * Returns the type if what is executed by the stepper.
	 */
	protected abstract String getType();

	/**
	 * Returns the sub type if what is executed by the stepper.
	 */
	protected abstract String getSubType();

	/**
	 * Returns the contexts the stepper is working with.
	 */
	protected abstract IContext[] getContexts();

	/**
	 * Returns the variant delegate to use or <code>null</code>.
	 */
	protected abstract IVariantDelegate getVariantDelegate() throws CoreException;

	/**
	 * Creates a new instance of the step executor to use for executing a step.
	 */
	protected abstract IContextStepExecutor doCreateStepExecutor(IContextStep step, String secondaryId, IFullQualifiedId fullQualifiedStepId);

	/**
	 * Returns the step group for the given arguments.
	 */
	protected abstract IContextStepGroup getStepGroup(String type, String subType, String variant);

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IStepper#initialize(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public void initialize(IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws IllegalStateException {
		Assert.isNotNull(data);
		Assert.isNotNull(fullQualifiedId);
		Assert.isNotNull(monitor);

		// Assert stepper is not in use
		if (isInitialized()) {
			throw new IllegalStateException("Stepper instance already initialized!"); //$NON-NLS-1$
		}

		// set the initial stepper attributes
		this.data = data;
		this.monitor = monitor;
		this.fullQualifiedId = fullQualifiedId;

		// but not finished yet
		this.finished = false;

		// set the initial stepper attributes
		this.activeContext = null;
		this.activeContextId = null;

		setInitialized();

		CoreBundleActivator.getTraceHandler().trace("AbstractContextStepper#initialize:" //$NON-NLS-1$
													+ " type='" + getType() + "'" //$NON-NLS-1$ //$NON-NLS-2$
													+ ", mode='" + getSubType() + "'", //$NON-NLS-1$ //$NON-NLS-2$
													0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
	}

	/**
	 * Marks the stepper to be fully initialized.
	 */
	protected final void setInitialized() {
		initialized = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IStepper#isInitialized()
	 */
	@Override
    public final boolean isInitialized() {
		return initialized;
	}

	/**
	 * Sets the cancelable state of the stepper.
	 *
	 * @param cancelable <code>True</code> if the stepper shall be cancelable, <code>false</code> if not.
	 */
	protected final void setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
	}

	/**
	 * Returns the cancelable state of the stepper.
	 *
	 * @return <code>True</code> if the stepper is cancelable, <code>false</code> if not.
	 */
	protected final boolean isCancelable() {
		return cancelable;
	}

	/**
	 * Sets the active context.
	 *
	 * @param context The active context or <code>null</code>.
	 */
	protected final void setActiveContext(IContext context) {
		// do not use equals() here!!!
		if (activeContext != context) {
			if (activeContext instanceof IPropertiesContainer) {
				((IPropertiesContainer)activeContext).setProperty("stepperContext::" + getId(), null); //$NON-NLS-1$
			}
			activeContext = context;
			if (activeContext instanceof IPropertiesContainer) {
				((IPropertiesContainer)activeContext).setProperty("stepperContext::" + getId(), true); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Get the active context.
	 *
	 * @return The active context or <code>null</code>.
	 */
	protected IContext getActiveContext() {
		if (isInitialized() && activeContext == null) {
			IContext newContext = (IContext)StepperAttributeUtil.getProperty(IContextManipulator.CONTEXT, getFullQualifiedId(), getData());
			if (newContext != null) {
				setActiveContext(newContext);
			}
			if (activeContext == null) {
				IContext[] contexts =	getContexts();
				if (contexts != null && contexts.length > 0) {
					for (IContext context : contexts) {
						setActiveContext(context);
						StepperAttributeUtil.setProperty(IContextManipulator.CONTEXT, getFullQualifiedId(), getData(), activeContext);
						break;
					}
				}
			}
		}
		return activeContext;
	}

	/**
	 * Sets the active context id.
	 *
	 * @param contextId The active context id or <code>null</code>.
	 */
	protected final void setActiveContextId(String contextId) {
		activeContextId = contextId;
	}

	/**
	 * Get the active context id.
	 *
	 * @return The active context id or <code>null</code>.
	 */
	protected String getActiveContextId() {
		if (isInitialized() && activeContextId == null) {
			String newContextId = (String)StepperAttributeUtil.getProperty(IContextManipulator.CONTEXT_ID, getFullQualifiedId(), getData());
			if (newContextId != null && newContextId.trim().length() > 0) {
				activeContextId = newContextId.trim();
			}
			if (activeContextId == null) {
				IContext context = getActiveContext();
				if (context != null) {
					activeContextId = context.getContextId();
					StepperAttributeUtil.setProperty(IContextManipulator.CONTEXT_ID, getFullQualifiedId(), getData(), activeContextId);
				}
			}
		}
		return activeContextId;
	}

	/**
	 * Returns the currently associated data. The method returns
	 * <code>null</code> if the stepper is not in initialized state.
	 *
	 * @return The data or <code>null</code>
	 */
	protected final IPropertiesContainer getData() {
		return isInitialized() ? data : null;
	}

	/**
	 * Returns the full qualified id for this stepper.
	 *
	 * @return The full qualified stepper id.
	 */
	protected final IFullQualifiedId getFullQualifiedId() {
		return fullQualifiedId;
	}

	/**
	 * Returns the currently associated progress monitor. The method returns
	 * <code>null</code> if the stepper is not in initialized state.
	 *
	 * @return The progress monitor or <code>null</code>
	 */
	protected final IProgressMonitor getMonitor() {
		return isInitialized() ? monitor : null;
	}

	/**
	 * Marks the stepper to be finished.
	 */
	protected final void setFinished() {
		finished = true;
		if (activeContext instanceof IPropertiesContainer) {
			((IPropertiesContainer)activeContext).setProperty("stepperContext::" + getId(), null); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IStepper#isFinished()
	 */
	@Override
    public final boolean isFinished() {
		return finished;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IStepper#cleanup()
	 */
	@Override
    public void cleanup() {
		// Set the progress monitor done here in any case
		if (getMonitor() != null) getMonitor().done();

		// Reset the initial stepper attributes
		data = null;
		monitor = null;
		fullQualifiedId = null;
		finished = false;
		initialized = false;

		// Reset the initial stepper attributes
		setActiveContext(null);
		setActiveContextId(null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(getClass().getSimpleName());
		buffer.append(" (" + getLabel() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(": "); //$NON-NLS-1$
		buffer.append("id = " + getId()); //$NON-NLS-1$
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IStepper#execute()
	 */
	@Override
    public final void execute() throws CoreException {
		long startTime = System.currentTimeMillis();

		CoreBundleActivator.getTraceHandler().trace("AbstractContextStepper#execute: *** ENTERED", //$NON-NLS-1$
													0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
		CoreBundleActivator.getTraceHandler().trace(" [" + ISharedConstants.TIME_FORMAT.format(new Date(startTime)) + "]" //$NON-NLS-1$ //$NON-NLS-2$
													+ " ***", //$NON-NLS-1$
													0, ITraceIds.PROFILE_STEPPING, IStatus.WARNING, this);

		try {
			// stepper must be initialized before executing
			if (!isInitialized()) {
				throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
					Messages.AbstractContextStepper_error_initializeNotCalled));
			}

			// Create a container for collecting the non-severe status objects
			// during the step execution. Non-severe status objects will
			// be hold back till the execution completed or stopped with an error.
			// Severe status objects are errors or cancellation.
			List<IStatus> statusContainer = new ArrayList<IStatus>();

			// start execution
			internalExecute(statusContainer);

			// If the warnings container is not empty, create a new status and
			// throw a core exception
			if (!statusContainer.isEmpty()) {
				IStatus status = null;

				// Check if we need a multi status
				if (statusContainer.size() > 1) {
					MultiStatus multiStatus =
						new MultiStatus(CoreBundleActivator.getUniqueIdentifier(), 0,
										NLS.bind(Messages.AbstractContextStepper_multiStatus_finishedWithWarnings, getName()), null);
					for (IStatus subStatus : statusContainer) {
						multiStatus.merge(subStatus);
					}
					status = multiStatus;
				}
				else {
					status = statusContainer.get(0);
				}

				throw new CoreException(status);
			}
		}
		finally {
			// Mark the stepper finished
			setFinished();

			long endTime = System.currentTimeMillis();
			CoreBundleActivator.getTraceHandler().trace("AbstractContextStepper#execute: *** DONE", //$NON-NLS-1$
														0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
			CoreBundleActivator.getTraceHandler().trace(" [" + ISharedConstants.TIME_FORMAT.format(new Date(endTime)) //$NON-NLS-1$
														+ " , delay = " + (endTime - startTime) + " ms]" //$NON-NLS-1$ //$NON-NLS-2$
														+ " ***", //$NON-NLS-1$
														0, ITraceIds.PROFILE_STEPPING, IStatus.WARNING, this);
		}
	}

	/**
	 * Executes a step or step group.
	 *
	 * @param statusContainer The status container. Must not be <code>null</code>.
	 * @throws CoreException If the execution fails.
	 */
	protected void internalExecute(List<IStatus> statusContainer) throws CoreException {
		Assert.isNotNull(statusContainer);

		// Get the variant delegate
		IVariantDelegate variantDelegate = getVariantDelegate();
		String[] variants = null;
		if (variantDelegate != null) {
			// Determine the valid variants
			variants = variantDelegate.getValidVariants(getActiveContext(), getData());
		}

		// Get the step group
		IContextStepGroup stepGroup = null;
		if (variants != null) {
			for (String variant : variants) {
				stepGroup = getStepGroup(getType(), getSubType(), variant);
				if (stepGroup != null) {
					break;
				}
			}
		}
		if (stepGroup == null) {
			stepGroup = getStepGroup(getType(), getSubType(), null);
		}

		// If no step group could be found for any of the valid variants, throw an exception
		if (stepGroup == null) {
			throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
											   NLS.bind(Messages.AbstractContextStepper_error_missingStepGroup, getName())));
		}

		// Initialize the progress monitor
		getMonitor().beginTask(stepGroup.getLabel(), calculateTotalWork(stepGroup));

		IFullQualifiedId fullQualifiedId = getFullQualifiedId().createChildId(ID_TYPE_CONTEXT_ID, getActiveContextId(), null);
		fullQualifiedId = fullQualifiedId.createChildId(ID_TYPE_STEP_GROUP_ID, stepGroup.getId(), null);
		// Execute the step group
		executeStepGroup(stepGroup, statusContainer, new ArrayList<ExecutedContextStep>(), fullQualifiedId);
	}

	/**
	 * Executes a step group.
	 *
	 * @param stepGroup The step group. Must be not <code>null</code>.
	 * @param statusContainer A list holding the warnings occurred during the execution. Must be not <code>null</code>.
	 * @param executedSteps A list holding the id's of the steps executed before. Must be not <code>null</code>.
	 * @param fullQualifiedGroupId The hierarchy of all parent step group id's separated by "::". Must be not <code>null</code>.
	 *
	 * @throws CoreException If the execution fails.
	 */
	private void executeStepGroup(IContextStepGroup stepGroup, List<IStatus> statusContainer, List<ExecutedContextStep> executedSteps, IFullQualifiedId fullQualifiedGroupId) throws CoreException {
		Assert.isNotNull(stepGroup);
		Assert.isNotNull(statusContainer);
		Assert.isNotNull(executedSteps);
		Assert.isNotNull(fullQualifiedGroupId);

		// Return immediately if the user canceled the monitor in the meanwhile
		if (isCancelable() && getMonitor().isCanceled()) {
			rollback(executedSteps, Status.CANCEL_STATUS, getMonitor());
			throw new CoreException(StatusHelper.getStatus(new OperationCanceledException()));
		}

		CoreBundleActivator.getTraceHandler().trace("AbstractContextStepper#execute: step group: '" + stepGroup.getLabel() + "'", //$NON-NLS-1$ //$NON-NLS-2$
													0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);

		IContextStepGroupIterator iterator = stepGroup.getStepGroupIterator();
		IFullQualifiedId fullQualifiedIterationId = fullQualifiedGroupId;
		int iteration = 0;

		if (iterator != null) {
			iterator.initialize(getActiveContext(), getData(), fullQualifiedGroupId, getMonitor());
		}
		boolean next = iterator == null || iterator.hasNext(getActiveContext(), getData(), fullQualifiedGroupId, getMonitor());

		while (next) {
			if (iterator != null) {
				fullQualifiedIterationId = fullQualifiedGroupId.createChildId(ID_TYPE_STEP_GROUP_ITERATION_ID, iterator.getId(), ""+iteration); //$NON-NLS-1$
				iterator.next(getActiveContext(), getData(), fullQualifiedIterationId, getMonitor());
				// set the active context if the step has manipulated it
				if (iterator instanceof IContextManipulator) {
					IContext newContext =
						(IContext)StepperAttributeUtil.getProperty(IContextManipulator.CONTEXT, fullQualifiedIterationId, getData());
					String newContextId =
						StepperAttributeUtil.getStringProperty(IContextManipulator.CONTEXT_ID, fullQualifiedIterationId, getData());
					if (newContext != null) {
						setActiveContext(newContext);
					}
					if (newContextId != null &&
						newContextId.trim().length() > 0) {
						setActiveContextId(newContextId.trim());
					}
				}
			}
			// Get the list of steps or step groups to execute.
			IContextStepGroupable[] groupables = stepGroup.getSteps(getType(), getSubType());
			for (IContextStepGroupable groupable : groupables) {
				executeGroupable(groupable, statusContainer, executedSteps, fullQualifiedIterationId);
			}
			iteration++;
			next = iterator != null && iterator.hasNext(getActiveContext(), getData(), fullQualifiedGroupId, getMonitor());
		}
	}

	/**
	 * Executes a step groupable. The groupable might encapsulate a
	 * step or a step group.
	 *
	 * @param step The step groupable. Must be not <code>null</code>.
	 * @param statusContainer A list holding the warnings occurred during the execution. Must be not <code>null</code>.
	 * @param executedSteps A list holding the id's of the steps executed before. Must be not <code>null</code>.
	 * @param fullQualifiedParentId The hierarchy of all parent step group id's separated by "::". Must be not <code>null</code>.
	 *
	 * @throws CoreException If the execution failed.
	 */
	private void executeGroupable(IContextStepGroupable groupable, List<IStatus> statusContainer, List<ExecutedContextStep> executedSteps, IFullQualifiedId fullQualifiedParentId) throws CoreException {
		Assert.isNotNull(groupable);
		Assert.isNotNull(statusContainer);
		Assert.isNotNull(executedSteps);
		Assert.isNotNull(fullQualifiedParentId);

		// Return immediately if the user canceled the monitor in the meanwhile
		if (isCancelable() && getMonitor() != null && getMonitor().isCanceled()) {
			rollback(executedSteps, Status.CANCEL_STATUS, getMonitor());
			throw new CoreException(StatusHelper.getStatus(new OperationCanceledException()));
		}

		// If the passed in groupable is disabled -> we are done immediately
		if (groupable.isDisabled()) {
			CoreBundleActivator.getTraceHandler().trace("AbstractContextStepper#executeGroupable: DROPPED DISABLED groupable: id = '" + groupable.getExtension().getId() + "'" //$NON-NLS-1$ //$NON-NLS-2$
														+ ", secondaryId = '" + groupable.getSecondaryId() + "'", //$NON-NLS-1$ //$NON-NLS-2$
														0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
			return;
		}

		// Check if all dependencies of the groupable have been executed before
		checkForDependenciesExecuted(groupable, executedSteps);

		if (groupable.getExtension() instanceof IContextStepGroup) {
			IFullQualifiedId id = fullQualifiedParentId.createChildId(ID_TYPE_STEP_GROUP_ID, groupable.getExtension().getId(), groupable.getSecondaryId());
			// If the passed in groupable is associated with a step group
			// -> get the groupable from that group and execute them
			executeStepGroup((IContextStepGroup)groupable.getExtension(), statusContainer, executedSteps, id);
		}
		else if (groupable.getExtension() instanceof IContextStep) {
			// If the passed in groupable is associated with a step
			// -> check if the required steps have been executed before,
			//    create a step executor and invoke the executor.
			IContextStep step = (IContextStep)groupable.getExtension();

			IFullQualifiedId id = fullQualifiedParentId.createChildId(ID_TYPE_STEP_ID, step.getId(), groupable.getSecondaryId());

			// Create the step executor now
			IContextStepExecutor executor = doCreateStepExecutor(step, groupable.getSecondaryId(), id);
			Assert.isNotNull(executor);

			try {
				executedSteps.add(new ExecutedContextStep(id, step));
				// Invoke the executor now
				executor.execute(step, id, getActiveContext(), getData(), getMonitor());
				// set the active context if the step has manipulated it
				if (step instanceof IContextManipulator) {
					IContext newContext = (IContext)StepperAttributeUtil.getProperty(IContextManipulator.CONTEXT, id, getData());
					String newContextId = StepperAttributeUtil.getStringProperty(IContextManipulator.CONTEXT_ID, id, getData());
					if (newContext != null) {
						setActiveContext(newContext);
					}
					if (newContextId != null &&
						newContextId.trim().length() > 0) {
						setActiveContextId(newContextId.trim());
					}
				}
			}
			catch (Exception e) {
				// Catch the CoreException first hand as we need to continue the
				// stepping if the step returned with warnings or information only.
				CoreException coreException = normalizeStatus(e, statusContainer);
				// If the exception has been not eaten, rollback previously executed
				// steps and re-throw the exception.
				if (coreException != null) {
					// Rollback everything, if the step(s) are supporting this and
					// the cleanup hasn't been done already.
					if (isInitialized()) {
						rollback(executedSteps, coreException.getStatus(), getMonitor());
					}

					// Re-throw the exception
					throw coreException;
				}
			}
		}
	}

	/**
	 * Checks if all required dependencies have been executed before. If not, the method
	 * will throw an error status.
	 *
	 * @param groupable The groupable. Must be not <code>null</code>.
	 * @param executedSteps A list holding the id's of the steps executed before. Must be not <code>null</code>.
	 *
	 * @throws CoreException If a dependency has not been executed before.
	 */
	protected void checkForDependenciesExecuted(IContextStepGroupable groupable, List<ExecutedContextStep> executedSteps) throws CoreException {
		Assert.isNotNull(groupable);
		Assert.isNotNull(executedSteps);

		// Build up the complete list of dependencies.
		List<String> dependencies = new ArrayList<String>(Arrays.asList(groupable.getDependencies()));
		// If the groupable wraps a step, the step can have additional dependencies to check
		if (groupable.getExtension() instanceof IContextStep) {
			dependencies.addAll(Arrays.asList(((IContextStep)groupable.getExtension()).getDependencies()));
		}

		// Check each dependency now.
		for (String dependency : dependencies) {
			// The dependencies might be fully qualified. Split out the primary id.
			String[] splitted = dependency.split("##", 2); //$NON-NLS-1$
			String primaryId = splitted.length == 2 ? splitted[0] : dependency;

			// Check if the id is in the list of executed steps. As the list contains
			// the fully qualified id's, we cannot just check for contained
			boolean requiredStepExecuted = false;
			for (ExecutedContextStep step : executedSteps) {
				if (step.step.getId().equals(primaryId)) {
					requiredStepExecuted = true;
					break;
				}
			}

			if (!requiredStepExecuted) {
				throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
					MessageFormat.format(Messages.AbstractContextStepper_error_requiredStepNotExecuted,
										 NLS.bind((groupable.getExtension() instanceof IContextStep
										 				? Messages.AbstractContextStepper_error_step
										 				: Messages.AbstractContextStepper_error_requiredStepOrGroup), dependency),
										 NLS.bind(Messages.AbstractContextStepper_error_typeAndSubtype, getType(), getSubType()))));
			}

			// Recursive checking is not necessary here as the step or step group
			// id's would have made it the executed steps list of they missed required
			// steps or step groups.
		}

	}

	/**
	 * Rollback the steps previously executed to the failed step. The rollback
	 * is executed in reverse order and the step must be of type {@link IWRExtendedTargetContextStep}
	 * to participate in the rollback.
	 *
	 * @param executedSteps
	 * @param progress
	 */
	protected final void rollback(final List<ExecutedContextStep> executedSteps, final IStatus rollBackStatus, IProgressMonitor progress) {
		Assert.isNotNull(executedSteps);

		final IProgressMonitor rollbackProgress = ProgressHelper.getProgressMonitor(progress, 1);
		ProgressHelper.beginTask(rollbackProgress, "Cancel", executedSteps.size()); //$NON-NLS-1$
		final Callback finalCallback = new Callback();
		final Callback rollbackCallback = new Callback() {
			@Override
			protected void internalDone(Object caller, IStatus status) {
				if (!executedSteps.isEmpty()) {
					setProperty(PROPERTY_IS_DONE, false);
					ExecutedContextStep executedStep = executedSteps.remove(executedSteps.size()-1);
					if (executedStep.step instanceof IExtendedContextStep) {
						IExtendedContextStep step = (IExtendedContextStep)executedStep.step;
						step.rollback(getActiveContext(), getData(), rollBackStatus, executedStep.id, rollbackProgress, this);
					}
					else {
						this.done(this, status);
					}
				}
				else {
					finalCallback.done(this, Status.OK_STATUS);
				}
			}
		};

		rollbackCallback.done(this, rollBackStatus);
		ExecutorsUtil.waitAndExecute(0, finalCallback.getDoneConditionTester(null));
	}

	/**
	 * Calculates the total work required for the step group. The total
	 * work is the sum of the total work of each sub step. If one of the
	 * steps returns {@link IProgressMonitor#UNKNOWN}, the total work will
	 * be unknown for the whole step group.
	 *
	 * @param stepGroup The step group. Must be not <code>null</code>.
	 * @return The total work required or {@link IProgressMonitor#UNKNOWN}.
	 *
	 * @throws CoreException If the total work of the step group cannot be determined.
	 */
	protected int calculateTotalWork(IContextStepGroup stepGroup) throws CoreException {
		Assert.isNotNull(stepGroup);

		int totalWork = 0;

		// Loop the group steps and summarize the returned total work
		IContextStepGroupable[] groupables = stepGroup.getSteps(getType(), getSubType());
		for (IContextStepGroupable groupable : groupables) {
			int work = groupable.getExtension() instanceof IContextStep
								? ((IContextStep)groupable.getExtension()).getTotalWork(getActiveContext(), getData())
								: groupable.getExtension() instanceof IContextStepGroup
										? calculateTotalWork((IContextStepGroup)groupable.getExtension())
									    : IProgressMonitor.UNKNOWN;

			if (work == IProgressMonitor.UNKNOWN) {
				totalWork = IProgressMonitor.UNKNOWN;
				break;
			}

			totalWork += work;
		}

		return totalWork;
	}

	/**
	 * Normalize the associated status object of the given {@link CoreException}.
	 * <p>
	 * If the associated status contains only WARNING or INFORMATION status objects,
	 * the objects are added to the passed in status container. The passed in exception
	 * is dropped and the method will return <code>null</code>.
	 * <p>
	 * If the associated status contains only OK status objects, the passed in
	 * exception and the associated status are dropped and the method will return
	 * <code>null</code>.
	 * <p>
	 * If the associated status contain ERROR status objects, the passed in exception
	 * and the associated status objects are returned if the passed in status container
	 * is empty. If the status container is not empty, a new exception and multi status
	 * object is created and returned. The multi status object will contain all status
	 * objects from the status container and all objects of the originally associated
	 * status.
	 * <p>
	 * If the associated status contains a CANCEL status object, the passed in
	 * exception and the associated status objects are returned unmodified.
	 *
	 * @param e The core exception. Must be not <code>null</code>.
	 * @param statusContainer The list of non-severe status objects. Must be not <code>null</code>.
	 * @return The exception to re-throw or <code>null</code>.
	 */
	private CoreException normalizeStatus(Exception e, List<IStatus> statusContainer) {
		Assert.isNotNull(statusContainer);

		CoreException coreException = null;

		IStatus status = Status.OK_STATUS;
		// Get the associated status from the exception
		if (e instanceof CoreException) {
			status = ((CoreException)e).getStatus();
			coreException = (CoreException)e;
		}
		else if (e instanceof OperationCanceledException) {
			status = new Status(IStatus.CANCEL, CoreBundleActivator.getUniqueIdentifier(), e.getLocalizedMessage(), e);
			coreException = new CoreException(status);
		}
		else if (e != null) {
			status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getLocalizedMessage(), e);
			coreException = new CoreException(status);
		}

		// Check the severity
		// PS: MultiStatus.getSeverity() returns always the highest severity.
		if (status.getSeverity() == IStatus.OK) {
			// OK -> drop completely and return null
			coreException = null;
		}
		else if (status.getSeverity() == IStatus.CANCEL) {
			// CANCEL -> Check monitor to be canceled.
			if (isCancelable()) {
				if (getMonitor() != null && !getMonitor().isCanceled()) {
					getMonitor().setCanceled(true);
				}
			}
		}
		else if (status.getSeverity() == IStatus.WARNING || status.getSeverity() == IStatus.INFO) {
			// WARNING or INFORMATION -> add to the list and return null
			statusContainer.add(status);
			coreException = null;
		}
		else if (status.getSeverity() == IStatus.ERROR) {
			// Error -> If the warnings container not empty, create
			//          a new MultiStatus.
			if (!statusContainer.isEmpty()) {
				MultiStatus multiStatus = new MultiStatus(status.getPlugin(), status.getCode(),
														  NLS.bind(Messages.AbstractContextStepper_multiStatus_finishedWithErrors, getName()), null);
				for (IStatus stat : statusContainer) {
					multiStatus.merge(stat);
				}
				// Re-throw via a new CoreException
				coreException = new CoreException(multiStatus);
			}
		}

		return coreException;
	}
}
