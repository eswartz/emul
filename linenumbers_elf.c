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

#if (SERVICE_LineNumbers) && !defined(WIN32)

#include <errno.h>
#include <assert.h>
#include <stdio.h>
#include "linenumbers.h"
#include "context.h"
#include "myalloc.h"
#include "exceptions.h"
#include "json.h"
#include "protocol.h"
#include "elf.h"
#include "dwarfio.h"
#include "dwarf.h"

static const char * LINENUMBERS = "LineNumbers";

typedef struct FileInfo FileInfo;

struct FileInfo {
    char * name;
    char * dir;
    U4_T mtime;
    U4_T size;
};

typedef struct LineNumbersState LineNumbersState;

struct LineNumbersState {
    unsigned file;
    unsigned line;
    unsigned column;
    ContextAddress address;
    U1_T isa;
    U1_T is_stmt;
    U1_T basic_block;
    U1_T prologue_end;
    U1_T epilogue_begin;
    U1_T end_sequence;
};

typedef struct CompUnit CompUnit;

struct CompUnit {
    ContextAddress low_pc;
    ContextAddress high_pc;
    U8_T debug_ranges_offs;
    U8_T debug_info_offs;
    U8_T line_info_offs;
    char * name;
    char * dir;

    U4_T files_cnt;
    U4_T files_max;
    FileInfo * files;

    U4_T dirs_cnt;
    U4_T dirs_max;
    char ** dirs;

    U4_T states_cnt;
    U4_T states_max;
    LineNumbersState * states;
};

typedef struct LineNumbersCache LineNumbersCache;

struct LineNumbersCache {
    ELF_File * file;
    CompUnit * units;
    U4_T units_max;
    U4_T units_cnt;
    ELF_Section * debug_ranges;
    ELF_Section * debug_line;
    LineNumbersCache * next;
};

static LineNumbersCache * read_cache;

static void read_tag_com_unit(U2_T attr, U2_T form) {
    static CompUnit * unit;
    switch (attr) {
    case 0:
        if (form) {
            if (read_cache->units_cnt >= read_cache->units_max) {
                read_cache->units_max = read_cache->units_max == 0 ? 16 : read_cache->units_max * 2;
                read_cache->units = (CompUnit *)loc_realloc(read_cache->units, sizeof(CompUnit) * read_cache->units_max);
            }
            unit = read_cache->units + read_cache->units_cnt++;
            memset(unit, 0, sizeof(CompUnit));
            unit->debug_ranges_offs = ~(U8_T)0;
        }
        else {
            /* Skip to next compilation unit */
            assert(dio_gUnitSize > 0);
            dio_Skip(dio_gUnitPos + dio_gUnitSize - dio_GetPos());
        }
        break;
    case AT_low_pc:
        dio_ChkAddr(form);
        unit->low_pc = (ContextAddress)dio_gFormRef;
        break;
    case AT_high_pc:
        dio_ChkAddr(form);
        unit->high_pc = (ContextAddress)dio_gFormRef;
        break;
    case AT_ranges:
        dio_ChkData(form);
        unit->debug_ranges_offs = dio_gFormData;
        break;
    case AT_name:
        dio_ChkString(form);
        unit->name = (char *)loc_alloc(dio_gFormBlockSize);
        strcpy(unit->name, (char *)dio_gFormBlockBuf);
        break;
    case AT_comp_dir:
        dio_ChkString(form);
        unit->dir = (char *)loc_alloc(dio_gFormBlockSize);
        strcpy(unit->dir, (char *)dio_gFormBlockBuf);
        break;
    case AT_stmt_list:
        dio_ChkData(form);
        unit->line_info_offs = dio_gFormData;
        break;
    }
}

static void entry_callback(U2_T Tag, U2_T attr, U2_T form) {
    switch (Tag) {
    case TAG_compile_unit           :
        read_tag_com_unit(attr, form);
        break;
    }
}
                
static void free_unit_cache(CompUnit * unit) {
    U4_T j;
    
    for (j = 0; j < unit->files_cnt; j++) {
        loc_free(unit->files[j].name);
    }
    unit->files_cnt = 0;
    unit->files_max = 0;
    loc_free(unit->files);

    for (j = 0; j < unit->dirs_cnt; j++) {
        loc_free(unit->dirs[j]);
    }
    unit->dirs_cnt = 0;
    unit->dirs_max = 0;
    loc_free(unit->dirs);

    unit->states_cnt = 0;
    unit->states_max = 0;
    loc_free(unit->states);
}

