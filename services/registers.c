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
 * TCF Registers - CPU registers access service.
 */

#include "config.h"

#if SERVICE_Registers

#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include "myalloc.h"
#include "protocol.h"
#include "context.h"
#include "json.h"
#include "cache.h"
#include "exceptions.h"
#include "stacktrace.h"
#include "registers.h"

static const char * REGISTERS = "Registers";

static short endianess_test = 0x0201;
#define BIG_ENDIAN_DATA (*(char *)&endianess_test == 0x02)

static TCFBroadcastGroup * broadcast_group = NULL;

static void write_context(OutputStream * out, char * id, Context * ctx, int frame, RegisterDefinition * reg_def) {
    assert(!ctx->exited);

    write_stream(out, '{');

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, id);

    write_stream(out, ',');
    json_write_string(out, "ParentID");
    write_stream(out, ':');
    if (frame < 0 || is_top_frame(ctx, frame)) {
        json_write_string(out, ctx->id);
    }
    else {
        json_write_string(out, frame2id(ctx, frame));
    }

    write_stream(out, ',');
    json_write_string(out, "Name");
    write_stream(out, ':');
    json_write_string(out, reg_def->name);

    write_stream(out, ',');
    json_write_string(out, "Size");
    write_stream(out, ':');
    json_write_long(out, reg_def->size);

    write_stream(out, ',');
    json_write_string(out, "Readable");
    write_stream(out, ':');
    json_write_boolean(out, 1);

    write_stream(out, ',');
    json_write_string(out, "Writeable");
    write_stream(out, ':');
    json_write_boolean(out, 1);

    if (reg_def == get_PC_definition(ctx)) {
        write_stream(out, ',');
        json_write_string(out, "Role");
        write_stream(out, ':');
        json_write_string(out, "PC");
    }

    if (reg_def->dwarf_id >= 0) {
        write_stream(out, ',');
        json_write_string(out, "DwarfID");
        write_stream(out, ':');
        json_write_long(out, reg_def->dwarf_id);
    }

    if (reg_def->eh_frame_id >= 0) {
        write_stream(out, ',');
        json_write_string(out, "EhFrameID");
        write_stream(out, ':');
        json_write_long(out, reg_def->eh_frame_id);
    }

    if (reg_def->traceable) {
        write_stream(out, ',');
        json_write_string(out, "Traceable");
        write_stream(out, ':');
        json_write_boolean(out, reg_def->traceable);
    }

    write_stream(out, ',');
    json_write_string(out, "ProcessID");
    write_stream(out, ':');
    json_write_string(out, ctx->mem->id);

    write_stream(out, ',');
    json_write_string(out, "BigEndian");
    write_stream(out, ':');
    json_write_boolean(out, BIG_ENDIAN_DATA);

    write_stream(out, '}');
    write_stream(out, 0);
}

typedef struct GetContextArgs {
    char token[256];
    char id[256];
} GetContextArgs;

static void command_get_context_cache_client(void * x) {
    GetContextArgs * args = (GetContextArgs *)x;
    Channel * c  = cache_channel();
    Context * ctx = NULL;
    int frame = STACK_NO_FRAME;
    RegisterDefinition * reg_def = NULL;
    Trap trap;

    if (set_trap(&trap)) {
        if (id2register(args->id, &ctx, &frame, &reg_def) < 0) exception(errno);
        clear_trap(&trap);
    }

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, trap.error);
    if (reg_def != NULL) {
        write_context(&c->out, args->id, ctx, frame, reg_def);
    }
    else {
        write_stringz(&c->out, "null");
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_context(char * token, Channel * c) {
    GetContextArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(command_get_context_cache_client, c, &args, sizeof(args));
}

typedef struct GetChildrenArgs {
    char token[256];
    char id[256];
} GetChildrenArgs;

static void command_get_children_cache_client(void * x) {
    GetChildrenArgs * args = (GetChildrenArgs *)x;
    Channel * c  = cache_channel();
    Context * ctx = NULL;
    int frame = STACK_NO_FRAME;
    StackFrame * frame_info = NULL;
    RegisterDefinition * defs = NULL;
    Trap trap;

    if (set_trap(&trap)) {
        if (id2frame(args->id, &ctx, &frame) == 0) {
            if (get_frame_info(ctx, frame, &frame_info) < 0) exception(errno);
        }
        else {
            ctx = id2ctx(args->id);
            frame = STACK_TOP_FRAME;
        }
        if (ctx != NULL) defs = get_reg_definitions(ctx);
        clear_trap(&trap);
    }

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);

    write_errno(&c->out, trap.error);

    write_stream(&c->out, '[');
    if (defs != NULL) {
        int cnt = 0;
        RegisterDefinition * reg_def;
        for (reg_def = defs; reg_def->name != NULL; reg_def++) {
            if (frame == STACK_TOP_FRAME || read_reg_value(reg_def, frame_info, NULL) == 0) {
                if (cnt > 0) write_stream(&c->out, ',');
                json_write_string(&c->out, register2id(ctx, frame, reg_def));
                cnt++;
            }
        }
    }
    write_stream(&c->out, ']');
    write_stream(&c->out, 0);

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    GetChildrenArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(command_get_children_cache_client, c, &args, sizeof(args));
}

