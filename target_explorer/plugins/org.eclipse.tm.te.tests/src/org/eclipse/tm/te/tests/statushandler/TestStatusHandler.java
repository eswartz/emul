/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tests.statushandler;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tm.te.core.statushandler.AbstractStatusHandler;

/**
 * Target Explorer: Test status handler implementation.
 */
public class TestStatusHandler extends AbstractStatusHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.statushandler.interfaces.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, org.eclipse.tm.te.core.statushandler.interfaces.IStatusHandler.DoneHandleStatus)
	 */
	public void handleStatus(IStatus status, DoneHandleStatus done) {
	}
}
