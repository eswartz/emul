/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.adapters;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.te.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable;

/**
 * Model node persistable adapter implementation.
 */
public class ModelNodePersistableAdapter implements IPersistable {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable#getStorageID()
	 */
	@Override
	public String getStorageID() {
		return "org.eclipse.tm.te.runtime.persistence.properties"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable#getURI(java.lang.Object)
	 */
	@Override
	public URI getURI(Object data) {
		Assert.isNotNull(data);

		URI uri = null;

		// Only model nodes are supported
		if (data instanceof IModelNode) {
			IModelNode node = (IModelNode) data;
			if (node.getName() != null && !"".equals(node.getName().trim())) { //$NON-NLS-1$
				// Get the node name and make it a valid file system name (no spaces etc).
				IPath path = getRoot().append(makeValidFileSystemName(((IModelNode) data).getName().trim()));
				if (!"ini".equals(path.getFileExtension())) path = path.addFileExtension("ini"); //$NON-NLS-1$ //$NON-NLS-2$
				uri = path.toFile().toURI();
			}
			// If the name is not set, check for "Path"
			else if (node.getStringProperty("Path") != null && !"".equals(node.getStringProperty("Path").trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				IPath path = new Path(node.getStringProperty("Path")); //$NON-NLS-1$
				uri = path.toFile().toURI();
			}
		}

		return uri;
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

	/**
	 * Returns the root location.
	 *
	 * @return The root location or <code>null</code> if it cannot be determined.
	 */
	public IPath getRoot() {
		IPath location = null;

		// Try the bundles state location first (not available if launched with -data @none).
		try {
			IPath path = Platform.getStateLocation(CoreBundleActivator.getContext().getBundle()).append(".store"); //$NON-NLS-1$
			if (!path.toFile().exists()) path.toFile().mkdirs();
			if (path.toFile().canRead() && path.toFile().isDirectory()) {
				location = path;
			}
		} catch (IllegalStateException e) {
			// Workspace less environments (-data @none)
			// The users local target definition persistence directory is $HOME/.tcf/.store.
			IPath path = new Path(System.getProperty("user.home")).append(".tcf/.store"); //$NON-NLS-1$ //$NON-NLS-2$
			if (!path.toFile().exists()) path.toFile().mkdirs();
			if (path.toFile().canRead() && path.toFile().isDirectory()) {
				location = path;
			}
		}

		return location;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable#exportFrom(java.lang.Object)
	 */
	@Override
	public Map<String, Object> exportFrom(Object data) throws IOException {
		Assert.isNotNull(data);

		Map<String, Object> result = null;

		// Only model nodes are supported
		if (data instanceof IModelNode) {
			result = ((IModelNode)data).getProperties();
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable#importTo(java.lang.Object, java.util.Map)
	 */
	@Override
	public void importTo(Object data, Map<String, Object> external) throws IOException {
		Assert.isNotNull(data);
		Assert.isNotNull(external);

		// Only model nodes are supported
		if (data instanceof IModelNode) {
			IModelNode node = (IModelNode) data;
			for (String key : external.keySet()) {
				node.setProperty(key, external.get(key));
			}
		}
	}

}
