/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
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
 * This module provides notifications of process/thread exited or stopped.
 */

#include <config.h>

#if (ENABLE_DebugContext && !ENABLE_ContextProxy) || SERVICE_Processes

#include <assert.h>
#include <errno.h>
#include <framework/errors.h>
#include <framework/myalloc.h>
#include <framework/events.h>
#include <framework/trace.h>
#include <framework/asyncreq.h>
#include <framework/waitpid.h>

typedef struct WaitPIDListenerInfo {
    WaitPIDListener * listener;
    void * args;
} WaitPIDListenerInfo;

#define MAX_LISTENERS 8

static WaitPIDListenerInfo listeners[MAX_LISTENERS];
static int listener_cnt = 0;

static void init(void);

void add_waitpid_listener(WaitPIDListener * listener, void * args) {
    assert(listener_cnt < MAX_LISTENERS);
    if (listener_cnt == 0) init();
    listeners[listener_cnt].listener = listener;
    listeners[listener_cnt].args = args;
    listener_cnt++;
}

#if defined(WIN32)

#define MAX_HANDLES 64

typedef struct WaitPIDThread {
    DWORD thread;
    HANDLE handles[MAX_HANDLES];
    DWORD handle_cnt;
    struct WaitPIDThread * next;
} WaitPIDThread;

static WaitPIDThread * threads = NULL;
static HANDLE semaphore = NULL;

#define check_error_win32(ok) { if (!(ok)) check_error(set_win32_errno(GetLastError())); }

static void waitpid_event(void * args) {
    int i;
    HANDLE prs = args;
    DWORD pid = GetProcessId(prs);
    DWORD exit_code = 0;
    check_error_win32(GetExitCodeProcess(prs, &exit_code));
    for (i = 0; i < listener_cnt; i++) {
        listeners[i].listener(pid, 1, exit_code, 0, 0, 0, listeners[i].args);
    }
    check_error_win32(CloseHandle(prs));
}

static DWORD WINAPI waitpid_thread_func(LPVOID x) {
    WaitPIDThread * thread = (WaitPIDThread *)x;
    check_error_win32(WaitForSingleObject(semaphore, INFINITE) != WAIT_FAILED);
    for (;;) {
        DWORD n = 0;
        HANDLE arr[MAX_HANDLES];
        DWORD cnt = thread->handle_cnt;
        memcpy(arr, thread->handles, cnt * sizeof(HANDLE));
        check_error_win32(ReleaseSemaphore(semaphore, 1, 0));
        n = WaitForMultipleObjects(cnt, arr, FALSE, INFINITE);
        check_error_win32(n != WAIT_FAILED);
        check_error_win32(WaitForSingleObject(semaphore, INFINITE) != WAIT_FAILED);
        if (n > 0) {
            assert(thread->handles[n] == arr[n]);
            post_event(waitpid_event, thread->handles[n]);
            memmove(thread->handles + n, thread->handles + n + 1, (thread->handle_cnt - n - 1) * sizeof(HANDLE));
            thread->handle_cnt--;
        }
    }
}

static void init(void) {
    assert(threads == NULL);
    semaphore = CreateSemaphore(NULL, 1, 1, NULL);
}

void add_waitpid_process(int pid) {
    HANDLE prs = NULL;
    WaitPIDThread * thread = threads;
    check_error_win32(WaitForSingleObject(semaphore, INFINITE) != WAIT_FAILED);
    while (thread != NULL && thread->handle_cnt >= MAX_HANDLES) thread = thread->next;
    if (thread == NULL) {
        thread = (WaitPIDThread *)loc_alloc_zero(sizeof(WaitPIDThread));
        thread->next = threads;
        threads = thread;
        check_error_win32((thread->handles[thread->handle_cnt++] = CreateSemaphore(NULL, 0, 1, NULL)) != NULL);
        check_error_win32(CreateThread(NULL, 0, waitpid_thread_func, thread, 0, &thread->thread) != NULL);
    }
    check_error_win32((prs = OpenProcess(PROCESS_QUERY_INFORMATION | SYNCHRONIZE, FALSE, pid)) != NULL);
    thread->handles[thread->handle_cnt++] = prs;
    check_error_win32(ReleaseSemaphore(thread->handles[0], 1, 0));
    check_error_win32(ReleaseSemaphore(semaphore, 1, 0));
}

