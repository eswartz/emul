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

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup;

/**
 * Step group extension manager implementation.
 */
public class StepGroupExtensionPointManager extends AbstractExtensionPointManager<IContextStepGroup> {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tm.te.runtime.stepper.stepGroups"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "stepGroup"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#doCreateExtensionProxy(org.eclipse.core.runtime.IConfigurationElement)
	 */
	@Override
	protected ExecutableExtensionProxy<IContextStepGroup> doCreateExtensionProxy(IConfigurationElement element) throws CoreException {
		return new StepGroupExtensionProxy(element);
	}

	/**
	 * Returns the step group identified by its unique id. If no step group with the specified id is
	 * registered, <code>null</code> is returned.
	 *
	 * @param id The step group unique id. Must not be <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the step group contribution.
	 *
	 * @return The step group instance or <code>null</code>.
	 */
	public IContextStepGroup getStepGroup(String id, boolean unique) {
		Assert.isNotNull(id);
		IContextStepGroup contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<IContextStepGroup> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#doStoreExtensionTo(java.util.Map, org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy, org.eclipse.core.runtime.IConfigurationElement)
	 */
	@Override
	protected void doStoreExtensionTo(Map<String, ExecutableExtensionProxy<IContextStepGroup>> extensions, ExecutableExtensionProxy<IContextStepGroup> candidate, IConfigurationElement element) throws CoreException {
		Assert.isNotNull(extensions);
		Assert.isNotNull(candidate);
		Assert.isNotNull(element);

		// If no extension with this id had been registered before, register now.
		if (!extensions.containsKey(candidate.getId())) {
			extensions.put(candidate.getId(), candidate);
		}
		else if (extensions.get(candidate.getId()) instanceof StepGroupExtensionProxy) {
			StepGroupExtensionProxy proxy = (StepGroupExtensionProxy)extensions.get(candidate.getId());
			proxy.addGroupExtension(element);
		}
		else {
			super.doStoreExtensionTo(extensions, candidate, element);
		}
	}

}
