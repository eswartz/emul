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

static int is_absolute_path(char * fnm) {
    if (fnm[0] == '/') return 1;
    if (fnm[0] == '\\') return 1;
    if (fnm[0] != 0 && fnm[1] == ':') {
        if (fnm[2] == '/') return 1;
        if (fnm[2] == '\\') return 1;
    }
    return 0;
}

static void canonic_path(char * fnm, char * buf, size_t buf_size) {
    unsigned i = 0;
    while (i < buf_size - 1) {
        char ch = *fnm++;
        if (ch == 0) break;
        if (ch == '\\') ch = '/';
        if (ch == '/' && i >= 2 && buf[i - 1] == '/') continue;
        if (ch == '.' && (i == 0 || buf[i - 1] == '/')) {
            if (*fnm == '/' || *fnm == '\\') {
                fnm++;
                continue;
            }
            if (i > 0 && *fnm == '.' && (fnm[1] == '/' || fnm[1] == '\\')) {
                unsigned j = i - 1;
                if (j > 0 && buf[j - 1] != '/') {
                    while (j > 0 && buf[j - 1] != '/') j--;
                    i = j;
                    fnm += 2;
                    continue;
                }
            }
        }
        buf[i++] = ch;
    }
    buf[i++] = 0;
}

static int compare_path(char * file, char * pwd, char * dir, char * name) {
    int i, j;
    char buf[FILE_PATH_SIZE];

    if (file == NULL) return 0;
    if (name == NULL) return 0;

    if (is_absolute_path(name)) {
        canonic_path(name, buf, sizeof(buf));
    }
    else if (dir != NULL && is_absolute_path(dir)) {
        snprintf(buf, sizeof(buf), "%s/%s", dir, name);
        canonic_path(buf, buf, sizeof(buf));
    }
    else if (dir != NULL && pwd != NULL) {
        snprintf(buf, sizeof(buf), "%s/%s/%s", pwd, dir, name);
        canonic_path(buf, buf, sizeof(buf));
    }
    else if (pwd != NULL) {
        snprintf(buf, sizeof(buf), "%s/%s", pwd, name);
        canonic_path(buf, buf, sizeof(buf));
    }
    else {
        canonic_path(name, buf, sizeof(buf));
    }

    i = strlen(file);
    j = strlen(buf);
    return i <= j && strcmp(file, buf + j - i) == 0;
}

static LineNumbersState * get_next_in_text(CompUnit * unit, LineNumbersState * state) {
    LineNumbersState * next = unit->mStates + state->mNext;
    if (state->mNext == 0) return NULL;
    while (next->mLine == state->mLine && next->mColumn == state->mColumn) {
        if (next->mNext == 0) return NULL;
        next = unit->mStates + next->mNext;
    }
    if (state->mFile != next->mFile) return NULL;
    return next;
}

static void call_client(CompUnit * unit, LineNumbersState * state,
                        ContextAddress state_addr, LineNumbersCallBack * client, void * args) {
    CodeArea area;
    LineNumbersState * next = get_next_in_text(unit, state);
    FileInfo * file_info = unit->mFiles + state->mFile;

    if (state->mAddress >= (state + 1)->mAddress) return;
    memset(&area, 0, sizeof(area));
    area.start_line = state->mLine;
    area.start_column = state->mColumn;
    area.end_line = next ? next->mLine : state->mLine;
    area.end_column = next ? next->mColumn : 0;

    area.directory = unit->mDir;
    if (state->mFileName != NULL) {
        area.file = state->mFileName;
    }
    else if (is_absolute_path(file_info->mName) || file_info->mDir == NULL) {
        area.file = file_info->mName;
    }
    else if (is_absolute_path(file_info->mDir)) {
        area.directory = file_info->mDir;
        area.file = file_info->mName;
    }
    else {
        char buf[FILE_PATH_SIZE];
        snprintf(buf, sizeof(buf), "%s/%s", file_info->mDir, file_info->mName);
        area.file = state->mFileName = loc_strdup(buf);
    }

    area.file_mtime = file_info->mModTime;
    area.file_size = file_info->mSize;
    area.start_address = state_addr;
    area.end_address = (state + 1)->mAddress - state->mAddress + state_addr;
    area.isa = state->mISA;
    area.is_statement = (state->mFlags & LINE_IsStmt) != 0;
    area.basic_block = (state->mFlags & LINE_BasicBlock) != 0;
    area.prologue_end = (state->mFlags & LINE_PrologueEnd) != 0;
    area.epilogue_begin = (state->mFlags & LINE_EpilogueBegin) != 0;
    client(&area, args);
}

