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

struct Value {
    int type_class;
    unsigned long size;
    void * value;
    ContextAddress address;
    int remote;
};

typedef struct Value Value;

extern int evaluate_expression(Context * ctx, int frame, char * s, int load, Value * v);

extern int value_to_boolean(Value * v);

extern ContextAddress value_to_address(Value * v);

extern char * get_expression_error_msg(void);

extern void string_value(Value * v, char * str);

#if SERVICE_Expressions

extern void ini_expressions_service(Protocol * proto);

#endif

#endif

