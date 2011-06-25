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

/*
 *	Logging routines
 */

#ifndef __LOG_H__
#define __LOG_H__

#include <stdarg.h>
#include "v9t9_types.h"
#include "centry.h"

#ifdef __LOG__
static char *log_src[] =
{
	"General",
	"OS",
	"Timer",
	"Parser",
	"CPU",
	"Memory",
	"CRU",
	"ROM",
	"Video",
	"Sprites",
	"Sound",
	"Keyboard",
	"Speech",

	"RealDsk",
	"EmuDsk",
	"RS232",

	"Modules",		
	"Internal",
	"Demo",

	"Compiler",

	"PIO",
};
#endif

enum
{
	LOG_GENERAL	= 0,
	LOG_HOSTOS,
	LOG_TIMER,
	LOG_COMMANDS,
	LOG_CPU,
	LOG_MEMORY,
	LOG_CRU,
	LOG_ROMS,
	LOG_VIDEO,
	LOG_SPRITES,
	LOG_SOUND,
	LOG_KEYBOARD,
	LOG_SPEECH,

	LOG_REALDISK,
	LOG_EMUDISK,
	LOG_RS232,

	LOG_MODULES,	// v9t9 modules, not roms
	LOG_INTERNAL,	// assertion failures
	LOG_DEMO,		// demo

	LOG_COMPILER,

	LOG_PIO,

	LOG_NUM_SRC,

	LOG_SRC_MASK = 0x1f,
	LOG_SRC_SHIFT = 0,

	/////

	LOG_USER	= 0x100,		// user message, force visible

	// levels

	L_0			= 0 << 16,		// always
	L_1			= 1 << 16,		// level 1
	L_2			= 2 << 16,
	L_3			= 3 << 16,
	L_4			= 4 << 16,

	LOG_VERBOSE_MASK = 15 << 16,
	LOG_VERBOSE_SHIFT = 16,

	/////

	LOG_INFO	= 0 << 20,	// no header
	LOG_WARN	= 1 << 20,	// warning:
	LOG_ERROR	= 2 << 20,	// error:
	LOG_DEBUG	= 3 << 20,	// #ifdef DEBUG:  debug:
	LOG_FATAL	= 4 << 20,	// fatal: shows dialog and exits
	LOG_ABORT	= 5 << 20,	// fatal error: shows dialog and dumps core

	LOG_TYPE_MASK = 7 << 20
};

//	Test whether this message is aborts v9t9
#define LOG_IS_FATAL(f)	(((f) & LOG_TYPE_MASK) >= LOG_FATAL)
//	Test whether we echo this to the screen or not
#define LOG_IS_VISIBLE(f) (((f) & LOG_USER) || LOG_IS_FATAL(f))

//	Initialize log
void	initlog(void);
//	Add commands
void	log_add_commands(void);
//	Terminate log
void 	termlog(void);

void	log_set_level(int src, int level);
int		log_level(int src);
const char *log_name(int src);

//	Is logging enabled given these flags?
bool	log_enabled(int srcflags);

//	Return source flags coerced to add LOG_USER if necessary
int		log_coerced_to_user(int srcflags);

//	Log something.  srcflags=bitmask of LOG_xxx
void	vlogger(u32 srcflags, const char *format, va_list va);

void	_logger(u32 srcflags, const char *format, ...);

#if __MWERKS__
#define logger(flags, ...)	do { if (log_enabled(flags))_logger(flags, __VA_ARGS__); } while (0)
#else
#define logger(flags, format, args...) do { if (log_enabled(flags)) _logger(flags, format, ##args); } while (0)
#endif

//	Print an OS error
void	OSerror(int err, char *format, ...);

//	Definitions for various status items for the UI
//
//	The comments in (...) indicate the varargs provided to report_status().
typedef enum
{
	STATUS_CYCLES_SECOND,		// execution speed:
								// avg cycles per second (#),
								// (#) average insts per second
	STATUS_FRAMES_SECOND,		// video update frames per second (#)
	STATUS_DISK_ACCESS,			// disk DSR active (disk # 1-xx, bool on/off)
	STATUS_RS232_ACCESS,		// RS232 DSR active (port # 1-xx, bool on/off)
	STATUS_PIO_ACCESS,			// PIO DSR active (bool on/off)

	// these are only reported when ST_DEBUG is set in stateflag
	STATUS_DEBUG_REFRESH,		// for each cycle
	STATUS_CPU_PC,				// PC value (addr)
	STATUS_CPU_STATUS,			// status value (#)
	STATUS_CPU_WP,				// WP value (addr)
	STATUS_CPU_REGISTER_VIEW,	// register view changed (wp, ptr to 16 regs)
	STATUS_CPU_REGISTER_READ,	// register read (reg#, val)
	STATUS_CPU_REGISTER_WRITE,	// register changed (reg#, val)
	STATUS_CPU_INSTRUCTION,		// instruction (Instruction *, hex, opdisasm, op1, op2)
	STATUS_CPU_INSTRUCTION_LAST,// last instruction (Instruction *, op1, op2)

	// a memory view is 'debugger_memory_view_size' bytes long
	STATUS_MEMORY_VIEW,			// memory view changes (Memory*)
	STATUS_MEMORY_READ,			// memory read in view (Memory*)
	STATUS_MEMORY_WRITE,		// memory write in view (Memory*)
}	status_item;

//	Print a status item for frontend
//	The descriptions above tell what parameters will follow.
void
report_status(status_item item, ...);

//	Format status item into text, if verbosity allows it
void
report_status_text(status_item item, va_list va, char *buffer, int bufsz);

void	my_assert_func(char *file, int line, char *message);

void    my_assert_func(char *file, int line, char *message);

#define my_assert(x) \
do { if (!(x)) my_assert_func(__FILE__, __LINE__, #x); } while (0)

#define DEBUG

#include "cexit.h"

#endif


