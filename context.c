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
 * This module handles process/thread OS contexts and their state machine.
 */

#include "mdep.h"
#include <stdlib.h>
#include <assert.h>
#include <errno.h>
#include <signal.h>
#include "context.h"
#include "events.h"
#include "errors.h"
#include "trace.h"
#include "myalloc.h"
#include "breakpoints.h"

#define CONTEXT_PID_ROOT_SIZE 1024
#define CONTEXT_PID_HASH(PID) ((PID) % CONTEXT_PID_ROOT_SIZE)
static LINK context_pid_root[CONTEXT_PID_ROOT_SIZE];
static ContextEventListener * event_listeners = NULL;

LINK context_root = { NULL, NULL };

#define CASE(var) case var: return ""#var;
char * signal_name(int signal) {
#ifndef WIN32
    switch (signal) {
    CASE(SIGHUP)
    CASE(SIGINT)
    CASE(SIGQUIT)
    CASE(SIGILL)
    CASE(SIGTRAP)
    CASE(SIGABRT)
    CASE(SIGBUS)
    CASE(SIGFPE)
    CASE(SIGKILL)
    CASE(SIGUSR1)
    CASE(SIGSEGV)
    CASE(SIGUSR2)
    CASE(SIGPIPE)
    CASE(SIGALRM)
    CASE(SIGTERM)
#ifdef SIGSTKFLT
    CASE(SIGSTKFLT)
#endif
    CASE(SIGCHLD)
    CASE(SIGCONT)
    CASE(SIGSTOP)
    CASE(SIGTSTP)
    CASE(SIGTTIN)
    CASE(SIGTTOU)
    CASE(SIGURG)
    CASE(SIGXCPU)
    CASE(SIGXFSZ)
    CASE(SIGVTALRM)
    CASE(SIGPROF)
#ifdef SIGWINCH
    CASE(SIGWINCH)
#endif
#ifdef SIGIO
    CASE(SIGIO)
#endif
#ifdef SIGPWR
    CASE(SIGPWR)
#endif
    CASE(SIGSYS)
    }
#endif
    return NULL;
}
#undef CASE

Context * context_find_from_pid(pid_t pid) {
    LINK * qhp = &context_pid_root[CONTEXT_PID_HASH(pid)];
    LINK * qp;

    assert(is_dispatch_thread());
    for (qp = qhp->next; qp != qhp; qp = qp->next) {
        Context * ctx = pidl2ctxp(qp);
        if (ctx->pid == pid && !ctx->exited) return ctx;
    }
    return NULL;
}

static Context * create_context(pid_t pid) {
    LINK * qhp = &context_pid_root[CONTEXT_PID_HASH(pid)];
    Context * ctx = (Context *)loc_alloc_zero(sizeof(Context));

    assert(context_find_from_pid(pid) == NULL);
    ctx->pid = pid;
    ctx->ref_count = 1;
    list_init(&ctx->children);
    list_add_first(&ctx->ctxl, &context_root);
    list_add_first(&ctx->pidl, qhp);
    return ctx;
}

char * pid2id(pid_t pid, pid_t parent) {
    static char s[64];
    char * p = s + sizeof(s);
    unsigned long n = (long)pid;
    *(--p) = 0;
    do {
        *(--p) = (char)(n % 10 + '0');
        n = n / 10;
    }
    while (n != 0);
    if (parent != 0) {
        n = (long)parent;
        *(--p) = '.';
        do {
            *(--p) = (char)(n % 10 + '0');
            n = n / 10;
        }
        while (n != 0);
    }
    *(--p) = 'P';
    return p;
}

char * ctx2id(Context * ctx) {
    /* For now this functions should only be used for processes, but
     * once linux have a context for the process and a separate
     * context for the initial thread, then this function can be used
     * to get the context-id for any context. */
    assert(ctx->parent == NULL);
    return pid2id(ctx->pid, 0);
}

char * thread_id(Context * ctx) {
    assert(context_has_state(ctx));
    if (ctx->parent == NULL) return pid2id(ctx->pid, ctx->pid);
    assert(ctx->parent->parent == NULL);
    return pid2id(ctx->pid, ctx->parent->pid);
}

char * container_id(Context * ctx) {
    if (ctx->parent != NULL) ctx = ctx->parent;
    assert(ctx->parent == NULL);
    return pid2id(ctx->pid, 0);
}

pid_t id2pid(char * id, pid_t * parent) {
    pid_t pid = 0;
    if (parent != NULL) *parent = 0;
    if (id == NULL) return 0;
    if (id[0] != 'P') return 0;
    if (id[1] == 0) return 0;
    pid = (pid_t)strtol(id + 1, &id, 10);
    if (id[0] == '.') {
        if (id[1] == 0) return 0;
        if (parent != NULL) *parent = pid;
        pid = (pid_t)strtol(id + 1, &id, 10);
    }
    if (id[0] != 0) return 0;
    return pid;
}

Context * id2ctx(char * id) {
    pid_t pid = id2pid(id, NULL);
    if (pid == 0) return NULL;
    return context_find_from_pid(pid);
}

void context_lock(Context * ctx) {
    assert(ctx->ref_count > 0);
    ctx->ref_count++;
}

void context_unlock(Context * ctx) {
    if (--(ctx->ref_count) == 0) {
        assert(list_is_empty(&ctx->children));
        assert(ctx->parent == NULL);
        list_remove(&ctx->ctxl);
        list_remove(&ctx->pidl);
        if (ctx->pending_clone) loc_free(ctx->pending_clone);
        loc_free(ctx);
    }
}

char * context_state_name(Context * ctx) {
    if (ctx->exited) return "exited";
    if (ctx->intercepted) return "intercepted";
    if (ctx->stopped) return "stopped";
    return "running";
}

static void event_context_created(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    while (listener != NULL) {
        if (listener->context_created != NULL) {
            listener->context_created(ctx, listener->client_data);
        }
        listener = listener->next;
    }
}

static void event_context_changed(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    while (listener != NULL) {
        if (listener->context_changed != NULL) {
            listener->context_changed(ctx, listener->client_data);
        }
        listener = listener->next;
    }
}

static void event_context_stopped(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    while (listener != NULL) {
        if (listener->context_stopped != NULL) {
            listener->context_stopped(ctx, listener->client_data);
        }
        listener = listener->next;
    }
}

static void event_context_started(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    while (listener != NULL) {
        if (listener->context_started != NULL) {
            listener->context_started(ctx, listener->client_data);
        }
        listener = listener->next;
    }
}

static void event_context_exited(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    while (listener != NULL) {
        if (listener->context_exited != NULL) {
            listener->context_exited(ctx, listener->client_data);
        }
        listener = listener->next;
    }
}

#if defined(WIN32)

struct DebugThreadArgs {
    int error;
    Context * ctx;
    DWORD debug_thread_id;
    HANDLE debug_thread;
    HANDLE debug_thread_semaphore;
};

struct DebugEvent {
    HANDLE event_semaphore;
    DEBUG_EVENT event;
    DWORD continue_status;
};

typedef struct DebugThreadArgs DebugThreadArgs;
typedef struct DebugEvent DebugEvent;

char * event_name(int event) {
    static char buf[32];
    snprintf(buf, sizeof(buf), "0x%08x", event);
    return buf;
}

static char * win32_debug_event_name(int event) {
    switch (event) {
    case CREATE_PROCESS_DEBUG_EVENT:
        return "CREATE_PROCESS_DEBUG_EVENT";
    case CREATE_THREAD_DEBUG_EVENT:
        return "CREATE_THREAD_DEBUG_EVENT";
    case EXCEPTION_DEBUG_EVENT:
        return "EXCEPTION_DEBUG_EVENT";
    case EXIT_PROCESS_DEBUG_EVENT:
        return "EXIT_PROCESS_DEBUG_EVENT";
    case EXIT_THREAD_DEBUG_EVENT:
        return "EXIT_THREAD_DEBUG_EVENT";
    case LOAD_DLL_DEBUG_EVENT:
        return "LOAD_DLL_DEBUG_EVENT";
    case OUTPUT_DEBUG_STRING_EVENT:
        return "OUTPUT_DEBUG_STRING_EVENT";
    case UNLOAD_DLL_DEBUG_EVENT:
        return "UNLOAD_DLL_DEBUG_EVENT";
    }
    return "Unknown";
}

