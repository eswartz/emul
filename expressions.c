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
 * Expression evaluation library.
 */

#include "mdep.h"
#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <string.h>
#include "myalloc.h"
#include "exceptions.h"
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

static ExpressionContext * expr_ctx = NULL;
static char * text = NULL;
static int text_pos = 0;
static int text_ch = 0;
static int text_sy = 0;
static Value text_val;
static char text_error[256];

static char str_pool[STR_POOL_SIZE];
static int str_pool_cnt = 0;
static StringValue * str_alloc_list = NULL;

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

static int to_boolean(Value * v) {
    if (v == NULL) return 0;
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
        if (expr_ctx->identifier == NULL || expr_ctx->identifier(text_val.str, v)) {
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

int evaluate_expression(ExpressionContext * ctx, char * s, Value * v) {
    int r = 0;
    Trap trap;
    if (set_trap(&trap)) {
        expr_ctx = ctx;
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

char * get_expression_error_msg(void) {
    if (text_error[0] == 0) return NULL;
    return text_error;
}

void ini_expression_library(void) {
#ifndef  NDEBUG
    Value v;
    ExpressionContext ctx = { NULL, NULL };
    assert(evaluate_expression(&ctx, "0", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 0 && v.str == NULL);
    assert(evaluate_expression(&ctx, "0.", &v) != 0);
    assert(evaluate_expression(&ctx, "2 * 2", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 4 && v.str == NULL);
    assert(evaluate_expression(&ctx, "1 ? 2 : 3", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 2 && v.str == NULL);
    assert(evaluate_expression(&ctx, "0 ? 2 : 3", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 3 && v.str == NULL);
    assert(evaluate_expression(&ctx, "(1?2:3) == 2 && (0?2:3) == 3", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 1 && v.str == NULL);
    assert(evaluate_expression(&ctx, "(1?2:3) != 2 || (0?2:3) != 3", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 0 && v.str == NULL);
    assert(evaluate_expression(&ctx, "5>2 && 4<6", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 1 && v.str == NULL);
    assert(evaluate_expression(&ctx, "5<=2 || 4>=6", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 0 && v.str == NULL);
    assert(evaluate_expression(&ctx, "((5*2+7-1)/2)>>1==4 && 1<<3==8 && 5%2==1", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 1 && v.str == NULL);
    assert(evaluate_expression(&ctx, "\042ABC\042 + \042DEF\042 == \042ABCDEF\042", &v) == 0);
    assert(v.type == VALUE_INT && v.value == 1 && v.str == NULL);
#endif
}

