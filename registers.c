/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
#include "mdep.h"
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
    char	*regName;	/* pointer to register name */
    int		regOff;		/* offset to entry in REG_SET */
    int		regWidth;	/* register width in bytes */
} REG_INDEX;

#define REG_WIDTH(x) (x).regWidth

#define REG_OFFSET(name) offsetof(REG_SET, name)

#if defined(__i386__)
static REG_INDEX regs_index[] = {
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
    { NULL,     0,                  0},
};

#else

/* TODO: Linux: support for CPU types other then I86 */
#error "Unknown CPU"

#endif

#endif /* _WRS_KERNEL */

static short endianess_test = 0x0201;
#define BIG_ENDIAN_DATA (*(char *)&endianess_test == 0x02)

static void write_context(OutputStream * out, char * id, Context * ctx, REG_INDEX * idx) {
    assert(!ctx->exited);

    out->write(out, '{');

    json_write_string(out, "ID");
    out->write(out, ':');
    json_write_string(out, id);

    out->write(out, ',');
    json_write_string(out, "ParentID");
    out->write(out, ':');
    json_write_string(out, thread_id(ctx));

    out->write(out, ',');
    json_write_string(out, "Name");
    out->write(out, ':');
    json_write_string(out, idx->regName);

    out->write(out, ',');
    json_write_string(out, "Readable");
    out->write(out, ':');
    json_write_boolean(out, 1);

    out->write(out, ',');
    json_write_string(out, "Writeable");
    out->write(out, ':');
    json_write_boolean(out, 1);

    out->write(out, ',');
    json_write_string(out, "Formats");
    out->write(out, ':');
    out->write(out, '[');
    json_write_string(out, "Hex");
    out->write(out, ',');
    json_write_string(out, "Decimal");
    out->write(out, ']');

#if !defined(_WRS_KERNEL)
    out->write(out, ',');
    json_write_string(out, "ProcessID");
    out->write(out, ':');
    json_write_string(out, pid2id(ctx->mem, 0));
#endif

    out->write(out, ',');
    json_write_string(out, "BigEndian");
    out->write(out, ':');
    json_write_boolean(out, BIG_ENDIAN_DATA);

    out->write(out, '}');
    out->write(out, 0);
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
    return 0;
}

static void command_get_context(char * token, InputStream * inp, OutputStream * out) {
    int err = 0;
    char id[256];
    Context * ctx = NULL;
    REG_INDEX * idx = NULL;

    json_read_string(inp, id, sizeof(id));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    id2register(id, &ctx, &idx);
    
    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;
    
    write_stringz(out, "R");
    write_stringz(out, token);
    write_errno(out, err);
    if (err == 0) {
        write_context(out, id, ctx, idx);
    }
    else {
        write_stringz(out, "null");
    }
    out->write(out, MARKER_EOM);
}

static void command_get_children(char * token, InputStream * inp, OutputStream * out) {
    char id[256];
    pid_t pid, parent;

    json_read_string(inp, id, sizeof(id));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, &parent);

    write_stringz(out, "R");
    write_stringz(out, token);

    write_errno(out, 0);

    out->write(out, '[');
    if (pid != 0 && parent != 0) {
        Context * ctx = context_find_from_pid(pid);
        if (ctx != NULL) {
            char t_id[128];
            REG_INDEX * idx = regs_index;
            strcpy(t_id, thread_id(ctx));
            while (idx->regName != NULL) {
                char r_id[128];
                if (idx != regs_index) out->write(out, ',');
                snprintf(r_id, sizeof(r_id), "R%s.%s", idx->regName, t_id);
                json_write_string(out, r_id);
                idx ++;
            }
        }
    }
    out->write(out, ']');
    out->write(out, 0);

    out->write(out, MARKER_EOM);
}

