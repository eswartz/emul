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
 * TCF service line Numbers
 * The service associates locations in the source files with the corresponding
 * machine instruction addresses in the executable object.
 */

#include "config.h"

#if SERVICE_LineNumbers && defined(WIN32)

#include <errno.h>
#include <assert.h>
#include <stdio.h>
#include "linenumbers.h"
#include "breakpoints.h"
#include "windbgcache.h"
#include "context.h"
#include "exceptions.h"
#include "symbols.h"
#include "json.h"
#include "protocol.h"

static const char * LINENUMBERS = "LineNumbers";


int line_to_address(Context * ctx, char * file, int line, int column, LineToAddressCallBack * callback, void * user_args) {
    int err = 0;

    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    if (err == 0) {
        LONG offset = 0;
        IMAGEHLP_LINE img_line;
        memset(&img_line, 0, sizeof(img_line));
        img_line.SizeOfStruct = sizeof(IMAGEHLP_LINE);

        if (!SymGetLineFromName(ctx->handle, NULL, file, line, &offset, &img_line)) {
            DWORD win_err = GetLastError();
            if (win_err != ERROR_NOT_FOUND) {
                err = set_win32_errno(win_err);
            }
        }
        else {
            callback(user_args, img_line.Address);
        }
    }

    if (err != 0) {
        errno = err;
        return -1;
    }
    return 0;
}

static int read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    int err = 0;
    if (context_read_mem(ctx, address, buf, size) < 0) err = errno;
    else check_breakpoints_on_memory_read(ctx, address, buf, size);
    return err;
}

#define JMPD08      0xeb
#define JMPD32      0xe9
#define GRP5        0xff
#define JMPN        0x25

static void command_map_to_source(char * token, Channel * c) {
    int err = 0;
    int not_found = 0;
    char id[256];
    ContextAddress addr0;
    ContextAddress addr1;
    Context * ctx = NULL;
    DWORD offset = 0;
    IMAGEHLP_LINE line;
    IMAGEHLP_LINE next;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    addr0 = json_read_ulong(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    addr1 = json_read_ulong(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    ctx = id2ctx(id);
    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    if (err == 0 && ctx->parent != NULL) ctx = ctx->parent;

    memset(&line, 0, sizeof(line));
    line.SizeOfStruct = sizeof(IMAGEHLP_LINE);
    if (addr0 >= addr1) not_found = 1;

    while (err == 0 && not_found == 0 && !SymGetLineFromAddr(ctx->handle, addr0, &offset, &line)) {
        DWORD w = GetLastError();
        if (w == ERROR_MOD_NOT_FOUND) {
            not_found = 1;
        }
        else if (w == ERROR_INVALID_ADDRESS) {
            /* Check if the address points to a jump instruction (e.g. inside a jump table)
             * and try to get line info for jump destination address.
             */
            unsigned char instr;    /* instruction opcode at <addr0> */
            ContextAddress dest = 0; /* Jump destination address */
            if (read_mem(ctx, addr0, &instr, 1) == 0) {
                /* If instruction is a JMP, get destination adrs */
                if (instr == JMPD08) {
                    signed char disp08;
                    if (read_mem(ctx, addr0 + 1, &disp08, 1) == 0) {
                        dest = addr0 + 2 + disp08;
                    }
                }
                else if (instr == JMPD32) {
                    int disp32;
                    assert(sizeof(disp32) == 4);
                    if (read_mem(ctx, addr0 + 1, &disp32, 4) == 0) {
                        dest = addr0 + 5 + disp32;
                    }
                }
                else if (instr == GRP5) {
                    if (read_mem(ctx, addr0 + 1, &instr, 1) == 0 && instr == JMPN) {
                        ContextAddress ptr = 0;
                        if (read_mem(ctx, addr0 + 2, &ptr, 4) == 0) {
                            read_mem(ctx, ptr, &dest, 4);
                        }
                    }
                }
            }
            if (dest != 0) {
                addr0 = dest;
                addr1 = dest + 1;
            }
            else {
                not_found = 1;
            }
        }
        else {
            err = set_win32_errno(w);
        }
    }
    memcpy(&next, &line, sizeof(next));
    if (err == 0 && !not_found && !SymGetLineNext(ctx->handle, &next)) {
        err = set_win32_errno(GetLastError());
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err != 0 || not_found) {
        write_stringz(&c->out, "null");
    }
    else {
        int cnt = 0;
        write_stream(&c->out, '[');
        for (;;) {
            if (cnt > 0) write_stream(&c->out, ',');
            write_stream(&c->out, '{');
            json_write_string(&c->out, "SLine");
            write_stream(&c->out, ':');
            json_write_ulong(&c->out, line.LineNumber);
            write_stream(&c->out, ',');
            json_write_string(&c->out, "ELine");
            write_stream(&c->out, ':');
            json_write_ulong(&c->out, next.LineNumber);
            write_stream(&c->out, ',');
            json_write_string(&c->out, "File");
            write_stream(&c->out, ':');
            json_write_string(&c->out, line.FileName);
            write_stream(&c->out, ',');
            json_write_string(&c->out, "SAddr");
            write_stream(&c->out, ':');
            json_write_ulong(&c->out, line.Address);
            write_stream(&c->out, ',');
            json_write_string(&c->out, "EAddr");
            write_stream(&c->out, ':');
            json_write_ulong(&c->out, next.Address);
            write_stream(&c->out, '}');
            cnt++;
            if (next.Address >= addr1) break;
            memcpy(&line, &next, sizeof(line));
            if (!SymGetLineNext(ctx->handle, &next)) break;
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }
    write_stream(&c->out, MARKER_EOM);
}

void ini_line_numbers_service(Protocol * proto) {
    add_command_handler(proto, LINENUMBERS, "mapToSource", command_map_to_source);
}

#endif /* SERVICE_LineNumbers && defined(WIN32) */

