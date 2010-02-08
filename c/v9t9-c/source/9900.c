#if __MWERKS__
#pragma optimization_level 4
#endif
/*
  9900.c					-- 9900 processor emulation	

  (c) 1994-2004 Edward Swartz

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
	9900 emulator.  "execute" executes one instruction at (PC,WP).

	Define GNU_X86_ASM to implement some instructions in GCC x86 assembly.
	(Define MW_X86_ASM for MW assembly)
		These implement shift, add, multiply, divide, and other 
		instructions in assembly to avoid complicated work re-calculating
		status bits.
	Define USE_STATUS_WORD to implement carry, overflow, parity bits in
		'status' word.  This avoids setting lastval/lastcmp.
*/

/*
	Updated 7/99 with iffy list of clock speeds per instruction.
	Updated 8/99 with better info from http://www.stanford.edu/~thierry1/ti99/
	11/99:  clock timings are WAY TOO SLOW!  Can't possibly be right.
	Whoops, the IS_PE_MEM macro was reversed.... ;)
*/

#define __9900__

#include "v9t9_common.h"
#include "cru.h"
#include "memory.h"
#include "emulate.h"
#include "debugger.h"
#include "log.h"
#include "compiler.h"
#include "9900.h"

#define _L	 LOG_CPU | LOG_INFO
#include "9900st.h"
#include "9900asm.h"
#include "opcode_callbacks.h"

void        execute(uop op);

uaddr       pc, wp;
u16        *wpptr;
uword       status;


s16 		lastcmp,lastval;

u8          intlevel9900;
u8          intpins9900;

u8 			dump_instructions;

extern long instcycles;	// # cycles estimated per instruction

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

/*	In the following, instcycles is incremented first by
	the number of cycles taken to execute the instruction,
	then by the number of cycles needed to read the instruction
	from memory -- it looks like the 9900 reads the instruction
	word several times for decoding!
	
	Time is taken for the instruction read by 'fetch'.
	Time is taken for argument decoding by 'decipheraddr'.
	Instructions also have added time for the transfer of
	arguments to and from memory, which is added in along
	with the execution time.
*/

//  Use this before reading the instruction, unless it
//  doesn't matter much that pc is pc+2 by now
#define CYCLES(base,mem)	((base) + (mem) * IS_PE_MEM(pc))

#if USE_STATUS_WORD

/*
	LAE bits maintained in lastcmp/lastval.
	
	ALWAYS, lastcmp is 0<=lastcmp<=0xffff.
	ALWAYS, status has 0xf mask for interrupt level, ST_X for XOP, etc.
*/
u16 statusto9900(void)
{
	status=(status&~(ST_L|ST_E|ST_A)) |
			( (u16)lastval > (u16)lastcmp ? ST_L : 0) |
			( (s16)lastval > (s16)lastcmp ? ST_A : 0) |
			(lastval==lastcmp ? ST_E : 0);
	return status;
}

void T9900tostatus(u16 stat)
{
	lastval=lastcmp=0;
	status=stat;
	if (!(status&ST_E)) {
		if (!(status&(ST_L|ST_A)))
			lastcmp++;
		else {
			lastval++;
			if (!(status&ST_L))
				lastcmp=0xffff;
			else
				if (!(status&ST_A))
					lastval=(-lastval)&0xffff;
		}
	}
		
}

#else

u8 st_o, st_c, st_p;