static void event_win32_context_stopped(void * arg) {
    Context * ctx = (Context *)arg;
    DWORD event_code = ctx->pending_event.ExceptionRecord.ExceptionCode;

    if (ctx->stopped && event_code == 0) return;
    memcpy(&ctx->suspend_reason, &ctx->pending_event, sizeof(EXCEPTION_DEBUG_INFO));
    memset(&ctx->pending_event, 0, sizeof(EXCEPTION_DEBUG_INFO));

    trace(LOG_CONTEXT, "context: stopped: ctx %#x, pid %d, exception 0x%08x",
        ctx, ctx->pid, event_code);
    assert(is_dispatch_thread());
    assert(!ctx->stopped);
    assert(ctx->handle != NULL);
    assert(ctx->parent != NULL);

    if (SuspendThread(ctx->handle) == (DWORD)-1) {
        DWORD err = GetLastError();
        if (err == ERROR_ACCESS_DENIED && event_code == 0) return; /* Already exited */
        trace(LOG_ALWAYS, "Can't suspend thread: tid %d, error %d", ctx->pid, err);
        return;
    }

    ctx->regs_error = 0;
    memset(&ctx->regs, 0, sizeof(ctx->regs));
    ctx->regs.ContextFlags = CONTEXT_CONTROL | CONTEXT_INTEGER;
    if (GetThreadContext(ctx->handle, &ctx->regs) == 0) {
        DWORD err = GetLastError();
        trace(LOG_ALWAYS, "Can't read thread registers: ctx %#x, pid %d, error %d",
            ctx, ctx->pid, err);
        set_win32_errno(err);
        ctx->regs_error = errno;
    }
    else {
        trace(LOG_CONTEXT, "context: get regs OK: ctx %#x, pid %d, PC %#x",
            ctx, ctx->pid, get_regs_PC(ctx->regs));
    }

    ctx->signal = SIGTRAP;
    ctx->event = event_code;
    ctx->stopped = 1;
    ctx->stopped_by_bp = 0;
    switch (event_code) {
    case 0:
        break;
    case EXCEPTION_SINGLE_STEP:
        ctx->pending_step = 0;
        break;
    case EXCEPTION_BREAKPOINT:
        if (!ctx->regs_error && is_breakpoint_address(ctx, get_regs_PC(ctx->regs) - BREAK_SIZE)) {
            set_regs_PC(ctx->regs, get_regs_PC(ctx->regs) - BREAK_SIZE);
            ctx->regs_dirty = 1;
            ctx->stopped_by_bp = 1;
        }
        break;
    default:
        ctx->pending_intercept = 1;
        break;
    }
    event_context_stopped(ctx);
}

static void event_win32_context_started(Context * ctx) {
    trace(LOG_CONTEXT, "context: started: ctx %#x, pid %d", ctx, ctx->pid);
    assert(ctx->stopped);
    ctx->stopped = 0;
    event_context_started(ctx);
}

static void event_win32_context_exited(Context * ctx) {
    assert(!ctx->exited);
    if (ctx->stopped) {
        event_win32_context_started(ctx);
    }
    while (!list_is_empty(&ctx->children)) {
        Context * c = cldl2ctxp(ctx->children.next);
        assert(c->parent == ctx);
        event_win32_context_exited(c);
    }
    ctx->exiting = 0;
    ctx->exited = 1;
    event_context_exited(ctx);
    if (ctx->handle != NULL) {
        CloseHandle(ctx->handle);
        ctx->handle = NULL;
    }
    if (ctx->file_handle != NULL) {
        CloseHandle(ctx->file_handle);
        ctx->file_handle = NULL;
    }
    if (ctx->parent != NULL) {
        list_remove(&ctx->cldl);
        context_unlock(ctx->parent);
        ctx->parent = NULL;
    }
    context_unlock(ctx);
}

static int win32_resume(Context * ctx) {
    if (ctx->regs_dirty && SetThreadContext(ctx->handle, &ctx->regs) == 0) {
        int err = GetLastError();
        trace(LOG_ALWAYS, "Can't write thread registers: ctx %#x, pid %d, error %d",
            ctx, ctx->pid, err);
        set_win32_errno(err);
        return -1;
    }
    ctx->regs_dirty = 0;
    if (ctx->pending_event.ExceptionRecord.ExceptionCode != 0) {
        event_win32_context_started(ctx);
        post_event(event_win32_context_stopped, ctx);
        return 0;
    }
    if (ctx->parent->pending_signals & (1 << SIGKILL)) {
        if (!TerminateProcess(ctx->parent->handle, 1)) {
            set_win32_errno(GetLastError());
            return -1;
        }
        ctx->parent->pending_signals &= ~(1 << SIGKILL);
    }
    while (1) {
        DWORD cnt = ResumeThread(ctx->handle);
        if (cnt == (DWORD)-1) {
            DWORD err = GetLastError();
            trace(LOG_ALWAYS, "Can't resume thread: error %d", err);
            set_win32_errno(err);
            return -1;
        }
        if (cnt <= 1) break;
    }
    event_win32_context_started(ctx);
    return 0;
}

static void debug_event_handler(void * x) {
    DebugEvent * args = (DebugEvent *)x;
    DEBUG_EVENT * debug_event = &args->event;
    Context * prs = context_find_from_pid(debug_event->dwProcessId);
    Context * ctx = context_find_from_pid(debug_event->dwThreadId);

    assert(prs != NULL);
    switch (debug_event->dwDebugEventCode) {
    case CREATE_PROCESS_DEBUG_EVENT:
        assert(ctx == NULL);
        assert(prs->handle == NULL);
        if (prs->handle != NULL) {
            CloseHandle(debug_event->u.CreateProcessInfo.hThread);
            break;
        }
        prs->handle = debug_event->u.CreateProcessInfo.hProcess;
        prs->file_handle = debug_event->u.CreateProcessInfo.hFile;
        prs->base_address = debug_event->u.CreateProcessInfo.lpBaseOfImage;
        assert(prs->handle != NULL);
        event_context_created(prs);
        ctx = create_context(debug_event->dwThreadId);
        ctx->mem = debug_event->dwProcessId;
        ctx->handle = debug_event->u.CreateProcessInfo.hThread;
        ctx->parent = prs;
        prs->ref_count++;
        list_add_first(&ctx->cldl, &prs->children);
        event_context_created(ctx);
        ctx->pending_intercept = 1;
        break;
    case CREATE_THREAD_DEBUG_EVENT:
        assert(ctx == NULL);
        ctx = create_context(debug_event->dwThreadId);
        ctx->mem = debug_event->dwProcessId;
        ctx->handle = debug_event->u.CreateThread.hThread;
        ctx->parent = prs;
        prs->ref_count++;
        list_add_first(&ctx->cldl, &prs->children);
        event_context_created(ctx);
        event_win32_context_stopped(ctx);
        break;
    case EXCEPTION_DEBUG_EVENT:
        if (ctx == NULL) break;
        assert(ctx->pending_event.ExceptionRecord.ExceptionCode == 0);
        args->continue_status = DBG_EXCEPTION_NOT_HANDLED;
        switch (args->event.u.Exception.ExceptionRecord.ExceptionCode) {
        case EXCEPTION_SINGLE_STEP:
        case EXCEPTION_BREAKPOINT:
            args->continue_status = DBG_CONTINUE;
            break;
        }
        memcpy(&ctx->pending_event, &args->event.u.Exception, sizeof(EXCEPTION_DEBUG_INFO));
        if (!ctx->stopped) event_win32_context_stopped(ctx);
        break;
    case EXIT_THREAD_DEBUG_EVENT:
        if (ctx == NULL) break;
        event_win32_context_exited(ctx);
        ctx = NULL;
        break;
    case EXIT_PROCESS_DEBUG_EVENT:
        assert(ctx != NULL);
        event_win32_context_exited(ctx);
        event_win32_context_exited(prs);
        prs = ctx = NULL;
        break;
    case RIP_EVENT:
        trace(LOG_ALWAYS, "System debugging error: debuggee pid %d, error type %d, error code %d",
            debug_event->dwProcessId, debug_event->u.RipInfo.dwType, debug_event->u.RipInfo.dwError);
        break;
    }
    assert(ctx == NULL || ctx->parent == prs);
    ReleaseSemaphore(args->event_semaphore, 1, 0);
}

