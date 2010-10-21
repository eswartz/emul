/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.tcf.target.ui"; //$NON-NLS-1$
	
	// Reusable pending string nodes
	public static final String[] PENDING_NODES = new String[] { "pending..." };

	// The shared instance
	private static Activator plugin;
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}
	
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> cls) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference ref = context.getServiceReference(cls.getName());
		return ref != null ? (T)context.getService(ref) : null;
	}
	
	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}
	
	public static void log(int severity, Throwable e) {
		plugin.getLog().log(createStatus(severity, e));
	}

	public static IStatus createStatus(int severity, Throwable e) {
		if (e instanceof CoreException)
			return ((CoreException)e).getStatus();
		
		return new Status(severity, PLUGIN_ID, e.getLocalizedMessage(), e);
	}
	
}
