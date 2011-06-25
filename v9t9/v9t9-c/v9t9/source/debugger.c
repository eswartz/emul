/*
  debugger.c					-- 99/4A debugger implementation

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
 *	This module handles the machine-level implementation of 
 *	the debugger -- keeping track of changed registers, affected
 *	memory, single-stepping, etc.
 *
 *	The UI for the debugger is handled by the frontend through
 *	the global report_status() callback (see log.h).
 */


#include <stdio.h>
#include <ctype.h>
#include "v9t9_common.h"
#include "9900.h"
#include "9900st.h"
#include "memory.h"
#include "vdp.h"
#include "grom.h"
#include "speech.h"
#include "system.h"
#include "dis9900.h"
#include "debugger.h"

#define _L LOG_CPU

static char *
decR(char *buf, int val)
{
	*buf++ = 'R';
	if (val >= 10) {
		*buf++ = '1';
		val -= 10;
	}
	*buf++ = (val + '0');
	return buf;
}

#define NO_DOMAIN	(mem_domain)(-1)

static Memory views[MEMORY_VIEW_COUNT];
int debugger_memory_view_size[MEMORY_VIEW_COUNT] = { 16, 16, 16, 16, 16 };
bool debugger_operand_view_verbose = true;

static u16 register_view;

static      u8
MEMORY_READ_MM_BYTE(u16 x)
{
	x &= 0x9c02;

	return x == 0x8800 ? domain_read_byte(md_video, vdp_mmio_get_addr()) :
		x == 0x8802 ? vdp_mmio_get_status() :
		x == 0x9000 ? domain_read_byte(md_speech, speech_mmio_get_addr()) :
		x == 0x9800 ? domain_read_byte(md_graphics, grom_mmio_get_addr()) :
		x == 0x9802 ? grom_mmio_get_addr_byte() : 
		0;
}

#define flatmem(x) 		(byteop ? flatmem8(x)&0xff : flatmem16(x)&0xffff)
#define flatmem8(x) 	(((x)>=0x8400 && (x)<0xa000 ? MEMORY_READ_MM_BYTE(x) : MEMORY_READ_BYTE(x))&0xff)
#define flatmem16(x) 	(((x)>=0x8400 && (x)<0xa000 ? MEMORY_READ_MM_BYTE(x)<<8 : MEMORY_READ_WORD(x))&0xffff)

u16 flat_read_word(u16 addr)
{
	return MEMORY_READ_WORD(addr);
}

/*
 *	Update register view according to effects of 
 *	previously executed instruction, including change to WP,
 *	reads and writes to register.
 */
static void
register_update_view(Instruction *inst, u16 wp)
{
	int reg;
	int read, written;
	u16 regs[16];

	// is new register set changing?

	if (wp != register_view) {
		register_view = wp;

		for (reg = 0; reg < 16; reg++) {
			regs[reg] = MEMORY_READ_WORD((reg<<1) + register_view);
		}
		report_status(STATUS_CPU_REGISTER_VIEW, register_view, regs);

		// don't bother reporting effects of previous instruction
		return;
	}

	// report accessed registers from prevous instruction

	read = written = 0;
	dis9900_derive_register_access(inst, &inst->op1, flat_read_word,
								   &read, &written);
	dis9900_derive_register_access(inst, &inst->op2, flat_read_word,
								   &read, &written);

	for (reg = 0; reg < 16; reg++) {
		if (written & (1 << reg)) {
			report_status(
				STATUS_CPU_REGISTER_WRITE,
				reg, MEMORY_READ_WORD((reg<<1) + inst->wp));
		} else if (read & (1 << reg)) {
			report_status(
				STATUS_CPU_REGISTER_READ,
				reg, MEMORY_READ_WORD((reg<<1) + inst->wp));
		}
	}
}

void
debugger_register_clear_view(void)
{
	u16 regs[16];
	int reg;

	register_view = wp;
	for (reg = 0; reg < 16; reg++) {
		regs[reg] = MEMORY_READ_WORD((reg<<1) + register_view);
	}
	report_status(STATUS_CPU_REGISTER_VIEW, register_view, regs);
}

static char *hexstr = "0123456789ABCDEF";

static char *
hex2(char *buf, u8 val)
{
	*buf++ = hexstr[(val & 0xf0) >> 4];
	*buf++ = hexstr[val & 0xf];
	return buf;
}

static char *
hex4(char *buf, u16 val)
{
	*buf++ = hexstr[(val & 0xf000) >> 12];
	*buf++ = hexstr[(val & 0xf00) >> 8];
	*buf++ = hexstr[(val & 0xf0) >> 4];
	*buf++ = hexstr[val & 0xf];
	return buf;
}

/*
 *	Setup a memory view, returning a bool telling
 *	whether the view changed.
 */
