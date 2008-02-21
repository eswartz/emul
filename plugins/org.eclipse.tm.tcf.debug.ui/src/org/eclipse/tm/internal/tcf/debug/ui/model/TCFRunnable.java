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
package org.eclipse.tm.internal.tcf.debug.ui.model;

import org.eclipse.debug.core.IRequest;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.tcf.protocol.Protocol;


public abstract class TCFRunnable implements Runnable {

    private final IRequest monitor;
    private final Display display;
    
    private boolean canceled;
    
    public TCFRunnable() {
        monitor = null;
        display = null;
    }

    public TCFRunnable(Display display, IRequest monitor) {
        this.monitor = monitor;
        this.display = display;
        Protocol.invokeLater(this);
    }

    public void cancel() {
        canceled = true;
        if (display == null) return;
        display.asyncExec(new Runnable() {
            public void run() {
                monitor.cancel();
                monitor.done();
            }
        });
    }

    public void done() {
        if (display == null) return;
        display.asyncExec(new Runnable() {
            public void run() {
                monitor.done();
            }
        });
    }
    
    public boolean isCanceled() {
        return canceled;
    }
}
