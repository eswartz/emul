/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
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
 * PThreads emulation for Windows.
 */

#ifndef D_pthreads_win32
#define D_pthreads_win32

#if defined(__CYGWIN__)
#  include <cygwin/types.h>
#else
typedef struct pthread_struct * pthread_t;
typedef struct pthread_attr_struct * pthread_attr_t;
typedef struct pthread_mutex_struct * pthread_mutex_t;
typedef struct pthread_cond_struct * pthread_cond_t;
typedef struct pthread_mutexattr_struct * pthread_mutexattr_t;
typedef struct pthread_condattr_struct * pthread_condattr_t;
#endif

extern int pthread_attr_init(pthread_attr_t * attr);

extern int pthread_cond_init(pthread_cond_t * cond, const pthread_condattr_t * attr);
extern int pthread_cond_signal(pthread_cond_t * cond);
extern int pthread_cond_broadcast(pthread_cond_t * cond);
extern int pthread_cond_wait(pthread_cond_t * cond, pthread_mutex_t * mutex);
extern int pthread_cond_timedwait(pthread_cond_t * cond, pthread_mutex_t * mutex,
                                  const struct timespec * abstime);
extern int pthread_cond_destroy(pthread_cond_t * cond);

extern int pthread_mutex_init(pthread_mutex_t * mutex, const pthread_mutexattr_t * attr);
extern int pthread_mutex_lock(pthread_mutex_t * mutex);
extern int pthread_mutex_unlock(pthread_mutex_t * mutex);
extern int pthread_mutex_destroy(pthread_mutex_t *mutex);

extern pthread_t pthread_self(void);
extern int pthread_create(pthread_t * thread, const pthread_attr_t * attr,
                          void * (*start_routine)(void *), void * arg);
extern int pthread_join(pthread_t thread, void **value_ptr);
extern int pthread_equal(pthread_t thread1, pthread_t thread2);

#endif /* D_pthreads_win32 */
