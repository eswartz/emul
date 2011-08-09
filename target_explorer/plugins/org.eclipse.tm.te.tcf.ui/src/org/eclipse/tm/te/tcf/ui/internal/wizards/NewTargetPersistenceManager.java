/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.wizards;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.te.tcf.locator.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.ui.internal.nls.Messages;


/**
 * Target Explorer: New target persistence manager implementation.
 */
@SuppressWarnings("restriction")
public class NewTargetPersistenceManager {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static NewTargetPersistenceManager instance = new NewTargetPersistenceManager();
	}

	/**
	 * Constructor.
	 */
	NewTargetPersistenceManager() {
		super();
	}

	/**
	 * Returns the singleton instance.
	 */
	public static NewTargetPersistenceManager getInstance() {
		return LazyInstance.instance;
	}

	/**
	 * Returns the root location of the peers storage.
	 *
	 * @return The root location or <code>null</code> if it cannot be determined.
	 */
	public IPath getRoot() {
		IPath location = null;

		// Try the bundles state location first (not available if launched with -data @none).
		try {
			IPath path = CoreBundleActivator.getDefault().getStateLocation().append(".peers"); //$NON-NLS-1$
			if (!path.toFile().exists()) path.toFile().mkdirs();
			if (path.toFile().canRead() && path.toFile().isDirectory()) {
				location = path;
			}
		} catch (IllegalStateException e) {
			// Workspace less environments (-data @none)
			// The users local peers lookup directory is $HOME/.tcf/.peers.
			IPath path = new Path(System.getProperty("user.home")).append(".tcf/.peers"); //$NON-NLS-1$ //$NON-NLS-2$
			if (!path.toFile().exists()) path.toFile().mkdirs();
			if (path.toFile().canRead() && path.toFile().isDirectory()) {
				location = path;
			}
		}

		return location;
	}

	/**
	 * Writes the given peer attributes to the persistence storage.
	 *
	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
	 * @throws IOException - if the operation fails.
	 */
	public void write(Map<String, String> peerAttributes) throws IOException {
		Assert.isNotNull(peerAttributes);

		// Get the root location
		IPath root = getRoot();
		if (root == null) throw new IOException(Messages.NewTargetPersistenceManager_error_noRootLocation);

		// Get the name of the peer and make it a valid
		// file system name (no spaces etc).
		String name = peerAttributes.get(IPeer.ATTR_NAME);
		if (name == null) name = peerAttributes.get(IPeer.ATTR_ID);
		name = makeValidFileSystemName(name);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(root.append(name).addFileExtension("ini").toFile()), "UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
		for (String attribute : peerAttributes.keySet()) {
			writer.write(attribute);
			writer.write('=');
			writer.write(peerAttributes.get(attribute));
			writer.newLine();
		}
		writer.close();
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
