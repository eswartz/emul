/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.wizards;

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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Abstract wizard command handler implementation.
 */
public abstract class AbstractWizardCommandHandler extends AbstractHandler {
	/**
     * The wizard dialog default width
     */
    private static final int DEFAULT_WIZARD_WIDTH = 500;

    /**
     * The wizard dialog default height
     */
    private static final int DEFAULT_WIZARD_HEIGHT = 500;

	/**
	 * Creates the wizard instance.
	 *
	 * @return The wizard instance. Must be never <code>null</code>.
	 */
	protected abstract IWizard createWizard();

	/**
	 * Creates the wizard dialog instance.
	 *
	 * @param shell The parent shell or <code>null</code>.
	 * @param wizard The wizard instance. Must not be <code>null</code>.
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

	/**
	 * Returns the default wizard dialog width.
	 *
	 * @return The default wizard dialog width in pixel.
	 */
	protected int getDefaultWidth() {
		return DEFAULT_WIZARD_WIDTH;
	}

	/**
	 * Returns the default wizard dialog height.
	 *
	 * @return The default wizard dialog height in pixel.
	 */
	protected int getDefaultHeight() {
		return DEFAULT_WIZARD_HEIGHT;
	}

	/**
	 * Returns the help id to be associated with the wizard dialog.
	 *
	 * @return The help id or <code>null</code>.
	 */
	protected abstract String getHelpId();

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window != null) {
			// Create the wizard
			IWizard wizard = createWizard();
			Assert.isNotNull(wizard);

			// If the wizard is a workbench wizard, initialize the wizard
			// with the current workbench instance and the current selection
			if (wizard instanceof IWorkbenchWizard) {
				((IWorkbenchWizard)wizard).init(window.getWorkbench(), getCurrentSelection(event));
			}

			// Create and configure the wizard dialog
			WizardDialog dialog = createWizardDialog(window.getShell(), wizard);
			dialog.create();
			dialog.getShell().setSize(Math.max(getDefaultWidth(), dialog.getShell().getSize().x), getDefaultHeight());

			// Configure the wizard dialog help id
			if (getHelpId() != null) {
				window.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), getHelpId());
			}

			// Open the dialog
			dialog.open();
		}

		return null;
	}


}
