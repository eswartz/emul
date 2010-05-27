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
 * This module implements handling of .debug_frame and .eh_frame sections.
 *
 * Functions in this module use exceptions to report errors, see exceptions.h
 */

#include <config.h>

#if ENABLE_ELF

#include <assert.h>
#include <stdio.h>
#include <framework/exceptions.h>
#include <framework/myalloc.h>
#include <services/dwarfio.h>
#include <services/dwarfframe.h>

#define EH_PE_omit              0xff

#define EH_PE_absptr            0x00
#define EH_PE_uleb128           0x01
#define EH_PE_udata2            0x02
#define EH_PE_udata4            0x03
#define EH_PE_udata8            0x04
#define EH_PE_sleb128           0x09
#define EH_PE_sdata2            0x0a
#define EH_PE_sdata4            0x0b
#define EH_PE_sdata8            0x0c

#define EH_PB_pcrel             0x01
#define EH_PB_textrel           0x02
#define EH_PB_datarel           0x03
#define EH_PB_funcrel           0x04
#define EH_PB_aligned           0x05

#define EH_PE_indirect          0x80

#define RULE_OFFSET             1
#define RULE_SAME_VALUE         2
#define RULE_REGISTER           3
#define RULE_EXPRESSION         4
#define RULE_VAL_OFFSET         5
#define RULE_VAL_EXPRESSION     6

typedef struct RegisterRules {
    int rule;
    I4_T offset;
    U1_T * expression;
} RegisterRules;

typedef struct StackFrameRegisters {
    RegisterRules * regs;
    int regs_cnt;
    int regs_max;
} StackFrameRegisters;

typedef struct StackFrameRules {
    Context * ctx;
    ELF_Section * section;
    int eh_frame;
    U1_T version;
    U4_T code_alignment;
    I4_T data_alignment;
    U8_T cie_pos;
    char * cie_aug;
    U8_T cie_eh_data;
    ELF_Section * cie_eh_data_section;
    U4_T fde_aug_length;
    U1_T * fde_aug_data;
    U1_T lsda_encoding;
    U1_T prh_encoding;
    U1_T addr_encoding;
    U8_T location;
    int return_address_register;
    int cfa_rule;
    I4_T cfa_offset;
    U4_T cfa_register;
    U1_T * cfa_expression;
} StackFrameRules;

static StackFrameRegisters frame_regs;
static StackFrameRegisters cie_regs;
static StackFrameRegisters * regs_stack = NULL;
static int regs_stack_max = 0;
static int regs_stack_pos = 0;

static StackFrameRules rules;

U8_T dwarf_stack_trace_addr = 0;
U8_T dwarf_stack_trace_size = 0;

StackTracingCommandSequence * dwarf_stack_trace_fp = NULL;

int dwarf_stack_trace_regs_cnt = 0;
StackTracingCommandSequence ** dwarf_stack_trace_regs = NULL;

static int trace_regs_max = 0;
static int trace_cmds_max = 0;
static int trace_cmds_cnt = 0;
static StackTracingCommand * trace_cmds = NULL;

static RegisterRules * get_reg(StackFrameRegisters * regs, int reg) {
    RegisterDefinition * reg_def;
    while (reg >= regs->regs_max) {
        regs->regs_max = regs->regs_max == 0 ? 32 : regs->regs_max * 2;
        regs->regs = (RegisterRules *)loc_realloc(regs->regs, sizeof(RegisterRules) * regs->regs_max);
    }
    while (regs->regs_cnt <= reg) {
        int n = regs->regs_cnt++;
        memset(regs->regs + n, 0, sizeof(RegisterRules));
        reg_def = get_reg_by_id(rules.ctx, n, rules.eh_frame ? REGNUM_EH_FRAME : REGNUM_DWARF);
        if (reg_def != NULL && reg_def->traceable) {
            /* It looks like GCC assumes that an unspecified register implies "same value" */
            regs->regs[n].rule = RULE_SAME_VALUE;
        }
    }
    return regs->regs + reg;
}

