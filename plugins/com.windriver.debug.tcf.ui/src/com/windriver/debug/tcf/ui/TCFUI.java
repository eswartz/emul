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
package com.windriver.debug.tcf.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import com.windriver.debug.tcf.ui.model.TCFModelManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class TCFUI extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.windriver.debug.tcf.ui";

    // The shared instance
    private static TCFUI plugin;
    private static TCFModelManager model_manager;

    private static final BundleListener bundle_listener = new BundleListener() {
        public void bundleChanged(BundleEvent event) {
            if (plugin != null && event.getBundle() == plugin.getBundle() &&
                    plugin.getBundle().getState() != Bundle.ACTIVE && model_manager != null) {
                model_manager.dispose();
                model_manager = null;
            }
        }
    };

    /**
     * The constructor
     */
    public TCFUI() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        context.addBundleListener(bundle_listener);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static TCFUI getDefault() {
        return plugin;
    }

    /**
     * Returns the shared TCFModel instance
     *
     * @return the shared TCFModel instance
     */
    public static TCFModelManager getModelManager() {
        if (plugin != null && model_manager == null && plugin.getBundle().getState() == Bundle.ACTIVE) {
            model_manager = new TCFModelManager();
        }
        return model_manager;
    }
}
