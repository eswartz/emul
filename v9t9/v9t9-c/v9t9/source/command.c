/*
  command.c						-- V9t9 command/configuration system

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

/*	This module handles dynamic changes to V9t9's state through the use
	of a generalized text parser.  The configuration file is the most
	obvious use of this parser.  */

#include <stdarg.h>
#include <stdlib.h>
#include <ctype.h>

#include "v9t9_common.h"
#include "log.h"
#define _L	 LOG_COMMANDS | LOG_INFO
#include "command.h"
#include "command_lexer.h"
#include "command_parser.h"

command_symbol_table *universe;
OSMutex command_mutex;

static bool session_only;

const char *
command_expr_atom_name(command_atom_type type)
{
	static char rtype_buf[16];

	switch (type)
	{
	case ca_VOID:		return _("void");
	case ca_SYM:		return _("symbol");
	case ca_NUM:		return _("number");
	case ca_STRING:		return _("string");
	case ca_ENUM:		return _("enumeration");

	case ca_SPEC:		return _("filepath");
	case ca_PATHSPEC:	return _("directory");
	case ca_NAMESPEC:	return _("filename");
	case ca_TOGGLE:		return _("boolean");
	}
	sprintf(rtype_buf, "<%d>", type); 
	return rtype_buf;
}

const char *
command_expr_type_name(command_expr_type type)
{
	static char type_buf[16];

	if (type < ca_LAST)
		return command_expr_atom_name(type);

	switch (type)
	{
	case ce_ASSIGN:	return "=";
	case ce_ADD:	return "+";
	case ce_SUB:	return "-";
	case ce_MUL:	return "*";
	case ce_DIV:	return "/";
	case ce_MOD:	return "%";
	case ce_OR:		return "|";
	case ce_AND:	return "&";
	case ce_XOR:	return "^";
	case ce_LSHIFT:	return "<<";
	case ce_RSHIFT:	return ">>";

	case ce_LOR:	return "||";
	case ce_LAND:	return "&&";
	case ce_LXOR:	return "^^";
	case ce_EQ:		return "==";
	case ce_NE:		return "!=";
	case ce_LE:		return "<=";
	case ce_GE:		return ">=";
	case ce_LT:		return "<";
	case ce_GT:		return ">";

	case ce_NEG:	return _("negate");
	case ce_NOT:	return "!";
	case ce_INV:	return "~";
	case ce_VALUE:	return _("()");

	case ce_COND:	return "?:";
	}

	sprintf(type_buf, "<%d>", type); 
	return type_buf;
}

command_expr_type
command_expr_type_equiv_class(command_expr_type type)
{
	return type == ca_ENUM || type == ca_TOGGLE ? ca_NUM 
		: type == ca_ISSPEC(type) ? ca_STRING 
		: type;
}

static command_expr_type
expr_type_unary_rtype(command_expr_type op, command_expr_type type)
{
	return command_expr_type_equiv_class(type);
}

static command_expr_type
expr_type_binary_rtype(command_expr_type op, command_expr_type a,
					   command_expr_type b)
{
	return op == ce_ASSIGN ? a : command_expr_type_equiv_class(b);
}


/***********************************/

/*	We consider a symbol to match if 'name' is a prefix
	of an item in 'list'.  'list' uses '|' to separate
	distinct spellings.  '||' separates standard commands
	from obsolete or compatibility commands; 'name' must 
	match exactly.

	'min_prefix_length' defines the shortest 'name' that can match
	an entry in 'list'.  If zero, any 'name' can match, else 'name'
	must match at least 'min_prefix_length' characters of a name in
	'list'.
	
	Returns -1 if no match, else the index of the 'name' in the 'list'.
*/
static const char *
get_next_string(const char *list, int *idx)
{
	while (*list && *list != '=' && *list != '|')
		list++;

	// get new value for string index
	if (*list == '=') {
		*idx = strtol(list+1, (char **)&list, 10);
		my_assert(!*list || *list == '|');

		if (*list == '|') list++;
	}
	// end of normal string
	else if (*list == '|') {
		list++;
	}

	return list;
}

//	Try to match 'name' in 'list' (choices separated by '|').  If
//	'exact' is not true, try to match a prefix of 'name' (of
//	min_prefix_length chars) in 'list', returning true if a match is
//	found, updating *prefix if 'name' was indeed a prefix.
static bool
match_string(const char *list, const char *name, 
			 int min_prefix_length, bool *prefix, bool exact)
{
	const char *nptr = name;

	if (prefix) *prefix = false;

	while (*list && *list != '|' && *list != '=') {		
		if (!exact && !*nptr) {	
			/* aaa|bbb , a; possible prefix match */
			if (!min_prefix_length 
				|| (nptr - name >= min_prefix_length))
			{
				if (prefix) *prefix = true;
				return true;
			}
			else
				return false;
		} else if (tolower(*list) != tolower(*nptr)) {
			return false;
		} else {
			/* matched a char */
			*list++;
			*nptr++;
		}
	}
	return !*nptr;
}

//	Get zero-based index of 'name' in 'list' (choices separated by '|')
//	of a given minimum prefix length, and update '*prefix' if the
//	name was indeed a prefix.
static int
get_string_index(const char *list, const char *name, 
				 int min_prefix_length, bool *prefix)
{
	const char	*nptr = name, *orig = list, *next;
	int         match = -1;
	int 		idx = 0;
	bool		exact = false;

	if (prefix) *prefix = false;

	while (*list) {
		next = get_next_string(list, &idx);
		if (match_string(list, name, min_prefix_length, prefix, exact)) {
			match = idx;
			break;
		}
		list = next;
		if (*next == '|') exact = true;
		idx++;
	}
	return match;
}

//	get the string value of the indexed value in the list.
// 	user must xfree() the result
static char *
get_indexed_string(const char *list, int idx)
{
	const char *str, *end, *next;
	char *val;
	int cur;

	cur = 0;
	str = list;
	while (*str)
	{
		next = get_next_string(str, &cur);
		if (cur == idx)
			break;
		str = next;
		cur++;
	}

	if (idx != cur) {
		char prn[16];
		sprintf(prn, "%d", idx);
		return xstrdup(prn);
	}

	end = str;
	while (*end && *end != '=' && *end != '|')
		end++;

	val = (char *)xmalloc(end - str + 1);
	memcpy(val, str, end - str);
	val[end - str] = 0;

	logger(_L|L_4, _("get_indexed_string: '%s' #%d --> '%s'\n"),
		   list, idx, val);

	return val;
}

//	string match 'name' in 'list' (entries separated by '|');
//	a prefix is allowed if min_prefix_length>0.
//	The index of the string (1...n) is returned; and if a prefix 
//	was matched, the negative index (-1..-n) is returned.
int  
symbol_match(const char *list, const char *name, int min_prefix_length)
{
	bool prefix;
	int match;
	match = get_string_index(list, name, min_prefix_length, &prefix);
	if (match < 0)
		return 0;
	else if (prefix)
		return -(match+1);
	else
		return match+1;
}

/*	Returns 0 for no match, 
	>=1 for a definite match, 
	and <=-1 for a prefix match */
