
/* A Bison parser, made by GNU Bison 2.4.1.  */

/* Skeleton interface for Bison's Yacc-like parsers in C
   
      Copyright (C) 1984, 1989, 1990, 2000, 2001, 2002, 2003, 2004, 2005, 2006
   Free Software Foundation, Inc.
   
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.
   
   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */


/* Tokens.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
   /* Put the tokens into the symbol table, so that GDB and other debuggers
      know about them.  */
   enum yytokentype {
     NUM = 258,
     STRING = 259,
     IDSTRING = 260,
     ERR = 261,
     ID = 262,
     LSHIFT = 263,
     RSHIFT = 264,
     COMPLE = 265,
     COMPGE = 266,
     COMPEQ = 267,
     COMPNE = 268,
     COMPAND = 269,
     COMPOR = 270,
     COMPXOR = 271,
     PRINT = 272,
     LOGGER = 273,
     ERROR = 274,
     WARNING = 275,
     INFO = 276,
     ASSERT = 277,
     IF = 278,
     ELSE = 279,
     FI = 280,
     ELSIF = 281,
     NEG = 282
   };
#endif
/* Tokens.  */
#define NUM 258
#define STRING 259
#define IDSTRING 260
#define ERR 261
#define ID 262
#define LSHIFT 263
#define RSHIFT 264
#define COMPLE 265
#define COMPGE 266
#define COMPEQ 267
#define COMPNE 268
#define COMPAND 269
#define COMPOR 270
#define COMPXOR 271
#define PRINT 272
#define LOGGER 273
#define ERROR 274
#define WARNING 275
#define INFO 276
#define ASSERT 277
#define IF 278
#define ELSE 279
#define FI 280
#define ELSIF 281
#define NEG 282




#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
typedef union YYSTYPE
{

/* Line 1676 of yacc.c  */
#line 68 "command_parser.y"

	command_expr		*expr;
	command_symbol		*sym;
	command_statement	*stmt;



/* Line 1676 of yacc.c  */
#line 114 "command_parser.h"
} YYSTYPE;
# define YYSTYPE_IS_TRIVIAL 1
# define yystype YYSTYPE /* obsolescent; will be withdrawn */
# define YYSTYPE_IS_DECLARED 1
#endif

extern YYSTYPE yylval;


