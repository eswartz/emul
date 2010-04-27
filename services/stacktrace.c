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
 * Target service implementation: stack trace (TCF name StackTrace)
 */

#include "config.h"

#if SERVICE_StackTrace

#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include "myalloc.h"
#include "protocol.h"
#include "trace.h"
#include "context.h"
#include "json.h"
#include "cache.h"
#include "exceptions.h"
#include "stacktrace.h"
#include "breakpoints.h"
#include "memorymap.h"
#include "symbols.h"

#define MAX_FRAMES  1000

static const char * STACKTRACE = "StackTrace";

typedef struct StackTrace {
    ErrorReport * error;
    int valid;
    int frame_cnt;
    int frame_max;
    StackFrame * frames; /* ordered bottom to top */
} StackTrace;

static size_t context_extension_offset = 0;

#define EXT(ctx) ((StackTrace *)((char *)(ctx) + context_extension_offset))

static void add_frame(StackTrace * stack_trace, StackFrame * frame) {
    if (stack_trace->frame_cnt >= stack_trace->frame_max) {
        stack_trace->frame_max += 32;
        stack_trace->frames = (StackFrame *)loc_realloc(stack_trace->frames,
            stack_trace->frame_max * sizeof(StackFrame));
    }
    stack_trace->frames[stack_trace->frame_cnt++] = *frame;
}

static void invalidate_stack_trace(StackTrace * stack_trace) {
    int i;
    release_error_report(stack_trace->error);
    for (i = 0; i < stack_trace->frame_cnt; i++) {
        if (!stack_trace->frames[i].is_top_frame) loc_free(stack_trace->frames[i].regs);
        loc_free(stack_trace->frames[i].mask);
    }
    stack_trace->error = NULL;
    stack_trace->frame_cnt = 0;
    stack_trace->valid = 0;
}

#if defined(_WRS_KERNEL)

#include <trcLib.h>

static Context * client_ctx;
static StackTrace * client_trace;
static ContextAddress frame_rp;

static void vxworks_stack_trace_callback(
    void *     callAdrs,       /* address from which function was called */
    int        funcAdrs,       /* address of function called */
    int        nargs,          /* number of arguments in function call */
    int *      args,           /* pointer to function args */
    int        taskId,         /* task's ID */
    int        isKernelAdrs    /* TRUE if Kernel addresses */
)
{
    StackFrame f;
    memset(&f, 0, sizeof(f));
    f.regs_size = client_ctx->regs_size;
    if (client_trace->frame_cnt == 0) {
        f.is_top_frame = 1;
        f.regs = client_ctx->regs;
        f.mask = loc_alloc(f.regs_size);
        memset(f.mask, 0xff, f.regs_size);
    }
    else {
        f.regs = loc_alloc_zero(f.regs_size);
        f.mask = loc_alloc_zero(f.regs_size);
        write_reg_value(get_PC_definition(client_ctx), &f, frame_rp);
    }
    f.fp = (ContextAddress)args;
    frame_rp = (ContextAddress)callAdrs;
    add_frame(client_trace, &f);
}

static void trace_stack(Context * ctx, StackTrace * s) {
    client_ctx = ctx;
    client_trace = s;
    trcStack((REG_SET *)ctx->regs, (FUNCPTR)vxworks_stack_trace_callback, ctx->pid);
    if (s->frame_cnt == 0) vxworks_stack_trace_callback(NULL, 0, 0, NULL, ctx->pid, 1);
}

#else

static void walk_frames(Context * ctx, StackTrace * stack_trace) {
    int error = 0;
    unsigned cnt = 0;
    StackFrame frame;

    memset(&frame, 0, sizeof(frame));
    frame.is_top_frame = 1;
    frame.regs_size = ctx->regs_size;
    frame.regs = ctx->regs;
    frame.mask = (RegisterData *)loc_alloc(frame.regs_size);
    memset(frame.mask, 0xff, frame.regs_size);
    while (cnt < MAX_FRAMES) {
        StackFrame down;
        memset(&down, 0, sizeof(down));
        down.regs_size = ctx->regs_size;
        down.regs = (RegisterData *)loc_alloc_zero(down.regs_size);
        down.mask = (RegisterData *)loc_alloc_zero(down.regs_size);
#if ENABLE_Symbols
        if (get_next_stack_frame(ctx, &frame, &down) < 0) {
            error = errno;
            loc_free(down.regs);
            loc_free(down.mask);
            break;
        }
#endif
        if (frame.fp == 0 && crawl_stack_frame(ctx, &frame, &down) < 0) {
            error = errno;
            loc_free(down.regs);
            loc_free(down.mask);
            break;
        }
        if (cnt > 0 && frame.fp == 0) {
            loc_free(down.regs);
            loc_free(down.mask);
            break;
        }
        add_frame(stack_trace, &frame);
        frame = down;
        cnt++;
    }

    if (!frame.is_top_frame) loc_free(frame.regs);
    loc_free(frame.mask);

    if (get_error_code(error) == ERR_CACHE_MISS) {
        invalidate_stack_trace(stack_trace);
    }
    else if (error) {
        stack_trace->error = get_error_report(error);
    }
}

