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
 * Target service implementation: run control (TCF name RunControl)
 */

#include <config.h>

#if SERVICE_RunControl

#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <errno.h>
#include <assert.h>
#include <framework/protocol.h>
#include <framework/channel.h>
#include <framework/json.h>
#include <framework/context.h>
#include <framework/myalloc.h>
#include <framework/trace.h>
#include <framework/events.h>
#include <framework/exceptions.h>
#include <framework/signames.h>
#include <services/runctrl.h>
#include <services/breakpoints.h>
#include <main/cmdline.h>

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

typedef struct ContextExtensionRC {
    int pending_safe_event; /* safe events are waiting for this context to be stopped */
    int intercepted;        /* context is reported to a host as suspended */
    int intercepted_by_bp;
} ContextExtensionRC;

static size_t context_extension_offset = 0;

#define EXT(ctx) (ctx ? ((ContextExtensionRC *)((char *)(ctx) + context_extension_offset)) : NULL)

typedef struct SafeEvent {
    Context * mem;
    EventCallBack * done;
    void * arg;
    struct SafeEvent * next;
} SafeEvent;

typedef struct GetContextArgs {
    Channel * c;
    char token[256];
    Context * ctx;
} GetContextArgs;

static SafeEvent * safe_event_list = NULL;
static SafeEvent * safe_event_last = NULL;
static int safe_event_pid_count = 0;
static int run_ctrl_lock_cnt = 0;
static int stop_all_timer_cnt = 0;
static int stop_all_timer_posted = 0;
static int run_safe_events_posted = 0;

static TCFBroadcastGroup * broadcast_group = NULL;

#if defined(__linux__)
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
    json_write_string(out, ctx->id);

    if (ctx->parent != NULL) {
        write_stream(out, ',');
        json_write_string(out, "ParentID");
        write_stream(out, ':');
        json_write_string(out, ctx->parent->id);
    }

    if (ctx->creator != NULL) {
        write_stream(out, ',');
        json_write_string(out, "CreatorID");
        write_stream(out, ':');
        json_write_string(out, ctx->creator->id);
    }

    write_stream(out, ',');
    json_write_string(out, "ProcessID");
    write_stream(out, ':');
    json_write_string(out, ctx->mem->id);

