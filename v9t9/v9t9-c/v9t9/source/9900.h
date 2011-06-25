/*
  (c) 1994-2001 Edward Swartz

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

#ifndef __9900_H__
#define __9900_H__

#include "centry.h"

#if __MWERKS__ && __INTEL__
#define MW_X86_ASM 1
//#define USE_STATUS_WORD 1
//#define USE_STATUS_WORD 1
//#define FAST_X86_STATUS 1

#else
#define MW_X86_ASM 0
#endif

#ifndef GNU_X86_ASM
#define GNU_X86_ASM 0
#endif

#ifndef USE_STATUS_WORD
#define USE_STATUS_WORD 0
#endif

#ifndef FAST_X86_STATUS
#define FAST_X86_STATUS 0
#endif

extern uaddr pc,wp;
extern u16 *wpptr;
extern uword status;

//	active interrupt pins
enum 		{ INTPIN_RESET = 1, INTPIN_LOAD = 2, INTPIN_INTREQ = 4 };
extern u8	intpins9900;		
void		hold9900pin(u8 mask);

// interrupt mask from LIMI
extern u8	intlevel9900;

extern u8 dump_instructions;

// status word masks
#define ST_L		0x8000
#define ST_A		0x4000
#define	ST_E		0x2000
#define	ST_C		0x1000
#define	ST_O		0x800
#define ST_P		0x400
#define ST_X		0x200
#define ST_INTLEVEL 0xf

/*	Fake opcodes for emulation */
enum
{
	OP_DSR 		= 0xC00,	/* if this changes, fix asm equate in ../tools/DSRs/opcodes.inc */
	OP_KEYSLOW	= 0xD40,
	OP_DBG		= 0xDE0,
};

extern void set9900wp(u16 wp);
void init9900(void);
void emulateloop(void);
void setup_status(void);
void change9900intmask(u16 mask);

u16 fetch(void);
void execute(uop op);
void contextswitch(uaddr addr);
bool verifypc(uaddr addr);
bool verifywp(uaddr addr);
void setandverifypc(uaddr addr);
void setandverifywp(uaddr addr);

/******************************************/

#define register(reg) (wpptr[reg])

/*
	wchange -- change a word in memory at 'addr'.  
	'prewhat' is done to the word before the operation.
	'what' is the operation.
	'expr' is the operand,
	'status' is the function that deciphers the answer.
*/
#define wchange(addr,prewhat,what,expr,status) {  \
	register u16 answer=prewhat (memory_read_word(addr)) what expr; \
	memory_write_word(addr,answer); \
	status(answer); \
	}

/*
	rchange -- change register 'reg'.  
	'prewhat' is done to the word before the operation.
	'what' is the operation.
	'expr' is the operand,
	'status' is the function that deciphers the answer.
	
	This assumes 'wp' is in range.
*/
#if 0
#define rchange(reg,prewhat,what,expr,status) {  \
	register u16 *raddr=wpptr+reg; \
	register int answer=(prewhat (*raddr)) what expr; \
	*raddr= answer; status(answer); } \


#define radd(reg,expr) {  \
	register u16 *raddr = wpptr+reg; \
    *raddr=setst_add_laeco(*raddr,expr); }
#else

#define rchange(reg,prewhat,what,expr,status) {  \
	register int answer=(prewhat (*(wpptr+reg))) what expr; \
	*(wpptr+reg)= answer; status(answer); } \


#define radd(reg,expr) {  \
    *(wpptr+reg)=setst_add_laeco(*(wpptr+reg),expr); }

#endif

#define wadd(addr,expr) {  \
	memory_write_word(addr,setst_add_laeco(memory_read_word(addr),expr)); \
	}
	

#include "cexit.h"

#endif
