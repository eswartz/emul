/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
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

extern int find_symbol(Context * ctx, char * name, Symbol * sym);

extern void ini_symbols_service(void);

#endif
