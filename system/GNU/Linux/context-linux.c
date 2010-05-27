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

#if defined(__linux__)

#if ENABLE_DebugContext && !ENABLE_ContextProxy

#include <stdlib.h>
#include <assert.h>
#include <errno.h>
#include <signal.h>
#include <sched.h>
#include <asm/unistd.h>
#include <sys/ptrace.h>
#include <framework/context.h>
#include <framework/events.h>
#include <framework/errors.h>
#include <framework/trace.h>
#include <framework/myalloc.h>
#include <framework/waitpid.h>
#include <framework/signames.h>
#include <services/breakpoints.h>
#include <services/expressions.h>
#include <services/memorymap.h>
#include <services/tcf_elf.h>
#include <system/GNU/Linux/regset.h>

#if !defined(PTRACE_SETOPTIONS)
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
#endif

#define USE_ESRCH_WORKAROUND    1
#define USE_PTRACE_SYSCALL      0

static const int PTRACE_FLAGS =
#if USE_PTRACE_SYSCALL
      PTRACE_O_TRACESYSGOOD |
#endif
      PTRACE_O_TRACEFORK |
      PTRACE_O_TRACEVFORK |
      PTRACE_O_TRACECLONE |
      PTRACE_O_TRACEEXEC |
      PTRACE_O_TRACEVFORKDONE |
      PTRACE_O_TRACEEXIT;

/* TODO: when inferior forks, the new process inherits breakpoints - need to account for that in BP service */

typedef struct ContextExtensionLinux {
    pid_t                   pid;
    ContextAttachCallBack * attach_callback;
    void *                  attach_data;
    int                     ptrace_flags;
    int                     ptrace_event;
    int                     syscall_enter;
    int                     syscall_exit;
    int                     syscall_id;
    ContextAddress          syscall_pc;
    ContextAddress          loader_state;
    int                     end_of_step;
    REG_SET *               regs;               /* copy of context registers, updated when context stops */
    ErrorReport *           regs_error;         /* if not NULL, 'regs' is invalid */
    int                     regs_dirty;         /* if not 0, 'regs' is modified and needs to be saved before context is continued */
} ContextExtensionLinux;

static size_t context_extension_offset = 0;

#define EXT(ctx) ((ContextExtensionLinux *)((char *)(ctx) + context_extension_offset))

#include <system/pid-hash.h>

static LINK pending_list;

static const char * event_name(int event) {
    switch (event) {
    case 0: return "none";
    case PTRACE_EVENT_FORK: return "fork";
    case PTRACE_EVENT_VFORK: return "vfork";
    case PTRACE_EVENT_CLONE: return "clone";
    case PTRACE_EVENT_EXEC: return "exec";
    case PTRACE_EVENT_VFORK_DONE: return "vfork-done";
    case PTRACE_EVENT_EXIT: return "exit";
    }
    trace(LOG_ALWAYS, "event_name(): unexpected event code %d", event);
    return "unknown";
}

const char * context_suspend_reason(Context * ctx) {
    static char reason[128];

    if (EXT(ctx)->end_of_step) return "Step";
    if (EXT(ctx)->ptrace_event != 0) {
        assert(ctx->signal == SIGTRAP);
        snprintf(reason, sizeof(reason), "Event: %s", event_name(EXT(ctx)->ptrace_event));
        return reason;
    }
    if (EXT(ctx)->syscall_enter) return "System Call";
    if (EXT(ctx)->syscall_exit) return "System Return";
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
    if (ptrace(PTRACE_TRACEME, 0, 0, 0) < 0) {
        int err = errno;
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_TRACEME) failed: pid %d, error %d %s",
              getpid(), err, errno_to_str(err));
        errno = err;
        return -1;
    }
    return 0;
}

