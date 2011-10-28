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
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpoint;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpointsModel;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;


public class TCFModelPresentation implements IDebugModelPresentation {

    private final Collection<ILabelProviderListener> listeners = new HashSet<ILabelProviderListener>();

    private static final TCFModelPresentation default_instance = new TCFModelPresentation();

    public static TCFModelPresentation getDefault() {
        return default_instance;
    }

    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    public void dispose() {
    }

    public void computeDetail(IValue value, IValueDetailListener listener) {
    }

    public Image getImage(Object element) {
        ImageDescriptor descriptor = null;
        if (element instanceof TCFBreakpoint) {
            final TCFBreakpoint breakpoint = (TCFBreakpoint)element;
            descriptor = ImageCache.getImageDescriptor(ImageCache.IMG_BREAKPOINT_DISABLED);
            try {
                if (breakpoint.isEnabled()) {
                    descriptor = new TCFTask<ImageDescriptor>() {
                        public void run() {
                            boolean installed = false;
                            boolean warning = false;
                            boolean error = false;
                            ImageDescriptor d = ImageCache.getImageDescriptor(ImageCache.IMG_BREAKPOINT_ENABLED);
                            for (Map<String,Object> map : Activator.getAnnotationManager().getBreakpointStatus(breakpoint).values()) {
                                if (map != null) {
                                    if ((String)map.get(IBreakpoints.STATUS_ERROR) != null) error = true;
                                    Object planted = map.get(IBreakpoints.STATUS_INSTANCES);
                                    if (planted != null) {
                                        @SuppressWarnings("unchecked")
                                        Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)planted;
                                        for (Map<String,Object> m : list) {
                                            if (m.get(IBreakpoints.INSTANCE_ERROR) == null) installed = true;
                                            else warning = true;
                                        }
                                    }
                                }
                            }
                            if (warning) d = ImageCache.addOverlay(d, ImageCache.IMG_BREAKPOINT_WARNING, 9, 8);
                            if (installed) d = ImageCache.addOverlay(d, ImageCache.IMG_BREAKPOINT_INSTALLED, 0, 8);
                            if (error) d = ImageCache.addOverlay(d, ImageCache.IMG_BREAKPOINT_ERROR, 9, 8);
                            done(d);
                        }
                    }.getE();
                }
                if (breakpoint.getMarker().getAttribute(TCFBreakpointsModel.ATTR_CONDITION, null) != null) {
                    descriptor = ImageCache.addOverlay(descriptor, ImageCache.IMG_BREAKPOINT_CONDITIONAL);
                }
            }
            catch (Throwable x) {
            }
        }
        if (descriptor != null) return ImageCache.getImage(descriptor);
        return null;
    }

    public String getText(Object element) {
        String text = null;
        if (element instanceof TCFBreakpoint) {
            final TCFBreakpoint breakpoint = (TCFBreakpoint)element;
            text = breakpoint.getText();
            String status = new TCFTask<String>() {
                public void run() {
                    done(Activator.getAnnotationManager().getBreakpointStatusText(breakpoint));
                }
            }.getE();
            if (status != null) text += " (" + status + ")";
        }
        return text;
    }

    public void setAttribute(String attribute, Object value) {
    }

    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    public String getEditorId(IEditorInput input, Object element) {
        String id = null;
        if (input != null) {
            IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
            IEditorDescriptor descriptor = registry.getDefaultEditor(input.getName());
            if (descriptor != null) id = descriptor.getId();
        }
        return id;
    }

    public IEditorInput getEditorInput(Object element) {
        if (element instanceof ILineBreakpoint) {
            element = ((ILineBreakpoint)element).getMarker();
        }
        if (element instanceof IMarker) {
            element = ((IMarker)element).getResource();
        }
        if (element instanceof IFile) {
            return new FileEditorInput((IFile)element);
        }
        if (element instanceof IStorage) {
            IPath fullPath = ((IStorage)element).getFullPath();
            URI uri = URIUtil.toURI(fullPath);
            if (uri != null) {
                try {
                    return new FileStoreEditorInput(EFS.getStore(uri));
                }
                catch (CoreException e) {
                    Activator.log(e);
                }
            }
        }
        return null;
    }
}
