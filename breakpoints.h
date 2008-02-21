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

typedef unsigned long address_t;

typedef struct SkipBreakpointInfo SkipBreakpointInfo;

struct SkipBreakpointInfo {
    Context * ctx;
    address_t address;
    int pending_intercept;
    void (*done)(SkipBreakpointInfo *);
    Channel * c;
    char token[256];
    int error;
};

extern int is_stopped_by_breakpoint(Context * ctx);

extern int evaluate_breakpoint_condition(Context * ctx);

extern SkipBreakpointInfo * skip_breakpoint(Context * ctx);

extern void ini_breakpoints_service(Protocol *, TCFBroadcastGroup *);

#endif
