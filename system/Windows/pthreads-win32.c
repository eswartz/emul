/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
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

#include <config.h>

#if defined(WIN32) && !defined(DISABLE_PTHREADS_WIN32)

#include <assert.h>
#include <framework/myalloc.h>
#include <framework/errors.h>
#include <system/Windows/pthreads-win32.h>

/*********************************************************************
    Support of pthreads on Windows is implemented according to
    recommendations from the paper:

    Strategies for Implementing POSIX Condition Variables on Win32
    C++ Report, SIGS, Vol. 10, No. 5, June, 1998

    Douglas C. Schmidt and Irfan Pyarali
    Department of Computer Science
    Washington University, St. Louis, Missouri
**********************************************************************/

/* TODO: POSIX pthread functions don't set errno */

typedef struct {
    int waiters_count;
    CRITICAL_SECTION waiters_count_lock;
    HANDLE sema;
    HANDLE waiters_done;
    size_t was_broadcast;
} PThreadCond;

int pthread_mutex_init(pthread_mutex_t * mutex, const pthread_mutexattr_t * attr) {
    assert(attr == NULL);
    *mutex = (pthread_mutex_t)CreateMutex(NULL, FALSE, NULL);
    if (*mutex == NULL) return set_win32_errno(GetLastError());
    return 0;
}

int pthread_mutex_lock(pthread_mutex_t * mutex) {
    assert(mutex != NULL);
    assert(*mutex != NULL);
    if (WaitForSingleObject(*mutex, INFINITE) == WAIT_FAILED) return set_win32_errno(GetLastError());
    return 0;
}

int pthread_mutex_unlock(pthread_mutex_t * mutex) {
    assert(mutex != NULL);
    assert(*mutex != NULL);
    if (!ReleaseMutex(*mutex)) return set_win32_errno(GetLastError());
    return 0;
}

int pthread_mutex_destroy(pthread_mutex_t *mutex) {
    assert(mutex != NULL);
    assert(*mutex != NULL);
    if (!CloseHandle(*mutex)) return set_win32_errno(GetLastError());
    return 0;
}

int pthread_cond_init(pthread_cond_t * cond, const pthread_condattr_t * attr) {
    PThreadCond * p = (PThreadCond *)loc_alloc_zero(sizeof(PThreadCond));
    assert(attr == NULL);
    p->waiters_count = 0;
    p->was_broadcast = 0;
    p->sema = CreateSemaphore(NULL, 0, 0x7fffffff, NULL);
    if (p->sema == NULL) return set_win32_errno(GetLastError());
    InitializeCriticalSection(&p->waiters_count_lock);
    p->waiters_done = CreateEvent(NULL, FALSE, FALSE, NULL);
    if (p->waiters_done == NULL) return set_win32_errno(GetLastError());
    *cond = (pthread_cond_t)p;
    return 0;
}

int pthread_cond_wait(pthread_cond_t * cond, pthread_mutex_t * mutex) {
    DWORD res = 0;
    int last_waiter = 0;
    PThreadCond * p = (PThreadCond *)*cond;

    EnterCriticalSection(&p->waiters_count_lock);
    p->waiters_count++;
    LeaveCriticalSection(&p->waiters_count_lock);

    /* This call atomically releases the mutex and waits on the */
    /* semaphore until <pthread_cond_signal> or <pthread_cond_broadcast> */
    /* are called by another thread. */
    res = SignalObjectAndWait(*mutex, p->sema, INFINITE, FALSE);
    if (res == WAIT_FAILED) return set_win32_errno(GetLastError());

    /* Re-acquire lock to avoid race conditions. */
    EnterCriticalSection(&p->waiters_count_lock);

    /* We're no longer waiting... */
    p->waiters_count--;

    /* Check to see if we're the last waiter after <pthread_cond_broadcast>. */
    last_waiter = p->was_broadcast && p->waiters_count == 0;

    LeaveCriticalSection(&p->waiters_count_lock);

    /* If we're the last waiter thread during this particular broadcast */
    /* then let all the other threads proceed. */
    if (last_waiter) {
        /* This call atomically signals the <waiters_done_> event and waits until */
        /* it can acquire the <mutex>.  This is required to ensure fairness.  */
        DWORD err = SignalObjectAndWait(p->waiters_done, *mutex, INFINITE, FALSE);
        if (err == WAIT_FAILED) return set_win32_errno(GetLastError());
    }
    else {
        /* Always regain the external mutex since that's the guarantee we */
        /* give to our callers.  */
        DWORD err = WaitForSingleObject(*mutex, INFINITE);
        if (err == WAIT_FAILED) return set_win32_errno(GetLastError());
    }
    assert(res == WAIT_OBJECT_0);
    return 0;
}

