/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Symbols service.
 */

#ifndef D_symbols
#define D_symbols

#include "context.h"

typedef struct Symbol Symbol;

struct Symbol {
    ContextAddress value;
    char * section;
    char * storage;
    unsigned long size;
    int base;
    int type;
};

#define SYM_BASE_ABS            1
#define SYM_BASE_FP             2

#define SYM_TYPE_UNKNOWN        0
#define SYM_TYPE_CARDINAL       1
#define SYM_TYPE_INTEGER        2
#define SYM_TYPE_REAL           3
#define SYM_TYPE_POINTER        4
#define SYM_TYPE_ARRAY          5
#define SYM_TYPE_COMPOSITE      6
#define SYM_TYPE_ENUMERATION    7
#define SYM_TYPE_FUNCTION       8


typedef void EnumerateSymbolsCallBack(void *, char * name, Symbol *);

/*
 * Find symbol information for given symbol name in given context.
 * On error, returns -1 and sets errno.
 * On success returns 0.
 */
extern int find_symbol(Context * ctx, int frame, char * name, Symbol * sym);

/*
 * Enumerate symbols in given context.
 * If frame >= 0 enumerates local symbols and function arguments.
 * If frame < 0 enumerates global symbols.
 * On error returns -1 and sets errno.
 * On success returns 0.
 */
extern int enumerate_symbols(Context * ctx, int frame, EnumerateSymbolsCallBack *, void * args);

/*
 * Check if given address is inside a PLT section, then return address of the section.
 * If not PLT address return 0;
 */
extern ContextAddress is_plt_section(Context * ctx, ContextAddress addr);

/*
 * Initialize symbol service.
 */
extern void ini_symbols_service(void);

#endif
