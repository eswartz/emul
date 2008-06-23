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
 * This module implements Breakpoints service.
 * The service maintains a list of breakpoints.
 * Each breakpoint consists of one or more conditions that determine
 * when a program's execution should be interrupted.
 */

#ifndef D_breakpoints
#define D_breakpoints

#include "context.h"
#include "protocol.h"

typedef struct SkipBreakpointInfo SkipBreakpointInfo;

struct SkipBreakpointInfo {
    Context * ctx;
    ContextAddress address;
    int pending_intercept;
    void (*done)(SkipBreakpointInfo *);
    Channel * c;
    char token[256];
    int error;
};

#if SERVICE_Breakpoints

extern int evaluate_breakpoint_condition(Context * ctx);

extern SkipBreakpointInfo * skip_breakpoint(Context * ctx);

/* Return 1 if break instruction is planted at given address in the context memory */
extern int is_breakpoint_address(Context * ctx, ContextAddress address);

/* Check if memory data buffer contans planted break instructions and remove them */
extern void check_breakpoints_on_memory_read(Context * ctx, ContextAddress address, void * buf, size_t size);

/* Check if data is about to be written over planted break instructions and adjust the data and breakpoint backing storage */
extern void check_breakpoints_on_memory_write(Context * ctx, ContextAddress address, void * buf, size_t size);

#else

#define evaluate_breakpoint_condition(ctx) 0
#define skip_breakpoint(ctx) 0
#define is_breakpoint_address(ctx, address) 0
#define check_breakpoints_on_memory_read(ctx, address, buf, size)
#define check_breakpoints_on_memory_write(ctx, address, buf, size)

#endif

extern void ini_breakpoints_service(Protocol *, TCFBroadcastGroup *);

#endif
