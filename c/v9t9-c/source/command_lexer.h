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

#ifndef __COMMAND_LEXER_H__
#define __COMMAND_LEXER_H__

#include "OSLib.h"

#include "centry.h"

/* 	only reference pointer to this from outside command_lexer.l ! */

typedef struct command_context {
	const struct command_context 
			*outer;			/* encasing context */
	char 	*name;			/* name of this buffer */
	OSHandle hand;			/* text data */
	OSSize	len;			/* length of data */
	int		offs;			/* current offset in data */
	int		line;			/* current line number */
#ifdef __COMMAND_L__
	YY_BUFFER_STATE	yybuf;	/* yybuffer */
#endif
}	command_context;

extern command_context *cf;

static int 
command_context_new_text(command_context **context,
						 const command_context *outer,
						 const char *name, const char *data, int len);

static int 
command_context_new_file(command_context **context,
						 const command_context *outer,
						 const char *filename);

/*	Defined in command.l */

void lexer_error(const char *format, ...);
void lexer_include(char *yy);

command_context	*lexer_push_text(const char *name, const char *data, int len);
command_context *lexer_push_file(const char *filename);
command_context *lexer_pop_context(void);

int yylex(void);

#include "cexit.h"	

#endif
