/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.interfaces.nodes;

import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * The peer model is an extension to the TCF target/host representation, implementing the
 * {@link IPeer} interface. The peer model provides an offline cache for a peers known list of local
 * and remote services and is the merge point of peer attributes from custom data storages.
 * <p>
 * <b>Note:</b> The {@link #getProperty(String)} method provides access both the native peer
 * attributes and to the custom attributes. Alternatively, the native peer attributes can be access
 * via <i><code>getPeer().getAttributes()</code></i>.
 * <p>
 * <b>Note:</b> Read and write access to the peer model must happen within the TCF dispatch thread.
 */
public interface IPeerModel extends IPropertiesContainer {

	/**
	 * Returns the parent locator model instance.
	 *
	 * @return The parent locator model instance.
	 */
	public ILocatorModel getModel();

	/**
	 * Returns the native {@link IPeer} object.
	 *
	 * @return The native {@link IPeer} instance.
	 */
	public IPeer getPeer();
}
