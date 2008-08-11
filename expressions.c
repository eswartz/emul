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

#include "mdep.h"
#include "config.h"
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

static char * text = NULL;
static int text_pos = 0;
static int text_ch = 0;
static int text_sy = 0;
static Value text_val;
static char text_error[256];

static char str_pool[STR_POOL_SIZE];
static int str_pool_cnt = 0;
static StringValue * str_alloc_list = NULL;

static Context * expression_context = NULL;
static int expression_frame = STACK_NO_FRAME;

static char * alloc_str(int len) {
    if (str_pool_cnt + len < STR_POOL_SIZE) {
        char * s = str_pool + str_pool_cnt;
        str_pool_cnt += len + 1;
        return s;
    }
    else {
        StringValue * s = (StringValue *)loc_alloc(sizeof(StringValue) + len);
        s->next = str_alloc_list;
        str_alloc_list = s;
        return s->buf;
    }
}

void string_value(Value * v, char * str) {
    memset(v, 0, sizeof(Value));
    v->type = VALUE_STR;
    if (str != NULL) {
        int len = strlen(str);
        v->str = alloc_str(len);
        memcpy(v->str, str, len + 1);
    }
}

static void error(int no, char * msg) {
    snprintf(text_error, sizeof(text_error),
        "Expression evaluation error: %s, text pos %d", msg, text_pos);
    exception(no);
}

static void next_ch(void) {
    text_ch = (unsigned char)text[text_pos];
    if (text_ch != 0) text_pos++;
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
            text_val.type = VALUE_UNS;
            text_val.str = NULL;
            text_val.value = next_char_val();
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
                text_val.type = VALUE_STR;
                text_val.value = 0;
                text_val.str = alloc_str(len);
                text_val.addr = 0;
                text_val.size = len + 1;
                text_pos = pos - 1;
                next_ch();
                while (text_ch != '"') {
                    text_val.str[cnt++] = next_char_val();
                }
                assert(cnt == len);
                text_val.str[cnt] = 0;
                next_ch();
                text_sy = SY_VAL;
            }
            return;
        case '0':
            if (text_ch == 'x') {
                next_ch();
                text_val.type = VALUE_UNS;
                text_val.str = NULL;
                text_val.value = 0;
                text_val.addr = 0;
                text_val.size = sizeof(int);
                while (text_ch >= '0' && text_ch <= '9' ||
                        text_ch >= 'A' && text_ch <= 'F' ||
                        text_ch >= 'a' && text_ch <= 'f') {
                    text_val.value = (text_val.value << 4) | next_hex();
                }
            }
            else {
                text_val.type = VALUE_INT;
                text_val.str = NULL;
                text_val.value = 0;
                text_val.addr = 0;
                text_val.size = sizeof(int);
                while (text_ch >= '0' && text_ch <= '7') {
                    text_val.value = (text_val.value << 3) | next_oct();
                }
            }
            text_sy = SY_VAL;
            return;
        default:
            if (ch >= '0' && ch <= '9') {
                text_val.type = VALUE_INT;
                text_val.str = NULL;
                text_val.value = ch - '0';
                text_val.addr = 0;
                text_val.size = sizeof(int);
                while (text_ch >= '0' && text_ch <= '9') {
                    text_val.value = (text_val.value * 10) + next_dec();
                }
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
                text_val.type = VALUE_STR;
                text_val.value = 0;
                text_val.str = alloc_str(len);
                text_val.addr = 0;
                text_val.size = len + 1;
                text_pos = pos - 1;
                next_ch();
                while (is_name_character(text_ch)) {
                    text_val.str[cnt++] = text_ch;
                    next_ch();
                }
                assert(cnt == len);
                text_val.str[cnt] = 0;
                text_sy = SY_ID;
                return;
            }
            error(ERR_INV_EXPRESSION, "Illegal character");
            break;
        }
    }
}