int pthread_cond_timedwait(pthread_cond_t * cond, pthread_mutex_t * mutex, const struct timespec * abstime) {
    DWORD res = 0;
    int last_waiter = 0;
    PThreadCond * p = (PThreadCond *)*cond;
    DWORD timeout = 0;
    struct timespec timenow;

    if (clock_gettime(CLOCK_REALTIME, &timenow)) return errno;
    if (abstime->tv_sec < timenow.tv_sec) return ETIMEDOUT;
    if (abstime->tv_sec == timenow.tv_sec) {
        if (abstime->tv_nsec <= timenow.tv_nsec) return ETIMEDOUT;
    }
    timeout = (DWORD)((abstime->tv_sec - timenow.tv_sec) * 1000 + (abstime->tv_nsec - timenow.tv_nsec) / 1000000 + 5);

    EnterCriticalSection(&p->waiters_count_lock);
    p->waiters_count++;
    LeaveCriticalSection(&p->waiters_count_lock);

    /* This call atomically releases the mutex and waits on the */
    /* semaphore until <pthread_cond_signal> or <pthread_cond_broadcast> */
    /* are called by another thread. */
    res = SignalObjectAndWait(*mutex, p->sema, timeout, FALSE);
    if (res == WAIT_FAILED) return set_win32_errno(GetLastError());

    /* Re-acquire lock to avoid race conditions. */
    EnterCriticalSection(&p->waiters_count_lock);

    /* We're no longer waiting... */
    p->waiters_count--;

    /* Check to see if we're the last waiter after <pthread_cond_broadcast>. */
    last_waiter = p->was_broadcast && p->waiters_count == 0;

    LeaveCriticalSection(&p->waiters_count_lock);

    /* If we're the last waiter thread during this particular broadcast */
    /* then let all the other threads proceed. */
    if (last_waiter) {
        /* This call atomically signals the <waiters_done> event and waits until */
        /* it can acquire the <mutex>.  This is required to ensure fairness.  */
        DWORD err = SignalObjectAndWait(p->waiters_done, *mutex, INFINITE, FALSE);
        if (err == WAIT_FAILED) return set_win32_errno(GetLastError());
    }
    else {
        /* Always regain the external mutex since that's the guarantee we */
        /* give to our callers.  */
        DWORD err = WaitForSingleObject(*mutex, INFINITE);
        if (err == WAIT_FAILED) return set_win32_errno(GetLastError());
    }

    if (res == WAIT_TIMEOUT) return errno = ETIMEDOUT;
    assert(res == WAIT_OBJECT_0);
    return 0;
}

int pthread_cond_signal(pthread_cond_t * cond) {
    int have_waiters = 0;
    PThreadCond * p = (PThreadCond *)*cond;

    EnterCriticalSection(&p->waiters_count_lock);
    have_waiters = p->waiters_count > 0;
    LeaveCriticalSection(&p->waiters_count_lock);

    /* If there aren't any waiters, then this is a no-op.   */
    if (have_waiters) {
        if (!ReleaseSemaphore(p->sema, 1, 0)) return set_win32_errno(GetLastError());
    }
    return 0;
}

