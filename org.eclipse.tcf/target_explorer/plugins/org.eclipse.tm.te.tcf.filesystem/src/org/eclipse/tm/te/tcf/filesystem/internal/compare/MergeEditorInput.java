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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IPropertyChangeNotifier;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tm.te.tcf.filesystem.internal.ImageConsts;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
/**
 * A <code>MergeEditorInput</code> is the input of a compare editor used to
 * compare the local file and the remote file in a Target Explorer file system.
 */
public class MergeEditorInput extends CompareEditorInput implements
		ISaveablesSource, IPropertyListener, ICompareInputChangeListener {

	// The left element is a local file which can be editable.
	private LocalTypedElement left;
	// The right element is the remote file which is read only.
	private RemoteTypedElement right;

	// The active page of the current workbench.
	private final IWorkbenchPage page;

	// The current input change listener list.
	private final ListenerList inputChangeListeners = new ListenerList(ListenerList.IDENTITY);
	// The current saveable for the left element, i.e., the local file.
	private LocalFileSaveable saveable;

	/**
	 * Creates a new MergeEditorInput.
	 *
	 * @param left
	 * @param right
	 * @param page
	 */
	public MergeEditorInput(LocalTypedElement left, RemoteTypedElement right, IWorkbenchPage page) {
		super(new CompareConfiguration());
		this.page = page;
		this.left = left;
		this.right = right;
		configureCompare();
	}

	/**
	 * Configure the compare configuration using the left
	 * and right elements. Set the left and right label.
	 * Set the editable status.
	 */
	protected void configureCompare() {
		CompareConfiguration cc = getCompareConfiguration();
		cc.setLeftEditable(true);
		cc.setRightEditable(false);

		String name = TextProcessor.process(left.getName());
		String label = "Local: " + name; //$NON-NLS-1$
		cc.setLeftLabel(label);

		name = TextProcessor.process(right.toString());
		label = "Remote: " + name; //$NON-NLS-1$
		cc.setRightLabel(label);
	}

	/**
	 * Returns <code>true</code> if the other object is of type
	 * <code>MergeEditorInput</code> and both of their corresponding fLeft and
	 * fRight objects are identical. The content is not considered.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof MergeEditorInput) {
			MergeEditorInput other = (MergeEditorInput) obj;
			return other.left.equals(left) && other.right.equals(right);
		}
		return false;
	}

	/**
	 * Prepare the compare input of this editor input. This method is not
	 * intended to be overridden of extended by subclasses (but is not final for
	 * backwards compatibility reasons). The implementation of this method in
	 * this class delegates the creation of the compare input to the
	 * {@link #prepareCompareInput(IProgressMonitor)} method which subclasses
	 * must implement.
	 *
	 * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		right.cacheContents(monitor);
		MergeInput input = new MergeInput(left, right);
		setTitle(input.getName());
		return input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return "Compare " + left + " and " + right; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	@Override
	public String getTitle() {
		return "Compare " + left.getName() + " with Local Cache"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Override the super method to provide clear typed input.
	 */
	@Override
	public MergeInput getCompareResult() {
		return (MergeInput) super.getCompareResult();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#handleDispose()
	 */
	@Override
	protected void handleDispose() {
		super.handleDispose();
		ICompareInput compareInput = getCompareResult();
		if (compareInput != null)
			compareInput.removeCompareInputChangeListener(this);
		if(getCompareResult()!=null){
			getSaveable().removePropertyListener(this);
			getSaveable().dispose();
		}
		left.discardBuffer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#contentsCreated()
	 */
	@Override
	protected void contentsCreated() {
		super.contentsCreated();
		if (getCompareResult() != null) {
			getCompareResult().addCompareInputChangeListener(this);
			getSaveable().addPropertyListener(this);
			setDirty(getSaveable().isDirty());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IWorkbenchPartConstants.PROP_DIRTY && getCompareResult()!=null) {
			setDirty(getSaveable().isDirty());
		}
	}

	/**
	 * Close the editor if it is not dirty. If it is still dirty, let the
	 * content merge viewer handle the compare input change.
	 *
	 * @param checkForUnsavedChanges
	 *            whether to check for unsaved changes
	 * @return <code>true</code> if the editor was closed (note that the close
	 *         may be asynchronous)
	 */
	protected boolean closeEditor(boolean checkForUnsavedChanges) {
		if (isSaveNeeded() && checkForUnsavedChanges) {
			return false;
		}
		final IWorkbenchPage page = getPage();
		if (page == null)
			return false;

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Shell shell = page.getWorkbenchWindow().getShell();
				if (shell == null)
					return;

				IEditorPart part = page.findEditor(MergeEditorInput.this);
				getPage().closeEditor(part, false);
			}
		};
		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Shell shell = page.getWorkbenchWindow().getShell();
			if (shell == null)
				return false;
			Display display = shell.getDisplay();
			display.asyncExec(runnable);
		}
		return true;
	}

	/**
	 * Get the current active page.
	 * @return the workbench page.
	 */
	IWorkbenchPage getPage() {
		if (page == null)
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		return page;
	}

	/**
	 * Propagate the input change event to the compare input change listener list.
	 */
	/* default */void propogateInputChange() {
		if (!inputChangeListeners.isEmpty()) {
			Object[] allListeners = inputChangeListeners.getListeners();
			for (int i = 0; i < allListeners.length; i++) {
				final ICompareInputChangeListener listener = (ICompareInputChangeListener) allListeners[i];
				SafeRunner.run(new ISafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.compareInputChanged(getCompareResult());
					}

					@Override
					public void handleException(Throwable exception) {
						// Logged by the safe runner
					}
				});
			}
		}
	}

	/**
	 * Get the fSaveable that provides the save behavior for this compare editor
	 * input. The {@link #createSaveable()} is called to create the fSaveable if
	 * it does not yet exist. This method cannot be called until after the input
	 * is prepared (i.e. until after the {@link #run(IProgressMonitor)} method
	 * is called which will in turn will invoke
	 * {@link #prepareCompareInput(IProgressMonitor)}.
	 *
	 * @return fSaveable that provides the save behavior for this compare editor
	 *         input.
	 */
	public LocalFileSaveable getSaveable() {
		if (saveable == null) {
			Assert.isNotNull(getCompareResult());
			saveable = new LocalFileSaveable(getCompareResult(), this, left);
		}
		return saveable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablesSource#getActiveSaveables()
	 */
	@Override
	public Saveable[] getActiveSaveables() {
		if (getCompareResult() == null)
			return new Saveable[0];
		return new Saveable[] { getSaveable() };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablesSource#getSaveables()
	 */
	@Override
	public Saveable[] getSaveables() {
		return getActiveSaveables();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#addCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	@Override
	public void addCompareInputChangeListener(ICompareInput input,
			ICompareInputChangeListener listener) {
		if (input == getCompareResult()) {
			inputChangeListeners.add(listener);
		} else {
			super.addCompareInputChangeListener(input, listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#removeCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	@Override
	public void removeCompareInputChangeListener(ICompareInput input,
			ICompareInputChangeListener listener) {
		if (input == getCompareResult()) {
			inputChangeListeners.remove(listener);
		} else {
			super.removeCompareInputChangeListener(input, listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitleImage()
	 */
	@Override
	public Image getTitleImage() {
		return UIPlugin.getImage(ImageConsts.COMPARE_EDITOR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return UIPlugin.getImageDescriptor(ImageConsts.COMPARE_EDITOR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#findContentViewer(org.eclipse.jface.viewers.Viewer, org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Viewer findContentViewer(Viewer oldViewer, ICompareInput input,
			Composite parent) {
		Viewer newViewer = super.findContentViewer(oldViewer, input, parent);
		boolean isNewViewer = newViewer != oldViewer;
		if (isNewViewer && newViewer instanceof IPropertyChangeNotifier && getCompareResult()!=null) {
			// Register the model for change events if appropriate
			final IPropertyChangeNotifier dsp = (IPropertyChangeNotifier) newViewer;
			dsp.addPropertyChangeListener(getSaveable());
			Control c = newViewer.getControl();
			c.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					dsp.removePropertyChangeListener(getSaveable());
				}
			});
		}
		return newViewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#canRunAsJob()
	 */
	@Override
	public boolean canRunAsJob() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#isDirty()
	 */
	@Override
	public boolean isDirty() {
		if (saveable != null)
			return saveable.isDirty();
		return super.isDirty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener#compareInputChanged(org.eclipse.compare.structuremergeviewer.ICompareInput)
	 */
	@Override
	public void compareInputChanged(ICompareInput source) {
		if (source == getCompareResult()) {
			boolean closed = false;
			if (source.getKind() == Differencer.NO_CHANGE) {
				closed = closeEditor(true);
			}
			if (!closed) {
				// The editor was closed either because the compare
				// input still has changes or because the editor input
				// is dirty. In either case, fire the changes
				// to the registered listeners
				propogateInputChange();
			}
		}
	}
}