int context_attach(pid_t pid, ContextAttachCallBack * done, void * data, int selfattach) {
    Context * ctx = NULL;

    assert(done != NULL);
    trace(LOG_CONTEXT, "context: attaching pid %d", pid);
    if (!selfattach && ptrace(PTRACE_ATTACH, pid, 0, 0) < 0) {
        int err = errno;
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_ATTACH) failed: pid %d, error %d %s",
            pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    add_waitpid_process(pid);
    ctx = create_context(pid2id(pid, 0));
    ctx->mem = ctx;
    EXT(ctx)->pid = pid;
    EXT(ctx)->attach_callback = done;
    EXT(ctx)->attach_data = data;
    list_add_first(&ctx->ctxl, &pending_list);
    /* TODO: context_attach works only for main task in a process */
    return 0;
}

int context_has_state(Context * ctx) {
    return ctx != NULL && ctx->parent != NULL;
}

int context_stop(Context * ctx) {
    trace(LOG_CONTEXT, "context:%s suspending ctx %#lx id %s",
        ctx->pending_intercept ? "" : " temporary", ctx, ctx->id);
    assert(is_dispatch_thread());
    assert(!ctx->exited);
    assert(!ctx->stopped);
    assert(!EXT(ctx)->regs_dirty);
    if (tkill(EXT(ctx)->pid, SIGSTOP) < 0) {
        int err = errno;
        if (err != ESRCH) {
            trace(LOG_ALWAYS, "error: tkill(SIGSTOP) failed: ctx %#lx, id %s, error %d %s",
                ctx, ctx->id, err, errno_to_str(err));
        }
        errno = err;
        return -1;
    }
    return 0;
}

