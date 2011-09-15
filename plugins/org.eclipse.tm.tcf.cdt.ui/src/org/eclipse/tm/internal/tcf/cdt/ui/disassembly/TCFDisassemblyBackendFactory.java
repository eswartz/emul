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
package org.eclipse.tm.internal.tcf.cdt.ui.disassembly;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;

@SuppressWarnings({"restriction", "rawtypes"})
public class TCFDisassemblyBackendFactory implements IAdapterFactory {

    private static final Class<?>[] CLASSES = { IDisassemblyBackend.class };

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof TCFNode) {
            TCFDisassemblyBackend backend = new TCFDisassemblyBackend();
            if (backend.supportsDebugContext((TCFNode) adaptableObject)) {
                return backend;
            }
        }
        return null;
    }

    public Class[] getAdapterList() {
        return CLASSES;
    }

}
