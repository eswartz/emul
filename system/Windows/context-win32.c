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
 * This module handles process/thread OS contexts and their state machine.
 */

#include <config.h>

#if defined(WIN32)

#if ENABLE_DebugContext && !ENABLE_ContextProxy

#include <stdlib.h>
#include <assert.h>
#include <errno.h>
#include <signal.h>
#include <framework/context.h>
#include <framework/events.h>
#include <framework/errors.h>
#include <framework/trace.h>
#include <framework/myalloc.h>
#include <framework/waitpid.h>
#include <framework/signames.h>
#include <services/breakpoints.h>
#include <system/Windows/context-win32.h>
#include <system/Windows/regset.h>

typedef struct ContextExtensionWin32 {
    pid_t               pid;
    HANDLE              handle;
    HANDLE              file_handle;
    DWORD64             base_address;
    EXCEPTION_DEBUG_INFO suspend_reason;
    int                 stop_pending;
    REG_SET *           regs;               /* copy of context registers, updated when context stops */
    ErrorReport *       regs_error;         /* if not NULL, 'regs' is invalid */
    int                 regs_dirty;         /* if not 0, 'regs' is modified and needs to be saved before context is continued */
    int                 trace_flag;
    uint8_t             step_opcodes[4];
    SIZE_T              step_opcodes_len;
    ContextAddress      step_opcodes_addr;
    struct DebugState * debug_state;
} ContextExtensionWin32;

static size_t context_extension_offset = 0;

#define EXT(ctx) ((ContextExtensionWin32 *)((char *)(ctx) + context_extension_offset))

typedef struct DebugState {
    int                 error;
    int                 state;
    DWORD               process_id;
    DWORD               debug_thread_id;
    HANDLE              debug_thread;
    HANDLE              debug_thread_semaphore;
    HANDLE              debug_event_inp;
    HANDLE              debug_event_out;
    DWORD               ini_thread_id;
    HANDLE              ini_thread_handle;
    DWORD               main_thread_id;
    HANDLE              main_thread_handle;
    int                 process_suspended;
    int                 break_posted;
    HANDLE              break_process;
    HANDLE              break_thread;
    LPVOID              break_thread_code;
    DWORD               break_thread_id;
    int                 module_loaded;
    int                 module_unloaded;
    HANDLE              module_handle;
    DWORD64             module_address;
    ContextAttachCallBack * attach_callback;
    void *              attach_data;
} DebugState;

#define DEBUG_STATE_INIT            0
#define DEBUG_STATE_PRS_CREATED     1
#define DEBUG_STATE_PRS_ATTACHED    2

typedef struct DebugEvent {
    DebugState * debug_state;
    DEBUG_EVENT win32_event;
    DWORD continue_status;
} DebugEvent;

static OSVERSIONINFOEX os_version;

#define MAX_EXCEPTION_HANDLERS 8
static ContextExceptionHandler * exception_handlers[MAX_EXCEPTION_HANDLERS];
static unsigned exception_handler_cnt = 0;

#include <system/pid-hash.h>

#define EXCEPTION_DEBUGGER_IO 0x406D1388

static int is_big_endian(void) {
    short n = 0x0201;
    char * p = (char *)&n;
    return *p == 0x02;
}

const char * context_suspend_reason(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    DWORD exception_code = ext->suspend_reason.ExceptionRecord.ExceptionCode;
    static char buf[64];

    if (exception_code == 0) return "Suspended";
    if (ext->suspend_reason.dwFirstChance) {
        if (exception_code == EXCEPTION_SINGLE_STEP) return "Step";
        if (exception_code == EXCEPTION_BREAKPOINT) return "Eventpoint";
        snprintf(buf, sizeof(buf), "Exception %#lx", exception_code);
    }
    else {
        snprintf(buf, sizeof(buf), "Unhandled exception %#lx", exception_code);
    }
    return buf;
}

static int get_signal_index(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    DWORD exception_code = ext->suspend_reason.ExceptionRecord.ExceptionCode;

    if (exception_code == 0) return 0;
    return get_signal_from_code(exception_code);
}

