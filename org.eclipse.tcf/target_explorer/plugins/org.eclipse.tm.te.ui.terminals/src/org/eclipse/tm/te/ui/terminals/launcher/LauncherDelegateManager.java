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

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.ui.terminals.activator.UIPlugin;
import org.eclipse.tm.te.ui.terminals.interfaces.ILauncherDelegate;
import org.eclipse.ui.ISources;

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

	/**
	 * Returns the applicable terminal launcher delegates for the given selection.
	 *
	 * @param selection The selection or <code>null</code>.
	 * @return The list of applicable terminal launcher delegates or an empty array.
	 */
	public ILauncherDelegate[] getApplicableLauncherDelegates(ISelection selection) {
		List<ILauncherDelegate> applicable = new ArrayList<ILauncherDelegate>();

		for (ILauncherDelegate delegate : getLauncherDelegates(false)) {
			Expression enablement = delegate.getEnablement();

			// The launcher delegate is applicable by default if
			// no expression is specified.
			boolean isApplicable = enablement == null;

			if (enablement != null) {
				if (selection != null) {
					// Set the default variable to selection.
					EvaluationContext context = new EvaluationContext(null, selection);
					// Set the "selection" variable to the selection.
					context.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
					// Evaluate the expression
					try {
						isApplicable = enablement.evaluate(context).equals(EvaluationResult.TRUE);
					} catch (CoreException e) {
						IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
						                            e.getLocalizedMessage(), e);
						UIPlugin.getDefault().getLog().log(status);
					}
				} else {
					// The enablement is false by definition if
					// there is no selection.
					isApplicable = false;
				}
			}

			// Add the page if applicable
			if (isApplicable) applicable.add(delegate);
		}

		return applicable.toArray(new ILauncherDelegate[applicable.size()]);
	}

}
