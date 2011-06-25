
/*	Parser file for configuration engine */

/*
  (c) 1994-2000 Edward Swartz

  This library is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Library General Public License for more details.
 
  You should have received a copy of the GNU Library General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
*/

/*
  $Id$
 */

%{

#include "v9t9_common.h"
#include "command.h"
#include "command_lexer.h"

#define _L	LOG_COMMANDS|LOG_INFO

//#define YYDEBUG 1
#define YYERROR_VERBOSE 1

void parse_error(const char *format, ...);
int yyerror(char *s);
int yylex(void);

extern command_symbol_table *command_scope;

static command_statement *stmtlist;

#define  NUMBINOP(res, ce, name, a, b) \
		 res = command_expr_new_binary(ce, a, b)

#define  NUMUNOP(res, ce, name, a) \
		 res = command_expr_new_unary(ce, a)

static command_symbol *parse_symbol(char *str)
{
	command_symbol *sym;
	if (!command_match_symbol(command_scope, str, &sym))
	{	
		parse_error(_("unknown identifier '%s'\n"), str);
		sym = 0L;
	}
	//printf("Symbol = %s\n", str);
	xfree(str);
	return sym;
}

%}

%union 
{
	command_expr		*expr;
	command_symbol		*sym;
	command_statement	*stmt;
}

%token <expr>	NUM
%token <expr>	STRING
%token <expr>	IDSTRING
%token <expr>	ERR
%token <expr>	ID

%token LSHIFT
%token RSHIFT
%token COMPLE
%token COMPGE
%token COMPEQ
%token COMPNE
%token COMPAND
%token COMPOR
%token COMPXOR

%token PRINT
%token LOGGER
%token ERROR
%token WARNING
%token INFO
%token ASSERT
%token IF
%token ELSE
%token FI
%token ELSIF

%type <expr>	expr
%type <expr>	atom
%type <expr>	cond

%type <expr>	unary
%type <expr>	term
%type <expr>	comp
%type <expr>	shift
%type <expr>	factor
%type <expr>	logcond
%type <expr>	binlogcond
%type <expr>	compeq

%type <expr>	assign

%type <expr>	exprlist

%right '='
%left '-' '+' '&'
%right '*' '/' '|'
%left NEG

%type <stmt>	toplevel
%type <stmt>	stmtlist
%type <stmt>	exprstmt
%type <stmt>	assertstmt
%type <stmt>	msgstmt
%type <stmt>	stmt
%type <stmt>	ifstmt
%type <stmt>	iftail

%%

toplevel: stmtlist			{ stmtlist = $1; }

stmtlist: /* empty */		{ $$ = 0L; }
		| stmtlist stmt		{
				if ($2) { 
				   $$ = command_stmtlist_append_statement($1, $2); 
				} else {
				   $$ = $1; 
				}
								}
;

eol:	';' 
		| '\n'


stmt:	eol					{ $$ = 0L; }
		| exprstmt			{ $$ = $1; }
		| assertstmt 		{ $$ = $1; }
		| msgstmt 			{ $$ = $1; }
		| ifstmt			{ $$ = $1; }
;

exprstmt:	IDSTRING exprlist	{ 
				command_symbol *sym = parse_symbol($1->u.str);
			    if (sym) 
					$$ = command_statement_new_expr(cs_EXPR,
							command_expr_new_binary(ce_ASSIGN, 
													command_expr_new_symbol(sym), 
													$2)); 
				else
					$$ = 0L;
							}
			| assign 		{ 
				    $$ = command_statement_new_expr(cs_EXPR, $1);
							}
			| '(' expr ')'	{ 
				    $$ = command_statement_new_expr(cs_EXPR, $2);
							}


;


assertstmt: ASSERT expr  	{ 
					$$ = command_statement_new_expr(cs_ASSERT, 
													$2); 
							}

msgstmt: LOGGER exprlist		{
				if (!$2) {
				   parse_error(_("log needs a bitmask argument\n"));
				   $$ = 0L;
				}
				else
				    $$ = command_statement_new_exprlist(cs_LOG, 
								$2);
						}
	|	PRINT exprlist			{ 
				$$ = command_statement_new_exprlist(cs_LOG, 
						command_exprlist_append_expr( 
							command_expr_new_num(LOG_USER),
							$2));
								}
	|	ERROR exprlist			{ 
				$$ = command_statement_new_exprlist(cs_LOG, 
					command_exprlist_append_expr(
						command_expr_new_num(LOG_USER|LOG_ERROR),
						$2));
								}
	|	WARNING exprlist		{	
				$$ = command_statement_new_exprlist(cs_LOG, 
					command_exprlist_append_expr( 
						command_expr_new_num(LOG_USER|LOG_WARN),
						$2));
								}
	|	INFO exprlist			{ 
				$$ = command_statement_new_exprlist(cs_LOG, 
					command_exprlist_append_expr(
						command_expr_new_num(LOG_USER|LOG_INFO),
						$2));
								}

ifstmt: IF expr stmtlist iftail FI	{
			   if ($2->rtype == ca_VOID) {
			   		parse_error(_("expected non-void condition for if\n"));
					$$ = 0L;
				}
			   else
			   		$$ = command_statement_new_if(cs_IF, $2, $3, $4);
										}

