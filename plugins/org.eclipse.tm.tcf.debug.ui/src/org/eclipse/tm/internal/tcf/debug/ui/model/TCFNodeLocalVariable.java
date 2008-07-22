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

import java.math.BigInteger;

import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.util.TCFDataCache;

public class TCFNodeLocalVariable extends TCFNode {

    /**
     * Presentation column IDs.
     */
    public static final String
        COL_TYPE = "Type",
        COL_NAME = "Name",
        COL_HEX_VALUE = "HexValue",
        COL_DEC_VALUE = "DecValue";


    private final TCFDataCache<IExpressions.Expression> context;
    private final TCFDataCache<IExpressions.Value> value;

    protected TCFNodeLocalVariable(TCFNode parent, final String id) {
        super(parent, id);
        IChannel channel = parent.model.getLaunch().getChannel();
        context = new TCFDataCache<IExpressions.Expression>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                IExpressions exps = model.getLaunch().getService(IExpressions.class);
                command = exps.getContext(id, new IExpressions.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, IExpressions.Expression context) {
                        set(token, error, context);
                    }
                });
                return false;
            }
        };
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

    void onSuspended() {
        value.reset();
        addModelDelta(IModelDelta.STATE);
    }

    private void setLabel(ILabelUpdate result, int col, int radix) {
        IExpressions.Expression ctx = context.getData();
        Throwable error = value.getError();
        IExpressions.Value val = value.getData();
        byte[] data = val.getValue();
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
                result.setLabel(ctx.getName() + " = " + s, 0);
            }
        }
    }

    @Override
    protected void getData(ILabelUpdate result) {
        result.setImageDescriptor(ImageCache.getImageDescriptor(getImageName()), 0);
        IExpressions.Expression ctx = context.getData();
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
                    if (c.equals(COL_NAME)) result.setLabel(ctx.getName(), i);
                    else if (c.equals(COL_HEX_VALUE)) setLabel(result, i, 16);
                    else if (c.equals(COL_DEC_VALUE)) setLabel(result, i, 10);
                }
            }
        }
        else {
            result.setLabel(id, 0);
        }
    }

    @Override
    int getRelevantModelDeltaFlags(IPresentationContext p) {
        if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(p.getId())) {
            return super.getRelevantModelDeltaFlags(p);
        }
        return 0;
    }

    @Override
    public void invalidateNode() {
        context.reset();
        value.reset();
    }

    @Override
    public boolean validateNode(Runnable done) {
        TCFDataCache<?> pending = null;
        if (!context.validate()) pending = context;
        if (!value.validate()) pending = value;
        if (pending != null) {
            pending.wait(done);
            return false;
        }
        return true;
    }

    @Override
    protected String getImageName() {
        return ImageCache.IMG_VARIABLE;
    }
}
