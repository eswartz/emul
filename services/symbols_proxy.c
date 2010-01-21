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
 * Symbols service - proxy implementation, gets symbols information from host.
 */

#include "config.h"

#if ENABLE_SymbolsProxy

#include <assert.h>
#include "context.h"
#include "cache.h"
#include "json.h"
#include "events.h"
#include "myalloc.h"
#include "exceptions.h"
#include "symbols.h"

#define HASH_SIZE 511

typedef struct SymTable {
    LINK hash[HASH_SIZE];
} SymTable;

typedef struct SymCache {
    unsigned magic;
    LINK link_hash;
    AbstractCache cache;
    char * id;
    char * type_id;
    char * base_type_id;
    char * index_type_id;
    char * pointer_type_id;
    char * name;
    int sym_class;
    int type_class;
    int has_size;
    int has_address;
    int has_offset;
    int has_length;
    int has_lower_bound;
    int has_upper_bound;
    ContextAddress address;
    ContextAddress size;
    ContextAddress offset;
    ContextAddress length;
    ContextAddress lower_bound;
    ContextAddress upper_bound;
    char * value;
    int value_size;
    char ** children_ids;
    int children_count;
    ReplyHandlerInfo * pending_get_context;
    ReplyHandlerInfo * pending_get_children;
    ErrorReport * error_get_context;
    ErrorReport * error_get_children;
    int done_context;
    int done_children;
    LINK array_syms;
} SymCache;

typedef struct ArraySymCache {
    LINK link_sym;
    AbstractCache cache;
    ContextAddress length;
    ReplyHandlerInfo * pending;
    ErrorReport * error;
    char * id;
} ArraySymCache;

#define SYM_CACHE_MAGIC 0x38254865

#define hash2sym(A) ((SymCache *)((char *)(A) - offsetof(SymCache, link_hash)))
#define sym2arr(A)  ((ArraySymCache *)((char *)(A) - offsetof(ArraySymCache, link_sym)))

struct Symbol {
    unsigned magic;
    SymCache * cache;
};

#include "symbols_alloc.h"

static unsigned sym_hash(char * id) {
    int i;
    unsigned h = 0;
    for (i = 0; id[i]; i++) h += id[i];
    h = h + h / HASH_SIZE;
    return h % HASH_SIZE;
}

static SymTable * get_sym_table(void) {
}

static void free_sym_cache(SymCache * c) {
    int i;
    assert(c->magic == SYM_CACHE_MAGIC);
    assert(c->pending_get_context == NULL);
    assert(c->error_get_children == NULL);
    list_remove(&c->link_hash);
    cache_dispose(&c->cache);
    loc_free(c->id);
    loc_free(c->type_id);
    loc_free(c->base_type_id);
    loc_free(c->index_type_id);
    loc_free(c->pointer_type_id);
    loc_free(c->name);
    loc_free(c->value);
    for (i = 0; i < c->children_count; i++) loc_free(c->children_ids[i]);
    loc_free(c->children_ids);
    release_error_report(c->error_get_context);
    release_error_report(c->error_get_children);
    while (!list_is_empty(&c->array_syms)) {
        ArraySymCache * a = sym2arr(c->array_syms.next);
        assert(a->pending == NULL);
        list_remove(&a->link_sym);
        cache_dispose(&a->cache);
        release_error_report(a->error);
        loc_free(a->id);
        loc_free(a);
    }
    loc_free(c);
}

static void read_context_data(InputStream * inp, char * name, void * args) {
    char id[256];
    SymCache * s = args;
    if (strcmp(name, "ID") == 0) { json_read_string(inp, id, sizeof(id)); assert(strcmp(id, s->id) == 0); }
    else if (strcmp(name, "Name") == 0) s->name = json_read_alloc_string(inp);
    else if (strcmp(name, "Class") == 0) s->sym_class = json_read_long(inp);
    else if (strcmp(name, "TypeClass") == 0) s->type_class = json_read_long(inp);
    else if (strcmp(name, "TypeID") == 0) s->type_id = json_read_alloc_string(inp);
    else if (strcmp(name, "BaseTypeID") == 0) s->base_type_id = json_read_alloc_string(inp);
    else if (strcmp(name, "IndexTypeID") == 0) s->index_type_id = json_read_alloc_string(inp);
    else if (strcmp(name, "Size") == 0) { s->size = json_read_long(inp); s->has_size = 1; }
    else if (strcmp(name, "Length") == 0) { s->length = json_read_long(inp); s->has_length = 1; }
    else if (strcmp(name, "LowerBound") == 0) { s->lower_bound = json_read_long(inp); s->has_lower_bound = 1; }
    else if (strcmp(name, "UpperBound") == 0) { s->upper_bound = json_read_long(inp); s->has_upper_bound = 1; }
    else if (strcmp(name, "Offset") == 0) { s->offset = json_read_long(inp); s->has_offset = 1; }
    else if (strcmp(name, "Address") == 0) { s->address = (ContextAddress)json_read_int64(inp); s->has_address = 1; }
    else if (strcmp(name, "Value") == 0) s->value = json_read_alloc_binary(inp, &s->value_size);
    else loc_free(json_skip_object(inp));
}

