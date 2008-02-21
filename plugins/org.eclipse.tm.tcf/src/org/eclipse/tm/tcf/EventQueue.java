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
package org.eclipse.tm.tcf;

import java.util.LinkedList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tm.tcf.protocol.IEventQueue;


/**
 * Implementation of Target Communication Framework event queue.
 * This implementation is intended for Eclipse environment.
 */
class EventQueue implements IEventQueue, Runnable {

    private final boolean debug = Platform.inDebugMode();
    private final LinkedList<Runnable> queue = new LinkedList<Runnable>();
    private final Thread thread;
    private boolean waiting;
    private int job_cnt;

    EventQueue() {
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("TCF Event Dispatch");
        // Need to monitor jobs to detect congestion
        Job.getJobManager().addJobChangeListener(new IJobChangeListener() {

            public void aboutToRun(IJobChangeEvent event) {
                job_cnt++;
            }

            public void awake(IJobChangeEvent event) {
                //job_cnt++;
            }

            public void done(IJobChangeEvent event) {
                job_cnt--;
                if (Job.getJobManager().isIdle()) job_cnt = 0;
            }

            public void running(IJobChangeEvent event) {
            }

            public void scheduled(IJobChangeEvent event) {
            }

            public void sleeping(IJobChangeEvent event) {
                //job_cnt--;
            }
        });
    }
    
    void start() {
        thread.start();
    }

    private void error(Throwable x) {
        if (debug) x.printStackTrace();
        Activator.log("Unhandled excetion in TCF event dispatch", x);
    }

    public void run() {
        for (;;) {
            try {
                Runnable r = null;
                synchronized (this) {
                    while (queue.isEmpty()) {
                        waiting = true;
                        wait();
                    }
                    r = queue.removeFirst();
                }
                r.run();
            }
            catch (Throwable x) {
                error(x);
            }
        }
    }

    public synchronized void invokeLater(final Runnable r) {
        queue.add(r);
        if (waiting) {
            waiting = false;
            notifyAll();
        }
    }

    public boolean isDispatchThread() {
        return Thread.currentThread() == thread;
    }

    public synchronized int getCongestion() {
        int l0 = job_cnt / 100 - 100;
        int l1 = queue.size() / 100 - 100;
        if (l1 > l0) l0 = l1;
        if (l0 > 100) l0 = 100;
        return l0;
    }
}