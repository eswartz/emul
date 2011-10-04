/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal;

/**
 * Target Explorer: File system plug-in Image registry constants.
 */
public interface ImageConsts {

	// ***** The directory structure constants *****

	/**
	 * The root directory where to load the images from, relative to
	 * the bundle directory.
	 */
    public final static String  IMAGE_DIR_ROOT = "icons/"; //$NON-NLS-1$

    /**
     * The directory where to load model object images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_OBJ = "obj16/"; //$NON-NLS-1$

    // ***** The image constants *****

    /**
     * The key to access the base folder object image.
     */
    public static final String IMAGE_FOLDER = "Folder"; //$NON-NLS-1$

    /**
     * The key to access the base folder object image.
     */
    public static final String IMAGE_ROOT_DRIVE = "RootDrive"; //$NON-NLS-1$

    /**
     * The key to access the base folder object image.
     */
    public static final String IMAGE_ROOT_DRIVE_OPEN = "RootDriveOpen"; //$NON-NLS-1$
    
    /**
     * The key to access the image of compare editor.
     */
    public static final String IMAGE_COMPARE_EDITOR = "CompareEditor"; //$NON-NLS-1$
}
