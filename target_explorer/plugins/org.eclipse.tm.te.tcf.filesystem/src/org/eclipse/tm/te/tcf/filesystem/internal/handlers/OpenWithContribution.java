/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River)	[360494]Provide an "Open With" action in the pop 
 * 								up menu of file system nodes of Target Explorer.
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.handlers;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * The dynamic contribution of "Open With" submenu items.
 */
public class OpenWithContribution extends CompoundContributionItem implements IWorkbenchContribution {
	// Service locator to located the handler service.
	private IServiceLocator serviceLocator;

	/**
	 * Create the contribution instance.
	 */
	public OpenWithContribution() {
	}

	/**
	 * Create the contribution instance with the specified id.
	 */
	public OpenWithContribution(String id) {
		super(id);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.menus.IWorkbenchContribution#initialize(org.eclipse.ui.services.IServiceLocator
	 * )
	 */
	@Override
	public void initialize(IServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	@Override
	protected IContributionItem[] getContributionItems() {
		// Get the selected node.
		IHandlerService service = (IHandlerService) this.serviceLocator
		                .getService(IHandlerService.class);
		IEvaluationContext state = service.getCurrentState();
		ISelection selection = (ISelection) state
		                .getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		IStructuredSelection iss = (IStructuredSelection) selection;
		Object obj = iss.getFirstElement();
		Assert.isTrue(obj instanceof FSTreeNode);
		FSTreeNode node = (FSTreeNode) obj;
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		return new IContributionItem[] { new OpenWithMenu(page, node) };
	}
}
