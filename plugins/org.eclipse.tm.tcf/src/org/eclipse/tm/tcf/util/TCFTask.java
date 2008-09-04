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
package org.eclipse.tm.tcf.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * A <tt>TCFTask</tt> is an utility class that represents the result of an asynchronous
 * communication over TCF framework.  Methods are provided to check if the communication is
 * complete, to wait for its completion, and to retrieve the result of
 * the communication.
 * 
 * TCFTask is useful when communication is requested by a thread other then TCF dispatch thread.
 * If client has a global state, for example, cached remote data, multithreading should be avoided,
 * because it is extremely difficult to ensure absence of racing conditions or deadlocks in such environment.
 * Such clients should consider message driven design, see TCFDataCache and its usage as an example.    
 * 
 * If a client is extending TCFTask it should implement run() method to perform actual communications. 
 */
public abstract class TCFTask<V> implements Runnable, Future<V> {
    
    private V result;
    private Throwable error;
    private boolean canceled;
    
    public TCFTask() {
        Protocol.invokeLater(new Runnable() {
            public void run() {
                try {
                    TCFTask.this.run();
                }
                catch (Throwable x) {
                    if (result == null && error == null) error(x);
                }
            }
        });
    }
    
    public synchronized void done(V result) {
        assert Protocol.isDispatchThread();
        assert result != null;
        if (canceled) return;
        assert this.error == null;
        assert this.result == null;
        this.result = result;
        notifyAll();
    }
    
    public synchronized void error(Throwable error) {
        assert Protocol.isDispatchThread();
        assert error != null;
        if (canceled) return;
        assert this.error == null;
        assert this.result == null;
        this.error = error;
        notifyAll();
    }

    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        assert Protocol.isDispatchThread();
        if (isDone()) return false;
        canceled = true;
        error = new CancellationException();
        notifyAll();
        return true;
    }

    public synchronized V get() throws InterruptedException, ExecutionException {
        assert !Protocol.isDispatchThread();
        while (!isDone()) wait();
        assert error != null || result != null;
        if (error instanceof ExecutionException) throw (ExecutionException)error;
        if (error instanceof InterruptedException) throw (InterruptedException)error;
        if (error != null) throw new ExecutionException(error);
        return result;
    }
    
    public synchronized V getE() {
        assert !Protocol.isDispatchThread();
        while (!isDone()) {
            try {
                wait();
            }
            catch (InterruptedException x) {
                throw new Error(x);
            }
        }
        assert error != null || result != null;
        if (error instanceof Error) throw (Error)error;
        if (error != null) throw new Error(error);
        return result;
    }

    public synchronized V getIO() throws IOException {
        assert !Protocol.isDispatchThread();
        while (!isDone()) {
            try {
                wait();
            }
            catch (InterruptedException x) {
                throw new InterruptedIOException();
            }
        }
        assert error != null || result != null;
        if (error instanceof IOException) throw (IOException)error;
        if (error != null) {
            IOException y = new IOException();
            y.initCause(error);
            throw y;
        }
        return result;
    }

    public synchronized V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        unit.toNanos(timeout);
        // TODO: implement TCFTask.get() with timeout
        assert false;
        return null;
    }

    public synchronized boolean isCancelled() {
        return canceled;
    }

    public synchronized boolean isDone() {
        return error != null || result != null;
    }
}
