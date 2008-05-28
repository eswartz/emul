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
 * TCF service line Numbers
 * The service associates locations in the source files with the corresponding
 * machine instruction addresses in the executable object.
 */

#include "mdep.h"
#include "config.h"

#if (SERVICE_LineNumbers) && defined(WIN32)

#include <errno.h>
#include <assert.h>
#include <stdio.h>
#include <imagehlp.h>
#include "linenumbers.h"
#include "context.h"
#include "exceptions.h"
#include "symbols.h"
#include "json.h"
#include "protocol.h"

static const char * LINENUMBERS = "LineNumbers";


static void command_map_to_source(char * token, Channel * c) {
    int err = 0;
    char * err_msg = NULL;
    char id[256];
    unsigned long addr0;
    unsigned long addr1;
    Context * ctx = NULL;
    DWORD offset = 0;
    IMAGEHLP_LINE line;
    IMAGEHLP_LINE next;

    json_read_string(&c->inp, id, sizeof(id));
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    addr0 = json_read_ulong(&c->inp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    addr1 = json_read_ulong(&c->inp);
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    ctx = id2ctx(id);
    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    if (err == 0 && ctx->parent != NULL) ctx = ctx->parent;
    if (err == 0 && set_symbol_context(ctx) != 0) err = errno;

    memset(&line, 0, sizeof(line));
    line.SizeOfStruct = sizeof(IMAGEHLP_LINE);

    if (err == 0 && !SymGetLineFromAddr(ctx->handle, addr0, &offset, &line)) {
        set_win32_errno(GetLastError());
        err = errno;
    }
    memcpy(&next, &line, sizeof(next));
    if (err == 0 && !SymGetLineNext(ctx->handle, &next)) {
        set_win32_errno(GetLastError());
        err = errno;
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_err_msg(&c->out, err, err_msg);
    if (err != 0) {
        write_stringz(&c->out, "null");
    }
    else {
        int cnt = 0;
        c->out.write(&c->out, '[');
        while (addr0 < addr1) {
            if (cnt > 0) c->out.write(&c->out, ',');
            c->out.write(&c->out, '{');
            json_write_string(&c->out, "SLine");
            c->out.write(&c->out, ':');
            json_write_ulong(&c->out, line.LineNumber);
            c->out.write(&c->out, ',');
            json_write_string(&c->out, "ELine");
            c->out.write(&c->out, ':');
            json_write_ulong(&c->out, next.LineNumber);
            c->out.write(&c->out, ',');
            json_write_string(&c->out, "File");
            c->out.write(&c->out, ':');
            json_write_string(&c->out, line.FileName);
            c->out.write(&c->out, ',');
            json_write_string(&c->out, "SAddr");
            c->out.write(&c->out, ':');
            json_write_ulong(&c->out, line.Address);
            c->out.write(&c->out, ',');
            json_write_string(&c->out, "EAddr");
            c->out.write(&c->out, ':');
            json_write_ulong(&c->out, next.Address);
            c->out.write(&c->out, '}');
            cnt++;
            if (next.Address >= addr1) break;
            memcpy(&line, &next, sizeof(line));
            if (!SymGetLineNext(ctx->handle, &next)) break;
        }
        c->out.write(&c->out, ']');
        c->out.write(&c->out, 0);
    }
    c->out.write(&c->out, MARKER_EOM);
}

void ini_line_numbers_service(Protocol * proto) {
    add_command_handler(proto, LINENUMBERS, "mapToSource", command_map_to_source);
}

#endif