/*
	LAE bits maintained in lastcmp/lastval.
	
	ALWAYS, lastcmp is 0<=lastcmp<=0xffff.
	ALWAYS, status has 0xf mask for interrupt level, ST_X for XOP, etc.
	st_o, st_c, and st_p maintain those bits.
*/
u16 statusto9900(void)
{
#if MW_X86_ASM
	asm {
		movzx eax, status
		and eax, ~(ST_C|ST_O|ST_P|ST_L|ST_A|ST_E)
		movzx ebx, st_c
		movzx ecx, st_o
		movzx edx, st_p		
		sal ebx, 12
		sal ecx, 11
		or eax, ecx
		sal edx, 10
		or eax, ebx
		mov si, lastval
		or eax, edx
		cmp si, lastcmp
		setg cl
		seta dl
		sete bl
		sal ecx, 15
		sal edx, 14
		sal ebx, 13
		or eax, ecx
		or eax, edx
		or eax, ebx
		mov status, ax
	}

#else
	status= ( status & ~(ST_C|ST_O|ST_P|ST_L|ST_A|ST_E)) |
			( st_o ? ST_O : 0 ) |
			( st_c ? ST_C : 0 ) |
			( st_p ? ST_P : 0 ) |
			( (u16)lastval > (u16)lastcmp ? ST_L : 0) |
			( (s16)lastval > (s16)lastcmp ? ST_A : 0) |
			(lastval==lastcmp ? ST_E : 0);
#endif
	return status;
}

void T9900tostatus(u16 stat)
{
	lastval=lastcmp=0;
	status=stat;
	if (!(status&ST_E)) {
		if (!(status&(ST_L|ST_A)))
			lastcmp++;
		else {
			lastval++;
			if (!(status&ST_L))
				lastcmp=0xffff;
			else
				if (!(status&ST_A))
					lastval=(-lastval)&0xffff;
		}
	}
	st_o = (status & ST_O) != 0;
	st_c = (status & ST_C) != 0;
	st_p = (status & ST_P) != 0;
}

#endif


/**************************************************************************/

void        
hold9900pin(u8 mask)
{
	intpins9900 |= mask;
	stateflag |= ST_INTERRUPT;
}

void
change9900intmask(u16 mask)
{
	intlevel9900 = mask & 0xf;
}


u16 
fetch(void)
{
	register u16 op = memory_read_word(pc);

	instcycles += IS_PE_MEM(pc);
	pc = (pc + 2) & 0xfffe;
	return op;
}

bool 
verifywp(uaddr addr)
{
	if (compile_mode) compiler_flush_for_wp(addr);
	return (HAS_RAM_ACCESS(md_cpu, addr) && HAS_RAM_ACCESS(md_cpu, addr + 31));
}

void 
setandverifywp(uaddr addr)
{
	static u16 zero16[16];
	wp = addr & 0xfffe;
	wpptr = registerptr(0);
	if (wpptr < (u16 *)0x10000) wpptr = zero16;
	
	// allow user to have bad code running
	if (0 && !verifywp(wp)) {
		logger(_L | LOG_USER, _("Illegal workspace pointer set (>%04X)\n"),
			 wp);
		command_exec_text("Interactive on\n");
	}
}

bool
verifypc(uaddr addr)
{
	return true; //(HAS_ROM_ACCESS(md_cpu, pc) || (pc >= 0x4000 && pc < 0x6000));
}

void
setandverifypc(uaddr addr)
{
	pc = addr & 0xfffe;
	if (!verifypc(pc)) {
		logger(_L | LOG_USER, _("Invalid program counter set (>%04X), aborting\n"),
			 pc);
		command_exec_text("Interactive on\n");
	}
}

void
contextswitch(uaddr addr)
{
	u16         oldwp, oldpc, newwp, newpc;
	u16        *rptr;

//  instcycles += CYCLES(14,4);
	oldwp = wp;
	oldpc = pc;
	newwp = memory_read_word(addr);
	newpc = memory_read_word(addr + 2);

	if (oldwp == newwp && addr) {
		logger(_L, _("*** BLWP'ing to same workspace, may be hosed\n"));
//      debugger_enable();
	}
	setandverifywp(newwp);
	setandverifypc(newpc);
	rptr = wpptr + 13;
	*rptr++ = oldwp;
	*rptr++ = oldpc;
	*rptr = statusto9900();

	if (addr == 0) {
		/*  this mimics the behavior
		   where holding down fctn-quit
		   keeps the program going */
		trigger9901int(M_INT_VDP);
		hold9900pin(INTPIN_INTREQ);
	}
}

/*
	thierry times for address decodes:
	"memory access" means if the memory is over the PE box link;
	this is four cycles each!

   Address mode	Clock cycles 	Memory access
   Rx			0  				0
   *Rx 			4 				1
   *Rx+ (byte)	6				2
	   	(word)	8				2
   @>xxxx 		8 				1
   @>xxxx(Rx) 	8 				2

*/

