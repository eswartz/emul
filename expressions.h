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
 * Expression evaluation service.
 */

#ifndef D_expression
#define D_expression

#include "protocol.h"

#define VALUE_INT 1
#define VALUE_UNS 2
#define VALUE_STR 3

struct Value {
    int type;
    int value;
    char * str;
};

typedef struct Value Value;

struct ExpressionContext {
    int (*identifier)(char *, Value *);
    int (*type_cast)(char *, Value *);
};

typedef struct ExpressionContext ExpressionContext;

extern int evaluate_expression(ExpressionContext * ctx, char * s, Value * v);

extern char * get_expression_error_msg(void);

extern void string_value(Value * v, char * str);

extern void ini_expressions_service(Protocol * proto);

#endif

