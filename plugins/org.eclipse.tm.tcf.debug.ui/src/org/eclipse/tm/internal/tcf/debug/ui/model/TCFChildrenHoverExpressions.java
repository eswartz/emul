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
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.util.HashMap;

/**
 * Provides the cache of root nodes for the expression hover.
 */
class TCFChildrenHoverExpressions extends TCFChildren {

    TCFChildrenHoverExpressions(TCFNode parent) {
        super(parent, 16);
    }

    void onSuspended() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onSuspended();
    }

    void onRegisterValueChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onRegisterValueChanged();
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
        HashMap<String,TCFNode> data = new HashMap<String,TCFNode>();
        String expression_script = null;
        if (node instanceof TCFNodeExecContext) {
            expression_script = ((TCFNodeExecContext)node).getHoverExpression();
        }
        else if (node instanceof TCFNodeStackFrame) {
            expression_script = ((TCFNodeStackFrame)node).getHoverExpression();
        }
        if (expression_script != null) {
            TCFNodeExpression expression_node = findScript(expression_script);
            if (expression_node == null) {
                add(expression_node = new TCFNodeExpression(node, expression_script, null, null, -1, false));
            }
            data.put(expression_node.id, expression_node);
        }
        set(null, null, data);
        return true;
    }
}
