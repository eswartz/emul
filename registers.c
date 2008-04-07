/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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

#include "mdep.h"
#include "config.h"

#if SERVICE_Registers

#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
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

#if defined(__i386__)

static REG_INDEX regs_index[] = {
#ifdef WIN32
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
    { NULL,     0,                  0},
#else
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
#endif
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
    json_write_string(out, "Size");
    out->write(out, ':');
    json_write_long(out, REG_WIDTH(*idx));

    out->write(out, ',');
    json_write_string(out, "Readable");
    out->write(out, ':');
    json_write_boolean(out, 1);

    out->write(out, ',');
    json_write_string(out, "Writeable");
    out->write(out, ':');
    json_write_boolean(out, 1);

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

static void command_get_context(char * token, Channel * c) {
    int err = 0;
    char id[256];
    Context * ctx = NULL;
    REG_INDEX * idx = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    id2register(id, &ctx, &idx);
    
    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;
    
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err == 0) {
        write_context(&c->out, id, ctx, idx);
    }
    else {
        write_stringz(&c->out, "null");
    }
    c->out.write(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    char id[256];
    pid_t pid, parent;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, &parent);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    write_errno(&c->out, 0);

    c->out.write(&c->out, '[');
    if (pid != 0 && parent != 0) {
        Context * ctx = context_find_from_pid(pid);
        if (ctx != NULL) {
            char t_id[128];
            REG_INDEX * idx = regs_index;
            strcpy(t_id, thread_id(ctx));
            while (idx->regName != NULL) {
                char r_id[128];
                if (idx != regs_index) c->out.write(&c->out, ',');
                snprintf(r_id, sizeof(r_id), "R%s.%s", idx->regName, t_id);
                json_write_string(&c->out, r_id);
                idx ++;
            }
        }
    }
    c->out.write(&c->out, ']');
    c->out.write(&c->out, 0);

    c->out.write(&c->out, MARKER_EOM);
}

static void command_get(char * token, Channel * c) {
    int err = 0;
    char id[256];
    Context * ctx = NULL;
    REG_INDEX * idx = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    id2register(id, &ctx, &idx);
    
    if (ctx == NULL || idx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;
    else if (!ctx->intercepted) err = ERR_IS_RUNNING;
    
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err == 0) {
        char * data = (char *)&ctx->regs + idx->regOff;
        int size = REG_WIDTH(*idx);
        JsonWriteBinaryState state;

        json_write_binary_start(&state, &c->out);
        json_write_binary_data(&state, data, size);
        json_write_binary_end(&state);
        c->out.write(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }
    c->out.write(&c->out, MARKER_EOM);
}

static void send_event_register_changed(Channel * c, char * id) {
    write_stringz(&c->out, "E");
    write_stringz(&c->out, REGISTERS);
    write_stringz(&c->out, "registerChanged");

    json_write_string(&c->out, id);
    c->out.write(&c->out, 0);

    c->out.write(&c->out, MARKER_EOM);
}

static void command_set(char * token, Channel * c) {
    int err = 0;
    char id[256];
    char val[256];
    int val_len = 0;
    JsonReadBinaryState state;
    char * ptr = NULL;
    Context * ctx = NULL;
    REG_INDEX * idx = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_binary_start(&state, &c->inp);
    for (;;) {
        int rd = json_read_binary_data(&state, val + val_len, sizeof(val) - val_len);
        if (rd == 0) break;
        val_len += rd;
    }
    json_read_binary_end(&state);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    id2register(id, &ctx, &idx);
    
    if (ctx == NULL || idx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;
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
    c->out.write(&c->out, MARKER_EOM);
}

void ini_registers_service(Protocol * proto) {
    add_command_handler(proto, REGISTERS, "getContext", command_get_context);
    add_command_handler(proto, REGISTERS, "getChildren", command_get_children);
    add_command_handler(proto, REGISTERS, "get", command_get);
    add_command_handler(proto, REGISTERS, "set", command_set);
}

#endif

