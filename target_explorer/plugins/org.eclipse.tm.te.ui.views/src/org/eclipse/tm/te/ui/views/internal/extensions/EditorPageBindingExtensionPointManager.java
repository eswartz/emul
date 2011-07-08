/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.te.core.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.core.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.ui.views.activator.UIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISources;


/**
 * Target Explorer: Details editor page binding extension point manager implementation.
 */
public class EditorPageBindingExtensionPointManager extends AbstractExtensionPointManager<EditorPageBinding> {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static EditorPageBindingExtensionPointManager instance = new EditorPageBindingExtensionPointManager();
	}

	/**
	 * Constructor.
	 */
	EditorPageBindingExtensionPointManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the extension point manager.
	 */
	public static EditorPageBindingExtensionPointManager getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.views.internal.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tm.te.ui.views.editorPageBindings"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.views.internal.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "editorPageBinding"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.views.internal.extensions.AbstractExtensionPointManager#doCreateExtensionProxy(org.eclipse.core.runtime.IConfigurationElement)
	 */
	@Override
	protected ExecutableExtensionProxy<EditorPageBinding> doCreateExtensionProxy(IConfigurationElement element) throws CoreException {
		return new ExecutableExtensionProxy<EditorPageBinding>(element) {
			/* (non-Javadoc)
			 * @see org.eclipse.tm.te.ui.views.internal.extensions.ExecutableExtensionProxy#newInstance()
			 */
			@Override
			public EditorPageBinding newInstance() {
				EditorPageBinding instance = new EditorPageBinding();
				if (instance != null) {
					try {
						instance.setInitializationData(getConfigurationElement(), null, null);
					} catch (CoreException e) {
						IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
						                            e.getLocalizedMessage(), e);
						UIPlugin.getDefault().getLog().log(status);
					}
				}
				return instance;
			}
		};
	}

	/**
	 * Returns the applicable editor page bindings for the given data source model node..
	 *
	 * @param input The active editor input or <code>null</code>.
	 * @return The list of applicable editor page bindings or an empty array.
	 */
	public EditorPageBinding[] getApplicableEditorPageBindings(IEditorInput input) {
		List<EditorPageBinding> applicable = new ArrayList<EditorPageBinding>();

		for (EditorPageBinding binding : getEditorPageBindings()) {
			Expression enablement = binding.getEnablement();

			// The page binding is applicable by default if no expression
			// is specified.
			boolean isApplicable = enablement == null;

			if (enablement != null && input != null) {
				// Extract the node from the editor input
				Object node = input.getAdapter(Object.class);
				if (node != null) {
					// Set the default variable to the data source model node instance.
					EvaluationContext context = new EvaluationContext(null, node);
					// Set the "activeEditorInput" variable to the data source model node instance.
					context.addVariable(ISources.ACTIVE_EDITOR_INPUT_NAME, node);
					// Evaluate the expression
					try {
						isApplicable = enablement.evaluate(context).equals(EvaluationResult.TRUE);
					} catch (CoreException e) {
						IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
						                            e.getLocalizedMessage(), e);
						UIPlugin.getDefault().getLog().log(status);
					}
				} else {
					// The enablement is false by definition if we cannot
					// determine the data source model node.
					isApplicable = false;
				}
			}

			// Add the page if applicable
			if (isApplicable) applicable.add(binding);
		}

		return applicable.toArray(new EditorPageBinding[applicable.size()]);
	}

	/**
	 * Returns the list of all contributed editor page bindings.
	 *
	 * @return The list of contributed editor page bindings, or an empty array.
	 */
	public EditorPageBinding[] getEditorPageBindings() {
		List<EditorPageBinding> contributions = new ArrayList<EditorPageBinding>();
		Collection<ExecutableExtensionProxy<EditorPageBinding>> editorPageBindings = getExtensions().values();
		for (ExecutableExtensionProxy<EditorPageBinding> editorPageBinding : editorPageBindings) {
			EditorPageBinding instance = editorPageBinding.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new EditorPageBinding[contributions.size()]);
	}

	/**
	 * Returns the editor page binding identified by its unique id. If no editor
	 * page binding with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the editor page binding or <code>null</code>
	 *
	 * @return The editor page instance or <code>null</code>.
	 */
	public EditorPageBinding getEditorPageBinding(String id) {
		EditorPageBinding contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<EditorPageBinding> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = proxy.getInstance();
		}

		return contribution;
	}
}
