/*******************************************************************************
 * Copyright (c) 2007-2009 Wind River Systems, Inc. and others.
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

#include "config.h"

#if SERVICE_Expressions

#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <string.h>
#include "myalloc.h"
#include "exceptions.h"
#include "stacktrace.h"
#include "symbols.h"
#include "breakpoints.h"
#include "expressions.h"
#include "json.h"
#include "context.h"
#include "stacktrace.h"
#include "breakpoints.h"
#include "symbols.h"

#define STR_POOL_SIZE 1024

struct StringValue {
    struct StringValue * next;
    char buf[1];
};

typedef struct StringValue StringValue;

#define SY_LEQ   256
#define SY_GEQ   257
#define SY_EQU   258
#define SY_NEQ   259
#define SY_AND   260
#define SY_OR    261
#define SY_SHL   262
#define SY_SHR   263
#define SY_VAL   264
#define SY_ID    265
#define SY_REF   266
#define SY_DEC   267
#define SY_INC   268
#define SY_A_SUB 269
#define SY_A_ADD 270
#define SY_A_SHL 271
#define SY_A_SHR 272
#define SY_A_OR  273
#define SY_A_XOR 274
#define SY_A_AND 275
#define SY_A_MUL 276
#define SY_A_DIV 277
#define SY_A_MOD 278
#define SY_SIZEOF 279

#define MODE_NORMAL 0
#define MODE_TYPE   1
#define MODE_SKIP   2

static char * text = NULL;
static int text_pos = 0;
static int text_len = 0;
static int text_ch = 0;
static int text_sy = 0;
static Value text_val;

static char str_pool[STR_POOL_SIZE];
static int str_pool_cnt = 0;
static StringValue * str_alloc_list = NULL;

static Context * expression_context = NULL;
static int expression_frame = STACK_NO_FRAME;

#define MAX_ID_CALLBACKS 8
static ExpressionIdentifierCallBack * id_callbacks[MAX_ID_CALLBACKS];
static int id_callback_cnt = 0;

static void * alloc_str(int size) {
    if (str_pool_cnt + size <= STR_POOL_SIZE) {
        char * s = str_pool + str_pool_cnt;
        str_pool_cnt += size;
        return s;
    }
    else {
        StringValue * s = (StringValue *)loc_alloc(sizeof(StringValue) + size - 1);
        s->next = str_alloc_list;
        str_alloc_list = s;
        return s->buf;
    }
}

void set_value(Value * v, void * data, size_t size) {
    v->remote = 0;
    v->address = 0;
    v->size = size;
    v->value = alloc_str(v->size);
    memcpy(v->value, data, v->size);
}

static void set_ctx_word_value(Value * v, ContextAddress data) {
    v->remote = 0;
    v->address = 0;
    v->size = context_word_size(expression_context);
    v->value = alloc_str(v->size);
    switch (v->size) {
    case 1: *(uint8_t *)v->value = (uint8_t)data; break;
    case 2: *(uint16_t *)v->value = (uint16_t)data; break;
    case 4: *(uint32_t *)v->value = (uint32_t)data; break;
    case 8: *(uint64_t *)v->value = data; break;
    default: assert(0);
    }
}

static void string_value(Value * v, char * str) {
    memset(v, 0, sizeof(Value));
    v->type_class = TYPE_CLASS_ARRAY;
    if (str != NULL) {
        v->size = strlen(str) + 1;
        v->value = alloc_str(v->size);
        memcpy(v->value, str, v->size);
    }
}

static void error(int no, char * msg) {
    char buf[256];
    snprintf(buf, sizeof(buf), "%s, text pos %d", msg, text_pos);
    str_exception(no, buf);
}

static void next_ch(void) {
    if (text_pos >= text_len) return;
    text_ch = (unsigned char)text[text_pos++];
}

static int next_hex(void) {
    int ch = text_ch;
    next_ch();
    if (ch >= '0' && ch <= '9') return ch - '0';
    if (ch >= 'A' && ch <= 'F') return ch - 'A' + 10;
    if (ch >= 'a' && ch <= 'f') return ch - 'a' + 10;
    error(ERR_INV_EXPRESSION, "Invalid hexadecimal number");
    return 0;
}

static int next_oct(void) {
    int ch = text_ch;
    next_ch();
    if (ch >= '0' && ch <= '7') return ch - '0';
    error(ERR_INV_EXPRESSION, "Invalid octal number");
    return 0;
}

static int next_dec(void) {
    int ch = text_ch;
    next_ch();
    if (ch >= '0' && ch <= '9') return ch - '0';
    error(ERR_INV_EXPRESSION, "Invalid decimal number");
    return 0;
}

static int next_char_val(void) {
    int n = 0;
    if (text_ch == '\\') {
        next_ch();
        switch (text_ch) {
        case 'n' : n = '\n'; break;
        case 't' : n = '\t'; break;
        case 'v' : n = '\v'; break;
        case 'b' : n = '\b'; break;
        case 'r' : n = '\r'; break;
        case 'f' : n = '\f'; break;
        case 'a' : n = '\a'; break;
        case '\\': n = '\\'; break;
        case '\'': n = '\''; break;
        case '"' : n = '"'; break;
        case 'x' :
            next_ch();
            n = next_hex() << 8;
            n |= next_hex() << 4;
            n |= next_hex();
            return n;
        case '0' :
        case '1' :
        case '2' :
        case '3' :
            n = next_oct() << 6;
            n |= next_oct() << 3;
            n |= next_oct();
            return n;
        default  :
            n = text_ch;
            break;
        }
    }
    else {
        n = text_ch;
    }
    next_ch();
    return n;
}

static int is_name_character(int ch) {
    if (ch >= 'A' && ch <= 'Z') return 1;
    if (ch >= 'a' && ch <= 'z') return 1;
    if (ch >= '0' && ch <= '9') return 1;
    if (ch == '_') return 1;
    if (ch == '$') return 1;
    if (ch == '@') return 1;
    return 0;
}

static void next_sy(void) {
    for (;;) {
        int ch = text_ch;
        next_ch();
        switch (ch) {
        case 0:
            text_sy = 0;
            return;
        case ' ':
        case '\r':
        case '\n':
        case '\t':
            continue;
        case '(':
        case ')':
        case '{':
        case '}':
        case '~':
        case '[':
        case ']':
        case ';':
        case ':':
        case '?':
        case ',':
        case '.':
            text_sy = ch;
            return;
        case '-':
            if (text_ch == '>') {
                next_ch();
                text_sy = SY_REF;
                return;
            }
            if (text_ch == '-') {
                next_ch();
                text_sy = SY_DEC;
                return;
            }
            if (text_ch == '=') {
                next_ch();
                text_sy = SY_A_SUB;
                return;
            }
            text_sy = ch;
            return;
        case '+':
            if (text_ch == '+') {
                next_ch();
                text_sy = SY_INC;
                return;
            }
            if (text_ch == '=') {
                next_ch();
                text_sy = SY_A_ADD;
                return;
            }
            text_sy = ch;
            return;
        case '<':
            if (text_ch == '<') {
                next_ch();
                if (text_ch == '=') {
                    next_ch();
                    text_sy = SY_A_SHL;
                    return;
                }
                text_sy = SY_SHL;
                return;
            }
            if (text_ch == '=') {
                next_ch();
                text_sy = SY_LEQ;
                return;
            }
            text_sy = ch;
            return;
        case '>':
            if (text_ch == '>') {
                next_ch();
                if (text_ch == '=') {
                    next_ch();
                    text_sy = SY_A_SHR;
                    return;
                }
                text_sy = SY_SHR;
                return;
            }
            if (text_ch == '=') {
                next_ch();
                text_sy = SY_GEQ;
                return;
            }
            text_sy = ch;
            return;
        case '=':
            if (text_ch == '=') {
                next_ch();
                text_sy = SY_EQU;
                return;
            }
            text_sy = ch;
            return;
        case '!':
            if (text_ch == '=') {
                next_ch();
                text_sy = SY_NEQ;
                return;
            }
            text_sy = ch;
            return;
        case '&':
            if (text_ch == '&') {
                next_ch();
                text_sy = SY_AND;
                return;
            }
            if (text_ch == '=') {
                next_ch();
                text_sy = SY_A_AND;
                return;
            }
            text_sy = ch;
            return;
        case '|':
            if (text_ch == '|') {
                next_ch();
                text_sy = SY_OR;
                return;
            }
            if (text_ch == '=') {
                next_ch();
                text_sy = SY_A_OR;
                return;
            }
            text_sy = ch;
            return;
        case '*':
            if (text_ch == '=') {
                next_ch();
                text_sy = SY_A_MUL;
                return;
            }
            text_sy = ch;
            return;
        case '/':
            if (text_ch == '|') {
                next_ch();
                text_sy = SY_A_DIV;
                return;
            }
            text_sy = ch;
            return;
        case '%':
            if (text_ch == '|') {
                next_ch();
                text_sy = SY_A_MOD;
                return;
            }
            text_sy = ch;
            return;
        case '^':
            if (text_ch == '=') {
                next_ch();
                text_sy = SY_A_XOR;
                return;
            }
            text_sy = ch;
            return;
        case '\'':
            memset(&text_val, 0, sizeof(text_val));
            text_val.type_class = TYPE_CLASS_INTEGER;
            text_val.size = sizeof(int);
            text_val.value = alloc_str(text_val.size);
            text_val.constant = 1;
            *(int *)text_val.value = next_char_val();
            if (text_ch != '\'') error(ERR_INV_EXPRESSION, "Missing 'single quote'");
            next_ch();
            text_sy = SY_VAL;
            return;
        case '"':
            {
                int len = 0;
                int cnt = 0;
                int pos = text_pos;
                while (text_ch != '"') {
                    next_char_val();
                    len++;
                }
                memset(&text_val, 0, sizeof(text_val));
                text_val.type_class = TYPE_CLASS_ARRAY;
                text_val.size = len + 1;
                text_val.value = alloc_str(text_val.size);
                text_val.constant = 1;
                text_pos = pos - 1;
                next_ch();
                while (text_ch != '"') {
                    ((char *)text_val.value)[cnt++] = (char)next_char_val();
                }
                assert(cnt == len);
                ((char *)text_val.value)[cnt] = 0;
                next_ch();
                text_sy = SY_VAL;
            }
            return;
        case '0':
            if (text_ch == 'x') {
                uint64_t value = 0;
                next_ch();
                memset(&text_val, 0, sizeof(text_val));
                text_val.type_class = TYPE_CLASS_CARDINAL;
                text_val.size = sizeof(uint64_t);
                text_val.value = alloc_str(text_val.size);
                text_val.constant = 1;
                while (text_ch >= '0' && text_ch <= '9' ||
                        text_ch >= 'A' && text_ch <= 'F' ||
                        text_ch >= 'a' && text_ch <= 'f') {
                    value = (value << 4) | next_hex();
                }
                *(uint64_t *)text_val.value = value;
            }
            else {
                int64_t value = 0;
                memset(&text_val, 0, sizeof(text_val));
                text_val.type_class = TYPE_CLASS_INTEGER;
                text_val.size = sizeof(int64_t);
                text_val.value = alloc_str(text_val.size);
                text_val.constant = 1;
                while (text_ch >= '0' && text_ch <= '7') {
                    value = (value << 3) | next_oct();
                }
                *(int64_t *)text_val.value = value;
            }
            text_sy = SY_VAL;
            return;
        default:
            if (ch >= '0' && ch <= '9') {
                int pos = text_pos - 2;
                int64_t value = ch - '0';
                while (text_ch >= '0' && text_ch <= '9') {
                    value = (value * 10) + next_dec();
                }
                memset(&text_val, 0, sizeof(text_val));
                if (text_ch == '.') {
                    char * end = NULL;
                    double x = strtod(text + pos, &end);
                    text_pos = end - text;
                    next_ch();
                    text_val.type_class = TYPE_CLASS_REAL;
                    text_val.size = sizeof(double);
                    text_val.value = alloc_str(text_val.size);
                    *(double *)text_val.value = x;
                }
                else {
                    text_val.type_class = TYPE_CLASS_INTEGER;
                    text_val.size = sizeof(int64_t);
                    text_val.value = alloc_str(text_val.size);
                    *(int64_t *)text_val.value = value;
                }
                text_val.constant = 1;
                text_sy = SY_VAL;
                return;
            }
            if (is_name_character(ch)) {
                int len = 1;
                int cnt = 0;
                int pos = text_pos - 1;
                while (is_name_character(text_ch)) {
                    len++;
                    next_ch();
                }
                memset(&text_val, 0, sizeof(text_val));
                text_val.type_class = TYPE_CLASS_ARRAY;
                text_val.size = len + 1;
                text_val.value = alloc_str(text_val.size);
                text_val.constant = 1;
                text_pos = pos - 1;
                next_ch();
                while (is_name_character(text_ch)) {
                    ((char *)text_val.value)[cnt++] = (char)text_ch;
                    next_ch();
                }
                assert(cnt == len);
                ((char *)text_val.value)[cnt] = 0;
                if (strcmp(text_val.value, "sizeof") == 0) text_sy = SY_SIZEOF;
                else text_sy = SY_ID;
                return;
            }
            error(ERR_INV_EXPRESSION, "Illegal character");
            break;
        }
    }
}

static int identifier(char * name, Value * v) {
    int i;
    memset(v, 0, sizeof(Value));
    for (i = 0; i < id_callback_cnt; i++) {
        if (id_callbacks[i](expression_context, expression_frame, name, v)) return SYM_CLASS_VALUE;
    }
    if (expression_context == NULL) {
        exception(ERR_INV_CONTEXT);
    }
    if (strcmp(name, "$thread") == 0) {
        if (context_has_state(expression_context)) {
            string_value(v, thread_id(expression_context));
        }
        else {
            string_value(v, container_id(expression_context));
        }
        v->constant = 1;
        return SYM_CLASS_VALUE;
    }
#if SERVICE_Symbols
    {
        Symbol sym;
        if (find_symbol(expression_context, expression_frame, name, &sym) < 0) {
            error(errno, "Invalid identifier");
        }
        else {
            if (get_symbol_type(&sym, &v->type) < 0) {
                error(errno, "Cannot retrieve symbol type");
            }
            if (get_symbol_type_class(&sym, &v->type_class) < 0) {
                error(errno, "Cannot retrieve symbol type class");
            }
            switch (sym.sym_class) {
            case SYM_CLASS_VALUE:
                {
                    size_t size = 0;
                    void * value = NULL;
                    if (get_symbol_value(&sym, &value, &size) < 0) {
                        error(errno, "Cannot retrieve symbol value");
                    }
                    v->size = size;
                    v->value = alloc_str(v->size);
                    v->constant = 1;
                    memcpy(v->value, value, size);
                    loc_free(value);
                }
                break;
            case SYM_CLASS_REFERENCE:
                v->remote = 1;
                if (get_symbol_size(&sym, expression_frame, &v->size) < 0) {
                    error(errno, "Cannot retrieve symbol size");
                }
                if (get_symbol_address(&sym, expression_frame, &v->address) < 0) {
                    error(errno, "Cannot retrieve symbol address");
                }
                break;
            case SYM_CLASS_FUNCTION:
                {
                    ContextAddress word = 0;
                    v->type_class = TYPE_CLASS_CARDINAL;
                    if (get_symbol_address(&sym, expression_frame, &word) < 0) {
                        error(errno, "Cannot retrieve symbol address");
                    }
                    set_ctx_word_value(v, word);
                }
                break;
            case SYM_CLASS_TYPE:
                assert(v->size == 0);
                break;
            default:
                error(ERR_UNSUPPORTED, "Invalid symbol class");
            }
            return sym.sym_class;
        }
    }
#else
    error(ERR_UNSUPPORTED, "Symbols service not available");
#endif
    return SYM_CLASS_UNKNOWN;
}

static int type_name(int mode, Symbol * type) {
    Value v;
    char * name = text_val.value;
    int sym_class;

    if (text_sy != SY_ID) return 0;
    next_sy();
    sym_class = identifier(name, &v);
    if (sym_class != SYM_CLASS_TYPE) return 0;
    while (text_sy == '*') {
        next_sy();
        if (mode == MODE_SKIP) continue;
#if SERVICE_Symbols
        if (get_symbol_pointer(&v.type, &v.type)) {
            error(errno, "Cannot get pointer type");
        }
#else
        memset(&v.type, 0, sizeof(v.type));
#endif
    }
    *type = v.type;
    return 1;
}

static void load_value(Value * v) {
    void * value;

    if (!v->remote) return;
    assert(!v->constant);
    value = alloc_str(v->size);
    if (context_read_mem(expression_context, v->address, value, v->size) < 0) {
        error(errno, "Can't read variable value");
    }
    check_breakpoints_on_memory_read(expression_context, v->address, value, v->size);
    v->value = value;
    v->remote = 0;
}

static int is_number(Value * v) {
    switch (v->type_class) {
    case TYPE_CLASS_INTEGER:
    case TYPE_CLASS_CARDINAL:
    case TYPE_CLASS_REAL:
    case TYPE_CLASS_ENUMERATION:
        return 1;
    }
    return 0;
}

static int is_whole_number(Value * v) {
    switch (v->type_class) {
    case TYPE_CLASS_INTEGER:
    case TYPE_CLASS_CARDINAL:
    case TYPE_CLASS_ENUMERATION:
        return 1;
    }
    return 0;
}

static int64_t to_int(int mode, Value * v) {
    if (mode != MODE_NORMAL) {
        if (v->remote) {
            v->value = alloc_str(v->size);
            v->remote = 0;
        }
        return 0;
    }

    if (v->type_class == TYPE_CLASS_POINTER) {
        load_value(v);
        switch (v->size)  {
        case 1: return *(uint8_t *)v->value;
        case 2: return *(uint16_t *)v->value;
        case 4: return *(uint32_t *)v->value;
        case 8: return *(uint64_t *)v->value;
        }
    }
    if (is_number(v)) {
        load_value(v);

        if (v->type_class == TYPE_CLASS_REAL) {
            switch (v->size)  {
            case 4: return (int64_t)*(float *)v->value;
            case 8: return (int64_t)*(double *)v->value;
            }
        }
        else if (v->type_class == TYPE_CLASS_CARDINAL) {
            switch (v->size)  {
            case 1: return (int64_t)*(uint8_t *)v->value;
            case 2: return (int64_t)*(uint16_t *)v->value;
            case 4: return (int64_t)*(uint32_t *)v->value;
            case 8: return (int64_t)*(uint64_t *)v->value;
            }
        }
        else {
            switch (v->size)  {
            case 1: return *(int8_t *)v->value;
            case 2: return *(int16_t *)v->value;
            case 4: return *(int32_t *)v->value;
            case 8: return *(int64_t *)v->value;
            }
        }
    }

    error(ERR_INV_EXPRESSION, "Operation is not applicable for the value type");
    return 0;
}

static uint64_t to_uns(int mode, Value * v) {
    if (mode != MODE_NORMAL) {
        if (v->remote) {
            v->value = alloc_str(v->size);
            v->remote = 0;
        }
        return 0;
    }

    if (v->type_class == TYPE_CLASS_ARRAY && v->remote) {
        return (uint64_t)v->address;
    }
    if (v->type_class == TYPE_CLASS_POINTER) {
        load_value(v);
        switch (v->size)  {
        case 1: return *(uint8_t *)v->value;
        case 2: return *(uint16_t *)v->value;
        case 4: return *(uint32_t *)v->value;
        case 8: return *(uint64_t *)v->value;
        }
    }
    if (is_number(v)) {
        load_value(v);

        if (v->type_class == TYPE_CLASS_REAL) {
            switch (v->size)  {
            case 4: return (uint64_t)*(float *)v->value;
            case 8: return (uint64_t)*(double *)v->value;
            }
        }
        else if (v->type_class == TYPE_CLASS_CARDINAL) {
            switch (v->size)  {
            case 1: return *(uint8_t *)v->value;
            case 2: return *(uint16_t *)v->value;
            case 4: return *(uint32_t *)v->value;
            case 8: return *(uint64_t *)v->value;
            }
        }
        else {
            switch (v->size)  {
            case 1: return (uint64_t)*(int8_t *)v->value;
            case 2: return (uint64_t)*(int16_t *)v->value;
            case 4: return (uint64_t)*(int32_t *)v->value;
            case 8: return (uint64_t)*(int64_t *)v->value;
            }
        }
    }

    error(ERR_INV_EXPRESSION, "Operation is not applicable for the value type");
    return 0;
}

static double to_double(int mode, Value * v) {
    if (mode != MODE_NORMAL) {
        if (v->remote) {
            v->value = alloc_str(v->size);
            v->remote = 0;
        }
        return 0;
    }

    if (is_number(v)) {
        load_value(v);

        if (v->type_class == TYPE_CLASS_REAL) {
            switch (v->size)  {
            case 4: return *(float *)v->value;
            case 8: return *(double *)v->value;
            }
        }
        else if (v->type_class == TYPE_CLASS_CARDINAL) {
            switch (v->size)  {
            case 1: return (double)*(uint8_t *)v->value;
            case 2: return (double)*(uint16_t *)v->value;
            case 4: return (double)*(uint32_t *)v->value;
            case 8: return (double)*(uint64_t *)v->value;
            }
        }
        else {
            switch (v->size)  {
            case 1: return (double)*(int8_t *)v->value;
            case 2: return (double)*(int16_t *)v->value;
            case 4: return (double)*(int32_t *)v->value;
            case 8: return (double)*(int64_t *)v->value;
            }
        }
    }

    error(ERR_INV_EXPRESSION, "Operation is not applicable for the value type");
    return 0;
}

static int to_boolean(int mode, Value * v) {
    return to_int(mode, v) != 0;
}

static void expression(int mode, Value * v);

static void primary_expression(int mode, Value * v) {
    if (text_sy == '(') {
        next_sy();
        expression(mode, v);
        if (text_sy != ')') error(ERR_INV_EXPRESSION, "Missing ')'");
        next_sy();
    }
    else if (text_sy == SY_VAL) {
        if (mode != MODE_SKIP) *v = text_val;
        next_sy();
    }
    else if (text_sy == SY_ID) {
        if (mode != MODE_SKIP) {
            int sym_class = identifier((char *)text_val.value, v);
            if (sym_class == SYM_CLASS_TYPE) error(ERR_INV_EXPRESSION, "Illegal usage of type name");
        }
        next_sy();
    }
    else {
        error(ERR_INV_EXPRESSION, "Syntax error");
    }
}

static void op_deref(int mode, Value * v) {
    if (mode == MODE_SKIP) return;
#if SERVICE_Symbols
    if (v->type_class != TYPE_CLASS_ARRAY && v->type_class != TYPE_CLASS_POINTER) {
        error(ERR_INV_EXPRESSION, "Array or pointer type expected");
    }
    if (v->type_class == TYPE_CLASS_POINTER) {
        if (mode == MODE_TYPE) {
            v->address = 0;
        }
        else {
            load_value(v);
            switch (v->size)  {
            case 2: v->address = (ContextAddress)*(uint16_t *)v->value; break;
            case 4: v->address = (ContextAddress)*(uint32_t *)v->value; break;
            case 8: v->address = (ContextAddress)*(uint64_t *)v->value; break;
            default: error(ERR_INV_EXPRESSION, "Invalid value size");
            }
        }
    }
    if (get_symbol_base_type(&v->type, &v->type) < 0) {
        error(errno, "Cannot retrieve symbol type");
    }
    if (get_symbol_type_class(&v->type, &v->type_class) < 0) {
        error(errno, "Cannot retrieve symbol type class");
    }
    if (get_symbol_size(&v->type, expression_frame, &v->size) < 0) {
        error(errno, "Cannot retrieve symbol size");
    }
    v->value = NULL;
    v->remote = 1;
    v->constant = 0;
#else
    error(ERR_UNSUPPORTED, "Symbols service not available");
#endif
}

static void op_field(int mode, Value * v) {
    char * name = text_val.value;
    if (text_sy != SY_ID) error(ERR_INV_EXPRESSION, "Field name expected");
    next_sy();
    if (mode == MODE_SKIP) return;
    if (v->type_class != TYPE_CLASS_COMPOSITE) {
        error(ERR_INV_EXPRESSION, "Composite type expected");
    }
#if SERVICE_Symbols
    {
        Symbol sym;
        size_t size = 0;
        unsigned long offs = 0;
        Symbol * children = NULL;
        int count = 0;
        int i;

        if (get_symbol_children(&v->type, &children, &count) < 0) {
            error(errno, "Cannot retrieve field list");
        }
        memset(&sym, 0, sizeof(sym));
        for (i = 0; i < count; i++) {
            char * s = NULL;
            if (get_symbol_name(children + i, &s) < 0) {
                error(errno, "Cannot retrieve field name");
            }
            if (s == NULL) continue;
            if (strcmp(s, name) == 0) {
                loc_free(s);
                sym = children[i];
                break;
            }
            loc_free(s);
        }
        loc_free(children);
        if (i == count) {
            error(ERR_SYM_NOT_FOUND, "Symbol not found");
        }
        if (sym.sym_class != SYM_CLASS_REFERENCE) {
            error(ERR_UNSUPPORTED, "Invalid symbol class");
        }
        if (get_symbol_size(&sym, expression_frame, &size) < 0) {
            error(errno, "Cannot retrieve field size");
        }
        if (get_symbol_offset(&sym, &offs) < 0) {
            error(errno, "Cannot retrieve field offset");
        }
        if (offs + size > v->size) {
            error(ERR_INV_EXPRESSION, "Invalid field offset and/or size");
        }
        if (v->remote) {
            if (mode != MODE_TYPE) v->address += offs;
        }
        else {
            v->value = (char *)v->value + offs;
        }
        v->size = size;
        if (get_symbol_type(&sym, &v->type) < 0) {
            error(errno, "Cannot retrieve symbol type");
        }
        if (get_symbol_type_class(&sym, &v->type_class) < 0) {
            error(errno, "Cannot retrieve symbol type class");
        }
    }
#else
    error(ERR_UNSUPPORTED, "Symbols service not available");
#endif
}

static void op_index(int mode, Value * v) {
#if SERVICE_Symbols
    Value i;
    unsigned long offs = 0;
    size_t size = 0;
    Symbol type;

    expression(mode, &i);
    if (mode == MODE_SKIP) return;

    if (v->type_class != TYPE_CLASS_ARRAY && v->type_class != TYPE_CLASS_POINTER) {
        error(ERR_INV_EXPRESSION, "Array or pointer expected");
    }
    if (v->type_class == TYPE_CLASS_POINTER) {
        if (mode == MODE_TYPE) {
            v->address = 0;
        }
        else {
            load_value(v);
            switch (v->size)  {
            case 2: v->address = (ContextAddress)*(uint16_t *)v->value; break;
            case 4: v->address = (ContextAddress)*(uint32_t *)v->value; break;
            case 8: v->address = (ContextAddress)*(uint64_t *)v->value; break;
            default: error(ERR_INV_EXPRESSION, "Invalid value size");
            }
        }
    }
    if (get_symbol_base_type(&v->type, &type) < 0) {
        error(errno, "Cannot get array element type");
    }
    if (get_symbol_size(&type, expression_frame, &size) < 0) {
        error(errno, "Cannot get array element type");
    }
    /* TODO: array lowest bound */
    offs = (unsigned long)to_uns(mode, &i) * size;
    if (v->type_class == TYPE_CLASS_ARRAY && offs + size > v->size) {
        error(ERR_INV_EXPRESSION, "Invalid index");
    }
    if (v->remote) {
        v->address += offs;
    }
    else {
        v->value = (char *)v->value + offs;
    }
    v->size = size;
    v->type = type;
    if (get_symbol_type_class(&type, &v->type_class) < 0) {
        error(errno, "Cannot retrieve symbol type class");
    }