static void free_line_numbers_cache(ELF_File * file) {
    LineNumbersCache * cache = (LineNumbersCache *)file->line_numbers_cache;
    if (cache != NULL) {
        U4_T i;
        for (i = 0; i < cache->units_cnt; i++) {
            CompUnit * unit = cache->units + i;
            loc_free(unit->name);
            loc_free(unit->dir);
            free_unit_cache(unit);
        }
        loc_free(cache->units);
        loc_free(cache);
    }
}

static LineNumbersCache * get_line_numbers_cache(ELF_File * file) {
    if (file->line_numbers_cache == NULL) {
        Trap trap;
        if (set_trap(&trap)) {
            unsigned idx;
            LineNumbersCache * cache = NULL;
            dio_LoadAbbrevTable(file);
            cache = (LineNumbersCache *)(file->line_numbers_cache = loc_alloc_zero(sizeof(LineNumbersCache)));
            cache->file = file;
            read_cache = cache;
            for (idx = 0; idx < file->section_cnt; idx++) {
                ELF_Section * sec = file->sections[idx];
                if (sec == NULL) continue;
                if (sec->size == 0) continue;
                if (sec->name == NULL) continue;
                if (strcmp(sec->name, ".debug") == 0 || strcmp(sec->name, ".debug_info") == 0) {
                    dio_EnterSection(sec, 0);
                    dio_gVersion = strcmp(sec->name, ".debug") == 0 ? 1 : 2;
                    while (dio_GetPos() < sec->size) dio_ReadUnit(entry_callback);
                    dio_ExitSection();
                }
                else if (strcmp(sec->name, ".debug_ranges") == 0) {
                    cache->debug_ranges = sec;
                }
                else if (strcmp(sec->name, ".debug_line") == 0) {
                    cache->debug_line = sec;
                }
            }
            read_cache = NULL;
            clear_trap(&trap);
        }
        else {
            free_line_numbers_cache(file);
            str_exception(trap.error, trap.msg);
        }
    }
    return (LineNumbersCache *)file->line_numbers_cache;
}

static void add_dir(CompUnit * unit, char * name) {
    if (unit->dirs_cnt >= unit->dirs_max) {
        unit->dirs_max = unit->dirs_max == 0 ? 16 : unit->dirs_max * 2;
        unit->dirs = (char **)loc_realloc(unit->dirs, sizeof(char *) * unit->dirs_max);
    }
    unit->dirs[unit->dirs_cnt++] = name;
}

static void add_file(CompUnit * unit, FileInfo * file) {
    if (unit->files_cnt >= unit->files_max) {
        unit->files_max = unit->files_max == 0 ? 16 : unit->files_max * 2;
        unit->files = (FileInfo *)loc_realloc(unit->files, sizeof(FileInfo) * unit->files_max);
    }
    if (file->dir == NULL) file->dir = unit->dir;
    unit->files[unit->files_cnt++] = *file;
}

static void add_state(CompUnit * unit, LineNumbersState * state) {
    if (unit->states_cnt >= unit->states_max) {
        unit->states_max = unit->states_max == 0 ? 128 : unit->states_max * 2;
        unit->states = (LineNumbersState *)loc_realloc(unit->states, sizeof(LineNumbersState) * unit->states_max);
    }
    unit->states[unit->states_cnt++] = *state;
}

