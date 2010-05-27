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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.util.TCFTask;

public abstract class TCFDebugTask<V> extends TCFTask<V> {

    public synchronized V getD() throws DebugException {
        assert !Protocol.isDispatchThread();
        while (!isDone()) {
            try {
                wait();
            }
            catch (InterruptedException x) {
                throw new DebugException(new Status(
                        IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
                        "Debugger requiest interrupted", x));
            }
        }
        assert isDone();
        Throwable x = getError();
        if (x instanceof DebugException) throw (DebugException)x;
        if (x != null) throw new DebugException(new Status(
                IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
                "Debugger requiest failed", x));
        return getResult();
    }

    public void error(String msg) {
        error(new DebugException(new Status(
                IStatus.ERROR, Activator.PLUGIN_ID, DebugException.REQUEST_FAILED,
                msg, null)));
    }
}
