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
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.services.IMemory.MemoryError;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

public class TCFNodeExpression extends TCFNode implements IElementEditor, ICastToType {

    // TODO: User commands: Add Global Variables, Remove Global Variables
    // TODO: enable Change Value user command

    private final String script;
    private final int index;
    private final boolean deref;
    private final TCFDataCache<ISymbols.Symbol> field;
    private final TCFDataCache<IExpressions.Expression> var_expression;
    private final TCFDataCache<String> text;
    private final TCFDataCache<Expression> expression;
    private final TCFDataCache<IExpressions.Value> value;
    private final TCFDataCache<ISymbols.Symbol> type;
    private final TCFChildrenSubExpressions children;
    private final TCFDataCache<String> type_name;
    private final TCFDataCache<String> string;
    private int sort_pos;
    private IExpressions.Value prev_value;
    private IExpressions.Value next_value;

    private static int expr_cnt;

    private class Expression {

        final IExpressions.Expression expression;
        boolean must_be_disposed;

        Expression(IExpressions.Expression expression) {
            assert expression != null;
            this.expression = expression;
        }

        void dispose() {
            if (!must_be_disposed) return;
            if (channel.getState() == IChannel.STATE_OPEN) {
                IExpressions exps = channel.getRemoteService(IExpressions.class);
                exps.dispose(expression.getID(), new IExpressions.DoneDispose() {
                    public void doneDispose(IToken token, Exception error) {
                        if (error == null) return;
                        if (channel.getState() != IChannel.STATE_OPEN) return;
                        Activator.log("Error disposing remote expression evaluator", error);
                    }
                });
            }
            must_be_disposed = false;
        }
    }

