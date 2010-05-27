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
 * TCF service line Numbers
 * The service associates locations in the source files with the corresponding
 * machine instruction addresses in the executable object.
 */

#include <config.h>

#if SERVICE_LineNumbers && !ENABLE_LineNumbersProxy && defined(_MSC_VER) && !ENABLE_ELF

#include <errno.h>
#include <assert.h>
#include <stdio.h>
#include <framework/json.h>
#include <framework/protocol.h>
#include <framework/context.h>
#include <framework/exceptions.h>
#include <services/linenumbers.h>
#include <system/Windows/windbgcache.h>
#include <system/Windows/context-win32.h>

int line_to_address(Context * ctx, char * file, int line, int column, LineNumbersCallBack * callback, void * user_args) {
    int err = 0;

    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    if (err == 0 && ctx->parent != NULL) ctx = ctx->parent;

    if (err == 0) {
        LONG offset = 0;
        IMAGEHLP_LINE img_line;
        CodeArea area;
        memset(&img_line, 0, sizeof(img_line));
        memset(&area, 0, sizeof(area));
        img_line.SizeOfStruct = sizeof(IMAGEHLP_LINE);

        if (!SymGetLineFromName(get_context_handle(ctx), NULL, file, line, &offset, &img_line)) {
            DWORD win_err = GetLastError();
            if (win_err != ERROR_NOT_FOUND) {
                err = set_win32_errno(win_err);
            }
        }
        else {
            IMAGEHLP_LINE img_next;
            memcpy(&img_next, &img_line, sizeof(img_next));
            if (!SymGetLineNext(get_context_handle(ctx), &img_next)) {
                err = set_win32_errno(GetLastError());
            }
            else {
                area.file = img_line.FileName;
                area.start_line = img_line.LineNumber;
                area.start_address = img_line.Address;
                area.end_line = img_next.LineNumber;
                area.end_address = img_next.Address;
                callback(&area, user_args);
            }
        }
    }

    if (err != 0) {
        errno = err;
        return -1;
    }
    return 0;
}

#define JMPD08      0xeb
#define JMPD32      0xe9
#define GRP5        0xff
#define JMPN        0x25

int address_to_line(Context * ctx, ContextAddress addr0, ContextAddress addr1, LineNumbersCallBack * callback, void * user_args) {
    int err = 0;
    int not_found = 0;
    DWORD offset = 0;
    IMAGEHLP_LINE line;
    IMAGEHLP_LINE next;

    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    if (err == 0 && ctx->parent != NULL) ctx = ctx->parent;

    memset(&line, 0, sizeof(line));
    line.SizeOfStruct = sizeof(IMAGEHLP_LINE);
    if (addr0 >= addr1) not_found = 1;

    while (err == 0 && not_found == 0 && !SymGetLineFromAddr(get_context_handle(ctx), addr0, &offset, &line)) {
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
            if (context_read_mem(ctx, addr0, &instr, 1) == 0) {
                /* If instruction is a JMP, get destination adrs */
                if (instr == JMPD08) {
                    signed char disp08;
                    if (context_read_mem(ctx, addr0 + 1, &disp08, 1) == 0) {
                        dest = addr0 + 2 + disp08;
                    }
                }
                else if (instr == JMPD32) {
                    int disp32;
                    assert(sizeof(disp32) == 4);
                    if (context_read_mem(ctx, addr0 + 1, &disp32, 4) == 0) {
                        dest = addr0 + 5 + disp32;
                    }
                }
                else if (instr == GRP5) {
                    if (context_read_mem(ctx, addr0 + 1, &instr, 1) == 0 && instr == JMPN) {
                        ContextAddress ptr = 0;
                        if (context_read_mem(ctx, addr0 + 2, &ptr, 4) == 0) {
                            context_read_mem(ctx, ptr, &dest, 4);
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
    if (err == 0 && !not_found && !SymGetLineNext(get_context_handle(ctx), &next)) {
        err = set_win32_errno(GetLastError());
    }

    if (err == 0 && !not_found) {
        for (;;) {
            CodeArea area;
            memset(&area, 0, sizeof(area));
            area.file = line.FileName;
            area.start_address = line.Address;
            area.start_line = line.LineNumber;
            area.end_address = next.Address;
            area.end_line = next.LineNumber;
            callback(&area, user_args);
            if (next.Address >= addr1) break;
            memcpy(&line, &next, sizeof(line));
            if (!SymGetLineNext(get_context_handle(ctx), &next)) break;
        }
    }

    if (err != 0) {
        errno = err;
        return -1;
    }
    return 0;
}

#endif /* SERVICE_LineNumbers && !ENABLE_LineNumbersProxy && defined(_MSC_VER) && !ENABLE_ELF */

