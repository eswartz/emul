/*
  help.c						-- generate help text for commands
_
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

#include <stdarg.h>
#include <stdlib.h>
#include "v9t9_common.h"
#include "command.h"
#include "command_lexer.h"
#include "command_parser.h"
#include "system.h"

/*************************************************/

/*	Codes used in help text:

	\t  	-- indent block
	\b		-- end block
	\n		-- paragraph break
*/

static OSHandle helptext;

#define	MAXLINEWIDTH	256		// maximum line width
#define COLS			80		// # cols to use

/*	This struct holds information about a side of the screen.  */

#define SBUF 1024				// power of two, please!
#define SBUFMASK 1023
typedef struct {
	short       offset;			// start column (absolute)
	short       width;			// # cols of data (no blank space)
	char        buffer[SBUF];	// (circular) info waiting to be expelled
	short       bptr;			// ptr to start of buffer 
	short       blen;			// ptr to # chars waiting in buffer

	short       indent;			// # cols indented (absolute per line)
	short       vrow;			// virtual row of screen (for sync)
	short       vcol;			// virtual column (0--width-1)

	bool        atEOL;			// is the line ready to be flushed?
	bool        impInd;			// are we implicitly indenting?
} HelpBuf;

HelpBuf     helpbuf;

/*	Initialize a buffer */
static void
h_init(HelpBuf * s, short offset, short width)
{
	memset((void *) s, 0, sizeof(*s));

	s->offset = offset;
	s->width = width;
}

static void h_output(HelpBuf * helpbuf);

static void
hprintf(HelpBuf * s, char *format, ...)
{
	va_list     ap;
	char        buffer[256], *bptr;
	char       *text;

	va_start(ap, format);
	bptr = mvprintf(buffer, sizeof(buffer), format, ap);
	va_end(ap);

	text = bptr;
	while (*text) {
		if (s->blen >= SBUF)
			h_output(&helpbuf);

		if (s->blen < SBUF) {
			s->buffer[(s->bptr + s->blen) & SBUFMASK] = *text++;
			s->blen++;
		}
	}

	if (bptr != buffer)
		free(bptr);
}



/*
 *	Indent more 
 */
static void
h_indent(HelpBuf * s, short how)
{
	if (s->width - s->indent - how > (COLS / 8))	// minimum viewable
		s->indent += how;
	else if (s->width - s->indent - 1 > (COLS / 10))
		s->indent++;			// mini-indent
}

/*
 *	Indent less 
 */
static void
h_outdent(HelpBuf * s, short how)
{
	if (s->width - s->indent < (COLS / 8))
		s->indent++;			// we mini-indented
	else if (s->indent >= how)
		s->indent -= how;
}


//  This is the only buffer printed to the screen (via HelpBufs)
//
static char outLine[MAXLINEWIDTH];	// always enough (s->width)

static      bool
isBreaker(char c)
{
	return c == '\n' || c == '\r' || c == ' ' || c == '\t' || c == '\b'
		|| c == '|';
}

/*	
 *	Dump one line from a buffer, without breaking words
 *	
 *	Allow dumping an empty buffer.
 */
static void
h_dumpline(HelpBuf * s)
{
	short       col;
	short       len;			// # chars in line[]
	short       afterspace, eol = 0, ind = 0;


	col = s->offset + s->indent;	// where to store
	s->vcol = s->indent;
	len = 0;
	afterspace = s->width - s->indent;	// maximum # chars if no other breakers

//  memset(outLine+s->offset, ' ', s->width);               // TEST init

	while ((s->blen > 0) && (s->vcol < s->width) && !(eol || ind)) {
		char        c = s->buffer[s->bptr];

		outLine[col + len++] = c;
		s->vcol++;

		if (isBreaker(c))		// breakers
		{
			afterspace = len;
			eol = (c == '\n' || c == '\r');	// end of line/section
			eol += (c == '\r');	// 1=section break, 2=line break
			ind = (c == '\t' || c == '\b');	// indent section
			ind += (c == '\b');	// 1==in, 2==out
		}

		s->bptr = (s->bptr + 1) & SBUFMASK;
		s->blen--;
	}

	if (s->blen != 0 || (eol || ind)) {
		//  put back what we ignored    
		s->blen += (len - afterspace);
		s->bptr = (s->bptr - (len - afterspace)) & SBUFMASK;

		if (eol || ind) {
			len++;				// eat up control char
			afterspace--;
		}

		while (afterspace < len)
			outLine[col + (--len)] = ' ';	// erase wrapped partofword
	}

	s->vcol = 0;
	s->vrow++;
	s->atEOL = (eol == 1) || (ind) || (s->blen == 0);

	if (s->atEOL || ind)
		if (s->impInd) {
			h_outdent(s, (COLS / 40));
			s->impInd = false;
		}

	if (ind) {
		if (ind == 1)
			h_indent(s, (COLS / 25));
		else
			h_outdent(s, (COLS / 25));
	}
}

static void
h_line(HelpBuf * helpbuf, char ch)
{
	char        line[256];

	memset(line, ch, 255);
	line[255] = 0;

	hprintf(helpbuf, "%.*s\r", helpbuf->width - helpbuf->indent - 2, line);
}

