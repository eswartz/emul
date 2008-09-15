package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.math.BigInteger;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.util.TCFDataCache;

public class TCFNodeExpression extends TCFNode {

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
                TCFDataCache<String> t = ((TCFNodeExpression)parent).getExpressionText();
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
                    while (n instanceof TCFNodeExpression) n = n.parent;
                    command = exps.create(n.id, null, e, new IExpressions.DoneCreate() {
                        public void doneCreate(IToken token, Exception error, IExpressions.Expression context) {
                            if (isDisposed()) {
                                IExpressions exps = channel.getRemoteService(IExpressions.class);
                                exps.dispose(context.getID(), new IExpressions.DoneDispose() {
                                    public void doneDispose(IToken token, Exception error) {
                                        if (error == null) return;
                                        if (channel.getState() != IChannel.STATE_OPEN) return;
                                        Activator.log("Error disposing remote expression evaluator", error);
                                    }
                                });
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
                String id = null;
                if (value.getData() != null) id = value.getData().getTypeID();
                ISymbols syms = model.getLaunch().getService(ISymbols.class);
                if (id == null || syms == null) {
                    set(null, null, null);
                    return true;
                }
                command = syms.getContext(id, new ISymbols.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, ISymbols.Symbol sym) {
                        set(token, error, sym);
                    }
                });
                return false;
            }
        };
        children = new TCFChildrenSubExpressions(this);
    }
    
    @Override
    void dispose() {
        children.dispose();
        super.dispose();
        if (!expression.isValid() || expression.getData() == null) return;
        final IChannel channel = model.getLaunch().getChannel();
        if (channel.getState() != IChannel.STATE_OPEN) return;
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
    
    TCFDataCache<String> getExpressionText() {
        return text;
    }
    
    TCFDataCache<IExpressions.Value> getValue() {
        return value;
    }
    
    TCFDataCache<ISymbols.Symbol> getType() {
        return type;
    }
    
    private BigInteger toBigInteger(byte[] data, boolean big_endian, boolean sign_extension) {
        byte[] temp = null;
        if (sign_extension) {
            temp = new byte[data.length];
        }
        else {
            temp = new byte[data.length + 1];
            temp[0] = 0; // Extra byte to avoid sign extension by BigInteger
        }
        if (big_endian) {
            System.arraycopy(data, 0, temp, sign_extension ? 0 : 1, data.length);
        }
        else {
            for (int i = 0; i < data.length; i++) {
                temp[temp.length - i - 1] = data[i];
            }
        }
        return new BigInteger(temp);
    }
    
    private void setLabel(ILabelUpdate result, String name, int col, int radix) {
        String s = null;
        IExpressions.Value val = value.getData();
        if (val != null) {
            byte[] data = val.getValue();
            if (data == null) s = "n/a";
            if (s == null && data.length == 0) s = "";
            if (s == null && radix == 10 && data.length <= 16) {
                ISymbols.Symbol t = type.getData();
                if (t != null) {
                    switch (t.getTypeClass()) {
                    case integer:
                        s = toBigInteger(data, val.isBigEndian(), true).toString();
                        break;
                    case real:
                        switch (t.getSize()) {
                        case 4:
                            s = Float.toString(Float.intBitsToFloat(toBigInteger(
                                    data, val.isBigEndian(), true).intValue()));
                            break;
                        case 8:
                            s = Double.toString(Double.longBitsToDouble(toBigInteger(
                                    data, val.isBigEndian(), true).longValue()));
                            break;
                        }
                        break;
                    }
                }
            }
            if (s == null && data.length <= 16) {
                s = toBigInteger(data, val.isBigEndian(), false).toString(radix);
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
            }
        }
        if (s == null) s = "...";
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
        if (s == null) s = "";
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
        if (error != null) {
            result.setForeground(new RGB(255, 0, 0), 0);
            result.setLabel(name + ": " + error.getMessage(), 0);
        }
        else {
            String[] cols = result.getColumnIds();
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
    public void invalidateNode() {
        value.reset();
        type.reset();
        children.reset();
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
}
