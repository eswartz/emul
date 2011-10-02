/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.concurrent.event;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.te.core.concurrent.util.ExecutorsUtil;
import org.eclipse.tm.te.core.interfaces.events.IEventFireDelegate;
import org.eclipse.tm.te.core.interfaces.events.IEventListener;

/**
 * Abstract notification listener implementation executing the
 * notifications within the shared executor thread.
 */
public abstract class ExecutorThreadNotificationListener extends PlatformObject implements IEventListener, IEventFireDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.events.IEventFireDelegate#fire(java.lang.Runnable)
	 */
	public final void fire(Runnable runnable) {
		// Force notification into the executor thread.
		//
		// Note: The executor thread is not identical with the display thread!
		//       Use ExecutorsUtil.executeInUI(runnable) to execute the runnable
		//       within the display thread.
		ExecutorsUtil.execute(runnable);
	}
}