static const char * win32_debug_event_name(int event) {
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

static int log_error(const char * fn, int ok) {
    int err;
    if (ok) return 0;
    assert(is_dispatch_thread());
    err = set_win32_errno(GetLastError());
    trace(LOG_ALWAYS, "context: %s: %s", fn, errno_to_str(errno));
    return err;
}

static void event_win32_context_exited(Context * ctx);

static void get_registers(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);

    if (ext->regs->ContextFlags) return;

    assert(!ctx->exited);
    assert(context_has_state(ctx));
    assert(ctx->stopped);

    ext->regs->ContextFlags = CONTEXT_CONTROL | CONTEXT_INTEGER;
    if (GetThreadContext(ext->handle, ext->regs) == 0) {
        ext->regs_error = get_error_report(log_error("GetThreadContext", 0));
    }
    else {
        ext->trace_flag = (ext->regs->EFlags & 0x100) != 0;
        trace(LOG_CONTEXT, "context: get regs OK: ctx %#lx, id %s, PC %#lx",
            ctx, ctx->id, ext->regs->Eip);
    }
}

static DWORD event_win32_context_stopped(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    DWORD exception_code = ext->suspend_reason.ExceptionRecord.ExceptionCode;
    DWORD continue_status = DBG_CONTINUE;
    size_t break_size = 0;

    assert(is_dispatch_thread());
    assert(!ctx->exited);
    assert(!ctx->stopped);
    assert(ext->handle != NULL);
    assert(ctx->parent != NULL);

    ext->stop_pending = 0;

    trace(LOG_CONTEXT, "context: stopped: ctx %#lx, id %s, exception %#lx",
        ctx, ctx->id, exception_code);

    if (SuspendThread(ext->handle) == (DWORD)-1) {
        DWORD err = GetLastError();
        ctx->exiting = 1;
        if (err == ERROR_ACCESS_DENIED) {
            /* Already exited */
            return DBG_CONTINUE;
        }
        log_error("SuspendThread", 0);
        return DBG_EXCEPTION_NOT_HANDLED;
    }

    if (ext->regs_error) {
        release_error_report(ext->regs_error);
        ext->regs_error = NULL;
    }
    memset(ext->regs, 0, sizeof(REG_SET));

    ctx->signal = get_signal_index(ctx);
    ctx->pending_signals = 0;
    ctx->stopped = 1;
    ctx->stopped_by_bp = 0;
    if (exception_code == 0) {
        ctx->stopped_by_exception = 0;
    }
    else if (ext->suspend_reason.dwFirstChance) {
        ctx->stopped_by_exception = 0;
        switch (exception_code) {
        case EXCEPTION_SINGLE_STEP:
            if (ext->step_opcodes_len == 0) {
                continue_status = DBG_EXCEPTION_NOT_HANDLED;
            }
            else if (ext->step_opcodes[0] == 0x9c) {
                /* PUSHF instruction: need to clear trace flag from top of the stack */
                SIZE_T bcnt = 0;
                ContextAddress buf = 0;
                get_registers(ctx);
                if (!ext->regs_error) {
                    assert(ext->regs->EFlags & 0x100);
                    assert(ext->step_opcodes_addr == ext->regs->Eip - 1);
                    if (!ReadProcessMemory(EXT(ctx->mem)->handle, (LPCVOID)ext->regs->Esp, &buf, sizeof(ContextAddress), &bcnt) || bcnt != sizeof(ContextAddress)) {
                        log_error("ReadProcessMemory", 0);
                    }
                    else {
                        assert(buf & 0x100);
                        buf &= ~0x100;
                        if (!WriteProcessMemory(EXT(ctx->mem)->handle, (LPVOID)ext->regs->Esp, &buf, sizeof(ContextAddress), &bcnt) || bcnt != sizeof(ContextAddress)) {
                            log_error("WriteProcessMemory", 0);
                        }
                    }
                }
            }
            ext->step_opcodes_len = 0;
            ext->step_opcodes_addr = 0;
            break;
        case EXCEPTION_BREAKPOINT:
            get_break_instruction(ctx, &break_size);
            get_registers(ctx);
            if (!ext->regs_error && is_breakpoint_address(ctx, ext->regs->Eip - break_size)) {
                ext->regs->Eip -= break_size;
                ext->regs_dirty = 1;
                ctx->stopped_by_bp = 1;
            }
            else {
                continue_status = DBG_EXCEPTION_NOT_HANDLED;
            }
            break;
        case EXCEPTION_DEBUGGER_IO:
            trace(LOG_ALWAYS, "Debugger IO request %#lx",
                ext->suspend_reason.ExceptionRecord.ExceptionInformation[0]);
            break;
        default:
            continue_status = DBG_EXCEPTION_NOT_HANDLED;
            break;
        }
        if (continue_status == DBG_EXCEPTION_NOT_HANDLED) {
            unsigned i;
            for (i = 0; i < exception_handler_cnt; i++) {
                if (exception_handlers[i](ctx, &ext->suspend_reason)) {
                    continue_status = DBG_CONTINUE;
                }
            }
        }
        if (continue_status == DBG_EXCEPTION_NOT_HANDLED) {
            int intercept = 1;
            ctx->stopped_by_exception = 1;
            if (ctx->signal) {
                ctx->pending_signals |= 1 << ctx->signal;
                if (ctx->sig_dont_pass & (1 << ctx->signal)) {
                    continue_status = DBG_CONTINUE;
                }
                if (ctx->sig_dont_stop & (1 << ctx->signal)) {
                    intercept = 0;
                }
            }
            if (intercept) ctx->pending_intercept = 1;
        }
    }
    else {
        ctx->stopped_by_exception = 1;
        if (!ctx->mem->exiting) ctx->pending_intercept = 1;
        continue_status = DBG_EXCEPTION_NOT_HANDLED;
    }
    send_context_stopped_event(ctx);
    return continue_status;
}

