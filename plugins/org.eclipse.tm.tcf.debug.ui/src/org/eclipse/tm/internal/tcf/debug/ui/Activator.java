/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFAnnotationManager;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModelManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.tm.tcf.debug.ui";

    // The shared instance
    private static Activator plugin;
    private static TCFModelManager model_manager;
    private static TCFAnnotationManager annotation_manager;

    private static final BundleListener bundle_listener = new BundleListener() {
        public void bundleChanged(BundleEvent event) {
            if (plugin != null && event.getBundle() == plugin.getBundle() &&
                    plugin.getBundle().getState() != Bundle.ACTIVE) {
                if (model_manager != null) {
                    model_manager.dispose();
                    model_manager = null;
                }
                if (annotation_manager != null) {
                    annotation_manager.dispose();
                    annotation_manager = null;
                }
            }
        }
    };

    /**
     * The constructor
     */
    public Activator() {
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
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns the shared TCFModelManager instance
     *
     * @return the shared TCFModelManager instance
     */
    public static TCFModelManager getModelManager() {
        if (plugin != null && model_manager == null && plugin.getBundle().getState() == Bundle.ACTIVE) {
            model_manager = new TCFModelManager();
        }
        return model_manager;
    }

    /**
     * Returns the shared TCFAnnotationManager instance
     *
     * @return the shared TCFAnnotationManager instance
     */
    public static TCFAnnotationManager getAnnotationManager() {
        if (plugin != null && annotation_manager == null && plugin.getBundle().getState() == Bundle.ACTIVE) {
            annotation_manager = new TCFAnnotationManager();
        }
        return annotation_manager;
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
