package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.math.BigInteger;

import org.eclipse.debug.core.model.IExpression;
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
import org.eclipse.tm.tcf.util.TCFDataCache;

public class TCFNodeExpression extends TCFNode {

    private final Exception error;
    private final IExpression local_expression;
    private final IExpressions.Expression remote_expression;
    private final TCFDataCache<IExpressions.Value> value;
    
    private static int expr_cnt;

    TCFNodeExpression(TCFNode parent, Exception error,
            IExpression local_expression, IExpressions.Expression remote_expression) {
        super(parent, remote_expression == null ? "Expr" + expr_cnt++ : remote_expression.getID());
        this.error = error;
        this.local_expression = local_expression;
        this.remote_expression = remote_expression;
        IChannel channel = model.getLaunch().getChannel();
        value = new TCFDataCache<IExpressions.Value>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                IExpressions exps = model.getLaunch().getService(IExpressions.class);
                command = exps.evaluate(id, new IExpressions.DoneEvaluate() {
                    public void doneEvaluate(IToken token, Exception error, IExpressions.Value value) {
                        set(token, error, value);
                    }
                });
                return false;
            }
        };
    }
    
    @Override
    void dispose() {
        super.dispose();
        final IChannel channel = model.getLaunch().getChannel();
        if (channel.getState() != IChannel.STATE_OPEN) return;
        IExpressions exps = channel.getRemoteService(IExpressions.class);
        if (exps != null) {
            exps.dispose(id, new IExpressions.DoneDispose() {
                public void doneDispose(IToken token, Exception error) {
                    if (error == null) return;
                    if (channel.getState() != IChannel.STATE_OPEN) return;
                    Activator.log("Error disposing remote expression evaluator", error);
                }
            });
        }
    }
    
    void onSuspended() {
        value.reset();
        addModelDelta(IModelDelta.STATE);
    }
    
    IExpression getExpression() {
        return local_expression;
    }
    
    private void setLabel(ILabelUpdate result, int col, int radix) {
        Throwable error = value.getError();
        IExpressions.Value val = value.getData();
        byte[] data = val.getValue();
        if (error != null) {
            result.setForeground(new RGB(255, 0, 0), col);
            result.setLabel(local_expression.getExpressionText() + ": " + error.getMessage(), col);
        }
        else if (data != null) {
            byte[] temp = new byte[data.length + 1];
            temp[0] = 0; // Extra byte to avoid sign extension by BigInteger
            if (val.isBigEndian()) {
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
            if (col >= 0) {
                result.setLabel(s, col);
            }
            else {
                result.setLabel(remote_expression.getName() + " = " + s, 0);
            }
        }
    }

    @Override
    protected void getData(ILabelUpdate result) {
        result.setImageDescriptor(ImageCache.getImageDescriptor(getImageName()), 0);
        if (error != null) {
            result.setForeground(new RGB(255, 0, 0), 0);
            result.setLabel(local_expression.getExpressionText() + ": " + error.getMessage(), 0);
        }
        else {
            String[] cols = result.getColumnIds();
            if (cols == null) {
                setLabel(result, -1, 16);
            }
            else {
                for (int i = 0; i < cols.length; i++) {
                    String c = cols[i];
                    if (c.equals(TCFColumnPresentationExpression.COL_NAME)) {
                        result.setLabel(remote_expression.getExpression(), i);
                    }
                    else if (c.equals(TCFColumnPresentationExpression.COL_HEX_VALUE)) {
                        setLabel(result, i, 16);
                    }
                    else if (c.equals(TCFColumnPresentationExpression.COL_DEC_VALUE)) {
                        setLabel(result, i, 10);
                    }
                }
            }
        }
    }

    @Override
    int getRelevantModelDeltaFlags(IPresentationContext p) {
        if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(p.getId())) {
            return super.getRelevantModelDeltaFlags(p);
        }
        return 0;
    }

    @Override
    public void invalidateNode() {
        value.reset();
    }

    @Override
    public boolean validateNode(Runnable done) {
        if (!value.validate()) {
            value.wait(done);
            return false;
        }
        return true;
    }
}