static void event_win32_context_started(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    trace(LOG_CONTEXT, "context: started: ctx %#lx, id %s", ctx, ctx->id);
    assert(ctx->stopped);
    ext->stop_pending = 0;
    send_context_started_event(ctx);
}

static void event_win32_context_exited(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    LINK * l = NULL;
    trace(LOG_CONTEXT, "context: exited: ctx %#lx, id %s", ctx, ctx->id);
    assert(!ctx->exited);
    context_lock(ctx);
    ctx->exiting = 1;
    ext->stop_pending = 0;
    ext->debug_state = NULL;
    if (ctx->stopped) send_context_started_event(ctx);
    l = ctx->children.next;
    while (l != &ctx->children) {
        Context * c = cldl2ctxp(l);
        l = l->next;
        assert(c->parent == ctx);
        if (!c->exited) event_win32_context_exited(c);
    }
    release_error_report(ext->regs_error);
    loc_free(ext->regs);
    ext->regs_error = NULL;
    ext->regs = NULL;
    send_context_exited_event(ctx);
    if (ext->handle != NULL) {
        if (ctx->mem != ctx) {
            log_error("CloseHandle", CloseHandle(ext->handle));
        }
        else if (os_version.dwMajorVersion <= 5) {
            /* Bug in Windows XP: ContinueDebugEvent() does not close exited process handle */
            log_error("CloseHandle", CloseHandle(ext->handle));
        }
        ext->handle = NULL;
    }
    if (ext->file_handle != NULL) {
        log_error("CloseHandle", CloseHandle(ext->file_handle));
        ext->file_handle = NULL;
    }
    context_unlock(ctx);
}

static void suspend_threads(DWORD prs_id) {
    LINK * l;
    Context * prs = context_find_from_pid(prs_id, 0);

    if (prs == NULL || prs->exited) return;
    assert(EXT(prs)->debug_state->process_suspended);
    for (l = prs->children.next; l != &prs->children; l = l->next) {
        Context * ctx = cldl2ctxp(l);
        ContextExtensionWin32 * ext = EXT(ctx);
        if (!ctx->stopped) {
            memset(&ext->suspend_reason, 0, sizeof(ext->suspend_reason));
            event_win32_context_stopped(ctx);
        }
    }
}

static DWORD WINAPI remote_thread_func(LPVOID args) {
    return 0;
}

static void break_process_event(void * args) {
    Context * ctx = (Context *)args;
    ContextExtensionWin32 * ext = EXT(ctx);

    if (ext->debug_state != NULL) {
        LINK * l;
        int cnt = 0;
        DebugState * debug_state = ext->debug_state;

        if (!ctx->exited && debug_state->break_thread == NULL) {
            for (l = ctx->children.next; l != &ctx->children; l = l->next) {
                ContextExtensionWin32 * x = EXT(cldl2ctxp(l));
                if (x->stop_pending) cnt++;
            }
            if (cnt > 0) {
                const SIZE_T buf_size = 0x100;
                DWORD size = 0;
                int error = 0;

                trace(LOG_CONTEXT, "context: creating remote thread in process %#lx, id %s", ctx, ctx->id);
                debug_state->break_process = ext->handle;
                debug_state->break_thread_code = VirtualAllocEx(debug_state->break_process,
                    NULL, buf_size, MEM_COMMIT, PAGE_EXECUTE);
                error = log_error("VirtualAllocEx", debug_state->break_thread_code != NULL);

                if (!error) error = log_error("WriteProcessMemory", WriteProcessMemory(debug_state->break_process,
                    debug_state->break_thread_code, (LPCVOID)remote_thread_func, buf_size, &size) && size == buf_size);

                if (!error) error = log_error("CreateRemoteThread", (debug_state->break_thread = CreateRemoteThread(debug_state->break_process,
                    0, 0, (DWORD (WINAPI*)(LPVOID))debug_state->break_thread_code, NULL, 0, &debug_state->break_thread_id)) != NULL);

                if (error && debug_state->break_thread_code != NULL) {
                    VirtualFreeEx(debug_state->break_process, debug_state->break_thread_code, 0, MEM_RELEASE);
                    debug_state->break_thread_code = NULL;
                    debug_state->break_thread = NULL;
                    debug_state->break_process = NULL;
                }
            }
        }
        debug_state->break_posted = 0;
    }
    context_unlock(ctx);
}

