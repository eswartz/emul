/*
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

#ifndef __COMMAND_H__
#define __COMMAND_H__

//#include "OSLib.h"

#include "centry.h"

typedef enum 
{
	caa_READ, caa_WRITE
}	command_arg_action_type;	/* tasks for action */

struct command_arg;
typedef int (*command_arg_action)(struct command_arg *self, command_arg_action_type task);

#define DECL_ARG_ACTION(name) int name(struct command_arg *arg, command_arg_action_type task)

//	Flags for commands:  bitmask
typedef enum
{
	c_NONE = 0,
	c_STATIC = 0,			// static value for saving
	c_DYNAMIC = 1,			// dynamically determines config, 
							// pass csa_READ multiple times with increasing iter
	c_DONT_SAVE = 2,		// don't save value back to config
	c_SESSION_ONLY = 4,		// if set, item should be saved for sessions but 
							// not in general config
	c_CONFIG_ONLY = 8,		// if set, item should only be saved in basic
							// config file, not in session
	c_OPTIONAL_ARGS = 16,	// all the arguments are optional
}	command_symbol_flags;

//	Types of atoms
typedef enum 
{
	ca_VOID, ca_SYM, ca_NUM, ca_STRING, ca_ENUM
}	command_atom_type;

//	Types of command arguments
typedef enum 
{
	// includes command_atom_types ca_XXXX

	// complex types which appear in command_symbol argument lists
	ca_SPEC = ca_ENUM + 1,
	ca_PATHSPEC, ca_NAMESPEC,
	ca_TOGGLE,
	ca_LAST
}	command_arg_type;

#define ca_ISSIMPLE(x)	((x) >= ca_VOID && (x) <= ca_ENUM)

const char 				*command_expr_atom_name(command_atom_type type);

#define ca_ISSPEC(x) ((x)==ca_SPEC || (x)==ca_PATHSPEC || (x)==ca_NAMESPEC)

typedef struct command_arg {
	char	*name;					/* name of argument */
	char	*help;					/* usage info */
	command_arg_action action;		/* action associated with argument */
	
	command_arg_type type;			/* ca_XXX */
	union	
	{
		struct	
		{
			int		sz;				/* 1,2,4 = size of pointed var */
			void	*mem;
		}			num;			/* ca_NUM, ca_ENUM (w/name) */
		struct	
		{
			int		sz;				/* 1,2,4 = size of pointed var */
			void	*mem;
			int		flag;
		}			toggle;			/* ca_TOGGLE */
		struct
		{
			int		maxlen;			/* max len of string [memory] */
			union
			{
				char	*mem;		/* if maxlen >= 0 */
				char	**ptr;		/* if maxlen < 0, we store ptr to buffer */
			}		m;
		}			string;			/* ca_STRING */
		struct
		{
			struct OSSpec	*mem;
		}			spec;			/* ca_SPEC */
		struct
		{
			struct OSPathSpec	*mem;
		}			pathspec;		/* ca_PATHSPEC */
		struct
		{
			struct OSNameSpec	*mem;
		}			namespec;		/* ca_NAMESPEC */
	}		u;
	struct 	command_arg *next;
}	command_arg;

enum
{
	csa_READ, csa_WRITE
};	/* tasks for action */

struct command_symbol;
typedef int (*command_symbol_action)(struct command_symbol *self, int task, int iter);

// task is caa_XXX, iter=0..x for caa_READ_CONFIG
#define DECL_SYMBOL_ACTION(name) int name(struct command_symbol *sym, int task, int iter)

#define SYM_ARG_1st	(sym->args)
#define SYM_ARG_2nd (SYM_ARG_1st ? sym->args->next : 0L)
#define SYM_ARG_3rd (SYM_ARG_2nd ? sym->args->next->next : 0L)
#define SYM_ARG_4th (SYM_ARG_3rd ? sym->args->next->next->next : 0L)
#define SYM_ARG_5th (SYM_ARG_4th ? sym->args->next->next->next->next : 0L)
#define SYM_ARG_6th (SYM_ARG_5th ? sym->args->next->next->next->next->next : 0L)