static void copy_register_rules(StackFrameRegisters * dst, StackFrameRegisters * src) {
    int n;
    dst->regs_cnt = 0;
    for (n = 0; n < src->regs_cnt; n++) {
        *get_reg(dst, n) = *get_reg(src, n);
    }
}

static StackFrameRegisters * get_regs_stack_item(int n) {
    while (n >= regs_stack_max) {
        int max = regs_stack_max;
        regs_stack_max = regs_stack_max == 0 ? 8 : regs_stack_max * 2;
        regs_stack = (StackFrameRegisters *)loc_realloc(regs_stack, sizeof(StackFrameRegisters) * regs_stack_max);
        memset(regs_stack + max, 0, sizeof(StackFrameRegisters) * (regs_stack_max - max));
    }
    return regs_stack + n;
}

static U8_T read_frame_data_pointer(U1_T encoding, ELF_Section ** sec) {
    U8_T v = 0;
    if (encoding != EH_PE_omit) {
        U8_T pos = dio_GetPos();
        switch (encoding & 0xf) {
        case EH_PE_absptr:
            v = dio_ReadAddress(sec);
            break;
        case EH_PE_uleb128:
            v = dio_ReadU8LEB128();
            break;
        case EH_PE_udata2:
            v = dio_ReadU2();
            break;
        case EH_PE_udata4:
            v = dio_ReadU4();
            break;
        case EH_PE_udata8:
            v = dio_ReadU8();
            break;
        case EH_PE_sleb128:
            v = dio_ReadS8LEB128();
            break;
        case EH_PE_sdata2:
            v = (I2_T)dio_ReadU2();
            break;
        case EH_PE_sdata4:
            v = (I4_T)dio_ReadU4();
            break;
        case EH_PE_sdata8:
            v = (I8_T)dio_ReadU8();
            break;
        default:
            str_exception(ERR_INV_DWARF, "Unknown encoding of .eh_frame section pointers");
            break;
        }
        if (v != 0 && sec != NULL) {
            switch ((encoding >> 4) & 0x7) {
            case 0:
                break;
            case EH_PB_pcrel:
                *sec = rules.section;
                v += rules.section->addr + pos;
                break;
            case EH_PB_datarel:
                *sec = rules.section;
                v += rules.section->addr;
                break;
            case EH_PB_textrel:
            case EH_PB_funcrel:
            case EH_PB_aligned:
            default:
                str_exception(ERR_INV_DWARF, "Unknown encoding of .eh_frame section pointers");
                break;
            }
            if (encoding & EH_PE_indirect) {
                unsigned idx;
                ELF_File * file = rules.section->file;
                size_t size = file->elf64 ? 8 : 4;
                U8_T res = 0;
                for (idx = 1; idx < file->section_cnt; idx++) {
                    ELF_Section * sec = file->sections + idx;
                    if ((sec->flags & SHF_ALLOC) == 0) continue;
                    if (sec->addr <= v && sec->addr + sec->size >= v + size) {
                        U1_T * p;
                        size_t i;
                        if (sec->data == NULL && elf_load(sec) < 0) exception(errno);
                        p = (U1_T *)sec->data + (uintptr_t)(v - sec->addr);
                        for (i = 0; i < size; i++) {
                            res = (res << 8) | p[file->big_endian ? i : size - i - 1];
                        }
                        break;
                    }
                }
                v = res;
            }
        }
    }
    return v;
}

