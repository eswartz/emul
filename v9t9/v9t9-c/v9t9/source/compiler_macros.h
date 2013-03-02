/*
compiler_macros.h

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
#define __MEMORY_H__

#define THE_AREA(state, dmn, addr) (&(*(state)->map)[dmn][((addr) & (PHYSMEMORYSIZE-1)) >> AREASHIFT])

#define AREA_SETUP(state, dmn, addr) \
	mrstruct *area = THE_AREA(state, dmn, addr)

/*	These macros are for strict static access to memory.
	No routines will be called, so there will be no side effects.
	Unfortunately, though, a lot of memory looks like zeroes. */	
#define AREA_READ_WORD(area, addr) 	\
	((area)->arearead ? \
		WORD((area)->arearead, (addr & (AREASIZE-1))) : 0)
#define AREA_READ_BYTE(area, addr) 	\
	((area)->arearead ? \
		BYTE((area)->arearead, (addr & (AREASIZE-1))) : 0)
#define AREA_WRITE_WORD(area, addr, val) 	\
	do { if ((area)->areawrite) \
		WORD((area)->areawrite, (addr & (AREASIZE-1))) = val; } while (0)
#define AREA_WRITE_BYTE(area, addr, val) 	\
	do { if ((area)->areawrite) \
		BYTE((area)->areawrite, (addr & (AREASIZE-1))) = val; } while (0)

#define DOMAIN_READ_WORD(state, dmn, addr)  \
		AREA_READ_WORD(THE_AREA(state, dmn,addr), addr)
#define DOMAIN_READ_BYTE(state, dmn, addr)  \
		AREA_READ_BYTE(THE_AREA(state, dmn,addr), addr)
#define DOMAIN_WRITE_WORD(state, dmn, addr, val)  \
		AREA_WRITE_WORD(THE_AREA(state, dmn, addr), addr, val)
#define DOMAIN_WRITE_BYTE(state, dmn, addr, val)  \
		AREA_WRITE_BYTE(THE_AREA(state, dmn, addr), addr, val)

static u16 domain_read_word(CompilerState *state, mem_domain md, u32 addr)
{
	AREA_SETUP(state, md, addr);

	printf("domain_read_word: area=%p map=%p md=%d, addr=%04X, rw=%p, rd=%p, ar=%p\n",
		 area,  state->map, md, addr, area->read_word, area->read_byte, area->arearead);

	if (area->read_word)
		return area->read_word(area, addr);
	else if (area->read_byte)
		return (area->read_byte(area, addr) << 8) |
			((area->read_byte(area, addr + 1)) & 0xff);
	else if (area->arearead)
		if (md == md_cpu)
			return WORD(area->arearead, (addr & (AREASIZE - 1)));
		else {
			return (FLAT_BYTE(area->arearead, (addr & (AREASIZE - 1))) << 8) +
				FLAT_BYTE(area->arearead, ((addr+1) & (AREASIZE - 1)));
		}
	else
		return 0;
}

static void
domain_write_word(CompilerState *state, mem_domain md, u32 addr, u16 val)
{
	AREA_SETUP(state, md, addr);

	if (area->write_word)
		area->write_word(area, addr, val);
	else if (area->write_byte) {
		area->write_byte(area, addr, val >> 8);
		area->write_byte(area, addr + 1, (s8) val);
	} else if (area->areawrite)
		if (md == md_cpu)
			WORD(area->areawrite, (addr & (AREASIZE - 1))) = val;
		else {
			FLAT_BYTE(area->areawrite, (addr & (AREASIZE - 1))) = (val >> 8) & 0xff;
			FLAT_BYTE(area->areawrite, ((addr+1) & (AREASIZE - 1))) = val & 0xff;
		}
}

static s8 domain_read_byte(CompilerState *state, mem_domain md, u32 addr)
{
	AREA_SETUP(state, md, addr);

	if (area->read_byte)
		return area->read_byte(area, addr);
	else if (area->arearead)
		if (md == md_cpu)
			return BYTE(area->arearead, (addr & (AREASIZE - 1)));
		else
			return FLAT_BYTE(area->arearead, (addr & (AREASIZE - 1)));
	else
		return 0;
}

