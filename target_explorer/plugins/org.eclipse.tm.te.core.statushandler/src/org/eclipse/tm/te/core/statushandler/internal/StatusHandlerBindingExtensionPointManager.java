/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.statushandler.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.te.core.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.core.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.core.statushandler.activator.CoreBundleActivator;


/**
 * Target Explorer: Status handler binding extension point manager implementation.
 */
public class StatusHandlerBindingExtensionPointManager extends AbstractExtensionPointManager<StatusHandlerBinding> {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static StatusHandlerBindingExtensionPointManager instance = new StatusHandlerBindingExtensionPointManager();
	}

	/**
	 * Constructor.
	 */
	StatusHandlerBindingExtensionPointManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the extension point manager.
	 */
	public static StatusHandlerBindingExtensionPointManager getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.views.internal.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tm.te.core.statushandler.bindings"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.views.internal.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "binding"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.views.internal.extensions.AbstractExtensionPointManager#doCreateExtensionProxy(org.eclipse.core.runtime.IConfigurationElement)
	 */
	@Override
	protected ExecutableExtensionProxy<StatusHandlerBinding> doCreateExtensionProxy(IConfigurationElement element) throws CoreException {
		return new ExecutableExtensionProxy<StatusHandlerBinding>(element) {
			/* (non-Javadoc)
			 * @see org.eclipse.tm.te.ui.views.internal.extensions.ExecutableExtensionProxy#newInstance()
			 */
			@Override
			public StatusHandlerBinding newInstance() {
				StatusHandlerBinding instance = new StatusHandlerBinding();
				if (instance != null) {
					try {
						instance.setInitializationData(getConfigurationElement(), null, null);
					} catch (CoreException e) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
						                            e.getLocalizedMessage(), e);
						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
					}
				}
				return instance;
			}
		};
	}

	/**
	 * Returns the applicable status handler bindings for the given handler context.
	 *
	 * @param context The handler context or <code>null</code>.
	 * @return The list of applicable editor page bindings or an empty array.
	 */
	public StatusHandlerBinding[] getApplicableBindings(Object context) {
		List<StatusHandlerBinding> applicable = new ArrayList<StatusHandlerBinding>();

		for (StatusHandlerBinding binding : getBindings()) {
			Expression enablement = binding.getEnablement();

			// The page binding is applicable by default if no expression
			// is specified.
			boolean isApplicable = enablement == null;

			if (enablement != null) {
				if (context != null) {
					// Set the default variable to the handler context.
					EvaluationContext evalContext = new EvaluationContext(null, context);
					// Evaluate the expression
					try {
						isApplicable = enablement.evaluate(evalContext).equals(EvaluationResult.TRUE);
					} catch (CoreException e) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
								e.getLocalizedMessage(), e);
						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
					}
				} else {
					// The enablement is false by definition if no
					// handler context is not given.
					isApplicable = false;
				}
			}

			// Add the page if applicable
			if (isApplicable) applicable.add(binding);
		}

		return applicable.toArray(new StatusHandlerBinding[applicable.size()]);
	}

	/**
	 * Returns the list of all contributed status handler bindings.
	 *
	 * @return The list of contributed status handler bindings, or an empty array.
	 */
	public StatusHandlerBinding[] getBindings() {
		List<StatusHandlerBinding> contributions = new ArrayList<StatusHandlerBinding>();
		Collection<ExecutableExtensionProxy<StatusHandlerBinding>> statusHandlerBindings = getExtensions().values();
		for (ExecutableExtensionProxy<StatusHandlerBinding> statusHandlerBinding : statusHandlerBindings) {
			StatusHandlerBinding instance = statusHandlerBinding.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new StatusHandlerBinding[contributions.size()]);
	}

	/**
	 * Returns the status handler binding identified by its unique id. If no status
	 * handler binding with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the status handler binding or <code>null</code>
	 *
	 * @return The status handler binding instance or <code>null</code>.
	 */
	public StatusHandlerBinding getBinding(String id) {
		StatusHandlerBinding contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<StatusHandlerBinding> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = proxy.getInstance();
		}

		return contribution;
	}
}