static void exec_stack_frame_instruction(void) {
    RegisterRules * reg;
    U4_T n;
    U1_T op = dio_ReadU1();
    switch (op) {
    case 0x00: /* DW_CFA_nop */
        break;
    case 0x01: /* DW_CFA_set_loc */
        rules.location = read_frame_data_pointer(rules.addr_encoding, 0);
        break;
    case 0x02: /* DW_CFA_advance_loc1 */
        rules.location += dio_ReadU1() * rules.code_alignment;
        break;
    case 0x03: /* DW_CFA_advance_loc2 */
        rules.location += dio_ReadU2() * rules.code_alignment;
        break;
    case 0x04: /* DW_CFA_advance_loc4 */
        rules.location += dio_ReadU4() * rules.code_alignment;
        break;
    case 0x05: /* DW_CFA_offset_extended */
        reg = get_reg(&frame_regs, dio_ReadULEB128());
        reg->rule = RULE_OFFSET;
        reg->offset = dio_ReadULEB128() * rules.data_alignment;
        break;
    case 0x06: /* DW_CFA_restore_extended */
        n = dio_ReadULEB128();
        reg = get_reg(&frame_regs, n);
        *reg = *get_reg(&cie_regs, n);
        break;
    case 0x07: /* DW_CFA_undefined */
        reg = get_reg(&frame_regs, dio_ReadULEB128());
        memset(reg, 0, sizeof(*reg));
        break;
    case 0x08: /* DW_CFA_same_value */
        reg = get_reg(&frame_regs, dio_ReadULEB128());
        reg->rule = RULE_SAME_VALUE;
        break;
    case 0x09: /* DW_CFA_register */
        reg = get_reg(&frame_regs, dio_ReadULEB128());
        reg->rule = RULE_REGISTER;
        reg->offset = dio_ReadULEB128();
        break;
    case 0x0a: /* DW_CFA_remember_state */
        copy_register_rules(get_regs_stack_item(regs_stack_pos++), &frame_regs);
        break;
    case 0x0b: /* DW_CFA_restore_state */
        if (regs_stack_pos <= 0) {
            str_exception(ERR_INV_DWARF, "Invalid DW_CFA_restore_state instruction");
        }
        copy_register_rules(&frame_regs, get_regs_stack_item(--regs_stack_pos));
        break;
    case 0x0c: /* DW_CFA_def_cfa */
        rules.cfa_rule = RULE_OFFSET;
        rules.cfa_register = dio_ReadULEB128();
        rules.cfa_offset = dio_ReadULEB128();
        break;
    case 0x0d: /* DW_CFA_def_cfa_register */
        rules.cfa_rule = RULE_OFFSET;
        rules.cfa_register = dio_ReadULEB128();
        break;
    case 0x0e: /* DW_CFA_def_cfa_offset */
        rules.cfa_rule = RULE_OFFSET;
        rules.cfa_offset = dio_ReadULEB128();
        break;
    case 0x0f: /* DW_CFA_def_cfa_expression */
        rules.cfa_rule = RULE_EXPRESSION;
        rules.cfa_offset = dio_ReadULEB128();
        rules.cfa_expression = dio_GetDataPtr();
        dio_Skip(rules.cfa_offset);
        break;
    case 0x10: /* DW_CFA_expression */
        reg = get_reg(&frame_regs, dio_ReadULEB128());
        reg->rule = RULE_EXPRESSION;
        reg->offset = dio_ReadULEB128();
        reg->expression = dio_GetDataPtr();
        dio_Skip(reg->offset);
        break;
    case 0x11: /* DW_CFA_offset_extended_sf */
        reg = get_reg(&frame_regs, dio_ReadULEB128());
        reg->rule = RULE_OFFSET;
        reg->offset = dio_ReadSLEB128() * rules.data_alignment;
        break;
    case 0x12: /* DW_CFA_def_cfa_sf */
        rules.cfa_rule = RULE_OFFSET;
        rules.cfa_register = dio_ReadULEB128();
        rules.cfa_offset = dio_ReadSLEB128() * rules.data_alignment;
        break;
    case 0x13: /* DW_CFA_def_cfa_offset_sf */
        rules.cfa_rule = RULE_OFFSET;
        rules.cfa_offset = dio_ReadSLEB128() * rules.data_alignment;
        break;
    case 0x14: /* DW_CFA_val_offset */
        reg = get_reg(&frame_regs, dio_ReadULEB128());
        reg->rule = RULE_VAL_OFFSET;
        reg->offset = dio_ReadULEB128() * rules.data_alignment;
        break;
    case 0x15: /* DW_CFA_val_offset_sf */
        reg = get_reg(&frame_regs, dio_ReadULEB128());
        reg->rule = RULE_VAL_OFFSET;
        reg->offset = dio_ReadSLEB128() * rules.data_alignment;
        break;
    case 0x16: /* DW_CFA_val_expression */
        reg = get_reg(&frame_regs, dio_ReadULEB128());
        reg->rule = RULE_VAL_EXPRESSION;
        reg->offset = dio_ReadULEB128();
        reg->expression = dio_GetDataPtr();
        dio_Skip(reg->offset);
        break;
    default:
        switch (op >> 6) {
        case 0:
            str_exception(ERR_INV_DWARF, "Unsupported instruction in Call Frame Information");
            break;
        case 1: /* DW_CFA_advance_loc */
            rules.location += (op & 0x3f) * rules.code_alignment;
            break;
        case 2: /* DW_CFA_offset */
            reg = get_reg(&frame_regs, op & 0x3f);
            reg->rule = RULE_OFFSET;
            reg->offset = dio_ReadULEB128() * rules.data_alignment;
            break;
        case 3: /* DW_CFA_restore */
            n = op & 0x3f;
            reg = get_reg(&frame_regs, n);
            *reg = *get_reg(&cie_regs, n);
            break;
        }
    }
}

