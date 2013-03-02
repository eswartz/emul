/*
  log.c							-- message logging routines

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

#include <stdarg.h>
#include <stdio.h>
#include <string.h>

#define __LOG__

#include "v9t9_common.h"
#include "system.h"
#include "v9t9.h"
#include "command.h"
#include "debugger.h"
#include "dis9900.h"
#include "dsr.h"
#include "log.h"

//  Verbosity level (0=minimum, ...) for each log subsystem
static int  loglevel[32];

//  Error buffer
static char strbuf[1024], *strbufptr;
static int strbufidx;

static void
lstart(void)
{
	strbufptr = strbuf;
	*strbufptr = 0;
	strbufidx = 0;
}

static void
lend(u32 srcflags)
{
	system_log(srcflags, strbufptr);
	if (strbufptr != strbuf) xfree(strbufptr);
}


static void
vlprintf(const char *format, va_list va)
{
	char appbuf[256], *append;
	int         applen;
	char *ptr;

	// append may or may not be appbuf
	append = mvprintf(appbuf, sizeof(appbuf),
					  format, va);
	if (!append)
		exit(1);

	applen = strlen(append);

	// strbufptr may not have enough space to append this,
	// or it may have already been allocated from this heap:
	// if so, get memory for the new string and construct it

	if (strbufptr != strbuf || applen + strbufidx >= sizeof(strbuf)) {
		ptr = (char *)xmalloc(applen + strbufidx + 1);

		memcpy(ptr, strbufptr, strbufidx);
		memcpy(ptr + strbufidx, append, applen + 1);

		if (strbufptr != strbuf)
			xfree(strbufptr);
		strbufptr = ptr;
		strbufidx += applen;

		if (append != appbuf)
			xfree(append);
	} else {
		memcpy(strbufptr + strbufidx, append, applen + 1);
		strbufidx += applen;
	}
}

static void
lprintf(const char *format, ...)
{
	va_list     va;

	va_start(va, format);
	vlprintf(format, va);
	va_end(va);
}

///////////////////////////////////

static      u32
lookup_system_name(char *name)
{
	int         idx;
	
	if (strcasecmp(name, "all") == 0)
		return ~0;
	if (!*name)
		return 1<<LOG_GENERAL;

	for (idx = 0; idx < sizeof(log_src) / sizeof(char *); idx++) {
		if (symbol_match(log_src[idx], name, 3) != 0)
			return 1 << idx;
	}
	return 0;
}

void log_set_level(int src, int level)
{
	loglevel[src] = level;
}

static
DECL_SYMBOL_ACTION(command_toggle_logging)
{
	int         level;
	char       *name;
	u32         mask, bit;
	int			idx;

	if (task == csa_READ)
	{
		if (iter >= sizeof(log_src) / sizeof(log_src[0]))
			return 0;
		command_arg_set_string(SYM_ARG_1st, log_src[iter]);
		command_arg_set_num(SYM_ARG_2nd, loglevel[iter]);
		return 1;
	}
	
	command_arg_get_string(SYM_ARG_1st, &name);
	command_arg_get_num(SYM_ARG_2nd, &level);

	if ((mask = lookup_system_name(name)) == 0) {
		logger(LOG_ERROR | LOG_USER, _("Unknown subsystem '%s'\n"), name);
		return 0;
	}

	for (idx = 0, bit = 1; 
		 idx < LOG_NUM_SRC; 
		 idx ++, bit += bit) {
		if (mask & bit) {
			log_set_level(idx, level);
		}
	}

	return 1;
}

static
DECL_SYMBOL_ACTION(command_list_log_systems)
{
	int         idx;

	logger(LOG_USER, _("Supported logging subsystems:"));
	for (idx = 0; idx < sizeof(log_src) / sizeof(char *); idx++) {
		logger(LOG_USER, _("\t%20s\t(level=%d)"), 
			   log_src[idx], loglevel[idx]);
	}
	logger(LOG_USER, "\n");
	return 1;
}

/*
static
DECL_SYMBOL_ACTION(command_log_verbosity)
{
	int         delta;

	if (task == csa_READ)
	{
		if (!iter)
		{
			command_arg_set_num(sym->args, logverbosity);
			return 1;
		}
		return 0;
	}
	command_arg_get_num(sym->args, &delta);
	log_verbose(delta);
	return 1;
}

static
DECL_SYMBOL_ACTION(command_log_visible_verbosity)
{
	int         delta;

	if (task == csa_READ)
	{
		if (!iter)
		{
			command_arg_set_num(sym->args, logvisibleverbosity);
			return 1;
		}
		return 0;
	}
	command_arg_get_num(sym->args, &delta);
	log_visible_verbose(delta);
	return 1;
}
*/

