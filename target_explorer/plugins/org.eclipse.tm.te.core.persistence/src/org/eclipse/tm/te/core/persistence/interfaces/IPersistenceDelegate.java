/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.persistence.interfaces;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.tm.te.core.interfaces.IExecutableExtension;
import org.eclipse.tm.te.core.interfaces.nodes.IPropertiesContainer;

/**
 * Target Explorer: Persistence delegate API declaration
 */
public interface IPersistenceDelegate extends IExecutableExtension {

	/**
	 * Writes the given data to the persistence storage given by
	 * the <code>&quot;path&quot;</code> parameter.
	 * <p>
	 * If the persistence delegate implements a file less storage,
	 * the <code>&quot;path&quot;</code> parameter might be ignored or
	 * set to <code>null</code>.
	 * <p>
	 * If the persistence delegate implements a file based storage,
	 * the persistence delegate contributor defines the exact semantic
	 * of the <code>&quot;path&quot;</code> parameter. If and how
	 * relative path information are processed may differ between
	 * the delegate implementations.
	 *
	 * @param path The persistence storage path or <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 *
	 * @throws IOException - if the operation fails
	 */
	public void write(IPath path, IPropertiesContainer data) throws IOException;

	/**
	 * Reads the data from the persistence storage given by
	 * the <code>&quot;path&quot;</code> parameter.
	 * <p>
	 * If the persistence delegate implements a file less storage,
	 * the <code>&quot;path&quot;</code> parameter might be ignored or
	 * set to <code>null</code>.
	 * <p>
	 * If the persistence delegate implements a file based storage,
	 * the persistence delegate contributor defines the exact semantic
	 * of the <code>&quot;path&quot;</code> parameter. If and how
	 * relative path information are processed may differ between
	 * the delegate implementations.
	 *
	 * @param path The persistence storage path or <code>null</code>.
	 * @return The data.
	 *
	 * @throws IOException - if the operation fails
	 */
	public IPropertiesContainer read(IPath path) throws IOException;

	/**
	 * Deletes the persistence storage given by the <code>&quot;path&quot;</code>
	 * parameter.
	 * <p>
	 * If the persistence delegate implements a file less storage,
	 * the <code>&quot;path&quot;</code> parameter might be ignored or
	 * set to <code>null</code>.
	 * <p>
	 * If the persistence delegate implements a file based storage,
	 * the persistence delegate contributor defines the exact semantic
	 * of the <code>&quot;path&quot;</code> parameter. If and how
	 * relative path information are processed may differ between
	 * the delegate implementations.
	 *
	 * @param path The persistence storage path or <code>null</code>.
	 * @return <code>True</code> if the persistence storage is successfully deleted; <code>false</code> otherwise.
	 *
	 * @throws IOException - if the operation fails
	 */
	public boolean delete(IPath path) throws IOException;
}
