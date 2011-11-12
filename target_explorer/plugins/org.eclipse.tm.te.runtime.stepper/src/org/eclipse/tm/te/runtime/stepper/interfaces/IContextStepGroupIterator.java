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
import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * A step group iterator.
 */
public interface IContextStepGroupIterator extends IExecutableExtension {

	/**
	 * Initialize the iterator.
	 *
	 * @param context The context. Must be not <code>null</code>.
	 * @param data The data. Must be not <code>null</code>.
	 * @param fullQualifiedId The full qualified id for this step. Must be not <code>null</code>.
	 * @param monitor The progress monitor. Must be not <code>null</code>.
	 */
	public void initialize(IContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor);

	/**
	 * Return the number of calculated iterations. If the iterator was not initialized,
	 * <code>-1</code> is returned.
	 */
	public int getNumIterations();

	/**
	 * Check if there is a next iteration possible.
	 *
	 * @param context The context. Must be not <code>null</code>.
	 * @param data The data. Must be not <code>null</code>.
	 * @param fullQualifiedId The full qualified id for this step. Must be not <code>null</code>.
	 * @param monitor The progress monitor. Must be not <code>null</code>.
	 * @return <code>true</code> if another iteration is possible.
	 */
	public boolean hasNext(IContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor);

	/**
	 * Set the next iteration to the data using the full qualified id.
	 *
	 * @param context The context. Must be not <code>null</code>.
	 * @param data The data. Must be not <code>null</code>.
	 * @param fullQualifiedId The full qualified id for this step. Must be not <code>null</code>.
	 * @param monitor The progress monitor. Must be not <code>null</code>.
	 * @throws CoreException
	 */
	public void next(IContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws CoreException;
}
