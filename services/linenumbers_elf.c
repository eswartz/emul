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
 * TCF service line Numbers - ELF version.
 *
 * The service associates locations in the source files with the corresponding
 * machine instruction addresses in the executable object.
 */

#include <config.h>

#if SERVICE_LineNumbers && !ENABLE_LineNumbersProxy && ENABLE_ELF

#include <errno.h>
#include <assert.h>
#include <stdio.h>
#include <framework/context.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <framework/cache.h>
#include <framework/trace.h>
#include <framework/json.h>
#include <framework/protocol.h>
#include <services/linenumbers.h>
#include <services/tcf_elf.h>
#include <services/dwarfio.h>
#include <services/dwarf.h>
#include <services/dwarfcache.h>
#include <services/stacktrace.h>

static CompUnit * find_unit(Context * ctx, DWARFCache * cache, ContextAddress addr0, ContextAddress addr1, ContextAddress * addr_next) {
    U4_T i;
    ContextAddress addr_min = 0;
    CompUnit * unit = NULL;
    /* TODO: faster unit search */
    for (i = 0; i < cache->mCompUnitsCnt; i++) {
        CompUnit * u = cache->mCompUnits[i];
        ContextAddress base = elf_map_to_run_time_address(ctx, cache->mFile, u->mTextSection, u->mLowPC);
        ContextAddress size = u->mHighPC - u->mLowPC;
        if (base == 0 || size == 0) continue;
        if (u->mDebugRangesOffs != ~(U8_T)0 && cache->mDebugRanges != NULL) {
            if (elf_load(cache->mDebugRanges)) exception(errno);
            dio_EnterSection(&u->mDesc, cache->mDebugRanges, u->mDebugRangesOffs);
            for (;;) {
                ELF_Section * s = NULL;
                U8_T x = dio_ReadAddress(&s);
                U8_T y = dio_ReadAddress(&s);
                if (x == 0 && y == 0) break;
                if (s != u->mTextSection) exception(ERR_INV_DWARF);
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

static void call_client(CompUnit * unit, LineNumbersState * state, LineNumbersState * next,
                        ContextAddress state_addr, ContextAddress next_addr,
                        LineNumbersCallBack * client, void * args) {
    CodeArea area;
    FileInfo * state_file = NULL;
    memset(&area, 0, sizeof(area));
    area.start_line = state->mLine;
    area.start_column = state->mColumn;
    area.end_line = next->mLine;
    area.end_column = next->mColumn;
    if (state->mFile >= 1 && state->mFile <= unit->mFilesCnt) {
        state_file = unit->mFiles + (state->mFile - 1);
    }
    if (state_file != NULL) {
        area.file = state_file->mName;
        area.directory = state_file->mDir;
    }
    area.start_address = state_addr;
    area.end_address = next_addr;
    area.isa = state->mISA;
    area.is_statement = (state->mFlags & LINE_IsStmt) != 0;
    area.basic_block = (state->mFlags & LINE_BasicBlock) != 0;
    area.prologue_end = (state->mFlags & LINE_PrologueEnd) != 0;
    area.epilogue_begin = (state->mFlags & LINE_EpilogueBegin) != 0;
    client(&area, args);
}

int line_to_address(Context * ctx, char * file_name, int line, int column, LineNumbersCallBack * client, void * args) {
    int err = 0;

    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;
    else ctx = ctx->mem;

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
                                char * state_file = unit->mName;
                                ContextAddress addr = 0;
                                ContextAddress next_addr = 0;
                                if (state->mFlags & LINE_EndSequence) continue;
                                if ((unsigned)line < state->mLine) continue;
                                if ((unsigned)line >= next->mLine) continue;
                                if (state->mFile >= 1 && state->mFile <= unit->mFilesCnt) {
                                    FileInfo * f = unit->mFiles + (state->mFile - 1);
                                    state_dir = f->mDir;
                                    state_file = f->mName;
                                }
                                if (!cmp_file(file_name, state_dir, state_file)) continue;
                                addr = elf_map_to_run_time_address(ctx, file, unit->mTextSection, state->mAddress);
                                if (addr == 0) continue;
                                next_addr = elf_map_to_run_time_address(ctx, file, unit->mTextSection, next->mAddress);
                                if (next_addr == 0) continue;
                                call_client(unit, state, next, addr, next_addr, client, args);
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

int address_to_line(Context * ctx, ContextAddress addr0, ContextAddress addr1, LineNumbersCallBack * client, void * args) {
    int err = 0;

    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;
    else ctx = ctx->mem;

    if (err == 0) {
        ELF_File * file = elf_list_first(ctx, addr0, addr1);
        if (file == NULL) err = errno;
        while (file != NULL) {
            Trap trap;
            if (set_trap(&trap)) {
                DWARFCache * cache = get_dwarf_cache(file);
                load_line_numbers_in_range(ctx, cache, addr0, addr1);
                while (err == 0 && addr0 < addr1) {
                    ContextAddress next_unit_addr = 0;
                    CompUnit * unit = find_unit(ctx, cache, addr0, addr1, &next_unit_addr);
                    if (unit == NULL) break;
                    if (unit->mStatesCnt >= 2) {
                        U4_T i;
                        for (i = 0; i < unit->mStatesCnt - 1; i++) {
                            LineNumbersState * state = unit->mStates + i;
                            LineNumbersState * next = unit->mStates + i + 1;
                            if ((state->mFlags & LINE_EndSequence) == 0) {
                                ContextAddress state_addr = elf_map_to_run_time_address(ctx, unit->mFile, unit->mTextSection, state->mAddress);
                                ContextAddress next_addr = elf_map_to_run_time_address(ctx, unit->mFile, unit->mTextSection, next->mAddress);
                                if (next_addr > addr0 && state_addr < addr1) call_client(unit, state, next, state_addr, next_addr, client, args);
                            }
                        }
                    }
                    addr0 = next_unit_addr;
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

#endif /* SERVICE_LineNumbers && !ENABLE_LineNumbersProxy && ENABLE_ELF */
