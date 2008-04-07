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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IChannel;
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


    private final TCFDataCache<IRegisters.RegistersContext> context;
    private final TCFDataCache<String> hex_value;
    private final TCFDataCache<String> dec_value;

    TCFNodeRegister(TCFNode parent, final String id) {
        super(parent, id);
        IChannel channel = parent.model.getLaunch().getChannel();
        context = new TCFDataCache<IRegisters.RegistersContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                IRegisters regs = model.getLaunch().getService(IRegisters.class);
                command = regs.getContext(id, new IRegisters.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, IRegisters.RegistersContext context) {
                        set(token, error, context);
                    }
                });
                return false;
            }
        };
        hex_value = new TCFDataCache<String>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!context.validate()) {
                    context.wait(this);
                    return false;
                }
                IRegisters.RegistersContext ctx = context.getData();
                if (ctx == null) {
                    set(null, null, null);
                    return true;
                }
                String[] fmts = ctx.getAvailableFormats();
                String fmt = null;
                for (String s : fmts) {
                    if (s.equals(IRegisters.FORMAT_HEX)) fmt = s;
                }
                if (fmt == null) {
                    set(null, null, null);
                    return true;
                }
                command = ctx.get(fmt, new IRegisters.DoneGet() {
                    public void doneGet(IToken token, Exception error, String value) {
                        set(token, error, value);
                    }
                });
                return false;
            }
        };
        dec_value = new TCFDataCache<String>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!context.validate()) {
                    context.wait(this);
                    return false;
                }
                IRegisters.RegistersContext ctx = context.getData();
                if (ctx == null) {
                    set(null, null, null);
                    return true;
                }
                String[] fmts = ctx.getAvailableFormats();
                String fmt = null;
                for (String s : fmts) {
                    if (s.equals(IRegisters.FORMAT_DECIMAL)) fmt = s;
                }
                if (fmt == null) {
                    set(null, null, null);
                    return true;
                }
                command = ctx.get(fmt, new IRegisters.DoneGet() {
                    public void doneGet(IToken token, Exception error, String value) {
                        set(token, error, value);
                    }
                });
                return false;
            }
        };
    }

    @Override
    protected void getData(ILabelUpdate result) {
        result.setImageDescriptor(ImageCache.getImageDescriptor(getImageName()), 0);
        IRegisters.RegistersContext ctx = context.getData();
        Throwable error = context.getError();
        if (error != null) {
            result.setForeground(new RGB(255, 0, 0), 0);
            result.setLabel(id + ": " + error.getClass().getName() + ": " + error.getMessage(), 0);
        }
        else if (ctx != null) {
            String[] cols = result.getColumnIds();
            if (cols == null) {
                result.setLabel(ctx.getName() + " = " + hex_value.getData(), 0);
            }
            else {
                for (int i = 0; i < cols.length; i++) {
                    String c = cols[i];
                    if (c.equals(COL_NAME)) result.setLabel(ctx.getName(), i);
                    else if (c.equals(COL_HEX_VALUE)) setLabel(result, hex_value, i);
                    else if (c.equals(COL_DEC_VALUE)) result.setLabel(dec_value.getData(), i);
                    else if (c.equals(COL_DESCRIPTION)) result.setLabel(ctx.getDescription(), i);
                    else if (c.equals(COL_READBLE)) result.setLabel(bool(ctx.isReadable()), i);
                    else if (c.equals(COL_READ_ONCE)) result.setLabel(bool(ctx.isReadOnce()), i);
                    else if (c.equals(COL_WRITEABLE)) result.setLabel(bool(ctx.isWriteable()), i);
                    else if (c.equals(COL_WRITE_ONCE)) result.setLabel(bool(ctx.isWriteOnce()), i);
                    else if (c.equals(COL_SIDE_EFFECTS)) result.setLabel(bool(ctx.hasSideEffects()), i);
                    else if (c.equals(COL_VOLATILE)) result.setLabel(bool(ctx.isVolatile()), i);
                    else if (c.equals(COL_FLOAT)) result.setLabel(bool(ctx.isFloat()), i);
                    else if (c.equals(COL_MNEMONIC)) result.setLabel(getMnemonic(ctx), i);
                }
            }
        }
        else {
            result.setLabel(id, 0);
        }
    }
    
    private void setLabel(ILabelUpdate result, TCFDataCache<String> data, int pos) {
        Throwable error = data.getError();
        if (error != null) {
            result.setForeground(new RGB(255, 0, 0), pos);
            result.setLabel(error.getClass().getName() + ": " + error.getMessage(), pos);
        }
        else if (data.getData() != null) {
            result.setLabel(data.getData(), pos);
        }
    }

    private String bool(boolean b) {
        return b ? "yes" : "no";
    }

    private String getMnemonic(IRegisters.RegistersContext ctx) {
        if (dec_value.getData() != null) {
            IRegisters.NamedValue[] arr = ctx.getNamedValues();
            if (arr != null) {
                if (ctx.isFloat()) {
                    double v = Double.parseDouble(dec_value.getData());
                    for (IRegisters.NamedValue n : arr) {
                        if (n.getValue().doubleValue() == v) return n.getName();
                    }
                }
                else {
                    long v = Long.parseLong(dec_value.getData());
                    for (IRegisters.NamedValue n : arr) {
                        if (n.getValue().longValue() == v) return n.getName();
                    }
                }
            }
        }
        else if (!ctx.isFloat() && hex_value.getData() != null) {
            IRegisters.NamedValue[] arr = ctx.getNamedValues();
            if (arr != null) {
                long v = Long.parseLong(hex_value.getData(), 16);
                for (IRegisters.NamedValue n : arr) {
                    if (n.getValue().longValue() == v) return n.getName();
                }
            }
        }
        return "";
    }

    int getRelevantModelDeltaFlags(IPresentationContext p) {
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(p.getId())) {
            return IModelDelta.CONTENT | IModelDelta.STATE;
        }
        return 0;
    }

    void onValueChanged() {
        onSuspended();
    }

    /**
     * Invalidate register value only, keep cached register attributes.
     */
    void onSuspended() {
        hex_value.reset();
        dec_value.reset();
        makeModelDelta(IModelDelta.STATE);
    }
    
    void onRegistersChanged() {
        context.reset();
        hex_value.reset();
        dec_value.reset();
        makeModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    @Override
    public void invalidateNode() {
        context.reset();
        hex_value.reset();
        dec_value.reset();
    }

    @Override
    public boolean validateNode(Runnable done) {
        boolean ctx_valid = context.validate();
        boolean dec_valid = dec_value.validate();
        boolean hex_valid = hex_value.validate();
        if (!ctx_valid) {
            if (done != null) context.wait(done);
            return false;
        }
        if (!dec_valid) {
            if (done != null) dec_value.wait(done);
            return false;
        }
        if (!hex_valid) {
            if (done != null) hex_value.wait(done);
            return false;
        }
        return true;
    }

    @Override
    protected String getImageName() {
        return "icons/full/obj16/genericregister_obj.gif";
    }
}
