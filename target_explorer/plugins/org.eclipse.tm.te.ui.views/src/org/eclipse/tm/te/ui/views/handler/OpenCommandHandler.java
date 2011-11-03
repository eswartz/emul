/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tm.te.ui.views.activator.UIPlugin;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * TCF tree elements open command handler implementation.
 */
public class OpenCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// The selection is the Target Explorer tree selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		// The active part is the Target Explorer view instance
		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		if (selection instanceof IStructuredSelection && !selection.isEmpty() && part instanceof CommonNavigator) {
			// If the tree node is expandable, expand or collapse it
			TreeViewer viewer = ((CommonNavigator)part).getCommonViewer();
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (viewer.isExpandable(element)) {
				viewer.setExpandedState(element, !viewer.getExpandedState(element));
			} else {
				// Node is not an expandable node, forward to the properties action.
				ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
				Command command = service != null ? service.getCommand(ActionFactory.PROPERTIES.getCommandId()) : null;
				if (command != null && command.isDefined() && command.isEnabled()) {
					try {
						command.executeWithChecks(event);
					} catch (Exception e) {
						// If the platform is in debug mode, we print the exception to the log view
						if (Platform.inDebugMode()) {
							IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e);
							UIPlugin.getDefault().getLog().log(status);
						}
					}
				}
			}
		}

		return null;
	}

}
