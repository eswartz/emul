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
 * This module provides support for JSON - a computer data interchange format.
 * It is a text-based, human-readable format for representing simple data structures and
 * associative arrays (called objects). The JSON format is specified in RFC 4627 by Douglas Crockford. 
 * JSON is TCF preffered marshaling format.
 */

#ifndef D_json
#define D_json

#include <stdlib.h>
#include "mdep.h"
#include "streams.h"

extern int json_read_string(InputStream * inp, char * str, size_t size);
extern int json_read_boolean(InputStream * inp);
extern long json_read_long(InputStream * inp);
extern unsigned long json_read_ulong(InputStream * inp);
extern int64 json_read_int64(InputStream * inp);
extern char * json_read_alloc_string(InputStream * inp);
extern char ** json_read_alloc_string_array(InputStream * inp, int * len);

typedef void (*struct_call_back)(InputStream *, char *, void *);
extern int json_read_struct(InputStream * inp, struct_call_back call_back, void * arg);

extern void json_skip_object(InputStream * inp);

extern void json_write_ulong(OutputStream * out, unsigned long n);
extern void json_write_long(OutputStream * out, long n);
extern void json_write_int64(OutputStream * out, int64 n);
extern void json_write_char(OutputStream * out, char ch);
extern void json_write_string(OutputStream * out, const char * str);
extern void json_write_string_len(OutputStream * out, const char * str, size_t len);
extern void json_write_boolean(OutputStream * out, int b);

extern void write_errno(OutputStream * out, int err);
extern void write_err_msg(OutputStream * out, int err, char * msg);

#endif
