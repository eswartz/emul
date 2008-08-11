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
 * Expression evaluation service.
 */

#ifndef D_expression
#define D_expression

#include "protocol.h"
#include "context.h"

#define VALUE_INT 1
#define VALUE_UNS 2
#define VALUE_STR 3
#define VALUE_VAR 4

struct Value {
    int type;
    int value;
    char * str;
    ContextAddress addr;
    unsigned long size;
};

typedef struct Value Value;

extern int evaluate_expression(Context * ctx, int frame, char * s, Value * v);

extern int value_to_boolean(Value * v);

extern char * get_expression_error_msg(void);

extern void string_value(Value * v, char * str);

#if SERVICE_Expressions

extern void ini_expressions_service(Protocol * proto);

#endif

#endif

