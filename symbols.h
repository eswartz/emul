/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
};

#define SYM_BASE_ABS 1
#define SYM_BASE_FP  2

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
 * Check if given address is inside a PLT section and return address of the section.
 * Othewise return 0;
 */
extern ContextAddress is_plt_section(Context * ctx, ContextAddress addr);

/*
 * Initialize symbol service.
 */
extern void ini_symbols_service(void);

#endif
