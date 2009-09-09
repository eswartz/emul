/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
 * This module handles process/thread OS contexts and their state machine.
 */

#include "config.h"

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
#include "waitpid.h"

static ContextEventListener * event_listeners = NULL;

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

char * thread_id(Context * ctx) {
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

void add_context_event_listener(ContextEventListener * listener, void * client_data) {
    listener->client_data = client_data;
    listener->next = event_listeners;
    event_listeners = listener;
}

#ifdef WIN32

typedef struct ExceptionName {
    DWORD code;
    char * name;
    char * desc;
} ExceptionName;

static ExceptionName exception_names[] = {
    { 0x40010005, NULL, "Control-C" },
    { 0x40010008, NULL, "Control-Break" },
    { EXCEPTION_DATATYPE_MISALIGNMENT, "EXCEPTION_DATATYPE_MISALIGNMENT", "Datatype Misalignment" },
    { EXCEPTION_ACCESS_VIOLATION, "EXCEPTION_ACCESS_VIOLATION", "Access Violation" },
    { EXCEPTION_IN_PAGE_ERROR, "EXCEPTION_IN_PAGE_ERROR", "In Page Error" },
    { EXCEPTION_ILLEGAL_INSTRUCTION, "EXCEPTION_ILLEGAL_INSTRUCTION", "Illegal Instruction" },
    { EXCEPTION_ARRAY_BOUNDS_EXCEEDED, "EXCEPTION_ARRAY_BOUNDS_EXCEEDED", "Array Bounds Exceeded" },
    { EXCEPTION_FLT_DENORMAL_OPERAND, "EXCEPTION_FLT_DENORMAL_OPERAND", "Float Denormal Operand" },
    { EXCEPTION_FLT_DIVIDE_BY_ZERO, "EXCEPTION_FLT_DIVIDE_BY_ZERO", "Float Divide by Zero" },
    { EXCEPTION_FLT_INEXACT_RESULT, "EXCEPTION_FLT_INEXACT_RESULT", "Float Inexact Result" },
    { EXCEPTION_FLT_INVALID_OPERATION, "EXCEPTION_FLT_INVALID_OPERATION", "Float Invalid Operation" },
    { EXCEPTION_FLT_OVERFLOW, "EXCEPTION_FLT_OVERFLOW", "Float Overflow" },
    { EXCEPTION_FLT_STACK_CHECK, "EXCEPTION_FLT_STACK_CHECK", "Float Stack Check" },
    { EXCEPTION_FLT_UNDERFLOW, "EXCEPTION_FLT_UNDERFLOW", "Float Underflow" },
    { EXCEPTION_NONCONTINUABLE_EXCEPTION, "EXCEPTION_NONCONTINUABLE_EXCEPTION", "Noncontinuable Exception" },
    { EXCEPTION_INVALID_DISPOSITION, "EXCEPTION_INVALID_DISPOSITION", "Invalid Disposition" },
    { EXCEPTION_INT_DIVIDE_BY_ZERO, "EXCEPTION_INT_DIVIDE_BY_ZERO", "Integer Divide by Zero" },
    { EXCEPTION_INT_OVERFLOW, "EXCEPTION_INT_OVERFLOW", "Integer Overflow" },
    { EXCEPTION_PRIV_INSTRUCTION, "EXCEPTION_PRIV_INSTRUCTION", "Privileged Instruction" },
    { EXCEPTION_STACK_OVERFLOW, "EXCEPTION_STACK_OVERFLOW", "Stack Overflow" },
    { EXCEPTION_GUARD_PAGE, "EXCEPTION_GUARD_PAGE", "Guard Page" },
    { 0xC0000194, "EXCEPTION_POSSIBLE_DEADLOCK", "Possible Deadlock" },
    { EXCEPTION_INVALID_HANDLE, "EXCEPTION_INVALID_HANDLE", "Invalid Handle" },
    { 0xc0000017, NULL, "No Memory" },
    { 0xc0000135, NULL, "DLL Not Found" },
    { 0xc0000142, NULL, "DLL Initialization Failed" },
    { 0xc06d007e, NULL, "Module Not Found" },
    { 0xc06d007f, NULL, "Procedure Not Found" },
    { 0xe06d7363, NULL, "Microsoft C++ Exception" },
};

#define EXCEPTION_NAMES_CNT (sizeof(exception_names) / sizeof(ExceptionName))

char * signal_name(int signal) {
    int n = signal - 1;
    if (n >= 0 && n < EXCEPTION_NAMES_CNT) return exception_names[n].name;
    return NULL;
}

char * signal_description(int signal) {
    int n = signal - 1;
    if (n >= 0 && n < EXCEPTION_NAMES_CNT) return exception_names[n].desc;
    return NULL;
}

unsigned signal_code(int signal) {
    int n = signal - 1;
    if (n >= 0 && n < EXCEPTION_NAMES_CNT) return exception_names[n].code;
    return 0;
}

#else

#define CASE(var) case var: return ""#var;
char * signal_name(int signal) {
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
    return NULL;
}
#undef CASE

char * signal_description(int signal) {
    return NULL;
}

unsigned signal_code(int signal) {
    return signal;
}

#endif

#if ENABLE_DebugContext

#define CONTEXT_PID_ROOT_SIZE 1024
#define CONTEXT_PID_HASH(PID) ((unsigned)(PID) % CONTEXT_PID_ROOT_SIZE)
static LINK context_pid_root[CONTEXT_PID_ROOT_SIZE];

LINK context_root = { NULL, NULL };

Context * context_find_from_pid(pid_t pid) {
    LINK * qhp = &context_pid_root[CONTEXT_PID_HASH(pid)];
    LINK * qp = qhp->next;

    assert(is_dispatch_thread());
    if (qp == NULL) return NULL;
    while (qp != qhp) {
        Context * ctx = pidl2ctxp(qp);
        if (ctx->pid == pid && !ctx->exited) return ctx;
        qp = qp->next;
    }
    return NULL;
}

void context_lock(Context * ctx) {
    assert(ctx->ref_count > 0);
    ctx->ref_count++;
}

void context_unlock(Context * ctx) {
    assert(ctx->ref_count > 0);
    if (--(ctx->ref_count) == 0) {
        assert(list_is_empty(&ctx->children));
        assert(ctx->parent == NULL);
        list_remove(&ctx->ctxl);
        list_remove(&ctx->pidl);
        loc_free(ctx->bp_ids);
        loc_free(ctx);
    }
}

static void link_context(Context * ctx) {
    LINK * qhp = &context_pid_root[CONTEXT_PID_HASH(ctx->pid)];

    assert(context_find_from_pid(ctx->pid) == NULL);
    list_remove(&ctx->ctxl);
    list_remove(&ctx->pidl);
    list_add_first(&ctx->ctxl, &context_root);
    list_add_first(&ctx->pidl, qhp);
    ctx->ref_count++;
}

static Context * create_context(pid_t pid) {
    Context * ctx = (Context *)loc_alloc_zero(sizeof(Context));

    ctx->pid = pid;
    list_init(&ctx->children);
    list_init(&ctx->ctxl);
    list_init(&ctx->pidl);
    list_init(&ctx->cldl);
    return ctx;
}

char * context_state_name(Context * ctx) {
    if (ctx->exited) return "exited";
    if (ctx->intercepted) return "intercepted";
    if (ctx->stopped) return "stopped";
    return "running";
}

static void event_context_created(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    assert(ctx->ref_count > 0);
    while (listener != NULL) {
        if (listener->context_created != NULL) {
            listener->context_created(ctx, listener->client_data);
        }
        listener = listener->next;
    }
}

static void event_context_changed(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    assert(ctx->ref_count > 0);
    while (listener != NULL) {
        if (listener->context_changed != NULL) {
            listener->context_changed(ctx, listener->client_data);
        }
        listener = listener->next;
    }
}

static void event_context_stopped(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    assert(ctx->ref_count > 0);
    if (ctx->stopped_by_bp) {
        evaluate_breakpoint_condition(ctx);
    }
    while (listener != NULL) {
        if (listener->context_stopped != NULL) {
            listener->context_stopped(ctx, listener->client_data);
        }
        listener = listener->next;
    }
}

static void event_context_started(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    assert(ctx->ref_count > 0);
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

typedef struct DebugThreadArgs {
    int error;
    DWORD context_id;
    DWORD debug_thread_id;
    HANDLE debug_thread;
    HANDLE debug_thread_semaphore;
    ContextAttachCallBack * attach_callback;
    void * attach_data;
} DebugThreadArgs;

typedef struct DebugEvent {
    DebugThreadArgs * debug_thread_args;
    HANDLE event_semaphore;
    DEBUG_EVENT event;
    DWORD continue_status;
    int early_event;  /* Event received before debugger is fully attached */
    struct DebugEvent * next;
} DebugEvent;

#define EXCEPTION_DEBUGGER_IO 0x406D1388

char * context_suspend_reason(Context * ctx) {
    DWORD exception_code = ctx->suspend_reason.ExceptionRecord.ExceptionCode;
    static char buf[64];
    int n = 0;

    if (ctx->stopped_by_bp) return "Breakpoint";
    if (exception_code == 0) return "Suspended";
    if (ctx->debug_started && exception_code == EXCEPTION_BREAKPOINT) return "Suspended";
    if (exception_code == EXCEPTION_SINGLE_STEP) return "Step";

    while (n < EXCEPTION_NAMES_CNT) {
        if (exception_names[n].code == exception_code) return exception_names[n].desc;
        n++;
    }

    snprintf(buf, sizeof(buf), "Exception %#lx", exception_code);
    return buf;
}

static int get_signal_index(Context * ctx) {
    DWORD exception_code = ctx->suspend_reason.ExceptionRecord.ExceptionCode;
    int n = 0;

    if (exception_code == 0) return 0;
    if (ctx->debug_started && exception_code == EXCEPTION_BREAKPOINT) return 0;

    while (n < EXCEPTION_NAMES_CNT) {
        if (exception_names[n].code == exception_code) return n;
        n++;
    }
    return 0;
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

static int log_error(char * fn, int ok) {
    int err;
    if (ok) return 0;
    err = set_win32_errno(GetLastError());
    trace(LOG_ALWAYS, "context: %s: %s", fn, errno_to_str(errno));
    return err;
}

static void event_win32_context_exited(Context * ctx);

static void event_win32_context_stopped(Context * ctx) {
    DWORD exception_code = ctx->pending_event.ExceptionRecord.ExceptionCode;

    if (ctx->exited || ctx->stopped && exception_code == 0) return;
    memcpy(&ctx->suspend_reason, &ctx->pending_event, sizeof(EXCEPTION_DEBUG_INFO));
    memset(&ctx->pending_event, 0, sizeof(EXCEPTION_DEBUG_INFO));

    trace(LOG_CONTEXT, "context: stopped: ctx %#lx, pid %d, exception %#lx",
        ctx, ctx->pid, exception_code);
    assert(is_dispatch_thread());
    assert(!ctx->stopped);
    assert(ctx->handle != NULL);
    assert(ctx->parent != NULL);

    if (SuspendThread(ctx->handle) == (DWORD)-1) {
        DWORD err = GetLastError();
        if (err == ERROR_ACCESS_DENIED && exception_code == 0) {
            /* Already exited */
            event_win32_context_exited(ctx);
            return;
        }
        log_error("SuspendThread", 0);
        return;
    }

    ctx->regs_error = 0;
    memset(&ctx->regs, 0, sizeof(ctx->regs));
    ctx->regs.ContextFlags = CONTEXT_CONTROL | CONTEXT_INTEGER;
    if (GetThreadContext(ctx->handle, &ctx->regs) == 0) {
        ctx->regs_error = log_error("GetThreadContext", 0);
    }
    else {
        trace(LOG_CONTEXT, "context: get regs OK: ctx %#lx, pid %d, PC %#lx",
            ctx, ctx->pid, get_regs_PC(ctx->regs));
    }

    ctx->signal = get_signal_index(ctx);
    ctx->pending_signals = 0;
    ctx->stopped = 1;
    ctx->stopped_by_bp = 0;
    switch (exception_code) {
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
        else {
            ctx->pending_intercept = 1;
        }
        break;
    case EXCEPTION_DEBUGGER_IO:
        trace(LOG_ALWAYS, "Debugger IO request %#lx",
            ctx->suspend_reason.ExceptionRecord.ExceptionInformation[0]);
        break;
    default:
        ctx->pending_signals |= 1 << ctx->signal;
        if (ctx->signal != 0 && (ctx->sig_dont_stop & (1 << ctx->signal)) != 0) break;
        ctx->pending_intercept = 1;
        break;
    }
    event_context_stopped(ctx);
}

static void event_win32_context_stopped_async(void * arg) {
    Context * ctx = (Context *)arg;
    ctx->context_stopped_async_pending = 0;
    event_win32_context_stopped(ctx);
    context_unlock(ctx);
}

static void event_win32_context_started(Context * ctx) {
    DWORD exception_code = ctx->suspend_reason.ExceptionRecord.ExceptionCode;
    trace(LOG_CONTEXT, "context: started: ctx %#lx, pid %d", ctx, ctx->pid);
    assert(ctx->stopped);
    ctx->stopped = 0;
    if (ctx->debug_started && exception_code == EXCEPTION_BREAKPOINT) ctx->debug_started = 0;
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
        if (!c->exited) event_win32_context_exited(c);
    }
    ctx->exiting = 0;
    ctx->exited = 1;
    event_context_exited(ctx);
    if (ctx->handle != NULL) {
        if (ctx->parent == NULL) {
            log_error("CloseHandle", CloseHandle(ctx->handle));
        }
        ctx->handle = NULL;
    }
    if (ctx->file_handle != NULL) {
        log_error("CloseHandle", CloseHandle(ctx->file_handle));
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
        errno = log_error("SetThreadContext", 0);
        return -1;
    }
    ctx->regs_dirty = 0;
    if (ctx->pending_event.ExceptionRecord.ExceptionCode != 0) {
        event_win32_context_started(ctx);
        context_lock(ctx);
        post_event(event_win32_context_stopped_async, ctx);
        return 0;
    }
    if (ctx->parent->pending_signals & (1 << SIGKILL)) {
        if (!ctx->parent->exiting && !TerminateProcess(ctx->parent->handle, 1)) {
            errno = log_error("TerminateProcess", 0);
            return -1;
        }
        ctx->parent->pending_signals &= ~(1 << SIGKILL);
        ctx->parent->exiting = 1;
    }
    for (;;) {
        DWORD cnt = ResumeThread(ctx->handle);
        if (cnt == (DWORD)-1) {
            errno = log_error("ResumeThread", 0);
            return -1;
        }
        if (cnt <= 1) break;
    }
    event_win32_context_started(ctx);
    return 0;
}

static void debug_event_handler(void * x) {
    DebugEvent * args = (DebugEvent *)x;
    HANDLE event_semaphore = args->event_semaphore;

    while (args != NULL) {

        DEBUG_EVENT * debug_event = &args->event;
        Context * prs = context_find_from_pid(debug_event->dwProcessId);
        Context * ctx = context_find_from_pid(debug_event->dwThreadId);

        assert(ctx == NULL || ctx->parent == prs);

        switch (debug_event->dwDebugEventCode) {
        case CREATE_PROCESS_DEBUG_EVENT:
            assert(prs == NULL);
            assert(ctx == NULL);
            prs = create_context(debug_event->dwProcessId);
            prs->mem = debug_event->dwProcessId;
            prs->handle = debug_event->u.CreateProcessInfo.hProcess;
            prs->file_handle = debug_event->u.CreateProcessInfo.hFile;
            prs->base_address = (unsigned)debug_event->u.CreateProcessInfo.lpBaseOfImage;
            assert(prs->handle != NULL);
            link_context(prs);
            event_context_created(prs);
            args->debug_thread_args->attach_callback(0, prs, args->debug_thread_args->attach_data);
            args->debug_thread_args->attach_callback = NULL;
            args->debug_thread_args->attach_data = NULL;
            ctx = create_context(debug_event->dwThreadId);
            ctx->mem = debug_event->dwProcessId;
            ctx->handle = debug_event->u.CreateProcessInfo.hThread;
            ctx->debug_started = 1;
            ctx->parent = prs;
            prs->ref_count++;
            list_add_first(&ctx->cldl, &prs->children);
            link_context(ctx);
            event_context_created(ctx);
            break;
        case CREATE_THREAD_DEBUG_EVENT:
            assert(prs != NULL);
            assert(ctx == NULL);
            ctx = create_context(debug_event->dwThreadId);
            ctx->mem = debug_event->dwProcessId;
            ctx->handle = debug_event->u.CreateThread.hThread;
            ctx->parent = prs;
            prs->ref_count++;
            list_add_first(&ctx->cldl, &prs->children);
            link_context(ctx);
            event_context_created(ctx);
            event_win32_context_stopped(ctx);
            break;
        case EXCEPTION_DEBUG_EVENT:
            assert(prs != NULL);
            if (ctx == NULL) break;
            if (args->early_event) break; /* Can anything be done about such exceptions? */
            assert(ctx->pending_event.ExceptionRecord.ExceptionCode == 0);
            switch (args->event.u.Exception.ExceptionRecord.ExceptionCode) {
            case EXCEPTION_SINGLE_STEP:
            case EXCEPTION_BREAKPOINT:
            case EXCEPTION_DEBUGGER_IO:
                args->continue_status = DBG_CONTINUE;
                break;
            default:
                args->continue_status = DBG_EXCEPTION_NOT_HANDLED;
                break;
            }
            memcpy(&ctx->pending_event, &args->event.u.Exception, sizeof(EXCEPTION_DEBUG_INFO));
            if (!ctx->stopped) {
                int signal = 0;
                if (ctx->context_stopped_async_pending) {
                    cancel_event(event_win32_context_stopped_async, ctx, 0);
                    ctx->context_stopped_async_pending = 0;
                }
                else {
                    context_lock(ctx);
                }
                event_win32_context_stopped(ctx);
                signal = get_signal_index(ctx);
                if (signal != 0 && (ctx->sig_dont_pass & (1 << signal)) != 0) {
                    args->continue_status = DBG_CONTINUE;
                }
                context_unlock(ctx);
            }
            break;
        case EXIT_THREAD_DEBUG_EVENT:
            assert(prs != NULL);
            if (ctx && !ctx->exited) event_win32_context_exited(ctx);
            break;
        case EXIT_PROCESS_DEBUG_EVENT:
            assert(prs != NULL);
            if (ctx && !ctx->exited) event_win32_context_exited(ctx);
            event_win32_context_exited(prs);
            break;
        case LOAD_DLL_DEBUG_EVENT:
            assert(prs != NULL);
            prs->module_loaded = 1;
            prs->module_handle = args->event.u.LoadDll.hFile;
            prs->module_address = (unsigned)args->event.u.LoadDll.lpBaseOfDll;
            event_context_changed(prs);
            if (prs->module_handle != NULL) {
                log_error("CloseHandle", CloseHandle(prs->module_handle));
            }
            prs->module_handle = NULL;
            prs->module_address = 0;
            prs->module_loaded = 0;
            break;
        case UNLOAD_DLL_DEBUG_EVENT:
            assert(prs != NULL);
            prs->module_unloaded = 1;
            prs->module_address = (unsigned)args->event.u.UnloadDll.lpBaseOfDll;
            event_context_changed(prs);
            prs->module_address = 0;
            prs->module_unloaded = 0;
            break;
        case RIP_EVENT:
            trace(LOG_ALWAYS, "System debugging error: debuggee pid %d, error type %d, error code %d",
                debug_event->dwProcessId, debug_event->u.RipInfo.dwType, debug_event->u.RipInfo.dwError);
            break;
        }
        args = args->next;
    }

    log_error("ReleaseSemaphore", ReleaseSemaphore(event_semaphore, 1, 0));
}

static void debugger_exit_handler(void * x) {
    DebugThreadArgs * args = (DebugThreadArgs *)x;
    Context * prs = context_find_from_pid(args->context_id);

    trace(LOG_WAITPID, "debugger thread %d exited, debuggee pid %d",
        args->debug_thread_id, args->context_id);

    log_error("WaitForSingleObject", WaitForSingleObject(args->debug_thread, INFINITE) != WAIT_FAILED);
    log_error("CloseHandle", CloseHandle(args->debug_thread));
    log_error("CloseHandle", CloseHandle(args->debug_thread_semaphore));

    if (prs != NULL && !prs->exited) event_win32_context_exited(prs);

    loc_free(args);
}

static DWORD WINAPI debugger_thread_func(LPVOID x) {
    DebugThreadArgs * args = (DebugThreadArgs *)x;
    HANDLE event_semaphore = CreateSemaphore(NULL, 0, 1, NULL);
    DebugEvent event_buffer;
    DebugEvent create_process;
    DebugEvent fantom_process;
    int state = 0;

    if (event_semaphore == NULL) {
        args->error = GetLastError();
        trace(LOG_ALWAYS, "Can't create semaphore: error %d", args->error);
        ReleaseSemaphore(args->debug_thread_semaphore, 1, 0);
        return 0;
    }

    if (DebugActiveProcess(args->context_id) == 0) {
        args->error = GetLastError();
        trace(LOG_ALWAYS, "Can't attach to a process: error %d", args->error);
        ReleaseSemaphore(args->debug_thread_semaphore, 1, 0);
        CloseHandle(event_semaphore);
        return 0;
    }

    trace(LOG_WAITPID, "debugger thread %d started", GetCurrentThreadId());

    memset(&event_buffer, 0, sizeof(event_buffer));
    memset(&create_process, 0, sizeof(create_process));
    memset(&fantom_process, 0, sizeof(fantom_process));

    event_buffer.debug_thread_args = args;
    event_buffer.event_semaphore = event_semaphore;
    create_process.debug_thread_args = args;
    create_process.event_semaphore = event_semaphore;
    fantom_process.debug_thread_args = args;
    fantom_process.event_semaphore = event_semaphore;

    for (;;) {
        DEBUG_EVENT * debug_event = &event_buffer.event;

        memset(debug_event, 0, sizeof(DEBUG_EVENT));
        if (WaitForDebugEvent(debug_event, INFINITE) == 0) {
            trace(LOG_ALWAYS, "WaitForDebugEvent() error %d", GetLastError());
            break;
        }
        if (debug_event->dwDebugEventCode == EXCEPTION_DEBUG_EVENT) {
            trace(LOG_WAITPID, "%s, process %d, thread %d, code %#lx",
                win32_debug_event_name(debug_event->dwDebugEventCode),
                debug_event->dwProcessId, debug_event->dwThreadId,
                debug_event->u.Exception.ExceptionRecord.ExceptionCode);
        }
        else {
            trace(LOG_WAITPID, "%s, process %d, thread %d",
                win32_debug_event_name(debug_event->dwDebugEventCode),
                debug_event->dwProcessId, debug_event->dwThreadId);
        }
        assert(args->context_id == debug_event->dwProcessId);
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
                /* 2. if we don't suspend second process, debugee crashes. */
                assert(fantom_process.event.u.CreateProcessInfo.hThread == NULL);
                memcpy(&fantom_process, &event_buffer, sizeof(event_buffer));
                SuspendThread(fantom_process.event.u.CreateProcessInfo.hThread);
                CloseHandle(fantom_process.event.u.CreateProcessInfo.hFile);
                fantom_process.event.u.CreateProcessInfo.hFile = NULL;
                ResumeThread(create_process.event.u.CreateProcessInfo.hThread);
            }
            break;
        default:
            if (fantom_process.event.dwThreadId == debug_event->dwThreadId) {
                if (debug_event->dwDebugEventCode == EXIT_THREAD_DEBUG_EVENT) {
                    memset(&fantom_process, 0, sizeof(fantom_process));
                }
                else if (debug_event->dwDebugEventCode == EXCEPTION_DEBUG_EVENT) {
                    event_buffer.continue_status = DBG_EXCEPTION_NOT_HANDLED;
                }
                else if (debug_event->dwDebugEventCode == LOAD_DLL_DEBUG_EVENT) {
                    if (debug_event->u.LoadDll.hFile != NULL) CloseHandle(debug_event->u.LoadDll.hFile);
                }
                break;
            }
            if (state == 0) {
                if (debug_event->dwDebugEventCode == EXCEPTION_DEBUG_EVENT) {
                    event_buffer.continue_status = DBG_EXCEPTION_NOT_HANDLED;
                }
                break;
            }
            if (state == 1 && debug_event->dwDebugEventCode == EXCEPTION_DEBUG_EVENT &&
                    debug_event->u.Exception.ExceptionRecord.ExceptionCode == EXCEPTION_BREAKPOINT) {
                post_event(debug_event_handler, &create_process);
                ReleaseSemaphore(args->debug_thread_semaphore, 1, 0);
                WaitForSingleObject(event_semaphore, INFINITE);
                while (create_process.next != NULL) {
                    DebugEvent * e = create_process.next;
                    create_process.next = e->next;
                    loc_free(e);
                }
                state++;
                if (fantom_process.event.u.CreateProcessInfo.hThread != NULL) {
                    ResumeThread(fantom_process.event.u.CreateProcessInfo.hThread);
                }
            }
            if (state == 2) {
                post_event(debug_event_handler, &event_buffer);
                WaitForSingleObject(event_semaphore, INFINITE);
            }
            else {
                /* Delay posting event to foreground thread until debugger is fully attached */
                DebugEvent * e = (DebugEvent *)loc_alloc(sizeof(DebugEvent));
                DebugEvent ** p = &create_process.next;
                while (*p != NULL) p = &(*p)->next;
                memcpy(e, &event_buffer, sizeof(DebugEvent));
                e->early_event = 1;
                *p = e;
            }
            break;
        }

        if (ContinueDebugEvent(debug_event->dwProcessId, debug_event->dwThreadId, event_buffer.continue_status) == 0) {
            trace(LOG_ALWAYS, "Can't continue debug event: process %d, thread %d, error %d",
                debug_event->dwProcessId, debug_event->dwThreadId, GetLastError());
            break;
        }

        if (debug_event->dwDebugEventCode == EXIT_PROCESS_DEBUG_EVENT) break;
        if (debug_event->dwDebugEventCode == RIP_EVENT) break;
    }

    if (state < 2) ReleaseSemaphore(args->debug_thread_semaphore, 1, 0);

    CloseHandle(event_semaphore);
    post_event(debugger_exit_handler, args);
    return 0;
}