static void load_line_numbers(LineNumbersCache * cache, CompUnit * unit) {
    Trap trap;
    if (unit->files != NULL && unit->dirs != NULL) return;
    dio_gUnitPos = unit->line_info_offs;
    dio_EnterSection(cache->debug_line, dio_gUnitPos);
    if (set_trap(&trap)) {
        U8_T header_pos = 0;
        U1_T opcode_base = 0;
        U1_T opcode_size[256];
        U4_T header_size = 0;
        U1_T min_instruction_length = 0;
        U1_T is_stmt_default = 0;
        I1_T line_base = 0;
        U1_T line_range = 0;
        U4_T unit_size = 0;
        LineNumbersState state;
        
        /* Read header */
        unit_size = dio_ReadU4();
        if (unit_size == 0xffffffffu) {
            str_exception(ERR_INV_DWARF, "64-bit DWARF is not supported yet");
        }
        else {
            unit_size += 4;
        }
        dio_ReadU2(); /* line info version */
        header_size = dio_ReadU4();
        header_pos = dio_GetPos();
        min_instruction_length = dio_ReadU1();
        is_stmt_default = dio_ReadU1() != 0;
        line_base = (I1_T)dio_ReadU1();
        line_range = dio_ReadU1();
        opcode_base = dio_ReadU1();
        memset(opcode_size, 0, sizeof(opcode_size));
        dio_Read(opcode_size + 1, opcode_base - 1);

        /* Read directory names */
        for (;;) {
            char * name = dio_ReadString();
            if (name == NULL) break;
            add_dir(unit, name);
        }

        /* Read source files info */
        for (;;) {
            U4_T dir = 0;
            FileInfo file;
            memset(&file, 0, sizeof(file));
            file.name = dio_ReadString();
            if (file.name == NULL) break;
            dir = dio_ReadULEB128();
            if (dir > 0 && dir <= unit->dirs_cnt) file.dir = unit->dirs[dir - 1];
            file.mtime = dio_ReadULEB128();
            file.size = dio_ReadULEB128();
            add_file(unit, &file);
        }

        /* Run the program */
        if (header_pos + header_size != dio_GetPos())
            str_exception(ERR_INV_DWARF, "Invalid line info header");
        memset(&state, 0, sizeof(state));
        state.file = 1;
        state.line = 1;
        state.is_stmt = is_stmt_default;
        while (dio_GetPos() < dio_gUnitPos + unit_size) {
            U1_T opcode = dio_ReadU1();
            if (opcode >= opcode_base) {
                state.line += (unsigned)((int)((opcode - opcode_base) % line_range) + line_base);
                state.address += (opcode - opcode_base) / line_range * min_instruction_length;
                add_state(unit, &state);
                state.basic_block = 0;
                state.prologue_end = 0;
                state.epilogue_begin = 0;
            }
            else if (opcode == 0) {
                U4_T op_size = dio_ReadULEB128();
                U8_T op_pos = dio_GetPos();
                switch (dio_ReadU1()) {
                case DW_LNE_define_file: {
                    U4_T dir = 0;
                    FileInfo file;
                    memset(&file, 0, sizeof(file));
                    file.name = dio_ReadString();
                    dir = dio_ReadULEB128();
                    if (dir > 0 && dir <= unit->dirs_cnt) file.dir = unit->dirs[dir - 1];
                    file.mtime = dio_ReadULEB128();
                    file.size = dio_ReadULEB128();
                    add_file(unit, &file);
                    break;
                }
                case DW_LNE_end_sequence:
                    state.end_sequence = 1;
                    add_state(unit, &state);
                    memset(&state, 0, sizeof(state));
                    state.file = 1;
                    state.line = 1;
                    state.is_stmt = is_stmt_default;
                    break;
                case DW_LNE_set_address:
                    state.address = (ContextAddress)dio_ReadAddress();
                    break;
                default:
                    dio_Skip(op_size - 1);
                    break;
                }
                assert(dio_GetPos() == op_pos + op_size);
            }
            else {
                switch (opcode) {
                case DW_LNS_copy:
                    add_state(unit, &state);
                    state.basic_block = 0;
                    state.prologue_end = 0;
                    state.epilogue_begin = 0;
                    break;
                case DW_LNS_advance_pc:
                    state.address += (ContextAddress)(dio_ReadU8LEB128() * min_instruction_length);
                    break;
                case DW_LNS_advance_line:
                    state.line += dio_ReadSLEB128();
                    break;
                case DW_LNS_set_file:
                    state.file = dio_ReadULEB128();
                    break;
                case DW_LNS_set_column:
                    state.column = dio_ReadULEB128();
                    break;
                case DW_LNS_negate_stmt:
                    state.is_stmt = !state.is_stmt;
                    break;
                case DW_LNS_set_basic_block:
                    state.basic_block = 1;
                    break;
                case DW_LNS_const_add_pc:
                    state.address += (255 - opcode_base) / line_range * min_instruction_length;
                    break;
                case DW_LNS_fixed_advance_pc:
                    state.address += dio_ReadU2();
                    break;
                case DW_LNS_set_prologue_end:
                    state.prologue_end = 1;
                    break;
                case DW_LNS_set_epilogue_begin:
                    state.epilogue_begin = 1;
                    break;
                case DW_LNS_set_isa:
                    state.isa = (U1_T)dio_ReadULEB128();
                    break;
                default:
                    str_exception(ERR_INV_DWARF, "Invalid line info op code");
                    break;
                }
            }
        }
        dio_ExitSection();
        clear_trap(&trap);
    }
    else {
        dio_ExitSection();
        free_unit_cache(unit);
        str_exception(trap.error, trap.msg);
    }
}

