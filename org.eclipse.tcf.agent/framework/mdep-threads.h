/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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
 *     Nokia - Symbian support
 *******************************************************************************/

/*
 * Machine and OS dependend definitions for threads.
 */

#ifndef D_mdep_threads
#define D_mdep_threads

#if defined(WIN32) || defined(__CYGWIN__)

#ifdef DISABLE_PTHREADS_WIN32
#  include <pthread.h>
#else
#  include <system/Windows/pthreads-win32.h>
#endif

#elif defined(_WRS_KERNEL)

#include <pthread.h>

#elif defined __SYMBIAN32__

#include <pthreadtypes.h>
#include <pthread.h>

#else

#include <pthread.h>

#endif

extern pthread_attr_t pthread_create_attr;

#endif /* D_mdep_threads */
