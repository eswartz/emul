/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @since 1.0
 */
public class CoreBundleActivator implements BundleActivator {
	// The bundle context
	private static BundleContext context;

	/**
	 * Returns the bundle context
	 *
	 * @return the bundle context
	 * @since 1.0
	 */
	public static BundleContext getContext() {
		return context;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 * @since 1.0
	 */
	public static String getUniqueIdentifier() {
		if (getContext() != null && getContext().getBundle() != null) {
			return getContext().getBundle().getSymbolicName();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 * @since 1.0
	 */
	public void start(BundleContext bundleContext) throws Exception {
		CoreBundleActivator.context = bundleContext;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 * @since 1.0
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		CoreBundleActivator.context = null;
	}

}
