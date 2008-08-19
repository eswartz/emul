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

/*
 * Symbol information can change at any time as result of target background activities.
 * Clients should not cache symbol information, and should not retain the information
 * longer then one dispatch cycle.
 */

typedef uns64 ModuleID;
typedef uns64 SymbolID;

typedef struct Symbol Symbol;

struct Symbol {
    Context * ctx;
    ModuleID module_id;
    SymbolID object_id;
    SymbolID type_id;

    int sym_class;
    ContextAddress address;
    char * section;
    char * storage;
    unsigned long size;
    int base;
};

#define SYM_CLASS_VALUE         1   /* Symbol represents a constant value */
#define SYM_CLASS_REFERENCE     2   /* Symbol is an address of an object (variable) in memory */
#define SYM_CLASS_FUNCTION      3   /* Symbol is an address of a function */
#define SYM_CLASS_TYPE          4   /* Symbol represents a type declaration */

#define SYM_BASE_ABS            1   /* Symbol address is an absolute address */
#define SYM_BASE_FP             2   /* Symbol address is offset relative to frame pointer */

#define TYPE_CLASS_UNKNOWN      0
#define TYPE_CLASS_CARDINAL     1
#define TYPE_CLASS_INTEGER      2
#define TYPE_CLASS_REAL         3
#define TYPE_CLASS_POINTER      4
#define TYPE_CLASS_ARRAY        5
#define TYPE_CLASS_COMPOSITE    6
#define TYPE_CLASS_ENUMERATION  7
#define TYPE_CLASS_FUNCTION     8


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


/*************** Functions for retrieving type information ***************************************/
/*
 * Each function retireves one particular attribute of an object or type
 * On error returns -1 and sets errno.
 * On success returns 0.
 */

/* Get type class, see TYPE_CLASS_* */
extern int get_symbol_class(Context * ctx, ModuleID module_id, SymbolID symbol_id, int * type_class);

/* Get type name, clients should call loc_free to dispose result */
extern int get_symbol_name(Context * ctx, ModuleID module_id, SymbolID symbol_id, char ** name);

/* Get value size of the type, in bytes */
extern int get_symbol_size(Context * ctx, ModuleID module_id, SymbolID symbol_id, uns64 * size);

/* Get base type ID */
extern int get_symbol_base_type(Context * ctx, ModuleID module_id, SymbolID symbol_id, SymbolID * base_type);

/* Get array index type ID */
extern int get_symbol_index_type(Context * ctx, ModuleID module_id, SymbolID symbol_id, SymbolID * index_type);

/* Get array length (number of elements) */
extern int get_symbol_length(Context * ctx, ModuleID module_id, SymbolID symbol_id, unsigned long * length);

/* Get children type IDs (for struct, union, class, function and enum) */
extern int get_symbol_children(Context * ctx, ModuleID module_id, SymbolID symbol_id, SymbolID ** children);

/* Get offset in parent type (for fields) */
extern int get_symbol_offset(Context * ctx, ModuleID module_id, SymbolID symbol_id, unsigned long * offset);

/* Get value (for constant objects and enums) */
extern int get_symbol_value(Context * ctx, ModuleID module_id, SymbolID symbol_id, size_t * size, void * value);

/*************************************************************************************************/


/*
 * Check if given address is inside a PLT section, then return address of the section.
 * If not a PLT address return 0;
 */
extern ContextAddress is_plt_section(Context * ctx, ContextAddress addr);

/*
 * Initialize symbol service.
 */
extern void ini_symbols_service(void);

#endif
