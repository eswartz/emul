/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.processes;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

public class TCFSystemViewProcessAdapterFactory implements IAdapterFactory {

    private final TCFSystemViewRemoteProcessAdapter adapter =
        new TCFSystemViewRemoteProcessAdapter();

    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        assert adaptableObject instanceof TCFRemoteProcess;
        if (adapterType == IPropertySource.class) {
            ((ISystemViewElementAdapter)adapter).setPropertySourceInput(adaptableObject);
        }
        return adapter;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return new Class[] {
            ISystemViewElementAdapter.class,
            ISystemDragDropAdapter.class,
            ISystemRemoteElementAdapter.class,
            IPropertySource.class,
            IWorkbenchAdapter.class,
            IActionFilter.class,
            IDeferredWorkbenchAdapter.class,
            IRemoteObjectIdentifier.class,
        };
    }
}
