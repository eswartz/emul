/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRegisters;


//TODO: hierarchical registers
public class TCFNodeRegister extends TCFNode {

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


    private IRegisters.RegistersContext context;
    private String hex_value;
    private String dec_value;
    private Number num_value;
    private boolean valid_context;
    private boolean valid_hex_value;
    private boolean valid_dec_value;

    TCFNodeRegister(TCFNode parent, String id) {
        super(parent, id);
    }

    @Override
    protected void getData(ILabelUpdate result) {
        result.setImageDescriptor(getImageDescriptor(getImageName()), 0);
        if (context != null) {
            String[] cols = result.getColumnIds();
            if (cols == null) {
                result.setLabel(context.getName() + " = " + hex_value, 0);
            }
            else {
                for (int i = 0; i < cols.length; i++) {
                    String c = cols[i];
                    if (c.equals(COL_NAME)) result.setLabel(context.getName(), i);
                    else if (c.equals(COL_HEX_VALUE)) result.setLabel(hex_value, i);
                    else if (c.equals(COL_DEC_VALUE)) result.setLabel(dec_value, i);
                    else if (c.equals(COL_DESCRIPTION)) result.setLabel(context.getDescription(), i);
                    else if (c.equals(COL_READBLE)) result.setLabel(bool(context.isReadable()), i);
                    else if (c.equals(COL_READ_ONCE)) result.setLabel(bool(context.isReadOnce()), i);
                    else if (c.equals(COL_WRITEABLE)) result.setLabel(bool(context.isWriteable()), i);
                    else if (c.equals(COL_WRITE_ONCE)) result.setLabel(bool(context.isWriteOnce()), i);
                    else if (c.equals(COL_SIDE_EFFECTS)) result.setLabel(bool(context.hasSideEffects()), i);
                    else if (c.equals(COL_VOLATILE)) result.setLabel(bool(context.isVolatile()), i);
                    else if (c.equals(COL_FLOAT)) result.setLabel(bool(context.isFloat()), i);
                    else if (c.equals(COL_MNEMONIC)) result.setLabel(getMnemonic(), i);
                }
            }
        }
        else {
            result.setLabel(id, 0);
        }
    }

    private String bool(boolean b) {
        return b ? "yes" : "no";
    }

    private String getMnemonic() {
        if (num_value != null) {
            IRegisters.NamedValue[] arr = context.getNamedValues();
            if (arr != null) {
                if (context.isFloat()) {
                    for (IRegisters.NamedValue n : arr) {
                        if (n.getValue().doubleValue() == num_value.doubleValue()) return n.getName();
                    }
                }
                else {
                    for (IRegisters.NamedValue n : arr) {
                        if (n.getValue().longValue() == num_value.longValue()) return n.getName();
                    }
                }
            }
        }
        return "";
    }

    void onValueChanged() {
        onSuspended();
    }

    /**
     * Invalidate register value only, keep cached register attributes.
     */
    void onSuspended() {
        super.invalidateNode();
        valid_hex_value = false;
        valid_dec_value = false;
        hex_value = null;
        dec_value = null;
        num_value = null;
        makeModelDelta(IModelDelta.STATE);
    }

    @Override
    public void invalidateNode() {
        super.invalidateNode();
        valid_context = false;
        valid_hex_value = false;
        valid_dec_value = false;
        hex_value = null;
        dec_value = null;
        num_value = null;
    }

    @Override
    protected boolean validateNodeData() {
        if (!valid_context && !validateRegisterContext()) return false;
        if (!valid_hex_value && !validateRegisterHexValue()) return false;
        if (!valid_dec_value && !validateRegisterDecValue()) return false;
        return true;
    }

    private boolean validateRegisterContext() {
        assert pending_command == null;
        IRegisters regs = model.getLaunch().getService(IRegisters.class);
        pending_command = regs.getContext(id, new IRegisters.DoneGetContext() {
            public void doneGetContext(IToken token, Exception error, IRegisters.RegistersContext context) {
                if (pending_command != token) return;
                pending_command = null;
                if (error != null) {
                    node_error = error;
                }
                else {
                    TCFNodeRegister.this.context = context;
                }
                valid_context = true;
                validateNode();
            }
        });
        return false;
    }

    private boolean validateRegisterHexValue() {
        assert pending_command == null;
        String[] fmts = context.getAvailableFormats();
        String fmt = null;
        for (String s : fmts) {
            if (s.equals(IRegisters.FORMAT_HEX)) fmt = s;
        }
        if (fmt == null) {
            valid_hex_value = true;
            return true;
        }
        pending_command = context.get(fmt, new IRegisters.DoneGet() {
            public void doneGet(IToken token, Exception error, String value) {
                if (pending_command != token) return;
                pending_command = null;
                if (error != null) {
                    node_error = error;
                }
                else {
                    hex_value = value;
                    if (!context.isFloat()) num_value = Long.valueOf(value, 16);
                }
                valid_hex_value = true;
                validateNode();
            }
        });
        return false;
    }

    private boolean validateRegisterDecValue() {
        assert pending_command == null;
        String[] fmts = context.getAvailableFormats();
        String fmt = null;
        for (String s : fmts) {
            if (s.equals(IRegisters.FORMAT_DECIMAL)) fmt = s;
        }
        if (fmt == null) {
            valid_dec_value = true;
            return true;
        }
        pending_command = context.get(fmt, new IRegisters.DoneGet() {
            public void doneGet(IToken token, Exception error, String value) {
                if (pending_command != token) return;
                pending_command = null;
                if (error != null) {
                    node_error = error;
                }
                else {
                    dec_value = value;
                    if (!context.isFloat()) num_value = Long.valueOf(value, 10);
                    else num_value = Double.valueOf(value);
                }
                valid_dec_value = true;
                validateNode();
            }
        });
        return false;
    }

    @Override
    protected String getImageName() {
        return "icons/full/obj16/genericregister_obj.gif";
    }
}