static void
h_printline(void)
{
	char *ptr = outLine + COLS - 1;
	while (ptr > outLine && isspace(*(ptr-1)))
		*--ptr = 0;
	*ptr++ = '\n'; *ptr= 0;
	OS_AppendHandle(&helptext, outLine, ptr - outLine);
}

static void
h_output(HelpBuf * helpbuf)
{
	while (helpbuf->blen)		// while there's stuff to dump...
	{
		memset(outLine, ' ', COLS);
		outLine[COLS] = '\n';

		if (helpbuf->atEOL)
			helpbuf->atEOL = false;

		if (!helpbuf->atEOL)
			h_dumpline(helpbuf);

		// output generated line
		h_printline();
	}
}

/*************************************/

static void
command_arg_help(command_arg * arg)
{
	const char *ptr;
	char *dup, *dupptr;
	while (arg) {
		/* clean up the argument name to remove hidden args (||)
		   and enumerations (=...) */
		ptr = arg->name;
		dup = dupptr = xstrdup(arg->name);
		while (*ptr) {
			if (*ptr == '|' && *(ptr+1) == '|')
				break;
			if (*ptr == '=') {
				while (*ptr && *ptr != '|')
					ptr++;
			}
			else
				*dupptr++ = *ptr++;
		}
		*dupptr = 0;

		hprintf(&helpbuf, "%s (%s): %s\r",
				dup,
				command_expr_atom_name(arg->type), 
				arg->help ? arg->help : "");
		xfree(dup);

		h_output(&helpbuf);
		arg = arg->next;
	}
}

static void
command_symbol_help(command_symbol * sym)
{
	char       *ptr = sym->name;

	if (sym->help) {
		while (*ptr && *ptr != '|')
			ptr++;
		hprintf(&helpbuf, "%.*s: %s\n\t",
				ptr - sym->name, sym->name, sym->help);
		if (sym->flags == c_DONT_SAVE) {
			hprintf(&helpbuf, _("Not saved to configuration file\r"));
		} else if (sym->flags & c_SESSION_ONLY) {
			hprintf(&helpbuf, _("Only saved to session files\r"));
		} else if (sym->flags & c_CONFIG_ONLY) {
			hprintf(&helpbuf, _("Only saved to config files\r"));
		}
		if (sym->args) {
			hprintf(&helpbuf, _("Arguments%s:\t"),
					(sym->flags & c_OPTIONAL_ARGS) ? _(" (optional)") : "");
			command_arg_help(sym->args);
			hprintf(&helpbuf, "\b");
		}
		if (sym->args && sym->ret == sym->args) {
			hprintf(&helpbuf, _("Returns first argument\r"));
		} else if (sym->ret != NULL) {
			hprintf(&helpbuf, _("Returns:\r"));
			command_arg_help(sym->ret);
		}
		hprintf(&helpbuf, "\b");
		h_output(&helpbuf);
	}
}

static void
command_symbols_help(command_symbol * sym)
{
	while (sym) {
		command_symbol_help(sym);
		sym = sym->next;
	}
}

static void
command_symbol_table_help(command_symbol_table * table)
{
	while (table) {
		if (table->name) {
			h_line(&helpbuf, '=');
			hprintf(&helpbuf, "%s\r", table->name);
		}
		if (table->help) {
			h_line(&helpbuf, '=');
			hprintf(&helpbuf, "%s\r", table->help);
		}

		h_line(&helpbuf, '-');

		if (table->list)
			command_symbols_help(table->list);

		if (table->sub) {
			hprintf(&helpbuf, "\t");
			h_output(&helpbuf);
			command_symbol_table_help(table->sub);
			hprintf(&helpbuf, "\b");
		}

		h_output(&helpbuf);
		table = table->next;
	}
}

void
command_help_symbol(const char *var)
{
	void       *ptr;
	command_symbol *sym;
	OSSize		sz;

	OS_NewHandle(0, &helptext);
	h_init(&helpbuf, 0, COLS);

	if (command_match_symbol(universe, var, &sym))
		command_symbol_help(sym);
	else
		hprintf(&helpbuf, _("No command matches '%s'\n"), var);

	h_output(&helpbuf);
	ptr = OS_LockHandle(&helptext);
	OS_GetHandleSize(&helptext, &sz);
	((char *)ptr)[sz-1] = 0;
	system_log(LOG_USER, ptr);
	OS_UnlockHandle(&helptext);
	OS_FreeHandle(&helptext);
}

void
command_help(void)
{
	void       *ptr;
	OSSize		sz;

	OS_NewHandle(0, &helptext);
	h_init(&helpbuf, 0, COLS);

	if (universe)
		command_symbol_table_help(universe);
	else
		hprintf(&helpbuf, _("No commands defined\n"));

	h_output(&helpbuf);
	ptr = OS_LockHandle(&helptext);
	OS_GetHandleSize(&helptext, &sz);
	((char *)ptr)[sz-1] = 0;
	system_log(LOG_USER, ptr);
	OS_UnlockHandle(&helptext);
	OS_FreeHandle(&helptext);
}
