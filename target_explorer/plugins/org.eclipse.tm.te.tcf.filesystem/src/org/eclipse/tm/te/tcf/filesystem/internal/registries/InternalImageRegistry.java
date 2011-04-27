/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.registries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.tm.te.ui.images.AbstractImageRegistry;


/**
 * Target Explorer: File System UI Plug-in image registry.
 */
public class InternalImageRegistry extends AbstractImageRegistry {
	private static List<Object[]> fStore = new ArrayList<Object[]>();

	// declare all keys down here
	public static final String OBJ_RootDrive = declareLocalImage("obj16", "rootdrive.gif"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final String OBJ_RootDriveOpen = declareLocalImage("obj16", "rootdriveopen.gif"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final String OBJ_Folder = declareLocalImage("obj16", "folder.gif"); //$NON-NLS-1$ //$NON-NLS-2$

	// external eclipse icons not reachable using ISharedImages

	/**
	 * Constructor.
	 *
	 * @param plugin The plugin descriptor the image registry is created for.
	 */
	public InternalImageRegistry(Plugin plugin) {
		super(plugin);
	}

	/**
	 * Initialize image registry with all keys known yet.
	 */
	public void initialize() {
		for (Iterator<Object[]> iter = fStore.iterator(); iter.hasNext();) {
			Object[] element = iter.next();
			if (element.length == 3) {
				localImage((String)element[0], (String)element[1], (String)element[2]);
			}
			else if (element.length > 3){
				externalImage((String)element[0], (String)element[1], (String[])element[2], (String)element[3]);
			}
		}
	}

	/**
	 * Declare a locally stored image to the image registry.
	 */
	static String declareLocalImage(String dir, String name) {
		List<Object> registryObject = new ArrayList<Object>();
		registryObject.add(dir);
		registryObject.add(name);
		String key = name + "_" + registryObject.hashCode(); //$NON-NLS-1$
		registryObject.add(0, key);
		fStore.add(registryObject.toArray());
		return key;
	}

	/**
	 * Declare a externally stored image to the image registry.
	 */
	static String declareExternalImage(String plugin, String[] dirs, String name) {
		List<Object> registryObject = new ArrayList<Object>();
		registryObject.add(plugin);
		registryObject.add(dirs);
		registryObject.add(name);
		String key = name + "_" + registryObject.hashCode(); //$NON-NLS-1$
		registryObject.add(0, key);
		fStore.add(registryObject.toArray());
		return key;
	}
}