static bool
memory_view_setup(Memory *s, MemoryView view, u16 addr, int len)
{
	bool changed = false;
	mem_domain dmn;
	u8 *mem;
	mrstruct *area;

	s->which = view;
	s->addr = addr;
	s->len = len;

	if (debugger_memory_view_size[s->which] <= 0) {
		debugger_memory_view_size[s->which] = 16;
	}

	// get base address fixed at multiple of view size
	if ((s->addr < s->base || 
		s->addr + len >= s->base + debugger_memory_view_size[s->which]) 
		|| s->base % debugger_memory_view_size[s->which]) 
	{
		s->base = s->addr - (s->addr % debugger_memory_view_size[s->which]);
		changed = true;
	}
	
	// if not enough room for operand, fudge base address
	if (s->base + debugger_memory_view_size[s->which] < s->addr + len) 
	{
		s->base = s->addr;
		changed = true;
	}

	// set up memory pointer
	dmn = (view == MEMORY_VIEW_VIDEO) ? md_video :
		(view == MEMORY_VIEW_GRAPHICS) ? md_graphics :
		(view == MEMORY_VIEW_SPEECH) ? md_speech : md_cpu;

	//mem = FLAT_MEMORY_PTR(dmn, s->base);
	area = THE_AREA(dmn, s->base);
	if (area->areamemory)
		mem = area->areamemory + (s->base & (AREASIZE-1));
	else
		mem = zeroes;


	if (mem != s->mem)
		changed = true;

	s->mem = mem;

	return changed;
}

/*
 *	For a given address reference, select a view for the
 *	type of memory it is referencing.
 */

INLINE int memory_distance(u16 a, u16 b)
{
	return (a < 0x8000 && b < 0x8000) ? a - b :
		(a < 0x8000 && b >= 0x8000) ? a - (0x10000 - b) :
		(a >= 0x8000 && b < 0x8000) ? (0x10000 - a) - b :
		(0x10000 - a) - (0x10000 - b);
}

static Memory *
memory_view_get(u16 addr, int len, bool dest, Memory *using, bool *changed)
{
	MemoryView	view = dest ? MEMORY_VIEW_CPU_2 : MEMORY_VIEW_CPU_1;
	Memory       *s;

	// !!! warning, this assumes a 99/4A with this memory
	// configuration
	if (addr >= 0x8400 && addr < 0xa000) {
		addr &= 0x9c02;

		switch (addr) {
		case 0x8800:
		case 0x8c00:
		case 0x8c02:
			if (!vdp_mmio_addr_is_complete())
				return 0L;
			addr = vdp_mmio_get_addr();
			view = MEMORY_VIEW_VIDEO;
			break;
		case 0x9000:
		case 0x9400:
			if (!speech_mmio_addr_is_complete())
				return 0L;
			addr = speech_mmio_get_addr();
			view = MEMORY_VIEW_SPEECH;
			break;
		case 0x9800:
		case 0x9802:
		case 0x9c00:
		case 0x9c02:
			if (!grom_mmio_addr_is_complete())
				return 0L;
			addr = grom_mmio_get_addr();
			view = MEMORY_VIEW_GRAPHICS;
			break;
		}
	}

	// divide cpu views 1 and 2 into source and
	// destination, or, based on distance from previous view

	if (view == MEMORY_VIEW_CPU_1 || view == MEMORY_VIEW_CPU_2) {
		int dist1 = memory_distance(views[MEMORY_VIEW_CPU_1].addr, addr);
		int dist2 = memory_distance(views[MEMORY_VIEW_CPU_2].addr, addr);

		if (dist1 > dist2 + 32 ||
			(views[MEMORY_VIEW_CPU_1].coverage > 
			 views[MEMORY_VIEW_CPU_2].coverage + 32))
			view = MEMORY_VIEW_CPU_2;
		else if (dist2 > dist1 + 32 ||
				 (views[MEMORY_VIEW_CPU_2].coverage > 
				  views[MEMORY_VIEW_CPU_1].coverage + 32))
			view = MEMORY_VIEW_CPU_1;

		views[view].coverage++;
	}


	// setup the view info
	s = &views[view];

	*changed = memory_view_setup(s, view, addr, len);

	return s;
}

/*
 *	Update views of memory according to effect of
 *	previous instruction
 */
