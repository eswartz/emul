/*******************************************************************************
 * Copyright (c) 2007-2009 Wind River Systems, Inc. and others.
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
#include "protocol.h"

/*
 * Symbol information can change at any time as result of target background activities.
 * Clients should not cache symbol information, and should not retain the information
 * longer then one dispatch cycle.
 */

typedef struct Symbol {
    Context * ctx;
    int sym_class;
    char location[64];
} Symbol;

#define SYM_CLASS_UNKNOWN       0
#define SYM_CLASS_VALUE         1   /* Symbol represents a constant value */
#define SYM_CLASS_REFERENCE     2   /* Symbol is an address of an object (variable) in memory */
#define SYM_CLASS_FUNCTION      3   /* Symbol is an address of a function */
#define SYM_CLASS_TYPE          4   /* Symbol represents a type declaration */

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

/*
 * Get (relatively) permanent symbol ID that can be used across dispatch cycles.
 */
extern char * symbol2id(const Symbol * sym);

/*
 * Find symbol by symbol ID.
 * On error, returns -1 and sets errno.
 * On success returns 0.
 */
extern int id2symbol(char * id, Symbol * sym);


/*************** Functions for retrieving symbol properties ***************************************/
/*
 * Each function retireves one particular attribute of an object or type
 * On error returns -1 and sets errno.
 * On success returns 0.
 */

/* Get symbol type */
extern int get_symbol_type(const Symbol * sym, Symbol * type);

/* Get type class, see TYPE_CLASS_* */
extern int get_symbol_type_class(const Symbol * sym, int * type_class);

/* Get type name, clients should call loc_free to dispose result */
extern int get_symbol_name(const Symbol * sym, char ** name);

/* Get value size of the type, in bytes */
extern int get_symbol_size(const Symbol * sym, int frame, size_t * size);

/* Get base type ID */
extern int get_symbol_base_type(const Symbol * sym, Symbol * base_type);

/* Get array index type ID */
extern int get_symbol_index_type(const Symbol * sym, Symbol * index_type);

/* Get array length (number of elements) */
extern int get_symbol_length(const Symbol * sym, int frame, unsigned long * length);

/* Get children type IDs (struct, union, class, function and enum), clients should call loc_free to dispose resul */
extern int get_symbol_children(const Symbol * sym, Symbol ** children, int * count);

/* Get offset in parent type (fields) */
extern int get_symbol_offset(const Symbol * sym, unsigned long * offset);

/* Get value (constant objects and enums), clients should call loc_free to dispose result */
extern int get_symbol_value(const Symbol * sym, void ** value, size_t * size);

/* Get address (variables) */
extern int get_symbol_address(const Symbol * sym, int frame, ContextAddress * address);

/* Get a type that represents a pointer to given base type */
extern int get_symbol_pointer(const Symbol * sym, Symbol * ptr);

/*************************************************************************************************/


/*
 * Check if given address is inside a PLT section, then return address of the section.
 * If not a PLT address return 0;
 */
extern ContextAddress is_plt_section(Context * ctx, ContextAddress addr);

/*
 * Initialize symbol service.
 */
extern void ini_symbols_service(Protocol * proto);

#endif /* D_symbols */
