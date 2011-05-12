/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal;

/**
 * Target Explorer: TCF UI Plug-in Image registry constants.
 */
public interface ImageConsts {

	// ***** The directory structure constants *****

	/**
	 * The root directory where to load the images from, relative to
	 * the bundle directory.
	 */
    public final static String  IMAGE_DIR_ROOT = "icons/"; //$NON-NLS-1$

    /**
     * The directory where to load disabled local toolbar images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_DLCL = "dlcl16/"; //$NON-NLS-1$

    /**
     * The directory where to load enabled local toolbar images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_ELCL = "elcl16/"; //$NON-NLS-1$

    /**
     * The directory where to load model object images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_OBJ = "obj16/"; //$NON-NLS-1$

    /**
     * The directory where to load object overlay images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_OVR = "ovr16/"; //$NON-NLS-1$

    // ***** The image constants *****

    /**
     * The key to access the base target object image.
     */
    public static final String IMAGE_TARGET = "TargetObject"; //$NON-NLS-1$

    /**
     * The key to access the target object gold overlay image.
     */
    public static final String IMAGE_GOLD_OVR = "GoldOverlay"; //$NON-NLS-1$

    /**
     * The key to access the target object green overlay image.
     */
    public static final String IMAGE_GREEN_OVR = "GreenOverlay"; //$NON-NLS-1$

    /**
     * The key to access the target object grey overlay image.
     */
    public static final String IMAGE_GREY_OVR = "GreyOverlay"; //$NON-NLS-1$

    /**
     * The key to access the target object red overlay image.
     */
    public static final String IMAGE_RED_OVR = "RedOverlay"; //$NON-NLS-1$

    /**
     * The key to access the target object red X overlay image.
     */
    public static final String IMAGE_RED_X_OVR = "RedXOverlay"; //$NON-NLS-1$
}
