/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.tm.tcf.services.ILineNumbers;

/**
 * The TCF source lookup participant knows how to translate a ILineNumbers.CodeArea
 * into a source file name
 */
public class TCFSourceLookupParticipant extends AbstractSourceLookupParticipant {

    public String getSourceName(Object object) throws CoreException {
        if (object instanceof ILineNumbers.CodeArea) {
            ILineNumbers.CodeArea area = (ILineNumbers.CodeArea)object;
            return area.file;
        }
        return null;
    }
}
