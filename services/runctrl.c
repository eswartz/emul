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
#include <framework/cache.h>
#include <services/runctrl.h>
#include <services/breakpoints.h>
#include <services/linenumbers.h>
#include <services/stacktrace.h>
#include <services/symbols.h>
#include <main/cmdline.h>

#define EN_STEP_OVER (SERVICE_Breakpoints && SERVICE_StackTrace && ENABLE_Symbols)
#define EN_STEP_LINE (SERVICE_LineNumbers)

#define STOP_ALL_TIMEOUT 1000000
#define STOP_ALL_MAX_CNT 20

typedef struct Listener {
    RunControlEventListener * func;
    void * args;
} Listener;

static Listener * listeners = NULL;
static unsigned listener_cnt = 0;
static unsigned listener_max = 0;

static const char RUN_CONTROL[] = "RunControl";

typedef struct ContextExtensionRC {
    int pending_safe_event; /* safe events are waiting for this context to be stopped */
    int intercepted;        /* context is reported to a host as suspended */
    int intercepted_by_bp;  /* intercept counter - only first intercept should report "Breakpoint" reason */
    int intercept_group;
    int reverse_run;
    int step_mode;
    int step_cnt;
    int step_line_cnt;
    ContextAddress step_range_start;
    ContextAddress step_range_end;
    ContextAddress step_frame;
    ContextAddress step_bp_addr;
    BreakpointInfo * step_bp_info;
    CodeArea * step_code_area;
    ErrorReport * step_error;
    const char * step_done;
    Channel * step_channel;
    int step_continue_mode;
    int safe_single_step;   /* not zero if the context is performing a "safe" single instruction step */
    LINK link;
} ContextExtensionRC;

static size_t context_extension_offset = 0;

#define EXT(ctx) (ctx ? ((ContextExtensionRC *)((char *)(ctx) + context_extension_offset)) : NULL)
#define link2ctx(lnk) ((Context *)((char *)(lnk) - offsetof(ContextExtensionRC, link) - context_extension_offset))

