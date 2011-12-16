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

import org.eclipse.debug.core.IRequest;
import org.eclipse.tm.tcf.protocol.Protocol;


public abstract class TCFRunnable implements Runnable {

    private final IRequest request;

    protected boolean done;

    public TCFRunnable(IRequest request) {
        this.request = request;
        Protocol.invokeLater(this);
    }

    public void done() {
        assert !done;
        done = true;
        request.done();
    }
}