    TCFNodeExpression(final TCFNode parent, final String script,
            final TCFDataCache<ISymbols.Symbol> field, final String var_id,
            final int index, final boolean deref) {
        super(parent, var_id != null ? var_id : "Expr" + expr_cnt++);
        assert script != null || field != null || var_id != null || index >= 0;
        this.script = script;
        this.field = field;
        this.index = index;
        this.deref = deref;
        var_expression = new TCFDataCache<IExpressions.Expression>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                IExpressions exps = model.getLaunch().getService(IExpressions.class);
                if (exps == null || var_id == null) {
                    set(null, null, null);
                    return true;
                }
                command = exps.getContext(var_id, new IExpressions.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, IExpressions.Expression context) {
                        set(token, error, context);
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
                    if (!var_expression.validate(this)) return false;
                    Throwable err = null;
                    String exp = null;
                    if (var_expression.getData() == null) {
                        err = var_expression.getError();
                    }
                    else {
                        exp = var_expression.getData().getExpression();
                        if (exp == null) err = new Exception("Missing 'Expression' property");
                    }
                    set(null, err, exp);
                    return true;
                }
                TCFNode n = parent;
                while (n instanceof TCFNodeArrayPartition) n = n.parent;
                TCFDataCache<String> t = ((TCFNodeExpression)n).getExpressionText();
                if (!t.validate(this)) return false;
                String e = t.getData();
                if (e == null) {
                    set(null, t.getError(), null);
                    return true;
                }
                String cast = model.getCastToType(n.id);
                if (cast != null) e = "(" + cast + ")(" + e + ")";
                if (field != null) {
                    if (!field.validate(this)) return false;
                    if (field.getData() == null) {
                        set(null, field.getError(), null);
                        return true;
                    }
                    String name = field.getData().getName();
                    if (name == null) {
                        set(null, new Exception("Field nas no name"), null);
                        return true;
                    }
                    if (deref) {
                        e = "(" + e + ")->" + name;
                    }
                    else {
                        e = "(" + e + ")." + name;
                    }
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
        expression = new TCFDataCache<Expression>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                IExpressions exps = model.getLaunch().getService(IExpressions.class);
                if (exps == null) {
                    set(null, null, null);
                    return true;
                }
                String cast = model.getCastToType(id);
                if (var_id != null && cast == null) {
                    if (!var_expression.validate(this)) return false;
                    Expression exp = null;
                    if (var_expression.getData() != null) exp = new Expression(var_expression.getData());
                    set(null, var_expression.getError(), exp);
                    return true;
                }
                if (!text.validate(this)) return false;
                String e = text.getData();
                if (e == null) {
                    set(null, text.getError(), null);
                    return true;
                }
                if (cast != null) e = "(" + cast + ")(" + e + ")";
                TCFNode n = parent;
                while (n instanceof TCFNodeExpression || n instanceof TCFNodeArrayPartition) n = n.parent;
                if (n instanceof TCFNodeStackFrame && ((TCFNodeStackFrame)n).isEmulated()) n = n.parent;
                command = exps.create(n.id, null, e, new IExpressions.DoneCreate() {
                    public void doneCreate(IToken token, Exception error, IExpressions.Expression context) {
                        Expression e = null;
                        if (context != null) {
                            e = new Expression(context);
                            e.must_be_disposed = true;
                        }
                        if (!isDisposed()) set(token, error, e);
                        else if (e != null) e.dispose();
                    }
                });
                return false;
            }
            @Override
            public void cancel() {
                if (isValid() && getData() != null) getData().dispose();
                super.cancel();
            }
            @Override
            public void dispose() {
                if (isValid() && getData() != null) getData().dispose();
                super.dispose();
            }
        };
        value = new TCFDataCache<IExpressions.Value>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                if (!expression.validate(this)) return false;
                final Expression exp = expression.getData();
                if (exp == null) {
                    set(null, expression.getError(), null);
                    return true;
                }
                IExpressions exps = model.getLaunch().getService(IExpressions.class);
                command = exps.evaluate(exp.expression.getID(), new IExpressions.DoneEvaluate() {
                    public void doneEvaluate(IToken token, Exception error, IExpressions.Value value) {
                        set(token, error, value);
                    }
                });
                return false;
            }
            public void reset() {
                super.reset();
            }
        };
        type = new TCFDataCache<ISymbols.Symbol>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                String type_id = null;
                if (!value.validate(this)) return false;
                IExpressions.Value val = value.getData();
                if (val != null) type_id = val.getTypeID();
                if (type_id == null) {
                    if (!expression.validate(this)) return false;
                    Expression exp = expression.getData();
                    if (exp != null) type_id = exp.expression.getTypeID();
                }
                if (type_id == null) {
                    set(null, value.getError(), null);
                    return true;
                }
                TCFDataCache<ISymbols.Symbol> type_cache = model.getSymbolInfoCache(type_id);
                if (type_cache == null) {
                    set(null, null, null);
                    return true;
                }
                if (!type_cache.validate(this)) return false;
                set(null, type_cache.getError(), type_cache.getData());
                return true;
            }
        };
        string = new TCFDataCache<String>(channel) {
            IMemory.MemoryContext mem;
            ISymbols.Symbol base_type_data;
            boolean big_endian;
            BigInteger addr;
            byte[] buf;
            int size;
            int offs;
            @Override
            protected boolean startDataRetrieval() {
                if (addr != null && size == 0) {
                    // data is ASCII string
                    if (buf == null) buf = new byte[256];
                    if (offs >= buf.length) {
                        byte[] tmp = new byte[buf.length * 2];
                        System.arraycopy(buf, 0, tmp, 0, buf.length);
                        buf = tmp;
                    }
                    command = mem.get(addr.add(BigInteger.valueOf(offs)), 1, buf, offs, 1, 0, new IMemory.DoneMemory() {
                        public void doneMemory(IToken token, MemoryError error) {
                            if (error != null) {
                                set(command, error, null);
                            }
                            else if (buf[offs] == 0 || offs >= 2048) {
                                set(command, null, toASCIIString(buf, 0, offs));
                            }
                            else if (command == token) {
                                command = null;
                                offs++;
                                run();
                            }
                        }
                    });
                    return false;
                }
                if (addr != null) {
                    // data is a struct
                    if (offs != size) {
                        if (buf == null || buf.length < size) buf = new byte[size];
                        command = mem.get(addr, 1, buf, 0, size, 0, new IMemory.DoneMemory() {
                            public void doneMemory(IToken token, MemoryError error) {
                                if (error != null) {
                                    set(command, error, null);
                                }
                                else if (command == token) {
                                    command = null;
                                    offs = size;
                                    run();
                                }
                            }
                        });
                        return false;
                    }
                    StringBuffer bf = new StringBuffer();
                    if (!appendCompositeValueText(bf, 1, base_type_data, buf, 0, size, big_endian, this)) return false;
                    set(null, null, bf.toString());
                    return true;
                }
                TCFNode n = parent;
                while (n != null) {
                    if (n instanceof TCFNodeExecContext) {
                        TCFDataCache<IMemory.MemoryContext> mem_cache = ((TCFNodeExecContext)n).getMemoryContext();
                        if (!mem_cache.validate(this)) return false;
                        mem = mem_cache.getData();
                        if (mem != null) break;
                    }
                    n = n.parent;
                }
                if (mem != null) {
                    if (!type.validate(this)) return false;
                    ISymbols.Symbol type_data = type.getData();
                    if (type_data != null) {
                        switch (type_data.getTypeClass()) {
                        case pointer:
                        case array:
                            TCFDataCache<ISymbols.Symbol> base_type_cahce = model.getSymbolInfoCache(type_data.getBaseTypeID());
                            if (base_type_cahce != null) {
                                if (!base_type_cahce.validate(this)) return false;
                                base_type_data = base_type_cahce.getData();
                                if (base_type_data != null) {
                                    offs = 0;
                                    size = base_type_data.getSize();
                                    switch (base_type_data.getTypeClass()) {
                                    case integer:
                                    case cardinal:
                                        if (base_type_data.getSize() != 1) break;
                                        size = 0; // read until character = 0
                                    case composite:
                                        if (base_type_data.getSize() == 0) break;
                                        if (type_data.getTypeClass() == ISymbols.TypeClass.array &&
                                                base_type_data.getTypeClass() == ISymbols.TypeClass.composite) break;
                                        if (!value.validate(this)) return false;
                                        IExpressions.Value v = value.getData();
                                        if (v != null) {
                                            byte[] data = v.getValue();
                                            if (type_data.getTypeClass() == ISymbols.TypeClass.array) {
                                                set(null, null, toASCIIString(data, 0, data.length));
                                                return true;
                                            }
                                            big_endian = v.isBigEndian();
                                            BigInteger a = toBigInteger(data, 0, data.length, big_endian, false);
                                            if (!a.equals(BigInteger.valueOf(0))) {
                                                addr = a;
                                                Protocol.invokeLater(this);
                                                return false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                set(null, null, null);
                return true;
            }
            @Override
            public void reset() {
                super.reset();
                addr = null;
            }
        };
        type_name = new TCFDataCache<String>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                String name = null;
                TCFDataCache<ISymbols.Symbol> type_cache = type;
                for (;;) {
                    String s = null;
                    boolean get_base_type = false;
                    if (!type_cache.validate(this)) return false;
                    ISymbols.Symbol type_symbol = type_cache.getData();
                    if (type_symbol != null) {
                        s = type_symbol.getName();
                        if (s != null && type_symbol.getTypeClass() == ISymbols.TypeClass.composite) s = "struct " + s;
                        if (s == null && type_symbol.getSize() == 0) s = "void";
                        if (s == null) {
                            switch (type_symbol.getTypeClass()) {
                            case integer:
                                switch (type_symbol.getSize()) {
                                case 1: s = "char"; break;
                                case 2: s = "short"; break;
                                case 4: s = "int"; break;
                                case 8: s = "long long"; break;
                                default: s = "<Integer>"; break;
                                }
                                break;
                            case cardinal:
                                switch (type_symbol.getSize()) {
                                case 1: s = "unsigned char"; break;
                                case 2: s = "unsigned short"; break;
                                case 4: s = "unsigned"; break;
                                case 8: s = "unsigned long long"; break;
                                default: s = "<Unsigned>"; break;
                                }
                                break;
                            case real:
                                switch (type_symbol.getSize()) {
                                case 4: s = "float"; break;
                                case 8: s = "double"; break;
                                default: s = "<Float>"; break;
                                }
                                break;
                            case pointer:
                                s = "*";
                                get_base_type = true;
                                break;
                            case array:
                                s = "[" + type_symbol.getLength() + "]";
                                get_base_type = true;
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
                    if (s == null) {
                        name = "N/A";
                        break;
                    }
                    if (name == null) name = s;
                    else if (!get_base_type) name = s + " " + name;
                    else name = s + name;

                    if (!get_base_type) break;

                    type_cache = model.getSymbolInfoCache(type_symbol.getBaseTypeID());
                    if (type_cache == null) {
                        name = "N/A";
                        break;
                    }
                }
                set(null, null, name);
                return true;
            }
        };
        children = new TCFChildrenSubExpressions(this, 0, 0, 0);
    }

    @Override
    void dispose() {
        var_expression.dispose();
        value.dispose();
        type.dispose();
        type_name.dispose();
        string.dispose();
        children.dispose();
        expression.dispose();
        super.dispose();
    }

    @Override
    void dispose(String id) {
        children.dispose(id);
    }

    void onSuspended() {
        prev_value = next_value;
        value.reset();
        type.reset();
        type_name.reset();
        string.reset();
        children.onSuspended();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    public void onCastToTypeChanged() {
        expression.cancel();
        value.cancel();
        type.cancel();
        type_name.cancel();
        string.cancel();
        children.onCastToTypeChanged();
        addModelDelta(IModelDelta.STATE | IModelDelta.CONTENT);
    }

    String getScript() {
        return script;
    }

    TCFDataCache<ISymbols.Symbol> getField() {
        return field;
    }

    int getIndex() {
        return index;
    }

    boolean isDeref() {
        return deref;
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

    public TCFDataCache<ISymbols.Symbol> getType() {
        return type;
    }

    private String toASCIIString(byte[] data, int offs, int size) {
        StringBuffer bf = new StringBuffer();
        bf.append('"');
        for (int i = 0; i < size; i++) {
            int ch = data[offs + i] & 0xff;
            if (ch >= ' ' && ch < 0x7f) {
                bf.append((char)ch);
            }
            else {
                switch (ch) {
                case '\r': bf.append("\\r"); break;
                case '\n': bf.append("\\n"); break;
                case '\b': bf.append("\\b"); break;
                case '\t': bf.append("\\t"); break;
                case '\f': bf.append("\\f"); break;
                default:
                    bf.append('\\');
                    bf.append((char)('0' + ch / 64));
                    bf.append((char)('0' + ch / 8 % 8));
                    bf.append((char)('0' + ch % 8));
                }
            }
        }
        if (data.length <= offs + size || data[offs + size] == 0) bf.append('"');
        else bf.append("...");
        return bf.toString();
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
        if (s == null) s = "N/A";
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

    private boolean setTypeLabel(ILabelUpdate result, int col, Runnable done) {
        if (!type_name.validate(done)) return false;
        result.setLabel(type_name.getData(), col);
        return true;
    }

    private boolean isValueChanged(IExpressions.Value x, IExpressions.Value y) {
        if (x == null || y == null) return false;
        byte[] xb = x.getValue();
        byte[] yb = y.getValue();
        if (xb == null || yb == null) return false;
        if (xb.length != yb.length) return true;
        for (int i = 0; i < xb.length; i++) {
            if (xb[i] != yb[i]) return true;
        }
        return false;
    }

    @Override
    protected boolean getData(ILabelUpdate result, Runnable done) {
        TCFDataCache<?> pending = null;
        if (field != null && !field.validate()) pending = field;
        if (!text.validate()) pending = text;
        if (!value.validate()) pending = value;
        if (!type.validate()) pending = type;
        if (!type_name.validate()) pending = type_name;
        if (pending != null) {
            pending.wait(done);
            return false;
        }
        String name = null;
        if (index >= 0) {
            if (index == 0 && deref) {
                name = "*";
            }
            else {
                name = "[" + index + "]";
            }
        }
        if (name == null && field != null && field.getData() != null) name = field.getData().getName();
        if (name == null && text.getData() != null) name = text.getData();
        if (name != null) {
            String cast = model.getCastToType(id);
            if (cast != null) name = "(" + cast + ")(" + name + ")";
        }
        Throwable error = text.getError();
        if (error == null) error = value.getError();
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
                    else if (c.equals(TCFColumnPresentationExpression.COL_TYPE)) {
                        if (!setTypeLabel(result, i, done)) return false;
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
                        if (!setTypeLabel(result, i, done)) return false;
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
        next_value = value.getData();
        if (isValueChanged(prev_value, next_value)) {
            RGB c = new RGB(255, 255, 0);
            result.setBackground(c, 0);
            if (cols != null) {
                for (int i = 1; i < cols.length; i++) {
                    result.setBackground(c, i);
                }
            }
        }
        result.setImageDescriptor(ImageCache.getImageDescriptor(ImageCache.IMG_VARIABLE), 0);
        return true;
    }

    private void appendErrorText(StringBuffer bf, Throwable error) {
        if (error == null) return;
        bf.append("Exception: ");
        bf.append(TCFModel.getErrorMessage(error, true));
    }

    private boolean appendArrayValueText(StringBuffer bf, int level, ISymbols.Symbol type,
            byte[] data, int offs, int size, boolean big_endian, Runnable done) {
        assert offs + size <= data.length;
        int length = type.getLength();
        bf.append('[');
        if (length > 0) {
            int elem_size = size / length;
            for (int n = 0; n < length; n++) {
                if (n >= 100) {
                    bf.append("...");
                    break;
                }
                if (n > 0) bf.append(", ");
                if (!appendValueText(bf, level + 1, type.getBaseTypeID(),
                        data, offs + n * elem_size, elem_size, big_endian, done)) return false;
            }
        }
        bf.append(']');
        return true;
    }

    private boolean appendCompositeValueText(StringBuffer bf, int level, ISymbols.Symbol type,
            byte[] data, int offs, int size, boolean big_endian, Runnable done) {
        TCFDataCache<String[]> children_cache = model.getSymbolChildrenCache(type.getID());
        if (children_cache == null) {
            bf.append("{...}");
            return true;
        }
        if (!children_cache.validate(done)) return false;
        String[] children_data = children_cache.getData();
        if (children_data == null) {
            bf.append("{...}");
            return true;
        }
        bf.append('{');
        for (String id : children_data) {
            if (id != children_data[0]) bf.append(", ");
            TCFDataCache<ISymbols.Symbol> field_cache = model.getSymbolInfoCache(id);
            if (!field_cache.validate(done)) return false;
            ISymbols.Symbol field_data = field_cache.getData();
            if (field_data == null || offs + field_data.getOffset() + field_data.getSize() > data.length) {
                bf.append('?');
                continue;
            }
            bf.append(field_data.getName());
            bf.append('=');
            if (!appendValueText(bf, level + 1, field_data.getTypeID(),
                    data, offs + field_data.getOffset(), field_data.getSize(), big_endian, done)) return false;
        }
        bf.append('}');
        return true;
    }

    private boolean appendValueText(StringBuffer bf, int level, String type_id,
            byte[] data, int offs, int size, boolean big_endian, Runnable done) {
        if (data == null) return true;
        ISymbols.Symbol type_data = null;
        if (type_id != null) {
            TCFDataCache<ISymbols.Symbol> type_cache = model.getSymbolInfoCache(type_id);
            if (!type_cache.validate(done)) return false;
            type_data = type_cache.getData();
        }
        if (type_data == null) {
            if (level == 0) {
                bf.append("Type: not available\n");
                bf.append("Size: ");
                bf.append(data.length);
                bf.append(data.length == 1 ? " byte\n" : " bytes\n");
                bf.append("Hex: ");
                bf.append(toNumberString(16, type_data, data, 0, data.length, big_endian));
                bf.append("\n");
            }
            else {
                bf.append(toNumberString(16, type_data, data, 0, data.length, big_endian));
            }
            return true;
        }
        if (level == 0) {
            if (!string.validate(done)) return false;
            Throwable e = string.getError();
            String s = string.getData();
            if (s != null) {
                bf.append(s);
                bf.append("\n");
            }
            else if (e != null) {
                bf.append("Cannot read pointed value: ");
                bf.append(TCFModel.getErrorMessage(e, true));
            }
        }
        if (type_data.getSize() > 0) {
            switch (type_data.getTypeClass()) {
            case enumeration:
            case integer:
            case cardinal:
            case real:
                if (level == 0) {
                    bf.append("Dec: ");
                    bf.append(toNumberString(10, type_data, data, offs, size, big_endian));
                    bf.append("\n");
                    bf.append("Oct: ");
                    bf.append(toNumberString(8, type_data, data, offs, size, big_endian));
                    bf.append("\n");
                    bf.append("Hex: ");
                    bf.append(toNumberString(16, type_data, data, offs, size, big_endian));
                    bf.append("\n");
                }
                else if (type_data.getTypeClass() == ISymbols.TypeClass.cardinal) {
                    bf.append("0x");
                    bf.append(toNumberString(16, type_data, data, offs, size, big_endian));
                }
                else {
                    bf.append(toNumberString(10, type_data, data, offs, size, big_endian));
                }
                break;
            case pointer:
            case function:
                if (level == 0) {
                    bf.append("Oct: ");
                    bf.append(toNumberString(8, type_data, data, offs, size, big_endian));
                    bf.append("\n");
                    bf.append("Hex: ");
                    bf.append(toNumberString(16, type_data, data, offs, size, big_endian));
                    bf.append("\n");
                }
                else {
                    bf.append("0x");
                    bf.append(toNumberString(16, type_data, data, offs, size, big_endian));
                }
                break;
            case array:
                if (!appendArrayValueText(bf, level, type_data, data, offs, size, big_endian, done)) return false;
                if (level == 0) bf.append("\n");
                break;
            case composite:
                if (!appendCompositeValueText(bf, level, type_data, data, offs, size, big_endian, done)) return false;
                if (level == 0) bf.append("\n");
                break;
            }
        }
        if (level == 0) {
            if (!type_name.validate(done)) return false;
            String nm = type_name.getData();
            if (nm != null) {
                bf.append("Type: ");
                bf.append(nm);
                bf.append("\n");
            }
            bf.append("Size: ");
            bf.append(type_data.getSize());
            bf.append(type_data.getSize() == 1 ? " byte\n" : " bytes\n");
        }
        return true;
    }

    String getDetailText(Runnable done) {
        if (!expression.validate(done)) return null;
        if (!value.validate(done)) return null;
        StringBuffer bf = new StringBuffer();
        appendErrorText(bf, expression.getError());
        if (bf.length() == 0) appendErrorText(bf, value.getError());
        if (bf.length() == 0) {
            IExpressions.Value v = value.getData();
            if (v != null) {
                byte[] data = v.getValue();
                boolean big_endian = v.isBigEndian();
                if (!appendValueText(bf, 0, v.getTypeID(),
                        data, 0, data.length, big_endian, done)) return null;
            }
        }
        return bf.toString();
    }

    @Override
    protected boolean getData(IChildrenCountUpdate result, Runnable done) {
        if (!children.validate(done)) return false;
        result.setChildCount(children.size());
        return true;
    }

    @Override
    protected boolean getData(IChildrenUpdate result, Runnable done) {
        if (!children.validate(done)) return false;
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
        return true;
    }

    @Override
    protected boolean getData(IHasChildrenUpdate result, Runnable done) {
        if (!children.validate(done)) return false;
        result.setHasChilren(children.size() > 0);
        return true;
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
                    if (!node.expression.validate(this)) return;
                    if (node.expression.getData() != null && node.expression.getData().expression.canAssign()) {
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
                    if (!node.value.validate(this)) return;
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
                        if (!node.expression.validate(this)) return;
                        if (node.expression.getData() != null) {
                            IExpressions.Expression exp = node.expression.getData().expression;
                            if (exp.canAssign()) {
                                byte[] bf = null;
                                int size = exp.getSize();
                                boolean is_float = false;
                                boolean big_endian = false;
                                boolean signed = false;
                                if (!node.value.validate(this)) return;
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
                                    exps.assign(exp.getID(), bf, new IExpressions.DoneAssign() {
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
                                                done(Boolean.TRUE);
                                            }
                                        }
                                    });
                                    return;
                                }
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
