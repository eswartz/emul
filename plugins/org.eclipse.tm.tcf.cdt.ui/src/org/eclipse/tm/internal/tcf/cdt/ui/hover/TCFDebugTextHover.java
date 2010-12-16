/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui.hover;

import java.math.BigInteger;
import java.util.Map;

import org.eclipse.cdt.debug.ui.editors.AbstractDebugTextHover;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFChildren;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFChildrenStackTrace;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExpression;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.services.IExpressions.DoneCreate;
import org.eclipse.tm.tcf.services.IExpressions.DoneDispose;
import org.eclipse.tm.tcf.services.IExpressions.DoneEvaluate;
import org.eclipse.tm.tcf.services.IExpressions.Expression;
import org.eclipse.tm.tcf.services.IExpressions.Value;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

/**
 * TCF implementation of debug expression hover for the C/C++ Editor.
 */
public class TCFDebugTextHover extends AbstractDebugTextHover implements ITextHoverExtension2 {

    @Override
    public IInformationControlCreator getHoverControlCreator() {
        if (useExpressionExplorer()) {
            return createExpressionInformationControlCreator();
        }
        else {
            return new IInformationControlCreator() {
                public IInformationControl createInformationControl(Shell parent) {
                    return new DefaultInformationControl(parent, false);
                }
            };
        }
    }

    private IInformationControlCreator createExpressionInformationControlCreator() {
        return new ExpressionInformationControlCreator();
    }

    protected boolean useExpressionExplorer() {
        return true;
    }

    public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
        if (!useExpressionExplorer()) {
            return getHoverInfo(textViewer, hoverRegion);
        }
        final TCFNodeStackFrame activeFrame = getActiveFrame();
        if (activeFrame == null) {
            return null;
        }
        final String text= getExpressionText(textViewer, hoverRegion);
        if (text == null || text.length() == 0) {
            return null;
        }
        TCFNode hoverNode = new TCFTask<TCFNode>() {
            public void run() {
                TCFNode evalContext = activeFrame.isEmulated() ? activeFrame.getParent() : activeFrame;
                TCFChildren cache = evalContext.getModel().getHoverExpressionCache(evalContext, text);
                if (!cache.validate(this)) return;
                Map<String,TCFNode> nodes = cache.getData();
                if (nodes != null) {
                    for (TCFNode node : nodes.values()) {
                        TCFDataCache<IExpressions.Value> value = ((TCFNodeExpression)node).getValue();
                        if (!value.validate(this)) return;
                        if (value.getData() != null) {
                            done(node.getParent());
                            return;
                        }
                    }
                }
                done(null);
            }
        }.getE();
        return hoverNode;
    }

    @Override
    protected boolean canEvaluate() {
        if (getActiveFrame() == null) {
            return false;
        }
        return true;
    }

    private TCFNodeStackFrame getActiveFrame() {
        IAdaptable context = getSelectionAdaptable();
        if (context instanceof TCFNodeStackFrame) {
            return (TCFNodeStackFrame) context;
        } else if (context instanceof TCFNodeExecContext) {
            final TCFNodeExecContext _execContext = (TCFNodeExecContext) context;
            TCFNodeStackFrame frame = new TCFTask<TCFNodeStackFrame>() {
                public void run() {
                    TCFChildrenStackTrace stack = _execContext.getStackTrace();
                    if (!stack.validate(this)) {
                        return;
                    }
                    done(stack.getTopFrame());
                }
            }.getE();
            return frame;
        }
        return null;
    }

    @Override
    protected String evaluateExpression(final String expression) {
        final TCFNodeStackFrame activeFrame = getActiveFrame();
        if (activeFrame == null) {
            return null;
        }
        String value = new TCFTask<String>() {
            public void run() {
                IChannel channel = activeFrame.getChannel();
                final IExpressions exprSvc = channel.getRemoteService(IExpressions.class);
                if (exprSvc != null) {
                    TCFNode evalContext = activeFrame.isEmulated() ? activeFrame.getParent() : activeFrame;
                    exprSvc.create(evalContext.getID(), null, expression, new DoneCreate() {
                        public void doneCreate(IToken token, Exception error, final Expression context) {
                            if (error == null) {
                                exprSvc.evaluate(context.getID(), new DoneEvaluate() {
                                    public void doneEvaluate(IToken token, Exception error, Value value) {
                                        if (error == null) {
                                            done(getValueText(value));
                                        } else {
                                            done(null);
                                        }
                                        exprSvc.dispose(context.getID(), new DoneDispose() {
                                            public void doneDispose(IToken token, Exception error) {
                                                // no-op
                                            }
                                        });
                                    }
                                });
                            } else {
                                done(null);
                            }
                        }
                    });
                } else {
                    done(null);
                }
            }
        }.getE();
        return value;
    }

    private static String getValueText(Value value) {
        BigInteger bigInteger = toBigInteger(value.getValue(), value.isBigEndian(), true);
        switch(value.getTypeClass()) {
        case integer:
            return bigInteger.toString();
        case real:
            if (value.getValue().length <= 4) {
                return String.valueOf(Float.intBitsToFloat(bigInteger.intValue()));
            } else if (value.getValue().length <= 8) {
                return String.valueOf(Double.longBitsToDouble(bigInteger.longValue()));
            } else {
                return "N/A";
            }
        default:
            return "0x"+bigInteger.toString(16);
        }
    }

    private static BigInteger toBigInteger(byte[] data, boolean big_endian, boolean sign_extension) {
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

}
