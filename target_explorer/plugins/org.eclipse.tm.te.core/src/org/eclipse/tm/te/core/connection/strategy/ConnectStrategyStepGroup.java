/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.connection.strategy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.runtime.stepper.extensions.AbstractContextStepGroup;
import org.eclipse.tm.te.runtime.stepper.extensions.ContextStepGroupable;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStep;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroupable;

/**
 * Connect strategy step group implementation.
 */
public class ConnectStrategyStepGroup extends AbstractContextStepGroup {

	private final List<ExecutableExtensionProxy<IContextStep>> steps = new ArrayList<ExecutableExtensionProxy<IContextStep>>();


	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.extensions.AbstractContextStepGroup#doSetInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void doSetInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	    super.doSetInitializationData(config, propertyName, data);

		for (IConfigurationElement stepElement : config.getChildren("step")) { //$NON-NLS-1$
			ExecutableExtensionProxy<IContextStep> step = new ExecutableExtensionProxy<IContextStep>(stepElement);
			steps.add(step);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup#getSteps(java.lang.String, java.lang.String)
	 */
	@Override
    public IContextStepGroupable[] getSteps(String type, String subType) throws CoreException {
		List<IContextStepGroupable> steps = new ArrayList<IContextStepGroupable>();
		for (ExecutableExtensionProxy<IContextStep> stepProxy : this.steps) {
			IContextStep step = stepProxy.newInstance();
			if (step != null) {
				IContextStepGroupable groupable = new ContextStepGroupable(step);
				steps.add(groupable);
			}
		}
		return !steps.isEmpty() ? steps.toArray(new IContextStepGroupable[steps.size()]) : NO_STEPS;
	}
}
