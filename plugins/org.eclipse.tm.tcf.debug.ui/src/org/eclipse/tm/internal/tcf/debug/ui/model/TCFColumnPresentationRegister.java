/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
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

public class TCFColumnPresentationRegister implements IColumnPresentation {

    public static final String PRESENTATION_ID = "Registers";

    /**
     * Presentation column IDs.
     */
    public static final String
        COL_NAME = "Name",
        COL_HEX_VALUE = "HexValue",
        COL_DEC_VALUE = "DecValue",
        COL_DESCRIPTION = "Description",
        COL_READBLE = "Readable",
        COL_READ_ONCE = "ReadOnce",
        COL_WRITEABLE = "Writeable",
        COL_WRITE_ONCE = "WriteOnce",
        COL_SIDE_EFFECTS = "SideEffects",
        COL_VOLATILE = "Volatile",
        COL_FLOAT = "Float",
        COL_MNEMONIC = "Menimonic";

    private static String[] cols_all = {
        COL_NAME,
        COL_HEX_VALUE,
        COL_DEC_VALUE,
        COL_DESCRIPTION,
        COL_READBLE,
        COL_READ_ONCE,
        COL_WRITEABLE,
        COL_WRITE_ONCE,
        COL_SIDE_EFFECTS,
        COL_VOLATILE,
        COL_FLOAT,
        COL_MNEMONIC
    };

    private static String[] headers  = {
        "Name",
        "Hex",
        "Decimal",
        "Description",
        "Readable",
        "Read Once",
        "Writable",
        "Write Once",
        "Side Effects",
        "Volatile",
        "Float",
        "Mnemonic"
    };

    private static String[] cols_ini = {
        COL_NAME,
        COL_HEX_VALUE,
        COL_DEC_VALUE,
        COL_DESCRIPTION,
        COL_MNEMONIC
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
        return false;
    }
}