static void
memory_update_views(Instruction *inst)
{
	Memory 	*view1 = 0L, *view2 = 0L;
	bool view1changed, view2changed;

	// pick a view for each memory operand

	if (OP_IS_MEMORY(inst->op1)) {
		view1 = memory_view_get(inst->op1.ea, 
								inst->op1.byteop ? 1 : 2,
								inst->op1.dest,
								NULL,
								&view1changed);
	}

	if (OP_IS_MEMORY(inst->op2)) {
		view2 = memory_view_get(inst->op2.ea,
								inst->op2.byteop ? 1 : 2,
								inst->op2.dest,
								view1,
								&view2changed);
	}

	// don't show the same one twice

	if (view1 && view1 == view2)
		view2 = 0L;

	// update each view

	if (view1) {
		if (view1changed)
			report_status(STATUS_MEMORY_VIEW, view1);

		report_status(inst->op1.dest ? 
					  STATUS_MEMORY_WRITE :
					  STATUS_MEMORY_READ, view1);
	}

	if (view2) {
		if (view2changed)
			report_status(STATUS_MEMORY_VIEW, view2);

		report_status(inst->op2.dest ? 
					  STATUS_MEMORY_WRITE :
					  STATUS_MEMORY_READ, view2);
	}
}

static u16
memory_view_real_address(Memory *s)
{
	switch (s->which) 
	{
	case MEMORY_VIEW_VIDEO:
		s->len = 1;
		if (vdp_mmio_addr_is_complete())
			s->addr = vdp_mmio_get_addr();
		return true;
	case MEMORY_VIEW_GRAPHICS:
		s->len = 1;
		if (grom_mmio_addr_is_complete())
			s->addr = grom_mmio_get_addr();
		return true;
	case MEMORY_VIEW_SPEECH:
		s->len = 1;
		if (speech_mmio_addr_is_complete())
			s->addr = speech_mmio_get_addr();
		return true;
	}
	return false;
}

void
debugger_memory_clear_views(void)
{
	MemoryView view;
	Memory *s;
	bool memory_mapped;

	for (view = MEMORY_VIEW_CPU_1;
		 view < MEMORY_VIEW_COUNT;
		 view++)
	{  
		s = &views[view];
		memory_mapped = memory_view_real_address(s);
		memory_view_setup(s, view, s->addr, 0);
		report_status(STATUS_MEMORY_VIEW, s);
	}
}

/*
 *	Print value of operand to buffer
 *
 *	verbose==true means to print extra info
 *	after==true means this operand as the destination of 
 *	previous instruction
 */
