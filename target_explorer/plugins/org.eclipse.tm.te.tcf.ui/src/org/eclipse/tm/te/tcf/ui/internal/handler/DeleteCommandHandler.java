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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tm.te.tcf.ui.internal.PeersPersistenceManager;
import org.eclipse.tm.te.tcf.ui.internal.model.Model;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Target Explorer: TCF static peers delete command handler implementation.
 */
public class DeleteCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				Object candidate = iterator.next();
				if (candidate instanceof IPeerModel && !(candidate.getClass().getSimpleName().equals("RemotePeer"))) { //$NON-NLS-1$
					try {
						PeersPersistenceManager.getInstance().delete((IPeerModel)candidate);
					} catch (IOException e) {
						// Ignore it for now, we will have to pass it to the status handler later
					}

					// Get the locator model
					final ILocatorModel model = Model.getModel();
					if (model != null) {
						// Trigger a refresh of the model
						final ILocatorModelRefreshService service = model.getService(ILocatorModelRefreshService.class);
						if (service != null) {
							Protocol.invokeLater(new Runnable() {
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
