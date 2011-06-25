/*
  emulate.c						-- main emulator loop (execute/interrupt/state)

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

#define __EMULATE__

#include <stdio.h>
#include <signal.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <setjmp.h>
#include <ctype.h>
#if __MWERKS__
#include <time.h>
#else
#include <sys/time.h>
#include <sys/types.h>
#endif

#include "v9t9_common.h"
#include "v9t9.h"
#include "9900.h"
#include "grom.h"
#include "vdp.h"
#include "video.h"
#include "sound.h"
#include "cru.h"
#include "keyboard.h"
#include "emulate.h"
#include "timer.h"
#include "debugger.h"
#include "memory.h"
#include "command.h"
#include "system.h"
#include "9900st.h"
#include "opcode_callbacks.h"
#include "speech.h"
#include "demo.h"
#include "compiler.h"

#define _L	 LOG_CPU | LOG_INFO

u32 features;
u32 stateflag;


/*
 *	The emulator functions from the perspective of
 *	a 3 MHz clock, which drives the instruction speed of
 *	the processor, and secondarily, the 9901 timer, which
 *	is derived from it.
 */

/*	Variables for controlling a "real time" emulation of the 9900
	processor.  Each call to execute() sets an estimated cycle count
	for the instruction and parameters in "instcycles".  We waste
	time in 1/BASE_EMULATOR_HZ second quanta to maintain the appearance of a
	3.0 MHz clock. */

int         realtime = 0;
u32         baseclockhz = 3300000;

u32         instcycles;	// cycles for each instruction

u64         targetcycles;	// target # cycles to be executed per second
u64         totaltargetcycles;	// total # target cycles expected
u64         currentcycles = 0;	// current cycles per 1/BASE_EMULATOR_HZ second
u64         totalcurrentcycles;	// total # current cycles executed

u32         currenttime;	/* time, [0..baseclockhz / BASE_EMULATOR_HZ) */
u64         totalticks;	/* total number of BASE_EMULATOR_HZ events passed */

u32         executed;		/* # instructions executed in this tick */
u64         totalexecuted;	/* total # executed */

/*	Old way of controlling time */

unsigned long delaybetweeninstructions = 0;


u64         delaycycles = 0;	// delay cycles used to maintain time per 1/BASE_EMULATOR_HZ

int 	allow_debug_interrupts = 0;	// interrupt processor while debugging
int		lockup_sanity_check = 1;	// check and abort when locked up
int		lockup_sanity_count = 0;	// 1/60 seconds locked up

///////////

static void 
inserthacks(void)
{
	if (0 && MEMORY_READ_WORD(0x394) == 0x20C) {
		AREA_SETUP(md_cpu, 0x2B2);
		logger(_L | L_1, _("inserting keyboard delay hack\n"));
		WORD(area->arearead, (0x2B2 & (AREASIZE-1))) = OP_KEYSLOW;
		WORD(area->arearead, (0x2B4 & (AREASIZE-1))) = 0x1000; // NOP
	}
}

#if 0
static sigjmp_buf bogocalc;
static void
stop_bogo(int unused)
{
	siglongjmp(bogocalc, 1);
}

static void
calibrate_processor(void)
{
	/* Calibrate processor. */
	unsigned char flag = 0;
	long long   cnt;
	int         tmtag = TM_UniqueTag();
	int         killtimer = 0;
	int         origtime;

	logger(_L | LOG_USER, _("Calibrating processor... "));

	stateflag = 0;

	killtimer = TM_Start();
	origtime = TM_GetTicks();
	while (origtime == TM_GetTicks());
	if (sigsetjmp(bogocalc, ~0) == 0) {
		TM_SetEvent(tmtag, TM_HZ * BASE_EMULATOR_HZ, 0, TM_FUNC, stop_bogo);
		cnt = ~0;
		while (--cnt && !stateflag);
	}
	TM_ResetEvent(tmtag);
	if (killtimer)
		TM_Stop();

	bogocycles = (~cnt) / BASE_EMULATOR_HZ;
	logger(_L | LOG_USER |  0, _("%Ld bogocycles.\n\n"), bogocycles);
}
#endif

/*************************************************/

static bool calibrated_processor;

static vmResult emulate9900_restart(void);
static vmResult emulate9900_restop(void);