static char *
dis9900_operand_value_print(Instruction *inst, Operand *op, 
							 bool verbose, bool after, 
							 char *buffer, int bufsize)
{
	const char *equ = after ? ":=" : "=";

	// is operand not a destination?
	if (after && !op->dest)
		return NULL;

	// if source operand is killed, we don't care to see it
	if (!after && op->dest == OP_DEST_KILLED)
		return NULL;

	// ignore this operand?
	if (op->addr)
		return NULL;

	switch (op->type) 
	{
	case OP_REG:
		if (inst->opcode >= 0x3800 && inst->opcode < 0x3C00)
		{
			// MPY uses two adjacent registers
			if (after)
				if (verbose)
					snprintf(buffer, bufsize, "R%d,R%d%s>%04X%04X",
							op->val, op->val+1, equ,
							flatmem16(op->ea), 
							flatmem16(op->ea + 2));
				else
					snprintf(buffer, bufsize, ">%04X%04X",
							flatmem16(op->ea), 
							flatmem16(op->ea + 2));
			else
				if (verbose)
					snprintf(buffer, bufsize, "R%d%s>%04X", 
							op->val, equ, flatmem16(op->ea));
				else
					snprintf(buffer, bufsize, ">%04X", flatmem16(op->ea));
		}
		else if (inst->opcode >= 0x3C00 && inst->opcode < 0x4000)
		{
			// DIV uses two adjacent registers
			if (!after)
				if (verbose)
					snprintf(buffer, bufsize, "R%d,R%d%s>%04X%04X",
							op->val, op->val+1, equ,
							flatmem16(op->ea), 
							flatmem16(op->ea + 2));
				else
					snprintf(buffer, bufsize, ">%04X%04X",
							flatmem16(op->ea), 
							flatmem16(op->ea + 2));

			else
				if (verbose)
					snprintf(buffer, bufsize, "qR%d%s>%04X,rR%d%s>%04X",
							op->val, equ, flatmem16(op->ea),
							op->val+1, equ, flatmem16(op->ea + 2));
				else
					snprintf(buffer, bufsize, "Q>%04X R>%04X",
							flatmem16(op->ea),
							flatmem16(op->ea + 2));
		}
		else
		{
			if (op->byteop)
				if (verbose)
					snprintf(buffer, bufsize, "R%d%s>%02X", 
							op->val, equ, flatmem8(op->ea));
				else
					snprintf(buffer, bufsize, ">%02X", flatmem8(op->ea));
			else
				if (verbose)
					snprintf(buffer, bufsize, "R%d%s>%04X", 
							op->val, equ, flatmem16(op->ea));
				else
					snprintf(buffer, bufsize, ">%04X", flatmem16(op->ea));
		}
		break;

	case OP_INC:
	case OP_IND:
		if (after) 
		{
			if (op->byteop)
				if (verbose)
					snprintf(buffer, bufsize, "R%d%s>%02X", 
							op->val, equ, flatmem8(op->ea));
				else
					snprintf(buffer, bufsize, ">%02X", flatmem8(op->ea));
			else 
				if (verbose)
					snprintf(buffer, bufsize, "R%d%s>%04X", 
							op->val, equ, flatmem16(op->ea));
				else
					snprintf(buffer, bufsize, ">%04X", flatmem16(op->ea));
			break;
		}
		// else show address

	case OP_ADDR:
		if (op->byteop)
			if (verbose)
				// if address points to a register, point this out
				if (op->ea >= inst->wp && op->ea < inst->wp+32)
					snprintf(buffer, bufsize, "%c%d%s>%02X", 
							(op->ea&1) ? 'r' : 'R', // low or high byte
							(op->ea - inst->wp)>>1,
							equ,
							flatmem8(op->ea));
				else
					snprintf(buffer, bufsize, ">%04X%s>%02X", 
							op->ea, equ,
							flatmem8(op->ea));
			else
				snprintf(buffer, bufsize, ">%02X", flatmem8(op->ea));
		else
			if (verbose)
				// if address points to a register, point this out
				if (op->ea >= inst->wp && op->ea < inst->wp+32)
					snprintf(buffer, bufsize, "R%d%s>%04X", 
							(op->ea - inst->wp)>>1,
							equ,
							flatmem16(op->ea));
				else
					snprintf(buffer, bufsize, ">%04X%s>%04X", 
							op->ea, equ,
							flatmem16(op->ea));
			else
				snprintf(buffer, bufsize, ">%04X", flatmem16(op->ea));
		break;

/*
	case OP_IMMED:
		snprintf(buffer, bufsize, ">%04X",op->immed);
		break;	

	case OP_CNT:
		snprintf(buffer, bufsize, ">%04X",op->val);
		break;
*/

	case OP_OFFS:
	case OP_JUMP:
		snprintf(buffer, bufsize, ">%04X",op->ea);
		break;

	case OP_STATUS:
		snprintf(buffer, bufsize, "<%s%s%s%s%s%s%s|%x>",
					   (inst->status&ST_L) ? "L" : "",
					   (inst->status&ST_A) ? "A" : "",
					   (inst->status&ST_E) ? "E" : "",
					   (inst->status&ST_C) ? "C" : "",
					   (inst->status&ST_O) ? "O" : "",
					   (inst->status&ST_P) ? "P" : "",
					   (inst->status&ST_X) ? "X" : "",
					   inst->status&ST_INTLEVEL);
		break;

	case OP_INST:
		{
			// sub-instruction!
			Instruction xinst;
			char op1[32], *op1ptr, op2[32], *op2ptr;
			dis9900_decode(op->val, 
						   inst->pc, inst->wp, inst->status, 
						   flat_read_word,	
						   &xinst);
			if (verbose)
			{
				op1ptr = dis9900_operand_print(&xinst.op1, op1, sizeof(op1));
				op2ptr = dis9900_operand_print(&xinst.op2, op2, sizeof(op2));
				snprintf(buffer, bufsize, "(>%04X %s %s%s%s)",
						xinst.opcode,
						xinst.name,
						op1ptr ? op1ptr : "",
						op2ptr ? "," : "",
						op2ptr ? op2ptr : "");
			} 
			else
			{
				snprintf(buffer, bufsize, "(>%04X %s)",
						xinst.opcode,
						xinst.name);
			} 
		}
		break;

	default:
		return 0L;
	}

	return buffer;
}


/*
 *	Update view of instruction.  When a previous instruction
 *	is being viewed, we send any destination operands changed.
 *	For a current instruction, we preview the values of the operands
 *	and send those, as well as a disassembly.
 */
