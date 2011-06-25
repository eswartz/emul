/*
  command_rl.c					-- interface to GNU readline for command parser

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

#include "v9t9_common.h"
#include "command.h"
#include "command_rl.h"

#if HAVE_READLINE
#ifdef __unix__
#include <readline/readline.h>
#else
#include <readline.h>
#endif
#endif

#include <signal.h>

static command_symbol **command_matches;
static int  command_nmatches;
static int  command_match_idx;
static char *command_match_idx_ptr;

/*	state is 0 for first time through, increments until 0L is returned */
static char *
commands_matching(const char *text, int state)
{
	int         ln;
	char       *ret;

	if (!state) {
		if (command_matches) {
			xfree(command_matches);
			command_matches = NULL;
			command_nmatches = 0;
		}
		command_match_symbols(universe, text,
							  &command_matches, &command_nmatches);
		command_match_idx = 0;
		command_match_idx_ptr = 0;
	}

	if (command_match_idx >= command_nmatches)
		return NULL;

	if (!command_match_idx_ptr || !*command_match_idx_ptr)
		command_match_idx_ptr = command_matches[command_match_idx]->name;

	ln = 0;
	while (command_match_idx_ptr[ln]
		   && command_match_idx_ptr[ln] != '|')
		ln++;

	ret = xstrdup(command_match_idx_ptr);
	ret[ln] = 0;

	command_match_idx_ptr += ln;
	if (*command_match_idx_ptr == '|')
		command_match_idx_ptr++;

	/* at the end of the name list, or at the first secret name,
	   skip to next entry */
	if (!*command_match_idx_ptr || *command_match_idx_ptr == '|') {
		command_match_idx++;
		command_match_idx_ptr = 0L;
	}

	return ret;
}

#if HAVE_READLINE
/*
 *	this is used by readline and by GTK as well
 */
char **
readline_completion(const char *text, int start, int end)
{
	char      **matches = NULL;
	int         nwc = 1;		// new command?
	int         ptr;

	ptr = -start;
	while (ptr < 0) {
		if (text[ptr] == '\"')
			nwc = !nwc;
		ptr++;
	}

//!!! detect readline 4.2 vs later
#if 0 && _WIN32
	if (nwc && *text)
		matches = completion_matches(text, commands_matching);
#else
	if (nwc && *text)
		matches = rl_completion_matches(text, commands_matching);
#endif

	return matches;
}
#endif

static void
emit_startup_help(FILE *out)
{
	fprintf(out, _("\n%s (a TI-99/4A Emulator) version %s\n"
			"%s comes with ABSOLUTELY NO WARRANTY; for details type 'license'\n"
			"Type 'exit' to continue emulating, 'quit' to quit %s\n\n"),
			PACKAGE, VERSION,
			PACKAGE,
			PACKAGE);
}

static int emitted_help = 0;

#if HAVE_READLINE

void
readline_getcommands(FILE * in, FILE * out)
{
	if (!emitted_help) {
		emit_startup_help(out);
		emitted_help = 1;
	}

	while ((stateflag & ST_INTERACTIVE) && !feof(in)) {
		fprintf (out, "stateflag=%x\n", stateflag);
		readline_getcommand(in, out);
	}
}

void
readline_getcommand(FILE *in, FILE *out)
{
	char       *buf;

	rl_catch_signals = 0;

	rl_instream = in;
	rl_outstream = out;
	rl_readline_name = PACKAGE;
	rl_basic_word_break_characters = "\t\n\"\\'`@$<>=;|&{(,";
	rl_attempted_completion_function = readline_completion;
	
	buf = readline(_("Enter a command> "));
	if (buf) {
#warning add_history?		
//				if (*buf)
//					add_history(buf);
		OS_LockMutex(&command_mutex);
		command_exec_text(buf);
		OS_UnlockMutex(&command_mutex);
		free(buf);
	} else
		fprintf(out, _("<EOF>\n"));
}

#else // !HAVE_READLINE

void
readline_getcommands(FILE * in, FILE * out)
{
	if (!emitted_help) {
		emit_startup_help(out);
		emitted_help = 1;
	}

	while ((stateflag & ST_INTERACTIVE) && !feof(in)) {
		fprintf (out, "stateflag=%x\n", stateflag);
		readline_getcommand(in, out);
	}
}

void
readline_getcommand(FILE *in, FILE *out)
{
	char       buf[1024];
	
	fprintf(out, _("Enter a command> "));
	fgets(buf, sizeof(buf), in);
	if (*buf)
		command_exec_text(buf);
	else	
		command_exec_text("Die\n");
}

#endif