int context_attach(pid_t pid, ContextAttachCallBack * done, void * data, int selfattach) {
    DebugThreadArgs * args = (DebugThreadArgs *)loc_alloc_zero(sizeof(DebugThreadArgs));

    assert(done != NULL);
    assert(!selfattach);
    args->context_id = pid;
    args->attach_callback = done;
    args->attach_data = data;

    args->debug_thread_semaphore = CreateSemaphore(NULL, 0, 1, NULL);
    if (args->debug_thread_semaphore == NULL) {
        int err = log_error("CreateSemaphore", 0);
        loc_free(args);
        errno = err;
        return -1;
    }

    args->debug_thread = CreateThread(NULL, 0, debugger_thread_func, args, 0, &args->debug_thread_id);
    if (args->debug_thread == NULL) {
        int err = log_error("CreateThread", 0);
        log_error("CloseHandle", CloseHandle(args->debug_thread_semaphore));
        loc_free(args);
        errno = err;
        return -1;
    }


    log_error("WaitForSingleObject", WaitForSingleObject(args->debug_thread_semaphore, INFINITE) != WAIT_FAILED);

    if (args->error) {
        log_error("WaitForSingleObject", WaitForSingleObject(args->debug_thread, INFINITE) != WAIT_FAILED);
        log_error("CloseHandle", CloseHandle(args->debug_thread));
        log_error("CloseHandle", CloseHandle(args->debug_thread_semaphore));
        loc_free(args);
        set_win32_errno(args->error);
        return -1;
    }

    add_waitpid_process(pid);
    return 0;
}

