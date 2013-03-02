/*
  dis9900.c						-- 9900 instruction decoder

  (c) 1994-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
*/

/*
  $Id$
 */

/*
 *	This module decodes 9900 instructions and returns information
 *	about them.
 */


#include <stdio.h>
#include <ctype.h>
#include "v9t9_common.h"
#include "9900.h"
#include "9900st.h"
#include "dis9900.h"

/*	Penalties for accessing memory other than scratch pad or ROMs. */

static int  _is_pe_mem[] = { 
	0,		// 0x0000
	4,		// 0x2000
	4,		// 0x4000
	2,		// 0x6000
	0,		// 0x8000
	4,		// 0xa000
	4,		// 0xc000
	4,		// 0xe000
};
#define IS_PE_MEM(x)	_is_pe_mem[((x) >> 13) & 7]
//#define IS_PE_MEM(x)  (((x) & 0xe000) != 0)*4

//  Use this before reading the instruction, unless it
//  doesn't matter much that pc is pc+2 by now
#define CYCLES(pc, base,mem)	((base) + (mem) * IS_PE_MEM(pc))

static int i9900_mem_cycles(uaddr addr)
{
	return IS_PE_MEM(addr);
}

static int i9900_cycles(uaddr pc, int base, int mem)
{
	return CYCLES(pc, base, mem);
}


/*
 *	Complete an operand by fixing up val and ea as needed.
 *
 *	pc is the instruction's original address.
 *	addr is the address of the PC during instruction parsing.
 */
static void
operand_complete(dis9900_read_word read_word, Operand *op, u16 wp, u16 pc, u32 *addr, u16 *cycles)
{
	uaddr ad;
	switch (op->type)
	{
	case OP_NONE:	
		break;
	case OP_REG:	// Rx
		op->ea = (op->val<<1) + wp;
		*cycles += 0 * 4;
		break;
	case OP_INC:	// *Rx+
	case OP_IND:	// *Rx
		ad = (op->val<<1) + wp;
		op->ea = read_word(ad);
		*cycles += 4 + i9900_mem_cycles(ad);
		break;
	case OP_ADDR:	// @>xxxx or @>xxxx(Rx)
		op->ea = op->immed = read_word(*addr); 
		*cycles += 8 + i9900_mem_cycles(*addr);
		*addr += 2;
		if (op->val != 0) {
			ad = (op->val<<1) + wp;
			op->ea += read_word(ad);
			*cycles += i9900_mem_cycles(ad);
		}
		break;
	case OP_IMMED:	// immediate
		op->ea = *addr;
		op->immed = read_word(*addr); 
		*cycles += i9900_mem_cycles(ad);
		*addr += 2;
		break;
	case OP_CNT:	// shift count
		break;
	case OP_OFFS:	// offset from R12
		op->ea = read_word((12<<1) + wp) + op->val;
		break;
	case OP_JUMP:	// jump target
		op->val <<= 1;				// byte -> word
		op->val += (*addr - pc);
		op->ea = op->val + pc;
		break;
	case OP_STATUS:	// status word
		break;
	case OP_INST:
		break;		// can't handle here
	}
}

/*
 *	Print out an operand into a disassembler operand,
 *	returns NULL if no printable information
 */
char *
dis9900_operand_print(Operand *op, char *buffer, int bufsize)
{
	switch (op->type) 
	{
	case OP_REG:
		snprintf(buffer, bufsize, "R%d", op->val);
		break;

	case OP_IND:
		snprintf(buffer, bufsize, "*R%d", op->val);
		break;

	case OP_ADDR:
		if (op->val == 0) {
			snprintf(buffer, bufsize, "@>%04X", op->immed);
		} else {
			snprintf(buffer, bufsize, "@>%04X(R%d)", op->immed, op->val);
		}
		break;

	case OP_INC:
		snprintf(buffer, bufsize, "*R%d+", op->val);
		break;

	case OP_IMMED:
		snprintf(buffer, bufsize, ">%04X", op->immed);
		break;

	case OP_CNT:
		snprintf(buffer, bufsize, "%d", op->val);
		break;

	case OP_OFFS:
		snprintf(buffer, bufsize, ">%s%02X",
				(op->val & 0x8000) ? "-" : "", 
				(op->val & 0x8000) ? -op->val : op->val);
		break;

	case OP_JUMP:
		snprintf(buffer, bufsize, "$+>%04X", op->val);
		break;

	case OP_STATUS:		// not real operands
	case OP_INST:		
	default:
		return 0L;
	}

	return buffer;
}

