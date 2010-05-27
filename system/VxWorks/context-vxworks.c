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

#if defined(_WRS_KERNEL)

#if ENABLE_DebugContext && !ENABLE_ContextProxy

#include <stdlib.h>
#include <assert.h>
#include <errno.h>
#include <signal.h>
#include <framework/events.h>
#include <framework/errors.h>
#include <framework/trace.h>
#include <framework/myalloc.h>
#include <framework/waitpid.h>
#include <framework/signames.h>
#include <services/breakpoints.h>
#include <system/VxWorks/context-vxworks.h>

/* TODO: VxWorks RTP support */

#include <taskHookLib.h>
#include <private/vxdbgLibP.h>

#define TRACE_EVENT_STEP        2

typedef struct ContextExtensionVxWorks {
    pid_t               pid;
    VXDBG_BP_INFO       bp_info;        /* breakpoint information */
    pid_t               bp_pid;         /* process or thread that hit breakpoint */
    int                 event;
    REG_SET *           regs;           /* copy of context registers, updated when context stops */
    ErrorReport *       regs_error;     /* if not NULL, 'regs' is invalid */
    int                 regs_dirty;     /* if not 0, 'regs' is modified and needs to be saved before context is continued */
} ContextExtensionVxWorks;

static size_t context_extension_offset = 0;

#define EXT(ctx) ((ContextExtensionVxWorks *)((char *)(ctx) + context_extension_offset))

#include <system/pid-hash.h>

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
static Context * parent_ctx = NULL;

