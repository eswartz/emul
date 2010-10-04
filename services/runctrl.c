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

/* TODO: add support for containerSuspended, containerResumed events */

static const char RUN_CONTROL[] = "RunControl";

typedef struct ContextExtensionRC {
    int pending_safe_event; /* safe events are waiting for this context to be stopped */
    int intercepted;        /* context is reported to a host as suspended */
    int intercepted_by_bp;
    ContextAddress step_range_start;
    ContextAddress step_range_end;
    int stepping_in_range;
    int safe_single_step;   /* not zero if the context is performing a "safe" single instruction step */
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

static void run_safe_events(void * arg);

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

    if (ctx->mem != NULL) {
        write_stream(out, ',');
        json_write_string(out, "ProcessID");
        write_stream(out, ':');
        json_write_string(out, ctx->mem->id);
    }

    if (ctx->name != NULL) {
        write_stream(out, ',');
        json_write_string(out, "Name");
        write_stream(out, ':');
        json_write_string(out, ctx->name);
    }

#if defined(__linux__)
    if (ctx->parent == NULL) {
        pid_t pid = id2pid(ctx->id, NULL);
        Context * x = context_find_from_pid(pid, 1);
        if (x != NULL && !x->exiting && !x->exited) {
            write_stream(out, ',');
            json_write_string(out, "File");
            write_stream(out, ':');
            json_write_string(out, get_executable(pid));
        }
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
        json_write_long(out, (1 << RM_RESUME) | (1 << RM_STEP_INTO) | (1 << RM_STEP_INTO_RANGE));
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
        const char * name = signal_name(ctx->signal);
        const char * desc = signal_description(ctx->signal);
        json_write_string(out, "Signal");
        write_stream(out, ':');
        json_write_long(out, ctx->signal);
        if (name != NULL) {
            write_stream(out, ',');
            json_write_string(out, "SignalName");
            write_stream(out, ':');
            json_write_string(out, name);
        }
        if (desc != NULL) {
            write_stream(out, ',');
            json_write_string(out, "SignalDescription");
            write_stream(out, ':');
            json_write_string(out, desc);
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
            if (ctx->parent != NULL) continue;
            if (ctx->exited) continue;
            if (cnt > 0) write_stream(&c->out, ',');
            json_write_string(&c->out, ctx->id);
            cnt++;
        }
    }
    else {
        Context * parent = id2ctx(id);
        if (parent != NULL) {
            LINK * l;
            int cnt = 0;
            for (l = parent->children.next; l != &parent->children; l = l->next) {
                Context * ctx = cldl2ctxp(l);
                assert(ctx->parent == parent);
                if (ctx->exited) continue;
                if (cnt > 0) write_stream(&c->out, ',');
                json_write_string(&c->out, ctx->id);
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

typedef struct ResumeParams {
    ContextAddress range_start;
    ContextAddress range_end;
    int error;
} ResumeParams;

static void resume_params_callback(InputStream * inp, const char * name, void * x) {
    ResumeParams * args = (ResumeParams *)x;
    if (strcmp(name, "RangeStart") == 0) args->range_start = (ContextAddress)json_read_uint64(inp);
    else if (strcmp(name, "RangeEnd") == 0) args->range_end = (ContextAddress)json_read_uint64(inp);
    else {
        json_skip_object(inp);
        args->error = ERR_UNSUPPORTED;
    }
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
    ResumeParams args;

    memset(&args, 0, sizeof(args));
    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    mode = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    count = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (peek_stream(&c->inp) != MARKER_EOM) {
        json_read_struct(&c->inp, resume_params_callback, &args);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        err = args.error;
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
        else if (context_has_state(ctx) && (mode == RM_STEP_INTO || mode == RM_STEP_INTO_RANGE)) {
            if (mode == RM_STEP_INTO_RANGE) {
                ext->step_range_start = args.range_start;
                ext->step_range_end = args.range_end;
            }
            else {
                ext->step_range_start = get_regs_PC(ctx);
                ext->step_range_end = ext->step_range_start + 1;
            }
            ext->stepping_in_range = 1;
            send_event_context_resumed(ctx);
            assert(!ext->intercepted);
            if (run_ctrl_lock_cnt == 0 && run_safe_events_posted < 4) {
                run_safe_events_posted++;
                post_event(run_safe_events, NULL);
            }
        }
        else if (mode == RM_RESUME) {
            ext->step_range_start = 0;
            ext->step_range_end = 0;
            ext->stepping_in_range = 0;
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
                if (!ext->safe_single_step && context_stop(ctx) < 0) return -1;
            }
        }
        else if (!ext->intercepted) {
            if (run_ctrl_lock_cnt > 0) {
                ctx->pending_intercept = 1;
            }
            else {
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

static void event_terminate(void * args) {
    Context * ctx = (Context *)args;
    ContextExtensionRC * ext = EXT(ctx);
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
}

int terminate_debug_context(Context * ctx) {
    int err = 0;
    if (ctx == NULL) {
        err = ERR_INV_CONTEXT;
    }
    else if (ctx->exited) {
        err = ERR_ALREADY_EXITED;
    }
    else {
        context_lock(ctx);
        post_safe_event(ctx->mem, event_terminate, ctx);
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

    if (terminate_debug_context(id2ctx(id)) != 0) err = errno;

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
    assert(!ext->safe_single_step);
    ext->intercepted = 1;
    ext->step_range_start = 0;
    ext->step_range_end = 0;
    ext->stepping_in_range = 0;
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
    assert(!ext->safe_single_step);
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
    OutputStream * out = &broadcast_group->out;

    write_stringz(out, "E");
    write_stringz(out, RUN_CONTROL);
    write_stringz(out, "contextException");

    /* String: Context ID */
    json_write_string(out, ctx->id);
    write_stream(out, 0);

    /* String: Human readable description of the exception */
    if (ctx->exception_description) {
        json_write_string(out, ctx->exception_description);
    }
    else {
        char buf[128];
        const char * desc = signal_description(ctx->signal);
        if (desc == NULL) desc = signal_name(ctx->signal);
        snprintf(buf, sizeof(buf), desc == NULL ? "Signal %d" : "Signal %d: %s", ctx->signal, desc);
        json_write_string(out, buf);
    }
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
            if (ext->stepping_in_range && !ctx->pending_intercept) {
                ContextAddress pc = get_regs_PC(ctx);
                if (pc < ext->step_range_start || pc >= ext->step_range_end) {
                    ext->stepping_in_range = 0;
                    ctx->pending_intercept = 1;
                }
            }
            if (ctx->pending_intercept) {
                send_event_context_suspended(ctx);
                continue;
            }
            assert(!ctx->pending_intercept);
            if (ext->stepping_in_range) {
                n = context_single_step(ctx);
            }
            else {
                n = context_continue(ctx);
            }
            if (n < 0) {
                int error = errno;
                trace(LOG_ALWAYS, "error: can't resume %s; error %d: %s",
                    ctx->id, error, errno_to_str(error));
                send_event_context_suspended(ctx);
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
            if (stop_all_timer_cnt == STOP_ALL_MAX_CNT / 2) {
                const char * msg = ext->safe_single_step ? "finish single step" : "stop";
                trace(LOG_ALWAYS, "warning: waiting too long for context %s to %s", ctx->id, msg);
            }
            if (!ext->safe_single_step || stop_all_timer_cnt >= STOP_ALL_MAX_CNT / 2) {
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
            assert(run_ctrl_lock_cnt > 0);
            if (run_safe_events_posted == 0) {
                run_safe_events_posted++;
                post_event(run_safe_events, NULL);
            }
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

int safe_context_single_step(Context * ctx) {
    int res = 0;
    ContextExtensionRC * ext = EXT(ctx);
    assert(run_ctrl_lock_cnt != 0);
    assert(ext->safe_single_step == 0);
    ext->safe_single_step = 1;
    res = context_single_step(ctx);
    if (res < 0) ext->safe_single_step = 0;
    return res;
}

void run_ctrl_lock(void) {
    if (run_ctrl_lock_cnt == 0) {
        assert(safe_event_list == NULL);
#if ENABLE_Cmdline
        cmdline_suspend();
#endif
    }
    run_ctrl_lock_cnt++;
}

void run_ctrl_unlock(void) {
    assert(run_ctrl_lock_cnt > 0);
    run_ctrl_lock_cnt--;
    if (run_ctrl_lock_cnt == 0) {
        assert(safe_event_list == NULL);
#if ENABLE_Cmdline
        cmdline_resume();
#endif
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
    ext->safe_single_step = 0;
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
        if (!ext->safe_single_step && !ctx->exiting) {
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
    ext->safe_single_step = 0;
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
