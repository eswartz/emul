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
 * TCF service line Numbers - ELF version.
 *
 * The service associates locations in the source files with the corresponding
 * machine instruction addresses in the executable object.
 */

#include "config.h"

#if SERVICE_LineNumbers && ENABLE_ELF

#include <errno.h>
#include <assert.h>
#include <stdio.h>
#include "linenumbers.h"
#include "context.h"
#include "myalloc.h"
#include "exceptions.h"
#include "json.h"
#include "protocol.h"
#include "tcf_elf.h"
#include "dwarfio.h"
#include "dwarf.h"
#include "dwarfcache.h"
#include "trace.h"

static const char * LINENUMBERS = "LineNumbers";

static CompUnit * find_unit(Context * ctx, DWARFCache * cache, ContextAddress addr0, ContextAddress addr1, ContextAddress * addr_next) {
    U4_T i;
    ContextAddress addr_min = 0;
    CompUnit * unit = NULL;
    /* TODO: faster unit search */
    for (i = 0; i < cache->mCompUnitsCnt; i++) {
        CompUnit * u = cache->mCompUnits[i];
        ContextAddress base = elf_map_to_run_time_address(ctx, cache->mFile, u->mLowPC);
        ContextAddress size = u->mHighPC - u->mLowPC;
        if (base == 0 || size == 0) continue;
        if (u->mDebugRangesOffs != ~(U8_T)0 && cache->mDebugRanges != NULL) {
            if (elf_load(cache->mDebugRanges)) exception(errno);
            dio_EnterDataSection(&u->mDesc, cache->mDebugRanges->data, u->mDebugRangesOffs, cache->mDebugRanges->size);
            for (;;) {
                U8_T x = dio_ReadAddress();
                U8_T y = dio_ReadAddress();
                if (x == 0 && y == 0) break;
                if (x == ((U8_T)1 << u->mDesc.mAddressSize * 8) - 1) {
                    base = (ContextAddress)y;
                }
                else {
                    x = base + x;
                    y = base + y;
                    if (addr0 < y && addr1 > x) {
                        if (unit == NULL || addr_min > x) {
                            unit = u;
                            addr_min = (ContextAddress)x;
                            *addr_next = (ContextAddress)y;
                        }
                    }
                }
            }
            dio_ExitSection();
        }
        else if (addr0 < base + size && addr1 > base) {
            if (unit == NULL || addr_min > base) {
                unit = u;
                addr_min = base;
                *addr_next = base + size;
            }
        }
    }
    return unit;
}

static void load_line_numbers_in_range(Context * ctx, DWARFCache * cache, ContextAddress addr0, ContextAddress addr1) {
    while (addr0 < addr1) {
        ContextAddress next = 0;
        CompUnit * unit = find_unit(ctx, cache, addr0, addr1, &next);
        if (unit == NULL) break;
        load_line_numbers(cache, unit);
        addr0 = next;
    }
}

static int cmp_file(char * file, char * dir, char * name) {
    int i;
    if (file == NULL) return 0;
    if (name == NULL) return 0;
    if (strcmp(file, name) == 0) return 1;
    i = strlen(name);
    while (i > 0 && name[i - 1] != '/' && name[i - 1] != '\\') i--;
    if (strcmp(file, name + i) == 0) return 1;
    if (dir == NULL) return 0;
    i = strlen(dir);
    if (strncmp(dir, file, i) == 0 && (file[i] == '/' || file[i] == '\\') &&
            strcmp(file + i + 1, name) == 0) return 1;
    return 0;
}

