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
package org.eclipse.tm.internal.tcf.debug.ui;

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
        IMG_TCF = "icons/tcf.gif",
        IMG_TARGET_TAB = "icons/target_tab.gif",
        IMG_TARGET_WIZARD = "icons/full/wizban/debug_wiz.png",
        IMG_ARGUMENTS_TAB = "icons/arguments_tab.gif",
        IMG_ATTRIBUTE = "icons/attribute.gif",
        IMG_PATH = "icons/path.gif",

        IMG_THREAD_TERMINATED = "icons/full/obj16/threadt_obj.gif",
        IMG_THREAD_SUSPENDED = "icons/full/obj16/threads_obj.gif",
        IMG_THREAD_RUNNNIG = "icons/full/obj16/thread_obj.gif",

        IMG_PROCESS_TERMINATED = "icons/full/obj16/debugtt_obj.gif",
        IMG_PROCESS_SUSPENDED = "icons/full/obj16/debugts_obj.gif",
        IMG_PROCESS_RUNNING = "icons/full/obj16/debugt_obj.gif",

        IMG_REGISTER = "icons/full/obj16/genericregister_obj.gif",

        IMG_VARIABLE = "icons/full/obj16/genericvariable_obj.gif",

        IMG_SIGNALS = "icons/signals.gif",

        IMG_ARRAY_PARTITION = "icons/full/obj16/arraypartition_obj.gif",

        IMG_STACK_FRAME_SUSPENDED = "icons/full/obj16/stckframe_obj.gif",
        IMG_STACK_FRAME_RUNNING = "icons/full/obj16/stckframe_running_obj.gif",

        IMG_BREAKPOINT_ENABLED = "icons/full/obj16/brkp_obj.gif",
        IMG_BREAKPOINT_DISABLED = "icons/full/obj16/brkpd_obj.gif";

    private static final Map<String,ImageDescriptor> desc_cache = new HashMap<String,ImageDescriptor>();
    private static final Map<ImageDescriptor,Image> image_cache = new HashMap<ImageDescriptor,Image>();

    public static synchronized ImageDescriptor getImageDescriptor(String name) {
        if (name == null) return null;
        ImageDescriptor descriptor = desc_cache.get(name);
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
