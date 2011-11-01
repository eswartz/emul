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
import java.net.URI;
import java.util.Map;

import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;

/**
 * Interface to be implemented by persistence delegates.
 */
public interface IPersistenceDelegate extends IExecutableExtension {

	/**
	 * Writes the given data to the given persistence storage.
	 * <p>
	 * The persistence storage location is defined by the specified URI reference. The exact
	 * interpretation and semantic of the URI reference is up to the persistence delegate
	 * contributor.
	 *
	 * @param uri The persistence storage location URI reference. Must not be <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 *
	 * @throws IOException - if the operation fails
	 */
	public void write(URI uri, Map<String, Object> data) throws IOException;

	/**
	 * Reads the data from the given persistence storage.
	 * <p>
	 * The persistence storage location is defined by the specified URI reference. The exact
	 * interpretation and semantic of the URI reference is up to the persistence delegate
	 * contributor.
	 *
	 * @param uri The persistence storage location URI reference. Must not be <code>null</code>.
	 * @return The data.
	 *
	 * @throws IOException - if the operation fails
	 */
	public Map<String, Object> read(URI uri) throws IOException;

	/**
	 * Deletes the given persistence storage.
	 * <p>
	 * The persistence storage location is defined by the specified URI reference. The exact
	 * interpretation and semantic of the URI reference is up to the persistence delegate
	 * contributor.
	 *
	 * @param uri The persistence storage location URI reference. Must not be <code>null</code>.
	 * @return <code>True</code> if the persistence storage is successfully deleted;
	 *         <code>false</code> otherwise.
	 *
	 * @throws IOException - if the operation fails
	 */
	public boolean delete(URI uri) throws IOException;
}
