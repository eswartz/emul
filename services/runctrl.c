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
 * Target service implementation: run control (TCF name RunControl)
 */

#include "config.h"

#if SERVICE_RunControl

#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <errno.h>
#include <assert.h>
#include "runctrl.h"
#include "protocol.h"
#include "channel.h"
#include "json.h"
#include "context.h"
#include "myalloc.h"
#include "trace.h"
#include "events.h"
#include "exceptions.h"
#include "breakpoints.h"
#include "cmdline.h"

#define RM_RESUME                   0
#define RM_STEP_OVER                1
#define RM_STEP_INTO                2
#define RM_STEP_OVER_LINE           3
#define RM_STEP_INTO_LINE           4
#define RM_STEP_OUT                 5
#define RM_REVERSE_RESUME           6
#define RM_REVERSE_STEP_OVER        7
#define RM_REVERSE_STEP_INTO        8
#define RM_REVERSE_STEP_OVER_LINE   9
#define RM_REVERSE_STEP_INTO_LINE   10
#define RM_REVERSE_STEP_OUT         11
#define RM_STEP_OVER_RANGE          12
#define RM_STEP_INTO_RANGE          13
#define RM_REVERSE_STEP_OVER_RANGE  14
#define RM_REVERSE_STEP_INTO_RANGE  15

#define STOP_ALL_TIMEOUT 1000000
#define STOP_ALL_MAX_CNT 20

static const char RUN_CONTROL[] = "RunControl";
static TCFSuspendGroup * suspend_group = NULL;

typedef struct SafeEvent SafeEvent;

struct SafeEvent {
    pid_t mem;
    EventCallBack * done;
    void * arg;
    SafeEvent * next;
};

typedef struct GetContextArgs GetContextArgs;

struct GetContextArgs {
    Channel * c;
    char token[256];
    Context * ctx;
};

static SafeEvent * safe_event_list = NULL;
static SafeEvent * safe_event_last = NULL;
static int safe_event_pid_count = 0;
static uintptr_t safe_event_generation = 0;
static int run_ctrl_lock_cnt = 0;

#if !defined(WIN32) && !defined(_WRS_KERNEL)
static char * get_executable(pid_t pid) {
    static char s[FILE_PATH_SIZE + 1];
    char tmpbuf[100];
    int sz;

    snprintf(tmpbuf, sizeof(tmpbuf), "/proc/%d/exe", pid);
    if ((sz = readlink(tmpbuf, s, FILE_PATH_SIZE)) < 0) {
        trace(LOG_ALWAYS, "error: readlink() failed; pid %d, error %d %s",
            pid, errno, errno_to_str(errno));
        return NULL;
    }
    s[sz] = 0;
    return s;
}
#endif

static void write_context(OutputStream * out, Context * ctx) {
    assert(!ctx->exited);

    write_stream(out, '{');

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, ctx2id(ctx));

    if (ctx->parent != NULL) {
        write_stream(out, ',');
        json_write_string(out, "ParentID");
        write_stream(out, ':');
        json_write_string(out, ctx2id(ctx->parent));
    }

#if !defined(_WRS_KERNEL)
    write_stream(out, ',');
    json_write_string(out, "ProcessID");
    write_stream(out, ':');
    json_write_string(out, pid2id(ctx->mem, 0));
#endif

#if !defined(WIN32) && !defined(_WRS_KERNEL)
    if (!ctx->exiting && ctx->parent == NULL) {
        write_stream(out, ',');
        json_write_string(out, "File");
        write_stream(out, ':');
        json_write_string(out, get_executable(ctx->pid));
    }
#endif

    write_stream(out, ',');
    json_write_string(out, "CanSuspend");
    write_stream(out, ':');
    json_write_boolean(out, 1);

    write_stream(out, ',');
    json_write_string(out, "CanResume");
    write_stream(out, ':');
    if (context_has_state(ctx)) {
        json_write_long(out, (1 << RM_RESUME) | (1 << RM_STEP_INTO));
    }
    else {
        json_write_long(out, 1 << RM_RESUME);
    }

    if (context_has_state(ctx)) {
        write_stream(out, ',');
        json_write_string(out, "HasState");
        write_stream(out, ':');
        json_write_boolean(out, 1);
    }

#if !defined(_WRS_KERNEL)
    if (ctx->parent == NULL) {
        write_stream(out, ',');
        json_write_string(out, "IsContainer");
        write_stream(out, ':');
        json_write_boolean(out, 1);
    }