static void debugger_exit_handler(void * x) {
    DebugThreadArgs * dbg = (DebugThreadArgs *)x;
    Context * prs = dbg->ctx;

    trace(LOG_WAITPID, "debugger thread %d exited, debuggee pid %d",
        dbg->debug_thread_id, prs->pid);

    WaitForSingleObject(dbg->debug_thread, INFINITE);
    CloseHandle(dbg->debug_thread);
    CloseHandle(dbg->debug_thread_semaphore);

    if (!prs->exited) event_win32_context_exited(prs);

    context_unlock(prs);
    loc_free(dbg);
}

static DWORD WINAPI debugger_thread_func(LPVOID x) {
    DebugThreadArgs * args = (DebugThreadArgs *)x;
    HANDLE event_semaphore = CreateSemaphore(NULL, 0, 1, NULL);
    DebugEvent event_buffer;
    DebugEvent create_process;
    DebugEvent fantom_process;
    int state = 0;
    int abort = 0;

    if (event_semaphore == NULL) {
        args->error = GetLastError();
        trace(LOG_ALWAYS, "Can't create semaphore: error %d", args->error);
        ReleaseSemaphore(args->debug_thread_semaphore, 1, 0);
        return 0;
    }

    if (DebugActiveProcess(args->ctx->pid) == 0) {
        args->error = GetLastError();
        trace(LOG_ALWAYS, "Can't attach to a process: error %d", args->error);
        ReleaseSemaphore(args->debug_thread_semaphore, 1, 0);
        CloseHandle(event_semaphore);
        return 0;
    }

    trace(LOG_WAITPID, "debugger thread %d started", GetCurrentThreadId());

    memset(&create_process, 0, sizeof(create_process));
    memset(&fantom_process, 0, sizeof(fantom_process));
    while (!abort) {
        DebugEvent * buf = NULL;
        DEBUG_EVENT * debug_event = &event_buffer.event;

        memset(&event_buffer, 0, sizeof(event_buffer));
        if (WaitForDebugEvent(debug_event, INFINITE) == 0) {
            trace(LOG_ALWAYS, "WaitForDebugEvent() error %d", GetLastError());
            break;
        }
        if (debug_event->dwDebugEventCode == EXCEPTION_DEBUG_EVENT) {
            trace(LOG_WAITPID, "%s, process %d, thread %d, code 0x%08x",
                win32_debug_event_name(debug_event->dwDebugEventCode),
                debug_event->dwProcessId, debug_event->dwThreadId,
                debug_event->u.Exception.ExceptionRecord.ExceptionCode);
        }
        else {
            trace(LOG_WAITPID, "%s, process %d, thread %d",
                win32_debug_event_name(debug_event->dwDebugEventCode),
                debug_event->dwProcessId, debug_event->dwThreadId);
        }
        assert(args->ctx->pid == debug_event->dwProcessId);
        event_buffer.continue_status = DBG_CONTINUE;

        switch (debug_event->dwDebugEventCode) {
        case CREATE_PROCESS_DEBUG_EVENT:
            if (state == 0) {
                memcpy(&create_process, &event_buffer, sizeof(event_buffer));
                state++;
            }
            else {
                /* This looks like a bug in Windows: */
                /* 1. according to the documentation, we should get only one CREATE_PROCESS_DEBUG_EVENT. */
                /* 2. if second CREATE_PROCESS_DEBUG_EVENT is handled immediately, debugee crashes. */
                memcpy(&fantom_process, &event_buffer, sizeof(event_buffer));
                CloseHandle(fantom_process.event.u.CreateProcessInfo.hFile);
                ResumeThread(create_process.event.u.CreateProcessInfo.hThread);
                SuspendThread(fantom_process.event.u.CreateProcessInfo.hThread);
            }
            break;
        case LOAD_DLL_DEBUG_EVENT:
            CloseHandle(debug_event->u.LoadDll.hFile);
            break;
        default:
            if (fantom_process.event.dwThreadId == debug_event->dwThreadId) {
                if (debug_event->dwDebugEventCode == EXIT_THREAD_DEBUG_EVENT) {
                    memset(&fantom_process, 0, sizeof(fantom_process));
                }
                else if (debug_event->dwDebugEventCode == EXCEPTION_DEBUG_EVENT) {
                    event_buffer.continue_status = DBG_EXCEPTION_NOT_HANDLED;
                }
                break;
            }
            if (fantom_process.event.u.CreateProcessInfo.hThread != NULL) {
                /* It seems that leaving this thread suspended is not right thing to do,
                 * however, resuming the thread causes debugee to crash for unknown reason.
                 */
                /* ResumeThread(fantom_process.event.u.CreateProcessInfo.hThread); */
                fantom_process.event.u.CreateProcessInfo.hThread = NULL;
            }
            if (state == 1) {
                create_process.event_semaphore = event_semaphore;
                post_event(debug_event_handler, &create_process);
                ReleaseSemaphore(args->debug_thread_semaphore, 1, 0);
                WaitForSingleObject(event_semaphore, INFINITE);
                state++;
            }
            if (state == 2) {
                event_buffer.event_semaphore = event_semaphore;
                post_event(debug_event_handler, &event_buffer);
                WaitForSingleObject(event_semaphore, INFINITE);
            }
            break;
        }

        if (ContinueDebugEvent(debug_event->dwProcessId, debug_event->dwThreadId, event_buffer.continue_status) == 0) {
            trace(LOG_ALWAYS, "Can't continue debug event: process %d, thread %d, error %d",
                debug_event->dwProcessId, debug_event->dwThreadId, GetLastError());
            break;
        }

        switch (debug_event->dwDebugEventCode) {
        case EXIT_PROCESS_DEBUG_EVENT:
        case RIP_EVENT:
            abort = 1;
            break;
        }
    }

    if (state < 2) ReleaseSemaphore(args->debug_thread_semaphore, 1, 0);

    CloseHandle(event_semaphore);
    post_event(debugger_exit_handler, args);
    return 0;
}

int context_attach(pid_t pid, Context ** res, int selfattach) {
    DebugThreadArgs * dbg = (DebugThreadArgs *)loc_alloc_zero(sizeof(DebugThreadArgs));

    assert(!selfattach);
    dbg->ctx = create_context(pid);
    dbg->ctx->mem = pid;
    assert(dbg->ctx->ref_count == 1);
    if (res != NULL) *res = dbg->ctx;
    context_lock(dbg->ctx);

    dbg->debug_thread_semaphore = CreateSemaphore(NULL, 0, 1, NULL);
    if (dbg->debug_thread_semaphore == NULL) {
        DWORD err = GetLastError();
        trace(LOG_ALWAYS, "Can't create semaphore: error %d", err);
        loc_free(dbg);
        set_win32_errno(err);
        return -1;
    }
    
    dbg->debug_thread = CreateThread(NULL, 0, debugger_thread_func, dbg, 0, &dbg->debug_thread_id);
    if (dbg->debug_thread == NULL) {
        DWORD err = GetLastError();
        trace(LOG_ALWAYS, "Can't create thread: error %d", err);
        CloseHandle(dbg->debug_thread_semaphore);
        loc_free(dbg);
        set_win32_errno(err);
        return -1;
    }


    WaitForSingleObject(dbg->debug_thread_semaphore, INFINITE);

    if (dbg->error) {
        WaitForSingleObject(dbg->debug_thread, INFINITE);
        CloseHandle(dbg->debug_thread);
        CloseHandle(dbg->debug_thread_semaphore);
        loc_free(dbg);
        set_win32_errno(dbg->error);
        return -1;
    }

    return 0;
}

int context_has_state(Context * ctx) {
    return ctx != NULL && ctx->pid != ctx->mem;
}

int context_stop(Context * ctx) {
    trace(LOG_CONTEXT, "context:%s suspending ctx %#x pid %d",
        ctx->pending_intercept ? "" : " temporary", ctx, ctx->pid);
    assert(context_has_state(ctx));
    assert(!ctx->stopped);
    assert(!ctx->exited);
    if (SuspendThread(ctx->handle) == (DWORD)-1) {
        DWORD err = GetLastError();
        if (err == ERROR_ACCESS_DENIED) return 0;
        trace(LOG_ALWAYS, "Can't suspend thread: tid %d, error %d", ctx->pid, err);
        set_win32_errno(err);
        return -1;
    }
    post_event(event_win32_context_stopped, ctx);
    return 0;
}