const char * context_suspend_reason(Context * ctx) {
    if (EXT(ctx)->event == TRACE_EVENT_STEP) return "Step";
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
    if (context_find_from_pid(args->pid, 0) != NULL) {
        args->done(ERR_ALREADY_ATTACHED, NULL, args->data);
    }
    else {
        Context * ctx = NULL;
        if (parent_ctx == NULL) {
            pid_t pid = taskIdSelf();
            parent_ctx = create_context(pid2id(pid, 0));
            EXT(parent_ctx)->pid = pid;
            parent_ctx->mem = parent_ctx;
            link_context(parent_ctx);
            send_context_created_event(parent_ctx);
        }
        assert(parent_ctx->ref_count > 0);
        ctx = create_context(pid2id(args->pid, EXT(parent_ctx)->pid));
        EXT(ctx)->pid = args->pid;
        EXT(ctx)->regs = (REG_SET *)loc_alloc(sizeof(REG_SET));
        ctx->mem = parent_ctx;
        (ctx->parent = parent_ctx)->ref_count++;
        list_add_first(&ctx->cldl, &parent_ctx->children);
        link_context(ctx);
        trace(LOG_CONTEXT, "context: attached: ctx %#lx, id %#x", ctx, EXT(ctx)->pid);
        send_context_created_event(ctx);
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
    return ctx != NULL && ctx->parent != NULL;
}

int context_stop(Context * ctx) {
    struct event_info * info;
    VXDBG_CTX vxdbg_ctx;

    assert(is_dispatch_thread());
    assert(ctx->parent != NULL);
    assert(!ctx->stopped);
    assert(!ctx->exited);
    assert(!EXT(ctx)->regs_dirty);
    if (ctx->pending_intercept) {
        trace(LOG_CONTEXT, "context: stop ctx %#lx, id %#x", ctx, EXT(ctx)->pid);
    }
    else {
        trace(LOG_CONTEXT, "context: temporary stop ctx %#lx, id %#x", ctx, EXT(ctx)->pid);
    }

    taskLock();
    if (taskIsStopped(EXT(ctx)->pid)) {
        trace(LOG_CONTEXT, "context: already stopped ctx %#lx, id %#x", ctx, EXT(ctx)->pid);
    }
    else {
        vxdbg_ctx.ctxId = EXT(ctx)->pid;
        vxdbg_ctx.ctxType = VXDBG_CTX_TASK;
        if (vxdbgStop(vxdbg_clnt_id, &vxdbg_ctx) != OK) {
            int error = errno;
            taskUnlock();
            if (error == S_vxdbgLib_INVALID_CTX) return 0;
            trace(LOG_ALWAYS, "context: can't stop ctx %#lx, id %#x: %s",
                    ctx, EXT(ctx)->pid, errno_to_str(error));
            return -1;
        }
    }
    assert(taskIsStopped(EXT(ctx)->pid));
    info = event_info_alloc(EVENT_HOOK_STOP);
    if (info != NULL) {
        info->stopped_ctx.ctxId = EXT(ctx)->pid;
        event_info_post(info);
    }
    taskUnlock();
    return 0;
}

static int kill_context(Context * ctx) {
    assert(ctx->stopped);
    assert(ctx->parent != NULL);
    assert(ctx->pending_signals & (1 << SIGKILL));

    ctx->pending_signals &= ~(1 << SIGKILL);
    if (taskDelete(EXT(ctx)->pid) != OK) {
        int error = errno;
        trace(LOG_ALWAYS, "context: can't kill ctx %#lx, id %#x: %s",
                ctx, EXT(ctx)->pid, errno_to_str(error));
        return -1;
    }
    send_context_started_event(ctx);
    trace(LOG_CONTEXT, "context: killed ctx %#lx, id %#x", ctx, EXT(ctx)->pid);
    release_error_report(EXT(ctx)->regs_error);
    loc_free(EXT(ctx)->regs);
    EXT(ctx)->regs_error = NULL;
    EXT(ctx)->regs = NULL;
    send_context_exited_event(ctx);
    return 0;
}

int context_continue(Context * ctx) {
    VXDBG_CTX vxdbg_ctx;

    assert(is_dispatch_thread());
    assert(ctx->parent != NULL);
    assert(ctx->stopped);
    assert(!ctx->pending_intercept);
    assert(!ctx->exited);
    assert(!ctx->pending_step);
    assert(taskIsStopped(EXT(ctx)->pid));

    if (skip_breakpoint(ctx, 0)) return 0;

    trace(LOG_CONTEXT, "context: continue ctx %#lx, id %#x", ctx, EXT(ctx)->pid);

    if (EXT(ctx)->regs_dirty) {
        if (taskRegsSet(EXT(ctx)->pid, EXT(ctx)->regs) != OK) {
            int error = errno;
            trace(LOG_ALWAYS, "context: can't set regs ctx %#lx, id %#x: %s",
                    ctx, EXT(ctx)->pid, errno_to_str(error));
            return -1;
        }
        EXT(ctx)->regs_dirty = 0;
    }

    if (ctx->pending_signals & (1 << SIGKILL)) {
        return kill_context(ctx);
    }

    vxdbg_ctx.ctxId = EXT(ctx)->pid;
    vxdbg_ctx.ctxType = VXDBG_CTX_TASK;
    taskLock();
    if (vxdbgCont(vxdbg_clnt_id, &vxdbg_ctx) != OK) {
        int error = errno;
        taskUnlock();
        trace(LOG_ALWAYS, "context: can't continue ctx %#lx, id %#x: %s",
                ctx, EXT(ctx)->pid, errno_to_str(error));
        return -1;
    }
    assert(!taskIsStopped(EXT(ctx)->pid));
    taskUnlock();
    send_context_started_event(ctx);
    return 0;
}

int context_single_step(Context * ctx) {
    VXDBG_CTX vxdbg_ctx;

    assert(is_dispatch_thread());
    assert(context_has_state(ctx));
    assert(ctx->parent != NULL);
    assert(ctx->stopped);
    assert(!ctx->exited);
    assert(!ctx->pending_step);
    assert(taskIsStopped(EXT(ctx)->pid));

    if (skip_breakpoint(ctx, 1)) return 0;

    trace(LOG_CONTEXT, "context: single step ctx %#lx, id %#x", ctx, EXT(ctx)->pid);

    if (EXT(ctx)->regs_dirty) {
        if (taskRegsSet(EXT(ctx)->pid, EXT(ctx)->regs) != OK) {
            int error = errno;
            trace(LOG_ALWAYS, "context: can't set regs ctx %#lx, id %#x: %s",
                    ctx, EXT(ctx)->pid, errno_to_str(error));
            return -1;
        }
        EXT(ctx)->regs_dirty = 0;
    }

    if (ctx->pending_signals & (1 << SIGKILL)) {
        return kill_context(ctx);
    }

    vxdbg_ctx.ctxId = EXT(ctx)->pid;
    vxdbg_ctx.ctxType = VXDBG_CTX_TASK;
    taskLock();
    if (vxdbgStep(vxdbg_clnt_id, &vxdbg_ctx, NULL, NULL) != OK) {
        int error = errno;
        taskUnlock();
        trace(LOG_ALWAYS, "context: can't step ctx %#lx, id %#x: %d",
                ctx, EXT(ctx)->pid, errno_to_str(error));
        return -1;
    }
    ctx->pending_step = 1;
    taskUnlock();
    send_context_started_event(ctx);
    return 0;
}

int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    if (address < 0x100) {
        /* TODO: need proper handling of Access Violation exception in VxWorks version of context_read_mem() */
        errno = ERR_INV_ADDRESS;
        return -1;
    }
#ifdef _WRS_PERSISTENT_SW_BP
    vxdbgMemRead((void *)address, buf, size);
#else
    bcopy((void *)address, buf, size);
#endif
    return 0;
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    if (address < 0x100) {
        errno = ERR_INV_ADDRESS;
        return -1;
    }
#ifdef _WRS_PERSISTENT_SW_BP
    vxdbgMemWrite((void *)address, buf, size);
#else
    bcopy(buf, (void *)address, size);
#endif
    return 0;
}