static void trace_stack(Context * ctx, StackTrace * s) {
    int i;
    walk_frames(ctx, s);
    for (i = 0; i < s->frame_cnt / 2; i++) {
        StackFrame f = s->frames[i];
        s->frames[i] = s->frames[s->frame_cnt - i - 1];
        s->frames[s->frame_cnt - i - 1] = f;
    }
}

#endif

static StackTrace * create_stack_trace(Context * ctx) {
    StackTrace * stack_trace = EXT(ctx);
    if (!stack_trace->valid) {
        stack_trace->frame_cnt = 0;
        if (ctx->regs_error != NULL) {
            stack_trace->error = get_error_report(set_error_report_errno(ctx->regs_error));
        }
        else {
            trace_stack(ctx, stack_trace);
        }
        stack_trace->valid = 1;
    }
    return stack_trace;
}

static void write_context(OutputStream * out, char * id, Context * ctx, int level, StackFrame * frame, StackFrame * down) {
    uint64_t v;
    RegisterDefinition * reg_def = get_PC_definition(ctx);

    write_stream(out, '{');

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, id);

    write_stream(out, ',');
    json_write_string(out, "ParentID");
    write_stream(out, ':');
    json_write_string(out, ctx2id(ctx));

    write_stream(out, ',');
    json_write_string(out, "Level");
    write_stream(out, ':');
    json_write_long(out, level);

#if !defined(_WRS_KERNEL)
    write_stream(out, ',');
    json_write_string(out, "ProcessID");
    write_stream(out, ':');
    json_write_string(out, pid2id(ctx->mem, 0));
#endif

    if (frame->is_top_frame) {
        write_stream(out, ',');
        json_write_string(out, "TopFrame");
        write_stream(out, ':');
        json_write_boolean(out, 1);
    }

    if (frame->fp) {
        write_stream(out, ',');
        json_write_string(out, "FP");
        write_stream(out, ':');
        json_write_uint64(out, frame->fp);
    }

    if (read_reg_value(reg_def, frame, &v) == 0) {
        write_stream(out, ',');
        json_write_string(out, "IP");
        write_stream(out, ':');
        json_write_uint64(out, v);
    }

    if (down != NULL && read_reg_value(reg_def, down, &v) == 0) {
        write_stream(out, ',');
        json_write_string(out, "RP");
        write_stream(out, ':');
        json_write_uint64(out, v);
    }

    write_stream(out, '}');
}

typedef struct CommandGetContextData {
    Context * ctx;
    int frame;
    StackFrame * info;
    StackFrame * down;
} CommandGetContextData;

typedef struct CommandGetContextArgs {
    char token[256];
    int id_cnt;
    char ** ids;
    CommandGetContextData * data;
} CommandGetContextArgs;

static void command_get_context_cache_client(void * x) {
    int i;
    int err = 0;
    CommandGetContextArgs * args = (CommandGetContextArgs *)x;
    Channel * c = cache_channel();

    memset(args->data, 0, sizeof(CommandGetContextData) * args->id_cnt);
    for (i = 0; i < args->id_cnt; i++) {
        StackTrace * s = NULL;
        CommandGetContextData * d = args->data + i;
        if (id2frame(args->ids[i], &d->ctx, &d->frame) < 0) {
            err = errno;
            break;
        }
        if (!d->ctx->stopped) {
            err = ERR_IS_RUNNING;
            break;
        }
        s = create_stack_trace(d->ctx);
        if (s->error) {
            err = set_error_report_errno(s->error);
            break;
        }
        if (d->frame >= s->frame_cnt) {
            err = ERR_INV_CONTEXT;
            break;
        }
        d->info = s->frames + d->frame;
        d->down = d->frame > 0 ? d->info - 1 : NULL;
    }

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_stream(&c->out, '[');
    for (i = 0; i < args->id_cnt; i++) {
        CommandGetContextData * d = args->data + i;
        if (i > 0) write_stream(&c->out, ',');
        if (d->info == NULL) {
            write_string(&c->out, "null");
        }
        else {
            write_context(&c->out, args->ids[i], d->ctx, d->frame, d->info, d->down);
        }
    }
    write_stream(&c->out, ']');
    write_stream(&c->out, 0);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);

    loc_free(args->ids);
    loc_free(args->data);
}

