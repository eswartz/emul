/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.core;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import com.windriver.debug.tcf.core.model.TCFBreakpointsModel;

/**
 * The activator class controls the plug-in life cycle
 */
public class TCFCore extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.windriver.debug.tcf.core";

    // The shared instance
    private static TCFCore plugin;
    private static TCFBreakpointsModel bp_model;

    public TCFCore() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        bp_model = new TCFBreakpointsModel();
        runTCFStartup();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        bp_model.dispose();
        bp_model = null;
        plugin = null;
        super.stop(context);
    }
    
    private void runTCFStartup() {
        try {
            IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "startup");
            IExtension[] extensions = point.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                try {
                    Platform.getBundle(extensions[i].getNamespaceIdentifier()).start();
                    IConfigurationElement[] e = extensions[i].getConfigurationElements();
                    for (int j = 0; j < e.length; j++) {
                        String nm = e[j].getName();
                        if (nm.equals("class")) { //$NON-NLS-1$
                            Class.forName(e[j].getAttribute("name")); //$NON-NLS-1$
                        }
                    }
                }
                catch (Throwable x) {
                    log("TCF startup error", x);
                }
            }
        }
        catch (Exception x) {
            log("TCF startup error", x);
        }
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static TCFCore getDefault() {
        return plugin;
    }
    
    public static TCFBreakpointsModel getBreakpointsModel() {
        return bp_model;
    }

    /**
     * Send error message into Eclipse log.
     * @param msg - error message test
     * @param err - exception
     */
    public static void log(String msg, Throwable err) {
        getDefault().getLog().log(new Status(IStatus.ERROR,
                getDefault().getBundle().getSymbolicName(), IStatus.OK, msg, err));
    }  
}
