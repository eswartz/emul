/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
#include "registers.h"

static const char * REGISTERS = "Registers";

#if defined(_WRS_KERNEL)

#  define regs_index taskRegName
#  if defined(_WRS_REG_INDEX_REGWIDTH) || (CPU_FAMILY==COLDFIRE)
#    define REG_WIDTH(x) (x).regWidth
#  else
#    define REG_WIDTH(x) 4
#  endif

#else

typedef struct {
    char        *regName;       /* pointer to register name */
    int         regOff;         /* offset to entry in REG_SET */
    int         regWidth;       /* register width in bytes */
} REG_INDEX;

#define REG_WIDTH(x) (x).regWidth

#define REG_OFFSET(name) offsetof(REG_SET, name)

static REG_INDEX regs_index[] = {
#if defined(WIN32) && defined(__i386__)
    { "edi",    REG_OFFSET(Edi),    4},
    { "esi",    REG_OFFSET(Esi),    4},
    { "ebp",    REG_OFFSET(Ebp),    4},
    { "esp",    REG_OFFSET(Esp),    4},
    { "ebx",    REG_OFFSET(Ebx),    4},
    { "edx",    REG_OFFSET(Edx),    4},
    { "ecx",    REG_OFFSET(Ecx),    4},
    { "eax",    REG_OFFSET(Eax),    4},
    { "eflags", REG_OFFSET(EFlags), 4},
    { "eip",    REG_OFFSET(Eip),    4},
    { "cs",     REG_OFFSET(SegCs),  4},
    { "ss",     REG_OFFSET(SegSs),  4},
#elif defined(__APPLE__) && defined(__i386__)
    { "edi",    REG_OFFSET(__edi),    4},
    { "esi",    REG_OFFSET(__esi),    4},
    { "ebp",    REG_OFFSET(__ebp),    4},
    { "esp",    REG_OFFSET(__esp),    4},
    { "ebx",    REG_OFFSET(__ebx),    4},
    { "edx",    REG_OFFSET(__edx),    4},
    { "ecx",    REG_OFFSET(__ecx),    4},
    { "eax",    REG_OFFSET(__eax),    4},
    { "eflags", REG_OFFSET(__eflags), 4},
    { "eip",    REG_OFFSET(__eip),    4},
#elif defined(__x86_64__)
    { "rax",    REG_OFFSET(rax),    8},
    { "rbx",    REG_OFFSET(rbx),    8},
    { "rcx",    REG_OFFSET(rcx),    8},
    { "rdx",    REG_OFFSET(rdx),    8},
    { "rsi",    REG_OFFSET(rsi),    8},
    { "rdi",    REG_OFFSET(rdi),    8},
    { "r8",     REG_OFFSET(r8),     8},
    { "r9",     REG_OFFSET(r9),     8},
    { "r10",    REG_OFFSET(r10),    8},
    { "r11",    REG_OFFSET(r11),    8},
    { "r12",    REG_OFFSET(r12),    8},
    { "r13",    REG_OFFSET(r13),    8},
    { "r14",    REG_OFFSET(r14),    8},
    { "r15",    REG_OFFSET(r15),    8},
    { "rbp",    REG_OFFSET(rbp),    8},
    { "rsp",    REG_OFFSET(rsp),    8},
    { "rip",    REG_OFFSET(rip),    8},
    { "eflags", REG_OFFSET(eflags), 4},
    { "cs",     REG_OFFSET(cs),     4},
    { "ss",     REG_OFFSET(ss),     4},
    { "ds",     REG_OFFSET(ds),     4},
    { "es",     REG_OFFSET(es),     4},
    { "fs",     REG_OFFSET(fs),     4},
    { "gs",     REG_OFFSET(gs),     4},
    { "fs_base", REG_OFFSET(fs_base), 4},
    { "gs_base", REG_OFFSET(gs_base), 4},
#elif defined(__i386__)
    { "edi",    REG_OFFSET(edi),    4},
    { "esi",    REG_OFFSET(esi),    4},
    { "ebp",    REG_OFFSET(ebp),    4},
    { "esp",    REG_OFFSET(esp),    4},
    { "ebx",    REG_OFFSET(ebx),    4},
    { "edx",    REG_OFFSET(edx),    4},
    { "ecx",    REG_OFFSET(ecx),    4},
    { "eax",    REG_OFFSET(eax),    4},
    { "eflags", REG_OFFSET(eflags), 4},
    { "eip",    REG_OFFSET(eip),    4},
#else
#  error "Unknown CPU"
#endif
    { NULL,     0,                  0},
};

