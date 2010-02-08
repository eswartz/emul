/*	scanner for configuration tokens  */

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
	#define YY_NEVER_INTERACTIVE 1
	#define YY_INPUT(buf,result,maxsize) result = command_context_getstring(cf, buf, maxsize)

	#define __COMMAND_L__

    #include "moduledb.h"
    #include "v9t9_common.h"
	#include "command_lexer.h"
    #include "command.h"
	#include "command_parser.h"


    #define _L LOG_COMMANDS | LOG_INFO

	
	#undef DEBUG
//	#define DEBUG 1


	static int 
	command_context_getstring(command_context *context, 
		char *buf, int maxsize);

	command_symbol_table *command_scope = NULL;

	#define TEST_MEMERR(x)  do { if ((x) != OS_NOERR) logger(_L | LOG_FATAL,_("Out of memory\n")); } while (0)

	/*	Current command context being used by the scanner. */
	command_context *cf;
	
	/*	String buffer */
	OSHandle lexer_string;

		// F_V9t9 and F_TIFILES
	#include "fiad.h"
%}

%x  grouper
%x	comment
%x	preproc
%x 	string

DIGIT			[0-9]
HEXDIG			[0-9a-fA-F]
OCTDIG			[0-7]
	/* allow '/' as a string char for filepaths */
STRINGFIRST     [A-Za-z_/]
STRINGCHAR	    [^ \t\r\n=()\";+\-*^&#]
STRING			({STRINGFIRST})({STRINGCHAR})*
EOLCH			[\r\n]
EOL				("\r\n"|"\r"|"\n")
WS				[ \t]
SIGN            ("-")*

%%


{SIGN}"0x"{HEXDIG}+	{
					int num;
					int sign = (*yytext == '-');
				#ifdef DEBUG
					logger(_L | LOG_USER, _("a hex number: %s\n"), yytext);
				#endif
					num = strtoul(yytext+2+sign, NULL, 16);

					yylval.expr = command_expr_new_num(num* (sign ? -1 : 1));
					return NUM;
				}

{SIGN}"$"{HEXDIG}+	{
					int num;
					int sign = (*yytext == '-');
				#ifdef DEBUG
					logger(_L | LOG_USER, _("a hex number: %s\n"), yytext);
				#endif
					num = strtoul(yytext+1+sign, NULL, 16);
					yylval.expr = command_expr_new_num(num * (sign ? -1 : 1));
					return NUM;
				}

{SIGN}"0"{OCTDIG}+ 	{
					int sign = (*yytext == '-');
				#ifdef DEBUG
					logger(_L | LOG_USER, _("an oct number: %s\n"), yytext);
				#endif
					yylval.expr = command_expr_new_num(
								strtoul(yytext+sign, NULL, 8)*(sign ? -1 : 1));
					return NUM;
				}

{SIGN}{DIGIT}+		{
					int sign = (*yytext == '-');
				#ifdef DEBUG
					logger(_L | LOG_USER, _("a decimal number: %s\n"), yytext);
				#endif
					yylval.expr = command_expr_new_num(
								strtoul(yytext+sign, NULL, 10)*(sign ? -1 : 1));
					return NUM;	
				} 

">>"			return RSHIFT;
"<<"			return LSHIFT;
">="			return COMPGE;
"<="			return COMPLE;
"==" 			return COMPEQ;
"!="|"<>"		return COMPNE;
"&&"			return COMPAND;
"||"			return COMPOR;
"^^"			return COMPXOR;

	/* keywords */

".print"		return PRINT;
".assert"		return ASSERT;
".error"		return ERROR;
".warning"		return WARNING;
".info"			return INFO;
".logger"		return LOGGER;

".if"			return IF;
".else"			return ELSE;
".fi"			return FI;
".elsif"		return ELSIF;

"\\"{EOL}		{
					command_source_linenum = cf->line++;
				}

{EOL}			{
				#ifdef DEBUG
					logger(_L | LOG_USER, _("line ending\n"));
				#endif
					command_source_linenum = cf->line++;
					return '\n';
				}


"#"				BEGIN(preproc);

<preproc>"include"{WS}+"\""([^\r\n]+)"\""{WS}* { 
					lexer_include(yytext);
					BEGIN(INITIAL);
				}

<preproc>"include"{WS}.* {
					lexer_error(_("incorrectly formed #include statement"));
				}

<preproc>{EOL}	{ BEGIN(INITIAL); command_source_linenum = cf->line++; }

<preproc>.		/* ignore rest of comment */

"//"[^\r\n]*	/* ignore rest of chars on line */

{WS}			/* ignore whitespace */

 /* comment rules stolen from flex manual */

"/*"			BEGIN(comment);

<comment>[^*\r\n]*
<comment>[^*\r\n]*{EOL}			command_source_linenum = cf->line++;
<comment>"*"+[^*/\r\n]*
<comment>"*"+[^*/\r\n]*{EOL} 	command_source_linenum = cf->line++;
<comment>"*"+"/" 				BEGIN(INITIAL);

 /* grouper comment */

"["			BEGIN(grouper);

<grouper>[^]\r\n]*
<grouper>[^]\r\n]*{EOL}			command_source_linenum = cf->line++;
<grouper>"]"					BEGIN(INITIAL);