static StackTracingCommand * add_command(int op) {
    StackTracingCommand * cmd = NULL;
    if (trace_cmds_cnt >= trace_cmds_max) {
        trace_cmds_max += 16;
        trace_cmds = (StackTracingCommand *)loc_realloc(trace_cmds, trace_cmds_max * sizeof(StackTracingCommand));
    }
    cmd = trace_cmds + trace_cmds_cnt++;
    memset(cmd, 0, sizeof(*cmd));
    cmd->cmd = op;
    return cmd;
}

static void add_command_sequence(StackTracingCommandSequence ** ptr, RegisterDefinition * reg) {
    StackTracingCommandSequence * seq = *ptr;
    if (seq == NULL || seq->cmds_max < trace_cmds_cnt) {
        *ptr = seq = (StackTracingCommandSequence *)loc_realloc(seq, sizeof(StackTracingCommandSequence) + (trace_cmds_cnt - 1) * sizeof(StackTracingCommand));
        seq->cmds_max = trace_cmds_cnt;
    }
    seq->reg = reg;
    seq->cmds_cnt = trace_cmds_cnt;
    memcpy(seq->cmds, trace_cmds, trace_cmds_cnt * sizeof(StackTracingCommand));
}

static void generate_register_commands(RegisterRules * reg, RegisterDefinition * reg_def) {
    if (reg_def == NULL) return;
    trace_cmds_cnt = 0;
    switch (reg->rule) {
    case RULE_VAL_OFFSET:
    case RULE_OFFSET:
        add_command(SFT_CMD_FP);
        if (reg->offset != 0) {
            add_command(SFT_CMD_NUMBER)->num = reg->offset;
            add_command(SFT_CMD_ADD);
        }
        if (reg->rule == RULE_OFFSET) {
            StackTracingCommand * cmd = add_command(SFT_CMD_DEREF);
            cmd->size = reg_def->size;
            cmd->big_endian = rules.section->file->big_endian;
        }
        break;
    case RULE_SAME_VALUE:
        add_command(SFT_CMD_REGISTER)->reg = reg_def;
        break;
    case RULE_REGISTER:
        {
            RegisterDefinition * src_sef = get_reg_by_id(rules.ctx, reg->offset, rules.eh_frame ? REGNUM_EH_FRAME : REGNUM_DWARF);
            if (src_sef != NULL) add_command(SFT_CMD_REGISTER)->reg = src_sef;
        }
        break;
    default:
        /* TODO: RULE_EXPRESSION, RULE_VAL_EXPRESSION */
        str_exception(ERR_UNSUPPORTED, "Not implemented yet: expression in .debug_frame");
        break;
    }
    if (dwarf_stack_trace_regs_cnt >= trace_regs_max) {
        int i;
        trace_regs_max += 16;
        dwarf_stack_trace_regs = (StackTracingCommandSequence **)loc_realloc(dwarf_stack_trace_regs, trace_regs_max * sizeof(StackTracingCommandSequence *));
        for (i = dwarf_stack_trace_regs_cnt; i < trace_regs_max; i++) dwarf_stack_trace_regs[i] = NULL;
    }
    if (trace_cmds_cnt == 0) return;
    add_command_sequence(dwarf_stack_trace_regs + dwarf_stack_trace_regs_cnt++, reg_def);
}