int
command_match_symbol(const command_symbol_table * table,
					 const char *name, command_symbol ** sym)
{
	command_symbol *match = NULL;
	int         ret = 0;

	while (table != NULL) {
		command_symbol *lst = table->list;
		int         subret;

		// note:  comment out "ret != 1" to debug options
		while (lst != NULL && ret != 1) {
			subret = symbol_match(lst->name, name, 5);
			if (subret) {
				if (match) {
					// only report this if we don't already have a good idea
					if (subret < 0 && ret <= 0)
						command_logger(_L|LOG_ERROR|L_1,
							_("%s:  ambiguous identifier (collides with '%s')\n"),
							 name, lst->name);
				} else {
					match = lst;
					ret = subret;
				}
			}
			lst = lst->next;
		}

		// note:  comment out "ret != 1" to debug options
		if (table->sub && ret != 1) {
			subret = command_match_symbol(table->sub, name, &lst);
			if (subret) {
				if (!match) {
					match = lst;
					ret = subret;
				}
			}
		}

		table = table->next;
	}

	if (match)
		logger(LOG_COMMANDS | L_3, _("for '%s', matched '%s' (%d)\n"), name,
			   match->name, ret);

	*sym = match;

	return ret;
}

/*	Return list of matching symbols. */

#define ADD_DELTA 16
static void
add_match(command_symbol *** matches, int *nmatches, command_symbol * match)
{
	if (*nmatches % ADD_DELTA == 0) {
		*matches = (command_symbol **) xrealloc(*matches,
												sizeof(command_symbol *) *
												(*nmatches + ADD_DELTA));
	}
	(*matches)[(*nmatches)++] = match;
}

/*
 *	return a list of all the symbols for which 'name'
 *	is a prefix.
 */
void
command_match_symbols(const command_symbol_table * table,
					  const char *name,
					  command_symbol *** matches, int *nmatches)
{
	while (table != NULL) {
		command_symbol *lst = table->list;
		int         subret;

		while (lst != NULL) {
			subret = symbol_match(lst->name, name, 0);
			if (subret)
				add_match(matches, nmatches, lst);
			lst = lst->next;
		}

		if (table->sub)
			command_match_symbols(table->sub, name, matches, nmatches);

		table = table->next;
	}
}

command_symbol_table *
command_symbol_table_new(char *name, char *help,
						 command_symbol * list, command_symbol_table * sub,
						 command_symbol_table * next)
{
	command_symbol_table *tbl =
		(command_symbol_table *) xmalloc(sizeof(command_symbol_table));
	tbl->name = name;
	tbl->help = help;
	tbl->list = list;
	tbl->sub = sub;
	tbl->next = next;
	return tbl;
}

command_symbol_table *
command_symbol_table_add_subtable(command_symbol_table * parent,
								  command_symbol_table * table)
{
	command_symbol_table **ptr = &parent->sub;

	while (*ptr)
		ptr = &(*ptr)->next;
	*ptr = table;
	return parent;
}

command_symbol_table *
command_symbol_table_add(command_symbol_table * parent, command_symbol * list)
{
	command_symbol **ptr = &parent->list;

	while (*ptr)
		ptr = &(*ptr)->next;
	*ptr = list;
	return parent;
}

command_symbol *
command_symbol_new(char *name, char *help, command_symbol_flags flags,
				   command_symbol_action action, command_arg * ret, 
				   command_arg * args, command_symbol * next)
{
	command_symbol *sym = (command_symbol *) xmalloc(sizeof(command_symbol));

	sym->name = name ? name : _("<unnamed>");
	sym->help = help;			// ? help : "<no help>";
	sym->flags = flags;
	sym->action = action;
	sym->ret = ret == RET_FIRST_ARG ? args : ret;
	sym->args = args;
	sym->next = next;
	return sym;
}

command_arg *
command_arg_new_num(char *name, char *help, command_arg_action action,
					int sz, void *mem, command_arg * next)
{
	command_arg *sym = (command_arg *) xmalloc(sizeof(command_arg));

	sym->name = name ? name : _("<unnamed>");
	sym->help = help ? help : _("a number");
	sym->action = action;
	sym->type = ca_NUM;
	sym->u.num.sz = sz;
	sym->u.num.mem = mem;
	sym->next = next;
	return sym;
}

command_arg *
command_arg_new_string(char *name, char *help, command_arg_action action,
					   int maxlen, void *mem, command_arg * next)
{
	command_arg *sym = (command_arg *) xmalloc(sizeof(command_arg));

	sym->name = name ? name : _("<unnamed>");
	sym->help = help ? help : _("a string");
	sym->action = action;
	sym->type = ca_STRING;
	sym->u.string.maxlen = maxlen;
	if (maxlen >= 0)
		sym->u.string.m.mem = (char *) mem;
	else
		sym->u.string.m.ptr = (char **) mem;

	sym->next = next;
	return sym;
}

command_arg *
command_arg_new_spec(char *name, char *help, command_arg_action action,
					 OSSpec * spec, command_arg * next)
{
	command_arg *sym = (command_arg *) xmalloc(sizeof(command_arg));

	sym->name = name ? name : _("<unnamed>");
	sym->help = help ? help : _("a filespec");
	sym->action = action;
	sym->type = ca_SPEC;
	sym->u.spec.mem = spec;
	sym->next = next;
	return sym;
}

command_arg *
command_arg_new_pathspec(char *name, char *help, command_arg_action action,
						 OSPathSpec * pathspec, command_arg * next)
{
	command_arg *sym = (command_arg *) xmalloc(sizeof(command_arg));

	sym->name = name ? name : _("<unnamed>");
	sym->help = help ? help : _("a pathspec");
	sym->action = action;
	sym->type = ca_PATHSPEC;
	sym->u.pathspec.mem = pathspec;
	sym->next = next;
	return sym;
}

command_arg *
command_arg_new_namespec(char *name, char *help, command_arg_action action,
						 OSNameSpec * namespec, command_arg * next)
{
	command_arg *sym = (command_arg *) xmalloc(sizeof(command_arg));

	sym->name = name ? name : _("<unnamed>");
	sym->help = help ? help : _("a filename");
	sym->action = action;
	sym->type = ca_NAMESPEC;
	sym->u.namespec.mem = namespec;
	sym->next = next;
	return sym;
}

command_arg *
command_arg_new_toggle(char *name, char *help, command_arg_action action,
					   int sz, void *mem, int val, command_arg * next)
{
	command_arg *sym = (command_arg *) xmalloc(sizeof(command_arg));

	my_assert(name);
	sym->name = name;
	sym->help = help ? help : _("a number");
	sym->action = action;
	sym->type = ca_TOGGLE;
	sym->u.toggle.sz = sz;
	sym->u.toggle.mem = mem;
	sym->u.toggle.flag = val;
	sym->next = next;
	return sym;
}

command_arg *
command_arg_new_enum(char *name, char *help, command_arg_action action,
		int sz, void *mem, command_arg *next)
{
	command_arg *sym = (command_arg *) xmalloc(sizeof(command_arg));

	my_assert(name);
	sym->name = name;
	sym->help = help ? help : _("one of the items in the list");
	sym->action = action;
	sym->type = ca_ENUM;
	sym->u.num.sz = sz;
	sym->u.num.mem = mem;
	sym->next = next;
	return sym;
}

/*************************************************/

void
command_arg_read_num(command_arg *arg, int *val)
{
	switch (arg->u.num.sz) {
	case 1:
	{
		u8          u8val;

		u8val = *(u8 *) arg->u.num.mem;
		*val = u8val;
		break;
	}
	case 2:
	{
		u16         u16val;

		u16val = *(u16 *) arg->u.num.mem;
		*val = u16val;
		break;
	}
	case 4:
	{
		u32         u32val;

		u32val = *(u32 *) arg->u.num.mem;
		*val = u32val;
		break;
	}
	case 8:
	{
		u64         u64val;

		u64val = *(u64 *) arg->u.num.mem;
		*val = u64val;
		break;
	}
	default:
		command_logger(_L | LOG_ABORT, _("Unhandled size in command_arg_read_num (%d)\n"),
			 arg->u.num.sz);
		break;
	}
}