<<EOF>>			{ if (!lexer_pop_context()) yyterminate(); }

   /* explicit string constant */
"\""			{ 
				#ifdef DEBUG
					logger(_L | LOG_USER, _("a string: "));
				#endif
					BEGIN(string); 
					TEST_MEMERR(OS_NewHandle(0, &lexer_string)); 
				}

<string>"\\\""	TEST_MEMERR(OS_AppendHandle(&lexer_string, "\"", 1));

<string>"\\t"	TEST_MEMERR(OS_AppendHandle(&lexer_string, "\t", 1));
<string>"\\b"	TEST_MEMERR(OS_AppendHandle(&lexer_string, "\b", 1));
<string>"\\r"	TEST_MEMERR(OS_AppendHandle(&lexer_string, "\r", 1));
<string>"\\n"	TEST_MEMERR(OS_AppendHandle(&lexer_string, "\n", 1));
<string>"\\\\"	TEST_MEMERR(OS_AppendHandle(&lexer_string, "\\", 1));

<string>"\""	{ 
					TEST_MEMERR(OS_AppendHandle(&lexer_string, "", 1)); 
					yylval.expr = command_expr_new_string(
										OS_LockHandle(&lexer_string));
					OS_UnlockHandle(&lexer_string);
					OS_FreeHandle(&lexer_string);
				#ifdef DEBUG
					logger(_L | LOG_USER, "%s\n", yylval.expr->u.str);
				#endif
					BEGIN(INITIAL);
					return STRING;
				}

<string>{EOL}   { TEST_MEMERR(OS_AppendHandle(&lexer_string, "\n", 1)); }

<string><<EOF>>  {
					TEST_MEMERR(OS_AppendHandle(&lexer_string, "", 1)); 
					yylval.expr = command_expr_new_string(
									OS_LockHandle(&lexer_string));
					OS_UnlockHandle(&lexer_string);
					OS_FreeHandle(&lexer_string);
					lexer_error(_("Unterminated string (%s)\n"), yylval.expr->u.str);
					BEGIN(INITIAL);
					return STRING;
				}
<string>.		TEST_MEMERR(OS_AppendHandle(&lexer_string, yytext, strlen(yytext)));

	/* implicit string constant */

{STRING}		{
					if (strcmp(yytext, "/") == 0)
						return '/';
					else

					{
/*
						command_symbol *sym;
						if (command_match_symbol(command_scope, yytext, &sym))
						{
							yylval.expr = command_expr_new_symbol(sym);
							return ID;
						}
						else 
*/
						{
							yylval.expr = command_expr_new_string(yytext);
							return IDSTRING;
						}
					}
				}

.				{
					return *yytext;
				}

%%

/*	Routine to read text from our command buffer instead of stdin */	
static int command_context_getstring(command_context *context, 
	char *buf, int maxsize)
{
	void *ptr;
	OSSize cpy;
//	my_assert(context != NULL);

	if (context == NULL)
		return 0;

	ptr = OS_LockHandle(&context->hand);

	cpy = ((context->len - context->offs) < maxsize) ? 
		(context->len - context->offs) : 
		maxsize;
	memcpy(buf, ((char *)ptr) + context->offs, cpy);
	context->offs += cpy;

	OS_UnlockHandle(&context->hand);
	
	return cpy;
}

/******************************/

