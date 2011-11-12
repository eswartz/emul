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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * A single step associated with a context.
 * <p>
 * Context steps are assumed to be asynchronous. If the context step execution
 * finished, the passed in <b>callback must be invoked</b>. The parent launch
 * stepper suspend the step sequence execution till the callback is invoked.
 * <p>
 * Context steps signals the execution state to the parent launch stepper via
 * the <code>IStatus</code> object passed to the callback as first argument.
 * The status object is mandatory and cannot be <code>null</code>. If the step
 * execution succeeds, an status with severity {@link IStatus#OK} is expected.
 */
public interface IContextStep extends IExecutableExtension {

	/**
	 * Additional data property for ICallback.
	 */
	public static final String CALLBACK_PROPERTY_DATA = "data"; //$NON-NLS-1$

	/**
	 * Executes the context step logic.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @param data The data giving object. Must not be <code>null</code>.
	 * @param fullQualifiedId The full qualified id for this step. Must not be <code>null</code>.
	 * @param monitor The progress monitor. Must not be <code>null</code>.
	 * @param callback The callback to invoke if finished. Must not be <code>null</code>.
	 */
	public void execute(IContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, ICallback callback);

	/**
	 * Returns the number of total work the step is consuming.
	 *
	 * @return The number of total work or {@link IProgressMonitor#UNKNOWN}.
	 */
	public int getTotalWork(IContext context, IPropertiesContainer data);

	/**
	 * Returns the list of required context step or context step group id's. The
	 * execution of a context step fails if not all of the required steps are
	 * available or have not been executed before.
	 * <p>
	 * If the listed required steps have dependencies on their own, these dependencies
	 * are implicitly inherited.
	 *
	 * @return The list of required context step or context step group id's or an empty list.
	 */
	public String[] getDependencies();
}
