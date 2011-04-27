/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.ui.handlers.HandlerUtil;


/**
 * Target Explorer: TCF refresh command handler.
 */
public class RefreshCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			List<ILocatorModel> refreshedModels = new ArrayList<ILocatorModel>();
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof IAdaptable) {
					final ILocatorModel model = (ILocatorModel)((IAdaptable)element).getAdapter(ILocatorModel.class);
					if (model != null && !refreshedModels.contains(model)) {
						if (Protocol.isDispatchThread()) {
							model.getService(ILocatorModelRefreshService.class).refresh();
						} else {
							Protocol.invokeLater(new Runnable() {
								public void run() {
									model.getService(ILocatorModelRefreshService.class).refresh();
								}
							});
						}
						refreshedModels.add(model);
					}
				}
			}

		}

		return null;
	}

}
