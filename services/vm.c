/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

#include <config.h>

#if ENABLE_DebugContext

#include <errno.h>
#include <framework/trace.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <services/stacktrace.h>
#include <services/dwarf.h>
#include <services/vm.h>

#define check_e_stack(n) { if (state->stk_pos < n) inv_dwarf("Invalid DWARF expression stack"); }

static VMState * state = NULL;
static uint8_t * code = NULL;
static unsigned code_pos = 0;
static unsigned code_len = 0;

static void inv_dwarf(const char * msg) {
    str_exception(ERR_INV_DWARF, msg);
}

static StackFrame * get_stack_frame(void) {
    StackFrame * info = NULL;
    if (state->stack_frame == STACK_NO_FRAME) return NULL;
    if (get_frame_info(state->ctx, state->stack_frame, &info) < 0) exception(errno);
    return info;
}

static uint64_t read_memory(uint64_t addr, size_t size) {
    size_t i;
    uint64_t n = 0;
    uint8_t buf[8];

    if (context_read_mem(state->ctx, (ContextAddress)addr, buf, size) < 0) exception(errno);
    for (i = 0; i < size; i++) {
        n = (n << 8) | buf[state->big_endian ? i : size - i - 1];
    }
    return n;
}

static uint8_t read_u1(void) {
    if (code_pos >= code_len) inv_dwarf("Invalid command");
    return code[code_pos++];
}

static uint16_t read_u2(void) {
    uint16_t x0 = read_u1();
    uint16_t x1 = read_u1();
    return state->big_endian ? (x0 << 8) | x1 : x0 | (x1 << 8);
}

static uint32_t read_u4(void) {
    uint32_t x0 = read_u2();
    uint32_t x1 = read_u2();
    return state->big_endian ? (x0 << 16) | x1 : x0 | (x1 << 16);
}

static uint64_t read_u8(void) {
    uint64_t x0 = read_u4();
    uint64_t x1 = read_u4();
    return state->big_endian ? (x0 << 32) | x1 : x0 | (x1 << 32);
}

static uint32_t read_u4leb128(void) {
    uint32_t res = 0;
    int i = 0;
    for (;; i += 7) {
        uint8_t n = read_u1();
        res |= (n & 0x7Fu) << i;
        if ((n & 0x80) == 0) break;
    }
    return res;
}

static uint64_t read_u8leb128(void) {
    uint64_t res = 0;
    int i = 0;
    for (;; i += 7) {
        uint8_t n = read_u1();
        res |= (n & 0x7Fu) << i;
        if ((n & 0x80) == 0) break;
    }
    return res;
}

static int64_t read_i8leb128(void) {
    uint64_t res = 0;
    int i = 0;
    for (;; i += 7) {
        uint8_t n = read_u1();
        res |= (n & 0x7Fu) << i;
        if ((n & 0x80) == 0) {
            res |= -(n & 0x40) << i;
            break;
        }
    }
    return (int64_t)res;
}

static uint64_t read_ia(void) {
    switch (state->addr_size) {
    case 1: return (int8_t)read_u1();
    case 2: return (int16_t)read_u2();
    case 4: return (int32_t)read_u4();
    case 8: return (int64_t)read_u8();
    default: inv_dwarf("Invalid address size");
    }
    return 0;
}

static uint64_t read_ua(void) {
    switch (state->addr_size) {
    case 1: return read_u1();
    case 2: return read_u2();
    case 4: return read_u4();
    case 8: return read_u8();
    default: inv_dwarf("Invalid address size");
    }
    return 0;
}

static void set_state(VMState * s) {
    state = s;
    code = state->code;
    code_pos = state->code_pos;
    code_len = state->code_len;
    state->reg = NULL;
    state->piece_offs = 0;
    state->piece_bits = 0;
}

static void get_state(VMState * s) {
    s->code_pos = code_pos;
    state = NULL;
    code = NULL;
    code_pos = 0;
    code_len = 0;
}

