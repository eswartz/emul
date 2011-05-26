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
#include <services/memorymap.h>
#include <services/runctrl.h>
#include <system/Windows/context-win32.h>
#include <system/Windows/regset.h>
#include <system/Windows/windbgcache.h>

#if !defined(USE_HW_BPS)
#  define USE_HW_BPS 1
#endif
#if USE_HW_BPS
#  define MAX_HW_BPS 4
#endif

typedef struct ContextExtensionWin32 {
    pid_t               pid;
    HANDLE              handle;
    DEBUG_EVENT         debug_event;
    EXCEPTION_DEBUG_INFO suspend_reason;
    int                 stop_pending;
    int                 start_pending;
    REG_SET *           regs;               /* copy of context registers, updated when context stops */
    ErrorReport *       regs_error;         /* if not NULL, 'regs' is invalid */
    int                 regs_dirty;         /* if not 0, 'regs' is modified and needs to be saved before context is continued */
    int                 trace_flag;
    uint8_t             step_opcodes[4];
    SIZE_T              step_opcodes_len;
    ContextAddress      step_opcodes_addr;
    struct DebugState * debug_state;
#if USE_HW_BPS
    ContextBreakpoint * triggered_hw_bps[MAX_HW_BPS + 1];
    unsigned            hw_bps_regs_generation;
    DWORD               skip_hw_bp_addr;
#endif
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
    int                 reporting_debug_event;
    int                 break_posted;
    HANDLE              break_thread;
    LPVOID              break_thread_code;
    DWORD               break_thread_id;
    HANDLE              file_handle;
    DWORD64             base_address;
    HANDLE              module_handle;
    DWORD64             module_address;
    ContextAttachCallBack * attach_callback;
    void *              attach_data;
#if USE_HW_BPS
    int                 ok_to_use_hw_bp;    /* NtContinue() changes Dr6 and Dr7, so HW breakpoints should be disabled until NtContinue() is done */
    ContextBreakpoint * hw_bps[MAX_HW_BPS];
    unsigned            hw_bps_generation;
#endif
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

const char * context_suspend_reason(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    DWORD exception_code = ext->suspend_reason.ExceptionRecord.ExceptionCode;
    static char buf[64];

    if (exception_code == 0) return REASON_USER_REQUEST;
    if (ext->suspend_reason.dwFirstChance) {
        if (exception_code == EXCEPTION_SINGLE_STEP) return REASON_STEP;
        if (exception_code == EXCEPTION_BREAKPOINT) return "Break Instruction";
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
#if USE_HW_BPS
    ext->regs->ContextFlags |= CONTEXT_DEBUG_REGISTERS;
#endif
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
    DebugState * debug_state = EXT(ctx->mem)->debug_state;
    ContextAddress exception_addr = (ContextAddress)ext->suspend_reason.ExceptionRecord.ExceptionAddress;
    DWORD exception_code = ext->suspend_reason.ExceptionRecord.ExceptionCode;
    DWORD continue_status = DBG_CONTINUE;

    assert(is_dispatch_thread());
    assert(!ctx->exited);
    assert(!ctx->stopped);
    assert(ext->handle != NULL);
    assert(ctx->parent != NULL);

    ext->stop_pending = 0;
    ext->start_pending = 0;

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
    ctx->stopped_by_cb = NULL;
    if (exception_code == 0) {
        ctx->stopped_by_exception = 0;
    }
    else if (ext->suspend_reason.dwFirstChance) {
        ctx->stopped_by_exception = 0;
        switch (exception_code) {
        case EXCEPTION_SINGLE_STEP:
            get_registers(ctx);
            if (!ext->regs_error) {
#if USE_HW_BPS
                if (ext->regs->Eip != ext->skip_hw_bp_addr) ext->skip_hw_bp_addr = 0;
                if (ext->regs->Dr6 & 0xfu) {
                    int i, j = 0;
                    for (i = 0; i < MAX_HW_BPS; i++) {
                        if (ext->regs->Dr6 & (1u << i)) {
                            ContextBreakpoint * bp = debug_state->hw_bps[i];
                            if (bp == NULL) continue;
                            if (bp->address == ext->regs->Eip && (bp->access_types & CTX_BP_ACCESS_INSTRUCTION)) {
                                ext->skip_hw_bp_addr = ext->regs->Eip;
                            }
                            ctx->stopped_by_cb = ext->triggered_hw_bps;
                            ctx->stopped_by_cb[j++] = bp;
                            ctx->stopped_by_cb[j] = NULL;
                        }
                    }
                }
#endif
                if (ext->step_opcodes_len > 0 && ext->step_opcodes[0] == 0x9c && ext->step_opcodes_addr != ext->regs->Eip) {
                    /* PUSHF instruction: need to clear trace flag from top of the stack */
                    SIZE_T bcnt = 0;
                    ContextAddress buf = 0;
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
            if ((!ctx->stopped_by_cb && ext->step_opcodes_len == 0) || ext->regs_error) {
                continue_status = DBG_EXCEPTION_NOT_HANDLED;
            }
            ext->step_opcodes_len = 0;
            ext->step_opcodes_addr = 0;
            break;
        case EXCEPTION_BREAKPOINT:
            get_registers(ctx);
            if (!ext->regs_error) {
                if (is_breakpoint_address(ctx, exception_addr)) {
                    ext->regs->Eip = exception_addr;
                    ext->regs_dirty = 1;
                    ctx->stopped_by_bp = 1;
#if USE_HW_BPS
                    if (!debug_state->ok_to_use_hw_bp) {
                        debug_state->ok_to_use_hw_bp = 1;
                        send_context_changed_event(ctx->mem);
                    }
#endif
                }
                else {
                    ext->regs->Eip = exception_addr;
                    ext->regs_dirty = 1;
                }
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
    if (ext->debug_state != NULL && ext->debug_state->file_handle != NULL) {
        log_error("CloseHandle", CloseHandle(ext->debug_state->file_handle));
        ext->debug_state->file_handle = NULL;
    }
    ext->debug_state = NULL;
    context_unlock(ctx);
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
                if (debug_state->break_thread_code == NULL) {
                    debug_state->break_thread_code = VirtualAllocEx(ext->handle,
                        NULL, buf_size, MEM_COMMIT, PAGE_EXECUTE);
                    error = log_error("VirtualAllocEx", debug_state->break_thread_code != NULL);
                }

                if (!error) error = log_error("WriteProcessMemory", WriteProcessMemory(ext->handle,
                    debug_state->break_thread_code, (LPCVOID)remote_thread_func, buf_size, &size) && size == buf_size);

                if (!error) error = log_error("CreateRemoteThread", (debug_state->break_thread = CreateRemoteThread(ext->handle,
                    0, 0, (DWORD (WINAPI*)(LPVOID))debug_state->break_thread_code, NULL, 0, &debug_state->break_thread_id)) != NULL);

                if (error) {
                    debug_state->break_thread = NULL;
                    debug_state->break_thread_id = 0;
                }
            }
        }
        debug_state->break_posted = 0;
    }
    context_unlock(ctx);
}

static int win32_resume(Context * ctx, int step) {
    Context * prs = ctx->parent;
    ContextExtensionWin32 * ext = EXT(ctx);
    ContextExtensionWin32 * prs_ext = EXT(prs);
    DebugState * debug_state = prs_ext->debug_state;

    assert(ctx->stopped);
    assert(!ctx->exited);

    if (debug_state->reporting_debug_event) {
        debug_state->reporting_debug_event++;
    }

#if USE_HW_BPS

    if (ext->skip_hw_bp_addr == 0 && skip_breakpoint(ctx, step)) return 0;

    /* Update debug registers */
    if (ext->skip_hw_bp_addr != 0 || ext->hw_bps_regs_generation != debug_state->hw_bps_generation) {
        int i;
        DWORD Dr7 = 0;
        int step_over_hw_bp = 0;

        get_registers(ctx);
        if (ext->regs_error) {
            errno = set_error_report_errno(ext->regs_error);
            return -1;
        }
        Dr7 = ext->regs->Dr7;
        for (i = 0; i < MAX_HW_BPS; i++) {
            ContextBreakpoint * bp = debug_state->hw_bps[i];
            if (bp != NULL &&
                    ext->skip_hw_bp_addr == bp->address &&
                    bp->access_types == CTX_BP_ACCESS_INSTRUCTION) {
                /* Skipping the breakpoint */
                step_over_hw_bp = 1;
                bp = NULL;
            }
            Dr7 &= ~(3u << (i * 2));
            if (bp != NULL) {
                switch (i) {
                case 0:
                    if (ext->regs->Dr0 != bp->address) {
                        ext->regs->Dr0 = bp->address;
                        ext->regs_dirty = 1;
                    }
                    break;
                case 1:
                    if (ext->regs->Dr1 != bp->address) {
                        ext->regs->Dr1 = bp->address;
                        ext->regs_dirty = 1;
                    }
                    break;
                case 2:
                    if (ext->regs->Dr2 != bp->address) {
                        ext->regs->Dr2 = bp->address;
                        ext->regs_dirty = 1;
                    }
                    break;
                case 3:
                    if (ext->regs->Dr3 != bp->address) {
                        ext->regs->Dr3 = bp->address;
                        ext->regs_dirty = 1;
                    }
                    break;
                }
                Dr7 |= 1u << (i * 2);
                if (bp->access_types == CTX_BP_ACCESS_INSTRUCTION) {
                    Dr7 &= ~(3u << (i * 4 + 16));
                }
                else if (bp->access_types == CTX_BP_ACCESS_DATA_WRITE) {
                    Dr7 &= ~(3u << (i * 4 + 16));
                    Dr7 |= 1u << (i * 4 + 16);
                }
                else if (bp->access_types == (CTX_BP_ACCESS_DATA_READ | CTX_BP_ACCESS_DATA_WRITE)) {
                    Dr7 |= 3u << (i * 4 + 16);
                }
                else {
                    errno = set_errno(ERR_UNSUPPORTED, "Invalid hardware breakpoint: unsupported access mode");
                    return -1;
                }
                if (bp->length == 1) {
                    Dr7 &= ~(3u << (i * 4 + 18));
                }
                else if (bp->length == 2) {
                    Dr7 &= ~(3u << (i * 4 + 18));
                    Dr7 |= 1u << (i * 4 + 18);
                }
                else if (bp->length == 4) {
                    Dr7 |= 3u << (i * 4 + 18);
                }
                else if (bp->length == 8) {
                    Dr7 &= ~(3u << (i * 4 + 18));
                    Dr7 |= 2u << (i * 4 + 18);
                }
                else {
                    errno = set_errno(ERR_UNSUPPORTED, "Invalid hardware breakpoint: unsupported length");
                    return -1;
                }
            }
        }
        if (ext->regs->Dr7 != Dr7) {
            ext->regs->Dr7 = Dr7;
            ext->regs_dirty = 1;
        }
        ext->hw_bps_regs_generation = debug_state->hw_bps_generation;
        if (step_over_hw_bp) {
            step = 1;
            ext->hw_bps_regs_generation--;
        }
        else {
            ext->skip_hw_bp_addr = 0;
        }
    }

#else

    if (skip_breakpoint(ctx, step)) return 0;

#endif

    /* Update CPU trace flag */
    if (!step && ext->trace_flag) {
        get_registers(ctx);
        ext->regs->EFlags &= ~0x100;
        ext->regs_dirty = 1;
    }
    else if (step && !ext->trace_flag) {
        get_registers(ctx);
        ext->regs->EFlags |= 0x100;
        ext->regs_dirty = 1;
    }

    /* Flash registers if dirty */
    if (ext->regs_dirty) {
        assert(ext->regs->ContextFlags);
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

    if (ext->trace_flag) {
        get_registers(ctx);
        if (ext->regs_error) {
            set_error_report_errno(ext->regs_error);
            return -1;
        }
        ext->step_opcodes_addr = ext->regs->Eip;
        if (!ReadProcessMemory(prs_ext->handle, (LPCVOID)ext->regs->Eip, &ext->step_opcodes,
                sizeof(ext->step_opcodes), &ext->step_opcodes_len) || ext->step_opcodes_len == 0) {
            errno = log_error("ReadProcessMemory", 0);
            return -1;
        }
    }
    if (debug_state->reporting_debug_event) {
        ext->start_pending = 1;
    }
    else {
        for (;;) {
            DWORD cnt = ResumeThread(ext->handle);
            if (cnt == (DWORD)-1) {
                errno = log_error("ResumeThread", 0);
                return -1;
            }
            if (cnt <= 1) break;
        }
    }

    event_win32_context_started(ctx);
    return 0;
}

static int win32_terminate(Context * ctx) {
    LINK * l;
    ContextExtensionWin32 * ext = EXT(ctx);
    DebugState * debug_state = ext->debug_state;

    if (debug_state->reporting_debug_event) {
        debug_state->reporting_debug_event++;
    }

    trace(LOG_CONTEXT, "context: terminating process %#lx, id %s", ctx, ctx->id);
    if (!ctx->exiting) {
        if (!TerminateProcess(ext->handle, 1)) {
            errno = log_error("TerminateProcess", 0);
            return -1;
        }
        ctx->exiting = 1;
        for (l = ctx->children.next; l != &ctx->children; l = l->next) {
            Context * c = cldl2ctxp(l);
            if (!c->stopped) continue;
            event_win32_context_started(c);
            c->exiting = 1;
        }
    }

    return 0;
}

static void debug_event_handler(DebugEvent * debug_event) {
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
            debug_state->file_handle = win32_event->u.CreateProcessInfo.hFile;
            debug_state->base_address = (uintptr_t)win32_event->u.CreateProcessInfo.lpBaseOfImage;
            assert(prs == NULL);
            assert(ctx == NULL);
            ext = EXT(prs = create_context(pid2id(win32_event->dwProcessId, 0)));
            prs->mem = prs;
            prs->mem_access |= MEM_ACCESS_INSTRUCTION;
            prs->mem_access |= MEM_ACCESS_DATA;
            prs->mem_access |= MEM_ACCESS_USER;
            prs->big_endian = big_endian_host();
            ext->pid = win32_event->dwProcessId;
            ext->handle = win32_event->u.CreateProcessInfo.hProcess;
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
        ext->debug_event = *win32_event;
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
            ext->debug_event = *win32_event;
        }
        else if (ctx == NULL || ctx->exiting) {
            /* Does not work as expected: debug_event->continue_status = DBG_EXCEPTION_NOT_HANDLED; */
        }
        else {
            assert(prs != NULL);
            assert(!ctx->exited);
            if (ctx->stopped) {
                DWORD exception_code = win32_event->u.Exception.ExceptionRecord.ExceptionCode;
#if USE_HW_BPS
                if (exception_code == EXCEPTION_SINGLE_STEP && win32_event->u.Exception.dwFirstChance) {
                    /* This event appears to be caused by a hardware breakpoint.
                     * It is safe to ignore the event - the breakpoint will be triggered again
                     * when the context resumed. */
                    debug_event->continue_status = DBG_CONTINUE;
                    break;
                }
#endif
                trace(LOG_ALWAYS, "context: already stopped, id %s, exception 0x%08x", ctx->id, exception_code);
                send_context_started_event(ctx);
            }
            ext = EXT(ctx);
            memcpy(&ext->suspend_reason, &win32_event->u.Exception, sizeof(EXCEPTION_DEBUG_INFO));
            debug_event->continue_status = event_win32_context_stopped(ctx);
            ext->debug_event = *win32_event;
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
            ext = EXT(prs);
            log_error("CloseHandle", CloseHandle(debug_state->break_thread));
            debug_state->break_thread = NULL;
            debug_state->break_thread_id = 0;
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
        debug_state->module_handle = win32_event->u.LoadDll.hFile;
        debug_state->module_address = (uintptr_t)win32_event->u.LoadDll.lpBaseOfDll;
        memory_map_event_module_loaded(prs);
        if (debug_state->module_handle != NULL) {
            log_error("CloseHandle", CloseHandle(debug_state->module_handle));
        }
        debug_state->module_handle = NULL;
        debug_state->module_address = 0;
        break;
    case UNLOAD_DLL_DEBUG_EVENT:
        assert(prs != NULL);
        debug_state->module_address = (uintptr_t)win32_event->u.UnloadDll.lpBaseOfDll;
        memory_map_event_module_unloaded(prs);
        debug_state->module_address = 0;
        break;
    case RIP_EVENT:
        trace(LOG_ALWAYS, "System debugging error: debuggee pid %d, error type %d, error code %d",
            win32_event->dwProcessId, win32_event->u.RipInfo.dwType, win32_event->u.RipInfo.dwError);
        break;
    }
}

static void continue_debug_event(void * args) {
    DebugEvent * debug_event = (DebugEvent *)args;
    DebugState * debug_state = debug_event->debug_state;
    Context * prs = context_find_from_pid(debug_state->process_id, 0);

    assert(debug_state->reporting_debug_event);
    if (debug_state->reporting_debug_event > 1) {
        debug_state->reporting_debug_event = 1;
        post_event(continue_debug_event, debug_event);
        return;
    }

    trace(LOG_WAITPID, "continue debug event 1, process id %u", debug_state->process_id);

    if (prs != NULL && !prs->exited) {
        LINK * l;
        for (l = prs->children.next; l != &prs->children; l = l->next) {
            Context * ctx = cldl2ctxp(l);
            ContextExtensionWin32 * ext = EXT(ctx);
            if (ctx->stopped || ctx->exited) {
                ext->stop_pending = 0;
                ext->start_pending = 0;
                continue;
            }
            if (ext->stop_pending) {
                memset(&ext->suspend_reason, 0, sizeof(ext->suspend_reason));
                event_win32_context_stopped(ctx);
                ext->debug_event = debug_event->win32_event;
            }
            if (ext->start_pending) {
                for (;;) {
                    DWORD cnt = ResumeThread(ext->handle);
                    if (cnt <= 1) break;
                }
                ext->start_pending = 0;
            }
        }
    }

    trace(LOG_WAITPID, "continue debug event, process id %u", debug_state->process_id);
    log_error("ReleaseSemaphore", SetEvent(debug_state->debug_event_inp));
    log_error("WaitForSingleObject", WaitForSingleObject(debug_state->debug_event_out, INFINITE) != WAIT_FAILED);
    debug_state->reporting_debug_event = 0;

    if (prs != NULL && !prs->exited) {
        LINK * l;
        for (l = prs->children.next; l != &prs->children; l = l->next) {
            Context * ctx = cldl2ctxp(l);
            ContextExtensionWin32 * ext = EXT(ctx);
            if (ext->start_pending) {
                for (;;) {
                    DWORD cnt = ResumeThread(ext->handle);
                    if (cnt <= 1) break;
                }
                ext->start_pending = 0;
            }
        }
    }

    log_error("ReleaseSemaphore", SetEvent(debug_state->debug_event_inp));
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

    debug_state->reporting_debug_event = 1;
    debug_event_handler(debug_event);
    post_event(continue_debug_event, debug_event);
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
        WaitForSingleObject(debug_state->debug_event_inp, INFINITE);

        if (win32_event->dwDebugEventCode == EXIT_PROCESS_DEBUG_EVENT) break;
        if (win32_event->dwDebugEventCode == RIP_EVENT) break;
    }

    post_event(debugger_exit_handler, debug_state);
    return 0;
}

int context_attach(pid_t pid, ContextAttachCallBack * done, void * data, int mode) {
    int error = 0;
    DebugState * debug_state = (DebugState *)loc_alloc_zero(sizeof(DebugState));

    assert(done != NULL);
    assert((mode & CONTEXT_ATTACH_SELF) == 0);
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
    DebugState * debug_state = EXT(ctx->parent)->debug_state;

    trace(LOG_CONTEXT, "context:%s suspending ctx %#lx id %s",
        ctx->pending_intercept ? "" : " temporary", ctx, ctx->id);
    assert(context_has_state(ctx));
    assert(!ctx->stopped);
    assert(!ctx->exited);
    if (debug_state->reporting_debug_event) {
        debug_state->reporting_debug_event++;
    }
    else if (!debug_state->break_posted) {
        context_lock(ctx->parent);
        post_event_with_delay(break_process_event, ctx->parent, 10000);
        debug_state->break_posted = 1;
    }
    ext->stop_pending = 1;
    return 0;
}

int context_continue(Context * ctx) {
    assert(is_dispatch_thread());
    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(!ctx->exited);

    trace(LOG_CONTEXT, "context: resuming ctx %#lx, id %s", ctx, ctx->id);
    return win32_resume(ctx, 0);
}

int context_single_step(Context * ctx) {
    assert(is_dispatch_thread());
    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(!ctx->exited);

    trace(LOG_CONTEXT, "context: single step ctx %#lx, id %s", ctx, ctx->id);
    return win32_resume(ctx, 1);
}

static int context_terminate(Context * ctx) {
    assert(is_dispatch_thread());
    assert(!context_has_state(ctx));
    assert(!ctx->exited);

    trace(LOG_CONTEXT, "context: terminate ctx %#lx, id %s", ctx, ctx->id);
    return win32_terminate(ctx);
}

int context_resume(Context * ctx, int mode, ContextAddress range_start, ContextAddress range_end) {
    switch (mode) {
    case RM_RESUME:
        return context_continue(ctx);
    case RM_STEP_INTO:
        return context_single_step(ctx);
    case RM_TERMINATE:
        return context_terminate(ctx);
    }
    errno = ERR_UNSUPPORTED;
    return -1;
}

int context_can_resume(Context * ctx, int mode) {
    switch (mode) {
    case RM_RESUME:
        return 1;
    case RM_STEP_INTO:
        return context_has_state(ctx);
    case RM_TERMINATE:
        return ctx != NULL && ctx->parent == NULL;
    }
    return 0;
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
    return check_breakpoints_on_memory_read(ctx, address, buf, size);
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    ContextExtensionWin32 * ext = EXT(ctx = ctx->mem);
    SIZE_T bcnt = 0;

    trace(LOG_CONTEXT, "context: write memory ctx %#lx, id %s, address %#lx, size %d",
        ctx, ctx->id, address, (int)size);
    assert(is_dispatch_thread());
    ctx = ctx->mem;
    if (check_breakpoints_on_memory_write(ctx, address, buf, size) < 0) return -1;
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

int context_get_canonical_addr(Context * ctx, ContextAddress addr,
        Context ** canonical_ctx, ContextAddress * canonical_addr,
        ContextAddress * block_addr, ContextAddress * block_size) {
    /* Direct mapping, page size is irrelevant */
    ContextAddress page_size = 0x100000;
    assert(is_dispatch_thread());
    *canonical_ctx = ctx->mem;
    if (canonical_addr != NULL) *canonical_addr = addr;
    if (block_addr != NULL) *block_addr = addr & ~(page_size - 1);
    if (block_size != NULL) *block_size = page_size;
    return 0;
}

Context * context_get_group(Context * ctx, int group) {
    static Context * cpu_group = NULL;
    switch (group) {
    case CONTEXT_GROUP_INTERCEPT:
        return ctx;
    case CONTEXT_GROUP_CPU:
        if (cpu_group == NULL) cpu_group = create_context("CPU");
        return cpu_group;
    }
    return ctx->mem;
}

int context_get_supported_bp_access_types(Context * ctx) {
#if USE_HW_BPS
    if (ctx->mem == ctx) return
        CTX_BP_ACCESS_DATA_READ |
        CTX_BP_ACCESS_DATA_WRITE |
        CTX_BP_ACCESS_INSTRUCTION;
#endif
    return 0;
}

int context_plant_breakpoint(ContextBreakpoint * bp) {
#if USE_HW_BPS
    int i;
    Context * ctx = bp->ctx;
    assert(bp->access_types);
    if (ctx->mem == ctx) {
        ContextExtensionWin32 * ext = EXT(ctx);
        DebugState * debug_state = ext->debug_state;
        if (debug_state->ok_to_use_hw_bp && bp->length <= 8 && ((1u << bp->length) & 0x116u)) {
            if (bp->access_types == CTX_BP_ACCESS_INSTRUCTION) {
                /* Don't use more then 2 HW slots for regular instruction breakpoints */
                int cnt = 0;
                for (i = 0; i < MAX_HW_BPS; i++) {
                    assert(debug_state->hw_bps[i] != bp);
                    if (debug_state->hw_bps[i] == NULL) continue;
                    if (debug_state->hw_bps[i]->access_types != CTX_BP_ACCESS_INSTRUCTION) continue;
                    cnt++;
                }
                if (cnt >= MAX_HW_BPS / 2) {
                    errno = ERR_UNSUPPORTED;
                    return -1;
                }
            }
            else if (bp->access_types != CTX_BP_ACCESS_DATA_WRITE &&
                        bp->access_types != (CTX_BP_ACCESS_DATA_READ | CTX_BP_ACCESS_DATA_WRITE)) {
                errno = ERR_UNSUPPORTED;
                return -1;
            }
            for (i = 0; i < MAX_HW_BPS; i++) {
                if (debug_state->hw_bps[i] == NULL || debug_state->hw_bps[i] == bp) {
                    debug_state->hw_bps[i] = bp;
                    debug_state->hw_bps_generation++;
                    return 0;
                }
            }
        }
    }
#endif
    errno = ERR_UNSUPPORTED;
    return -1;
}

int context_unplant_breakpoint(ContextBreakpoint * bp) {
#if USE_HW_BPS
    int i;
    Context * ctx = bp->ctx;
    if (ctx->mem == ctx && !ctx->exited) {
        ContextExtensionWin32 * ext = EXT(ctx);
        DebugState * debug_state = ext->debug_state;
        for (i = 0; i < MAX_HW_BPS; i++) {
            if (debug_state->hw_bps[i] == bp) {
                debug_state->hw_bps[i] = NULL;
                debug_state->hw_bps_generation++;
            }
        }
    }
#endif
    return 0;
}

#if defined(_MSC_VER)

static void add_map_region(MemoryMap * map, DWORD64 addr, ULONG size, char * file) {
    MemoryRegion * r = NULL;
    if (map->region_cnt >= map->region_max) {
        map->region_max += 8;
        map->regions = (MemoryRegion *)loc_realloc(map->regions, sizeof(MemoryRegion) * map->region_max);
    }
    r = map->regions + map->region_cnt++;
    memset(r, 0, sizeof(MemoryRegion));
    r->addr = (ContextAddress)addr;
    r->size = (ContextAddress)size;
    r->file_name = loc_strdup(file);
}

static BOOL CALLBACK modules_callback(PCWSTR ModuleName, DWORD64 ModuleBase, ULONG ModuleSize, PVOID UserContext) {
    MemoryMap * map = (MemoryMap *)UserContext;
    static char * fnm_buf = NULL;
    static int fnm_max = 0;
    int fnm_len = 0;
    int fnm_err = 0;

    if (fnm_buf == NULL) {
        fnm_max = 256;
        fnm_buf = (char *)loc_alloc(fnm_max);
    }
    for (;;) {
        fnm_len = WideCharToMultiByte(CP_UTF8, 0, ModuleName, -1, fnm_buf, fnm_max - 1, NULL, NULL);
        if (fnm_len != 0) break;
        fnm_err = GetLastError();
        if (fnm_err != ERROR_INSUFFICIENT_BUFFER) {
            set_win32_errno(fnm_err);
            trace(LOG_ALWAYS, "Can't get module name: %s", errno_to_str(errno));
            return TRUE;
        }
        fnm_max *= 2;
        fnm_buf = (char *)loc_realloc(fnm_buf, fnm_max);
    }
    fnm_buf[fnm_len] = 0;

    add_map_region(map, ModuleBase, ModuleSize, fnm_buf);

    return TRUE;
}

#endif

int context_get_memory_map(Context * ctx, MemoryMap * map) {
    ctx = ctx->mem;
    assert(!ctx->exited);
#if defined(_MSC_VER)
    {
        ContextExtensionWin32 * ext = EXT(ctx);
        if (!EnumerateLoadedModulesW64(ext->handle, modules_callback, map)) {
            set_win32_errno(GetLastError());
            return -1;
        }
    }
#endif
    return 0;
}

HANDLE get_context_handle(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->handle;
}

HANDLE get_context_file_handle(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->debug_state->file_handle;
}

DWORD64 get_context_base_address(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->debug_state->base_address;
}

HANDLE get_context_module_handle(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->debug_state->module_handle;
}

DWORD64 get_context_module_address(Context * ctx) {
    ContextExtensionWin32 * ext = EXT(ctx);
    return ext->debug_state->module_address;
}

void add_context_exception_handler(ContextExceptionHandler * h) {
    assert(exception_handler_cnt < MAX_EXCEPTION_HANDLERS);
    exception_handlers[exception_handler_cnt++] = h;
}

static void eventpoint_at_main(Context * ctx, void * args) {
    suspend_debug_context(ctx);
}

void init_contexts_sys_dep(void) {
    context_extension_offset = context_extension(sizeof(ContextExtensionWin32));
    ini_context_pid_hash();
    memset(&os_version, 0, sizeof(os_version));
    os_version.dwOSVersionInfoSize = sizeof(os_version);
    GetVersionEx((OSVERSIONINFO *)&os_version);
    create_eventpoint("main", NULL, eventpoint_at_main, NULL);
}

#endif  /* if ENABLE_DebugContext */
#endif /* WIN32 */
