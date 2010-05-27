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
 * Target service implementation: run control (TCF name RunControl)
 */

#ifndef D_runctrl
#define D_runctrl

#include <config.h>
#include <framework/events.h>
#include <framework/context.h>
#include <framework/protocol.h>

/*
 * Lock run control: don't resume any thread while run control is locked.
 * Each call to run_ctrl_lock() increments lock counter.
 */
extern void run_ctrl_lock(void);

/*
 * Unlock run control: resume debuggee threads that are not intercepted.
 * Each call to run_ctrl_unlock() decrements lock counter, debuggee resumed when the counter reaches zero.
 */
extern void run_ctrl_unlock(void);

/*
 * Add "safe" event.
 * Stops debuggee threads.
 * Callback function 'done' will be called when threads are stopped and
 * it is safe to access debuggee memory, plant breakpoints, etc.
 * 'mem' is memory context, only threads that belong to that memory are stopped.
 * post_safe_event() uses run_ctrl_lock()/run_ctrl_unlock() to suspend/resume debuggee.
 */
extern void post_safe_event(Context * mem, EventCallBack * done, void * arg);

/*
 * Return 1 if all threads in debuggee are stopped and handling of incoming messages
 * is suspended and it is safe to access debuggee memory, plant breakpoints, etc.
 * 'mem' is memory context, only threads that belong to that memory are checked.
 * if 'mem' = 0, check all threads.
 */
extern int is_all_stopped(Context * mem);

/*
 * Terminate debug context - thread or process.
 * Returns 0 if no errors, otherwise returns -1 and sets errno.
 * Note: this function is asynchronous, it returns before context is terminated.
 */
extern int terminate_debug_context(Channel * c, Context * ctx);

/*
 * Suspend (stop and intercept) debug context - thread or process.
 * If "ctx" is a process, suspend all children.
 * RunControl.suspended event is sent if it was not sent before.
 * Returns 0 if no errors, otherwise returns -1 and sets errno.
 * Note: this function is asynchronous, it returns before contexts are suspended.
 */
extern int suspend_debug_context(Context * ctx);

/*
 * Initialize run control service.
 */
extern void ini_run_ctrl_service(Protocol * proto, TCFBroadcastGroup * bcg);

#endif /* D_runctrl */
