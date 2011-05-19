/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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
 * Expression evaluation service.
 */

#ifndef D_expressions
#define D_expressions

#include <config.h>
#include <framework/protocol.h>
#include <framework/context.h>
#include <services/symbols.h>

#if SERVICE_Expressions

/* Value represents result of expression evaluation */
struct Value {
    Symbol * sym;               /* Value symbol, can be NULL */
    Symbol * type;              /* Value type symbol, can be NULL */
    int type_class;             /* See symbols.h for type class definitions */
    void * value;               /* Pointer to value data buffer, or NULL if remote value */
    RegisterDefinition * reg;   /* Not NULL if the value represents a register variable */
    ContextAddress address;     /* Address of value data in remote target memory */
    ContextAddress size;        /* Value size in bytes */
    int remote;                 /* 1 if value data is in remote target memory, 0 if loaded into a local buffer */
    int constant;               /* 1 if the value is not expected to change during execution of value context */
    int big_endian;             /* 1 if the value is big endian */
    int function;               /* 1 if the value represents a function */
};

typedef struct Value Value;

/*
 * ExpressionIdentifierCallBack is called for every identifier found in an expression during evaluation,
 * If callback knows value of the idenfifier it should fill Value struct and return 1,
 * otherwise it should return 0.
 */
typedef int ExpressionIdentifierCallBack(Context *, int /*frame*/, char * /*name*/, Value *);

/*
 * Evaluate given expression in given context.
 * 'ctx' - debug context to use for memory access and symbols lookup.
 * 'frame' - stack frame to use for registers and local variables values.
 * 'addr' - instruction address for symbols lookup, ignored if frame != STACK_NO_FRAME.
 * 's' - the expression text.
 * If load != 0 then result value is always loaded into a local buffer,
 * otherwise 'v' can point to a value in the target memory.
 * Return 0 if no errors, otherwise return -1 and sets errno.
 */
extern int evaluate_expression(Context * ctx, int frame, ContextAddress addr, char * s, int load, Value * v);

/*
 * Cast a Value to another type ("boolean" means 0 or 1).
 * Returns 0 if no errors, otherwise returns -1 and sets errno.
 */
int value_to_boolean(Value *v, int *res);
int value_to_address(Value *v, ContextAddress *res);
int value_to_signed(Value *v, int64_t *res);
int value_to_unsigned(Value *v, uint64_t *res);
int value_to_double(Value *v, double *res);

/*
 * Allocate and fill local data buffer for a value.
 * The buffer is freed automatically at the end of current event dispatch cycle.
 */
extern void set_value(Value * v, void * data, size_t size, int big_endian);

/*
 * Add identifier callback to the list of expression callbacks.
 * The callbacks are called for each identifier found in an expression during evaluation.
 */
extern void add_identifier_callback(ExpressionIdentifierCallBack * callback);

extern void ini_expressions_service(Protocol * proto);

#endif /* SERVICE_Expressions */

#endif /* D_expressions */
