/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal;

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.te.ui.views.activator.UIPlugin;
import org.eclipse.tm.te.ui.views.interfaces.IRoot;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonActionConstants;


/**
 * Target Explorer View implementation.
 * <p>
 * The view is based on the Eclipse Common Navigator framework.
 */
public class View extends CommonNavigator {
	// The root object instance associated with this view instance
	private final IRoot root;

	/**
	 * Target Explorer root node implementation
	 */
	protected static class Root extends PlatformObject implements IRoot {
		/**
		 * Constructor.
		 */
		public Root() {
		}
	}

	/**
	 * Constructor.
	 */
	public View() {
		root = new Root();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonNavigator#getInitialInput()
	 */
	@Override
	protected Object getInitialInput() {
		return root;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonNavigator#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// Add the additional custom Target Explorer toolbar groups
		addCustomToolbarGroups();
	}

	/**
	 * Adds the custom Target Explorer toolbar groups to the view toolbar.
	 */
	protected void addCustomToolbarGroups() {
		if (getViewSite() != null && getViewSite().getActionBars() != null) {
			IToolBarManager tbManager = getViewSite().getActionBars().getToolBarManager();
			if (tbManager != null) {
				tbManager.insertBefore("FRAME_ACTION_GROUP_ID", new GroupMarker("group.new")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.new", new Separator("group.configure")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.configure", new Separator("group.connect")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.connect", new Separator("group.symbols.rd")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.symbols.rd", new GroupMarker("group.symbols")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.symbols", new Separator("group.refresh")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.refresh", new Separator(IWorkbenchActionConstants.MB_ADDITIONS)); //$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonNavigator#handleDoubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	protected void handleDoubleClick(DoubleClickEvent dblClickEvent) {
		// If an handled and enabled command is registered for the ICommonActionConstants.OPEN
		// retargetable action id, redirect the double click handling to the command handler.
		//
		// Note: The default tree node expansion must be re-implemented in the active handler!
		ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service != null ? service.getCommand(ICommonActionConstants.OPEN) : null;
		if (command != null && command.isDefined() && command.isEnabled()) {
			try {
				ISelection selection = dblClickEvent.getSelection();
				EvaluationContext ctx = new EvaluationContext(null, selection);
				ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
				ctx.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
				ctx.addVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				ctx.addVariable(ISources.ACTIVE_PART_ID_NAME, getViewSite().getId());
				ctx.addVariable(ISources.ACTIVE_PART_NAME, this);
				ctx.addVariable(ISources.ACTIVE_SITE_NAME, getViewSite());
				ctx.addVariable(ISources.ACTIVE_SHELL_NAME, getViewSite().getShell());
				ExecutionEvent event = new ExecutionEvent(command, Collections.EMPTY_MAP, this, ctx);
				command.executeWithChecks(event);
			} catch (Exception e) {
				// If the platform is in debug mode, we print the exception to the log view
				if (Platform.inDebugMode()) {
					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e);
					UIPlugin.getDefault().getLog().log(status);
				}
			}
		} else {
			// Fallback to the default implementation
			super.handleDoubleClick(dblClickEvent);
		}
	}
}