u16         INLINE
decipheraddr(uop op, int regmask, int tmask, int shiftright)
{
	register uword reg = (op & regmask) >> shiftright;
	uword       ts = ((op & tmask) >> shiftright);
	register u16 imm;
	uword       addr;

	if (ts == 0) {
		addr = (wp + reg + reg) & 0xffff;
		instcycles += 0 * 4;
	} else if (ts == 0x10) {
		addr = memory_read_word((wp + reg + reg) & 0xffff);
		instcycles += 4 + IS_PE_MEM((wp + reg + reg) & 0xffff) * 1;
	} else if (ts == 0x20) {
		imm = fetch();
		if (reg) {
			addr =
				(memory_read_word((wp + reg + reg) & 0xffff) + imm) & 0xffff;
			instcycles += 8 + IS_PE_MEM((wp + reg + reg) & 0xffff) * 2;
		} else {
			addr = imm;
			instcycles += 8 + IS_PE_MEM(imm) * 1;
		}
	} else {
		register u16 *regval = wpptr + reg;

		addr = *regval;
		(*regval) += 2;
		instcycles += 8 + IS_PE_MEM((wp + reg + reg) & 0xffff) * 2;

	}

	return addr;

}


u16         INLINE
decipheraddrbyte(uop op, int regmask, int tmask, int shiftright)
{
	register uword reg = (op & regmask) >> shiftright;
	uword       ts = ((op & tmask) >> shiftright);
	register u16 imm;
	uaddr       addr;

	if (ts == 0) {
		addr = (wp + reg + reg) & 0xffff;
		instcycles += 0 * 4;
	} else if (ts == 0x10) {
		addr = memory_read_word((wp + reg + reg) & 0xffff);
		instcycles += 4 + IS_PE_MEM((wp + reg + reg) & 0xffff) * 1;
	} else if (ts == 0x20) {
		imm = fetch();
		instcycles += 2;
		if (reg) {
			addr =
				(memory_read_word((wp + reg + reg) & 0xffff) + imm) & 0xffff;
			instcycles += 8 + IS_PE_MEM((wp + reg + reg) & 0xffff) * 2;
		} else {
			addr = imm;
			instcycles += 8 + IS_PE_MEM(imm) * 1;
		}
	} else {
		register u16 *regval = wpptr + reg;

		addr = *regval;
		(*regval)++;
		instcycles += 6 + IS_PE_MEM((wp + reg + reg) & 0xffff) * 2;

	}

	return addr;

}

/*************************************************************************/

/*
;==========================================================================
;       Data instructions,                                      >0000->01FF
;==========================================================================
*/
void        INLINE
h0000(uop op)
{
	instcycles += CYCLES(6, 1);
}


