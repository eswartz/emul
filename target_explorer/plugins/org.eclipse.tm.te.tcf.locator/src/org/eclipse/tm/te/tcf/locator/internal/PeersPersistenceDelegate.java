/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.internal;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.persistence.AbstractPersistenceDelegate;
import org.eclipse.tm.te.tcf.locator.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.locator.internal.nls.Messages;

/**
 * Static peers persistence delegate implementation.
 */
public class PeersPersistenceDelegate extends AbstractPersistenceDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceDelegate#write(org.eclipse.core.runtime.IPath, org.eclipse.tm.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
	public void write(IPath path, IPropertiesContainer data) throws IOException {
		Assert.isNotNull(path);
		Assert.isNotNull(data);

		// If the given path is relative, append it to the root path.
		if (!path.isAbsolute()) {
			IPath root = getRoot();
			if (root == null) throw new IOException(Messages.PeersPersistenceDelegate_error_noRootLocation);
			path = root.append(path);
		}

		// Check if the file extension is "ini" (otherwise it is not picked up)
		if (!"ini".equals(path.getFileExtension())) { //$NON-NLS-1$
			path = path.addFileExtension("ini"); //$NON-NLS-1$
		}

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path.toFile()), "UTF-8")); //$NON-NLS-1$
			for (String attribute : data.getProperties().keySet()) {
				writer.write(attribute);
				writer.write('=');
				writer.write(data.getStringProperty(attribute));
				writer.newLine();
			}
		} finally {
			writer.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceDelegate#delete(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public boolean delete(IPath path) throws IOException {
		Assert.isNotNull(path);

		// If the given path is relative, append it to the root path.
		if (!path.isAbsolute()) {
			IPath root = getRoot();
			if (root == null) throw new IOException(Messages.PeersPersistenceDelegate_error_noRootLocation);
			path = root.append(path);
		}

		// Check if the file extension is "ini" (otherwise it is not picked up)
		if (!"ini".equals(path.getFileExtension())) { //$NON-NLS-1$
			path = path.addFileExtension("ini"); //$NON-NLS-1$
		}

		return path.toFile().delete();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceDelegate#read(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IPropertiesContainer read(IPath path) throws IOException {
		return null;
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
}
