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

#include <config.h>
#include <stdio.h>
#include <assert.h>
#include <framework/json.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <framework/base64.h>

#define ENCODING_BINARY     0
#define ENCODING_BASE64     1

static char * buf = NULL;
static unsigned buf_pos = 0;
static unsigned buf_size = 0;

static void realloc_buf(void) {
    if (buf == NULL) {
        buf_size = 0x1000;
        buf = (char *)loc_alloc(buf_size);
    }
    else {
        buf_size *= 2;
        buf = (char *)loc_realloc(buf, buf_size);
    }
}

#define buf_add(ch) { if (buf_pos >= buf_size) realloc_buf(); buf[buf_pos++] = (char)(ch); }

void json_write_ulong(OutputStream * out, unsigned long n) {
    if (n >= 10) {
        json_write_ulong(out, n / 10);
        n = n % 10;
    }
    write_stream(out, n + '0');
}

void json_write_long(OutputStream * out, long n) {
    if (n < 0) {
        write_stream(out, '-');
        n = -n;
    }
    json_write_ulong(out, (unsigned long)n);
}

void json_write_uint64(OutputStream * out, uint64_t n) {
    if (n >= 10) {
        json_write_uint64(out, n / 10);
        n = n % 10;
    }
    write_stream(out, (int)n + '0');
}

void json_write_int64(OutputStream * out, int64_t n) {
    if (n < 0) {
        write_stream(out, '-');
        n = -n;
    }
    json_write_uint64(out, (uint64_t)n);
}

void json_write_double(OutputStream * out, double n) {
    char buf[256];
    snprintf(buf, sizeof(buf), "%.18g", n);
    write_string(out, buf);
}

void json_write_boolean(OutputStream * out, int b) {
    if (b) write_string(out, "true");
    else write_string(out, "false");
}

static int hex_digit(unsigned n) {
    n &= 0xf;
    if (n < 10) return '0' + n;
    return 'A' + (n - 10);
}

void json_write_char(OutputStream * out, char ch) {
    unsigned n = ch & 0xff;
    if (n < ' ') {
        write_stream(out, '\\');
        write_stream(out, 'u');
        write_stream(out, '0');
        write_stream(out, '0');
        write_stream(out, hex_digit(n >> 4));
        write_stream(out, hex_digit(n));
    }
    else {
        if (n == '"' || n == '\\') write_stream(out, '\\');
        write_stream(out, n);
    }
}

void json_write_string(OutputStream * out, const char * str) {
    if (str == NULL) {
        write_string(out, "null");
    }
    else {
        write_stream(out, '"');
        while (*str) json_write_char(out, *str++);
        write_stream(out, '"');
    }
}

void json_write_string_len(OutputStream * out, const char * str, size_t len) {
    if (str == NULL) {
        write_string(out, "null");
    }
    else {
        write_stream(out, '"');
        while (len > 0) {
            json_write_char(out, *str++);
            len--;
        }
        write_stream(out, '"');
    }
}

