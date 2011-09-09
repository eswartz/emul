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
package org.eclipse.tm.internal.tcf.debug.launch;

import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.tm.tcf.services.ILineNumbers;

/**
 * TCF source lookup director.
 * For TCF source lookup there is one source lookup participant.
 */
public class TCFSourceLookupDirector extends AbstractSourceLookupDirector {

    public static Object lookup(ISourceLocator locator, Object element) {
        Object source_element = null;
        if (locator instanceof ISourceLookupDirector) {
            if (element instanceof ILineNumbers.CodeArea) {
                String file_name = TCFSourceLookupParticipant.toFileName((ILineNumbers.CodeArea)element);
                if (file_name != null) source_element = ((ISourceLookupDirector)locator).getSourceElement(file_name);
            }
            else {
                source_element = ((ISourceLookupDirector)locator).getSourceElement(element);
            }
        }
        else if (element instanceof IStackFrame) {
            source_element = locator.getSourceElement((IStackFrame)element);
        }
        return source_element;
    }

    public void initializeParticipants() {
        addParticipants(new ISourceLookupParticipant[] { new TCFSourceLookupParticipant() });
    }
}