static int win32_resume(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    assert(ctx->stopped);
    assert(!ctx->exited);
    if (ext->regs_dirty) {
        if (ext->regs_error) {
            trace(LOG_ALWAYS, "Can't resume thread, registers copy is invalid: ctx %#lx, id %s", ctx, ctx->id);
            errno = set_error_report_errno(ext->regs_error);
            return -1;
        }
        if (SetThreadContext(ext->handle, ext->regs) == 0) {
            errno = log_error("SetThreadContext", 0);
            return -1;
        }
        ext->trace_flag = (ext->regs->EFlags & 0x100) != 0;
        ext->regs_dirty = 0;
    }
    if (ctx->parent->pending_signals & (1 << SIGKILL)) {
        LINK * l;
        Context * prs = ctx->parent;
        trace(LOG_CONTEXT, "context: terminating process %#lx, id %s", prs, prs->id);
        if (!prs->exiting && !TerminateProcess(EXT(ctx->parent)->handle, 1)) {
            errno = log_error("TerminateProcess", 0);
            return -1;
        }
        prs->pending_signals &= ~(1 << SIGKILL);
        prs->exiting = 1;
        for (l = prs->children.next; l != &prs->children; l = l->next) {
            Context * c = cldl2ctxp(l);
            c->exiting = 1;
            if (c->stopped) event_win32_context_started(c);
        }
    }
    else {
        if (ext->trace_flag) {
            get_registers(ctx);
            if (ext->regs_error) {
                set_error_report_errno(ext->regs_error);
                return -1;
            }
            ext->step_opcodes_addr = ext->regs->Eip;
            if (!ReadProcessMemory(EXT(ctx->mem)->handle, (LPCVOID)ext->regs->Eip, &ext->step_opcodes,
                    sizeof(ext->step_opcodes), &ext->step_opcodes_len) || ext->step_opcodes_len == 0) {
                errno = log_error("ReadProcessMemory", 0);
                return -1;
            }
        }
        for (;;) {
            DWORD cnt = ResumeThread(ext->handle);
            if (cnt == (DWORD)-1) {
                errno = log_error("ResumeThread", 0);
                return -1;
            }
            if (cnt <= 1) break;
        }
        event_win32_context_started(ctx);
    }
    return 0;
}

