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
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * A step executor.
 */
public interface IContextStepExecutor {

	/**
	 * Executes the associated step.
	 *
	 * @param step The step to execute. Must not be <code>null</code>.
	 * @param id The full qualified step id. Must not be <code>null</code>.
	 * @param context The context. Must not be <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 * @param monitor The progress monitor. Must not be <code>null</code>.
	 *
	 * @throws CoreException if the execution cannot be continue. The associated status should describe the failure cause.
	 */
	public void execute(IContextStep step, IFullQualifiedId id, IContext context, IPropertiesContainer data, IProgressMonitor monitor) throws CoreException;
}
