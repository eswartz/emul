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

/**
 * The service to refresh the parent locator model from remote.
 */
public interface ILocatorModelRefreshService extends ILocatorModelService {

	/**
	 * Refreshes the list of known peers from the local locator service
	 * and update the locator model.
	 */
	public void refresh();

}