static void send_event_register_changed(char * id) {
    OutputStream * out = &broadcast_group->out;
    write_stringz(out, "E");
    write_stringz(out, REGISTERS);
    write_stringz(out, "registerChanged");

    json_write_string(out, id);
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

typedef struct GetArgs {
    char token[256];
    char id[256];
} GetArgs;

static void command_get_cache_client(void * x) {
    GetArgs * args = (GetArgs *)x;
    Channel * c  = cache_channel();
    uint8_t * data = NULL;
    int data_len = 0;
    Trap trap;

    if (set_trap(&trap)) {
        int frame = 0;
        Context * ctx = NULL;
        RegisterDefinition * reg_def = NULL;

        if (id2register(args->id, &ctx, &frame, &reg_def) < 0) exception(errno);
        if (!context_has_state(ctx)) exception(ERR_INV_CONTEXT);
        if (!ctx->stopped) exception(ERR_IS_RUNNING);

        if (is_top_frame(ctx, frame)) {
            data = (uint8_t *)ctx->regs + reg_def->offset;
        }
        else {
            StackFrame * info = NULL;
            if (get_frame_info(ctx, frame, &info) < 0) exception(errno);
            if (read_reg_value(reg_def, info, NULL) < 0) exception(errno);
            data = (uint8_t *)info->regs + reg_def->offset;
        }
        data_len = reg_def->size;
        clear_trap(&trap);
    }

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, trap.error);
    json_write_binary(&c->out, data, data_len);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
}

static void command_get(char * token, Channel * c) {
    GetArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(command_get_cache_client, c, &args, sizeof(args));
}

typedef struct SetArgs {
    char token[256];
    char id[256];
    int data_len;
    uint8_t * data;
} SetArgs;

static void command_set_cache_client(void * x) {
    SetArgs * args = (SetArgs *)x;
    Channel * c  = cache_channel();
    Trap trap;

    if (set_trap(&trap)) {
        int frame = 0;
        Context * ctx = NULL;
        RegisterDefinition * reg_def = NULL;

        if (id2register(args->id, &ctx, &frame, &reg_def) < 0) exception(errno);
        if (!is_top_frame(ctx, frame)) exception(ERR_INV_CONTEXT);
        if (!ctx->stopped) exception(ERR_IS_RUNNING);
        if (args->data_len != reg_def->size) exception(ERR_INV_DATA_SIZE);

        memcpy((uint8_t *)ctx->regs + reg_def->offset, args->data, reg_def->size);
        ctx->regs_dirty = 1;
        send_event_register_changed(args->id);
        clear_trap(&trap);
    }

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, trap.error);
    write_stream(&c->out, MARKER_EOM);

    loc_free(args->data);
}

static void command_set(char * token, Channel * c) {
    SetArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    args.data = (uint8_t *)json_read_alloc_binary(&c->inp, &args.data_len);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(command_set_cache_client, c, &args, sizeof(args));
}

typedef struct Location {
    char id[256];
    Context * ctx;
    int frame;
    StackFrame * frame_info;
    RegisterDefinition * reg_def;
    unsigned offs;
    unsigned size;
} Location;

static Location * buf = NULL;
static unsigned buf_pos = 0;
static unsigned buf_len = 0;

static uint8_t * bbf = NULL;
static unsigned bbf_pos = 0;
static unsigned bbf_len = 0;

static void read_location(InputStream * inp, void * args) {
    int ch = read_stream(inp);
    Location * loc = NULL;
    if (ch != '[') exception(ERR_JSON_SYNTAX);
    if (buf_pos >= buf_len) {
        buf_len = buf_len == 0 ? 0x10 : buf_len * 2;
        buf = (Location *)loc_realloc(buf, buf_len * sizeof(Location));
    }
    loc = buf + buf_pos++;
    memset(loc, 0, sizeof(Location));
    json_read_string(inp, loc->id, sizeof(loc->id));
    if (read_stream(inp) != ',') exception(ERR_JSON_SYNTAX);
    loc->offs = (unsigned)json_read_ulong(inp);
    if (read_stream(inp) != ',') exception(ERR_JSON_SYNTAX);
    loc->size = (unsigned)json_read_ulong(inp);
    if (read_stream(inp) != ']') exception(ERR_JSON_SYNTAX);
}

static Location * read_location_list(InputStream * inp, unsigned * cnt) {
    Location * locs = NULL;

    buf_pos = 0;
    json_read_array(inp, read_location, NULL);
    locs = (Location *)loc_alloc(buf_pos * sizeof(Location));
    memcpy(locs, buf, buf_pos * sizeof(Location));
    *cnt = buf_pos;
    return locs;
}

