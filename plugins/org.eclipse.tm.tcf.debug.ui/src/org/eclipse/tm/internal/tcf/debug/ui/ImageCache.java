/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;

public class ImageCache {

    private static final Map<String,ImageDescriptor> image_cache = new HashMap<String,ImageDescriptor>();

    public static ImageDescriptor getImageDescriptor(String name) {
        if (name == null) return null;
        ImageDescriptor descriptor = image_cache.get(name);
        if (descriptor == null) {
            Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
            if (bundle != null){
                URL url = FileLocator.find(bundle, new Path(name), null);
                if (url != null) descriptor = ImageDescriptor.createFromURL(url);
            }
            if (descriptor == null) {
                bundle = Platform.getBundle("org.eclipse.debug.ui");
                if (bundle != null){
                    URL url = FileLocator.find(bundle, new Path(name), null);
                    if (url != null) descriptor = ImageDescriptor.createFromURL(url);
                }
            }
            if (descriptor == null) {
                descriptor = ImageDescriptor.getMissingImageDescriptor();
            }
            image_cache.put(name, descriptor);
        }
        return descriptor;
    }
}
