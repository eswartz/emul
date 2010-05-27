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
 * TCF Memory - memory access service.
 */

#include <config.h>

#if SERVICE_Memory

#include <assert.h>
#include <framework/protocol.h>
#include <framework/context.h>
#include <framework/json.h>
#include <framework/exceptions.h>
#include <framework/myalloc.h>
#include <framework/channel.h>
#include <framework/trace.h>
#include <services/memoryservice.h>
#include <services/runctrl.h>

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

typedef struct MemoryCommandArgs {
    Channel * c;
    char token[256];
    ContextAddress addr;
    unsigned long size;
    int word_size;
    int mode;
    Context * ctx;
} MemoryCommandArgs;

static void write_context(OutputStream * out, Context * ctx) {
    assert(!ctx->exited);
    assert(ctx->parent == NULL);

    write_stream(out, '{');

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, ctx->id);

    write_stream(out, ',');
    json_write_string(out, "ProcessID");
    write_stream(out, ':');
    json_write_string(out, ctx->mem->id);

    /* Check endianness */
    {
        short n = 0x0201;
        char * p = (char *)&n;
        write_stream(out, ',');
        json_write_string(out, "BigEndian");
        write_stream(out, ':');
        json_write_boolean(out, *p == 0x02);
    }

    write_stream(out, ',');
    json_write_string(out, "AddressSize");
    write_stream(out, ':');
    json_write_ulong(out, sizeof(char *));

    write_stream(out, ',');
    json_write_string(out, "AccessTypes");
    write_stream(out, ':');
    write_stream(out, '[');
    json_write_string(out, "instruction");
    write_stream(out, ',');
    json_write_string(out, "data");
#if !defined(_WRS_KERNEL)
    write_stream(out, ',');
    json_write_string(out, "user");
#endif
    write_stream(out, ']');

    write_stream(out, '}');
}

static void write_ranges(OutputStream * out, ContextAddress addr, int size, int offs, int status, int err) {
    int cnt = 0;
    write_stream(out, '[');
    if (offs > 0) {
        write_stream(out, '{');

        json_write_string(out, "addr");
        write_stream(out, ':');
        json_write_ulong(out, addr);
        write_stream(out, ',');

        json_write_string(out, "size");
        write_stream(out, ':');
        json_write_ulong(out, offs);
        write_stream(out, ',');

        json_write_string(out, "stat");
        write_stream(out, ':');
        json_write_ulong(out, 0);

        write_stream(out, '}');
        cnt++;
    }
    if (offs < size) {
        if (cnt > 0) write_stream(out, ',');
        write_stream(out, '{');

        json_write_string(out, "addr");
        write_stream(out, ':');
        json_write_ulong(out, addr + offs);
        write_stream(out, ',');

        json_write_string(out, "size");
        write_stream(out, ':');
        json_write_ulong(out, size - offs);
        write_stream(out, ',');

        json_write_string(out, "stat");
        write_stream(out, ':');
        json_write_ulong(out, status);
        write_stream(out, ',');

        json_write_string(out, "msg");
        write_stream(out, ':');
        write_error_object(out, err);

        write_stream(out, '}');
    }
    write_stream(out, ']');
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

    if (ctx == NULL || ctx->mem != ctx) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err == 0 && ctx->parent == NULL) {
        write_context(&c->out, ctx);
    }
    else {
        write_string(&c->out, "null");
    }
    write_stream(&c->out, 0);
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
        LINK * qp;
        int cnt = 0;
        for (qp = context_root.next; qp != &context_root; qp = qp->next) {
            Context * ctx = ctxl2ctxp(qp);
            if (ctx->exited) continue;
            if (ctx->mem != ctx) continue;
            if (cnt > 0) write_stream(&c->out, ',');
            json_write_string(&c->out, ctx->id);
            cnt++;
        }
    }
    write_stream(&c->out, ']');
    write_stream(&c->out, 0);

    write_stream(&c->out, MARKER_EOM);
}

