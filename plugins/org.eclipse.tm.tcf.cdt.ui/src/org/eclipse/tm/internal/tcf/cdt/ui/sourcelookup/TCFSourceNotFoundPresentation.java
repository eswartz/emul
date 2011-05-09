/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui.sourcelookup;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceNotFoundElement;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.CSourceNotFoundEditorInput;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.tm.internal.tcf.debug.ui.model.ISourceNotFoundPresentation;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.ui.IEditorInput;

/**
 * Reuse CDT's source-not-found editor for TCF.
 */
@SuppressWarnings("restriction")
public class TCFSourceNotFoundPresentation implements ISourceNotFoundPresentation {

    final static class TCFCSourceNotFoundElement extends CSourceNotFoundElement {
        private final TCFModel fModel;

        private TCFCSourceNotFoundElement(IAdaptable element, ILaunchConfiguration cfg, String file) {
            super(element, cfg, file);
            fModel = (TCFModel) element.getAdapter(TCFModel.class);
        }

        TCFModel getModel() {
            return fModel;
        }
        
        @Override
        public String getDescription() {
            return getFile();
        }
        
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof TCFCSourceNotFoundElement)) return false;
            TCFCSourceNotFoundElement otherElement = (TCFCSourceNotFoundElement) other;
            return getFile().equals(otherElement.getFile()) && getModel() == otherElement.getModel();
        }
    }

    final static class TCFCSourceNotFoundEditorInput extends CSourceNotFoundEditorInput {
        public TCFCSourceNotFoundEditorInput(CSourceNotFoundElement element) {
            super(element);
        }
        
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof TCFCSourceNotFoundEditorInput)) return false;
            return getArtifact().equals(((TCFCSourceNotFoundEditorInput) other).getArtifact());
        }
    }

    public IEditorInput getEditorInput(Object element, ILaunchConfiguration cfg, String file) {
        if (element instanceof IAdaptable) {
            return new TCFCSourceNotFoundEditorInput(
                    new TCFCSourceNotFoundElement((IAdaptable) element, cfg, file));
        }
        return null;
    }

    public String getEditorId(IEditorInput input, Object element) {
        if (input instanceof CSourceNotFoundEditorInput) {
            return TCFCSourceNotFoundEditor.ID;
        }
        return null;
    }

}