int
command_arg_get_num(command_arg * arg, int *val)
{
	if (!arg) {
		*val = 0;
		return 0;
	}
	my_assert(arg->type == ca_NUM 
			  || arg->type == ca_TOGGLE
				|| arg->type == ca_ENUM);

	if (arg->action)
		if (!arg->action(arg, caa_READ))
			return 0;

	command_arg_read_num(arg, val);
	return 1;
}

int
command_arg_set_num(command_arg * arg, int val)
{
	if (!arg) {
		return 0;
	}

	my_assert(arg->type == ca_NUM 
			  || arg->type == ca_TOGGLE 
			  || arg->type == ca_ENUM);

	switch (arg->u.num.sz) {
	case 1:
	{
		u8          u8val = val;

		*(u8 *) arg->u.num.mem = u8val;
		break;
	}
	case 2:
	{
		u16         u16val = val;

		*(u16 *) arg->u.num.mem = u16val;
		break;
	}
	case 4:
	{
		u32         u32val = val;

		*(u32 *) arg->u.num.mem = u32val;
		break;
	}
	case 8:
	{
		u64         u64val = val;

		*(u64 *) arg->u.num.mem = u64val;
		break;
	}
	default:
		command_logger(_L | LOG_ABORT, _("Unhandled size in command_arg_set_num (%d)\n"),
			 arg->u.num.sz);
		break;
	}

	if (arg->action)
		if (!arg->action(arg, caa_WRITE))
			return 0;

	return 1;
}

void
command_arg_read_string(command_arg * arg, char **str)
{
	if (arg->u.string.maxlen >= 0)
		*str = arg->u.string.m.mem;
	else
		*str = *arg->u.string.m.ptr;
}

int
command_arg_get_string(command_arg * arg, char **str)
{
	if (!arg) {
		*str = 0L;
		return 0;
	}
	my_assert(arg->type == ca_STRING);
	if (arg->action)
		if (!arg->action(arg, caa_READ))
			return 0;
	command_arg_read_string(arg, str);
	return 1;
}

int
command_arg_set_string(command_arg * arg, const char *str)
{
	if (!arg) {
		return 0;
	}
	my_assert(arg->type == ca_STRING);

	if (arg->u.string.maxlen >= 0) {
		if (str) {
			strncpy(arg->u.string.m.mem, str, arg->u.string.maxlen);
			arg->u.string.m.mem[arg->u.string.maxlen - 1] = 0;
		} else {
			*arg->u.string.m.mem = 0;
		}
	} else {
		if (str) {
			*arg->u.string.m.ptr = (char *) xrealloc(*arg->u.string.m.ptr,
													 strlen(str) + 1);
			strcpy(*arg->u.string.m.ptr, str);
		} else {
			if (*arg->u.string.m.ptr) {
				xfree(arg->u.string.m.ptr);
			}
			*arg->u.string.m.ptr = 0L;
		}
	}

	if (arg->action)
		if (!arg->action(arg, caa_WRITE))
			return 0;

	return 1;
}

void
command_arg_read_spec(command_arg * arg, char **str)
{
	if (arg->type == ca_SPEC)
		*str = OS_SpecToString1(arg->u.spec.mem);
	else if (arg->type == ca_PATHSPEC)
		*str = OS_PathSpecToString1(arg->u.pathspec.mem);
	else						/* if (arg->type == ca_NAMESPEC) */
		*str = OS_NameSpecToString1(arg->u.namespec.mem);
}

int
command_arg_get_spec(command_arg * arg, char **str)
{
	if (!arg) {
		*str = 0L;
		return 0;
	}
	my_assert(ca_ISSPEC(arg->type));
	if (arg->action)
		if (!arg->action(arg, caa_READ))
			return 0;
	command_arg_read_spec(arg, str);
	return 1;
}

int
command_arg_set_spec(command_arg * arg, const char *str)
{
	OSError     err = OS_FNFERR;
	char		*path;

	if (!arg) {
		return 0;
	}

	my_assert(ca_ISSPEC(arg->type));

	if (str) {
		path = OS_PathExpand(str);
		if (arg->type == ca_SPEC)
			err = OS_MakeSpec(path, arg->u.spec.mem, NULL);
		else if (arg->type == ca_PATHSPEC)
			err = OS_MakePathSpec(NULL, path, arg->u.pathspec.mem);
		else						/* if (arg->type == ca_NAMESPEC) */
			err = OS_MakeNameSpec(path, arg->u.namespec.mem);
	}

	if (err != OS_NOERR) {
		command_logger(_L|LOG_ERROR,_("argument '%s': cannot set to '%s' (%s)\n"), 
					   arg->name, path, OS_GetErrText(err));
		return 0;
	}

	if (arg->action)
		if (!arg->action(arg, caa_WRITE))
			return 0;

	return 1;
}

void
command_arg_read_toggle(command_arg * arg, char **val)
{
	int idx;

	command_arg_read_num(arg, &idx);
	idx = (arg->u.toggle.flag & idx) != 0;
	*val = get_indexed_string(arg->name, idx);
}

int
command_arg_get_toggle(command_arg * arg, int *val)
{
	if (!arg) {
		*val = 0L;
		return 0;
	}
	if (!command_arg_get_num(arg, val))
		return 0;

	*val = (arg->u.toggle.flag & *val) != 0;
	return 1;
}

int
command_arg_set_toggle(command_arg * arg, int val)
{
	int         curval;

	if (!arg) {
		return 0;
	}

	if (!command_arg_get_num(arg, &curval))
		return 0;

	val = val ? (curval | arg->u.toggle.flag) :
		(curval & ~arg->u.toggle.flag);

	if (!command_arg_set_num(arg, val))
		return 0;

	return 1;
}

int
command_arg_set_toggle_str(command_arg * arg, const char *str)
{
	int val;

	val = get_string_index(arg->name, str, 5, 0L);
	if (val < 0) {
		command_logger(_L|LOG_ERROR,_("'%s' does not match an enumeration in '%s'\n"),
					str, arg->name);	
		return 0;
	}
	return command_arg_set_toggle(arg, val);
}

void
command_arg_read_enum(command_arg * arg, char **val)
{
	int idx;

	command_arg_read_num(arg, &idx);
	*val = get_indexed_string(arg->name, idx);
}

int
command_arg_get_enum(command_arg * arg, int *val)
{
	return command_arg_get_num(arg, val);
}

int
command_arg_set_enum(command_arg * arg, int val)
{
	return command_arg_set_num(arg, val);
}

int
command_arg_set_enum_str(command_arg * arg, const char *str)
{
	int val;

	val = get_string_index(arg->name, str, 5, 0L);
	if (val < 0) {
		command_logger(_L|LOG_ERROR,_("'%s' does not match an enumeration in '%s'\n"),
					str, arg->name);	
		return 0;
	}

	return command_arg_set_num(arg, val);
}

/***************************************/

/*
 *	return the basic type of a symbol
 */
static int
command_get_rtype(command_symbol *sym)
{
	command_arg *ret;

	ret = sym->ret;
	if (!ret) {
		return ca_VOID;
	} else if (ret->type == ca_NUM) {
		return ca_NUM;
	} else if (ret->type == ca_STRING) {
		return ca_STRING;
	} else if (ca_ISSPEC(ret->type)) {
		return ca_STRING;
	} else if (ret->type == ca_TOGGLE) {
		return ca_ENUM;
	} else if (ret->type == ca_ENUM) {
		return ca_ENUM;
	} else {
		command_logger(_L | LOG_ABORT, _("Unhandled ca_XXXX (%s)\n"), 
			   command_expr_atom_name(ret->type));
	}

	return 1;
}

