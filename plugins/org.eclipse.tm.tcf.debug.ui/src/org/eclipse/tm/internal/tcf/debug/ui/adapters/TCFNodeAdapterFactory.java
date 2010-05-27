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
package org.eclipse.tm.internal.tcf.debug.ui.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.tm.internal.tcf.debug.ui.commands.BreakpointCommand;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;


public class TCFNodeAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] adapter_list = {
        IToggleBreakpointsTarget.class,
        IToggleBreakpointsTargetExtension.class,
    };

    private final BreakpointCommand breakpoint_command = new BreakpointCommand();

    @SuppressWarnings("unchecked")
    public Object getAdapter(Object obj, Class cls) {
        if (obj instanceof TCFNode) {
            if (cls == IToggleBreakpointsTarget.class) return breakpoint_command;
            if (cls == IToggleBreakpointsTargetExtension.class) return breakpoint_command;
        }
        return null;
    }

    public Class<?>[] getAdapterList() {
        return adapter_list;
    }
}
