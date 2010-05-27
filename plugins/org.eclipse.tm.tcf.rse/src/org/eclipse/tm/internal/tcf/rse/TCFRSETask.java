/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Martin Oberhuber (Wind River) - [238564] Adopt TM 3.0 APIs
 *     Uwe Stieber (Wind River) - [271227] Fix compiler warnings in org.eclipse.tm.tcf.rse
 *     Uwe Stieber (Wind River) - [273572] SystemMessage contains exception class name instead of error message
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.tm.tcf.util.TCFTask;

public abstract class TCFRSETask<V> extends TCFTask<V> {

    public V get(IProgressMonitor monitor, String task_name)
            throws InterruptedException, ExecutionException {
        monitor.beginTask(task_name, 1);
        try {
            return get();
        }
        finally {
            monitor.done();
        }
    }

    public V getS(IProgressMonitor monitor, String task_name) throws SystemMessageException {
        if (monitor != null) monitor.beginTask(task_name, 1);
        try {
            return get();
        }
        catch (Throwable e) {
            if (e instanceof SystemMessageException) throw (SystemMessageException)e;
            SystemMessage m = new SystemMessage("TCF", "C", "0001", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    SystemMessage.ERROR,
                    "TCF task aborted".equals(e.getMessage()) && e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), //$NON-NLS-1$
                    ""); //$NON-NLS-1$
            throw new SystemMessageException(m);
        }
        finally {
            if (monitor != null) monitor.done();
        }
    }

    public V getI(IProgressMonitor monitor, String task_name) throws InvocationTargetException, InterruptedException {
        if (monitor != null) monitor.beginTask(task_name, 1);
        try {
            return get();
        }
        catch (Throwable e) {
            if (e instanceof InvocationTargetException) throw (InvocationTargetException)e;
            if (e instanceof InterruptedException) throw (InterruptedException)e;
            throw new InvocationTargetException(e);
        }
        finally {
            if (monitor != null) monitor.done();
        }
    }
}
