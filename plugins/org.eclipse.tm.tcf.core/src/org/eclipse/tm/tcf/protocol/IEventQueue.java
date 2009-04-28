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
package org.eclipse.tm.tcf.protocol;

/**
 * Clients of stand-alone version the framework should implement this interface and call Protocol.setEventQueue.
 * Eclipse based clients don't need to implement IEventQueue since the implementation is provide by TCF bundle activator.
 * 
 * Implementation should encapsulate a queue and asynchronous event dispatch machinery, which
 * extracts events from the queue and dispatches them by calling event's run() method.
 * The implementation is used by framework to queue and dispatch all events.
 */
public interface IEventQueue {

    /**
     * Causes <code>runnable</code> to have its <code>run</code>
     * method called in the dispatch thread of this event queue.
     * Events are dispatched in same order as queued.
     *
     * @param runnable  the <code>Runnable</code> whose <code>run</code>
     *                  method should be executed asynchronously.
     */
    void invokeLater(Runnable runnable);

    /**
     * Returns true if the calling thread is this event queue's dispatch thread.
     * Use this call the ensure that a given task is being executed (or not being) on dispatch thread.
     *
     * @return true if running on the dispatch thread.
     */
    boolean isDispatchThread();

    /**
     * Get current level of queue congestion.
     * 
     * @return integer value in range –100..100, where –100 means no pending
     *         messages (no traffic), 0 means optimal load, and positive numbers
     *         indicate level of congestion.
     */
    int getCongestion();
}