#endif

#ifdef WIN32
    if (ctx->parent == NULL)
#endif
    {
        write_stream(out, ',');
        json_write_string(out, "CanTerminate");
        write_stream(out, ':');
        json_write_boolean(out, 1);
    }

    write_stream(out, '}');
}

static void write_context_state(OutputStream * out, Context * ctx) {
    int fst = 1;
    assert(!ctx->exited);

    if (!ctx->intercepted) {
        write_stringz(out, "0");
        write_stringz(out, "null");
        write_stringz(out, "null");
        return;
    }

    /* Number: PC */
    json_write_ulong(out, get_regs_PC(ctx->regs));
    write_stream(out, 0);

    /* String: Reason */
    json_write_string(out, context_suspend_reason(ctx));
    write_stream(out, 0);

    /* Object: Additional context state info */
    write_stream(out, '{');
    if (ctx->signal) {
        json_write_string(out, "Signal");
        write_stream(out, ':');
        json_write_long(out, ctx->signal);
        if (signal_name(ctx->signal)) {
            write_stream(out, ',');
            json_write_string(out, "SignalName");
            write_stream(out, ':');
            json_write_string(out, signal_name(ctx->signal));
        }
        fst = 0;
    }
    if (ctx->bp_ids != NULL && ctx->bp_ids[0] != NULL) {
        int i = 0;
        if (!fst) write_stream(out, ',');
        json_write_string(out, "BPs");
        write_stream(out, ':');
        write_stream(out, '[');
        while (ctx->bp_ids[i] != NULL) {
            if (i > 0) write_stream(out, ',');
            json_write_string(out, ctx->bp_ids[i++]);
        }
        write_stream(out, ']');
        fst = 0;
    }
    write_stream(out, '}');
    write_stream(out, 0);
}

static void event_get_context(void * arg) {
    GetContextArgs * s = (GetContextArgs *)arg;
    Channel * c = s->c;
    Context * ctx = s->ctx;

    if (!is_channel_closed(c)) {
        int err = 0;

        write_stringz(&c->out, "R");
        write_stringz(&c->out, s->token);

        if (ctx->exited) err = ERR_ALREADY_EXITED;
        write_errno(&c->out, err);

        if (err == 0) {
            write_context(&c->out, ctx);
            write_stream(&c->out, 0);
        }
        else {
            write_stringz(&c->out, "null");
        }

        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
    }
    channel_unlock(c);
    context_unlock(ctx);
    loc_free(s);
}

static void command_get_context(char * token, Channel * c) {
    int err = 0;
    char id[256];
    Context * ctx = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    ctx = id2ctx(id);

    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    if (err) {
        write_stringz(&c->out, "R");
        write_stringz(&c->out, token);
        write_errno(&c->out, err);
        write_stringz(&c->out, "null");
        write_stream(&c->out, MARKER_EOM);
    }
    else {
        /* Need to stop everything to access context properties.
         * In particular, proc FS access can fail when process is running.
         */
        GetContextArgs * s = (GetContextArgs *)loc_alloc_zero(sizeof(GetContextArgs));
        s->c = c;
        channel_lock(c);
        strcpy(s->token, token);
        s->ctx = ctx;
        context_lock(ctx);
        post_safe_event(ctx->mem, event_get_context, s);
    }
}

