/*******************************************************************************
 * Copyright (c) 2009 Nokia Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ed Swartz (Nokia) - initial API and implementation
 *******************************************************************************/

package org.ejs.gui.common;

import java.io.File;

import org.eclipse.swt.widgets.DirectoryDialog;

/**
 * This helper works around some OS issues with directory
 * dialogs.
 * 
 * (1) It provides support for properly initializing the "filter path"
 * for a dialog when the selected directory no longer exists.
 * We choose the nearest enclosing existing directory.  This avoids
 * the pretty common OS behavior of defaulting to $HOME.
 * @author eswartz
 *
 */
public final class DirectoryDialogHelper {
	protected DirectoryDialogHelper() { }
	
	/**
	 * Configure a directory dialog to set the filter path to the 
	 * nearest existing directory.
	 * @param dialog
	 * @param path the path to start from (file or directory), or <code>null</code>
	 * @see DirectoryDialog#setFilterPath(String)
	 */
	public static void setFilterPathToExistingDirectory(DirectoryDialog dialog,
			String path) {
		if (path == null)
			return;
		
		File dir = new File(path);
		if (dir.isFile()) {
			dir = dir.getParentFile();
		}
		
		while (dir != null && !dir.exists()) {
			dir = dir.getParentFile();
		}
		
		if (dir != null) {
			dialog.setFilterPath(dir.getAbsolutePath());
		}
	}
}
