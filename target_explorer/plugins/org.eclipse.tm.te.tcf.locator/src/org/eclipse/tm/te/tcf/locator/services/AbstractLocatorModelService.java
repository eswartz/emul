/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.services;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelService;


/**
 * Abstract locator model service base implementation.
 */
public abstract class AbstractLocatorModelService extends PlatformObject implements ILocatorModelService {
	// Reference to the parent locator model
	private final ILocatorModel fLocatorModel;

	/**
	 * Constructor.
	 *
	 * @param parentModel The parent locator model instance. Must not be <code>null</code>.
	 */
	public AbstractLocatorModelService(ILocatorModel parentModel) {
		assert parentModel != null;
		fLocatorModel = parentModel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelService#getLocatorModel()
	 */
	public final ILocatorModel getLocatorModel() {
		return fLocatorModel;
	}
}
