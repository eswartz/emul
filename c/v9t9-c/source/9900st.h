/*
  9900st.h						-- 9900 status word update functions

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

/*
	Status word functions

*/

#include "centry.h"

extern s16 lastcmp, lastval;

#if !USE_STATUS_WORD
extern u8 st_o, st_c, st_p;
#endif

/************************************************************************/

u16 statusto9900(void);
void T9900tostatus(u16 stat);

#ifdef __9900__

static u8 parity[16] = { 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0};
   
// determine parity of an 8-bit byte, 0=even, 1=odd
#define PARITY(val) ((parity[(val & 0xf)] ^ parity[(val >> 4) & 0xf]) & 1)


/*
	Set lae, preserve C and O
*/
s16 INLINE setst_lae(s16 val)
{
	lastcmp=0;
	lastval=val;
	return val;
}


/*
	Set lae, preserve C and O (BYTE)
*/
s8 INLINE setst_byte_laep(s8 val)
{
#if !USE_STATUS_WORD

	#if MW_X86_ASM
	
		asm {
			or val, val
			setpo st_p
		}
	#else
	
 		st_p = PARITY(val);

	#endif
	
#else

	#if GNU_X86_ASM
	
		status &= ~ST_P;
		asm(
			"\t or %0,%0\n"
			"\t jpe 1f\n"
			"\t	orb $0x4,status+1\n"
			"\t1:\n"
			: 
			: "r" (val)
		);
		
		lastval=val;
		lastcmp=0;
		return val;

	#elif MW_X86_ASM

		asm {
			and status, ~ST_P
			or val, val
			jpe lab
			or status, ST_P
		lab:
		}

	#else
	
		if (PARITY(val))
			status |= ST_P;
		else
			status &= ~ST_P;
	
	#endif

#endif

	lastval=val;
	lastcmp=0;
	
	return val;
}

/*
	For COC, CZC, and TB
*/
void INLINE setst_e(u16 val,u16 to)
{
	if (val==to){ lastcmp=lastval=0; }
	else		{ if (lastcmp==lastval) lastval++; }
}


#if USE_STATUS_WORD && FAST_X86_STATUS

#define I86_CF 1
#define I86_OF (1<<11)
#define I86_PF (1<<2)

u8 i86_to_9900[I86_OF+I86_CF+I86_PF+1];

/*	One-time setup of i86_to_9900 array.
	For each entry corresponding to some combination of bits set 
	in the x86 status flag,	we set the corresponding entry to
	the 9900 status flag value. */

void setup_status(void)
{
	int	i;

	memset(i86_to_9900,0x00,sizeof(i86_to_9900));
	for (i=0; i<sizeof(i86_to_9900); i++)
	{
		if (i&I86_CF)
			i86_to_9900[i]|=0x10;
		if (i&I86_OF)
			i86_to_9900[i]|=0x8;
		if (!(i&I86_PF))
			i86_to_9900[i]|=0x4;
	}
}

#endif

