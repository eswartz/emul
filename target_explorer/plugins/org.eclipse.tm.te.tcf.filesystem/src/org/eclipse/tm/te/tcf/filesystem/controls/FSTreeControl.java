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

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.part.MultiPageSelectionProvider;


/**
 * File system browser control.
 */
public class FSTreeControl extends AbstractTreeControl implements ISelectionChangedListener, IDoubleClickListener {

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
		viewer.addDoubleClickListener(this);
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
		FSTreeLabelProvider labelProvider = new FSTreeLabelProvider(viewer);
		IWorkbench workbench = PlatformUI.getWorkbench();
		IDecoratorManager manager = workbench.getDecoratorManager();
		ILabelDecorator decorator = manager.getLabelDecorator();
		return new FSTreeDecoratingLabelProvider(labelProvider,decorator);
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

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		// If an handled and enabled command is registered for the ICommonActionConstants.OPEN
		// retargetable action id, redirect the double click handling to the command handler.
		//
		// Note: The default tree node expansion must be re-implemented in the active handler!
		ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service != null ? service.getCommand(ICommonActionConstants.OPEN) : null;
		if (command != null && command.isDefined() && command.isEnabled()) {
			try {
				ISelection selection = event.getSelection();
				EvaluationContext ctx = new EvaluationContext(null, selection);
				ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
				ctx.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
				ctx.addVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				IWorkbenchPart part = getParentPart();
				if (part != null) {
					IWorkbenchPartSite site = part.getSite();
					ctx.addVariable(ISources.ACTIVE_PART_ID_NAME, site.getId());
					ctx.addVariable(ISources.ACTIVE_PART_NAME, part);
					ctx.addVariable(ISources.ACTIVE_SITE_NAME, site);
					ctx.addVariable(ISources.ACTIVE_SHELL_NAME, site.getShell());
				}
				ExecutionEvent executionEvent = new ExecutionEvent(command, Collections.EMPTY_MAP, part, ctx);
				command.executeWithChecks(executionEvent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
