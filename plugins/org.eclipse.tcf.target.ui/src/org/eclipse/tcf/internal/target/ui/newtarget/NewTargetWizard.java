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
package org.eclipse.tcf.internal.target.ui.newtarget;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tcf.internal.target.ui.Activator;

/**
 * Wizard to create a new target object for the Targets view.
 * 
 * @author Doug Schaefer
 */
public class NewTargetWizard extends Wizard {

	NewTargetPage newTargetPage = new NewTargetPage();
	
	public NewTargetWizard() {
		final String SECTION = "NewTargetWizard";
		IDialogSettings rootSettings = Activator.getDefault().getDialogSettings();
		IDialogSettings mySettings = rootSettings.getSection(SECTION);
		if (mySettings == null)
			mySettings = rootSettings.addNewSection(SECTION);
		setDialogSettings(mySettings);
	}
	
	@Override
	public void addPages() {
		addPage(newTargetPage);
	}
	
	@Override
	public boolean performFinish() {
		newTargetPage.performFinish();
		return true;
	}

}
