/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tests.interfaces;

import org.eclipse.tm.te.tests.activator.UIPlugin;

/**
 * Public test configuration property id's.
 */
public interface IConfigurationProperties {

	/**
	 * If set to <code>true</code>, the test framework will maximize the
	 * Target Explorer tree view before starting the test.
	 * <p>
	 * Default value is <b><code>false</code></b>.
	 */
	public static final String MAXIMIZE_VIEW = UIPlugin.getUniqueIdentifier() + ".maximizeView"; //$NON-NLS-1$

	/**
	 * Set to the perspective id to switch to before starting the test.
	 * <p>
	 * Default value is <b><code>org.eclipse.tm.te.ui.perspective</code></b>.
	 */
	public static final String TARGET_PERSPECTIVE = UIPlugin.getUniqueIdentifier() + ".targetPerspective"; //$NON-NLS-1$
}
