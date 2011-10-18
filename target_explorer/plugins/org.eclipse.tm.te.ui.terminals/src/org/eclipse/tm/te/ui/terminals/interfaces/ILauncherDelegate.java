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

import org.eclipse.core.expressions.Expression;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel;

/**
 * Terminal launcher delegate.
 */
public interface ILauncherDelegate extends IExecutableExtension {

	/**
	 * Returns the enablement expression.
	 *
	 * @return The enablement expression or <code>null</code>.
	 */
	public Expression getEnablement();

	/**
	 * Returns the configuration panel instance to present to the
	 * user. The instance must be always the same on subsequent calls
	 * until disposed.
	 *
	 * @param parentControl The parent control. Must not be <code>null</code>.
	 * @return The configuration panel instance.
	 */
	public IWizardConfigurationPanel getPanel(BaseDialogPageControl parentControl);

	/**
	 * Execute the terminal launch.
	 *
	 * @param properties The properties. Must not be <code>null</code>.
	 * @param callback The callback or <code>null</code>.
	 */
	public void execute(IPropertiesContainer properties, ICallback callback);
}
