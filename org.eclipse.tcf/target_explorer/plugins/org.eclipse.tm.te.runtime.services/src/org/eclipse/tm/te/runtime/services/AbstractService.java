/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.services;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.te.runtime.services.interfaces.IService;

/**
 * Abstract service implementation.
 */
public abstract class AbstractService extends PlatformObject implements IService {

	private String id = null;

	/**
	 * Constructor.
	 */
	public AbstractService() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.services.IService#setId(java.lang.String)
	 */
	@Override
    public final void setId(String id) {
		if (id == null) this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.services.IService#getId()
	 */
	@Override
    public final String getId() {
		return id;
	}

}
