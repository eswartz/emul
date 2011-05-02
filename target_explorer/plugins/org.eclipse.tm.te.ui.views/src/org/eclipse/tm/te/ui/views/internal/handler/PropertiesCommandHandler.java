/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.te.ui.views.activator.UIPlugin;
import org.eclipse.tm.te.ui.views.interfaces.IUIConstants;
import org.eclipse.tm.te.ui.views.internal.editor.EditorInput;
import org.eclipse.tm.te.ui.views.internal.nls.Messages;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;


/**
 * Target Explorer: Properties command handler implementation.
 */
public class PropertiesCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the active selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element != null) {
				// Get the currently active workbench window
				IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
				if (window == null) window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null) {
					// Get the active page
					IWorkbenchPage page = window.getActivePage();
					// Create the editor input object
					IEditorInput input = new EditorInput(element);
					// Check for the Target Explorer editor already opened
					IEditorReference[] references = page.findEditors(input, IUIConstants.ID_EDITOR, IWorkbenchPage.MATCH_INPUT);
					if (references.length == 0) {
						try {
							// Opens the Target Explorer properties editor
							page.openEditor(input, IUIConstants.ID_EDITOR);
						} catch (PartInitException e) {
							IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
							                            Messages.PropertiesCommandHandler_error_initPartFailed, e);
							UIPlugin.getDefault().getLog().log(status);
						}
					}
				}
			}
		}

		return null;
	}

}