/*
	Set laeco for add, preserve none
*/
u16 INLINE setst_add_laeco(u16 dst,u16 src)
{
#if !USE_STATUS_WORD

	#if MW_X86_ASM

		register u16 res = dst;
		
		asm {
			add res, src
			setc st_c
			seto st_o
		}
	
	#else
	
		u16 res = dst + src;
		
		st_c = (((dst & src) & 0x8000) || 
			(((dst & 0x8000) ^ (src & 0x8000)) && !(res & 0x8000))) != 0;
			
		st_o = (((~dst & ~src & res) | (dst & src & ~res)) & 0x8000) != 0;

	#endif
	
#else

#if !FAST_X86_STATUS

	#if GNU_X86_ASM
	
		register u16 res=dst;

		status&=~(ST_C|ST_O);
		asm(
			"\t addw %1,%0\n"
			"\t pushf\n"
			"\t jno 1f\n"
			"\t orb $0x8,status+1\n"
			"\t1: popf\n"
			"\t jnc 2f\n"
			"\t orb $0x10,status+1\n"
			"\t2:\n"
			: "=r" (res)
			: "g" (src), "0" (dst)
			);
		
	#elif MW_X86_ASM

		register u16 res=dst;

		asm {
			and status, ~(ST_C|ST_O)
			add res, src
			pushfd 
			jno lab1
			or status, ST_O
		lab1:
			popfd
			jnc lab2
			or status, ST_C
		lab2:
		}
	
	#else

		u16 res = dst + src;

		status&=~(ST_C|ST_O);
		if ((((dst & src) & 0x8000) || 
			(((dst & 0x8000) ^ (src & 0x8000)) && !(res & 0x8000))))
			status |= ST_C;
			
		if ((((~dst & ~src & res) | (dst & src & ~res)) & 0x8000) != 0)
			status |= ST_O;
	
	#endif
	
#else		//FAST_X86
	
	#if GNU_X86_ASM
		register u16 res=dst;
		register u32 flags;
		register u8 tmp;

		status&=~(ST_C|ST_O);
		asm(
			"\t addw %1,%0\n"
			"\t pushf\n"
			"\t popl %3\n"
			"\t andl $2049,%3\n"
			"\t movb i86_to_9900(%3),%4\n"
			"\t orb %4,status+1\n"
			: "=r" (res)
			: "g" (src), "0" (dst), "r" (flags), "r" (tmp)
			);
		
	#elif MW_X86_ASM

		register u16 res=dst;

		asm {
			and status,~(ST_C|ST_O)
			add res, src
			pushfd
			pop ecx
			and ecx, 2049
			movzx ax, i86_to_9900[ecx]
			or status+1, ax
		}

	#else

	#error
	
	#endif

#endif	// FAST_X86_STATUS

#endif	// USE_STATUS_WORD

	lastval=res;
	lastcmp=0;

	return res;
}


/*
	Set laeco for subtract, preserve none
*/
u16 INLINE setst_sub_laeco(u16 dst,u16 src)
{
#if !USE_STATUS_WORD

	#if MW_X86_ASM

		register u16 res=src;

		asm {
			neg res
			setz st_c
			add res,dst
			seto st_o
			setc al
			or st_c, al
		}
		
	#else
	
		u16 res = setst_add_laeco(dst, 1+~src);
		st_c |= (src==0 || src==0x8000);	// the inverse + increment ==> carry
		
	#endif
	
#else

#if !FAST_X86_STATUS

	#if GNU_X86_ASM

		register u16 res=dst;

		status&=~(ST_C|ST_O);
		asm(
			"\t negw %0\n"
			"\t jnz 3f\n"
			"\t orb $0x10,status+1\n"
			"3:\t addw %1,%0\n"
			"\t pushf\n"
			"\t jno 1f\n"
			"\t orb $0x8,status+1\n"
			"\t1: popf\n"
			"\t jnc 2f\n"
			"\t orb $0x10,status+1\n"
			"\t2:\n"
			
			: "=r" (res)
			: "g" (dst), "0" (src)
		);

	#elif MW_X86_ASM

		register u16 res=src;

		asm {
			and status, ~(ST_C|ST_O)
			neg res
			jnz lab3
			or status, ST_C
		lab3:
			add res,dst
			pushfd
			jno lab1
			or status, ST_O
		lab1:
			popfd
			jnc lab2
			or status, ST_C
		lab2:
		}

	#else
	
		u16 res = setst_add_laeco(dst, 1+~src);
		if (src == 0 || src == 0x8000)
			status |= ST_C;
		
	#endif

#else	// FAST_X86_STATUS

	#if GNU_X86_ASM

	register u16 res=dst;
	register u32 flags;
	register u8 tmp;

	status&=~(ST_C|ST_O);
	asm(
		"\t negw %0\n"
		"\t jnz 3f\n"
		"\t orb $0x10,status+1\n"
		"3:\t addw %1,%0\n"

		"\t pushf\n"
		"\t popl %3\n"
		"\t andl $2049,%3\n"
		"\t movb i86_to_9900(%3),%4\n"
		"\t orb %4,status+1\n"
		
		: "=r" (res)
		: "g" (dst), "0" (src), "r" (flags), "r" (tmp)
	);

	#elif MW_X86_ASM

	register u16 res=src;

	asm {
		and status,~(ST_C|ST_O)
		neg res
		jnz lab3
		or status,ST_C
	lab3:
		add res,dst
		pushfd
		pop ecx
		and ecx, 2049
		movzx ax, i86_to_9900[ecx]
		or status,ax
	}
	
	#else
	#error
	#endif
	
#endif	// FAST_X86_STATUS

#endif	// USE_STATUS_WORD

	lastval=res;
	lastcmp=0;

	return res;
}