static MemoryCommandArgs * read_command_args(char * token, Channel * c, int cmd) {
    int err = 0;
    char id[256];
    MemoryCommandArgs buf;
    memset(&buf, 0, sizeof(buf));

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    buf.addr = json_read_ulong(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    buf.word_size = (int)json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    buf.size = (int)json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    buf.mode = (int)json_read_long(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (cmd == CMD_GET && read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    buf.ctx = id2ctx(id);
    if (buf.ctx == NULL) err = ERR_INV_CONTEXT;
    else if (buf.ctx->parent != NULL) err = ERR_INV_CONTEXT;
    else if (buf.ctx->exited) err = ERR_ALREADY_EXITED;

    if (err != 0) {
        if (cmd != CMD_GET) {
            int ch;
            while ((ch = read_stream(&c->inp)) != 0) {
                if (ch < 0) exception(ERR_JSON_SYNTAX);
            }
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
        }

        write_stringz(&c->out, "R");
        write_stringz(&c->out, token);
        if (cmd == CMD_GET) write_stringz(&c->out, "null");
        write_errno(&c->out, err);
        write_stringz(&c->out, "null");
        write_stream(&c->out, MARKER_EOM);
        return NULL;
    }
    else {
        MemoryCommandArgs * args = (MemoryCommandArgs *)loc_alloc(sizeof(MemoryCommandArgs));
        *args = buf;
        args->c = c;
        strlcpy(args->token, token, sizeof(args->token));
        channel_lock(c);
        context_lock(buf.ctx);
        return args;
    }
}

static void send_event_memory_changed(OutputStream * out, Context * ctx, ContextAddress addr, unsigned long size) {
    assert(ctx->parent == NULL);

    write_stringz(out, "E");
    write_stringz(out, MEMORY);
    write_stringz(out, "memoryChanged");

    json_write_string(out, ctx->id);
    write_stream(out, 0);

    /* <array of addres ranges> */
    write_stream(out, '[');
    write_stream(out, '{');

    json_write_string(out, "addr");
    write_stream(out, ':');
    json_write_ulong(out, addr);

    write_stream(out, ',');

    json_write_string(out, "size");
    write_stream(out, ':');
    json_write_ulong(out, size);

    write_stream(out, '}');
    write_stream(out, ']');
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void safe_memory_set(void * parm) {
    MemoryCommandArgs * args = (MemoryCommandArgs *)parm;
    Channel * c = args->c;
    Context * ctx = args->ctx;

    if (!is_channel_closed(c)) {
        Trap trap;
        if (set_trap(&trap)) {
            InputStream * inp = &c->inp;
            OutputStream * out = &c->out;
            char * token = args->token;
            ContextAddress addr0 = args->addr;
            ContextAddress addr = args->addr;
            unsigned long size = 0;
            char buf[BUF_SIZE];
            int err = 0;
            JsonReadBinaryState state;

            if (ctx->exiting || ctx->exited) err = ERR_ALREADY_EXITED;

            json_read_binary_start(&state, inp);
            for (;;) {
                int rd = json_read_binary_data(&state, buf, sizeof(buf));
                if (rd == 0) break;
                if (err == 0) {
                    /* TODO: word size, mode */
                    if (context_write_mem(ctx, addr, buf, rd) < 0) err = errno;
                    else addr += rd;
                }
                size += rd;
            }
            json_read_binary_end(&state);
            if (read_stream(inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

            send_event_memory_changed(&c->bcg->out, ctx, addr0, size);

            write_stringz(out, "R");
            write_stringz(out, token);
            write_errno(out, err);
            if (err == 0) {
                write_stringz(out, "null");
            }
            else {
                write_ranges(out, addr0, size, addr - addr0, BYTE_INVALID | BYTE_CANNOT_WRITE, err);
            }
            write_stream(out, MARKER_EOM);
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception in message handler: %d %s",
                  trap.error, errno_to_str(trap.error));
            channel_close(c);
        }
    }
    channel_unlock(c);
    context_unlock(ctx);
    loc_free(args);
}

static void command_set(char * token, Channel * c) {
    MemoryCommandArgs * args = read_command_args(token, c, CMD_SET);
    if (args != NULL) post_safe_event(args->ctx->mem, safe_memory_set, args);
}

static void safe_memory_get(void * parm) {
    MemoryCommandArgs * args = (MemoryCommandArgs *)parm;
    Channel * c = args->c;
    Context * ctx = args->ctx;

    if (!is_channel_closed(c)) {
        Trap trap;
        if (set_trap(&trap)) {
            OutputStream * out = &args->c->out;
            char * token = args->token;
            ContextAddress addr0 = args->addr;
            ContextAddress addr = args->addr;
            unsigned long size = args->size;
            unsigned long pos = 0;
            char buf[BUF_SIZE];
            int err = 0;
            JsonWriteBinaryState state;

            if (ctx->exiting || ctx->exited) err = ERR_ALREADY_EXITED;

            write_stringz(out, "R");
            write_stringz(out, token);

            json_write_binary_start(&state, out, size);
            while (pos < size) {
                int rd = size - pos;
                if (rd > BUF_SIZE) rd = BUF_SIZE;
                /* TODO: word size, mode */
                if (err == 0) {
                    if (context_read_mem(ctx, addr, buf, rd) < 0) err = errno;
                    else addr += rd;
                }
                else {
                    memset(buf, 0, rd);
                }
                json_write_binary_data(&state, buf, rd);
                pos += rd;
            }
            json_write_binary_end(&state);
            write_stream(out, 0);

            write_errno(out, err);
            if (err == 0) {
                write_stringz(out, "null");
            }
            else {
                write_ranges(out, addr0, size, addr - addr0, BYTE_INVALID | BYTE_CANNOT_READ, err);
            }
            write_stream(out, MARKER_EOM);
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception in message handler: %d %s",
                  trap.error, errno_to_str(trap.error));
            channel_close(c);
        }
    }
    channel_unlock(c);
    context_unlock(ctx);
    loc_free(args);
}

static void command_get(char * token, Channel * c) {
    MemoryCommandArgs * args = read_command_args(token, c, CMD_GET);
    if (args != NULL) post_safe_event(args->ctx->mem, safe_memory_get, args);
}

static void safe_memory_fill(void * parm) {
    MemoryCommandArgs * args = (MemoryCommandArgs *)parm;
    Channel * c = args->c;
    Context * ctx = args->ctx;

    if (!is_channel_closed(c)) {
        Trap trap;
        if (set_trap(&trap)) {
            InputStream * inp = &c->inp;
            OutputStream * out = &c->out;
            char * token = args->token;
            ContextAddress addr0 = args->addr;
            ContextAddress addr = args->addr;
            unsigned long size = args->size;
            char buf[0x1000];
            int buf_pos = 0;
            int err = 0;

            if (ctx->exiting || ctx->exited) err = ERR_ALREADY_EXITED;

            if (read_stream(inp) != '[') exception(ERR_JSON_SYNTAX);
            if (peek_stream(inp) == ']') {
                read_stream(inp);
            }
            else {
                for (;;) {
                    int ch;
                    if (err == 0) {
                        if (buf_pos >= (int)sizeof(buf)) err = ERR_BUFFER_OVERFLOW;
                        else buf[buf_pos++] = (char)json_read_ulong(inp);
                    }
                    else {
                        json_read_ulong(inp);
                    }
                    ch = read_stream(inp);
                    if (ch == ',') continue;
                    if (ch == ']') break;
                    exception(ERR_JSON_SYNTAX);
                }
            }
            if (read_stream(inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

            while (err == 0 && buf_pos < (int)size && buf_pos <= (int)(sizeof(buf) / 2)) {
                if (buf_pos == 0) {
                    buf[buf_pos++] = 0;
                }
                else {
                    memcpy(buf + buf_pos, buf, buf_pos);
                    buf_pos *= 2;
                }
            }

            while (err == 0 && addr < addr0 + size) {
                char tmp[sizeof(buf)];
                int wr = addr0 + size - addr;
                if (wr > buf_pos) wr = buf_pos;
                /* TODO: word size, mode */
                memcpy(tmp, buf, wr);
                if (context_write_mem(ctx, addr, tmp, wr) < 0) err = errno;
                else addr += wr;
            }

            send_event_memory_changed(&c->bcg->out, ctx, addr0, size);

            write_stringz(out, "R");
            write_stringz(out, token);
            write_errno(out, err);
            if (err == 0) {
                write_stringz(out, "null");
            }
            else {
                write_ranges(out, addr0, size, addr - addr0, BYTE_INVALID | BYTE_CANNOT_WRITE, err);
            }
            write_stream(out, MARKER_EOM);
            clear_trap(&trap);
        }
        else {
            trace(LOG_ALWAYS, "Exception in message handler: %d %s",
                  trap.error, errno_to_str(trap.error));
            channel_close(c);
        }
    }
    channel_unlock(c);
    context_unlock(ctx);
    loc_free(args);
}

static void command_fill(char * token, Channel * c) {
    MemoryCommandArgs * args = read_command_args(token, c, CMD_FILL);
    if (args != NULL) post_safe_event(args->ctx->mem, safe_memory_fill, args);
}

static void send_event_context_added(OutputStream * out, Context * ctx) {
    write_stringz(out, "E");
    write_stringz(out, MEMORY);
    write_stringz(out, "contextAdded");

    /* <array of context data> */
    write_stream(out, '[');
    write_context(out, ctx);
    write_stream(out, ']');
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void send_event_context_changed(OutputStream * out, Context * ctx) {
    write_stringz(out, "E");
    write_stringz(out, MEMORY);
    write_stringz(out, "contextChanged");

    /* <array of context data> */
    write_stream(out, '[');
    write_context(out, ctx);
    write_stream(out, ']');
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void send_event_context_removed(OutputStream * out, Context * ctx) {
    write_stringz(out, "E");
    write_stringz(out, MEMORY);
    write_stringz(out, "contextRemoved");

    /* <array of context IDs> */
    write_stream(out, '[');
    json_write_string(out, ctx->id);
    write_stream(out, ']');
    write_stream(out, 0);

    write_stream(out, MARKER_EOM);
}

static void event_context_created(Context * ctx, void * client_data) {
    TCFBroadcastGroup * bcg = (TCFBroadcastGroup *)client_data;

    if (ctx->parent != NULL) return;
    send_event_context_added(&bcg->out, ctx);
}

static void event_context_changed(Context * ctx, void * client_data) {
    TCFBroadcastGroup * bcg = (TCFBroadcastGroup *)client_data;

    if (ctx->parent != NULL) return;
    send_event_context_changed(&bcg->out, ctx);
}

static void event_context_exited(Context * ctx, void * client_data) {
    TCFBroadcastGroup * bcg = (TCFBroadcastGroup *)client_data;

    if (ctx->parent != NULL) return;
    send_event_context_removed(&bcg->out, ctx);
}

void ini_memory_service(Protocol * proto, TCFBroadcastGroup * bcg) {
    static ContextEventListener listener = {
        event_context_created,
        event_context_exited,
        NULL,
        NULL,
        event_context_changed
    };
    add_context_event_listener(&listener, bcg);
    add_command_handler(proto, MEMORY, "getContext", command_get_context);
    add_command_handler(proto, MEMORY, "getChildren", command_get_children);
    add_command_handler(proto, MEMORY, "set", command_set);
    add_command_handler(proto, MEMORY, "get", command_get);
    add_command_handler(proto, MEMORY, "fill", command_fill);
}

#endif /* SERVICE_Memory */