static void command_get(char * token, InputStream * inp, OutputStream * out) {
    int err = 0;
    char id[256];
    char fmt[256];
    int hex = 0;
    Context * ctx = NULL;
    REG_INDEX * idx = NULL;

    json_read_string(inp, id, sizeof(id));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(inp, fmt, sizeof(fmt));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    id2register(id, &ctx, &idx);
    
    if (ctx == NULL || idx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;
    else if (!ctx->intercepted) err = ERR_IS_RUNNING;
    else if (strcmp(fmt, "Hex") == 0) hex = 1;
    else if (strcmp(fmt, "Decimal") != 0) err = ERR_INV_FORMAT;
    
    write_stringz(out, "R");
    write_stringz(out, token);
    write_errno(out, err);
    if (err == 0) {
        int64 n = 0;
        char val[64];
        int val_len = 0;
        assert(REG_WIDTH(*idx) <= sizeof(n));
        memcpy( (char *)&n + (BIG_ENDIAN_DATA ? sizeof(n) - REG_WIDTH(*idx) : 0),
                (char *)&ctx->regs + idx->regOff,
                REG_WIDTH(*idx));
        if (hex) {
            while (val_len < REG_WIDTH(*idx) * 2) {
                int i = (int)(n & 0xf);
                val[val_len++] = i < 10 ? '0' + i : 'A' + i - 10;
                n = n >> 4;
            }
        }
        else {
            int neg = n < 0;
            uns64 m = neg ? -n : n;
            do {
                int i = (int)(m % 10);
                val[val_len++] = '0' + i;
                m = m / 10;
            }
            while (m != 0);
            if (neg) val[val_len++] = '-';
        }
        out->write(out, '"');
        while (val_len > 0) out->write(out, val[--val_len]);
        out->write(out, '"');
        out->write(out, 0);
    }
    else {
        write_stringz(out, "null");
    }
    out->write(out, MARKER_EOM);
}

static void send_event_register_changed(OutputStream * out, char * id) {
    write_stringz(out, "E");
    write_stringz(out, REGISTERS);
    write_stringz(out, "registerChanged");

    json_write_string(out, id);
    out->write(out, 0);

    out->write(out, MARKER_EOM);
}

static void command_set(char * token, InputStream * inp, OutputStream * out) {
    int err = 0;
    char id[256];
    char fmt[256];
    char val[256];
    int hex = 0;
    char * ptr = NULL;
    Context * ctx = NULL;
    REG_INDEX * idx = NULL;

    json_read_string(inp, id, sizeof(id));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(inp, fmt, sizeof(fmt));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(inp, val, sizeof(val));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    id2register(id, &ctx, &idx);
    
    if (ctx == NULL || idx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;
    else if (!ctx->intercepted) err = ERR_IS_RUNNING;
    else if (strcmp(fmt, "Hex") == 0) hex = 1;
    else if (strcmp(fmt, "Decimal") != 0) err = ERR_INV_FORMAT;
    
    if (err == 0) {
        int64 n = 0;
        ptr = val;
        if (hex) {
            while (1) {
                if (*ptr >= '0' && *ptr <= '9') n = (n << 4) | (int64)(*ptr++ - '0');
                else if (*ptr >= 'A' && *ptr <= 'F') n = (n << 4) | (int64)(*ptr++ - 'A' + 10);
                else if (*ptr >= 'a' && *ptr <= 'f') n = (n << 4) | (int64)(*ptr++ - 'a' + 10);
                else break;
            }
        }
        else {
            uns64 m = 0;
            int neg = *ptr == '-';
            if (neg) ptr++;
            while (1) {
                if (*ptr >= '0' && *ptr <= '9') m = (m * 10) + (uns64)(*ptr++ - '0');
                else break;
            }
            n = neg ? (~m + 1) : m;
        }
        if (*ptr != 0) {
            err = ERR_INV_NUMBER;
        }
        else {
            assert(REG_WIDTH(*idx) <= sizeof(n));
            memcpy( (char *)&ctx->regs + idx->regOff,
                    (char *)&n + (BIG_ENDIAN_DATA ? sizeof(n) - REG_WIDTH(*idx) : 0),
                    REG_WIDTH(*idx));
            ctx->regs_dirty = 1;
            send_event_register_changed(out, id);
        }
    }

    write_stringz(out, "R");
    write_stringz(out, token);
    write_errno(out, err);
    out->write(out, MARKER_EOM);
}

void ini_registers_service(void) {
    add_command_handler(REGISTERS, "getContext", command_get_context);
    add_command_handler(REGISTERS, "getChildren", command_get_children);
    add_command_handler(REGISTERS, "get", command_get);
    add_command_handler(REGISTERS, "set", command_set);
}

#endif