static void debug_event_handler(void * x) {
    DebugEvent * debug_event = (DebugEvent *)x;
    DebugState * debug_state = debug_event->debug_state;
    DEBUG_EVENT * win32_event = &debug_event->win32_event;
    Context * prs = context_find_from_pid(win32_event->dwProcessId, 0);
    Context * ctx = context_find_from_pid(win32_event->dwThreadId, 1);
    ContextExtensionWin32 * ext = NULL;

    assert(ctx == NULL || ctx->parent == prs);

    switch (win32_event->dwDebugEventCode) {
    case CREATE_PROCESS_DEBUG_EVENT:
        if (debug_state->state == DEBUG_STATE_INIT) {
            debug_state->state = DEBUG_STATE_PRS_CREATED;
            debug_state->main_thread_id = win32_event->dwThreadId;
            debug_state->main_thread_handle = win32_event->u.CreateProcessInfo.hThread;
            assert(prs == NULL);
            assert(ctx == NULL);
            ext = EXT(prs = create_context(pid2id(win32_event->dwProcessId, 0)));
            prs->mem = prs;
            prs->mem_access |= MEM_ACCESS_INSTRUCTION;
            prs->mem_access |= MEM_ACCESS_DATA;
            prs->mem_access |= MEM_ACCESS_USER;
            prs->big_endian = is_big_endian();
            ext->pid = win32_event->dwProcessId;
            ext->handle = win32_event->u.CreateProcessInfo.hProcess;
            ext->file_handle = win32_event->u.CreateProcessInfo.hFile;
            ext->base_address = (uintptr_t)win32_event->u.CreateProcessInfo.lpBaseOfImage;
            ext->debug_state = debug_state;
            assert(ext->handle != NULL);
            link_context(prs);
            send_context_created_event(prs);
        }
        else {
            /* This looks like a bug in Windows XP: */
            /* 1. according to the documentation, we should get only one CREATE_PROCESS_DEBUG_EVENT. */
            /* 2. if we don't suspend second process, debugee crashes. */
            assert(debug_state->ini_thread_handle == NULL);
            debug_state->ini_thread_id = win32_event->dwThreadId;
            debug_state->ini_thread_handle = win32_event->u.CreateProcessInfo.hThread;
            SuspendThread(debug_state->ini_thread_handle);
            CloseHandle(win32_event->u.CreateProcessInfo.hFile);
            ResumeThread(debug_state->main_thread_handle);
        }
        break;
    case CREATE_THREAD_DEBUG_EVENT:
        assert(prs != NULL);
        assert(ctx == NULL);
        if (debug_state->state < DEBUG_STATE_PRS_ATTACHED) break;
        if (debug_state->break_thread_id == win32_event->dwThreadId) break;
        ext = EXT(ctx = create_context(pid2id(win32_event->dwThreadId, win32_event->dwProcessId)));
        ext->regs = (REG_SET *)loc_alloc_zero(sizeof(REG_SET));
        ext->pid = win32_event->dwThreadId;
        ext->handle = OpenThread(THREAD_ALL_ACCESS, FALSE, win32_event->dwThreadId);
        ext->debug_state = debug_state;
        ctx->mem = prs;
        ctx->big_endian = prs->big_endian;
        (ctx->parent = prs)->ref_count++;
        list_add_first(&ctx->cldl, &prs->children);
        link_context(ctx);
        send_context_created_event(ctx);
        debug_event->continue_status = event_win32_context_stopped(ctx);
        break;
    case EXCEPTION_DEBUG_EVENT:
        if (debug_state->state == DEBUG_STATE_PRS_CREATED && win32_event->u.Exception.ExceptionRecord.ExceptionCode == EXCEPTION_BREAKPOINT) {
            if (debug_state->ini_thread_handle != NULL) ResumeThread(debug_state->ini_thread_handle);
            debug_state->attach_callback(0, prs, debug_state->attach_data);
            debug_state->attach_callback = NULL;
            debug_state->attach_data = NULL;
            debug_state->state = DEBUG_STATE_PRS_ATTACHED;
            ext = EXT(ctx = create_context(pid2id(debug_state->main_thread_id, win32_event->dwProcessId)));
            ext->regs = (REG_SET *)loc_alloc_zero(sizeof(REG_SET));
            ext->pid = debug_state->main_thread_id;
            ext->handle = OpenThread(THREAD_ALL_ACCESS, FALSE, debug_state->main_thread_id);
            ext->debug_state = debug_state;
            ctx->mem = prs;
            ctx->big_endian = prs->big_endian;
            (ctx->parent = prs)->ref_count++;
            list_add_first(&ctx->cldl, &prs->children);
            link_context(ctx);
            send_context_created_event(ctx);
            ctx->pending_intercept = 1;
            debug_event->continue_status = event_win32_context_stopped(ctx);
        }
        else if (ctx == NULL || ctx->exiting) {
            debug_event->continue_status = DBG_EXCEPTION_NOT_HANDLED;
        }
        else {
            assert(prs != NULL);
            assert(!ctx->exited);
            assert(!ctx->stopped);
            ext = EXT(ctx);
            memcpy(&ext->suspend_reason, &win32_event->u.Exception, sizeof(EXCEPTION_DEBUG_INFO));
            debug_event->continue_status = event_win32_context_stopped(ctx);
        }
        break;
    case EXIT_THREAD_DEBUG_EVENT:
        assert(prs != NULL);
        if (ctx && !ctx->exited) event_win32_context_exited(ctx);
        if (debug_state->ini_thread_id == win32_event->dwThreadId) {
            debug_state->ini_thread_id = 0;
            debug_state->ini_thread_handle = NULL;
        }
        else if (debug_state->break_thread_id == win32_event->dwThreadId) {
            log_error("CloseHandle", CloseHandle(debug_state->break_thread));
            log_error("VirtualFreeEx", VirtualFreeEx(debug_state->break_process, debug_state->break_thread_code, 0, MEM_RELEASE));
            debug_state->break_thread = NULL;
            debug_state->break_thread_id = 0;
            debug_state->break_thread_code = NULL;
            debug_state->break_process = NULL;
        }
        break;
    case EXIT_PROCESS_DEBUG_EVENT:
        assert(prs != NULL);
        if (ctx && !ctx->exited) event_win32_context_exited(ctx);
        event_win32_context_exited(prs);
        prs = NULL;
        if (debug_state->attach_callback != NULL) {
            int error = set_win32_errno(win32_event->u.ExitProcess.dwExitCode);
            debug_state->attach_callback(error, NULL, debug_state->attach_data);
            debug_state->attach_callback = NULL;
            debug_state->attach_data = NULL;
        }
        break;
    case LOAD_DLL_DEBUG_EVENT:
        assert(prs != NULL);
        debug_state->module_loaded = 1;
        debug_state->module_handle = win32_event->u.LoadDll.hFile;
        debug_state->module_address = (uintptr_t)win32_event->u.LoadDll.lpBaseOfDll;
        send_context_changed_event(prs);
        if (debug_state->module_handle != NULL) {
            log_error("CloseHandle", CloseHandle(debug_state->module_handle));
        }
        debug_state->module_handle = NULL;
        debug_state->module_address = 0;
        debug_state->module_loaded = 0;
        break;
    case UNLOAD_DLL_DEBUG_EVENT:
        assert(prs != NULL);
        debug_state->module_unloaded = 1;
        debug_state->module_address = (uintptr_t)win32_event->u.UnloadDll.lpBaseOfDll;
        send_context_changed_event(prs);
        debug_state->module_address = 0;
        debug_state->module_unloaded = 0;
        break;
    case RIP_EVENT:
        trace(LOG_ALWAYS, "System debugging error: debuggee pid %d, error type %d, error code %d",
            win32_event->dwProcessId, win32_event->u.RipInfo.dwType, win32_event->u.RipInfo.dwError);
        break;
    }
}

