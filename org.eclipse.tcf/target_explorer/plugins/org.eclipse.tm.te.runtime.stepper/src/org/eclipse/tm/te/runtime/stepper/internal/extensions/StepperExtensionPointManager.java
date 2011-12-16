/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.stepper.internal.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.runtime.stepper.interfaces.IStepper;

/**
 * Stepper extension point manager implementation.
 */
public class StepperExtensionPointManager extends AbstractExtensionPointManager<IStepper> {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tm.te.runtime.stepper.steppers"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "stepper"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed stepper.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed stepper.
	 *
	 * @return The list of contributed stepper, or an empty array.
	 */
	public IStepper[] getStepper(boolean unique) {
		List<IStepper> contributions = new ArrayList<IStepper>();
		Collection<ExecutableExtensionProxy<IStepper>> delegates = getExtensions().values();
		for (ExecutableExtensionProxy<IStepper> delegate : delegates) {
			IStepper instance = unique ? delegate.newInstance() : delegate.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new IStepper[contributions.size()]);
	}

	/**
	 * Returns the stepper identified by its unique id. If no stepper with the specified id is registered,
	 * <code>null</code> is returned.
	 *
	 * @param id The unique id of the stepper or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the stepper contribution.
	 *
	 * @return The stepper instance or <code>null</code>.
	 */
	public IStepper getStepper(String id, boolean unique) {
		Assert.isNotNull(id);
		IStepper contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<IStepper> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}
}
