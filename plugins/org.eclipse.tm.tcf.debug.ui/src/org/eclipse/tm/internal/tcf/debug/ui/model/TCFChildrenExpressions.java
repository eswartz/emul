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

import java.util.HashMap;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IExpressions;

public class TCFChildrenExpressions extends TCFChildren {

    private final TCFNodeStackFrame node;
    
    private final IExpressionsListener listener = new IExpressionsListener() {
        
        int generation;
        
        public void expressionsAdded(IExpression[] expressions) {
            expressionsRemoved(expressions);
        }

        public void expressionsChanged(IExpression[] expressions) {
            expressionsRemoved(expressions);
        }

        public void expressionsRemoved(IExpression[] expressions) {
            final int g = ++generation;
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    reset();
                    if (g != generation) return;
                    node.addModelDelta(IModelDelta.CONTENT);
                    node.model.fireModelChanged();
                }
            });
        }
    };

    TCFChildrenExpressions(TCFNodeStackFrame node) {
        super(node.model.getLaunch().getChannel(), 32);
        this.node = node;
        IExpressionManager m = DebugPlugin.getDefault().getExpressionManager();
        m.addExpressionListener(listener);
    }

    @Override
    void dispose() {
        IExpressionManager m = DebugPlugin.getDefault().getExpressionManager();
        m.removeExpressionListener(listener);
        super.dispose();
    }
    
    void onSuspended() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onSuspended();
    }
    
    private TCFNodeExpression findScript(String text) {
        for (TCFNode n : getNodes()) {
            TCFNodeExpression e = (TCFNodeExpression)n;
            if (text.equals(e.getScript())) return e;
        }
        return null;
    }

    @Override
    protected boolean startDataRetrieval() {
        IExpressions exps = node.model.getLaunch().getService(IExpressions.class);
        if (exps == null) {
            set(null, null, new HashMap<String,TCFNode>());
            return true;
        }
        HashMap<String,TCFNode> data = new HashMap<String,TCFNode>();
        IExpressionManager m = DebugPlugin.getDefault().getExpressionManager();
        int cnt = 0;
        for (final IExpression e : m.getExpressions()) {
            String text = e.getExpressionText();
            TCFNodeExpression n = findScript(text);
            if (n == null) add(n = new TCFNodeExpression(node, text, null, null, -1));
            n.setSortPosition(cnt++);
            data.put(n.id, n);
        }
        set(null, null, data);
        return true;
    }
}