static void
instruction_update_view(Instruction *inst, bool after)
{
	char hex[16];
	char disasm[64];
	char op1[32], *op1ptr;
	char op2[32], *op2ptr;

	if (!after) {
		// tell about the system registers
		report_status(STATUS_CPU_PC, inst->pc);
		report_status(STATUS_CPU_WP, inst->wp);
		report_status(STATUS_CPU_STATUS, inst->status);

		// get hex representation of instruction
		sprintf(hex, "%04X=%04X", inst->pc, inst->opcode);
		
		// get disassembly with operand representations
		op1ptr = dis9900_operand_print(&inst->op1, op1, sizeof(op1));
		op2ptr = dis9900_operand_print(&inst->op2, op2, sizeof(op2));
		if (!op1ptr) {
			op1ptr = op2ptr;
			op2ptr = 0L;
		}
		snprintf(disasm, sizeof(disasm), 
				 "%s%s%s",
				op1ptr ? op1ptr : "",
				op2ptr ? "," : "",
				op2ptr ? op2ptr : "");

		// get operand values
		op1ptr = dis9900_operand_value_print(
					inst, 
					&inst->op1,
					debugger_operand_view_verbose,
					false /*after*/,
					op1, sizeof(op1));

		op2ptr = dis9900_operand_value_print(
					inst, 
					&inst->op2,
					debugger_operand_view_verbose,
					false /*after*/,
					op2, sizeof(op2));

		if (!op1ptr) {							 
			op1ptr = op2ptr;
			op2ptr = 0L;
		}

		// send it to frontend
		report_status(STATUS_CPU_INSTRUCTION,
					  inst,
					  hex,
					  disasm,
					  op1ptr,
					  op2ptr);
	} else {
		// afterwards, show destination operands
	
		// get operand values
		op1ptr = dis9900_operand_value_print(
					inst, 
					&inst->op1,
					debugger_operand_view_verbose,
					true /*after*/,
					op1, sizeof(op1));

		op2ptr = dis9900_operand_value_print(
					inst, 
					&inst->op2,
					debugger_operand_view_verbose,
					true /*after*/,
					op2, sizeof(op2));

		if (!op1ptr) {							 
			op1ptr = op2ptr;
			op2ptr = 0L;
		}

		// send it to frontend
		report_status(STATUS_CPU_INSTRUCTION_LAST,
					  inst,
					  op1ptr,
					  op2ptr);
	}
}

void
debugger_instruction_clear_view(void)
{
	// send refresh signal to frontend
	report_status(STATUS_CPU_INSTRUCTION_LAST,
				  0L,
				  0L,
				  0L);
}

/*
 *	Utility for status reporters.  Given the slot, it writes a one-line hex dump to
 *	the given buffer, and sets start/astart and end/aend to point to the extent
 *	of the last memory access within the buffer.  (These will be spaces.)
 *
 *	addr_separator: char appearing between address and bytes
 *	byte_separator: char appearing between each hex byte
 *	ascii_separator: char appearing between hex field and ascii field
 *	line_separator: char appearing between lines, and at end
 */	
void
debugger_hex_dump_line(Memory * slot, int offset, int length,
					   char addr_separator, char byte_separator, 
					   char ascii_separator, char line_separator,
					   char *buffer, int bufsz,
					   char **start, char **end,
					   char **astart, char **aend)
{
	char       *dumpptr = buffer, *asciiptr = dumpptr + 6+length*3;
	u16         idx, addr;
	u8          *bytes;

	if (asciiptr + length + 1 >= buffer + bufsz)
	{
		length = debugger_hex_dump_chars_to_bytes(bufsz-1);
		asciiptr = dumpptr + 6+length*3;
		my_assert(asciiptr + length < buffer + bufsz);
	}

	*dumpptr = 0;
	*start = *end = *astart = *aend = 0L;

	bytes = slot->mem + offset;
	addr = slot->base + offset;

	*dumpptr++ = MEMORY_VIEW_TOKEN(slot->which);
	dumpptr = hex4(dumpptr, addr);
	*dumpptr++ = addr_separator;

	if (slot->len
		&& addr > slot->addr
		&& addr < slot->addr + slot->len) 
	{
		*start = dumpptr;
		*astart = asciiptr;
	}

	idx = 0;
	while (idx < length) {
		u8          ch;

		if (slot->len && addr + idx == slot->addr) {
			*start = dumpptr;
			*astart = asciiptr;
		}

		if (idx + offset < 65536) {
			if (slot->which <= MEMORY_VIEW_CPU_2 && SWAPPED_ENDIAN)
				ch = bytes[idx ^ 1];
			else
				ch = bytes[idx];
		}
		else
			ch = 0;

		dumpptr = hex2(dumpptr, ch);
		*asciiptr++ = isprint(ch) ? ch : '.';
		idx++;

		if (slot->len && addr + idx == slot->addr + slot->len) {
			*end = dumpptr;
			*aend = asciiptr;
		}
		if (idx  < length)
			*dumpptr++ = byte_separator;
	}

	if (slot->len
		&& addr + length > slot->addr 
		&& addr + length < slot->addr + slot->len)
	{
		*end = dumpptr;
		*aend = asciiptr;
	}

	*dumpptr++ = ascii_separator;
	*asciiptr++ = line_separator;
	*asciiptr = 0;
}

/*
 *	How long will this text be?
 */
int 
debugger_hex_dump_bytes_to_chars(int bytes)
{
	return bytes * 4 + 6 + 2;
}

/*
 *	How many bytes fit in this length?
 */
int 
debugger_hex_dump_chars_to_bytes(int chars)
{
	return (chars - 6 - 2) / 4;
}

/*
 *	Breakpoint manager
 */

typedef struct bkpt {
	u16 pc;			// location of bkpt
	bool temporary;	// remove after hitting?
	bool memory;	// true: memory access bkpt, false: execution bkpt
	mem_domain md;	// memory domain
	mrstruct origmr;	// original memory view if 'memory'
	bool memread, memwrite; // access flags if 'memory'
	struct bkpt *next;
} bkpt;

