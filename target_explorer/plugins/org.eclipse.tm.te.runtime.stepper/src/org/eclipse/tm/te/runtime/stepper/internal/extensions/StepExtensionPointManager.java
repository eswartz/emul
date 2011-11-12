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
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStep;

/**
 * Step extension point manager implementation.
 */
public class StepExtensionPointManager extends AbstractExtensionPointManager<IContextStep> {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tm.te.runtime.stepper.steps"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "step"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed steps.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed step.
	 *
	 * @return The list of contributed steps, or an empty array.
	 */
	public IContextStep[] getSteps(boolean unique) {
		List<IContextStep> contributions = new ArrayList<IContextStep>();
		Collection<ExecutableExtensionProxy<IContextStep>> delegates = getExtensions().values();
		for (ExecutableExtensionProxy<IContextStep> delegate : delegates) {
			IContextStep instance = unique ? delegate.newInstance() : delegate.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new IContextStep[contributions.size()]);
	}

	/**
	 * Returns the step identified by its unique id. If no step with the specified id is registered,
	 * <code>null</code> is returned.
	 *
	 * @param id The unique id of the step or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the step contribution.
	 *
	 * @return The step instance or <code>null</code>.
	 */
	public IContextStep getStep(String id, boolean unique) {
		Assert.isNotNull(id);
		IContextStep contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<IContextStep> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}
}
