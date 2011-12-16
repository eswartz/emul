/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.compare;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ICompareContainer;
import org.eclipse.compare.IContentChangeListener;
import org.eclipse.compare.IContentChangeNotifier;
import org.eclipse.compare.ISharedDocumentAdapter;
import org.eclipse.compare.SharedDocumentAdapter;
import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.te.tcf.filesystem.internal.compare.EditableSharedDocumentAdapter.ISharedDocumentAdapterListener;
import org.eclipse.tm.te.tcf.filesystem.internal.handlers.CacheManager;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * A <code>LocalFileSaveable</code> is used as the saveable object that provides
 * the save behavior for the merge editor input(<code>MergeEditorInput</code>).
 */
public class LocalFileSaveable extends Saveable implements
		IPropertyChangeListener, ISharedDocumentAdapterListener,
		IContentChangeListener {
	// The property listener list.
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	// The merge input that provides the left and the right compared elements.
	private final MergeInput input;

	// The input of the editor that provides the comparing view.
	private final MergeEditorInput editorInput;

	// If the current document of the file is being saved.
	private boolean saving;

	// The local file element.
	private LocalTypedElement fileElement;

	// The document provided by the local file.
	private IDocument document;

	// If the current document has been connected.
	private boolean connected;

	/**
	 * Create the file-based saveable comparison.
	 *
	 * @param input
	 *            the compare input to be save
	 * @param editorInput
	 *            the editor input containing the comparison
	 * @param fileElement
	 *            the file element that handles the saving and change
	 *            notification
	 */
	public LocalFileSaveable(MergeInput input, MergeEditorInput editorInput, LocalTypedElement fileElement) {
		Assert.isNotNull(input);

		this.input = input;
		this.editorInput = editorInput;
		this.fileElement = fileElement;
		this.fileElement.addContentChangeListener(this);
		this.fileElement.setDocumentListener(this);
	}

	/**
	 * Dispose of the saveable.
	 */
	public void dispose() {
		fileElement.removeContentChangeListener(this);
		fileElement.discardBuffer();
		fileElement.setDocumentListener(null);
	}

	/**
	 * Performs the save.
	 *
	 * @param monitor The progress monitor.
	 *
	 * @throws CoreException If the save operation fails.
	 */
	protected void performSave(IProgressMonitor monitor) throws CoreException {
		try {
			saving = true;
			monitor.beginTask(null, 100);
			// First, we need to flush the viewers so the changes get buffered
			// in the input
			flushViewers(monitor);
			// Then we tell the input to commit its changes
			// Only the left is ever saveable
			if (fileElement.isDirty()) {
				if (fileElement.isConnected()) {
					fileElement.store2Document(monitor);
				} else {
					fileElement.store2Cache(monitor);
				}
			}
		} finally {
			// Make sure we fire a change for the compare input to update the
			// viewers
			fireInputChange();
			setDirty(false);
			saving = false;
			monitor.done();
			//Trigger upload action
			FSTreeNode node = fileElement.getFSTreeNode();
			CacheManager.getInstance().upload(node);
		}
	}

	/**
	 * Flush the contents of any viewers into the compare input.
	 *
	 * @param monitor
	 *            a progress monitor
	 * @throws CoreException
	 */
	protected void flushViewers(IProgressMonitor monitor) throws CoreException {
		editorInput.saveChanges(monitor);
	}

	/**
	 * Fire an input change for the compare input after it has been saved.
	 */
	protected void fireInputChange() {
		editorInput.getCompareResult().fireInputChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return editorInput.isSaveNeeded();
	}

	/**
	 * Marks the editor input dirty.
	 *
	 * @param dirty <code>True</code> if the editor is dirty, <code>false</code> if not.
	 */
	protected void setDirty(boolean dirty) {
		if (isDirty() != dirty) {
			editorInput.setDirty(dirty);
			firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#getName()
	 */
	@Override
	public String getName() {
		// Return the name of the file element as held in the compare input
		if (fileElement.equals(input.getLeft())) {
			return input.getLeft().getName();
		}
		if (fileElement.equals(input.getRight())) {
			return input.getRight().getName();
		}

		// Fallback call returning name of the main non-null element of the input
		//
		// see org.eclipse.team.internal.ui.mapping.AbstractCompareInput#getMainElement()
		return input.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return editorInput.getToolTipText();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		Image image = input.getImage();
		if (image != null)
			return ImageDescriptor.createFromImage(image);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String propertyName = e.getProperty();
		if (CompareEditorInput.DIRTY_STATE.equals(propertyName)) {
			boolean changed = false;
			Object newValue = e.getNewValue();
			if (newValue instanceof Boolean)
				changed = ((Boolean) newValue).booleanValue();

			ContentMergeViewer cmv = (ContentMergeViewer) e.getSource();

			if (fileElement.equals(input.getLeft())) {
				if (changed && cmv.internalIsLeftDirty())
					setDirty(changed);
				else if (!changed && !cmv.internalIsLeftDirty()) {
					setDirty(changed);
				}
			}
			if (fileElement.equals(input.getRight())) {
				if (changed && cmv.internalIsRightDirty())
					setDirty(changed);
				else if (!changed && !cmv.internalIsRightDirty()) {
					setDirty(changed);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#hashCode()
	 */
	@Override
	public int hashCode() {
		if (document != null) {
			return document.hashCode();
		}
		return input.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Saveable))
			return false;

		if (document != null) {
			Object otherDocument = ((Saveable) obj).getAdapter(IDocument.class);

			if (document == null && otherDocument == null)
				return false;

			return document != null && document.equals(otherDocument);
		}

		if (obj instanceof LocalFileSaveable) {
			LocalFileSaveable saveable = (LocalFileSaveable) obj;
			return saveable.input.equals(input);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.Saveable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IDocument.class) {
			if (document != null)
				return document;
			if (fileElement.isConnected()) {
				ISharedDocumentAdapter sda = (ISharedDocumentAdapter) fileElement
						.getAdapter(ISharedDocumentAdapter.class);
				if (sda != null) {
					IEditorInput input = sda.getDocumentKey(fileElement);
					if (input != null) {
						IDocumentProvider provider = SharedDocumentAdapter
								.getDocumentProvider(input);
						if (provider != null)
							return provider.getDocument(input);
					}
				}
			}
		}
		if (adapter == IEditorInput.class) {
			return fileElement.getEditorInput();
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Get an ISaveablesLifecycleListener from the specified workbench
	 * part.
	 * @param part The workbench part.
	 * @return The listener.
	 */
	private ISaveablesLifecycleListener getSaveablesLifecycleListener(
			IWorkbenchPart part) {
		if (part instanceof ISaveablesLifecycleListener)
			return (ISaveablesLifecycleListener) part;

		Object adapted = part.getAdapter(ISaveablesLifecycleListener.class);
		if (adapted instanceof ISaveablesLifecycleListener)
			return (ISaveablesLifecycleListener) adapted;

		adapted = Platform.getAdapterManager().loadAdapter(part,
				ISaveablesLifecycleListener.class.getName());
		if (adapted instanceof ISaveablesLifecycleListener)
			return (ISaveablesLifecycleListener) adapted;

		return (ISaveablesLifecycleListener) part.getSite().getService(
				ISaveablesLifecycleListener.class);
	}

	/**
	 * When the document of the local file is connected, register this saveable.
	 */
	private void registerSaveable() {
		ICompareContainer container = editorInput.getContainer();
		IWorkbenchPart part = container.getWorkbenchPart();
		if (part != null) {
			ISaveablesLifecycleListener lifecycleListener = getSaveablesLifecycleListener(part);
			// Remove this saveable from the lifecycle listener
			lifecycleListener.handleLifecycleEvent(new SaveablesLifecycleEvent(
					part, SaveablesLifecycleEvent.POST_CLOSE,
					new Saveable[] { this }, false));
			// Now fix the hashing so it uses the fConnected fDocument
			IDocument document = (IDocument) getAdapter(IDocument.class);
			if (document != null) {
				this.document = document;
			}
			// Finally, add this saveable back to the listener
			lifecycleListener.handleLifecycleEvent(new SaveablesLifecycleEvent(
					part, SaveablesLifecycleEvent.POST_OPEN,
					new Saveable[] { this }, false));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.filesystem.internal.compare.EditableSharedDocumentAdapter.ISharedDocumentAdapterListener#handleDocumentConnected()
	 */
	@Override
	public void handleDocumentConnected() {
		if (connected)
			return;
		connected = true;
		registerSaveable();
		fileElement.setDocumentListener(null);
	}

	@Override
	public void handleDocumentDeleted() {
		// Ignore
	}

	@Override
	public void handleDocumentDisconnected() {
		// Ignore
	}

	@Override
	public void handleDocumentFlushed() {
		// Ignore
	}

	@Override
	public void handleDocumentSaved() {
		// Ignore
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.compare.IContentChangeListener#contentChanged(org.eclipse.compare.IContentChangeNotifier)
	 */
	@Override
	public void contentChanged(IContentChangeNotifier source) {
		if (!saving) {
			try {
				performSave(new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doSave(IProgressMonitor monitor) throws CoreException {
		if (isDirty()) {
			performSave(monitor);
			setDirty(false);
		}
	}

	/**
	 * Revert any changes in the buffer back to the last saved state.
	 *
	 * @param monitor
	 *            a progress monitor on <code>null</code> if progress feedback
	 *            is not required
	 */
	public void doRevert(IProgressMonitor monitor) {
		if (!isDirty())
			return;
		fileElement.discardBuffer();
		setDirty(false);
	}

	/**
	 * Add a property change listener. Adding a listener that is already
	 * registered has no effect.
	 *
	 * @param listener
	 *            the listener
	 */
	public void addPropertyListener(IPropertyListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a property change listener. Removing a listener that is not
	 * registered has no effect.
	 *
	 * @param listener
	 *            the listener
	 */
	public void removePropertyListener(IPropertyListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Fire a property change event for this buffer.
	 *
	 * @param property
	 *            the property that changed
	 */
	protected void firePropertyChange(final int property) {
		Object[] allListeners = listeners.getListeners();
		for (int i = 0; i < allListeners.length; i++) {
			final Object object = allListeners[i];
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					((IPropertyListener) object).propertyChanged(
							LocalFileSaveable.this, property);
				}

				@Override
				public void handleException(Throwable exception) {
					// handled by platform
				}
			});
		}
	}
}
