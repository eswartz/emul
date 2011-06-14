/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.interfaces.preferences;

/**
 * The bundle's preference key identifiers.
 */
public interface IPreferenceKeys {
	/**
	 * Common prefix for all core preference keys
	 */
	public final String PREFIX = "tcf.filesystem.core."; //$NON-NLS-1$

	/**
	 * If set to <code>true</code>, the file system content contribution to the target
	 * explorer details editor will be activated and visible to the user.
	 */
	public final String PREF_FEATURE_ENABLE_EDITOR_CONTENT_CONTRIBUTION = PREFIX + "feature.editor.content.enable"; //$NON-NLS-1$
}
