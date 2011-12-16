/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.handler;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.persistence.interfaces.IPersistenceService;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;
import org.eclipse.tm.te.runtime.services.ServiceManager;
import org.eclipse.tm.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tm.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tm.te.tcf.ui.internal.help.IContextHelpIds;
import org.eclipse.tm.te.tcf.ui.model.Model;
import org.eclipse.tm.te.tcf.ui.nls.Messages;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * TCF static peers delete command handler implementation.
 */
public class DeleteCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				Object candidate = iterator.next();
				if (candidate instanceof IPeerModel && !(candidate.getClass().getSimpleName().equals("RemotePeer"))) { //$NON-NLS-1$
					try {
						IPersistenceService service = ServiceManager.getInstance().getService(IPersistenceService.class);
						if (service == null) throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
						service.delete(candidate);
					} catch (IOException e) {
						// Create the status
						IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
													Messages.DeleteCommandHandler_error_deleteFailed, e);

						// Fill in the status handler custom data
						IPropertiesContainer data = new PropertiesContainer();
						data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, Messages.DeleteCommandHandler_error_title);
						data.setProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID, IContextHelpIds.MESSAGE_DELETE_FAILED);
						data.setProperty(IStatusHandlerConstants.PROPERTY_DONT_ASK_AGAIN_ID, IContextHelpIds.MESSAGE_DELETE_FAILED);
						data.setProperty(IStatusHandlerConstants.PROPERTY_CALLER, this);

						// Get the status handler
						IStatusHandler[] handler = StatusHandlerManager.getInstance().getHandler(candidate);
						if (handler.length > 0) handler[0].handleStatus(status, data, null);
					}

					// Get the locator model
					final ILocatorModel model = Model.getModel();
					if (model != null) {
						// Trigger a refresh of the model
						final ILocatorModelRefreshService service = model.getService(ILocatorModelRefreshService.class);
						if (service != null) {
							Protocol.invokeLater(new Runnable() {
								@Override
								public void run() {
									// Refresh the model now (must be executed within the TCF dispatch thread)
									service.refresh();
								}
							});
						}
					}

				}
			}
		}

		return null;
	}

}