int context_continue(Context * ctx) {
    trace(LOG_CONTEXT, "context: resuming ctx %#x, pid %d", ctx, ctx->pid);
    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(!ctx->intercepted);
    assert(!ctx->exited);
#ifdef __i386__
    if (!ctx->pending_step && (ctx->regs.EFlags & 0x100) != 0) {
        ctx->regs.EFlags &= ~0x100;
        ctx->regs_dirty = 1;
    }
#endif
    if (ctx->regs_dirty && ctx->regs_error) {
        trace(LOG_ALWAYS, "Can't resume thread, registers copy is invalid: ctx %#x, pid %d, error %d",
            ctx, ctx->pid, ctx->regs_error);
        errno = ctx->regs_error;
        return -1;
    }
    return win32_resume(ctx);
}

int context_single_step(Context * ctx) {
    trace(LOG_CONTEXT, "context: single step ctx %#x, pid %d", ctx, ctx->pid);
    assert(is_dispatch_thread());
    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(!ctx->pending_intercept);
    assert(!ctx->exited);
#ifdef __i386__
    if (ctx->regs_error) {
        trace(LOG_ALWAYS, "Can't resume thread, registers copy is invalid: ctx %#x, pid %d, error %d",
            ctx, ctx->pid, ctx->regs_error);
        errno = ctx->regs_error;
        return -1;
    }
    ctx->regs.EFlags |= 0x100;
    ctx->regs_dirty = 1;
#else
#   error "context_single_step() is not implemented for CPU other then X86"
#endif
    ctx->pending_step = 1;
    return win32_resume(ctx);
}

int context_read_mem(Context * ctx, unsigned long address, void * buf, size_t size) {
    SIZE_T bcnt = 0;
    trace(LOG_CONTEXT, "context: read memory ctx %#x, pid %d, address 0x%08x, size %d", ctx, ctx->pid, address, size);
    assert(is_dispatch_thread());
    if (ctx->parent != NULL) ctx = ctx->parent;
    assert(ctx->pid == ctx->mem);
    if (ReadProcessMemory(ctx->handle, (LPCVOID)address, buf, size, &bcnt) == 0 || bcnt != size) {
        DWORD err = GetLastError();
        trace(LOG_ALWAYS, "Can't read process memory: pid %d, addr %#x, error %d", ctx->pid, address, err);
        set_win32_errno(err);
        return -1;
    }
    return 0;
}

int context_write_mem(Context * ctx, unsigned long address, void * buf, size_t size) {
    SIZE_T bcnt = 0;
    trace(LOG_CONTEXT, "context: write memory ctx %#x, pid %d, address 0x%08x, size %d", ctx, ctx->pid, address, size);
    assert(is_dispatch_thread());
    if (ctx->parent != NULL) ctx = ctx->parent;
    assert(ctx->pid == ctx->mem);
    if (WriteProcessMemory(ctx->handle, (LPVOID)address, buf, size, &bcnt) == 0 || bcnt != size) {
        DWORD err = GetLastError();
        trace(LOG_ALWAYS, "Can't write process memory: pid %d, addr %#x, error %d", ctx->pid, address, err);
        set_win32_errno(err);
        return -1;
    }
    if (FlushInstructionCache(ctx->handle, (LPCVOID)address, size) == 0) {
        DWORD err = GetLastError();
        trace(LOG_ALWAYS, "Can't flush instruction cache: pid %d, error %d", ctx->pid, err);
        set_win32_errno(err);
        return -1;
    }
    return 0;
}

static void init(void) {
}

#elif defined(_WRS_KERNEL)

/* TODO: RTP support */

#include <taskHookLib.h>
#include <private/vxdbgLibP.h>

#define TRACE_EVENT_STEP        2

#define EVENT_HOOK_IGNORE       1
#define EVENT_HOOK_BREAKPOINT   2
#define EVENT_HOOK_STEP_DONE    3
#define EVENT_HOOK_STOP         4
#define EVENT_HOOK_TASK_ADD     5
#define EVENT_HOOK_TASK_DEL     6

struct event_info {
    int                 event;
    VXDBG_CTX           current_ctx;    /* context that hit breakpoint */
    VXDBG_CTX           stopped_ctx;    /* context stopped by the breakpoint */
    REG_SET             regs;           /* task registers before exception */
    UINT32              addr;           /* breakpoint addr */
    int                 bp_info_ok;     /* breakpoint information available */
    VXDBG_BP_INFO       bp_info;        /* breakpoint information */
    SEM_ID              delete_signal;
};

VXDBG_CLNT_ID vxdbg_clnt_id = 0;

#define MAX_EVENTS 64
static struct event_info events[MAX_EVENTS];
static int events_inp = 0;
static int events_out = 0;
static int events_buf_overflow = 0;
static spinlockIsr_t events_lock;
static VX_COUNTING_SEMAPHORE(events_signal_mem);
static SEM_ID events_signal;
static pthread_t events_thread;
static WIND_TCB * main_thread;

char * event_name(int event) {
    switch (event) {
    case 0: return "none";
    case TRACE_EVENT_STEP: return "Single Step"; 
    }
    return NULL;
}

static struct event_info * event_info_alloc(int event) {
    int nxt;
    struct event_info * info;
    SPIN_LOCK_ISR_TAKE(&events_lock);
    if (events_buf_overflow) {
        SPIN_LOCK_ISR_GIVE(&events_lock);
        return NULL;
    }
    info = events + events_inp;
    nxt = (events_inp + 1) % MAX_EVENTS;
    if (nxt == events_out) {
        events_buf_overflow = 1;
        semGive(events_signal);
        SPIN_LOCK_ISR_GIVE(&events_lock);
        return NULL;
    }
    memset(info, 0, sizeof(struct event_info));
    info->event = event;
    events_inp = nxt;
    return info;
}

static void event_info_post(struct event_info * info) {
    assert(info != NULL);
    semGive(events_signal);
    SPIN_LOCK_ISR_GIVE(&events_lock);
}

int context_attach(pid_t pid, Context ** res, int selfattach) {
    struct event_info * info;
    Context * ctx = create_context(pid);

    assert(!selfattach);
    ctx->mem = taskIdSelf();
    assert(ctx->ref_count == 1);
    event_context_created(ctx);
    if (taskIsStopped(pid)) {
        ctx->pending_intercept = 1;
        info = event_info_alloc(EVENT_HOOK_STOP);
        if (info != NULL) {
            info->stopped_ctx.ctxId = pid;
            event_info_post(info);
        }
    }
    if (res != NULL) *res = ctx;
    return 0;
}

int context_has_state(Context * ctx) {
    return 1;
}

int context_stop(Context * ctx) {
    struct event_info * info;
    VXDBG_CTX vxdbg_ctx;

    assert(is_dispatch_thread());
    assert(!ctx->stopped);
    assert(!ctx->exited);
    assert(!ctx->regs_dirty);
    assert(!ctx->intercepted);
    if (ctx->pending_intercept) {
        trace(LOG_CONTEXT, "context: stop ctx %#x, id %#x", ctx, ctx->pid);
    }
    else {
        trace(LOG_CONTEXT, "context: temporary stop ctx %#x, id %#x", ctx, ctx->pid);
    }
    
    vxdbg_ctx.ctxId = ctx->pid;
    vxdbg_ctx.ctxType = VXDBG_CTX_TASK;
    taskLock();
    if (vxdbgStop(vxdbg_clnt_id, &vxdbg_ctx) != OK) {
        int error = errno;
        taskUnlock();
        trace(LOG_ALWAYS, "context: can't stop ctx %#x, id %#x: %s",
                ctx, ctx->pid, errno_to_str(error));
        return -1;
    }
    assert(taskIsStopped(ctx->pid));
    info = event_info_alloc(EVENT_HOOK_STOP);
    if (info != NULL) {
        info->stopped_ctx.ctxId = ctx->pid;
        event_info_post(info);
    }
    taskUnlock();
    return 0;
}

static int kill_context(Context * ctx) {
    ctx->pending_signals &= ~(1 << SIGKILL);
    if (taskDelete(ctx->pid) != OK) {
        int error = errno;
        trace(LOG_ALWAYS, "context: can't kill ctx %#x, id %#x: %s",
                ctx, ctx->pid, errno_to_str(error));
        return -1;
    }
    ctx->stopped = 0;
    event_context_started(ctx);
    ctx->exiting = 0;
    ctx->exited = 1;
    event_context_exited(ctx);
    if (ctx->parent != NULL) {
        list_remove(&ctx->cldl);
        context_unlock(ctx->parent);
        ctx->parent = NULL;
    }
    context_unlock(ctx);
    return 0;
}

