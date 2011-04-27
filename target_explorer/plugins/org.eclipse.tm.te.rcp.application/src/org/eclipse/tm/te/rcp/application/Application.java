/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.rcp.application;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Target Explorer, RCP: Application implementation.
 *
 * @author uwe.stieber@windriver.com
 */
public class Application implements IApplication {

    private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

    /**
     * A special return code that will be recognized by the launcher and used to
     * restart the application.
     */
    private static final Integer EXIT_RELAUNCH = new Integer(24);

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
        Display display = createDisplay();

        try {
            // create the workbench with this advisor and run it until it exits
            // N.B. createWorkbench remembers the advisor, and also registers
            // the workbench globally so that all UI plug-ins can find it using
            // PlatformUI.getWorkbench() or AbstractUIPlugin.getWorkbench()
            int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());

            // the workbench doesn't support relaunch yet (bug 61809) so
            // for now restart is used, and exit data properties are checked
            // here to substitute in the relaunch return code if needed
            if (returnCode != PlatformUI.RETURN_RESTART) {
				return EXIT_OK;
			}

            // if the exit code property has been set to the relaunch code, then
            // return that code now, otherwise this is a normal restart
            return EXIT_RELAUNCH.equals(Integer.getInteger(PROP_EXIT_CODE)) ? EXIT_RELAUNCH : EXIT_RESTART;
        } finally {
            if (display != null) {
				display.dispose();
			}
            Location instanceLoc = Platform.getInstanceLocation();
            if (instanceLoc != null)
            	instanceLoc.release();
        }
	}

    /**
     * Creates the display used by the application.
     *
     * @return the display used by the application
     */
    protected Display createDisplay() {
        return PlatformUI.createDisplay();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) return;

		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}

}