static void
domain_write_byte(CompilerState *state, mem_domain md, u32 addr, s8 val)
{
	AREA_SETUP(state, md, addr);

	if (area->write_byte)
		area->write_byte(area, addr, val);
	else if (area->areawrite)
		if (md == md_cpu)
			BYTE(area->areawrite, (addr & (AREASIZE - 1))) = val;
		else
			FLAT_BYTE(area->areawrite, (addr & (AREASIZE - 1))) = val;
}

#if 1
#define MEMORY_READ_WORD(addr) 		DOMAIN_READ_WORD(state, md_cpu, addr)
#define MEMORY_READ_BYTE(addr) 		DOMAIN_READ_BYTE(state, md_cpu, addr)
#define MEMORY_WRITE_WORD(addr, val) DOMAIN_WRITE_WORD(state, md_cpu, addr, val)
#define MEMORY_WRITE_BYTE(addr, val) DOMAIN_WRITE_BYTE(state, md_cpu, addr, val)
#else
#define MEMORY_READ_WORD(addr) 		domain_read_word(state, md_cpu, addr)
#define MEMORY_READ_BYTE(addr) 		domain_read_byte(state, md_cpu, addr)
#define MEMORY_WRITE_WORD(addr, val) domain_write_word(state, md_cpu, addr, val)
#define MEMORY_WRITE_BYTE(addr, val) domain_write_byte(state, md_cpu, addr, val)
#endif

#define REGADDR(r)			((state->wp+((r)<<1))&0xfffe)
#define REGREADWORD(r) 		MEMORY_READ_WORD(REGADDR(r))
#define REGWRITEWORD(r,v) 	MEMORY_WRITE_WORD(REGADDR(r),v)
#define REGREADBYTE(r) 		MEMORY_READ_BYTE(REGADDR(r))
#define REGWRITEBYTE(r,v) 	MEMORY_WRITE_BYTE(REGADDR(r),v)

#define READWORD(a) 		state->read_word((a)&0xfffe)
#define READBYTE(a) 		state->read_byte((a)&0xffff)
#define WRITEWORD(a,v) 		state->write_word((a)&0xfffe,v)
#define WRITEBYTE(a,v) 		state->write_byte((a)&0xffff,v)

#define REGINC(r,v)			REGWRITEWORD(r, REGREADWORD(r)+(v))

///////////////////////////////////

// status word masks
#define ST_L		0x8000
#define ST_A		0x4000
#define	ST_E		0x2000
#define	ST_C		0x1000
#define	ST_O		0x800
#define ST_P		0x400
#define ST_X		0x200
#define ST_INTLEVEL 0xf

#define write_stat(s) state->stat=T9900tostatus(s,&lastval,&lastcmp)
#define read_stat() statusto9900(state->stat,lastval,lastcmp)

/*
	LAE bits maintained in lastcmp/lastval.
	
	ALWAYS, lastcmp is 0<=lastcmp<=0xffff.
	ALWAYS, status has 0xf mask for interrupt level, ST_X for XOP, etc.
*/
INLINE u16 statusto9900(u16 status, u16 lastval, u16 lastcmp)
{
	status=(status&~(ST_L|ST_E|ST_A)) |
			( (u16)lastval > (u16)lastcmp ? ST_L : 0) |
			( (s16)lastval > (s16)lastcmp ? ST_A : 0) |
			(lastval==lastcmp ? ST_E : 0);
	return status;
}

INLINE u16 T9900tostatus(u16 status, u16 *lastval, u16 *lastcmp)
{
	*lastval=*lastcmp=0;
	if (!(status&ST_E)) {
		if (!(status&(ST_L|ST_A)))
			(*lastcmp)++;
		else {
			(*lastval)++;
			if (!(status&ST_L))
				(*lastcmp)=0xffff;
			else
				if (!(status&ST_A))
					(*lastval)=(-(*lastval))&0xffff;
		}
	}
	return status;
}


static u8 parity[16] = { 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0};
   
// determine parity of an 8-bit byte, 0=even, 1=odd
#define PARITY(val) ((parity[(val & 0xf)] ^ parity[(val >> 4)&0xf]) & 1)

