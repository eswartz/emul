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
package org.eclipse.tcf.internal.target.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.target.core.ITargetManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	private TargetManager targetManager;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		// Register the target manager
		targetManager = new TargetManager();
		context.registerService(ITargetManager.class.getName(), targetManager, null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		targetManager.dispose();
	}

	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> cls) {
		ServiceReference ref = context.getServiceReference(cls.getName());
		return ref != null ? (T)context.getService(ref) : null;
	}
	
	public static void log(IStatus status) {
		getService(ILog.class).log(status);
	}
	
	public static void log(int severity, Throwable e) {
		getService(ILog.class).log(createStatus(severity, e));
	}

	public static IStatus createStatus(int severity, Throwable e) {
		if (e instanceof CoreException)
			return ((CoreException)e).getStatus();
		
		String pluginId = context.getBundle().getSymbolicName();
		return new Status(severity, pluginId, e.getLocalizedMessage(), e);
	}

}