#define SYMBOL_ACTION_BIT_TOGGLE(var, bit) \
	do {											\
		int flag;									\
		if (task == caa_WRITE) {					\
			command_arg_get_num(sym->args, &flag);	\
			if (flag)								\
				var |= bit;							\
			else									\
				var &= ~bit;						\
		} else {									\
			flag = (var & bit) != 0;				\
			command_arg_set_num(sym->args, flag);	\
		}											\
	}	while (0)


typedef struct command_symbol {
	char	*name;					/* name (case-insensitive) */
	char	*help;					/* description of use */
	command_symbol_flags flags;		/* flags for symbol */
	command_symbol_action action;	/* action associated with command */
	struct	command_arg *ret;		/* value if used on RHS */
	struct	command_arg	*args;		/* arguments we take */
	struct 	command_symbol *next;
}	command_symbol;

typedef struct command_symbol_table {
	char	*name;					/* name of table */
	char	*help;					/* more info */
	struct 	command_symbol_table *sub;	/* table of subcommands */
	struct 	command_symbol *list;		/* list of symbols */
	struct 	command_symbol_table *next;	/* next table */
}	command_symbol_table;

extern command_symbol_table *universe;

command_symbol_table *
command_symbol_table_new(char *name, char *help, 
		command_symbol *list, command_symbol_table *sub, 
		command_symbol_table *next);

command_symbol_table *
command_symbol_table_add_subtable(command_symbol_table *parent,
		command_symbol_table *table);

command_symbol_table *
command_symbol_table_add(command_symbol_table *parent, command_symbol *list);

/*	Any symbol may define ret==RET_FIRST_ARG to mean return value of first argument. */

#define RET_FIRST_ARG (command_arg *)(-1)
command_symbol *
command_symbol_new(char *name, char *help, command_symbol_flags flags,
		command_symbol_action action, command_arg *ret, command_arg *args, 
		command_symbol *next);

int		command_arg_get_rtype(command_symbol *sym);


#define ARG_NUM(x)	sizeof(x), &x
#define NEW_ARG_NUM(t) sizeof(t), xmalloc(sizeof(t))
#define NEW_ARG_CONST_NUM(x)	sizeof(int), xintdup(x)

command_arg *
command_arg_new_num(char *name, char *help, command_arg_action action,
		int sz, void *mem, command_arg *next);

/*	You may specify an unbounded string array by passing
	maxlen < 0 and treating 'str' as a pointer to a pointer to
	the string. */
#define ARG_STR(x)	sizeof(x), x
#define NEW_ARG_STR(l) l, xmalloc(l)
#define NEW_ARG_STRBUF(p) -1, p
#define NEW_ARG_NEW_STRBUF -1, xcalloc(sizeof(void *))

command_arg *
command_arg_new_string(char *name, char *help, command_arg_action action,
		int maxlen, void *str, command_arg *next);

command_arg *
command_arg_new_spec(char *name, char *help, command_arg_action action,
		struct OSSpec *spec, command_arg *next);

command_arg *
command_arg_new_pathspec(char *name, char *help, command_arg_action action,
		struct OSPathSpec *pathspec, command_arg *next);

command_arg *
command_arg_new_namespec(char *name, char *help, command_arg_action action,
		struct OSNameSpec *namespec, command_arg *next);

command_arg *
command_arg_new_toggle(char *name, char *help, command_arg_action action,
		int sz, void *mem, int val, command_arg *next);

command_arg *
command_arg_new_enum(char *list, char *help, command_arg_action action,
		int sz, void *mem, command_arg *next);

/*****************/

void	command_arg_read_num(command_arg *arg, int *val);
int	command_arg_get_num(command_arg *arg, int *val);
int	command_arg_set_num(command_arg *arg, int val);

void	command_arg_read_string(command_arg *arg, char **str);
int	command_arg_get_string(command_arg *arg, char **str);
int	command_arg_set_string(command_arg *arg, const char *str);

