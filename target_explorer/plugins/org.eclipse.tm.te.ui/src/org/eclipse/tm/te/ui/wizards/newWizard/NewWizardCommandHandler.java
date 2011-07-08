/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.wizards.newWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.ui.interfaces.IContextHelpIds;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Target Explorer: &quot;org.eclipse.tm.te.ui.command.newWizards" default command handler implementation.
 */
public class NewWizardCommandHandler extends AbstractHandler {
	/**
     * The wizard dialog default width
     */
    private static final int DEFAULT_WIZARD_WIDTH = 500;

    /**
     * The wizard dialog default height
     */
    private static final int DEFAULT_WIZARD_HEIGHT = 500;

	/**
	 * Creates the new target wizard instance.
	 *
	 * @return The new target wizard instance.
	 */
	protected NewWizard createWizard() {
		return new NewWizard();
	}

	/**
	 * Creates the new target wizard dialog instance.
	 *
	 * @param shell The parent shell or <code>null</code>.
	 * @param wizard The new target wizard instance. Must not be <code>null</code>.
	 *
	 * @return The wizard dialog instance.
	 */
	protected WizardDialog createWizardDialog(Shell shell, IWizard wizard) {
		Assert.isNotNull(wizard);
		return new WizardDialog(shell, wizard);
	}

	/**
	 * Returns the current selection casted to {@link IStructuredSelection}.
	 *
	 * @param event The execution event.
	 * @return The current selection casted to {@link IStructuredSelection} or an empty selection.
	 */
	protected IStructuredSelection getCurrentSelection(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		return selection instanceof IStructuredSelection ? (IStructuredSelection)selection : StructuredSelection.EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window != null) {
			// Create the wizard
			NewWizard wizard = createWizard();
			// Initialize the wizard with the current workbench instance
			// and the current selection
			wizard.init(window.getWorkbench(), getCurrentSelection(event));

			// Create and open the wizard dialog
			WizardDialog dialog = createWizardDialog(window.getShell(), wizard);
			dialog.create();
			dialog.getShell().setSize(Math.max(DEFAULT_WIZARD_WIDTH, dialog.getShell().getSize().x), DEFAULT_WIZARD_HEIGHT);

			window.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IContextHelpIds.NEW_TARGET_WIZARD);
			dialog.open();
		}

		return null;
	}

}
