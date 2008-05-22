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
