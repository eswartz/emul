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
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.model.IWatchInExpressions;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IWorkbenchPage;

public class WatchInExpressionsCommand extends AbstractActionDelegate {

    @Override
    protected void selectionChanged() {
        getAction().setEnabled(getNodes().length > 0);
    }

    @Override
    protected void run() {
        try {
            IWorkbenchPage page = getWindow().getActivePage();
            page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
            for (final TCFNode node : getNodes()) {
                final IExpressionManager manager = node.getModel().getExpressionManager();
                IExpression e = new TCFTask<IExpression>(node.getChannel()) {
                    public void run() {
                        try {
                            IExpression e = null;
                            if (node instanceof IWatchInExpressions) {
                                TCFDataCache<String> text_cache = ((IWatchInExpressions)node).getExpressionText();
                                if (!text_cache.validate(this)) return;
                                String text_data = text_cache.getData();
                                if (text_data != null) {
                                    for (final IExpression x : manager.getExpressions()) {
                                        if (text_data.equals(x.getExpressionText())) {
                                            done(null);
                                            return;
                                        }
                                    }
                                    e = manager.newWatchExpression(text_data);
                                }
                            }
                            done(e);
                        }
                        catch (Exception x) {
                            error(x);
                        }
                    }
                }.get();
                if (e != null) manager.addExpression(e);
            }
        }
        catch (Exception x) {
            Activator.log("Cannot open expressions view", x);
        }
    }

    private TCFNode[] getNodes() {
        TCFNode[] arr = getSelectedNodes();
        for (TCFNode n : arr) {
            if (n instanceof IWatchInExpressions) continue;
            return new TCFNode[0];
        }
        return arr;
    }
}
