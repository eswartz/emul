/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.connection.interfaces;

import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * A connection type.
 * <p>
 * Each node on each level of the Target Explorer tree can be bound to a connection type. Child
 * nodes does inherit the connection type of the parent nodes automatically if not bound to another
 * connection type explicitly.
 * <p>
 * A node can be bound to a single connection type only. The first connection type with an
 * enablement matching a node is chosen if multiple connection types would match.
 * <p>
 * Connection types not bound to any enablement are considered invalid. Enablements are
 * contributed through the connection type bindings extension point. Multiple enablements
 * from different plug-ins are allowed. The ordering of multiple enablements is undefined and
 * given by the order the extension points are provided to the extension point manager by
 * the Eclipse platform.
 * <p>
 * Connection types can bind connection type specific services through the connection type
 * bindings extension point.
 */
public interface IConnectionType extends IPropertiesContainer, IExecutableExtension {

	/**
	 * Returns if or if not this connection type has been explicitly disabled by the
	 * connection type contributor.
	 * <p>
	 * Even if this method returns <code>true</code>, connection types might be still
	 * disabled by other conditions like disabled capabilities.
	 *
	 * @return <code>True</code> if the connection type is enabled, <code>false</code> otherwise.
	 */
	public boolean isEnabled();

	/**
	 * Returns if or if not this connection types is valid.
	 * <p>
	 * A connection type is considered valid if all prerequisites like installed plug-ins or
	 * products, runtime platforms or licensing conditions are fulfilled.
	 *
	 * @return <code>True</code> if the connection type is valid, <code>false</code> otherwise.
	 */
	public boolean isValid();
}
