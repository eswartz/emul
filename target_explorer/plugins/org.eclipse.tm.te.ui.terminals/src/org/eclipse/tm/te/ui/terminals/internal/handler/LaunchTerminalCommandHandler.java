/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.ui.terminals.interfaces.ILauncherDelegate;
import org.eclipse.tm.te.ui.terminals.internal.dialogs.LaunchTerminalSettingsDialog;
import org.eclipse.tm.te.ui.terminals.launcher.LauncherDelegateManager;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch terminal command handler implementation.
 */
public class LaunchTerminalCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the active shell
		Shell shell = HandlerUtil.getActiveShell(event);
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		// Create the launch terminal settings dialog
		LaunchTerminalSettingsDialog dialog = new LaunchTerminalSettingsDialog(shell);
		dialog.setSelection(selection);
		if (dialog.open() == Window.OK) {
			// Get the terminal settings from the dialog
			IPropertiesContainer settings = dialog.getSettings();
			if (settings != null) {
				String delegateId = settings.getStringProperty("delegateId"); //$NON-NLS-1$
				Assert.isNotNull(delegateId);
				ILauncherDelegate delegate = LauncherDelegateManager.getInstance().getLauncherDelegate(delegateId, false);
				Assert.isNotNull(delegateId);
				delegate.execute(settings, null);
			}
		}

		return null;
	}

}
