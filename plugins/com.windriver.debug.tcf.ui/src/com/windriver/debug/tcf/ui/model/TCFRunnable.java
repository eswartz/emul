/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.ui.model;

import org.eclipse.debug.core.IRequest;
import org.eclipse.swt.widgets.Display;

import com.windriver.tcf.api.protocol.Protocol;

public abstract class TCFRunnable implements Runnable {

    private final IRequest monitor;
    private final Display display;

    public TCFRunnable(Display display, IRequest monitor) {
        this.monitor = monitor;
        this.display = display;
        Protocol.invokeLater(this);
    }

    public void cancel() {
        display.asyncExec(new Runnable() {
            public void run() {
                monitor.cancel();
                monitor.done();
            }
        });
    }

    public void done() {
        display.asyncExec(new Runnable() {
            public void run() {
                monitor.done();
            }
        });
    }
}