static void check_location_list(Location * locs, unsigned cnt, int setm) {
    unsigned pos;
    for (pos = 0; pos < cnt; pos++) {
        Location * loc = locs + pos;

        if (id2register(loc->id, &loc->ctx, &loc->frame, &loc->reg_def) < 0) exception(errno);
        if (!context_has_state(loc->ctx)) exception(ERR_INV_CONTEXT);
        if (!loc->ctx->stopped) exception(ERR_IS_RUNNING);
        if (loc->offs + loc->size > (unsigned)loc->reg_def->size) exception(ERR_INV_DATA_SIZE);

        if (is_top_frame(loc->ctx, loc->frame)) continue;

        if (setm) exception(ERR_INV_CONTEXT);
        if (get_frame_info(loc->ctx, loc->frame, &loc->frame_info) < 0) exception(errno);
        if (read_reg_value(loc->reg_def, loc->frame_info, NULL) < 0) exception(errno);
    }
}

typedef struct GetmArgs {
    char token[256];
    unsigned locs_cnt;
    Location * locs;
} GetmArgs;

static void command_getm_cache_client(void * x) {
    GetmArgs * args = (GetmArgs *)x;
    Channel * c  = cache_channel();
    Trap trap;

    bbf_pos = 0;
    if (set_trap(&trap)) {
        unsigned locs_pos = 0;
        check_location_list(args->locs, args->locs_cnt, 0);
        while (locs_pos < args->locs_cnt) {
            Location * l = args->locs + locs_pos++;
            RegisterData * regs = l->frame_info == NULL ? l->ctx->regs : l->frame_info->regs;
            if (bbf_pos + l->size > bbf_len) {
                bbf_len += 0x100 + l->size;
                bbf = (uint8_t *)loc_realloc(bbf, bbf_len);
            }
            memcpy(bbf + bbf_pos, (uint8_t *)regs + l->reg_def->offset + l->offs, l->size);
            bbf_pos += l->size;
        }
        clear_trap(&trap);
    }

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, trap.error);
    json_write_binary(&c->out, bbf, bbf_pos);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);

    loc_free(args->locs);
}

static void command_getm(char * token, Channel * c) {
    GetmArgs args;

    args.locs = read_location_list(&c->inp, &args.locs_cnt);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(command_getm_cache_client, c, &args, sizeof(args));
}

typedef struct SetmArgs {
    char token[256];
    unsigned locs_cnt;
    Location * locs;
    int data_len;
    uint8_t * data;
} SetmArgs;

static void command_setm_cache_client(void * x) {
    SetmArgs * args = (SetmArgs *)x;
    Channel * c  = cache_channel();
    Trap trap;

    if (set_trap(&trap)) {
        unsigned locs_pos = 0;
        unsigned data_pos = 0;
        check_location_list(args->locs, args->locs_cnt, 1);
        while (locs_pos < args->locs_cnt) {
            Location * l = args->locs + locs_pos++;
            RegisterData * regs = l->ctx->regs;
            assert(l->frame_info == NULL);
            if (data_pos + l->size > (unsigned)args->data_len) exception(ERR_INV_DATA_SIZE);
            memcpy((uint8_t *)regs + l->reg_def->offset + l->offs, args->data + data_pos, l->size);
            data_pos += l->size;
            if (l->size > 0) {
                l->ctx->regs_dirty = 1;
                send_event_register_changed(l->id);
            }
        }
        clear_trap(&trap);
    }

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, trap.error);
    write_stream(&c->out, MARKER_EOM);

    loc_free(args->locs);
    loc_free(args->data);
}

static void command_setm(char * token, Channel * c) {
    SetmArgs args;

    args.locs = read_location_list(&c->inp, &args.locs_cnt);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    args.data = (uint8_t *)json_read_alloc_binary(&c->inp, &args.data_len);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(command_setm_cache_client, c, &args, sizeof(args));
}

static void read_filter_attrs(InputStream * inp, const char * nm, void * arg) {
    json_skip_object(inp);
}

static void command_search(char * token, Channel * c) {
    char id[256];

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_struct(&c->inp, read_filter_attrs, NULL);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, ERR_UNSUPPORTED);
    write_stringz(&c->out, "null");
    write_stream(&c->out, MARKER_EOM);
}

void ini_registers_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    broadcast_group = bcg;
    add_command_handler(proto, REGISTERS, "getContext", command_get_context);
    add_command_handler(proto, REGISTERS, "getChildren", command_get_children);
    add_command_handler(proto, REGISTERS, "get", command_get);
    add_command_handler(proto, REGISTERS, "set", command_set);
    add_command_handler(proto, REGISTERS, "getm", command_getm);
    add_command_handler(proto, REGISTERS, "setm", command_setm);
    add_command_handler(proto, REGISTERS, "search", command_search);
}

#endif /* SERVICE_Registers */