int context_write_reg(Context * ctx, RegisterDefinition * def, unsigned offs, unsigned size, void * buf) {
    ContextExtensionVxWorks * ext = EXT(ctx);

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
    ContextExtensionVxWorks * ext = EXT(ctx);

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

int get_context_task_id(Context * ctx) {
    return EXT(ctx)->pid;
}

static void event_handler(void * arg) {
    struct event_info * info = (struct event_info *)arg;
    Context * current_ctx = context_find_from_pid(info->current_ctx.ctxId, 1);
    Context * stopped_ctx = context_find_from_pid(info->stopped_ctx.ctxId, 1);

    switch (info->event) {
    case EVENT_HOOK_BREAKPOINT:
        if (stopped_ctx == NULL) break;
        assert(!stopped_ctx->stopped);
        assert(!EXT(stopped_ctx)->regs_dirty);
        if (EXT(stopped_ctx)->regs_error) {
            release_error_report(EXT(stopped_ctx)->regs_error);
            EXT(stopped_ctx)->regs_error = NULL;
        }
        memcpy(EXT(stopped_ctx)->regs, &info->regs, sizeof(REG_SET));
        EXT(stopped_ctx)->event = 0;
        stopped_ctx->signal = SIGTRAP;
        stopped_ctx->pending_step = 0;
        stopped_ctx->stopped = 1;
        stopped_ctx->stopped_by_bp = info->bp_info_ok;
        stopped_ctx->stopped_by_exception = 0;
        assert(get_regs_PC(stopped_ctx) == info->addr);
        if (stopped_ctx->stopped_by_bp && !is_breakpoint_address(stopped_ctx, info->addr)) {
            /* Break instruction that is not planted by us */
            stopped_ctx->stopped_by_bp = 0;
            stopped_ctx->pending_intercept = 1;
        }
        EXT(stopped_ctx)->bp_info = info->bp_info;
        if (current_ctx != NULL) EXT(stopped_ctx)->bp_pid = EXT(current_ctx)->pid;
        assert(taskIsStopped(EXT(stopped_ctx)->pid));
        trace(LOG_CONTEXT, "context: stopped by breakpoint: ctx %#lx, id %#x",
                stopped_ctx, EXT(stopped_ctx)->pid);
        send_context_stopped_event(stopped_ctx);
        break;
    case EVENT_HOOK_STEP_DONE:
        if (current_ctx == NULL) break;
        assert(!current_ctx->stopped);
        assert(!EXT(current_ctx)->regs_dirty);
        if (EXT(current_ctx)->regs_error) {
            release_error_report(EXT(current_ctx)->regs_error);
            EXT(current_ctx)->regs_error = NULL;
        }
        memcpy(EXT(current_ctx)->regs, &info->regs, sizeof(REG_SET));
        EXT(current_ctx)->event = TRACE_EVENT_STEP;
        current_ctx->signal = SIGTRAP;
        current_ctx->pending_step = 0;
        current_ctx->stopped = 1;
        current_ctx->stopped_by_bp = 0;
        current_ctx->stopped_by_exception = 0;
        assert(taskIsStopped(EXT(current_ctx)->pid));
        trace(LOG_CONTEXT, "context: stopped by end of step: ctx %#lx, id %#x",
                current_ctx, EXT(current_ctx)->pid);
        send_context_stopped_event(current_ctx);
        break;
    case EVENT_HOOK_STOP:
        if (stopped_ctx == NULL) break;
        assert(!stopped_ctx->exited);
        if (stopped_ctx->stopped) break;
        if (EXT(stopped_ctx)->regs_error) {
            release_error_report(EXT(stopped_ctx)->regs_error);
            EXT(stopped_ctx)->regs_error = NULL;
        }
        if (taskRegsGet(EXT(stopped_ctx)->pid, EXT(stopped_ctx)->regs) != OK) {
            EXT(stopped_ctx)->regs_error = get_error_report(errno);
            assert(EXT(stopped_ctx)->regs_error != NULL);
        }
        EXT(stopped_ctx)->event = 0;
        stopped_ctx->signal = SIGSTOP;
        stopped_ctx->pending_step = 0;
        stopped_ctx->stopped = 1;
        stopped_ctx->stopped_by_bp = 0;
        stopped_ctx->stopped_by_exception = 0;
        assert(taskIsStopped(EXT(stopped_ctx)->pid));
        trace(LOG_CONTEXT, "context: stopped by sofware request: ctx %#lx, id %#x",
                stopped_ctx, EXT(stopped_ctx)->pid);
        send_context_stopped_event(stopped_ctx);
        break;
    case EVENT_HOOK_TASK_ADD:
        if (current_ctx == NULL) break;
        assert(stopped_ctx == NULL);
        stopped_ctx = create_context(pid2id((pid_t)info->stopped_ctx.ctxId, EXT(current_ctx->parent)->pid));
        EXT(stopped_ctx)->pid = (pid_t)info->stopped_ctx.ctxId;
        EXT(stopped_ctx)->regs = (REG_SET *)loc_alloc(sizeof(REG_SET));
        stopped_ctx->mem = current_ctx->mem;
        (stopped_ctx->creator = current_ctx)->ref_count++;
        (stopped_ctx->parent = current_ctx->parent)->ref_count++;
        assert(stopped_ctx->mem == stopped_ctx->parent->mem);
        list_add_first(&stopped_ctx->cldl, &stopped_ctx->parent->children);
        link_context(stopped_ctx);
        trace(LOG_CONTEXT, "context: created: ctx %#lx, id %#x",
                stopped_ctx, EXT(stopped_ctx)->pid);
        send_context_created_event(stopped_ctx);
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
    taskPrioritySet(0, VX_TASK_PRIORITY_MIN);
    for (;;) {
        struct event_info * info = loc_alloc(sizeof(struct event_info));
        semTake(events_signal, WAIT_FOREVER);

        SPIN_LOCK_ISR_TAKE(&events_lock);
        if (events_buf_overflow && events_inp == events_out) {
            SPIN_LOCK_ISR_GIVE(&events_lock);
            loc_free(info);
            break;
        }
        assert(events_inp != events_out);
        *info = events[events_out];
        events_out = (events_out + 1) % MAX_EVENTS;
        SPIN_LOCK_ISR_GIVE(&events_lock);

        post_event(event_handler, info);
    }
    post_event(event_error, NULL);
    return NULL;
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
        Context * stopped_ctx = context_find_from_pid(pid, 1);
        if (stopped_ctx != NULL) {
            /* TODO: need call back for vxdbgCont()
             * assert(!stopped_ctx->stopped) can fail if a task is resumed outside TCF agent.
             */
            assert(!stopped_ctx->stopped);
            assert(!stopped_ctx->exited);
            stopped_ctx->pending_step = 0;
            trace(LOG_CONTEXT, "context: exited ctx %#lx, id %#x", stopped_ctx, EXT(stopped_ctx)->pid);
            release_error_report(EXT(stopped_ctx)->regs_error);
            loc_free(EXT(stopped_ctx)->regs);
            EXT(stopped_ctx)->regs_error = NULL;
            EXT(stopped_ctx)->regs = NULL;
            send_context_exited_event(stopped_ctx);
        }
    }
}

void init_contexts_sys_dep(void) {
    SPIN_LOCK_ISR_INIT(&events_lock, 0);
    if ((events_signal = semCInitialize(events_signal_mem, SEM_Q_FIFO, 0)) == NULL) {
        check_error(errno);
    }
    vxdbg_clnt_id = vxdbgClntRegister(EVT_BP);
    if (vxdbg_clnt_id == NULL) {
        check_error(errno);
    }
    context_extension_offset = context_extension(sizeof(ContextExtensionVxWorks));
    taskCreateHookAdd((FUNCPTR)task_create_hook);
    vxdbgHookAdd(vxdbg_clnt_id, EVT_BP, vxdbg_event_hook);
    vxdbgHookAdd(vxdbg_clnt_id, EVT_TRACE, vxdbg_event_hook);
    check_error(pthread_create(&events_thread, &pthread_create_attr, event_thread_func, NULL));
    add_waitpid_listener(waitpid_listener, NULL);
    ini_context_pid_hash();
}

#endif  /* if ENABLE_DebugContext */
#endif /* _WRS_KERNEL */
