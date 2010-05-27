/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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

#include <framework/context.h>
#include <framework/protocol.h>

/*
 * Symbol information can change at any time as result of target background activities.
 * Clients should not cache symbol information, and should not retain the information
 * longer then one dispatch cycle.
 */

typedef struct Symbol Symbol;

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

/* Symbol properties update policies */
#define UPDATE_ON_MEMORY_MAP_CHANGES 0
#define UPDATE_ON_EXE_STATE_CHANGES  1

typedef void EnumerateSymbolsCallBack(void *, Symbol *);

#if ENABLE_Symbols

/*
 * Find symbol information for given symbol name in given context.
 * On error, returns -1 and sets errno.
 * On success returns 0.
 */
extern int find_symbol(Context * ctx, int frame, char * name, Symbol ** sym);

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
extern const char * symbol2id(const Symbol * sym);

/*
 * Find symbol by symbol ID.
 * On error, returns -1 and sets errno.
 * On success returns 0.
 */
extern int id2symbol(const char * id, Symbol ** sym);


/*************** Functions for retrieving symbol properties ***************************************/
/*
 * Each function retireves one particular attribute of an object or type.
 * On error returns -1 and sets errno.
 * On success returns 0.
 */

/* Get symbol class */
extern int get_symbol_class(const Symbol * sym, int * symbol_class);

/* Get symbol type */
extern int get_symbol_type(const Symbol * sym, Symbol ** type);

/* Get type class, see TYPE_CLASS_* */
extern int get_symbol_type_class(const Symbol * sym, int * type_class);

/* Get symbol owner ID and update policy ID.
 * Symbol owner can be memory space or executable context.
 * Certain changes in owner state can invalidate cached symbol properties.
 * Update policy ID selects a specific set of rules that a client should follow
 * if it wants to cache symbol properties.
 * The string returned shall not be modified by the client,
 * and it may be overwritten by a subsequent calls to symbol functions */
extern int get_symbol_update_policy(const Symbol * sym, char ** parent_id, int * policy);

/* Get symbol name.
 * The string returned shall not be modified by the client,
 * and it may be overwritten by a subsequent calls to symbol functions */
extern int get_symbol_name(const Symbol * sym, char ** name);

/* Get value size of the type, in bytes */
extern int get_symbol_size(const Symbol * sym, ContextAddress * size);

/* Get base type ID */
extern int get_symbol_base_type(const Symbol * sym, Symbol ** base_type);

/* Get array index type ID */
extern int get_symbol_index_type(const Symbol * sym, Symbol ** index_type);

/* Get array length (number of elements) */
extern int get_symbol_length(const Symbol * sym, ContextAddress * length);

/* Get array index lower bound (index of first element) */
extern int get_symbol_lower_bound(const Symbol * sym, int64_t * value);

/* Get children type IDs (struct, union, class, function and enum).
 * The array returned shall not be modified by the client,
 * and it may be overwritten by a subsequent calls to symbol functions */
extern int get_symbol_children(const Symbol * sym, Symbol *** children, int * count);

/* Get offset in parent type (fields) */
extern int get_symbol_offset(const Symbol * sym, ContextAddress * offset);

/* Get value (constant objects and enums).
 * The array returned shall not be modified by the client,
 * and it may be overwritten by a subsequent calls to symbol functions */
extern int get_symbol_value(const Symbol * sym, void ** value, size_t * size);

/* Get address (variables) */
extern int get_symbol_address(const Symbol * sym, ContextAddress * address);

/* Get a type that represents an array of elements of given base type.
 * If 'length' is zero, returned type represents pointer to given type */
extern int get_array_symbol(const Symbol * sym, ContextAddress length, Symbol ** ptr);

/*************************************************************************************************/

/*
 * Check if given address is inside a PLT section, then return address of the section.
 * If not a PLT address return 0;
 */
extern ContextAddress is_plt_section(Context * ctx, ContextAddress addr);

/*
 * For given context and its registers in a stack frame,
 * compute stack frame location and next frame register values.
 * If frame info is not available, do nothing.
 * Return -1 and set errno in case of an error.
 */
extern int get_next_stack_frame(StackFrame * frame, StackFrame * down);

/*
 * For given context and instruction address,
 * search for stack tracing information.
 * Return -1 and set errno in case of an error.
 */
extern int get_stack_tracing_info(Context * ctx, ContextAddress addr, StackTracingInfo ** info);

/*
 * Initialize symbol service.
 */
extern void ini_symbols_service(Protocol * proto);
extern void ini_symbols_lib(void);

#endif /* ENABLE_Symbols */

#endif /* D_symbols */
