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
 * Agent self-testing service.
 */

#include "mdep.h"
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <signal.h>
#include <assert.h>
#include "myalloc.h"
#include "test.h"
#include "trace.h"
#include "context.h"

extern void tcf_test_func2(void);
extern void tcf_test_func1(void);
extern void tcf_test_func0(void);

void tcf_test_func2(void) {
    usleep(1000);
}

void tcf_test_func1(void) {
    tcf_test_func2();
}

void tcf_test_func0(void) {
    tcf_test_func1();
}

char * tcf_test_array = NULL;

static void * test_sub(void * x) {
    volatile int * test_done = (int *)x;
    while (!*test_done) {
        tcf_test_func0();
    }
    return NULL;
}

void test_proc(void) {
    int i;
    pthread_t thread[4];
    int test_done = 0;
#if defined(WIN32)
    HANDLE h;
    tcf_test_array = loc_alloc(0x1000);
    h = OpenThread(THREAD_SUSPEND_RESUME, FALSE, GetCurrentThreadId());
    SuspendThread(h);
    CloseHandle(h);
#endif
    tcf_test_func0();
    for (i = 0; i < 4; i++) {
        thread[i] = 0;
    }
    for (i = 0; i < 4; i++) {
        if (pthread_create(thread + i, &pthread_create_attr, test_sub, &test_done) != 0) {
            perror("pthread_create");
            break;
        }
    }
    for (i = 0; i < 9; i++) {
        tcf_test_func0();
    }
    test_done = 1;
    for (i = 0; i < 4; i++) {
        if (thread[i]) pthread_join(thread[i], NULL);
    }
}

int run_test_process(pid_t * res) {
#if defined(WIN32)
    Context * ctx = NULL;
    int r = 0;
    int error = 0;
    char fnm[FILE_PATH_SIZE];
    char cmd[FILE_PATH_SIZE];
    STARTUPINFO si;
    PROCESS_INFORMATION prs;
    memset(&si, 0, sizeof(si));
    memset(&prs, 0, sizeof(prs));
    if (GetModuleFileName(NULL, fnm, sizeof(fnm)) == 0) {
        error = GetLastError();
    }
    si.cb = sizeof(si);
    strcpy(cmd, "agent.exe -t");
    if (error == 0 && CreateProcess(fnm, cmd, NULL, NULL, FALSE, 0, NULL, NULL, &si, &prs) == 0) {
        error = GetLastError();
    }
    while (error == 0) {
        DWORD cnt = SuspendThread(prs.hThread);
        if (cnt == (DWORD)-1) {
            error = GetLastError();
            break;
        }
        ResumeThread(prs.hThread);
        if (cnt > 0) break;
        Sleep(10);
    }
    if (error != 0) {
        errno = EINVAL;
        return -1;
    }
    if (res != NULL) *res = prs.dwProcessId;
    r = context_attach(prs.dwProcessId, &ctx);
    CloseHandle(prs.hThread);
    CloseHandle(prs.hProcess);
    return 0;
#elif defined(_WRS_KERNEL)
    int tid = taskCreate("tTcf", 100, 0, 0x4000, (FUNCPTR)test_proc, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    if (tid == 0) return -1;
    taskStop(tid);
    taskActivate(tid);
    assert(taskIsStopped(tid));
    if (tcf_test_array == NULL) tcf_test_array = loc_alloc(0x1000);
    if (res != NULL) *res = tid;
    return context_attach(tid, NULL);
#else
    /* Create child process to debug */
    Context * ctx = NULL;
    int pid = fork();
    if (pid < 0) return -1;
    if (pid == 0) {
        tcf_test_array = loc_alloc(0x1000);
        tkill(getpid(), SIGSTOP);
        test_proc();
        exit(0);
    }
    if (res != NULL) *res = pid;
    if (context_attach(pid, &ctx) < 0) return -1;
    ctx->pending_intercept = 1;
    return 0;
#endif
}


