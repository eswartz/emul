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
 * Expression evaluation service.
 */

#include <config.h>

#if SERVICE_Expressions

#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <framework/json.h>
#include <framework/cache.h>
#include <framework/context.h>
#include <services/symbols.h>
#include <services/stacktrace.h>
#include <services/expressions.h>
#include <main/test.h>

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
    if (data == NULL) memset(v->value, 0, v->size);
    else memcpy(v->value, data, v->size);
}

static void set_int_value(Value * v, uint64_t n) {
    v->remote = 0;
    v->value = alloc_str(v->size);
    switch (v->size) {
    case 1: *(uint8_t *)v->value = (uint8_t)n; break;
    case 2: *(uint16_t *)v->value = (uint16_t)n; break;
    case 4: *(uint32_t *)v->value = (uint32_t)n; break;
    case 8: *(uint64_t *)v->value = n; break;
    default: assert(0);
    }
}

static void set_ctx_word_value(Value * v, ContextAddress data) {
    v->address = 0;
    v->size = context_word_size(expression_context);
    set_int_value(v, data);
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

static void error(int no, const char * fmt, ...) {
    va_list ap;
    char buf[256];
    size_t l = 0;

    va_start(ap, fmt);
    l = snprintf(buf, sizeof(buf), "At col %d: ", text_pos);
    vsnprintf(buf + l, sizeof(buf) - l, fmt, ap);
    va_end(ap);
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
                if (strcmp((const char *)text_val.value, "sizeof") == 0) text_sy = (int)SY_SIZEOF;
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
        string_value(v, expression_context->id);
        v->constant = 1;
        return SYM_CLASS_VALUE;
    }
#if ENABLE_Symbols
    {
        Symbol * sym = NULL;
        if (find_symbol(expression_context, expression_frame, name, &sym) < 0) {
            if (get_error_code(errno) != ERR_SYM_NOT_FOUND) error(errno, "Cannot read symbol data");
        }
        else {
            int sym_class = 0;
            if (get_symbol_class(sym, &sym_class) < 0) {
                error(errno, "Cannot retrieve symbol class");
            }
            if (get_symbol_type(sym, &v->type) < 0) {
                error(errno, "Cannot retrieve symbol type");
            }
            if (get_symbol_type_class(sym, &v->type_class) < 0) {
                error(errno, "Cannot retrieve symbol type class");
            }
            switch (sym_class) {
            case SYM_CLASS_VALUE:
                {
                    size_t size = 0;
                    void * value = NULL;
                    if (get_symbol_value(sym, &value, &size) < 0) {
                        error(errno, "Cannot retrieve symbol value");
                    }
                    v->constant = 1;
                    v->size = size;
                    if (value != NULL) {
                        v->value = alloc_str(v->size);
                        memcpy(v->value, value, size);
                    }
                    else {
                        v->value = NULL;
                    }
                }
                break;
            case SYM_CLASS_REFERENCE:
                v->remote = 1;
                if (get_symbol_size(sym, &v->size) < 0) {
                    error(errno, "Cannot retrieve symbol size");
                }
                if (get_symbol_address(sym, &v->address) < 0) {
                    error(errno, "Cannot retrieve symbol address");
                }
                break;
            case SYM_CLASS_FUNCTION:
                {
                    ContextAddress word = 0;
                    v->type_class = TYPE_CLASS_CARDINAL;
                    if (get_symbol_address(sym, &word) < 0) {
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
            return sym_class;
        }
    }
#elif ENABLE_RCBP_TEST
    {
        void * ptr = NULL;
        int cls = 0;
        if (find_test_symbol(expression_context, name, &ptr, &cls) >= 0) {
            v->type_class = TYPE_CLASS_CARDINAL;
            set_ctx_word_value(v, (ContextAddress)ptr);
            return cls;
        }
    }
#else
    errno = ERR_SYM_NOT_FOUND;
#endif
    return SYM_CLASS_UNKNOWN;
}

static int64_t to_int(int mode, Value * v);
#define TYPE_EXPR_LENGTH 64

static int type_expression(int mode, int * buf) {
    int i = 0;
    int pos = 0;
    int expr_buf[TYPE_EXPR_LENGTH];
    int expr_len = 0;
    while (text_sy == '*') {
        next_sy();
        if (pos >= TYPE_EXPR_LENGTH) error(ERR_BUFFER_OVERFLOW, "Type expression is too long");
        buf[pos++] = 1;
    }
    if (text_sy == '(') {
        next_sy();
        expr_len = type_expression(mode, expr_buf);
        if (text_sy != ')') error(ERR_INV_EXPRESSION, "')' expected");
        next_sy();
    }
    while (text_sy == '[') {
        next_sy();
        if (text_sy != SY_VAL) error(ERR_INV_EXPRESSION, "Number expected");
        if (pos >= TYPE_EXPR_LENGTH) error(ERR_BUFFER_OVERFLOW, "Type expression is too long");
        buf[pos] = (int)to_int(mode, &text_val);
        if (mode == MODE_NORMAL && buf[pos] < 1) error(ERR_INV_EXPRESSION, "Positive number expected");
        pos++;
        next_sy();
        if (text_sy != ']') error(ERR_INV_EXPRESSION, "']' expected");
        next_sy();
    }
    for (i = 0; i < expr_len; i++) {
        if (pos >= TYPE_EXPR_LENGTH) error(ERR_BUFFER_OVERFLOW, "Type expression is too long");
        buf[pos++] = expr_buf[i];
    }
    return pos;
}

static int type_name(int mode, Symbol ** type) {
    Value v;
    int expr_buf[TYPE_EXPR_LENGTH];
    int expr_len = 0;
    char name[256];
    int sym_class;
    int name_cnt = 0;

    if (text_sy != SY_ID) return 0;
    name[0] = 0;
    do {
        if (strlen((const char *)(text_val.value)) + strlen(name) >= sizeof(name) - 1) {
            error(ERR_BUFFER_OVERFLOW, "Type name is too long");
        }
        if (name[0]) strcat(name, " ");
        strcat(name, (const char *)(text_val.value));
        name_cnt++;
        next_sy();
    }
    while (text_sy == SY_ID);
    sym_class = identifier(name, &v);
    if (sym_class != SYM_CLASS_TYPE) return 0;
    expr_len = type_expression(mode, expr_buf);
    if (mode != MODE_SKIP) {
        int i;
        for (i = 0; i < expr_len; i++) {
#if ENABLE_Symbols
            if (expr_buf[i] == 1) {
                if (get_array_symbol(v.type, 0, &v.type)) {
                    error(errno, "Cannot create pointer type");
                }
            }
            else {
                if (get_array_symbol(v.type, expr_buf[i], &v.type)) {
                    error(errno, "Cannot create array type");
                }
            }
#else
            v.type = NULL;
#endif
        }
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
            if (sym_class == SYM_CLASS_UNKNOWN) error(errno, "Undefined identifier '%s'", text_val.value);
            if (sym_class == SYM_CLASS_TYPE) error(ERR_INV_EXPRESSION, "Illegal usage of type '%s'", text_val.value);
        }
        next_sy();
    }
    else {
        error(ERR_INV_EXPRESSION, "Syntax error");
    }
}

static void op_deref(int mode, Value * v) {
    if (mode == MODE_SKIP) return;
#if ENABLE_Symbols
    if (v->type_class != TYPE_CLASS_ARRAY && v->type_class != TYPE_CLASS_POINTER) {
        error(ERR_INV_EXPRESSION, "Array or pointer type expected");
    }
    if (v->type_class == TYPE_CLASS_POINTER) {
        v->address = (ContextAddress)to_uns(mode, v);
        v->remote = 1;
        v->constant = 0;
        v->value = NULL;
    }
    if (get_symbol_base_type(v->type, &v->type) < 0) {
        error(errno, "Cannot retrieve symbol type");
    }
    if (get_symbol_type_class(v->type, &v->type_class) < 0) {
        error(errno, "Cannot retrieve symbol type class");
    }
    if (get_symbol_size(v->type, &v->size) < 0) {
        error(errno, "Cannot retrieve symbol size");
    }
#else
    error(ERR_UNSUPPORTED, "Symbols service not available");
#endif
}

static void op_field(int mode, Value * v) {
#if ENABLE_Symbols
    char * name = (char *)text_val.value;
    if (text_sy != SY_ID) error(ERR_INV_EXPRESSION, "Field name expected");
    next_sy();
    if (mode == MODE_SKIP) return;
    if (v->type_class != TYPE_CLASS_COMPOSITE) {
        error(ERR_INV_EXPRESSION, "Composite type expected");
    }
    else {
        Symbol * sym = NULL;
        int sym_class = 0;
        ContextAddress size = 0;
        ContextAddress offs = 0;
        Symbol ** children = NULL;
        int count = 0;
        int i;

        if (get_symbol_children(v->type, &children, &count) < 0) {
            error(errno, "Cannot retrieve field list");
        }
        for (i = 0; i < count; i++) {
            char * s = NULL;
            if (get_symbol_name(children[i], &s) < 0) {
                error(errno, "Cannot retrieve field name");
            }
            if (s != NULL && strcmp(s, name) == 0) {
                sym = children[i];
                break;
            }
        }
        if (sym == NULL) {
            error(ERR_SYM_NOT_FOUND, "Symbol not found");
        }
        if (get_symbol_class(sym, &sym_class) < 0) {
            error(errno, "Cannot retrieve symbol class");
        }
        if (sym_class != SYM_CLASS_REFERENCE) {
            error(ERR_UNSUPPORTED, "Invalid symbol class");
        }
        if (get_symbol_size(sym, &size) < 0) {
            error(errno, "Cannot retrieve field size");
        }
        if (get_symbol_offset(sym, &offs) < 0) {
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
        if (get_symbol_type(sym, &v->type) < 0) {
            error(errno, "Cannot retrieve symbol type");
        }
        if (get_symbol_type_class(sym, &v->type_class) < 0) {
            error(errno, "Cannot retrieve symbol type class");
        }
    }
#else
    error(ERR_UNSUPPORTED, "Symbols service not available");
#endif
}

static void op_index(int mode, Value * v) {
#if ENABLE_Symbols
    Value i;
    int64_t lower_bound = 0;
    ContextAddress offs = 0;
    ContextAddress size = 0;
    Symbol * type = NULL;

    expression(mode, &i);
    if (mode == MODE_SKIP) return;

    if (v->type_class != TYPE_CLASS_ARRAY && v->type_class != TYPE_CLASS_POINTER) {
        error(ERR_INV_EXPRESSION, "Array or pointer expected");
    }
    if (v->type_class == TYPE_CLASS_POINTER) {
        v->address = (ContextAddress)to_uns(mode, v);
        v->remote = 1;
        v->constant = 0;
        v->value = NULL;
    }
    if (get_symbol_base_type(v->type, &type) < 0) {
        error(errno, "Cannot get array element type");
    }
    if (get_symbol_size(type, &size) < 0) {
        error(errno, "Cannot get array element type");
    }
    if (get_symbol_lower_bound(v->type, &lower_bound) < 0) {
        error(errno, "Cannot get array lower bound");
    }
    offs = (ContextAddress)(to_int(mode, &i) - lower_bound) * size;
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
    if (get_symbol_type_class(type, &v->type_class) < 0) {
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
#if ENABLE_Symbols
    if (get_array_symbol(v->type, 0, &v->type)) {
        error(errno, "Cannot get pointer type");
    }
#else
    v->type = NULL;
#endif
}

static void unary_expression(int mode, Value * v);

static void op_sizeof(int mode, Value * v) {
    Symbol * type = NULL;
    int pos = 0;
    int p = text_sy == '(';

    if (p) next_sy();
    pos = text_pos - 2;
    if (type_name(mode, &type)) {
        if (mode != MODE_SKIP) {
            ContextAddress type_size = 0;
#if ENABLE_Symbols
            if (get_symbol_size(type, &type_size) < 0) {
                error(errno, "Cannot retrieve symbol size");
            }
#endif
            set_ctx_word_value(v, type_size);
            v->type = NULL;
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
            v->type = NULL;
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
                double * value = (double *)alloc_str(sizeof(double));
                *value = -to_double(mode, v);
                v->type_class = TYPE_CLASS_REAL;
                v->size = sizeof(double);
                v->value = value;
            }
            else if (v->type_class != TYPE_CLASS_CARDINAL) {
                int64_t * value = (int64_t *)alloc_str(sizeof(int64_t));
                *value = -to_int(mode, v);
                v->type_class = TYPE_CLASS_INTEGER;
                v->size = sizeof(int64_t);
                v->value = value;
            }
            assert(!v->remote);
            v->type = NULL;
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
                int * value = (int *)alloc_str(sizeof(int));
                *value = !to_int(mode, v);
                v->type_class = TYPE_CLASS_INTEGER;
                v->size = sizeof(int);
                v->value = value;
            }
            assert(!v->remote);
            v->type = NULL;
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
                int64_t * value = (int64_t *)alloc_str(sizeof(int64_t));
                *value = ~to_int(mode, v);
                v->size = sizeof(int64_t);
                v->value = value;
            }
            assert(!v->remote);
            v->type = NULL;
        }
        break;
    default:
        postfix_expression(mode, v);
        break;
    }
}

static void cast_expression(int mode, Value * v) {
#if ENABLE_Symbols
    if (text_sy == '(') {
        Symbol * type = NULL;
        int type_class = TYPE_CLASS_UNKNOWN;
        ContextAddress type_size = 0;
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
        if (get_symbol_type_class(type, &type_class) < 0) {
            error(errno, "Cannot retrieve symbol type class");
        }
        if (get_symbol_size(type, &type_size) < 0) {
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
                set_int_value(v, value);
            }
            break;
        case TYPE_CLASS_INTEGER:
        case TYPE_CLASS_ENUMERATION:
            {
                int64_t value = to_int(mode, v);
                v->type = type;
                v->type_class = type_class;
                v->size = type_size;
                set_int_value(v, value);
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
        case TYPE_CLASS_ARRAY:
            if (v->type_class == TYPE_CLASS_POINTER) {
                v->address = (ContextAddress)to_uns(mode, v);
                v->type = type;
                v->type_class = type_class;
                v->size = type_size;
                v->remote = 1;
                v->constant = 0;
                v->value = NULL;
            }
            else {
                error(ERR_INV_EXPRESSION, "Invalid type cast: illegal source type");
            }
            break;
        default:
            error(ERR_INV_EXPRESSION, "Invalid type cast: illegal destination type");
            break;
        }
        return;
    }
#endif
    unary_expression(mode, v);
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
                double * value = (double *)alloc_str(sizeof(double));
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
                uint64_t * value = (uint64_t *)alloc_str(sizeof(uint64_t));
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
                int64_t * value = (int64_t *)alloc_str(sizeof(int64_t));
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
            v->type = NULL;
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
                    value = (char *)alloc_str(v->size);
                    strcpy(value, (const char *)(v->value));
                    strcat(value, (const char *)(x.value));
                    v->value = value;
                }
                v->type = NULL;
            }
