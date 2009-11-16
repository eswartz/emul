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

#if defined(__APPLE__)

#include <stdlib.h>
#include <assert.h>
#include <errno.h>
#include <signal.h>
#include <sys/syscall.h>
#include <sys/ptrace.h>
#include <sched.h>
#include <mach/thread_status.h>
#include "context.h"
#include "events.h"
#include "errors.h"
#include "trace.h"
#include "myalloc.h"
#include "breakpoints.h"
#include "waitpid.h"

#if ENABLE_DebugContext

#define WORD_SIZE   4

static LINK pending_list;

char * context_suspend_reason(Context * ctx) {
    static char reason[128];

    if (ctx->stopped_by_bp && ctx->bp_ids != NULL) return "Breakpoint";
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
        trace(LOG_ALWAYS, "error: ptrace(PT_TRACE_ME) failed: pid %d, error %d %s",
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
        trace(LOG_ALWAYS, "error: ptrace(PT_ATTACH) failed: pid %d, error %d %s",
            pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    add_waitpid_process(pid);
    ctx = create_context(pid);
    list_add_first(&ctx->ctxl, &pending_list);
    ctx->mem = pid;
    ctx->attach_callback = done;
    ctx->attach_data = data;
    ctx->pending_intercept = 1;
    /* TODO: context_attach works only for main task in a process */
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
            send_context_started_event(ctx);
            return 0;
        }
#endif
        trace(LOG_ALWAYS, "error: ptrace(PT_CONTINUE, ...) failed: ctx %#lx, pid %d, error %d %s",
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
    send_context_started_event(ctx);
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
            send_context_started_event(ctx);
            return 0;
        }
#endif
        trace(LOG_ALWAYS, "error: ptrace(PT_STEP, ...) failed: ctx %#lx, pid %d, error %d %s",
            ctx, ctx->pid, err, errno_to_str(err));
        errno = err;
        return -1;
    }
    ctx->pending_step = 1;
    ctx->stopped = 0;
    send_context_started_event(ctx);
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
                trace(LOG_CONTEXT, "error: ptrace(PT_PEEKDATA, ...) failed: ctx %#lx, pid %d, error %d %s",
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
            trace(LOG_ALWAYS, "error: ptrace(PT_POKEDATA, ...) failed: ctx %#lx, pid %d, error %d %s",
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
            trace(LOG_CONTEXT, "error: ptrace(PT_PEEKDATA, ...) failed: ctx %#lx, pid %d, error %d %s",
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
                send_context_started_event(ctx);
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
                send_context_exited_event(c);
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
        send_context_exited_event(ctx);
        if (ctx->parent != NULL) {
            list_remove(&ctx->cldl);
            context_unlock(ctx->parent);
            ctx->parent = NULL;
        }
        context_unlock(ctx);
    }
}

static void event_pid_stopped(pid_t pid, int signal, int event, int syscall) {
    int stopped_by_exception = 0;
    unsigned long msg = 0;
    Context * ctx = NULL;
    Context * ctx2 = NULL;

    trace(LOG_EVENTS, "event: pid %d stopped, signal %d", pid, signal);

    ctx = context_find_from_pid(pid);

    if (ctx == NULL) {
        ctx = find_pending(pid);
        if (ctx != NULL) {
            link_context(ctx);
            send_context_created_event(ctx);
            if (ctx->attach_callback) {
                ctx->attach_callback(0, ctx, ctx->attach_data);
                ctx->attach_callback = NULL;
                ctx->attach_data = NULL;
            }
        }
    }

    if (ctx == NULL) return;

    assert(!ctx->exited);
    if (signal != SIGSTOP && signal != SIGTRAP) {
        assert(signal < 32);
        ctx->pending_signals |= 1 << signal;
        if ((ctx->sig_dont_stop & (1 << signal)) == 0) {
            ctx->pending_intercept = 1;
            stopped_by_exception = 1;
        }
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
            ctx->stopped_by_exception = stopped_by_exception;
            ctx->end_of_step = 0;
            if (signal == SIGTRAP && event == 0 && !syscall) {
                ctx->stopped_by_bp = !ctx->regs_error &&
                    is_breakpoint_address(ctx, get_regs_PC(ctx->regs) - BREAK_SIZE);
                ctx->end_of_step = !ctx->stopped_by_bp && ctx->pending_step;
            }
            ctx->pending_step = 0;
            if (ctx->stopped_by_bp) {
                set_regs_PC(ctx->regs, get_regs_PC(ctx->regs) - BREAK_SIZE);
                ctx->regs_dirty = 1;
            }
            send_context_stopped_event(ctx);
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

void init_contexts_sys_dep(void) {
    list_init(&pending_list);
    add_waitpid_listener(waitpid_listener, NULL);
}

#endif  /* if ENABLE_DebugContext */
#endif /* __APPLE__ */
