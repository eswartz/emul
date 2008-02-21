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
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.tm.internal.tcf.debug.ui.commands.BreakpointCommand;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;


public class TCFBreakpointAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("unchecked")
    public Object getAdapter(Object obj, Class adapterType) {
        if (obj instanceof TCFNode) {
            return new BreakpointCommand();
        }
        System.out.println(obj.getClass().getName() + " -> " + adapterType);
        return null;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return new Class[]{ IToggleBreakpointsTarget.class, IToggleBreakpointsTargetExtension.class };
    }
}
