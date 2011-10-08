/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.statushandler;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.statushandler.AbstractStatusHandler;

/**
 * The default status handler implementation.
 * <p>
 * This status handler is returned by the status handler manager if no other
 * status handler can be found for a given context object.
 */
public class DefaultStatusHandler extends AbstractStatusHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandler.DoneHandleStatus)
	 */
	@Override
	public void handleStatus(IStatus status, IPropertiesContainer data, DoneHandleStatus done) {
	}

}
