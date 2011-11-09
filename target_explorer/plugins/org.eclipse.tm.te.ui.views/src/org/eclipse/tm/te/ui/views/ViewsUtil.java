/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views;

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * Utility methods to deal with views.
 */
public class ViewsUtil {

	/**
	 * Returns the workbench part identified by the given id.
	 *
	 * @param id The view id. Must not be <code>null</code>.
	 * @return The workbench part or <code>null</code>.
	 */
	public static IWorkbenchPart getPart(String id) {
		// Check the active workbench window and active page instances
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
			// Get the view reference
			IViewReference reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(id);
			// Return the view part from the reference, but do not restore it
			return reference != null ? reference.getPart(false) : null;
		}
		return null;
	}

	/**
	 * Asynchronously refresh the view identified by the given id.
	 *
	 * @param id The view id. Must not be <code>null</code>.
	 */
	public static void refresh(final String id) {
		Assert.isNotNull(id);

		// Create the runnable
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// Check the active workbench window and active page instances
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
					// Get the view reference
					IViewReference reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(id);
					// Get the view part from the reference, but do not restore it
					IWorkbenchPart part = reference != null ? reference.getPart(false) : null;
					// If the part is a common navigator, get the common viewer
					Viewer viewer = part instanceof CommonNavigator ? ((CommonNavigator)part).getCommonViewer() : null;
					// If not a common navigator, try to adapt to the viewer
					if (viewer == null) viewer = part != null ? (Viewer)part.getAdapter(Viewer.class) : null;
					// Refresh the viewer
					if (viewer != null) viewer.refresh();
				}
			}
		};

		// Execute asynchronously
		if (PlatformUI.isWorkbenchRunning()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
		}
	}

	/**
	 * Asynchronously set the given selection to the view identified by the given id.
	 *
	 * @param id The view id. Must not be <code>null</code>.
	 * @param selection The selection or <code>null</code>.
	 */
	public static void setSelection(final String id, final ISelection selection) {
		Assert.isNotNull(id);

		// Create the runnable
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// Check the active workbench window and active page instances
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
					// Get the view reference
					IViewReference reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(id);
					// Get the view part from the reference, but do not restore it
					IWorkbenchPart part = reference != null ? reference.getPart(false) : null;
					// Get the selection provider
					ISelectionProvider selectionProvider = part != null && part.getSite() != null ? part.getSite().getSelectionProvider() : null;
					// And apply the selection
					if (selectionProvider != null) selectionProvider.setSelection(selection);
				}
			}
		};

		// Execute asynchronously
		if (PlatformUI.isWorkbenchRunning()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
		}
	}

	/**
	 * Opens the properties editor or dialog on the given selection.
	 *
	 * @param selection The selection. Must be not <code>null</code>.
	 */
	public static void openProperties(final ISelection selection) {
		Assert.isNotNull(selection);

		// Create the runnable
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				ICommandService service = (ICommandService)PlatformUI.getWorkbench().getAdapter(ICommandService.class);
				if (service != null) {
					final Command command = service.getCommand("org.eclipse.ui.file.properties"); //$NON-NLS-1$
					if (command != null && command.isDefined()) {
						// Construct the application context
						EvaluationContext context = new EvaluationContext(null, selection);
						// Apply the selection to the "activeMenuSelection" and "selection" variable too
						context.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
						context.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
						context.addVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
						// Construct the execution event
						ExecutionEvent execEvent = new ExecutionEvent(command, Collections.EMPTY_MAP, this, context);
						// And execute the event
						try {
							command.executeWithChecks(execEvent);
						} catch (Exception e) { /* ignored on purpose */ }
					}
				}
			}
		};

		// Execute asynchronously
		if (PlatformUI.isWorkbenchRunning()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
		}
	}
}
