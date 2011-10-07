/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.services.interfaces;

/**
 * Common service.
 */
public interface IService {

	/**
	 * Sets the id this service is registered to.
	 * <p>
	 * <b>Note:</b> Once set to a non-null value, the service id cannot be changed anymore.
	 *
	 * @param id The id or <code>null</code>.
	 */
	public void setId(String id);

	/**
	 * Returns the id this service is registered to.
	 *
	 * @return The id or <code>null</code> if the service id is not yet set.
	 */
	public String getId();
}
