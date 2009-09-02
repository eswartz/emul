/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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

#include "config.h"
#include "events.h"
#include "context.h"
#include "protocol.h"

/*
 * Add "safe" event.
 * Temporary suspends handling of incoming messages and stops all debuggee threads.
 * Callback function 'done' will be called when everything is stopped and
 * it is safe to access debuggee memory, plant breakpoints, etc.
 * 'mem' is memory ID, only threads that belong to that memory are stopped.
 * if 'mem' = 0, stopp all threads.
 */
#if SERVICE_RunControl
extern void post_safe_event(int mem, EventCallBack * done, void * arg);
#else
#define post_safe_event post_event
#endif

/*
 * Return 1 if all threads in debuggee are stopped and handling of incoming messages
 * is suspended and it is safe to access debuggee memory, plant breakpoints, etc.
 * 'mem' is memory ID, only threads that belong to that memory are checked.
 * if 'mem' = 0, check all threads.
 */
extern int is_all_stopped(pid_t mem);

/*
 * Terminate debug context - thread or process.
 * Returns 0 if no errors, otherwise returns -1 and sets errno.
 */
extern int terminate_debug_context(TCFBroadcastGroup * bcg, Context * ctx);

/*
 * Initialize run control service.
 */
extern void ini_run_ctrl_service(Protocol * proto, TCFBroadcastGroup * bcg, TCFSuspendGroup * spg);

#endif /* D_runctrl */
