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
package com.windriver.tcf.dsf.core.launch;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.dd.dsf.concurrent.DsfExecutor;

import com.windriver.tcf.api.protocol.Protocol;

public class TCFDSFExecuter extends AbstractExecutorService implements DsfExecutor {

    private class ScheduledFutureTask<V> extends FutureTask<V> implements ScheduledFuture<V> {

        private long time; // Milliseconds
        private final int id;
        private final long period; // Milliseconds

        public ScheduledFutureTask(long delay, long period, Runnable runnable, V result) {
            super(runnable, result);
            time = System.currentTimeMillis() + delay;
            id = sf_count++;
            this.period = period;
        }

        public ScheduledFutureTask(long delay, Callable<V> callable) {
            super(callable);
            time = System.currentTimeMillis() + delay;
            id = sf_count++;
            period = 0;
        }

        public long getDelay(TimeUnit unit) {
            return unit.convert(time - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        public int compareTo(Delayed o) {
            if (o == this) return 0;
            ScheduledFutureTask<?> x = (ScheduledFutureTask<?>)o;
            if (time < x.time) return -1;
            if (time > x.time) return +1;
            if (id < x.id) return -1;
            if (id > x.id) return +1;
            assert false;
            return 0;
        }

        public void run() {
            if (period == 0) {
                super.run();
            }
            else {
                boolean ok = super.runAndReset();
                synchronized (TCFDSFExecuter.this) {
                    // Reschedule if not canceled and not shutdown
                    if (ok && !is_shutdown) {
                        time = period > 0 ? time + period : System.currentTimeMillis() - period;
                        queue.add(this);
                        notify();
                    }
                }
            }
        }
    }

    private static int sf_count = 0;
    private final TreeSet<ScheduledFutureTask<?>> queue = new TreeSet<ScheduledFutureTask<?>>();
    private final Thread thread;
    private boolean is_shutdown;
    private boolean is_terminated;

    public TCFDSFExecuter() {
        thread = new Thread(new Runnable() {
            public void run() {
                synchronized (TCFDSFExecuter.this) {
                    try {
                        while (true) {
                            if (queue.isEmpty()) {
                                if (is_shutdown) break;
                                TCFDSFExecuter.this.wait();
                            }
                            else {
                                long time = System.currentTimeMillis();
                                ScheduledFutureTask<?> s = queue.first();
                                if (s.time <= time) {
                                    queue.remove(s);
                                    Protocol.invokeLater(s);
                                }
                                else {
                                    TCFDSFExecuter.this.wait(s.time - time);
                                }
                            }
                        }
                    }
                    catch (Throwable x) {
                        x.printStackTrace();
                    }
                    is_terminated = true;
                }
            }
        });
        thread.setName("TCF Future Task Scheduler");
        thread.start();
    }

    public boolean isInExecutorThread() {
        return Protocol.isDispatchThread();
    }

    public synchronized ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (command == null || unit == null) throw new NullPointerException();
        if (is_shutdown) throw new RejectedExecutionException();
        delay = unit.toMillis(delay);
        ScheduledFutureTask<Boolean> s = new ScheduledFutureTask<Boolean>(delay, 0, command, Boolean.TRUE);
        queue.add(s);
        notify();
        return s;
    }

    public synchronized <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        if (callable == null || unit == null) throw new NullPointerException();
        if (is_shutdown) throw new RejectedExecutionException();
        delay = unit.toMillis(delay);
        ScheduledFutureTask<V> s = new ScheduledFutureTask<V>(delay, callable);
        queue.add(s);
        notify();
        return s;
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
            long initialDelay, long period, TimeUnit unit) {
        if (command == null || unit == null) throw new NullPointerException();
        if (is_shutdown) throw new RejectedExecutionException();
        if (period <= 0) throw new RejectedExecutionException();
        ScheduledFutureTask<Boolean> s = new ScheduledFutureTask<Boolean>(
                unit.toMillis(initialDelay), unit.toMillis(period), command, Boolean.TRUE);
        queue.add(s);
        notify();
        return s;
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
            long initialDelay, long delay, TimeUnit unit) {
        if (command == null || unit == null) throw new NullPointerException();
        if (is_shutdown) throw new RejectedExecutionException();
        if (delay <= 0) throw new RejectedExecutionException();
        ScheduledFutureTask<Boolean> s = new ScheduledFutureTask<Boolean>(
                unit.toMillis(initialDelay), -unit.toMillis(delay), command, Boolean.TRUE);
        queue.add(s);
        notify();
        return s;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        thread.join(unit.toMillis(timeout));
        return is_terminated;
    }

    public synchronized boolean isShutdown() {
        return is_shutdown;
    }

    public synchronized boolean isTerminated() {
        return is_terminated;
    }

    public synchronized void shutdown() {
        is_shutdown = true;
        notify();
    }

    public synchronized List<Runnable> shutdownNow() {
        List<Runnable> res = new ArrayList<Runnable>(queue);
        queue.clear();
        is_shutdown = true;
        notify();
        return res;
    }

    public synchronized void execute(Runnable command) {
        if (is_shutdown) throw new RejectedExecutionException();
        Protocol.invokeLater(command);
    }
}