/*
;==========================================================================
;       Immediate, Control instructions,                        >0200->03FF
;--------------------------------------------------------------------------
;
;         0 1 2 3-4 5 6 7+8 9 A B-C D E F               LI, AI, ANDI, ORI,
;       ----------------------------------              CI, STWP, STST,
;       |      o p c o d e     |0| reg # |              LIMI, LWPI, IDLE,
;       ----------------------------------              RSET, RTWP, CKON,
;                                                       CKOF, LREX
;==========================================================================
*/
void        INLINE
h0200(uop op)
{
	register int reg = op & 0xf;
	register int imm;

	switch ((op & 0x1e0) >> 5) {
	case 0:					/* LI */
		instcycles += CYCLES(12, 3);
		imm = fetch();
		register    (reg) = imm;

		setst_lae(imm);

		break;
	case 1:					/* AI */
		instcycles += CYCLES(14, 4);
		imm = fetch();
		radd(reg, imm);
		break;
	case 2:					/* ANDI */
		instcycles += CYCLES(14, 4);
		imm = fetch();
		rchange(reg,, &, imm, setst_lae);
		break;
	case 3:					/* ORI */
		instcycles += CYCLES(14, 4);
		imm = fetch();
		rchange(reg,, |, imm, setst_lae);
		break;
	case 4:					/* CI */
		instcycles += CYCLES(14, 3);
		lastval = register (reg);

		lastcmp = fetch();
		break;
	case 5:					/* STWP */
		instcycles += CYCLES(8, 2);
		register    (reg) = wp;

		break;
	case 6:					/* STST */
		instcycles += CYCLES(8, 2);
		register    (reg) = statusto9900();

		break;
	case 7:					/* LWPI */
		instcycles += CYCLES(10, 2);
		imm = fetch();
		setandverifywp(imm);
		break;
	case 8:					/* LIMI */
		instcycles += CYCLES(16, 2);
		imm = fetch();
		status = (status & ~0xf) | (imm & 0xf);
		change9900intmask(imm);
		logger(LOG_CPU | L_2, "*** LIMI %d\n", imm);
		break;
	case 10:					/* IDLE */
		instcycles += CYCLES(12, 1);
		/*  should activate CRUCLK  */
		break;
	case 11:					/* RSET */
		instcycles += CYCLES(12, 1);
		change9900intmask(0);
		/*  should activate CRUCLK  */
		break;
	case 12:					/* RTWP */
	{
		u16        *rptr;

		instcycles += CYCLES(14, 4);
		rptr = wpptr + 15;
		T9900tostatus(*rptr--);
		pc = *rptr--;
		setandverifywp(*rptr);
		//intlevel9900 = (status & 0xf);  // moved to emulate.c
		break;
	}
	case 13:					/* CKON */
	case 14:					/* CKOF */
	case 15:					/* LREX */
		instcycles += CYCLES(12, 1);
		/*  should activate CRUCLK  */
		break;
	}
}

/*
;==========================================================================
;       Single-operand instructions,                            >0400->07FF
;--------------------------------------------------------------------------
;
;         0 1 2 3-4 5 6 7+8 9 A B-C D E F               BLWP, B, X, CLR,
;       ----------------------------------              NEG, INV, INC, INCT,
;       |      o p c o d e   |TS |   S   |              DEC, DECT, BL, SWPB,
;       ----------------------------------              SETO, ABS
;
;==========================================================================
*/

static void
h0400(uop op)
{
	register uaddr addr;

	addr = decipheraddr(op, 0xf, 0x30, 0);

	switch ((op & 0x3c0) >> 6) {
	case 0:					/* BLWP */
		instcycles += CYCLES(26, 6);
		contextswitch(addr);
		break;

	case 1:					/* B */
		instcycles += CYCLES(8, 2);
		pc = addr;
		break;

	case 2:					/* X */
		instcycles += CYCLES(8, 2);
		op = memory_read_word(addr);
		execute(op);
		break;

	case 3:					/* CLR */
		instcycles += CYCLES(10, 3);
		memory_write_word(addr, 0);
		break;

	case 4:					/* NEG */
		instcycles += CYCLES(12, 3);
		wchange(addr, -,,, setst_laeo);
		break;

	case 5:					/* INV */
		instcycles += CYCLES(10, 3);
		wchange(addr, ~,,, setst_lae);
		break;

	case 6:					/* INC */
		instcycles += CYCLES(10, 3);
		wadd(addr, 1);
		break;

	case 7:					/* INCT */
		instcycles += CYCLES(10, 3);
		wadd(addr, 2);
		break;

	case 8:					/* DEC */
		instcycles += CYCLES(10, 3);
		wadd(addr, 0xffff);
		break;

	case 9:					/* DECT */
		instcycles += CYCLES(10, 3);
		wadd(addr, 0xfffe);
		break;

	case 10:					/* BL */
		instcycles += CYCLES(12, 3);
		register(11) = pc;

		pc = addr;
		break;

	case 11:					/* SWPB */
		instcycles += CYCLES(10, 3);
		wchange(addr, swpb,,,);
		break;

	case 12:					/* SETO */
		instcycles += CYCLES(10, 3);
		memory_write_word(addr, 0xffff);
		break;

	case 13:					/* ABS */
	{
		u16         val;

		instcycles += CYCLES(12, 2);

		val = memory_read_word(addr);
		setst_lae(val);
		if (val >= 0x8000) {
			memory_write_word(addr, -setst_o(val));
			instcycles += CYCLES(2, 1);
		}
		break;
	}

	}
}

