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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tm.te.runtime.persistence.AbstractPersistenceDelegate;

/**
 * Static peers persistence delegate implementation.
 */
public class PeersPersistenceDelegate extends AbstractPersistenceDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceDelegate#write(java.net.URI, java.util.Map)
	 */
	@Override
	public void write(URI uri, Map<String, Object> data) throws IOException {
		Assert.isNotNull(uri);
		Assert.isNotNull(data);

		// Only "file:" URIs are supported
		if (!"file".equalsIgnoreCase(uri.getScheme())) { //$NON-NLS-1$
			throw new IOException("Unsupported URI schema '" + uri.getScheme() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Create the file object from the given URI
		File file = new File(uri.normalize());

		// The file must be absolute
		if (!file.isAbsolute()) {
			throw new IOException("URI must denote an absolute file path."); //$NON-NLS-1$
		}

		// Check if the file extension is "ini" (otherwise it is not picked up)
		IPath path = new Path(file.getCanonicalPath());
		if (!"ini".equals(path.getFileExtension())) { //$NON-NLS-1$
			path = path.addFileExtension("ini"); //$NON-NLS-1$
		}

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path.toFile()), "UTF-8")); //$NON-NLS-1$
			for (String attribute : data.keySet()) {
				writer.write(attribute);
				writer.write('=');
				writer.write(data.get(attribute).toString());
				writer.newLine();
			}
		} finally {
			writer.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceDelegate#delete(java.net.URI)
	 */
	@Override
	public boolean delete(URI uri) throws IOException {
		Assert.isNotNull(uri);

		// Only "file:" URIs are supported
		if (!"file".equalsIgnoreCase(uri.getScheme())) { //$NON-NLS-1$
			throw new IOException("Unsupported URI schema '" + uri.getScheme() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Create the file object from the given URI
		File file = new File(uri.normalize());

		// The file must be absolute
		if (!file.isAbsolute()) {
			throw new IOException("URI must denote an absolute file path."); //$NON-NLS-1$
		}

		// Check if the file extension is "ini" (otherwise it is not picked up)
		IPath path = new Path(file.getCanonicalPath());
		if (!"ini".equals(path.getFileExtension())) { //$NON-NLS-1$
			path = path.addFileExtension("ini"); //$NON-NLS-1$
		}

		return path.toFile().delete();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceDelegate#read(java.net.URI)
	 */
	@Override
	public Map<String, Object> read(URI uri) throws IOException {
		Assert.isNotNull(uri);

		// Only "file:" URIs are supported
		if (!"file".equalsIgnoreCase(uri.getScheme())) { //$NON-NLS-1$
			throw new IOException("Unsupported URI schema '" + uri.getScheme() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Create the file object from the given URI
		File file = new File(uri.normalize());

		// The file must be absolute
		if (!file.isAbsolute()) {
			throw new IOException("URI must denote an absolute file path."); //$NON-NLS-1$
		}

		return null;
	}
}