static void command_get_context(char * token, Channel * c) {
    CommandGetContextArgs args;

    args.ids = json_read_alloc_string_array(&c->inp, &args.id_cnt);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    args.data = (CommandGetContextData *)loc_alloc(sizeof(CommandGetContextData) * args.id_cnt);
    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(command_get_context_cache_client, c, &args, sizeof(args));
}

typedef struct CommandGetChildrenArgs {
    char token[256];
    char id[256];
} CommandGetChildrenArgs;

static void command_get_children_cache_client(void * x) {
    int err = 0;
    Context * ctx = NULL;
    StackTrace * s = NULL;
    CommandGetChildrenArgs * args = (CommandGetChildrenArgs *)x;
    Channel * c = cache_channel();

    ctx = id2ctx(args->id);
    if (ctx == NULL || !context_has_state(ctx)) {
        /* no children */
    }
    else if (!ctx->stopped) {
        err = ERR_IS_RUNNING;
    }
    else {
        s = create_stack_trace(ctx);
    }

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);

    write_errno(&c->out, s != NULL ? set_error_report_errno(s->error) : err);

    if (s == NULL) {
        write_stringz(&c->out, "null");
    }
    else {
        int i;
        write_stream(&c->out, '[');
        for (i = 0; i < s->frame_cnt; i++) {
            if (i > 0) write_stream(&c->out, ',');
            json_write_string(&c->out, frame2id(ctx, i));
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    CommandGetChildrenArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(command_get_children_cache_client, c, &args, sizeof(args));
}

int get_top_frame(Context * ctx) {
    StackTrace * s;

    if (!ctx->stopped) {
        errno = ERR_IS_RUNNING;
        return STACK_TOP_FRAME;
    }

    s = create_stack_trace(ctx);
    if (s->error != NULL) {
        set_error_report_errno(s->error);
        return STACK_TOP_FRAME;
    }

    return s->frame_cnt - 1;
}

int get_frame_info(Context * ctx, int frame, StackFrame ** info) {
    StackTrace * stack;

    *info = NULL;
    if (ctx == NULL || !context_has_state(ctx)) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (!ctx->stopped) {
        errno = ERR_IS_RUNNING;
        return -1;
    }

    stack = create_stack_trace(ctx);
    if (stack->error != NULL) {
        set_error_report_errno(stack->error);
        return -1;
    }

    if (frame == STACK_TOP_FRAME) {
        frame = stack->frame_cnt - 1;
    }
    else if (frame < 0 || frame >= stack->frame_cnt) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }

    *info = stack->frames + frame;
    return 0;
}

int is_top_frame(Context * ctx, int frame) {
    StackTrace * stack;

    if (ctx == NULL || !context_has_state(ctx)) return 0;
    if (frame == STACK_TOP_FRAME) return 1;
    if (!ctx->stopped) return 0;
    stack = create_stack_trace(ctx);
    if (stack->error != NULL) return 0;
    return frame == stack->frame_cnt - 1;
}

static void flush_stack_trace(Context * ctx, void * args) {
    invalidate_stack_trace(EXT(ctx));
}

static void delete_stack_trace(Context * ctx, void * args) {
    invalidate_stack_trace(EXT(ctx));
    loc_free(EXT(ctx)->frames);
    memset(EXT(ctx), 0, sizeof(StackTrace));
}

void ini_stack_trace_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    static ContextEventListener listener = {
        NULL,
        flush_stack_trace,
        flush_stack_trace,
        flush_stack_trace,
        flush_stack_trace,
        delete_stack_trace
    };
    add_context_event_listener(&listener, bcg);
    add_command_handler(proto, STACKTRACE, "getContext", command_get_context);
    add_command_handler(proto, STACKTRACE, "getChildren", command_get_children);
    context_extension_offset = context_extension(sizeof(StackTrace));
}

#endif