static void generate_commands(void) {
    int i;
    RegisterRules * reg;
    RegisterDefinition * reg_def;

    reg = get_reg(&frame_regs, rules.return_address_register);
    if (reg->rule != 0) generate_register_commands(reg, get_PC_definition(rules.ctx));
    for (i = 0; i < frame_regs.regs_cnt; i++) {
        if (i == rules.return_address_register) continue;
        reg = get_reg(&frame_regs, i);
        if (reg->rule == 0) continue;
        reg_def = get_reg_by_id(rules.ctx, i, rules.eh_frame ? REGNUM_EH_FRAME : REGNUM_DWARF);
        generate_register_commands(reg, reg_def);
    }

    trace_cmds_cnt = 0;
    switch (rules.cfa_rule) {
    case RULE_OFFSET:
        reg_def = get_reg_by_id(rules.ctx, rules.cfa_register, rules.eh_frame ? REGNUM_EH_FRAME : REGNUM_DWARF);
        if (reg_def != NULL) {
            add_command(SFT_CMD_REGISTER)->reg = reg_def;
            if (rules.cfa_offset != 0) {
                add_command(SFT_CMD_NUMBER)->num = rules.cfa_offset;
                add_command(SFT_CMD_ADD);
            }
        }
        break;
    default:
        /* TODO: RULE_EXPRESSION */
        str_exception(ERR_UNSUPPORTED, "Not implemented yet: expression in .debug_frame");
    }
    add_command_sequence(&dwarf_stack_trace_fp, NULL);
}

static void read_frame_cie(U8_T pos) {
    int cie_dwarf64 = 0;
    U8_T saved_pos = dio_GetPos();
    U8_T cie_length = 0;
    U8_T cie_end = 0;

    rules.cie_pos = pos;
    dio_Skip(pos - dio_GetPos());
    cie_length = dio_ReadU4();
    if (cie_length == 0xffffffffu) {
        cie_length = dio_ReadU8();
        cie_dwarf64 = 1;
    }
    cie_end = dio_GetPos() + cie_length;
    dio_Skip(cie_dwarf64 ? 8 : 4);
    rules.version = dio_ReadU1();
    if (rules.version != 1 && rules.version != 3) {
        str_exception(ERR_INV_DWARF, "Unsupported version of Call Frame Information");
    }
    rules.cie_aug = dio_ReadString();
    if (rules.cie_aug != NULL && strcmp(rules.cie_aug, "eh") == 0) {
        rules.cie_eh_data = dio_ReadAddress(&rules.cie_eh_data_section);
    }
    rules.code_alignment = dio_ReadULEB128();
    rules.data_alignment = dio_ReadSLEB128();
    rules.return_address_register = dio_ReadULEB128();
    rules.lsda_encoding = 0;
    rules.prh_encoding = 0;
    rules.addr_encoding = 0;
    if (rules.cie_aug != NULL && rules.cie_aug[0] == 'z') {
        U4_T aug_length = dio_ReadULEB128();
        U8_T aug_pos = dio_GetPos();
        char * p = rules.cie_aug + 1;
        while (*p) {
            switch (*p++) {
            case 'L':
                rules.lsda_encoding = dio_ReadU1();
                break;
            case 'P':
                rules.prh_encoding = dio_ReadU1();
                read_frame_data_pointer(rules.prh_encoding, 0);
                break;
            case 'R':
                rules.addr_encoding = dio_ReadU1();
                break;
            }
        }
        dio_Skip(aug_pos + aug_length - dio_GetPos());
    }
    cie_regs.regs_cnt = 0;
    frame_regs.regs_cnt = 0;
    regs_stack_pos = 0;
    while (dio_GetPos() < cie_end) {
        exec_stack_frame_instruction();
    }
    copy_register_rules(&cie_regs, &frame_regs);
    dio_Skip(saved_pos - dio_GetPos());
}