static void validate_context(Channel * c, void * args, int error) {
    SymCache * s = args;
    assert(s->pending_get_context != NULL);
    assert(s->error_get_context == NULL);
    assert(!s->done_context);
    s->pending_get_context = NULL;
    s->error_get_context = get_error_report(error);
    s->done_context = 1;
    if (!error) {
        s->error_get_context = get_error_report(read_errno(&c->inp));
        json_read_struct(&c->inp, read_context_data, s);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    }
    cache_notify(&s->cache);
}

static SymCache * get_sym_cache(const Symbol * sym) {
    Trap trap;
    SymCache * s = sym->cache;
    assert(sym->magic == SYMBOL_MAGIC);
    assert(s->magic == SYM_CACHE_MAGIC);
    assert(s->id != NULL);
    if (!set_trap(&trap)) return NULL;
    if (s->pending_get_context != NULL) {
        cache_wait(&s->cache);
    }
    else if (s->error_get_context != NULL) {
        exception(set_error_report_errno(s->error_get_context));
    }
    else if (!s->done_context) {
        Channel * c = cache_channel();
        if (c == NULL) exception(ERR_CACHE_MISS);
        s->pending_get_context = protocol_send_command(c, "Symbols", "getContext", validate_context, s);
        json_write_string(&c->out, s->id);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
        cache_wait(&s->cache);
    }
    clear_trap(&trap);
    return s;
}

int find_symbol(Context * ctx, int frame, char * name, Symbol ** sym) {
}

int enumerate_symbols(Context * ctx, int frame, EnumerateSymbolsCallBack * func, void * args) {
}

char * symbol2id(const Symbol * sym) {
    SymCache * s = sym->cache;
    assert(s->magic == SYM_CACHE_MAGIC);
    assert(s->id != NULL);
    return s->id;
}

int id2symbol(char * id, Symbol ** sym) {
    LINK * l;
    SymCache * s = NULL;
    unsigned h = sym_hash(id);
    SymTable * t = get_sym_table();
    for (l = t->hash[h].next; l != t->hash + h; l = l->next) {
        SymCache * x = hash2sym(l);
        if (strcmp(x->id, id) == 0) {
            s = x;
            break;
        }
    }
    if (s == NULL) {
        s = loc_alloc_zero(sizeof(*s));
        s->magic = SYM_CACHE_MAGIC;
        s->id = loc_strdup(id);
        list_add_first(&s->link_hash, t->hash + h);
        list_init(&s->array_syms);
    }
    *sym = alloc_symbol();
    (*sym)->cache = s;
    return 0;
}

/*************** Functions for retrieving symbol properties ***************************************/

int get_symbol_class(const Symbol * sym, int * symbol_class) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    *symbol_class = c->sym_class;
    return 0;
}

int get_symbol_type(const Symbol * sym, Symbol ** type) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    if (c->type_id) return id2symbol(c->type_id, type);
    return 0;
}

int get_symbol_type_class(const Symbol * sym, int * type_class) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    *type_class = c->type_class;
    return 0;
}

int get_symbol_name(const Symbol * sym, char ** name) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    *name = c->name;
    return 0;
}

int get_symbol_base_type(const Symbol * sym, Symbol ** type) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    if (c->base_type_id) return id2symbol(c->base_type_id, type);
    return 0;
}

int get_symbol_index_type(const Symbol * sym, Symbol ** type) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    if (c->index_type_id) return id2symbol(c->index_type_id, type);
    return 0;
}

int get_symbol_size(const Symbol * sym, ContextAddress * size) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    if (!c->has_size) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *size = c->size;
    return 0;
}

int get_symbol_length(const Symbol * sym, ContextAddress * length) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    if (!c->has_length) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *length = c->length;
    return 0;
}

