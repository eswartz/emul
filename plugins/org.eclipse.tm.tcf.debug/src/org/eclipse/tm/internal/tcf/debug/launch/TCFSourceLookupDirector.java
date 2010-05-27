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

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

/**
 * TCF source lookup director.
 * For TCF source lookup there is one source lookup participant.
 */
public class TCFSourceLookupDirector extends AbstractSourceLookupDirector {

    public void initializeParticipants() {
        addParticipants(new ISourceLookupParticipant[] { new TCFSourceLookupParticipant() });
    }
}
