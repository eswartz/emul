/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.persistence.interfaces;

import java.io.IOException;

import org.eclipse.tm.te.runtime.services.interfaces.IService;

/**
 * A service for persisting elements to a persistence store.
 */
public interface IPersistenceService extends IService {

	/**
	 * Writes the given data object via a persistence delegate to a persistence storage. The
	 * persistence delegate to use will be determined by adapting the given data object to an
	 * {@link IPersistable}.
	 *
	 * @param data The data object. Must not be <code>null</code>.
	 *
	 * @throws IOException - if the operation fails.
	 */
	public void write(Object data) throws IOException;

	/**
	 * Fills the given data object with the data read via a persistence delegate from a given
	 * persistence storage. The persistence delegate to use will be determined by adapting the given
	 * data object to an {@link IPersistable}.
	 *
	 * @param data The data object. Must not be <code>null</code>.
	 *
	 * @throws IOException - if the operation fails
	 */
	public void read(Object data) throws IOException;

	/**
	 * Deletes the persistence storage for the given data object via a persistence delegate. The
	 * persistence delegate to use will be determined by adapting the given data object to an
	 * {@link IPersistable}.
	 *
	 * @param data The data object. Must not be <code>null</code>.
	 *
	 * @return <code>True</code> if the persistence storage is successfully deleted;
	 *         <code>false</code> otherwise.
	 *
	 * @throws IOException - if the operation fails
	 */
	public boolean delete(Object data) throws IOException;

}
