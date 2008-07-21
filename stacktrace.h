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
 * Target service implementation: stack trace (TCF name StackTrace)
 */

#ifndef D_stacktrace
#define D_stacktrace

#include "protocol.h"
#include "context.h"

#define STACK_TOP_FRAME (-2)
#define STACK_BOTTOM_FRAME 0
#define STACK_NO_FRAME  (-1)

/*
 * Dump current stack trace into log.
 * The function can be used to debug the agent itself.
 */
extern void dump_stack_trace(void);


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
 * Get information about given stack frame.
 * ip - instruction pointer (in this frame)
 * rp - return pointer (parent frame instruction pointer)
 * fp - frame pointer (frame address on the thread stack)
 */
extern int get_frame_info(Context * ctx, int frame, ContextAddress * ip, ContextAddress * rp, ContextAddress * fp);

/*
 * Initialize stack trace service.
 */
extern void ini_stack_trace_service(Protocol *, TCFBroadcastGroup *);


#endif
