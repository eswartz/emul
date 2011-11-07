/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.te.ui.views.interfaces.IRoot;

/**
 * Target Explorer view root node implementation
 */
public class ViewRoot extends PlatformObject implements IRoot {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static ViewRoot instance = new ViewRoot();
	}

	/**
	 * Returns the singleton view root instance.
	 */
	public static ViewRoot getInstance() {
		return LazyInstance.instance;
	}

	/**
	 * Constructor.
	 */
	/* default */ ViewRoot() {
	}
}