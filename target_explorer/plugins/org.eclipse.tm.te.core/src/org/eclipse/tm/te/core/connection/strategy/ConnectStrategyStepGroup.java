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
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.stepper.AbstractContextStepGroup;
import org.eclipse.tm.te.runtime.stepper.ContextStepGroupable;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStep;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroupable;

/**
 * Connect strategy step group implementation.
 */
public class ConnectStrategyStepGroup extends AbstractContextStepGroup<IPropertiesContainer> {

	private final List<ExecutableExtensionProxy<IContextStep<IPropertiesContainer>>> steps = new ArrayList<ExecutableExtensionProxy<IContextStep<IPropertiesContainer>>>();

	/**
	 * Constructor.
	 */
	public ConnectStrategyStepGroup() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.AbstractContextStepGroup#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		super.setInitializationData(config, propertyName, data);

		for (IConfigurationElement stepElement : config.getChildren("step")) { //$NON-NLS-1$
			ExecutableExtensionProxy<IContextStep<IPropertiesContainer>> step = new ExecutableExtensionProxy<IContextStep<IPropertiesContainer>>(stepElement);
			steps.add(step);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup#getSteps(java.lang.String, java.lang.String)
	 */
	@Override
    public IContextStepGroupable<IPropertiesContainer>[] getSteps(String type, String subType) throws CoreException {
		List<IContextStepGroupable<IPropertiesContainer>> steps = new ArrayList<IContextStepGroupable<IPropertiesContainer>>();
		for (ExecutableExtensionProxy<IContextStep<IPropertiesContainer>> stepProxy : this.steps) {
			IContextStep<IPropertiesContainer> step = stepProxy.newInstance();
			if (step != null) {
				IContextStepGroupable<IPropertiesContainer> groupable = new ContextStepGroupable<IPropertiesContainer>(step);
				steps.add(groupable);
			}
		}
		return !steps.isEmpty() ? steps.toArray(new IContextStepGroupable[steps.size()]) : NO_STEPS;
	}
}
