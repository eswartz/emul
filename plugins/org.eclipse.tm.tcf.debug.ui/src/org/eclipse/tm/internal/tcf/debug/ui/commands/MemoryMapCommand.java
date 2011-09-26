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
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeArrayPartition;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExpression;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeModule;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

public class MemoryMapCommand extends AbstractActionDelegate {

    private static boolean isValidNode(final TCFNode n) {
        if (n instanceof TCFNodeLaunch) return true;
        if (n instanceof TCFNodeExecContext) {
            return new TCFTask<Boolean>(n.getChannel()) {
                public void run() {
                    TCFDataCache<TCFNodeExecContext> mem_cache = n.getModel().searchMemoryContext(n);
                    if (mem_cache == null) {
                        done(false);
                        return;
                    }
                    if (!mem_cache.validate(this)) return;
                    TCFNodeExecContext mem_node = mem_cache.getData();
                    if (mem_node == null) {
                        done(false);
                        return;
                    }
                    TCFDataCache<TCFNodeExecContext> syms_cache = mem_node.getSymbolsNode();
                    if (!syms_cache.validate(this)) return;
                    TCFNodeExecContext syms_node = syms_cache.getData();
                    if (syms_node == null) {
                        done(false);
                        return;
                    }
                    TCFDataCache<IMemory.MemoryContext> ctx_cache = syms_node.getMemoryContext();
                    if (!ctx_cache.validate(this)) return;
                    done(ctx_cache.getData() != null);
                }
            }.getE();
        }
        if (n instanceof TCFNodeStackFrame) return true;
        if (n instanceof TCFNodeExpression) return true;
        if (n instanceof TCFNodeArrayPartition) return true;
        if (n instanceof TCFNodeModule) return true;
        return false;
    }

    protected void selectionChanged() {
        getAction().setEnabled(isValidNode(getSelectedNode()));
    }

    protected void run() {
        TCFNode node = getSelectedNode();
        if (!isValidNode(node)) return;
        Shell shell = getWindow().getShell();
        try {
            new MemoryMapDialog(shell, node).open();
        }
        catch (Throwable x) {
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            mb.setText("Cannot open Symbol Files dialog");
            mb.setMessage(TCFModel.getErrorMessage(x, true));
            mb.open();
        }
    }
}
