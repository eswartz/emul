/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.stepper.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroupIterator;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroupable;

/**
 * Abstract context step group implementation.
 */
public abstract class AbstractContextStepGroup extends ExecutableExtension implements IContextStepGroup {

	private ExecutableExtensionProxy<IContextStepGroupIterator> iteratorProxy = null;

	/**
	 * Constant to be returned in case the step group contains no steps.
	 */
	protected final static IContextStepGroupable[] NO_STEPS = new IContextStepGroupable[0];

	/**
	 * Constructor.
	 */
	public AbstractContextStepGroup() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup#isLocked()
	 */
	@Override
    public boolean isLocked() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.ExecutableExtension#doSetInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void doSetInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	    super.doSetInitializationData(config, propertyName, data);

		if (iteratorProxy == null) {
			iteratorProxy = new ExecutableExtensionProxy<IContextStepGroupIterator>(config) {
				@Override
				protected String getExecutableExtensionAttributeName() {
					return "iterator"; //$NON-NLS-1$
				}
			};
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup#getStepGroupIterator()
	 */
	@Override
    public IContextStepGroupIterator getStepGroupIterator() {
		return iteratorProxy != null ? iteratorProxy.newInstance() : null;
	}
}
