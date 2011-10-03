/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;

/**
 * Filter implementation filtering unreachable peers.
 */
public class UnreachablePeersFilter extends ViewerFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {

		// Filter only elements of type IPeerModel
		if (element instanceof IPeerModel) {
			final IPeerModel peerModel = (IPeerModel)element;

			// Determine the current state of the peer model
			final int[] state = new int[1];
			if (Protocol.isDispatchThread()) {
				state[0] = peerModel.getIntProperty(IPeerModelProperties.PROP_STATE);
			} else {
				Protocol.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						state[0] = peerModel.getIntProperty(IPeerModelProperties.PROP_STATE);
					}
				});
			}

			return state[0] != IPeerModelProperties.STATE_NOT_REACHABLE && state[0] != IPeerModelProperties.STATE_ERROR;
		}

		return true;
	}

}
