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
 *     Michael Sills-Lavoie(École Polytechnique de Montréal)  - ZeroCopy support
 *              *                         *                 - json_splice_binary
 *******************************************************************************/

/*
 * This module provides support for JSON - a computer data interchange format.
 * It is a text-based, human-readable format for representing simple data structures and
 * associative arrays (called objects). The JSON format is specified in RFC 4627 by Douglas Crockford.
 * JSON is TCF preffered marshaling format.
 */

#ifndef D_json
#define D_json

#include <config.h>
#include <stdlib.h>
#include <framework/streams.h>

extern int json_read_string(InputStream * inp, char * str, size_t size);
extern int json_read_boolean(InputStream * inp);
extern long json_read_long(InputStream * inp);
extern unsigned long json_read_ulong(InputStream * inp);
extern int64_t json_read_int64(InputStream * inp);
extern uint64_t json_read_uint64(InputStream * inp);
extern double json_read_double(InputStream * inp);
extern char * json_read_alloc_string(InputStream * inp);
extern char ** json_read_alloc_string_array(InputStream * inp, int * len);

typedef void JsonArrayCallBack(InputStream *, void *);
/* Read JSON array. Call "call_back" for each array element. Return 0 if array if null, return 1 if not null */
extern int json_read_array(InputStream * inp, JsonArrayCallBack * call_back, void * arg);

typedef void JsonStructCallBack(InputStream *, const char *, void *);
/* Read JSON object (struct). Call "call_back" for each struct member. Return 0 if object if null, return 1 if not null */
extern int json_read_struct(InputStream * inp, JsonStructCallBack * call_back, void * arg);

/* Read JSON object and return is as JSON string. Clients should use loc_free() to dispose the string */
extern char * json_read_object(InputStream * inp);
/* Skip one JSON object in the input stream */
extern void json_skip_object(InputStream * inp);

extern void json_write_ulong(OutputStream * out, unsigned long n);
extern void json_write_long(OutputStream * out, long n);
extern void json_write_uint64(OutputStream * out, uint64_t n);
extern void json_write_int64(OutputStream * out, int64_t n);
extern void json_write_double(OutputStream * out, double n);
extern void json_write_char(OutputStream * out, char ch);
extern void json_write_string(OutputStream * out, const char * str);
extern void json_write_string_len(OutputStream * out, const char * str, size_t len);
extern void json_write_boolean(OutputStream * out, int b);

extern int read_errno(InputStream * inp);
extern void write_error_object(OutputStream * out, int err);
extern void write_errno(OutputStream * out, int err);
extern void write_service_error(OutputStream * out, int err, const char * service_name, int service_error);

/*
 * The following API to stream binary data is designed to allow
 * multiple encodings of the data.  The state structure is necessary
 * because the streaming does not give visibility to all data at once
 * and some encoding schemes require data to come in groups, for
 * example for base64 data encodes 3 bytes at the time.  The members
 * of the state structures are private to the implementation and
 * should not be used in any way by clients of the API.
 */

extern char * json_read_alloc_binary(InputStream * inp, int * size);
extern void json_write_binary(OutputStream * out, const void * data, size_t size);
extern void json_splice_binary(OutputStream * out, int fd, size_t size);
extern void json_splice_binary_offset(OutputStream * out, int fd, size_t size, off_t * offset);

typedef struct JsonReadBinaryState {
    /* Private members */
    InputStream * inp;
    int encoding;
    int size_start;
    int size_done;
    unsigned rem;
    char buf[3];
} JsonReadBinaryState;

extern void json_read_binary_start(JsonReadBinaryState * state, InputStream * inp);
extern size_t json_read_binary_data(JsonReadBinaryState * state, void * buf, size_t buf_size);
extern void json_read_binary_end(JsonReadBinaryState * state);

typedef struct JsonWriteBinaryState {
    /* Private members */
    OutputStream * out;
    int encoding;
    int size_start;
    int size_done;
    unsigned rem;
    char buf[3];
} JsonWriteBinaryState;

/* json_write_binary_start() argument 'size' can be 0 if client does not know the size upfront */
extern void json_write_binary_start(JsonWriteBinaryState * state, OutputStream * out, int size);
extern void json_write_binary_data(JsonWriteBinaryState * state, const void * data, size_t size);
extern void json_write_binary_end(JsonWriteBinaryState * state);

#endif /* D_json */