/*
;==========================================================================
;       Shift instructions,                                     >0800->0BFF
;       AND my own instructions,                                >0C00->0FFF
;--------------------------------------------------------------------------
;
;         0 1 2 3-4 5 6 7+8 9 A B-C D E F               SRA, SRL, SLA, SRC
;       ----------------------------------              ------------------
;       |  o p c o d e   |   C   |   W   |              DSR, KEYS, SPRI,
;       ----------------------------------              TRAN, INT1, BRK,
;                                                       TIDSR, KEYSLOW,
;                                                       SCREEN, DBG, -DBG
;
;==========================================================================
*/


static void
h0800(uop op)
{
	register u32 reg = (op & 0xf);
	register u32 cnt = (op & 0xf0) >> 4;
	u16        *rptr = wpptr + reg;

	if (cnt == 0) {
		cnt = register (0) & 0xf;

		if (cnt == 0)
			cnt = 16;			// whoops!  archiver-3 needs this
		instcycles += CYCLES(20, 3);
	} else {
		instcycles += CYCLES(12, 4);
	}

	instcycles += cnt * 2;

	/* ONLY shift now */
	switch ((op & 0x700) >> 8) {
	case 0:					/* SRA */
		*rptr = setst_sra_laec(*rptr, cnt);
		break;

	case 1:					/* SRL */
		*rptr = setst_srl_laec(*rptr, cnt);
		break;

	case 2:					/* SLA */
		*rptr = setst_sla_laeco(*rptr, cnt);
		break;

	case 3:					/* SRC */
		*rptr = setst_src_laec(*rptr, cnt);
		break;

	default:
		switch ((op & 0x3e0) >> 5) {
			// 0xc00
		case 0:				/* DSR, OP_DSR */
			emulate_dsr();
			break;

			// 0xd40
		case 10:			/* KEYSLOW */
			emulate_keyslow();
			break;

			// 0xd60
		case 11:			/* TICKS */
			*rptr = totalticks;
			break;

			// 0xdc0
		case 14:				/* EMITCHAR */
			if ((*rptr >> 8) == 0xd)
				printf("\n");
			else
				printf("%c", (*rptr) >> 0x8);
			break;

			// 0xde0
		case 15:				/* DBG, -DBG */
			debugger_enable(!(op & 0xf));
			if (!(op & 0xf))
				execution_pause(1);
			break;
		default:
			logger(LOG_CPU | L_1,
				   _("unhandled extended opcode >%04X at >%04X [%d]\n"), op, pc,
				   (op & 0x3e0) >> 5);
			break;
		}
		break;
	}
}

