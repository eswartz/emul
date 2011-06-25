/*
  9900asm.h						-- asm implementations of status word routines

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

#include "centry.h"


u16 INLINE swpb(u16 val)
{
#if MW_X86_ASM

	asm {
		ror val, 8
	}
	return val;
	
#else

	union swapstruc
	{
		struct
		{
			u8 hi,lo;
		}	b;
		u16	val;
	}	v;
	u8 tmp;
	
	v.val = val;
	tmp=v.b.hi;
	v.b.hi=v.b.lo;
	v.b.lo=tmp;
	return v.val;
	
#endif

}

/*
	Meat of SRA
*/
u16 INLINE setst_sra_laec(s16 a,u16 c)
{
#if !USE_STATUS_WORD

	#if MW_X86_ASM

		asm {
			mov cl, byte ptr c
			sar a, cl
			setc st_c
		}
		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#else

		u16 mask= c ? (1 << (c-1)) : 0;
		
		st_c = (a & mask) != 0;
		
		a >>= c;

		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#endif
	
#else

	#if GNU_X86_ASM
	
		register u16 ans asm ("ax")=a;
		register u8 cnt asm ("cl");
		
		cnt=c;
		status&=~ST_C;
		asm(
			"\t sarw %1,%0\n"
			"\t jnc 2f\n"
			"\t orb $0x10,status+1\n"
			"\t2:\n"
			: "=r" (ans)
			: "r" (cnt), "r" (ans)
			: "cc", "ax"
		);
		lastval=ans;
		lastcmp=0;
		
		return setst_lae(ans);

	#elif MW_X86_ASM

		asm {
			and status,~ST_C
			mov cl, byte ptr c
			sar a, cl
			jnc lab
			or status,ST_C
		lab:
		}
		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#else

		u16 mask= c ? (1 << (c-1)) : 0;
		
		if (a & mask)
			status |= ST_C;
		else
			status &= ~ST_C;
		
		a >>= c;

		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);
	
	#endif
	
#endif
}


/*
	Meat of SRL
*/
u16 INLINE setst_srl_laec(u16 a,u16 c)
{
#if !USE_STATUS_WORD

	#if MW_X86_ASM 
	
		asm {
			mov cl, byte ptr c
			shr a, cl
			setc st_c
		}
		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#else
	
		u16 mask = c ? (1 << (c-1)) : 0;

		st_c = (a & mask) != 0;
			
		a >>= c;

		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#endif
	
#else

	#if GNU_X86_ASM
	
		register u16 ans asm ("ax")=a;
		register u8 cnt asm ("cl");
		
		cnt=c;
		status&=~ST_C;
		asm(
			"\t shrw %1,%0\n"
			"\t jnc 2f\n"
			"\t orb $0x10,status+1\n"
			"\t2:\n"
			: "=r" (ans)
			: "r" (cnt), "r" (ans)
			: "cc", "ax"
		);
		lastval=ans;
		lastcmp=0;
		
		return setst_lae(ans);

	#elif MW_X86_ASM 
	
		asm {
			and status,~ST_C
			mov cl, byte ptr c
			shr a, cl
			jnc lab
			or status,ST_C
		lab:
		}
		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#else

		u16 mask = c ? (1 << (c-1)) : 0;

		if (a & mask)
			status |= ST_C;
		else
			status &= ~ST_C;
			
		a >>= c;

		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#endif
	
#endif
}


/*
	Meat of SRC
*/
u16 INLINE setst_src_laec(u16 a,u16 c)
{
#if !USE_STATUS_WORD

	#if MW_X86_ASM 
	
		asm {
			mov cl, byte ptr c
			ror a, cl
			setc st_c
		}
		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#else

		u16 mask = c ? (1 << (c-1)) : 0;

		st_c = (a & mask) != 0;
			
		a = (a >> c) | (a << (16-c));

		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#endif
	
#else

	#if GNU_X86_ASM
	
		register u16 ans asm ("ax")=a;
		register u8 cnt asm ("cl");
		
		cnt=c;
		status&=~ST_C;
		asm(
			"\t rorw %1,%0\n"
			"\t jnc 2f\n"
			"\t orb $0x10,status+1\n"
			"\t2:\n"
			: "=r" (ans)
			: "r" (cnt), "r" (ans)
			: "cc", "ax"
		);
		lastval=ans;
		lastcmp=0;
		
		return setst_lae(ans);

	#elif MW_X86_ASM

		asm {
			and status,~ST_C
			mov cl, byte ptr c
			ror a, cl
			jnc lab
			or status,ST_C
		lab:
		}
		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);
	
	#else

		u16 mask = c ? (1 << (c-1)) : 0;

		if (a & mask)
			status |= ST_C;
		else	
			status &= ~ST_C;
			
		a = (a >> c) | (a << (16-c));

		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#endif
	
#endif
}


/*
	Meat of SLA
*/
u16 INLINE setst_sla_laeco(u16 a,u16 c)
{
#if !USE_STATUS_WORD

	#if MW_X86_ASM
	
		asm {
			mov cl, byte ptr c
			sal a, cl
			seto st_o
			setc st_c
		}
		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);
	
	#else
	
		u16 mask = 0x10000 >> c;
		u16 oa;

		st_c = (a & mask) != 0;

		oa = a;
		a <<= c;

		st_o = ((oa ^ a) & 0x8000) != 0;
			
		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#endif
	
#else

	#if GNU_X86_ASM
	
		register u16 ans asm ("ax")=a;
		register u8 cnt asm ("cl");
		
		cnt=c;
		status&=~(ST_C|ST_O);
		asm(
			"\t salw %1,%0\n"
			"\t pushf\n"
			"\t jno 1f\n"
			"\t orb $0x8,status+1\n"
			"\t1: popf\n"
			"\t jnc 2f\n"
			"\t orb $0x10,status+1\n"
			"\t2:\n"
			: "=r" (ans)
			: "r" (cnt), "r" (ans)
			: "cc", "ax"
		);
		lastval=ans;
		lastcmp=0;
		
		return setst_lae(ans);

	#elif MW_X86_ASM
	
		asm {
			and status,~(ST_C|ST_O)
			mov cl, byte ptr c
			sal a, cl
			pushfd
			jno lab
			or status,ST_O
		lab:
			popfd
			jnc lab2
			or status,ST_C
		lab2:
		}
		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#else

		u16 mask = 0x10000 >> c;
		u16 oa;

		if (a & mask)
			status |= ST_C;
		else
			status &= ~ST_C;

		oa = a;
		a <<= c;

		if ((oa ^ a) & 0x8000)
			status |= ST_O;
		else
			status &= ~ST_O;
			
		lastval=a;
		lastcmp=0;
		
		return setst_lae(a);

	#endif
	
#endif
}

#include "cexit.h"


