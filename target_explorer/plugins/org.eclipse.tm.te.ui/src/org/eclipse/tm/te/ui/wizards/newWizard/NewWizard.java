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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.te.ui.activator.UIPlugin;
import org.eclipse.tm.te.ui.interfaces.ImageConsts;
import org.eclipse.tm.te.ui.nls.Messages;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * The New Target wizard implementation.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public final class NewWizard extends Wizard implements INewWizard {
	// The new target wizard registry to use
	private NewWizardRegistry wizardRegistry;
	// The new wizard selection page instance
	private NewWizardSelectionPage mainPage;

	/**
	 * Constructor.
	 */
	public NewWizard() {
		super();
		setWindowTitle(Messages.NewWizard_dialog_title);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);

		// Initialize the dialog settings for this wizard
		IDialogSettings settings = UIPlugin.getDefault().getDialogSettings();
		String sectionName = this.getClass().getName();
		if (settings.getSection(sectionName) == null) settings.addNewSection(sectionName);
		setDialogSettings(settings.getSection(sectionName));

		wizardRegistry = NewWizardRegistry.getInstance();
		mainPage = new NewWizardSelectionPage(wizardRegistry);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		mainPage = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getDefaultPageImage()
	 */
	@Override
	public Image getDefaultPageImage() {
		return UIPlugin.getImage(ImageConsts.IMAGE_NEW_TARGET_WIZARD);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (mainPage != null) mainPage.init(workbench, selection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		if (mainPage != null) addPage(mainPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.AbstractSystemWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		if (mainPage != null) {
			// Trigger the saving of the widget values of
			// the main wizard selection page
			mainPage.saveWidgetValues();

			// If the finish is invoked directly from the main page,
			// call performFinish() for the selected wizard too.
			if (mainPage.equals(getContainer().getCurrentPage())) {
				if (mainPage.canFinishEarly() && mainPage.getSelectedNode() != null) {
					// Get the selected wizard
					IWizard wizard = mainPage.getSelectedNode().getWizard();
					if (wizard != null) {
						wizard.setContainer(getContainer());
						wizard.performFinish();
					}
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	@Override
	public boolean canFinish() {
		if (mainPage != null && mainPage.equals(getContainer().getCurrentPage())) {
			return mainPage.canFinishEarly();
		}
		return super.canFinish();
	}
}