static bkpt	*breakpoints;

// check to see if the address matches a breakpoint
static bkpt *
debugger_search_breakpoint(u16 pc, mem_domain md, bool temporary, bool memory)
{
	bkpt *ptr = breakpoints;
	while (ptr) {
		if (ptr->pc == pc && ptr->md == md 
			&& ptr->temporary == temporary
			&& ptr->memory == memory) {
			return ptr;
		}
		else
			ptr = ptr->next;
	}
	return 0;
}

// check to see if the address matches a breakpoint
static bkpt *
debugger_match_breakpoint_addr(u16 pc, mem_domain md, bool memory)
{
	bkpt *ptr = breakpoints;
	while (ptr)
		if (ptr->pc == pc 
			&& ptr->md == md 
			&& ptr->memory == memory) {
			return ptr;
		}
		else
			ptr = ptr->next;
	return 0;
}

// add the address to the list of breakpoints
static bkpt*
debugger_set_breakpoint(u16 pc, mem_domain md, bool temporary, bool memory, bool memread, bool memwrite)
{
	bkpt *ptr;

	if (debugger_search_breakpoint(pc, md, temporary, memory))
		return;

	ptr = (bkpt *)xmalloc(sizeof(bkpt));
	ptr->pc = pc;
	ptr->temporary = temporary;
	ptr->md = md;
	ptr->memory = memory;
	ptr->memread = memread;
	ptr->memwrite = memwrite;
	ptr->next = breakpoints;
	breakpoints = ptr;
}

// add the address to the list of breakpoints
void
debugger_set_pc_breakpoint(u16 pc, bool temporary)
{
	debugger_set_breakpoint(pc, md_cpu, temporary, false /*memory*/, false, false);
}

// remove the address from the list of breakpoints
static void
debugger_reset_breakpoint(u16 pc, mem_domain md, bool memory)
{
	bkpt *ptr, *prev;

	prev = 0L;
	ptr = breakpoints;
	while (ptr && ptr->pc != pc && ptr->md != md && ptr->memory != memory)
	{
		prev = ptr;
		ptr = ptr->next;
	}

	if (!ptr) return;

	if (prev)
		prev->next = ptr->next;
	else
		breakpoints = ptr->next;

	xfree(ptr);
}


// check to see if the address matches a breakpoint
// (called during emulation)
int 
debugger_check_breakpoint(u16 pc)
{
	bkpt *ptr = debugger_match_breakpoint_addr(pc, md_cpu, false);

	if (!ptr) return 0;
	if (ptr->temporary)
		debugger_reset_breakpoint(pc, md_cpu, false);

	return 1;
}

// add the address to the list of breakpoints

static DECL_SYMBOL_ACTION(debugger_breakpoint)
{
	if (task == csa_READ) {
		static bkpt *list;

		if (!iter) list = breakpoints;

		while (list && list->temporary)
			list = list->next;

		if (list == 0L)
			return 0;
		
		command_arg_set_num(SYM_ARG_1st, list->pc);
		list = list->next;
	} else {
		int val;

		command_arg_get_num(SYM_ARG_1st, &val);
		debugger_set_breakpoint(val, md_cpu, false /*temporary*/,
								false /*memory*/, false /*r*/, false /*w*/);
	}
	return 1;
}

static DECL_SYMBOL_ACTION(debugger_breakpoint_temporary)
{
	if (task == csa_READ) {
		static bkpt *list;

		if (!iter) list = breakpoints;

		while (list && !list->temporary)
			list = list->next;

		if (list == 0L)
			return 0;
		
		command_arg_set_num(SYM_ARG_1st, list->pc);
		list = list->next;
	} else {
		int val;

		command_arg_get_num(SYM_ARG_1st, &val);
		debugger_set_breakpoint(val, md_cpu, true /*temporary*/,
								false /*memory*/, false /*r*/, false /*w*/);
	}
	return 1;
}

static DECL_SYMBOL_ACTION(debugger_clear_breakpoint)
{
	int val;

	command_arg_get_num(SYM_ARG_1st, &val);
	debugger_reset_breakpoint(val, md_cpu, false /*memory*/);
	return 1;
}

static void
debug_on_memory_write_word(const mrstruct *mr, u32 addr, u16 val)
{
	if (mr->bkpt && (addr & 0xfffe) == (mr->bkpt->pc & 0xfffe))
		emulate_break();
	domain_write_word_mr(&mr->bkpt->origmr, mr->bkpt->md, addr, val);
}

static      u16
debug_on_memory_read_word(const mrstruct *mr, u32 addr)
{
	if (mr->bkpt && (addr & 0xfffe) == (mr->bkpt->pc & 0xfffe))
		emulate_break();
	return domain_read_word_mr(&mr->bkpt->origmr, mr->bkpt->md, addr);
}

