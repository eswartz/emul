/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.interfaces;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel;

/**
 * Terminal launcher configuration panel.
 */
public interface IConfigurationPanel extends IWizardConfigurationPanel {

	/**
	 * Set the selection to the configuration panel.
	 *
	 * @param selection The selection or <code>null</code>.
	 */
	public void setSelection(ISelection selection);

	/**
	 * Returns the selection associated with the configuration panel.
	 *
	 * @return The selection or <code>null</code>.
	 */
	public ISelection getSelection();
}
