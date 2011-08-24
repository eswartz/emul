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

import java.util.HashMap;

import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;

public class TCFChildrenExpressions extends TCFChildren {

    TCFChildrenExpressions(TCFNode node) {
        super(node, 128);
    }

    void onSuspended() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onSuspended();
    }

    void onRegisterValueChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onRegisterValueChanged();
    }

    void onMemoryChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onMemoryChanged();
    }

    void onMemoryMapChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onMemoryMapChanged();
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
        int cnt = 0;
        HashMap<String,TCFNode> data = new HashMap<String,TCFNode>();
        for (final IExpression e : node.model.getExpressionManager().getExpressions()) {
            String text = e.getExpressionText();
            TCFNodeExpression n = findScript(text);
            if (n == null) add(n = new TCFNodeExpression(node, text, null, null, -1, false));
            n.setSortPosition(cnt++);
            if (e instanceof IWatchExpression) n.setEnabled(((IWatchExpression)e).isEnabled());
            data.put(n.id, n);
        }
        set(null, null, data);
        return true;
    }
}
