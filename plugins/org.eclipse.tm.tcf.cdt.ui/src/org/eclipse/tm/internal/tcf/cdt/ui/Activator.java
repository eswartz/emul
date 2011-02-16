/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.eclipse.tm.tcf.cdt.ui";
    private static Activator plugin;
    private static TCFBreakpointStatusListener bp_status_listener;

    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        EvaluationContextManager.startup();
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (bp_status_listener == null) bp_status_listener = new TCFBreakpointStatusListener();
            }
        });
    }

    public void stop(BundleContext context) throws Exception {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                if (bp_status_listener != null) {
                    bp_status_listener.dispose();
                    bp_status_listener = null;
                }
            }
        });
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    public static void log(Throwable e) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    public static IWorkbenchPage getActivePage() {
        IWorkbenchWindow w = getActiveWorkbenchWindow();
        if (w != null) return w.getActivePage();
        return null;
    }

    public static Shell getActiveWorkbenchShell() {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window != null) return window.getShell();
        return null;
    }

    public static void errorDialog(String message, IStatus status) {
        log(status);
        Shell shell = getActiveWorkbenchShell();
        if (shell == null) return;
        ErrorDialog.openError(shell, "Error", message, status);
    }

    public static void errorDialog(String message, Throwable t) {
        log(t);
        Shell shell = getActiveWorkbenchShell();
        if (shell == null) return;
        IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, 1, t.getMessage(), null);
        ErrorDialog.openError(shell, "Error", message, status);
    }
}