#else
    error(ERR_UNSUPPORTED, "Symbols service not available");
#endif
}

static void op_addr(int mode, Value * v) {
    if (mode == MODE_SKIP) return;
    if (!v->remote) error(ERR_INV_EXPRESSION, "Invalid '&': value has no address");
    assert(!v->constant);
    set_ctx_word_value(v, v->address);
    v->type_class = TYPE_CLASS_POINTER;
#if SERVICE_Symbols
    if (get_symbol_pointer(&v->type, &v->type)) {
        error(errno, "Cannot get pointer type");
    }
#else
    memset(&v->type, 0, sizeof(v->type));
#endif
}

static void unary_expression(int mode, Value * v);

static void op_sizeof(int mode, Value * v) {
    Symbol type;
    int pos;
    int p = text_sy == '(';

    if (p) next_sy();
    pos = text_pos - 2;
    if (type_name(mode, &type)) {
        if (mode != MODE_SKIP) {
            size_t type_size = 0;
#if SERVICE_Symbols
            if (get_symbol_size(&type, expression_frame, &type_size) < 0) {
                error(errno, "Cannot retrieve symbol size");
            }
#endif
            set_ctx_word_value(v, type_size);
            memset(&v->type, 0, sizeof(v->type));
            v->type_class = TYPE_CLASS_CARDINAL;
            v->constant = 1;
        }
    }
    else {
        text_pos = pos;
        next_ch();
        next_sy();
        unary_expression(mode == MODE_NORMAL ? MODE_TYPE : mode, v);
        if (mode != MODE_SKIP) {
            set_ctx_word_value(v, v->size);
            memset(&v->type, 0, sizeof(v->type));
            v->type_class = TYPE_CLASS_CARDINAL;
            v->constant = 1;
        }
    }
    if (p) {
        if (text_sy != ')') error(ERR_INV_EXPRESSION, "')' expected");
        next_sy();
    }
}


