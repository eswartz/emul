/*
  config.c						-- configuration/session file utilities

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

/*
 *	Configuration stuff.
 *
 *	This code supports saving changes to the configuration file.
 *	
 */

#include <stdarg.h>
#include "v9t9.h"
#include "v9t9_common.h"
#include "command.h"
#include "configfile.h"

#define _L	LOG_COMMANDS | LOG_INFO

static OSHandle config;

/*
 *	Escape an outgoing quoted string
 */
char *escape(const char *s, int quote)
{
	char *ret;
	char *ptr;
	
	if (!s)
		return xstrdup("");
		
	ret = (char *)xmalloc(strlen(s)*2+1);
	ptr = ret;
	while (*s) {
		if (*s == '\\' || 
			(quote == '\"' && *s == '\"') || 
			(quote == '\'' && *s == '\''))
		{
			*ptr++ = '\\';
		}
		*ptr++ = *s++;
	}
	*ptr = 0;
	
	return ret;
}

static void  cprintf(const char *format, ...)
{
	va_list va;
	char tmp[256], *ptr;

	va_start(va, format);
	ptr = mvprintf(tmp, sizeof(tmp), format, va);
	if (OS_AppendHandle(&config, ptr, strlen(ptr)) != OS_NOERR)
		logger(_L | LOG_FATAL, _("Out of memory for config file\n"));
	if (ptr != tmp) xfree(ptr);
}

static void dump_name(command_symbol *sym)
{
	char *ptr = sym->name;
	int len = 0;
	while (ptr[len] && ptr[len] != '|')
		len++;
	cprintf("%.*s ", len, ptr);
}

static void dump_command(command_symbol *sym, bool session)
{
	command_arg *arg;
	int iter = 0;
	int ret;

	// don't save certain active commands
	if (sym->flags & c_DONT_SAVE)
		return;

	// don't save machine state unless it's a session
	if ((sym->flags & c_SESSION_ONLY) && !session)
		return;

	// don't save state unless it's not a session
	if ((sym->flags & c_CONFIG_ONLY) && session)
		return;

	do {

		if ((sym->flags & c_DYNAMIC) && sym->action) {
			ret = sym->action(sym, csa_READ, iter);
		} else {
			ret = (iter == 0);
		}
		
		if (!ret)
			break;

		dump_name(sym);

		arg = sym->args;
		while (arg) {
			char *str;
			int val;

			if (arg->type == ca_NUM) {
				command_arg_read_num(arg, &val);
				/*
				if (strcmp(arg->name, "on|off") == 0 || strcmp(arg->name, "off|on") == 0)
					cprintf("%s ", val ? "on" : "off");
				else if (arg->u.num.sz > 2)
					cprintf("%d ", val);
				else
				*/
					cprintf("0x%x ", arg->u.num.sz == 1 ? (val & 0xff) :
							arg->u.num.sz == 2 ? (val & 0xffff) : val);
			} else if (arg->type == ca_STRING) {
				command_arg_read_string(arg, &str);
				str = str ? escape(str, '\"') : 0L;
				cprintf("\"%s\" ", str ? str : "");
				xfree(str);
			} else if (arg->type == ca_SPEC || arg->type == ca_PATHSPEC 
					   || arg->type == ca_NAMESPEC) {
				command_arg_read_spec(arg, &str);
				str = str ? escape(str, '\"') : 0L;
				cprintf("\"%s\" ", str ? str : "");
				xfree(str);
			} else if (arg->type == ca_TOGGLE) {
				command_arg_read_toggle(arg, &str);
				cprintf("%s ", str);
				xfree(str);
			} else if (arg->type == ca_ENUM) {
				command_arg_read_enum(arg, &str);
				cprintf("%s ", str);
				xfree(str);
			} else {
				logger(_L | LOG_ABORT, _("Unhandled type in dump_command (%d)\n"), arg->type);
			}
			arg = arg->next;
		}	

		cprintf("\n");
		iter++;
	} while (ret);

}

static void dump_symbol_table(command_symbol_table *table, bool session)
{	
	while (table) {
		command_symbol *sym;
		cprintf("\n[%s]\n", table->name);
		sym = table->list;
		while (sym) {
			dump_command(sym, session);
			sym = sym->next;
		}
		if (table->sub)
			dump_symbol_table(table->sub, session);
		table = table->next;
	}
}