/*
;==========================================================================
;       Jump, CRU bit instructions,                             >1000->1FFF
;--------------------------------------------------------------------------
;
;         0 1 2 3-4 5 6 7+8 9 A B-C D E F               JMP, JLT, JLE, JEQ,
;       ----------------------------------              JHE, JGT, JNE, JNC,
;       |   o p c o d e  | signed offset |              JOC, JNO, JL,JH,JOP
;       ----------------------------------              ---------------------
;                                                       SBO, SBZ, TB
;
;==========================================================================
*/
static void
h1000(uop op)
{
	register s32 offs = (s8) (op) << 1;

	if (op < 0x1D00)			/* jumps */
		instcycles = CYCLES(8, 1);
	else
		instcycles = CYCLES(12, 2);

	switch ((op & 0xf00) >> 8) {
	case 0:					/* JMP */
		setandverifypc(pc + offs);
		instcycles += 2;
		break;
	case 1:					/* JLT */
		if ((s16) lastval < (s16) lastcmp) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}

		break;
	case 2:					/* JLE */
		if ((u16) lastval <= (u16) lastcmp) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
		break;
	case 3:					/* JEQ */
		if (lastval == lastcmp) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
		break;
	case 4:					/* JHE */
		if ((u16) lastval >= (u16) lastcmp) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
		break;
	case 5:					/* JGT */
		if ((s16) lastval > (s16) lastcmp) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
		break;
	case 6:					/* JNE */
		if (lastval != lastcmp) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
		break;
	case 7:					/* JNC */
#if USE_STATUS_WORD
		if ((status & ST_C) == 0) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
#else
		if (!st_c) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
#endif
		break;
	case 8:					/* JOC */
#if USE_STATUS_WORD
		if (status & ST_C) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
#else
		if (st_c) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
#endif
		break;
	case 9:					/* JNO */
#if USE_STATUS_WORD
		if ((status & ST_O) == 0) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
#else
		if (!st_o) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
#endif
		break;
	case 10:					/* JL */
		if ((u16) lastval < (u16) lastcmp) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
		break;
	case 11:					/* JH */
		if ((u16) lastval > (u16) lastcmp) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
		break;
	case 12:					/* JOP */
#if USE_STATUS_WORD
		if (status & ST_P) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
#else
		if (st_p) {
			setandverifypc(pc + offs);
			instcycles += 2;
		}
#endif
		break;

	case 13:					/* SBO */
		cruwrite(register (12) + offs, 1, 1);

		break;
	case 14:					/* SBZ */
		cruwrite(register (12) + offs, 0, 1);

		break;
	case 15:					/* TB */
	{
		u8          ans = cruread(register (12) + offs, 1);

		setst_e(ans & 1, 1);
	}
		break;
	}
}

