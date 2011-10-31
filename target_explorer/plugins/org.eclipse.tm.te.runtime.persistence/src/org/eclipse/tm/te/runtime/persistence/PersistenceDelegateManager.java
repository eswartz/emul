/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceDelegate;
import org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;

/**
 * Persistence delegate manager implementation.
 */
public class PersistenceDelegateManager extends AbstractExtensionPointManager<IPersistenceDelegate> {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static PersistenceDelegateManager instance = new PersistenceDelegateManager();
	}

	/**
	 * Constructor.
	 */
	PersistenceDelegateManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the extension point manager.
	 */
	public static PersistenceDelegateManager getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tm.te.runtime.persistence.delegates"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "delegate"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed persistence delegates.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed persistence delegate.
	 *
	 * @return The list of contributed persistence delegates, or an empty array.
	 */
	public IPersistenceDelegate[] getDelegates(boolean unique) {
		List<IPersistenceDelegate> contributions = new ArrayList<IPersistenceDelegate>();
		Collection<ExecutableExtensionProxy<IPersistenceDelegate>> delegates = getExtensions().values();
		for (ExecutableExtensionProxy<IPersistenceDelegate> delegate : delegates) {
			IPersistenceDelegate instance = unique ? delegate.newInstance() : delegate.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new IPersistenceDelegate[contributions.size()]);
	}

	/**
	 * Returns the persistence delegate identified by its unique id. If no persistence
	 * delegate with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the persistence delegate or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the persistence delegate contribution.
	 *
	 * @return The persistence delegate instance or <code>null</code>.
	 */
	public IPersistenceDelegate getDelegate(String id, boolean unique) {
		IPersistenceDelegate contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<IPersistenceDelegate> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}
}