static void continue_debug_event(void * args) {
    DebugState * debug_state = (DebugState *)args;

    suspend_threads(debug_state->process_id);
    debug_state->process_suspended = 0;

    trace(LOG_WAITPID, "continue debug event, process id %u", debug_state->process_id);
    log_error("ReleaseSemaphore", SetEvent(debug_state->debug_event_inp));
    log_error("WaitForSingleObject", WaitForSingleObject(debug_state->debug_event_out, INFINITE) != WAIT_FAILED);
}

static void early_debug_event_handler(void * x) {
    DebugEvent * debug_event = (DebugEvent *)x;
    DebugState * debug_state = debug_event->debug_state;
    DEBUG_EVENT * win32_event = &debug_event->win32_event;

    if (win32_event->dwDebugEventCode == EXCEPTION_DEBUG_EVENT) {
        trace(LOG_WAITPID, "%s, process %d, thread %d, code %#lx",
            win32_debug_event_name(win32_event->dwDebugEventCode),
            win32_event->dwProcessId, win32_event->dwThreadId,
            win32_event->u.Exception.ExceptionRecord.ExceptionCode);
    }
    else {
        trace(LOG_WAITPID, "%s, process %d, thread %d",
            win32_debug_event_name(win32_event->dwDebugEventCode),
            win32_event->dwProcessId, win32_event->dwThreadId);
    }

    debug_state->process_suspended = 1;
    debug_event_handler(debug_event);
    post_event(continue_debug_event, debug_state);
}

static void debugger_exit_handler(void * x) {
    DebugState * debug_state = (DebugState *)x;
    Context * prs = context_find_from_pid(debug_state->process_id, 0);

    trace(LOG_WAITPID, "debugger thread %d exited, debuggee pid %d", debug_state->debug_thread_id, debug_state->process_id);

    log_error("WaitForSingleObject", WaitForSingleObject(debug_state->debug_thread, INFINITE) != WAIT_FAILED);
    log_error("CloseHandle", CloseHandle(debug_state->debug_thread));
    log_error("CloseHandle", CloseHandle(debug_state->debug_event_inp));
    log_error("CloseHandle", CloseHandle(debug_state->debug_event_out));

    if (prs != NULL && !prs->exited) event_win32_context_exited(prs);

    loc_free(debug_state);
}

