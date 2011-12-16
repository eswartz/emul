/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.swt;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Utility providing convenience methods to use the SWT Display.
 */
public class DisplayUtil {

    /**
	 * Does what Display does in syncExec, but ensures
	 * that the exec is not executed if the display is
	 * currently being disposed or already unset.
	 *
	 * @param exec the Runnable to be executed.
	 *
	 * @see Display#asyncExec(Runnable)
	 * @see Runnable#run()
	 */
	public static void safeSyncExec(Runnable exec){
        try {
            Display display = PlatformUI.getWorkbench().getDisplay();
			display.syncExec(exec);
        }
        catch (Exception e) {
            // if display is disposed, silently ignore.
        }
	}
	/**
	 * Does what Display does in timerExec, but ensures
	 * that the exec is not executed if the display is
	 * currently being disposed or already unset.
	 *
	 * @param exec the Runnable to be executed.
	 * @param millisec time to wait.
	 *
	 * @see Display#timerExec(int, Runnable)
	 * @see Runnable#run()
	 */
	public static void safeTimerExec(Runnable exec, int millisec){
        try {
            Display display = PlatformUI.getWorkbench().getDisplay();
            display.timerExec(millisec,exec);
        }
        catch (Exception e) {
            // if display is disposed, silently ignore.
        }
	}

	/**
	 * Does what Display does in asyncExec, but ensures
	 * that the exec is not executed if the display is
	 * currently being disposed or already unset.
	 *
	 * @param exec The Runnable to be executed.
	 *
	 * @see Display#asyncExec(Runnable)
	 * @see Runnable#run()
	 */
	public static void safeAsyncExec(Runnable exec){
        try {
            Display display = PlatformUI.getWorkbench().getDisplay();
            display.asyncExec(exec);
        }
        catch (Exception e) {
            // if display is disposed, silently ignore.
        }
	}
}