int pthread_cond_broadcast(pthread_cond_t * cond) {
    int have_waiters = 0;
    PThreadCond * p = (PThreadCond *)*cond;

    /* This is needed to ensure that <waiters_count_> and <was_broadcast_> are */
    /* consistent relative to each other. */
    EnterCriticalSection(&p->waiters_count_lock);

    if (p->waiters_count > 0) {
        /* We are broadcasting, even if there is just one waiter... */
        /* Record that we are broadcasting, which helps optimize */
        /* <pthread_cond_wait> for the non-broadcast case. */
        p->was_broadcast = 1;
        have_waiters = 1;
    }

    if (have_waiters) {
        /* Wake up all the waiters atomically. */
        if (!ReleaseSemaphore(p->sema, p->waiters_count, 0)) return set_win32_errno(GetLastError());

        LeaveCriticalSection(&p->waiters_count_lock);

        /* Wait for all the awakened threads to acquire the counting */
        /* semaphore.  */
        if (WaitForSingleObject(p->waiters_done, INFINITE) == WAIT_FAILED) return set_win32_errno(GetLastError());
        /* This assignment is okay, even without the <waiters_count_lock_> held  */
        /* because no other waiter threads can wake up to access it. */
        p->was_broadcast = 0;
    }
    else {
        LeaveCriticalSection(&p->waiters_count_lock);
    }
    return 0;
}

int pthread_cond_destroy(pthread_cond_t * cond) {
    PThreadCond * p = (PThreadCond *)*cond;

    DeleteCriticalSection(&p->waiters_count_lock);
    if (!CloseHandle(p->sema)) return set_win32_errno(GetLastError());
    if (!CloseHandle(p->waiters_done)) return set_win32_errno(GetLastError());

    loc_free(p);
    *cond = NULL;
    return 0;
}

typedef struct ThreadArgs ThreadArgs;

struct ThreadArgs {
    void * (*start)(void *);
    void * args;
};

static void start_thread(void * x) {
    ThreadArgs a = *(ThreadArgs *)x;

    loc_free(x);
    ExitThread((DWORD)a.start(a.args));
}

int pthread_create(pthread_t * res, const pthread_attr_t * attr,
                   void * (*start)(void *), void * args) {
    HANDLE thread = NULL;
    DWORD thread_id = 0;
    ThreadArgs * a = (ThreadArgs *)loc_alloc(sizeof(ThreadArgs));

    a->start = start;
    a->args = args;
    thread = CreateThread(0, 0, (LPTHREAD_START_ROUTINE)start_thread, a, 0, &thread_id);
    if (thread == NULL) {
        int err = set_win32_errno(GetLastError());
        loc_free(a);
        return errno = err;
    }
    if (!CloseHandle(thread)) return set_win32_errno(GetLastError());
    *res = (pthread_t)thread_id;
    return 0;
}

int pthread_join(pthread_t thread_id, void ** value_ptr) {
    int error = 0;
    HANDLE thread = OpenThread(SYNCHRONIZE | THREAD_QUERY_INFORMATION, FALSE, (DWORD)thread_id);

    if (thread == NULL) return set_win32_errno(GetLastError());
    if (WaitForSingleObject(thread, INFINITE) == WAIT_FAILED) error = set_win32_errno(GetLastError());
    if (!error && value_ptr != NULL && !GetExitCodeThread(thread, (LPDWORD)value_ptr)) error = set_win32_errno(GetLastError());
    if (!CloseHandle(thread) && !error) error = set_win32_errno(GetLastError());
    return error;
}

pthread_t pthread_self(void) {
    return (pthread_t)GetCurrentThreadId();
}

int pthread_equal(pthread_t thread1, pthread_t thread2) {
    return thread1 == thread2;
}

int pthread_attr_init(pthread_attr_t * attr) {
    *attr = NULL;
    return 0;
}

#endif
