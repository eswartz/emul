/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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

#if defined(WIN32)

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

#if ENABLE_DebugContext

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

    if (ctx->stopped_by_bp && ctx->bp_ids != NULL) return "Breakpoint";
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
    ctx->stopped_by_exception = 0;
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
        ctx->stopped_by_exception = 1;
        ctx->pending_intercept = 1;
        break;
    }
    send_context_stopped_event(ctx);
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
    send_context_started_event(ctx);
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
    send_context_exited_event(ctx);
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
            send_context_created_event(prs);
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
            send_context_created_event(ctx);
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
            send_context_created_event(ctx);
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
            send_context_changed_event(prs);
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
            send_context_changed_event(prs);
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

void init_contexts_sys_dep(void) {
}

#endif  /* if ENABLE_DebugContext */
#endif /* WIN32 */