static void evaluate_expression(void) {
    uint64_t data = 0;

    if (code_len == 0) inv_dwarf("DWARF expression size = 0");

    while (code_pos < code_len && state->piece_bits == 0) {
        uint8_t op = code[code_pos++];

        if (state->stk_pos + 4 > state->stk_max) {
            state->stk_max += 8;
            state->stk = (uint64_t *)loc_realloc(state->stk, sizeof(uint64_t) * state->stk_max);
        }

        switch (op) {
        case OP_deref:
            check_e_stack(1);
            state->stk[state->stk_pos - 1] = read_memory(state->stk[state->stk_pos - 1], state->addr_size);
            break;
        case OP_deref2:
            check_e_stack(1);
            state->stk[state->stk_pos - 1] = (int16_t)read_memory(state->stk[state->stk_pos - 1], 2);
            break;
        case OP_deref_size:
            check_e_stack(1);
            state->stk[state->stk_pos - 1] = read_memory(state->stk[state->stk_pos - 1], read_u1());
            break;
        case OP_const:
            state->stk[state->stk_pos++] = read_ia();
            break;
        case OP_const1u:
            state->stk[state->stk_pos++] = read_u1();
            break;
        case OP_const1s:
            state->stk[state->stk_pos++] = (int8_t)read_u1();
            break;
        case OP_const2u:
            state->stk[state->stk_pos++] = read_u2();
            break;
        case OP_const2s:
            state->stk[state->stk_pos++] = (int16_t)read_u2();
            break;
        case OP_const4u:
            state->stk[state->stk_pos++] = read_u4();
            break;
        case OP_const4s:
            state->stk[state->stk_pos++] = (int32_t)read_u4();
            break;
        case OP_const8u:
            state->stk[state->stk_pos++] = read_u8();
            break;
        case OP_const8s:
            state->stk[state->stk_pos++] = (int64_t)read_u8();
            break;
        case OP_constu:
            state->stk[state->stk_pos++] = read_u8leb128();
            break;
        case OP_consts:
            state->stk[state->stk_pos++] = read_i8leb128();
            break;
        case OP_dup:
            check_e_stack(1);
            state->stk[state->stk_pos] = state->stk[state->stk_pos - 1];
            state->stk_pos++;
            break;
        case OP_drop:
            check_e_stack(1);
            state->stk_pos--;
            break;
        case OP_over:
            check_e_stack(2);
            state->stk[state->stk_pos] = state->stk[state->stk_pos - 2];
            state->stk_pos++;
            break;
        case OP_pick:
            {
                unsigned n = read_u1();
                check_e_stack(n + 1);
                state->stk[state->stk_pos] = state->stk[state->stk_pos - n - 1];
                state->stk_pos++;
            }
            break;
        case OP_swap:
            check_e_stack(2);
            data = state->stk[state->stk_pos - 1];
            state->stk[state->stk_pos - 1] = state->stk[state->stk_pos - 2];
            state->stk[state->stk_pos - 2] = data;
            break;
        case OP_rot:
            check_e_stack(3);
            data = state->stk[state->stk_pos - 1];
            state->stk[state->stk_pos - 1] = state->stk[state->stk_pos - 2];
            state->stk[state->stk_pos - 2] = state->stk[state->stk_pos - 3];
            state->stk[state->stk_pos - 3] = data;
            break;
        case OP_xderef:
            check_e_stack(2);
            state->stk[state->stk_pos - 2] = read_memory(state->stk[state->stk_pos - 1], state->addr_size);
            state->stk_pos--;
            break;
        case OP_xderef_size:
            check_e_stack(2);
            state->stk[state->stk_pos - 2] = read_memory(state->stk[state->stk_pos - 1], read_u1());
            state->stk_pos--;
            break;
        case OP_abs:
            check_e_stack(1);
            if ((int64_t)state->stk[state->stk_pos - 1] < 0) {
                state->stk[state->stk_pos - 1] = ~state->stk[state->stk_pos - 1] + 1;
            }
            break;
        case OP_and:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] = state->stk[state->stk_pos - 1] & state->stk[state->stk_pos];
            break;
        case OP_div:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] /= state->stk[state->stk_pos];
            break;
        case OP_minus:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] -= state->stk[state->stk_pos];
            break;
        case OP_mod:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] %= state->stk[state->stk_pos];
            break;
        case OP_mul:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] *= state->stk[state->stk_pos];
            break;
        case OP_neg:
            check_e_stack(1);
            state->stk[state->stk_pos - 1] = ~state->stk[state->stk_pos - 1] + 1;
            break;
        case OP_not:
            check_e_stack(1);
            state->stk[state->stk_pos - 1] = ~state->stk[state->stk_pos - 1];
            break;
        case OP_or:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] = state->stk[state->stk_pos - 1] | state->stk[state->stk_pos];
            break;
        case OP_add:
        case OP_plus:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] += state->stk[state->stk_pos];
            break;
        case OP_plus_uconst:
            check_e_stack(1);
            state->stk[state->stk_pos - 1] += read_u8leb128();
            break;
        case OP_shl:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] <<= state->stk[state->stk_pos];
            break;
        case OP_shr:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] >>= state->stk[state->stk_pos];
            break;
        case OP_shra:
            {
                uint64_t cnt;
                check_e_stack(2);
                data = state->stk[state->stk_pos - 2];
                cnt = state->stk[state->stk_pos - 1];
                while (cnt > 0) {
                    int s = (data & ((uint64_t)1 << 63)) != 0;
                    data >>= 1;
                    if (s) data |= (uint64_t)1 << 63;
                    cnt--;
                }
                state->stk[state->stk_pos - 2] = data;
                state->stk_pos--;
            }
            break;
        case OP_xor:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] = state->stk[state->stk_pos - 1] ^ state->stk[state->stk_pos];
            break;
        case OP_bra:
            check_e_stack(1);
            {
                unsigned offs = (int16_t)read_u2();
                if (state->stk[state->stk_pos - 1]) {
                    code_pos += offs;
                    if (code_pos > code_len) inv_dwarf("Invalid command");
                }
                state->stk_pos--;
            }
            break;
        case OP_eq:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] = state->stk[state->stk_pos - 1] == state->stk[state->stk_pos];
            break;
        case OP_ge:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] = state->stk[state->stk_pos - 1] >= state->stk[state->stk_pos];
            break;
        case OP_gt:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] = state->stk[state->stk_pos - 1] > state->stk[state->stk_pos];
            break;
        case OP_le:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] = state->stk[state->stk_pos - 1] <= state->stk[state->stk_pos];
            break;
        case OP_lt:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] = state->stk[state->stk_pos - 1] < state->stk[state->stk_pos];
            break;
        case OP_ne:
            check_e_stack(2);
            state->stk_pos--;
            state->stk[state->stk_pos - 1] = state->stk[state->stk_pos - 1] != state->stk[state->stk_pos];
            break;
        case OP_skip:
            code_pos += (int16_t)read_u2();
            if (code_pos > code_len) inv_dwarf("Invalid command");
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
            state->stk[state->stk_pos++] = op - OP_lit0;
            break;
        case OP_reg0:
        case OP_reg1:
        case OP_reg2:
        case OP_reg3:
        case OP_reg4:
        case OP_reg5:
        case OP_reg6:
        case OP_reg7:
        case OP_reg8:
        case OP_reg9:
        case OP_reg10:
        case OP_reg11:
        case OP_reg12:
        case OP_reg13:
        case OP_reg14:
        case OP_reg15:
        case OP_reg16:
        case OP_reg17:
        case OP_reg18:
        case OP_reg19:
        case OP_reg20:
        case OP_reg21:
        case OP_reg22:
        case OP_reg23:
        case OP_reg24:
        case OP_reg25:
        case OP_reg26:
        case OP_reg27:
        case OP_reg28:
        case OP_reg29:
        case OP_reg30:
        case OP_reg31:
            {
                unsigned n = op - OP_reg0;
                if (code_pos < code_len && code[code_pos] != OP_piece) inv_dwarf("OP_reg must be last instruction");
                state->reg = get_reg_by_id(state->ctx, n, &state->reg_id_scope);
                if (state->reg == NULL) exception(errno);
            }
            break;
        case OP_regx:
            {
                unsigned n = (unsigned)read_u4leb128();
                if (code_pos < code_len && code[code_pos] != OP_piece) inv_dwarf("OP_regx must be last instruction");
                state->reg = get_reg_by_id(state->ctx, n, &state->reg_id_scope);
                if (state->reg == NULL) exception(errno);
            }
            break;
        case OP_reg:
            {
                unsigned n = (unsigned)read_ua();
                if (code_pos < code_len && code[code_pos] != OP_piece) inv_dwarf("OP_reg must be last instruction");
                state->reg = get_reg_by_id(state->ctx, n, &state->reg_id_scope);
                if (state->reg == NULL) exception(errno);
            }
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
                RegisterDefinition * def = get_reg_by_id(state->ctx, op - OP_breg0, &state->reg_id_scope);
                if (def == NULL) exception(errno);
                if (read_reg_value(get_stack_frame(), def, state->stk + state->stk_pos) < 0) exception(errno);
                state->stk[state->stk_pos++] += read_i8leb128();
            }
            break;
        case OP_bregx:
            {
                RegisterDefinition * def = get_reg_by_id(state->ctx, (unsigned)read_u4leb128(), &state->reg_id_scope);
                if (def == NULL) exception(errno);
                if (read_reg_value(get_stack_frame(), def, state->stk + state->stk_pos) < 0) exception(errno);
                state->stk[state->stk_pos++] += read_i8leb128();
            }
            break;
        case OP_basereg:
            {
                RegisterDefinition * def = get_reg_by_id(state->ctx, (unsigned)read_ua(), &state->reg_id_scope);
                if (def == NULL) exception(errno);
                if (read_reg_value(get_stack_frame(), def, state->stk + state->stk_pos) < 0) exception(errno);
                state->stk_pos++;
            }
            break;
        case OP_call_frame_cfa:
            {
                StackFrame * frame = get_stack_frame();
                if (frame == NULL) str_exception(ERR_INV_ADDRESS, "Stack frame address not available");
                state->stk[state->stk_pos++] = frame->fp;
            }
            break;
        case OP_nop:
            break;
        case OP_push_object_address:
            state->stk[state->stk_pos++] = state->object_address;
            break;
        case OP_piece:
            state->piece_bits = read_u4leb128() * 8;
            state->piece_offs = 0;
            if (code_pos < code_len && state->piece_bits == 0) {
                if (state->reg) state->reg = NULL;
                else state->stk_pos--;
            }
            break;
        case OP_bit_piece:
            state->piece_bits = read_u4leb128();
            state->piece_offs = read_u4leb128();
            if (code_pos < code_len && state->piece_bits == 0) {
                if (state->reg) state->reg = NULL;
                else state->stk_pos--;
                state->piece_offs = 0;
            }
            break;
        case OP_call2:
        case OP_call4:
        case OP_call_ref:
        default:
            {
                VMState * s = state;
                get_state(s);
                s->client_op(op);
                set_state(s);
            }
        }
    }
}

int evaluate_vm_expression(VMState * vm_state) {
    int error = 0;
    Trap trap;

    set_state(vm_state);
    if (set_trap(&trap)) {
        evaluate_expression();
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    get_state(vm_state);
    if (!error) return 0;
    errno = error;
    return -1;
}

#endif /* ENABLE_DebugContext */
