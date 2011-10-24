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
import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;


/**
 * A step group.
 * <p>
 * A step group is a set of single steps or other step groups.
 */
public interface IContextStepGroup<Data extends Object> extends IExecutableExtension {

	/**
	 * Returns if or if not the step group is locked for user modifications.
	 *
	 * @return <code>True</code> if locked for user modifications, <code>false</code> otherwise.
	 */
	public boolean isLocked();

	/**
	 * Returns the list of steps or step groups enlisted in the group for the specified stepper,
	 * type and sub type.
	 *
	 * @param type The type id. Must be not <code>null</code>.
	 * @param subType The sub type Must be not <code>null</code>.
	 *
	 * @return The list of steps and step groups or an empty array.
	 *
	 * @throws CoreException If the steps cannot be determined.
	 */
	public IContextStepGroupable<Data>[] getSteps(String type, String subType) throws CoreException;

	/**
	 * Return the step group iterator or <code>null</code>. The step group iterator can be used to
	 * generate loops and conditions for a step group.
	 */
	public IContextStepGroupIterator<Data> getStepGroupIterator();
}
