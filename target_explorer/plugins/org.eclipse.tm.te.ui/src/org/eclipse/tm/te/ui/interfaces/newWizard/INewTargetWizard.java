/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.interfaces.newWizard;

import org.eclipse.tm.te.ui.wizards.newWizard.NewWizardSelectionPage;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * Target Explorer: Interface to be implemented optionally by new target wizards.
 */
public interface INewTargetWizard {

	/**
	 * Called by the {@link NewWizardSelectionPage} to associate the wizard
	 * instance with the wizard descriptor the wizard got created from.
	 * <p>
	 * This method is called just before
	 * {@link #init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)}
	 * is called.
	 *
	 * @param descriptor
	 */
	public void setWizardDescriptor(IWizardDescriptor descriptor);
}