static void postfix_expression(int mode, Value * v) {
    primary_expression(mode, v);
    for (;;) {
        if (text_sy == '.') {
            next_sy();
            op_field(mode, v);
        }
        else if (text_sy == '[') {
            next_sy();
            op_index(mode, v);
            if (text_sy != ']') {
                error(ERR_INV_EXPRESSION, "']' expected");
            }
            next_sy();
        }
        else if (text_sy == SY_REF) {
            next_sy();
            op_deref(mode, v);
            op_field(mode, v);
        }
        else {
            break;
        }
    }
}

static void unary_expression(int mode, Value * v) {
    switch (text_sy) {
    case '*':
        next_sy();
        unary_expression(mode, v);
        op_deref(mode, v);
        break;
    case '&':
        next_sy();
        unary_expression(mode, v);
        op_addr(mode, v);
        break;
    case SY_SIZEOF:
        next_sy();
        op_sizeof(mode, v);
        break;
    case '+':
        next_sy();
        unary_expression(mode, v);
        break;
    case '-':
        next_sy();
        unary_expression(mode, v);
        if (mode != MODE_SKIP) {
            if (!is_number(v)) {
                error(ERR_INV_EXPRESSION, "Numeric types expected");
            }
            else if (v->type_class == TYPE_CLASS_REAL) {
                double * value = alloc_str(sizeof(double));
                *value = -to_double(mode, v);
                v->type_class = TYPE_CLASS_REAL;
                v->size = sizeof(double);
                v->value = value;
            }
            else if (v->type_class != TYPE_CLASS_CARDINAL) {
                int64_t * value = alloc_str(sizeof(int64_t));
                *value = -to_int(mode, v);
                v->type_class = TYPE_CLASS_INTEGER;
                v->size = sizeof(int64_t);
                v->value = value;
            }
            assert(!v->remote);
            memset(&v->type, 0, sizeof(Symbol));
        }
        break;
    case '!':
        next_sy();
        unary_expression(mode, v);
        if (mode != MODE_SKIP) {
            if (!is_whole_number(v)) {
                error(ERR_INV_EXPRESSION, "Integral types expected");
            }
            else {
                int * value = alloc_str(sizeof(int));
                *value = !to_int(mode, v);
                v->type_class = TYPE_CLASS_INTEGER;
                v->size = sizeof(int);
                v->value = value;
            }
            assert(!v->remote);
            memset(&v->type, 0, sizeof(Symbol));
        }
        break;
    case '~':
        next_sy();
        unary_expression(mode, v);
        if (mode != MODE_SKIP) {
            if (!is_whole_number(v)) {
                error(ERR_INV_EXPRESSION, "Integral types expected");
            }
            else {
                int64_t * value = alloc_str(sizeof(int64_t));
                *value = ~to_int(mode, v);
                v->size = sizeof(int64_t);
                v->value = value;
            }
            assert(!v->remote);
            memset(&v->type, 0, sizeof(Symbol));
        }
        break;
    default:
        postfix_expression(mode, v);
        break;
    }
}

