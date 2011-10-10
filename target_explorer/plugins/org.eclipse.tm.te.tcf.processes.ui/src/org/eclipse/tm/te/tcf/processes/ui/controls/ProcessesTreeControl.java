/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.ui.controls;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tm.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tm.te.ui.interfaces.IUIConstants;
import org.eclipse.tm.te.ui.trees.AbstractTreeControl;
import org.eclipse.tm.te.ui.trees.TreeViewerComparator;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Processes tree control.
 */
public class ProcessesTreeControl extends AbstractTreeControl {

	/**
	 * Constructor.
	 */
	public ProcessesTreeControl() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param parentPart The parent workbench part this control is embedded in or <code>null</code>.
	 */
	public ProcessesTreeControl(IWorkbenchPart parentPart) {
		super(parentPart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.trees.AbstractTreeControl#configureTreeViewer(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		super.configureTreeViewer(viewer);

		Tree tree = viewer.getTree();
		if (hasColumns()) {
			TreeColumn column = new TreeColumn(tree, SWT.LEFT);
			column.setText(Messages.ProcessesTreeControl_column_name_label);
			column.setWidth(250);

			column = new TreeColumn(tree, SWT.RIGHT);
			column.setText(Messages.ProcessesTreeControl_column_pid_label);
			column.setWidth(50);

			column = new TreeColumn(tree, SWT.RIGHT);
			column.setText(Messages.ProcessesTreeControl_column_ppid_label);
			column.setWidth(50);

			column = new TreeColumn(tree, SWT.RIGHT);
			column.setText(Messages.ProcessesTreeControl_column_state_label);
			column.setWidth(50);

			column = new TreeColumn(tree, SWT.RIGHT);
			column.setText(Messages.ProcessesTreeControl_column_user_label);
			column.setWidth(100);
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
	 * @see org.eclipse.tm.te.ui.trees.AbstractTreeControl#doCreateTreeViewerContentProvider(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ITreeContentProvider doCreateTreeViewerContentProvider(TreeViewer viewer) {
		return new ProcessesTreeContentProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.trees.AbstractTreeControl#doCreateTreeViewerLabelProvider(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ILabelProvider doCreateTreeViewerLabelProvider(TreeViewer viewer) {
		return new ProcessesTreeLabelProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.trees.AbstractTreeControl#doCreateTreeViewerSelectionChangedListener(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ISelectionChangedListener doCreateTreeViewerSelectionChangedListener(TreeViewer viewer) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.trees.AbstractTreeControl#doCreateTreeViewerComparator(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ViewerComparator doCreateTreeViewerComparator(TreeViewer viewer) {
		return new TreeViewerComparator(viewer, (ILabelProvider) viewer.getLabelProvider());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.trees.AbstractTreeControl#getAutoExpandLevel()
	 */
	@Override
	protected int getAutoExpandLevel() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.trees.AbstractTreeControl#getContextMenuId()
	 */
	@Override
	protected String getContextMenuId() {
		return IUIConstants.ID_CONTROL_MENUS_BASE + ".menu.processes"; //$NON-NLS-1$;
	}

}