static void write_line_info(Context * ctx, OutputStream * out, CompUnit * unit,
                            ContextAddress addr0, ContextAddress addr1,
                            int * cnt, FileInfo ** file_info) {
    U4_T i;
    FileInfo * state_file = NULL;
    if (unit->mStatesCnt < 2) return;
    for (i = 0; i < unit->mStatesCnt - 1; i++) {
        LineNumbersState * state = unit->mStates + i;
        LineNumbersState * next = unit->mStates + i + 1;
        ContextAddress state_addr = elf_map_to_run_time_address(ctx, unit->mFile, state->mAddress);
        ContextAddress next_addr = elf_map_to_run_time_address(ctx, unit->mFile, next->mAddress);
        if (state->mFlags & LINE_EndSequence) continue;
        if (next_addr > addr0 && state_addr < addr1) {
            if (*cnt > 0) write_stream(out, ',');
            write_stream(out, '{');
            json_write_string(out, "SLine");
            write_stream(out, ':');
            json_write_ulong(out, state->mLine);
            if (state->mColumn > 0) {
                write_stream(out, ',');
                json_write_string(out, "SCol");
                write_stream(out, ':');
                json_write_ulong(out, state->mColumn);
            }
            write_stream(out, ',');
            json_write_string(out, "ELine");
            write_stream(out, ':');
            json_write_ulong(out, next->mLine);
            if (next->mColumn > 0) {
                write_stream(out, ',');
                json_write_string(out, "ECol");
                write_stream(out, ':');
                json_write_ulong(out, next->mColumn);
            }
            state_file = NULL;
            if (state->mFile >= 1 && state->mFile <= unit->mFilesCnt) {
                state_file = unit->mFiles + (state->mFile - 1);
            }
            if (*file_info != state_file) {
                *file_info = state_file;
                write_stream(out, ',');
                json_write_string(out, "File");
                write_stream(out, ':');
                json_write_string(out, *file_info == NULL ? NULL : (*file_info)->mName);
                write_stream(out, ',');
                json_write_string(out, "Dir");
                write_stream(out, ':');
                json_write_string(out, *file_info == NULL ? NULL : (*file_info)->mDir);
            }
            write_stream(out, ',');
            json_write_string(out, "SAddr");
            write_stream(out, ':');
            json_write_ulong(out, state_addr);
            write_stream(out, ',');
            json_write_string(out, "EAddr");
            write_stream(out, ':');
            json_write_ulong(out, next_addr);
            if (state->mISA != 0) {
                write_stream(out, ',');
                json_write_string(out, "ISA");
                write_stream(out, ':');
                json_write_ulong(out, state->mISA);
            }
            if (state->mFlags & LINE_IsStmt) {
                write_stream(out, ',');
                json_write_string(out, "IsStmt");
                write_stream(out, ':');
                json_write_boolean(out, 1);
            }
            if (state->mFlags & LINE_BasicBlock) {
                write_stream(out, ',');
                json_write_string(out, "BasicBlock");
                write_stream(out, ':');
                json_write_boolean(out, 1);
            }
            if (state->mFlags & LINE_PrologueEnd) {
                write_stream(out, ',');
                json_write_string(out, "PrologueEnd");
                write_stream(out, ':');
                json_write_boolean(out, 1);
            }
            if (state->mFlags & LINE_EpilogueBegin) {
                write_stream(out, ',');
                json_write_string(out, "EpilogueBegin");
                write_stream(out, ':');
                json_write_boolean(out, 1);
            }
            write_stream(out, '}');
            (*cnt)++;
        }
    }
}

