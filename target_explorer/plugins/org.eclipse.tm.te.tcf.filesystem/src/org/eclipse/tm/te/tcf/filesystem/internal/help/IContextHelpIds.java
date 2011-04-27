/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.help;

import org.eclipse.tm.te.tcf.filesystem.activator.UIPlugin;

/**
 * Plugin context help id definitions.
 */
public interface IContextHelpIds {

	/**
	 * Target Explorer file system UI plug-in common context help id prefix.
	 */
	public final static String PREFIX = UIPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$

	/**
	/**
	 * Target Explorer details editor page: File system explorer
	 */
	public final static String FS_EXPLORER_EDITOR_PAGE = PREFIX + "FSExplorerEditorPage"; //$NON-NLS-1$
}