/*
;==========================================================================
;       General and One-Register instructions                   >2000->3FFF
;--------------------------------------------------------------------------
;
;         0 1 2 3-4 5 6 7+8 9 A B-C D E F               COC, CZC, XOR,
;       ----------------------------------              LDCR, STCR, XOP,
;       |   opcode   |   D   |TS |   S   |              MPY, DIV
;       ----------------------------------
;
;==========================================================================
*/
static void
h2000(uop op)
{
	register u32 reg = (op & 0x3c0) >> 6;
	register u32 src;
	register u16 val;
	u16        *rptr;

	if (op < 0x3000 || op >= 0x3800) {
		src = decipheraddr(op, 0xf, 0x30, 0);
	} else {
		// CRU instructions:  'reg' is treated
		// as # of bits, where 0 == 16.  0-7 bits
		// causes a byte operation, 8-16 causes a word
		// operation.

		if (reg == 0)
			reg = 16;
		if (reg <= 8)
			src = decipheraddrbyte(op, 0xf, 0x30, 0);
		else
			src = decipheraddr(op, 0xf, 0x30, 0);
	}

	switch ((op & 0x1c00) >> 10) {
	case 0:					/* COC */
		val = memory_read_word(src);
		setst_e(val & register (reg), val);

		instcycles += CYCLES(14, 3);
		break;

	case 1:					/* CZC */
		val = memory_read_word(src);
		setst_e(val & ~register (reg), val);

		instcycles += CYCLES(14, 3);
		break;

	case 2:					/* XOR */
		rptr = wpptr + reg;
		*rptr = setst_lae((*rptr) ^ memory_read_word(src));

		instcycles += CYCLES(14, 4);
		break;

	case 3:					/* XOP */
		contextswitch(0x40 + reg * 4);
		status |= ST_X;			// done here so it is reset on rtwp
		register    (11) = src;

		instcycles += CYCLES(36, 8);
		break;

	case 4:					/* LDCR */
		cruwrite(register (12),
				 (reg <= 8 ? setst_byte_laep(memory_read_byte(src)) :
				  setst_lae(memory_read_word(src))), reg);

		instcycles += CYCLES(20 + 2 * reg, 3);
		break;

	case 5:					/* STCR */
		if (reg <= 8)
			memory_write_byte(src,

							  setst_byte_laep(cruread(register (12), reg)));
		else
			memory_write_word(src, setst_lae(cruread(register (12), reg)));

		instcycles +=
			CYCLES(reg <= 7 ? 42 : reg == 8 ? 44 : reg <= 15 ? 58 : 60, 4);
		break;

	case 6:					/* MPY */
	{
		// oops, egcs fucks it up
#if 0 && GNU_X86_ASM 
		register u16 a asm("ax");
		register u16 b asm("dx");
		register u16 *r asm("ebx");
		a = register (reg);

		b = memory_read_word(src);
		r = wpptr + reg;
	  asm("\tmulw %1\n" "\tmovw %1,(%2)\n" "\tmovw %0,2(%2)\n":
	  :"=r"(a), "=r"(b), "r"(r)
	  :"ax", "dx");
#elif MW_X86_ASM 
		register u16 a asm("ax");
		register u16 b asm("dx");
		register u16 *r;

		b = memory_read_word(src);
		r = wpptr + reg;
		a = register (reg);
		asm {
			mov ebx, r
			mul b
			mov [ebx], dx
			mov [ebx+2], ax
		}
#else

		u32         prod;

		rptr = wpptr + reg;
		prod = *rptr * memory_read_word(src);
		*rptr++ = prod >> 16;
		*rptr = prod & 0xffff;

#endif


		instcycles += CYCLES(52, 5);
	}
		break;

	case 7:					/* DIV */
	{
		// oops, egcs fucks this up
#if 0 && GNU_X86_ASM
		register u16 hi asm("dx");
		register u16 lo asm("ax");
		register u16 d;
		register u16 *r;		// asm ("ebx");

		d = memory_read_word(src);
		hi = wpptr[reg];
		lo = wpptr[reg+1];
		r = wpptr + reg;
		if (d <= hi) {
			status |= ST_O;
			instcycles += CYCLES(16, 3);
		} else {
		  asm("\tdivw %2,%0\n" "\tmovw %0,(%3)\n" "\tmovw %1,2(%3)\n":
		  :"r"(lo), "r"(hi), "g"(d), "r"(r)
		  :"ax", "dx");
			status &= ~ST_O;
			instcycles += CYCLES(124, 6);
		}

#elif MW_X86_ASM 

		register u16 hi asm("dx");
		register u16 lo asm("ax");
		register u16 d;
		register u16 *r;

		d = memory_read_word(src);
		hi = wpptr[reg];
		lo = wpptr[reg+1];
		r = wpptr + reg;
		if (d <= hi) {
			status |= ST_O;
			instcycles += CYCLES(16, 3);
		} else {
			asm {
				mov ebx, r
				div d
				mov [ebx], lo		// need to use lo/hi explicitly for lifetimes
				mov [ebx+2], hi
			}
			status &= ~ST_O;
			instcycles += CYCLES(124, 6);
		}

#else

		u16         hi, lo;
		u32         dividend;
		u16         d;

		rptr = wpptr + reg;
		hi = *rptr++;
		lo = *rptr;
		dividend = ((u32) hi << 16) | lo;
		d = memory_read_word(src);

		if (d <= hi) {
#if USE_STATUS_WORD
			status |= ST_O;
#else
			st_o = 1;
#endif
			instcycles += CYCLES(16, 3);
		} else {
			u16 mod = dividend % d, div = dividend / d;
			*rptr-- = mod;
			*rptr = div;
			//*rptr-- = dividend % d;
			//*rptr = dividend / d;

#if USE_STATUS_WORD
			status &= ~ST_O;
#else
			st_o = 0;
#endif
			instcycles += CYCLES(124, 6);	// assume avg case (92-124)
		}
#endif
	}
	}
}