static void cast_expression(int mode, Value * v) {
    if (text_sy == '(') {
#if SERVICE_Symbols
        Symbol type;
        int type_class = TYPE_CLASS_UNKNOWN;
        size_t type_size = 0;
        int pos = text_pos - 2;

        assert(text[pos] == '(');
        next_sy();
        if (!type_name(mode, &type)) {
            text_pos = pos;
            next_ch();
            next_sy();
            assert(text_sy == '(');
            unary_expression(mode, v);
            return;
        }
        if (text_sy != ')') error(ERR_INV_EXPRESSION, "')' expected");
        next_sy();
        cast_expression(mode, v);
        if (mode == MODE_SKIP) return;
        if (get_symbol_type_class(&type, &type_class) < 0) {
            error(errno, "Cannot retrieve symbol type class");
        }
        if (get_symbol_size(&type, expression_frame, &type_size) < 0) {
            error(errno, "Cannot retrieve symbol size");
        }
        if (v->remote && v->size == type_size) {
            /* A type cast can be an l-value expression as long as the size does not change */
            int ok = 0;
            switch (type_class) {
            case TYPE_CLASS_CARDINAL:
            case TYPE_CLASS_POINTER:
            case TYPE_CLASS_INTEGER:
            case TYPE_CLASS_ENUMERATION:
                switch (v->type_class) {
                case TYPE_CLASS_CARDINAL:
                case TYPE_CLASS_POINTER:
                case TYPE_CLASS_INTEGER:
                case TYPE_CLASS_ENUMERATION:
                    ok = 1;
                    break;
                }
                break;
            case TYPE_CLASS_REAL:
                ok = v->type_class == TYPE_CLASS_REAL;
                break;
            }
            if (ok) {
                v->type = type;
                v->type_class = type_class;
                return;
            }
        }
        switch (type_class) {
        case TYPE_CLASS_UNKNOWN:
            error(ERR_INV_EXPRESSION, "Unknown type class");
            break;
        case TYPE_CLASS_CARDINAL:
        case TYPE_CLASS_POINTER:
            {
                uint64_t value = to_uns(mode, v);
                v->type = type;
                v->type_class = type_class;
                v->size = type_size;
                v->remote = 0;
                v->value = alloc_str(v->size);
                switch (v->size) {
                case 1: *(uint8_t *)v->value = (uint8_t)value; break;
                case 2: *(uint16_t *)v->value = (uint16_t)value; break;
                case 4: *(uint32_t *)v->value = (uint32_t)value; break;
                case 8: *(uint64_t *)v->value = value; break;
                default: assert(0);
                }
            }
            break;
        case TYPE_CLASS_INTEGER:
        case TYPE_CLASS_ENUMERATION:
            {
                int64_t value = to_int(mode, v);
                v->type = type;
                v->type_class = type_class;
                v->size = type_size;
                v->remote = 0;
                v->value = alloc_str(v->size);
                switch (v->size) {
                case 1: *(int8_t *)v->value = (int8_t)value; break;
                case 2: *(int16_t *)v->value = (int16_t)value; break;
                case 4: *(int32_t *)v->value = (int32_t)value; break;
                case 8: *(int64_t *)v->value = value; break;
                default: assert(0);
                }
            }
            break;
        case TYPE_CLASS_REAL:
            {
                double value = to_double(mode, v);
                v->type = type;
                v->type_class = type_class;
                v->size = type_size;
                v->remote = 0;
                v->value = alloc_str(v->size);
                switch (v->size) {
                case 4: *(float *)v->value = (float)value; break;
                case 8: *(double *)v->value = value; break;
                default: assert(0);
                }
            }
            break;
        default:
            error(ERR_INV_EXPRESSION, "Invalid type cast: illegal destination type");
            break;
        }
#else
    error(ERR_UNSUPPORTED, "Symbols service not available");
#endif
    }
    else {
        unary_expression(mode, v);
    }
}

