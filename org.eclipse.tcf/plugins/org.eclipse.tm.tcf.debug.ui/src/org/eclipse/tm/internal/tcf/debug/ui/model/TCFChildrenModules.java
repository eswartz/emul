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
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IMemoryMap;

/**
 * Provides and caches memory regions (modules) for a context.
 */
public class TCFChildrenModules extends TCFChildren {

    public TCFChildrenModules(TCFNode node) {
        super(node, 128);
    }

    void onMemoryMapChanged() {
        reset();
    }

    @Override
    protected boolean startDataRetrieval() {
        assert command == null;
        IMemoryMap mmap = node.model.getLaunch().getService(IMemoryMap.class);
        if (mmap == null) {
            set(null, null, null);
            return true;
        }
        command = mmap.get(node.id, new IMemoryMap.DoneGet() {
            public void doneGet(IToken token, Exception error, IMemoryMap.MemoryRegion[] map) {
                Map<String, TCFNode> data = new HashMap<String, TCFNode>();
                if (map != null) {
                    for (IMemoryMap.MemoryRegion region : map) {
                        String id = node.id + ".Module-" + region.getFileName() + '@' + region.getAddress();
                        TCFNodeModule module = (TCFNodeModule) node.model.getNode(id);
                        if (module == null) {
                            module = new TCFNodeModule(node, id, region);
                        }
                        data.put(id, module);
                    }
                }
                set(token, error, data);
            }
        });
        return false;
    }
}