static int identifier(char * name, Value * v) {
    if (v == NULL) return 0;
    memset(v, 0, sizeof(Value));
    if (expression_context == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (strcmp(name, "$thread") == 0) {
        if (context_has_state(expression_context)) {
            string_value(v, thread_id(expression_context));
        }
        else {
            string_value(v, container_id(expression_context));
        }
        return 0;
    }
#if SERVICE_Symbols
    {
        Symbol sym;
        if (find_symbol(expression_context, expression_frame, name, &sym) < 0) {
            if (errno != ERR_SYM_NOT_FOUND) return -1;
        }
        else if (sym.type == SYM_TYPE_FUNCTION) {
            v->type = VALUE_INT;
            v->value = sym.value;
            v->addr = 0;
            v->size = sizeof(int);
            return 0;
        }
        else {
            v->type = VALUE_VAR;
            v->addr = sym.value;
            v->size = sym.size;
            if (sym.base == SYM_BASE_FP) {
                ContextAddress fp = 0;
                if (get_frame_info(expression_context, expression_frame, NULL, NULL, &fp) < 0) return -1;
                v->addr = fp + sym.value;
            }
            return 0;
        }
    }
#endif
    errno = ERR_SYM_NOT_FOUND;
    return -1;
}

static void load_value(Value * v) {
    int i = 1;
    char * addr;

    if (v->type != VALUE_VAR) return;
    if (v->size > sizeof(int)) {
        error(ERR_UNSUPPORTED, "Can't read variable value: too large");
    }
    addr = (char *)&v->addr;
    if (*(char *)&i != 1) addr += sizeof(int) - v->size;
    v->value = 0;
    if (context_read_mem(expression_context, v->addr, &v->value, v->size) < 0) {
        char msg[256];
        int err = errno;
        snprintf(msg, sizeof(msg), "Can't read variable value: %d %s", err, errno_to_str(err));
        error(err, msg);
    }
    check_breakpoints_on_memory_read(expression_context, v->addr, &v->value, v->size);
    v->type = VALUE_INT;
    v->size = sizeof(int);
    v->addr = 0;
}

static int to_boolean(Value * v) {
    if (v == NULL) return 0;
    load_value(v);
    switch (v->type) {
    case VALUE_INT: return v->value != 0;
    case VALUE_UNS: return v->value != 0;
    case VALUE_STR: return v->str != NULL;
    default: assert(0);
    }
    return 0;
}

static int to_int(Value * v) {
    if (v == NULL) return 0;
    load_value(v);
    switch (v->type) {
    case VALUE_INT: return v->value;
    case VALUE_UNS: return v->value;
    case VALUE_STR: return v->str != NULL;
    default: assert(0);
    }
    return 0;
}

static unsigned to_uns(Value * v) {
    if (v == NULL) return 0;
    load_value(v);
    switch (v->type) {
    case VALUE_INT: return (unsigned)v->value;
    case VALUE_UNS: return (unsigned)v->value;
    case VALUE_STR: return v->str != NULL;
    default: assert(0);
    }
    return 0;
}

static void expression(Value * v);

static void primary_expression(Value * v) {
    if (text_sy == '(') {
        next_sy();
        expression(v);
        if (text_sy != ')') error(ERR_INV_EXPRESSION, "Missing ')'");
        next_sy();
    }
    else if (text_sy == SY_VAL) {
        if (v) *v = text_val;
        next_sy();
    }
    else if (text_sy == SY_ID) {
        errno = 0;
        if (identifier(text_val.str, v) < 0) {
            char msg[256];
            int err = ERR_INV_EXPRESSION;
            if (errno != 0) {
                err = errno;
                snprintf(msg, sizeof(msg), "Can't evaluate '%s': %d %s",
                    text_val.str, err, errno_to_str(err));
            }
            else {
                snprintf(msg, sizeof(msg), "Undeclared identifier '%s'", text_val.str);
            }
            error(err, msg);
        }
        next_sy();
    }
    else {
        error(ERR_INV_EXPRESSION, "Syntax error");
    }
}

static void postfix_expression(Value * v) {
    /* TODO: postfix_expression() */
    primary_expression(v);
}

static void unary_expression(Value * v) {
    /* TODO: unary_expression() */
    postfix_expression(v);
}

static void cast_expression(Value * v) {
    /* TODO: cast_expression() */
    unary_expression(v);
}

static void multiplicative_expression(Value * v) {
    cast_expression(v);
    while (text_sy == '*' || text_sy == '/' || text_sy == '%') {
        Value x;
        int sy = text_sy;
        next_sy();
        cast_expression(v ? &x : NULL);
        if (v) {
            load_value(v);
            load_value(&x);
            if (v->type == VALUE_STR || x.type == VALUE_STR) {
                error(ERR_INV_EXPRESSION, "Operation is not applicable to string");
            }
            if (sy != '*' && x.value == 0) {
                error(ERR_INV_EXPRESSION, "Dividing by zero");
            }
            if (v->type == VALUE_UNS || x.type == VALUE_UNS) {
                switch (sy) {
                case '*': v->value = to_uns(v) * to_uns(&x); break;
                case '/': v->value = to_uns(v) / to_uns(&x); break;
                case '%': v->value = to_uns(v) % to_uns(&x); break;
                }
                v->type = VALUE_UNS;
            }
            else {
                switch (sy) {
                case '*': v->value = to_int(v) * to_int(&x); break;
                case '/': v->value = to_int(v) / to_int(&x); break;
                case '%': v->value = to_int(v) % to_int(&x); break;
                }
                v->type = VALUE_INT;
            }
        }
    }
}

static void additive_expression(Value * v) {
    multiplicative_expression(v);
    while (text_sy == '+' || text_sy == '-') {
        Value x;
        int sy = text_sy;
        next_sy();
        multiplicative_expression(v ? &x : NULL);
        if (v) {
            load_value(v);
            load_value(&x);
            if (v->type == VALUE_STR && x.type == VALUE_STR) {
                char * s;
                if (sy != '+') error(ERR_INV_EXPRESSION, "Operation is not applicable to string");
                if (v->str == NULL) {
                    v->str = x.str;
                }
                else if (x.str != NULL) {
                    s = alloc_str(strlen(v->str) + strlen(x.str));
                    strcpy(s, v->str);
                    strcat(s, x.str);
                    v->str = s;
                }
            }
            else if (v->type == VALUE_STR || x.type == VALUE_STR) {
                error(ERR_INV_EXPRESSION, "Operation is not applicable to string");
            }
            else if (v->type == VALUE_UNS || x.type == VALUE_UNS) {
                switch (sy) {
                case '+': v->value = to_uns(v) + to_uns(&x); break;
                case '-': v->value = to_uns(v) - to_uns(&x); break;
                }
                v->type = VALUE_UNS;
            }
            else {
                switch (sy) {
                case '+': v->value = to_int(v) + to_int(&x); break;
                case '-': v->value = to_int(v) - to_int(&x); break;
                }
                v->type = VALUE_INT;
            }
        }
    }
}

static void shift_expression(Value * v) {
    additive_expression(v);
    while (text_sy == SY_SHL || text_sy == SY_SHR) {
        Value x;
        int sy = text_sy;
        next_sy();
        additive_expression(v ? &x : NULL);
        if (v) {
            load_value(v);
            load_value(&x);
            if (v->type == VALUE_STR || x.type == VALUE_STR) {
                error(ERR_INV_EXPRESSION, "Integral types expected");
            }
            if (x.type == VALUE_INT && to_int(&x) < 0) {
                if (v->type == VALUE_UNS) {
                    switch (sy) {
                    case SY_SHL: v->value = to_uns(v) >> -to_int(&x); break;
                    case SY_SHR: v->value = to_uns(v) << -to_int(&x); break;
                    }
                }
                else {
                    switch (sy) {
                    case SY_SHL: v->value = to_int(v) >> -to_int(&x); break;
                    case SY_SHR: v->value = to_int(v) << -to_int(&x); break;
                    }
                }
            }
            else {
                if (v->type == VALUE_UNS) {
                    switch (sy) {
                    case SY_SHL: v->value = to_uns(v) << to_uns(&x); break;
                    case SY_SHR: v->value = to_uns(v) >> to_uns(&x); break;
                    }
                }
                else {
                    switch (sy) {
                    case SY_SHL: v->value = to_int(v) << to_uns(&x); break;
                    case SY_SHR: v->value = to_int(v) >> to_uns(&x); break;
                    }
                }
            }
        }
    }
}

static void relational_expression(Value * v) {
    shift_expression(v);
    while (text_sy == '<' || text_sy == '>' || text_sy == SY_LEQ || text_sy == SY_GEQ) {
        Value x;
        int sy = text_sy;
        next_sy();
        shift_expression(v ? &x : NULL);
        if (v) {
            load_value(v);
            load_value(&x);
            if (v->type == VALUE_STR && x.type == VALUE_STR) {
                int n = 0;
                if (v->str == NULL && x.str == NULL) n = 0;
                else if (v->str == NULL) n = -1;
                else if (x.str == NULL) n = 1;
                else n = strcmp(v->str, x.str);
                switch (sy) {
                case '<': v->value = n < 0; break;
                case '>': v->value = n > 0; break;
                case SY_LEQ: v->value = n <= 0; break;
                case SY_GEQ: v->value = n >= 0; break;
                }
            }
            else if (v->type == VALUE_UNS || x.type == VALUE_UNS) {
                switch (sy) {
                case '<': v->value = to_uns(v) < to_uns(&x); break;
                case '>': v->value = to_uns(v) > to_uns(&x); break;
                case SY_LEQ: v->value = to_uns(v) <= to_uns(&x); break;
                case SY_GEQ: v->value = to_uns(v) >= to_uns(&x); break;
                }
            }
            else {
                switch (sy) {
                case '<': v->value = to_int(v) < to_int(&x); break;
                case '>': v->value = to_int(v) > to_int(&x); break;
                case SY_LEQ: v->value = to_int(v) <= to_int(&x); break;
                case SY_GEQ: v->value = to_int(v) >= to_int(&x); break;
                }
            }
            v->str = NULL;
            v->type = VALUE_INT;
        }
    }
}

static void equality_expression(Value * v) {
    relational_expression(v);
    while (text_sy == SY_EQU || text_sy == SY_NEQ) {
        Value x;
        int sy = text_sy;
        next_sy();
        relational_expression(v ? &x : NULL);
        if (v) {
            load_value(v);
            load_value(&x);
            if (v->type == VALUE_STR && x.type == VALUE_STR) {
                if (v->str == NULL && x.str == NULL) v->value = 1;
                else if (v->str == NULL || x.str == NULL) v->value = 0;
                else v->value = strcmp(v->str, x.str) == 0;
            }
            else {
                v->value = to_int(v) == to_int(&x);
            }
            v->str = NULL;
            v->type = VALUE_INT;
            if (sy == SY_NEQ) v->value = !v->value;
        }
    }
}

static void and_expression(Value * v) {
    equality_expression(v);
    while (text_sy == '&') {
        Value x;
        next_sy();
        equality_expression(v ? &x : NULL);
        if (v) {
            v->value = to_int(v) & to_int(&x);
            v->str = NULL;
            v->type = v->type == VALUE_UNS || x.type == VALUE_UNS ? VALUE_UNS : VALUE_INT;
        }
    }
}

static void exclusive_or_expression(Value * v) {
    and_expression(v);
    while (text_sy == '^') {
        Value x;
        next_sy();
        and_expression(v ? &x : NULL);
        if (v) {
            v->value = to_int(v) ^ to_int(&x);
            v->str = NULL;
            v->type = v->type == VALUE_UNS || x.type == VALUE_UNS ? VALUE_UNS : VALUE_INT;
        }
    }
}

static void inclusive_or_expression(Value * v) {
    exclusive_or_expression(v);
    while (text_sy == '|') {
        Value x;
        next_sy();
        exclusive_or_expression(v ? &x : NULL);
        if (v) {
            v->value = to_int(v) | to_int(&x);
            v->str = NULL;
            v->type = v->type == VALUE_UNS || x.type == VALUE_UNS ? VALUE_UNS : VALUE_INT;
        }
    }
}

static void logical_and_expression(Value * v) {
    inclusive_or_expression(v);
    while (text_sy == SY_AND) {
        Value x;
        int b = to_boolean(v);
        next_sy();
        inclusive_or_expression(v && b ? &x : NULL);
        if (v && b) *v = x;
    }
}

static void logical_or_expression(Value * v) {
    logical_and_expression(v);
    while (text_sy == SY_OR) {
        Value x;
        int b = to_boolean(v);
        next_sy();
        logical_and_expression(v && !b ? &x : NULL);
        if (v && !b) *v = x;
    }
}

static void conditional_expression(Value * v) {
    logical_or_expression(v);
    if (text_sy == '?') {
        Value x;
        Value y;
        int b = to_boolean(v);
        next_sy();
        expression(v && b ? &x : NULL);
        if (text_sy != ':') error(ERR_INV_EXPRESSION, "Missing ':'");
        next_sy();
        conditional_expression(v && !b ? &y : NULL);
        if (v) *v = b ? x : y;
    }
}

static void expression(Value * v) {
    /* TODO: assignments in expressions */
    conditional_expression(v);
}

int evaluate_expression(Context * ctx, int frame, char * s, Value * v) {
    int r = 0;
    Trap trap;

    expression_context = ctx;
    expression_frame = frame;
    if (set_trap(&trap)) {
        text_error[0] = 0;
        text_val.str = NULL;
        str_pool_cnt = 0;
        while (str_alloc_list != NULL) {
            StringValue * str = str_alloc_list;
            str_alloc_list = str->next;
            loc_free(str);
        }
        text = s;
        text_pos = 0;
        next_ch();
        next_sy();
        expression(v);
        if (text_sy != 0) error(ERR_INV_EXPRESSION, "Illegal characters at the end of expression");
        clear_trap(&trap);
    }
    else {
        errno = trap.error;
        r = -1;
    }
    return r;
}

int value_to_boolean(Value * v) {
    return to_boolean(v);
}

char * get_expression_error_msg(void) {
    if (text_error[0] == 0) return NULL;
    return text_error;
}

#if SERVICE_Expressions

#include "json.h"
#include "context.h"
#include "stacktrace.h"
#include "breakpoints.h"
#include "symbols.h"

typedef struct Expression Expression;

struct Expression {
    LINK link_all;
    LINK link_id;
    char id[256];
    char parent[256];
    char language[256];
    Channel * channel;
    char * script;
};

#define link_all2exp(A)  ((Expression *)((char *)(A) - (int)&((Expression *)0)->link_all))
#define link_id2exp(A)   ((Expression *)((char *)(A) - (int)&((Expression *)0)->link_id))

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

static void write_context(OutputStream * out, char * id, char * parent, char * name, Expression * expr) {
    write_stream(out, '{');
    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, id);

    write_stream(out, ',');

    json_write_string(out, "ParentID");
    write_stream(out, ':');
    json_write_string(out, parent);

    if (name) {
        write_stream(out, ',');

        json_write_string(out, "Name");
        write_stream(out, ':');
        json_write_string(out, name);
    }

    if (expr) {
        write_stream(out, ',');

        json_write_string(out, "Expression");
        write_stream(out, ':');
        json_write_string(out, expr->script);
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

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (expression_context_id(id, parent, &ctx, &frame, name, &expr) < 0) err = errno;

    if (!err && expr == NULL) {
#if SERVICE_Symbols
        Symbol sym;
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
        write_context(&c->out, id, parent, name, expr);
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
        strncpy(args.id, id, sizeof(args.id));

        if ((ctx = id2ctx(id)) != NULL) {
            if (context_has_state(ctx)) {
                char * frame_id = get_stack_frame_id(ctx, STACK_TOP_FRAME);
                if (frame_id == NULL) {
                    err = errno;
                }
                else {
                    frame = STACK_TOP_FRAME;
                    strncpy(args.id, frame_id, sizeof(args.id));
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
    strncpy(e->parent, parent, sizeof(e->parent));
    strncpy(e->language, language, sizeof(e->language));
    e->channel = c;
    e->script = script;
    list_add_last(&e->link_all, &expressions);
    list_add_last(&e->link_id, id2exp + expression_hash(e->id));

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, err);

    if (err) {
        write_stringz(&c->out, "null");
    }
    else {
        write_context(&c->out, e->id, parent, NULL, e);
        write_stream(&c->out, 0);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_evaluate(char * token, Channel * c) {
    int err = 0;
    char id[256];
    char parent[256];
    char name[MAX_SYM_NAME];
    Context * ctx;
    int frame;
    Expression * expr = NULL;
    Value value;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (expression_context_id(id, parent, &ctx, &frame, name, &expr) < 0) err = errno;
    if (!err && evaluate_expression(ctx, frame, expr ? expr->script : name, &value) < 0) err = errno;
    
    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    if (err) {
        write_stringz(&c->out, "null");
    }
    else if (value.type == VALUE_VAR) {
        ContextAddress addr;
        ContextAddress addr0 = value.addr;
        unsigned long size = value.size;
        JsonWriteBinaryState state;
        char buf[BUF_SIZE];

        json_write_binary_start(&state, &c->out);
        addr = addr0;
        while (err == 0 && addr < addr0 + size) {
            int rd = addr0 + size - addr;
            if (rd > BUF_SIZE) rd = BUF_SIZE;
            if (context_read_mem(ctx, addr, buf, rd) < 0) {
                err = errno;
            }
            else {
                check_breakpoints_on_memory_read(ctx, addr, buf, rd);
                json_write_binary_data(&state, buf, rd);
                addr += rd;
            }
        }
        json_write_binary_end(&state);
        write_stream(&c->out, 0);
    }
    else {
        void * buf = NULL;
        size_t size = 0;
        JsonWriteBinaryState state;

        switch (value.type) {
        case VALUE_INT:
        case VALUE_UNS:
            buf = &value.value;
            size = sizeof(value.value);
            break;
        case VALUE_STR:
            buf = value.str;
            size = value.size;
            break;
        }
        json_write_binary_start(&state, &c->out);
        json_write_binary_data(&state, buf, size);
        json_write_binary_end(&state);
        write_stream(&c->out, 0);
    }
    write_errno(&c->out, err);
    if (err) {
        write_stringz(&c->out, "null");
    }
    else {
        write_stream(&c->out, '{');
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

    if (expression_context_id(id, parent, &ctx, &frame, name, &expr) < 0) err = errno;
    if (!err && evaluate_expression(ctx, frame, expr ? expr->script : name, &value) < 0) err = errno;

    addr0 = value.addr;
    size0 = value.size;
    addr = addr0;

    json_read_binary_start(&state, &c->inp);
    for (;;) {
        int rd = json_read_binary_data(&state, buf, sizeof(buf));
        if (rd == 0) break;
        if (err == 0) {
            if (value.type == VALUE_VAR) {
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

void ini_expressions_service(Protocol * proto) {
    unsigned i;
#ifndef  NDEBUG
    Value v;
    assert(evaluate_expression(NULL, STACK_NO_FRAME, "0", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 0 && v.str == NULL);
    assert(evaluate_expression(NULL, STACK_NO_FRAME, "0.", &v) != 0);
    assert(evaluate_expression(NULL, STACK_NO_FRAME, "2 * 2", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 4 && v.str == NULL);
    assert(evaluate_expression(NULL, STACK_NO_FRAME, "1 ? 2 : 3", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 2 && v.str == NULL);
    assert(evaluate_expression(NULL, STACK_NO_FRAME, "0 ? 2 : 3", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 3 && v.str == NULL);
    assert(evaluate_expression(NULL, STACK_NO_FRAME, "(1?2:3) == 2 && (0?2:3) == 3", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 1 && v.str == NULL);
    assert(evaluate_expression(NULL, STACK_NO_FRAME, "(1?2:3) != 2 || (0?2:3) != 3", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 0 && v.str == NULL);
    assert(evaluate_expression(NULL, STACK_NO_FRAME, "5>2 && 4<6", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 1 && v.str == NULL);
    assert(evaluate_expression(NULL, STACK_NO_FRAME, "5<=2 || 4>=6", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 0 && v.str == NULL);
    assert(evaluate_expression(NULL, STACK_NO_FRAME, "((5*2+7-1)/2)>>1==4 && 1<<3==8 && 5%2==1", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 1 && v.str == NULL);
    assert(evaluate_expression(NULL, STACK_NO_FRAME, "\042ABC\042 + \042DEF\042 == \042ABCDEF\042", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 1 && v.str == NULL);
#endif
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

#endif