#elif defined(_WRS_KERNEL)

#include <taskHookLib.h>

typedef struct EventInfo {
    UINT32 pid;
    SEM_ID signal;
} EventInfo;

static WIND_TCB * main_thread;

static void task_delete_event(void * args) {
    int i;
    EventInfo * info = args;
    for (i = 0; i < listener_cnt; i++) {
        listeners[i].listener(info->pid, 1, 0, 0, 0, 0, listeners[i].args);
    }
    semGive(info->signal);
}

static void task_delete_hook(WIND_TCB * tcb) {
    if (tcb != main_thread && taskIdCurrent != main_thread) {
        EventInfo info;
        VX_COUNTING_SEMAPHORE(signal_mem);
        info.signal = semCInitialize(signal_mem, SEM_Q_FIFO, 0);
        info.pid = (UINT32)tcb;
        post_event(task_delete_event, &info);
        semTake(info.signal, WAIT_FOREVER);
        semTerminate(info.signal);
    }
}

static void init(void) {
    main_thread = taskIdCurrent;
    taskDeleteHookAdd((FUNCPTR)task_delete_hook);
}

void add_waitpid_process(int pid) {
}

#else

#include <sys/wait.h>

static void waitpid_done(void * arg) {
    int i;
    AsyncReqInfo * req = (AsyncReqInfo *)arg;
    pid_t pid = req->u.wpid.pid;
    int status = req->u.wpid.status;
    int error = req->error;
    int exited = 0;
    int exit_code = 0;
    int signal = 0;
    int event_code = 0;
    int syscall = 0;

    trace(LOG_WAITPID, "waitpid: pid %d status %#x, error %d", pid, status, error);
    assert(req->u.wpid.rval == -1 || req->u.wpid.rval == pid);

    if (req->u.wpid.rval == -1) {
        assert(error);
        trace(LOG_ALWAYS, "waitpid error (pid %d): %d %d", pid, error, errno_to_str(error));
        exited = 1;
        exit_code = error;
    }
    else if (WIFEXITED(status)) {
        exited = 1;
        exit_code = WEXITSTATUS(status);
        trace(LOG_WAITPID, "waitpid: pid %d exited, exit code %d", pid, exit_code);
    }
    else if (WIFSIGNALED(status)) {
        exited = 1;
        signal = WTERMSIG(status);
        trace(LOG_WAITPID, "waitpid: pid %d terminated, signal %d", pid, signal);
    }
    else if (WIFSTOPPED(status)) {
        signal = WSTOPSIG(status) & 0x7f;
        event_code = status >> 16;
        syscall = (WSTOPSIG(status) & 0x80) != 0;
        trace(LOG_WAITPID, "waitpid: pid %d suspended, signal %d, event code %d", pid, signal, event_code);
    }
    else {
        trace(LOG_ALWAYS, "unexpected status (0x%x) from waitpid (pid %d)", status, pid);
        exited = 1;
    }
    for (i = 0; i < listener_cnt; i++) {
        listeners[i].listener(pid, exited, exit_code, signal, event_code, syscall, listeners[i].args);
    }
    if (exited) {
        loc_free(req);
    }
    else {
        req->error = 0;
        req->u.wpid.status = 0;
        async_req_post(req);
    }
}

void add_waitpid_process(int pid) {
    AsyncReqInfo * req = (AsyncReqInfo *)loc_alloc_zero(sizeof(AsyncReqInfo));
    assert(listener_cnt > 0);
    req->done = waitpid_done;
    req->type = AsyncReqWaitpid;
    req->u.wpid.pid = pid;
#if defined(__linux__)
    req->u.wpid.options |= __WALL;
#endif
    async_req_post(req);
}

static void init(void) {
}

#endif
#endif
