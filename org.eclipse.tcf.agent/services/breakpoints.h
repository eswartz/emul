/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
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
typedef struct BreakpointAttribute BreakpointAttribute;

struct BreakpointAttribute {
    BreakpointAttribute * next;
    char * name;        /* Attribute name */
    char * value;       /* Attribute value as JSON string */
};

#if SERVICE_Breakpoints

/*
 * Breakpoint attribute names.
 * Clients may define additional attributes.
 */
#define BREAKPOINT_ID               "ID"
#define BREAKPOINT_ENABLED          "Enabled"
#define BREAKPOINT_TYPE             "BreakpointType"
#define BREAKPOINT_CONTEXTNAMES     "ContextNames"
#define BREAKPOINT_CONTEXTIDS       "ContextIds"
#define BREAKPOINT_EXECUTABLEPATHS  "ExecPaths"
#define BREAKPOINT_LOCATION         "Location"
#define BREAKPOINT_SIZE             "Size"
#define BREAKPOINT_ACCESSMODE       "AccessMode"
#define BREAKPOINT_FILE             "File"
#define BREAKPOINT_LINE             "Line"
#define BREAKPOINT_COLUMN           "Column"
#define BREAKPOINT_PATTERN          "MaskValue"
#define BREAKPOINT_MASK             "Mask"
#define BREAKPOINT_STOP_GROUP       "StopGroup"
#define BREAKPOINT_IGNORECOUNT      "IgnoreCount"
#define BREAKPOINT_TIME             "Time"
#define BREAKPOINT_SCALE            "TimeScale"
#define BREAKPOINT_UNITS            "TimeUnits"
#define BREAKPOINT_CONDITION        "Condition"
#define BREAKPOINT_TEMPORARY        "Temporary"
#define BREAKPOINT_CLIENT_DATA      "ClientData"


/* Breakpoints event listener */
typedef struct BreakpointsEventListener {
    void (*breakpoint_created)(BreakpointInfo *, void *);
    void (*breakpoint_changed)(BreakpointInfo *, void *);
    void (*breakpoint_deleted)(BreakpointInfo *, void *);
    void (*breakpoint_status_changed)(BreakpointInfo *, void *);
} BreakpointsEventListener;

/*
 * Add a listener for Breakpoints service events.
 */
extern void add_breakpoint_event_listener(BreakpointsEventListener * listener, void * args);

/*
 * Remove a listener of Breakpoints service events.
 */
extern void rem_breakpoint_event_listener(BreakpointsEventListener * listener);

/*
 * Iterate all breakpoints known to the Breakpoints service,
 * including breakpoints that are created by other (remote) clients.
 */
typedef void IterateBreakpointsCallBack(BreakpointInfo *, void *);
extern void iterate_breakpoints(IterateBreakpointsCallBack * callback, void * args);

/*
 * Get breakpoint attributes.
 */
extern BreakpointAttribute * get_breakpoint_attributes(BreakpointInfo * bp);

/*
 * Create new breakpoint with given attributes.
 * Attributes must include, at least, BREAKPOINT_ID.
 * If a breakpoint with such ID already exists, it will be modified to match
 * new attributes instead of creating a new one.
 * Caller should allocate attributes using myalloc.h functions.
 * Breakpoints service will free attributes memory using loc_free().
 */
extern BreakpointInfo * create_breakpoint(BreakpointAttribute * attrs);

/*
 * Change breakpoint attributes to given attributes.
 * Caller should allocate attributes using myalloc.h functions.
 * Breakpoints service will free attributes memory using loc_free().
 * The function compares existing attributes with new ones,
 * and calls listeners only if attributes are different.
 */
extern void change_breakpoint_attributes(BreakpointInfo * bp, BreakpointAttribute * attrs);

/*
 * Delete a breakpoint.
 * If other (remote) client also created a breakpoint with same ID,
 * the breakpoint will be deleted when all clients have requested it to be deleted.
 */
extern void delete_breakpoint(BreakpointInfo * bp);

/*
 * Iterate all breakpoints that are linked to context breakpoint 'cb' in the breakpoint address space
 * associated with executable context 'ctx'. Breakpoint address space is the context returned by
 * context_get_group(ctx, CONTEXT_GROUP_BREAKPOINT).
 * Single 'cb' can be linked to multiple breakpoints if those breakpoint locations are evaluated
 * to same address in same address space. Single breakpoint can be linked to multiple CBs if the
 * breakpoint scope spawns multiple address spaces.
 */
typedef void IterateCBLinksCallBack(BreakpointInfo *, void *);
extern void iterate_context_breakpoint_links(Context * ctx, ContextBreakpoint * cb, IterateCBLinksCallBack * callback, void * args);

/*
 * The function is called from context.c every time a context is stopped by a breakpoint.
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
 * Return 0 if it is OK to resume context from current state,
 * return 1 if context needs to step over a breakpoint.
 */
extern int skip_breakpoint(Context * ctx, int single_step);

/* Return 1 if break instruction is planted at given address in the context memory */
extern int is_breakpoint_address(Context * ctx, ContextAddress address);

/* Clone all planted breakpoints when a process forks */
extern void clone_breakpoints_on_process_fork(Context * parent, Context * child);

/* Unplant all breakpoints in a process (e.g. before detaching) */
extern void unplant_breakpoints(Context * ctx);

/*
 * Check if memory data buffer contans planted break instructions and remove them.
 * Return -1 and set errno if the check cannot be done.
 */
extern int check_breakpoints_on_memory_read(Context * ctx, ContextAddress address, void * buf, size_t size);

/*
 * Check if data is about to be written over planted break instructions and adjust the data and breakpoint backing storage
 * Return -1 and set errno if the check cannot be done.
 * Return 0 on success.
 */
extern int check_breakpoints_on_memory_write(Context * ctx, ContextAddress address, void * buf, size_t size);

/* Evenpoint callback. It is called when context is suspended by eventpoint, right before "context_stopped" event */
typedef void EventPointCallBack(Context *, void *);

/* Create, plant and return eventpoint. Eventpoints are breakpoints that are created by agent to control execution of debugee.
 * Eventpoint are not exposed through "Breakpoints" TCF service, they are handled by agent itself. */
extern BreakpointInfo * create_eventpoint(const char * location, Context * ctx, EventPointCallBack * callback, void * callback_args);

/* Unplant and destroy eventpoint */
extern void destroy_eventpoint(BreakpointInfo * eventpoint);

#else /* SERVICE_Breakpoints */

#define skip_breakpoint(ctx, single_step) 0
#define is_breakpoint_address(ctx, address) 0
#define clone_breakpoints_on_process_fork(parent, child) 0
#define unplant_breakpoints(ctx) 0
#define check_breakpoints_on_memory_read(ctx, address, buf, size) 0
#define check_breakpoints_on_memory_write(ctx, address, buf, size) 0
#define create_eventpoint(location, ctx, callback, callback_args) 0

#endif /* SERVICE_Breakpoints */

extern void ini_breakpoints_service(Protocol *, TCFBroadcastGroup *);

#endif /* D_breakpoints */
