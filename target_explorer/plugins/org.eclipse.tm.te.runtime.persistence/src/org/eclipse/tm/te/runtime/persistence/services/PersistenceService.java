/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.persistence.services;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.te.runtime.persistence.PersistenceDelegateManager;
import org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable;
import org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceDelegate;
import org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceService;
import org.eclipse.tm.te.runtime.services.AbstractService;

/**
 * Persistence service implementation.
 */
public class PersistenceService extends AbstractService implements IPersistenceService {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceService#write(java.lang.Object)
	 */
	@Override
	public void write(Object data) throws IOException {
		Assert.isNotNull(data);

		// Determine the persistable element for the given data object
		IPersistable persistable = data instanceof IPersistable ? (IPersistable)data : null;
		// If the element isn't a persistable by itself, try to adapt the element
		if (persistable == null) persistable = data instanceof IAdaptable ? (IPersistable) ((IAdaptable)data).getAdapter(IPersistable.class) : null;
		if (persistable == null) persistable = (IPersistable) Platform.getAdapterManager().getAdapter(data, IPersistable.class);

		// If the persistable could be still not determined, throw an IOException
		if (persistable == null) throw new IOException("'data' must be adaptable to IPersistable."); //$NON-NLS-1$

		// Determine the persistence delegate
		IPersistenceDelegate delegate = persistable.getStorageID() != null ? PersistenceDelegateManager.getInstance().getDelegate(persistable.getStorageID(), false) : null;
		// If the persistence delegate could not be determined, throw an IOException
		if (delegate == null) throw new IOException("The persistence delegate for ID '" + persistable.getStorageID() + "' cannot be determined."); //$NON-NLS-1$ //$NON-NLS-2$

		// Determine the URI
		URI uri = persistable.getURI(data);
		if (uri == null) throw new IOException("The URI cannot be determined."); //$NON-NLS-1$

		// Get the external representation of the data object from the
		// associated persistable.
		Map<String, Object> externalData = persistable.exportFrom(data);
		// If the external representation cannot be retrieved, throw an IOException
		if (externalData == null) throw new IOException("Failed to retrieve external data representation."); //$NON-NLS-1$

		// Pass on to the delegate for writing
		delegate.write(uri, externalData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceService#read(java.lang.Object)
	 */
	@Override
	public void read(Object data) throws IOException {
		Assert.isNotNull(data);

		// Determine the persistable element for the given data object
		IPersistable persistable = data instanceof IPersistable ? (IPersistable)data : null;
		// If the element isn't a persistable by itself, try to adapt the element
		if (persistable == null) persistable = data instanceof IAdaptable ? (IPersistable) ((IAdaptable)data).getAdapter(IPersistable.class) : null;
		if (persistable == null) persistable = (IPersistable) Platform.getAdapterManager().getAdapter(data, IPersistable.class);

		// If the persistable could be still not determined, throw an IOException
		if (persistable == null) throw new IOException("'data' must be adaptable to IPersistable."); //$NON-NLS-1$

		// Determine the persistence delegate
		IPersistenceDelegate delegate = persistable.getStorageID() != null ? PersistenceDelegateManager.getInstance().getDelegate(persistable.getStorageID(), false) : null;
		// If the persistence delegate could not be determined, throw an IOException
		if (delegate == null) throw new IOException("The persistence delegate for ID '" + persistable.getStorageID() + "' cannot be determined."); //$NON-NLS-1$ //$NON-NLS-2$

		// Determine the URI
		URI uri = persistable.getURI(data);
		if (uri == null) throw new IOException("The URI cannot be determined."); //$NON-NLS-1$

		// Pass on to the delegate for reading
		Map<String, Object> externalData = delegate.read(uri);
		if (externalData == null) throw new IOException("Failed to read external data representation from URI '" + uri.toString() + "'"); //$NON-NLS-1$ //$NON-NLS-2$

		// Import the external representation into the data object via
		// the associated persistable
		persistable.importTo(data, externalData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceService#delete(java.lang.Object)
	 */
	@Override
	public boolean delete(Object data) throws IOException {
		Assert.isNotNull(data);

		// Determine the persistable element for the given data object
		IPersistable persistable = data instanceof IPersistable ? (IPersistable)data : null;
		// If the element isn't a persistable by itself, try to adapt the element
		if (persistable == null) persistable = data instanceof IAdaptable ? (IPersistable) ((IAdaptable)data).getAdapter(IPersistable.class) : null;
		if (persistable == null) persistable = (IPersistable) Platform.getAdapterManager().getAdapter(data, IPersistable.class);

		// If the persistable could be still not determined, throw an IOException
		if (persistable == null) throw new IOException("'data' must be adaptable to IPersistable."); //$NON-NLS-1$

		// Determine the persistence delegate
		IPersistenceDelegate delegate = persistable.getStorageID() != null ? PersistenceDelegateManager.getInstance().getDelegate(persistable.getStorageID(), false) : null;
		// If the persistence delegate could not be determined, throw an IOException
		if (delegate == null) throw new IOException("The persistence delegate for ID '" + persistable.getStorageID() + "' cannot be determined."); //$NON-NLS-1$ //$NON-NLS-2$

		// Determine the URI
		URI uri = persistable.getURI(data);
		if (uri == null) throw new IOException("The URI cannot be determined."); //$NON-NLS-1$

		// Pass on to the delegate for deleting
	    return delegate.delete(uri);
	}
}