int context_has_state(Context * ctx) {
    return ctx != NULL && ctx->pid != ctx->mem;
}

int context_stop(Context * ctx) {
    trace(LOG_CONTEXT, "context:%s suspending ctx %#lx pid %d",
        ctx->pending_intercept ? "" : " temporary", ctx, ctx->pid);
    assert(context_has_state(ctx));
    assert(!ctx->stopped);
    assert(!ctx->exited);
    if (SuspendThread(ctx->handle) == (DWORD)-1) {
        if (GetLastError() != ERROR_ACCESS_DENIED) {
            errno = log_error("SuspendThread", 0);
            return -1;
        }
    }
    if (!ctx->context_stopped_async_pending) {
        context_lock(ctx);
        post_event(event_win32_context_stopped_async, ctx);
        ctx->context_stopped_async_pending = 1;
    }
    return 0;
}

int context_continue(Context * ctx) {
    assert(is_dispatch_thread());
    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(!ctx->intercepted);
    assert(!ctx->exited);

    if (skip_breakpoint(ctx, 0)) return 0;

    trace(LOG_CONTEXT, "context: resuming ctx %#lx, pid %d", ctx, ctx->pid);
#if defined(__i386__) || defined(__x86_64__)
    if (!ctx->pending_step && (ctx->regs.EFlags & 0x100) != 0) {
        ctx->regs.EFlags &= ~0x100;
        ctx->regs_dirty = 1;
    }
#endif
    if (ctx->regs_dirty && ctx->regs_error) {
        trace(LOG_ALWAYS, "Can't resume thread, registers copy is invalid: ctx %#lx, pid %d, error %d",
            ctx, ctx->pid, ctx->regs_error);
        errno = ctx->regs_error;
        return -1;
    }
    return win32_resume(ctx);
}

