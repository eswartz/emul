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
 * TCF Memory - memory access service.
 */

#include "config.h"
#if SERVICE_Memory

#include <assert.h>
#include "mdep.h"
#include "protocol.h"
#include "context.h"
#include "json.h"
#include "exceptions.h"
#include "runctrl.h"
#include "myalloc.h"
#include "channel.h"
#include "base64.h"

static const char * MEMORY = "Memory";

#define BYTE_VALID        0x00
#define BYTE_UNKNOWN      0x01
#define BYTE_INVALID      0x02
#define BYTE_CANNOT_READ  0x04
#define BYTE_CANNOT_WRITE 0x08

#define CMD_GET     1
#define CMD_SET     2
#define CMD_FILL    3

#define BUF_SIZE    0x1000

struct MemoryCommandArgs {
    InputStream * inp;
    OutputStream * out;
    char token[256];
    unsigned long addr;
    unsigned long size;
    int word_size;
    int mode;
    Context * ctx;
};

static void write_context(OutputStream * out, Context * ctx) {
    assert(!ctx->exited);

    out->write(out, '{');

    json_write_string(out, "ID");
    out->write(out, ':');
    json_write_string(out, container_id(ctx));

#if !defined(_WRS_KERNEL)
    out->write(out, ',');
    json_write_string(out, "ProcessID");
    out->write(out, ':');
    json_write_string(out, pid2id(ctx->mem, 0));
#endif

    /* Check endianness */
    {
        short n = 0x0201;
        char * p = (char *)&n;
        out->write(out, ',');
        json_write_string(out, "BigEndian");
        out->write(out, ':');
        json_write_boolean(out, *p == 0x02);
    }

    out->write(out, ',');
    json_write_string(out, "AddressSize");
    out->write(out, ':');
    json_write_ulong(out, sizeof(char *));
    
    out->write(out, '}');
}

static void write_ranges(OutputStream * out, unsigned long addr, int size, int offs, int status, char * msg) {
    out->write(out, '[');
    if (offs > 0) {
        out->write(out, '{');

        json_write_string(out, "addr");
        out->write(out, ':');
        json_write_ulong(out, addr);
        out->write(out, ',');

        json_write_string(out, "size");
        out->write(out, ':');
        json_write_ulong(out, offs);
        out->write(out, ',');

        json_write_string(out, "stat");
        out->write(out, ':');
        json_write_ulong(out, 0);

        out->write(out, '}');
        out->write(out, ',');
    }
    if (offs < size) {
        out->write(out, '{');

        json_write_string(out, "addr");
        out->write(out, ':');
        json_write_ulong(out, addr + offs);
        out->write(out, ',');

        json_write_string(out, "size");
        out->write(out, ':');
        json_write_ulong(out, size - offs);
        out->write(out, ',');

        json_write_string(out, "stat");
        out->write(out, ':');
        json_write_ulong(out, status);
        out->write(out, ',');

        json_write_string(out, "msg");
        out->write(out, ':');
        json_write_string(out, msg);

        out->write(out, '}');
    }
    out->write(out, ']');
    out->write(out, 0);
}

static void command_get_context(char * token, InputStream * inp, OutputStream * out) {
    int err = 0;
    char id[256];
    Context * ctx = NULL;

    json_read_string(inp, id, sizeof(id));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    ctx = id2ctx(id);
    
    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;
    
    write_stringz(out, "R");
    write_stringz(out, token);
    write_errno(out, err);
    if (err == 0) {
        write_context(out, ctx);
    }
    else {
        write_stringz(out, "null");
    }
    out->write(out, 0);
    out->write(out, MARKER_EOM);
}

static void command_get_children(char * token, InputStream * inp, OutputStream * out) {
    char id[256];

    json_read_string(inp, id, sizeof(id));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(out, "R");
    write_stringz(out, token);

    write_errno(out, 0);

    out->write(out, '[');
    if (id[0] == 0) {
        LINK * qp;
        int cnt = 0;
        for (qp = context_root.next; qp != &context_root; qp = qp->next) {
            Context * ctx = ctxl2ctxp(qp);
            if (ctx->exited) continue;
            if (ctx->parent != NULL) continue;
            if (cnt > 0) out->write(out, ',');
            json_write_string(out, container_id(ctx));
            cnt++;
        }
    }
    out->write(out, ']');
    out->write(out, 0);

    out->write(out, MARKER_EOM);
}

