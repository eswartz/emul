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
 * Symbols service - symbol memory management code.
 */

#define SYMBOL_MAGIC 0x34875234
#define SYMBOL_BLOCK_SIZE 64

typedef struct SymbolBlock {
    struct SymbolBlock * next;
    Symbol syms[SYMBOL_BLOCK_SIZE];
    int busy_cnt;
} SymbolBlock;

static SymbolBlock * syms_free = NULL;
static SymbolBlock * syms_busy = NULL;
static int syms_event_posted = 0;
static int syms_cnt = 0;

static void syms_event(void * arg) {
    syms_event_posted = 0;
    if (syms_cnt > 8 && syms_free != NULL) {
        SymbolBlock * s = syms_free;
        syms_free = s->next;
        loc_free(s);
        syms_cnt--;
    }
    while (syms_busy != NULL) {
        SymbolBlock * s = syms_busy;
        syms_busy = s->next;
        memset(s, 0, sizeof(SymbolBlock));
        s->next = syms_free;
        syms_free = s;
    }
}

static Symbol * alloc_symbol(void) {
    Symbol * s = NULL;

    if (syms_busy == NULL || syms_busy->busy_cnt >= SYMBOL_BLOCK_SIZE) {
        SymbolBlock * b = syms_free;
        if (b != NULL) {
            syms_free = b->next;
        }
        else {
            b = (SymbolBlock *)loc_alloc_zero(sizeof(SymbolBlock));
            syms_cnt++;
        }
        b->next = syms_busy;
        syms_busy = b;
        assert(b->busy_cnt == 0);
    }

    s = syms_busy->syms + syms_busy->busy_cnt++;
    assert(s->magic == 0);
    s->magic = SYMBOL_MAGIC;
    if (!syms_event_posted) {
        post_event(syms_event, NULL);
        syms_event_posted = 1;
    }
    return s;
}
