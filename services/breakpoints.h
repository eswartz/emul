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
 * This module implements Breakpoints service.
 * The service maintains a list of breakpoints.
 * Each breakpoint consists of one or more conditions that determine
 * when a program's execution should be interrupted.
 */

#ifndef D_breakpoints
#define D_breakpoints

#include <framework/context.h>
#include <framework/protocol.h>

typedef struct BreakpointInfo BreakpointInfo;

#if SERVICE_Breakpoints

/*
 * The function is called from context.c every time a context is stopped by breakpoint.
 * The function evaluates breakpoint condition and calls suspend_debug_context() if the condition is true.
 */
extern void evaluate_breakpoint(Context * ctx);

/*
 * Return NULL-terminated array of breakpoint IDs if the context is stopped by breakpoint.
 * Otherwise return NULL.
 */
extern char ** get_context_breakpoint_ids(Context * ctx);

/*
 * When a context is stopped by breakpoint, it is necessary to disable
 * the breakpoint temporarily before the context can be resumed.
 * This function function removes break instruction, then does single step
 * over breakpoint location, then restores break intruction.
 * Return: 0 if it is OK to resume context from current state,
 * return 1 if context needs to step over a breakpoint.
 */
extern int skip_breakpoint(Context * ctx, int single_step);

/* Return 1 if break instruction is planted at given address in the context memory */
extern int is_breakpoint_address(Context * ctx, ContextAddress address);

/* Check if memory data buffer contans planted break instructions and remove them */
extern void check_breakpoints_on_memory_read(Context * ctx, ContextAddress address, void * buf, size_t size);

/* Check if data is about to be written over planted break instructions and adjust the data and breakpoint backing storage */
extern void check_breakpoints_on_memory_write(Context * ctx, ContextAddress address, void * buf, size_t size);

/* Evenpoint callback. It is called when context is suspended by eventpoint, right before "context_stopped" event */
typedef void EventPointCallBack(Context *, void *);

/* Create, plant and return eventpoint. Eventpoints are breakpoints that are created by agent to control execution of debugee.
 * Eventpoint are not exposed through "Breakpoints" TCF service, they are handled by agent itself. */
extern BreakpointInfo * create_eventpoint(const char * location, EventPointCallBack * callback, void * callback_args);

/* Unplant and destroy eventpoint */
extern void destroy_eventpoint(BreakpointInfo * eventpoint);

#else /* SERVICE_Breakpoints */

#define evaluate_breakpoint(ctx)
#define skip_breakpoint(ctx, single_step) 0
#define is_breakpoint_address(ctx, address) 0
#define check_breakpoints_on_memory_read(ctx, address, buf, size)
#define check_breakpoints_on_memory_write(ctx, address, buf, size)
#define create_eventpoint(location, callback, callback_args) 0

#endif /* SERVICE_Breakpoints */

extern void ini_breakpoints_service(Protocol *, TCFBroadcastGroup *);

#endif /* D_breakpoints */
