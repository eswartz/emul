/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls.interfaces;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Composite;

/**
 * Target explorer: Interface to deal with the replaceable wizard configuration panels.
 */
public interface IWizardConfigurationPanel extends IMessageProvider {

	/**
	 * Creates the wizard configuration panel UI elements within the given parent composite.
	 * Wizard configuration panels should always create another composite within the given
	 * composite, which is the panel top control. The top control is queried later from the
	 * stack layout to show the different panels if the backend selection is changing.
	 *
	 * @param parent The parent composite to create the UI elements in. Must not be <code>null</code>.
	 */
	public void setupPanel(Composite parent);

	/**
	 * Cleanup all resources the wizard configuration panel might have been created.
	 */
	public void dispose();

	/**
	 * Returns the wizard configuration panels top control, typically a composite control.
	 * This control is requested every time the stack layout is required to set a new top control
	 * because the backend selection had been changed.
	 *
	 * @return The wizard configuration panels top control or <code>null</code> if the configuration panel has been not setup yet.
	 */
	public Composite getControl();

	/**
	 * Validates the control and sets the message text and type so the parent
	 * page or control is able to display validation result informations.
	 * The validation should be done by implementations of {@link WRValidator}!
	 * The default implementation of this method does nothing.
	 *
	 * @return Result of validation.
	 */
	public boolean isValid();

	/**
	 * Restore the widget values plain from the given dialog settings. This method should
	 * not fragment the given dialog settings any further.
	 *
	 * @param settings The dialog settings to restore the widget values from. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix);

	/**
	 * Save the widget values plain to the given dialog settings. This method should
	 * not fragment the given dialog settings any further.
	 *
	 * @param settings The dialog settings to save the widget values to. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix);

	/**
	 * Called to adjust the wizard configuration panels child controls enablement.
	 */
	public void adjustControlEnablement();
}