void	command_arg_read_spec(command_arg *arg, char **str);
int	command_arg_get_spec(command_arg *arg, char **str);
int	command_arg_set_spec(command_arg *arg, const char *str);

void	command_arg_read_toggle(command_arg *arg, char **val);
int	command_arg_get_toggle(command_arg *arg, int *val);
int	command_arg_set_toggle(command_arg *arg, int val);
int	command_arg_set_toggle_str(command_arg *arg, const char *val);

void	command_arg_read_enum(command_arg *arg, char **val);
int	command_arg_get_enum(command_arg *arg, int *val);
int	command_arg_set_enum(command_arg *arg, int val);
int	command_arg_set_enum_str(command_arg *arg, const char *val);

int	command_match_symbol(const command_symbol_table *table,
				const char *name, command_symbol **sym);
void
command_match_symbols(const command_symbol_table *table,
					const char *name, 
					command_symbol ***matches, int *nmatches);

struct command_expr;
int command_set_args(struct command_expr *ret, command_symbol *sym, 
					 struct command_expr *list);

int command_get_val(command_symbol *sym, struct command_expr *val);

/* 	Utility function to match 'name' against a list of symbols in 'list'.
	'need_long_prefix' is >=0 if 'name' must match at least 
	that many characters.  (i.e., 'f' can match 'foo|bar' if 
	'need_long_prefix' is 1, but not if it is 2.) */
int  
symbol_match(const char *list, const char *name, int min_prefix_length);

/*	Tree structure for parsed commands  */

typedef enum
{
	// ca_XXX take the first positions here
	ce_VOID = ca_VOID,
	ce_SYM = ca_SYM,
	ce_NUM = ca_NUM,
	ce_STRING = ca_STRING,	
	ce_ENUM = ca_ENUM,
	// complex types not used
	ce_ASSIGN = ca_LAST,			// assignment expression
	ce_ADD, ce_SUB, 				// binary arithmetic operations
	ce_MUL, ce_DIV, ce_MOD,
	ce_OR, ce_AND, ce_XOR, 
	ce_LSHIFT, ce_RSHIFT, 
	ce_LOR, ce_LAND, ce_LXOR,		// binary logical operations
	ce_EQ, ce_NE, ce_LE, ce_GE, ce_LT, ce_GT,
	ce_NEG, ce_NOT, ce_INV,			// unary
	ce_VALUE,						// get value of symbol
	ce_COND,							// trinary
	ce_LAST
}	command_expr_type;

const char 				*command_expr_type_name(command_expr_type type);

#define CASE_EXPR_BINARY \
	case ce_ASSIGN: \
	case ce_ADD: case ce_SUB: case ce_MUL: case ce_DIV: case ce_MOD: \
	case ce_OR: case ce_AND: case ce_XOR: \
	case ce_LSHIFT: case ce_RSHIFT: \
	case ce_LOR: case ce_LAND: case ce_LXOR: \
	case ce_EQ: case ce_NE: case ce_GE: case ce_LE: case ce_LT: case ce_GT \

#define CASE_EXPR_UNARY \
	case ce_NEG: case ce_NOT: case ce_INV: case ce_VALUE

typedef struct command_expr command_expr;

struct command_expr
{
	command_expr_type		type;				   	// ca_XXX or ce_XXX
	command_atom_type		rtype;					// ca_XXX return type
	union
	{
		int					num;	// ca_NUM, ca_TOGGLE, ca_ENUM
		struct {
			int				val;
			const char 		*list;	// not copied
		}	enum_;					// ca_ENUM
		char				*str;	// ca_STRING, ca_SPEC, ca_PATHSPEC, ca_NAMESPEC
		command_symbol		*sym;	// ca_SYM
		struct {
			command_expr	*left, 
							*right;	// binary operations, ca_ASSIGN
		}  	binary;
		struct {
			command_expr	*cond,
							*left,
							*right; // ce_COND
		}	cond;
		command_expr		*unary;	// unary operations
	}	u;
	command_expr			*next;	// next item in a list
};