static void multiplicative_expression(int mode, Value * v) {
    cast_expression(mode, v);
    while (text_sy == '*' || text_sy == '/' || text_sy == '%') {
        Value x;
        int sy = text_sy;
        next_sy();
        cast_expression(mode, &x);
        if (mode != MODE_SKIP) {
            if (!is_number(v) || !is_number(&x)) {
                error(ERR_INV_EXPRESSION, "Numeric types expected");
            }
            if (mode == MODE_NORMAL && sy != '*' && to_int(mode, &x) == 0) {
                error(ERR_INV_EXPRESSION, "Dividing by zero");
            }
            if (v->type_class == TYPE_CLASS_REAL || x.type_class == TYPE_CLASS_REAL) {
                double * value = alloc_str(sizeof(double));
                if (mode == MODE_NORMAL) {
                    switch (sy) {
                    case '*': *value = to_double(mode, v) * to_double(mode, &x); break;
                    case '/': *value = to_double(mode, v) / to_double(mode, &x); break;
                    default: error(ERR_INV_EXPRESSION, "Invalid type");
                    }
                }
                v->type_class = TYPE_CLASS_REAL;
                v->size = sizeof(double);
                v->value = value;
            }
            else if (v->type_class == TYPE_CLASS_CARDINAL || x.type_class == TYPE_CLASS_CARDINAL) {
                uint64_t * value = alloc_str(sizeof(uint64_t));
                if (mode == MODE_NORMAL) {
                    switch (sy) {
                    case '*': *value = to_uns(mode, v) * to_uns(mode, &x); break;
                    case '/': *value = to_uns(mode, v) / to_uns(mode, &x); break;
                    case '%': *value = to_uns(mode, v) % to_uns(mode, &x); break;
                    }
                }
                v->type_class = TYPE_CLASS_CARDINAL;
                v->size = sizeof(uint64_t);
                v->value = value;
            }
            else {
                int64_t * value = alloc_str(sizeof(int64_t));
                if (mode == MODE_NORMAL) {
                    switch (sy) {
                    case '*': *value = to_int(mode, v) * to_int(mode, &x); break;
                    case '/': *value = to_int(mode, v) / to_int(mode, &x); break;
                    case '%': *value = to_int(mode, v) % to_int(mode, &x); break;
                    }
                }
                v->type_class = TYPE_CLASS_INTEGER;
                v->size = sizeof(int64_t);
                v->value = value;
            }
            v->remote = 0;
            v->constant = v->constant && x.constant;
            memset(&v->type, 0, sizeof(Symbol));
        }
    }
}

static void additive_expression(int mode, Value * v) {
    multiplicative_expression(mode, v);
    while (text_sy == '+' || text_sy == '-') {
        Value x;
        int sy = text_sy;
        next_sy();
        multiplicative_expression(mode, &x);
        if (mode != MODE_SKIP) {
            if (sy == '+' && v->type_class == TYPE_CLASS_ARRAY && x.type_class == TYPE_CLASS_ARRAY) {
                if (mode == MODE_TYPE) {
                    v->size = 0;
                    v->value = alloc_str(v->size);
                }
                else {
                    char * value;
                    load_value(v);
                    load_value(&x);
                    v->size = strlen((char *)v->value) + strlen((char *)x.value) + 1;
                    value = alloc_str(v->size);
                    strcpy(value, v->value);
                    strcat(value, x.value);
                    v->value = value;
                }
            }
            else if (!is_number(v) || !is_number(&x)) {
                error(ERR_INV_EXPRESSION, "Numeric types expected");
            }
            else if (v->type_class == TYPE_CLASS_REAL || x.type_class == TYPE_CLASS_REAL) {
                double * value = alloc_str(sizeof(double));
                switch (sy) {
                case '+': *value = to_double(mode, v) + to_double(mode, &x); break;
                case '-': *value = to_double(mode, v) - to_double(mode, &x); break;
                }
                v->type_class = TYPE_CLASS_REAL;
                v->size = sizeof(double);
                v->value = value;
            }
            else if (v->type_class == TYPE_CLASS_CARDINAL || x.type_class == TYPE_CLASS_CARDINAL) {
                uint64_t * value = alloc_str(sizeof(uint64_t));
                switch (sy) {
                case '+': *value = to_uns(mode, v) + to_uns(mode, &x); break;
                case '-': *value = to_uns(mode, v) - to_uns(mode, &x); break;
                }
                v->type_class = TYPE_CLASS_CARDINAL;
                v->size = sizeof(uint64_t);
                v->value = value;
            }
            else {
                int64_t * value = alloc_str(sizeof(int64_t));
                switch (sy) {
                case '+': *value = to_int(mode, v) + to_int(mode, &x); break;
                case '-': *value = to_int(mode, v) - to_int(mode, &x); break;
                }
                v->type_class = TYPE_CLASS_INTEGER;
                v->size = sizeof(int64_t);
                v->value = value;
            }
            v->remote = 0;
            v->constant = v->constant && x.constant;
            memset(&v->type, 0, sizeof(Symbol));
        }
    }
}

static void shift_expression(int mode, Value * v) {
    additive_expression(mode, v);
    while (text_sy == SY_SHL || text_sy == SY_SHR) {
        Value x;
        int sy = text_sy;
        next_sy();
        additive_expression(mode, &x);
        if (mode != MODE_SKIP) {
            uint64_t * value = alloc_str(sizeof(uint64_t));
            if (!is_whole_number(v) || !is_whole_number(&x)) {
                error(ERR_INV_EXPRESSION, "Integral types expected");
            }
            if (x.type_class != TYPE_CLASS_CARDINAL && to_int(mode, &x) < 0) {
                if (v->type_class == TYPE_CLASS_CARDINAL) {
                    switch (sy) {
                    case SY_SHL: *value = to_uns(mode, v) >> -to_int(mode, &x); break;
                    case SY_SHR: *value = to_uns(mode, v) << -to_int(mode, &x); break;
                    }
                }
                else {
                    switch (sy) {
                    case SY_SHL: *value = to_int(mode, v) >> -to_int(mode, &x); break;
                    case SY_SHR: *value = to_int(mode, v) << -to_int(mode, &x); break;
                    }
                    v->type_class = TYPE_CLASS_INTEGER;
                }
            }
            else {
                if (v->type_class == TYPE_CLASS_CARDINAL) {
                    switch (sy) {
                    case SY_SHL: *value = to_uns(mode, v) << to_uns(mode, &x); break;
                    case SY_SHR: *value = to_uns(mode, v) >> to_uns(mode, &x); break;
                    }
                }
                else {
                    switch (sy) {
                    case SY_SHL: *value = to_int(mode, v) << to_uns(mode, &x); break;
                    case SY_SHR: *value = to_int(mode, v) >> to_uns(mode, &x); break;
                    }
                    v->type_class = TYPE_CLASS_INTEGER;
                }
            }
            v->value = value;
            v->size = sizeof(uint64_t);
            v->remote = 0;
            v->constant = v->constant && x.constant;
            memset(&v->type, 0, sizeof(Symbol));
        }
    }
}

