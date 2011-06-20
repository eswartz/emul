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

import java.util.ArrayList;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExpression;
import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IWorkbenchPage;

public class ViewMemoryCommand extends AbstractActionDelegate {

    private static class Block {
        long addr;
        long size;
    }

    @Override
    protected void run() {
        try {
            IWorkbenchPage page = getWindow().getActivePage();
            page.showView(IDebugUIConstants.ID_MEMORY_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
            final ArrayList<IMemoryBlock> list = new ArrayList<IMemoryBlock>();
            for (final TCFNode node : getSelectedNodes()) {
                final IMemoryBlockRetrievalExtension mem_retrieval = (IMemoryBlockRetrievalExtension)
                        node.getAdapter(IMemoryBlockRetrievalExtension.class);
                if (mem_retrieval == null) continue;
                Block b = new TCFTask<Block>(node.getChannel()) {
                    public void run() {
                        try {
                            Number addr = null;
                            long size = -1;
                            if (node instanceof TCFNodeExpression) {
                                TCFDataCache<IExpressions.Value> val_cache = ((TCFNodeExpression)node).getValue();
                                if (!val_cache.validate(this)) return;
                                IExpressions.Value val_data = val_cache.getData();
                                if (val_data != null) {
                                    addr = val_data.getAddress();
                                    if (addr != null) {
                                        byte[] bytes = val_data.getValue();
                                        if (bytes != null) size = bytes.length;
                                    }
                                }
                            }
                            Block b = null;
                            if (addr != null) {
                                b = new Block();
                                b.addr = addr.longValue();
                                b.size = size;
                            }
                            done(b);
                        }
                        catch (Exception x) {
                            error(x);
                        }
                    }
                }.get();
                if (b != null) list.add(mem_retrieval.getMemoryBlock(b.addr, b.size));
            }
            DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks(list.toArray(new IMemoryBlock[list.size()]));
        }
        catch (Exception x) {
            Activator.log("Cannot open memory view", x);
        }
    }

    @Override
    protected void selectionChanged() {
        getAction().setEnabled(getSelectedNodes().length > 0);
    }
}
