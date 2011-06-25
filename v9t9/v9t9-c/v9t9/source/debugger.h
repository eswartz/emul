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

#ifndef __DEBUGGER_H__
#define __DEBUGGER_H__

#include "memory.h"

#include "centry.h"

void debugger_init(void);
void debugger_enable(bool enable);
INLINE bool debugger_enabled(void) { return !!(stateflag & ST_DEBUG); }
void debugger(void);


//	memory views
typedef enum
{
	MEMORY_VIEW_CPU_1,
	MEMORY_VIEW_CPU_2,
	MEMORY_VIEW_VIDEO,
	MEMORY_VIEW_GRAPHICS,
	MEMORY_VIEW_SPEECH,
	MEMORY_VIEW_COUNT
}	MemoryView;

//	Struct keeps track of active addresses in the
//	areas of memory so frontend can maintain views
typedef struct Memory {
	MemoryView	which;			// MEMORY_VIEW_xxx
	u16			base;			// base address 
	u16         addr;			// last accessed addr
	int			len;			// last accessed length of memory
	u8			*mem;			// pointer to that memory
	int			coverage;		// amount of times selected
} Memory;

//	Size of area Memory is expected to cover (changed by frontend)
extern int debugger_memory_view_size[MEMORY_VIEW_COUNT];

// 	Send verbose operand views (changed by frontend)
extern bool debugger_operand_view_verbose;

#define DOMAIN_TOKEN(dmn)		((dmn) == md_cpu ? '>' : \
								(dmn) == md_graphics ? 'G' : \
								(dmn) == md_video ? 'V' : \
								(dmn) == md_speech ? 'S' : '?')

#define MEMORY_VIEW_TOKEN(v)	((v) == MEMORY_VIEW_CPU_1 ? '>' : \
								(v) == MEMORY_VIEW_CPU_2 ? '>' : \
								(v) == MEMORY_VIEW_GRAPHICS ? 'G' : \
								(v) == MEMORY_VIEW_VIDEO ? 'V' : \
								(v) == MEMORY_VIEW_SPEECH ? 'S' : '?')

/*
 *	Utility for status reporters.  Given the slot, it writes a one-line hex dump to
 *	the given buffer, and sets start/astart and end/aend to point to the extent
 *	of the last memory access within the buffer.  (These will be spaces.)
 *
 *	addr_separator: char appearing between address and bytes
 *	byte_separator: char appearing between each hex byte
 *	ascii_separator: char appearing between hex field and ascii field
 */	
void
debugger_hex_dump_line(Memory * slot, int offset, int length,
					   char addr_separator, char byte_separator, 
					   char ascii_separator, char line_separator,
					   char *buffer, int bufsz,
					   char **start, char **end,
					   char **astart, char **aend);

/*
 *	How long will this text be?
 */
int 
debugger_hex_dump_bytes_to_chars(int bytes);

/*
 *	How many bytes fit in this length?
 */
int 
debugger_hex_dump_chars_to_bytes(int chars);

/*
 *	Force updates of these items by sending system_report_status() messages
 */ 
void
debugger_register_clear_view(void);
void
debugger_memory_clear_views(void);
void
debugger_instruction_clear_view(void);

/*
 *	Force next update to refresh all status items
 */
void 
debugger_refresh(void);

/*
 *	Check current PC to see if it's breakpointed
 */
int
debugger_check_breakpoint(u16 pc);

/*
 * 	Add the address to the list of breakpoints
 *	(a temporary breakpoint is deleted after being hit)
 */
void
debugger_set_pc_breakpoint(u16 pc, bool temporary);

u16 flat_read_word(u16 addr);

#include "cexit.h"

#endif