static void relational_expression(int mode, Value * v) {
    shift_expression(mode, v);
    while (text_sy == '<' || text_sy == '>' || text_sy == SY_LEQ || text_sy == SY_GEQ) {
        Value x;
        int sy = text_sy;
        next_sy();
        shift_expression(mode, &x);
        if (mode != MODE_SKIP) {
            int * value = alloc_str(sizeof(int));
            if (v->type_class == TYPE_CLASS_ARRAY && x.type_class == TYPE_CLASS_ARRAY) {
                int n = 0;
                load_value(v);
                load_value(&x);
                n = strcmp((char *)v->value, (char *)x.value);
                switch (sy) {
                case '<': *value = n < 0; break;
                case '>': *value = n > 0; break;
                case SY_LEQ: *value = n <= 0; break;
                case SY_GEQ: *value = n >= 0; break;
                }
            }
            else if (v->type_class == TYPE_CLASS_REAL || x.type_class == TYPE_CLASS_REAL) {
                switch (sy) {
                case '<': *value = to_double(mode, v) < to_double(mode, &x); break;
                case '>': *value = to_double(mode, v) > to_double(mode, &x); break;
                case SY_LEQ: *value = to_double(mode, v) <= to_double(mode, &x); break;
                case SY_GEQ: *value = to_double(mode, v) >= to_double(mode, &x); break;
                }
            }
            else if (v->type_class == TYPE_CLASS_CARDINAL || x.type_class == TYPE_CLASS_CARDINAL) {
                switch (sy) {
                case '<': *value = to_uns(mode, v) < to_uns(mode, &x); break;
                case '>': *value = to_uns(mode, v) > to_uns(mode, &x); break;
                case SY_LEQ: *value = to_uns(mode, v) <= to_uns(mode, &x); break;
                case SY_GEQ: *value = to_uns(mode, v) >= to_uns(mode, &x); break;
                }
            }
            else {
                switch (sy) {
                case '<': *value = to_int(mode, v) < to_int(mode, &x); break;
                case '>': *value = to_int(mode, v) > to_int(mode, &x); break;
                case SY_LEQ: *value = to_int(mode, v) <= to_int(mode, &x); break;
                case SY_GEQ: *value = to_int(mode, v) >= to_int(mode, &x); break;
                }
            }
            if (mode != MODE_NORMAL) *value = 0;
            v->type_class = TYPE_CLASS_INTEGER;
            v->value = value;
            v->size = sizeof(int);
            v->remote = 0;
            v->constant = v->constant && x.constant;
            memset(&v->type, 0, sizeof(Symbol));
        }
    }
}

static void equality_expression(int mode, Value * v) {
    relational_expression(mode, v);
    while (text_sy == SY_EQU || text_sy == SY_NEQ) {
        Value x;
        int sy = text_sy;
        next_sy();
        relational_expression(mode, &x);
        if (mode != MODE_SKIP) {
            int * value = alloc_str(sizeof(int));
            if (v->type_class == TYPE_CLASS_ARRAY && x.type_class == TYPE_CLASS_ARRAY) {
                load_value(v);
                load_value(&x);
                *value = strcmp((char *)v->value, (char *)x.value) == 0;
            }
            else if (v->type_class == TYPE_CLASS_REAL || x.type_class == TYPE_CLASS_REAL) {
                *value = to_double(mode, v) == to_double(mode, &x); break;
            }
            else {
                *value = to_int(mode, v) == to_int(mode, &x);
            }
            if (sy == SY_NEQ) *value = !*value;
            if (mode != MODE_NORMAL) *value = 0;
            v->type_class = TYPE_CLASS_INTEGER;
            v->value = value;
            v->size = sizeof(int);
            v->remote = 0;
            v->constant = v->constant && x.constant;
            memset(&v->type, 0, sizeof(Symbol));
        }
    }
}

static void and_expression(int mode, Value * v) {
    equality_expression(mode, v);
    while (text_sy == '&') {
        Value x;
        next_sy();
        equality_expression(mode, &x);
        if (mode != MODE_SKIP) {
            int64_t * value = alloc_str(sizeof(int64_t));
            if (!is_whole_number(v) || !is_whole_number(&x)) {
                error(ERR_INV_EXPRESSION, "Integral types expected");
            }
            *value = to_int(mode, v) & to_int(mode, &x);
            if (v->type_class == TYPE_CLASS_CARDINAL || x.type_class == TYPE_CLASS_CARDINAL) {
                v->type_class = TYPE_CLASS_CARDINAL;
            }
            else {
                v->type_class = TYPE_CLASS_INTEGER;
            }
            if (mode != MODE_NORMAL) *value = 0;
            v->value = value;
            v->size = sizeof(int64_t);
            v->remote = 0;
            v->constant = v->constant && x.constant;
            memset(&v->type, 0, sizeof(Symbol));
        }
    }
}

static void exclusive_or_expression(int mode, Value * v) {
    and_expression(mode, v);
    while (text_sy == '^') {
        Value x;
        next_sy();
        and_expression(mode, &x);
        if (mode != MODE_SKIP) {
            int64_t * value = alloc_str(sizeof(int64_t));
            if (!is_whole_number(v) || !is_whole_number(&x)) {
                error(ERR_INV_EXPRESSION, "Integral types expected");
            }
            *value = to_int(mode, v) ^ to_int(mode, &x);
            if (v->type_class == TYPE_CLASS_CARDINAL || x.type_class == TYPE_CLASS_CARDINAL) {
                v->type_class = TYPE_CLASS_CARDINAL;
            }
            else {
                v->type_class = TYPE_CLASS_INTEGER;
            }
            if (mode != MODE_NORMAL) *value = 0;
            v->value = value;
            v->size = sizeof(int64_t);
            v->remote = 0;
            v->constant = v->constant && x.constant;
            memset(&v->type, 0, sizeof(Symbol));
        }
    }
}

static void inclusive_or_expression(int mode, Value * v) {
    exclusive_or_expression(mode, v);
    while (text_sy == '|') {
        Value x;
        next_sy();
        exclusive_or_expression(mode, &x);
        if (mode != MODE_SKIP) {
            int64_t * value = alloc_str(sizeof(int64_t));
            if (!is_whole_number(v) || !is_whole_number(&x)) {
                error(ERR_INV_EXPRESSION, "Integral types expected");
            }
            *value = to_int(mode, v) | to_int(mode, &x);
            if (v->type_class == TYPE_CLASS_CARDINAL || x.type_class == TYPE_CLASS_CARDINAL) {
                v->type_class = TYPE_CLASS_CARDINAL;
            }
            else {
                v->type_class = TYPE_CLASS_INTEGER;
            }
            if (mode != MODE_NORMAL) *value = 0;
            v->value = value;
            v->size = sizeof(int64_t);
            v->remote = 0;
            v->constant = v->constant && x.constant;
            memset(&v->type, 0, sizeof(Symbol));
        }
    }
}

static void logical_and_expression(int mode, Value * v) {
    inclusive_or_expression(mode, v);
    while (text_sy == SY_AND) {
        Value x;
        int b = to_boolean(mode, v);
        next_sy();
        inclusive_or_expression(b ? mode : MODE_SKIP, &x);
        if (b) {
            if (!v->constant) x.constant = 0;
            *v = x;
        }
    }
}

static void logical_or_expression(int mode, Value * v) {
    logical_and_expression(mode, v);
    while (text_sy == SY_OR) {
        Value x;
        int b = to_boolean(mode, v);
        next_sy();
        logical_and_expression(!b ? mode : MODE_SKIP, &x);
        if (!b) {
            if (!v->constant) x.constant = 0;
            *v = x;
        }
    }
}

static void conditional_expression(int mode, Value * v) {
    logical_or_expression(mode, v);
    if (text_sy == '?') {
        Value x;
        Value y;
        int b = to_boolean(mode, v);
        next_sy();
        expression(b ? mode : MODE_SKIP, &x);
        if (text_sy != ':') error(ERR_INV_EXPRESSION, "Missing ':'");
        next_sy();
        conditional_expression(!b ? mode : MODE_SKIP, &y);
        if (!v->constant) x.constant = y.constant = 0;
        *v = b ? x : y;
    }
}