static CompUnit * find_unit(LineNumbersCache * cache, ContextAddress addr0, ContextAddress addr1, ContextAddress * addr_next) {
    U4_T i;
    CompUnit * unit = NULL;
    ContextAddress low_pc = 0;
    /* TODO: faster unit search */
    for (i = 0; i < cache->units_cnt; i++) {
        CompUnit * u = cache->units + i;
        if (u->debug_ranges_offs != ~(U8_T)0) {
            if (cache->debug_ranges != NULL) {
                U8_T base = u->low_pc;
                U8_T max = 0;
                dio_gUnitPos = u->debug_ranges_offs;
                dio_EnterSection(cache->debug_ranges, dio_gUnitPos);
                while (1) {
                    U8_T x = dio_ReadAddress();
                    U8_T y = dio_ReadAddress();
                    if (x == 0 && y == 0) break;
                    if (x == ((U8_T)1 << dio_gAddressSize * 8) - 1) {
                        base = y;
                    }
                    else {
                        x = base + x;
                        y = base + y;
                        if (addr0 < y && addr1 > x) {
                            if (unit == NULL || low_pc > x) {
                                unit = u;
                                low_pc = (ContextAddress)x;
                                *addr_next = (ContextAddress)y;
                            }
                        }
                    }
                }
                dio_ExitSection();
            }
        }
        else if (u->low_pc != 0 && u->high_pc != 0) {
            if (addr0 < u->high_pc && addr1 > u->low_pc) {
                if (unit == NULL || low_pc > u->low_pc) {
                    unit = u;
                    low_pc = u->low_pc;
                    *addr_next = u->high_pc;
                }
            }
        }
    }
    return unit;
}

