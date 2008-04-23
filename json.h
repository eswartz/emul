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
 * This module provides support for JSON - a computer data interchange format.
 * It is a text-based, human-readable format for representing simple data structures and
 * associative arrays (called objects). The JSON format is specified in RFC 4627 by Douglas Crockford. 
 * JSON is TCF preffered marshaling format.
 */

#ifndef D_json
#define D_json

#include "mdep.h"
#include <stdlib.h>
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

/*
 * The following API to stream binary data is designed to allow
 * multiple encodings of the data.  The state structure is necessary
 * because the streaming does not give visibility to all data at once
 * and some encoding schemes require data to come in groups, for
 * example for base64 data encodes 3 bytes at the time.  The members
 * fo the state structures are private to the implementation and
 * should not be used in any way by clients of the API.
 */

typedef struct JsonReadBinaryState {
    /* Private members */
    InputStream * inp;
    unsigned rem;
    char buf[3];
} JsonReadBinaryState;

extern void json_read_binary_start(JsonReadBinaryState * state, InputStream * inp);
extern size_t json_read_binary_data(JsonReadBinaryState * state, char * buf, size_t len);
extern void json_read_binary_end(JsonReadBinaryState * state);

typedef struct JsonWriteBinaryState {
    /* Private members */
    OutputStream * out;
    unsigned rem;
    char buf[3];
} JsonWriteBinaryState;

extern void json_write_binary_start(JsonWriteBinaryState * state, OutputStream * out);
extern void json_write_binary_data(JsonWriteBinaryState * state, const char * str, size_t len);
extern void json_write_binary_end(JsonWriteBinaryState * state);

#endif