int line_to_address(Context * ctx, char * file_name, int line, int column, LineToAddressCallBack * callback, void * user_args) {
    int err = 0;

    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    if (err == 0) {
        ELF_File * file = elf_list_first(ctx, 0, ~(ContextAddress)0);
        if (file == NULL) err = errno;
        while (file != NULL) {
            Trap trap;
            if (set_trap(&trap)) {
                U4_T i;
                DWARFCache * cache = get_dwarf_cache(file);
                for (i = 0; i < cache->mCompUnitsCnt; i++) {
                    CompUnit * unit = cache->mCompUnits[i];
                    int equ = 0;
                    assert(unit->mFile == file);
                    if (unit->mDir != NULL && unit->mName != NULL) {
                        equ = cmp_file(file_name, unit->mDir, unit->mName);
                    }
                    if (!equ) {
                        U4_T j;
                        for (j = 0; j < unit->mFilesCnt; j++) {
                            FileInfo * f = unit->mFiles + j;
                            if (f->mDir != NULL && f->mName != NULL) {
                                equ = cmp_file(file_name, f->mDir, f->mName);
                                if (equ) break;
                            }
                        }
                    }
                    if (equ) {
                        load_line_numbers(cache, unit);
                        if (unit->mStatesCnt >= 2) {
                            U4_T j;
                            for (j = 0; j < unit->mStatesCnt - 1; j++) {
                                LineNumbersState * state = unit->mStates + j;
                                LineNumbersState * next = unit->mStates + j + 1;
                                char * state_dir = unit->mDir;
                                char * state_name = unit->mName;
                                ContextAddress addr = 0;
                                if (state->mFlags & LINE_EndSequence) continue;
                                if ((unsigned)line < state->mLine) continue;
                                if ((unsigned)line >= next->mLine) continue;
                                if (state->mFile >= 1 && state->mFile <= unit->mFilesCnt) {
                                    FileInfo * f = unit->mFiles + (state->mFile - 1);
                                    state_dir = f->mDir;
                                    state_name = f->mName;
                                }
                                if (!cmp_file(file_name, state_dir, state_name)) continue;
                                addr = elf_map_to_run_time_address(ctx, file, state->mAddress);
                                if (addr == 0) continue;
                                callback(user_args, addr);
                            }
                        }
                    }
                }
                clear_trap(&trap);
            }
            else {
                err = trap.error;
                break;
            }
            file = elf_list_next(ctx);
            if (file == NULL) err = errno;
        }
        elf_list_done(ctx);
    }

    if (err != 0) {
        errno = err;
        return -1;
    }
    return 0;
}

static void command_map_to_source(char * token, Channel * c) {
    int err = 0;
    char id[256];
    ContextAddress addr0;
    ContextAddress addr1;
    Context * ctx = NULL;
    DWARFCache * cache_first = NULL;
    DWARFCache * cache_last = NULL;

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

    if (err == 0) {
        ELF_File * file = elf_list_first(ctx, addr0, addr1);
        if (file == NULL) err = errno;
        while (file != NULL) {
            Trap trap;
            if (set_trap(&trap)) {
                DWARFCache * cache = get_dwarf_cache(file);
                load_line_numbers_in_range(ctx, cache, addr0, addr1);
                clear_trap(&trap);
                if (cache_last == NULL) {
                    cache_first = cache;
                }
                else {
                    cache_last->mLineInfoNext = cache;
                }
                cache_last = cache;
            }
            else {
                err = trap.error;
                break;
            }
            file = elf_list_next(ctx);
            if (file == NULL) err = errno;
        }
        elf_list_done(ctx);
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    if (err != 0) {
        write_stringz(&c->out, "null");
    }
    else {
        int cnt = 0;
        FileInfo * file_info = NULL;
        DWARFCache * cache = cache_first;
        write_stream(&c->out, '[');
        while (cache != NULL) {
            Trap trap;
            if (set_trap(&trap)) {
                while (err == 0 && addr0 < addr1) {
                    ContextAddress next = 0;
                    CompUnit * unit = find_unit(ctx, cache, addr0, addr1, &next);
                    if (unit == NULL) break;
                    write_line_info(ctx, &c->out, unit, addr0, addr1, &cnt, &file_info);
                    addr0 = next;
                }
                clear_trap(&trap);
            }
            else {
                err = trap.error;
            }
            if (cache == cache_last) break;
            cache = cache->mLineInfoNext;
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
        if (err != 0) trace(LOG_ALWAYS, "Line numbers info error %d: %d", err, errno_to_str(err));
    }
    write_stream(&c->out, MARKER_EOM);
}

void ini_line_numbers_service(Protocol * proto) {
    add_command_handler(proto, LINENUMBERS, "mapToSource", command_map_to_source);
}

#endif /* SERVICE_LineNumbers && ENABLE_ELF */

