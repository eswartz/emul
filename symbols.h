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
    unsigned long value;
    char * section;
    char * storage;
    int abs;
};

#ifdef WIN32
/*
 * Initialize dbghelp.dll symbol handler for a given context.
 * Call this function right before calling any functions from dnghelp.dll.
 * On error, returns -1 and sets errno.
 */
extern int set_symbol_context(Context * ctx);
#endif

/*
 * Find symbol information for given symbol name in given context.
 * On error, returns -1 and sets errno.
 */
extern int find_symbol(Context * ctx, char * name, Symbol * sym);

/*
 * Check if given address is inside a PLT section and return address of the section.
 * Othewise return 0;
 */
extern unsigned long is_plt_section(Context * ctx, unsigned long addr);

/*
 * Initialize symbol service.
 */
extern void ini_symbols_service(void);

#endif
