/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.ui.internal.handler;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.te.runtime.callback.Callback;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;
import org.eclipse.tm.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.processes.core.interfaces.launcher.IProcessLauncher;
import org.eclipse.tm.te.tcf.processes.core.launcher.ProcessLauncher;
import org.eclipse.tm.te.tcf.processes.ui.internal.dialogs.LaunchObjectDialog;
import org.eclipse.tm.te.tcf.processes.ui.internal.help.IContextHelpIds;
import org.eclipse.tm.te.tcf.processes.ui.nls.Messages;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch a process on the selected peer.
 */
public class LaunchProcessesCommandHandler extends AbstractChannelCommandHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.processes.ui.internal.handler.AbstractChannelCommandHandler#execute(org.eclipse.core.commands.ExecutionEvent, org.eclipse.tm.tcf.protocol.IChannel, org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel, org.eclipse.tm.te.tcf.processes.ui.internal.handler.AbstractChannelCommandHandler.DoneExecute)
	 */
	@Override
	protected void execute(final ExecutionEvent event, final IChannel channel, final IPeerModel node, final DoneExecute callback) {
		Assert.isNotNull(event);
		Assert.isNotNull(channel);
		Assert.isNotNull(node);
		Assert.isNotNull(callback);

		// Get the shell
		Shell shell = HandlerUtil.getActiveShell(event);
		// Get the parent editor part
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		IEditorPart editorPart = activePart != null ? (IEditorPart)activePart.getAdapter(IEditorPart.class) : null;
		// Open the dialog
		LaunchObjectDialog dialog = doCreateDialog(editorPart, shell);
		if (dialog.open() == Window.OK) {
			// Get the new launch attributes
			Map<String, Object> launchAttributes = dialog.getLaunchAttributes();
			if (launchAttributes != null) {
				// Construct the launcher object
				ProcessLauncher launcher = new ProcessLauncher();

				// Add some additional options
				launchAttributes.put(IProcessLauncher.PROP_PROCESS_ASSOCIATE_CONSOLE, Boolean.TRUE);

				// Launch the process
				IPropertiesContainer container = new PropertiesContainer();
				container.setProperties(launchAttributes);
				launcher.launch(channel.getRemotePeer(), container, new Callback() {
					/* (non-Javadoc)
					 * @see org.eclipse.tm.te.runtime.callback.Callback#internalDone(java.lang.Object, org.eclipse.core.runtime.IStatus)
					 */
					@Override
					protected void internalDone(Object caller, IStatus status) {
						if (!status.isOK() && status.getSeverity() != IStatus.CANCEL) {
							// Launch failed, pass on to the user
							IStatusHandler[] handler = StatusHandlerManager.getInstance().getHandler(LaunchProcessesCommandHandler.class);
							if (handler != null && handler.length > 0) {
								IPropertiesContainer data = new PropertiesContainer();
								data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, Messages.LaunchProcessesCommandHandler_error_title);
								data.setProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID, IContextHelpIds.LAUNCH_PROCESS_ERROR_DIALOG);
								data.setProperty(IStatusHandlerConstants.PROPERTY_CALLER, caller != null ? caller : LaunchProcessesCommandHandler.this);

								// Take the first status handler in the list
								handler[0].handleStatus(status, data, null);
							}
						}
						// Invoke the outer callback
						callback.doneExecute(Status.OK_STATUS, null);
					}
				});
				// Callback will be invoked once the launch is done
				return;
			}
		}
		// Invoke the outer callback
		callback.doneExecute(Status.OK_STATUS, null);
	}

	/**
	 * Create the dialog object.
	 *
	 * @param part The active editor part or <code>null</code>.
	 * @param shell The shell or <code>null</code>.
	 * @return The dialog.
	 */
	protected LaunchObjectDialog doCreateDialog(IEditorPart part, Shell shell) {
		return new LaunchObjectDialog(part, shell);
	}
}