#if ENABLE_Symbols
            else if (v->type_class == TYPE_CLASS_POINTER && is_number(&x)) {
                uint64_t value = 0;
                Symbol * base = NULL;
                ContextAddress size = 0;
                if (v->type == NULL || get_symbol_base_type(v->type, &base) < 0 ||
                    base == 0 || get_symbol_size(base, &size) < 0 || size == 0) {
                    error(ERR_INV_EXPRESSION, "Unknown pointer base type size");
                }
                switch (sy) {
                case '+': value = to_uns(mode, v) + to_uns(mode, &x) * size; break;
                case '-': value = to_uns(mode, v) - to_uns(mode, &x) * size; break;
                }
                set_int_value(v, value);
            }
#endif
            else if (!is_number(v) || !is_number(&x)) {
                error(ERR_INV_EXPRESSION, "Numeric types expected");
            }
            else if (v->type_class == TYPE_CLASS_REAL || x.type_class == TYPE_CLASS_REAL) {
                double * value = (double *)alloc_str(sizeof(double));
                switch (sy) {
                case '+': *value = to_double(mode, v) + to_double(mode, &x); break;
                case '-': *value = to_double(mode, v) - to_double(mode, &x); break;
                }
                v->type_class = TYPE_CLASS_REAL;
                v->size = sizeof(double);
                v->value = value;
                v->type = NULL;
            }
            else if (v->type_class == TYPE_CLASS_CARDINAL || x.type_class == TYPE_CLASS_CARDINAL) {
                uint64_t * value = (uint64_t *)alloc_str(sizeof(uint64_t));
                switch (sy) {
                case '+': *value = to_uns(mode, v) + to_uns(mode, &x); break;
                case '-': *value = to_uns(mode, v) - to_uns(mode, &x); break;
                }
                v->type_class = TYPE_CLASS_CARDINAL;
                v->size = sizeof(uint64_t);
                v->value = value;
                v->type = NULL;
            }
            else {
                int64_t * value = (int64_t *)alloc_str(sizeof(int64_t));
                switch (sy) {
                case '+': *value = to_int(mode, v) + to_int(mode, &x); break;
                case '-': *value = to_int(mode, v) - to_int(mode, &x); break;
                }
                v->type_class = TYPE_CLASS_INTEGER;
                v->size = sizeof(int64_t);
                v->value = value;
                v->type = NULL;
            }
            v->remote = 0;
            v->constant = v->constant && x.constant;
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
            uint64_t * value = (uint64_t *)alloc_str(sizeof(uint64_t));
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
            v->type = NULL;
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
            int * value = (int *)alloc_str(sizeof(int));
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
            v->type = NULL;
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
            int * value = (int *)alloc_str(sizeof(int));
            if (v->type_class == TYPE_CLASS_ARRAY && x.type_class == TYPE_CLASS_ARRAY) {
                load_value(v);
                load_value(&x);
                *value = strcmp((char *)v->value, (char *)x.value) == 0;
            }
            else if (v->type_class == TYPE_CLASS_REAL || x.type_class == TYPE_CLASS_REAL) {
                *value = to_double(mode, v) == to_double(mode, &x);
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
            v->type = NULL;
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
            int64_t * value = (int64_t *)alloc_str(sizeof(int64_t));
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
            v->type = NULL;
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
            int64_t * value = (int64_t *)alloc_str(sizeof(int64_t));
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
            v->type = NULL;
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
            int64_t * value = (int64_t *)alloc_str(sizeof(int64_t));
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
            v->type = NULL;
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
    if (!set_trap(&trap)) return -1;
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

int evaluate_expression(Context * ctx, int frame, char * s, int load, Value * v) {
    Trap trap;

    expression_context = ctx;
    expression_frame = frame;
    if (!set_trap(&trap)) return -1;
    if (s == NULL || *s == 0) str_exception(ERR_INV_EXPRESSION, "Empty expression");
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

int value_to_boolean(Value * v) {
    /* TODO: error handling in value_to_boolean() */
    int r = 0;
    Trap trap;
    if (!set_trap(&trap)) return 0;
    r = to_boolean(MODE_NORMAL, v);
    clear_trap(&trap);
    return r;
}

ContextAddress value_to_address(Value * v) {
    /* TODO: error handling in value_to_address() */
    ContextAddress r = 0;
    Trap trap;
    if (!set_trap(&trap)) return 0;
    r = (ContextAddress)to_uns(MODE_NORMAL, v);
    clear_trap(&trap);
    return r;
}

/********************** Commands **************************/

typedef struct CommandArgs {
    char token[256];
    char id[256];
} CommandArgs;

typedef struct CommandCreateArgs {
    char token[256];
    char id[256];
    char language[256];
    char * script;
} CommandCreateArgs;

typedef struct CommandAssignArgs {
    char token[256];
    char id[256];
    char * value_buf;
    int value_size;
} CommandAssignArgs;

typedef struct Expression {
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
} Expression;

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

static int symbol_to_expression(char * expr_id, char * parent, char * sym_id, Expression * expr) {
#if ENABLE_Symbols
    Symbol * sym = NULL;
    Symbol * type = NULL;
    char * name = NULL;
    int sym_class = 0;
    int type_class = 0;
    ContextAddress size = 0;
    static char script[256];

    memset(expr, 0, sizeof(Expression));

    strlcpy(expr->id, expr_id, sizeof(expr->id));
    strlcpy(expr->parent, parent, sizeof(expr->parent));

    if (id2symbol(sym_id, &sym) < 0) return -1;
    if (get_symbol_name(sym, &name) < 0) return -1;

    if (name != NULL) {
        strlcpy(script, name, sizeof(script));
        expr->script = script;
    }

    if (get_symbol_class(sym, &sym_class) == 0) {
        expr->can_assign = sym_class == SYM_CLASS_REFERENCE;
    }

    if (get_symbol_type_class(sym, &type_class) == 0) {
        expr->type_class = type_class;
    }

    if (get_symbol_type(sym, &type) == 0 && type != NULL) {
        strlcpy(expr->type, symbol2id(type), sizeof(expr->type));
    }

    if (get_symbol_size(sym, &size) == 0) {
        expr->size = size;
    }
    return 0;
#else
    memset(expr, 0, sizeof(Expression));
    errno = ERR_UNSUPPORTED;
    return -1;
#endif
}

static int expression_context_id(char * id, Context ** ctx, int * frame, Expression ** expr) {
    int err = 0;
    Expression * e = NULL;

    if (id[0] == 'S') {
        static Expression expr_buf;
        char parent[256];
        char * s = id + 1;
        size_t i = 0;
        while (*s && i < sizeof(parent) - 1) {
            char ch = *s++;
            if (ch == '.') {
                if (*s == '.') {
                    parent[i++] = *s++;
                    continue;
                }
                break;
            }
            parent[i++] = ch;
        }
        parent[i] = 0;
        e = &expr_buf;
        if (symbol_to_expression(id, parent, s, e) < 0) err = errno;
    }
    else if ((e = find_expression(id)) == NULL) {
        err = ERR_INV_CONTEXT;
    }

    if (!err) {
        if ((*ctx = id2ctx(e->parent)) != NULL) {
            *frame = context_has_state(*ctx) ? STACK_TOP_FRAME : STACK_NO_FRAME;
        }
        else if (id2frame(e->parent, ctx, frame) < 0) {
            err = errno;
        }
    }

    if (err) {
        errno = err;
        return -1;
    }

    *expr = e;
    return 0;
}

static void write_context(OutputStream * out, Expression * expr) {
    write_stream(out, '{');
    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, expr->id);

    write_stream(out, ',');

    json_write_string(out, "ParentID");
    write_stream(out, ':');
    json_write_string(out, expr->parent);

    write_stream(out, ',');

    json_write_string(out, "Expression");
    write_stream(out, ':');
    json_write_string(out, expr->script);

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

    write_stream(out, '}');
}

static void get_context_cache_client(void * x) {
    CommandArgs * args = (CommandArgs *)x;
    Channel * c = cache_channel();
    Context * ctx = NULL;
    int frame = STACK_NO_FRAME;
    Expression * expr = NULL;
    int err = 0;

    if (expression_context_id(args->id, &ctx, &frame, &expr) < 0) err = errno;

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, err);

    if (err) {
        write_stringz(&c->out, "null");
    }
    else {
        write_context(&c->out, expr);
        write_stream(&c->out, 0);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_context(char * token, Channel * c) {
    CommandArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(get_context_cache_client, c, &args, sizeof(args));
}

#if ENABLE_Symbols

static int sym_cnt = 0;
static int sym_max = 0;
static Symbol ** sym_buf = NULL;

static void get_children_callback(void * x, Symbol * symbol) {
    if (sym_cnt >= sym_max) {
        sym_max += 8;
        sym_buf = (Symbol **)loc_realloc(sym_buf, sizeof(Symbol *) * sym_max);
    }
    sym_buf[sym_cnt++] = symbol;
}

#endif

static void get_children_cache_client(void * x) {
    CommandArgs * args = (CommandArgs *)x;
    Channel * c = cache_channel();
    int err = 0;

    /* TODO: Expressions.getChildren - structures */
#if ENABLE_Symbols
    char parent_id[256];
    {
        Context * ctx;
        int frame = STACK_NO_FRAME;

        sym_cnt = 0;

        if ((ctx = id2ctx(args->id)) != NULL && context_has_state(ctx)) {
            frame = get_top_frame(ctx);
            strlcpy(parent_id, frame2id(ctx, frame), sizeof(parent_id));
        }
        else if (id2frame(args->id, &ctx, &frame) == 0) {
            strlcpy(parent_id, args->id, sizeof(parent_id));
        }
        else {
            ctx = NULL;
        }

        if (ctx != NULL && err == 0 && enumerate_symbols(
                ctx, frame, get_children_callback, &args) < 0) err = errno;
    }
#else
    err = ERR_UNSUPPORTED;
#endif

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);

    write_errno(&c->out, err);

    write_stream(&c->out, '[');
#if ENABLE_Symbols
    {
        int i;
        for (i = 0; i < sym_cnt; i++) {
            const char * s = parent_id;
            if (i > 0) write_stream(&c->out, ',');
            write_stream(&c->out, '"');
            write_stream(&c->out, 'S');
            while (*s) {
                if (*s == '.') write_stream(&c->out, '.');
                json_write_char(&c->out, *s++);
            }
            write_stream(&c->out, '.');
            s = symbol2id(sym_buf[i]);
            while (*s) json_write_char(&c->out, *s++);
            write_stream(&c->out, '"');
        }
    }
#endif
    write_stream(&c->out, ']');
    write_stream(&c->out, 0);

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    CommandArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(get_children_cache_client, c, &args, sizeof(args));
}

static void command_create_cache_client(void * x) {
    CommandCreateArgs * args = (CommandCreateArgs *)x;
    Expression * e;
    Expression buf;
    Channel * c = cache_channel();
    int frame = STACK_NO_FRAME;
    int err = 0;

    memset(e = &buf, 0, sizeof(buf));
    do snprintf(e->id, sizeof(e->id), "EXPR%d", expr_id_cnt++);
    while (find_expression(e->id) != NULL);
    strlcpy(e->parent, args->id, sizeof(e->parent));
    strlcpy(e->language, args->language, sizeof(e->language));
    e->channel = c;
    e->script = args->script;

    if (!err) {
        Value value;
        Context * ctx = NULL;
        memset(&value, 0, sizeof(value));
        if ((ctx = id2ctx(e->parent)) != NULL) {
            frame = context_has_state(ctx) ? STACK_TOP_FRAME : STACK_NO_FRAME;
        }
        else if (id2frame(e->parent, &ctx, &frame) < 0) {
            err = errno;
        }
        if (!err && evaluate_type(ctx, frame, e->script, &value) < 0) err = errno;
        if (!err) {
            e->can_assign = value.remote;
            e->type_class = value.type_class;
            e->size = value.size;
#if ENABLE_Symbols
            if (value.type != NULL) strlcpy(e->type, symbol2id(value.type), sizeof(e->type));
#endif
        }
    }

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, err);

    if (err) {
        write_stringz(&c->out, "null");
    }
    else {
        *(e = (Expression *)loc_alloc(sizeof(Expression))) = buf;
        list_add_last(&e->link_all, &expressions);
        list_add_last(&e->link_id, id2exp + expression_hash(e->id));
        write_context(&c->out, e);
        write_stream(&c->out, 0);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_create(char * token, Channel * c) {
    CommandCreateArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(&c->inp, args.language, sizeof(args.language));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    args.script = json_read_alloc_string(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(command_create_cache_client, c, &args, sizeof(args));
}

static void command_evaluate_cache_client(void * x) {
    CommandCreateArgs * args = (CommandCreateArgs *)x;
    Channel * c = cache_channel();
    Context * ctx = NULL;
    int frame = STACK_NO_FRAME;
    Expression * expr = NULL;
    int value_ok = 0;
    Value value;
    int err = 0;

    memset(&value, 0, sizeof(value));
    if (expression_context_id(args->id, &ctx, &frame, &expr) < 0) err = errno;
    if (!err && frame != STACK_NO_FRAME && !ctx->stopped) err = ERR_IS_RUNNING;
    if (!err && evaluate_expression(ctx, frame, expr->script, 0, &value) < 0) err = errno;
    if (value.size >= 0x100000) err = ERR_BUFFER_OVERFLOW;

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
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
                if (size > (int)sizeof(buf)) size = (int)sizeof(buf);
                if (!err && context_read_mem(ctx, value.address + offs, buf, size) < 0) err = errno;
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

#if ENABLE_Symbols
        if (value.type != NULL) {
            if (cnt > 0) write_stream(&c->out, ',');
            json_write_string(&c->out, "Type");
            write_stream(&c->out, ':');
            json_write_string(&c->out, symbol2id(value.type));
            cnt++;
        }
#endif

        write_stream(&c->out, '}');
        write_stream(&c->out, 0);
    }
    write_stream(&c->out, MARKER_EOM);
}

static void command_evaluate(char * token, Channel * c) {
    CommandArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(command_evaluate_cache_client, c, &args, sizeof(args));
}

static void command_assign_cache_client(void * x) {
    CommandAssignArgs * args = (CommandAssignArgs *)x;
    Channel * c = cache_channel();
    Context * ctx = NULL;
    int frame = STACK_NO_FRAME;
    Expression * expr = NULL;
    Value value;
    int err = 0;

    memset(&value, 0, sizeof(value));
    if (expression_context_id(args->id, &ctx, &frame, &expr) < 0) err = errno;
    if (!err && frame != STACK_NO_FRAME && !ctx->stopped) err = ERR_IS_RUNNING;
    if (!err && evaluate_expression(ctx, frame, expr->script, 0, &value) < 0) err = errno;
    if (!err && !value.remote) err = ERR_INV_EXPRESSION;
    if (!err && context_write_mem(ctx, value.address, args->value_buf, args->value_size) < 0) err = errno;

    cache_exit();

    write_stringz(&c->out, "R");
    write_stringz(&c->out, args->token);
    write_errno(&c->out, err);
    write_stream(&c->out, MARKER_EOM);
    loc_free(args->value_buf);
}

static void command_assign(char * token, Channel * c) {
    CommandAssignArgs args;

    json_read_string(&c->inp, args.id, sizeof(args.id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    args.value_buf = json_read_alloc_binary(&c->inp, &args.value_size);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    strlcpy(args.token, token, sizeof(args.token));
    cache_enter(command_assign_cache_client, c, &args, sizeof(args));
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
