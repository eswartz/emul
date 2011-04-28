/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.trees;


import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.tm.te.ui.WorkbenchPartControl;
import org.eclipse.tm.te.ui.forms.CustomFormToolkit;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.Section;


/**
 * Target Explorer: Abstract tree control implementation.
 */
public abstract class AbstractTreeControl extends WorkbenchPartControl {
	// Reference to the tree viewer instance
	private TreeViewer fViewer;
	// Reference to the selection changed listener
	private ISelectionChangedListener fSelectionChangedListener;

	/**
	 * Constructor.
	 */
	public AbstractTreeControl() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param parentPart The parent workbench part this control is embedded in or <code>null</code>.
	 */
	public AbstractTreeControl(IWorkbenchPart parentPart) {
		super(parentPart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.WorkbenchPartControl#dispose()
	 */
	@Override
	public void dispose() {
		// Unregister the selection changed listener
		if (fSelectionChangedListener != null) {
			if (getViewer() != null) {
				getViewer().removeSelectionChangedListener(fSelectionChangedListener);
			}
			fSelectionChangedListener = null;
		}

		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.WorkbenchPartControl#setupFormPanel(org.eclipse.swt.widgets.Composite, org.eclipse.tm.te.ui.forms.CustomFormToolkit)
	 */
	@Override
	public void setupFormPanel(Composite parent, CustomFormToolkit toolkit) {
		super.setupFormPanel(parent, toolkit);

		// Create the tree viewer
		fViewer = doCreateTreeViewer(parent);
		// And configure the tree viewer
		configureTreeViewer(fViewer);

		// Prepare popup menu and toolbar
		createContributionItems(fViewer);
	}

	/**
	 * Creates the tree viewer instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @return The tree viewer.
	 */
	protected TreeViewer doCreateTreeViewer(Composite parent) {
		assert parent != null;
		return new TreeViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE);
	}

	/**
	 * Configure the tree viewer.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 */
	protected void configureTreeViewer(TreeViewer viewer) {
		assert viewer != null;

		viewer.setAutoExpandLevel(getAutoExpandLevel());

		viewer.setLabelProvider(doCreateTreeViewerLabelProvider(viewer));
		viewer.setContentProvider(doCreateTreeViewerContentProvider(viewer));
		viewer.setComparator(doCreateTreeViewerComparator(viewer));

		viewer.getTree().setLayoutData(doCreateTreeViewerLayoutData(viewer));

		// Attach the selection changed listener
		fSelectionChangedListener = doCreateTreeViewerSelectionChangedListener(viewer);
		if (fSelectionChangedListener != null) {
			viewer.addSelectionChangedListener(fSelectionChangedListener);
		}
	}

	/**
	 * Returns the number of levels to auto expand.
	 * If the method returns <code>0</code>, no auto expansion will happen
	 *
	 * @return The number of levels to auto expand or <code>0</code>.
	 */
	protected int getAutoExpandLevel() {
		return 2;
	}

	/**
	 * Creates the tree viewer layout data instance.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 * @return The tree viewer layout data instance.
	 */
	protected Object doCreateTreeViewerLayoutData(TreeViewer viewer) {
		return new GridData(GridData.FILL_BOTH);
	}

	/**
	 * Creates the tree viewer label provider instance.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 * @return The tree viewer label provider instance.
	 */
	protected abstract ILabelProvider doCreateTreeViewerLabelProvider(TreeViewer viewer);

	/**
	 * Creates the tree viewer content provider instance.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 * @return The tree viewer content provider instance.
	 */
	protected abstract ITreeContentProvider doCreateTreeViewerContentProvider(TreeViewer viewer);

	/**
	 * Creates the tree viewer comparator instance.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 * @return The tree viewer comparator instance or <code>null</code> to turn of sorting.
	 */
	protected ViewerComparator doCreateTreeViewerComparator(TreeViewer viewer) {
		assert viewer != null;
		return null;
	}

	/**
	 * Creates a new selection changed listener instance.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 * @return The selection changed listener instance.
	 */
	protected abstract ISelectionChangedListener doCreateTreeViewerSelectionChangedListener(TreeViewer viewer);

	/**
	 * Create the context menu and toolbar groups.
	 *
	 * @param viewer The tree viewer instance. Must not be <code>null</code>.
	 */
	protected void createContributionItems(TreeViewer viewer) {
		assert viewer != null;

		// Create the menu manager
		MenuManager manager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		// Attach the menu listener
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		// All items are removed when menu is closing
		manager.setRemoveAllWhenShown(true);
		// Associated with the tree
		viewer.getTree().setMenu(manager.createContextMenu(viewer.getTree()));

		// Register the context menu at the parent workbench part site.
		if (getParentPart() != null && getParentPart().getSite() != null && getContextMenuId() != null) {
			getParentPart().getSite().registerContextMenu(getContextMenuId(), manager, viewer);
		}

		// The toolbar is a bit more complicated as we want to have the
		// toolbar placed within the section title.
		createToolbarContributionItem(viewer);
	}

	/**
	 * Returns the context menu id.
	 *
	 * @return The context menu id.
	 */
	protected abstract String getContextMenuId();

	/**
	 * Creates the toolbar within the section parent of the given tree viewer.
	 *
	 * @param viewer The tree viewer instance. Must not be <code>null</code>.
	 */
	protected void createToolbarContributionItem(TreeViewer viewer) {
		assert viewer != null;

		// Determine the section parent from the tree viewer
		Composite parent = viewer.getTree().getParent();
		while (parent != null && !(parent instanceof Section)) {
			parent = parent.getParent();
		}

		// We are done here if we cannot find a section parent or the parent is disposed
		if (parent == null || parent.isDisposed()) {
			return;
		}

		// Create the toolbar control
		ToolBar toolbar = new ToolBar(parent, SWT.FLAT | SWT.HORIZONTAL | SWT.RIGHT);

		// The cursor within the toolbar shall change to an hand
		final Cursor handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);
		// Cursor needs to be explicitly disposed
		toolbar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if ((handCursor != null) && (handCursor.isDisposed() == false)) {
					handCursor.dispose();
				}
			}
		});

		// If the parent composite is a forms section, set the toolbar
		// as text client to the section header
		if (parent instanceof Section) {
			Section section = (Section)parent;
			// Set the toolbar as text client
			section.setTextClient(toolbar);
		}

		// create the toolbar items
		createToolBarItems(toolbar);
	}

	/**
	 * Create the toolbar items to be added to the toolbar. Override
	 * to add the wanted toolbar items.
	 * <p>
	 * <b>Note:</b> The toolbar items are added from left to right.
	 *
	 * @param toolbar The toolbar to add the toolbar items too. Must not be <code>null</code>.
	 */
	protected void createToolBarItems(ToolBar toolbar) {
		assert toolbar != null;
	}

	/**
	 * Returns the viewer instance.
	 *
	 * @return The viewer instance or <code>null</code>.
	 */
	public Viewer getViewer() {
		return fViewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (Viewer.class.isAssignableFrom(adapter)) {
			// We have to double check if our real viewer is assignable to
			// the requested Viewer class.
			Viewer viewer = getViewer();
			if (!adapter.isAssignableFrom(viewer.getClass())) {
				viewer = null;
			}
			return viewer;
		}

		return super.getAdapter(adapter);
	}

}
