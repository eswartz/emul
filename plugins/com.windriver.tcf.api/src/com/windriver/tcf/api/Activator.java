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
package com.windriver.tcf.api;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import com.windriver.tcf.api.protocol.Protocol;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.windriver.tcf.api";

    // The shared instance
    private static Activator plugin;

    public Activator() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        Protocol.setEventQueue(new EventQueue());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
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
                    getDefault().getBundle().getSymbolicName(), IStatus.OK, msg, err));
        }
    }  
}