static int readHex(InputStream * inp) {
    int ch = read_stream(inp);
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
    int ch = read_stream(inp);
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
    int ch = read_stream(inp);
    if (ch == 'n') {
        if (read_stream(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        str[0] = 0;
        return -1;
    }
    if (ch != '"') exception(ERR_JSON_SYNTAX);
    for (;;) {
        ch = read_stream(inp);
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
    int ch = read_stream(inp);
    if (ch == 'n') {
        if (read_stream(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        return NULL;
    }
    buf_pos = 0;
    if (ch != '"') exception(ERR_JSON_SYNTAX);
    for (;;) {
        ch = read_stream(inp);
        if (ch == '"') break;
        if (ch == '\\') ch = read_esc_char(inp);
        buf_add(ch);
    }
    buf_add(0);
    str = (char *)loc_alloc(buf_pos);
    memcpy(str, buf, buf_pos);
    return str;
}

int json_read_boolean(InputStream * inp) {
    int ch = read_stream(inp);
    if (ch == 'f') {
        if (read_stream(inp) != 'a') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 's') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'e') exception(ERR_JSON_SYNTAX);
        return 0;
    }
    if (ch == 't') {
        if (read_stream(inp) != 'r') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'e') exception(ERR_JSON_SYNTAX);
        return 1;
    }
    exception(ERR_JSON_SYNTAX);
    return 0;
}

long json_read_long(InputStream * inp) {
    long res = 0;
    int neg = 0;
    int ch = read_stream(inp);
    if (ch == '-') {
        neg = 1;
        ch = read_stream(inp);
    }
    if (ch < '0' || ch > '9') exception(ERR_JSON_SYNTAX);
    res = ch - '0';
    for (;;) {
        ch = peek_stream(inp);
        if (ch < '0' || ch > '9') break;
        read_stream(inp);
        res = res * 10 + (ch - '0');
    }
    if (neg) return -res;
    return res;
}

unsigned long json_read_ulong(InputStream * inp) {
    unsigned long res = 0;
    int neg = 0;
    int ch = read_stream(inp);
    if (ch == '-') {
        neg = 1;
        ch = read_stream(inp);
    }
    if (ch < '0' || ch > '9') exception(ERR_JSON_SYNTAX);
    res = ch - '0';
    for (;;) {
        ch = peek_stream(inp);
        if (ch < '0' || ch > '9') break;
        read_stream(inp);
        res = res * 10 + (ch - '0');
    }
    if (neg) return ~res + 1;
    return res;
}

int64_t json_read_int64(InputStream * inp) {
    int64_t res = 0;
    int neg = 0;
    int ch = read_stream(inp);
    if (ch == '-') {
        neg = 1;
        ch = read_stream(inp);
    }
    if (ch < '0' || ch > '9') exception(ERR_JSON_SYNTAX);
    res = ch - '0';
    for (;;) {
        ch = peek_stream(inp);
        if (ch < '0' || ch > '9') break;
        read_stream(inp);
        res = res * 10 + (ch - '0');
    }
    if (neg) return -res;
    return res;
}

uint64_t json_read_uint64(InputStream * inp) {
    uint64_t res = 0;
    int neg = 0;
    int ch = read_stream(inp);
    if (ch == '-') {
        neg = 1;
        ch = read_stream(inp);
    }
    if (ch < '0' || ch > '9') exception(ERR_JSON_SYNTAX);
    res = ch - '0';
    for (;;) {
        ch = peek_stream(inp);
        if (ch < '0' || ch > '9') break;
        read_stream(inp);
        res = res * 10 + (ch - '0');
    }
    if (neg) return ~res + 1;
    return res;
}

double json_read_double(InputStream * inp) {
    char buf[256];
    int pos = 0;
    double n = 0;
    char * end = buf;

    for (;;) {
        int ch = peek_stream(inp);
        switch (ch) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
        case '-':
        case '+':
        case 'e':
        case 'E':
        case '.':
            if (pos >= (int)sizeof(buf) - 1) exception(ERR_BUFFER_OVERFLOW);
            buf[pos++] = (char)read_stream(inp);
            continue;
        }
        break;
    }
    if (pos == 0) exception(ERR_JSON_SYNTAX);
    buf[pos++] = 0;
    n = strtod(buf, &end);
    if (*end != 0) exception(ERR_JSON_SYNTAX);
    return n;
}

int json_read_struct(InputStream * inp, JsonStructCallBack * call_back, void * arg) {
    int ch = read_stream(inp);
    if (ch == 'n') {
        if (read_stream(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        return 0;
    }
    if (ch == '{') {
        ch = read_stream(inp);
        if (ch != '}') {
            for (;;) {
                int nm_len = 0;
                char nm[256];
                if (ch != '"') exception(ERR_JSON_SYNTAX);
                for (;;) {
                    ch = read_stream(inp);
                    if (ch == '"') break;
                    if (ch == '\\') ch = read_esc_char(inp);
                    if (nm_len < (int)sizeof(nm) - 1) nm[nm_len++] = (char)ch;
                }
                nm[nm_len] = 0;
                ch = read_stream(inp);
                if (ch != ':') exception(ERR_JSON_SYNTAX);
                call_back(inp, nm, arg);
                ch = read_stream(inp);
                if (ch == '}') break;
                if (ch != ',') exception(ERR_JSON_SYNTAX);
                ch = read_stream(inp);
            }
        }
        return 1;
    }
    exception(ERR_JSON_SYNTAX);
    return 0;
}

char ** json_read_alloc_string_array(InputStream * inp, int * cnt) {
    int ch = read_stream(inp);
    if (ch == 'n') {
        if (read_stream(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (cnt) *cnt = 0;
        return NULL;
    }
    else if (ch != '[') {
        exception(ERR_PROTOCOL);
        return NULL;
    }
    else {
        static unsigned * len_buf = NULL;
        static unsigned len_buf_size = 0;
        unsigned len_pos = 0;

        unsigned i, j;
        char * str = NULL;
        char ** arr = NULL;

        buf_pos = 0;

        if (peek_stream(inp) == ']') {
            read_stream(inp);
        }
        else {
            for (;;) {
                int ch = read_stream(inp);
                int len = 0;
                if (len_pos >= len_buf_size) {
                    len_buf_size = len_buf_size == 0 ? 0x100 : len_buf_size * 2;
                    len_buf = (unsigned *)loc_realloc(len_buf, len_buf_size * sizeof(unsigned));
                }
                if (ch == 'n') {
                    if (read_stream(inp) != 'u') exception(ERR_JSON_SYNTAX);
                    if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
                    if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
                }
                else {
                    if (ch != '"') exception(ERR_JSON_SYNTAX);
                    for (;;) {
                        ch = read_stream(inp);
                        if (ch == '"') break;
                        if (ch == '\\') ch = read_esc_char(inp);
                        buf_add(ch);
                        len++;
                    }
                }
                buf_add(0);
                len_buf[len_pos++] = len;
                ch = read_stream(inp);
                if (ch == ',') continue;
                if (ch == ']') break;
                exception(ERR_JSON_SYNTAX);
            }
        }
        buf_add(0);
        arr = (char **)loc_alloc((len_pos + 1) * sizeof(char *) + buf_pos);
        str = (char *)(arr + len_pos + 1);
        memcpy(str, buf, buf_pos);
        j = 0;
        for (i = 0; i < len_pos; i++) {
            arr[i] = str + j;
            j += len_buf[i] + 1;
        }
        arr[len_pos] = NULL;
        if (cnt) *cnt = len_pos;
        return arr;
    }
}

/*
* json_read_array - generic read array function
*
* This function will call the call_back with inp and arg as
*       arguments for each element of the list.
* Return 0 if null, 1 otherwise
*/
int json_read_array(InputStream * inp, JsonArrayCallBack * call_back, void * arg) {
    int ch = read_stream(inp);
    if (ch == 'n') {
        if (read_stream(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        return 0;
    }
    if (ch != '[') {
        exception(ERR_PROTOCOL);
        return 1;
    }
    if (peek_stream(inp) == ']') {
        read_stream(inp);
        return 1;
    }
    for (;;) {
        call_back(inp, arg);
        ch = read_stream(inp);
        if (ch == ',') continue;
        if (ch == ']') break;
        exception(ERR_JSON_SYNTAX);
    }
    return 1;
}

void json_read_binary_start(JsonReadBinaryState * state, InputStream * inp) {
    int ch = read_stream(inp);
    state->inp = inp;
    state->rem = 0;
    state->size_start = 0;
    state->size_done = 0;
    if (ch == '(') {
        state->encoding = ENCODING_BINARY;
        state->size_start = json_read_ulong(inp);
        if (read_stream(inp) != ')') exception(ERR_JSON_SYNTAX);
    }
    else if (ch == '"') {
        state->encoding = ENCODING_BASE64;
    }
    else if (ch == 'n') {
        if (read_stream(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        state->encoding = ENCODING_BINARY;
        state->size_start = 0;
    }
    else {
        exception(ERR_JSON_SYNTAX);
    }
}

size_t json_read_binary_data(JsonReadBinaryState * state, void * buf, size_t len) {
    size_t res = 0;
    uint8_t * ptr = (uint8_t *)buf;
    if (state->encoding == ENCODING_BINARY) {
        if (len > (size_t)(state->size_start - state->size_done)) len = state->size_start - state->size_done;
        while (res < len) ptr[res++] = (uint8_t)read_stream(state->inp);
    }
    else {
        while (len > 0) {
            if (state->rem > 0) {
                unsigned i = 0;
                while (i < state->rem && i < len) *ptr++ = state->buf[i++];
                len -= i;
                res += i;
                if (i < state->rem) {
                    int j = 0;
                    while (i < state->rem) state->buf[j++] = state->buf[i++];
                    state->rem = j;
                    return res;
                }
                state->rem = 0;
            }
            if (len >= 3) {
                int i = read_base64(state->inp, (char *)ptr, len);
                if (i == 0) break;
                ptr += i;
                len -= i;
                res += i;
            }
            else {
                state->rem = read_base64(state->inp, state->buf, 3);
                if (state->rem == 0) break;
            }
        }
    }
    state->size_done += res;
    return res;
}

void json_read_binary_end(JsonReadBinaryState * state) {
    if (state->rem != 0) exception(ERR_JSON_SYNTAX);
    if (state->encoding == ENCODING_BINARY) {
        assert(state->size_start == state->size_done);
    }
    else {
        if (read_stream(state->inp) != '"') exception(ERR_JSON_SYNTAX);
    }
}

char * json_read_alloc_binary(InputStream * inp, int * size) {
    char * data = NULL;
    int ch = peek_stream(inp);
    *size = 0;
    if (ch == 'n') {
        read_stream(inp);
        if (read_stream(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (read_stream(inp) != 'l') exception(ERR_JSON_SYNTAX);
    }
    else {
        JsonReadBinaryState state;

        json_read_binary_start(&state, inp);

        buf_pos = 0;
        for (;;) {
            int rd;
            if (buf_pos >= buf_size) realloc_buf();
            rd = json_read_binary_data(&state, buf + buf_pos, buf_size - buf_pos);
            if (rd == 0) break;
            buf_pos += rd;
        }

        assert(state.size_start <= 0 || (int)buf_pos == state.size_start);

        json_read_binary_end(&state);
        data = (char *)loc_alloc(buf_pos);
        memcpy(data, buf, buf_pos);
        *size = buf_pos;
    }
    return data;
}

void json_write_binary_start(JsonWriteBinaryState * state, OutputStream * out, int size) {
    state->out = out;
    state->rem = 0;
    state->encoding = out->supports_zero_copy && size > 0 ? ENCODING_BINARY : ENCODING_BASE64;
    state->size_start = size;
    state->size_done = 0;
    if (state->encoding == ENCODING_BINARY) {
        write_stream(state->out, '(');
        json_write_ulong(out, size);
        write_stream(state->out, ')');
    }
    else {
        write_stream(state->out, '"');
    }
}

void json_write_binary_data(JsonWriteBinaryState * state, const void * data, size_t len) {
    if (len <= 0) return;
    if (state->encoding == (int)ENCODING_BINARY) {
        write_block_stream(state->out, (const char *)data, len);
    }
    else {
        const uint8_t * ptr = (uint8_t *)data;
        size_t rem = state->rem;

        if (rem > 0) {
            while (rem < 3 && len > 0) {
                state->buf[rem++] = *ptr++;
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
            write_base64(state->out, (char *)ptr, len);
            if (rem > 0) memcpy(state->buf, ptr + len, rem);
        }
        state->rem = rem;
    }
    state->size_done += len;
}

void json_write_binary_end(JsonWriteBinaryState * state) {
    if (state->encoding == ENCODING_BINARY) {
        assert(state->size_start == state->size_done);
    }
    else {
        size_t rem;

        if ((rem = state->rem) > 0) {
            write_base64(state->out, state->buf, rem);
        }
        write_stream(state->out, '"');
    }
}

void json_write_binary(OutputStream * out, const void * data, size_t size) {
    if (data == NULL) {
        write_string(out, "null");
    }
    else {
        JsonWriteBinaryState state;
        json_write_binary_start(&state, out, size);
        json_write_binary_data(&state, data, size);
        json_write_binary_end(&state);
    }
}

void json_splice_binary(OutputStream * out, int fd, size_t size) {
    json_splice_binary_offset(out, fd, size, NULL);
}

void json_splice_binary_offset(OutputStream * out, int fd, size_t size, off_t * offset) {
    if (out->supports_zero_copy && size > 0) {
        write_stream(out, '(');
        json_write_ulong(out, size);
        write_stream(out, ')');
        while (size > 0) {
            ssize_t rd = splice_block_stream(out, fd, size, offset);
            if (rd < 0) exception(errno);
            if (rd == 0) exception(ERR_EOF);
            size -= rd;
        }
    }
    else {
        char buffer[0x1000];
        JsonWriteBinaryState state;
        json_write_binary_start(&state, out, size);

        while (size > 0) {
            ssize_t rd = 0;
            if (offset != NULL) {
                rd = pread(fd, buffer, size < sizeof(buffer) ? size : sizeof(buffer), *offset);
                if (rd > 0) *offset += rd;
            }
            else {
                rd = read(fd, buffer, size < sizeof(buffer) ? size : sizeof(buffer));
            }
            if (rd < 0) exception(errno);
            if (rd == 0) exception(ERR_EOF);
            json_write_binary_data(&state, buffer, rd);
            size -= rd;
        }
        json_write_binary_end(&state);
    }
}

static int skip_char(InputStream * inp) {
    int ch = read_stream(inp);
    buf_add(ch);
    return ch;
}

static void skip_object(InputStream * inp) {
    int ch = skip_char(inp);
    if (ch == 'n') {
        if (skip_char(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (skip_char(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (skip_char(inp) != 'l') exception(ERR_JSON_SYNTAX);
        return;
    }
    if (ch == 'f') {
        if (skip_char(inp) != 'a') exception(ERR_JSON_SYNTAX);
        if (skip_char(inp) != 'l') exception(ERR_JSON_SYNTAX);
        if (skip_char(inp) != 's') exception(ERR_JSON_SYNTAX);
        if (skip_char(inp) != 'e') exception(ERR_JSON_SYNTAX);
        return;
    }
    if (ch == 't') {
        if (skip_char(inp) != 'r') exception(ERR_JSON_SYNTAX);
        if (skip_char(inp) != 'u') exception(ERR_JSON_SYNTAX);
        if (skip_char(inp) != 'e') exception(ERR_JSON_SYNTAX);
        return;
    }
    if (ch == '"') {
        for (;;) {
            ch = skip_char(inp);
            if (ch == '"') break;
            if (ch == '\\') skip_char(inp);
        }
        return;
    }
    if (ch == '-' || ch >= '0' && ch <= '9') {
        for (;;) {
            ch = peek_stream(inp);
            if ((ch < '0' || ch > '9') && ch != '.'
                    && ch != 'e' && ch != 'E' && ch != '-' && ch != '+') break;
            skip_char(inp);
        }
        return;
    }
    if (ch == '[') {
        if (peek_stream(inp) == ']') {
            skip_char(inp);
        }
        else {
            for (;;) {
                int ch;
                skip_object(inp);
                ch = skip_char(inp);
                if (ch == ',') continue;
                if (ch == ']') break;
                exception(ERR_JSON_SYNTAX);
            }
        }
        return;
    }
    if (ch == '{') {
        if (peek_stream(inp) == '}') {
            skip_char(inp);
        }
        else {
            for (;;) {
                int ch;
                skip_object(inp);
                if (skip_char(inp) != ':') exception(ERR_JSON_SYNTAX);
                skip_object(inp);
                ch = skip_char(inp);
                if (ch == ',') continue;
                if (ch == '}') break;
                exception(ERR_JSON_SYNTAX);
            }
        }
        return;
    }
    exception(ERR_JSON_SYNTAX);
}

char * json_read_object(InputStream * inp) {
    char * str = NULL;
    buf_pos = 0;
    skip_object(inp);
    buf_add(0);
    str = (char *)loc_alloc(buf_pos);
    memcpy(str, buf, buf_pos);
    return str;
}

void json_skip_object(InputStream * inp) {
    buf_pos = 0;
    skip_object(inp);
}

int read_errno(InputStream * inp) {
    int no = 0;
    ErrorReport * err = NULL;
    int ch = read_stream(inp);
    if (ch == 0) return 0;
    if (ch != '{') exception(ERR_JSON_SYNTAX);
    if (peek_stream(inp) == '}') {
        read_stream(inp);
    }
    else {
        for (;;) {
            char name[256];
            json_read_string(inp, name, sizeof(name));
            if (read_stream(inp) != ':') exception(ERR_JSON_SYNTAX);
            if (err == NULL) err = create_error_report();
            if (strcmp(name, "Code") == 0) {
                err->code = json_read_long(inp);
            }
            else if (strcmp(name, "Time") == 0) {
                err->time_stamp = json_read_uint64(inp);
            }
            else if (strcmp(name, "Format") == 0) {
                err->format = json_read_alloc_string(inp);
            }
            else {
                ErrorReportItem * i = (ErrorReportItem *)loc_alloc_zero(sizeof(ErrorReportItem));
                i->name = loc_strdup(name);
                i->value = json_read_object(inp);
                i->next = err->props;
                err->props = i;
            }
            ch = read_stream(inp);
            if (ch == ',') continue;
            if (ch == '}') break;
            exception(ERR_JSON_SYNTAX);
        }
    }
    if (read_stream(inp) != 0) exception(ERR_JSON_SYNTAX);
    if (err == NULL) return 0;
    if (err->code != 0) no = set_error_report_errno(err);
    release_error_report(err);
    return no;
}

static void write_error_props(OutputStream * out, ErrorReport * rep) {
    ErrorReportItem * i = rep->props;

    if (rep->time_stamp != 0) {
        write_stream(out, ',');
        json_write_string(out, "Time");
        write_stream(out, ':');
        json_write_uint64(out, rep->time_stamp);
    }

    if (rep->format != NULL) {
        write_stream(out, ',');
        json_write_string(out, "Format");
        write_stream(out, ':');
        json_write_string(out, rep->format);
    }

    while (i != NULL) {
        write_stream(out, ',');
        json_write_string(out, i->name);
        write_stream(out, ':');
        write_string(out, i->value);
        i = i->next;
    }
}

void write_error_object(OutputStream * out, int err) {
    ErrorReport * rep = get_error_report(err);
    if (rep == NULL) {
        write_string(out, "null");
    }
    else {
        write_stream(out, '{');

        json_write_string(out, "Code");
        write_stream(out, ':');
        json_write_long(out, rep->code);

        write_error_props(out, rep);
        release_error_report(rep);

        write_stream(out, '}');
    }
}

void write_errno(OutputStream * out, int err) {
    if (err != 0) write_error_object(out, err);
    write_stream(out, 0);
}

void write_service_error(OutputStream * out, int err, const char * service_name, int service_error) {
    ErrorReport * rep = get_error_report(err);
    if (rep != NULL) {
        write_stream(out, '{');

        json_write_string(out, "Service");
        write_stream(out, ':');
        json_write_string(out, service_name);

        write_stream(out, ',');

        json_write_string(out, "Code");
        write_stream(out, ':');
        json_write_long(out, service_error);

        write_error_props(out, rep);
        release_error_report(rep);

        write_stream(out, '}');
    }
    write_stream(out, 0);
}
