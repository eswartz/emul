/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class ImageCache {

    public static final String
        IMG_TARGET_TAB = "icons/target_tab.gif";

    private static final Map<String,ImageDescriptor> desc_cache = new HashMap<String,ImageDescriptor>();
    private static final Map<ImageDescriptor,Image> image_cache = new HashMap<ImageDescriptor,Image>();

    public static synchronized ImageDescriptor getImageDescriptor(String name) {
        if (name == null) return null;
        ImageDescriptor descriptor = desc_cache.get(name);
        if (descriptor == null) {
            Bundle bundle = Platform.getBundle("org.eclipse.tm.tcf.debug.ui");
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
            desc_cache.put(name, descriptor);
        }
        return descriptor;
    }

    public static synchronized Image getImage(ImageDescriptor desc) {
        Image image = image_cache.get(desc);
        if (image == null) {
            image = desc.createImage();
            image_cache.put(desc, image);
        }
        return image;
    }

    public static synchronized Image getImage(String name) {
        return getImage(getImageDescriptor(name));
    }
}
