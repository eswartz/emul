/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Event queue manager.
 * Event is a data pointer plus a function pointer (a.k.a. event handler).
 *
 * Posting event means placing event into event queue.
 * Dispatching event means removing event from the queue and then calling
 * event function with event data as argument.
 *
 * All events are dispatched by single thread - dispatch thread. This makes it safe
 * to access global data structures from event handlers without further synchronization,
 * while allows for high level of concurrency.
 */

#ifndef D_events
#define D_events

typedef void EventCallBack(void *);

/*
 * Causes event to have its handler
 * function called in the dispatch thread of the framework.
 * Events are dispatched in same order as queued.
 * If post_event is called from the dispatching thread
 * the handler will still be deferred until
 * all pending events have been processed.
 *
 * This function can be invoked from any thread.
 *
 * handler - the function that should be executed asynchronously.
 * arg - pointer to event data.
 */
extern void post_event(EventCallBack * handler, void * arg);

/*
 * Causes event to have its handler
 * function called in the dispatch thread of the framework.
 * The event is dispatched after given delay.
 *
 * This function can be invoked from any thread.
 *
 * handler - the function that should be executed asynchronously.
 * arg - pointer to event data.
 * us_delay - microseconds to delay event dispatch.
 */
extern void post_event_with_delay(EventCallBack * handler, void * arg, unsigned long us_delay);

/*
 * Cancel pending event with matching 'handler' and 'arg', or if event
 * is not pending and 'wait' is true then wait for matching event to
 * be posted.  Can only be called from the dispatch thread.  Returns
 * true if a posted event was cancelled.
 */
extern int cancel_event(EventCallBack * handler, void * arg, int wait);

/*
 * Returns true if the calling thread is TCF event dispatch thread.
 * Use this call the ensure that a given task is being executed (or not being)
 * on dispatch thread.
 */
extern int is_dispatch_thread(void);

/*
 * Run TCF event loop.
 * Should be called from main().
 */
extern void run_event_loop(void);

/*
 * Cancel event loop.
 * The function causes run_event_loop() to stop event dispatching and return.
 */
extern void cancel_event_loop(void);

/*
 * Initialize event queue.
 * Should be called from main before run_event_loop().
 */
extern void ini_events_queue(void);

#endif /* D_events */
