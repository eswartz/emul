/*
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

#ifndef __DIS9900_H__
#define __DIS9900_H__

#include "v9t9_types.h"

#include "centry.h"

typedef u16 (*dis9900_read_word)(u16 addr);

// 	operands for instructions

typedef enum {
	OP_NONE=-1,	// no operand

	// from ts/td field of opcode, don't change order
	OP_REG=0,	// register Rx
	OP_IND=1,	// indirect *Rx
	OP_ADDR=2,	// address @>xxxx
	OP_INC=3,	// register increment *Rx+

	OP_IMMED,	// immediate >xxxx
	OP_CNT,		// shift count x (4 bits)
	OP_JUMP,   	// jump target >xxxx
	OP_OFFS,	// offset >xxxx or ->xxxx
	OP_STATUS,	// status word >xxxx
	OP_INST		// instruction for X
}	OperandType;

#define OP_DEST_KILLED 2

struct Object;

typedef struct Operand {
	OperandType	type;			// type of operand
	u16			val;			// value in opcode
	u16			immed;			// immediate word
	u16			ea;				// effective address of operand
	bool		byteop;			// for OP_REG...OP_INC
	char		dest;			// operand changes (OP_DEST_KILLED=killed)
	bool		addr;			// operand is an address
	struct Object *obj;			// high level data
}	Operand;

#define OP_IS_MEMORY(op) \
		(((op).type == OP_IND || (op).type == OP_ADDR || (op).type == OP_INC) \
		&& !(op).addr)

#define OP_JUMP_COND	2

typedef enum {
	Idata,
	Ili,
	Iai,
	Iandi,
	Iori,
	Ici,
	Istwp,
	Istst,
	Ilwpi,
	Ilimi,
	Iidle,
	Irset,
	Irtwp,
	Ickon,
	Ickof,
	Ilrex,
	Iblwp,
	Ib,
	Ix,
	Iclr,
	Ineg,
	Iinv,
	Iinc,
	Iinct,
	Idec,
	Idect,
	Ibl,
	Iswpb,
	Iseto,
	Iabs,
	Isra,
	Isrl,
	Isla,
	Isrc,
	Ijmp,
	Ijlt,
	Ijle,
	Ijeq,
	Ijhe,
	Ijgt,
	Ijne,
	Ijnc,
	Ijoc,
	Ijno,
	Ijl,
	Ijh,
	Ijop,
	Isbo,
	Isbz,
	Itb,
	Icoc,
	Iczc,
	Ixor,
	Ixop,
	Impy,
	Idiv,
	Ildcr,
	Istcr,
	Iszc,
	Iszcb,
	Is,
	Isb,
	Ic,
	Icb,
	Ia,
	Iab,
	Imov,
	Imovb,
	Isoc,
	Isocb
}	inst9900;

typedef enum
{
	st_NONE,		// status not affected
	st_ALL,			// all bits changed
	st_INT,			// interrupt mask
	st_XOP,			// xop bits changed
	st_CMP,			// comparison
	st_BYTE_CMP,	// with bytes
	st_LAE,			// arithmetic...
	st_LAEO,
	st_O,
	st_SHIFT_LAEC,	
	st_SHIFT_LAECO,	
	st_E,
	st_BYTE_LAEP,
	st_SUB_LAECO,
	st_SUB_BYTE_LAECOP,
	st_ADD_LAECO,
	st_ADD_BYTE_LAECOP
}	StatusSetting;

typedef struct Instruction {
	const char	*name;			// name of instruction
	u16			pc;				// PC of opcode
	u16			wp;				// current WP
	u16			status;			// current status
	u16			opcode;			// opcode (full)
	inst9900	inst;			// instruction
	Operand		op1, op2;	   	// operands of instruction
	u16			cycles;			// execution cycles
	StatusSetting stset;		// method status is set
	bool		jump;			// operand is a jump (OP_JUMP_COND = conditional)

	// high-level stuff
	struct Instruction *next, *prev;	// if in a list...
	struct Block *block;
	u32			flags;			
}	Instruction;

/*
 *	Decode an instruction with opcode 'op' at 'addr'
 *	into 'ins'
 */
u32
dis9900_decode(u16 op, u32 pc, u16 wp, u16 st,
						   dis9900_read_word read_word,
						   Instruction *ins);

/*
 *	Tell if the operand has any effect on a register;
 *	return a bitmap for each
 */
void
dis9900_derive_register_access(Instruction *inst, Operand *op, 
							   dis9900_read_word read_word,
							   int *read, int *written);

/*
 *	Print out an operand into a disassembler operand,
 *	returns NULL if no printable information
 */
char *
dis9900_operand_print(Operand *op, char *buffer, int bufsize);

#include "cexit.h"

#endif // __DIS9900_H_
