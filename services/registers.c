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
#include "exceptions.h"
#include "stacktrace.h"
#include "registers.h"

static const char * REGISTERS = "Registers";

static short endianess_test = 0x0201;
#define BIG_ENDIAN_DATA (*(char *)&endianess_test == 0x02)

static void write_context(OutputStream * out, char * id, Context * ctx, int frame, RegisterDefinition * reg_def) {
    assert(!ctx->exited);

    write_stream(out, '{');

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, id);

    write_stream(out, ',');
    json_write_string(out, "ParentID");
    write_stream(out, ':');
    json_write_string(out, thread_id(ctx));

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

#if !defined(_WRS_KERNEL)
    write_stream(out, ',');
    json_write_string(out, "ProcessID");
    write_stream(out, ':');
    json_write_string(out, pid2id(ctx->mem, 0));
#endif

    write_stream(out, ',');
    json_write_string(out, "BigEndian");
    write_stream(out, ':');
    json_write_boolean(out, BIG_ENDIAN_DATA);

    write_stream(out, '}');
    write_stream(out, 0);
}

static int id2register(char * id, Context ** ctx, int * frame, RegisterDefinition ** reg_def) {
    int i;
    char name[64];
    RegisterDefinition * reg_defs = get_reg_definitions();

    *ctx = NULL;
    *frame = STACK_TOP_FRAME;
    *reg_def = NULL;
    if (*id++ != 'R') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    i = 0;
    while (*id != '.' && *id != '@') {
        if (*id == 0) {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        name[i++] = *id++;
    }
    name[i++] = 0;
    if (*id == '@') {
        int n = 0;
        id++;
        while (*id != '.') {
            if (*id >= '0' && *id <= '9') {
                n = n * 10 + (*id++ - '0');
            }
            else {
                errno = ERR_INV_CONTEXT;
                return -1;
            }
        }
        *frame = n;
    }
    id++;
    for (i = 0; reg_defs[i].name != NULL; i++) {
        if (strcmp(reg_defs[i].name, name) == 0) break;
    }
    if (reg_defs[i].name == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *ctx = id2ctx(id);
    *reg_def = reg_defs + i;
    if (*ctx == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if ((*ctx)->exited) {
        errno = ERR_ALREADY_EXITED;
        return -1;
    }
    return 0;
}

static void command_get_context(char * token, Channel * c) {
    int err = 0;
    char id[256];
    int frame = 0;
    Context * ctx = NULL;
    RegisterDefinition * reg_def = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (id2register(id, &ctx, &frame, &reg_def) < 0) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err == 0) {
        write_context(&c->out, id, ctx, frame, reg_def);
    }
    else {
        write_stringz(&c->out, "null");
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    char id[256];
    Context * ctx = NULL;
    int frame = 0;
    StackFrame * frame_info = NULL;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (is_stack_frame_id(id, &ctx, &frame)) {
        if (get_frame_info(ctx, frame, &frame_info) < 0) err = errno;
    }
    else {
        pid_t pid, parent;
        pid = id2pid(id, &parent);
        if (pid != 0 && parent != 0) ctx = context_find_from_pid(pid);
        frame = STACK_TOP_FRAME;
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    write_errno(&c->out, err);

    write_stream(&c->out, '[');
    if (err == 0 && ctx != NULL && context_has_state(ctx)) {
        if (ctx != NULL) {
            int cnt = 0;
            char t_id[128];
            RegisterDefinition * reg_def;
            strcpy(t_id, thread_id(ctx));
            for (reg_def = get_reg_definitions(); reg_def->name != NULL; reg_def++) {
                if (frame == STACK_TOP_FRAME || read_reg_value(reg_def, frame_info, NULL) == 0) {
                    char r_id[128];
                    if (cnt > 0) write_stream(&c->out, ',');
                    if (frame == STACK_TOP_FRAME) {
                        snprintf(r_id, sizeof(r_id), "R%s.%s", reg_def->name, t_id);
                    }
                    else {
                        snprintf(r_id, sizeof(r_id), "R%s@%d.%s", reg_def->name, frame, t_id);
                    }
                    json_write_string(&c->out, r_id);
                    cnt++;
                }
            }
        }
    }
    write_stream(&c->out, ']');
    write_stream(&c->out, 0);

    write_stream(&c->out, MARKER_EOM);
}

static void send_event_register_changed(Channel * c, char * id) {
    write_stringz(&c->out, "E");
    write_stringz(&c->out, REGISTERS);
    write_stringz(&c->out, "registerChanged");

    json_write_string(&c->out, id);
    write_stream(&c->out, 0);

    write_stream(&c->out, MARKER_EOM);
}

static void command_get(char * token, Channel * c) {
    int err = 0;
    char id[256];
    int frame = 0;
    Context * ctx = NULL;
    RegisterDefinition * reg_def = NULL;
    char * data = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (id2register(id, &ctx, &frame, &reg_def) < 0) err = errno;
    else if (!ctx->intercepted) err = ERR_IS_RUNNING;

    if (!err) {
        if (is_top_frame(ctx, frame)) {
            data = (char *)&ctx->regs + reg_def->offset;
        }
        else {
            StackFrame * info = NULL;
            if (get_frame_info(ctx, frame, &info)) err = errno;
            else if (read_reg_value(reg_def, info, NULL) < 0) err = errno;
            else data = (char *)&info->regs + reg_def->offset;
        }
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err == 0) {
        json_write_binary(&c->out, data, reg_def->size);
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_set(char * token, Channel * c) {
    int err = 0;
    char id[256];
    char val[256];
    int val_len = 0;
    JsonReadBinaryState state;
    int frame = 0;
    Context * ctx = NULL;
    RegisterDefinition * reg_def = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_binary_start(&state, &c->inp);
    for (;;) {
        int rd = json_read_binary_data(&state, val + val_len, sizeof(val) - val_len);
        if (rd == 0) break;
        val_len += rd;
    }
    json_read_binary_end(&state);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (id2register(id, &ctx, &frame, &reg_def) < 0) err = errno;
    else if (!is_top_frame(ctx, frame)) err = ERR_INV_CONTEXT;
    else if (!ctx->intercepted) err = ERR_IS_RUNNING;

    if (err == 0) {
        char * data = (char *)&ctx->regs + reg_def->offset;
        int size = reg_def->size;
        if (val_len != size) {
            err = ERR_INV_DATA_SIZE;
        }
        else {
            memcpy(data, val, val_len);
            ctx->regs_dirty = 1;
            send_event_register_changed(c, id);
        }
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

struct Location {
    char id[256];
    Context * ctx;
    int frame;
    StackFrame * frame_info;
    RegisterDefinition * reg_def;
    unsigned offs;
    unsigned size;
};
typedef struct Location Location;

static Location * buf = NULL;
static int buf_pos = 0;
static int buf_len = 0;

static int read_location_list(InputStream * inp, int setm) {
    int err = 0;
    int ch = read_stream(inp);

    buf_pos = 0;
    if (ch == 'n') {
        if (read_stream(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
    }
    else if (ch != '[') {
        exception(ERR_PROTOCOL);
    }
    else {
        if (peek_stream(inp) == ']') {
            read_stream(inp);
        }
        else {
            for (;;) {
                int ch = read_stream(inp);
                if (ch == 'n') {
                    if (read_stream(inp) != 'u') exception(ERR_JSON_SYNTAX);
                    if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
                    if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
                }
                else {
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
                    if (id2register(loc->id, &loc->ctx, &loc->frame, &loc->reg_def) < 0) err = errno;
                    else if (!loc->ctx->intercepted) err = ERR_IS_RUNNING;
                    if (!err && !is_top_frame(loc->ctx, loc->frame)) {
                        if (setm) err = ERR_INV_CONTEXT;
                        else if (get_frame_info(loc->ctx, loc->frame, &loc->frame_info) < 0) err = errno;
                        else if (read_reg_value(loc->reg_def, loc->frame_info, NULL) < 0) err = errno;
                    }
                }
                ch = read_stream(inp);
                if (ch == ',') continue;
                if (ch == ']') break;
                exception(ERR_JSON_SYNTAX);
            }
        }
    }
    return err;
}

static void command_getm(char * token, Channel * c) {
    int err = read_location_list(&c->inp, 0);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err == 0) {
        int i = 0;
        JsonWriteBinaryState state;
        json_write_binary_start(&state, &c->out, -1);
        for (i = 0; i < buf_pos; i++) {
            Location * l = buf + i;
            char * data = l->frame_info == NULL ?
                (char *)&l->ctx->regs + l->reg_def->offset + l->offs :
                (char *)&l->frame_info->regs + l->reg_def->offset + l->offs;
            json_write_binary_data(&state, data, l->size);
        }
        json_write_binary_end(&state);
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_setm(char * token, Channel * c) {
    int i = 0;
    char tmp[256];
    JsonReadBinaryState state;
    int err = read_location_list(&c->inp, 1);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_binary_start(&state, &c->inp);
    for (i = 0; i < buf_pos; i++) {
        unsigned rd_done = 0;
        Location * l = buf + i;
        char * data = l->frame_info == NULL ?
            (char *)&l->ctx->regs + l->reg_def->offset + l->offs :
            (char *)&l->frame_info->regs + l->reg_def->offset + l->offs;
        while (rd_done < l->size) {
            int rd = json_read_binary_data(&state, err ? tmp : (data + rd_done), l->size - rd_done);
            if (rd == 0) break;
            rd_done += rd;
        }
        if (!err) send_event_register_changed(c, l->id);
    }
    json_read_binary_end(&state);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void read_filter_attrs(InputStream * inp, char * nm, void * arg) {
    loc_free(json_skip_object(inp));
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

void ini_registers_service(Protocol * proto) {
    add_command_handler(proto, REGISTERS, "getContext", command_get_context);
    add_command_handler(proto, REGISTERS, "getChildren", command_get_children);
    add_command_handler(proto, REGISTERS, "get", command_get);
    add_command_handler(proto, REGISTERS, "set", command_set);
    add_command_handler(proto, REGISTERS, "getm", command_getm);
    add_command_handler(proto, REGISTERS, "setm", command_setm);
    add_command_handler(proto, REGISTERS, "search", command_search);
}

#endif /* SERVICE_Registers */


