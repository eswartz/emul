package com.windriver.debug.tcf.ui.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

public class TCFColumnPresentationRegister implements IColumnPresentation {
    
    public static final String PRESENTATION_ID = "Registers";
    
    private static String[] cols_all = {
        TCFNodeRegister.COL_NAME,
        TCFNodeRegister.COL_HEX_VALUE,
        TCFNodeRegister.COL_DEC_VALUE,
        TCFNodeRegister.COL_DESCRIPTION,
        TCFNodeRegister.COL_READBLE,
        TCFNodeRegister.COL_READ_ONCE,
        TCFNodeRegister.COL_WRITEABLE,
        TCFNodeRegister.COL_WRITE_ONCE,
        TCFNodeRegister.COL_SIDE_EFFECTS,
        TCFNodeRegister.COL_VOLATILE,
        TCFNodeRegister.COL_FLOAT,
        TCFNodeRegister.COL_MNEMONIC
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
        TCFNodeRegister.COL_NAME,
        TCFNodeRegister.COL_HEX_VALUE,
        TCFNodeRegister.COL_DEC_VALUE,
        TCFNodeRegister.COL_DESCRIPTION,
        TCFNodeRegister.COL_MNEMONIC
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