/*
;==========================================================================
;       Two-Register instructions                               >4000->FFFF
;--------------------------------------------------------------------------
;
;         0 1 2 3-4 5 6 7+8 9 A B-C D E F               SZC, SZCB, S, SB,
;       ----------------------------------              C, CB, A, AB, MOV,
;       |opcode|B|TD |   D   |TS |   S   |              MOVB, SOC, SOCB
;       ----------------------------------
;
;==========================================================================
*/
static void
h4000(uop op)
{
	register u32 src;
	register u32 dest;
	u16 srcword;
	s8 srcbyte;

	/* bugfix: read src before calculating dest addr,
	   i.e. MOV R5, *R5+ */
	if (op & 0x1000) {
		src = decipheraddrbyte(op, 0xf, 0x30, 0);
		srcbyte = memory_read_byte(src);
		dest = decipheraddrbyte(op, 0x3c0, 0xc00, 6);
	} else {
		src = decipheraddr(op, 0xf, 0x30, 0);
		srcword = memory_read_word(src);
		dest = decipheraddr(op, 0x3c0, 0xc00, 6);
	}

	switch ((op & 0xf000) >> 12) {
	case 4:					/* SZC */
		memory_write_word(dest,
						  setst_lae(memory_read_word(dest) &
									~srcword));
		instcycles += CYCLES(14, 4);
		break;
	case 5:					/* SZCB */
		memory_write_byte(dest,
						  setst_byte_laep(memory_read_byte(dest) &
										  ~srcbyte));
		instcycles += CYCLES(14, 4);
		break;

	case 6:					/* S */
		memory_write_word(dest,
						  setst_sub_laeco(memory_read_word(dest),
										  srcword));
		instcycles += CYCLES(14, 4);
		break;
	case 7:					/* SB */
		memory_write_byte(dest,
						  setst_subbyte_laecop(memory_read_byte(dest),
											   srcbyte));
		instcycles += CYCLES(14, 4);
		break;

	case 8:					/* C */
		lastval = srcword;
		lastcmp = memory_read_word(dest);
		instcycles += CYCLES(14, 3);
		break;
	case 9:					/* CB */
		lastval = (s8) srcbyte;
		lastcmp = (s8) memory_read_byte(dest);
		instcycles += CYCLES(14, 3);
		break;

	case 10:					/* A */
		memory_write_word(dest,
						  setst_add_laeco(memory_read_word(dest),
										  srcword));
		instcycles += CYCLES(14, 4);
		break;
	case 11:					/* AB */
		memory_write_byte(dest,
						  setst_addbyte_laecop(memory_read_byte(dest),
											   srcbyte));
		instcycles += CYCLES(14, 4);
		break;

	case 12:					/* MOV */
		memory_write_word(dest, setst_lae(srcword));
		instcycles += CYCLES(14, 4);
		break;
	case 13:					/* MOVB */
		memory_write_byte(dest, setst_byte_laep(srcbyte));
		instcycles += CYCLES(14, 4);
		break;

	case 14:					/* SOC */
		memory_write_word(dest,
						  setst_lae(memory_read_word(dest) |
									srcword));
		instcycles += CYCLES(14, 4);
		break;
	case 15:					/* SOCB */
		memory_write_byte(dest,
						  setst_byte_laep(memory_read_byte(dest) |
										  srcbyte));
		instcycles += CYCLES(14, 4);
		break;
	}
}

void
execute(uop op)
{
	static void (*jumptable[]) (uop) = {
		&h0000, &h0200, &h0400, &h0400, &h0800, &h0800, &h0800, &h0800,
		&h1000, &h1000, &h1000, &h1000, &h1000, &h1000, &h1000, &h1000,
		&h2000, &h2000, &h2000, &h2000, &h2000, &h2000, &h2000, &h2000,
		&h2000, &h2000, &h2000, &h2000, &h2000, &h2000, &h2000, &h2000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000,
		&h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000, &h4000
	};

/*
	static u16 trace[65536]; static int traceptr=0;

	trace[traceptr++] = pc;
	traceptr &= 0xffff;
	if (domain_read_word(md_cpu, 0x83fe) ==  0x3002) {
		int i = traceptr;
		do {
			logger(LOG_USER, ">%04X ", trace[i]);
			i = (i++) & 0xffff;
			if (i % 8 == 0) logger(LOG_USER, "\n");
		} while (i != traceptr);
		debugger_enable(true);
		execution_pause(true);
	}
*/
	if (dump_instructions)
		fprintf(stderr, "%04X %04X %04X %04X\n", pc-2, statusto9900(),
		   vdp_mmio_get_addr(), grom_mmio_get_addr());

	(jumptable[op >> 9]) (op);
}

void
init9900(void)
{
#if USE_STATUS_WORD && FAST_X86_STATUS
	setup_status();
#endif
	wpptr = registerptr(0);
}