/*
 *	return the value of a command.  
 *
 *	For c_STATIC commands, the value is dictated by sym->ret,
 *	otherwise we call the sym->action routine.
 */
int
command_get_val(command_symbol * sym, command_expr * val)
{
	int rtype;
	command_arg *ret;

	rtype = command_get_rtype(sym);

	if ((sym->flags & c_DYNAMIC) && sym->action)
		if (!sym->action(sym, csa_READ, 0))
			return 0;

	ret = sym->ret;
	if (!ret) {
		val->type = val->rtype = ca_VOID;
		return 1;
	} else if (ret->type == ca_NUM) {
		val->type = val->rtype = ca_NUM;
		if (!command_arg_get_num(ret, &val->u.num))
			return 0;
	} else if (ret->type == ca_STRING) {
		char       *str;

		val->type = val->rtype = ca_STRING;
		if (!command_arg_get_string(ret, &str))
			return 0;
		val->u.str = xstrdup(str);
	} else if (ca_ISSPEC(ret->type)) {
		char       *str;

		val->type = val->rtype = ca_STRING;
		if (!command_arg_get_spec(ret, &str))
			return 0;
		val->u.str = xstrdup(str);
	} else if (ret->type == ca_TOGGLE) {
		val->type = ca_ENUM;
		val->rtype = ca_NUM;
		val->u.enum_.list = ret->name;
		if (!command_arg_get_toggle(ret, &val->u.enum_.val))
			return 0;
	} else if (ret->type == ca_ENUM) {
		val->type = ca_ENUM;
		val->rtype = ca_NUM;
		val->u.enum_.list = ret->name;
		if (!command_arg_get_enum(ret, &val->u.enum_.val))
			return 0;
	} else {
		command_logger(_L | LOG_ABORT, _("Unhandled ca_XXXX (%s)\n"), 
			   command_expr_atom_name(ret->type));
	}


	return 1;
}


int
command_set_args(command_expr * ret, command_symbol * sym, command_expr *val)
{
	command_arg *arg;
	int         argnum = 0;

	command_logger(_L | L_2, "sym->name='%s'\n", sym->name);

	arg = sym->args;
	while (val != 0L) {
		argnum++;

		if (arg == NULL) {
			command_logger(_L|LOG_ERROR,_("%s:  unexpected additional %s argument #%d\n"),
						sym->name, command_expr_atom_name(val->type), argnum);
			return 0;
		}

		/*  Look for implicit conversions */
		if (val->type == ca_STRING && ca_ISSPEC(arg->type)) {
			if (!command_arg_set_spec(arg, val->u.str))
				return 0;
			//	xfree(val->u.str);

		} else if (val->type == ca_STRING && arg->type == ca_ENUM) {
			if (!command_arg_set_enum_str(arg, val->u.str))
				return 0;

		} else if (val->type == ca_NUM && arg->type == ca_ENUM) {
			if (!command_arg_set_enum(arg, val->u.num))
				return 0;

		} else if (val->type == ca_STRING && arg->type == ca_TOGGLE) {
			if (!command_arg_set_toggle_str(arg, val->u.str))
				return 0;

		} else if (val->type == ca_NUM && arg->type == ca_TOGGLE) {
			if (!command_arg_set_toggle(arg, val->u.num))
				return 0;

		} else if (val->type == ca_NUM && arg->type == ca_NUM) {
			if (!command_arg_set_num(arg, val->u.num))
				return 0;

		} else if (val->type == ca_STRING && arg->type == ca_STRING) {
			if (!command_arg_set_string(arg, val->u.str))
				return 0;
			//	xfree(val->u.str);

		} else if (val->type == ca_ENUM && arg->type == ca_ENUM) {
			if (!command_arg_set_enum(arg, val->u.enum_.val))
				return 0;

		} else if (val->type != arg->type) {
			command_logger(_L|LOG_ERROR,_("%s:  type mismatch in argument '%s' (expected %s)\n"),
						sym->name, arg->name, command_expr_atom_name(arg->type));
			return 0;
		} else
			command_logger(_L | LOG_ABORT, _("command_set_args:  cannot handle %s\n"),
				 command_expr_atom_name(val->type));

		arg = arg->next;
		val = val->next;
	}

	if (arg != NULL && !(sym->flags & c_OPTIONAL_ARGS)) {
		command_logger(_L|LOG_ERROR,_("%s:  expected additional parameters\n"), sym->name);
		return 0;
	}

	// clear out the remaining arguments
	while (arg != NULL) {
		if (ca_ISSPEC(arg->type)) {
			if (!command_arg_set_spec(arg, ""))
				return 0;
		} else if (arg->type == ca_TOGGLE) {
			if (!command_arg_set_toggle(arg, 0))
				return 0;
		} else if (arg->type == ca_NUM) {
			if (!command_arg_set_num(arg, 0))
				return 0;
		} else if (arg->type == ca_STRING) {
			if (!command_arg_set_string(arg, 0L))
				return 0;
		} else
			command_logger(_L | LOG_ABORT, _("command_set_args:  cannot handle %s\n"),
				 command_expr_atom_name(val->type));

		arg = arg->next;
	}

	if (sym->action)
		if (!sym->action(sym, csa_WRITE, 0))
			return 0;

	if (ret) {
		ret->type = ca_VOID;
		if (sym->ret && !command_get_val(sym, ret))
			return 0;
	}

	return 1;
}

/*************************/

static bool
validate_expr(command_expr *expr)
{
	if (!expr) return false;

	if (expr->type < ca_VOID || expr->type >= ce_LAST) {
		command_logger(_L|LOG_ABORT, _("invalid expr type %d\n"), expr->type);
		return false;
	}

	// all exprs should return simple types
	if (!ca_ISSIMPLE(expr->rtype)) {
		command_logger(_L|LOG_ABORT, _("invalid expr rtype %d\n"), expr->rtype);
		return false;
	}

	return true;
}

command_expr *
command_expr_copy_expr(command_expr *atom)
{
	command_expr *expr;

	if (!atom) return 0L;

	if (!validate_expr(atom)) return 0L;

//	if (atom->next)
//		command_logger(_L|LOG_ABORT, _("command_expr_copy_expr: did not expect a list\n"));

	expr = (command_expr *) xmalloc(sizeof(command_expr));

	*expr = *atom;
	if (expr->type == ca_STRING)
		expr->u.str = xstrdup(atom->u.str);
	expr->next = 0L;
	return expr;
}

static void
command_expr_set_num(command_expr *expr, int num)
{
	expr->type = ca_NUM;
	expr->rtype = ca_NUM;
	expr->u.num = num;
}

static void
command_expr_set_string(command_expr *expr,const char *str)
{
	expr->type = ca_STRING;
	expr->rtype = ca_STRING;
	expr->u.str = xstrdup(str);
}

static void
command_expr_set_symbol(command_expr *expr, command_symbol *sym)
{
	expr->type = ca_SYM;
	expr->rtype = command_get_rtype(sym);
	expr->u.sym = sym;
}

static void
command_expr_set_enum(command_expr *expr, const char *list, int val)
{
	expr->type = ca_ENUM;
	expr->rtype = ca_ENUM;
	expr->u.enum_.list = list;
	expr->u.enum_.val = val;
}

static command_expr *
command_expr_new(void)
{
	command_expr *expr = (command_expr *) xmalloc(sizeof(command_expr));
	memset(expr, 0, sizeof(command_expr));
	return expr;
}

command_expr *
command_expr_new_num(int num)
{
	command_expr *expr = command_expr_new();

	command_expr_set_num(expr, num);

	return expr;
}

command_expr *
command_expr_new_string(const char *str)
{
	command_expr *expr = command_expr_new();

	command_expr_set_string(expr, str);

	return expr;
}