static DWORD WINAPI debugger_thread_func(LPVOID x) {
    DebugState * debug_state = (DebugState *)x;
    DebugEvent debug_event;

    if (DebugActiveProcess(debug_state->process_id) == 0) {
        debug_state->error = GetLastError();
        trace(LOG_ALWAYS, "Can't attach to a process: error %d", debug_state->error);
        ReleaseSemaphore(debug_state->debug_thread_semaphore, 1, 0);
        return 0;
    }

    trace(LOG_WAITPID, "debugger thread %d started", GetCurrentThreadId());
    ReleaseSemaphore(debug_state->debug_thread_semaphore, 1, 0);

    memset(&debug_event, 0, sizeof(debug_event));

    debug_event.debug_state = debug_state;

    for (;;) {
        DEBUG_EVENT * win32_event = &debug_event.win32_event;

        memset(win32_event, 0, sizeof(DEBUG_EVENT));
        if (WaitForDebugEvent(win32_event, INFINITE) == 0) {
            trace(LOG_ALWAYS, "WaitForDebugEvent() error %d", GetLastError());
            break;
        }

        assert(debug_state->process_id == win32_event->dwProcessId);
        debug_event.continue_status = DBG_CONTINUE;

        post_event(early_debug_event_handler, &debug_event);
        WaitForSingleObject(debug_state->debug_event_inp, INFINITE);
        if (ContinueDebugEvent(win32_event->dwProcessId, win32_event->dwThreadId, debug_event.continue_status) == 0) {
            trace(LOG_ALWAYS, "Can't continue debug event: process %d, thread %d: error %d",
                win32_event->dwProcessId, win32_event->dwThreadId, GetLastError());
        }
        SetEvent(debug_state->debug_event_out);

        if (win32_event->dwDebugEventCode == EXIT_PROCESS_DEBUG_EVENT) break;
        if (win32_event->dwDebugEventCode == RIP_EVENT) break;
    }

    post_event(debugger_exit_handler, debug_state);
    return 0;
}

int context_attach(pid_t pid, ContextAttachCallBack * done, void * data, int selfattach) {
    int error = 0;
    DebugState * debug_state = (DebugState *)loc_alloc_zero(sizeof(DebugState));

    assert(done != NULL);
    assert(!selfattach);
    debug_state->process_id = pid;
    debug_state->attach_callback = done;
    debug_state->attach_data = data;

    debug_state->debug_event_inp = CreateEvent(NULL, 0, 0, NULL);
    if (debug_state->debug_event_inp == NULL) error = log_error("CreateEvent", 0);

    if (!error) {
        debug_state->debug_event_out = CreateEvent(NULL, 0, 0, NULL);
        if (debug_state->debug_event_out == NULL) error = log_error("CreateEvent", 0);
    }

    if (!error) {
        debug_state->debug_thread_semaphore = CreateSemaphore(NULL, 0, 1, NULL);
        if (debug_state->debug_thread_semaphore == NULL) error = log_error("CreateSemaphore", 0);
    }

    if (!error) {
        debug_state->debug_thread = CreateThread(NULL, 0, debugger_thread_func, debug_state, 0, &debug_state->debug_thread_id);
        if (debug_state->debug_thread == NULL) error = log_error("CreateThread", 0);
    }

    if (!error) {
        error = log_error("WaitForSingleObject", WaitForSingleObject(debug_state->debug_thread_semaphore, INFINITE) != WAIT_FAILED);
    }

    if (!error) {
        error = log_error("CloseHandle", CloseHandle(debug_state->debug_thread_semaphore));
        debug_state->debug_thread_semaphore = NULL;
    }

    if (!error) {
        error = set_win32_errno(debug_state->error);
    }

    if (error) {
        if (debug_state->debug_thread) log_error("WaitForSingleObject", WaitForSingleObject(debug_state->debug_thread, INFINITE) != WAIT_FAILED);
        if (debug_state->debug_thread) log_error("CloseHandle", CloseHandle(debug_state->debug_thread));
        if (debug_state->debug_event_inp) log_error("CloseHandle", CloseHandle(debug_state->debug_event_inp));
        if (debug_state->debug_event_out) log_error("CloseHandle", CloseHandle(debug_state->debug_event_out));
        if (debug_state->debug_thread_semaphore) log_error("CloseHandle", CloseHandle(debug_state->debug_thread_semaphore));
        loc_free(debug_state);
        errno = error;
        return -1;
    }

    add_waitpid_process(pid);
    return 0;
}

int context_has_state(Context * ctx) {
    return ctx != NULL && ctx->parent != NULL;
}

int context_stop(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    ContextExtensionWin32 * prs = EXT(ctx->parent);

    trace(LOG_CONTEXT, "context:%s suspending ctx %#lx id %s",
        ctx->pending_intercept ? "" : " temporary", ctx, ctx->id);
    assert(context_has_state(ctx));
    assert(!ctx->stopped);
    assert(!ctx->exited);
    if (!prs->debug_state->process_suspended && !prs->debug_state->break_posted) {
        context_lock(ctx->parent);
        post_event_with_delay(break_process_event, ctx->parent, 10000);
        prs->debug_state->break_posted = 1;
    }
    ext->stop_pending = 1;
    return 0;
}