int context_single_step(Context * ctx) {
    assert(is_dispatch_thread());
    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(!ctx->exited);

    if (skip_breakpoint(ctx, 1)) return 0;

    trace(LOG_CONTEXT, "context: single step ctx %#lx, pid %d", ctx, ctx->pid);
    if (ctx->regs_error) {
        trace(LOG_ALWAYS, "Can't resume thread, registers copy is invalid: ctx %#lx, pid %d, error %d",
            ctx, ctx->pid, ctx->regs_error);
        errno = ctx->regs_error;
        return -1;
    }
#if defined(__i386__) || defined(__x86_64__)
    ctx->regs.EFlags |= 0x100;
    ctx->regs_dirty = 1;
#else
#   error "context_single_step() is not implemented for CPU other then X86"
#endif
    ctx->pending_step = 1;
    return win32_resume(ctx);
}

int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    SIZE_T bcnt = 0;
    trace(LOG_CONTEXT, "context: read memory ctx %#lx, pid %d, address %#lx, size %zd",
        ctx, ctx->pid, address, size);
    assert(is_dispatch_thread());
    if (ctx->parent != NULL) ctx = ctx->parent;
    assert(ctx->pid == ctx->mem);
    if (ReadProcessMemory(ctx->handle, (LPCVOID)address, buf, size, &bcnt) == 0 || bcnt != size) {
        errno = log_error("ReadProcessMemory", 0);
        return -1;
    }
    return 0;
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    SIZE_T bcnt = 0;
    trace(LOG_CONTEXT, "context: write memory ctx %#lx, pid %d, address %#lx, size %zd",
        ctx, ctx->pid, address, size);
    assert(is_dispatch_thread());
    if (ctx->parent != NULL) ctx = ctx->parent;
    assert(ctx->pid == ctx->mem);
    if (WriteProcessMemory(ctx->handle, (LPVOID)address, buf, size, &bcnt) == 0 || bcnt != size) {
        DWORD err = GetLastError();
        if (err == ERROR_ACCESS_DENIED) errno = set_win32_errno(err);
        else errno = log_error("WriteProcessMemory", 0);
        return -1;
    }
    if (FlushInstructionCache(ctx->handle, (LPCVOID)address, size) == 0) {
        errno = log_error("FlushInstructionCache", 0);
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

struct event_info {
    int                 event;
    VXDBG_CTX           current_ctx;    /* context that hit breakpoint */
    VXDBG_CTX           stopped_ctx;    /* context stopped by the breakpoint */
    REG_SET             regs;           /* task registers before exception */
    UINT32              addr;           /* breakpoint addr */
    int                 bp_info_ok;     /* breakpoint information available */
    VXDBG_BP_INFO       bp_info;        /* breakpoint information */
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

char * context_suspend_reason(Context * ctx) {
    if (ctx->stopped_by_bp) return "Breakpoint";
    if (ctx->event == TRACE_EVENT_STEP) return "Step";
    return "Suspended";
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

typedef struct AttachDoneArgs {
    pid_t pid;
    ContextAttachCallBack * done;
    void * data;
} AttachDoneArgs;

static void event_attach_done(void * x) {
    AttachDoneArgs * args = (AttachDoneArgs *)x;
    Context * ctx = create_context(args->pid);

    ctx->mem = taskIdSelf();
    link_context(ctx);
    event_context_created(ctx);
    args->done(0, ctx, args->data);
    if (taskIsStopped(args->pid)) {
        struct event_info * info;
        ctx->pending_intercept = 1;
        info = event_info_alloc(EVENT_HOOK_STOP);
        if (info != NULL) {
            info->stopped_ctx.ctxId = args->pid;
            event_info_post(info);
        }
    }
    loc_free(x);
}

int context_attach(pid_t pid, ContextAttachCallBack * done, void * data, int selfattach) {
    AttachDoneArgs * args = (AttachDoneArgs *)loc_alloc(sizeof(AttachDoneArgs));

    assert(done != NULL);
    assert(!selfattach);
    args->pid = pid;
    args->done = done;
    args->data = data;
    post_event(event_attach_done, args);

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
        trace(LOG_CONTEXT, "context: stop ctx %#lx, id %#x", ctx, ctx->pid);
    }
    else {
        trace(LOG_CONTEXT, "context: temporary stop ctx %#lx, id %#x", ctx, ctx->pid);
    }

    taskLock();
    if (taskIsStopped(ctx->pid)) {
        taskUnlock();
        return 0;
    }
    vxdbg_ctx.ctxId = ctx->pid;
    vxdbg_ctx.ctxType = VXDBG_CTX_TASK;
    if (vxdbgStop(vxdbg_clnt_id, &vxdbg_ctx) != OK) {
        int error = errno;
        taskUnlock();
        if (error == S_vxdbgLib_INVALID_CTX) return 0;
        trace(LOG_ALWAYS, "context: can't stop ctx %#lx, id %#x: %s",
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
        trace(LOG_ALWAYS, "context: can't kill ctx %#lx, id %#x: %s",
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

    if (skip_breakpoint(ctx, 0)) return 0;

    trace(LOG_CONTEXT, "context: continue ctx %#lx, id %#x", ctx, ctx->pid);

    if (ctx->regs_dirty) {
        if (taskRegsSet(ctx->pid, &ctx->regs) != OK) {
            int error = errno;
            trace(LOG_ALWAYS, "context: can't set regs ctx %#lx, id %#x: %s",
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
        trace(LOG_ALWAYS, "context: can't continue ctx %#lx, id %#x: %s",
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
    assert(!ctx->pending_step);
    assert(!ctx->exited);

    if (skip_breakpoint(ctx, 1)) return 0;

    trace(LOG_CONTEXT, "context: single step ctx %#lx, id %#x", ctx, ctx->pid);

    if (ctx->regs_dirty) {
        if (taskRegsSet(ctx->pid, &ctx->regs) != OK) {
            int error = errno;
            trace(LOG_ALWAYS, "context: can't set regs ctx %#lx, id %#x: %s",
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
        trace(LOG_ALWAYS, "context: can't step ctx %#lx, id %#x: %d",
                ctx, ctx->pid, errno_to_str(error));
        return -1;
    }
    ctx->pending_step = 1;
    ctx->stopped = 0;
    taskUnlock();
    event_context_started(ctx);
    return 0;
}

int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
#ifdef _WRS_PERSISTENT_SW_BP
    vxdbgMemRead((void *)address, buf, size);
#else
    bcopy((void *)address, buf, size);
#endif
    return 0;
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
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
        current_ctx->stopped_by_bp = 0;
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
        stopped_ctx->stopped_by_bp = 0;
        assert(taskIsStopped(stopped_ctx->pid));
        event_context_stopped(stopped_ctx);
        break;
    case EVENT_HOOK_TASK_ADD:
        if (current_ctx == NULL) break;
        assert(stopped_ctx == NULL);
        stopped_ctx = create_context((pid_t)info->stopped_ctx.ctxId);
        stopped_ctx->mem = current_ctx->mem;
        stopped_ctx->parent = current_ctx->parent != NULL ? current_ctx->parent : current_ctx;
        stopped_ctx->parent->ref_count++;
        list_add_first(&stopped_ctx->cldl, &stopped_ctx->parent->children);
        link_context(stopped_ctx);
        event_context_created(stopped_ctx);
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

static void waitpid_listener(int pid, int exited, int exit_code, int signal, int event_code, int syscall, void * args) {
    if (exited) {
        Context * stopped_ctx = context_find_from_pid(pid);
        if (stopped_ctx != NULL) {
            /* TODO: need call back for vxdbgCont()
             * assert(!stopped_ctx->stopped) can fail if a task is resumed outside TCF agent.
             */
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
    }
}

static void init(void) {
    SPIN_LOCK_ISR_INIT(&events_lock, 0);
    if ((events_signal = semCInitialize(events_signal_mem, SEM_Q_FIFO, 0)) == NULL) {
        check_error(errno);
    }
    vxdbg_clnt_id = vxdbgClntRegister(EVT_BP);
    if (vxdbg_clnt_id == NULL) {
        check_error(errno);
    }
    taskCreateHookAdd((FUNCPTR)task_create_hook);
    vxdbgHookAdd(vxdbg_clnt_id, EVT_BP, vxdbg_event_hook);
    vxdbgHookAdd(vxdbg_clnt_id, EVT_TRACE, vxdbg_event_hook);
    check_error(pthread_create(&events_thread, &pthread_create_attr, event_thread_func, NULL));
    add_waitpid_listener(waitpid_listener, NULL);
}

#elif defined(__APPLE__)

#include <sys/syscall.h>
#include <sys/ptrace.h>
#include <sched.h>
#include <mach/thread_status.h>

#define WORD_SIZE   4

typedef struct PendingEvent {
    int event;
    int syscall;
    int signal;
    struct PendingEvent * next;
} PendingEvent;

static LINK pending_list;

char * context_suspend_reason(Context * ctx) {
    static char reason[128];

    if (ctx->stopped_by_bp) return "Breakpoint";
    if (ctx->end_of_step) return "Step";
    if (ctx->syscall_enter) return "System Call";
    if (ctx->syscall_exit) return "System Return";
    if (ctx->signal == SIGSTOP || ctx->signal == SIGTRAP) {
        return "Suspended";
    }
    if (signal_name(ctx->signal)) {
        snprintf(reason, sizeof(reason), "Signal %d %s", ctx->signal, signal_name(ctx->signal));
        return reason;
    }

    snprintf(reason, sizeof(reason), "Signal %d", ctx->signal);
    return reason;
}

int context_attach_self(void) {
    pid_t pid = getpid();

    if (ptrace(PT_TRACE_ME, 0, 0, 0) < 0) {
        int err = errno;
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_TRACEME) failed: pid %d, error %d %s",
              pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    return 0;
}

int context_attach(pid_t pid, ContextAttachCallBack * done, void * data, int selfattach) {
    Context * ctx = NULL;

    assert(done != NULL);
    if (!selfattach && ptrace(PT_ATTACH, pid, 0, 0) < 0) {
        int err = errno;
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_ATTACH) failed: pid %d, error %d %s",
            pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    ctx = create_context(pid);
    list_add_first(&ctx->ctxl, &pending_list);
    ctx->mem = pid;
    ctx->attach_callback = done;
    ctx->attach_data = data;
    ctx->pending_intercept = 1;
    /* TODO: context_attach works only for main task in a process */
    add_waitpid_process(pid);
    return 0;
}

int context_has_state(Context * ctx) {
    return 1;
}

int context_stop(Context * ctx) {
    trace(LOG_CONTEXT, "context:%s suspending ctx %#lx, pid %d",
        ctx->pending_intercept ? "" : " temporary", ctx, ctx->pid);
    assert(is_dispatch_thread());
    assert(!ctx->exited);
    assert(!ctx->stopped);
    assert(!ctx->regs_dirty);
    assert(!ctx->intercepted);
    if (kill(ctx->pid, SIGSTOP) < 0) {
        int err = errno;
        if (err != ESRCH) {
            trace(LOG_ALWAYS, "error: tkill(SIGSTOP) failed: ctx %#lx, pid %d, error %d %s",
                ctx, ctx->pid, err, errno_to_str(err));
        }
        errno = err;
        return -1;
    }
    return 0;
}

static int syscall_never_returns(Context * ctx) {
    if (ctx->syscall_enter) {
        switch (ctx->syscall_id) {
        case SYS_sigreturn:
            return 1;
        }
    }
    return 0;
}

int context_continue(Context * ctx) {
    int signal = 0;

    assert(is_dispatch_thread());
    assert(ctx->stopped);
    assert(!ctx->pending_intercept);
    assert(!ctx->pending_step);
    assert(!ctx->exited);

    if (skip_breakpoint(ctx, 0)) return 0;

    if (!ctx->syscall_enter) {
        while (ctx->pending_signals != 0) {
            while ((ctx->pending_signals & (1 << signal)) == 0) signal++;
            if (ctx->sig_dont_pass & (1 << signal)) {
                ctx->pending_signals &= ~(1 << signal);
                signal = 0;
            }
            else {
                break;
            }
        }
        assert(signal != SIGSTOP);
        assert(signal != SIGTRAP);
    }

    trace(LOG_CONTEXT, "context: resuming ctx %#lx, pid %d, with signal %d", ctx, ctx->pid, signal);
#if defined(__i386__) || defined(__x86_64__)
    if (ctx->regs.__eflags & 0x100) {
        ctx->regs.__eflags &= ~0x100;
        ctx->regs_dirty = 1;
    }
#endif
    if (ctx->regs_dirty) {
        unsigned int state_count;
        if (thread_set_state(ctx->pid, x86_THREAD_STATE32, &ctx->regs, &state_count) != KERN_SUCCESS) {
            int err = errno;
            trace(LOG_ALWAYS, "error: thread_set_state failed: ctx %#lx, pid %d, error %d %s",
                ctx, ctx->pid, err, errno_to_str(err));
            errno = err;
            return -1;
        }
        ctx->regs_dirty = 0;
    }
    if (ptrace(PT_CONTINUE, ctx->pid, 0, signal) < 0) {
        int err = errno;
#if USE_ESRCH_WORKAROUND
        if (err == ESRCH) {
            ctx->stopped = 0;
            event_context_started(ctx);
            return 0;
        }
#endif
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_CONT, ...) failed: ctx %#lx, pid %d, error %d %s",
            ctx, ctx->pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    ctx->pending_signals &= ~(1 << signal);
    if (syscall_never_returns(ctx)) {
        ctx->syscall_enter = 0;
        ctx->syscall_exit = 0;
        ctx->syscall_id = 0;
    }
    ctx->stopped = 0;
    event_context_started(ctx);
    return 0;
}

int context_single_step(Context * ctx) {
    assert(is_dispatch_thread());
    assert(ctx->stopped);
    assert(!ctx->pending_step);
    assert(!ctx->exited);

    if (skip_breakpoint(ctx, 1)) return 0;

    if (syscall_never_returns(ctx)) return context_continue(ctx);
    trace(LOG_CONTEXT, "context: single step ctx %#lx, pid %d", ctx, ctx->pid);
    if (ctx->regs_dirty) {
        unsigned int state_count;
        if (thread_set_state(ctx->pid, x86_THREAD_STATE32, &ctx->regs, &state_count) != KERN_SUCCESS) {
            int err = errno;
            trace(LOG_ALWAYS, "error: thread_set_state failed: ctx %#lx, pid %d, error %d %s",
                ctx, ctx->pid, err, errno_to_str(err));
            errno = err;
            return -1;
        }
        ctx->regs_dirty = 0;
    }
    if (ptrace(PT_STEP, ctx->pid, 0, 0) < 0) {
        int err = errno;
#if USE_ESRCH_WORKAROUND
        if (err == ESRCH) {
            ctx->stopped = 0;
            ctx->pending_step = 1;
            event_context_started(ctx);
            return 0;
        }
#endif
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_SINGLESTEP, ...) failed: ctx %#lx, pid %d, error %d %s",
            ctx, ctx->pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    ctx->pending_step = 1;
    ctx->stopped = 0;
    event_context_started(ctx);
    return 0;
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    /*
    ContextAddress word_addr;
    assert(is_dispatch_thread());
    assert(!ctx->exited);
    trace(LOG_CONTEXT, "context: write memory ctx %#lx, pid %d, address %#lx, size %zd",
        ctx, ctx->pid, address, size);
    assert(WORD_SIZE == sizeof(unsigned));
    for (word_addr = address & ~(WORD_SIZE - 1); word_addr < address + size; word_addr += WORD_SIZE) {
        unsigned word = 0;
        if (word_addr < address || word_addr + WORD_SIZE > address + size) {
            int i;
            errno = 0;
            word = ptrace(PT_PEEKDATA, ctx->pid, word_addr, 0);
            if (errno != 0) {
                int err = errno;
                trace(LOG_ALWAYS, "error: ptrace(PTRACE_PEEKDATA, ...) failed: ctx %#lx, pid %d, error %d %s",
                    ctx, ctx->pid, err, errno_to_str(err));
                errno = err;
                return -1;
            }
            for (i = 0; i < WORD_SIZE; i++) {
                if (word_addr + i >= address && word_addr + i < address + size) {
                    ((char *)&word)[i] = ((char *)buf)[word_addr + i - address];
                }
            }
        }
        else {
            word = *(unsigned *)((char *)buf + (word_addr - address));
        }
        if (ptrace(PT_POKEDATA, ctx->pid, word_addr, word) < 0) {
            int err = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_POKEDATA, ...) failed: ctx %#lx, pid %d, error %d %s",
                ctx, ctx->pid, err, errno_to_str(err));
            errno = err;
            return -1;
        }
    }
    */
    return 0;
}

int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    /*
    ContextAddress word_addr;
    assert(is_dispatch_thread());
    assert(!ctx->exited);
    trace(LOG_CONTEXT, "context: read memory ctx %#lx, pid %d, address %#lx, size %zd",
        ctx, ctx->pid, address, size);
    assert(WORD_SIZE == sizeof(unsigned));
    for (word_addr = address & ~(WORD_SIZE - 1); word_addr < address + size; word_addr += WORD_SIZE) {
        unsigned word = 0;
        errno = 0;
        word = ptrace(PT_PEEKDATA, ctx->pid, word_addr, 0);
        if (errno != 0) {
            int err = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_PEEKDATA, ...) failed: ctx %#lx, pid %d, error %d %s",
                ctx, ctx->pid, err, errno_to_str(err));
            errno = err;
            return -1;
        }
        if (word_addr < address || word_addr + WORD_SIZE > address + size) {
            int i;
            for (i = 0; i < WORD_SIZE; i++) {
                if (word_addr + i >= address && word_addr + i < address + size) {
                    ((char *)buf)[word_addr + i - address] = ((char *)&word)[i];
                }
            }
        }
        else {
            *(unsigned *)((char *)buf + (word_addr - address)) = word;
        }
    }
    */
    return 0;
}

static Context * find_pending(pid_t pid) {
    LINK * qp = pending_list.next;
    while (qp != &pending_list) {
        Context * c = ctxl2ctxp(qp);
        if (c->pid == pid) return c;
        qp = qp->next;
    }
    return NULL;
}

static void event_pid_exited(pid_t pid, int status, int signal) {
    Context * ctx;

    ctx = context_find_from_pid(pid);
    if (ctx == NULL) {
        ctx = find_pending(pid);
        if (ctx == NULL) {
            trace(LOG_EVENTS, "event: ctx not found, pid %d, exit status %d, term signal %d", pid, status, signal);
        }
        else {
            assert(ctx->ref_count == 0);
            if (ctx->attach_callback != NULL) {
                if (status == 0) status = EINVAL;
                ctx->attach_callback(status, ctx, ctx->attach_data);
                ctx->attach_callback = NULL;
                ctx->attach_data = NULL;
            }
            assert(list_is_empty(&ctx->children));
            assert(ctx->parent == NULL);
            list_remove(&ctx->ctxl);
            loc_free(ctx);
        }
    }
    else {
        assert(ctx->attach_callback == NULL);
        if (ctx->stopped || ctx->intercepted || ctx->exited) {
            trace(LOG_EVENTS, "event: ctx %#lx, pid %d, exit status %d unexpected, stopped %d, intercepted %d, exited %d",
                ctx, pid, status, ctx->stopped, ctx->intercepted, ctx->exited);
            if (ctx->stopped) {
                ctx->stopped = 0;
                event_context_started(ctx);
            }
        }
        else {
            trace(LOG_EVENTS, "event: ctx %#lx, pid %d, exit status %d, term signal %d", ctx, pid, status, signal);
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
         * between PTRACE_SYSCALL and SIGTRAP/PTRACE_EVENT_EXIT. So, ctx->exiting can be 0.
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
}

static void event_pid_stopped(pid_t pid, int signal, int event, int syscall) {
    unsigned long msg = 0;
    Context * ctx = NULL;
    Context * ctx2 = NULL;
    Context * pending_eap = NULL;

    trace(LOG_EVENTS, "event: pid %d stopped, signal %d", pid, signal);

    ctx = context_find_from_pid(pid);

    if (ctx == NULL) {
        ctx = find_pending(pid);
        if (ctx != NULL) {
            if (ctx->pending_events != NULL) {
                PendingEvent * e = loc_alloc_zero(sizeof(PendingEvent));
                PendingEvent ** p = (PendingEvent **)&ctx->pending_events;
                while (*p != NULL) p = &(*p)->next;
                e->signal = signal;
                e->event = event;
                e->syscall = syscall;
                *p = e;
                return;
            }
            link_context(ctx);
            event_context_created(ctx);
            if (ctx->attach_callback) {
                ctx->attach_callback(0, ctx, ctx->attach_data);
                ctx->attach_callback = NULL;
                ctx->attach_data = NULL;
            }
        }
    }

    if (ctx == NULL) {
        /* Clone & fork notifications can arrive after child
         * notification because the clone/fork notification comes from
         * the parent while the stop notification comes from the child
         * and Linux does not seem to order between them. */
        PendingEvent * e = loc_alloc_zero(sizeof(PendingEvent));
        trace(LOG_EVENTS, "event: pid %d is not traced - expecting OOO clone, fork or vfork event for pid", pid);
        ctx = create_context(pid);
        list_add_first(&ctx->ctxl, &pending_list);
        e->signal = signal;
        e->event = event;
        e->syscall = syscall;
        ctx->pending_events = e;
        return;
    }

    assert(!ctx->exited);
    if (signal != SIGSTOP && signal != SIGTRAP) {
        assert(signal < 32);
        ctx->pending_signals |= 1 << signal;
        if ((ctx->sig_dont_stop & (1 << signal)) == 0) ctx->pending_intercept = 1;
    }
    if (!ctx->stopped) {
        thread_state_t state;
        unsigned int state_count;
        ContextAddress pc0 = ctx->regs_error ? 0 : get_regs_PC(ctx->regs);
        assert(!ctx->regs_dirty);
        assert(!ctx->intercepted);
        ctx->regs_error = 0;
        if (thread_get_state(ctx->pid, x86_THREAD_STATE32, &ctx->regs, &state_count) != KERN_SUCCESS) {
            assert(errno != 0);
            ctx->regs_error = errno;
            trace(LOG_ALWAYS, "error: thread_get_state failed; pid %d, error %d %s",
                ctx->pid, errno, errno_to_str(errno));
        }

        if (!ctx->syscall_enter || ctx->regs_error || pc0 != get_regs_PC(ctx->regs)) {
            ctx->syscall_enter = 0;
            ctx->syscall_exit = 0;
            ctx->syscall_id = 0;
            ctx->syscall_pc = 0;
        }
        trace(LOG_EVENTS, "event: pid %d stopped at PC = %#lx", ctx->pid, get_regs_PC(ctx->regs));

        if (signal == SIGSTOP && ctx->pending_step && !ctx->regs_error && pc0 == get_regs_PC(ctx->regs)) {
            trace(LOG_EVENTS, "event: pid %d, single step failed because of pending SIGSTOP, retrying");
            ptrace(PT_STEP, ctx->pid, 0, 0);
        }
        else {
            ctx->signal = signal;
            ctx->ptrace_event = event;
            ctx->stopped = 1;
            ctx->stopped_by_bp = 0;
            ctx->end_of_step = 0;
            if (ctx->signal == SIGTRAP && ctx->ptrace_event == 0 && !syscall) {
                ctx->stopped_by_bp = !ctx->regs_error &&
                    is_breakpoint_address(ctx, get_regs_PC(ctx->regs) - BREAK_SIZE);
                ctx->end_of_step = !ctx->stopped_by_bp && ctx->pending_step;
            }
            ctx->pending_step = 0;
            if (ctx->stopped_by_bp) {
                set_regs_PC(ctx->regs, get_regs_PC(ctx->regs) - BREAK_SIZE);
                ctx->regs_dirty = 1;
            }
            event_context_stopped(ctx);
        }
    }

    if (pending_eap != NULL) {
        while (pending_eap->pending_events != NULL) {
            PendingEvent * e = pending_eap->pending_events;
            pending_eap->pending_events = e->next;
            event_pid_stopped(pending_eap->pid, e->signal, e->event, e->syscall);
            loc_free(e);
        }
    }
}

static void waitpid_listener(int pid, int exited, int exit_code, int signal, int event_code, int syscall, void * args) {
    if (exited) {
        event_pid_exited(pid, exit_code, signal);
    }
    else {
        event_pid_stopped(pid, signal, event_code, syscall);
    }
}

static void init(void) {
    list_init(&pending_list);
    add_waitpid_listener(waitpid_listener, NULL);
}

#else

#include <sys/ptrace.h>
#include <asm/unistd.h>
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
#define USE_PTRACE_SYSCALL      0

#if USE_PTRACE_SYSCALL
#define PTRACE_FLAGS ( \
    PTRACE_O_TRACESYSGOOD | \
    PTRACE_O_TRACEFORK | \
    PTRACE_O_TRACEVFORK | \
    PTRACE_O_TRACECLONE | \
    PTRACE_O_TRACEEXEC | \
    PTRACE_O_TRACEVFORKDONE | \
    PTRACE_O_TRACEEXIT)
#else
#define PTRACE_FLAGS ( \
    PTRACE_O_TRACEFORK | \
    PTRACE_O_TRACEVFORK | \
    PTRACE_O_TRACECLONE | \
    PTRACE_O_TRACEEXEC | \
    PTRACE_O_TRACEVFORKDONE | \
    PTRACE_O_TRACEEXIT)
#endif

typedef struct PendingEvent {
    int event;
    int syscall;
    int signal;
    struct PendingEvent * next;
} PendingEvent;

static LINK pending_list;

static char * event_name(int event) {
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

char * context_suspend_reason(Context * ctx) {
    static char reason[128];

    if (ctx->stopped_by_bp) return "Breakpoint";
    if (ctx->end_of_step) return "Step";
    if (ctx->ptrace_event != 0) {
        assert(ctx->signal == SIGTRAP);
        snprintf(reason, sizeof(reason), "Event: %s", event_name(ctx->ptrace_event));
        return reason;
    }
    if (ctx->syscall_enter) return "System Call";
    if (ctx->syscall_exit) return "System Return";
    if (ctx->signal == SIGSTOP || ctx->signal == SIGTRAP) {
        return "Suspended";
    }
    if (signal_name(ctx->signal)) {
        snprintf(reason, sizeof(reason), "Signal %d %s", ctx->signal, signal_name(ctx->signal));
        return reason;
    }

    snprintf(reason, sizeof(reason), "Signal %d", ctx->signal);
    return reason;
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

int context_attach(pid_t pid, ContextAttachCallBack * done, void * data, int selfattach) {
    Context * ctx = NULL;

    assert(done != NULL);
    if (!selfattach && ptrace(PTRACE_ATTACH, pid, 0, 0) < 0) {
        int err = errno;
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_ATTACH) failed: pid %d, error %d %s",
            pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    ctx = create_context(pid);
    list_add_first(&ctx->ctxl, &pending_list);
    ctx->mem = pid;
    ctx->attach_callback = done;
    ctx->attach_data = data;
    ctx->pending_intercept = 1;
    /* TODO: context_attach works only for main task in a process */
    add_waitpid_process(pid);
    return 0;
}

int context_has_state(Context * ctx) {
    return 1;
}

int context_stop(Context * ctx) {
    trace(LOG_CONTEXT, "context:%s suspending ctx %#lx pid %d",
        ctx->pending_intercept ? "" : " temporary", ctx, ctx->pid);
    assert(is_dispatch_thread());
    assert(!ctx->exited);
    assert(!ctx->stopped);
    assert(!ctx->regs_dirty);
    assert(!ctx->intercepted);
    if (tkill(ctx->pid, SIGSTOP) < 0) {
        int err = errno;
        if (err != ESRCH) {
            trace(LOG_ALWAYS, "error: tkill(SIGSTOP) failed: ctx %#lx, pid %d, error %d %s",
                ctx, ctx->pid, err, errno_to_str(err));
        }
        errno = err;
        return -1;
    }
    return 0;
}

static int syscall_never_returns(Context * ctx) {
    if (ctx->syscall_enter) {
        switch (ctx->syscall_id) {
#ifdef __NR_sigreturn
        case __NR_sigreturn:
            return 1;
#endif
        }
    }
    return 0;
}

int context_continue(Context * ctx) {
    int signal = 0;

    assert(is_dispatch_thread());
    assert(ctx->stopped);
    assert(!ctx->pending_intercept);
    assert(!ctx->pending_step);
    assert(!ctx->exited);

    if (skip_breakpoint(ctx, 0)) return 0;

    if (!ctx->syscall_enter) {
        while (ctx->pending_signals != 0) {
            while ((ctx->pending_signals & (1 << signal)) == 0) signal++;
            if (ctx->sig_dont_pass & (1 << signal)) {
                ctx->pending_signals &= ~(1 << signal);
                signal = 0;
            }
            else {
                break;
            }
        }
        assert(signal != SIGSTOP);
        assert(signal != SIGTRAP);
    }

    trace(LOG_CONTEXT, "context: resuming ctx %#lx, pid %d, with signal %d", ctx, ctx->pid, signal);
#if defined(__i386__) || defined(__x86_64__)
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
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_SETREGS) failed: ctx %#lx, pid %d, error %d %s",
                ctx, ctx->pid, err, errno_to_str(err));
            errno = err;
            return -1;
        }
        ctx->regs_dirty = 0;
    }
    if (ptrace((ctx->ptrace_flags & PTRACE_O_TRACESYSGOOD) != 0 ? PTRACE_SYSCALL : PTRACE_CONT, ctx->pid, 0, signal) < 0) {
        int err = errno;
#if USE_ESRCH_WORKAROUND
        if (err == ESRCH) {
            ctx->stopped = 0;
            event_context_started(ctx);
            return 0;
        }
#endif
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_CONT, ...) failed: ctx %#lx, pid %d, error %d %s",
            ctx, ctx->pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    ctx->pending_signals &= ~(1 << signal);
    if (syscall_never_returns(ctx)) {
        ctx->syscall_enter = 0;
        ctx->syscall_exit = 0;
        ctx->syscall_id = 0;
    }
    ctx->stopped = 0;
    event_context_started(ctx);
    return 0;
}

int context_single_step(Context * ctx) {
    assert(is_dispatch_thread());
    assert(ctx->stopped);
    assert(!ctx->pending_step);
    assert(!ctx->exited);

    if (skip_breakpoint(ctx, 1)) return 0;

    if (syscall_never_returns(ctx)) return context_continue(ctx);
    trace(LOG_CONTEXT, "context: single step ctx %#lx, pid %d", ctx, ctx->pid);
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
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_SETREGS) failed: ctx %#lx, pid %d, error %d %s",
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
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_SINGLESTEP, ...) failed: ctx %#lx, pid %d, error %d %s",
            ctx, ctx->pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    ctx->pending_step = 1;
    ctx->stopped = 0;
    event_context_started(ctx);
    return 0;
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    ContextAddress word_addr;
    unsigned word_size = context_word_size(ctx);
    assert(is_dispatch_thread());
    assert(!ctx->exited);
    trace(LOG_CONTEXT, "context: write memory ctx %#lx, pid %d, address %#lx, size %zd",
        ctx, ctx->pid, address, size);
    assert(word_size <= sizeof(unsigned long));
    for (word_addr = address & ~((ContextAddress)word_size - 1); word_addr < address + size; word_addr += word_size) {
        unsigned long word = 0;
        if (word_addr < address || word_addr + word_size > address + size) {
            int i;
            errno = 0;
            word = ptrace(PTRACE_PEEKDATA, ctx->pid, word_addr, 0);
            if (errno != 0) {
                int err = errno;
                trace(LOG_ALWAYS, "error: ptrace(PTRACE_PEEKDATA, ...) failed: ctx %#lx, pid %d, addr %#lx, error %d %s",
                    ctx, ctx->pid, word_addr, err, errno_to_str(err));
                errno = err;
                return -1;
            }
            for (i = 0; i < word_size; i++) {
                if (word_addr + i >= address && word_addr + i < address + size) {
                    ((char *)&word)[i] = ((char *)buf)[word_addr + i - address];
                }
            }
        }
        else {
            memcpy(&word, (char *)buf + (word_addr - address), word_size);
        }
        if (ptrace(PTRACE_POKEDATA, ctx->pid, word_addr, word) < 0) {
            int err = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_POKEDATA, ...) failed: ctx %#lx, pid %d, addr %#lx, error %d %s",
                ctx, ctx->pid, word_addr, err, errno_to_str(err));
            errno = err;
            return -1;
        }
    }
    return 0;
}

int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    ContextAddress word_addr;
    unsigned word_size = context_word_size(ctx);
    assert(is_dispatch_thread());
    assert(!ctx->exited);
    trace(LOG_CONTEXT, "context: read memory ctx %#lx, pid %d, address %#lx, size %zd",
        ctx, ctx->pid, address, size);
    assert(word_size <= sizeof(unsigned long));
    for (word_addr = address & ~((ContextAddress)word_size - 1); word_addr < address + size; word_addr += word_size) {
        unsigned long word = 0;
        errno = 0;
        word = ptrace(PTRACE_PEEKDATA, ctx->pid, word_addr, 0);
        if (errno != 0) {
            int err = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_PEEKDATA, ...) failed: ctx %#lx, pid %d, addr %#lx, error %d %s",
                ctx, ctx->pid, word_addr, err, errno_to_str(err));
            errno = err;
            return -1;
        }
        if (word_addr < address || word_addr + word_size > address + size) {
            int i;
            for (i = 0; i < word_size; i++) {
                if (word_addr + i >= address && word_addr + i < address + size) {
                    ((char *)buf)[word_addr + i - address] = ((char *)&word)[i];
                }
            }
        }
        else {
            memcpy((char *)buf + (word_addr - address), &word, word_size);
        }
    }
    return 0;
}

static Context * find_pending(pid_t pid) {
    LINK * qp = pending_list.next;
    while (qp != &pending_list) {
        Context * c = ctxl2ctxp(qp);
        if (c->pid == pid) return c;
        qp = qp->next;
    }
    return NULL;
}

static void event_pid_exited(pid_t pid, int status, int signal) {
    Context * ctx;

    ctx = context_find_from_pid(pid);
    if (ctx == NULL) {
        ctx = find_pending(pid);
        if (ctx == NULL) {
            trace(LOG_EVENTS, "event: ctx not found, pid %d, exit status %d, term signal %d", pid, status, signal);
        }
        else {
            assert(ctx->ref_count == 0);
            if (ctx->attach_callback != NULL) {
                if (status == 0) status = EINVAL;
                ctx->attach_callback(status, ctx, ctx->attach_data);
                ctx->attach_callback = NULL;
                ctx->attach_data = NULL;
            }
            assert(list_is_empty(&ctx->children));
            assert(ctx->parent == NULL);
            list_remove(&ctx->ctxl);
            loc_free(ctx);
        }
    }
    else {
        assert(ctx->attach_callback == NULL);
        if (ctx->stopped || ctx->intercepted || ctx->exited) {
            trace(LOG_EVENTS, "event: ctx %#lx, pid %d, exit status %d unexpected, stopped %d, intercepted %d, exited %d",
                ctx, pid, status, ctx->stopped, ctx->intercepted, ctx->exited);
            if (ctx->stopped) {
                ctx->stopped = 0;
                event_context_started(ctx);
            }
        }
        else {
            trace(LOG_EVENTS, "event: ctx %#lx, pid %d, exit status %d, term signal %d", ctx, pid, status, signal);
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
         * between PTRACE_SYSCALL and SIGTRAP/PTRACE_EVENT_EXIT. So, ctx->exiting can be 0.
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
}

#if defined(__x86_64__)
#   define get_syscall_id(ctx) (ctx->regs.orig_rax)
#elif defined(__i386__)
#   define get_syscall_id(ctx) (ctx->regs.orig_eax)
#else
#   error "get_syscall_id() is not implemented for CPU other then X86"
#endif

static void event_pid_stopped(pid_t pid, int signal, int event, int syscall) {
    unsigned long msg = 0;
    Context * ctx = NULL;
    Context * ctx2 = NULL;
    Context * pending_eap = NULL;

    trace(LOG_EVENTS, "event: pid %d stopped, signal %d, event %s", pid, signal, event_name(event));

    ctx = context_find_from_pid(pid);

    if (ctx == NULL) {
        ctx = find_pending(pid);
        if (ctx != NULL) {
            if (ctx->pending_events != NULL) {
                PendingEvent * e = loc_alloc_zero(sizeof(PendingEvent));
                PendingEvent ** p = (PendingEvent **)&ctx->pending_events;
                while (*p != NULL) p = &(*p)->next;
                e->signal = signal;
                e->event = event;
                e->syscall = syscall;
                *p = e;
                return;
            }
            link_context(ctx);
            event_context_created(ctx);
            if (ctx->attach_callback) {
                ctx->attach_callback(0, ctx, ctx->attach_data);
                ctx->attach_callback = NULL;
                ctx->attach_data = NULL;
            }
        }
    }

    if (ctx == NULL) {
        /* Clone & fork notifications can arrive after child
         * notification because the clone/fork notification comes from
         * the parent while the stop notification comes from the child
         * and Linux does not seem to order between them. */
        PendingEvent * e = loc_alloc_zero(sizeof(PendingEvent));
        trace(LOG_EVENTS, "event: pid %d is not traced - expecting OOO clone, fork or vfork event for pid", pid);
        ctx = create_context(pid);
        list_add_first(&ctx->ctxl, &pending_list);
        e->signal = signal;
        e->event = event;
        e->syscall = syscall;
        ctx->pending_events = e;
        return;
    }

    assert(!ctx->exited);
    assert(!ctx->stopped || event == 0 || event == PTRACE_EVENT_EXIT);
    if (ctx->ptrace_flags != PTRACE_FLAGS) {
        if (ptrace(PTRACE_SETOPTIONS, ctx->pid, 0, PTRACE_FLAGS) < 0) {
            int err = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_SETOPTIONS) failed: pid %d, error %d %s",
                ctx->pid, err, errno_to_str(err));
        }
        else {
            ctx->ptrace_flags = PTRACE_FLAGS;
        }
    }

    switch (event) {
    case PTRACE_EVENT_FORK:
    case PTRACE_EVENT_VFORK:
    case PTRACE_EVENT_CLONE:
        assert(!ctx->attach_callback);
        if (ptrace(PTRACE_GETEVENTMSG, pid, 0, &msg) < 0) {
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_GETEVENTMSG) failed; pid %d, error %d %s",
                pid, errno, errno_to_str(errno));
            break;
        }
        assert(msg != 0);
        ctx2 = find_pending(msg);
        if (ctx2) {
            assert(ctx2->pending_events != NULL);
            pending_eap = ctx2;
        }
        else {
            ctx2 = create_context(msg);
        }
        link_context(ctx2);
        assert(ctx2->parent == NULL);
        trace(LOG_EVENTS, "event: new context 0x%x, pid %d", ctx2, ctx2->pid);
        ctx2->sig_dont_stop = ctx->sig_dont_stop;
        ctx2->sig_dont_pass = ctx->sig_dont_pass;
        if (event == PTRACE_EVENT_CLONE) {
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
        if (!ctx->attach_callback) {
            event_context_changed(ctx);
        }
        break;
    }

    if (signal != SIGSTOP && signal != SIGTRAP) {
        assert(signal < 32);
        ctx->pending_signals |= 1 << signal;
        if ((ctx->sig_dont_stop & (1 << signal)) == 0) ctx->pending_intercept = 1;
    }
    if (event == PTRACE_EVENT_EXIT) {
        ctx->exiting = 1;
        ctx->regs_dirty = 0;
    }
    if (!ctx->stopped) {
        ContextAddress pc0 = ctx->regs_error ? 0 : get_regs_PC(ctx->regs);
        assert(!ctx->regs_dirty);
        assert(!ctx->intercepted);
        ctx->regs_error = 0;
        if (ptrace(PTRACE_GETREGS, ctx->pid, 0, &ctx->regs) < 0) {
            assert(errno != 0);
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
                return;
            }
#endif
            ctx->regs_error = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_GETREGS) failed; pid %d, error %d %s",
                ctx->pid, errno, errno_to_str(errno));
        }

        if (syscall && !ctx->regs_error) {
            if (!ctx->syscall_enter) {
                ctx->syscall_id = get_syscall_id(ctx);
                ctx->syscall_pc = get_regs_PC(ctx->regs);
                ctx->syscall_enter = 1;
                ctx->syscall_exit = 0;
                trace(LOG_EVENTS, "event: pid %d enter sys call %d, PC = %#lx",
                    ctx->pid, ctx->syscall_id, ctx->syscall_pc);
            }
            else {
                if (ctx->syscall_pc != get_regs_PC(ctx->regs)) {
                    trace(LOG_ALWAYS, "Invalid PC at sys call exit: pid %d, sys call %d, PC %#lx, expected PC %#lx",
                        ctx->pid, ctx->syscall_id, get_regs_PC(ctx->regs), ctx->syscall_pc);
                }
                trace(LOG_EVENTS, "event: pid %d exit sys call %d, PC = %#lx",
                    ctx->pid, ctx->syscall_id, get_regs_PC(ctx->regs));
                switch (ctx->syscall_id) {
                case __NR_mmap:
                case __NR_munmap:
#ifdef __NR_mmap2
                case __NR_mmap2:
#endif
                case __NR_mremap:
                case __NR_remap_file_pages:
                    event_context_changed(ctx);
                    break;
                }
                ctx->syscall_enter = 0;
                ctx->syscall_exit = 1;
            }
        }
        else {
            if (!ctx->syscall_enter || ctx->regs_error || pc0 != get_regs_PC(ctx->regs)) {
                ctx->syscall_enter = 0;
                ctx->syscall_exit = 0;
                ctx->syscall_id = 0;
                ctx->syscall_pc = 0;
            }
            trace(LOG_EVENTS, "event: pid %d stopped at PC = %#lx", ctx->pid, get_regs_PC(ctx->regs));
        }

        if (signal == SIGSTOP && ctx->pending_step && !ctx->regs_error && pc0 == get_regs_PC(ctx->regs)) {
            trace(LOG_EVENTS, "event: pid %d, single step failed because of pending SIGSTOP, retrying");
            ptrace(PTRACE_SINGLESTEP, ctx->pid, 0, 0);
        }
        else {
            ctx->signal = signal;
            ctx->ptrace_event = event;
            ctx->stopped = 1;
            ctx->stopped_by_bp = 0;
            ctx->end_of_step = 0;
            if (ctx->signal == SIGTRAP && ctx->ptrace_event == 0 && !syscall) {
                ctx->stopped_by_bp = !ctx->regs_error &&
                    is_breakpoint_address(ctx, get_regs_PC(ctx->regs) - BREAK_SIZE);
                ctx->end_of_step = !ctx->stopped_by_bp && ctx->pending_step;
            }
            ctx->pending_step = 0;
            if (ctx->stopped_by_bp) {
                set_regs_PC(ctx->regs, get_regs_PC(ctx->regs) - BREAK_SIZE);
                ctx->regs_dirty = 1;
            }
            event_context_stopped(ctx);
        }
    }

    if (pending_eap != NULL) {
        while (pending_eap->pending_events != NULL) {
            PendingEvent * e = pending_eap->pending_events;
            pending_eap->pending_events = e->next;
            event_pid_stopped(pending_eap->pid, e->signal, e->event, e->syscall);
            loc_free(e);
        }
    }
}

static void waitpid_listener(int pid, int exited, int exit_code, int signal, int event_code, int syscall, void * args) {
    if (exited) {
        event_pid_exited(pid, exit_code, signal);
    }
    else {
        event_pid_stopped(pid, signal, event_code, syscall);
    }
}

static void init(void) {
    list_init(&pending_list);
    add_waitpid_listener(waitpid_listener, NULL);
}

#endif

unsigned context_word_size(Context * ctx) {
    /* Place holder to support variable context word size */
    return sizeof(ContextAddress);
}

static void eventpoint_at_main(Context * ctx, void * args) {
#if ENABLE_ELF
    ctx->debug_structure_searched = 0;
    ctx->debug_structure_address = 0;
#endif
    ctx->pending_intercept = 1;
    event_context_changed(ctx);
}

void ini_contexts(void) {
    int i;

    list_init(&context_root);
    for (i = 0; i < CONTEXT_PID_ROOT_SIZE; i++) {
        list_init(&context_pid_root[i]);
    }
    init();
    create_eventpoint("main", eventpoint_at_main, NULL);
}

#endif  /* if ENABLE_DebugContext */
