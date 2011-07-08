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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tm.te.ui.activator.UIPlugin;

/**
 * An abstract wizard implementation.
 * <p>
 * This wizard implementation is adding dialog settings management.
 */
public abstract class AbstractWizard extends Wizard {
	// A marker to remember if the dialog settings got
	// initialized for this wizard
	private boolean dialogSettingsInitialized = false;

	/**
	 * Initialize the dialog settings and associate them with the wizard.
	 */
	private final void initializeDialogSettings() {
		// Get the root dialog settings
		IDialogSettings rootSettings = getRootDialogSettings();
		// Get the wizards dialog settings section
		IDialogSettings section = rootSettings.getSection(getWizardSectionName());
		if (section == null) {
			// The section does not exist -> create it
			section = rootSettings.addNewSection(getWizardSectionName());
		}
		// Push the section to the wizard
		setDialogSettings(section);
		// Mark the dialog settings initialized
		dialogSettingsInitialized = true;
	}

	/**
	 * Returns the root dialog settings.
	 * <p>
	 * Typically, this are the dialog settings of the parent bundle. The
	 * default implementation returns the dialog settings of the bundle
	 * &quot;<code>org.eclipse.tm.te.ui</code>&quot;. Overwrite to return
	 * different root dialog settings.
	 *
	 * @return The root dialog settings.
	 */
	protected IDialogSettings getRootDialogSettings() {
		return UIPlugin.getDefault().getDialogSettings();
	}

	/**
	 * Returns the name of the wizards associated dialog settings
	 * section.
	 * <p>
	 * The default implementation returns the simple name of the
	 * implementation class.
	 *
	 * @return The name of the wizards dialog settings section.
	 */
	protected String getWizardSectionName() {
		return getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getDialogSettings()
	 */
	@Override
	public IDialogSettings getDialogSettings() {
		if (!dialogSettingsInitialized) {
			initializeDialogSettings();
		}
		return super.getDialogSettings();
	}
}