int context_continue(Context * ctx) {
    VXDBG_CTX vxdbg_ctx;
    
    assert(is_dispatch_thread());
    assert(ctx->stopped);
    assert(!ctx->pending_intercept);
    assert(!ctx->exited);
    assert(!ctx->pending_step);
    assert(taskIsStopped(ctx->pid));
    trace(LOG_CONTEXT, "context: continue ctx %#x, id %#x", ctx, ctx->pid);

    if (ctx->regs_dirty) {
        if (taskRegsSet(ctx->pid, &ctx->regs) != OK) {
            int error = errno;
            trace(LOG_ALWAYS, "context: can't set regs ctx %#x, id %#x: %s",
                    ctx, ctx->pid, errno_to_str(error));
            return -1;
        }
        ctx->regs_dirty = 0;
    }
    
    if (ctx->pending_signals & (1 << SIGKILL)) {
        return kill_context(ctx);
    }

    vxdbg_ctx.ctxId = ctx->pid;
    vxdbg_ctx.ctxType = VXDBG_CTX_TASK;
    taskLock();
    if (vxdbgCont(vxdbg_clnt_id, &vxdbg_ctx) != OK) {
        int error = errno;
        taskUnlock();
        trace(LOG_ALWAYS, "context: can't continue ctx %#x, id %#x: %s",
                ctx, ctx->pid, errno_to_str(error));
        return -1;
    }
    assert(!taskIsStopped(ctx->pid));
    ctx->stopped = 0;
    taskUnlock();
    event_context_started(ctx);
    return 0;
}

int context_single_step(Context * ctx) {
    VXDBG_CTX vxdbg_ctx;
    struct event_info * info;
    
    assert(is_dispatch_thread());
    assert(ctx->stopped);
    assert(!ctx->pending_intercept);
    assert(!ctx->pending_step);
    assert(!ctx->exited);
    trace(LOG_CONTEXT, "context: single step ctx %#x, id %#x", ctx, ctx->pid);

    if (ctx->regs_dirty) {
        if (taskRegsSet(ctx->pid, &ctx->regs) != OK) {
            int error = errno;
            trace(LOG_ALWAYS, "context: can't set regs ctx %#x, id %#x: %s",
                    ctx, ctx->pid, errno_to_str(error));
            return -1;
        }
        ctx->regs_dirty = 0;
    }

    if (ctx->pending_signals & (1 << SIGKILL)) {
        return kill_context(ctx);
    }

    vxdbg_ctx.ctxId = ctx->pid;
    vxdbg_ctx.ctxType = VXDBG_CTX_TASK;
    taskLock();
    if (vxdbgStep(vxdbg_clnt_id, &vxdbg_ctx, NULL, NULL) != OK) {
        int error = errno;
        taskUnlock();
        trace(LOG_ALWAYS, "context: can't step ctx %#x, id %#x: %d",
                ctx, ctx->pid, errno_to_str(error));
        return -1;
    }
    ctx->pending_step = 1;
    ctx->stopped = 0;
    taskUnlock();
    event_context_started(ctx);
    return 0;
}

int context_read_mem(Context * ctx, unsigned long address, void * buf, size_t size) {
#ifdef _WRS_PERSISTENT_SW_BP
    vxdbgMemRead((void *)address, buf, size);
#else    
    bcopy((void *)address, buf, size);
#endif    
    return 0;
}

int context_write_mem(Context * ctx, unsigned long address, void * buf, size_t size) {
#ifdef _WRS_PERSISTENT_SW_BP
    vxdbgMemWrite((void *)address, buf, size);
#else
    bcopy(buf, (void *)address, size);
#endif    
    return 0;
}

static void event_handler(void * arg) {
    struct event_info * info = (struct event_info *)arg;
    Context * current_ctx = context_find_from_pid(info->current_ctx.ctxId); 
    Context * stopped_ctx = context_find_from_pid(info->stopped_ctx.ctxId);
    
    switch (info->event) {
    case EVENT_HOOK_BREAKPOINT:
        if (stopped_ctx == NULL) break;
        assert(!stopped_ctx->stopped);
        assert(!stopped_ctx->regs_dirty);
        assert(!stopped_ctx->intercepted);
        stopped_ctx->regs_error = 0;
        stopped_ctx->regs = info->regs;
        stopped_ctx->signal = SIGTRAP;
        assert(get_regs_PC(stopped_ctx->regs) == info->addr);
        stopped_ctx->event = 0;
        stopped_ctx->pending_step = 0;
        stopped_ctx->stopped = 1;
        stopped_ctx->stopped_by_bp = info->bp_info_ok;
        if (stopped_ctx->stopped_by_bp && !is_breakpoint_address(stopped_ctx, get_regs_PC(stopped_ctx->regs))) {
            /* Break instruction that is not planted by us */
            stopped_ctx->stopped_by_bp = 0;
            stopped_ctx->pending_intercept = 1;
        }
        stopped_ctx->bp_info = info->bp_info;
        if (current_ctx != NULL) stopped_ctx->bp_pid = current_ctx->pid;
        assert(taskIsStopped(stopped_ctx->pid));
        event_context_stopped(stopped_ctx);
        break;
    case EVENT_HOOK_STEP_DONE:
        if (current_ctx == NULL) break;
        assert(!current_ctx->stopped);
        assert(!current_ctx->regs_dirty);
        assert(!current_ctx->intercepted);
        current_ctx->regs_error = 0;
        current_ctx->regs = info->regs;
        current_ctx->signal = SIGTRAP;
        current_ctx->event = TRACE_EVENT_STEP;
        current_ctx->pending_step = 0;
        current_ctx->stopped = 1;
        assert(taskIsStopped(current_ctx->pid));
        event_context_stopped(current_ctx);
        break;
    case EVENT_HOOK_STOP:
        if (stopped_ctx == NULL) break;
        assert(!stopped_ctx->stopped);
        stopped_ctx->regs_error = 0;
        if (taskRegsGet(stopped_ctx->pid, &stopped_ctx->regs) != OK) {
            stopped_ctx->regs_error = errno;
            assert(stopped_ctx->regs_error != 0);
        }
        stopped_ctx->signal = SIGSTOP;
        stopped_ctx->event = 0;
        stopped_ctx->pending_step = 0;
        stopped_ctx->stopped = 1;
        assert(taskIsStopped(stopped_ctx->pid));
        event_context_stopped(stopped_ctx);
        break;
    case EVENT_HOOK_TASK_ADD:
        if (current_ctx == NULL) break;
        assert(stopped_ctx == NULL);
        stopped_ctx = create_context((pid_t)info->stopped_ctx.ctxId);
        assert(stopped_ctx->ref_count == 1);
        stopped_ctx->mem = current_ctx->mem;
        stopped_ctx->parent = current_ctx->parent != NULL ? current_ctx->parent : current_ctx;
        stopped_ctx->parent->ref_count++;
        list_add_first(&stopped_ctx->cldl, &stopped_ctx->parent->children);
        event_context_created(stopped_ctx);
        break;
    case EVENT_HOOK_TASK_DEL:
        if (stopped_ctx != NULL) {
            assert(!stopped_ctx->stopped);
            assert(!stopped_ctx->intercepted);
            assert(!stopped_ctx->exited);
            stopped_ctx->pending_step = 0;
            stopped_ctx->exiting = 0;
            stopped_ctx->exited = 1;
            event_context_exited(stopped_ctx);
            if (stopped_ctx->parent != NULL) {
                list_remove(&stopped_ctx->cldl);
                context_unlock(stopped_ctx->parent);
                stopped_ctx->parent = NULL;
            }
            context_unlock(stopped_ctx);
        }
        semGive(info->delete_signal);
        break;
    default:
        assert(0);
        break;
    }
    loc_free(info);
}

static void event_error(void * arg) {
    trace(LOG_ALWAYS, "Fatal error: VXDBG events buffer overflow");
    exit(1);
}

