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

/**
 * Interface to be implemented by persistable elements.
 */
public interface IPersistable {

	/**
	 * Returns the id of the persistence storage to use for persisting data objects.
	 * <p>
	 * The method is expected to return never <code>null</code>.
	 *
	 * @return The persistence storage id.
	 */
	public String getStorageID();

	/**
	 * Returns the URI reference to pass on to the associated persistence delegate to
	 * denote the given data object.
	 * <p>
	 * The interpretation of the URI reference is up to the persistence delegate, but
	 * the method is expected to return never <code>null</code>.
	 *
	 * @param data The data object. Must not be <code>null</code>.
	 *
	 * @return The URI.
	 */
	public URI getURI(Object data);

	/**
	 * Exports the given data object to an external representation.
	 * <p>
	 * As a general guide line, it is expected that the external representation contains only base
	 * Java objects like maps, lists and Strings. Details about the valid object types can be taken
	 * from the referenced persistence delegate.
	 *
	 * @param data The data object. Must not be <code>null</code>.
	 * @return The external representation of the given data object.
	 *
	 * @throws IOException - if the operation fails.
	 */
	public Map<String, Object> exportFrom(Object data) throws IOException;

	/**
	 * Imports the given external representation into the given data object.
	 *
	 * @param data The data object. Must not be <code>null</code>.
	 * @param external The external representation. Must not be <code>null</code>.
	 *
	 * @throws IOException - if the operation fails.
	 */
	public void importTo(Object data, Map<String, Object> external) throws IOException;
}
