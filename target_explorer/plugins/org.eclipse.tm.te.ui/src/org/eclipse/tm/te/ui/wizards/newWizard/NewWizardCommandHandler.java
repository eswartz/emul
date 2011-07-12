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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.tm.te.ui.interfaces.IContextHelpIds;
import org.eclipse.tm.te.ui.wizards.AbstractWizardCommandHandler;

/**
 * Target Explorer: &quot;org.eclipse.tm.te.ui.command.newWizards" default command handler implementation.
 */
public class NewWizardCommandHandler extends AbstractWizardCommandHandler {

    /* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.wizards.AbstractWizardCommandHandler#createWizard()
     */
    @Override
    protected IWizard createWizard() {
		return new NewWizard();
	}

    /* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.wizards.AbstractWizardCommandHandler#getHelpId()
     */
    @Override
    protected String getHelpId() {
    	return IContextHelpIds.NEW_TARGET_WIZARD;
    }
}
