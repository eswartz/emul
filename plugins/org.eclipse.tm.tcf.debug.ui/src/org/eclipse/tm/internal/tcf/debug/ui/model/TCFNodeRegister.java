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

import java.math.BigInteger;
import java.util.Arrays;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRegisters;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;


//TODO: hierarchical registers
public class TCFNodeRegister extends TCFNode implements IElementEditor {

    private final TCFDataCache<IRegisters.RegistersContext> context;
    private final TCFDataCache<byte[]> value;

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
        value = new TCFDataCache<byte[]>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!context.validate(this)) return false;
                IRegisters.RegistersContext ctx = context.getData();
                if (ctx == null) {
                    set(null, null, null);
                    return true;
                }
                command = ctx.get(new IRegisters.DoneGet() {
                    public void doneGet(IToken token, Exception error, byte[] value) {
                        set(token, error, value);
                    }
                });
                return false;
            }
        };
    }

    @Override
    public void dispose() {
        context.reset(null);
        value.reset(null);
        super.dispose();
    }

    @Override
    protected boolean getData(ILabelUpdate result, Runnable done) {
        TCFDataCache<?> pending = null;
        if (!context.validate()) pending = context;
        if (!value.validate()) pending = value;
        if (pending != null) {
            pending.wait(done);
            return false;
        }
        IRegisters.RegistersContext ctx = context.getData();
        Throwable error = context.getError();
        if (error != null) {
            result.setForeground(new RGB(255, 0, 0), 0);
            result.setLabel(id + ": " + error.getClass().getName() + ": " + error.getMessage(), 0);
        }
        else if (ctx != null) {
            String[] cols = result.getColumnIds();
            if (cols == null) {
                setLabel(result, -1, 16);
            }
            else {
                for (int i = 0; i < cols.length; i++) {
                    String c = cols[i];
                    if (c.equals(TCFColumnPresentationRegister.COL_NAME)) {
                        result.setLabel(ctx.getName(), i);
                    }
                    else if (c.equals(TCFColumnPresentationRegister.COL_HEX_VALUE)) {
                        setLabel(result, i, 16);
                    }
                    else if (c.equals(TCFColumnPresentationRegister.COL_DEC_VALUE)) {
                        setLabel(result, i, 10);
                    }
                    else if (c.equals(TCFColumnPresentationRegister.COL_DESCRIPTION)) {
                        result.setLabel(ctx.getDescription(), i);
                    }
                    else if (c.equals(TCFColumnPresentationRegister.COL_READBLE)) {
                        result.setLabel(bool(ctx.isReadable()), i);
                    }
                    else if (c.equals(TCFColumnPresentationRegister.COL_READ_ONCE)) {
                        result.setLabel(bool(ctx.isReadOnce()), i);
                    }
                    else if (c.equals(TCFColumnPresentationRegister.COL_WRITEABLE)) {
                        result.setLabel(bool(ctx.isWriteable()), i);
                    }
                    else if (c.equals(TCFColumnPresentationRegister.COL_WRITE_ONCE)) {
                        result.setLabel(bool(ctx.isWriteOnce()), i);
                    }
                    else if (c.equals(TCFColumnPresentationRegister.COL_SIDE_EFFECTS)) {
                        result.setLabel(bool(ctx.hasSideEffects()), i);
                    }
                    else if (c.equals(TCFColumnPresentationRegister.COL_VOLATILE)) {
                        result.setLabel(bool(ctx.isVolatile()), i);
                    }
                    else if (c.equals(TCFColumnPresentationRegister.COL_FLOAT)) {
                        result.setLabel(bool(ctx.isFloat()), i);
                    }
                    else if (c.equals(TCFColumnPresentationRegister.COL_MNEMONIC)) {
                        result.setLabel(getMnemonic(ctx), i);
                    }
                }
            }
        }
        else {
            result.setLabel(id, 0);
        }
        result.setImageDescriptor(ImageCache.getImageDescriptor(ImageCache.IMG_REGISTER), 0);
        return true;
    }

    private void setLabel(ILabelUpdate result, int col, int radix) {
        IRegisters.RegistersContext ctx = context.getData();
        Throwable error = value.getError();
        byte[] data = value.getData();
        if (error != null) {
            if (col >= 0) {
                result.setForeground(new RGB(255, 0, 0), col);
                result.setLabel(error.getMessage(), col);
            }
            else {
                result.setLabel(ctx.getName() + ": " + error.getMessage(), 0);
            }
        }
        else if (data != null) {
            String s = toNumberString(radix);
            if (col >= 0) {
                result.setLabel(s, col);
            }
            else {
                result.setLabel(ctx.getName() + " = " + s, 0);
            }
        }
    }

    private String toNumberString(int radix) {
        IRegisters.RegistersContext ctx = context.getData();
        byte[] data = value.getData();
        if (ctx == null || data == null) return "N/A";
        byte[] temp = new byte[data.length + 1];
        temp[0] = 0; // Extra byte to avoid sign extension by BigInteger
        if (ctx.isBigEndian()) {
            System.arraycopy(data, 0, temp, 1, data.length);
        }
        else {
            for (int i = 0; i < data.length; i++) {
                temp[temp.length - i - 1] = data[i];
            }
        }
        String s = new BigInteger(temp).toString(radix);
        switch (radix) {
        case 8:
            if (!s.startsWith("0")) s = "0" + s;
            break;
        case 16:
            int l = data.length * 2 - s.length();
            if (l < 0) l = 0;
            if (l > 16) l = 16;
            s = "0000000000000000".substring(0, l) + s;
            break;
        }
        return s;
    }

    private String bool(boolean b) {
        return b ? "yes" : "no";
    }

    private String getMnemonic(IRegisters.RegistersContext ctx) {
        if (value.getData() != null) {
            IRegisters.NamedValue[] arr = ctx.getNamedValues();
            if (arr != null) {
                for (IRegisters.NamedValue n : arr) {
                    if (Arrays.equals(n.getValue(), value.getData())) return n.getName();
                }
            }
        }
        return "";
    }

    @Override
    int getRelevantModelDeltaFlags(IPresentationContext p) {
        if (IDebugUIConstants.ID_REGISTER_VIEW.equals(p.getId())) {
            return super.getRelevantModelDeltaFlags(p);
        }
        return 0;
    }

    void onValueChanged() {
        onSuspended();
    }

    void onSuspended() {
        value.reset();
        addModelDelta(IModelDelta.STATE);
    }

    void onRegistersChanged() {
        context.reset();
        value.reset();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    public CellEditor getCellEditor(IPresentationContext context, String column_id, Object element, Composite parent) {
        assert element == this;
        if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(column_id)) {
            return new TextCellEditor(parent);
        }
        if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(column_id)) {
            return new TextCellEditor(parent);
        }
        return null;
    }

    private static final ICellModifier cell_modifier = new ICellModifier() {

        public boolean canModify(Object element, final String property) {
            final TCFNodeRegister node = (TCFNodeRegister)element;
            return new TCFTask<Boolean>() {
                public void run() {
                    if (!node.context.validate(this)) return;
                    if (node.context.getData() != null && node.context.getData().isWriteable()) {
                        if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(property)) {
                            done(TCFNumberFormat.isValidHexNumber(node.toNumberString(16)) == null);
                            return;
                        }
                        if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(property)) {
                            done(TCFNumberFormat.isValidDecNumber(true, node.toNumberString(10)) == null);
                            return;
                        }
                    }
                    done(Boolean.FALSE);
                }
            }.getE();
        }

        public Object getValue(Object element, final String property) {
            final TCFNodeRegister node = (TCFNodeRegister)element;
            return new TCFTask<String>() {
                public void run() {
                    if (!node.value.validate(this)) return;
                    if (node.value.getData() != null) {
                        if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(property)) {
                            done(node.toNumberString(16));
                            return;
                        }
                        if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(property)) {
                            done(node.toNumberString(10));
                            return;
                        }
                    }
                    done(null);
                }
            }.getE();
        }

        public void modify(Object element, final String property, final Object value) {
            if (value == null) return;
            final TCFNodeRegister node = (TCFNodeRegister)element;
            new TCFTask<Boolean>() {
                public void run() {
                    try {
                        if (!node.context.validate(this)) return;
                        IRegisters.RegistersContext ctx = node.context.getData();
                        if (ctx != null && ctx.isWriteable()) {
                            byte[] bf = null;
                            boolean is_float = ctx.isFloat();
                            int size = ctx.getSize();
                            boolean big_endian = ctx.isBigEndian();
                            String input = (String)value;
                            String error = null;
                            if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(property)) {
                                error = TCFNumberFormat.isValidHexNumber(input);
                                if (error == null) bf = TCFNumberFormat.toByteArray(input, 16, false, size, false, big_endian);
                            }
                            else if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(property)) {
                                error = TCFNumberFormat.isValidDecNumber(is_float, input);
                                if (error == null) bf = TCFNumberFormat.toByteArray(input, 10, is_float, size, is_float, big_endian);
                            }
                            if (error != null) throw new Exception("Invalid value: " + value, new Exception(error));
                            if (bf != null) {
                                ctx.set(bf, new IRegisters.DoneSet() {
                                    public void doneSet(IToken token, Exception error) {
                                        if (error != null) {
                                            node.model.showMessageBox("Cannot modify register value", error);
                                            done(Boolean.FALSE);
                                        }
                                        else {
                                            node.value.reset();
                                            node.addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
                                            done(Boolean.TRUE);
                                        }
                                    }
                                });
                                return;
                            }
                        }
                        done(Boolean.FALSE);
                    }
                    catch (Throwable x) {
                        node.model.showMessageBox("Cannot modify register value", x);
                        done(Boolean.FALSE);
                    }
                }
            }.getE();
        }
    };

    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        assert element == this;
        return cell_modifier;
    }
}
