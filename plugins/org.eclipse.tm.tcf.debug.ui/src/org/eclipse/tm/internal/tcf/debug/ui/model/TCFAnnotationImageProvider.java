/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

public class TCFAnnotationImageProvider implements IAnnotationImageProvider {

    public Image getManagedImage(Annotation annotation) {
        return ((TCFAnnotationManager.TCFAnnotation)annotation).image;
    }

    public String getImageDescriptorId(Annotation annotation) {
        return null;
    }

    public ImageDescriptor getImageDescriptor(String imageDescritporId) {
        return null;
    }
}
