/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
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

typedef uint32_t SYM_FLAGS;

#define SYM_FLAG_PARAMETER      0x00001
#define SYM_FLAG_TYPEDEF        0x00002
#define SYM_FLAG_CONST_TYPE     0x00004
#define SYM_FLAG_PACKET_TYPE    0x00008
#define SYM_FLAG_SUBRANGE_TYPE  0x00010
#define SYM_FLAG_VOLATILE_TYPE  0x00020
#define SYM_FLAG_RESTRICT_TYPE  0x00040
#define SYM_FLAG_UNION_TYPE     0x00080
#define SYM_FLAG_CLASS_TYPE     0x00100
#define SYM_FLAG_INTERFACE_TYPE 0x00200
#define SYM_FLAG_SHARED_TYPE    0x00400
#define SYM_FLAG_REFERENCE      0x00800
#define SYM_FLAG_BIG_ENDIAN     0x01000
#define SYM_FLAG_LITTLE_ENDIAN  0x02000
#define SYM_FLAG_OPTIONAL       0x04000
#define SYM_FLAG_EXTERNAL       0x08000
#define SYM_FLAG_VARARG         0x10000

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
extern int find_symbol_by_name(Context * ctx, int frame, ContextAddress ip, char * name, Symbol ** sym);

/*
 * Find symbol information for given symbol name in given context and visibility scope.
 * On error, returns -1 and sets errno.
 * On success returns 0.
 */
extern int find_symbol_in_scope(Context * ctx, int frame, ContextAddress ip, Symbol * scope, char * name, Symbol ** sym);

/*
 * Find symbol information for given address in given context.
 * On error, returns -1 and sets errno.
 * On success returns 0.
 */
extern int find_symbol_by_addr(Context * ctx, int frame, ContextAddress addr, Symbol ** sym);

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

/* Get symbol type.
 * If the symbol is a modified type, like "volatile int", return original (unmodified) type */
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

/* Get children IDs of a type (struct, union, class, function and enum).
 * The array returned shall not be modified by the client,
 * and it may be overwritten by a subsequent calls to symbol functions */
extern int get_symbol_children(const Symbol * sym, Symbol *** children, int * count);

/* Get offset in parent type (fields) */
extern int get_symbol_offset(const Symbol * sym, ContextAddress * offset);

/* Get value (constant objects and enums).
 * The array returned shall not be modified by the client,
 * and it may be overwritten by a subsequent calls to symbol functions */
extern int get_symbol_value(const Symbol * sym, void ** value, size_t * size, int * big_endian);

/* Get address (variables) */
extern int get_symbol_address(const Symbol * sym, ContextAddress * address);

/* Get register if the symbol is a register variable */
extern int get_symbol_register(const Symbol * sym, Context ** ctx, int * frame, RegisterDefinition ** reg);

/* Get symbol flags, see SYM_FLAG_* */
extern int get_symbol_flags(const Symbol * sym, SYM_FLAGS * flags);

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
 * Return 0 on success.
 */
extern int get_next_stack_frame(StackFrame * frame, StackFrame * down);

/*
 * For given context and instruction address,
 * search for stack tracing information.
 * Return -1 and set errno in case of an error.
 * Return 0 on success.
 * Set 'info' to NULL if no stack tracing information found for the address.
 */
extern int get_stack_tracing_info(Context * ctx, ContextAddress addr, StackTracingInfo ** info);

/*
 * Initialize symbol service.
 */
extern void ini_symbols_service(Protocol * proto);
extern void ini_symbols_lib(void);

#endif /* ENABLE_Symbols */

#endif /* D_symbols */