static void * event_thread_func(void * arg) {
    struct event_info * info;
    
    taskPrioritySet(0, VX_TASK_PRIORITY_MIN);
    for (;;) {
        semTake(events_signal, WAIT_FOREVER);
        info = (struct event_info *)loc_alloc(sizeof(struct event_info));
        
        SPIN_LOCK_ISR_TAKE(&events_lock);
        if (events_buf_overflow && events_inp == events_out) {
            SPIN_LOCK_ISR_GIVE(&events_lock);
            break;
        }
        assert(events_inp != events_out);
        *info = events[events_out];
        events_out = (events_out + 1) % MAX_EVENTS;
        SPIN_LOCK_ISR_GIVE(&events_lock);
        
        if (info->event != EVENT_HOOK_IGNORE) {
            post_event(event_handler, info);
        }
    }
    post_event(event_error, NULL);
}

static void vxdbg_event_hook(
        VXDBG_CTX *     current_ctx,    /* context that hit breakpoint */
        VXDBG_CTX *     stopped_ctx,    /* context stopped by the breakpoint */
        REG_SET *       regs,           /* task registers before exception */
        UINT32          addr,           /* breakpoint addr */
        VXDBG_BP_INFO * bp_info) {      /* breakpoint information */
    
    struct event_info * info = event_info_alloc(EVENT_HOOK_BREAKPOINT);
    if (info != NULL) {
        if (stopped_ctx == NULL) info->event = EVENT_HOOK_STEP_DONE;
        if (current_ctx != NULL) info->current_ctx = *current_ctx;
        if (stopped_ctx != NULL) info->stopped_ctx = *stopped_ctx;
        if (regs != NULL) info->regs = *regs;
        info->addr = addr;
        if (bp_info != NULL) {
            info->bp_info_ok = 1;
            info->bp_info = *bp_info;
        }
        event_info_post(info);
    }
}

static void task_create_hook(WIND_TCB * tcb) {
    struct event_info * info = event_info_alloc(EVENT_HOOK_TASK_ADD);
    if (info != NULL) {
        info->current_ctx.ctxId = taskIdSelf();
        info->stopped_ctx.ctxId = (UINT32)tcb;
        event_info_post(info);
    }
}

static void task_delete_hook(WIND_TCB * tcb) {
    if (tcb != main_thread && taskIdCurrent != main_thread) {
        struct event_info * info = event_info_alloc(EVENT_HOOK_TASK_DEL);
        if (info != NULL) {
            VX_COUNTING_SEMAPHORE(signal_mem);
            SEM_ID signal = info->delete_signal = semCInitialize(signal_mem, SEM_Q_FIFO, 0);
            info->current_ctx.ctxId = taskIdSelf();
            info->stopped_ctx.ctxId = (UINT32)tcb;
            event_info_post(info);
            semTake(signal, WAIT_FOREVER);
            semTerminate(signal);
        }
    }
}

static void init(void) {
    SPIN_LOCK_ISR_INIT(&events_lock, 0);
    main_thread = taskIdCurrent;
    if ((events_signal = semCInitialize(events_signal_mem, SEM_Q_FIFO, 0)) == NULL) {
        check_error(errno);
    }
    vxdbg_clnt_id = vxdbgClntRegister(EVT_BP);
    if (vxdbg_clnt_id == NULL) {
        check_error(errno);
    }
    taskCreateHookAdd((FUNCPTR)task_create_hook);
    taskDeleteHookAdd((FUNCPTR)task_delete_hook);
    vxdbgHookAdd(vxdbg_clnt_id, EVT_BP, vxdbg_event_hook);
    vxdbgHookAdd(vxdbg_clnt_id, EVT_TRACE, vxdbg_event_hook);   
    check_error(pthread_create(&events_thread, &pthread_create_attr, event_thread_func, NULL));
}

#else

#include <sys/wait.h>
#include <sys/types.h>
#include <sys/ptrace.h>
#include <sched.h>

#define PTRACE_SETOPTIONS       0x4200
#define PTRACE_GETEVENTMSG      0x4201
#define PTRACE_GETSIGINFO       0x4202
#define PTRACE_SETSIGINFO       0x4203

#define PTRACE_O_TRACESYSGOOD   0x00000001
#define PTRACE_O_TRACEFORK      0x00000002
#define PTRACE_O_TRACEVFORK     0x00000004
#define PTRACE_O_TRACECLONE     0x00000008
#define PTRACE_O_TRACEEXEC      0x00000010
#define PTRACE_O_TRACEVFORKDONE 0x00000020
#define PTRACE_O_TRACEEXIT      0x00000040

#define PTRACE_EVENT_FORK       1
#define PTRACE_EVENT_VFORK      2
#define PTRACE_EVENT_CLONE      3
#define PTRACE_EVENT_EXEC       4
#define PTRACE_EVENT_VFORK_DONE 5
#define PTRACE_EVENT_EXIT       6

#define USE_ESRCH_WORKAROUND    1

#define WORD_SIZE   4

#define PTRACE_FLAGS ( \
    PTRACE_O_TRACEFORK | \
    PTRACE_O_TRACEVFORK | \
    PTRACE_O_TRACECLONE | \
    PTRACE_O_TRACEEXEC | \
    PTRACE_O_TRACEVFORKDONE | \
    PTRACE_O_TRACEEXIT)

struct pid_exit_info {
    pid_t       pid;
    int         value;
};

struct pid_stop_info {
    pid_t       pid;
    int         signal;
    int         event;
};

static pthread_t wpid_thread;
static pid_t my_pid = 0;
static pthread_mutex_t waitpid_lock;
static pthread_cond_t waitpid_cond;
static int attach_poll_rate;

char * event_name(int event) {
    switch (event) {
    case 0: return "none";
    case PTRACE_EVENT_FORK: return "fork";  
    case PTRACE_EVENT_VFORK: return "vfork";  
    case PTRACE_EVENT_CLONE: return "clone";  
    case PTRACE_EVENT_EXEC: return "exec";  
    case PTRACE_EVENT_VFORK_DONE: return "vfork-done";  
    case PTRACE_EVENT_EXIT: return "exit";  
    default:
        trace(LOG_ALWAYS, "event_name() called with unexpected event code %d", event);
        return "unknown";
    }
}

int context_attach_self(void) {
    pid_t pid = getpid();

    if (ptrace(PTRACE_TRACEME, 0, 0, 0) < 0) {
        int err = errno;
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_TRACEME) failed: pid %d, error %d %s",
              pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    return 0;
}

int context_attach(pid_t pid, Context ** res, int selfattach) {
    Context * ctx = NULL;
    check_error(pthread_mutex_lock(&waitpid_lock));
    if (!selfattach && ptrace(PTRACE_ATTACH, pid, 0, 0) < 0) {
        int err = errno;
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_ATTACH) failed: pid %d, error %d %s",
            pid, err, errno_to_str(err));
        check_error(pthread_mutex_unlock(&waitpid_lock));
        errno = err;
        return -1;
    }
    ctx = create_context(pid);
    ctx->pending_intercept = 1;
    ctx->pending_attach = 1;
    /* TODO: context_attach works only for main task in a process */
    ctx->mem = pid;
    assert(ctx->ref_count == 1);
    attach_poll_rate = 1;
    trace(LOG_WAITPID, "waitpid: poll rate reset");
    check_error(pthread_cond_signal(&waitpid_cond));
    check_error(pthread_mutex_unlock(&waitpid_lock));
    if (res != NULL) *res = ctx;
    return 0;
}

int context_has_state(Context * ctx) {
    return 1;
}