static void command_get_children(char * token, Channel * c) {
    char id[256];

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    write_errno(&c->out, 0);

    write_stream(&c->out, '[');
    if (id[0] == 0) {
        LINK * qp;
        int cnt = 0;
        for (qp = context_root.next; qp != &context_root; qp = qp->next) {
            Context * ctx = ctxl2ctxp(qp);
            if (ctx->exited) continue;
            if (ctx->parent != NULL) continue;
            if (cnt > 0) write_stream(&c->out, ',');
            json_write_string(&c->out, ctx2id(ctx));
            cnt++;
        }
    }
    else if (id[0] == 'P') {
        Context * parent = id2ctx(id);
        if (parent != NULL) {
            LINK * qp;
            int cnt = 0;
            for (qp = parent->children.next; qp != &parent->children; qp = qp->next) {
                Context * ctx = cldl2ctxp(qp);
                assert(!ctx->exited);
                assert(ctx->parent == parent);
                if (cnt > 0) write_stream(&c->out, ',');
                json_write_string(&c->out, ctx2id(ctx));
                cnt++;
            }
        }
    }
    write_stream(&c->out, ']');
    write_stream(&c->out, 0);

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_state(char * token, Channel * c) {
    char id[256];
    Context * ctx = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    ctx = id2ctx(id);
    if (ctx == NULL || !context_has_state(ctx)) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    write_errno(&c->out, err);

    json_write_boolean(&c->out, ctx != NULL && ctx->intercepted);
    write_stream(&c->out, 0);

    if (err) {
        write_stringz(&c->out, "0");
        write_stringz(&c->out, "null");
        write_stringz(&c->out, "null");
    }
    else {
        write_context_state(&c->out, ctx);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void send_simple_result(Channel * c, char * token, int err) {
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void send_event_context_resumed(OutputStream * out, Context * ctx);

static void resume_params_callback(InputStream * inp, char * name, void * args) {
    int * err = (int *)args;
    /* Current agent implementation does not support resume parameters */
    loc_free(json_skip_object(inp));
    *err = ERR_UNSUPPORTED;
}

static int context_continue_recursive(OutputStream * out, Context * ctx) {
    LINK * qp;
    int err = 0;
    if (context_has_state(ctx)) {
        send_event_context_resumed(out, ctx);
        if (run_ctrl_lock_cnt == 0 && context_continue(ctx) < 0) err = errno;
    }
    for (qp = ctx->children.next; qp != &ctx->children; qp = qp->next) {
        Context * cld = cldl2ctxp(qp);
        if (cld->exited || context_has_state(cld) && !cld->intercepted) continue;
        context_continue_recursive(out, cld);
    }
    if (err == 0) return 0;
    errno = err;
    return -1;
}

static void command_resume(char * token, Channel * c) {
    char id[256];
    long mode;
    long count;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    mode = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    count = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (peek_stream(&c->inp) != MARKER_EOM) {
        json_read_struct(&c->inp, resume_params_callback, &err);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    }
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    if (err == 0) {
        Context * ctx = id2ctx(id);

        if (ctx == NULL) {
            err = ERR_INV_CONTEXT;
        }
        else if (ctx->exited) {
            err = ERR_ALREADY_EXITED;
        }
        else if (context_has_state(ctx) && !ctx->intercepted) {
            err = ERR_ALREADY_RUNNING;
        }
        else if (ctx->regs_error) {
            err = set_error_report_errno(ctx->regs_error);
        }
        else if (count != 1) {
            err = EINVAL;
        }
        else if (context_has_state(ctx) && mode == RM_STEP_INTO) {
            send_event_context_resumed(&c->bcg->out, ctx);
            if (context_single_step(ctx) < 0) {
                err = errno;
            }
            else {
                assert(!ctx->intercepted);
                ctx->pending_intercept = 1;
            }
        }
        else if (mode == RM_RESUME) {
            if (context_continue_recursive(&c->bcg->out, ctx) < 0) err = errno;
        }
        else {
            err = EINVAL;
        }
    }
    send_simple_result(c, token, err);
}

static void send_event_context_suspended(OutputStream * out, Context * ctx);

int suspend_debug_context(TCFBroadcastGroup * bcg, Context * ctx) {
    LINK * qp;
    if (context_has_state(ctx) && !ctx->exited && !ctx->exiting) {
        if (!ctx->stopped) {
            assert(!ctx->intercepted);
            ctx->pending_intercept = 1;
            if (!ctx->pending_step && context_stop(ctx) < 0) return -1;
        }
        else if (!ctx->intercepted) {
            if (ctx->event_notification || ctx->stepping_over_bp != NULL) {
                ctx->pending_intercept = 1;
            }
            else {
                send_event_context_suspended(&bcg->out, ctx);
            }
        }
    }
    for (qp = ctx->children.next; qp != &ctx->children; qp = qp->next) {
        suspend_debug_context(bcg, cldl2ctxp(qp));
    }
    return 0;
}

static void command_suspend(char * token, Channel * c) {
    char id[256];
    Context * ctx;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    ctx = id2ctx(id);

    if (ctx == NULL) {
        err = ERR_INV_CONTEXT;
    }
    else if (ctx->exited) {
        err = ERR_ALREADY_EXITED;
    }
    else if (ctx->intercepted) {
        err = ERR_ALREADY_STOPPED;
    }
    else if (ctx->stopped) {
        send_event_context_suspended(&c->bcg->out, ctx);
    }
    else if (suspend_debug_context(c->bcg, ctx) < 0) {
        err = errno;
    }

    send_simple_result(c, token, err);
}

typedef struct TerminateArgs {
    Context * ctx;
    Channel * channel;
} TerminateArgs;

static void event_terminate(void * x) {
    TerminateArgs * args = (TerminateArgs *)x;
    Context * ctx = args->ctx;
    Channel * c = args->channel;
    LINK * qp = ctx->children.next;
    while (qp != &ctx->children) {
        Context * x = cldl2ctxp(qp);
        if (x->intercepted) send_event_context_resumed(&c->bcg->out, x);
        x->pending_intercept = 0;
        x->pending_signals |= 1 << SIGKILL;
        qp = qp->next;
    }
    if (ctx->intercepted) send_event_context_resumed(&c->bcg->out, ctx);
    ctx->pending_intercept = 0;
    ctx->pending_signals |= 1 << SIGKILL;
    flush_stream(&c->bcg->out);
    context_unlock(ctx);
    channel_unlock(c);
    loc_free(args);
}

int terminate_debug_context(Channel * c, Context * ctx) {
    int err = 0;
    if (ctx == NULL) {
        err = ERR_INV_CONTEXT;
    }
    else if (ctx->exited) {
        err = ERR_ALREADY_EXITED;
    }
    else {
        TerminateArgs * args = (TerminateArgs *)loc_alloc(sizeof(TerminateArgs));
        args->ctx = ctx;
        args->channel = c;
        context_lock(ctx);
        channel_lock(c);
        post_safe_event(ctx->mem, event_terminate, args);
    }
    if (err) {
        errno = err;
        return -1;
    }
    return 0;
}

static void command_terminate(char * token, Channel * c) {
    char id[256];
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (terminate_debug_context(c, id2ctx(id)) != 0) err = errno;

    send_simple_result(c, token, err);
}

static void send_event_context_added(OutputStream * out, Context * ctx) {
    write_stringz(out, "E");
    write_stringz(out, RUN_CONTROL);
    write_stringz(out, "contextAdded");

    /* <array of context data> */
    write_stream(out, '[');
    write_context(out, ctx);
    write_stream(out, ']');
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void send_event_context_changed(OutputStream * out, Context * ctx) {
    write_stringz(out, "E");
    write_stringz(out, RUN_CONTROL);
    write_stringz(out, "contextChanged");

    /* <array of context data> */
    write_stream(out, '[');
    write_context(out, ctx);
    write_stream(out, ']');
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void send_event_context_removed(OutputStream * out, Context * ctx) {
    write_stringz(out, "E");
    write_stringz(out, RUN_CONTROL);
    write_stringz(out, "contextRemoved");

    /* <array of context IDs> */
    write_stream(out, '[');
    json_write_string(out, ctx2id(ctx));
    write_stream(out, ']');
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void send_event_context_suspended(OutputStream * out, Context * ctx) {
    assert(ctx->stopped);
    assert(!ctx->exited);
    assert(!ctx->intercepted);
    assert(!ctx->pending_step);
    ctx->intercepted = 1;
    ctx->pending_intercept = 0;

    write_stringz(out, "E");
    write_stringz(out, RUN_CONTROL);
    write_stringz(out, "contextSuspended");

    /* String: Context ID */
    json_write_string(out, ctx2id(ctx));
    write_stream(out, 0);

    write_context_state(out, ctx);
    write_stream(out, MARKER_EOM);
}

static void send_event_context_resumed(OutputStream * out, Context * ctx) {
    assert(ctx->intercepted);
    assert(!ctx->pending_intercept);
    ctx->intercepted = 0;
    if (ctx->bp_ids != NULL) {
        loc_free(ctx->bp_ids);
        ctx->bp_ids = NULL;
    }

    write_stringz(out, "E");
    write_stringz(out, RUN_CONTROL);
    write_stringz(out, "contextResumed");

    /* String: Context ID */
    json_write_string(out, ctx2id(ctx));
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void send_event_context_exception(OutputStream * out, Context * ctx) {
    char buf[128];

    write_stringz(out, "E");
    write_stringz(out, RUN_CONTROL);
    write_stringz(out, "contextException");

    /* String: Context ID */
    json_write_string(out, ctx2id(ctx));
    write_stream(out, 0);

    /* String: Human readable description of the exception */
    snprintf(buf, sizeof(buf), "Signal %d", ctx->signal);
    json_write_string(out, buf);
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

int is_all_stopped(pid_t mem) {
    LINK * qp;
    for (qp = context_root.next; qp != &context_root; qp = qp->next) {
        Context * ctx = ctxl2ctxp(qp);
        if (ctx->exited || ctx->exiting) continue;
        if (!context_has_state(ctx)) continue;
        if (mem > 0 && ctx->mem != mem) continue;
        if (!ctx->stopped) return 0;
    }
    return 1;
}

static void run_safe_events(void * arg) {
    LINK * qp;
    pid_t mem;

    if ((uintptr_t)arg != safe_event_generation) return;

    if (run_ctrl_lock_cnt == 0) {
        assert(safe_event_list == NULL);
        assert(safe_event_pid_count == 0);

        if (channels_get_message_count(suspend_group) > 0) {
            post_event(run_safe_events, (void *)++safe_event_generation);
            return;
        }

        for (qp = context_root.next; qp != &context_root; qp = qp->next) {
            Context * ctx = ctxl2ctxp(qp);
            if (ctx->exited) continue;
            if (!ctx->stopped) continue;
            if (ctx->intercepted) continue;
            assert(!ctx->pending_intercept);
            context_continue(ctx);
        }
        return;
    }

    safe_event_pid_count = 0;
    mem = safe_event_list ? safe_event_list->mem : 0;

    for (qp = context_root.next; qp != &context_root; qp = qp->next) {
        Context * ctx = ctxl2ctxp(qp);
        if (ctx->exited || ctx->exiting || ctx->stopped || !context_has_state(ctx)) {
            ctx->pending_safe_event = 0;
            continue;
        }
        if (mem > 0 && ctx->mem != mem) {
            ctx->pending_safe_event = 0;
            continue;
        }
        if (!ctx->pending_step || ctx->pending_safe_event >= STOP_ALL_MAX_CNT / 2) {
            if (context_stop(ctx) < 0) {
                int error = errno;
#ifdef _WRS_KERNEL
                if (error == S_vxdbgLib_INVALID_CTX) {
                    /* Most often this means that context has exited,
                     * but exit event is not delivered yet.
                     * Not an error. */
                    error = 0;
                }
#endif
                if (error) {
                    trace(LOG_ALWAYS, "error: can't temporary stop pid %d; error %d: %s",
                        ctx->pid, error, errno_to_str(error));
                }
            }
            assert(!ctx->stopped);
        }
        if (ctx->pending_safe_event >= STOP_ALL_MAX_CNT) {
            trace(LOG_ALWAYS, "error: can't temporary stop pid %d; error: timeout", ctx->pid);
            ctx->exiting = 1;
            ctx->pending_safe_event = 0;
        }
        else {
            ctx->pending_safe_event++;
            safe_event_pid_count++;
        }
    }

    while (safe_event_list && (uintptr_t)arg == safe_event_generation) {
        Trap trap;
        SafeEvent * i = safe_event_list;
        if (safe_event_pid_count > 0) {
            post_event_with_delay(run_safe_events, (void *)++safe_event_generation, STOP_ALL_TIMEOUT);
            return;
        }
        if (mem > 0 && i->mem != mem) {
            post_event(run_safe_events, (void *)++safe_event_generation);
            return;
        }
        assert(is_all_stopped(i->mem));
        safe_event_list = i->next;
        if (set_trap(&trap)) {
            i->done(i->arg);
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Unhandled exception in \"safe\" event dispatch: %d %s",
                  trap.error, errno_to_str(trap.error));
        }
        loc_free(i);
        run_ctrl_unlock();
    }
}

static void check_safe_events(Context * ctx) {
    assert(ctx->stopped || ctx->exited);
    assert(ctx->pending_safe_event);
    assert(run_ctrl_lock_cnt > 0);
    assert(safe_event_pid_count > 0);
    ctx->pending_safe_event = 0;
    safe_event_pid_count--;
    if (safe_event_pid_count == 0) {
        post_event(run_safe_events, (void *)++safe_event_generation);
    }
}

void post_safe_event(int mem, EventCallBack * done, void * arg) {
    SafeEvent * i = (SafeEvent *)loc_alloc_zero(sizeof(SafeEvent));
    run_ctrl_lock();
    if (run_ctrl_lock_cnt > 1 && safe_event_list == NULL) {
        post_event(run_safe_events, (void *)++safe_event_generation);
    }
    i->mem = mem;
    i->done = done;
    i->arg = arg;
    if (safe_event_list == NULL) safe_event_list = i;
    else safe_event_last->next = i;
    safe_event_last = i;
}

void run_ctrl_lock(void) {
    if (run_ctrl_lock_cnt == 0) {
        assert(safe_event_pid_count == 0);
        assert(safe_event_list == NULL);
        cmdline_suspend();
        post_event(run_safe_events, (void *)++safe_event_generation);
    }
    run_ctrl_lock_cnt++;
}

void run_ctrl_unlock(void) {
    assert(run_ctrl_lock_cnt > 0);
    run_ctrl_lock_cnt--;
    if (run_ctrl_lock_cnt == 0) {
        cmdline_resume();
        /* Lazily continue execution of temporary stopped contexts */
        post_event(run_safe_events, (void *)++safe_event_generation);
    }
}

static void event_context_created(Context * ctx, void * client_data) {
    TCFBroadcastGroup * bcg = (TCFBroadcastGroup *)client_data;
    assert(!ctx->exited);
    assert(!ctx->intercepted);
    assert(!ctx->stopped);
    send_event_context_added(&bcg->out, ctx);
    flush_stream(&bcg->out);
}

static void event_context_changed(Context * ctx, void * client_data) {
    TCFBroadcastGroup * bcg = (TCFBroadcastGroup *)client_data;

    send_event_context_changed(&bcg->out, ctx);
    flush_stream(&bcg->out);
}

static void event_context_stopped(Context * ctx, void * client_data) {
    TCFBroadcastGroup * bcg = (TCFBroadcastGroup *)client_data;

    assert(ctx->stopped);
    assert(!ctx->intercepted);
    assert(!ctx->exited);
    if (ctx->pending_safe_event) check_safe_events(ctx);
    if (ctx->stopped_by_exception) {
        send_event_context_exception(&bcg->out, ctx);
    }
    if (ctx->pending_intercept) {
        send_event_context_suspended(&bcg->out, ctx);
    }
    if (!ctx->intercepted && run_ctrl_lock_cnt == 0) {
        context_continue(ctx);
    }
    flush_stream(&bcg->out);
}

static void event_context_started(Context * ctx, void * client_data) {
    TCFBroadcastGroup * bcg = (TCFBroadcastGroup *)client_data;

    assert(!ctx->stopped);
    if (ctx->intercepted) {
        send_event_context_resumed(&bcg->out, ctx);
    }
    if (run_ctrl_lock_cnt > 0) {
        if (!ctx->pending_step) {
            context_stop(ctx);
        }
        if (!ctx->pending_safe_event) {
            ctx->pending_safe_event = 1;
            safe_event_pid_count++;
        }
    }
    flush_stream(&bcg->out);
}

static void event_context_exited(Context * ctx, void * client_data) {
    TCFBroadcastGroup * bcg = (TCFBroadcastGroup *)client_data;

    assert(!ctx->stopped);
    assert(!ctx->intercepted);
    if (ctx->pending_safe_event) check_safe_events(ctx);
    send_event_context_removed(&bcg->out, ctx);
    flush_stream(&bcg->out);
}

void ini_run_ctrl_service(Protocol * proto, TCFBroadcastGroup * bcg, TCFSuspendGroup * spg) {
    static ContextEventListener listener = {
        event_context_created,
        event_context_exited,
        event_context_stopped,
        event_context_started,
        event_context_changed
    };
    suspend_group = spg;
    add_context_event_listener(&listener, bcg);
    add_command_handler(proto, RUN_CONTROL, "getContext", command_get_context);
    add_command_handler(proto, RUN_CONTROL, "getChildren", command_get_children);
    add_command_handler(proto, RUN_CONTROL, "getState", command_get_state);
    add_command_handler(proto, RUN_CONTROL, "resume", command_resume);
    add_command_handler(proto, RUN_CONTROL, "suspend", command_suspend);
    add_command_handler(proto, RUN_CONTROL, "terminate", command_terminate);
}

#else

#include "runctrl.h"

void post_safe_event(int mem, EventCallBack * done, void * arg) {
    post_event(done, arg);
}

#endif /* SERVICE_RunControl */
