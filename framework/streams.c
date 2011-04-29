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
 * Abstract byte stream. Bytes in the stream can be divided into groups - messages.
 */

#include <config.h>
#include <stddef.h>
#include <string.h>
#include <framework/myalloc.h>
#include <framework/streams.h>

int (read_stream)(InputStream * inp) {
    return (inp->cur < inp->end) ? *inp->cur++ : inp->read(inp);
}

int (peek_stream)(InputStream * inp) {
    return (inp->cur < inp->end) ? *inp->cur : inp->peek(inp);
}

void (write_stream)(OutputStream * out, int b) {
    if (b > ESC && out->cur < out->end) *out->cur++ = (unsigned char)b;
    else out->write(out, b);
}

void (write_block_stream)(OutputStream * out, const char * bytes, size_t size) {
    out->write_block(out, bytes, size);
}

ssize_t (splice_block_stream)(OutputStream * out, int fd, size_t size, off_t * offset) {
    return out->splice_block(out, fd, size, offset);
}

void write_string(OutputStream * out, const char * str) {
    while (*str) write_stream(out, (*str++) & 0xff);
}

void write_stringz(OutputStream * out, const char * str) {
    while (*str) write_stream(out, (*str++) & 0xff);
    write_stream(out, 0);
}

static void write_byte_array_output_stream(OutputStream * out, int byte) {
    ByteArrayOutputStream * buf = (ByteArrayOutputStream *)((char *)out - offsetof(ByteArrayOutputStream, out));
    if (buf->pos < sizeof(buf->buf)) {
        buf->buf[buf->pos++] = (char)byte;
    }
    else {
        if (buf->mem == NULL) {
            buf->mem = (char *)loc_alloc(buf->max = buf->pos * 2);
            memcpy(buf->mem, buf->buf, buf->pos);
        }
        else if (buf->pos >= buf->max) {
            buf->mem = (char *)loc_realloc(buf->mem, buf->max *= 2);
        }
        buf->mem[buf->pos++] = (char)byte;
    }
}

static void write_block_byte_array_output_stream(OutputStream * out, const char * bytes, size_t size) {
    size_t pos = 0;
    while (pos < size) write_byte_array_output_stream(out, ((const uint8_t *)bytes)[pos++]);
}

OutputStream * create_byte_array_output_stream(ByteArrayOutputStream * buf) {
    memset(buf, 0, sizeof(ByteArrayOutputStream));
    buf->out.write_block = write_block_byte_array_output_stream;
    buf->out.write = write_byte_array_output_stream;
    return &buf->out;
}

void get_byte_array_output_stream_data(ByteArrayOutputStream * buf, char ** data, size_t * size) {
    if (buf->mem == NULL) {
        buf->max = buf->pos;
        buf->mem = (char *)loc_alloc(buf->max);
        memcpy(buf->mem, buf->buf, buf->pos);
    }
    if (data != NULL) *data = buf->mem;
    if (size != NULL) *size = buf->pos;
    buf->mem = NULL;
    buf->max = 0;
    buf->pos = 0;
}

static int read_byte_array_input_stream(InputStream * inp) {
    ByteArrayInputStream * buf = (ByteArrayInputStream *)((char *)inp - offsetof(ByteArrayInputStream, inp));
    if (buf->pos >= buf->max) return -1;
    return ((unsigned char *)buf->buf)[buf->pos++];
}

static int peek_byte_array_input_stream(InputStream * inp) {
    ByteArrayInputStream * buf = (ByteArrayInputStream *)((char *)inp - offsetof(ByteArrayInputStream, inp));
    if (buf->pos >= buf->max) return -1;
    return ((unsigned char *)buf->buf)[buf->pos];
}

InputStream * create_byte_array_input_stream(ByteArrayInputStream * buf, char * data, size_t size) {
    memset(buf, 0, sizeof(ByteArrayInputStream));
    buf->inp.read = read_byte_array_input_stream;
    buf->inp.peek = peek_byte_array_input_stream;
    buf->buf = data;
    buf->max = size;
    return &buf->inp;
}

static int read_forwarding_input_stream(InputStream * inp) {
    ForwardingInputStream * buf = (ForwardingInputStream *)((char *)inp - offsetof(ForwardingInputStream, fwd));
    int ch = read_stream(buf->inp);
    if (ch != MARKER_EOS) write_stream(buf->out, ch);
    return ch;
}

static int peek_forwarding_input_stream(InputStream * inp) {
    ForwardingInputStream * buf = (ForwardingInputStream *)((char *)inp - offsetof(ForwardingInputStream, fwd));
    return peek_stream(buf->inp);
}

InputStream * create_forwarding_input_stream(ForwardingInputStream * buf, InputStream * inp, OutputStream * out) {
    memset(buf, 0, sizeof(ForwardingInputStream));
    buf->fwd.read = read_forwarding_input_stream;
    buf->fwd.peek = peek_forwarding_input_stream;
    buf->inp = inp;
    buf->out = out;
    return &buf->fwd;
}