/*
 *	Decode an instruction with opcode 'op' at 'addr'
 *	into 'ins'
 */
u32
dis9900_decode(u16 op, u32 pc, u16 wp, u16 st,
						   dis9900_read_word read_word,
						   Instruction *ins)
{
	Instruction	inst;
//	static unknown_buf[16];
	memset(&inst, 0, sizeof(inst));

	inst.cycles = i9900_mem_cycles(pc);
	inst.opcode = op;
//	snprintf(unknown_buf, sizeof(unknown_buf), "DATA >%04X", op);
//	inst.name = unknown_buf;
	inst.inst = Idata;
	inst.name = 0;
	inst.op1.type = inst.op2.type = OP_NONE;
	inst.stset = st_NONE;
	inst.jump = false;

	inst.pc = pc;
	inst.wp = wp;
	inst.status = st;

	// Collect the instruction name
	// and operand structure.

	pc += 2;	// point to operands

	// Initially, inst.op?.val is incomplete, and is whatever
	// raw data from the opcode we can decode;
	// inst.op?.ea is that of the instruction or immediate
	// if the operand needs it.
   
	// after decoding the instruction, we complete
	// the operand, making inst.op?.val and inst.op?.ea valid.
	
	if (op < 0x200) {
		inst.cycles += i9900_cycles(pc, 6, 1);
	} else if (op < 0x2a0) {
		inst.op1.type = OP_REG;
		inst.op1.val = op & 15;
		inst.op1.dest = true;
		inst.op2.type = OP_IMMED;
		switch ((op & 0x1e0) >> 5) 
		{
		case 0:		inst.name = "LI  ";	inst.inst = Ili; inst.stset = st_LAE;
			inst.op1.dest = OP_DEST_KILLED;
			inst.cycles += i9900_cycles(pc, 12, 3);
			break;
		case 1:		inst.name = "AI  ";	inst.inst = Iai; inst.stset = st_ADD_LAECO; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 2:		inst.name = "ANDI";	inst.inst = Iandi; inst.stset = st_LAE; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 3:		inst.name = "ORI ";	inst.inst = Iori; inst.stset = st_LAE; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 4:		inst.name = "CI  ";	inst.inst = Ici; inst.stset = st_CMP;
			inst.op1.dest = false; 
			inst.cycles += i9900_cycles(pc, 14, 3);
			break;
		}

	} else if (op < 0x2e0) {
		inst.op1.type = OP_REG;
		inst.op1.val = op & 15;
		inst.op1.dest = OP_DEST_KILLED;
		switch ((op & 0x1e0) >> 5) 
		{
		case 5:		inst.name = "STWP";	inst.inst = Istwp; 
			inst.cycles += i9900_cycles(pc, 8, 2);
			break;
		case 6:		inst.name = "STST"; inst.inst = Istst;
			inst.op2.type = OP_STATUS;
			inst.op2.val = st;
			inst.cycles += i9900_cycles(pc, 8, 2);
			break;
		}

	} else if (op < 0x320) {
		inst.op1.type = OP_IMMED;

		switch ((op & 0x1e0) >> 5) {
		case 7:		inst.name = "LWPI";	inst.inst = Ilwpi; 
			inst.cycles += i9900_cycles(pc, 10, 2);
			break;
		case 8:		inst.name = "LIMI";	inst.inst = Ilimi; inst.stset = st_INT; 
			inst.cycles += i9900_cycles(pc, 16, 2);
			break;
		}

	} else if (op < 0x400) {
		switch ((op & 0x1e0) >> 5) {
		case 10:	inst.name = "IDLE";	inst.inst = Iidle; 
			inst.cycles += i9900_cycles(pc, 12, 1);
			break;
		case 11:	inst.name = "RSET";	inst.inst = Irset; 
			inst.cycles += i9900_cycles(pc, 12, 1);
			break;
		case 12:	inst.name = "RTWP"; inst.inst = Irtwp; inst.stset = st_ALL;
			inst.op1.type = OP_STATUS;
			inst.op1.val = st;
			inst.jump = true;
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 13:	inst.name = "CKON";	inst.inst = Ickon; 
			inst.cycles += i9900_cycles(pc, 12, 1);
			break;
		case 14:	inst.name = "CKOF";	inst.inst = Ickof; 
			inst.cycles += i9900_cycles(pc, 12, 1);
			break;
		case 15:	inst.name = "LREX";	inst.inst = Ilrex; 
			inst.cycles += i9900_cycles(pc, 12, 1);
			break;
		}

	} else if (op < 0x800) {
		inst.op1.type = (op & 0x30) >> 4;
		inst.op1.val = op & 15;
		inst.op1.dest = true;

		switch ((op & 0x3c0) >> 6) 
		{
		case 0:		inst.name = "BLWP";	inst.inst = Iblwp;
			inst.op1.dest = false;
			inst.op1.addr = true;
			inst.jump = true;
			inst.cycles += i9900_cycles(pc, 26, 6);
			break;
		case 1:		inst.name = "B   ";	inst.inst = Ib;
			inst.op1.dest = false;
			inst.op1.addr = true;
			inst.jump = true;
			inst.cycles += i9900_cycles(pc, 8, 2);
			break;
		case 2:		inst.name = "X   "; inst.inst = Ix;
			inst.op1.dest = false;
			inst.op2.type = OP_INST;
			inst.cycles += i9900_cycles(pc, 8, 2);
			break;
		case 3:		inst.name = "CLR ";	inst.inst = Iclr;
			inst.op1.dest = OP_DEST_KILLED;
			inst.cycles += i9900_cycles(pc, 10, 3);
			break;
		case 4:		inst.name = "NEG ";	inst.inst = Ineg; inst.stset = st_LAEO; 
			inst.cycles += i9900_cycles(pc, 12, 3);
			break;
		case 5:		inst.name = "INV ";	inst.inst = Iinv; inst.stset = st_LAE; 
			inst.cycles += i9900_cycles(pc, 10, 3);
			break;
		case 6:		inst.name = "INC ";	inst.inst = Iinc; inst.stset = st_ADD_LAECO; 
			inst.cycles += i9900_cycles(pc, 10, 3);
			break;
		case 7:		inst.name = "INCT";	inst.inst = Iinct; inst.stset = st_ADD_LAECO; 
			inst.cycles += i9900_cycles(pc, 10, 3);
			break;
		case 8:		inst.name = "DEC ";	inst.inst = Idec; inst.stset = st_ADD_LAECO; 
			inst.cycles += i9900_cycles(pc, 10, 3);
			break;
		case 9:		inst.name = "DECT";	inst.inst = Idect; inst.stset = st_ADD_LAECO; 
			inst.cycles += i9900_cycles(pc, 10, 3);
			break;
		case 10:	inst.name = "BL  ";	inst.inst = Ibl;
			inst.op1.dest = false;
			inst.op1.addr = true;
			inst.jump = true;
			inst.cycles += i9900_cycles(pc, 12, 3);
			break;
		case 11:	inst.name = "SWPB";	inst.inst = Iswpb; 
			inst.cycles += i9900_cycles(pc, 10, 3);
			break;
		case 12:	inst.name = "SETO";	inst.inst = Iseto;
			inst.op1.dest = OP_DEST_KILLED;
			inst.cycles += i9900_cycles(pc, 10, 3);
			break;
		case 13:	inst.name = "ABS ";	inst.inst = Iabs; inst.stset = st_LAEO; 
			inst.cycles += i9900_cycles(pc, 12, 2);
			break;
		}

	} else if (op < 0xc00) {
		inst.op1.type = OP_REG;
		inst.op1.val = op & 15;
		inst.op1.dest = true;
		inst.op2.type = OP_CNT;
		inst.op2.val = (op & 0xf0) >> 4;

		// shift of zero comes from R0
		if (inst.op2.val == 0) {
			inst.op2.type = OP_REG;
			inst.op2.val = 0;
			inst.cycles += i9900_cycles(pc, 20, 3);
		}
		else {
			inst.cycles += i9900_cycles(pc, 12, 4);
		}

		switch ((op & 0x700) >> 8) 
		{
		case 0:		inst.name = "SRA ";	inst.inst = Isra; inst.stset = st_SHIFT_LAEC; break;
		case 1:		inst.name = "SRL ";	inst.inst = Isrl; inst.stset = st_SHIFT_LAEC; break;
		case 2:		inst.name = "SLA ";	inst.inst = Isla; inst.stset = st_SHIFT_LAECO; break;
		case 3:		inst.name = "SRC ";	inst.inst = Isrc; inst.stset = st_SHIFT_LAEC; break;
		}

	} else if (op < 0x1000) {
		switch ((op & 0x7e0) >> 5) {
			// !!! extended instructions
		}

	} else if (op < 0x2000) {
		if (op < 0x1d00) {
			inst.op1.type = OP_JUMP;
			inst.op1.val = ((s8) (op & 0xff));
			inst.op2.type = OP_STATUS;
			inst.op2.val = st;
			inst.jump = op < 0x1100 ? true : OP_JUMP_COND;
			inst.cycles += i9900_cycles(pc, 8, 1);
		} else {
			inst.op1.type = OP_OFFS;
			inst.op1.val = ((s8) (op & 0xff));
			inst.cycles += i9900_cycles(pc, 12, 2);
		}

		switch ((op & 0xf00) >> 8) {
		case 0:		inst.name = "JMP "; inst.inst = Ijmp; break;
		case 1:		inst.name = "JLT ";	inst.inst = Ijlt; break;
		case 2:		inst.name = "JLE ";	inst.inst = Ijle; break;
		case 3:		inst.name = "JEQ ";	inst.inst = Ijeq; break;
		case 4:		inst.name = "JHE ";	inst.inst = Ijhe; break;
		case 5:		inst.name = "JGT ";	inst.inst = Ijgt; break;
		case 6:		inst.name = "JNE ";	inst.inst = Ijne; break;
		case 7:		inst.name = "JNC ";	inst.inst = Ijnc; break;
		case 8:		inst.name = "JOC ";	inst.inst = Ijoc; break;
		case 9:		inst.name = "JNO ";	inst.inst = Ijno; break;
		case 10:	inst.name = "JL  ";	inst.inst = Ijl; break;
		case 11:	inst.name = "JH  ";	inst.inst = Ijh; break;
		case 12:	inst.name = "JOP ";	inst.inst = Ijop; break;
		case 13:	inst.name = "SBO ";	inst.inst = Isbo; break;
		case 14:	inst.name = "SBZ ";	inst.inst = Isbz; break;
		case 15:	inst.name = "TB  ";	inst.inst = Itb; inst.stset = st_E; break;
		}

	} else if (op < 0x4000 && !(op >= 0x3000 && op < 0x3800)) {
		inst.op1.type = (op & 0x30) >> 4;
		inst.op1.val = (op & 15);
		inst.op1.dest = false;
		inst.op2.type = OP_REG;
		inst.op2.val = (op & 0x3c0) >> 6;
		inst.op2.dest = true;

		switch ((op & 0x1c00) >> 10) {
		case 0:		inst.name = "COC ";	inst.inst = Icoc; inst.stset = st_E;
			inst.op2.dest = false;
			inst.cycles += i9900_cycles(pc, 14, 3);
			break;
		case 1:		inst.name = "CZC "; inst.inst = Iczc; inst.stset = st_E;
			inst.op2.dest = false;
			inst.cycles += i9900_cycles(pc, 14, 3);
			break;
		case 2:		inst.name = "XOR ";	inst.inst = Ixor; inst.stset = st_LAE; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 3:		inst.name = "XOP ";	inst.inst = Ixop; inst.stset = st_XOP; 
			inst.cycles += i9900_cycles(pc, 36, 8);
			break;
		case 6:		inst.name = "MPY ";	inst.inst = Impy; 
//			inst.op2.type = OP_MPY;
			inst.cycles += i9900_cycles(pc, 52, 5);
			break;
		case 7:		inst.name = "DIV "; inst.inst = Idiv; inst.stset = st_O;
//			inst.op2.type = OP_DIV;
			inst.cycles += i9900_cycles(pc, 124, 6);
			break;
		}

	} else if (op >= 0x3000 && op < 0x3800) {
		inst.op1.type = (op & 0x30) >> 4;
		inst.op1.val = (op & 15);
		inst.op2.type = OP_CNT;
		inst.op2.val = (op & 0x3c0) >> 6;
		if (inst.op2.val == 0) inst.op2.val = 16;
		inst.op1.byteop = (inst.op2.val <= 8);

		if (op < 0x3400) {
			inst.name = "LDCR"; inst.inst = Ildcr; 
			inst.stset = inst.op1.byteop ? st_BYTE_LAEP : st_LAE;
			inst.op1.dest = false;
			inst.cycles += i9900_cycles(pc, 20 + 2 * inst.op1.val, 3);
		} else {
			inst.name = "STCR"; inst.inst = Istcr;
			inst.stset = inst.op1.byteop ? st_BYTE_LAEP : st_LAE;
			inst.op1.dest = true;
			inst.cycles += i9900_cycles(pc, inst.op1.val < 8 ? 42 
										: inst.op1.val == 8 ? 44
										: 58, 4);
		}

	} else {
		inst.op1.type = (op & 0x30) >> 4;
		inst.op1.val = (op & 15);
		inst.op2.type = (op & 0x0c00) >> 10;
		inst.op2.val = (op & 0x3c0) >> 6;
		inst.op2.dest = true;
		inst.op1.byteop = inst.op2.byteop = ((op & 0x1000) != 0);
		if (inst.op1.byteop)
			inst.stset = st_BYTE_LAEP;
		else
			inst.stset = st_LAE;

		switch ((op & 0xf000) >> 12) {
		case 4:		inst.name = "SZC ";	inst.inst = Iszc; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 5:		inst.name = "SZCB";	inst.inst = Iszcb; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 6:		inst.name = "S   ";	inst.inst = Is; inst.stset = st_SUB_LAECO; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 7:		inst.name = "SB  ";	inst.inst = Isb; inst.stset = st_SUB_BYTE_LAECOP; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 8:		inst.name = "C   ";	inst.inst = Ic; inst.stset = st_CMP;
			inst.op2.dest = false;
			inst.cycles += i9900_cycles(pc, 14, 3);
			break;
		case 9:		inst.name = "CB  ";	inst.inst = Icb; inst.stset = st_BYTE_CMP;
			inst.op2.dest = false;
			inst.cycles += i9900_cycles(pc, 14, 3);
			break;
		case 10:	inst.name = "A   ";	inst.inst = Ia; inst.stset = st_ADD_LAECO; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 11:	inst.name = "AB  ";	inst.inst = Iab; inst.stset = st_ADD_BYTE_LAECOP; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 12:	inst.name = "MOV ";	inst.inst = Imov;
			inst.op2.dest = OP_DEST_KILLED;
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 13:	inst.name = "MOVB";	inst.inst = Imovb;
			inst.op2.dest = OP_DEST_KILLED;
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 14:	inst.name = "SOC ";	inst.inst = Isoc; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		case 15:	inst.name = "SOCB";	inst.inst = Isocb; 
			inst.cycles += i9900_cycles(pc, 14, 4);
			break;
		}
	}

	if (!inst.name) 				// data
	{
		inst.op1.type = OP_IMMED;
		pc -= 2;			  	// instruction itself is value
		inst.name = "DATA";
	}

	// Figure out the ea for the operands
	operand_complete(read_word, &inst.op1, wp, inst.pc, &pc, &inst.cycles);
	operand_complete(read_word, &inst.op2, wp, inst.pc, &pc, &inst.cycles);

	// And the instruction for X
	if (inst.op2.type == OP_INST) {
		inst.op2.val = read_word(inst.op1.ea);
	}

	*ins = inst;

	return pc;
}