int context_stop(Context * ctx) {
    trace(LOG_CONTEXT, "context:%s suspending ctx %#x pid %d",
        ctx->pending_intercept ? "" : " temporary", ctx, ctx->pid);
    assert(is_dispatch_thread());
    assert(!ctx->exited);
    assert(!ctx->stopped);
    assert(!ctx->regs_dirty);
    assert(!ctx->intercepted);
    if (tkill(ctx->pid, SIGSTOP) < 0) {
        int err = errno;
        trace(LOG_ALWAYS, "error: tkill(SIGSTOP) failed: ctx %#x, pid %d, error %d %s",
            ctx, ctx->pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    return 0;
}

int context_continue(Context * ctx) {
    int signal = 0;
    if (ctx->pending_signals != 0) {
        while ((ctx->pending_signals & (1 << signal)) == 0) signal++;
    }
    assert(signal != SIGSTOP);
    assert(signal != SIGTRAP);
    assert(is_dispatch_thread());
    assert(ctx->stopped);
    assert(!ctx->pending_intercept);
    assert(!ctx->pending_step);
    assert(!ctx->exited);
    trace(LOG_CONTEXT, "context: resuming ctx %#x, pid %d, with signal %d", ctx, ctx->pid, signal);
#ifdef __i386__
    /* Bug in ptrace: trap flag is not cleared after single step */
    if (ctx->regs.eflags & 0x100) {
        ctx->regs.eflags &= ~0x100;
        ctx->regs_dirty = 1;
    }
#endif
    if (ctx->regs_dirty) {
        if (ptrace(PTRACE_SETREGS, ctx->pid, 0, &ctx->regs) < 0) {
            int err = errno;
#if USE_ESRCH_WORKAROUND
            if (err == ESRCH) {
                ctx->regs_dirty = 0;
                ctx->stopped = 0;
                event_context_started(ctx);
                return 0;
            }
#endif
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_SETREGS) failed: ctx %#x, pid %d, error %d %s",
                ctx, ctx->pid, err, errno_to_str(err));
            errno = err;
            return -1;
        }
        ctx->regs_dirty = 0;
    }
    if (ptrace(PTRACE_CONT, ctx->pid, 0, signal) < 0) {
        int err = errno;
#if USE_ESRCH_WORKAROUND
        if (err == ESRCH) {
            ctx->stopped = 0;
            event_context_started(ctx);
            return 0;
        }
#endif
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_CONT, ...) failed: ctx %#x, pid %d, error %d %s",
            ctx, ctx->pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    ctx->pending_signals &= ~(1 << signal);
    ctx->stopped = 0;
    event_context_started(ctx);
    return 0;
}

int context_single_step(Context * ctx) {
    assert(is_dispatch_thread());
    assert(ctx->stopped);
    assert(!ctx->pending_intercept);
    assert(!ctx->pending_step);
    assert(!ctx->exited);
    trace(LOG_CONTEXT, "context: single step ctx %#x, pid %d", ctx, ctx->pid);
    if (ctx->regs_dirty) {
        if (ptrace(PTRACE_SETREGS, ctx->pid, 0, &ctx->regs) < 0) {
            int err = errno;
#if USE_ESRCH_WORKAROUND
            if (err == ESRCH) {
                ctx->regs_dirty = 0;
                ctx->pending_step = 1;
                ctx->stopped = 0;
                event_context_started(ctx);
                return 0;
            }
#endif
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_SETREGS) failed: ctx %#x, pid %d, error %d %s",
                ctx, ctx->pid, err, errno_to_str(err));
            errno = err;
            return -1;
        }
        ctx->regs_dirty = 0;
    }
    if (ptrace(PTRACE_SINGLESTEP, ctx->pid, 0, 0) < 0) {
        int err = errno;
#if USE_ESRCH_WORKAROUND
        if (err == ESRCH) {
            ctx->stopped = 0;
            ctx->pending_step = 1;
            event_context_started(ctx);
            return 0;
        }
#endif
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_SINGLESTEP, ...) failed: ctx %#x, pid %d, error %d %s",
            ctx, ctx->pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    ctx->pending_step = 1;
    ctx->stopped = 0;
    event_context_started(ctx);
    return 0;
}

int context_write_mem(Context * ctx, unsigned long address, void * buf, size_t size) {
    unsigned long word_addr;
    assert(is_dispatch_thread());
    assert(!ctx->exited);
    assert(ctx->stopped);
    trace(LOG_CONTEXT, "context: write memory ctx %#x, pid %d, address 0x%08x, size %d", ctx, ctx->pid, address, size);
    for (word_addr = address & ~3ul; word_addr < address + size; word_addr += WORD_SIZE) {
        int i;
        unsigned int word = 0;
        if (word_addr < address || word_addr + WORD_SIZE > address + size) {
            errno = 0;
            word = ptrace(PTRACE_PEEKDATA, ctx->pid, word_addr, 0);
            if (errno != 0) {
                int err = errno;
                trace(LOG_ALWAYS, "error: ptrace(PTRACE_PEEKDATA, ...) failed: ctx %#x, pid %d, error %d %s",
                    ctx, ctx->pid, err, errno_to_str(err));
                errno = err;
                return -1;
            }
        }
        for (i = 0; i < WORD_SIZE; i++) {
            if (word_addr + i >= address && word_addr + i < address + size) {
                /* TODO: big endian support */
                ((unsigned char *)&word)[i] = ((unsigned char *)buf)[word_addr + i - address];
            }
        }
        if (ptrace(PTRACE_POKEDATA, ctx->pid, word_addr, word) < 0) {
            int err = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_POKEDATA, ...) failed: ctx %#x, pid %d, error %d %s",
                ctx, ctx->pid, err, errno_to_str(err));
            errno = err;
            return -1;
        }
    }
    return 0;
}

int context_read_mem(Context * ctx, unsigned long address, void * buf, size_t size) {
    unsigned long word_addr;
    assert(is_dispatch_thread());
    assert(!ctx->exited);
    assert(ctx->stopped);
    trace(LOG_CONTEXT, "context: read memory ctx %#x, pid %d, address 0x%08x, size %d", ctx, ctx->pid, address, size);
    for (word_addr = address & ~3ul; word_addr < address + size; word_addr += WORD_SIZE) {
        int i;
        unsigned int word = 0;
        errno = 0;
        word = ptrace(PTRACE_PEEKDATA, ctx->pid, word_addr, 0);
        if (errno != 0) {
            int err = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_PEEKDATA, ...) failed: ctx %#x, pid %d, error %d %s",
                ctx, ctx->pid, err, errno_to_str(err));
            errno = err;
            return -1;
        }
        for (i = 0; i < WORD_SIZE; i++) {
            if (word_addr + i >= address && word_addr + i < address + size) {
                /* TODO: big endian support */
                ((unsigned char *)buf)[word_addr + i - address] = ((unsigned char *)&word)[i];
            }
        }
    }
    return 0;
}

static void event_pid_exited(void *arg) {
    struct pid_exit_info *eap = arg;
    Context * ctx;

    ctx = context_find_from_pid(eap->pid);
    if (ctx == NULL) {
        trace(LOG_EVENTS, "event: ctx not found, pid %d, exit status %d", eap->pid, eap->value);
    }
    else {
        if (ctx->stopped || ctx->intercepted || ctx->exited) {
            trace(LOG_EVENTS, "event: ctx %#x, pid %d, exit status %d unexpected, stopped %d, intercepted %d, exited %d",
                ctx, eap->pid, eap->value, ctx->stopped, ctx->intercepted, ctx->exited);
            if (ctx->stopped) {
                ctx->stopped = 0;
                event_context_started(ctx);
            }
        }
        else {
            trace(LOG_EVENTS, "event: ctx %#x, pid %d, exit status %d", ctx, eap->pid, eap->value);
        }
        if (!list_is_empty(&ctx->children)) {
            /* Linux kernel 2.4 does not notify waitpid() when thread exits if the thread is not main thread.
             * As workaround, assume all non-main thread have exited and remove them from ctx->children list.
             */
            while (!list_is_empty(&ctx->children)) {
                Context * c = cldl2ctxp(ctx->children.next);
                assert(!c->exited);
                assert(c->parent == ctx);
                c->exiting = 0;
                c->exited = 1;
                event_context_exited(c);
                list_remove(&c->cldl);
                context_unlock(c->parent);
                c->parent = NULL;
                context_unlock(c);
            }
        }
        /* Note: ctx->exiting should be 1 here. However, PTRACE_EVENT_EXIT can be lost by PTRACE because of racing
         * between PTRACE_CONT and SIGTRAP/PTRACE_EVENT_EXIT. So, ctx->exiting can be 0.
         */
        ctx->exiting = 0;
        ctx->exited = 1;
        event_context_exited(ctx);
        if (ctx->parent != NULL) {
            list_remove(&ctx->cldl);
            context_unlock(ctx->parent);
            ctx->parent = NULL;
        }
        context_unlock(ctx);
    }
    loc_free(eap);
}