/*
	Set laeco for add, preserve none (BYTE)
*/
s8 INLINE setst_addbyte_laecop(s8 dst,s8 src)
{
#if !USE_STATUS_WORD

	#if MW_X86_ASM

		register s8 res=dst;

		asm {
			add res, src
			seto st_o
			setc st_c
			setpo st_p
		}

	#else
		s8 res = dst + src;
		
		st_c = (((dst & src) & 0x80) || 
			(((dst & 0x80) ^ (src & 0x80)) && !(res & 0x80))) != 0;
			
		st_o = (((~dst & ~src & res) | (dst & src & ~res)) & 0x80) != 0;
			
		st_p = (PARITY(res));
	
	#endif
		
#else

#if !FAST_X86_STATUS

	#if GNU_X86_ASM
	
		register s8 res=dst;

		status&=~(ST_C|ST_O|ST_P);
		asm(
			"\t addb %1,%0\n"
			
			"\t pushf\n"
			"\t jno 1f\n"
			"\t orb $0x8,status+1\n"
			"\t1: popf\n"
			"\t pushf\n"
			"\t jnc 2f\n"
			"\t orb $0x10,status+1\n"
			"\t2:\n"
			"\t popf\n"
			"\t jpe 3f\n"
			"\t orb $0x4,status+1\n"
			"\t3:\n"
			
			: "=r" (res)
			: "g" (src), "0" (dst)
		);

	#elif MW_X86_ASM

		register s8 res=dst;

		asm {
			and status,~(ST_C|ST_O|ST_P)
			add res, src
			pushfd
			jno lab1
			or status,ST_O
		lab1:
			popfd
			pushfd
			jnc lab2
			or status,ST_C
		lab2:
			popfd
			jpe lab3
			or status,ST_P
		lab3:
		}

	#else
	
		s8 res = dst + src;

		status &= ~(ST_C|ST_O|ST_P);
		
		if ((((dst & src) & 0x80) || 
			(((dst & 0x80) ^ (src & 0x80)) && !(res & 0x80))) != 0)
			status |= ST_C;
			
		if ((((~dst & ~src & res) | (dst & src & ~res)) & 0x80) != 0)
			status |= ST_O;
			
		if ((PARITY(res)))
			status |= ST_P;


	#endif

#else	// USE_STATUS_WORD

	#if GNU_X86_ASM
	
		register s8 res=dst;
		register u32 flags;
		register u8 tmp;

		status&=~(ST_C|ST_O|ST_P);
		asm(
			"\t addb %1,%0\n"
			
			"\t pushf\n"
			"\t popl %3\n"
			"\t andl $2053,%3\n"
			"\t movb i86_to_9900(%3),%4\n"
			"\t orb %4,status+1\n"
					
			: "=r" (res)
			: "g" (src), "0" (dst), "r" (flags), "r" (tmp)
		);

	#elif MW_X86_ASM

		register s8 res=dst;

		asm {
			and status,~(ST_C|ST_O|ST_P)
			add res, src
			pushfd
			pop ecx
			and ecx, 2053
			movzx ax, i86_to_9900[ecx]
			or status, ax
		}
	
	#else
	#error
	#endif
	
#endif	// FAST_X86_STATUS

#endif	// USE_STATUS_WORD

	lastval=(s16)res;
	lastcmp=0;
	
	return res;
}


