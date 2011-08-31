/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tm.te.ui.views.interfaces.IEditorPage;
import org.eclipse.tm.te.ui.views.internal.extensions.EditorPageBinding;
import org.eclipse.tm.te.ui.views.internal.extensions.EditorPageBindingExtensionPointManager;
import org.eclipse.tm.te.ui.views.internal.extensions.EditorPageExtensionPointManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;


/**
 * Target Explorer: Details editor.
 */
public class Editor extends FormEditor implements IPersistableEditor {

	// The reference to an memento to restore once the editor got activated
	private IMemento mementoToRestore;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	@Override
	protected void addPages() {
		// Read extension point and add the contributed pages.
		IEditorInput input = getEditorInput();
		// Get all applicable editor page bindings
		EditorPageBinding[] bindings = EditorPageBindingExtensionPointManager.getInstance().getApplicableEditorPageBindings(input);
		for (EditorPageBinding binding : bindings) {
			String pageId = binding.getPageId();
			if (pageId != null) {
				// Get the corresponding editor page instance
				IEditorPage page = EditorPageExtensionPointManager.getInstance().getEditorPage(pageId, true);
				if (page != null) {
					try {
						// Associate this editor with the page instance.
						// This is typically done in the constructor, but we are
						// utilizing a default constructor to instantiate the page.
						page.initialize(this);

						// Read in the "insertBefore" and "insertAfter" properties of the binding
						String insertBefore = binding.getInsertBefore().trim();
						String insertAfter = binding.getInsertAfter().trim();

						// insertBefore will eclipse insertAfter is both is specified.
						if (!"".equals(insertBefore)) { //$NON-NLS-1$
							// If it is "first", we insert the page at index 0
							if ("first".equalsIgnoreCase(insertBefore)) { //$NON-NLS-1$
								addPage(0, page);
							} else {
								// Find the index of the page we shall insert this page before
								int index = getIndexOf(insertBefore);
								if (index != -1) addPage(index, page);
								else addPage(page);
							}
						} else if (!"".equals(insertAfter) && !"last".equalsIgnoreCase(insertAfter)) { //$NON-NLS-1$ //$NON-NLS-2$
							// Find the index of the page we shall insert this page after
							int index = getIndexOf(insertAfter);
							if (index != -1 && index + 1 < pages.size()) addPage(index + 1, page);
							else addPage(page);
						} else {
							// And add the page to the editor as last page.
							addPage(page);
						}
					} catch (PartInitException e) { /* ignored on purpose */ }
				}
			}
		}

		if (mementoToRestore != null) {
			// Loop over all registered pages and pass on the editor specific memento
			// to the pages which implements IPersistableEditor as well
			for (Object page : pages) {
				if (page instanceof IPersistableEditor) {
					((IPersistableEditor)page).restoreState(mementoToRestore);
				}
			}
			mementoToRestore = null;
		}
	}

	/**
	 * Returns the index of the page with the given id.
	 *
	 * @param pageId The page id. Must not be <code>null</code>.
	 * @return The page index or <code>-1</code> if not found.
	 */
	private int getIndexOf(String pageId) {
		Assert.isNotNull(pageId);
		for (int i = 0; i < pages.size(); i++) {
			Object page = pages.get(i);
			if (page instanceof IFormPage) {
				IFormPage fpage = (IFormPage)page;
				if (fpage.getId().equals(pageId))
					return i;
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);

		// Update the part name
		if (!"".equals(input.getName())) setPartName(input.getName()); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		commitPages(true);
		editorDirtyStateChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPersistableEditor#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento memento) {
		// Get the editor specific memento
		mementoToRestore = internalGetMemento(memento);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		// Get the editor specific memento
		memento = internalGetMemento(memento);
		// Loop over all registered pages and pass on the editor specific memento
		// to the pages which implements IPersistable as well
		for (Object page : pages) {
			if (page instanceof IPersistable) {
				((IPersistable)page).saveState(memento);
			}
		}
	}

	/**
	 * Internal helper method accessing our editor local child memento
	 * from the given parent memento.
	 */
	private IMemento internalGetMemento(IMemento memento) {
		// Assume the editor memento to be the same as the parent memento
		IMemento editorMemento = memento;

		// If the parent memento is not null, create a child within the parent
		if (memento != null) {
			editorMemento = memento.getChild(Editor.class.getName());
			if (editorMemento == null) {
				editorMemento = memento.createChild(Editor.class.getName());
			}
		} else {
			// The parent memento is null. Create a new internal instance
			// of a XMLMemento. This case is happening if the user switches
			// to another perspective an the view becomes visible by this switch.
			editorMemento = XMLMemento.createWriteRoot(Editor.class.getName());
		}

		return editorMemento;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.MultiPageEditorPart#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		// We pass on the adapt request to the currently active page
		Object adapterInstance = getActivePageInstance() != null ? getActivePageInstance().getAdapter(adapter) : null;
		if (adapterInstance == null) {
			// If failed to adapt via the currently active page, pass on to the super implementation
			adapterInstance = super.getAdapter(adapter);
		}
		return adapterInstance;
	}
}
