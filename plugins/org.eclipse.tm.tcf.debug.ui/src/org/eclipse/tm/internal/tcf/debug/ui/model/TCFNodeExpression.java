/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

public class TCFNodeExpression extends TCFNode implements IElementEditor {

    private final String script;
    private final String field_id;
    private final String var_id;
    private final int index;
    private final TCFDataCache<ISymbols.Symbol> field;
    private final TCFDataCache<String> text;
    private final TCFDataCache<IExpressions.Expression> expression;
    private final TCFDataCache<IExpressions.Value> value;
    private final TCFDataCache<ISymbols.Symbol> type;
    private final TCFChildrenSubExpressions children;
    private int sort_pos;

    private static int expr_cnt;

    TCFNodeExpression(final TCFNode parent, final String script, final String field_id, final String var_id, final int index) {
        super(parent, var_id != null ? var_id : "Expr" + expr_cnt++);
        assert script != null || field_id != null || var_id != null || index >= 0;
        this.script = script;
        this.field_id = field_id;
        this.var_id = var_id;
        this.index = index;
        IChannel channel = model.getLaunch().getChannel();
        field = new TCFDataCache<ISymbols.Symbol>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                ISymbols syms = model.getLaunch().getService(ISymbols.class);
                if (field_id == null || syms == null) {
                    set(null, null, null);
                    return true;
                }
                command = syms.getContext(field_id, new ISymbols.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, ISymbols.Symbol sym) {
                        set(token, error, sym);
                    }
                });
                return false;
            }
        };
        text = new TCFDataCache<String>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (script != null) {
                    set(null, null, script);
                    return true;
                }
                if (var_id != null) {
                    if (!expression.validate()) {
                        expression.wait(this);
                        return false;
                    }
                    if (expression.getData() == null) {
                        set(null, expression.getError(), null);
                        return true;
                    }
                    String e = expression.getData().getExpression();
                    if (e == null) {
                        set(null, new Exception("Missing 'Expression' property"), null);
                        return true;
                    }
                    set(null, null, e);
                    return true;
                }
                TCFNode n = parent;
                while (n instanceof TCFNodeArrayPartition) n = n.parent;
                TCFDataCache<String> t = ((TCFNodeExpression)n).getExpressionText();
                if (!t.validate()) {
                    t.wait(this);
                    return false;
                }
                String e = t.getData();
                if (e == null) {
                    set(null, t.getError(), null);
                    return true;
                }
                if (field_id != null) {
                    if (!field.validate()) {
                        field.wait(this);
                        return false;
                    }
                    if (field.getData() == null) {
                        set(null, field.getError(), null);
                        return true;
                    }
                    String name = field.getData().getName();
                    if (name == null) {
                        set(null, new Exception("Field nas no name"), null);
                        return true;
                    }
                    e = "(" + e + ")." + name;
                }
                else if (index == 0) {
                    e = "*(" + e + ")";
                }
                else if (index > 0) {
                    e = "(" + e + ")[" + index + "]";
                }
                set(null, null, e);
                return true;
            }
        };
        expression = new TCFDataCache<IExpressions.Expression>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                IExpressions exps = model.getLaunch().getService(IExpressions.class);
                if (exps == null) {
                    set(null, null, null);
                    return true;
                }
                if (var_id != null) {
                    command = exps.getContext(var_id, new IExpressions.DoneGetContext() {
                        public void doneGetContext(IToken token, Exception error, IExpressions.Expression context) {
                            set(token, error, context);
                        }
                    });
                }
                else {
                    if (!text.validate()) {
                        text.wait(this);
                        return false;
                    }
                    String e = text.getData();
                    if (e == null) {
                        set(null, text.getError(), null);
                        return true;
                    }
                    TCFNode n = parent;
                    while (n instanceof TCFNodeExpression || n instanceof TCFNodeArrayPartition) n = n.parent;
                    command = exps.create(n.id, null, e, new IExpressions.DoneCreate() {
                        public void doneCreate(IToken token, Exception error, IExpressions.Expression context) {
                            if (isDisposed()) {
                                if (error == null && context != null) {
                                    IExpressions exps = channel.getRemoteService(IExpressions.class);
                                    exps.dispose(context.getID(), new IExpressions.DoneDispose() {
                                        public void doneDispose(IToken token, Exception error) {
                                            if (error == null) return;
                                            if (channel.getState() != IChannel.STATE_OPEN) return;
                                            Activator.log("Error disposing remote expression evaluator", error);
                                        }
                                    });
                                }
                                return;
                            }
                            set(token, error, context);
                        }
                    });
                }
                return false;
            }
        };
        value = new TCFDataCache<IExpressions.Value>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!expression.validate()) {
                    expression.wait(this);
                    return false;
                }
                final IExpressions.Expression ctx = expression.getData();
                if (ctx == null) {
                    set(null, null, null);
                    return true;
                }
                IExpressions exps = model.getLaunch().getService(IExpressions.class);
                command = exps.evaluate(ctx.getID(), new IExpressions.DoneEvaluate() {
                    public void doneEvaluate(IToken token, Exception error, IExpressions.Value value) {
                        set(token, error, value);
                    }
                });
                return false;
            }
        };
        type = new TCFDataCache<ISymbols.Symbol>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!value.validate()) {
                    value.wait(this);
                    return false;
                }
                IExpressions.Value v = value.getData();
                if (v == null) {
                    set(null, null, null);
                    return true;
                }
                String ctx_id = v.getExeContextID();
                String type_id = v.getTypeID();
                if (ctx_id == null || type_id == null) {
                    set(null, null, null);
                    return true;
                }
                TCFDataCache<ISymbols.Symbol> s = model.getSymbolInfoCache(ctx_id, type_id);
                if (!s.validate()) {
                    s.wait(this);
                    return false;
                }
                set(null, s.getError(), s.getData());
                return true;
            }
        };
        children = new TCFChildrenSubExpressions(this, 0, 0, 0);
    }

    @Override
    void dispose() {
        value.reset(null);
        type.reset(null);
        children.reset(null);
        children.dispose();
        super.dispose();
        if (!expression.isValid() || expression.getData() == null) return;
        final IChannel channel = model.getLaunch().getChannel();
        if (channel.getState() != IChannel.STATE_OPEN) return;
        if (var_id != null) return;
        IExpressions exps = channel.getRemoteService(IExpressions.class);
        exps.dispose(expression.getData().getID(), new IExpressions.DoneDispose() {
            public void doneDispose(IToken token, Exception error) {
                if (error == null) return;
                if (channel.getState() != IChannel.STATE_OPEN) return;
                Activator.log("Error disposing remote expression evaluator", error);
            }
        });
    }

    @Override
    void dispose(String id) {
        children.dispose(id);
    }

    void onSuspended() {
        value.reset();
        type.reset();
        children.reset();
        children.onSuspended();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    String getScript() {
        return script;
    }

    String getFieldID() {
        return field_id;
    }

    int getIndex() {
        return index;
    }

    void setSortPosition(int sort_pos) {
        this.sort_pos = sort_pos;
    }

    TCFDataCache<String> getExpressionText() {
        return text;
    }

    TCFDataCache<IExpressions.Value> getValue() {
        return value;
    }

    TCFDataCache<ISymbols.Symbol> getType() {
        return type;
    }

    private BigInteger toBigInteger(byte[] data, int offs, int size, boolean big_endian, boolean sign_extension) {
        assert offs + size <= data.length;
        byte[] temp = null;
        if (sign_extension) {
            temp = new byte[size];
        }
        else {
            temp = new byte[size + 1];
            temp[0] = 0; // Extra byte to avoid sign extension by BigInteger
        }
        if (big_endian) {
            System.arraycopy(data, offs, temp, sign_extension ? 0 : 1, size);
        }
        else {
            for (int i = 0; i < size; i++) {
                temp[temp.length - i - 1] = data[i + offs];
            }
        }
        return new BigInteger(temp);
    }

    private String toNumberString(int radix, ISymbols.Symbol t, byte[] data, int offs, int size, boolean big_endian) {
        String s = null;
        if (data == null) s = "N/A";
        if (s == null && size == 0) s = "";
        if (s == null && radix == 10 && size <= 16) {
            if (t != null) {
                switch (t.getTypeClass()) {
                case integer:
                    s = toBigInteger(data, offs, size, big_endian, true).toString();
                    break;
                case real:
                    switch (t.getSize()) {
                    case 4:
                        s = Float.toString(Float.intBitsToFloat(toBigInteger(
                                data, offs, size, big_endian, true).intValue()));
                        break;
                    case 8:
                        s = Double.toString(Double.longBitsToDouble(toBigInteger(
                                data, offs, size, big_endian, true).longValue()));
                        break;
                    }
                    break;
                }
            }
        }
        if (s == null && size <= 16) {
            s = toBigInteger(data, offs, size, big_endian, false).toString(radix);
            switch (radix) {
            case 8:
                if (!s.startsWith("0")) s = "0" + s;
                break;
            case 16:
                int l = size * 2 - s.length();
                if (l < 0) l = 0;
                if (l > 16) l = 16;
                s = "0000000000000000".substring(0, l) + s;
                break;
            }
        }
        if (s == null) s = "N/A";
        return s;
    }

    private String toNumberString(int radix) {
        String s = null;
        IExpressions.Value val = value.getData();
        if (val != null) {
            byte[] data = val.getValue();
            s = toNumberString(radix, type.getData(), data, 0, data.length, val.isBigEndian());
        }
        if (s == null) s = "...";
        return s;
    }

    private void setLabel(ILabelUpdate result, String name, int col, int radix) {
        String s = toNumberString(radix);
        if (name == null) {
            result.setLabel(s, col);
        }
        else {
            result.setLabel(name + " = " + s, col);
        }
    }

    private void setTypeLabel(ILabelUpdate result, int col) {
        String s = null;
        ISymbols.Symbol t = type.getData();
        if (t != null) {
            s = t.getName();
            if (s == null && t.getSize() == 0) s = "<Void>";
            if (s == null) {
                switch (t.getTypeClass()) {
                case integer:
                    s = "<Integer>";
                    break;
                case cardinal:
                    s = "<Unsigned>";
                    break;
                case real:
                    s = "<Float>";
                    break;
                case pointer:
                    s = "<Pointer>";
                    break;
                case array:
                    s = "<Array>";
                    break;
                case composite:
                    s = "<Structure>";
                    break;
                case function:
                    s = "<Function>";
                    break;
                }
            }
        }
        if (s == null) s = "N/A";
        result.setLabel(s, col);
    }

    @Override
    protected void getData(ILabelUpdate result) {
        result.setImageDescriptor(ImageCache.getImageDescriptor(getImageName()), 0);
        String name = null;
        if (script != null) name = script;
        if (name == null && index >= 0) name = "[" + index + "]";
        if (name == null && field_id != null && field.getData() != null) name = field.getData().getName();
        if (name == null && var_id != null && expression.getData() != null) name = expression.getData().getExpression();
        Throwable error = expression.getError();
        if (error == null) error = value.getError();
        if (error == null) error = type.getError();
        String[] cols = result.getColumnIds();
        if (error != null) {
            if (cols == null || cols.length <= 1) {
                result.setForeground(new RGB(255, 0, 0), 0);
                result.setLabel(name + ": N/A", 0);
            }
            else {
                for (int i = 0; i < cols.length; i++) {
                    String c = cols[i];
                    if (c.equals(TCFColumnPresentationExpression.COL_NAME)) {
                        result.setLabel(name, i);
                    }
                    else if (c.equals(TCFColumnPresentationExpression.COL_TYPE) && type.getError() == null) {
                        setTypeLabel(result, i);
                    }
                    else {
                        result.setForeground(new RGB(255, 0, 0), i);
                        result.setLabel("N/A", i);
                    }
                }
            }
        }
        else {
            if (cols == null) {
                setLabel(result, name, 0, 16);
            }
            else {
                for (int i = 0; i < cols.length; i++) {
                    String c = cols[i];
                    if (c.equals(TCFColumnPresentationExpression.COL_NAME)) {
                        result.setLabel(name, i);
                    }
                    else if (c.equals(TCFColumnPresentationExpression.COL_TYPE)) {
                        setTypeLabel(result, i);
                    }
                    else if (c.equals(TCFColumnPresentationExpression.COL_HEX_VALUE)) {
                        setLabel(result, null, i, 16);
                    }
                    else if (c.equals(TCFColumnPresentationExpression.COL_DEC_VALUE)) {
                        setLabel(result, null, i, 10);
                    }
                }
            }
        }
    }

    private void appendErrorText(StringBuffer bf, Throwable error) {
        if (error == null) return;
        bf.append("Exception: ");
        for (;;) {
            String s = error.getLocalizedMessage();
            if (s == null || s.length() == 0) s = error.getClass().getName();
            bf.append(s);
            if (!s.endsWith("\n")) bf.append('\n');
            Throwable cause = error.getCause();
            if (cause == null) return;
            bf.append("Caused by: ");
            error = cause;
        }
    }

    private boolean appendArrayValueText(StringBuffer bf, int level, ISymbols.Symbol t,
            byte[] data, int offs, int size, boolean big_endian, Runnable done) {
        assert offs + size <= data.length;
        TCFDataCache<ISymbols.Symbol> c = model.getSymbolInfoCache(t.getExeContextID(), t.getBaseTypeID());
        if (!c.validate()) {
            c.wait(done);
            return false;
        }
        ISymbols.Symbol b = c.getData();
        int length = t.getLength();
        if (level == 0) {
            if (size == length) {
                try {
                    bf.append('"');
                    String s = new String(data, offs, size, "ASCII");
                    int l = s.length();
                    String end_q = "\"";
                    if (l > 300) {
                        l = 300;
                        end_q = "...";
                    }
                    for (int i = 0; i < l; i++) {
                        char ch = s.charAt(i);
                        if (ch < ' ') ch = ' ';
                        bf.append(ch);
                    }
                    bf.append(end_q);
                }
                catch (UnsupportedEncodingException e) {
                    Protocol.log("ASCII", e);
                }
                bf.append('\n');
            }
        }
        if (b == null) return true;
        bf.append('[');
        if (length > 0) {
            int elem_size = size / length;
            for (int n = 0; n < length; n++) {
                if (n >= 100) {
                    bf.append("...");
                    break;
                }
                if (n > 0) bf.append(", ");
                if (!appendValueText(bf, level + 1, b, data, offs + n * elem_size, elem_size, big_endian, done)) return false;
            }
        }
        bf.append(']');
        if (level == 0) bf.append('\n');
        return true;
    }

    private boolean appendCompositeValueText(StringBuffer bf, int level, ISymbols.Symbol t,
            byte[] data, int offs, int size, boolean big_endian, Runnable done) {
        TCFDataCache<String[]> c = model.getSymbolChildrenCache(t.getExeContextID(), t.getID());
        if (!c.validate()) {
            c.wait(done);
            return false;
        }
        String[] ids = c.getData();
        if (ids == null) return true;
        bf.append('{');
        for (String id : ids) {
            if (id != ids[0]) bf.append(", ");
            TCFDataCache<ISymbols.Symbol> s = model.getSymbolInfoCache(t.getExeContextID(), id);
            if (!s.validate()) {
                s.wait(done);
                return false;
            }
            ISymbols.Symbol f = s.getData();
            if (f == null || offs + f.getOffset() + f.getSize() > data.length) {
                bf.append('?');
                continue;
            }
            bf.append(f.getName());
            bf.append('=');
            if (!appendValueText(bf, level + 1, f, data, offs + f.getOffset(), f.getSize(), big_endian, done)) return false;
        }
        bf.append('}');
        return true;
    }

    private boolean appendValueText(StringBuffer bf, int level, ISymbols.Symbol t,
            byte[] data, int offs, int size, boolean big_endian, Runnable done) {
        if (data == null) return true;
        switch (t.getTypeClass()) {
        case enumeration:
        case integer:
        case cardinal:
        case real:
            if (level == 0) {
                bf.append("Size: ");
                bf.append(t.getSize());
                bf.append(t.getSize() == 1 ? " byte\n" : " bytes\n");
                if (t.getSize() == 0) break;
                bf.append("Dec: ");
                bf.append(toNumberString(10, t, data, offs, size, big_endian));
                bf.append("\n");
                bf.append("Oct: ");
                bf.append(toNumberString(8, t, data, offs, size, big_endian));
                bf.append("\n");
                bf.append("Hex: ");
                bf.append(toNumberString(16, t, data, offs, size, big_endian));
                bf.append("\n");
            }
            else if (t.getTypeClass() == ISymbols.TypeClass.cardinal) {
                bf.append("0x");
                bf.append(toNumberString(16, t, data, offs, size, big_endian));
            }
            else {
                bf.append(toNumberString(10, t, data, offs, size, big_endian));
            }
            break;
        case pointer:
        case function:
            if (level == 0) {
                bf.append("Oct: ");
                bf.append(toNumberString(8, t, data, offs, size, big_endian));
                bf.append("\n");
                bf.append("Hex: ");
                bf.append(toNumberString(16, t, data, offs, size, big_endian));
                bf.append("\n");
            }
            else {
                bf.append("0x");
                bf.append(toNumberString(16, t, data, offs, size, big_endian));
            }
            break;
        case array:
            if (!appendArrayValueText(bf, level, t, data, offs, size, big_endian, done)) return false;
            break;
        case composite:
            if (!appendCompositeValueText(bf, level, t, data, offs, size, big_endian, done)) return false;
            break;
        default:
            bf.append('?');
            break;
        }
        return true;
    }

    String getDetailText(Runnable done) {
        StringBuffer bf = new StringBuffer();
        appendErrorText(bf, expression.getError());
        appendErrorText(bf, value.getError());
        appendErrorText(bf, type.getError());
        if (bf.length() == 0) {
            IExpressions.Value v = value.getData();
            if (v != null) {
                byte[] data = v.getValue();
                boolean big_endian = v.isBigEndian();
                ISymbols.Symbol t = type.getData();
                if (t != null) {
                    if (!appendValueText(bf, 0, t, data, 0, data.length, big_endian, done)) return null;
                }
                else {
                    bf.append("Hex: ");
                    bf.append(toNumberString(16, t, data, 0, data.length, big_endian));
                    bf.append("\n");
                    bf.append("Value type is not available\n");
                }
            }
        }
        return bf.toString();
    }

    @Override
    protected void getData(IChildrenCountUpdate result) {
        result.setChildCount(children.size());
    }

    @Override
    protected void getData(IChildrenUpdate result) {
        TCFNode[] arr = children.toArray();
        int offset = 0;
        int r_offset = result.getOffset();
        int r_length = result.getLength();
        for (TCFNode n : arr) {
            if (offset >= r_offset && offset < r_offset + r_length) {
                result.setChild(n, offset);
            }
            offset++;
        }
    }

    @Override
    protected void getData(IHasChildrenUpdate result) {
        result.setHasChilren(children.size() > 0);
    }

    @Override
    int getRelevantModelDeltaFlags(IPresentationContext p) {
        if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(p.getId()) ||
                IDebugUIConstants.ID_VARIABLE_VIEW.equals(p.getId())) {
            return super.getRelevantModelDeltaFlags(p);
        }
        return 0;
    }

    @Override
    public boolean validateNode(Runnable done) {
        TCFDataCache<?> pending = null;
        if (!field.validate()) pending = field;
        if (!expression.validate()) pending = expression;
        if (!value.validate()) pending = value;
        if (!type.validate()) pending = type;
        if (!children.validate()) pending = children;
        if (pending == null) return true;
        pending.wait(done);
        return false;
    }

    @Override
    protected String getImageName() {
        return ImageCache.IMG_VARIABLE;
    }

    @Override
    public int compareTo(TCFNode n) {
        TCFNodeExpression e = (TCFNodeExpression)n;
        if (sort_pos < e.sort_pos) return -1;
        if (sort_pos > e.sort_pos) return +1;
        return 0;
    }

    public CellEditor getCellEditor(IPresentationContext context, String column_id, Object element, Composite parent) {
        assert element == this;
        if (TCFColumnPresentationExpression.COL_NAME.equals(column_id) && script != null) {
            return new TextCellEditor(parent);
        }
        if (TCFColumnPresentationExpression.COL_HEX_VALUE.equals(column_id)) {
            return new TextCellEditor(parent);
        }
        if (TCFColumnPresentationExpression.COL_DEC_VALUE.equals(column_id)) {
            return new TextCellEditor(parent);
        }
        return null;
    }

    private static final ICellModifier cell_modifier = new ICellModifier() {

        public boolean canModify(Object element, final String property) {
            final TCFNodeExpression node = (TCFNodeExpression)element;
            return new TCFTask<Boolean>() {
                public void run() {
                    if (TCFColumnPresentationExpression.COL_NAME.equals(property)) {
                        done(node.script != null);
                        return;
                    }
                    if (!node.validateNode(this)) return;
                    if (node.expression.getData() != null && node.expression.getData().canAssign()) {
                        if (TCFColumnPresentationExpression.COL_HEX_VALUE.equals(property)) {
                            done(TCFNumberFormat.isValidHexNumber(node.toNumberString(16)) == null);
                            return;
                        }
                        if (TCFColumnPresentationExpression.COL_DEC_VALUE.equals(property)) {
                            done(TCFNumberFormat.isValidDecNumber(true, node.toNumberString(10)) == null);
                            return;
                        }
                    }
                    done(Boolean.FALSE);
                }
            }.getE();
        }

        public Object getValue(Object element, final String property) {
            final TCFNodeExpression node = (TCFNodeExpression)element;
            return new TCFTask<String>() {
                public void run() {
                    if (TCFColumnPresentationExpression.COL_NAME.equals(property)) {
                        done(node.script);
                        return;
                    }
                    if (!node.validateNode(this)) return;
                    if (node.value.getData() != null) {
                        if (TCFColumnPresentationExpression.COL_HEX_VALUE.equals(property)) {
                            done(node.toNumberString(16));
                            return;
                        }
                        if (TCFColumnPresentationExpression.COL_DEC_VALUE.equals(property)) {
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
            final TCFNodeExpression node = (TCFNodeExpression)element;
            new TCFTask<Boolean>() {
                public void run() {
                    try {
                        if (TCFColumnPresentationExpression.COL_NAME.equals(property)) {
                            if (!node.script.equals(value)) {
                                IExpressionManager m = DebugPlugin.getDefault().getExpressionManager();
                                for (final IExpression e : m.getExpressions()) {
                                    if (node.script.equals(e.getExpressionText())) m.removeExpression(e);
                                }
                                IExpression e = m.newWatchExpression((String)value);
                                m.addExpression(e);
                            }
                            done(Boolean.TRUE);
                            return;
                        }
                        if (!node.validateNode(this)) return;
                        if (node.expression.getData() != null && node.expression.getData().canAssign()) {
                            byte[] bf = null;
                            int size = node.expression.getData().getSize();
                            boolean is_float = false;
                            boolean big_endian = false;
                            boolean signed = false;
                            IExpressions.Value eval = node.value.getData();
                            if (eval != null) {
                                switch(eval.getTypeClass()) {
                                case real:
                                    is_float = true;
                                case integer:
                                    signed = true;
                                    break;
                                }
                                big_endian = eval.isBigEndian();
                                size = eval.getValue().length;
                            }
                            String input = (String)value;
                            String error = null;
                            if (TCFColumnPresentationExpression.COL_HEX_VALUE.equals(property)) {
                                error = TCFNumberFormat.isValidHexNumber(input);
                                if (error == null) bf = TCFNumberFormat.toByteArray(input, 16, false, size, signed, big_endian);
                            }
                            else if (TCFColumnPresentationExpression.COL_DEC_VALUE.equals(property)) {
                                error = TCFNumberFormat.isValidDecNumber(is_float, input);
                                if (error == null) bf = TCFNumberFormat.toByteArray(input, 10, is_float, size, signed, big_endian);
                            }
                            if (error != null) throw new Exception("Invalid value: " + value, new Exception(error));
                            if (bf != null) {
                                IExpressions exps = node.model.getLaunch().getService(IExpressions.class);
                                exps.assign(node.expression.getData().getID(), bf, new IExpressions.DoneAssign() {
                                    public void doneAssign(IToken token, Exception error) {
                                        TCFNodeExpression n = node;
                                        while (n.parent instanceof TCFNodeExpression) n = (TCFNodeExpression)n.parent;
                                        n.onSuspended();
                                        if (error != null) {
                                            node.model.showMessageBox("Cannot modify element value", error);
                                            done(Boolean.FALSE);
                                        }
                                        else {
                                            node.value.reset();
                                            node.addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
                                            node.model.fireModelChanged();
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
                        node.model.showMessageBox("Cannot modify element value", x);
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