static int 
command_context_new_text(command_context **context,
		const command_context *outer,
		const char *name, const char *data, int len)
{
	my_assert(data != NULL);

	*context = (command_context *)xmalloc(sizeof(command_context));
	
	(*context)->outer = outer;

	if (name) (*context)->name = xstrdup(name); 
	else (*context)->name = xstrdup(_("(unnamed)"));
	
	if (!(*context) || !(*context)->name || 
		OS_NewHandle(len, &(*context)->hand) != OS_NOERR)	
		return 0;
	
	(*context)->len = len;
	memcpy(OS_LockHandle(&(*context)->hand), data, len);
	OS_UnlockHandle(&(*context)->hand);
	
	(*context)->offs = 0;
	(*context)->line = 1;

	
	return 1;
}		

static int 
command_context_new_file(command_context **context,
		const command_context *outer,
		const char *filename)
{
	OSFileHandle fhand;
	OSError err;
	OSSpec spec;

	my_assert(filename != NULL);

	*context = (command_context *)xmalloc(sizeof(command_context));
	if (!*context)		return 0;
	
	(*context)->outer = outer;

	(*context)->name = (char *)xstrdup(filename); 
	if (!(*context)->name)	return 0;

	if ((err = OS_MakeFileSpec(filename, &spec)) != OS_NOERR ||
		(err = OS_NewFileHandle(&spec, NULL, false, &fhand)) != OS_NOERR ||
		(err = OS_CopyHandle(&fhand.hand, &(*context)->hand)) != OS_NOERR ||
		(err = OS_FreeFileHandle(&fhand)) != OS_NOERR)
	{
		logger(LOG_COMMANDS|LOG_ERROR|LOG_USER, _("Couldn't get/copy file text from '%s':\n%s\n"), filename, OS_GetErrText(err));
		return 0;
	}

	OS_GetHandleSize(&(*context)->hand, &(*context)->len);
	
	(*context)->offs = 0;
	(*context)->line = 1;

	return 1;
}

static void 
command_context_free(command_context **context)
{
	if (context == NULL || *context == NULL)
		return;
		
	if ((*context)->name)	xfree((*context)->name);
	OS_FreeHandle(&(*context)->hand);
	xfree(*context);
	
	*context = NULL;
}


command_context *lexer_push_text(const char *name, const char *data, int len)
{
	command_context *cc;
	logger(_L | L_3, _("lexer: reading from text '%s'\n"), name);
	if (!command_context_new_text(&cc, cf, name, data, len))
		return NULL;

	if (cf)
	{
		cf->yybuf = YY_CURRENT_BUFFER;
		yy_switch_to_buffer(yy_create_buffer(NULL, cc->len));
	}
	
	cf = cc;
	return cf;
}

command_context *lexer_push_file(const char *filename)
{
	command_context *cc;
	logger(_L | L_2, _("lexer: reading from file '%s'\n"), filename);
	if (!command_context_new_file(&cc, cf, filename))
		return NULL;

	if (cf)
	{
		cf->yybuf = YY_CURRENT_BUFFER;
		yy_switch_to_buffer(yy_create_buffer(NULL, cc->len));
	}

	cf = cc;
	return cf;
}

command_context *lexer_pop_context(void)
{
	if (cf != NULL)
	{
		command_context *outer = (command_context *)cf->outer;
		command_context_free(&cf);

		if (outer)
		{
			yy_delete_buffer(YY_CURRENT_BUFFER);
			yy_switch_to_buffer(outer->yybuf);
		}
		
		cf = outer;
		logger(_L | L_3, _("lexer: popping context to '%s'\n"), cf ? cf->name : "<null>");
		return cf;
	}

#ifdef DEBUG
	logger(LOG_FATAL,_("popped null lexer context\n"));
#endif
	return NULL;
}

void	lexer_error(const char *comm, ...)
{
	va_list va;
	static char buf[256], *bptr;
	va_start(va, comm);
	bptr = mvprintf(buf, sizeof(buf), comm, va);
	logger(_L|LOG_USER|LOG_ERROR, "%s:%d: %s\n", cf ? cf->name : _("<unnamed>"), 
			cf ? cf->line : 1, bptr);
	if (bptr != buf) free(bptr);
}

void	lexer_include(char *yy)
{
	char extract[256];
	
	char *st = strchr(yy, '\"')+1;
	char *en = strchr(st, '\"');
	if (en-st > 256) en = st+256;
	memcpy(extract, st, en-st);
	extract[en-st] = 0;
	
	lexer_push_file(extract);
}

int yywrap(void)
{
	return 1;
}
