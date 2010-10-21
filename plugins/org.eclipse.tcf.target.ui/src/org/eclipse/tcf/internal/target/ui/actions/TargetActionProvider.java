/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.ui.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

public class TargetActionProvider extends CommonActionProvider {

	private InspectTargetAction inspectTargetAction;
	private AttachDebuggerAction attachDebuggerAction;
	
	private ICommonViewerWorkbenchSite viewSite;
	private boolean contribute = false;

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		if (aSite.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			viewSite = (ICommonViewerWorkbenchSite)aSite.getViewSite();
			inspectTargetAction = new InspectTargetAction(viewSite.getPage());
			attachDebuggerAction = new AttachDebuggerAction();
			contribute = true;
		}
	}
	
	@Override
	public void fillContextMenu(IMenuManager menu) {
		if (!contribute || getContext().getSelection().isEmpty())
			return;
		
		IStructuredSelection selection = (IStructuredSelection)getContext().getSelection();
		
		inspectTargetAction.selectionChanged(selection);
		if (inspectTargetAction.isEnabled())
			menu.insertAfter(ICommonMenuConstants.GROUP_OPEN, inspectTargetAction);
		
		attachDebuggerAction.selectionChanged(selection);
		if (attachDebuggerAction.isEnabled())
			menu.add(attachDebuggerAction);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (!contribute)
			return;
		
		IStructuredSelection selection = (IStructuredSelection)getContext().getSelection();
		if (selection.size() == 1 && selection.getFirstElement() instanceof ITarget) {
			inspectTargetAction.selectionChanged(selection);
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, inspectTargetAction);
		}
	}
	
}
