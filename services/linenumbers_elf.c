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
                DWARFCache * cache = get_dwarf_cache(file);
                ObjectInfo * info = cache->mCompUnits;
                while (info != NULL) {
                    CompUnit * unit = info->mCompUnit;
                    int equ = 0;
                    assert(unit->mFile == file);
                    info = info->mSibling;
                    if (unit->mDir != NULL && unit->mObject->mName != NULL) {
                        equ = cmp_file(file_name, unit->mDir, unit->mObject->mName);
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
                        load_line_numbers(unit);
                        if (unit->mStatesCnt >= 2) {
                            U4_T j;
                            for (j = 0; j < unit->mStatesCnt - 1; j++) {
                                LineNumbersState * state = unit->mStates + j;
                                LineNumbersState * next = unit->mStates + j + 1;
                                char * state_dir = unit->mDir;
                                char * state_file = unit->mObject->mName;
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
    Trap trap;

    if (!set_trap(&trap)) return -1;
    if (ctx == NULL) exception(ERR_INV_CONTEXT);
    if (ctx->exited) exception(ERR_ALREADY_EXITED);
    ctx = ctx->mem;
    while (addr0 < addr1) {
        UnitAddressRange * range = elf_find_unit(ctx, addr0, addr1);
        if (range == NULL) break;
        load_line_numbers(range->mUnit);
        if (range->mUnit->mStatesCnt >= 2) {
            U4_T i;
            CompUnit * unit = range->mUnit;
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
        addr0 = range->mAddr + range->mSize;
    }
    clear_trap(&trap);
    return 0;
}

#endif /* SERVICE_LineNumbers && !ENABLE_LineNumbersProxy && ENABLE_ELF */
