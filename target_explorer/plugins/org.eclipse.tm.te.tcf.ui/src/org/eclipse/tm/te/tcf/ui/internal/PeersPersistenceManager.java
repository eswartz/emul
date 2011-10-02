/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Path;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.core.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.core.persistence.PersistenceDelegateManager;
import org.eclipse.tm.te.core.persistence.interfaces.IPersistenceDelegate;
import org.eclipse.tm.te.core.properties.PropertiesContainer;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;


/**
 * Target Explorer: New target persistence manager implementation.
 */
public class PeersPersistenceManager {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static PeersPersistenceManager instance = new PeersPersistenceManager();
	}

	/**
	 * Constructor.
	 */
	PeersPersistenceManager() {
		super();
	}

	/**
	 * Returns the singleton instance.
	 */
	public static PeersPersistenceManager getInstance() {
		return LazyInstance.instance;
	}

	/**
	 * Writes the given peer attributes to the persistence storage.
	 *
	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
	 * @throws IOException - if the operation fails.
	 */
	public void write(Map<String, String> peerAttributes) throws IOException {
		Assert.isNotNull(peerAttributes);

		// Get the name of the peer and make it a valid
		// file system name (no spaces etc).
		String name = peerAttributes.get(IPeer.ATTR_NAME);
		if (name == null) name = peerAttributes.get(IPeer.ATTR_ID);
		name = makeValidFileSystemName(name);

		// Convert into a IPropertiesContainer to pass it on to the persistence delegates
		IPropertiesContainer data = new PropertiesContainer();
		for (String key : peerAttributes.keySet()) data.setProperty(key, peerAttributes.get(key));

		IPersistenceDelegate delegate = PersistenceDelegateManager.getInstance().getDelegate("org.eclipse.tm.te.tcf.locator.persistence", false); //$NON-NLS-1$
		Assert.isNotNull(delegate);
		delegate.write(new Path(name), data);
	}

	/**
	 * Deletes the given peer from the persistence storage.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @throws IOException - if the operation fails.
	 */
	public void delete(final IPeerModel peer) throws IOException {
		Assert.isNotNull(peer);

		// Get the file path the peer model has been created from
		final String[] path = new String[1];
		if (Protocol.isDispatchThread()) {
			path[0] = peer.getPeer().getAttributes().get("Path"); //$NON-NLS-1$
		} else {
			Protocol.invokeAndWait(new Runnable() {
				public void run() {
					path[0] = peer.getPeer().getAttributes().get("Path"); //$NON-NLS-1$
				}
			});
		}

		if (path[0] != null && !"".equals(path[0].trim())) { //$NON-NLS-1$
			IPersistenceDelegate delegate = PersistenceDelegateManager.getInstance().getDelegate("org.eclipse.tm.te.tcf.locator.persistence", false); //$NON-NLS-1$
			Assert.isNotNull(delegate);
			delegate.delete(new Path(path[0].trim()));
		}
	}

	/**
	 * Make a valid file system name from the given name.
	 *
	 * @param name The original name. Must not be <code>null</code>.
	 * @return The valid file system name.
	 */
	private String makeValidFileSystemName(String name) {
		Assert.isNotNull(name);
		return name.replaceAll("\\W", "_"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
