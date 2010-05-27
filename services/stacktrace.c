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
 * Target service implementation: stack trace (TCF name StackTrace)
 */

#include <config.h>

#if SERVICE_StackTrace

#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <framework/myalloc.h>
#include <framework/protocol.h>
#include <framework/trace.h>
#include <framework/context.h>
#include <framework/json.h>
#include <framework/cache.h>
#include <framework/exceptions.h>
#include <services/stacktrace.h>
#include <services/symbols.h>

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

static void add_frame(StackTrace * stack, StackFrame * frame) {
    if (stack->frame_cnt >= stack->frame_max) {
        stack->frame_max += 32;
        stack->frames = (StackFrame *)loc_realloc(stack->frames,
            stack->frame_max * sizeof(StackFrame));
    }
    stack->frames[stack->frame_cnt++] = *frame;
}

static void invalidate_stack_trace(StackTrace * stack) {
    int i;
    release_error_report(stack->error);
    for (i = 0; i < stack->frame_cnt; i++) {
        loc_free(stack->frames[i].regs);
        stack->frames[i].regs = NULL;
    }
    stack->error = NULL;
    stack->frame_cnt = 0;
    stack->valid = 0;
}

static void trace_stack(Context * ctx, StackTrace * stack) {
    int i;
    int error = 0;
    StackFrame frame;

    stack->frame_cnt = 0;
    memset(&frame, 0, sizeof(frame));
    frame.is_top_frame = 1;
    frame.ctx = ctx;
    while (stack->frame_cnt < MAX_FRAMES) {
        StackFrame down;
        memset(&down, 0, sizeof(down));
        down.ctx = ctx;
#if ENABLE_Symbols
        if (get_next_stack_frame(&frame, &down) < 0) {
            error = errno;
            loc_free(down.regs);
            break;
        }
#endif
        if (frame.fp == 0 && crawl_stack_frame(&frame, &down) < 0) {
            error = errno;
            loc_free(down.regs);
            break;
        }
        if (stack->frame_cnt > 0 && frame.fp == 0) {
            loc_free(down.regs);
            break;
        }
        add_frame(stack, &frame);
        frame = down;
    }

    loc_free(frame.regs);

    if (get_error_code(error) == ERR_CACHE_MISS) {
        invalidate_stack_trace(stack);
    }
    else if (error) {
        stack->error = get_error_report(error);
    }
    for (i = 0; i < stack->frame_cnt / 2; i++) {
        StackFrame f = stack->frames[i];
        stack->frames[i] = stack->frames[stack->frame_cnt - i - 1];
        stack->frames[stack->frame_cnt - i - 1] = f;
    }
}

static StackTrace * create_stack_trace(Context * ctx) {
    StackTrace * stack = EXT(ctx);
    if (!stack->valid) {
        stack->frame_cnt = 0;
        stack->valid = 1;
        trace_stack(ctx, stack);
    }
    return stack;
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
    json_write_string(out, ctx->id);

    write_stream(out, ',');
    json_write_string(out, "Level");
    write_stream(out, ':');
    json_write_long(out, level);

    write_stream(out, ',');
    json_write_string(out, "ProcessID");
    write_stream(out, ':');
    json_write_string(out, ctx->mem->id);

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

    if (read_reg_value(frame, reg_def, &v) == 0) {
        write_stream(out, ',');
        json_write_string(out, "IP");
        write_stream(out, ':');
        json_write_uint64(out, v);
    }

    if (down != NULL && read_reg_value(down, reg_def, &v) == 0) {
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
        StackTrace * stack = NULL;
        CommandGetContextData * d = args->data + i;
        if (id2frame(args->ids[i], &d->ctx, &d->frame) < 0) {
            err = errno;
            break;
        }
        if (!d->ctx->stopped) {
            err = ERR_IS_RUNNING;
            break;
        }
        stack = create_stack_trace(d->ctx);
        if (stack->error) {
            err = set_error_report_errno(stack->error);
            break;
        }
        if (d->frame >= stack->frame_cnt) {
            err = ERR_INV_CONTEXT;
            break;
        }
        d->info = stack->frames + d->frame;
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
    StackTrace * stack = NULL;
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
        stack = create_stack_trace(ctx);
    }

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);

    write_errno(&c->out, stack != NULL ? set_error_report_errno(stack->error) : err);

    if (stack == NULL) {
        write_stringz(&c->out, "null");
    }
    else {
        int i;
        write_stream(&c->out, '[');
        for (i = 0; i < stack->frame_cnt; i++) {
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
    StackTrace * stack;

    if (!ctx->stopped) {
        errno = ERR_IS_RUNNING;
        return STACK_TOP_FRAME;
    }

    stack = create_stack_trace(ctx);
    if (stack->error != NULL) {
        set_error_report_errno(stack->error);
        return STACK_TOP_FRAME;
    }

    return stack->frame_cnt - 1;
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
        NULL,
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