command_expr *
command_expr_new_symbol(command_symbol *sym)
{
	command_expr *expr = command_expr_new();

	if (!sym)
		return 0L;

	command_expr_set_symbol(expr, sym);

	return expr;
}

command_expr *
command_expr_new_enum(const char *list, int val)
{
	command_expr *expr = command_expr_new();

	command_expr_set_enum(expr, list, val);

	return expr;
}

/*
 *	create expression node combining 'left' and 'right' with operation 'type'
 *
 *	'left' or 'right' may be null to indicate an error condition,
 *	in which case null is returned.
 */
command_expr *
command_expr_new_binary(command_expr_type type, 
						command_expr *left, 
						command_expr *right)
{
	command_expr *expr;
	int rtype;

	if (!left || (type != ce_ASSIGN && !right))
		return 0L;

	if (type < ca_VOID || type >= ce_LAST) {
		logger(_L|LOG_ABORT, _("invalid expr type %d\n"), type);
		return 0L;
	}

	if (!validate_expr(left)) return 0L;
	if (right && !validate_expr(right)) return 0L;

	if (left->next || (type != ce_ASSIGN && right->next))
	{
		command_logger(_L|LOG_ERROR,_("cannot combine lists with '%s'\n"),
					command_expr_type_name(type));
		return 0L;
	}

	switch (type) 
	{
	default:
		logger(_L|LOG_ABORT, _("invalid binary expr type '%s'\n"), 
			   command_expr_type_name(type));
		break;

	CASE_EXPR_BINARY:
		// currently nothing complex happens here...

		my_assert(!(type == ce_ASSIGN && left->type != ca_SYM));

		if (left->type != ce_SYM)
		{
			if (left->rtype == ca_VOID || (right && right->rtype == ca_VOID)) {
				command_logger(_L|LOG_ERROR,_("invalid use of 'void' expression\n"));
				return 0L;
			}

			if (right && command_expr_type_equiv_class(left->rtype) 
					!= command_expr_type_equiv_class(right->rtype)) {
				command_logger(_L|LOG_ERROR,_("type mismatch in '%s' %s '%s'\n"), 
							command_expr_atom_name(left->rtype),
							command_expr_type_name(type),
							command_expr_atom_name(right->rtype));
				return 0L;
			}
		}
		else
		{
			// we could check argument types here, but
			// we actually do that later...
		}

		rtype = expr_type_binary_rtype(type, left->rtype, 
									   right ? right->rtype : ca_VOID);
		break;
	}

	expr = (command_expr *) xmalloc(sizeof(command_expr));
	
	expr->type = type;
	expr->rtype = rtype;
	expr->u.binary.left = left;
	expr->u.binary.right = right;
	expr->next = 0L;

	return expr;
}

command_expr *
command_expr_new_unary(command_expr_type type, 
					   command_expr *node)
{
	command_expr *expr;
	int rtype;

	if (!node)
		return 0L;

	if (type < ca_VOID || type >= ce_LAST) {
		logger(_L|LOG_ABORT, _("invalid expr type %d\n"), type);
		return 0L;
	}

	if (!validate_expr(node)) return 0L;

	my_assert(!(type == ce_VALUE && node->type != ca_SYM));

	if (node->next)
	{
		command_logger(_L|LOG_ERROR,_("cannot modify list with '%s'\n"),
					command_expr_type_name(type));
		return 0L;
	}
	
	switch (type) 
	{
	default:
		logger(_L|LOG_ABORT, _("invalid unary expr type %s\n"), 
			   command_expr_type_name(type));
		break;

	CASE_EXPR_UNARY:
		if (node->rtype == ca_VOID) {
			command_logger(_L|LOG_ERROR,_("invalid use of 'void' expression\n"));
			return 0L;
		}

		rtype = expr_type_unary_rtype(type, node->rtype);
		break;
	}

	expr = (command_expr *) xmalloc(sizeof(command_expr));
	
	expr->type = type;
	expr->rtype = rtype;
	expr->u.unary = node;
	expr->next = 0L;

	return expr;
}

command_expr *
command_expr_new_cond(command_expr *cond, 
					  command_expr *left, 
					  command_expr *right)
{
	command_expr *expr;
	int rtype;
	int type = ce_COND;

	if (!left || !right || !cond)
		return 0L;

	if (!validate_expr(cond)) return 0L;
	if (!validate_expr(left)) return 0L;
	if (!validate_expr(right)) return 0L;

	my_assert(!(cond->next || left->next || right->next));

	my_assert(!(cond->rtype == ca_VOID));

	rtype = left->rtype;

	expr = (command_expr *) xmalloc(sizeof(command_expr));
	
	expr->type = type;
	expr->rtype = rtype;
	expr->u.cond.cond = cond;
	expr->u.cond.left = left;
	expr->u.cond.right = right;
	expr->next = 0L;

	return expr;
}

command_expr *
command_exprlist_append_expr(command_expr *list,
							 command_expr *node)
{
	command_expr **step = &list;

	while (*step != 0L)
		step = &(*step)->next;

	*step = node;
	return list;
}

void
command_expr_free(command_expr *expr)
{
	if (!expr) return;
	switch (expr->type)
	{
	case ca_VOID:
	case ca_SYM:
	case ca_NUM:
	case ca_ENUM:
		break;
	case ca_STRING:
		xfree(expr->u.str);
		expr->u.str = 0;
		break;
	case ca_SPEC:
	case ca_PATHSPEC:
	case ca_NAMESPEC:
	case ca_TOGGLE:
		break;
	CASE_EXPR_BINARY:
		command_expr_free(expr->u.binary.left);
		command_expr_free(expr->u.binary.right);
		expr->u.binary.left = expr->u.binary.right = 0L;
		break;
	CASE_EXPR_UNARY:
		command_expr_free(expr->u.unary);
		expr->u.unary = 0L;
		break;
	case ce_COND:
		command_expr_free(expr->u.cond.cond);
		command_expr_free(expr->u.cond.left);
		command_expr_free(expr->u.cond.right);
		expr->u.cond.cond = expr->u.cond.left = 
			expr->u.cond.right = 0L;
		break;
	default:
		logger(_L|LOG_ABORT, _("unhandled ca_XXX (%s)"),
			   command_expr_type_name(expr->type));
		break;
	}
	xfree(expr);
}

void
command_exprlist_free(command_expr *list)
{
	command_expr *step = list;
	while (step)
	{
		command_expr *next = step->next;
		command_expr_free(step);
		step = next;
	}
}

/********************************/

static char *
command_expr_type_stringify(command_expr *expr, int type)
{
	char *ret, *str;
	static char strbuf[256];
	
	switch (type)
	{
	case ca_VOID:	
		ret = mprintf(strbuf, sizeof(strbuf), "%s", _("<void>"));
		break;
	case ca_SYM:
		ret = mprintf(strbuf, sizeof(strbuf), "'%s'", expr->u.sym->name);
		break;
	case ca_NUM:	
		ret = mprintf(strbuf, sizeof(strbuf), "%d", expr->u.num);
		break;
	case ca_STRING:	
		ret = mprintf(strbuf, sizeof(strbuf), "%s", expr->u.str);
		break;
	case ca_ENUM:
		ret = mprintf(strbuf, sizeof(strbuf), "%s", 
					  (str = get_indexed_string(expr->u.enum_.list, expr->u.enum_.val)));
		xfree(str);
		break;
	default:
		logger(_L|LOG_ABORT, _("command_expr_type_stringify: unhandled type '%s'"),
			   command_expr_type_name(type));
		ret = mprintf(strbuf, sizeof(strbuf), "%s", command_expr_type_name(type));
		break;
	}
	if (ret == strbuf)
		return xstrdup(ret);
	else
		return ret;
}