typedef struct SafeEvent {
    Context * grp;
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

static void write_context(OutputStream * out, Context * ctx) {
    int md, modes;
    Context * grp = context_get_group(ctx, CONTEXT_GROUP_INTERCEPT);
    int has_state = context_has_state(ctx);

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
    json_write_string(out, context_get_group(ctx, CONTEXT_GROUP_PROCESS)->id);

    if (ctx->name != NULL) {
        write_stream(out, ',');
        json_write_string(out, "Name");
        write_stream(out, ':');
        json_write_string(out, ctx->name);
    }

    write_stream(out, ',');
    json_write_string(out, "CanSuspend");
    write_stream(out, ':');
    json_write_boolean(out, 1);

    write_stream(out, ',');
    json_write_string(out, "CanResume");
    write_stream(out, ':');
    modes = 0;
    for (md = 0; md < RM_TERMINATE; md++) {
        if (context_can_resume(ctx, md)) modes |= 1 << md;
    }
    if (!has_state) {
        modes &= (1 << RM_RESUME) | (1 << RM_REVERSE_RESUME);
    }
    else {
        if (modes & (1 << RM_STEP_INTO)) {
            modes |= (1 << RM_STEP_INTO_RANGE);
#if EN_STEP_OVER
            modes |= (1 << RM_STEP_OVER);
            modes |= (1 << RM_STEP_OVER_RANGE);
            modes |= (1 << RM_STEP_OUT);
#endif
#if EN_STEP_LINE
            modes |= (1 << RM_STEP_INTO_LINE);
#endif
#if EN_STEP_OVER && EN_STEP_LINE
            modes |= (1 << RM_STEP_OVER_LINE);
#endif
        }
        if (modes & (1 << RM_REVERSE_STEP_INTO)) {
            modes |= (1 << RM_REVERSE_STEP_INTO_RANGE);
#if EN_STEP_OVER
            modes |= (1 << RM_REVERSE_STEP_OVER);
            modes |= (1 << RM_REVERSE_STEP_OVER_RANGE);
            modes |= (1 << RM_REVERSE_STEP_OUT);
#endif
#if EN_STEP_LINE
            modes |= (1 << RM_REVERSE_STEP_INTO_LINE);
#endif
#if EN_STEP_OVER && EN_STEP_LINE
            modes |= (1 << RM_REVERSE_STEP_OVER_LINE);
#endif
        }
    }
    json_write_long(out, modes);

    if (has_state) {
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

    if (context_can_resume(ctx, RM_TERMINATE)) {
        write_stream(out, ',');
        json_write_string(out, "CanTerminate");
        write_stream(out, ':');
        json_write_boolean(out, 1);
    }

    if (grp != ctx) {
        write_stream(out, ',');
        json_write_string(out, "RCGroup");
        write_stream(out, ':');
        json_write_string(out, grp->id);
    }

    write_stream(out, '}');
}

static int get_current_pc(Context * ctx, ContextAddress * res) {
    size_t i;
    uint8_t buf[8];
    ContextAddress pc = 0;
    RegisterDefinition * def = get_PC_definition(ctx);
    if (def == NULL) {
        set_errno(ERR_OTHER, "Program counter register not found");
        return -1;
    }
    assert(def->size <= sizeof(buf));
    if (context_read_reg(ctx, def, 0, def->size, buf) < 0) return -1;
    for (i = 0; i < def->size; i++) {
        pc = pc << 8;
        pc |= buf[def->big_endian ? i : def->size - i - 1];
    }
    *res = pc;
    return 0;
}

static void write_context_state(OutputStream * out, Context * ctx) {
    int fst = 1;
    const char * reason = NULL;
    char ** bp_ids = NULL;
    ContextExtensionRC * ext = EXT(ctx);
    ContextAddress pc = 0;
    int pc_error = 0;

    assert(!ctx->exited);

    if (!ext->intercepted) {
        write_stringz(out, "0");
        write_stringz(out, "null");
        write_stringz(out, "null");
        return;
    }

    /* Number: PC */
    if (get_current_pc(ctx, &pc) < 0) pc_error = errno;
    json_write_ulong(out, pc);
    write_stream(out, 0);

    /* String: Reason */
    if (ext->intercepted_by_bp == 1) bp_ids = get_context_breakpoint_ids(ctx);
    if (bp_ids != NULL) reason = REASON_BREAKPOINT;
    else if (ctx->exception_description != NULL) reason = ctx->exception_description;
    else if (ext->step_error != NULL) reason = errno_to_str(set_error_report_errno(ext->step_error));
    else if (ext->step_done != NULL) reason = ext->step_done;
    else reason = context_suspend_reason(ctx);
    json_write_string(out, reason);
    write_stream(out, 0);

    /* Object: Additional context state info */
    write_stream(out, '{');
    if (ext->step_error == NULL && ext->step_done == NULL && ctx->signal) {
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
    if (pc_error) {
        if (!fst) write_stream(out, ',');
        json_write_string(out, "PCError");
        write_stream(out, ':');
        write_error_object(out, pc_error);
        fst = 0;
    }
#if ENABLE_ContextStateProperties
    {
        /* Back-end context state properties */
        int cnt = 0;
        const char ** names = NULL;
        const char ** values = NULL;
        if (context_get_state_properties(ctx, &names, &values, &cnt) == 0) {
            while (cnt > 0) {
                if (*values != NULL) {
                    if (!fst) write_stream(out, ',');
                    json_write_string(out, *names++);
                    write_stream(out, ':');
                    json_write_string(out, *values++);
                    fst = 0;
                }
                cnt--;
            }
        }
    }
#endif
    write_stream(out, '}');
    write_stream(out, 0);
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

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
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

static int resume_context_tree(Context * ctx) {
    if (!context_has_state(ctx)) {
        LINK * l;
        for (l = ctx->children.next; l != &ctx->children; l = l->next) {
            Context * x = cldl2ctxp(l);
            if (!x->exited) resume_context_tree(x);
        }
    }
    else if (EXT(ctx)->intercepted) {
        Context * grp = context_get_group(ctx, CONTEXT_GROUP_INTERCEPT);
        send_event_context_resumed(grp);
        assert(!EXT(ctx)->intercepted);
        if (run_ctrl_lock_cnt == 0 && run_safe_events_posted < 4) {
            run_safe_events_posted++;
            post_event(run_safe_events, NULL);
        }
    }
    return 0;
}

static void free_code_area(CodeArea * area) {
    loc_free(area->directory);
    loc_free(area->file);
    loc_free(area);
}

static void cancel_step_mode(Context * ctx) {
    ContextExtensionRC * ext = EXT(ctx);

    if (ext->step_channel) {
        channel_unlock(ext->step_channel);
        ext->step_channel = NULL;
    }
    if (ext->step_bp_info != NULL) {
        destroy_eventpoint(ext->step_bp_info);
        ext->step_bp_info = NULL;
    }
    if (ext->step_code_area != NULL) {
        free_code_area(ext->step_code_area);
        ext->step_code_area = NULL;
    }
    ext->step_cnt = 0;
    ext->step_line_cnt = 0;
    ext->step_range_start = 0;
    ext->step_range_end = 0;
    ext->step_frame = 0;
    ext->step_bp_addr = 0;
    ext->step_continue_mode = RM_RESUME;
    ext->step_mode = RM_RESUME;
}

static void start_step_mode(Context * ctx, Channel * c, int mode, ContextAddress range_start, ContextAddress range_end) {
    ContextExtensionRC * ext = EXT(ctx);

    cancel_step_mode(ctx);
    if (ext->step_error) {
        release_error_report(ext->step_error);
        ext->step_error = NULL;
    }
    if (c != NULL) {
        ext->step_channel = c;
        channel_lock(ext->step_channel);
    }
    else if (ext->step_channel != NULL) {
        channel_unlock(ext->step_channel);
        ext->step_channel = NULL;
    }
    ext->step_done = NULL;
    ext->step_mode = mode;
    ext->step_range_start = range_start;
    ext->step_range_end = range_end;
}

int continue_debug_context(Context * ctx, Channel * c,
                           int mode, int count, ContextAddress range_start, ContextAddress range_end) {
    ContextExtensionRC * ext = EXT(ctx);
    Context * grp = context_get_group(ctx, CONTEXT_GROUP_INTERCEPT);
    int err = 0;

    EXT(grp)->reverse_run = 0;
    switch (mode) {
    case RM_REVERSE_RESUME:
    case RM_REVERSE_STEP_OVER:
    case RM_REVERSE_STEP_INTO:
    case RM_REVERSE_STEP_OVER_LINE:
    case RM_REVERSE_STEP_INTO_LINE:
    case RM_REVERSE_STEP_OUT:
    case RM_REVERSE_STEP_OVER_RANGE:
    case RM_REVERSE_STEP_INTO_RANGE:
    case RM_REVERSE_UNTIL_ACTIVE:
        EXT(grp)->reverse_run = 1;
        break;
    }

    if (context_has_state(ctx)) start_step_mode(ctx, c, mode, range_start, range_end);

    if (ctx->exited) {
        err = ERR_ALREADY_EXITED;
    }
    else if (context_has_state(ctx) && !ext->intercepted) {
        err = ERR_ALREADY_RUNNING;
    }
    else if (count != 1) {
        err = EINVAL;
    }
    else if (resume_context_tree(ctx) < 0) {
        err = errno;
    }

    assert(err || !ext->intercepted);
    if (err) {
        cancel_step_mode(ctx);
        errno = err;
        return -1;
    }

    return 0;
}

static void command_resume(char * token, Channel * c) {
    char id[256];
    int mode;
    long count;
    int err = 0;
    ResumeParams args;
    Context * ctx = NULL;

    memset(&args, 0, sizeof(args));
    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    mode = (int)json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    count = json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (peek_stream(&c->inp) != MARKER_EOM) {
        json_read_struct(&c->inp, resume_params_callback, &args);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        err = args.error;
    }
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (err == 0 && (ctx = id2ctx(id)) == NULL) err = ERR_INV_CONTEXT;
    if (err == 0 && continue_debug_context(ctx, c, mode, count, args.range_start, args.range_end) < 0) err = errno;
    send_simple_result(c, token, err);
}

int suspend_debug_context(Context * ctx) {
    ContextExtensionRC * ext = EXT(ctx);

    if (ctx->exited) {
        /* do nothing */
    }
    else if (context_has_state(ctx)) {
        assert(!ctx->stopped || !ext->safe_single_step);
        if (!ctx->stopped) {
            assert(!ext->intercepted);
            if (!ctx->exiting) {
                ctx->pending_intercept = 1;
                if (!ext->safe_single_step && context_stop(ctx) < 0) return -1;
            }
        }
        else if (!ext->intercepted) {
            ctx->pending_intercept = 1;
            if (run_ctrl_lock_cnt == 0 && run_safe_events_posted < 4) {
                run_safe_events_posted++;
                post_event(run_safe_events, NULL);
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

static void terminate_context_tree(Context * ctx) {
    if (ctx->exited) return;
    if (context_can_resume(ctx, RM_TERMINATE)) {
        ContextExtensionRC * ext = EXT(ctx);
        cancel_step_mode(ctx);
        ctx->pending_intercept = 0;
        ext->step_mode = RM_TERMINATE;
        resume_context_tree(ctx);
    }
    else {
        LINK * l = ctx->children.next;
        while (l != &ctx->children) {
            Context * x = cldl2ctxp(l);
            terminate_context_tree(x);
            l = l->next;
        }
    }
}

static void event_terminate(void * args) {
    Context * ctx = (Context *)args;
    terminate_context_tree(ctx);
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
        post_safe_event(ctx, event_terminate, ctx);
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

static void notify_context_intercepted(Context * ctx) {
    unsigned i;
    ContextExtensionRC * ext = EXT(ctx);
    assert(!ext->intercepted);
    ext->intercepted = 1;
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->func->context_intercepted == NULL) continue;
        l->func->context_intercepted(ctx, l->args);
    }
}

static void notify_context_released(Context * ctx) {
    unsigned i;
    ContextExtensionRC * ext = EXT(ctx);
    assert(ext->intercepted);
    ext->intercepted = 0;
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->func->context_released == NULL) continue;
        l->func->context_released(ctx, l->args);
    }
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
    ContextExtensionRC * ext = EXT(ctx);

    if (ext->intercepted) notify_context_released(ctx);

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

static void send_event_context_suspended(void) {
    LINK p;
    Context * ctx = NULL;
    LINK * l = context_root.next;
    list_init(&p);
    while (l != &context_root) {
        Context * x = ctxl2ctxp(l);
        l = l->next;
        if (x->pending_intercept && x->stopped) {
            ContextExtensionRC * e = EXT(x);
            assert(!x->exited);
            assert(!e->intercepted);
            assert(!e->safe_single_step);
            cancel_step_mode(x);
            notify_context_intercepted(x);
            x->pending_intercept = 0;
            list_add_last(&e->link, &p);
            if (get_context_breakpoint_ids(x) != NULL) e->intercepted_by_bp++;
            if (ctx != NULL) {
                if (ctx->stopped_by_exception) continue;
                if (!x->stopped_by_exception) {
                    if (EXT(ctx)->intercepted_by_bp == 1) continue;
                }
            }
            ctx = x;
        }
    }

    if (!list_is_empty(&p)) {
        OutputStream * out = &broadcast_group->out;

        write_stringz(out, "E");
        write_stringz(out, RUN_CONTROL);
        write_stringz(out, p.next == p.prev ? "contextSuspended" : "containerSuspended");

        json_write_string(out, ctx->id);
        write_stream(out, 0);

        write_context_state(out, ctx);

        if (p.next != p.prev) {
            l = p.next;
            write_stream(out, '[');
            while (l != &p) {
                Context * x = link2ctx(l);
                if (l != p.next) write_stream(out, ',');
                json_write_string(out, x->id);
                l = l->next;
            }
            write_stream(out, ']');
            write_stream(out, 0);
        }

        write_stream(out, MARKER_EOM);
    }
}

static void send_event_context_resumed(Context * grp) {
    LINK * l = NULL;
    LINK p;

    list_init(&p);
    l = context_root.next;
    while (l != &context_root) {
        Context * ctx = ctxl2ctxp(l);
        ContextExtensionRC * ext = EXT(ctx);
        if (ext->intercepted && context_get_group(ctx, CONTEXT_GROUP_INTERCEPT) == grp) {
            assert(!ctx->pending_intercept);
            assert(!ext->safe_single_step);
            notify_context_released(ctx);
            list_add_last(&ext->link, &p);
        }
        l = l->next;
    }

    if (!list_is_empty(&p)) {
        OutputStream * out = &broadcast_group->out;

        write_stringz(out, "E");
        write_stringz(out, RUN_CONTROL);

        if (p.next == p.prev) {
            Context * ctx = link2ctx(p.next);
            write_stringz(out, "contextResumed");
            json_write_string(out, ctx->id);
        }
        else {
            l = p.next;
            write_stringz(out, "containerResumed");
            write_stream(out, '[');
            while (l != &p) {
                Context * ctx = link2ctx(l);
                if (l != p.next) write_stream(out, ',');
                json_write_string(out, ctx->id);
                l = l->next;
            }
            write_stream(out, ']');
        }
        write_stream(out, 0);

        write_stream(out, MARKER_EOM);
    }
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
    else if (ctx->signal > 0) {
        char buf[128];
        const char * desc = signal_description(ctx->signal);
        if (desc == NULL) desc = signal_name(ctx->signal);
        snprintf(buf, sizeof(buf), desc == NULL ? "Signal %d" : "Signal %d: %s", ctx->signal, desc);
        json_write_string(out, buf);
    }
    else {
        json_write_string(out, context_suspend_reason(ctx));
    }
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

int is_all_stopped(Context * ctx) {
    LINK * l;
    Context * grp = context_get_group(ctx, CONTEXT_GROUP_STOP);
    for (l = context_root.next; l != &context_root; l = l->next) {
        Context * ctx = ctxl2ctxp(l);
        if (ctx->exited || ctx->exiting) continue;
        if (!context_has_state(ctx)) continue;
        if (context_get_group(ctx, CONTEXT_GROUP_STOP) != grp) continue;
        if (!ctx->stopped) return 0;
    }
    return 1;
}

#if EN_STEP_LINE
static int is_same_line(CodeArea * x, CodeArea * y) {
    if (x == NULL || y == NULL) return 0;
    if (x->start_line != y->start_line) return 0;
    if (x->directory != y->directory && (x->directory == NULL || strcmp(x->directory, y->directory))) return 0;
    if (x->file != y->file && (x->file == NULL || strcmp(x->file, y->file))) return 0;
    return 1;
}

static void update_step_machine_code_area(CodeArea * area, void * args) {
    ContextExtensionRC * ext = (ContextExtensionRC *)args;
    if (ext->step_code_area != NULL) return;
    ext->step_code_area = (CodeArea *)loc_alloc(sizeof(CodeArea));
    *ext->step_code_area = *area;
    if (area->directory != NULL) ext->step_code_area->directory = loc_strdup(area->directory);
    if (area->file != NULL) ext->step_code_area->file = loc_strdup(area->file);
}
#endif

#if EN_STEP_OVER
static void step_machine_breakpoint(Context * ctx, void * args) {
}
#endif

static int update_step_machine_state(Context * ctx) {
    ContextExtensionRC * ext = EXT(ctx);
    ContextAddress addr = 0;

    if (!context_has_state(ctx) || ctx->exited || ctx->pending_intercept) {
        cancel_step_mode(ctx);
        return 0;
    }

    if (get_current_pc(ctx, &addr) < 0) {
        int n = errno;
        if (ctx->stopped && get_error_code(n) == ERR_NOT_ACTIVE) {
            switch (ext->step_mode) {
            case RM_STEP_OVER:
            case RM_STEP_INTO:
            case RM_STEP_OVER_LINE:
            case RM_STEP_INTO_LINE:
            case RM_STEP_OUT:
            case RM_STEP_OVER_RANGE:
            case RM_STEP_INTO_RANGE:
                if (context_can_resume(ctx, ext->step_continue_mode = RM_UNTIL_ACTIVE)) return 0;
                break;
            case RM_REVERSE_STEP_OVER:
            case RM_REVERSE_STEP_INTO:
            case RM_REVERSE_STEP_OVER_LINE:
            case RM_REVERSE_STEP_INTO_LINE:
            case RM_REVERSE_STEP_OUT:
            case RM_REVERSE_STEP_OVER_RANGE:
            case RM_REVERSE_STEP_INTO_RANGE:
                if (context_can_resume(ctx, ext->step_continue_mode = RM_REVERSE_UNTIL_ACTIVE)) return 0;
                break;
            }
        }
        errno = n;
        return -1;
    }

    assert(ctx->stopped);
    assert(ctx->pending_intercept == 0);
    assert(ext->intercepted == 0);

#if EN_STEP_OVER
    {
        ContextAddress step_bp_addr = 0;

        switch (ext->step_mode) {
        case RM_STEP_OVER:
        case RM_STEP_OVER_RANGE:
        case RM_STEP_OVER_LINE:
        case RM_REVERSE_STEP_OVER:
        case RM_REVERSE_STEP_OVER_RANGE:
        case RM_REVERSE_STEP_OVER_LINE:
            if (ext->step_cnt == 0) {
                StackFrame * info = NULL;
                ext->step_frame = 0;
                if (get_frame_info(ctx, STACK_TOP_FRAME, &info) < 0) break;
                ext->step_frame = info->fp;
            }
            else if (addr < ext->step_range_start || addr >= ext->step_range_end) {
                StackFrame * info = NULL;
                int n = get_top_frame(ctx);
                if (n < 0) return -1;
                if (get_frame_info(ctx, n, &info) < 0) return -1;
                if (ext->step_frame != info->fp) {
                    while (n > 0) {
                        if (get_frame_info(ctx, --n, &info) < 0) return -1;
                        if (ext->step_frame == info->fp) {
                            uint64_t pc = 0;
                            if (read_reg_value(info, get_PC_definition(ctx), &pc) < 0) return -1;
                            step_bp_addr = (ContextAddress)pc;
                            break;
                        }
                    }
                }
                else {
                    switch (ext->step_mode) {
                    case RM_REVERSE_STEP_OVER:
                    case RM_REVERSE_STEP_OVER_RANGE:
                    case RM_REVERSE_STEP_OVER_LINE:
                        {
                            /* Workaround for GCC debug information that contains
                             * invalid stack frame information for function epilogues */
                            Symbol * sym_org = NULL;
                            Symbol * sym_now = NULL;
                            ContextAddress sym_org_addr = ext->step_range_start;
                            ContextAddress sym_now_addr = addr;
                            int anomaly =
                                    find_symbol_by_addr(ctx, n, sym_now_addr, &sym_now) >= 0 &&
                                    find_symbol_by_addr(ctx, n, sym_org_addr, &sym_org) >= 0 &&
                                    get_symbol_address(sym_now, &sym_now_addr) >= 0 &&
                                    get_symbol_address(sym_org, &sym_org_addr) >= 0 &&
                                    sym_now_addr != sym_org_addr;
                            if (anomaly) {
                                /* Step over the anomaly */
                                ext->step_continue_mode = RM_REVERSE_STEP_INTO;
                                return 0;
                            }
                        }
                        break;
                    }
                }
            }
            break;
        case RM_STEP_OUT:
        case RM_REVERSE_STEP_OUT:
            {
                StackFrame * info = NULL;
                int n = get_top_frame(ctx);
                if (n < 0) return -1;
                if (ext->step_cnt == 0) {
                    if (n == 0) {
                        set_errno(ERR_OTHER, "Cannot step out: no parent stack frame");
                        return -1;
                    }
                    if (get_frame_info(ctx, n - 1, &info) < 0) return -1;
                    ext->step_frame = info->fp;
                }
                while (n > 0) {
                    if (get_frame_info(ctx, --n, &info) < 0) return -1;
                    if (ext->step_frame == info->fp) {
                        uint64_t pc = 0;
                        if (read_reg_value(info, get_PC_definition(ctx), &pc) < 0) return -1;
                        step_bp_addr = (ContextAddress)pc;
                        break;
                    }
                }
            }
            break;
        }

        if (step_bp_addr) {
            switch (ext->step_mode) {
            case RM_STEP_OVER:
            case RM_STEP_OVER_RANGE:
            case RM_STEP_OVER_LINE:
            case RM_STEP_OUT:
                ext->step_continue_mode = RM_RESUME;
                break;
            case RM_REVERSE_STEP_OVER:
            case RM_REVERSE_STEP_OVER_RANGE:
            case RM_REVERSE_STEP_OVER_LINE:
            case RM_REVERSE_STEP_OUT:
                if (ext->step_line_cnt > 1) {
                    ext->step_continue_mode = RM_RESUME;
                }
                else {
                    step_bp_addr--;
                    ext->step_continue_mode = RM_REVERSE_RESUME;
                }
                break;
            default:
                errno = ERR_UNSUPPORTED;
                return -1;
            }
            if (ext->step_bp_info == NULL || ext->step_bp_addr != step_bp_addr) {
                char bf[64];
                snprintf(bf, sizeof(bf), "0x%" PRIX64, (uint64_t)step_bp_addr);
                if (ext->step_bp_info != NULL) destroy_eventpoint(ext->step_bp_info);
                ext->step_bp_info = create_eventpoint(bf, ctx, step_machine_breakpoint, NULL);
                ext->step_bp_addr = step_bp_addr;
            }
            return 0;
        }

        if (ext->step_bp_info != NULL) {
            destroy_eventpoint(ext->step_bp_info);
            ext->step_bp_info = NULL;
        }
    }
#endif /* EN_STEP_OVER */

    switch (ext->step_mode) {
    case RM_RESUME:
    case RM_REVERSE_RESUME:
        {
            int mode = ext->step_mode;
            cancel_step_mode(ctx);
            ext->step_continue_mode = mode;
        }
        return 0;
    case RM_STEP_INTO:
    case RM_STEP_OVER:
    case RM_REVERSE_STEP_INTO:
    case RM_REVERSE_STEP_OVER:
        if (ext->step_cnt == 0) {
            ext->step_range_start = addr;
            ext->step_range_end = addr + 1;
        }
    case RM_STEP_INTO_RANGE:
    case RM_STEP_OVER_RANGE:
    case RM_REVERSE_STEP_INTO_RANGE:
    case RM_REVERSE_STEP_OVER_RANGE:
        if (ext->step_cnt > 0 && (addr < ext->step_range_start || addr >= ext->step_range_end)) {
            ctx->pending_intercept = 1;
            ext->step_done = REASON_STEP;
            return 0;
        }
        break;
#if EN_STEP_LINE
    case RM_STEP_INTO_LINE:
    case RM_STEP_OVER_LINE:
    case RM_REVERSE_STEP_INTO_LINE:
    case RM_REVERSE_STEP_OVER_LINE:
        if (ext->step_cnt == 0) {
            if (ext->step_code_area == NULL) {
                if (address_to_line(ctx, addr, addr + 1, update_step_machine_code_area, ext) < 0) return -1;
            }
            if (ext->step_code_area != NULL) {
                ext->step_range_start = ext->step_code_area->start_address;
                ext->step_range_end = ext->step_code_area->end_address;
            }
            else {
                ext->step_range_start = addr;
                ext->step_range_end = addr + 1;
            }
        }
        else if (addr < ext->step_range_start || addr >= ext->step_range_end) {
            int same_line = 0;
            CodeArea * area = ext->step_code_area;
            if (area == NULL) {
                ctx->pending_intercept = 1;
                ext->step_done = REASON_STEP;
                return 0;
            }
            ext->step_code_area = NULL;
            if (address_to_line(ctx, addr, addr + 1, update_step_machine_code_area, ext) < 0) {
                free_code_area(area);
                return -1;
            }
            if (ext->step_code_area == NULL) {
                free_code_area(area);
                ctx->pending_intercept = 1;
                ext->step_done = REASON_STEP;
                return 0;
            }
            same_line = is_same_line(ext->step_code_area, area);
            free_code_area(area);
            if (!same_line) {
                if ((ext->step_mode != RM_REVERSE_STEP_INTO_LINE && ext->step_mode != RM_REVERSE_STEP_OVER_LINE) ||
                        (ext->step_line_cnt == 0 && addr == ext->step_code_area->start_address) ||
                        ext->step_line_cnt >= 2) {
                    ctx->pending_intercept = 1;
                    ext->step_done = REASON_STEP;
                    return 0;
                }
                /* Current IP is in the middle of a source line.
                 * Continue stepping to get to the beginning of the line */
                ext->step_line_cnt++;
            }
            ext->step_range_start = ext->step_code_area->start_address;
            ext->step_range_end = ext->step_code_area->end_address;
        }
        break;
#endif
    case RM_STEP_OUT:
    case RM_REVERSE_STEP_OUT:
        ctx->pending_intercept = 1;
        ext->step_done = REASON_STEP;
        return 0;
    default:
        errno = ERR_UNSUPPORTED;
        return -1;
    }

    if (ext->step_line_cnt > 1) {
        if (ext->step_mode == RM_REVERSE_STEP_INTO_LINE) ext->step_continue_mode = RM_STEP_INTO_LINE;
        if (ext->step_mode == RM_REVERSE_STEP_OVER_LINE) ext->step_continue_mode = RM_STEP_OVER_LINE;
    }
    else {
        ext->step_continue_mode = ext->step_mode;
        if (ext->step_line_cnt == 0 && context_can_resume(ctx, ext->step_continue_mode)) return 0;
    }

    switch (ext->step_continue_mode) {
    case RM_STEP_INTO_LINE:
        if (context_can_resume(ctx, ext->step_continue_mode = RM_STEP_INTO_RANGE)) return 0;
        break;
    case RM_STEP_OVER_LINE:
        if (context_can_resume(ctx, ext->step_continue_mode = RM_STEP_OVER_RANGE)) return 0;
        break;
    case RM_REVERSE_STEP_INTO_LINE:
        if (context_can_resume(ctx, ext->step_continue_mode = RM_REVERSE_STEP_INTO_RANGE)) return 0;
        break;
    case RM_REVERSE_STEP_OVER_LINE:
        if (context_can_resume(ctx, ext->step_continue_mode = RM_REVERSE_STEP_OVER_RANGE)) return 0;
        break;
    }

    switch (ext->step_continue_mode) {
    case RM_STEP_OVER:
        if (context_can_resume(ctx, ext->step_continue_mode = RM_STEP_INTO)) return 0;
        break;
    case RM_STEP_OVER_RANGE:
        if (context_can_resume(ctx, ext->step_continue_mode = RM_STEP_INTO_RANGE)) return 0;
        break;
    case RM_REVERSE_STEP_OVER:
        if (context_can_resume(ctx, ext->step_continue_mode = RM_REVERSE_STEP_INTO)) return 0;
        break;
    case RM_REVERSE_STEP_OVER_RANGE:
        if (context_can_resume(ctx, ext->step_continue_mode = RM_REVERSE_STEP_INTO_RANGE)) return 0;
        break;
    }

    switch (ext->step_continue_mode) {
    case RM_STEP_INTO_RANGE:
        if (context_can_resume(ctx, ext->step_continue_mode = RM_STEP_INTO)) return 0;
        break;
    case RM_REVERSE_STEP_INTO_RANGE:
        if (context_can_resume(ctx, ext->step_continue_mode = RM_REVERSE_STEP_INTO)) return 0;
        break;
    }

    errno = ERR_UNSUPPORTED;
    return -1;
}

static void step_machine_cache_client(void * args) {
    int error = 0;
    Context * ctx = *(Context **)args;
    ContextExtensionRC * ext = EXT(ctx);

    assert(ext->step_mode);
    assert(ext->step_error == NULL);

    if (update_step_machine_state(ctx) < 0) error = errno;

    cache_exit();

    if (error) {
        cancel_step_mode(ctx);
        ext->step_error = get_error_report(error);
        ctx->pending_intercept = 1;
    }
    run_ctrl_unlock();
    context_unlock(ctx);
}

static void stop_all_timer(void * args) {
    stop_all_timer_posted = 0;
    stop_all_timer_cnt++;
    run_safe_events_posted++;
    post_event(run_safe_events, NULL);
}

static void sync_run_state() {
    int err_cnt = 0;
    LINK * l;
    LINK p;

    assert(run_ctrl_lock_cnt == 0);
    assert(safe_event_list == NULL);
    stop_all_timer_cnt = 0;

    /* Clear intercept group flags */
    l = context_root.next;
    while (l != &context_root) {
        Context * ctx = ctxl2ctxp(l);
        ContextExtensionRC * ext = EXT(ctx);
        ext->pending_safe_event = 0;
        if (context_has_state(ctx)) {
            Context * grp = context_get_group(ctx, CONTEXT_GROUP_INTERCEPT);
            EXT(grp)->intercept_group = 0;
        }
        else if (ext->step_mode == RM_TERMINATE) {
            context_resume(ctx, RM_TERMINATE, 0, 0);
        }
        l = l->next;
    }

    /* Set intercept group flags */
    l = context_root.next;
    while (l != &context_root) {
        Context * grp = NULL;
        Context * ctx = ctxl2ctxp(l);
        ContextExtensionRC * ext = EXT(ctx);
        l = l->next;
        if (ctx->exited) continue;
        if (!ctx->stopped) continue;
        grp = context_get_group(ctx, CONTEXT_GROUP_INTERCEPT);
        if (ext->intercepted) {
            EXT(grp)->intercept_group = 1;
            continue;
        }
        if (ext->step_mode == RM_RESUME || ext->step_mode == RM_REVERSE_RESUME || ext->step_mode == RM_TERMINATE) {
            ext->step_continue_mode = ext->step_mode;
        }
        else {
            if (ext->step_channel == NULL) {
                if (update_step_machine_state(ctx) < 0) {
                    ext->step_error = get_error_report(errno);
                    cancel_step_mode(ctx);
                    ctx->pending_intercept = 1;
                }
            }
            else if (is_channel_closed(ext->step_channel)) {
                cancel_step_mode(ctx);
            }
            else {
                run_ctrl_lock();
                context_lock(ctx);
                cache_enter(step_machine_cache_client, ext->step_channel, &ctx, sizeof(ctx));
            }
        }
        if (ctx->pending_intercept) {
            EXT(grp)->intercept_group = 1;
        }
    }

    /* Stop or continue contexts as needed */
    list_init(&p);
    l = context_root.next;
    while (err_cnt == 0 && run_ctrl_lock_cnt == 0 && l != &context_root) {
        Context * grp = NULL;
        Context * ctx = ctxl2ctxp(l);
        ContextExtensionRC * ext = EXT(ctx);
        l = l->next;
        if (ctx->exited) continue;
        if (ext->intercepted) continue;
        if (!context_has_state(ctx)) continue;
        grp = context_get_group(ctx, CONTEXT_GROUP_INTERCEPT);
        if (EXT(grp)->intercept_group) {
            ctx->pending_intercept = 1;
            if (!ctx->stopped && !ctx->exiting) {
                assert(!ext->safe_single_step);
                context_stop(ctx);
                ext->pending_safe_event = 1;
                safe_event_pid_count++;
                assert(!ctx->stopped);
            }
        }
        else if (ctx->stopped && !ctx->pending_intercept) {
            if (ext->step_continue_mode != RM_RESUME) {
                list_add_last(&ext->link, &p);
            }
            else if (context_resume(ctx, EXT(grp)->reverse_run ? RM_REVERSE_RESUME : RM_RESUME, 0, 0) < 0) {
                int error = set_errno(errno, "Cannot resume");
                ctx->signal = 0;
                ctx->stopped = 1;
                ctx->stopped_by_bp = 0;
                ctx->stopped_by_cb = NULL;
                ctx->stopped_by_exception = 1;
                ctx->pending_intercept = 1;
                loc_free(ctx->exception_description);
                ctx->exception_description = loc_strdup(errno_to_str(error));
                send_context_changed_event(ctx);
                err_cnt++;
            }
        }
    }

    /* Resume contexts with resume mode other then RM_RESUME */
    l = p.next;
    while (err_cnt == 0 && run_ctrl_lock_cnt == 0 && l != &p) {
        Context * ctx = link2ctx(l);
        ContextExtensionRC * ext = EXT(ctx);
        l = l->next;
        if (context_resume(ctx, ext->step_continue_mode, ext->step_range_start, ext->step_range_end) < 0) {
            int error = set_errno(errno, "Cannot resume");
            ctx->signal = 0;
            ctx->stopped = 1;
            ctx->stopped_by_bp = 0;
            ctx->stopped_by_cb = NULL;
            ctx->stopped_by_exception = 1;
            ctx->pending_intercept = 1;
            loc_free(ctx->exception_description);
            ctx->exception_description = loc_strdup(errno_to_str(error));
            send_context_changed_event(ctx);
            err_cnt++;
        }
    }

    if (safe_event_pid_count > 0 || run_ctrl_lock_cnt > 0) return;
    if (err_cnt > 0) {
        if (run_safe_events_posted < 4) {
            run_safe_events_posted++;
            post_event(run_safe_events, NULL);
        }
        return;
    }
    send_event_context_suspended();
}

static void run_safe_events(void * arg) {
    LINK * l;
    Context * grp;

    run_safe_events_posted--;
    if (run_safe_events_posted > 0) return;

    safe_event_pid_count = 0;

    if (run_ctrl_lock_cnt == 0) {
        sync_run_state();
        return;
    }

    if (safe_event_list == NULL) return;
    grp = safe_event_list->grp;
    context_lock(grp);

    l = context_root.next;
    while (l != &context_root) {
        Context * ctx = ctxl2ctxp(l);
        ContextExtensionRC * ext = EXT(ctx);
        l = l->next;
        ext->pending_safe_event = 0;
        if (context_get_group(ctx, CONTEXT_GROUP_STOP) != grp) continue;
        if (ctx->exited || ctx->exiting) continue;
        if (ctx->stopped || !context_has_state(ctx)) continue;
        if (stop_all_timer_cnt >= STOP_ALL_MAX_CNT) {
            trace(LOG_ALWAYS, "can't stop %s; error: timeout", ctx->id);
            ctx->exiting = 1;
        }
        else {
            if (stop_all_timer_cnt == STOP_ALL_MAX_CNT / 2) {
                const char * msg = ext->safe_single_step ? "finish single step" : "stop";
                trace(LOG_ALWAYS, "warning: waiting too long for context %s to %s", ctx->id, msg);
            }
            if (!ext->safe_single_step || stop_all_timer_cnt >= STOP_ALL_MAX_CNT / 2) {
                if (context_stop(ctx) < 0) {
                    trace(LOG_ALWAYS, "can't stop %s; error %d: %s",
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
        if (i->grp != grp) {
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
        assert(is_all_stopped(i->grp));
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
        context_unlock(i->grp);
        loc_free(i);
    }
    context_unlock(grp);
}

static void check_safe_events(Context * ctx) {
    ContextExtensionRC * ext = EXT(ctx);
    assert(ctx->stopped || ctx->exited);
    assert(ext->pending_safe_event);
    assert(safe_event_pid_count > 0);
    ext->pending_safe_event = 0;
    safe_event_pid_count--;
    if (safe_event_pid_count == 0) {
        run_safe_events_posted++;
        post_event(run_safe_events, NULL);
    }
}

void post_safe_event(Context * ctx, EventCallBack * done, void * arg) {
    SafeEvent * i = (SafeEvent *)loc_alloc_zero(sizeof(SafeEvent));
    Context * grp = context_get_group(ctx, CONTEXT_GROUP_STOP);
    run_ctrl_lock();
    context_lock(grp);
    if (safe_event_list == NULL) {
        run_safe_events_posted++;
        post_event(run_safe_events, NULL);
    }
    i->grp = grp;
    i->done = done;
    i->arg = arg;
    if (safe_event_list == NULL) safe_event_list = i;
    else safe_event_last->next = i;
    safe_event_last = i;
}

int safe_context_single_step(Context * ctx) {
    int res = 0;
    ContextExtensionRC * ext = EXT(ctx);
    assert(run_ctrl_lock_cnt > 0);
    assert(ext->safe_single_step == 0);
    ext->safe_single_step = 1;
    res = context_single_step(ctx);
    assert(res < 0 || !ctx->stopped);
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

void add_run_control_event_listener(RunControlEventListener * listener, void * args) {
    if (listener_cnt >= listener_max) {
        listener_max += 8;
        listeners = (Listener *)loc_realloc(listeners, listener_max * sizeof(Listener));
    }
    listeners[listener_cnt].func = listener;
    listeners[listener_cnt].args = args;
    listener_cnt++;
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
    if (ext->step_mode) {
        ext->step_cnt++;
    }
    else {
        if (ext->step_error != NULL) {
            release_error_report(ext->step_error);
            ext->step_error = NULL;
        }
        ext->step_done = NULL;
    }
    if (ctx->stopped_by_bp || ctx->stopped_by_cb) evaluate_breakpoint(ctx);
    if (ext->pending_safe_event) check_safe_events(ctx);
    if (ctx->stopped_by_exception) send_event_context_exception(ctx);
    if (run_ctrl_lock_cnt == 0 && run_safe_events_posted < 4) {
        /* Lazily continue execution of temporary stopped contexts */
        run_safe_events_posted++;
        post_event(run_safe_events, NULL);
    }
}

static void event_context_started(Context * ctx, void * client_data) {
    ContextExtensionRC * ext = EXT(ctx);
    assert(!ctx->stopped);
    if (ext->intercepted) resume_context_tree(ctx);
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
    ext->safe_single_step = 0;
    cancel_step_mode(ctx);
    send_event_context_removed(ctx);
    if (ext->pending_safe_event) check_safe_events(ctx);
}

static void event_context_disposed(Context * ctx, void * client_data) {
    cancel_step_mode(ctx);
}

void ini_run_ctrl_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    static ContextEventListener listener = {
        event_context_created,
        event_context_exited,
        event_context_stopped,
        event_context_started,
        event_context_changed,
        event_context_disposed
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

void post_safe_event(Context * ctx, EventCallBack * done, void * arg) {
    post_event(done, arg);
}

#endif /* SERVICE_RunControl */
