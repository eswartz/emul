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
 * Log file and tracing.
 */

#ifndef D_trace
#define D_trace

#include "config.h"
#include <stdio.h>

#define LOG_ALWAYS      0x0
#define LOG_ALLOC       0x1
#define LOG_EVENTCORE   0x2
#define LOG_WAITPID     0x4
#define LOG_EVENTS      0x8
#define LOG_CHILD       0x10
#define LOG_PROTOCOL    0x20
#define LOG_CONTEXT     0x40
#define LOG_DISCOVERY   0x80
#define LOG_ASYNCREQ    0x100

extern int log_mode;

#if ENABLE_Trace

/*
 * Print a trace message into log file.
 * Use macro 'trace' instead of calling this function directly.
 */
extern int print_trace(int mode, char * fmt, ...);

extern FILE * log_file;

#define trace log_file && print_trace

#else /* not ENABLE_Trace */

#if _MSC_VER >= 1400 || __GNUC__
#  define trace(...) ((void)0)
#else
#  define trace 0 &&
#endif

#endif /* ENABLE_Trace */

extern void ini_trace(void);

extern void open_log_file(char * name);

#endif
