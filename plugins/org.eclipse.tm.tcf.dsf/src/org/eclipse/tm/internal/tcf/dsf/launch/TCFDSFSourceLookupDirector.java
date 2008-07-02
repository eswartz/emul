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
package org.eclipse.tm.internal.tcf.dsf.launch;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

/**
 * TCF/DSF source lookup director.
 * For TCF/DSF source lookup there is one source lookup participant.
 */
public class TCFDSFSourceLookupDirector extends AbstractSourceLookupDirector {

    public void initializeParticipants() {
        addParticipants(new ISourceLookupParticipant[] { new TCFDSFSourceLookupParticipant() });
    }
}
