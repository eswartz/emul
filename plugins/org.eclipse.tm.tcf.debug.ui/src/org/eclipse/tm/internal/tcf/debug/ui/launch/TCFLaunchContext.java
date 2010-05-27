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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.osgi.framework.Bundle;

/**
 * TCF clients can implement ITCFLaunchContext to provide information about
 * workspace projects to TCF Launch Configuration.
 *
 * The information includes default values for launch configuration attributes,
 * list of executable binary files, etc.
 *
 * Since each project type can have its own methods to retrieve relevant information,
 * there should be implementation of this interface for each project type that support TCF.
 *
 * Implementation should be able to examine current IDE state (like active editor input source,
 * project explorer selection, etc.) and figure out an "active project".
 */
public class TCFLaunchContext {

    public static ITCFLaunchContext getLaunchContext(Object selection) {
        try {
            IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "launch_context");
            IExtension[] extensions = point.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                try {
                    Bundle bundle = Platform.getBundle(extensions[i].getNamespaceIdentifier());
                    bundle.start();
                    IConfigurationElement[] e = extensions[i].getConfigurationElements();
                    for (int j = 0; j < e.length; j++) {
                        String nm = e[j].getName();
                        if (nm.equals("class")) { //$NON-NLS-1$
                            Class<?> c = bundle.loadClass(e[j].getAttribute("name")); //$NON-NLS-1$
                            ITCFLaunchContext launch_context = (ITCFLaunchContext)c.newInstance();
                            if (selection != null) {
                                if (launch_context.isSupportedSelection(selection)) return launch_context;
                            }
                            else {
                                if (launch_context.isActive()) return launch_context;
                            }
                        }
                    }
                }
                catch (Throwable x) {
                    Activator.log("Cannot access launch context extension points", x);
                }
            }
        }
        catch (Exception x) {
            Activator.log("Cannot access launch context extension points", x);
        }
        return null;
    }

}
