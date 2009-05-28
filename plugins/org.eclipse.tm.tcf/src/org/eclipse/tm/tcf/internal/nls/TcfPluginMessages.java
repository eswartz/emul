/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.internal.nls;

import org.eclipse.osgi.util.NLS;

/**
 * TCF plugin externalized strings management.
 */
public class TcfPluginMessages extends NLS {

    // The plug-in resouce bundle name
    private static final String BUNDLE_NAME = "org.eclipse.tm.tcf.internal.tcf.TcfPluginMessages"; //$NON-NLS-1$

    /**
     * Static constructor.
     */
    static {
        // Load message values from bundle file
        NLS.initializeMessages(BUNDLE_NAME, TcfPluginMessages.class);
    }

    // **** Declare externalized string id's down here *****

    public static String Extension_error_missingRequiredAttribute;
    public static String Extension_error_duplicateExtension;
    public static String Extension_error_invalidExtensionPoint;

}
