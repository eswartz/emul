/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Column presentation for the Modules view.
 */
public class TCFColumnPresentationModules implements IColumnPresentation {

    public static final String PRESENTATION_ID = "Modules";

    /**
     * Presentation column IDs.
     */
    public static final String
        COL_NAME = "Name",
        COL_ADDRESS = "Address",
        COL_SIZE = "Size",
        COL_FLAGS = "Flags",
        COL_OFFSET = "Offset",
        COL_SECTION = "Section";

    private static String[] cols_all = {
        COL_NAME,
        COL_ADDRESS,
        COL_SIZE,
        COL_FLAGS,
        COL_OFFSET,
        COL_SECTION
    };

    private static String[] headers  = {
        "File Name",
        "Address",
        "Size",
        "Flags",
        "Offset",
        "Section"
    };

    private static String[] cols_ini = {
        COL_NAME,
        COL_ADDRESS,
        COL_SIZE
    };

    public void dispose() {
    }

    public String[] getAvailableColumns() {
        return cols_all;
    }

    public String getHeader(String id) {
        for (int i = 0; i < cols_all.length; i++) {
            if (id.equals(cols_all[i])) return headers[i];
        }
        return null;
    }

    public String getId() {
        return PRESENTATION_ID;
    }

    public ImageDescriptor getImageDescriptor(String id) {
        return null;
    }

    public String[] getInitialColumns() {
        return cols_ini;
    }

    public void init(IPresentationContext context) {
    }

    public boolean isOptional() {
        return true;
    }
}
