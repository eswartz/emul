/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRegisters;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;


//TODO: hierarchical registers
public class TCFNodeRegister extends TCFNode implements IElementEditor {

    private final TCFDataCache<IRegisters.RegistersContext> context;
    private final TCFDataCache<byte[]> value;

    private byte[] prev_value;
    private byte[] next_value;

    private static final RGB
        rgb_error = new RGB(255, 0, 0),
        rgb_highlight = new RGB(255, 255, 0);

    private int index;

    TCFNodeRegister(TCFNode parent, final String id) {
        super(parent, id);
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
        context.dispose();
        value.dispose();
        super.dispose();
    }

    void setIndex(int index) {
        this.index = index;
    }

    private void appendErrorText(StringBuffer bf, Throwable error) {
        if (error == null) return;
        bf.append("Exception: ");
        bf.append(TCFModel.getErrorMessage(error, true));
    }

    String getDetailText(Runnable done) {
        if (!context.validate(done)) return null;
        if (!value.validate(done)) return null;
        StringBuffer bf = new StringBuffer();
        appendErrorText(bf, context.getError());
        if (bf.length() == 0) {
            appendErrorText(bf, value.getError());
        }
        if (bf.length() == 0) {
            IRegisters.RegistersContext ctx = context.getData();
            if (ctx != null) {
                if (ctx.getDescription() != null) {
                    bf.append(ctx.getDescription());
                    bf.append('\n');
                }
                int l = bf.length();
                if (ctx.isReadable()) {
                    bf.append("readable");
                }
                if (ctx.isReadOnce()) {
                    if (l < bf.length()) bf.append(", ");
                    bf.append("read once");
                }
                if (ctx.isWriteable()) {
                    if (l < bf.length()) bf.append(", ");
                    bf.append("writable");
                }
                if (ctx.isWriteOnce()) {
                    if (l < bf.length()) bf.append(", ");
                    bf.append("write once");
                }
                if (ctx.hasSideEffects()) {
                    if (l < bf.length()) bf.append(", ");
                    bf.append("side effects");
                }
                if (l < bf.length()) bf.append('\n');
            }
            byte[] v = value.getData();
            if (v != null) {
                bf.append("Hex: ");
                bf.append(toNumberString(16));
                bf.append(", ");
                bf.append("Dec: ");
                bf.append(toNumberString(10));
                bf.append(", ");
                bf.append("Oct: ");
                bf.append(toNumberString(8));
                bf.append('\n');
            }
        }
        return bf.toString();
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
        String[] cols = result.getColumnIds();
        if (cols == null) {
            setLabel(result, -1, 16);
        }
        else {
            IRegisters.RegistersContext ctx = context.getData();
            for (int i = 0; i < cols.length; i++) {
                String c = cols[i];
                if (ctx == null) {
                    result.setForeground(rgb_error, i);
                    result.setLabel("N/A", i);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_NAME)) {
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
        boolean changed = false;
        next_value = value.getData();
        if (prev_value != null && next_value != null) {
            if (prev_value.length != next_value.length) {
                changed = true;
            }
            else {
                for (int i = 0; i < prev_value.length; i++) {
                    if (prev_value[i] != next_value[i]) changed = true;
                }
            }
        }
        if (changed) {
            result.setBackground(rgb_highlight, 0);
            if (cols != null) {
                for (int i = 1; i < cols.length; i++) {
                    result.setBackground(rgb_highlight, i);
                }
            }
        }
        result.setImageDescriptor(ImageCache.getImageDescriptor(ImageCache.IMG_REGISTER), 0);
        return true;
    }

    private void setLabel(ILabelUpdate result, int col, int radix) {
        IRegisters.RegistersContext ctx = context.getData();
        Throwable error = context.getError();
        if (error == null) error = value.getError();
        byte[] data = value.getData();
        if (error != null || ctx == null || data == null) {
            result.setForeground(rgb_error, col);
            result.setLabel("N/A", col);
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
        prev_value = next_value;
        value.reset();
        TCFNode n = parent;
        while (n != null) {
            if (n instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)n).onRegisterValueChanged();
            }
            n = n.parent;
        }
        addModelDelta(IModelDelta.STATE);
    }

    void onSuspended() {
        prev_value = next_value;
        value.reset();
        addModelDelta(IModelDelta.STATE);
    }

    void onRegistersChanged() {
        context.reset();
        value.reset();
        addModelDelta(IModelDelta.STATE);
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
                                            node.addModelDelta(IModelDelta.STATE);
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

    @Override
    public int compareTo(TCFNode n) {
        if (n instanceof TCFNodeRegister) {
            TCFNodeRegister r = (TCFNodeRegister)n;
            if (index < r.index) return -1;
            if (index > r.index) return +1;
        }
        return id.compareTo(n.id);
    }
}
