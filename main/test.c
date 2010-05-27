/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
 * Agent self-testing service.
 */

#include <config.h>

#if ENABLE_RCBP_TEST

#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <signal.h>
#include <assert.h>
#include <framework/myalloc.h>
#include <framework/trace.h>
#include <framework/errors.h>
#include <services/diagnostics.h>
#include <main/test.h>
#if defined(WIN32)
#  include <system/Windows/context-win32.h>
#endif

typedef enum test_enum {
    enum_val1 = 1,
    enum_val2 = 2,
    enum_val3 = 3
} test_enum;

typedef union test_union {
    int x;
    float y;
} test_union;

typedef struct test_struct {
    test_enum f_enum;
    int f_int;
    struct test_struct * f_struct;
    float f_float;
    double f_double;
    test_union f_union;
} test_struct;

typedef int test_array[10001];

extern void tcf_test_func3(void);
extern void tcf_test_func2(void);
extern void tcf_test_func1(void);
extern void tcf_test_func0(enum test_enum);

/* Main purpose of this declaration is to pull basic types info into DWARF */
char tcf_test_char = 0;
short tcf_test_short = 0;
long tcf_test_long = 0;

void tcf_test_func3(void) {
    usleep(1000);
}

void tcf_test_func2(void) {
    int func2_local1 = 1;
    int func2_local2 = 2;
    test_struct func2_local3 = { enum_val3, 153, NULL, 3.14f, 2.71 };
    func2_local3.f_struct = &func2_local3;
    tcf_test_func3();
    func2_local1++;
    func2_local2 = func2_local1;
}

void tcf_test_func1(void) {
    tcf_test_func2();
}

void tcf_test_func0(test_enum e) {
    tcf_test_func1();
}

static char array[0x1000];
char * tcf_test_array = array;

static void * test_sub(void * x) {
    volatile int * test_done = (int *)x;
    while (!*test_done) {
        tcf_test_func0(enum_val3);
    }
    return NULL;
}

void test_proc(void) {
    int i;
    pthread_t thread[4];
    int test_done = 0;
    tcf_test_func0(enum_val1);
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
        tcf_test_func0(enum_val2);
    }
    test_done = 1;
    for (i = 0; i < 4; i++) {
        if (thread[i]) pthread_join(thread[i], NULL);
    }
}

int find_test_symbol(Context * ctx, char * name, void ** addr, int * sym_class) {
    /* This code allows to run TCF diagnostic tests when symbols info is not available */
    if (is_test_process(ctx) && strncmp(name, "tcf_test_", 9) == 0) {
        *addr = NULL;
        if (strcmp(name, "tcf_test_array") == 0) {
            *sym_class = SYM_CLASS_REFERENCE;
            *addr = &tcf_test_array;
        }
        else {
            *sym_class = SYM_CLASS_FUNCTION;
            if (strcmp(name, "tcf_test_func0") == 0) *addr = (void *)&tcf_test_func0;
            else if (strcmp(name, "tcf_test_func1") == 0) *addr = (void *)&tcf_test_func1;
            else if (strcmp(name, "tcf_test_func2") == 0) *addr = (void *)&tcf_test_func2;
            else if (strcmp(name, "tcf_test_func3") == 0) *addr = (void *)&tcf_test_func3;
        }
        if (*addr != NULL) return 0;
    }
    errno = ERR_SYM_NOT_FOUND;
    return -1;
}

#if defined(WIN32)
typedef struct ContextAttachArgs {
    ContextAttachCallBack * done;
    void * data;
    HANDLE thread;
    HANDLE process;
} ContextAttachArgs;

static void done_context_attach(int error, Context * ctx, void * data) {
    ContextAttachArgs * args = (ContextAttachArgs *)data;
    args->done(error, ctx, args->data);
    assert(error || args->process != get_context_handle(ctx));
    CloseHandle(args->thread);
    CloseHandle(args->process);
    loc_free(args);
}
#endif /* defined(WIN32) */

int run_test_process(ContextAttachCallBack * done, void * data) {
#if defined(WIN32)
    char fnm[FILE_PATH_SIZE];
    char cmd[FILE_PATH_SIZE];
    int res = 0;
    STARTUPINFO si;
    PROCESS_INFORMATION prs;
    ContextAttachArgs * args;

    memset(&si, 0, sizeof(si));
    memset(&prs, 0, sizeof(prs));
    memset(fnm, 0, sizeof(fnm));
    if (GetModuleFileName(NULL, fnm, sizeof(fnm)) == 0) {
        set_win32_errno(GetLastError());
        return -1;
    }
    si.cb = sizeof(si);
    strcpy(cmd, "agent.exe -t");
    if (CreateProcess(fnm, cmd, NULL, NULL,
            FALSE, CREATE_SUSPENDED | CREATE_DEFAULT_ERROR_MODE | CREATE_NO_WINDOW,
            NULL, NULL, &si, &prs) == 0) {
        set_win32_errno(GetLastError());
        return -1;
    }
    args = (ContextAttachArgs *)loc_alloc(sizeof(ContextAttachArgs));
    args->done = done;
    args->data = data;
    args->thread = prs.hThread;
    args->process = prs.hProcess;
    res = context_attach(prs.dwProcessId, done_context_attach, args, 0);
    if (res != 0) loc_free(args);
    return res;
#elif defined(_WRS_KERNEL)
    int tid = taskCreate("tTcf", 100, 0, 0x4000, (FUNCPTR)test_proc, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    if (tid == 0) return -1;
    taskStop(tid);
    taskActivate(tid);
    assert(taskIsStopped(tid));
    return context_attach(tid, done, data, 0);
#else
    /* Create child process to debug */
    int pid = fork();
    if (pid < 0) return -1;
    if (pid == 0) {
        int fd;
        if (context_attach_self() < 0) exit(1);
        fd = sysconf(_SC_OPEN_MAX);
        while (fd-- > 2) close(fd);
        if (tkill(getpid(), SIGSTOP) < 0) exit(1);
        test_proc();
        exit(0);
    }
    return context_attach(pid, done, data, 1);
#endif
}

#endif /* ENABLE_RCBP_TEST */