static void expression(int mode, Value * v) {
    /* TODO: assignments in expressions */
    conditional_expression(mode, v);
}

static int evaluate_type(Context * ctx, int frame, char * s, Value * v) {
    Trap trap;

    expression_context = ctx;
    expression_frame = frame;
    if (set_trap(&trap)) {
        str_pool_cnt = 0;
        while (str_alloc_list != NULL) {
            StringValue * str = str_alloc_list;
            str_alloc_list = str->next;
            loc_free(str);
        }
        text = s;
        text_pos = 0;
        text_len = strlen(s) + 1;
        next_ch();
        next_sy();
        expression(MODE_TYPE, v);
        if (text_sy != 0) error(ERR_INV_EXPRESSION, "Illegal characters at the end of expression");
        clear_trap(&trap);
        return 0;
    }
    return -1;
}

int evaluate_expression(Context * ctx, int frame, char * s, int load, Value * v) {
    Trap trap;

    expression_context = ctx;
    expression_frame = frame;
    if (set_trap(&trap)) {
        str_pool_cnt = 0;
        while (str_alloc_list != NULL) {
            StringValue * str = str_alloc_list;
            str_alloc_list = str->next;
            loc_free(str);
        }
        text = s;
        text_pos = 0;
        text_len = strlen(s) + 1;
        next_ch();
        next_sy();
        expression(MODE_NORMAL, v);
        if (text_sy != 0) error(ERR_INV_EXPRESSION, "Illegal characters at the end of expression");
        if (load) load_value(v);
        clear_trap(&trap);
        return 0;
    }
    return -1;
}

int value_to_boolean(Value * v) {
    /* TODO: error handling */
    int r = 0;
    Trap trap;
    if (set_trap(&trap)) {
        r = to_boolean(MODE_NORMAL, v);
        clear_trap(&trap);
    }
    return r;
}

ContextAddress value_to_address(Value * v) {
    /* TODO: error handling */
    ContextAddress r = 0;
    Trap trap;
    if (set_trap(&trap)) {
        r = (ContextAddress)to_uns(MODE_NORMAL, v);
        clear_trap(&trap);
    }
    return r;
}

typedef struct Expression Expression;

struct Expression {
    LINK link_all;
    LINK link_id;
    char id[256];
    char parent[256];
    char language[256];
    Channel * channel;
    char * script;
    int can_assign;
    size_t size;
    int type_class;
    char type[256];
};

#define link_all2exp(A)  ((Expression *)((char *)(A) - offsetof(Expression, link_all)))
#define link_id2exp(A)   ((Expression *)((char *)(A) - offsetof(Expression, link_id)))

#define ID2EXP_HASH_SIZE 1023

static LINK expressions;
static LINK id2exp[ID2EXP_HASH_SIZE];

#define MAX_SYM_NAME 1024
#define BUF_SIZE 256

static const char * EXPRESSIONS = "Expressions";
static unsigned expr_id_cnt = 0;

#define expression_hash(id) ((unsigned)atoi(id + 4) % ID2EXP_HASH_SIZE)

static Expression * find_expression(char * id) {
    if (id[0] == 'E' && id[1] == 'X' && id[2] == 'P' && id[3] == 'R') {
        unsigned hash = expression_hash(id);
        LINK * l = id2exp[hash].next;
        while (l != &id2exp[hash]) {
            Expression * e = link_id2exp(l);
            l = l->next;
            if (strcmp(e->id, id) == 0) return e;
        }
    }
    return NULL;
}

static int expression_context_id(char * id, char * parent, Context ** ctx, int * frame, char * name, Expression ** expr) {
    int err = 0;
    Expression * e = NULL;

    if (id[0] == 'S') {
        char * s = id + 1;
        int i = 0;
        while (*s && i < MAX_SYM_NAME - 1) {
            char ch = *s++;
            if (ch == '.') {
                if (*s == '.') {
                    name[i++] = '.';
                    continue;
                }
                break;
            }
            name[i++] = ch;
        }
        name[i] = 0;
        strcpy(parent, s);
        *expr = NULL;
    }
    else if ((e = find_expression(id)) != NULL) {
        name[0] = 0;
        strcpy(parent, e->parent);
        *expr = e;
    }
    else {
        err = ERR_INV_CONTEXT;
    }
    if (!err) {
        if ((*ctx = id2ctx(parent)) != NULL) {
            *frame = STACK_TOP_FRAME;
        }
        else if (is_stack_frame_id(parent, ctx, frame)) {
            /* OK */
        }
        else {
            err = ERR_INV_CONTEXT;
        }
    }
    if (err) {
        errno = err;
        return -1;
    }
    return 0;
}

static void write_context(OutputStream * out, char * id, char * parent, int frame, char * name, Symbol * sym, Expression * expr) {
    write_stream(out, '{');
    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, id);

    write_stream(out, ',');

    json_write_string(out, "ParentID");
    write_stream(out, ':');
    json_write_string(out, parent);

    if (expr || name) {
        write_stream(out, ',');

        json_write_string(out, "Expression");
        write_stream(out, ':');
        json_write_string(out, expr ? expr->script : name);
    }

    if (expr) {
        write_stream(out, ',');

        json_write_string(out, "CanAssign");
        write_stream(out, ':');
        json_write_boolean(out, expr->can_assign);

        if (expr->type_class != TYPE_CLASS_UNKNOWN) {
            write_stream(out, ',');

            json_write_string(out, "Class");
            write_stream(out, ':');
            json_write_long(out, expr->type_class);
        }

        if (expr->type[0]) {
            write_stream(out, ',');

            json_write_string(out, "Type");
            write_stream(out, ':');
            json_write_string(out, expr->type);
        }

        write_stream(out, ',');

        json_write_string(out, "Size");
        write_stream(out, ':');
        json_write_long(out, expr->size);
    }
    else if (sym) {
        Symbol type;
        int type_class;
        size_t size;

        write_stream(out, ',');

        json_write_string(out, "CanAssign");
        write_stream(out, ':');
        json_write_boolean(out, sym->sym_class == SYM_CLASS_REFERENCE);

        if (get_symbol_type_class(sym, &type_class) == 0 && type_class != TYPE_CLASS_UNKNOWN) {
            write_stream(out, ',');

            json_write_string(out, "Class");
            write_stream(out, ':');
            json_write_long(out, type_class);
        }

        if (get_symbol_type(sym, &type) == 0) {
            write_stream(out, ',');

            json_write_string(out, "Type");
            write_stream(out, ':');
            json_write_string(out, symbol2id(&type));
        }

        if (get_symbol_size(sym, frame, &size) == 0) {
            write_stream(out, ',');

            json_write_string(out, "Size");
            write_stream(out, ':');
            json_write_long(out, size);
        }
    }

    write_stream(out, '}');
}

static void command_get_context(char * token, Channel * c) {
    int err = 0;
    char id[256];
    char parent[256];
    char name[MAX_SYM_NAME];
    Context * ctx = NULL;
    int frame = STACK_NO_FRAME;
    Expression * expr = NULL;
    Symbol sym;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (expression_context_id(id, parent, &ctx, &frame, name, &expr) < 0) err = errno;

    memset(&sym, 0, sizeof(Symbol));
    if (!err && expr == NULL) {
#if SERVICE_Symbols
        if (find_symbol(ctx, frame, name, &sym) < 0) err = errno;
#else
        err = ERR_INV_CONTEXT;
#endif
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);

    if (err) {
        write_stringz(&c->out, "null");
    }
    else {
        write_context(&c->out, id, parent, frame, name, &sym, expr);
        write_stream(&c->out, 0);
    }

    write_stream(&c->out, MARKER_EOM);
}

#if SERVICE_Symbols

typedef struct GetChildrenContext {
    Channel * channel;
    char id[256];
    int cnt;
} GetChildrenContext;

static void get_children_callback(void * x, char * name, Symbol * symbol) {
    GetChildrenContext * args = (GetChildrenContext *)x;
    Channel * c = args->channel;
    char * s;

    if (args->cnt == 0) {
        write_errno(&c->out, 0);
        write_stream(&c->out, '[');
    }
    else {
        write_stream(&c->out, ',');
    }
    write_stream(&c->out, '"');
    write_stream(&c->out, 'S');
    s = name;
    while (*s) {
        if (*s == '.') write_stream(&c->out, '.');
        json_write_char(&c->out, *s++);
    }
    write_stream(&c->out, '.');
    s = args->id;
    while (*s) json_write_char(&c->out, *s++);
    write_stream(&c->out, '"');
    args->cnt++;
}

#endif