/*
	Set laeco for subtract, preserve none (BYTE)
*/
s8 INLINE setst_subbyte_laecop(s8 dst,s8 src)
{
#if !USE_STATUS_WORD

	#if MW_X86_ASM

		register s8 res=src;

		asm {
			neg res
			setz st_c
			add res,dst
			seto st_o
			setpo st_p
			setc al
			or st_c, al
		}

	#else
	
		s8 res = setst_addbyte_laecop(dst, 1+~src);
		st_c |= (src==0 || (u8)src==0x80);	// the inverse + increment ==> carry

	#endif
	
#else

#if !FAST_X86_STATUS

	#if GNU_X86_ASM
	
		register s8 res=dst;
		register u32 flags;
		register u8 tmp;

		status&=~(ST_C|ST_O|ST_P);
		asm(
			"\t negb %0\n"
			"\t jnz 3f\n"
			"\t orb $0x10,status+1\n"
			"\t3:\t addb %1,%0\n"
			"\t pushf\n"
			"\t jno 1f\n"
			"\t orb $0x8,status+1\n"
			"\t1: popf\n"
			"\t pushf\n"
			"\t jnc 2f\n"
			"\t orb $0x10,status+1\n"
			"\t2:\n"
			"\t popf\n"
			"\t jpe 3f\n"
			"\t orb $0x4,status+1\n"
			"\t3:\n"

			: "=rb" (res)
			: "g" (dst), "0" (src)
		);

	#elif MW_X86_ASM

		register s8 res=src;

		asm {
			and status,~(ST_C|ST_O|ST_P)
			neg res
			jnz lab
			or status,ST_C
		lab:
			add res,dst
			pushfd 
			jno lab1
			or status,ST_O
		lab1:
			popfd
			pushfd
			jnc lab2
			or status,ST_C
		lab2:
			popfd
			jpe lab3
			or status,ST_P
		lab3:
		}

	#else
	
		s8 res = setst_addbyte_laecop(dst, 1+~src);
		if (src==0 || (u8)src==0x80)	// the inverse + increment ==> carry
			status |= ST_C;
		
	
	#endif

#else	// FAST_X86_STATUS

	#if GNU_X86_ASM
	
		register s8 res=dst;
		register u32 flags;
		register u8 tmp;

		status&=~(ST_C|ST_O|ST_P);
		asm(
			"\t negb %0\n"
			"\t jnz 3f\n"
			"\t orb $0x10,status+1\n"
			"3:\t addb %1,%0\n"
			
			"\t pushf\n"
			"\t popl %3\n"
			"\t andl $2053,%3\n"
			"\t movb i86_to_9900(%3),%4\n"
			"\t orb %4,status+1\n"

			: "=r" (res)
			: "g" (dst), "0" (src), "r" (flags), "r" (tmp)
		);
	
	#elif MW_X86_ASM

		register s8 res=src;

		asm {
			and status,~(ST_C|ST_O|ST_P)
			neg res
			jnz lab3
			or status,ST_C
		lab3:
			add res,dst
			pushfd
			pop ecx
			and ecx,2053
			movzx ax, i86_to_9900[ecx]
			or status, ax
		}
		
	#else
	#error
	#endif

#endif	// FAST_X86_STATUS

#endif	// USE_STATUS_WORD

	lastval=(s16)res;
	lastcmp=0;
	
	return res;
}


/*
	For ABS
*/
u16 INLINE setst_o(u16 val)
{
#if USE_STATUS_WORD
	status = (status & ~ST_O) | ((val == 0x8000) ? ST_O : 0);
#else
	st_o = (val==0x8000);
#endif
	return val;
}


/*
	For NEG
*/
u16 INLINE setst_laeo(u16 val)
{
	setst_o(val);
	lastval=val;
	lastcmp=0;
	return val;
}

#endif

/************************************************************************/
#include "cexit.h"