static void
debug_on_memory_write_byte(const mrstruct *mr, u32 addr, s8 val)
{
	if (mr->bkpt && (addr & 0xffff) == mr->bkpt->pc)
		emulate_break();
	domain_write_byte_mr(&mr->bkpt->origmr, mr->bkpt->md, addr, val);
}

static      s8
debug_on_memory_read_byte(const mrstruct *mr, u32 addr)
{
	if (mr->bkpt && (addr & 0xffff) == mr->bkpt->pc)
		emulate_break();
	return domain_read_byte_mr(&mr->bkpt->origmr, mr->bkpt->md, addr);
}

static DECL_SYMBOL_ACTION(debugger_breakpoint_memory)
{
	if (task == csa_READ) {
		static bkpt *list;
		char mem[2];

		if (!iter) list = breakpoints;

		while (list && !list->memory)
			list = list->next;

		if (list == 0L)
			return 0;

		command_arg_set_num(SYM_ARG_1st, list->pc);
		mem[1] = 0;
		mem[0] = emulate_stringize_mem_type(list->md);
		command_arg_set_string(SYM_ARG_2nd, mem);
		switch (list->memread*2+list->memwrite) {
		case 0: 
		case 1:
		default:
			command_arg_set_string(SYM_ARG_3rd, "W");
			break;
		case 2:
			command_arg_set_string(SYM_ARG_3rd, "R");
			break;
		case 3:
			command_arg_set_string(SYM_ARG_3rd, "RW");
			break;
		}
		list = list->next;
	} else {
		int val;
		char *mem, *type;
		mem_domain md;
		bool memwrite, memread;
		bool rom;
		mrstruct *area;
		bkpt *bp;

		command_arg_get_num(SYM_ARG_1st, &val);
		command_arg_get_string(SYM_ARG_2nd, &mem);
		md = emulate_parse_mem_type(mem, &rom);
		command_arg_get_string(SYM_ARG_3rd, &type);
		memread = (strchr(type, 'R') || strchr(type, 'r'));
		memwrite = (strchr(type, 'W') || strchr(type, 'w'));

		area = THE_AREA(md, val&0xffff);
		if (!area) {
			logger(_L|LOG_USER|LOG_ERROR, _("no usable memory at that address"));
			return 0;
		}

		bp = debugger_set_breakpoint(val, md, false /*temporary*/, 
								true /*memory*/, memread, memwrite);

		if (area->bkpt) {
			logger(_L|LOG_USER|LOG_WARN, _("setting multiple memory breakpoints in the same 1k area; unsetting any breakpoint in this area turns them all off"));
			bp->origmr = area->bkpt->origmr;
		}
		else {
			bp->origmr = *area;
		}
		area->bkpt = bp;
		if (memread) {
			area->arearead = 0;
			area->read_word = debug_on_memory_read_word;
			area->read_byte = debug_on_memory_read_byte;
		}
		if (memwrite) {
			area->areawrite = 0;
			area->write_word = debug_on_memory_write_word;
			area->write_byte = debug_on_memory_write_byte;
		}
	}
	return 1;
}


static DECL_SYMBOL_ACTION(debugger_clear_memory_breakpoint)
{
	int val;
	char *mem, *type;
	mem_domain md;
	bool memwrite, memread;
	bool rom;
	mrstruct *area;
	bkpt *bp;

	command_arg_get_num(SYM_ARG_1st, &val);
	command_arg_get_string(SYM_ARG_2nd, &mem);
	md = emulate_parse_mem_type(mem, &rom);

	area = THE_AREA(md, val&0xffff);
	if (!area) {
		logger(_L|LOG_USER|LOG_ERROR, _("no usable memory at that address"));
		return 0;
	}

	if (!(bp = debugger_search_breakpoint(val, md, false, true))
		&& !(bp = debugger_search_breakpoint(val, md, true, true)))
		return 0;

	*area = bp->origmr;

	debugger_reset_breakpoint(val, md, true /*memory*/);
	return 1;
}


static DECL_SYMBOL_ACTION(debugger_list_breakpoints)
{
	static bkpt *list;

	list = breakpoints;

	logger(_L|LOG_USER, _("Active breakpoints:\n"));
	if (list == 0L)
	{
		logger(_L|LOG_USER, _("\t<none>\n"));
		return 1;
	}
		
	while (list)
	{
		logger(_L|LOG_USER, "\t>%04X", list->pc);
		if (list->memory)
			logger(_L|LOG_USER," (memory: %s %s%s)",
				   emulate_stringize_mem_type(list->md),
				   list->memread ? "R" : "",
				   list->memwrite ? "W" : "");
		logger(_L|LOG_USER, "\n");
		list = list->next;
	}
	return 1;
}


