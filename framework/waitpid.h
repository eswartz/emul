/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
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
 * This module provides notifications of process/thread exited or stopped.
 */

#ifndef D_waitpid
#define D_waitpid

#include <config.h>

#if (ENABLE_DebugContext && !ENABLE_ContextProxy) || SERVICE_Processes || SERVICE_Terminals

typedef void WaitPIDListener(int pid, int exited, int exit_code, int signal, int event_code, int syscall, void * args);

extern void add_waitpid_listener(WaitPIDListener * listener, void * args);

extern void add_waitpid_process(int pid);

extern void detach_waitpid_process(void);

#endif

#endif /* D_waitpid */
