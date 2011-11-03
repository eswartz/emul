/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.interfaces;

/**
 * Image registry constants.
 */
public interface ImageConsts {

	// ***** The directory structure constants *****

	/**
	 * The root directory where to load the images from, relative to
	 * the bundle directory.
	 */
    public final static String  IMAGE_DIR_ROOT = "icons/"; //$NON-NLS-1$

    /**
     * The directory where to load view related images from, relative to
     * the image root directory.
     */
    public final static String  IMAGE_DIR_EVIEW = "eview16/"; //$NON-NLS-1$

    /**
     * The directory where to load model object images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_OBJ = "obj16/"; //$NON-NLS-1$

    // ***** The image constants *****

    /**
     * The key to access the Target Explorer editor image.
     */
    public static final String  EDITOR = "TargetExplorerEditor"; //$NON-NLS-1$

    /**
     * The key to access the Target Explorer view image.
     */
    public static final String  VIEW = "TargetExplorerView"; //$NON-NLS-1$

    /**
     * The key to access the Target Explorer working set image.
     */
    public static final String  WORKING_SET = "TargetExplorerWorkingSet"; //$NON-NLS-1$
}
