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
 * Target service implementation: run control (TCF name RunControl)
 */

#ifndef D_runctrl
#define D_runctrl

#include "mdep.h"
#include "events.h"
#include "context.h"
#include "protocol.h"

/*
 * Add "safe" event.
 * Temporary suspends handling of incoming messages and stops all debuggee threads.
 * Callback function 'done' will be called when everything is stopped and
 * it is safe to access debuggee memory, plant breakpoints, etc.
 */
extern void post_safe_event(EventCallBack * done, void * arg);

/*
 * Return 1 if all threads in debuggee are stopped and handling of incoming messages
 * is suspended and it is safe to access debuggee memory, plant breakpoints, etc.
 */
extern int is_all_stopped(void);

/*
 * Initialize run control service.
 */
extern void ini_run_ctrl_service(Protocol * proto, TCFBroadcastGroup * bcg, TCFSuspendGroup * spg);

#endif
