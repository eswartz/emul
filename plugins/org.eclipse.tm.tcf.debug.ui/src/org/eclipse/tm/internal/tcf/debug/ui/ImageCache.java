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
package org.eclipse.tm.internal.tcf.debug.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;
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
        IMG_THREAD_NOT_ACTIVE = "icons/thread_not_active.gif",
        IMG_THREAD_UNKNOWN_STATE = "icons/thread_not_active.gif",

        IMG_PROCESS_TERMINATED = "icons/full/obj16/debugtt_obj.gif",
        IMG_PROCESS_SUSPENDED = "icons/full/obj16/debugts_obj.gif",
        IMG_PROCESS_RUNNING = "icons/full/obj16/debugt_obj.gif",

        IMG_REGISTER = "icons/full/obj16/genericregister_obj.gif",

        IMG_VARIABLE = "icons/var_simple.gif",
        IMG_VARIABLE_POINTER = "icons/var_pointer.gif",
        IMG_VARIABLE_AGGREGATE = "icons/var_aggr.gif",

        IMG_SIGNALS = "icons/signals.gif",
        IMG_MEMORY_MAP = "icons/memory-map.gif",

        IMG_ARRAY_PARTITION = "icons/full/obj16/arraypartition_obj.gif",

        IMG_STACK_FRAME_SUSPENDED = "icons/full/obj16/stckframe_obj.gif",
        IMG_STACK_FRAME_RUNNING = "icons/full/obj16/stckframe_running_obj.gif",

        IMG_BREAKPOINT_ENABLED = "icons/full/obj16/brkp_obj.gif",
        IMG_BREAKPOINT_DISABLED = "icons/full/obj16/brkpd_obj.gif",
        IMG_BREAKPOINT_INSTALLED = "icons/ovr16/installed_ovr.gif",
        IMG_BREAKPOINT_CONDITIONAL = "icons/ovr16/conditional_ovr.gif",
        IMG_BREAKPOINT_WARNING = "icons/ovr16/warning_ovr.gif";

    private static final Map<String,ImageDescriptor> desc_cache = new HashMap<String,ImageDescriptor>();
    private static final Map<ImageDescriptor,Image> image_cache = new HashMap<ImageDescriptor,Image>();
    private static final Map<String,Map<ImageDescriptor,ImageDescriptor>> overlay_cache =
        new HashMap<String,Map<ImageDescriptor,ImageDescriptor>>();

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
                bundle = Platform.getBundle("org.eclipse.cdt.debug.ui");
                if (bundle != null){
                    URL url = FileLocator.find(bundle, new Path(name), null);
                    if (url != null) descriptor = ImageDescriptor.createFromURL(url);
                }
            }
            if (descriptor == null) {
                descriptor = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(name);
            }
            if (descriptor == null) {
                descriptor = ImageDescriptor.getMissingImageDescriptor();
            }
            desc_cache.put(name, descriptor);
        }
        return descriptor;
    }

    public static synchronized ImageDescriptor addOverlay(ImageDescriptor descriptor, String name) {
        if (descriptor == null || name == null) return descriptor;
        Map<ImageDescriptor,ImageDescriptor> map = overlay_cache.get(name);
        if (map == null) overlay_cache.put(name, map = new HashMap<ImageDescriptor,ImageDescriptor>());
        ImageDescriptor res = map.get(descriptor);
        if (res != null) return res;
        final ImageData base = descriptor.getImageData();
        final ImageData overlay = getImageDescriptor(name).getImageData();
        res = new CompositeImageDescriptor() {
            @Override
            protected void drawCompositeImage(int width, int height) {
                drawImage(base, 0, 0);
                drawImage(overlay, 0, 0);
            }
            @Override
            protected Point getSize() {
                return new Point(base.width, base.height);
            }
        };
        map.put(descriptor, res);
        return res;
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
