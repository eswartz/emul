/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River) - [345384] Provide property pages for remote file system nodes
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.controls;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tm.te.ui.interfaces.IUIConstants;
import org.eclipse.tm.te.ui.trees.AbstractTreeControl;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.MultiPageSelectionProvider;


/**
 * Target Explorer: File system browser control.
 */
public class FSTreeControl extends AbstractTreeControl implements ISelectionChangedListener{

	/**
	 * Constructor.
	 */
	public FSTreeControl() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param parentPart The parent workbench part this control is embedded in or <code>null</code>.
	 */
	public FSTreeControl(IWorkbenchPart parentPart) {
		super(parentPart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#configureTreeViewer(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		super.configureTreeViewer(viewer);

		Tree tree = viewer.getTree();
		if (hasColumns()) {
			TreeColumn column = new TreeColumn(tree, SWT.LEFT);
			column.setText(Messages.FSTreeControl_column_name_label);
			column.setWidth(300);

			column = new TreeColumn(tree, SWT.RIGHT);
			column.setText(Messages.FSTreeControl_column_size_label);
			column.setWidth(100);

			column = new TreeColumn(tree, SWT.RIGHT);
			column.setText(Messages.FSTreeControl_column_modified_label);
			column.setWidth(200);
		}
		tree.setHeaderVisible(hasColumns());
	}

	/**
	 * Returns if or if not to show the tree columns.
	 *
	 * @return <code>True</code> to show the tree columns, <code>false</code> otherwise.
	 */
	protected boolean hasColumns() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#doCreateTreeViewerContentProvider(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ITreeContentProvider doCreateTreeViewerContentProvider(TreeViewer viewer) {
		return new FSTreeContentProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#doCreateTreeViewerLabelProvider(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ILabelProvider doCreateTreeViewerLabelProvider(TreeViewer viewer) {
		return new FSTreeLabelProvider(viewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#doCreateTreeViewerSelectionChangedListener(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ISelectionChangedListener doCreateTreeViewerSelectionChangedListener(TreeViewer viewer) {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#doCreateTreeViewerComparator(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ViewerComparator doCreateTreeViewerComparator(TreeViewer viewer) {
		return new FSTreeViewerComparator(viewer, (ILabelProvider)viewer.getLabelProvider());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#getAutoExpandLevel()
	 */
	@Override
	protected int getAutoExpandLevel() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#getContextMenuId()
	 */
	@Override
	protected String getContextMenuId() {
		return IUIConstants.ID_CONTROL_MENUS_BASE + ".menu.fs"; //$NON-NLS-1$;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IWorkbenchPart parent = getParentPart();
		if (parent != null) {
			IWorkbenchPartSite site = parent.getSite();
			if (site != null) {
				ISelectionProvider selectionProvider = site.getSelectionProvider();
				if (selectionProvider instanceof MultiPageSelectionProvider) {
					// Propagate the selection event to update the selection context.
					((MultiPageSelectionProvider) selectionProvider).fireSelectionChanged(event);
				}
			}
		}
	}

}
