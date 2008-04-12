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

#include "mdep.h"
#include "json.h"
#include "assert.h"
#include "myalloc.h"
#include "exceptions.h"
#include "base64.h"

static char * buf = NULL;
static unsigned buf_size = 0;

static void realloc_buf(int buf_pos) {
    if (buf == NULL) {
        buf_size = 0x1000;
        buf = (char *)loc_alloc(buf_size);
    }
    else {
        char * tmp = (char *)loc_alloc(buf_size * 2);
        memcpy(tmp, buf, buf_pos);
        loc_free(buf);
        buf = tmp;
        buf_size *= 2;
    }
}

void json_write_ulong(OutputStream * out, unsigned long n) {
    if (n >= 10) {
        json_write_ulong(out, n / 10);
        n = n % 10;
    }
    out->write(out, n + '0');
}

void json_write_long(OutputStream * out, long n) {
    if (n < 0) {
        out->write(out, '-');
        n = -n;
    }
    json_write_ulong(out, (unsigned long)n);
}

void json_write_int64(OutputStream * out, int64 n) {
    if (n < 0) {
        out->write(out, '-');
        n = -n;
        if (n < 0) exception(EINVAL);
    }
    if (n >= 10) {
        json_write_int64(out, n / 10);
        n = n % 10;
    }
    out->write(out, (int)n + '0');
}

void json_write_boolean(OutputStream * out, int b) {
    if (b) write_string(out, "true");
    else write_string(out, "false");
}

static char hex_digit(unsigned n) {
    n &= 0xf;
    if (n < 10) return '0' + n;
    return 'A' + (n - 10);
}

void json_write_char(OutputStream * out, char ch) {
    unsigned n = ch & 0xff;
    if (n < ' ') {
        out->write(out, '\\');
        out->write(out, 'u');
        out->write(out, '0');
        out->write(out, '0');
        out->write(out, hex_digit(n >> 4));
        out->write(out, hex_digit(n));
    }
    else {
        if (n == '"' || n == '\\') out->write(out, '\\');
        out->write(out, n);
    }
}

void json_write_string(OutputStream * out, const char * str) {
    if (str == NULL) {
        write_string(out, "null");
    }
    else {
        out->write(out, '"');
        while (*str) json_write_char(out, *str++);
        out->write(out, '"');
    }
}

void json_write_string_len(OutputStream * out, const char * str, size_t len) {
    if (str == NULL) {
        write_string(out, "null");
    }
    else {
        out->write(out, '"');
        while (len > 0) {
            json_write_char(out, *str++);
            len--;
        }
        out->write(out, '"');
    }
}

static int readHex(InputStream * inp) {
    int ch = inp->read(inp);
    if (ch >= '0' && ch <= '9') return ch - '0';
    if (ch >= 'A' && ch <= 'F') return ch - 'A' + 10;
    if (ch >= 'a' && ch <= 'f') return ch - 'a' + 10;
    exception(ERR_JSON_SYNTAX);
    return 0;
}

static int readHexChar(InputStream * inp) {
    int n = readHex(inp) << 12;
    n |= readHex(inp) << 8;
    n |= readHex(inp) << 4;
    n |= readHex(inp);
    return n;
}

static int read_esc_char(InputStream * inp) {
    int ch = inp->read(inp);
    switch (ch) {
    case '"': break;
    case '\\': break;
    case '/': break;
    case 'b': ch = '\b'; break;
    case 'f': ch = '\f'; break;
    case 'n': ch = '\n'; break;
    case 'r': ch = '\r'; break;
    case 't': ch = '\t'; break;
    case 'u': ch = readHexChar(inp); break;
    default: exception(ERR_JSON_SYNTAX);
    }
    return ch;
}