static
DECL_SYMBOL_ACTION(emulate_reset_computer)
{
	/* Upon restart, save volatile RAMs and reload all ROMs,
	   in case they were overwritten by a bug or something.  ;) */
	if (task == csa_WRITE) {
		memory_volatile_save();
		memory_complete_load();
		stateflag &= ~ST_INTERRUPT;
		reset9901();
		init9900();
		hold9900pin(INTPIN_RESET);
		contextswitch(0);
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(emulate_debugger_enable)
{
	if (task == csa_READ) {
		if (iter)
			return 0;
		command_arg_set_num(SYM_ARG_1st, debugger_enabled());
	} else {
		int val;
		command_arg_get_num(SYM_ARG_1st, &val);
		debugger_enable(val);
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(emulate_execution_pause)
{
	if (task == csa_READ) {
		if (iter)
			return 0;
		command_arg_set_num(SYM_ARG_1st, execution_paused());
	} else {
		int val;
		command_arg_get_num(SYM_ARG_1st, &val);
		execution_pause(val);
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(emulate_single_step)
{
//	execution_pause(true);
//	execution_pause(false);
	stateflag |= ST_SINGLESTEP;
	return 1;
}

static
DECL_SYMBOL_ACTION(emulate_change_clock_speed)
{
	if (task == csa_WRITE) {
		emulate9900_restart();
	}
	return 1;
}

static 
DECL_SYMBOL_ACTION(emulate_set_pc)
{
	int val;
	if (task == csa_READ)
		if (!iter) {
			command_arg_set_num(sym->args, pc);
			return 1;
		} else {
			return 0;
		}
	
	command_arg_get_num(sym->args, &val);
	if (1 || verifypc(val)) {
		setandverifypc(val);
	} else {
		command_logger(_L | LOG_ERROR, _("Cannot set PC to %04X, ignoring\n"), val);
	}
	return 1;
}

static 
DECL_SYMBOL_ACTION(emulate_set_wp)
{
	int val;

	if (task == csa_READ)
		if (!iter) {
			command_arg_set_num(sym->args, wp);
			return 1;
		} else {
			return 0;
		}

	command_arg_get_num(sym->args, &val);
	if (1 || verifywp(val)) {
		setandverifywp(val);
	} else {
		command_logger(_L | LOG_ERROR, _("Cannot set WP to %04X, ignoring\n"), val);
	}
	return 1;
}

static 
DECL_SYMBOL_ACTION(emulate_set_st)
{
	int val;
	if (task == csa_READ)
		if (!iter) {
			statusto9900();
			command_arg_set_num(sym->args, status);
			return 1;
		} else {
			return 0;
		}

	command_arg_get_num(sym->args, &val);
	
	T9900tostatus(val);
	change9900intmask(status & 0xf);
	return 1;
}

#define HEXNUM(x) (((x)>='0' && (x)<='9') ? (x)-'0' : tolower(x)-'a'+10)
#define NUMHEX(x)  ((x) < 10 ? (x)+'0' : (x)-10+'A')

//	Utility function to make a hex string for machine_state routines
//	If 'hex' is NULL, we xmalloc() space and return a pointer to it,
//	else the buffer at 'hex' should have size*2+1 bytes available.
char	*emulate_bin2hex(u8 *mem, char *hex, int size)
{
	char *str;

	if (!hex) hex = (char *)xmalloc(size*2+1);
	str = hex;
	while (size--) {
		u8 byt = *mem++;
		*str++ = NUMHEX(byt>>4);
		*str++ = NUMHEX(byt&0xf);
	}
	*str = 0;
	return hex;
}

//	Utility for converting hex string to binary.
//	'size' is the upper limit on the expected size of the
//	string.  If 'hex' is smaller, we zero-fill the memory.
void emulate_hex2bin(char *hex, u8 *mem, int size)
{
	while (*hex && *(hex+1) && size) {
		u8 byt = (HEXNUM(*hex) << 4) + HEXNUM(*(hex+1));
		hex += 2;
		*mem++ = byt;
		size--;
	}
	while (size--) {
		*mem++ = 0;
	}
}

/*
 *	Attempt to RLE compress 'len' bytes of 'data' 
 *	into 'compress' but not using more than 'maxlen' bytes.
 *
 *	Returns length of 'compress' or 0 if compression failed.
 */
int rle_compress(u8 *data, int len, u8 *compress, int maxlen)
{
	u8 curseglen;
	u8 curtok;
	int curlen = 0;
	
	while (len && curlen < maxlen) {
		curtok = *data;
		
		// use 0xc0-0xff as repeat counts 1-64
		curseglen = 1;
		while (curseglen < len && 
			   data[curseglen] == curtok && 
			   curseglen < 64) 
		{
			curseglen++;
		}

		// due to using 0xc0 through 0xff as repeat counts, we must
		// allow a repeat of one for these tokens themselves.
		// However, we can only use RLE when there are two bytes 
		// available in the output buffer.
		if ((curtok >= 0xc0 || curseglen > 2) && curlen <= maxlen - 2) {
			*compress++ = 0xC0 + curseglen - 1;
			*compress++ = curtok;
			curlen += 2;
		} else {
			*compress++ = curtok;
			curlen++;
			curseglen = 1;
		}
		data += curseglen;
		len -= curseglen;
	}
	
	// whoops, ran out of output space before finishing the input
	if (len || (len == 0 && curlen == maxlen))
		return 0;
	else
		return curlen;
}

/*
 *	Attempt to RLE uncompress 'len' bytes of 'data' 
 *	into 'uncompress' using at most 'maxlen' bytes.
 *
 *	Returns zero if uncompressed data cannot fit, else 
 *	the length of that data.
 */
int rle_uncompress(u8 *data, int len, u8 *uncompress, int maxlen)
{
	u8 curseglen;
	u8 curtok;
	int curlen = 0;
	
	while (len && curlen < maxlen) {
		curtok = *data++; len--;
		
		// a repeat count and available data?
		// 0xc0==1, and 0xff==64
		if (curtok >= 0xC0 && len > 0) {
			curseglen = curtok - 0xC0 + 1;
			
			curtok = *data++; len--;
				
			memset(uncompress, curtok, curseglen > (maxlen - curlen) ? 
								(maxlen - curlen) : curseglen);
			uncompress += curseglen;
			curlen += curseglen;
		} else {
			*uncompress++ = curtok;
			curlen++;
		}
	}

	// unused compressed data?	
	if (len)
		return 0;
	else
		return curlen;
}

char emulate_stringize_mem_type(mem_domain md)
{
	switch (md)
	{
	case md_cpu: return 'C';
	case md_video: return 'V';
	case md_graphics: return 'G';
	case md_speech: return 'S';
	}
	return '?';
}

mem_domain emulate_parse_mem_type(const char *typ, bool *rom)
{
	mem_domain dmn = -1;

	if (!*typ) return dmn;
	if (rom) *rom = false;

	if (toupper(*typ) == 'C' || *typ == '>')
		dmn = md_cpu;
	else if (toupper(*typ) == 'V')
		dmn = md_video;
	else if (toupper(*typ) == 'G')
		dmn = md_graphics;
	else if (toupper(*typ) == 'S')	
		dmn = md_speech;
	else {
		command_logger(_L | LOG_ERROR | LOG_USER, _("Can't access memory type '%s'\n"), typ);
	}

	if (toupper(typ[1]) == 'R')
		if (rom) *rom = true;

	return dmn;
}

static
DECL_SYMBOL_ACTION(emulate_set_mem)
{
	char *str, *typ;
	int addr;
	char hexstr[256];		
	char memtyp[4];
	mem_domain dmn;
	u8 vector[64];
	u8 rle[64];
	int vectorlen, rlelen, idx;
	bool force;

	if (task == csa_READ) {
		static int saveaddr;
		u8 verify[64];
		
		/* Write memory to a hex string, CPU RAM followed by VDP RAM */
		if (!iter)
			saveaddr = 0;

		addr = saveaddr;

		// spit out 64 bytes at a time
		if (addr < 65536) {

			/* Note: this may not seem right, when we could have a
			 *	MemoryEntry for a MEMENT_STORED RAM block covering
			 *	this area already, but this preserves the state of
			 *	that RAM at the time of the session being saved.  */
			mrstruct  *mrarea;
			while (!HAS_RAM_ACCESS(md_cpu, addr))
				addr = (addr & ~4095) + 4096;
			dmn = md_cpu;
			mrarea = THE_AREA(md_cpu, addr);
			memtyp[0] = 'C';
			memtyp[1] = 0;		// not compressed
			memtyp[2] = 0;
		} else if (addr < 65536 + 16384) {
			addr -= 65536;
			memtyp[0] = 'V';
			memtyp[1] = 0;		// not compressed
			memtyp[2] = 0;
			dmn = md_video;
		} else {
			return 0;
		}

		command_arg_set_num(sym->args->next, addr);

		/* Read 64 bytes from memory */
		vectorlen = 0;
		do {
			vector[vectorlen++] = domain_read_byte(dmn, addr);
			addr++;
		} while (addr & 63);
		
		/* RLE compress the vector */
		rlelen = rle_compress(vector, vectorlen, rle, sizeof(rle));
		
		/* Returns zero if it couldn't compress any */
		if (rlelen) {
			memtyp[1] = '*';	// compressed

			rle_uncompress(rle, rlelen, verify, sizeof(verify));
			if (memcmp(vector, verify, vectorlen)) {
				command_logger(_L|LOG_ABORT, _("Mismatched on RLE compress:\n"
					   "In : %s\n"
					   "Out: %s\n"
					   "Ver: %s\n"),
					   emulate_bin2hex(vector, 0L, vectorlen),
					   emulate_bin2hex(rle, 0L, rlelen),
					   emulate_bin2hex(verify, 0L, sizeof(verify)));
			}
		} else {
			rlelen = vectorlen;
			memcpy(rle, vector, rlelen);
		}
		
		str = hexstr;
		idx = 0;
		while (idx < rlelen) {
			u8 byt;

			byt = rle[idx++];
			
			*str++ = NUMHEX(byt>>4);
			*str++ = NUMHEX(byt&0xf);
		}

		*str = 0;
		saveaddr = addr + (dmn == md_video ? 65536 : 0);
		
		command_arg_set_string(sym->args, memtyp);
		command_arg_set_string(sym->args->next->next, hexstr);
		return 1;
	}

	// write memory
	
	command_arg_get_string(sym->args, &typ);
	command_arg_get_num(sym->args->next, &addr);
	addr &= 0xffff;
	command_arg_get_string(sym->args->next->next, &str);

	// get memory type
	dmn = emulate_parse_mem_type(typ, &force);
	if ((int)dmn < 0)
		return 0;

	// decode string, assuming it's rle-encoded
	rlelen = 0;
	
	while (*str) {
		u8 byt = (HEXNUM(*str) << 4) + HEXNUM(*(str+1));
		rle[rlelen++] = byt;
		str+=2;
	}

	// second char of memory type indicates compression
	if (typ[1] && typ[1] == '*') {
		vectorlen = rle_uncompress(rle, rlelen, vector, sizeof(vector));
	} else {
		memcpy(vector, rle, rlelen);
		vectorlen = rlelen;
	}

	idx = 0;
	while (idx < vectorlen) {
		domain_write_byte(dmn, addr, vector[idx++]);
		addr++;
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(emulate_read_mem)
{
	char *typ;
	int addr;
	int bytes, shift;
	u32 ret;

	mem_domain dmn;

	if (task == csa_WRITE) {

//		if (iter) return 0;

		command_arg_get_string(SYM_ARG_1st, &typ);
		command_arg_get_num(SYM_ARG_2nd, &addr);
		addr &= 0xffff;
		command_arg_get_num(SYM_ARG_3rd, &bytes);
		if (bytes > 4) bytes = 4;
		shift = (bytes - 1) * 8;

		if ((int)(dmn = emulate_parse_mem_type(typ, 0L)) < 0)
			return 0;

		ret = 0;
		while (bytes > 0) {
			ret |= (domain_read_byte(dmn, addr) & 0xff) << shift;
			addr++;
			bytes--;
			shift -= 8;
		}
		command_arg_set_num(sym->ret, ret);
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(emulate_write_mem)
{
	char *typ;
	int addr;
	int bytes, shift;
	u32 val;
	bool force;

	mem_domain dmn;

	if (task == csa_WRITE) {
	
		command_arg_get_string(SYM_ARG_1st, &typ);
		command_arg_get_num(SYM_ARG_2nd, &addr);
		addr &= 0xffff;
		command_arg_get_num(SYM_ARG_3rd, &bytes);
		if (bytes > 4) bytes = 4;
		shift = (bytes - 1) * 8;
		command_arg_get_num(SYM_ARG_4th, (int *)&val);

		if ((int)(dmn = emulate_parse_mem_type(typ, &force)) < 0)
			return 0;

		while (bytes > 0) {
			domain_write_byte(dmn, addr, (val >> shift) & 0xff);
			addr++;
			bytes--;
			shift -= 8;
		}
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(emulate_bsave)
{
	char *filename;
	char *typ;
	int addr, eaddr;
	int bytes;
	u8 *mem;
	int offs;
	int ret = 1;

	mem_domain dmn;

	if (task == csa_WRITE) {

		command_arg_get_string(SYM_ARG_1st, &filename);
		command_arg_get_string(SYM_ARG_2nd, &typ);
		command_arg_get_num(SYM_ARG_3rd, &addr);
		addr &= 0xffff;
		command_arg_get_num(SYM_ARG_4th, &eaddr);
		if (eaddr < 0) eaddr = 0;
		if (eaddr > 0x10000) eaddr=0x10000;
		bytes = eaddr - addr;

		if (bytes < 0) {
			logger(_L|LOG_USER|LOG_ERROR, _("end address (>%04X) less than start address (>%04X)"),
				   eaddr, addr);
			return 0;
		}
		if (bytes == 0) {
			logger(_L|LOG_USER|LOG_WARN, _("end address (>%04X) equals start address (>%04X)"),
				   eaddr, addr);
		}

		mem = xmalloc(bytes);

		if ((int)(dmn = emulate_parse_mem_type(typ, 0L)) < 0)
			return 0;

		offs = 0;
		while (offs < bytes) {
			mem[offs++] = domain_read_byte(dmn, addr++) & 0xff;
		}

		ret = data_save_binary("binary image", datapath, filename, 0, mem,
							   0/*swap*/, bytes);
		xfree(mem);
	}
	return ret;
}

static
DECL_SYMBOL_ACTION(emulate_bload)
{
	char *filename;
	char *typ;
	int addr, eaddr;
	int bytes;
	u8 *mem;
	int offs;
	int ret = 1;

	mem_domain dmn;

	if (task == csa_WRITE) {

		command_arg_get_string(SYM_ARG_1st, &filename);
		command_arg_get_string(SYM_ARG_2nd, &typ);
		command_arg_get_num(SYM_ARG_3rd, &addr);
		addr &= 0xffff;
		command_arg_get_num(SYM_ARG_4th, &eaddr);
		if (eaddr < 0) eaddr = 0;
		if (eaddr > 0x10000) eaddr=0x10000;
		bytes = eaddr - addr;

		if (bytes < 0) {
			logger(_L|LOG_USER|LOG_ERROR, _("end address (>%04X) less than start address (>%04X)"),
				   eaddr, addr);
			return 0;
		}
		if (bytes == 0) {
			bytes = 65536 - addr;
		}

		mem = xmalloc(bytes);

		if ((int)(dmn = emulate_parse_mem_type(typ, 0L)) < 0)
			return 0;

		bytes = data_load_binary("binary image", datapath, filename, 
							   mem, 0/*swap*/, 0, bytes, bytes);
		if (!bytes)
			return 0;

		offs = 0;
		while (offs < bytes) {
			domain_write_byte(dmn, addr++, mem[offs++]);
		}

		xfree(mem);
	}
	return ret;
}

static
DECL_SYMBOL_ACTION(emulate_zap_memory)
{
	int addr;
	mem_domain dmn;

	for (dmn = md_cpu; dmn <= md_speech; dmn++) {
		for (addr = 0; addr < 0x10000; addr+=2) {
			domain_write_byte(dmn, addr, 0xAA);
			domain_write_byte(dmn, addr+1, 0x55);
		}
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(do_vdp_mmio_set_addr)
{
	int num;

	if (task == csa_READ) {
		if (iter) return 0;
		command_arg_set_num(SYM_ARG_1st, vdp_mmio_get_addr());
		return 1;
	} else {
		command_arg_get_num(SYM_ARG_1st, &num);
		vdp_mmio_set_addr(num);
		return 1;
	}
}

static
DECL_SYMBOL_ACTION(do_grom_mmio_set_addr)
{
	int num;

	if (task == csa_READ) {
		if (iter) return 0;
		command_arg_set_num(SYM_ARG_1st, grom_mmio_get_addr());
		return 1;
	} else {
		command_arg_get_num(SYM_ARG_1st, &num);
		grom_mmio_set_addr(num);
		return 1;
	}
}

static
DECL_SYMBOL_ACTION(do_speech_mmio_set_addr)
{
	int num;

	if (task == csa_READ) {
		if (iter) return 0;
		command_arg_set_num(SYM_ARG_1st, speech_mmio_get_addr());
		return 1;
	} else {
		command_arg_get_num(SYM_ARG_1st, &num);
		speech_mmio_set_addr(num);
		return 1;
	}
}

/***************************************/

static int  emulate_tag, slowdown_tag;

void        EmulateEvent(void);

static      vmResult
emulate9900_detect(void)
{
	return vmOk;
}

static      vmResult
emulate9900_init(void)
{
	command_symbol_table *internal =
		command_symbol_table_new(_("Internal Emulator Commands"),
								 _("These options affect the mechanics of 99/4A emulation"),

	 command_symbol_new
		 ("RealTimeEmulation",
		  _("Toggle real-time emulation mode (attempts to operate at the "
		  "same speed of the original 9900)"),
		  c_STATIC,
		   emulate_change_clock_speed,
		  RET_FIRST_ARG,
		  command_arg_new_enum
		    ("off|on",
			 _("on:  execute at 9900 speed; "
			 "off:  execute with DelayBetweenInstructions"),
			 NULL,
			 ARG_NUM(realtime),
			 NULL /* next */ )
		  ,

	  command_symbol_new
		  ("DelayBetweenInstructions",
		   _("Sets a constant delay between instructions (when not in real-time mode)"),
		   c_STATIC,
		   NULL /* action */ ,
		   RET_FIRST_ARG,
		   command_arg_new_num
		     (_("cycles"),
			  _("number of cycles to count"),
			  NULL,
			  ARG_NUM(delaybetweeninstructions),
			  NULL /* next */ )
		   ,

	  command_symbol_new
		  ("ResetComputer",
		   _("Resets the 99/4A via RESET"),
		   c_DONT_SAVE,
		   emulate_reset_computer,
		   NULL /* ret */ ,
		   NULL	/* args */
	  ,

	  command_symbol_new
		  ("PauseComputer|Pause",
		   _("Pauses emulation of the 99/4A"),
		   c_DONT_SAVE,
		   emulate_execution_pause /* action */ ,
		   RET_FIRST_ARG,
		   command_arg_new_enum
		     ("off|on", 
			  NULL /* help */,
			  NULL /* action */ ,
			  NEW_ARG_NUM(bool),
			  NULL /* next */ )
		   ,

	  command_symbol_new
		  ("Debugger",
		   _("Enable the debugger/tracer"),
		   c_DYNAMIC,
		   emulate_debugger_enable /* action */ ,
		   RET_FIRST_ARG,
		   command_arg_new_enum
		     ("off|on", 
			  NULL /* help */,
			  NULL /* action */ ,
			  NEW_ARG_NUM(bool),
			  NULL /* next */ )
		   ,

	  command_symbol_new
		  ("AllowDebuggingInterrupts",
		   _("Allow interrupts to occur while debugging"),
		   c_STATIC,
		   NULL /* action */ ,
		   RET_FIRST_ARG,
		   command_arg_new_enum
		     ("off|on", 
			  NULL /* help */,
			  NULL /* action */ ,
			  ARG_NUM(allow_debug_interrupts),
			  NULL /* next */ )
		   ,

	  command_symbol_new
		  ("SanityCheckLockupState",
		   _("Check for locked-up 99/4A and enter interactive mode. "
		   "The test is very simple and may not be true in all cases."),
		   c_STATIC,
           NULL /* action */ ,
		   RET_FIRST_ARG,
		   command_arg_new_enum
		     ("off|on", 
			  NULL /* help */,
			  NULL /* action */ ,
			  ARG_NUM(lockup_sanity_check),
			  NULL /* next */ )
		   ,

	  command_symbol_new
		  ("SingleStep",
		   _("Execute one instruction and stop"),
		   c_DONT_SAVE,
		   emulate_single_step /* action */ ,
		   RET_FIRST_ARG,
		   NULL /* args */
		   ,

	  command_symbol_new
		  ("BaseClockHZ",
		   _("Set HZ speed base clock (usually 3.0 MHz)"),
		   c_STATIC,
		   emulate_change_clock_speed,
		   RET_FIRST_ARG,
		   command_arg_new_num
		     (_("hertz"),
			  _("number of times per second"),
			  NULL  /* action */,
			  ARG_NUM(baseclockhz),
			  NULL /* next */),

	NULL	/* next */ ))))))))),

	NULL /* sub */ ,

	NULL	/* next */
);

	command_symbol_table *state =
		command_symbol_table_new(_("Memory / Debugging Commands"),
								 _("These options allow you to change the running state of the virtual machine"),
		 command_symbol_new("ProgramCounter|PC", 
							_("Set the program counter"),
							c_DYNAMIC|c_SESSION_ONLY,
							emulate_set_pc,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("address"), 
							 _("illegal addresses will be ignored"),
							 NULL /* action */,
							 NEW_ARG_NUM(u16),
							 NULL /* next */)
							,

			command_symbol_new
							("WorkspacePointer|WP",
							 _("Set the workspace pointer"),
							 c_DYNAMIC|c_SESSION_ONLY,
							 emulate_set_wp,
							 RET_FIRST_ARG,
							 command_arg_new_num
							 (_("address"),
							  _("illegal addresses will be ignored"),
							  NULL /* action */,
							  NEW_ARG_NUM(u16),
							  NULL /* next */)
							 ,


			command_symbol_new
							("StatusRegister|ST",
							 _("Set the status register"),
							 c_DYNAMIC|c_SESSION_ONLY,
							 emulate_set_st,
							 RET_FIRST_ARG,
							 command_arg_new_num
							 (_("address"),
							  _("illegal addresses will be ignored"),
							  NULL /* action */,
							  NEW_ARG_NUM(u16),
							  NULL /* next */)
							 ,

			command_symbol_new
							("VDPAddress",
							 _("Set the VDP address register"),
							 c_DYNAMIC|c_SESSION_ONLY,
							 do_vdp_mmio_set_addr /* action */,
							 RET_FIRST_ARG,
							 command_arg_new_num
							 (_("address"),
							  NULL,
							  NULL /* action */,
							  NEW_ARG_NUM(u16),
							  NULL /* next */)
							 ,

			command_symbol_new
							("VDPRegister",
							 _("Set a VDP register"),
							 c_DYNAMIC|c_SESSION_ONLY,
							 vdp_set_register,
							 NULL,
							 command_arg_new_num
							 (_("register"),
							  _("register number, 0-7"),
							  NULL /* action */,
							  NEW_ARG_NUM(u8),
							 command_arg_new_num
							 (_("value"),
							  _("value for register"),
							  NULL /* action */,
							  NEW_ARG_NUM(u8),

							  NULL /* next */))
							 ,

			command_symbol_new
							("VDPReadAhead",
							 _("Set VDP read-ahead value"),
							 c_DYNAMIC|c_SESSION_ONLY,
							 vdp_set_read_ahead,
							 NULL,
							 command_arg_new_num
							 (_("byte"),
							  _("next byte to read from VDP RAM"),
							  NULL /* action */,
							  NEW_ARG_NUM(u8),

							  NULL /* next */)
							 ,

			command_symbol_new
							("VDPAddrFlag",
							 _("Set VDP address"),
							 c_DYNAMIC|c_SESSION_ONLY,
							 vdp_set_addr_flag,
							 NULL,
							 command_arg_new_num
							 (_("flag"),
							  _("value of flag, 0 for high, 1 for low"),
							  NULL /* action */,
							  NEW_ARG_NUM(u8),
							  NULL /* next */)
							 ,

			command_symbol_new
							("GROMAddress",
							 _("Set the GROM address register"),
							 c_DYNAMIC|c_SESSION_ONLY,
							 do_grom_mmio_set_addr /* action */,
							 RET_FIRST_ARG,
							 command_arg_new_num
							 (_("address"),
							  NULL,
							  NULL /* action */,
							  NEW_ARG_NUM(u16),
							  NULL /* next */)
							 ,

			command_symbol_new
							("SpeechState",
							 NULL /* help */,
							 c_DYNAMIC|c_SESSION_ONLY,
							 speech_machine_state,
							 RET_FIRST_ARG,
							 command_arg_new_string("sp",
								NULL /* help */,
								NULL /* action */,
								NEW_ARG_NEW_STRBUF,
							 command_arg_new_string("lpc",
								NULL /* help */,
								NULL /* action */,
								NEW_ARG_NEW_STRBUF,
							NULL /* next */)),

			command_symbol_new
							("HW9901State",
							 NULL /* help */,
							 c_DYNAMIC|c_SESSION_ONLY,
							 hw9901_machine_state,
							 RET_FIRST_ARG,
							 command_arg_new_string("hw9901",
								NULL /* help */,
								NULL /* action */,
								NEW_ARG_NEW_STRBUF,
							 command_arg_new_string("audiogate",
								NULL /* help */,
								NULL /* action */,
								NEW_ARG_NEW_STRBUF,
								NULL /* next */)),

			
			command_symbol_new
							("ReadMemory",
							 _("Read contents of memory"),
							 c_STATIC|c_DONT_SAVE,
							 emulate_read_mem,

							 /* ret */
							 command_arg_new_num
							 (_("contents"),
							  _("memory bytes formatted into a single word"),
							  NULL /*action*/,
							  NEW_ARG_NUM(u32),
							  NULL),

							 command_arg_new_string
							 (_("type"),
							  _("memory type: C/V/G/S"),
							  NULL  /* action */,
							  NEW_ARG_STR (16),
							 command_arg_new_num
							 (_("address"),
							  _("illegal addresses will be ignored"),
							  NULL /* action */,
							  NEW_ARG_NUM(u16),
							 command_arg_new_num
							 (_("bytes"),
							  _("number of bytes to read (1, 2, or 4)"),
							  NULL  /* action */,
							  NEW_ARG_NUM(u8),
							  NULL /* next */)))
							 ,

			command_symbol_new
							("WriteMemory",
							 _("Write contents of memory"),
							 c_STATIC|c_DONT_SAVE,
							 emulate_write_mem,

							 NULL /*ret*/,

							 command_arg_new_string
							 (_("type"),
							  _("memory type: C/V/G/S, plus R to force writing of ROM "
								"(only valid if memory is not mapped as zero)"),
							  NULL  /* action */,
							  NEW_ARG_STR (16),
							 command_arg_new_num
							 (_("address"),
							  _("illegal addresses will be ignored"),
							  NULL /* action */,
							  NEW_ARG_NUM(u16),
							 command_arg_new_num
							 (_("bytes"),
							  _("number of bytes to write (1, 2, or 4)"),
							  NULL  /* action */,
							  NEW_ARG_NUM(u8),
							 command_arg_new_num
							 (_("data"),
							  _("data to write, as a single integer"),
							  NULL  /* action */,
							  NEW_ARG_NUM(u32),
							  NULL /* next */))))
							 ,

			command_symbol_new
							("SetRAM",
							 _("Change contents of multiple consecutive bytes of RAM"),
							 c_DYNAMIC|c_SESSION_ONLY,
							 emulate_set_mem,
							 NULL /* ret */,
							 command_arg_new_string
							 (_("type"),
							  _("memory type: C/V/G/S"),
							  NULL  /* action */,
							  NEW_ARG_STR (16),
							 command_arg_new_num
							 (_("address"),
							  _("illegal addresses will be ignored"),
							  NULL /* action */,
							  NEW_ARG_NUM(u16),
							 command_arg_new_string
							 (_("string"),
							  _("hexadecimal string"),
							  NULL  /* action */,
							  NEW_ARG_NEW_STRBUF,
							  NULL /* next */)))
							 ,

			command_symbol_new
							("SaveBinary",
							 _("Write contents of memory to a file (no headers!)"),
							 c_STATIC|c_DONT_SAVE,
							 emulate_bsave,
							 NULL /* ret */,
							 command_arg_new_string
							 (_("filename"),
							  _("file to write"),
							  NULL  /* action */,
							  NEW_ARG_STR (OS_PATHSIZE),
							 command_arg_new_string
							 (_("type"),
							  _("memory type: C/V/G/S"),
							  NULL  /* action */,
							  NEW_ARG_STR (16),
							 command_arg_new_num
							 (_("address"),
							  _("start address"),
							  NULL /* action */,
							  NEW_ARG_NUM(u16),
							 command_arg_new_num
							 (_("address"),
							  _("end address"),
							  NULL  /* action */,
							  NEW_ARG_NUM(u16),
							  NULL /* next */))))
							 ,

			command_symbol_new
							("LoadBinary",
							 _("Load contents of memory from a file (no headers!)"),
							 c_STATIC|c_DONT_SAVE,
							 emulate_bload,
							 NULL /* ret */,
							 command_arg_new_string
							 (_("filename"),
							  _("file to read"),
							  NULL  /* action */,
							  NEW_ARG_STR (OS_PATHSIZE),
							 command_arg_new_string
							 (_("type"),
							  _("memory type: C/V/G/S"),
							  NULL  /* action */,
							  NEW_ARG_STR (16),
							 command_arg_new_num
							 (_("address"),
							  _("start address"),
							  NULL /* action */,
							  NEW_ARG_NUM(u16),
							 command_arg_new_num
							 (_("address"),
							  _("end address"),
							  NULL  /* action */,
							  NEW_ARG_NUM(u16),
							  NULL /* next */))))
							 ,

			command_symbol_new
							("ZapMemory",
							 _("Set all RAM to a known pattern (0xAA55)"),
							 c_STATIC|c_DONT_SAVE,
							 emulate_zap_memory,
							 NULL /* ret */,
							 NULL /* args */
							 ,

			command_symbol_new
							("DumpCRUHandlers",
							 NULL /* help */,
							 c_STATIC|c_DONT_SAVE,
							 dump_cru_list,
							 0 /*ret*/,
							 0 /*args*/,

			command_symbol_new
							("DumpInstructions",
							 NULL /* help */,
							 c_STATIC|c_DONT_SAVE,
							 NULL,
							 0 /*ret*/,
		  command_arg_new_enum
		    ("off|on",
			 _("on:  dump trace to stdout; "
			 "off:  don't"),
			 NULL,
			 ARG_NUM(dump_instructions),
			 NULL /* next */ ),

							 NULL /* next */)))))))))))))))))),

		 NULL /* sub */,
		 NULL /* next */
		);

	command_symbol_table_add_subtable(universe, internal);
	command_symbol_table_add_subtable(universe, state);

	delaybetweeninstructions = 0;
#if 0
	bogocycles = 0;
	calibrate_processor();
#endif

	emulate_tag = TM_UniqueTag();
	slowdown_tag = TM_UniqueTag();	/* one shot */

	totalticks = 0;
	totalexecuted = 0;
	executed = 0;

	lockup_sanity_count = 0;

	memory_init();

	return vmOk;
}

static      vmResult
emulate9900_enable(void)
{
	return vmOk;
}

static      vmResult
emulate9900_disable(void)
{
	return vmOk;
}

static      vmResult
emulate9900_term(void)
{
	return vmOk;
}

static      vmResult
emulate9900_restart(void)
{
	targetcycles = baseclockhz / BASE_EMULATOR_HZ;
	currenttime = 0;
	totalcurrentcycles = totalticks = totalexecuted = executed = 0;

	lockup_sanity_count = 0;
	
	TM_ResetEvent(emulate_tag);
	TM_SetEvent(emulate_tag, TM_HZ * 100 / BASE_EMULATOR_HZ, 0,
				TM_FUNC | TM_REPEAT, EmulateEvent);

	calibrated_processor = false;
	command_exec_text("SaveMemory\n"
					  "LoadMemory\n");

	inserthacks();

	// be sure our register pointer is set up
	setandverifywp(wp);

	return vmOk;
}

static      vmResult
emulate9900_restop(void)
{
	TM_ResetEvent(emulate_tag);
	command_exec_text("SaveMemory\n");
	return vmOk;
}

/*********************************************************/

static int  keep_going;

int         handlestateflag(void);

static void
emulate9900_stop(void)
{
	keep_going = 0;
}

static long emulate_stopwatch;

#define EXEC_THRESHOLD		1024	/* # of instructions before compiling */

static void
execute_current_inst(void)
{
	mrstruct *area = THE_AREA(md_cpu, pc);
	
	/* in compile mode, try to make native code */
	if (compile_mode)
	{
		/* do we want to handle this area? */
		struct CodeBlock *cb = 0L;

		if (area->exec_hits >= EXEC_THRESHOLD)
			cb = compiler_add_cache(pc);

		if (cb)
		{
			u32 justexecuted;
			int emul;

			if (compiler_build(cb))
			{
				emul = compiler_execute(cb, pc, &justexecuted, &instcycles);
				executed += justexecuted;
				if (!emul) goto next;
			}
			// unhandled instruction, fall through
		}
		// not compiled or not emulated, fall through
		if (log_level(LOG_COMPILER) > 2) printf("emuling %04X\n", pc);
	}

	// execute() updates this for the current instruction
	instcycles = 0;
	area->exec_hits++;
	execute(fetch());
	executed++;

next:
	/*  Check for 9901 timer, which is keyed
		on baseclockhz / 64 */

	if ((currentcycles & ~63) != ((currentcycles + instcycles) & ~63)) {
		// tick at the baseclockhz/64 rate; 9901.c will handle
		// correlating this to the clock interrupt
		if (currenttime < baseclockhz / 64 / BASE_EMULATOR_HZ) {
			currenttime++;
			handle9901tick();
		}
	}

	currentcycles += instcycles;

	// store new interrupt level, if any
	// (prevents an interrupt right after RTWP from one)
	change9900intmask(status & 0xf);
}

static int
emulate9900_execute(void)
{
	int ret;

	// if we're already speeding along...
	if (!stateflag && realtime && currentcycles >= targetcycles)
		return em_TooFast;

	emulate_stopwatch = TM_GetTicks();

	while (1) {
		if (stateflag && (ret = handlestateflag()) != em_KeepGoing) {
			break;
		}

		execute_current_inst();

		if (!realtime) {
			ret = delaybetweeninstructions;
			while (ret--)
				;
		} else if (currentcycles >= targetcycles) {
			ret = em_TooFast;
			break;
		}

		// finally, don't take too long here
		if (TM_GetTicks() > emulate_stopwatch + TM_HZ/10) {
			ret = em_Interrupted;
			break;
		}

		if (stateflag & ST_SINGLESTEP) {
			stateflag &= ~ST_SINGLESTEP;
			ret = em_Interrupted;
			break;
		}

		if (!(stateflag & ST_PAUSE) && debugger_check_breakpoint(pc))
		{
			execution_pause(true);
			debugger_enable(true);
			debugger();	/* show the instruction we stopped at */
			ret = em_TooFast;
			break;
		}
	}

	return ret;
}

/*********************************************/

int
handlestateflag(void)
{
	int         ret = em_KeepGoing;

	logger(_L | L_2, "handlestateflag 0x%X\n", stateflag);
	
	// set when we want to quit
	if (stateflag & ST_TERMINATE)
		return em_Quitting;

	// set we're entering commands
	if (stateflag & ST_INTERACTIVE) 
		return em_Interrupted;

	// set to force emulation to halt
	if (stateflag & ST_STOP) {
		stateflag &= ~ST_STOP;
		ret = em_Interrupted;
		return ret;
	}

	// if not paused, we can move forward
	if ((stateflag & ST_PAUSE) && !(stateflag & ST_SINGLESTEP))
		return em_TooFast;

	if (stateflag & ST_DEBUG) {
		// if this is set, we want to execute the current instruction
		// then show the new one
		if (stateflag & ST_SINGLESTEP) {
			execute_current_inst();
			stateflag &= ~ST_SINGLESTEP;
		}

		// now show the current instruction
		debugger();

		// if paused, we must have single stepped, so clear out
		if (stateflag & ST_PAUSE)
			return em_TooFast;
	} else {

		// normal single step, from emulator land.
		if (stateflag & ST_SINGLESTEP) {
			execute_current_inst();
			stateflag &= ~ST_SINGLESTEP;
			execution_pause(true);
			return em_TooFast;
		}

		// don't try to execute anything
		if (stateflag & ST_PAUSE)
			return em_TooFast;
	}

	// any sort of interrupt that sets intpins9900
	if ((stateflag & ST_INTERRUPT)) {
		// non-maskable
		if (intpins9900 & INTPIN_LOAD) {
			intpins9900 &= ~INTPIN_LOAD;
			logger(_L | 0, "**** NMI ****");
			contextswitch(0xfffc);
			instcycles += 22;
			execute_current_inst();
		} else
			// non-maskable (?)
		if (intpins9900 & INTPIN_RESET) {
			intpins9900 &= ~INTPIN_RESET;
			logger(_L | 0, "**** RESET ****\n");
			contextswitch(0);
			instcycles += 26;
			execute_current_inst();
		} else
			// maskable
		if (intpins9900 & INTPIN_INTREQ) {
			u16         highmask = 1 << (intlevel9900 + 1);

			// 99/4A console hardcodes all interrupts as level 1,
			// so if any are available, level 1 is it.
			if (intlevel9900 && read9901int() &&
				(!(stateflag & ST_DEBUG) || (allow_debug_interrupts)))
			{
				intpins9900 &= ~INTPIN_INTREQ;
				contextswitch(0x4);
				intlevel9900--;
				instcycles += 22;
				execute_current_inst();
			}
		} else
			intpins9900 = 0;	// invalid

		if (!intpins9900)
			stateflag &= ~ST_INTERRUPT;
	}

	return ret;
}

void
emulate_break(void)
{
	execution_pause(true);
	debugger_enable(true);
}

/*	Ensure that a restore of machine state succeeds without a reboot */
void
emulate_setup_for_restore(void)
{
	// video module needs a jab
	TM_Start();
	vdpcompleteredraw();
	VIDEO(resetfromblank,());
	intpins9900 &= ~INTPIN_RESET;
	stateflag &= ~(ST_REBOOT | ST_INTERRUPT);
}

/*
	Always called BASE_EMULATOR_HZ times a second.
*/
void
EmulateEvent(void)
{
	if (stateflag & (ST_INTERACTIVE|ST_PAUSE|ST_TERMINATE)) return;
	if (demo_playing) return;

	totalcurrentcycles += currentcycles;
	totaltargetcycles += targetcycles;

	currentcycles = 0;
	currenttime = 0;

	totalexecuted += executed;
	executed = 0;
	totalticks++;

	if (totalticks % (BASE_EMULATOR_HZ * 10) == 0) {
		report_status(STATUS_CYCLES_SECOND, 
					  (long)(totalcurrentcycles / (totalticks / BASE_EMULATOR_HZ)),
					  (long)(totalexecuted / (totalticks / BASE_EMULATOR_HZ)));
		executed = 0;
	}

	// check for locked-up state
	if (lockup_sanity_check) {
		if (!verifywp(wp)) {
			lockup_sanity_count++;

			// panic after three seconds
			if (lockup_sanity_count >= BASE_EMULATOR_HZ * 2 
			&& 	!(stateflag & ST_INTERACTIVE)) 
			{
				logger(_L | LOG_USER, 
					   _("\nInvalid workspace pointer set (>%04X);\n"
					   "emulated 99/4A is probably locked up.\n"
					   "(If this is the first thing V9t9 is doing,\n"
					   "perhaps your ROMs are missing or misconfigured?)\n"
					   "\n"
					   "Enter 'SanityCheckLockupState off' to disable this message.\n"),
					   wp);
				command_exec_text("Interactive on\n");
				lockup_sanity_count = 0;
			}
		} else {
			lockup_sanity_count = 0;
		}
	}
}

static vmCPUModule myCPUModule = {
	2,
	emulate9900_execute,
	emulate9900_stop
};

vmModule    emulate9900CPU = {
	3,
	"9900 emulator",
	"cpu9900",

	vmTypeCPU,
	vmFlagsExclusive,

	emulate9900_detect,
	emulate9900_init,
	emulate9900_term,
	emulate9900_enable,
	emulate9900_disable,
	emulate9900_restart,
	emulate9900_restop,
	{(vmGenericModule *) & myCPUModule}
};

///////////////
static int  round, timeout, slowing;
static u8   lastkey = 0xff;
static volatile int resume;
void
emulate_keyslow(void)
{
	u8          curkey;
//	u8			status;

	if (pc == 0x2B2 + 2) {
		if (realtime) {
		finish:
//			MEMORY_WRITE_BYTE(0x83C8, register(0)>>8);
			MEMORY_WRITE_WORD(0x83D8, register(11));
			pc += 2;	// skip addr word
			return;
		}

		logger(LOG_KEYBOARD | LOG_INFO | L_2, _("emulate_keyslow; slowing=%d, resume=%d\n"), slowing, resume);

		if (slowing && resume) {
			resume = slowing = 0;
			goto finish;
		}

		// is the same key pressed?
		curkey = (u8) memory_read_byte(0x8375);
//		status = (u8) memory_read_byte(0x837C);	// not set yet
		if (1) { //lastkey != 0xff && curkey == 0xff && curkey == lastkey) {
			logger(LOG_KEYBOARD | LOG_INFO | L_2, _("got a keypress [%02X : %02X]\n"), curkey);
			if (!slowing) {
				logger(LOG_KEYBOARD | LOG_INFO| L_2, _("slowing down...\n"));
				/*
				  round = (round + 1) & 0x7;
				if (!round) {
					resume = 0;
					slowing = 1;
					TM_SetEvent(slowdown_tag, 6 * 100 / TM_HZ, 0, 0, &resume);
				} else {
					timeout = 1000;
					slowing = 1;
					resume = 0;
				}
				*/
				resume = 0;
				slowing = 1;
				TM_SetEvent(slowdown_tag, 6 * 100 / TM_HZ, 0, 0, &resume);
				{
					int cnt = 5000000;
					static int dummy;
					while (cnt--) dummy++;
				}
				usleep(5000);
			} /*else if (round) {
				if (!timeout--)
					resume = 1;
			}*/

			goto finish;
			//pc -= 2;	/* return to the instruction */
		} else {
			logger(LOG_KEYBOARD | LOG_INFO | L_2, _("different key [%02X != %02X] : %02X\n"), curkey, lastkey, status);
			if (curkey != 0xff)
				lastkey = curkey;
			slowing = resume = 0;
			goto finish;
		}
	}
}
