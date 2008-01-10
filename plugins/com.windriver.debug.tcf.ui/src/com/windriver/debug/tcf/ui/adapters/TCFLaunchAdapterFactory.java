/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.ui.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;

import com.windriver.debug.tcf.core.model.TCFLaunch;
import com.windriver.debug.tcf.ui.TCFUI;
import com.windriver.debug.tcf.ui.model.TCFModel;
import com.windriver.tcf.api.protocol.Protocol;

public class TCFLaunchAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("unchecked")
    private final Class[] adapter_list = {
        IElementContentProvider.class,
        IElementLabelProvider.class,
        IModelProxyFactory.class,
        ITerminateHandler.class
    };

    @SuppressWarnings("unchecked")
    public Object getAdapter(final Object from, final Class to) {
        if (from instanceof TCFLaunch) {
            final Object[] res = new Object[1];
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    TCFLaunch launch = (TCFLaunch)from;
                    TCFModel model = TCFUI.getModelManager().getModel(launch);
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
        System.err.println(from.getClass().getName() + " -> " + to);
        return null;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return adapter_list;
    }
}
