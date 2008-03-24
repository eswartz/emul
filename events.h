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

extern void post_event(EventCallBack * handler, void * arg);
extern void post_event_with_delay(EventCallBack * handler, void * arg, unsigned long us_delay);

/* Cancel pending event with matching 'handler' and 'arg', or if event
 * is not pending and 'wait' is true then wait for matching event to
 * be posted.  Can only be called from the dispatch thread.  Returns
 * true if a posted event was cancelled. */
extern int cancel_event(EventCallBack * handler, void * arg, int wait);

extern int is_dispatch_thread(void);

extern void run_event_loop(void);

extern void ini_events_queue(void);

#endif
