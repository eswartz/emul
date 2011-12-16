/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.internal.adapters;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable;
import org.eclipse.tm.te.tcf.locator.activator.CoreBundleActivator;

/**
 * Persistable implementation handling peer attributes.
 */
public class MapPersistableAdapter implements IPersistable {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable#getStorageID()
	 */
	@Override
	public String getStorageID() {
		return "org.eclipse.tm.te.tcf.locator.persistence"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable#getURI(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
    @Override
	public URI getURI(final Object data) {
		Assert.isNotNull(data);

		URI uri = null;

		// Only map objects are supported
		if (data instanceof Map) {
			// Get the name of the peer and make it a valid
			// file system name (no spaces etc).
			String name = ((Map<String, String>) data).get(IPeer.ATTR_NAME);
			if (name == null) name = ((Map<String, String>) data).get(IPeer.ATTR_ID);
			name = makeValidFileSystemName(name);
			// Get the URI from the name
			uri = getRoot().append(name).toFile().toURI();
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

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable#exportFrom(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
    @Override
	public Map<String, Object> exportFrom(Object data) throws IOException {
		Assert.isNotNull(data);

		Map<String, Object> result = null;

		// Only map objects are supported
		if (data instanceof Map) {
			// Convert into a String/Object map to pass it on to the persistence delegates
			result = new LinkedHashMap<String, Object>();
			for (String key : ((Map<String, String>) data).keySet()) result.put(key, ((Map<String, String>) data).get(key));
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

		// Only map objects are supported
		if (data instanceof Map) {
			@SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>)data;
			for (String key : external.keySet()) map.put(key, (String)external.get(key));
		}
	}

}