void
log_add_commands(void)
{
	command_symbol_table *logs = command_symbol_table_new(_("Logging Commands"),
														  _("These options control sources and verbosity of logging"),

	  command_symbol_new
	  ("Log",
	   _("Toggle logging for given subsystem"),
	   c_DYNAMIC,
	   command_toggle_logging  /* action */ ,
	   RET_FIRST_ARG		   /* ret */ ,
		command_arg_new_string
		(_("system"),
		 _("one of the subsystems from ListLogSystems"),
		 NULL,
		 NEW_ARG_STR(10),
	   command_arg_new_num
	   (_("log level"),
		_("level of verbosity (0=off...9=max); "
		"a negative value sends output only to file, else output "
		"is copied to console or log window"),
		NULL,
		NEW_ARG_NUM(int),
		NULL /* next */)),

	   command_symbol_new
	   ("ListLogSystems",
		_("List targets for logging"),
		c_DONT_SAVE,
		command_list_log_systems	/* action */ ,
		NULL /* ret */ ,
		NULL,

#if 0
		command_symbol_new
		("LogVerbosity",
		 _("Change logging verbosity for log file"),
		 c_DYNAMIC,
		 command_log_verbosity	 	/* action */ ,
		 RET_FIRST_ARG				/* ret */ ,
		 command_arg_new_num
		 (_("delta"),
		  _("delta>0 increases verbosity, delta<0 decreases verbosity"),
		  NULL,
		  NEW_ARG_NUM(u8),
		  NULL	/* next */
		 ),

		 command_symbol_new
		 ("ConsoleLogVerbosity",
		  _("Change logging verbosity for messages echoed to console; "
		  "note:  LogVerbosity is treated as being at least ConsoleLogVerbosity, "
		  "so messages will always be logged to disk"),
		  c_DYNAMIC,
		  command_log_visible_verbosity	/* action */ ,
		  RET_FIRST_ARG					/* ret */ ,
		  command_arg_new_num
		  (_("delta"),
		   _("delta>0 increases verbosity, delta<0 decreases verbosity"),
		   NULL,
		   NEW_ARG_NUM
		   (u8),
		   NULL	/* next */
		  ),
#endif
		  NULL	/* next */
		 )),

	  NULL /* sub */ ,

	  NULL	/* next */
	);

	command_symbol_table_add_subtable(universe, logs);

}

//  Initialize log
void
initlog(void)
{
	memset(loglevel, 0, sizeof(loglevel));
//	system_initlog();
}

//  Terminate log
void
termlog(void)
{
//	system_termlog();
}

static int log_2(u32 mask)
{
	int idx = 0;
	u32 bit = 1;
	while (idx < LOG_NUM_SRC && !(mask & bit)) {
		idx++;
		bit <<=1;
	}
	return idx;
}

#define log_abs(x) ((x) < 0 ? -(x) : (x))

int
log_level(int src)
{
	return log_abs(loglevel[src]);
}

const char *
log_name(int src)
{
	if (src >= LOG_GENERAL && src < LOG_NUM_SRC)
		if (*log_src[src])
			return log_src[src];
		else
			return "General";
	else
		return 0L;
}

//	Is logging enabled given these flags?
bool
log_enabled(int srcflags)
{
	int         verb = (srcflags & LOG_VERBOSE_MASK) >> LOG_VERBOSE_SHIFT;
	int			srcidx = (srcflags & LOG_SRC_MASK);

	// LOG_USER or >= LOG_FATAL forces output
	if (!LOG_IS_VISIBLE(srcflags)) {
		// verbosity?
		if (verb > log_abs(loglevel[srcidx]))
			return false;
	}
	return true;
}

//	Return source flags coerced to add LOG_USER if necessary
int
log_coerced_to_user(int srcflags)
{
	int         verb = (srcflags & LOG_VERBOSE_MASK) >> LOG_VERBOSE_SHIFT;
	int			srcidx = (srcflags & LOG_SRC_MASK);

	return srcflags | (verb < loglevel[srcidx] ? LOG_USER : 0);
}


//  Log something.  srcflags=LOG_xxx
void
vlogger(u32 srcflags, const char *format, va_list va)
{
	static u32	lastsrc = 0;

	if (!log_enabled(srcflags)) 
		return;

	// start new buffer
	lstart();

	// start new line?
	if (lastsrc != (srcflags & (LOG_SRC_MASK|LOG_TYPE_MASK)) &&
		(srcflags & LOG_SRC_MASK)) {
		lastsrc = srcflags & (LOG_SRC_MASK|LOG_TYPE_MASK);

#ifndef DEBUG
		if ((srcflags & LOG_TYPE_MASK) == LOG_DEBUG)
			return;
#endif
	}
	// emit formatted text
	vlprintf(format, va);

	// dump text
	// if loglevel is positive, LOG_USER will always be set (else
	// we would have exited above); if loglevel is negative,
	// LOG_USER will never be set.
	lend(log_coerced_to_user(srcflags));

	// die if it was fatal
	if (LOG_IS_FATAL(srcflags)) {
		v9t9_term(1);
		fflush(stdout); fflush(stderr);
		if ((srcflags & LOG_TYPE_MASK) == LOG_ABORT) {
			abort();
		} else if ((srcflags & LOG_TYPE_MASK) == LOG_FATAL) {
			v9t9_term(1);
			exit(1);
		}
	}
}

void
_logger(u32 flags, const char *format, ...)
{
	va_list     va;

	va_start(va, format);
	vlogger(flags, format, va);
	va_end(va);
}


void
OSerror(int err, char *format, ...)
{
	va_list     ap;

	va_start(ap, format);

	lstart();
	lprintf(_("\nOS error:\t"));
	vlprintf(format, ap);
	lprintf(_("%s (error %d)\n"), OS_GetErrText(err), err);
	lend(LOG_USER);
	va_end(ap);
}

//	Print a status item for frontend
void
report_status(status_item item, ...)
{
	va_list va;
	va_start(va, item);
	system_report_status(item, va);
	va_end(va);
}

//	Format status item into text, if verbosity allows it
void
report_status_text(status_item item, va_list va,
				   char *buffer, int bufsz)

{
	*buffer = 0;
	if (bufsz < 32)	return;

	switch (item)
	{
	case STATUS_CYCLES_SECOND:
	case STATUS_FRAMES_SECOND:
		break;

	case STATUS_DISK_ACCESS:
	{
		int disk, onoff;
		disk = va_arg(va, int);
		onoff = va_arg(va, int);

		if (log_enabled((dsr_is_emu_disk(disk) ? LOG_EMUDISK : LOG_REALDISK) | L_1))
			sprintf(buffer, _("Disk %d %s\n"),
					disk,
					onoff ? _("on") : _("off"));
		break;
	}
	case STATUS_RS232_ACCESS:
	{
		if (log_enabled(LOG_RS232 | L_1))
			sprintf(buffer, _("RS232/%d %s\n"),
					va_arg(va, int),
					va_arg(va, int) ? _("on") : _("off"));
		break;
	}

	case STATUS_PIO_ACCESS:
	{
		if (log_enabled(LOG_PIO | L_1))
			sprintf(buffer, _("PIO %s\n"),
					va_arg(va, int) ? _("on") : _("off"));
		break;
	}

	case STATUS_DEBUG_REFRESH:
		break;

	case STATUS_CPU_PC:
		if (log_enabled(LOG_CPU | L_3))
			sprintf(buffer, "[PC=>%04X] ",
				va_arg(va, int));
		break;
	case STATUS_CPU_STATUS:
		if (log_enabled(LOG_CPU | L_3))
			sprintf(buffer, "[ST=>%04X] ",
				va_arg(va, int));
		break;
	case STATUS_CPU_WP:
		if (log_enabled(LOG_CPU | L_2))
			sprintf(buffer, "[WP=>%04X] ",
				va_arg(va, int));
		break;

	case STATUS_CPU_REGISTER_VIEW:
	{
		if (log_enabled(LOG_CPU | L_1)) 
		{
			int reg;
			char *ptr = buffer;
			u16 wp = va_arg(va, int);
			u16 *regs = va_arg(va, u16 *);
		
			ptr += sprintf(ptr, "WP=>%04X\n", wp);
			for (reg = 0; reg < 16; reg++) {
				if (ptr + 12 > buffer + bufsz) break;
				ptr += sprintf(ptr, "R%1X=%04X  ",
							   reg, regs[reg]);
				if ((reg & 7) == 7) {
					ptr += sprintf(ptr, "\n");
				}
			}
			*ptr = 0;
		}
		break;
	}

	case STATUS_CPU_REGISTER_READ:
	{
		if (log_enabled(LOG_CPU | L_2))
		{
			int reg, val;
			reg = va_arg(va, int);
			val = va_arg(va, int);
			sprintf(buffer, "[Read R%d=>%04X] ", 
				reg, val);
		}
		break;
	}

	case STATUS_CPU_REGISTER_WRITE:
	{
		if (log_enabled(LOG_CPU | L_2))
		{
			int reg, val;
			reg = va_arg(va, int);
			val = va_arg(va, int);
			sprintf(buffer, "[Wrote R%d=>%04X] ", 
				reg, val);
		}
		break;
	}

	case STATUS_CPU_INSTRUCTION:
	{
		if (log_enabled(LOG_CPU | L_1))
		{
			Instruction *inst;
			char *hex, *disasm, *op1, *op2;
			inst = va_arg(va, Instruction *);
			hex = va_arg(va, char *);
			disasm = va_arg(va, char *);
			op1 = va_arg(va, char *);
			op2 = va_arg(va, char *);
		
			snprintf(buffer, bufsz, "%-10s %-5s %-22s %s%s%s%s%s%s\n",
					 hex, inst->name, disasm,
					 op1 ? "[" : "", op1 ? op1 : "", op1 ? "]" : "",
					 op2 ? " [" : "", op2 ? op2 : "", op2 ? "]" : "");
		}
		break;
	}

	case STATUS_CPU_INSTRUCTION_LAST:
	{
		if (log_enabled(LOG_CPU | L_1))
		{
			Instruction *inst;
			char tmp[32];
			char *op1, *op2;
		
			inst = va_arg(va, Instruction *);
			op1 = va_arg(va, char *);
			op2 = va_arg(va, char *);
		
			if (inst) {
				sprintf(tmp, "%s%s%s%s%s%s",
						op1 ? "{" : "", op1 ? op1 : "", op1 ? "}" : "",
						op2 ? " {" : "", op2 ? op2 : "", op2 ? "}" : "");
				sprintf(buffer, "%-16s", tmp);
			} else {
				sprintf(buffer, "%-16s", "");
			}
		}
		break;
	}

	case STATUS_MEMORY_VIEW:
	{
		if (log_enabled(LOG_MEMORY | L_1))
		{
			Memory *s = va_arg(va, Memory *);
			char *start, *end, *astart, *aend;

			debugger_hex_dump_line(s, 0, debugger_memory_view_size[s->which],
								   '=', ' ', ' ', '\n',
								   buffer, bufsz,
								   &start, &end,
								   &astart, &aend);
			if (start) *(start-1) = '|';
			if (end) *end = '|';
		}
		break;
	}
	case STATUS_MEMORY_READ:
	{
		if (log_enabled(LOG_MEMORY | L_2))
		{
			Memory *s = va_arg(va, Memory *);
			sprintf(buffer, "[Read %d bytes at >%04X] ",
					s->len, s->addr);
		}
		break;
	}
	case STATUS_MEMORY_WRITE:
	{
		if (log_enabled(LOG_MEMORY | L_2))
		{
			Memory *s = va_arg(va, Memory *);
			sprintf(buffer, "[Wrote %d bytes at >%04X] ",
					s->len, s->addr);
		}
		break;
	}

	}
}

void
my_assert_func(char *file, int line, char *message)
{
	logger(LOG_FATAL | LOG_ABORT, _("assertion failed:  %s:%d: %s\n"),
		   file, line, message);
}