/*
 *	Entry point for debugger backend, entered before every instruction
 *	executed when ST_DEBUG is set in the stateflag.
 */

static Instruction last;
static bool last_valid;

void
debugger(void)
{
	Instruction inst;

	report_status(STATUS_DEBUG_REFRESH);

	// Show effects of previous instruction
	if (last_valid) {
		memory_update_views(&last);
		register_update_view(&last, wp);
		instruction_update_view(&last, true /*after*/);
	} else {
		debugger_memory_clear_views();
		debugger_register_clear_view();
		debugger_instruction_clear_view();
	}

	// Get a status word for this instruction
	statusto9900();

	// Decode the current instruction
	dis9900_decode(MEMORY_READ_WORD(pc), 
				   pc, wp, status, 
				   flat_read_word,
				   &inst);

	if (!last_valid || last.pc != inst.pc) {
		// Show current instruction
		instruction_update_view(&inst, false /*after*/);
	}

	// Save instruction
	last = inst;
	last_valid = true;
}

void
debugger_init(void)
{
	command_symbol_table *debugcommands =
	  command_symbol_table_new(_("Debugger Options"),
								 _("These commands control the debugger"),

		 command_symbol_new("BreakPoint|Break",
							_("Add a breakpoint at the given PC"),
							c_DYNAMIC|c_SESSION_ONLY,
							debugger_breakpoint,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("address"),
							 _("PC address at which to break"),
							 NULL /* action */ ,
							 NEW_ARG_NUM(u16),
							 NULL /* next */ )
							,

		 command_symbol_new("ClearBreakPoint|DeleteBreakpoint",
							_("Remove breakpoint at the given PC"),
							c_STATIC|c_DONT_SAVE,
							debugger_clear_breakpoint,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("address"),
							 _("PC address of breakpoint"),
							 NULL /* action */ ,
							 NEW_ARG_NUM(u16),
							 NULL /* next */ )
							,

		 command_symbol_new("BreakTemporary",
							_("Add temporary breakpoint at the given PC, "
							  "which will be removed upon being caught"),
							c_DYNAMIC|c_SESSION_ONLY,
							debugger_breakpoint_temporary,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("address"),
							 _("PC address of breakpoint"),
							 NULL /* action */ ,
							 NEW_ARG_NUM(u16),
							 NULL /* next */ )
							,

		 command_symbol_new("ListBreakPoints",
							_("List active breakpoints"),
							c_STATIC|c_DONT_SAVE,
							debugger_list_breakpoints,
							NULL /*ret*/,
							NULL /*args*/,

		 command_symbol_new("BreakMemory",
							_("Add a breakpoint on memory access"),
							c_DYNAMIC|c_SESSION_ONLY,
							debugger_breakpoint_memory,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("address"),
							 _("address at which to break"),
							 NULL /* action */ ,
							 NEW_ARG_NUM(u16),

							 command_arg_new_string
							 (_("type"),
							  _("memory type: C/V/G/S"),
							  NULL  /* action */,
							  NEW_ARG_STR (16),
							 command_arg_new_string
							 (_("access"),
							  _("access type: R/W/RW"),
							  NULL  /* action */,
							  NEW_ARG_STR (16),
							NULL /* next */ )))
							,

		 command_symbol_new("ClearMemoryBreakPoint|DeleteMemoryBreakpoint",
							_("Remove memory breakpoint at the given address"),
							c_STATIC|c_DONT_SAVE,
							debugger_clear_memory_breakpoint,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("address"),
							 _("PC address of breakpoint"),
							 NULL /* action */ ,
							 NEW_ARG_NUM(u16),

							 command_arg_new_string
							 (_("type"),
							  _("memory type: C/V/G/S"),
							  NULL  /* action */,
							  NEW_ARG_STR (16),
							  NULL /* next */ ))
							,


			NULL /*next*/)))))),
	  NULL /* sub */,
	NULL /* next */
    );

	command_symbol_table_add_subtable(universe, debugcommands);

	memset((void *)&views, 0, sizeof(views));
	register_view = 0;
}

/*
 *	Force next update to refresh all status items
 */
void 
debugger_refresh(void)
{
	last_valid = false;
}

void 
debugger_enable(bool enable)
{
//	debugger_refresh();
	if (enable) {
		if (!(stateflag & ST_DEBUG)) {
			system_debugger_enabled(true);
			stateflag |= ST_DEBUG;
			debugger_refresh();
		}
		debugger_change_verbosity(true);
		debugger();
	} else if (!enable) {
		stateflag &= ~ST_DEBUG;
		system_debugger_enabled(false);
	}
}

void
execution_pause(bool enable)
{
//1	debugger_refresh();
	if (enable) {
		stateflag |= ST_PAUSE;
	} else {
		stateflag &= ~ST_PAUSE;
	}
	system_execution_paused(execution_paused());
}