void get_dwarf_stack_frame_info(Context * ctx, ELF_File * file, U8_T IP) {
    /* TODO: use .eh_frame_hdr section for faster frame data search */
    DWARFCache * cache = get_dwarf_cache(file);
    ELF_Section * section = cache->mDebugFrame;

    dwarf_stack_trace_regs_cnt = 0;
    if (dwarf_stack_trace_fp == NULL) {
        dwarf_stack_trace_fp = (StackTracingCommandSequence *)loc_alloc_zero(sizeof(StackTracingCommandSequence));
        dwarf_stack_trace_fp->cmds_max = 1;
    }
    dwarf_stack_trace_fp->cmds_cnt = 0;
    dwarf_stack_trace_addr = 0;
    dwarf_stack_trace_size = 0;

    if (section == NULL) section = cache->mEHFrame;
    if (section == NULL) return;

    memset(&rules, 0, sizeof(StackFrameRules));
    rules.ctx = ctx;
    rules.section = section;
    rules.eh_frame = section == cache->mEHFrame;
    rules.cie_pos = ~(U8_T)0;
    dio_EnterSection(NULL, section, 0);
    while (dio_GetPos() < section->size) {
        int fde_dwarf64 = 0;
        U8_T fde_length = 0;
        U8_T fde_pos = 0;
        U8_T fde_end = 0;
        U8_T cie_ref = 0;
        int fde_flag = 0;

        fde_length = dio_ReadU4();
        if (fde_length == 0xffffffffu) {
            fde_length = dio_ReadU8();
            fde_dwarf64 = 1;
        }
        if (fde_length == 0) break;
        fde_pos = dio_GetPos();
        fde_end = fde_pos + fde_length;
        cie_ref = fde_dwarf64 ? dio_ReadU8() : dio_ReadU4();
        if (rules.eh_frame) fde_flag = cie_ref != 0;
        else if (fde_dwarf64) fde_flag = cie_ref != ~(U8_T)0;
        else fde_flag = cie_ref != ~(U4_T)0;
        if (fde_flag) {
            U8_T Addr, Range, AddrRT;
            ELF_Section * sec = NULL;
            if (rules.eh_frame) cie_ref = fde_pos - cie_ref;
            if (cie_ref != rules.cie_pos) read_frame_cie(cie_ref);
            Addr = read_frame_data_pointer(rules.addr_encoding, &sec);
            Range = read_frame_data_pointer(rules.addr_encoding, NULL);
            AddrRT = elf_map_to_run_time_address(ctx, file, sec, (ContextAddress)Addr);
            if (AddrRT != 0 && AddrRT <= IP && AddrRT + Range > IP) {
                U8_T location0 = Addr;
                if (rules.cie_aug != NULL && rules.cie_aug[0] == 'z') {
                    rules.fde_aug_length = dio_ReadULEB128();
                    rules.fde_aug_data = dio_GetDataPtr();
                    dio_Skip(rules.fde_aug_length);
                }
                copy_register_rules(&frame_regs, &cie_regs);
                rules.location = Addr;
                regs_stack_pos = 0;
                for (;;) {
                    if (dio_GetPos() >= fde_end) {
                        rules.location = Addr + Range;
                        break;
                    }
                    exec_stack_frame_instruction();
                    assert(location0 - Addr + AddrRT <= IP);
                    if (rules.location - Addr + AddrRT > IP) break;
                    location0 = rules.location;
                }
                dwarf_stack_trace_addr = location0 - Addr + AddrRT;
                dwarf_stack_trace_size = rules.location - location0;
                generate_commands();
                break;
            }
        }
        dio_Skip(fde_end - dio_GetPos());
    }
    dio_ExitSection();
}

#endif /* ENABLE_ELF */
