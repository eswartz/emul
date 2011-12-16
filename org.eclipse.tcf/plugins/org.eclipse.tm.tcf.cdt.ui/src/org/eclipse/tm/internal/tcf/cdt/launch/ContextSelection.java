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
package org.eclipse.tm.internal.tcf.cdt.launch;

public class ContextSelection {
    public String fPeerId;
    public String fContextId;
    public boolean fIsAttached;
    public ContextSelection(String peerId, String contextId) {
        this(peerId, contextId, true);
    }
    public ContextSelection(String peerId, String contextId, boolean isAttached) {
        fPeerId = peerId;
        fContextId = contextId;
        fIsAttached = isAttached;
    }
}
