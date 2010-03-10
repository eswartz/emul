/*******************************************************************************
 * Copyright (c) 2007-2009 Wind River Systems, Inc. and others.
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
 * Target service implementation: stack trace (TCF name StackTrace)
 */

#ifndef D_stacktrace
#define D_stacktrace

#include "protocol.h"
#include "context.h"

#define STACK_TOP_FRAME (-2)
#define STACK_BOTTOM_FRAME 0
#define STACK_NO_FRAME  (-1)

#if SERVICE_StackTrace || ENABLE_ContextProxy

/*
 * Check if given context ID is stack frame ID.
 * Return 1 if frame ID, 0 otherwise.
 */
extern int is_stack_frame_id(char * id, Context ** ctx, int * frame);

/*
 * Get TCF ID of a stack frame.
 */
extern char * get_stack_frame_id(Context * ctx, int frame);

/*
 * Get index of the top frame of a context.
 */
extern int get_top_frame(Context * ctx);

/*
 * Get information about given stack frame.
 */
extern int get_frame_info(Context * ctx, int frame, StackFrame ** info);

/*
 * Return 1 if 'frame' is the top frame of the context.
 */
extern int is_top_frame(Context * ctx, int frame);

/*
 * Initialize stack trace service.
 */
extern void ini_stack_trace_service(Protocol *, TCFBroadcastGroup *);

#else /* SERVICE_StackTrace */

#define get_frame_info(ctx, frame, info) (errno = ERR_UNSUPPORTED, -1)
#define is_stack_frame_id(id, ctx, frame) 0
#define get_stack_frame_id(ctx, frame) NULL
#define is_top_frame(ctx, frame) (frame == STACK_TOP_FRAME)

#endif /* SERVICE_StackTrace */
#endif /* D_stacktrace */
