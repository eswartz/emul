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

import org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.ui.views.interfaces.IEditorPage;


/**
 * Target Explorer: Details editor page extension point manager implementation.
 */
public class EditorPageExtensionPointManager extends AbstractExtensionPointManager<IEditorPage> {
	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static EditorPageExtensionPointManager instance = new EditorPageExtensionPointManager();
	}

	/**
	 * Constructor.
	 */
	EditorPageExtensionPointManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the extension point manager.
	 */
	public static EditorPageExtensionPointManager getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tm.te.ui.views.editorPages"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "editorPage"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed editor pages.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed editor page.
	 *
	 * @return The list of contributed editor pages, or an empty array.
	 */
	public IEditorPage[] getEditorPages(boolean unique) {
		List<IEditorPage> contributions = new ArrayList<IEditorPage>();
		Collection<ExecutableExtensionProxy<IEditorPage>> editorPages = getExtensions().values();
		for (ExecutableExtensionProxy<IEditorPage> editorPage : editorPages) {
			IEditorPage instance = unique ? editorPage.newInstance() : editorPage.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new IEditorPage[contributions.size()]);
	}

	/**
	 * Returns the editor page identified by its unique id. If no editor
	 * page with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the editor page or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the editor page contribution.
	 *
	 * @return The editor page instance or <code>null</code>.
	 */
	public IEditorPage getEditorPage(String id, boolean unique) {
		IEditorPage contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<IEditorPage> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}
}