static void unit_line_to_address(Context * ctx, CompUnit * unit, unsigned file, unsigned line, unsigned column,
                                 LineNumbersCallBack * client, void * args) {
    if (unit->mStatesCnt >= 2) {
        unsigned l = 0;
        unsigned h = unit->mStatesCnt - 1;
        while (l < h) {
            unsigned k = (h + l) / 2;
            LineNumbersState * state = unit->mStatesIndex[k];
            if (state->mFile < file) {
                l = k + 1;
            }
            else if (state->mFile > file || state->mLine > line) {
                h = k;
            }
            else {
                LineNumbersState * next = get_next_in_text(unit, state);
                if (next != NULL && next->mLine <= line) {
                    l = k + 1;
                }
                else {
                    unsigned i = k;
                    while (i > 0) {
                        LineNumbersState * prev = unit->mStatesIndex[i - 1];
                        if (prev->mFile != state->mFile) break;
                        if (prev->mLine != state->mLine) break;
                        if (prev->mColumn != state->mColumn) break;
                        state = prev;
                        i--;
                    }
                    for (;;) {
                        ContextAddress addr = elf_map_to_run_time_address(ctx, unit->mFile, unit->mTextSection, state->mAddress);
                        if (addr != 0) call_client(unit, state, addr, client, args);
                        if (i == k) break;
                        state = unit->mStatesIndex[++i];
                    }
                    break;
                }
            }
        }
    }
}

int line_to_address(Context * ctx, char * file_name, int line, int column, LineNumbersCallBack * client, void * args) {
    int err = 0;

    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    if (err == 0) {
        ELF_File * file = elf_list_first(ctx, 0, ~(ContextAddress)0);
        if (file == NULL) err = errno;
        if (err == 0) {
            unsigned h;
            char fnm[FILE_PATH_SIZE];
            canonic_path(file_name, fnm, sizeof(fnm));
            h = calc_file_name_hash(fnm);
            while (file != NULL) {
                Trap trap;
                /* TODO: support for separate debug info files */
                if (set_trap(&trap)) {
                    DWARFCache * cache = get_dwarf_cache(file);
                    ObjectInfo * info = cache->mCompUnits;
                    while (info != NULL) {
                        unsigned j;
                        CompUnit * unit = info->mCompUnit;
                        assert(unit->mFile == file);
                        load_line_numbers(unit);
                        for (j = 0; j < unit->mFilesCnt; j++) {
                            FileInfo * f = unit->mFiles + j;
                            if (f->mNameHash != h) continue;
                            if (!compare_path(fnm, unit->mDir, f->mDir, f->mName)) continue;
                            unit_line_to_address(ctx, unit, j, line, column, client, args);
                        }
                        info = info->mSibling;
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
    while (addr0 < addr1) {
        ContextAddress range_rt_addr = 0;
        UnitAddressRange * range = elf_find_unit(ctx, addr0, addr1, &range_rt_addr);
        if (range == NULL) break;
        assert(range_rt_addr != 0);
        load_line_numbers(range->mUnit);
        if (range->mUnit->mStatesCnt >= 2) {
            CompUnit * unit = range->mUnit;
            unsigned l = 0;
            unsigned h = unit->mStatesCnt - 1;
            ContextAddress addr_min = addr0 - range_rt_addr + range->mAddr;
            ContextAddress addr_max = addr1 - range_rt_addr + range->mAddr;
            if (addr_min < range->mAddr) addr_min = range->mAddr;
            if (addr_max > range->mAddr + range->mSize) addr_max = range->mAddr + range->mSize;
            while (l < h) {
                unsigned k = (h + l) / 2;
                LineNumbersState * state = unit->mStates + k;
                if (state->mAddress >= addr_max) {
                    h = k;
                }
                else {
                    LineNumbersState * next = state + 1;
                    assert(next->mAddress >= state->mAddress);
                    if (next->mAddress <= addr_min) {
                        l = k + 1;
                    }
                    else {
                        while (k > 0) {
                            LineNumbersState * prev = unit->mStates + k - 1;
                            if (state->mAddress <= addr_min) break;
                            if (prev->mAddress >= addr_max) break;
                            next = state;
                            state = prev;
                            k--;
                        }
                        for (;;) {
                            call_client(unit, state, state->mAddress - range->mAddr + range_rt_addr, client, args);
                            k++;
                            if (k >= unit->mStatesCnt - 1) break;
                            state = unit->mStates + k;
                            if (state->mAddress >= addr_max) break;
                            next = state + 1;
                        }
                        break;
                    }
                }
            }
        }
        addr0 = range_rt_addr + range->mSize;
    }
    clear_trap(&trap);
    return 0;
}

void ini_line_numbers_lib(void) {
}

#endif /* SERVICE_LineNumbers && !ENABLE_LineNumbersProxy && ENABLE_ELF */
