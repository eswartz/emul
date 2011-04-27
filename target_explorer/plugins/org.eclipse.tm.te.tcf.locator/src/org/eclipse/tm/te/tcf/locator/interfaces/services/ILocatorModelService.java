/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.interfaces.services;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;


/**
 * Common parent interface for locator model services.
 */
public interface ILocatorModelService extends IAdaptable {

	/**
	 * Returns the parent locator model.
	 *
	 * @return The parent locator model.
	 */
	public ILocatorModel getLocatorModel();
}
