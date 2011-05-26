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
#include <framework/trace.h>
#include <services/dwarf.h>
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
    U8_T expression;
} RegisterRules;

typedef struct StackFrameRegisters {
    RegisterRules * regs;
    int regs_cnt;
    int regs_max;
} StackFrameRegisters;

typedef struct StackFrameRules {
    Context * ctx;
    ELF_Section * section;
    RegisterIdScope reg_id_scope;
    int eh_frame;
    U1_T version;
    U1_T address_size;
    U1_T segment_size;
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
    U8_T cfa_expression;
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
    while (reg >= regs->regs_max) {
        regs->regs_max = regs->regs_max == 0 ? 32 : regs->regs_max * 2;
        regs->regs = (RegisterRules *)loc_realloc(regs->regs, sizeof(RegisterRules) * regs->regs_max);
    }
    while (regs->regs_cnt <= reg) {
        int n = regs->regs_cnt++;
        memset(regs->regs + n, 0, sizeof(RegisterRules));
        /* Architecture specific implied rules */
        switch (rules.reg_id_scope.machine) {
        case EM_386:
            switch (n) {
            case 4: /* SP */
                regs->regs[n].rule = RULE_VAL_OFFSET;
                break;
            case 3: /* BX */
            case 5: /* BP */
            case 6: /* SI */
            case 7: /* DI */
                regs->regs[n].rule = RULE_SAME_VALUE;
                break;
            }
            break;
        case EM_X86_64:
            switch (n) {
            case 3: /* BX */
            case 6: /* BP */
            case 12: /* R12 */
            case 13: /* R13 */
            case 14: /* R14 */
            case 15: /* R15 */
                regs->regs[n].rule = RULE_SAME_VALUE;
                break;
            case 7: /* SP */
                regs->regs[n].rule = RULE_VAL_OFFSET;
                break;
            }
            break;
        case EM_PPC:
            if (n == 1) {
                regs->regs[n].rule = RULE_VAL_OFFSET;
            }
            else if ((n >= 14 && n <= 31) || (n >= 46 && n <= 63)) {
                regs->regs[n].rule = RULE_SAME_VALUE;
            }
            else if (n == rules.return_address_register) {
                regs->regs[n].rule = RULE_REGISTER;
                regs->regs[n].offset = 108;
            }
            break;
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
                size_t size = rules.address_size;
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
        rules.cfa_expression = dio_GetPos();
        dio_Skip(rules.cfa_offset);
        break;
    case 0x10: /* DW_CFA_expression */
        reg = get_reg(&frame_regs, dio_ReadULEB128());
        reg->rule = RULE_EXPRESSION;
        reg->offset = dio_ReadULEB128();
        reg->expression = dio_GetPos();
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
        reg->expression = dio_GetPos();
        dio_Skip(reg->offset);
        break;
    case 0x2e: /* DW_CFA_GNU_args_size */
        /* This instruction specifies the total size of the arguments
         * which have been pushed onto the stack. Not used by the debugger. */
        dio_ReadULEB128();
        break;
    case 0x2f: /* DW_CFA_GNU_negative_offset_extended */
        /* This instruction is identical to DW_CFA_offset_extended_sf
         * except that the operand is subtracted to produce the offset. */
        reg = get_reg(&frame_regs, dio_ReadULEB128());
        reg->rule = RULE_OFFSET;
        reg->offset = -dio_ReadSLEB128() * rules.data_alignment;
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

static void add_dwarf_expression_commands(U8_T cmds_offs, U4_T cmds_size) {
    dio_EnterSection(NULL, rules.section, cmds_offs);
    while (dio_GetPos() < cmds_offs + cmds_size) {
        U1_T op = dio_ReadU1();

        switch (op) {
        case OP_addr:
            {
                ELF_Section * section = NULL;
                U8_T lt_addr = dio_ReadAddress(&section);
                ContextAddress rt_addr = elf_map_to_run_time_address(
                    rules.ctx, rules.section->file, section, (ContextAddress)lt_addr);
                if (rt_addr == 0) str_exception(ERR_INV_DWARF, "object has no RT address");
                add_command(SFT_CMD_NUMBER)->num = rt_addr;
            }
            break;
        case OP_deref:
            {
                StackTracingCommand * cmd = add_command(SFT_CMD_DEREF);
                cmd->size = rules.address_size;
                cmd->big_endian = rules.section->file->big_endian;
            }
            break;
        case OP_deref_size:
            {
                StackTracingCommand * cmd = add_command(SFT_CMD_DEREF);
                cmd->size = dio_ReadU1();
                cmd->big_endian = rules.section->file->big_endian;
            }
            break;
        case OP_const1u:
            add_command(SFT_CMD_NUMBER)->num = dio_ReadU1();
            break;
        case OP_const1s:
            add_command(SFT_CMD_NUMBER)->num = (I1_T)dio_ReadU1();
            break;
        case OP_const2u:
            add_command(SFT_CMD_NUMBER)->num = dio_ReadU2();
            break;
        case OP_const2s:
            add_command(SFT_CMD_NUMBER)->num = (I2_T)dio_ReadU2();
            break;
        case OP_const4u:
            add_command(SFT_CMD_NUMBER)->num = dio_ReadU4();
            break;
        case OP_const4s:
            add_command(SFT_CMD_NUMBER)->num = (I4_T)dio_ReadU4();
            break;
        case OP_const8u:
            add_command(SFT_CMD_NUMBER)->num = dio_ReadU8();
            break;
        case OP_const8s:
            add_command(SFT_CMD_NUMBER)->num = (I8_T)dio_ReadU8();
            break;
        case OP_constu:
            add_command(SFT_CMD_NUMBER)->num = dio_ReadU8LEB128();
            break;
        case OP_consts:
            add_command(SFT_CMD_NUMBER)->num = dio_ReadS8LEB128();
            break;
        case OP_and:
            add_command(SFT_CMD_AND);
            break;
        case OP_minus:
            add_command(SFT_CMD_SUB);
            break;
        case OP_or:
            add_command(SFT_CMD_OR);
            break;
        case OP_plus:
            add_command(SFT_CMD_ADD);
            break;
        case OP_plus_uconst:
            add_command(SFT_CMD_NUMBER)->num = dio_ReadU8LEB128();
            add_command(SFT_CMD_ADD);
            break;
        case OP_lit0:
        case OP_lit1:
        case OP_lit2:
        case OP_lit3:
        case OP_lit4:
        case OP_lit5:
        case OP_lit6:
        case OP_lit7:
        case OP_lit8:
        case OP_lit9:
        case OP_lit10:
        case OP_lit11:
        case OP_lit12:
        case OP_lit13:
        case OP_lit14:
        case OP_lit15:
        case OP_lit16:
        case OP_lit17:
        case OP_lit18:
        case OP_lit19:
        case OP_lit20:
        case OP_lit21:
        case OP_lit22:
        case OP_lit23:
        case OP_lit24:
        case OP_lit25:
        case OP_lit26:
        case OP_lit27:
        case OP_lit28:
        case OP_lit29:
        case OP_lit30:
        case OP_lit31:
            add_command(SFT_CMD_NUMBER)->num = op - OP_lit0;
            break;
        case OP_breg0:
        case OP_breg1:
        case OP_breg2:
        case OP_breg3:
        case OP_breg4:
        case OP_breg5:
        case OP_breg6:
        case OP_breg7:
        case OP_breg8:
        case OP_breg9:
        case OP_breg10:
        case OP_breg11:
        case OP_breg12:
        case OP_breg13:
        case OP_breg14:
        case OP_breg15:
        case OP_breg16:
        case OP_breg17:
        case OP_breg18:
        case OP_breg19:
        case OP_breg20:
        case OP_breg21:
        case OP_breg22:
        case OP_breg23:
        case OP_breg24:
        case OP_breg25:
        case OP_breg26:
        case OP_breg27:
        case OP_breg28:
        case OP_breg29:
        case OP_breg30:
        case OP_breg31:
            {
                I8_T offs = dio_ReadS8LEB128();
                RegisterDefinition * def = get_reg_by_id(rules.ctx, op - OP_breg0, &rules.reg_id_scope);
                if (def == NULL) str_exception(errno, "Cannot read DWARF frame info");
                add_command(SFT_CMD_REGISTER)->reg = def;
                if (offs != 0) {
                    add_command(SFT_CMD_NUMBER)->num = offs;
                    add_command(SFT_CMD_ADD);
                }
            }
            break;
        case OP_nop:
            break;
        default:
            trace(LOG_ALWAYS, "Unsupported DWARF expression op 0x%02x", op);
            str_exception(ERR_UNSUPPORTED, "Unsupported DWARF expression op");
        }
    }
}

static void generate_register_commands(RegisterRules * reg, RegisterDefinition * dst_reg_def, RegisterDefinition * src_reg_def) {
    if (dst_reg_def == NULL) return;
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
            cmd->size = dst_reg_def->size;
            cmd->big_endian = rules.section->file->big_endian;
        }
        break;
    case RULE_SAME_VALUE:
        if (src_reg_def == NULL) return;
        add_command(SFT_CMD_REGISTER)->reg = src_reg_def;
        break;
    case RULE_REGISTER:
        {
            RegisterDefinition * src_sef = get_reg_by_id(rules.ctx, reg->offset, &rules.reg_id_scope);
            if (src_sef != NULL) add_command(SFT_CMD_REGISTER)->reg = src_sef;
        }
        break;
    case RULE_EXPRESSION:
    case RULE_VAL_EXPRESSION:
        add_command(SFT_CMD_FP);
        add_dwarf_expression_commands(reg->expression, reg->offset);
        if (reg->rule == RULE_EXPRESSION) {
            StackTracingCommand * cmd = add_command(SFT_CMD_DEREF);
            cmd->size = dst_reg_def->size;
            cmd->big_endian = rules.section->file->big_endian;
        }
        break;
    default:
        str_exception(ERR_INV_DWARF, "Invalid .debug_frame");
        break;
    }
    if (dwarf_stack_trace_regs_cnt >= trace_regs_max) {
        int i;
        trace_regs_max += 16;
        dwarf_stack_trace_regs = (StackTracingCommandSequence **)loc_realloc(dwarf_stack_trace_regs, trace_regs_max * sizeof(StackTracingCommandSequence *));
        for (i = dwarf_stack_trace_regs_cnt; i < trace_regs_max; i++) dwarf_stack_trace_regs[i] = NULL;
    }
    if (trace_cmds_cnt == 0) return;
    add_command_sequence(dwarf_stack_trace_regs + dwarf_stack_trace_regs_cnt++, dst_reg_def);
}

static void generate_commands(void) {
    int i;
    RegisterRules * reg;
    RegisterDefinition * reg_def;

    reg = get_reg(&frame_regs, rules.return_address_register);
    if (reg->rule != 0) {
        reg_def = get_reg_by_id(rules.ctx, rules.return_address_register, &rules.reg_id_scope);
        generate_register_commands(reg, get_PC_definition(rules.ctx), reg_def);
    }
    for (i = 0; i < frame_regs.regs_cnt; i++) {
        if (i == rules.return_address_register) continue;
        reg = get_reg(&frame_regs, i);
        if (reg->rule == 0) continue;
        reg_def = get_reg_by_id(rules.ctx, i, &rules.reg_id_scope);
        generate_register_commands(reg, reg_def, reg_def);
    }

    trace_cmds_cnt = 0;
    switch (rules.cfa_rule) {
    case RULE_OFFSET:
        reg_def = get_reg_by_id(rules.ctx, rules.cfa_register, &rules.reg_id_scope);
        if (reg_def != NULL) {
            add_command(SFT_CMD_REGISTER)->reg = reg_def;
            if (rules.cfa_offset != 0) {
                add_command(SFT_CMD_NUMBER)->num = rules.cfa_offset;
                add_command(SFT_CMD_ADD);
            }
        }
        break;
    case RULE_EXPRESSION:
        add_dwarf_expression_commands(rules.cfa_expression, rules.cfa_offset);
        break;
    default:
        str_exception(ERR_INV_DWARF, "Invalid .debug_frame");
        break;
    }
    add_command_sequence(&dwarf_stack_trace_fp, NULL);
}

static int generate_plt_section_commands(U8_T offs) {
    RegisterRules * reg = NULL;

    cie_regs.regs_cnt = 0;
    frame_regs.regs_cnt = 0;
    switch (rules.reg_id_scope.machine) {
    case EM_386:
        rules.cfa_rule = RULE_OFFSET;
        rules.cfa_register = 4; /* esp */
        if (offs == 0) {
            rules.cfa_offset = 8;
        }
        else if (offs < 16) {
            rules.cfa_offset = 12;
        }
        else if ((offs - 16) % 16 < 11) {
            rules.cfa_offset = 4;
        }
        else {
            rules.cfa_offset = 8;
        }
        rules.return_address_register = 8; /* eip */
        reg = get_reg(&frame_regs, rules.return_address_register);
        reg->rule = RULE_OFFSET;
        reg->offset = -4;
        generate_commands();
        return 1;
    case EM_X86_64:
        rules.cfa_rule = RULE_OFFSET;
        rules.cfa_register = 7; /* rsp */
        if (offs == 0) {
            rules.cfa_offset = 16;
        }
        else if (offs < 16) {
            rules.cfa_offset = 24;
        }
        else if ((offs - 16) % 16 < 11) {
            rules.cfa_offset = 8;
        }
        else {
            rules.cfa_offset = 16;
        }
        rules.return_address_register = 16; /* rip */
        reg = get_reg(&frame_regs, rules.return_address_register);
        reg->rule = RULE_OFFSET;
        reg->offset = -8;
        generate_commands();
        return 1;
    case EM_PPC:
        rules.return_address_register = 108; /* LR */
        rules.cfa_rule = RULE_OFFSET;
        rules.cfa_register = 1; /* R1 */
        rules.cfa_offset = 0;
        generate_commands();
        return 1;
    }
    return 0;
}

static void read_frame_cie(U8_T fde_pos, U8_T pos) {
    int cie_dwarf64 = 0;
    U8_T saved_pos = dio_GetPos();
    U8_T cie_length = 0;
    U8_T cie_end = 0;

    rules.cie_pos = pos;
    if (pos >= rules.section->size) {
        char msg[256];
        snprintf(msg, sizeof(msg),
            "Invalid CIE pointer 0x%" PRIX64
            " in FDE at 0x%" PRIX64, pos, fde_pos);
        str_exception(ERR_INV_DWARF, msg);
    }
    dio_Skip(pos - dio_GetPos());
    cie_length = dio_ReadU4();
    if (cie_length == ~(U4_T)0) {
        cie_length = dio_ReadU8();
        cie_dwarf64 = 1;
    }
    cie_end = dio_GetPos() + cie_length;
    dio_Skip(cie_dwarf64 ? 8 : 4);
    rules.version = dio_ReadU1();
    if (rules.version != 1 && rules.version != 3 && rules.version != 4) {
        str_exception(ERR_INV_DWARF, "Unsupported version of Call Frame Information");
    }
    rules.cie_aug = dio_ReadString();
    if (rules.cie_aug != NULL && strcmp(rules.cie_aug, "eh") == 0) {
        rules.cie_eh_data = dio_ReadAddress(&rules.cie_eh_data_section);
    }
    if (rules.version >= 4) {
        rules.address_size = dio_ReadU1();
        rules.segment_size = dio_ReadU1();
    }
    else {
        rules.address_size = rules.section->file->elf64 ? 8 : 4;
        rules.segment_size = 0;
    }
    if (rules.segment_size != 0) {
        str_exception(ERR_INV_DWARF, "Unsupported Call Frame Information: segment size != 0");
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

static void read_frame_fde(ELF_Section * section, U8_T IP, U8_T fde_pos) {
    int fde_dwarf64 = 0;
    U8_T fde_length = 0;
    U8_T fde_end = 0;
    U8_T ref_pos = 0;
    U8_T cie_ref = 0;
    int fde_flag = 0;

    dio_EnterSection(NULL, section, fde_pos);
    fde_length = dio_ReadU4();
    assert(fde_length > 0);
    if (fde_length == ~(U4_T)0) {
        fde_length = dio_ReadU8();
        fde_dwarf64 = 1;
    }
    ref_pos = dio_GetPos();
    fde_end = ref_pos + fde_length;
    cie_ref = fde_dwarf64 ? dio_ReadU8() : dio_ReadU4();
    if (rules.eh_frame) fde_flag = cie_ref != 0;
    else if (fde_dwarf64) fde_flag = cie_ref != ~(U8_T)0;
    else fde_flag = cie_ref != ~(U4_T)0;
    assert(fde_flag);
    if (fde_flag) {
        U8_T Addr, Range;
        ELF_Section * sec = NULL;
        if (rules.eh_frame) cie_ref = ref_pos - cie_ref;
        if (cie_ref != rules.cie_pos) read_frame_cie(fde_pos, cie_ref);
        Addr = read_frame_data_pointer(rules.addr_encoding, &sec);
        Range = read_frame_data_pointer(rules.addr_encoding, NULL);
        assert(Addr <= IP && Addr + Range > IP);
        if (Addr <= IP && Addr + Range > IP) {
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
                assert(location0 <= IP);
                if (rules.location > IP) break;
                location0 = rules.location;
            }
            dwarf_stack_trace_addr = location0;
            dwarf_stack_trace_size = rules.location - location0;
            generate_commands();
        }
    }
    dio_ExitSection();
}

static int cmp_frame_info_ranges(const void * x, const void * y) {
    FrameInfoRange * rx = (FrameInfoRange *)x;
    FrameInfoRange * ry = (FrameInfoRange *)y;
    if (rx->mAddr < ry->mAddr) return -1;
    if (rx->mAddr > ry->mAddr) return +1;
    return 0;
}

static void create_search_index(DWARFCache * cache, ELF_Section * section) {
    dio_EnterSection(NULL, section, 0);
    while (dio_GetPos() < section->size) {
        int fde_dwarf64 = 0;
        U8_T fde_length = 0;
        U8_T fde_pos = 0;
        U8_T fde_end = 0;
        U8_T ref_pos = 0;
        U8_T cie_ref = 0;
        int fde_flag = 0;

        fde_pos = dio_GetPos();
        fde_length = dio_ReadU4();
        if (fde_length == 0) continue;
        if (fde_length == ~(U4_T)0) {
            fde_length = dio_ReadU8();
            fde_dwarf64 = 1;
        }
        ref_pos = dio_GetPos();
        fde_end = ref_pos + fde_length;
        if (fde_end > rules.section->size) {
            char msg[256];
            snprintf(msg, sizeof(msg),
                "Invalid length 0x%" PRIX64
                " in FDE at 0x%" PRIX64, fde_length, fde_pos);
            str_exception(ERR_INV_DWARF, msg);
        }
        cie_ref = fde_dwarf64 ? dio_ReadU8() : dio_ReadU4();
        if (rules.eh_frame) fde_flag = cie_ref != 0;
        else if (fde_dwarf64) fde_flag = cie_ref != ~(U8_T)0;
        else fde_flag = cie_ref != ~(U4_T)0;
        if (fde_flag) {
            ELF_Section * sec = NULL;
            FrameInfoRange * range = NULL;
            if (rules.eh_frame) cie_ref = ref_pos - cie_ref;
            if (cie_ref != rules.cie_pos) read_frame_cie(fde_pos, cie_ref);
            if (cache->mFrameInfoRangesCnt >= cache->mFrameInfoRangesMax) {
                cache->mFrameInfoRangesMax += 512;
                if (cache->mFrameInfoRanges == NULL) cache->mFrameInfoRangesMax += (unsigned)(section->size / 32);
                cache->mFrameInfoRanges = (FrameInfoRange *)loc_realloc(cache->mFrameInfoRanges,
                    cache->mFrameInfoRangesMax * sizeof(FrameInfoRange));
            }
            range = cache->mFrameInfoRanges + cache->mFrameInfoRangesCnt++;
            range->mAddr = (ContextAddress)read_frame_data_pointer(rules.addr_encoding, &sec);
            range->mSize = (ContextAddress)read_frame_data_pointer(rules.addr_encoding, NULL);
            range->mOffset = fde_pos;
        }
        dio_Skip(fde_end - dio_GetPos());
    }
    dio_ExitSection();
    qsort(cache->mFrameInfoRanges, cache->mFrameInfoRangesCnt, sizeof(FrameInfoRange), cmp_frame_info_ranges);
}

void get_dwarf_stack_frame_info(Context * ctx, ELF_File * file, ELF_Section * sec, U8_T IP) {
    DWARFCache * cache = get_dwarf_cache(file);
    ELF_Section * section = cache->mDebugFrame;
    unsigned l, h;

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
    rules.reg_id_scope.big_endian = file->big_endian;
    rules.reg_id_scope.machine = file->machine;
    rules.reg_id_scope.os_abi = file->os_abi;
    rules.reg_id_scope.id_type = rules.eh_frame ? REGNUM_EH_FRAME : REGNUM_DWARF;
    rules.cie_pos = ~(U8_T)0;

    if (cache->mFrameInfoRanges == NULL) create_search_index(cache, section);
    l = 0;
    h = cache->mFrameInfoRangesCnt;
    while (l < h) {
        unsigned k = (l + h) / 2;
        FrameInfoRange * range = cache->mFrameInfoRanges + k;
        assert(cache->mFrameInfoRanges[l].mAddr <= cache->mFrameInfoRanges[h - 1].mAddr);
        if (range->mAddr > IP) {
            h = k;
        }
        else if (range->mAddr + range->mSize <= IP) {
            l = k + 1;
        }
        else {
            read_frame_fde(section, IP, range->mOffset);
            return;
        }
    }
    if (sec != NULL && sec->name != NULL && strcmp(sec->name, ".plt") == 0) {
        assert(IP >= sec->addr);
        assert(IP < sec->addr + sec->size);
        if (generate_plt_section_commands(IP - sec->addr)) return;
    }
}

#endif /* ENABLE_ELF */
