/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.tcf.protocol.Protocol;


public class TCFLaunchAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("unchecked")
    private final Class[] adapter_list = {
        IElementContentProvider.class,
        IElementLabelProvider.class,
        IModelProxyFactory.class,
        ITerminateHandler.class
    };

    private final IElementLabelProvider launch_label_provider = new TCFLaunchLabelProvider();

    @SuppressWarnings("unchecked")
    public Object getAdapter(final Object from, final Class to) {
        if (from instanceof TCFLaunch) {
            if (to.equals(IElementLabelProvider.class)) {
                return launch_label_provider;
            }
            final Object[] res = new Object[1];
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    TCFLaunch launch = (TCFLaunch)from;
                    TCFModel model = Activator.getModelManager().getModel(launch);
                    if (model != null) {
                        if (to.isInstance(model)) {
                            res[0] = model;
                            return;
                        }
                        Object cmd = model.getCommand(to);
                        if (cmd != null) {
                            res[0] = cmd;
                            return;
                        }
                    }
                }
            });
            if (res[0] != null) return res[0];
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return adapter_list;
    }
}
