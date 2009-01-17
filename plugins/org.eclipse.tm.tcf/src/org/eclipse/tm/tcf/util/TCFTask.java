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

import org.eclipse.tm.tcf.protocol.IChannel;
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
 * The run() method will be execute by TCF dispatch thread, and client code should then call either done() or
 * error() to indicate that task computations are complete.  
 */
public abstract class TCFTask<V> implements Runnable, Future<V> {
    
    private V result;
    private boolean done;
    private Throwable error;
    private boolean canceled;
    private IChannel channel;
    private IChannel.IChannelListener channel_listener;
    
    /**
     * Construct a TCF task object and schedule it for execution.
     */
    public TCFTask() {
        Protocol.invokeLater(new Runnable() {
            public void run() {
                try {
                    TCFTask.this.run();
                }
                catch (Throwable x) {
                    if (!done && error == null) error(x);
                }
            }
        });
    }
    
    /**
     * Construct a TCF task object and schedule it for execution.
     * The task will be canceled if it is not completed after given timeout.
     * @param timeout - max time in milliseconds.
     */
    public TCFTask(long timeout) {
        Protocol.invokeLater(new Runnable() {
            public void run() {
                try {
                    TCFTask.this.run();
                }
                catch (Throwable x) {
                    if (!done && error == null) error(x);
                }
            }
        });
        Protocol.invokeLater(timeout, new Runnable() {
            public void run() {
                cancel(true);
            }
        });
    }
    
    /**
     * Construct a TCF task object and schedule it for execution.
     * The task will be canceled if the given channel is closed or
     * terminated while the task is in progress.
     * @param channel
     */
    public TCFTask(final IChannel channel) {
        Protocol.invokeLater(new Runnable() {
            public void run() {
                try {
                    if (channel.getState() != IChannel.STATE_OPEN) throw new Exception("Channel is closed");
                    TCFTask.this.channel = channel;
                    channel_listener = new IChannel.IChannelListener() {

                        public void congestionLevel(int level) {
                        }

                        public void onChannelClosed(final Throwable error) {
                            cancel(true);
                        }

                        public void onChannelOpened() {
                        }
                    };
                    channel.addChannelListener(channel_listener);
                    TCFTask.this.run();
                }
                catch (Throwable x) {
                    if (!done && error == null) error(x);
                }
            }
        });
    }
    
    /**
     * Set a result of this task and notify all threads waiting for the task to complete.
     * The method is supposed to be called in response to executing of run() method of this task.
     * 
     * @param result - the computed result
     */
    public synchronized void done(V result) {
        assert Protocol.isDispatchThread();
        if (canceled) return;
        assert !done;
        assert this.error == null;
        assert this.result == null;
        this.result = result;
        done = true;
        if (channel != null) channel.removeChannelListener(channel_listener);
        notifyAll();
    }
    
    /**
     * Set a error and notify all threads waiting for the task to complete.
     * The method is supposed to be called in response to executing of run() method of this task.
     * 
     * @param error - computation error.
     */
    public synchronized void error(Throwable error) {
        assert Protocol.isDispatchThread();
        assert error != null;
        if (canceled) return;
        assert this.error == null;
        assert this.result == null;
        assert !done;
        this.error = error;
        if (channel != null) channel.removeChannelListener(channel_listener);
        notifyAll();
    }

    /**
     * Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, already been canceled,
     * or could not be canceled for some other reason. If successful,
     * and this task has not started when <tt>cancel</tt> is called,
     * this task should never run.  If the task has already started,
     * then the <tt>mayInterruptIfRunning</tt> parameter determines
     * whether the thread executing this task should be interrupted in
     * an attempt to stop the task.
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
     * task should be interrupted; otherwise, in-progress tasks are allowed
     * to complete
     * @return <tt>false</tt> if the task could not be canceled,
     * typically because it has already completed normally;
     * <tt>true</tt> otherwise
     */
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        assert Protocol.isDispatchThread();
        if (isDone()) return false;
        canceled = true;
        error = new CancellationException();
        if (channel != null) channel.removeChannelListener(channel_listener);
        notifyAll();
        return true;
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return the computed result
     * @throws CancellationException if the computation was canceled
     * @throws ExecutionException if the computation threw an
     * exception
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     */
    public synchronized V get() throws InterruptedException, ExecutionException {
        assert !Protocol.isDispatchThread();
        while (!isDone()) wait();
        if (error != null) {
            if (error instanceof ExecutionException) throw (ExecutionException)error;
            if (error instanceof InterruptedException) throw (InterruptedException)error;
            throw new ExecutionException("TCF task aborted", error);
        }
        return result;
    }
    
    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return the computed result
     * @throws Error if the computation was canceled or threw an exception
     */
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
        if (error != null) {
            if (error instanceof Error) throw (Error)error;
            throw new Error("TCF task aborted", error);
        }
        return result;
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return the computed result
     * @throws IOException if the computation was canceled or threw an exception
     */
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
        if (error != null) {
            if (error instanceof IOException) throw (IOException)error;
            IOException y = new IOException("TCF task aborted");
            y.initCause(error);
            throw y;
        }
        return result;
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException if the computation was canceled
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     * @throws TimeoutException if the wait timed out
     */
    public synchronized V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        assert !Protocol.isDispatchThread();
        if (!isDone()) {
            wait(unit.toMillis(timeout));
            if (!isDone()) throw new TimeoutException();
        }
        if (error != null) {
            if (error instanceof InterruptedException) throw (InterruptedException)error;
            if (error instanceof ExecutionException) throw (ExecutionException)error;
            if (error instanceof TimeoutException) throw (TimeoutException)error;
            throw new ExecutionException("TCF task aborted", error);
        }
        return result;
    }

    /**
     * Returns <tt>true</tt> if this task was canceled before it completed
     * normally.
     *
     * @return <tt>true</tt> if task was canceled before it completed
     */
    public synchronized boolean isCancelled() {
        return canceled;
    }

    /**
     * Returns <tt>true</tt> if this task completed.  
     *
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * <tt>true</tt>.
     * 
     * @return <tt>true</tt> if this task completed.
     */
    public synchronized boolean isDone() {
        return error != null || done;
    }
    
    /**
     * Return task execution error if any.
     * @return Throwable object or null
     */
    protected Throwable getError() {
        return error;
    }
    
    /**
     * Return task execution result if any.
     * @return result object
     */
    protected V getResult() {
        return result;
    }
}