int context_continue(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);

    assert(is_dispatch_thread());
    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(!ctx->exited);

    if (skip_breakpoint(ctx, 0)) return 0;

    trace(LOG_CONTEXT, "context: resuming ctx %#lx, id %s", ctx, ctx->id);
    if (ext->trace_flag) {
        get_registers(ctx);
        ext->regs->EFlags &= ~0x100;
        ext->regs_dirty = 1;
    }
    return win32_resume(ctx);
}

int context_single_step(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);

    assert(is_dispatch_thread());
    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(!ctx->exited);

    if (skip_breakpoint(ctx, 1)) return 0;

    trace(LOG_CONTEXT, "context: single step ctx %#lx, id %s", ctx, ctx->id);
    if (!ext->trace_flag) {
        get_registers(ctx);
        ext->regs->EFlags |= 0x100;
        ext->regs_dirty = 1;
    }
    return win32_resume(ctx);
}

int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    ContextExtensionWin32 * ext = EXT(ctx = ctx->mem);
    SIZE_T bcnt = 0;

    trace(LOG_CONTEXT, "context: read memory ctx %#lx, id %s, address %#lx, size %d",
        ctx, ctx->id, address, (int)size);
    assert(is_dispatch_thread());
    if (ReadProcessMemory(ext->handle, (LPCVOID)address, buf, size, &bcnt) == 0 || bcnt != size) {
        errno = log_error("ReadProcessMemory", 0);
        return -1;
    }
    check_breakpoints_on_memory_read(ctx, address, buf, size);
    return 0;
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    ContextExtensionWin32 * ext = EXT(ctx = ctx->mem);
    SIZE_T bcnt = 0;

    trace(LOG_CONTEXT, "context: write memory ctx %#lx, id %s, address %#lx, size %d",
        ctx, ctx->id, address, (int)size);
    assert(is_dispatch_thread());
    ctx = ctx->mem;
    check_breakpoints_on_memory_write(ctx, address, buf, size);
    if (WriteProcessMemory(ext->handle, (LPVOID)address, buf, size, &bcnt) == 0 || bcnt != size) {
        DWORD err = GetLastError();
        if (err == ERROR_ACCESS_DENIED) errno = set_win32_errno(err);
        else errno = log_error("WriteProcessMemory", 0);
        return -1;
    }
    if (FlushInstructionCache(ext->handle, (LPCVOID)address, size) == 0) {
        errno = log_error("FlushInstructionCache", 0);
        return -1;
    }
    return 0;
}

int context_write_reg(Context * ctx, RegisterDefinition * def, unsigned offs, unsigned size, void * buf) {
    ContextExtensionWin32 * ext = EXT(ctx);

    assert(is_dispatch_thread());
    assert(offs + size <= def->size);

    get_registers(ctx);
    if (ext->regs_error) {
        set_error_report_errno(ext->regs_error);
        return -1;
    }
    memcpy((uint8_t *)ext->regs + def->offset + offs, buf, size);
    ext->regs_dirty = 1;
    return 0;
}

int context_read_reg(Context * ctx, RegisterDefinition * def, unsigned offs, unsigned size, void * buf) {
    ContextExtensionWin32 * ext = EXT(ctx);

    assert(is_dispatch_thread());
    assert(offs + size <= def->size);

    get_registers(ctx);
    if (ext->regs_error) {
        set_error_report_errno(ext->regs_error);
        return -1;
    }
    memcpy(buf, (uint8_t *)ext->regs + def->offset + offs, size);
    return 0;
}

unsigned context_word_size(Context * ctx) {
    return sizeof(void *);
}

HANDLE get_context_handle(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->handle;
}

HANDLE get_context_file_handle(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->file_handle;
}

DWORD64 get_context_base_address(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->base_address;
}

HANDLE get_context_module_handle(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->debug_state->module_handle;
}

DWORD64 get_context_module_address(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->debug_state->module_address;
}

int is_context_module_loaded(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->debug_state->module_loaded;
}

int is_context_module_unloaded(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->debug_state->module_unloaded;
}

void add_context_exception_handler(ContextExceptionHandler * h) {
    assert(exception_handler_cnt < MAX_EXCEPTION_HANDLERS);
    exception_handlers[exception_handler_cnt++] = h;
}

void init_contexts_sys_dep(void) {
    context_extension_offset = context_extension(sizeof(ContextExtensionWin32));
    ini_context_pid_hash();
    memset(&os_version, 0, sizeof(os_version));
    os_version.dwOSVersionInfoSize = sizeof(os_version);
    GetVersionEx((OSVERSIONINFO *)&os_version);
}

#endif  /* if ENABLE_DebugContext */
#endif /* WIN32 */