#endif /* _WRS_KERNEL */

static short endianess_test = 0x0201;
#define BIG_ENDIAN_DATA (*(char *)&endianess_test == 0x02)

static void write_context(OutputStream * out, char * id, Context * ctx, REG_INDEX * idx) {
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
    json_write_string(out, idx->regName);

    write_stream(out, ',');
    json_write_string(out, "Size");
    write_stream(out, ':');
    json_write_long(out, REG_WIDTH(*idx));

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

static int id2register(char * id, Context ** ctx, REG_INDEX ** idx) {
    int i;
    char name[64];
    *ctx = NULL;
    *idx = NULL;
    if (*id++ != 'R') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    i = 0;
    while (*id != '.') {
        if (*id == 0) {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        name[i++] = *id++;
    }
    name[i++] = 0;
    id++;
    for (i = 0; regs_index[i].regName != NULL; i++) {
        if (strcmp(regs_index[i].regName, name) == 0) break;
    }
    if (regs_index[i].regName == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *ctx = id2ctx(id);
    *idx = regs_index + i;
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
    Context * ctx = NULL;
    REG_INDEX * idx = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (id2register(id, &ctx, &idx) < 0) err = errno;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err == 0) {
        write_context(&c->out, id, ctx, idx);
    }
    else {
        write_stringz(&c->out, "null");
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    char id[256];
    pid_t pid, parent;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, &parent);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    write_errno(&c->out, 0);

    write_stream(&c->out, '[');
    if (pid != 0 && parent != 0) {
        Context * ctx = context_find_from_pid(pid);
        if (ctx != NULL) {
            char t_id[128];
            REG_INDEX * idx = regs_index;
            strcpy(t_id, thread_id(ctx));
            while (idx->regName != NULL) {
                char r_id[128];
                if (idx != regs_index) write_stream(&c->out, ',');
                snprintf(r_id, sizeof(r_id), "R%s.%s", idx->regName, t_id);
                json_write_string(&c->out, r_id);
                idx ++;
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
    Context * ctx = NULL;
    REG_INDEX * idx = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (id2register(id, &ctx, &idx) < 0) err = errno;
    else if (!ctx->intercepted) err = ERR_IS_RUNNING;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err == 0) {
        char * data = (char *)&ctx->regs + idx->regOff;
        int size = REG_WIDTH(*idx);
        json_write_binary(&c->out, data, size);
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
    Context * ctx = NULL;
    REG_INDEX * idx = NULL;

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

    if (id2register(id, &ctx, &idx) < 0) err = errno;
    else if (!ctx->intercepted) err = ERR_IS_RUNNING;

    if (err == 0) {
        char * data = (char *)&ctx->regs + idx->regOff;
        int size = REG_WIDTH(*idx);
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
    REG_INDEX * idx;
    unsigned offs;
    unsigned size;
};
typedef struct Location Location;

static Location * buf = NULL;
static int buf_pos = 0;
static int buf_len = 0;

static int read_location_list(InputStream * inp) {
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
                    json_read_string(inp, loc->id, sizeof(loc->id));
                    if (read_stream(inp) != ',') exception(ERR_JSON_SYNTAX);
                    loc->offs = (unsigned)json_read_ulong(inp);
                    if (read_stream(inp) != ',') exception(ERR_JSON_SYNTAX);
                    loc->size = (unsigned)json_read_ulong(inp);
                    if (read_stream(inp) != ']') exception(ERR_JSON_SYNTAX);
                    if (id2register(loc->id, &loc->ctx, &loc->idx) < 0) err = errno;
                    else if (!loc->ctx->intercepted) err = ERR_IS_RUNNING;
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
    int err = read_location_list(&c->inp);
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
            char * data = (char *)&l->ctx->regs + l->idx->regOff + l->offs;
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
    int err = read_location_list(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_binary_start(&state, &c->inp);
    for (i = 0; i < buf_pos; i++) {
        unsigned rd_done = 0;
        Location * l = buf + i;
        char * data = (char *)&l->ctx->regs + l->idx->regOff + l->offs;
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