char *
command_expr_atom_stringify(command_expr *expr)
{
	return command_expr_type_stringify(expr, expr->type);
}

static void
command_expr_print(int flags, command_expr *expr)
{
	char *str = command_expr_atom_stringify(expr);
	logger(flags, "%s ", str);
	xfree(str);
}

/********************************/

static command_expr *
command_exprlist_evaluate(command_expr **expr, const command_arg *args);

static command_expr *
command_expr_force_to_int(command_expr *atom)
{
	if (atom->type == ca_ENUM)
	{
		command_expr_set_num(atom, atom->u.enum_.val);
	}
	return atom;
}

static int 
expr_type_binary_func(command_expr_type type, int a, int b)
{
	switch (type)
	{
	case ce_ADD:	return a+b;
	case ce_SUB:	return a-b;
	case ce_MUL:	return a*b;
	case ce_DIV:	if (b) return a/b; else {
		command_logger(_L|LOG_ERROR,_("Division by zero\n"));
		return 0;
	}
	case ce_MOD:	if (b) return a%b; else {
		command_logger(_L|LOG_ERROR,_("Division by zero\n"));
		return 0;
	}
	case ce_OR:		return a|b;
	case ce_AND:	return a&b;
	case ce_XOR:	return a^b;
	case ce_LSHIFT:	return a<<b;
	case ce_RSHIFT:	return a>>b;

	case ce_LOR:	return a||b;
	case ce_LAND:	return a&&b;
	case ce_LXOR:	return a!=b;

	case ce_EQ:		return a==b;
	case ce_NE:		return a!=b;
	case ce_GE:		return a>=b;
	case ce_LE:		return a<=b;
	case ce_LT:		return a<b;
	case ce_GT:		return a>b;

	default:
		command_logger(_L|LOG_ABORT, _("unhandled ce_XXX: '%s'\n"), 
			   command_expr_type_name(type));
		return 0;
	}
}

static int 
expr_type_unary_func(command_expr_type type, int a)
{
	switch (type)
	{
	case ce_NEG:	return -a;
	case ce_NOT:	return !a;
	case ce_INV:	return ~a;

	default:
		command_logger(_L|LOG_ABORT, _("unhandled ce_XXX: '%s'\n"), 
			   command_expr_type_name(type));
		return 0;
	}
}

/*
 *	evaluate an expression and return the result
 */
static command_expr *
command_expr_evaluate(command_expr *expr)
{
	command_expr *atom = 0L;
	command_expr *e1 = 0L, *e2 = 0L, *e3 = 0L;

	if (!expr)
		return 0;

	logger(_L|L_4, "command_expr_evaluate: %s\n", 
		   command_expr_type_name(expr->type));

	switch (expr->type)
	{
	case ca_VOID:
	case ca_NUM:
	case ca_STRING:
	case ca_SYM:
	case ca_ENUM:
		atom = command_expr_copy_expr(expr);
		break;

	CASE_EXPR_BINARY:
		if (expr->type == ce_ASSIGN)
		{
			// handle invocation of a command, return value in 'atom'
			// if it is static
			e1 = command_expr_evaluate(expr->u.binary.left);
			my_assert(e1->type == ca_SYM);
			e2 = command_exprlist_evaluate(&expr->u.binary.right, 
										   e1->u.sym->args);

			atom = command_expr_new();
			if (!command_set_args(atom, e1->u.sym, e2)) {
				command_expr_free(atom);
				atom = 0;
			}
			break;
		}

		e1 = command_expr_evaluate(expr->u.binary.left);
		e2 = command_expr_evaluate(expr->u.binary.right);

		// adding strings?
		if (expr->type == ce_ADD 
		&&	e1->type == ca_STRING && e2->type == ca_STRING)
		{
			if (e1->u.str && e2->u.str) {
				e1->u.str = (char *)xrealloc(e1->u.str,
										 strlen(e1->u.str) + 
										 strlen(e2->u.str) + 1);
				strcat(e1->u.str, e2->u.str);
				atom = e1; e1 = 0L;
			}
			else if (e1->u.str) {
				atom = e1; e1 = 0L;
			}
			else if (e2->u.str) {
				atom = e2; e2 = 0L;
			}
			else {
				atom = 0L;
			}
		}
		else if (command_expr_type_equiv_class(e1->type) == ca_NUM 
				 && command_expr_type_equiv_class(e2->type) == ca_NUM)
		{
			// normal binary op
			e1 = command_expr_force_to_int(e1);
			e2 = command_expr_force_to_int(e2);

			e1->u.num = expr_type_binary_func(
							expr->type, e1->u.num, e2->u.num);
			atom = e1;	e1 = 0L;
		}
		else
		{
			command_logger(_L|LOG_ERROR,_("unexpected arguments to '%s' (%s, %s)\n"),
				   command_expr_type_name(expr->type),
				   command_expr_type_name(e1->type),
				   command_expr_type_name(e2->type));
			atom = 0L;
		}
		break;

	CASE_EXPR_UNARY:
		e1 = command_expr_evaluate(expr->u.unary);
		if (expr->type == ce_VALUE)
		{
			// get value of symbol
			my_assert(e1->type == ca_SYM);
			atom = command_expr_new();
			if (!command_get_val(e1->u.sym, atom)) {
				command_expr_free(atom);
				atom = 0;
				break;
			}
		}
		else if (command_expr_type_equiv_class(e1->type) == ca_NUM)
		{
			// normal unary op

			e1 = command_expr_force_to_int(e1);

			e1->u.num = expr_type_unary_func(expr->type, e1->u.num);
			atom = e1; e1 = 0L;
		}
		else
		{
			command_logger(_L|LOG_ERROR,_("unexpected arguments to '%s' (%s)\n"),
						command_expr_type_name(expr->type),
						command_expr_type_name(e1->type));
			atom = 0L;
		}
		break;

	case ce_COND:
		e1 = command_expr_evaluate(expr->u.cond.cond);

		if (e1->type == ca_NUM)
		{
			// normal cond op
			e1 = command_expr_force_to_int(e1);

			if (e1->u.num)
			{
				atom = command_expr_evaluate(expr->u.cond.left);
			}
			else
			{
				atom = command_expr_evaluate(expr->u.cond.right);
			}
		}
		break;

	default:
		command_logger(_L|LOG_ABORT, _("command_expr_evaluate: unhandled type '%s'\n"),
			   command_expr_type_name(expr->type));
		atom = 0L;
	}

	if (atom && ca_ISSIMPLE(atom->type))
	{
		char *str;
		str = command_expr_atom_stringify(atom);
		logger(_L|L_3, "command_expr_evaluate: returning %s (%s)\n", 
			   command_expr_atom_name(atom->type),
			   str);
		xfree(str);
	}

	command_exprlist_free(e1);
	command_exprlist_free(e2);
	command_exprlist_free(e3);

	return atom;
}

/*
 *	ca_ENUM types look like ca_STRING, so force any such ca_STRINGs
 *	to be ca_ENUM.
 */
