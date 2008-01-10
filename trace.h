/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
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

#include <stdio.h>

#define LOG_ALWAYS      0x0
#define LOG_ALLOC       0x1
#define LOG_EVENTCORE   0x2
#define LOG_WAITPID     0x4
#define LOG_EVENTS      0x8
#define LOG_CHILD       0x10
#define LOG_PROTOCOL    0x20
#define LOG_CONTEXT     0x40

extern FILE * log_file;
extern int log_mode;

/*
 * Print a trace message into log file.
 * Use macro 'trace' intead of calling this function directly.
 */
extern int print_trace(int mode, char *fmt, ...);

#define trace log_file && print_trace

extern void ini_trace(void);

#endif