static struct MemoryCommandArgs * read_command_args(char * token, InputStream * inp, OutputStream * out, int cmd) {
    int err = 0;
    char id[256];
    struct MemoryCommandArgs buf;
    memset(&buf, 0, sizeof(buf));

    json_read_string(inp, id, sizeof(id));
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    buf.addr = json_read_ulong(inp);
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    buf.word_size = (int)json_read_long(inp);
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    buf.size = (int)json_read_long(inp);
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    buf.mode = (int)json_read_long(inp);
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (cmd == CMD_GET && inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    buf.ctx = id2ctx(id);
    if (buf.ctx == NULL) err = ERR_INV_CONTEXT;
    else if (buf.ctx->exited) err = ERR_ALREADY_EXITED;

    if (err != 0) {
        if (cmd == CMD_SET || cmd == CMD_FILL) {
            if (inp->read(inp) != '[') exception(ERR_JSON_SYNTAX);
            if (inp->peek(inp) == ']') {
                inp->read(inp);
            }
            else {
                while (1) {
                    char ch;
                    json_read_ulong(inp);
                    ch = inp->read(inp);
                    if (ch == ',') continue;
                    if (ch == ']') break;
                    exception(ERR_JSON_SYNTAX);
                }
            }
            if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
        }
        if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

        write_stringz(out, "R");
        write_stringz(out, token);
        write_stringz(out, "null");
        write_errno(out, err);
        write_stringz(out, "null");
        out->write(out, MARKER_EOM);
        return NULL;
    }
    else {
        struct MemoryCommandArgs * args = (struct MemoryCommandArgs *)
            loc_alloc(sizeof(struct MemoryCommandArgs));
        *args = buf;
        args->inp = inp;
        args->out = out;
        strncpy(args->token, token, sizeof(args->token));
        stream_lock(out);
        context_lock(buf.ctx);
        return args;
    }
}

static void send_event_memory_changed(OutputStream * out, Context * ctx, unsigned long addr, unsigned long size) {
    write_stringz(out, "E");
    write_stringz(out, MEMORY);
    write_stringz(out, "memoryChanged");

    json_write_string(out, container_id(ctx));
    out->write(out, 0);

    /* <array of addres ranges> */
    out->write(out, '[');
    out->write(out, '{');

    json_write_string(out, "addr");
    out->write(out, ':');
    json_write_ulong(out, addr);
    
    out->write(out, ',');

    json_write_string(out, "size");
    out->write(out, ':');
    json_write_ulong(out, size);
    
    out->write(out, '}');
    out->write(out, ']');
    out->write(out, 0);

    out->write(out, MARKER_EOM);
}

static void safe_memory_set(void * parm) {
    struct MemoryCommandArgs * args = (struct MemoryCommandArgs *)parm;
    InputStream * inp = args->inp;
    OutputStream * out = args->out;
    char * token = args->token;
    unsigned long addr0 = args->addr;
    unsigned long addr = args->addr;
    unsigned long size = 0;
    int word_size = args->word_size;
    int mode = args->mode;
    Context * ctx = args->ctx;
    char buf[BUF_SIZE];
    int err = 0;

    if (ctx->exiting || ctx->exited) err = ERR_ALREADY_EXITED;

    if (inp->read(inp) != '"') exception(ERR_JSON_SYNTAX);

    for (;;) {
        int rd = read_base64(inp, buf, sizeof(buf));
        if (rd == 0) break;
        if (err == 0) {
            // TODO: word size, mode
            if (context_write_mem(ctx, addr, buf, rd) < 0) {
                err = errno;
            }
            else {
                addr += rd;
            }
        }
        size += rd;
    }

    if (inp->read(inp) != '"') exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    send_event_memory_changed(&broadcast_stream, ctx, addr0, size);

    write_stringz(out, "R");
    write_stringz(out, token);
    write_errno(out, err);
    if (err == 0) {
        write_stringz(out, "null");
    }
    else {
        char msg[0x400];
        strncpy(msg, errno_to_str(err), sizeof(msg));
        write_ranges(out, addr0, size, addr - addr0, BYTE_INVALID | BYTE_CANNOT_WRITE, msg);
    }
    out->write(out, MARKER_EOM);
    out->flush(out);
    stream_unlock(out);
    context_unlock(ctx);
    loc_free(args);
}

static void command_set(char * token, InputStream * inp, OutputStream * out) {
    struct MemoryCommandArgs * args = read_command_args(token, inp, out, CMD_SET);
    if (args != NULL) post_safe_event(safe_memory_set, args);
}

static void safe_memory_get(void * parm) {
    struct MemoryCommandArgs * args = (struct MemoryCommandArgs *)parm;
    OutputStream * out = args->out;
    char * token = args->token;
    unsigned long addr0 = args->addr;
    unsigned long addr = args->addr;
    unsigned long size = args->size;
    int word_size = args->word_size;
    int mode = args->mode;
    Context * ctx = args->ctx;
    char buf[BUF_SIZE + 4];
    int buf_pos = 0;
    int err = 0;
    int rem = 0;
    unsigned long chk = 0;

    if (ctx->exiting || ctx->exited) err = ERR_ALREADY_EXITED;

    write_stringz(out, "R");
    write_stringz(out, token);

    out->write(out, '"');
    while (err == 0 && addr < addr0 + size) {
        int rd = addr0 + size - addr;
        if (rd > BUF_SIZE) rd = BUF_SIZE;
        // TODO: word size, mode
        if (context_read_mem(ctx, addr, buf + rem, rd) < 0) {
            err = errno;
        }
        else {
            int i = 0, j = rem + rd;
            rem = j % 3;
            chk += write_base64(out, buf, j - rem);
            for (i = 0; i < rem; i++) buf[i] = buf[j - rem + i];
            addr += rd;
        }
    }
    chk += write_base64(out, buf, rem);
    out->write(out, '"');
    out->write(out, 0);
    assert(chk == ((addr - addr0) + 2) / 3 * 4);

    write_errno(out, err);
    if (err == 0) {
        write_stringz(out, "null");
    }
    else {
        char msg[0x400];
        strncpy(msg, errno_to_str(err), sizeof(msg));
        write_ranges(out, addr0, size, addr - addr0, BYTE_INVALID | BYTE_CANNOT_READ, msg);
    }
    out->write(out, MARKER_EOM);
    out->flush(out);
    stream_unlock(out);
    context_unlock(ctx);
    loc_free(args);
}

static void command_get(char * token, InputStream * inp, OutputStream * out) {
    struct MemoryCommandArgs * args = read_command_args(token, inp, out, CMD_GET);
    if (args != NULL) post_safe_event(safe_memory_get, args);
}

static void safe_memory_fill(void * parm) {
    struct MemoryCommandArgs * args = (struct MemoryCommandArgs *)parm;
    InputStream * inp = args->inp;
    OutputStream * out = args->out;
    char * token = args->token;
    unsigned long addr0 = args->addr;
    unsigned long addr = args->addr;
    unsigned long size = args->size;
    int word_size = args->word_size;
    int mode = args->mode;
    Context * ctx = args->ctx;
    char buf[0x1000];
    int buf_pos = 0;
    int err = 0;

    if (ctx->exiting || ctx->exited) err = ERR_ALREADY_EXITED;

    if (inp->read(inp) != '[') exception(ERR_JSON_SYNTAX);
    if (inp->peek(inp) == ']') {
        inp->read(inp);
    }
    else {
        while (1) {
            char ch;
            if (err == 0) {
                if (buf_pos >= sizeof(buf)) err = ERR_BUFFER_OVERFLOW;
                else buf[buf_pos++] = (char)json_read_ulong(inp);
            }
            else {
                json_read_ulong(inp);
            }
            ch = inp->read(inp);
            if (ch == ',') continue;
            if (ch == ']') break;
            exception(ERR_JSON_SYNTAX);
        }
    }
    if (inp->read(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (inp->read(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    while (err == 0 && buf_pos < (int)size && buf_pos <= sizeof(buf) / 2) {
        if (buf_pos == 0) {
            buf[buf_pos++] = 0;
        }
        else {
            memcpy(buf + buf_pos, buf, buf_pos);
            buf_pos *= 2;
        }
    }

    while (err == 0 && addr < addr0 + size) {
        int wr = addr0 + size - addr;
        if (wr > buf_pos) wr = buf_pos;
        // TODO: word size, mode
        if (context_write_mem(ctx, addr, buf, wr) < 0) {
            err = errno;
        }
        else {
            addr += wr;
        }
    }

    send_event_memory_changed(&broadcast_stream, ctx, addr0, size);

    write_stringz(out, "R");
    write_stringz(out, token);
    write_errno(out, err);
    if (err == 0) {
        write_stringz(out, "null");
    }
    else {
        char msg[0x400];
        strncpy(msg, errno_to_str(err), sizeof(msg));
        write_ranges(out, addr0, size, addr - addr0, BYTE_INVALID | BYTE_CANNOT_WRITE, msg);
    }
    out->write(out, MARKER_EOM);
    out->flush(out);
    stream_unlock(out);
    context_unlock(ctx);
    loc_free(args);
}

static void command_fill(char * token, InputStream * inp, OutputStream * out) {
    struct MemoryCommandArgs * args = read_command_args(token, inp, out, CMD_FILL);
    if (args != NULL) post_safe_event(safe_memory_fill, args);
}

static void send_event_context_added(OutputStream * out, Context * ctx) {
    write_stringz(out, "E");
    write_stringz(out, MEMORY);
    write_stringz(out, "contextAdded");

    /* <array of context data> */
    out->write(out, '[');
    write_context(out, ctx);
    out->write(out, ']');
    out->write(out, 0);

    out->write(out, MARKER_EOM);
}

static void send_event_context_changed(OutputStream * out, Context * ctx) {
    write_stringz(out, "E");
    write_stringz(out, MEMORY);
    write_stringz(out, "contextChanged");

    /* <array of context data> */
    out->write(out, '[');
    write_context(out, ctx);
    out->write(out, ']');
    out->write(out, 0);

    out->write(out, MARKER_EOM);
}

static void send_event_context_removed(OutputStream * out, Context * ctx) {
    write_stringz(out, "E");
    write_stringz(out, MEMORY);
    write_stringz(out, "contextRemoved");

    /* <array of context IDs> */
    out->write(out, '[');
    json_write_string(out, container_id(ctx));
    out->write(out, ']');
    out->write(out, 0);

    out->write(out, MARKER_EOM);
}

static void event_context_created(Context * ctx) {
    if (ctx->parent != NULL) return;
    send_event_context_added(&broadcast_stream, ctx);
    broadcast_stream.flush(&broadcast_stream);
}

static void event_context_changed(Context * ctx) {
    if (ctx->parent != NULL) return;
    send_event_context_changed(&broadcast_stream, ctx);
    broadcast_stream.flush(&broadcast_stream);
}

static void event_context_exited(Context * ctx) {
    if (ctx->parent != NULL) return;
    send_event_context_removed(&broadcast_stream, ctx);
    broadcast_stream.flush(&broadcast_stream);
}

void ini_memory_service(void) {
    static ContextEventListener listener = {
        event_context_created,
        event_context_exited,
        NULL,
        NULL,
        event_context_changed,
        NULL
    };
    add_context_event_listener(&listener);
    add_command_handler(MEMORY, "getContext", command_get_context);
    add_command_handler(MEMORY, "getChildren", command_get_children);
    add_command_handler(MEMORY, "set", command_set);
    add_command_handler(MEMORY, "get", command_get);
    add_command_handler(MEMORY, "fill", command_fill);
}

#endif