iftail: /* empty */						{ $$ = 0L; }
	|	ELSE stmtlist					{ $$ = $2; }
	|	ELSIF expr stmtlist iftail	{
			   if ($2->rtype == ca_VOID) {
			   		parse_error(_("expected non-void condition for if\n"));
					$$ = 0L;
			   }
			   else
			   		$$ = command_statement_new_if(cs_IF, $2, $3, $4);
										}

exprlist: 	/* empty */		{ $$ = 0L; }
			| exprlist expr { $$ = command_exprlist_append_expr($1, $2); }
;


expr:		assign
			| cond				
		   
;

assign:		/* invoke ID with an argument */
			IDSTRING '=' expr		{ 
				command_symbol *sym = parse_symbol($1->u.str);

				if (sym)
					$$ = command_expr_new_binary(ce_ASSIGN, 
												 command_expr_new_symbol(sym), 
												 $3);
				else
					$$ = 0L;
								}
;


cond:		logcond				{ $$ = $1; }
			| logcond '?' cond ':' cond
							    {
				if ($1->rtype == ca_VOID)
					parse_error(_("cannot use '?:' with 'void' condition"));
				else if (command_expr_type_equiv_class($3->rtype) != 
						command_expr_type_equiv_class($5->rtype))
					parse_error(_("type mismatch in alternatives to '?:' (%s, %s)"),
								command_expr_atom_name($3->rtype),
								command_expr_atom_name($5->rtype));
			   else
				    $$ = command_expr_new_cond($1, $3, $5);
								}
;

logcond:	binlogcond			{ $$ =  $1; }
			| logcond COMPAND binlogcond { NUMBINOP($$, ce_LAND,"&&",$1,$3);	}
			| logcond COMPOR binlogcond { NUMBINOP($$, ce_LOR,"||",$1,$3); }
			| logcond COMPXOR binlogcond { NUMBINOP($$, ce_LXOR, "^^", $1,$3); }
;

binlogcond:	compeq				{ $$ = $1; }
			| binlogcond '&' compeq	 { NUMBINOP($$, ce_AND, "&",$1,$3); }
			| binlogcond '|' compeq	 { NUMBINOP($$, ce_OR, "|",$1,$3); }
			| binlogcond '^' compeq	 { NUMBINOP($$, ce_XOR, "^",$1,$3); }
;

compeq:		comp					{ $$ = $1; }
			| compeq COMPEQ comp 	{ NUMBINOP($$, ce_EQ,"==",$1,$3); }
			| compeq COMPNE comp 	{ NUMBINOP($$, ce_NE,"!=",$1,$3); }
;

comp:		shift					{ $$ = $1; }
			| comp COMPLE shift 	{ NUMBINOP($$, ce_LE,"<=",$1,$3); }
			| comp COMPGE shift 	{ NUMBINOP($$, ce_GE,">=",$1,$3); }
			| comp '<' shift 		{ NUMBINOP($$, ce_LT,"<",$1,$3); }
			| comp '>' shift 		{ NUMBINOP($$, ce_GT,">",$1,$3); }
;								

shift:		factor					{ $$ = $1; }
			| shift LSHIFT factor 	{ NUMBINOP($$, ce_LSHIFT,"<<",$1,$3); }
			| shift RSHIFT factor  	{ NUMBINOP($$, ce_RSHIFT,">>",$1,$3); }

factor:		term					{ $$ = $1; }
			| factor '+' term		{ NUMBINOP($$, ce_ADD, "+", $1, $3); }

			| factor '-' term 	{ NUMBINOP($$, ce_SUB,"-",$1,$3); }
;

term:		 unary				{ $$ = $1; }
			| term '*' unary 	{ NUMBINOP($$, ce_MUL,"*",$1,$3); }
			| term '/' unary 	{ NUMBINOP($$, ce_DIV,"/",$1,$3); }

unary:		atom				{ $$ = $1; }

			| '-' unary			{ NUMUNOP($$, ce_NEG, _("negation"), $2); }

			| '!' unary			{ NUMUNOP($$, ce_NOT, "!", $2); }

			| '~' unary			{ NUMUNOP($$, ce_INV, "~", $2); }

;

atom:		 ERR				{ $$ = 0L; yyerrok; }
			| NUM				{ $$ = $1; }
			| STRING			{ $$ = $1; }
				/* invoke ID as a function with arguments */
			| IDSTRING '(' exprlist ')'	{
				command_symbol *sym = parse_symbol($1->u.str);

				if (sym)
					if ($3) {
						$$ = command_expr_new_binary(ce_ASSIGN, 
													 command_expr_new_symbol(sym), 
													 $3);
					} else {
						$$ = command_expr_new_unary(ce_VALUE, 
													command_expr_new_symbol(sym));
					}
				else
					$$ = 0L;
								}
			| IDSTRING			{ $$ = $1; $$->type = ce_STRING; $$->rtype = ca_STRING; }
			| '(' expr ')'	 	{ $$ = $2; }
;


%%

void parse_error(const char *format, ...)
{
	va_list va;
	static char buf[256], *bptr;
	va_start(va, format);
	bptr = mvprintf(buf, sizeof(buf), format, va);
	lexer_error("%s", bptr);
}

int yyerror(char *s)
{
	parse_error("%s\n",s);
	return 0;
}

command_statement *
command_parse(command_symbol_table *universe)
{
	command_scope = universe;
	// ignore errors
	stmtlist = 0L;
	while (yyparse());
	return stmtlist;
}