static void event_pid_stopped(void * arg) {
    unsigned long msg = 0;
    Context * ctx = NULL;
    Context * ctx2 = NULL;
    struct pid_stop_info * eap = arg;
    struct pid_stop_info * pending_eap;

process_event:
    pending_eap = NULL;
    trace(LOG_EVENTS, "event: pid %d stopped, signal %d, event %s",
        eap->pid, eap->signal, event_name(eap->event));

    ctx = context_find_from_pid(eap->pid);
    if (ctx == NULL) {
        /* Clone & fork notifications can arrive after child
         * notification because the clone/fork notification comes from
         * the parent while the stop notification comes from the child
         * and Linux does not seem to order between them. */
        trace(LOG_EVENTS, "event: pid %d is not traced - expecting OOO clone, fork or vfork event for pid", eap->pid);
        ctx = create_context(eap->pid);
        ctx->pending_clone = eap;
        return;
    }
    else if (ctx->pending_clone != NULL) {
        trace(LOG_ALWAYS, "event: pid %d received stop event before processing of pending_clone event - ignored", eap->pid);
        loc_free(eap);
        return;
    }
    assert(!ctx->exited);
    assert(!ctx->stopped || eap->event == 0 || eap->event == PTRACE_EVENT_EXIT);
    if (ctx->trace_flags != PTRACE_FLAGS) {
        if (ptrace(PTRACE_SETOPTIONS, ctx->pid, 0, PTRACE_FLAGS) < 0) {
            int err = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_SETOPTIONS) failed: pid %d, error %d %s",
                ctx->pid, err, errno_to_str(err));
        }
        else {
            ctx->trace_flags = PTRACE_FLAGS;
        }
    }

    switch (eap->event) {
    case PTRACE_EVENT_FORK:
    case PTRACE_EVENT_VFORK:
    case PTRACE_EVENT_CLONE:
        assert(!ctx->pending_attach);
        if (ptrace(PTRACE_GETEVENTMSG, eap->pid, 0, &msg) < 0) {
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_GETEVENTMSG) failed; pid %d, error %d %s",
                eap->pid, errno, errno_to_str(errno));
            break;
        }
        assert(msg != 0);
        ctx2 = context_find_from_pid(msg);
        if (ctx2) {
            assert(ctx2->pending_clone);
            pending_eap = ctx2->pending_clone;
            ctx2->pending_clone = NULL;
        }
        else {
            ctx2 = create_context(msg);
        }
        assert(ctx2->parent == NULL);
        trace(LOG_EVENTS, "event: new context 0x%x, pid %d", ctx2, ctx2->pid);
        if (eap->event == PTRACE_EVENT_CLONE) {
            ctx2->mem = ctx->mem;
            ctx2->parent = ctx->parent != NULL ? ctx->parent : ctx;
            ctx2->parent->ref_count++;
            list_add_first(&ctx2->cldl, &ctx2->parent->children);
        }
        else {
            ctx2->mem = ctx2->pid;
        }
        assert(ctx2->mem != 0);
        event_context_created(ctx2);
        break;

    case PTRACE_EVENT_EXEC:
        if (!ctx->pending_attach) {
            event_context_changed(ctx);
        }
        break;
    }

    if (eap->signal != SIGSTOP && eap->signal != SIGTRAP) {
        ctx->pending_signals |= 1 << eap->signal;
    }

    if (eap->signal == SIGTRAP && eap->event == PTRACE_EVENT_EXIT) {
        ctx->exiting = 1;
        ctx->regs_dirty = 0;
    }
    if (ctx->pending_attach) {
        ctx->pending_attach = 0;
        event_context_created(ctx);
    }
    if (!ctx->stopped || !ctx->intercepted) {
        unsigned long pc0 = get_regs_PC(ctx->regs);
        assert(!ctx->regs_dirty);
        assert(!ctx->intercepted);
        ctx->regs_error = 0;
        if (ptrace(PTRACE_GETREGS, ctx->pid, 0, &ctx->regs) < 0) {
#if USE_ESRCH_WORKAROUND
            if (errno == ESRCH) {
                /* Racing condition: somebody resumed this context while we are handling stop event.
                 *
                 * One possible cause: main thread has exited forcing children to exit too.
                 * I beleive it is a bug in PTRACE implementation - PTRACE should delay exiting of
                 * a context while it is stopped, but it does not, which causes a nasty racing.
                 *
                 * Workaround: Ignore current event, assume context is running.
                 */
                loc_free(eap);
                return;
            }
#endif
            ctx->regs_error = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_GETREGS) failed; pid %d, error %d %s",
                ctx->pid, errno, errno_to_str(errno));
        }

        trace(LOG_EVENTS, "event: pid %d stopped at PC = %d (0x%08x)",
            ctx->pid, get_regs_PC(ctx->regs), get_regs_PC(ctx->regs));

        if (eap->signal == SIGSTOP && ctx->pending_step && ctx->regs_error == 0 && pc0 == get_regs_PC(ctx->regs)) {
            trace(LOG_EVENTS, "event: pid %d, single step failed because of pending SIGSTOP, retrying");
            ptrace(PTRACE_SINGLESTEP, ctx->pid, 0, 0);
        }
        else {
            ctx->signal = eap->signal;
            ctx->event = eap->event;
            ctx->pending_step = 0;
            ctx->stopped = 1;
            ctx->stopped_by_bp =
                ctx->signal == SIGTRAP && ctx->event == 0 && ctx->regs_error == 0 &&
                is_breakpoint_address(ctx, get_regs_PC(ctx->regs) - BREAK_SIZE);
            if (ctx->stopped_by_bp) {
                set_regs_PC(ctx->regs, get_regs_PC(ctx->regs) - BREAK_SIZE);
                ctx->regs_dirty = 1;
            }
            event_context_stopped(ctx);
        }
    }

    loc_free(eap);
    if (pending_eap != NULL) {
        eap = pending_eap;
        goto process_event;
    }
}

static void * wpid_handler(void * x) {
    pid_t pid;
    int err;
    int status;
    struct timespec timeout;

    attach_poll_rate = 1;
    for (;;) {
        if ((pid = waitpid(-1, &status, __WALL)) == (pid_t)-1) {
            if (errno == ECHILD) {
                check_error(pthread_mutex_lock(&waitpid_lock));
                if(attach_poll_rate < 60*1000) {
                    attach_poll_rate = (attach_poll_rate*3 + 1)/2;
                }
                clock_gettime(CLOCK_REALTIME, &timeout);
                timeout.tv_sec += attach_poll_rate / 1000;
                timeout.tv_nsec += (attach_poll_rate % 1000) * 1000 * 1000;
                if (timeout.tv_nsec >= 1000 * 1000 * 1000) {
                    timeout.tv_nsec -= 1000 * 1000 * 1000;
                    timeout.tv_sec++;
                }
                trace(LOG_WAITPID, "waitpid: poll rate = %d", attach_poll_rate);
                err = pthread_cond_timedwait(&waitpid_cond, &waitpid_lock, &timeout);
                if (err != ETIMEDOUT) check_error(err);
                check_error(pthread_mutex_unlock(&waitpid_lock));
                continue;
            }
            check_error(errno);
        }
        trace(LOG_WAITPID, "waitpid: pid %d status %#x", pid, status);
        if (WIFEXITED(status) || WIFSIGNALED(status)) {
            struct pid_exit_info *eap;

            eap = loc_alloc(sizeof *eap);
            eap->pid = pid;
            eap->value = WIFEXITED(status) ? WEXITSTATUS(status) : -WTERMSIG(status);
            post_event(event_pid_exited, eap);
        }
        else if (WIFSTOPPED(status)) {
            struct pid_stop_info *eap;

            eap = loc_alloc(sizeof *eap);
            eap->pid = pid;
            eap->signal = WSTOPSIG(status);
            eap->event = status >> 16;
            post_event(event_pid_stopped, eap);
        }
        else {
            trace(LOG_ALWAYS, "unexpected status (0x%x) from waitpid (pid %d)", status, pid);
        }
    }
}

static void init(void) {
    check_error(pthread_mutex_init(&waitpid_lock, NULL));
    check_error(pthread_cond_init(&waitpid_cond, NULL));
    my_pid = getpid();
    /* Create thread to get process events using waitpid() */
    check_error(pthread_create(&wpid_thread, &pthread_create_attr, wpid_handler, NULL));
}

#endif

void add_context_event_listener(ContextEventListener * listener, void * client_data) {
    listener->client_data = client_data;
    listener->next = event_listeners;
    event_listeners = listener;
}

void ini_contexts(void) {
    int i;

    list_init(&context_root);
    for (i = 0; i < CONTEXT_PID_ROOT_SIZE; i++) {
        list_init(&context_pid_root[i]);
    }
    init();
}