static void load_line_numbers_in_range(LineNumbersCache * cache, ContextAddress addr0, ContextAddress addr1) {
    while (addr0 < addr1) {
        ContextAddress next = 0;
        CompUnit * unit = find_unit(cache, addr0, addr1, &next);
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

static void write_line_info(OutputStream * out, CompUnit * unit,
                            ContextAddress addr0, ContextAddress addr1,
                            int * cnt, FileInfo ** file_info) {
    U4_T i;
    FileInfo * state_file = NULL;
    for (i = 0; i < unit->states_cnt - 1; i++) {
        LineNumbersState * state = unit->states + i;
        LineNumbersState * next = unit->states + i + 1;
        if (state->end_sequence) continue;
        if (next->address > addr0 && state->address < addr1) {
            if (*cnt > 0) write_stream(out, ',');
            write_stream(out, '{');
            json_write_string(out, "SLine");
            write_stream(out, ':');
            json_write_ulong(out, state->line);
            if (state->column > 0) {
                write_stream(out, ',');
                json_write_string(out, "SCol");
                write_stream(out, ':');
                json_write_ulong(out, state->column);
            }
            write_stream(out, ',');
            json_write_string(out, "ELine");
            write_stream(out, ':');
            json_write_ulong(out, next->line);
            if (next->column > 0) {
                write_stream(out, ',');
                json_write_string(out, "ECol");
                write_stream(out, ':');
                json_write_ulong(out, next->column);
            }
            state_file = NULL;
            if (state->file >= 1 && state->file <= unit->files_cnt) {
                state_file = unit->files + (state->file - 1);
            }
            if (*file_info != state_file) {
                *file_info = state_file;
                write_stream(out, ',');
                json_write_string(out, "File");
                write_stream(out, ':');
                json_write_string(out, *file_info == NULL ? NULL : (*file_info)->name);
                write_stream(out, ',');
                json_write_string(out, "Dir");
                write_stream(out, ':');
                json_write_string(out, *file_info == NULL ? NULL : (*file_info)->dir);
            }
            write_stream(out, ',');
            json_write_string(out, "SAddr");
            write_stream(out, ':');
            json_write_ulong(out, state->address);
            write_stream(out, ',');
            json_write_string(out, "EAddr");
            write_stream(out, ':');
            json_write_ulong(out, next->address);
            if (state->isa != 0) {
                write_stream(out, ',');
                json_write_string(out, "ISA");
                write_stream(out, ':');
                json_write_ulong(out, state->isa);
            }
            if (state->is_stmt) {
                write_stream(out, ',');
                json_write_string(out, "IsStmt");
                write_stream(out, ':');
                json_write_boolean(out, state->is_stmt);
            }
            if (state->basic_block) {
                write_stream(out, ',');
                json_write_string(out, "BasicBlock");
                write_stream(out, ':');
                json_write_boolean(out, state->basic_block);
            }
            if (state->prologue_end) {
                write_stream(out, ',');
                json_write_string(out, "PrologueEnd");
                write_stream(out, ':');
                json_write_boolean(out, state->prologue_end);
            }
            if (state->epilogue_begin) {
                write_stream(out, ',');
                json_write_string(out, "EpilogueBegin");
                write_stream(out, ':');
                json_write_boolean(out, state->epilogue_begin);
            }
            write_stream(out, '}');
            (*cnt)++;
        }
    }
}

int line_to_address(Context * ctx, char * file_name, int line, int column, LineToAddressCallBack * callback, void * user_args) {
    int err = 0;
    ELF_File * file = NULL;

    if (ctx == NULL) err = ERR_INV_CONTEXT;
    else if (ctx->exited) err = ERR_ALREADY_EXITED;

    file = elf_list_first(ctx, 0, ~(ContextAddress)0);
    if (file == NULL) err = errno;
    while (file != NULL) {
        Trap trap;
        if (set_trap(&trap)) {
            int i;
            LineNumbersCache * cache = get_line_numbers_cache(file);
            for (i = 0; i < cache->units_cnt; i++) {
                CompUnit * unit = cache->units + i;
                int equ = 0;
                if (unit->dir != NULL && unit->name != NULL) {
                    equ = cmp_file(file_name, unit->dir, unit->name);
                }
                if (!equ) {
                    int j;
                    for (j = 0; j < unit->files_cnt; j++) {
                        FileInfo * f = unit->files + j;
                        if (f->dir != NULL && f->name != NULL) {
                            equ = cmp_file(file_name, f->dir, f->name);
                            if (equ) break;
                        }
                    }
                }
                if (equ) {
                    int j;
                    load_line_numbers(cache, unit);
                    for (j = 0; j < unit->states_cnt - 1; j++) {
                        LineNumbersState * state = unit->states + j;
                        LineNumbersState * next = unit->states + j + 1;
                        char * state_dir = unit->dir;
                        char * state_name = unit->name;
                        if (state->end_sequence) continue;
                        if (line < state->line) continue;
                        if (line >= next->line) continue;
                        if (state->file >= 1 && state->file <= unit->files_cnt) {
                            FileInfo * f = unit->files + (state->file - 1);
                            state_dir = f->dir;
                            state_name = f->name;
                        }
                        if (!cmp_file(file_name, state_dir, state_name)) continue;
                        callback(user_args, state->address);
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

    if (err != 0) {
        errno = err;
        return -1;
    }
    return 0;
}

static void command_map_to_source(char * token, Channel * c) {
    int err = 0;
    char * err_msg = NULL;
    char id[256];
    ContextAddress addr0;
    ContextAddress addr1;
    Context * ctx = NULL;
    LineNumbersCache * cache_first = NULL;
    LineNumbersCache * cache_last = NULL;

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
                LineNumbersCache * cache = get_line_numbers_cache(file);
                load_line_numbers_in_range(cache, addr0, addr1);
                clear_trap(&trap);
                if (cache_last == NULL) {
                    cache_first = cache;
                }
                else {
                    cache_last->next = cache;
                }
                cache_last = cache;
                cache->file->ref_cnt++;
            }
            else {
                err = trap.error;
                err_msg = trap.msg;
                break;
            }
            file = elf_list_next(ctx);
            if (file == NULL) err = errno;
        }
        elf_list_done(ctx);
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_err_msg(&c->out, err, err_msg);
    if (err != 0) {
        write_stringz(&c->out, "null");
    }
    else {
        int cnt = 0;
        FileInfo * file_info = NULL;
        LineNumbersCache * cache = cache_first;
        write_stream(&c->out, '[');
        while (cache != NULL) {
            Trap trap;
            if (set_trap(&trap)) {
                while (err == 0 && addr0 < addr1) {
                    ContextAddress next = 0;
                    CompUnit * unit = find_unit(cache, addr0, addr1, &next);
                    if (unit == NULL) break;
                    write_line_info(&c->out, unit, addr0, addr1, &cnt, &file_info);
                    addr0 = next;
                }
                clear_trap(&trap);
            }
            else {
                err = trap.error;
                err_msg = trap.msg;
            }
            cache->file->ref_cnt--;
            if (cache == cache_last) break;
            cache = cache->next;
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }
    write_stream(&c->out, MARKER_EOM);
}

void ini_line_numbers_service(Protocol * proto) {
    elf_add_close_listener(free_line_numbers_cache);
    add_command_handler(proto, LINENUMBERS, "mapToSource", command_map_to_source);
}

#endif

