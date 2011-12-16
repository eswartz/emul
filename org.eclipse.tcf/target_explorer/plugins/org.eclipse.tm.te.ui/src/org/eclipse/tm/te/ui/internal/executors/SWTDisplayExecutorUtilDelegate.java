/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.internal.executors;

import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.te.runtime.concurrent.interfaces.IExecutorUtilDelegate;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtension;

/**
 * SWT display executor utility delegate implementation.
 */
public class SWTDisplayExecutorUtilDelegate extends ExecutableExtension implements IExecutorUtilDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.concurrent.interfaces.IExecutorUtilDelegate#isHandledExecutorThread()
	 */
	@Override
	public boolean isHandledExecutorThread() {
		return Display.getCurrent() != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.concurrent.interfaces.IExecutorUtilDelegate#readAndDispatch()
	 */
	@Override
	public boolean readAndDispatch() {
		return Display.getCurrent().readAndDispatch();
	}
}