int json_read_string(InputStream * inp, char * str, size_t size) {
    unsigned i = 0;
    int ch = inp->read(inp);
    if (ch == 'n') {
        if (inp->read(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
        str[0] = 0;
        return -1;
    }
    if (ch != '"') exception(ERR_JSON_SYNTAX);
    for (;;) {
        ch = inp->read(inp);
        if (ch == '"') break;
        if (ch == '\\') ch = read_esc_char(inp);
        if (i < size - 1) str[i] = (char)ch;
        i++;
    }
    if (i < size) str[i] = 0;
    else str[size - 1] = 0;
    return i;
}

char * json_read_alloc_string(InputStream * inp) {
    char * str = NULL;
    unsigned buf_pos = 0;
    int ch = inp->read(inp);
    if (ch == 'n') {
        if (inp->read(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
        return NULL;
    }
    if (ch != '"') exception(ERR_JSON_SYNTAX);
    for (;;) {
        ch = inp->read(inp);
        if (ch == '"') break;
        if (ch == '\\') ch = read_esc_char(inp);
        if (buf_pos >= buf_size) realloc_buf(buf_pos);
        buf[buf_pos++] = (char)ch;
    }
    if (buf_pos >= buf_size) realloc_buf(buf_pos);
    buf[buf_pos++] = 0;
    str = (char *)loc_alloc(buf_pos);
    memcpy(str, buf, buf_pos);
    return str;
}

int json_read_boolean(InputStream * inp) {
    int ch = inp->read(inp);
    if (ch == 'f') {
        if (inp->read(inp) != 'a') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 's') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'e') exception(ERR_JSON_SYNTAX);
        return 0;
    }
    if (ch == 't') {
        if (inp->read(inp) != 'r') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'e') exception(ERR_JSON_SYNTAX);
        return 1;
    }
    exception(ERR_JSON_SYNTAX);
    return 0;
}

long json_read_long(InputStream * inp) {
    long res = 0;
    int neg = 0;
    int ch = inp->read(inp);
    if (ch == '-') {
        neg = 1;
        ch = inp->read(inp);
    }
    if (ch < '0' || ch > '9') exception(ERR_JSON_SYNTAX);
    res = ch - '0';
    while (1) {
        ch = inp->peek(inp);
        if (ch < '0' || ch > '9') break;
        inp->read(inp);
        res = res * 10 + (ch - '0');
    }
    if (neg) return -res;
    return res;
}

unsigned long json_read_ulong(InputStream * inp) {
    unsigned long res = 0;
    int neg = 0;
    int ch = inp->read(inp);
    if (ch == '-') {
        neg = 1;
        ch = inp->read(inp);
    }
    if (ch < '0' || ch > '9') exception(ERR_JSON_SYNTAX);
    res = ch - '0';
    while (1) {
        ch = inp->peek(inp);
        if (ch < '0' || ch > '9') break;
        inp->read(inp);
        res = res * 10 + (ch - '0');
    }
    if (neg) return ~res + 1;
    return res;
}

int64 json_read_int64(InputStream * inp) {
    int64 res = 0;
    int neg = 0;
    int ch = inp->read(inp);
    if (ch == '-') {
        neg = 1;
        ch = inp->read(inp);
    }
    if (ch < '0' || ch > '9') exception(ERR_JSON_SYNTAX);
    res = ch - '0';
    while (1) {
        ch = inp->peek(inp);
        if (ch < '0' || ch > '9') break;
        inp->read(inp);
        res = res * 10 + (ch - '0');
    }
    if (neg) return -res;
    return res;
}

int json_read_struct(InputStream * inp, struct_call_back call_back, void * arg) {
    int ch = inp->read(inp);
    if (ch == 'n') {
        if (inp->read(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
        return 0;
    }
    if (ch == '{') {
        ch = inp->read(inp);
        if (ch != '}') {
            for (;;) {
                int nm_len = 0;
                char nm[256];
                if (ch != '"') exception(ERR_JSON_SYNTAX);
                for (;;) {
                    ch = inp->read(inp);
                    if (ch == '"') break;
                    if (ch == '\\') {
                        ch = inp->read(inp);
                        switch (ch) {
                        case '"': break;
                        case '\\': break;
                        case '/': break;
                        case 'b': ch = '\b'; break;
                        case 'f': ch = '\f'; break;
                        case 'n': ch = '\n'; break;
                        case 'r': ch = '\r'; break;
                        case 't': ch = '\t'; break;
                        case 'u': ch = readHexChar(inp); break;
                        default: exception(ERR_JSON_SYNTAX);
                        }
                    }
                    if (nm_len < sizeof(nm) - 1) {
                        nm[nm_len] = (char)ch;
                        nm_len++;
                    }
                }
                nm[nm_len] = 0;
                ch = inp->read(inp);
                if (ch != ':') exception(ERR_JSON_SYNTAX);
                call_back(inp, nm, arg);
                ch = inp->read(inp);
                if (ch == '}') break;
                if (ch != ',') exception(ERR_JSON_SYNTAX);
                ch = inp->read(inp);
            }
        }
        return 1;
    }
    exception(ERR_JSON_SYNTAX);
    return 0;
}

char ** json_read_alloc_string_array(InputStream * inp, int * pos) {
    int ch = inp->read(inp);
    *pos = 0;
    if (ch == 'n') {
        if (inp->read(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
        return NULL;
    }
    else if (ch != '[') {
        exception(ERR_PROTOCOL);
        return NULL;
    }
    else {
        static unsigned * len_buf = NULL;
        static unsigned len_buf_size = 0;

        unsigned buf_pos = 0;
        unsigned len_pos = 0;

        unsigned i, j;
        char * str = NULL;
        char ** arr = NULL;

        if (inp->peek(inp) == ']') {
            inp->read(inp);
        }
        else {
            while (1) {
                int ch = inp->read(inp);
                int len = 0;
                if (len_pos >= len_buf_size) {
                    len_buf_size = len_buf_size == 0 ? 0x100 : len_buf_size * 2;
                    len_buf = (unsigned *)loc_realloc(len_buf, len_buf_size * sizeof(unsigned));
                }
                if (ch == 'n') {
                    if (inp->read(inp) != 'u') exception(ERR_JSON_SYNTAX);
                    if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
                    if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
                }
                else {
                    if (ch != '"') exception(ERR_JSON_SYNTAX);
                    for (;;) {
                        ch = inp->read(inp);
                        if (ch == '"') break;
                        if (ch == '\\') ch = read_esc_char(inp);
                        if (buf_pos >= buf_size) realloc_buf(buf_pos);
                        buf[buf_pos++] = (char)ch;
                        len++;
                    }
                }
                if (buf_pos >= buf_size) realloc_buf(buf_pos);
                buf[buf_pos++] = 0;
                len_buf[len_pos++] = len;
                ch = inp->read(inp);
                if (ch == ',') continue;
                if (ch == ']') break;
                exception(ERR_JSON_SYNTAX);
            }
        }
        if (buf_pos >= buf_size) realloc_buf(buf_pos);
        buf[buf_pos++] = 0;
        arr = (char **)loc_alloc((len_pos + 1) * sizeof(char *) + buf_pos);
        str = (char *)(arr + len_pos + 1);
        memcpy(str, buf, buf_pos);
        j = 0;
        for (i = 0; i < len_pos; i++) {
            arr[i] = str + j;
            j += len_buf[i] + 1;
        }
        arr[len_pos] = NULL;
        *pos = len_pos;
        return arr;
    }
}

void json_read_binary_start(JsonReadBinaryState * state, InputStream * inp) {
    state->inp = inp;
    if (inp->read(inp) != '"') exception(ERR_JSON_SYNTAX);
}

size_t json_read_binary_data(JsonReadBinaryState * state, char * buf, size_t len) {
    return read_base64(state->inp, buf, len);
}

void json_read_binary_end(JsonReadBinaryState * state) {
    if (state->inp->read(state->inp) != '"') exception(ERR_JSON_SYNTAX);
}

void json_write_binary_start(JsonWriteBinaryState * state, OutputStream * out) {
    state->out = out;
    state->rem = 0;
    state->out->write(state->out, '"');
}

void json_write_binary_data(JsonWriteBinaryState * state, const char * str, size_t len) {
    size_t rem = state->rem;

    if (rem > 0) {
        while (rem < 3 && len > 0) {
            state->buf[rem++] = *str++;
            len--;
        }
        assert(rem <= 3);
        if (rem >= 3) {
            write_base64(state->out, state->buf, rem);
            rem = 0;
        }
    }
    if (len > 0) {
        assert(rem == 0);
        rem = len % 3;
        len -= rem;
        write_base64(state->out, str, len);
        if (rem > 0) {
            memcpy(state->buf, str + len, rem);
        }
    }
    state->rem = rem;
}

void json_write_binary_end(JsonWriteBinaryState * state) {
    size_t rem;

    if ((rem = state->rem) > 0) {
        write_base64(state->out, state->buf, rem);
    }
    state->out->write(state->out, '"');
}

void json_skip_object(InputStream * inp) {
    int ch = inp->read(inp);
    if (ch == 'n') {
        if (inp->read(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (inp->read(inp) != 'l') exception(ERR_JSON_SYNTAX);
        return;
    }
    if (ch == '"') {
        for (;;) {
            ch = inp->read(inp);
            if (ch == '"') break;
            if (ch == '\\') {
                if (inp->read(inp) == 'u') readHexChar(inp);
            }
        }
        return;
    }
    if (ch == '-' || ch >= '0' && ch <= '9') {
        while (1) {
            ch = inp->peek(inp);
            if (ch < '0' || ch > '9') break;
            inp->read(inp);
        }
        return;
    }
    if (ch == '[') {
        if (inp->peek(inp) == ']') {
            inp->read(inp);
        }
        else {
            while (1) {
                int ch;
                json_skip_object(inp);
                ch = inp->read(inp);
                if (ch == ',') continue;
                if (ch == ']') break;
                exception(ERR_JSON_SYNTAX);
            }
        }
        return;
    }
    if (ch == '{') {
        if (inp->peek(inp) == '}') {
            inp->read(inp);
        }
        else {
            while (1) {
                int ch;
                json_skip_object(inp);
                if (inp->read(inp) != ':') exception(ERR_JSON_SYNTAX);
                json_skip_object(inp);
                ch = inp->read(inp);
                if (ch == ',') continue;
                if (ch == '}') break;
                exception(ERR_JSON_SYNTAX);
            }
        }
    }
    exception(ERR_JSON_SYNTAX);
}

void write_errno(OutputStream * out, int err) {
    char * msg = NULL;
    json_write_long(out, err);
    out->write(out, 0);
    if (err != 0) msg = errno_to_str(err);
    json_write_string(out, msg);
    out->write(out, 0);
}

void write_err_msg(OutputStream * out, int err, char * msg) {
    json_write_long(out, err);
    out->write(out, 0);
    if (err == 0) {
        write_string(out, "null");
    }
    else {
        char * str = errno_to_str(err);
        out->write(out, '"');
        while (*str) json_write_char(out, *str++);
        if (msg != NULL) {
            out->write(out, ':');
            out->write(out, ' ');
            while (*msg) json_write_char(out, *msg++);
        }
        out->write(out, '"');
    }
    out->write(out, 0);
}

