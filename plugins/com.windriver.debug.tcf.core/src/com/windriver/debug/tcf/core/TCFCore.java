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

import org.eclipse.core.runtime.IStatus;
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
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        bp_model.dispose();
        bp_model = null;
        plugin = null;
        super.stop(context);
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
        if (plugin == null || plugin.getLog() == null) {
            err.printStackTrace();
        }
        else {
            plugin.getLog().log(new Status(IStatus.ERROR,
                    plugin.getBundle().getSymbolicName(), IStatus.OK, msg, err));
        }
    }  
}
