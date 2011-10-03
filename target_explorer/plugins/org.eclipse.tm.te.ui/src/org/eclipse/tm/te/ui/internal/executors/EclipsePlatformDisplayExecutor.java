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
import org.eclipse.tm.te.runtime.concurrent.interfaces.IExecutor;
import org.eclipse.tm.te.runtime.concurrent.interfaces.INestableExecutor;
import org.eclipse.tm.te.runtime.concurrent.interfaces.ISingleThreadedExecutor;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tm.te.ui.activator.UIPlugin;
import org.eclipse.ui.PlatformUI;

/**
 * Eclipse platform display executor implementation utilizing the platform display.
 */
public class EclipsePlatformDisplayExecutor extends ExecutableExtension implements IExecutor, ISingleThreadedExecutor, INestableExecutor {

	/* (non-Javadoc)
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	@Override
	public void execute(Runnable command) {
		// In case we do have a display, just execute the runnable asynchronously using this display
		if (PlatformUI.isWorkbenchRunning() &&
			PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getDisplay() != null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(command);
		} else {
			// Check if the current thread is the display thread
			Display display = Display.findDisplay(Thread.currentThread());
			// if we got the display for the riverbed dispatch thread, we can execute the
			// original runnable now
			if (display != null) {
				display.asyncExec(command);
			} else {
				// Well, we don't have any display to execute the runnable at.
				// Drop execution and write a trace message if enabled
				UIPlugin.getTraceHandler().trace("DROPPED display command invocation. No display instance found.!", 1, this); //$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.concurrent.interfaces.ISingleThreadedExecutor#isExecutorThread()
	 */
	@Override
	public boolean isExecutorThread() {
		return isExecutorThread(Thread.currentThread());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.concurrent.interfaces.ISingleThreadedExecutor#isExecutorThread(java.lang.Thread)
	 */
	@Override
	public boolean isExecutorThread(Thread thread) {
		if (thread != null) {
			// Find the display for this thread
			Display display = Display.findDisplay(thread);
			if (display != null) {
				return thread.equals(display.getThread());
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.concurrent.interfaces.INestableExecutor#getMaxDepth()
	 */
	@Override
	public int getMaxDepth() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.concurrent.interfaces.INestableExecutor#readAndExecute()
	 */
	@Override
	public boolean readAndExecute() {
		Display display = Display.getCurrent();
		if (display != null) {
			return display.readAndDispatch();
		}
		return false;
	}
}