int 
config_load_spec(const OSSpec *spec, bool session)
{
	char path[OS_PATHSIZE];
	int ret;
	bool sessionflag;

	OS_SpecToString2(spec, path);
	sessionflag = command_get_session_filter();
	command_set_session_filter(session);
	ret = command_exec_file(path);
	command_set_session_filter(sessionflag);
	return ret;
}

int 
config_save_spec(const OSSpec *spec, bool session)
{
	FILE *file;
	char *str;
	char path[OS_PATHSIZE];
	OSSize sz;
	const char *filename = OS_NameSpecToString1(&spec->name);

	logger(_L|L_2, _("Saving %s file to %s\n"), 
		   session ? _("session") : _("config"),
		   OS_SpecToString1(spec));

	if (!filename || !*filename || strcmp(filename, "-") == 0) 
		file = stdout;
	else {
		OS_SpecToString2(spec, path);
		file = fopen(path, "wt");
	}

	if (!file) {
		command_logger(_L | LOG_ERROR | LOG_USER, _("Cannot write to %s\n"), path);
		return 0;
	}

	if (OS_NewHandle(0, &config) != OS_NOERR)
		command_logger(_L | LOG_FATAL, _("Out of memory for config file\n"));

	fprintf(file, "#!%s %s%s\n", 
			OS_SpecToString1(&v9t9_progspec), 
			session ? "" : "-c",
			path);
	fprintf(file, _("#\n#  v9t9 configuration file\n#\n"));

	dump_symbol_table(universe, session);

	str = OS_LockHandle(&config);
	if (OS_GetHandleSize(&config, &sz) != OS_NOERR)
		sz = strlen(str);
	fwrite(str, sz, 1, file);
	OS_UnlockHandle(&config);

	if (file != stdout)
		fclose(file);

#if UNDER_UNIX
	chmod(path, 0777);
#endif
	return 1;
}

int 
config_load_file(const char *path, const char *filename, bool session)
{
	OSSpec spec;
	if (!data_find_file(path, filename, &spec))
		return 0;
	return config_load_spec(&spec, session);
}

int 
config_save_file(const char *path, const char *filename, bool session)
{
	OSSpec spec;
	if (!data_create_file(path, filename, &spec, &OS_TEXTTYPE))
		return 0;
	return config_save_spec(&spec, session);
}

DECL_SYMBOL_ACTION(save_config)
{
	char *filename;
	int ret;
	command_arg_get_string(SYM_ARG_1st, &filename);
	// this is wrong, it changes v9t9_modules
//	v9t9_restop();
	ret = config_save_file(configspath, filename, false);
//	v9t9_restart();
	if (!ret) {
		command_logger(_L|LOG_USER, _("Could not save config file to '%s' in:\n%s\n"), 
			   filename, configspath);
	}
	return ret;
}

DECL_SYMBOL_ACTION(load_config)
{
	char *filename;
	int ret;
	command_arg_get_string(SYM_ARG_1st, &filename);
	// this is wrong, it changes v9t9_modules
//	v9t9_restop();
	ret = config_load_file(configspath, filename, false);
//	v9t9_restart();
	if (!ret) {
		command_logger(_L|LOG_USER, _("Could not find config file '%s' in:\n%s\n"), 
			   filename, configspath);
	}
	return ret;
}

DECL_SYMBOL_ACTION(save_session)
{
	char *filename;
	int ret;
	// this is wrong, it changes v9t9_modules
//	v9t9_restop();
	command_arg_get_string(SYM_ARG_1st, &filename);
	ret = config_save_file(sessionspath, filename, true);
//	v9t9_restart();
	if (!ret) {
		command_logger(_L|LOG_USER, _("Could not save session to '%s' in:\n%s\n"), 
			   filename, sessionspath);
	}
	return ret;
}

DECL_SYMBOL_ACTION(load_session)
{
	char *filename;
	int ret;
	// this is wrong, it changes v9t9_modules
//	v9t9_restop();
	command_arg_get_string(SYM_ARG_1st, &filename);
	ret = config_load_file(sessionspath, filename, true);
//	v9t9_restart();
	if (!ret) {
		command_logger(_L|LOG_USER, _("Could not find session file '%s' in:\n%s\n"), 
			   filename, sessionspath);
	}
	return ret;
}