int get_symbol_lower_bound(const Symbol * sym, ContextAddress * lower_bound) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    if (!c->has_lower_bound) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *lower_bound = c->lower_bound;
    return 0;
}

int get_symbol_offset(const Symbol * sym, ContextAddress * offset) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    if (!c->has_offset) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *offset = c->offset;
    return 0;
}

int get_symbol_value(const Symbol * sym, void ** value, size_t * size) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    if (c->sym_class != SYM_CLASS_VALUE) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *value = c->value;
    *size = c->value_size;
    return 0;
}

int get_symbol_address(const Symbol * sym, ContextAddress * address) {
    SymCache * c = get_sym_cache(sym);
    if (c == NULL) return -1;
    if (!c->has_address) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *address = c->address;
    return 0;
}

static void validate_children(Channel * c, void * args, int error) {
    SymCache * s = args;
    assert(s->pending_get_children != NULL);
    assert(s->error_get_children == NULL);
    assert(!s->done_children);
    s->pending_get_children = NULL;
    s->error_get_children = get_error_report(error);
    s->done_children = 1;
    if (!error) {
        s->error_get_children = get_error_report(read_errno(&c->inp));
        s->children_ids = json_read_alloc_string_array(&c->inp, &s->children_count);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    }
    cache_notify(&s->cache);
}

int get_symbol_children(const Symbol * sym, Symbol *** children, int * count) {
    Trap trap;
    SymCache * s = get_sym_cache(sym);
    *children = NULL;
    *count = 0;
    if (s == NULL) return -1;
    if (!set_trap(&trap)) return -1;
    if (s->pending_get_children) {
        cache_wait(&s->cache);
    }
    else if (s->error_get_children) {
        exception(set_error_report_errno(s->error_get_children));
    }
    else if (!s->done_children) {
        Channel * c = cache_channel();
        if (c == NULL) exception(ERR_CACHE_MISS);
        s->pending_get_children = protocol_send_command(c, "Symbols", "getChildren", validate_children, s);
        json_write_string(&c->out, s->id);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
        cache_wait(&s->cache);
    }
    else if (s->children_count > 0) {
        int i, cnt = s->children_count;
        static Symbol ** buf = NULL;
        static int buf_len = 0;
        if (buf_len < cnt) {
            buf_len = cnt;
            buf = loc_realloc(buf, cnt * sizeof(Symbol *));
        }
        for (i = 0; i < cnt; i++) {
            if (id2symbol(s->children_ids[i], buf + i) < 0) exception(errno);
        }
        *children = buf;
        *count = cnt;
    }
    clear_trap(&trap);
    return 0;
}

static void validate_type_id(Channel * c, void * args, int error) {
    ArraySymCache * s = args;
    assert(s->pending != NULL);
    assert(s->error == NULL);
    assert(s->id == NULL);
    s->pending = NULL;
    s->error = get_error_report(error);
    if (!error) {
        s->error = get_error_report(read_errno(&c->inp));
        s->id = json_read_alloc_string(&c->inp);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    }
    cache_notify(&s->cache);
}

int get_array_symbol(const Symbol * sym, ContextAddress length, Symbol ** ptr) {
    LINK * l;
    Trap trap;
    ArraySymCache * a = NULL;
    SymCache * s = get_sym_cache(sym);
    if (s == NULL) return -1;
    if (!set_trap(&trap)) return -1;
    for (l = s->array_syms.next; l != &s->array_syms; l = l->next) {
        ArraySymCache * x = sym2arr(l);
        if (x->length == length) {
            a = x;
            break;
        }
    }
    if (a == NULL) {
        a = loc_alloc_zero(sizeof(*a));
        a->length = length;
        list_add_first(&a->link_sym, &s->array_syms);
    }
    if (a->pending) {
        cache_wait(&a->cache);
    }
    else if (a->error) {
        exception(set_error_report_errno(a->error));
    }
    else if (!a->id) {
        Channel * c = cache_channel();
        if (c == NULL) exception(ERR_CACHE_MISS);
        a->pending = protocol_send_command(c, "Symbols", "getArrayType", validate_type_id, a);
        json_write_string(&c->out, s->id);
        write_stream(&c->out, 0);
        json_write_int64(&c->out, length);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
        cache_wait(&a->cache);
    }
    clear_trap(&trap);
    return id2symbol(a->id, ptr);
}

/*************************************************************************************************/

ContextAddress is_plt_section(Context * ctx, ContextAddress addr) {
}

void ini_symbols_lib(void) {
}

#endif