static void command_get_children(char * token, Channel * c) {
    char id[256];

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    /* TODO: Expressions.getChildren - structures */
#if SERVICE_Symbols
    {
        Context * ctx;
        int frame = STACK_NO_FRAME;
        GetChildrenContext args;
        int err = 0;

        args.cnt = 0;
        args.channel = c;
        strncpy(args.id, id, sizeof(args.id) - 1);
        args.id[sizeof(args.id) - 1] = 0;

        if ((ctx = id2ctx(id)) != NULL) {
            if (context_has_state(ctx)) {
                char * frame_id = get_stack_frame_id(ctx, STACK_TOP_FRAME);
                if (frame_id == NULL) {
                    err = errno;
                }
                else {
                    frame = STACK_TOP_FRAME;
                    strncpy(args.id, frame_id, sizeof(args.id) - 1);
                    args.id[sizeof(args.id) - 1] = 0;
                }
            }
        }
        else if (is_stack_frame_id(id, &ctx, &frame)) {
            /* OK */
        }
        else {
            ctx = NULL;
        }

        if (ctx != NULL && err == 0 && enumerate_symbols(
                ctx, frame, get_children_callback, &args) < 0) err = errno;

        if (args.cnt == 0) {
            write_errno(&c->out, err);
            write_stream(&c->out, '[');
        }
    }
#else
    write_errno(&c->out, ERR_UNSUPPORTED);
    write_stream(&c->out, '[');
#endif
    write_stream(&c->out, ']');
    write_stream(&c->out, 0);

    write_stream(&c->out, MARKER_EOM);
}

static void command_create(char * token, Channel * c) {
    char parent[256];
    char language[256];
    char * script;
    int err = 0;
    int frame = STACK_NO_FRAME;
    Expression * e;

    json_read_string(&c->inp, parent, sizeof(parent));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(&c->inp, language, sizeof(language));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    script = json_read_alloc_string(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    e = (Expression *)loc_alloc_zero(sizeof(Expression));
    do snprintf(e->id, sizeof(e->id), "EXPR%d", expr_id_cnt++);
    while (find_expression(e->id) != NULL);
    strncpy(e->parent, parent, sizeof(e->parent) - 1);
    strncpy(e->language, language, sizeof(e->language) - 1);
    e->channel = c;
    e->script = script;

    if (!err) {
        Context * ctx = NULL;
        Value value;
        memset(&value, 0, sizeof(value));
        if ((ctx = id2ctx(parent)) != NULL) {
            frame = STACK_TOP_FRAME;
        }
        else if (is_stack_frame_id(parent, &ctx, &frame)) {
            /* OK */
        }
        else {
            err = ERR_INV_CONTEXT;
        }
        if (!err && evaluate_type(ctx, frame, script, &value) < 0) err = errno;
        if (!err) {
            e->can_assign = value.remote;
            e->type_class = value.type_class;
            e->size = value.size;
            if (value.type.ctx != NULL) strncpy(e->type, symbol2id(&value.type), sizeof(e->type) - 1);
        }
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);

    if (err) {
        loc_free(e);
        write_stringz(&c->out, "null");
    }
    else {
        list_add_last(&e->link_all, &expressions);
        list_add_last(&e->link_id, id2exp + expression_hash(e->id));
        write_context(&c->out, e->id, parent, frame, NULL, NULL, e);
        write_stream(&c->out, 0);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_evaluate(char * token, Channel * c) {
    int err = 0;
    int value_ok = 0;
    char id[256];
    char parent[256];
    char name[MAX_SYM_NAME];
    Context * ctx;
    int frame;
    Expression * expr = NULL;
    Value value;

    memset(&value, 0, sizeof(value));
    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (expression_context_id(id, parent, &ctx, &frame, name, &expr) < 0) err = errno;
    if (!err && evaluate_expression(ctx, frame, expr ? expr->script : name, 0, &value) < 0) err = errno;
    if (value.size >= 0x100000) err = ERR_BUFFER_OVERFLOW;

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    if (err) {
        write_stringz(&c->out, "null");
    }
    else {
        JsonWriteBinaryState state;

        value_ok = 1;
        json_write_binary_start(&state, &c->out, value.size);
        if (!value.remote) {
            json_write_binary_data(&state, value.value, value.size);
        }
        else {
            char buf[256];
            size_t offs = 0;
            while (offs < value.size) {
                int size = value.size - offs;
                if (size > sizeof(buf)) size = sizeof(buf);
                if (!err) {
                    if (context_read_mem(ctx, value.address + offs, buf, size) < 0) err = errno;
                    else check_breakpoints_on_memory_read(ctx, value.address + offs, buf, size);
                }
                json_write_binary_data(&state, buf, size);
                offs += size;
            }
        }
        json_write_binary_end(&state);
        write_stream(&c->out, 0);
    }
    write_errno(&c->out, err);
    if (!value_ok) {
        write_stringz(&c->out, "null");
    }
    else {
        int cnt = 0;
        write_stream(&c->out, '{');

        if (value.type_class != TYPE_CLASS_UNKNOWN) {
            json_write_string(&c->out, "Class");
            write_stream(&c->out, ':');
            json_write_long(&c->out, value.type_class);
            cnt++;
        }

        if (value.type.ctx != NULL) {
            if (cnt > 0) write_stream(&c->out, ',');
            json_write_string(&c->out, "Type");
            write_stream(&c->out, ':');
            json_write_string(&c->out, symbol2id(&value.type));
            write_stream(&c->out, ',');
            json_write_string(&c->out, "ExeID");
            write_stream(&c->out, ':');
            json_write_string(&c->out, container_id(value.type.ctx));
            cnt++;
        }

        write_stream(&c->out, '}');
        write_stream(&c->out, 0);
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_assign(char * token, Channel * c) {
    char id[256];
    int err = 0;
    char parent[256];
    char name[MAX_SYM_NAME];
    Context * ctx;
    int frame;
    Expression * expr = NULL;
    Value value;
    JsonReadBinaryState state;
    char buf[BUF_SIZE];
    unsigned long size = 0;
    ContextAddress addr;
    ContextAddress addr0;
    unsigned long size0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);

    memset(&value, 0, sizeof(value));
    if (expression_context_id(id, parent, &ctx, &frame, name, &expr) < 0) err = errno;
    if (!err && evaluate_expression(ctx, frame, expr ? expr->script : name, 0, &value) < 0) err = errno;

    addr0 = value.address;
    size0 = value.size;
    addr = addr0;

    json_read_binary_start(&state, &c->inp);
    for (;;) {
        int rd = json_read_binary_data(&state, buf, sizeof(buf));
        if (rd == 0) break;
        if (err == 0) {
            if (value.remote) {
                check_breakpoints_on_memory_write(ctx, addr, buf, rd);
                if (context_write_mem(ctx, addr, buf, rd) < 0) {
                    err = errno;
                }
                else {
                    addr += rd;
                }
            }
            else {
                err = ERR_UNSUPPORTED;
            }
        }
        size += rd;
    }
    json_read_binary_end(&state);

    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void command_dispose(char * token, Channel * c) {
    char id[256];
    int err = 0;
    Expression * e;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    e = find_expression(id);
    if (e != NULL) {
        list_remove(&e->link_all);
        list_remove(&e->link_id);
        loc_free(e->script);
        loc_free(e);
    }
    else {
        err = ERR_INV_CONTEXT;
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
}

static void on_channel_close(Channel * c) {
    LINK * l = expressions.next;
    while (l != &expressions) {
        Expression * e = link_all2exp(l);
        l = l->next;
        if (e->channel == c) {
            list_remove(&e->link_all);
            list_remove(&e->link_id);
            loc_free(e->script);
            loc_free(e);
        }
    }
}

void add_identifier_callback(ExpressionIdentifierCallBack * callback) {
    assert(id_callback_cnt < MAX_ID_CALLBACKS);
    id_callbacks[id_callback_cnt++] = callback;
}

void ini_expressions_service(Protocol * proto) {
    unsigned i;
    list_init(&expressions);
    for (i = 0; i < ID2EXP_HASH_SIZE; i++) list_init(id2exp + i);
    add_channel_close_listener(on_channel_close);
    add_command_handler(proto, EXPRESSIONS, "getContext", command_get_context);
    add_command_handler(proto, EXPRESSIONS, "getChildren", command_get_children);
    add_command_handler(proto, EXPRESSIONS, "create", command_create);
    add_command_handler(proto, EXPRESSIONS, "evaluate", command_evaluate);
    add_command_handler(proto, EXPRESSIONS, "assign", command_assign);
    add_command_handler(proto, EXPRESSIONS, "dispose", command_dispose);
}

#endif  /* if SERVICE_Expressions */
