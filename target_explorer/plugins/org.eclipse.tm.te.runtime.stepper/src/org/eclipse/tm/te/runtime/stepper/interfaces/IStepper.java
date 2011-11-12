/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.stepper.interfaces;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tm.te.runtime.interfaces.IConditionTester;
import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;


/**
 * A stepper.
 * <p>
 * Stepper are executing a set of steps and/or step groups read from a given type for the current
 * sub type. The stepper is responsible for handling any exceptions which occurred during step
 * execution.
 * <p>
 * Stepper are synchronous where steps are asynchronous.
 * <p>
 * Stepper must run in worker threads.
 */
public interface IStepper extends IExecutableExtension {

	public static final String ID_TYPE_STEPPER_ID = "Stepper"; //$NON-NLS-1$
	public static final String ID_TYPE_CONTEXT_ID = "Context"; //$NON-NLS-1$
	public static final String ID_TYPE_STEP_GROUP_ID = "StepGroup"; //$NON-NLS-1$
	public static final String ID_TYPE_STEP_GROUP_ITERATION_ID = "StepGroupIteration"; //$NON-NLS-1$
	public static final String ID_TYPE_STEP_ID = "Step"; //$NON-NLS-1$

	/**
	 * Condition Tester to test for finished execution of the associated stepper.
	 */
	public static class ExecutionFinishedConditionTester implements IConditionTester {
		private final IStepper stepper;

		/**
		 * Constructor.
		 *
		 * @param stepper The stepper. Must not be <code>null</code>.
		 */
		public ExecutionFinishedConditionTester(IStepper stepper) {
			Assert.isNotNull(stepper);
			this.stepper = stepper;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.runtime.interfaces.IConditionTester#cleanup()
		 */
		@Override
        public void cleanup() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.runtime.interfaces.IConditionTester#isConditionFulfilled()
		 */
		@Override
        public boolean isConditionFulfilled() {
			return stepper.isFinished();
		}
	}

	/**
	 * Initialize the stepper for a run. This method must be called before <i><code>execute()</code>
	 * </i>. Once the stepper finished the execution, the initialization is reseted and must be
	 * renewed before <i><code>execute()</code></i> can be called again.
	 *
	 * @param data The data. Must not be <code>null</code>.
	 * @param fullQualifiedId The full qualified id of this stepper.
	 * @param monitor The progress monitor. Must not be <code>null</code>.
	 *
	 * @throws IllegalStateException If called if the stepper is in initialized state already.
	 */
	public void initialize(IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws IllegalStateException;

	/**
	 * Returns if or if not the stepper got initialized for a new run.
	 * <p>
	 * The <i><code>execute()</code></i> method cannot be called if the stepper is not correctly
	 * initialized for each run. The initialized state can be set only by calling the <i>
	 * <code>initialize(...)</code></i> method. <i> <code>cleanup()</code></i> will reset the
	 * initialized state back to uninitialized.
	 *
	 * @return <code>True</code> if initialized, <code>false</code> otherwise.
	 */
	public boolean isInitialized();

	/**
	 * Executes the configured steps. The method is synchronous and must return only if all steps
	 * finished or an exception occurred.
	 * <p>
	 * Steps are assumed to be asynchronous. The stepper implementor must wait for callback(s) to be
	 * invoked by the step implementor(s) before the sequence can continue.
	 * <p>
	 * <b>Note:</b> Waiting for the step callback must not block the UI thread.
	 *
	 * @throws CoreException In case the execution fails or is canceled.
	 */
	public void execute() throws CoreException;

	/**
	 * Returns if or if not the stepper finished the execution.
	 *
	 * @return <code>True</code> if the execution is finished, <code>false</code> otherwise.
	 */
	public boolean isFinished();

	/**
	 * Cleanup and reset the stepper into a defined state.
	 */
	public void cleanup();
}