/*
	Set lae, preserve C and O
*/
#define setst_lae(val) do { lastcmp=0, lastval=(val); }while(0)

/*
	Set lae, preserve C and O (BYTE)
*/
#define setst_byte_laep(val) do { \
	s8 v = (val); if (PARITY(v)) state->stat|=ST_P; else state->stat&=~ST_P; \
	lastval=v; lastcmp=0; } while(0)

/*
	For COC, CZC, and TB
*/
#define setst_e(val,to) do { \
	if ((val)==(to)) { lastcmp=lastval=0; } \
	else { if (lastcmp==lastval) lastval++; } } while(0)

/*
	Set laeco for add, preserve none
*/
#define setst_add_laeco(dst,src) do { \
	u16 res=dst+src; \
	state->stat&=~(ST_C|ST_O); \
	if ( (((dst & src) & 0x8000) ||  \
		(((dst & 0x8000) ^ (src & 0x8000)) && !(res & 0x8000))) != 0) \
		state->stat |= ST_C; \
	if ( (((~dst & ~src & res) | (dst & src & ~res)) & 0x8000) != 0) \
		state->stat |= ST_O; \
	lastval = res; lastcmp = 0; } while(0)

/*
	Set laeco for subtract, preserve none
*/
#define setst_sub_laeco(dst,src) do { \
	setst_add_laeco(dst, 1+~src); \
	if (src==0 || src==0x8000) state->stat|=ST_C; } while(0)

/*
	Set laeco for add, preserve none (BYTE)
*/
#define setst_add_byte_laecop(dst,src) do { \
	s8 res = dst+src; \
	state->stat&=~(ST_C|ST_O|ST_P); \
	if ( (((dst & src) & 0x80) ||  \
		(((dst & 0x80) ^ (src & 0x80)) && !(res & 0x80))) != 0) \
		state->stat |= ST_C; \
	if ( (((~dst & ~src & res) | (dst & src & ~res)) & 0x80) != 0) \
		state->stat |= ST_O; \
	if (PARITY(res)) state->stat |= ST_P; \
	lastval = (s8)res; lastcmp = 0; } while(0)

/*
	Set laeco for subtract, preserve none (BYTE)
*/
#define setst_sub_byte_laecop(dst,src) do { \
	setst_add_byte_laecop(dst, 1+~src); \
	if (src==0 || (u8)(src)==0x80) state->stat|=ST_C; } while(0)

/*
	For ABS
*/
#define setst_o(b) do { \
	if (b) state->stat|=ST_O; \
	else state->stat&=~ST_O; } while(0)

/*	
	For CMP
*/
#define setst_cmp(a,b) do { lastval=a; lastcmp=b; } while(0)

/*
	Right shift carries
*/
#define setst_shift_right_c(a,c) do { \
	u16 mask= c ? (1 << (c-1)) : 0; \
	if (a & mask) state->stat |= ST_C; else state->stat &= ~ST_C; } while(0)

/*
	Left shift overflow & status
*/
#define setst_sla_co(a,c) do { \
	u16 mask = 0x10000 >> c; \
	if (a & mask) state->stat |= ST_C; else state->stat &= ~ST_C; \
	if ((a ^ ((a)<<c)) & 0x8000) state->stat |= ST_O; else state->stat &= ~ST_O; \
	} while(0)

/*
	For XOP
*/
#define setst_x() do { \
	state->stat|=ST_X; } while(0)

#define test_lt() ((s16)lastval < (s16)lastcmp)
#define test_le() ((u16)lastval <= (u16)lastcmp)
#define test_l() ((u16)lastval < (u16)lastcmp)
#define test_eq() (lastval == lastcmp)
#define test_ne() (lastval != lastcmp)
#define test_he() ((u16)lastval >= (u16)lastcmp)
#define test_gt() ((s16)lastval > (s16)lastcmp)
#define test_he() ((u16)lastval >= (u16)lastcmp)
#define test_h() ((u16)lastval > (u16)lastcmp)
#define test_c() ((state->stat & ST_C)!=0)
#define test_o() ((state->stat & ST_O)!=0)
#define test_p() ((state->stat & ST_P)!=0)