static command_expr *
command_expr_force_to_enum(command_expr *expr, const char *list)
{
	int val;

	if (!expr) return 0;

	switch (expr->type)
	{
	case ca_STRING:
		val = get_string_index(list, expr->u.str, 5, 0L);
		if (val < 0) {
			command_logger(_L|LOG_ERROR,_("'%s' does not match an enumeration in '%s'\n"),
						expr->u.str, list);
			return 0;
		}
		command_expr_set_enum(expr, list, val);
		break;

	case ce_ADD:
		if (!(expr->u.binary.left = command_expr_force_to_enum(
					expr->u.binary.left, list)))
			return 0;

		if (!(expr->u.binary.right = command_expr_force_to_enum(
					expr->u.binary.right, list)))
			return 0;

		if (expr->u.binary.left->type != ca_ENUM
		|| 	expr->u.binary.right->type != ca_ENUM) {
			command_logger(_L|LOG_ERROR,_("expected sum of enumeration values from '%s'\n"),
						list);
			return 0;
		}
		val = expr->u.binary.left->u.num | expr->u.binary.right->u.num;

		command_expr_free(expr->u.binary.left);
		command_expr_free(expr->u.binary.right);

		command_expr_set_enum(expr, list, val);
		break;
	}
	return expr;
}

/*
 *	evaluate a list of expressions from 'expr' and return list.
 *	if 'args' is not null, match types of arguments.
 */
static command_expr *
command_exprlist_evaluate(command_expr **exprptr, const command_arg *args)
{
	command_expr *expr;
	command_expr *node, *list = 0L;
	
	while ((expr = *exprptr))
	{
		if (args && args->type == ca_ENUM && expr->rtype == ca_STRING) {
			logger(_L|L_3, _("command_exprlist_evaluate: forcing to enum argument\n"));
			if (!(expr = command_expr_force_to_enum(expr, args->name))) {
				command_exprlist_free(list);
				return 0;
			}
			*exprptr = expr;
		}

		if (!(node = command_expr_evaluate(expr))) {
			command_exprlist_free(list);
			return 0;
		}

		if (args) {
			logger(_L|L_2, _("arg '%s' type '%s', expr type '%s'\n"),
				   args->name, command_expr_atom_name(args->type),
				   command_expr_type_name(expr->type));
		}

		list = command_exprlist_append_expr(list, node);

		exprptr = &(*exprptr)->next;
		if (args) args = args->next;
	}
	return list;
}

char *
command_expr_rtype_stringify(command_expr *expr)
{
	return command_expr_type_stringify(expr, expr->rtype);
}


/*************************/

static const char *
command_statement_name(command_statement_type type)
{
	static char stype_buf[16];

	switch (type)
	{
	case cs_EXPR:		return _("<expression>");
	case cs_LOG:		return _("log");
	case cs_ASSERT:		return _("assert");
	}
	sprintf(stype_buf, "<%d>", type); 
	return stype_buf;
}

command_statement *
command_stmtlist_append_statement(command_statement *list,
								  command_statement *stmt)
{
	command_statement **step = &list;
	while (*step) 
		step = &(*step)->next;
	*step = stmt;
	return list;
}

static bool
validate_statement(command_statement_type type, command_expr *expr)
{
	if (type == cs_EXPR && expr->type == ce_STRING) {
		command_logger(LOG_USER|LOG_ERROR, _("unknown identifier '%s'\n"), 
					   expr->u.str);
		return false;
	}
	return true;
}

command_statement *
command_statement_new_expr(command_statement_type type, 
						   command_expr *expr)
{
	command_statement *stmt;

	my_assert(type == cs_EXPR || type == cs_ASSERT);

	if (!expr)
		return 0L;

	if (!validate_statement(type, expr))
		return 0L;

	stmt = (command_statement *) xmalloc(sizeof(command_statement));

	stmt->name = command_source_current ? command_source_current->name : 0;
	stmt->line = command_source_linenum;
	stmt->type = type;
	stmt->u.expr = expr;
	stmt->next = 0L;

	return stmt;
}

command_statement *
command_statement_new_exprlist(command_statement_type type, 
							   command_expr *expr)
{
	command_statement *stmt;

	my_assert(type == cs_LOG);

	if (type == cs_LOG)
		my_assert(expr);

	if (!validate_statement(type, expr))
		return 0L;

	stmt = (command_statement *) xmalloc(sizeof(command_statement));

	stmt->name = command_source_current ? command_source_current->name : 0;
	stmt->line = command_source_linenum;
	stmt->type = type;
	stmt->u.expr = expr;
	stmt->next = 0L;

	return stmt;
}


command_statement	*command_statement_new_if(
							command_statement_type type,
							command_expr *cond,
							command_statement *iftrue,
							command_statement *iffalse)
{
	command_statement *stmt;

	my_assert(type == cs_IF);

	if (!cond)
		return 0L;

	if (!validate_statement(cs_EXPR, cond))
		return 0L;

	stmt = (command_statement *) xmalloc(sizeof(command_statement));

	stmt->name = command_source_current ? command_source_current->name : 0;
	stmt->line = command_source_linenum;
	stmt->type = type;
	stmt->u.ifstmt.cond = cond;
	stmt->u.ifstmt.iftrue = iftrue;
	stmt->u.ifstmt.iffalse = iffalse;
	stmt->next = 0L;

	return stmt;
}

void
command_statement_free(command_statement *stmt)
{
	switch (stmt->type)
	{
	case cs_EXPR:
	case cs_LOG:
	case cs_ASSERT:
		command_exprlist_free(stmt->u.expr);
		break;
	}
	xfree(stmt);
}

void
command_stmtlist_free(command_statement *list)
{
	command_statement *step = list;
	
	while (step)
	{
		command_statement *next = step->next;
		command_statement_free(step);
		step = next;
	}
}

/*******************************************/

static command_exec_context *exec_context;
static command_statement	*exec_pc;

int
command_stmtlist_exec(command_statement *list, command_expr *retval)
{
	int ret;
	
	logger(_L|L_3, "executing statement list\n");

	exec_context = command_exec_context_push(list, exec_context);

	ret = command_exec_context_execute(exec_context, retval);

	exec_context = command_exec_context_pop(exec_context);
	
	return ret;
}


command_exec_context *
command_exec_context_new(command_statement *list,
						 command_exec_context *outer)
{
	command_exec_context *context = 
		(command_exec_context *) xmalloc(sizeof(command_exec_context));

	context->stmts = list;
	context->outer = outer;
	
	return context;
}

void
command_exec_context_free(command_exec_context *context)
{
	xfree(context);
}

int
command_statement_execute(command_exec_context *context,
						  command_statement **stmtptr,
						  command_expr *ret);

int
command_exec_context_execute(command_exec_context *context,
							 command_expr *retval)
{
	command_statement *oldpc;
	int ret;

	if (!context)
		return 0;

	oldpc = exec_pc;
	exec_pc = context->stmts;

	while (exec_pc)
	{
		ret = command_statement_execute(exec_context, &exec_pc, retval);
//		if (!ret) break;
	}
	exec_pc = oldpc;
	return ret;
}

command_exec_context *
command_exec_context_push(command_statement *list,
						  command_exec_context *outer)
{
	return command_exec_context_new(list, outer);
}

command_exec_context *
command_exec_context_pop(command_exec_context *context)
{
	command_exec_context *outer;
	if (context)
	{
		outer = context->outer;
		command_exec_context_free(context);
		return outer;
	}
	else
	{
		logger(_L|LOG_ABORT, _("unnesting past NULL command_exec_context\n"));
		return 0L;
	}
}

void
command_logger(int flags, const char *format, ...)
{
	va_list va;
	char buf[256], *bptr;
	int user = (flags & LOG_TYPE_MASK) != LOG_INFO ? LOG_USER : 0;

	va_start(va, format);
	bptr = mvprintf(buf, sizeof(buf), format, va);
	if (exec_pc && exec_pc->name)
		logger(flags | user, "%s:%d: %s", 
			   exec_pc->name,
			   exec_pc->line,
			   bptr);
	else
		logger(flags | user, "%s", bptr);
	if (bptr != buf) xfree(bptr);
	va_end(va);
}