static int syscall_never_returns(Context * ctx) {
    if (EXT(ctx)->syscall_enter) {
        switch (EXT(ctx)->syscall_id) {
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

    if (!EXT(ctx)->syscall_enter && !EXT(ctx)->ptrace_event) {
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

    trace(LOG_CONTEXT, "context: resuming ctx %#lx, id %s, with signal %d", ctx, ctx->id, signal);
#if defined(__i386__) || defined(__x86_64__)
    if (EXT(ctx)->regs->eflags & 0x100) {
        EXT(ctx)->regs->eflags &= ~0x100;
        EXT(ctx)->regs_dirty = 1;
    }
#endif
    if (EXT(ctx)->regs_dirty) {
        if (ptrace(PTRACE_SETREGS, EXT(ctx)->pid, 0, EXT(ctx)->regs) < 0) {
            int err = errno;
#if USE_ESRCH_WORKAROUND
            if (err == ESRCH) {
                EXT(ctx)->regs_dirty = 0;
                send_context_started_event(ctx);
                return 0;
            }
#endif
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_SETREGS) failed: ctx %#lx, id %s, error %d %s",
                ctx, ctx->id, err, errno_to_str(err));
            errno = err;
            return -1;
        }
        EXT(ctx)->regs_dirty = 0;
    }
#if USE_PTRACE_SYSCALL
    if (ptrace(PTRACE_SYSCALL, EXT(ctx)->pid, 0, signal) < 0) {
#else
    if (ptrace(PTRACE_CONT, EXT(ctx)->pid, 0, signal) < 0) {
#endif
        int err = errno;
#if USE_ESRCH_WORKAROUND
        if (err == ESRCH) {
            send_context_started_event(ctx);
            return 0;
        }
#endif
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_CONT, ...) failed: ctx %#lx, id %s, error %d %s",
            ctx, ctx->id, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    ctx->pending_signals &= ~(1 << signal);
    if (syscall_never_returns(ctx)) {
        EXT(ctx)->syscall_enter = 0;
        EXT(ctx)->syscall_exit = 0;
        EXT(ctx)->syscall_id = 0;
    }
    send_context_started_event(ctx);
    return 0;
}

int context_single_step(Context * ctx) {
    assert(is_dispatch_thread());
    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(!ctx->exited);
    assert(!ctx->pending_step);

    if (skip_breakpoint(ctx, 1)) return 0;

    if (syscall_never_returns(ctx)) return context_continue(ctx);
    trace(LOG_CONTEXT, "context: single step ctx %#lx, id %s", ctx, ctx->id);
    if (EXT(ctx)->regs_dirty) {
        if (ptrace(PTRACE_SETREGS, EXT(ctx)->pid, 0, EXT(ctx)->regs) < 0) {
            int err = errno;
#if USE_ESRCH_WORKAROUND
            if (err == ESRCH) {
                EXT(ctx)->regs_dirty = 0;
                ctx->pending_step = 1;
                send_context_started_event(ctx);
                return 0;
            }
#endif
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_SETREGS) failed: ctx %#lx, id %s, error %d %s",
                ctx, ctx->id, err, errno_to_str(err));
            errno = err;
            return -1;
        }
        EXT(ctx)->regs_dirty = 0;
    }
    if (ptrace(PTRACE_SINGLESTEP, EXT(ctx)->pid, 0, 0) < 0) {
        int err = errno;
#if USE_ESRCH_WORKAROUND
        if (err == ESRCH) {
            ctx->pending_step = 1;
            send_context_started_event(ctx);
            return 0;
        }
#endif
        trace(LOG_ALWAYS, "error: ptrace(PTRACE_SINGLESTEP, ...) failed: ctx %#lx, id %s, error %d %s",
            ctx, ctx->id, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    ctx->pending_step = 1;
    send_context_started_event(ctx);
    return 0;
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    ContextAddress word_addr;
    unsigned word_size = context_word_size(ctx);
    assert(is_dispatch_thread());
    assert(!ctx->exited);
    trace(LOG_CONTEXT, "context: write memory ctx %#lx, id %s, address %#lx, size %zd",
        ctx, ctx->id, address, size);
    assert(word_size <= sizeof(unsigned long));
    check_breakpoints_on_memory_write(ctx, address, buf, size);
    for (word_addr = address & ~((ContextAddress)word_size - 1); word_addr < address + size; word_addr += word_size) {
        unsigned long word = 0;
        if (word_addr < address || word_addr + word_size > address + size) {
            unsigned i = 0;
            errno = 0;
            word = ptrace(PTRACE_PEEKDATA, EXT(ctx)->pid, (void *)word_addr, 0);
            if (errno != 0) {
                int err = errno;
                trace(LOG_CONTEXT, "error: ptrace(PTRACE_PEEKDATA, ...) failed: ctx %#lx, id %s, addr %#lx, error %d %s",
                    ctx, ctx->id, word_addr, err, errno_to_str(err));
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
        if (ptrace(PTRACE_POKEDATA, EXT(ctx)->pid, (void *)word_addr, word) < 0) {
            int err = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_POKEDATA, ...) failed: ctx %#lx, id %s, addr %#lx, error %d %s",
                ctx, ctx->id, word_addr, err, errno_to_str(err));
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
    trace(LOG_CONTEXT, "context: read memory ctx %#lx, id %s, address %#lx, size %zd",
        ctx, ctx->id, address, size);
    assert(word_size <= sizeof(unsigned long));
    for (word_addr = address & ~((ContextAddress)word_size - 1); word_addr < address + size; word_addr += word_size) {
        unsigned long word = 0;
        errno = 0;
        word = ptrace(PTRACE_PEEKDATA, EXT(ctx)->pid, (void *)word_addr, 0);
        if (errno != 0) {
            int err = errno;
            trace(LOG_CONTEXT, "error: ptrace(PTRACE_PEEKDATA, ...) failed: ctx %#lx, id %s, addr %#lx, error %d %s",
                ctx, ctx->id, word_addr, err, errno_to_str(err));
            errno = err;
            return -1;
        }
        if (word_addr < address || word_addr + word_size > address + size) {
            unsigned i = 0;
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
    check_breakpoints_on_memory_read(ctx, address, buf, size);
    return 0;
}

int context_write_reg(Context * ctx, RegisterDefinition * def, unsigned offs, unsigned size, void * buf) {
    ContextExtensionLinux * ext = EXT(ctx);

    assert(is_dispatch_thread());
    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(!ctx->exited);
    assert(offs + size <= def->size);

    if (ext->regs_error) {
        set_error_report_errno(ext->regs_error);
        return -1;
    }
    memcpy((uint8_t *)ext->regs + def->offset + offs, buf, size);
    ext->regs_dirty = 1;
    return 0;
}

int context_read_reg(Context * ctx, RegisterDefinition * def, unsigned offs, unsigned size, void * buf) {
    ContextExtensionLinux * ext = EXT(ctx);

    assert(is_dispatch_thread());
    assert(context_has_state(ctx));
    assert(ctx->stopped);
    assert(!ctx->exited);
    assert(offs + size <= def->size);

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

static Context * find_pending(pid_t pid) {
    LINK * l = pending_list.next;
    while (l != &pending_list) {
        Context * c = ctxl2ctxp(l);
        if (EXT(c)->pid == pid) {
            list_remove(&c->ctxl);
            return c;
        }
        l = l->next;
    }
    return NULL;
}

static void event_pid_exited(pid_t pid, int status, int signal) {
    Context * ctx;

    ctx = context_find_from_pid(pid, 1);
    if (ctx == NULL) {
        ctx = find_pending(pid);
        if (ctx == NULL) {
            trace(LOG_EVENTS, "event: ctx not found, pid %d, exit status %d, term signal %d", pid, status, signal);
        }
        else {
            assert(ctx->ref_count == 0);
            if (EXT(ctx)->attach_callback != NULL) {
                if (status == 0) status = EINVAL;
                EXT(ctx)->attach_callback(status, ctx, EXT(ctx)->attach_data);
            }
            assert(list_is_empty(&ctx->children));
            assert(ctx->parent == NULL);
            ctx->ref_count = 1;
            context_unlock(ctx);
        }
    }
    else {
        if (EXT(ctx->parent)->pid == pid) ctx = ctx->parent;
        assert(EXT(ctx)->attach_callback == NULL);
        if (ctx->stopped || ctx->exited) {
            trace(LOG_EVENTS, "event: ctx %#lx, pid %d, exit status %d unexpected, stopped %d, exited %d",
                ctx, pid, status, ctx->stopped, ctx->exited);
            if (ctx->stopped) send_context_started_event(ctx);
        }
        else {
            trace(LOG_EVENTS, "event: ctx %#lx, pid %d, exit status %d, term signal %d", ctx, pid, status, signal);
        }
        if (!list_is_empty(&ctx->children)) {
            LINK * l = ctx->children.next;
            while (l != &ctx->children) {
                Context * c = cldl2ctxp(l);
                l = l->next;
                assert(c->parent == ctx);
                if (!c->exited) {
                    if (c->stopped) send_context_started_event(c);
                    release_error_report(EXT(c)->regs_error);
                    loc_free(EXT(c)->regs);
                    EXT(c)->regs_error = NULL;
                    EXT(c)->regs = NULL;
                    send_context_exited_event(c);
                }
            }
        }
        /* Note: ctx->exiting should be 1 here. However, PTRACE_EVENT_EXIT can be lost by PTRACE because of racing
         * between PTRACE_CONT (or PTRACE_SYSCALL) and SIGTRAP/PTRACE_EVENT_EXIT. So, ctx->exiting can be 0.
         */
        send_context_exited_event(ctx);
    }
}

#if !USE_PTRACE_SYSCALL
#   define get_syscall_id(ctx) 0
#elif defined(__x86_64__)
#   define get_syscall_id(ctx) (EXT(ctx)->regs->orig_rax)
#elif defined(__i386__)
#   define get_syscall_id(ctx) (EXT(ctx)->regs->orig_eax)
#else
#   error "get_syscall_id() is not implemented for CPU other then X86"
#endif

static void event_pid_stopped(pid_t pid, int signal, int event, int syscall) {
    int stopped_by_exception = 0;
    unsigned long msg = 0;
    Context * ctx = NULL;

    trace(LOG_EVENTS, "event: pid %d stopped, signal %d, event %s", pid, signal, event_name(event));

    ctx = context_find_from_pid(pid, 1);

    if (ctx == NULL) {
        ctx = find_pending(pid);
        if (ctx != NULL) {
            Context * prs = ctx;
            assert(prs->ref_count == 0);
            ctx = create_context(pid2id(pid, pid));
            EXT(ctx)->pid = pid;
            EXT(ctx)->regs = (REG_SET *)loc_alloc(sizeof(REG_SET));
            ctx->pending_intercept = 1;
            ctx->mem = prs;
            (ctx->parent = prs)->ref_count++;
            list_add_first(&ctx->cldl, &prs->children);
            link_context(prs);
            link_context(ctx);
            send_context_created_event(prs);
            send_context_created_event(ctx);
            if (EXT(prs)->attach_callback) {
                EXT(prs)->attach_callback(0, prs, EXT(prs)->attach_data);
                EXT(prs)->attach_callback = NULL;
                EXT(prs)->attach_data = NULL;
            }
        }
    }

    if (ctx == NULL) return;

    assert(!ctx->exited);
    assert(!EXT(ctx)->attach_callback);
    if (EXT(ctx)->ptrace_flags != PTRACE_FLAGS) {
        if (ptrace((enum __ptrace_request)PTRACE_SETOPTIONS, EXT(ctx)->pid, 0, PTRACE_FLAGS) < 0) {
                int err = errno;
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_SETOPTIONS) failed: pid %d, error %d %s",
                EXT(ctx)->pid, err, errno_to_str(err));
        }
        else {
            EXT(ctx)->ptrace_flags = PTRACE_FLAGS;
        }
    }

    switch (event) {
    case PTRACE_EVENT_FORK:
    case PTRACE_EVENT_VFORK:
    case PTRACE_EVENT_CLONE:
        if (ptrace((enum __ptrace_request)PTRACE_GETEVENTMSG, pid, 0, &msg) < 0) {
                trace(LOG_ALWAYS, "error: ptrace(PTRACE_GETEVENTMSG) failed; pid %d, error %d %s",
                pid, errno, errno_to_str(errno));
            break;
        }
        assert(msg != 0);
        add_waitpid_process(msg);
        {
            Context * prs2 = NULL;
            Context * ctx2 = NULL;
            if (event == PTRACE_EVENT_CLONE) {
                /* TODO: using the PTRACE_EVENT_CLONE to determine if the new context is a thread is not correct.
                 * The only way I know of is to look at the Tgid field of /proc/<pid>/status */
                prs2 = ctx->parent;
            }
            else {
                prs2 = create_context(pid2id(msg, 0));
                EXT(prs2)->pid = msg;
                prs2->mem = prs2;
                (prs2->creator = ctx)->ref_count++;
                prs2->sig_dont_stop = ctx->sig_dont_stop;
                prs2->sig_dont_pass = ctx->sig_dont_pass;
                link_context(prs2);
                send_context_created_event(prs2);
            }

            ctx2 = create_context(pid2id(msg, EXT(prs2)->pid));
            EXT(ctx2)->pid = msg;
            EXT(ctx2)->regs = (REG_SET *)loc_alloc(sizeof(REG_SET));
            ctx2->mem = prs2;
            ctx2->sig_dont_stop = ctx->sig_dont_stop;
            ctx2->sig_dont_pass = ctx->sig_dont_pass;
            (ctx2->creator = ctx)->ref_count++;
            (ctx2->parent = prs2)->ref_count++;
            list_add_first(&ctx2->cldl, &prs2->children);
            link_context(ctx2);
            trace(LOG_EVENTS, "event: new context 0x%x, id %s", ctx2, ctx2->id);
            send_context_created_event(ctx2);
        }
        break;
    case PTRACE_EVENT_EXEC:
        send_context_changed_event(ctx);
        break;
    case PTRACE_EVENT_EXIT:
        ctx->exiting = 1;
        EXT(ctx)->regs_dirty = 0;
        break;
    }

    if (signal != SIGSTOP && signal != SIGTRAP) {
        assert(signal < 32);
        ctx->pending_signals |= 1 << signal;
        if ((ctx->sig_dont_stop & (1 << signal)) == 0) {
            ctx->pending_intercept = 1;
            stopped_by_exception = 1;
        }
    }

    if (ctx->stopped) {
        if (event != PTRACE_EVENT_EXEC) send_context_changed_event(ctx);
    }
    else {
        ContextAddress pc0 = 0;
        ContextAddress pc1 = 0;

        assert(!EXT(ctx)->regs_dirty);

        EXT(ctx)->end_of_step = 0;
        EXT(ctx)->ptrace_event = event;
        ctx->signal = signal;
        ctx->stopped_by_bp = 0;
        ctx->stopped_by_exception = stopped_by_exception;
        ctx->stopped = 1;

        if (EXT(ctx)->regs_error) {
            release_error_report(EXT(ctx)->regs_error);
            EXT(ctx)->regs_error = NULL;
        }
        else {
            pc0 = get_regs_PC(ctx);
        }

        if (ptrace(PTRACE_GETREGS, EXT(ctx)->pid, 0, EXT(ctx)->regs) < 0) {
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
                ctx->stopped = 0;
                return;
            }
#endif
            EXT(ctx)->regs_error = get_error_report(errno);
            trace(LOG_ALWAYS, "error: ptrace(PTRACE_GETREGS) failed; pid %d, error %d %s",
                EXT(ctx)->pid, errno, errno_to_str(errno));
        }
        else {
            pc1 = get_regs_PC(ctx);
        }

        if (syscall && !EXT(ctx)->regs_error) {
            if (!EXT(ctx)->syscall_enter) {
                EXT(ctx)->syscall_id = get_syscall_id(ctx);
                EXT(ctx)->syscall_pc = pc1;
                EXT(ctx)->syscall_enter = 1;
                EXT(ctx)->syscall_exit = 0;
                trace(LOG_EVENTS, "event: pid %d enter sys call %d, PC = %#lx",
                    pid, EXT(ctx)->syscall_id, EXT(ctx)->syscall_pc);
            }
            else {
                if (EXT(ctx)->syscall_pc != pc1) {
                    trace(LOG_ALWAYS, "Invalid PC at sys call exit: pid %d, sys call %d, PC %#lx, expected PC %#lx",
                        EXT(ctx)->pid, EXT(ctx)->syscall_id, pc1, EXT(ctx)->syscall_pc);
                }
                trace(LOG_EVENTS, "event: pid %d exit sys call %d, PC = %#lx",
                    pid, EXT(ctx)->syscall_id, pc1);
                switch (EXT(ctx)->syscall_id) {
                case __NR_mmap:
                case __NR_munmap:
#ifdef __NR_mmap2
                case __NR_mmap2:
#endif
                case __NR_mremap:
                case __NR_remap_file_pages:
                    send_context_changed_event(ctx);
                    break;
                }
                EXT(ctx)->syscall_enter = 0;
                EXT(ctx)->syscall_exit = 1;
            }
        }
        else {
            if (!EXT(ctx)->syscall_enter || EXT(ctx)->regs_error || pc0 != pc1) {
                EXT(ctx)->syscall_enter = 0;
                EXT(ctx)->syscall_exit = 0;
                EXT(ctx)->syscall_id = 0;
                EXT(ctx)->syscall_pc = 0;
            }
            trace(LOG_EVENTS, "event: pid %d stopped at PC = %#lx", pid, pc1);
        }

        if (signal == SIGSTOP && ctx->pending_step && !EXT(ctx)->regs_error && pc0 == pc1) {
            trace(LOG_EVENTS, "event: pid %d, single step failed because of pending SIGSTOP, retrying", EXT(ctx)->pid);
            ptrace(PTRACE_SINGLESTEP, EXT(ctx)->pid, 0, 0);
            ctx->stopped = 0;
            return;
        }

        if (signal == SIGTRAP && event == 0 && !syscall) {
            size_t break_size = 0;
            get_break_instruction(ctx, &break_size);
            ctx->stopped_by_bp = !EXT(ctx)->regs_error && is_breakpoint_address(ctx, pc1 - break_size);
            EXT(ctx)->end_of_step = !ctx->stopped_by_bp && ctx->pending_step;
            if (ctx->stopped_by_bp) {
                set_regs_PC(ctx, pc1 - break_size);
                EXT(ctx)->regs_dirty = 1;
            }
        }
        ctx->pending_step = 0;
        send_context_stopped_event(ctx);
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

#if SERVICE_Expressions && ENABLE_ELF

static int expression_identifier_callback(Context * ctx, int frame, char * name, Value * v) {
    if (ctx == NULL) return 0;
    if (strcmp(name, "$loader_brk") == 0) {
        v->address = elf_get_debug_structure_address(ctx, NULL);
        v->type_class = TYPE_CLASS_POINTER;
        v->size = context_word_size(ctx);
        if (v->address != 0) {
            switch (v->size) {
            case 4: v->address += 8; break;
            case 8: v->address += 16; break;
            default: assert(0);
            }
            v->remote = 1;
        }
        else {
            set_value(v, NULL, v->size);
        }
        return 1;
    }
    if (strcmp(name, "$loader_state") == 0) {
        v->address = elf_get_debug_structure_address(ctx, NULL);
        v->type_class = TYPE_CLASS_CARDINAL;
        v->size = context_word_size(ctx);
        if (v->address != 0) {
            switch (v->size) {
            case 4: v->address += 12; break;
            case 8: v->address += 24; break;
            default: assert(0);
            }
        }
        v->remote = 1;
        return 1;
    }
    return 0;
}

static void eventpoint_at_loader(Context * ctx, void * args) {
    typedef enum { RT_CONSISTENT, RT_ADD, RT_DELETE } r_state;
    ELF_File * file = NULL;
    ContextAddress addr = elf_get_debug_structure_address(ctx, &file);
    unsigned size = context_word_size(ctx);
    ContextAddress state = 0;

    if (ctx->parent != NULL) ctx = ctx->parent;

    if (addr != 0) {
        switch (size) {
        case 4: addr += 12; break;
        case 8: addr += 24; break;
        default: assert(0);
        }
        if (elf_read_memory_word(ctx, file, addr, &state) < 0) {
            int error = errno;
            trace(LOG_ALWAYS, "Can't read loader state flag: %d %s", error, errno_to_str(error));
            ctx->pending_intercept = 1;
            EXT(ctx)->loader_state = 0;
            return;
        }
    }

    switch (state) {
    case RT_CONSISTENT:
        if (EXT(ctx)->loader_state == RT_ADD) {
            memory_map_event_module_loaded(ctx);
        }
        else if (EXT(ctx)->loader_state == RT_DELETE) {
            memory_map_event_module_unloaded(ctx);
        }
        break;
    case RT_ADD:
        break;
    case RT_DELETE:
        /* TODO: need to call memory_map_event_code_section_ummapped() */
        break;
    }
    EXT(ctx)->loader_state = state;
}

#endif /* SERVICE_Expressions && ENABLE_ELF */

void init_contexts_sys_dep(void) {
    list_init(&pending_list);
    context_extension_offset = context_extension(sizeof(ContextExtensionLinux));
    add_waitpid_listener(waitpid_listener, NULL);
    ini_context_pid_hash();
#if SERVICE_Expressions && ENABLE_ELF
    add_identifier_callback(expression_identifier_callback);
    create_eventpoint("$loader_brk", eventpoint_at_loader, NULL);
#endif /* SERVICE_Expressions && ENABLE_ELF */
}

#endif  /* if ENABLE_DebugContext */
#endif /* __linux__ */
