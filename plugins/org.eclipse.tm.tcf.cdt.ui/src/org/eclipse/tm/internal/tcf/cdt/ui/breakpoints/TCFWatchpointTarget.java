/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui.breakpoints;

import org.eclipse.cdt.debug.internal.core.ICWatchpointTarget;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExpression;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

/**
 * TCF "Add Watchpoint" target implementation.
 */
@SuppressWarnings("restriction")
public class TCFWatchpointTarget implements ICWatchpointTarget {

    private final TCFNodeExpression fNode;

    public TCFWatchpointTarget(TCFNodeExpression node) {
        fNode = node;
    }
    
    public void canSetWatchpoint(CanCreateWatchpointRequest request) {
        request.setCanCreate(true);
        request.done();
    }

    public String getExpression() {
        final TCFDataCache<String> expressionText = fNode.getExpressionText();
        String expr = new TCFTask<String>(fNode.getChannel()) {
            public void run() {
                if (!expressionText.validate(this)) return;
                done(expressionText.getData());
            }
        }.getE();
        return expr != null ? expr : "";
    }

    public void getSize(final GetSizeRequest request) {
        final TCFDataCache<ISymbols.Symbol> expressionType = fNode.getType();
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (!expressionType.validate(this)) return;
                ISymbols.Symbol type = expressionType.getData();
                request.setSize(type != null ? type.getSize() : 1);
                request.done();
            }
        });
    }
}