static bool
command_expr_compare_to_zero(command_expr *arg)
{
	return ((arg->rtype == ca_SYM && arg->u.sym)
			|| (arg->rtype == ca_NUM && arg->u.num)
			|| (arg->rtype == ca_STRING && arg->u.str && *arg->u.str)
			|| (arg->rtype == ca_ENUM && arg->u.num));
}

int
command_statement_execute(command_exec_context *context,
						  command_statement **stmtptr,
						  command_expr *ret)
{
	command_statement *stmt = *stmtptr;
	command_expr *expr, *arg, *val;

	if (!stmt)
		return 0;

	*stmtptr = stmt->next;

	switch (stmt->type)
	{
	case cs_EXPR:
		arg = command_expr_evaluate(stmt->u.expr);
		if (arg) *ret = *command_expr_copy_expr(arg);
		command_expr_free(arg);
		break;
		
	case cs_LOG:
		expr = stmt->u.expr;
		if (!expr || !(arg = command_expr_evaluate(expr)))
			return 0;

		expr = expr->next;
		while (expr && (val = command_expr_evaluate(expr)))
		{
			command_expr_print(LOG_USER | arg->u.num, val);
			*ret = *command_expr_copy_expr(val);
			command_expr_free(val);
			expr = expr->next;
		}
		command_expr_free(arg);
		break;

	case cs_ASSERT:
	{
/*
		command_statement *test;

		expr = stmt->u.expr;
		if (!expr || !(arg = command_expr_evaluate(expr)))
			return 0;

		test = command_parse_text(arg->u.str);
		if (!command_stmtlist_exec(test, ret))
		{
			command_stmtlist_free(test);
			command_expr_free(arg);
			return 0;
		}
		command_stmtlist_free(test);
*/
		bool istrue;

		expr = stmt->u.expr;
		if (!expr || !(arg = command_expr_evaluate(expr)))
			return 0;

		istrue = command_expr_compare_to_zero(expr);

		if (!istrue)
		{
			command_logger(_L|LOG_ERROR,_("assertion '%s' failed\n"), arg->u.str);
			command_expr_free(arg);
			return 0;
		}
		command_expr_free(arg);
	}
	break;

	case cs_IF:
	{
		int istrue;

		expr = stmt->u.ifstmt.cond;
		if (!expr || !(arg = command_expr_evaluate(expr)))
			return 0;

		istrue = command_expr_compare_to_zero(arg);

		command_expr_free(arg);
		if (istrue)
		{
			return command_stmtlist_exec(stmt->u.ifstmt.iftrue, ret);
		}
		else if (stmt->u.ifstmt.iffalse)
		{
			return command_stmtlist_exec(stmt->u.ifstmt.iffalse, ret);
		}
	}
	break;
		
	default:
		command_logger(_L|LOG_ABORT, _("unhandled statement type '%s'\n"),
			   command_statement_name(stmt->type));
	}
	return 1;
}

/************************/

/*
 *	keep track of filenames so we don't have to duplicate
 *	them throughout the command_statement lists
 */
static command_source *command_source_list;
command_source *command_source_current;
int command_source_linenum;

command_source 	*
command_source_register(char *name)
{	
	command_source **ptr, *nw;
   
	ptr = &command_source_list;
	while (*ptr && strcmp((*ptr)->name, name))
		ptr = &(*ptr)->outer;

	nw = (command_source *) xmalloc(sizeof(command_source));
	nw->name = xstrdup(name);
	nw->outer = *ptr;
	
	*ptr = nw;
	return nw;
}

int
command_lexer_setup_text(char *name, char *text, int len)
{
	command_source_current = command_source_register(name);
	command_source_linenum = 1;

	return lexer_push_text(name, text, len) != NULL;
}

int
command_lexer_setup_file(char *filename)
{
	command_source_current = command_source_register(filename);
	command_source_linenum = 1;

	return lexer_push_file(filename) != NULL;
}


/**********************/


void
command_init(void)
{
	universe = command_symbol_table_new(_("V9t9 Options"),
										_("This is the complete list of options and commands "
										"you may specify in a configuration file or command prompt."),
										NULL, NULL, NULL);
	session_only = false;

	if (OS_CreateMutex(&command_mutex) != OS_NOERR)
	{
		logger(LOG_FATAL, _("Could not create mutex for command parser"));
	}
}

command_statement *
command_parse_file(char *name)
{
	command_statement *stmts;
	command_source *command_source_outer = command_source_current;

	if (!command_lexer_setup_file(name))
	{
		return 0L;
	}

	stmts = command_parse(universe);

	command_source_current = command_source_outer;

	return stmts;
}

command_statement *
command_parse_text(char *str)
{
	command_statement *stmts;
	command_source *command_source_outer = command_source_current;

	if (!command_lexer_setup_text("<command_parse_text>", str, strlen(str)))
		return 0;

	stmts = command_parse(universe);

	command_source_current = command_source_outer;

	return stmts;
}

int
command_exec_file(char *name)
{
	command_statement *stmt = command_parse_file(name);
	command_expr retval;
	if (stmt)
	{
		command_stmtlist_exec(stmt, &retval);
		command_stmtlist_free(stmt);
	}
	return 1;
}

int
command_exec_text(char *str)
{
	command_statement *stmt = command_parse_text(str);
	command_expr retval;
	if (stmt)
	{
		command_stmtlist_exec(stmt, &retval);
		command_stmtlist_free(stmt);
	}
	return 1;
}

bool	command_get_session_filter(void)
{
	return session_only;
}

void	command_set_session_filter(bool allow_session_only)
{
	session_only = allow_session_only;
}

/**********************/

#ifdef STANDALONE

int         a, b;
OSPathSpec  dir;
char        buf[4], buf2[16];
int         c;

int
main(int argc, char **argv)
{

/*
	command_symbol_table *table = 
		command_symbol_table_new(NULL, 
			command_symbol_new("a", NULL,
				command_arg_new_num(sizeof(a), &a, NULL
				),
				command_arg_new_num(sizeof(a), &a, NULL
				),
			command_symbol_new("b", NULL,
				command_arg_new_num(sizeof(b), &b, NULL
				),
				command_arg_new_num(sizeof(b), &b, NULL
				),
			command_symbol_new("ni", NULL,
				command_arg_new_num(sizeof(c), &c, NULL
				),
				command_arg_new_string(sizeof(buf), buf,
				command_arg_new_num(sizeof(c), &c, NULL
				)), 
			command_symbol_new("c", NULL,
				command_arg_new_string(sizeof(buf), buf, NULL
				),
				command_arg_new_string(sizeof(buf), buf, NULL
				), 
			command_symbol_new("d", NULL,
				command_arg_new_string(sizeof(buf2), buf2, NULL
				),
				command_arg_new_string(sizeof(buf2), buf2, NULL
				),
			NULL
			)))))
		);
*/
	char       *text = "#include \"test.cnf\"";

//  xminfo();

	command_symbol_table_add(universe,
							 command_symbol_new("DelayBetweenInstructions",
												"Sets a constant delay between instructions",
												NULL,
												NULL,
												command_arg_new_num("cycles",
																	"number of cycles to count",
																	NULL,
																	ARG_NUM
																	(a),
																	NULL),
							command_symbol_new
												("ModulesPath",
												 "Set directory to search for module ROMs",
												 NULL, NULL,
												 command_arg_new_pathspec
												 ("dir", "path to directory",
												  NULL, &dir, NULL), 

												 NULL)));

	command_help();

	if (!command_lexer_setup_text("test", text, strlen(text)))
		return -1;
	command_parse(universe);

	printf("a=%d, b=%d, buf=%s, buf2=%s, c=%d\n", a, b, buf, buf2, c);

//  xminfo();
}
#endif
