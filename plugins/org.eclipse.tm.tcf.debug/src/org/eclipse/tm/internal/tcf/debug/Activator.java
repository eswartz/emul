/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLocalAgent;
import org.eclipse.tm.internal.tcf.debug.launch.TCFUserDefPeer;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpointsModel;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.tm.tcf.debug";

    // The shared instance
    private static Activator plugin;
    private static TCFBreakpointsModel bp_model;

    public Activator() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        bp_model = new TCFBreakpointsModel();
        Protocol.invokeLater(new Runnable() {

            public void run() {
                TCFUserDefPeer.loadPeers();
            }
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        bp_model.dispose();
        bp_model = null;
        TCFLocalAgent.destroy();
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
            System.err.println(msg);
            if (err != null) err.printStackTrace();
        }
        else {
            plugin.getLog().log(new Status(IStatus.ERROR,
                    plugin.getBundle().getSymbolicName(), IStatus.OK, msg, err));
        }
    }
}
