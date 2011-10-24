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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;

/**
 * Extended single step providing additional life cycle methods.
 */
public interface IExtendedContextStep<Data extends Object> extends IContextStep<Data> {

	/**
	 * Returns if or if not this step can have multiple references within step groups. If
	 * <code>true</code> is returned, the step can occur exactly once per step group. This method
	 * effects all defined step groups and overwrite individual step settings.
	 * <p>
	 * The default implementation returns <code>false</code>.
	 *
	 * @return <code>True</code> if the step can be referenced only ones per step group,
	 *         <code>false</code> otherwise.
	 */
	public boolean isSingleton();

	/**
	 * Initialize the step from the given data.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 * @param fullQualifiedId The full qualified id for this step. Must not be <code>null</code>.
	 * @param monitor The progress monitor. Must not be <code>null</code>.
	 */
	public void initializeFrom(IContext context, Data data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor);

	/**
	 * Validate execution conditions.
	 * <p>
	 * This method is called from
	 * {@link #execute(IContext, Object, IFullQualifiedId, IProgressMonitor, org.eclipse.tm.te.runtime.interfaces.callback.ICallback)}
	 * after the step initialization. If any execution condition is not fulfilled, the method should
	 * throw an {@link CoreException} to signal the failure.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 * @param fullQualifiedId The full qualified id for this step. Must not be <code>null</code>.
	 * @param monitor The progress monitor. Must not be <code>null</code>.
	 *
	 * @throws CoreException if the execution cannot be continue. The associated status should describe the failure cause.
	 */
	public void validateExecute(IContext context, Data data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws CoreException;

	/**
	 * Cleanup intermediate data of the step.
	 * <p>
	 * This method will be called at the end of each step execution.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 * @param fullQualifiedId The full qualified id for this step. Must not be <code>null</code>.
	 * @param monitor The progress monitor. Must not be <code>null</code>.
	 */
	public void cleanup(IContext context, Data data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor);

	/**
	 * Called from the stepper engine once an error occurred during the stepping. Gives
	 * each step, completed previously to the error, the possibility to rollback
	 * whatever the step did.
	 * <p>
	 * <b>Note:</b> It is not guaranteed that the shared step data hasn't been overwritten
	 *              in the meanwhile by multiple invocation of the same step. If a
	 *              step supports multiple invocations, the implementer of the step is
	 *              required to identify all the step data to rollback by himself.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 * @param status The status of the last step executed and that caused the rollback.
	 * @param fullQualifiedId The full qualified id for this step. Must not be <code>null</code>.
	 * @param monitor The progress monitor. Must not be <code>null</code>.
	 * @param callback The callback to invoke if finished. Must not be <code>null</code>.
	 */
	public void rollback(IContext context, Data data, IStatus status, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, ICallback callback);
}
