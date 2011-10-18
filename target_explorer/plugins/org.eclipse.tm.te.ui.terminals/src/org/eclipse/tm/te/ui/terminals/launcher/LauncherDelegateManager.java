/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.launcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.ui.terminals.interfaces.ILauncherDelegate;

/**
 * Terminal launcher delegate manager implementation.
 */
public class LauncherDelegateManager extends AbstractExtensionPointManager<ILauncherDelegate> {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstanceHolder {
		public static LauncherDelegateManager instance = new LauncherDelegateManager();
	}

	/**
	 * Returns the singleton instance.
	 */
	public static LauncherDelegateManager getInstance() {
		return LazyInstanceHolder.instance;
	}

	/**
	 * Constructor.
	 */
	LauncherDelegateManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tm.te.ui.terminals.launcherDelegates"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "delegate"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed terminal launcher delegates.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed terminal launcher delegate.
	 *
	 * @return The list of contributed terminal launcher delegates, or an empty array.
	 */
	public ILauncherDelegate[] getLauncherDelegates(boolean unique) {
		List<ILauncherDelegate> contributions = new ArrayList<ILauncherDelegate>();
		Collection<ExecutableExtensionProxy<ILauncherDelegate>> launcherDelegates = getExtensions().values();
		for (ExecutableExtensionProxy<ILauncherDelegate> launcherDelegate : launcherDelegates) {
			ILauncherDelegate instance = unique ? launcherDelegate.newInstance() : launcherDelegate.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new ILauncherDelegate[contributions.size()]);
	}

	/**
	 * Returns the terminal launcher delegate identified by its unique id. If no terminal
	 * launcher delegate with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the terminal launcher delegate or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the terminal launcher delegate contribution.
	 *
	 * @return The terminal launcher delegate instance or <code>null</code>.
	 */
	public ILauncherDelegate getLauncherDelegate(String id, boolean unique) {
		ILauncherDelegate contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<ILauncherDelegate> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}
}