/*
 *	Tell if the operand has any effect on a register;
 *	return a bitmap for each
 */
void
dis9900_derive_register_access(Instruction *inst, Operand *op, 
							   dis9900_read_word read_word,
							   int *read, int *written)
{
	switch (op->type)
	{
	case OP_REG:
		if (op->dest != OP_DEST_KILLED)
			*read |= (1 << op->val);
		if (op->dest)
			*written |= (1 << op->val);

		// multiply writes two registers
		if ((inst->opcode >= 0x3800 && inst->opcode < 0x3C00) 
			&& op->dest)
			*written |= (1 << (op->val+1));

		// divide reads and writes two registers
		if (inst->opcode >= 0x3C00 && inst->opcode < 0x4000) {
			if (op->dest)
				*read |= (1 << (op->val+1));
			else
				*written |= (1 << (op->val+1));
		}
		break;

	case OP_IND:
	case OP_ADDR:
	case OP_INC:
		if (op->type != OP_ADDR || op->val != 0)
			*read |= (1 << op->val);
	   
		if (op->type == OP_INC)
			*written |= (1 << op->val);

		// memory write to register?
		if (op->ea >= inst->wp && op->ea < inst->wp + 32) {
			if (op->dest != OP_DEST_KILLED)
				*read |= (1 << ((op->ea - inst->wp) >> 1));
			if (op->dest)
				*written |= (1 << ((op->ea - inst->wp) >> 1));
		}
		break;

	case OP_INST:
		{
			Instruction xinst;
			dis9900_decode(op->val, 
						   inst->pc, inst->wp, inst->status,
						   read_word,
						   &xinst);

			// watch out for recursion
			if (xinst.op1.type != OP_INST
				&& xinst.op2.type != OP_INST) 
			{
				dis9900_derive_register_access(&xinst, &xinst.op1, read_word,
											   read, written);
				dis9900_derive_register_access(&xinst, &xinst.op2, read_word,
											   read, written);
			} 
			else 
			{
				// panic
				*read = *written = -1;
			}
		}
		break;
	}
}