command_expr 		*command_expr_new_num(int num);
command_expr 		*command_expr_new_string(const char *str);
command_expr 		*command_expr_new_symbol(command_symbol *sym);
command_expr 		*command_expr_new_enum(const char *list, int val);

command_expr		*command_expr_copy_expr(command_expr *atom);

command_expr 		*command_expr_new_binary(command_expr_type type, 
											 command_expr *left, 
											 command_expr *right);
command_expr 		*command_expr_new_unary(command_expr_type type, 
											command_expr *expr);
command_expr 		*command_expr_new_cond(command_expr *cond,
										   command_expr *left, 
										   command_expr *right);

command_expr		*command_exprlist_append_expr(command_expr *list,
												  command_expr *expr);

void				command_expr_free(command_expr *expr);
void				command_exprlist_free(command_expr *list);

char 				*command_expr_atom_stringify(command_expr *expr);
char 				*command_expr_rtype_stringify(command_expr *expr);

command_expr_type	command_expr_type_equiv_class(command_expr_type type);

typedef enum
{
	cs_EXPR,		// expression statement (throw away result)
	cs_LOG,			// log statement (first param is LOG_xxx info)
	cs_ASSERT,		// assert statement (emit error if not true)
	cs_IF,			// if statement (evaluate condition, and choose true or false statement list)
}	command_statement_type;

const char 				*command_statement_type_name(command_statement_type type);

typedef struct command_statement command_statement;

struct command_statement
{
	command_statement_type	type;		// cs_XXX
	union
	{
		command_expr		*expr;	// cs_LOG, cs_ASSIGN, cs_ASSERT
		struct
		{
			command_expr	*cond;
			command_statement *iftrue, *iffalse;
		}					ifstmt;	// cs_IF
	}	u;
	command_statement		*next;
	const char *name; int line;			// source context
};

command_statement	*command_stmtlist_append_statement(
							command_statement *list,
							command_statement *stmt);
command_statement	*command_statement_new_exprlist(
							command_statement_type type,
							command_expr *expr);
command_statement	*command_statement_new_expr(
							command_statement_type type,
							command_expr *expr);
command_statement	*command_statement_new_if(
							command_statement_type type,
							command_expr *cond,
							command_statement *iftrue,
							command_statement *iffalse);

void				command_statement_free(command_statement *stmt);
void				command_stmtlist_free(command_statement *list);

int					command_stmtlist_exec(command_statement *stmt,
										  command_expr *retval);

typedef struct command_exec_context command_exec_context;

struct command_exec_context
{
	command_statement *stmts;
	command_exec_context *outer;
};

command_exec_context *
command_exec_context_new(command_statement *list,
						 command_exec_context *outer);

void				
command_exec_context_free(command_exec_context *context);

int
command_exec_context_execute(command_exec_context *context,
							 command_expr *retval);

command_exec_context *
command_exec_context_push(command_statement *list,
						  command_exec_context *outer);

command_exec_context *
command_exec_context_pop(command_exec_context *context);

void	
command_logger(int flags, const char *format, ...);



/*	Interface to lexer  */

typedef struct command_source command_source;

struct command_source
{
	const char *name;
	command_source *outer;
};

extern command_source	*command_source_current;
extern int				command_source_linenum;

command_source 	*command_source_register(char *name);
int	command_lexer_setup_text(char *name, char *text, int len);
int	command_lexer_setup_file(char *filename);

/*	Interface to parser */

command_statement	*command_parse(command_symbol_table *universe);
command_statement	*command_parse_file(char *name);
command_statement	*command_parse_text(char *str);

/*	Interface to emulator */

void	command_init(void);
int		command_exec_file(char *name);
int		command_exec_text(char *str);

/*	Filter execution of session-only commands */

bool	command_get_session_filter(void);
void	command_set_session_filter(bool allow_session_only);

/*	Interface to help */
void	command_help(void);
void	command_help_symbol(const char *var);

/*	Mutex that must be held while executing commands */
#ifdef __OSLIB_H__
extern OSMutex	command_mutex;
#endif

#include "cexit.h"	

#endif