#if defined(__linux__)
    if (!ctx->exiting && ctx->parent == NULL) {
        write_stream(out, ',');
        json_write_string(out, "File");
        write_stream(out, ':');
        json_write_string(out, get_executable(id2pid(ctx->id, NULL)));
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
    else {
        write_stream(out, ',');
        json_write_string(out, "IsContainer");
        write_stream(out, ':');
        json_write_boolean(out, 1);
    }

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
    const char * reason = NULL;
    char ** bp_ids = NULL;
    ContextExtensionRC * ext = EXT(ctx);

    assert(!ctx->exited);

    if (!ext->intercepted) {
        write_stringz(out, "0");
        write_stringz(out, "null");
        write_stringz(out, "null");
        return;
    }

    /* Number: PC */
    json_write_ulong(out, get_regs_PC(ctx));
    write_stream(out, 0);

    /* String: Reason */
    if (ext->intercepted_by_bp == 1) bp_ids = get_context_breakpoint_ids(ctx);
    if (bp_ids != NULL) reason = "Breakpoint";
    else reason = context_suspend_reason(ctx);
    json_write_string(out, reason);
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
    if (bp_ids != NULL) {
        int i = 0;
        if (!fst) write_stream(out, ',');
        json_write_string(out, "BPs");
        write_stream(out, ':');
        write_stream(out, '[');
        while (bp_ids[i] != NULL) {
            if (i > 0) write_stream(out, ',');
            json_write_string(out, bp_ids[i++]);
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
        LINK * l;
        int cnt = 0;
        for (l = context_root.next; l != &context_root; l = l->next) {
            Context * ctx = ctxl2ctxp(l);
            if (ctx->exited) continue;
            if (ctx->parent != NULL) continue;
            if (cnt > 0) write_stream(&c->out, ',');
            json_write_string(&c->out, ctx->id);
            cnt++;
        }
    }
    else if (id[0] == 'P') {
        Context * parent = id2ctx(id);
        if (parent != NULL) {
            LINK * l;
            int cnt = 0;
            for (l = parent->children.next; l != &parent->children; l = l->next) {
                Context * ctx = cldl2ctxp(l);
                assert(ctx->parent == parent);
                if (!ctx->exited) {
                    if (cnt > 0) write_stream(&c->out, ',');
                    json_write_string(&c->out, ctx->id);
                    cnt++;
                }
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
    ContextExtensionRC * ext = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    ctx = id2ctx(id);
    ext = EXT(ctx);

    if (ctx == NULL || !context_has_state(ctx)) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    write_errno(&c->out, err);

    json_write_boolean(&c->out, ctx != NULL && ext->intercepted);
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

static void send_event_context_resumed(Context * ctx);

static void resume_params_callback(InputStream * inp, const char * name, void * args) {
    int * err = (int *)args;
    /* Current agent implementation does not support resume parameters */
    json_skip_object(inp);
    *err = ERR_UNSUPPORTED;
}

static int context_continue_recursive(Context * ctx) {
    int err = 0;

    if (context_has_state(ctx)) {
        send_event_context_resumed(ctx);
        if (run_ctrl_lock_cnt == 0 && context_continue(ctx) < 0) err = errno;
    }
    else {
        LINK * l;
        for (l = ctx->children.next; l != &ctx->children; l = l->next) {
            Context * x = cldl2ctxp(l);
            ContextExtensionRC * y = EXT(x);
            if (x->exited || context_has_state(x) && !y->intercepted) continue;
            context_continue_recursive(x);
        }
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
        ContextExtensionRC * ext = EXT(ctx);

        if (ctx == NULL) {
            err = ERR_INV_CONTEXT;
        }
        else if (ctx->exited) {
            err = ERR_ALREADY_EXITED;
        }
        else if (context_has_state(ctx) && !ext->intercepted) {
            err = ERR_ALREADY_RUNNING;
        }
        else if (count != 1) {
            err = EINVAL;
        }
        else if (context_has_state(ctx) && mode == RM_STEP_INTO) {
            send_event_context_resumed(ctx);
            if (run_ctrl_lock_cnt > 0) {
                ctx->pending_step = 1;
            }
            else if (context_single_step(ctx) < 0) {
                err = errno;
            }
            else {
                assert(!ext->intercepted);
                ctx->pending_intercept = 1;
            }
        }
        else if (mode == RM_RESUME) {
            if (context_continue_recursive(ctx) < 0) err = errno;
        }
        else {
            err = EINVAL;
        }
    }
    send_simple_result(c, token, err);
}

static void send_event_context_suspended(Context * ctx);

int suspend_debug_context(Context * ctx) {
    ContextExtensionRC * ext = EXT(ctx);

    if (ctx->exited) {
        /* do nothing */
    }
    else if (context_has_state(ctx)) {
        if (!ctx->stopped) {
            assert(!ext->intercepted);
            if (!ctx->exiting) {
                ctx->pending_intercept = 1;
                if (!ctx->pending_step && context_stop(ctx) < 0) return -1;
            }
        }
        else if (!ext->intercepted) {
            if (run_ctrl_lock_cnt > 0) {
                ctx->pending_intercept = 1;
            }
            else {
                ctx->pending_step = 0;
                send_event_context_suspended(ctx);
            }
        }
    }
    else {
        LINK * l;
        for (l = ctx->children.next; l != &ctx->children; l = l->next) {
            suspend_debug_context(cldl2ctxp(l));
        }
    }
    return 0;
}

static void command_suspend(char * token, Channel * c) {
    char id[256];
    Context * ctx = NULL;
    ContextExtensionRC * ext = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    ctx = id2ctx(id);
    ext = EXT(ctx);

    if (ctx == NULL) {
        err = ERR_INV_CONTEXT;
    }
    else if (ctx->exited) {
        err = ERR_ALREADY_EXITED;
    }
    else if (ext->intercepted) {
        err = ERR_ALREADY_STOPPED;
    }
    else if (suspend_debug_context(ctx) < 0) {
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
    ContextExtensionRC * ext = EXT(ctx);
    Channel * c = args->channel;
    LINK * l = ctx->children.next;
    while (l != &ctx->children) {
        Context * x = cldl2ctxp(l);
        ContextExtensionRC * y = EXT(x);
        if (!x->exited) {
            if (y->intercepted) send_event_context_resumed(x);
            x->pending_intercept = 0;
            x->pending_signals |= 1 << SIGKILL;
        }
        l = l->next;
    }
    if (ext->intercepted) send_event_context_resumed(ctx);
    ctx->pending_intercept = 0;
    ctx->pending_signals |= 1 << SIGKILL;
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

static void send_event_context_added(Context * ctx) {
    OutputStream * out = &broadcast_group->out;

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

static void send_event_context_changed(Context * ctx) {
    OutputStream * out = &broadcast_group->out;

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

static void send_event_context_removed(Context * ctx) {
    OutputStream * out = &broadcast_group->out;

    write_stringz(out, "E");
    write_stringz(out, RUN_CONTROL);
    write_stringz(out, "contextRemoved");

    /* <array of context IDs> */
    write_stream(out, '[');
    json_write_string(out, ctx->id);
    write_stream(out, ']');
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void send_event_context_suspended(Context * ctx) {
    OutputStream * out = &broadcast_group->out;
    ContextExtensionRC * ext = EXT(ctx);

    assert(ctx->stopped);
    assert(!ctx->exited);
    assert(!ext->intercepted);
    assert(!ctx->pending_step);
    ext->intercepted = 1;
    ctx->pending_intercept = 0;
    if (get_context_breakpoint_ids(ctx) != NULL) ext->intercepted_by_bp++;

    write_stringz(out, "E");
    write_stringz(out, RUN_CONTROL);
    write_stringz(out, "contextSuspended");

    /* String: Context ID */
    json_write_string(out, ctx->id);
    write_stream(out, 0);

    write_context_state(out, ctx);
    write_stream(out, MARKER_EOM);
}

static void send_event_context_resumed(Context * ctx) {
    OutputStream * out = &broadcast_group->out;
    ContextExtensionRC * ext = EXT(ctx);

    assert(ext->intercepted);
    assert(!ctx->pending_intercept);
    assert(!ctx->pending_step);
    ext->intercepted = 0;

    write_stringz(out, "E");
    write_stringz(out, RUN_CONTROL);
    write_stringz(out, "contextResumed");

    /* String: Context ID */
    json_write_string(out, ctx->id);
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void send_event_context_exception(Context * ctx) {
    char buf[128];
    OutputStream * out = &broadcast_group->out;

    write_stringz(out, "E");
    write_stringz(out, RUN_CONTROL);
    write_stringz(out, "contextException");

    /* String: Context ID */
    json_write_string(out, ctx->id);
    write_stream(out, 0);

    /* String: Human readable description of the exception */
    snprintf(buf, sizeof(buf), "Signal %d", ctx->signal);
    json_write_string(out, buf);
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

int is_all_stopped(Context * mem) {
    LINK * l;
    assert(mem->mem == mem);
    for (l = context_root.next; l != &context_root; l = l->next) {
        Context * ctx = ctxl2ctxp(l);
        if (ctx->exited || ctx->exiting) continue;
        if (!context_has_state(ctx)) continue;
        if (ctx->mem != mem) continue;
        if (!ctx->stopped) return 0;
    }
    return 1;
}

static void run_safe_events(void * arg);

static void stop_all_timer(void * args) {
    stop_all_timer_posted = 0;
    stop_all_timer_cnt++;
    run_safe_events_posted++;
    post_event(run_safe_events, NULL);
}

static void run_safe_events(void * arg) {
    LINK * l;
    Context * mem;

    run_safe_events_posted--;
    if (run_safe_events_posted > 0) return;

    safe_event_pid_count = 0;

    if (run_ctrl_lock_cnt == 0) {
        assert(safe_event_list == NULL);
        stop_all_timer_cnt = 0;
        l = context_root.next;
        while (l != &context_root) {
            int n = 0;
            Context * ctx = ctxl2ctxp(l);
            ContextExtensionRC * ext = EXT(ctx);
            l = l->next; /* Context can be deleted in the loop */
            ext->pending_safe_event = 0;
            if (ctx->exited) continue;
            if (!ctx->stopped) continue;
            if (ext->intercepted) continue;
            if (ctx->pending_intercept) {
                ctx->pending_step = 0;
                send_event_context_suspended(ctx);
                continue;
            }
            assert(!ctx->pending_intercept);
            if (ctx->pending_step) {
                ctx->pending_step = 0;
                n = context_single_step(ctx);
                if (n >= 0) ctx->pending_intercept = 1;
            }
            else {
                n = context_continue(ctx);
            }
            if (n < 0) {
                int error = errno;
                trace(LOG_ALWAYS, "error: can't resume %s; error %d: %s",
                    ctx->id, error, errno_to_str(error));
            }
            if (run_ctrl_lock_cnt > 0) break;
        }
        return;
    }

    if (safe_event_list == NULL) return;
    mem = safe_event_list->mem;
    context_lock(mem);

    l = context_root.next;
    while (l != &context_root) {
        Context * ctx = ctxl2ctxp(l);
        ContextExtensionRC * ext = EXT(ctx);
        l = l->next;
        ext->pending_safe_event = 0;
        if (ctx->mem != mem) continue;
        if (ctx->exited || ctx->exiting) continue;
        if (ctx->stopped || !context_has_state(ctx)) continue;
        if (stop_all_timer_cnt >= STOP_ALL_MAX_CNT) {
            trace(LOG_ALWAYS, "error: can't temporary stop %s; error: timeout", ctx->id);
            ctx->exiting = 1;
        }
        else {
            if (!ctx->pending_step || stop_all_timer_cnt >= STOP_ALL_MAX_CNT / 2) {
                if (context_stop(ctx) < 0) {
                    trace(LOG_ALWAYS, "error: can't temporary stop %s; error %d: %s",
                        ctx->id, errno, errno_to_str(errno));
                }
                assert(!ctx->stopped);
            }
            ext->pending_safe_event = 1;
            safe_event_pid_count++;
        }
    }

    while (safe_event_list) {
        Trap trap;
        SafeEvent * i = safe_event_list;
        if (i->mem != mem) {
            assert(run_safe_events_posted == 0);
            run_safe_events_posted++;
            post_event(run_safe_events, NULL);
            break;
        }
        if (safe_event_pid_count > 0) {
            if (!stop_all_timer_posted) {
                stop_all_timer_posted = 1;
                post_event_with_delay(stop_all_timer, NULL, STOP_ALL_TIMEOUT);
            }
            break;
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
        run_ctrl_unlock();
        context_unlock(i->mem);
        loc_free(i);
    }
    context_unlock(mem);
}

static void check_safe_events(Context * ctx) {
    ContextExtensionRC * ext = EXT(ctx);
    assert(ctx->stopped || ctx->exited);
    assert(ext->pending_safe_event);
    assert(safe_event_pid_count > 0);
    ext->pending_safe_event = 0;
    safe_event_pid_count--;
    if (safe_event_pid_count == 0 && run_ctrl_lock_cnt > 0) {
        run_safe_events_posted++;
        post_event(run_safe_events, NULL);
    }
}

void post_safe_event(Context * mem, EventCallBack * done, void * arg) {
    SafeEvent * i = (SafeEvent *)loc_alloc_zero(sizeof(SafeEvent));
    assert(mem->mem == mem);
    run_ctrl_lock();
    context_lock(mem);
    if (safe_event_list == NULL) {
        run_safe_events_posted++;
        post_event(run_safe_events, NULL);
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
        assert(safe_event_list == NULL);
        cmdline_suspend();
    }
    run_ctrl_lock_cnt++;
}

void run_ctrl_unlock(void) {
    assert(run_ctrl_lock_cnt > 0);
    run_ctrl_lock_cnt--;
    if (run_ctrl_lock_cnt == 0) {
        assert(safe_event_list == NULL);
        cmdline_resume();
        /* Lazily continue execution of temporary stopped contexts */
        run_safe_events_posted++;
        post_event(run_safe_events, NULL);
    }
}

static void event_context_created(Context * ctx, void * client_data) {
    assert(!ctx->exited);
    assert(!ctx->stopped);
    send_event_context_added(ctx);
}

static void event_context_changed(Context * ctx, void * client_data) {
    send_event_context_changed(ctx);
}

static void event_context_stopped(Context * ctx, void * client_data) {
    ContextExtensionRC * ext = EXT(ctx);
    assert(ctx->stopped);
    assert(!ctx->exited);
    assert(!ext->intercepted);
    if (ctx->stopped_by_bp) evaluate_breakpoint(ctx);
    if (ext->pending_safe_event) check_safe_events(ctx);
    if (ctx->stopped_by_exception) send_event_context_exception(ctx);
    if (!ext->intercepted && run_ctrl_lock_cnt == 0 && run_safe_events_posted < 4) {
        /* Lazily continue execution of temporary stopped contexts */
        run_safe_events_posted++;
        post_event(run_safe_events, NULL);
    }
}

static void event_context_started(Context * ctx, void * client_data) {
    ContextExtensionRC * ext = EXT(ctx);
    assert(!ctx->stopped);
    if (ext->intercepted) send_event_context_resumed(ctx);
    ext->intercepted_by_bp = 0;
    if (safe_event_list) {
        if (!ctx->pending_step) {
            context_stop(ctx);
        }
        if (!ext->pending_safe_event) {
            ext->pending_safe_event = 1;
            safe_event_pid_count++;
        }
    }
}

static void event_context_exited(Context * ctx, void * client_data) {
    ContextExtensionRC * ext = EXT(ctx);
    assert(!ctx->stopped);
    assert(!ext->intercepted);
    if (ext->pending_safe_event) check_safe_events(ctx);
    send_event_context_removed(ctx);
}

void ini_run_ctrl_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    static ContextEventListener listener = {
        event_context_created,
        event_context_exited,
        event_context_stopped,
        event_context_started,
        event_context_changed
    };
    broadcast_group = bcg;
    add_context_event_listener(&listener, NULL);
    context_extension_offset = context_extension(sizeof(ContextExtensionRC));
    add_command_handler(proto, RUN_CONTROL, "getContext", command_get_context);
    add_command_handler(proto, RUN_CONTROL, "getChildren", command_get_children);
    add_command_handler(proto, RUN_CONTROL, "getState", command_get_state);
    add_command_handler(proto, RUN_CONTROL, "resume", command_resume);
    add_command_handler(proto, RUN_CONTROL, "suspend", command_suspend);
    add_command_handler(proto, RUN_CONTROL, "terminate", command_terminate);
}

#else

#include <services/runctrl.h>
#include <assert.h>

void post_safe_event(Context * mem, EventCallBack * done, void * arg) {
    assert(mem->mem == mem);
    post_event(done, arg);
}

#endif /* SERVICE_RunControl */
