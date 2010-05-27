/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.launch;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;

public class TCFPropertyTester extends PropertyTester {

    public boolean test(Object receiver, String property, Object[] args, Object expected_value) {
        Object value = null;
        try {
            if (receiver instanceof IAdaptable) {
                IAdaptable selection = (IAdaptable)receiver;
                ITCFLaunchContext context = TCFLaunchContext.getLaunchContext(selection);
                if (context != null) {
                    if (property.equals("isExecutable")) {
                        IProject project = context.getProject(selection);
                        IPath path = context.getPath(selection);
                        if (project != null && path != null) {
                            value = context.isBinary(project, path);
                        }
                    }
                }
            }
        }
        catch (Throwable x) {
            Activator.log(x);
        }
        if (expected_value != null) return expected_value.equals(value);
        return (value instanceof Boolean) && ((Boolean)value).booleanValue();
    }
}
