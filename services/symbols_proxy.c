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

/* TODO: need to cleanup symbols cache from data that not used for long time */

#include "config.h"

#if ENABLE_SymbolsProxy

#include <assert.h>
#include <stdio.h>
#include "context.h"
#include "cache.h"
#include "json.h"
#include "events.h"
#include "myalloc.h"
#include "exceptions.h"
#include "stacktrace.h"
#include "symbols.h"

#define HASH_SIZE 511

/* Symbols cahce, one per channel */
typedef struct SymbolsCache {
    Channel * channel;
    LINK link_root;
    LINK link_sym[HASH_SIZE];
    LINK link_find[HASH_SIZE];
    LINK link_list[HASH_SIZE];
} SymbolsCache;

/* Symbol properties cache */
typedef struct SymInfoCache {
    unsigned magic;
    LINK link_syms;
    AbstractCache cache;
    char * id;
    char * type_id;
    char * base_type_id;
    char * index_type_id;
    char * pointer_type_id;
    char * owner_id;
    char * name;
    int update_policy;
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
    int disposed;
} SymInfoCache;

/* Cached result of get_array_symbol() */
typedef struct ArraySymCache {
    LINK link_sym;
    AbstractCache cache;
    ContextAddress length;
    ReplyHandlerInfo * pending;
    ErrorReport * error;
    char * id;
    int disposed;
} ArraySymCache;

/* Cached result of find_symbol() */
typedef struct FindSymCache {
    LINK link_syms;
    AbstractCache cache;
    ReplyHandlerInfo * pending;
    ErrorReport * error;
    pid_t pid;
    uint64_t ip;
    char * name;
    char * id;
    int disposed;
} FindSymCache;

/* Cached result of enumerate_symbols() */
typedef struct ListSymCache {
    LINK link_syms;
    AbstractCache cache;
    ReplyHandlerInfo * pending;
    ErrorReport * error;
    pid_t pid;
    uint64_t ip;
    char ** list;
    unsigned list_size;
    unsigned list_max;
    int disposed;
} ListSymCache;

#define SYM_CACHE_MAGIC 0x38254865

#define root2syms(A) ((SymbolsCache *)((char *)(A) - offsetof(SymbolsCache, link_root)))
#define syms2sym(A)  ((SymInfoCache *)((char *)(A) - offsetof(SymInfoCache, link_syms)))
#define syms2find(A) ((FindSymCache *)((char *)(A) - offsetof(FindSymCache, link_syms)))
#define syms2list(A) ((ListSymCache *)((char *)(A) - offsetof(ListSymCache, link_syms)))
#define sym2arr(A)   ((ArraySymCache *)((char *)(A) - offsetof(ArraySymCache, link_sym)))

struct Symbol {
    unsigned magic;
    SymInfoCache * cache;
};

#include "symbols_alloc.h"

static LINK root;

static unsigned hash_sym_id(const char * id) {
    int i;
    unsigned h = 0;
    for (i = 0; id[i]; i++) h += id[i];
    h = h + h / HASH_SIZE;
    return h % HASH_SIZE;
}

static unsigned hash_find(const char * name, uint64_t ip, pid_t pid) {
    int i;
    unsigned h = 0;
    for (i = 0; name[i]; i++) h += name[i];
    h = h + h / HASH_SIZE;
    return (h + (unsigned)ip + (unsigned)pid) % HASH_SIZE;
}

static unsigned hash_list(uint64_t ip, pid_t pid) {
    return ((unsigned)ip + (unsigned)pid) % HASH_SIZE;
}

static SymbolsCache * get_symbols_cache(void) {
    LINK * l = NULL;
    SymbolsCache * syms = NULL;
    Channel * c = cache_channel();
    if (c == NULL) exception(ERR_SYM_NOT_FOUND);
    for (l = root.next; l != &root; l = l->next) {
        SymbolsCache * x = root2syms(l);
        if (x->channel == c) {
            syms = x;
            break;
        }
    }
    if (syms == NULL) {
        int i = 0;
        syms = loc_alloc_zero(sizeof(SymbolsCache));
        syms->channel = c;
        list_add_first(&syms->link_root, &root);
        for (i = 0; i < HASH_SIZE; i++) {
            list_init(syms->link_sym + i);
            list_init(syms->link_find + i);
            list_init(syms->link_list + i);
        }
        channel_lock(c);
    }
    return syms;
}

static void free_arr_sym_cache(ArraySymCache * a) {
    list_remove(&a->link_sym);
    a->disposed = 1;
    if (a->pending == NULL) {
        cache_dispose(&a->cache);
        release_error_report(a->error);
        loc_free(a->id);
        loc_free(a);
    }
}

static void free_sym_info_cache(SymInfoCache * c) {
    int i;
    assert(c->magic == SYM_CACHE_MAGIC);
    list_remove(&c->link_syms);
    c->disposed = 1;
    if (c->pending_get_context == NULL && c->pending_get_children == NULL) {
        cache_dispose(&c->cache);
        loc_free(c->id);
        loc_free(c->type_id);
        loc_free(c->base_type_id);
        loc_free(c->index_type_id);
        loc_free(c->pointer_type_id);
        loc_free(c->owner_id);
        loc_free(c->name);
        loc_free(c->value);
        for (i = 0; i < c->children_count; i++) loc_free(c->children_ids[i]);
        loc_free(c->children_ids);
        release_error_report(c->error_get_context);
        release_error_report(c->error_get_children);
        while (!list_is_empty(&c->array_syms)) {
            free_arr_sym_cache(sym2arr(c->array_syms.next));
        }
        loc_free(c);
    }
}

static void free_find_sym_cache(FindSymCache * c) {
    list_remove(&c->link_syms);
    c->disposed = 1;
    if (c->pending == NULL) {
        cache_dispose(&c->cache);
        release_error_report(c->error);
        loc_free(c->name);
        loc_free(c->id);
        loc_free(c);
    }
}

static void free_list_sym_cache(ListSymCache * c) {
    list_remove(&c->link_syms);
    c->disposed = 1;
    if (c->pending == NULL) {
        unsigned j;
        cache_dispose(&c->cache);
        release_error_report(c->error);
        for (j = 0; j < c->list_size; j++) loc_free(c->list[j]);
        loc_free(c->list);
        loc_free(c);
    }
}

static void free_symbols_cache(SymbolsCache * syms) {
    int i;
    for (i = 0; i < HASH_SIZE; i++) {
        while (!list_is_empty(syms->link_sym + i)) {
            free_sym_info_cache(syms2sym(syms->link_sym[i].next));
        }
        while (!list_is_empty(syms->link_find + i)) {
            free_find_sym_cache(syms2find(syms->link_find[i].next));
        }
        while (!list_is_empty(syms->link_list + i)) {
            free_list_sym_cache(syms2list(syms->link_list[i].next));
        }
    }
    channel_unlock(syms->channel);
    list_remove(&syms->link_root);
    loc_free(syms);
}

static void read_context_data(InputStream * inp, char * name, void * args) {
    char id[256];
    SymInfoCache * s = args;
    if (strcmp(name, "ID") == 0) { json_read_string(inp, id, sizeof(id)); assert(strcmp(id, s->id) == 0); }
    else if (strcmp(name, "OwnerID") == 0) s->owner_id = json_read_alloc_string(inp);
    else if (strcmp(name, "Name") == 0) s->name = json_read_alloc_string(inp);
    else if (strcmp(name, "UpdatePolicy") == 0) s->update_policy = json_read_long(inp);
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
    else json_skip_object(inp);
}

static void validate_context(Channel * c, void * args, int error) {
    SymInfoCache * s = args;
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
    if (s->disposed) free_sym_info_cache(s);
}

static SymInfoCache * get_sym_info_cache(const Symbol * sym) {
    Trap trap;
    SymInfoCache * s = sym->cache;
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
        if (c == NULL) exception(ERR_SYM_NOT_FOUND);
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

static void validate_find(Channel * c, void * args, int error) {
    FindSymCache * f = args;
    assert(f->pending != NULL);
    assert(f->error == NULL);
    f->pending = NULL;
    f->error = get_error_report(error);
    if (!error) {
        f->error = get_error_report(read_errno(&c->inp));
        f->id = json_read_alloc_string(&c->inp);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    }
    assert(f->error != NULL || f->id != NULL);
    cache_notify(&f->cache);
    if (f->disposed) free_find_sym_cache(f);
}

int find_symbol(Context * ctx, int frame, char * name, Symbol ** sym) {
    uint64_t ip = 0;
    LINK * l = NULL;
    SymbolsCache * syms = NULL;
    FindSymCache * f = NULL;
    unsigned h;
    Trap trap;

    if (!set_trap(&trap)) return -1;

    if (frame == STACK_NO_FRAME) {
        while (ctx->parent != NULL && ctx->parent->mem == ctx->mem) ctx = ctx->parent;
    }
    else {
        StackFrame * info = NULL;
        if (get_frame_info(ctx, frame, &info) < 0) exception(errno);
        if (read_reg_value(get_PC_definition(ctx), info, &ip) < 0) exception(errno);
    }

    h = hash_find(name, ip, ctx->pid);
    syms = get_symbols_cache();
    for (l = syms->link_find[h].next; l != syms->link_find + h; l = l->next) {
        FindSymCache * c = syms2find(l);
        if (c->pid == ctx->pid && c->ip == ip && strcmp(c->name, name) == 0) {
            f = c;
            break;
        }
    }

    if (f == NULL) {
        Channel * c = cache_channel();
        if (c == NULL) exception(ERR_SYM_NOT_FOUND);
        f = loc_alloc_zero(sizeof(FindSymCache));
        list_add_first(&f->link_syms, syms->link_find + h);
        f->pid = ctx->pid;
        f->ip = ip;
        f->name = loc_strdup(name);
        f->pending = protocol_send_command(c, "Symbols", "find", validate_find, f);
        if (frame != STACK_NO_FRAME) {
            json_write_string(&c->out, get_stack_frame_id(ctx, frame));
        }
        else {
            json_write_string(&c->out, ctx2id(ctx));
        }
        write_stream(&c->out, 0);
        json_write_string(&c->out, name);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
        cache_wait(&f->cache);
    }
    else if (f->pending != NULL) {
        cache_wait(&f->cache);
    }
    else if (f->error != NULL) {
        char msg[256];
        snprintf(msg, sizeof(msg), "Symbol '%s' not found", name);
        exception(set_errno(set_error_report_errno(f->error), msg));
    }
    else if (id2symbol(f->id, sym) < 0) {
        exception(errno);
    }
    clear_trap(&trap);
    return 0;
}

static void read_sym_list_item(InputStream * inp, void * args) {
    ListSymCache * f = args;
    char * id = json_read_alloc_string(inp);
    if (f->list_size >= f->list_max) {
        f->list_max += 16;
        f->list = loc_realloc(f->list, f->list_max * sizeof(char *));
    }
    f->list[f->list_size++] = id;
}

static void validate_list(Channel * c, void * args, int error) {
    ListSymCache * f = args;
    assert(f->pending != NULL);
    assert(f->error == NULL);
    f->pending = NULL;
    f->error = get_error_report(error);
    if (!error) {
        f->error = get_error_report(read_errno(&c->inp));
        json_read_array(&c->inp, read_sym_list_item, f);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    }
    cache_notify(&f->cache);
    if (f->disposed) free_list_sym_cache(f);
}

int enumerate_symbols(Context * ctx, int frame, EnumerateSymbolsCallBack * func, void * args) {
    uint64_t ip = 0;
    unsigned h;
    LINK * l;
    Trap trap;
    SymbolsCache * syms = NULL;
    ListSymCache * f = NULL;

    if (!set_trap(&trap)) return -1;

    if (frame == STACK_NO_FRAME) {
        while (ctx->parent != NULL && ctx->parent->mem == ctx->mem) ctx = ctx->parent;
    }
    else {
        StackFrame * info = NULL;
        if (get_frame_info(ctx, frame, &info) < 0) exception(errno);
        if (read_reg_value(get_PC_definition(ctx), info, &ip) < 0) exception(errno);
    }

    h = hash_list(ip, ctx->pid);
    syms = get_symbols_cache();
    for (l = syms->link_list[h].next; l != syms->link_list + h; l = l->next) {
        ListSymCache * c = syms2list(l);
        if (c->pid == ctx->pid && c->ip == ip) {
            f = c;
            break;
        }
    }

    if (f == NULL) {
        Channel * c = cache_channel();
        if (c == NULL) exception(ERR_SYM_NOT_FOUND);
        f = loc_alloc_zero(sizeof(ListSymCache));
        list_add_first(&f->link_syms, syms->link_list + h);
        f->pid = ctx->pid;
        f->ip = ip;
        f->pending = protocol_send_command(c, "Symbols", "list", validate_list, f);
        if (frame != STACK_NO_FRAME) {
            json_write_string(&c->out, get_stack_frame_id(ctx, frame));
        }
        else {
            json_write_string(&c->out, ctx2id(ctx));
        }
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
        cache_wait(&f->cache);
    }
    else if (f->pending != NULL) {
        cache_wait(&f->cache);
    }
    else if (f->error != NULL) {
        exception(set_error_report_errno(f->error));
    }
    else {
        unsigned i;
        for (i = 0; i < f->list_size; i++) {
            Symbol * sym = NULL;
            if (id2symbol(f->list[i], &sym) < 0) exception(errno);
            func(args, sym);
        }
    }
    clear_trap(&trap);
    return 0;
}

const char * symbol2id(const Symbol * sym) {
    SymInfoCache * s = sym->cache;
    assert(s->magic == SYM_CACHE_MAGIC);
    assert(s->id != NULL);
    return s->id;
}

int id2symbol(const char * id, Symbol ** sym) {
    LINK * l;
    SymInfoCache * s = NULL;
    unsigned h = hash_sym_id(id);
    SymbolsCache * t = get_symbols_cache();
    for (l = t->link_sym[h].next; l != t->link_sym + h; l = l->next) {
        SymInfoCache * x = syms2sym(l);
        if (strcmp(x->id, id) == 0) {
            s = x;
            break;
        }
    }
    if (s == NULL) {
        s = loc_alloc_zero(sizeof(*s));
        s->magic = SYM_CACHE_MAGIC;
        s->id = loc_strdup(id);
        list_add_first(&s->link_syms, t->link_sym + h);
        list_init(&s->array_syms);
    }
    *sym = alloc_symbol();
    (*sym)->cache = s;
    return 0;
}

/*************** Functions for retrieving symbol properties ***************************************/

int get_symbol_class(const Symbol * sym, int * symbol_class) {
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    *symbol_class = c->sym_class;
    return 0;
}

int get_symbol_type(const Symbol * sym, Symbol ** type) {
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    if (c->type_id) return id2symbol(c->type_id, type);
    return 0;
}

int get_symbol_type_class(const Symbol * sym, int * type_class) {
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    *type_class = c->type_class;
    return 0;
}

int get_symbol_update_policy(const Symbol * sym, char ** id, int * policy) {
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    *id = c->owner_id;
    *policy = c->update_policy;
    return 0;
}

int get_symbol_name(const Symbol * sym, char ** name) {
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    *name = c->name;
    return 0;
}

int get_symbol_base_type(const Symbol * sym, Symbol ** type) {
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    if (c->base_type_id) return id2symbol(c->base_type_id, type);
    return 0;
}

int get_symbol_index_type(const Symbol * sym, Symbol ** type) {
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    if (c->index_type_id) return id2symbol(c->index_type_id, type);
    return 0;
}

int get_symbol_size(const Symbol * sym, ContextAddress * size) {
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    if (!c->has_size) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *size = c->size;
    return 0;
}

int get_symbol_length(const Symbol * sym, ContextAddress * length) {
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    if (!c->has_length) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *length = c->length;
    return 0;
}

int get_symbol_lower_bound(const Symbol * sym, ContextAddress * lower_bound) {
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    if (!c->has_lower_bound) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *lower_bound = c->lower_bound;
    return 0;
}

int get_symbol_offset(const Symbol * sym, ContextAddress * offset) {
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    if (!c->has_offset) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *offset = c->offset;
    return 0;
}

int get_symbol_value(const Symbol * sym, void ** value, size_t * size) {
    SymInfoCache * c = get_sym_info_cache(sym);
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
    SymInfoCache * c = get_sym_info_cache(sym);
    if (c == NULL) return -1;
    if (!c->has_address) {
        errno = ERR_INV_ADDRESS;
        return -1;
    }
    *address = c->address;
    return 0;
}

static void validate_children(Channel * c, void * args, int error) {
    SymInfoCache * s = args;
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
    if (s->disposed) free_sym_info_cache(s);
}

int get_symbol_children(const Symbol * sym, Symbol *** children, int * count) {
    Trap trap;
    SymInfoCache * s = get_sym_info_cache(sym);
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
        if (c == NULL) exception(ERR_SYM_NOT_FOUND);
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
    if (s->disposed) free_arr_sym_cache(s);
}

int get_array_symbol(const Symbol * sym, ContextAddress length, Symbol ** ptr) {
    LINK * l;
    Trap trap;
    ArraySymCache * a = NULL;
    SymInfoCache * s = get_sym_info_cache(sym);
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
        Channel * c = cache_channel();
        if (c == NULL) exception(ERR_SYM_NOT_FOUND);
        a = loc_alloc_zero(sizeof(*a));
        list_add_first(&a->link_sym, &s->array_syms);
        a->length = length;
        a->pending = protocol_send_command(c, "Symbols", "getArrayType", validate_type_id, a);
        json_write_string(&c->out, s->id);
        write_stream(&c->out, 0);
        json_write_int64(&c->out, length);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
        cache_wait(&a->cache);
    }
    else if (a->pending != NULL) {
        cache_wait(&a->cache);
    }
    else if (a->error != NULL) {
        exception(set_error_report_errno(a->error));
    }
    else if (id2symbol(a->id, ptr) < 0) {
        exception(errno);
    }
    clear_trap(&trap);
    return 0;
}

/*************************************************************************************************/

ContextAddress is_plt_section(Context * ctx, ContextAddress addr) {
    /* TODO: is_plt_section() in symbols proxy */
    return 0;
}

static void flush_syms(Context * ctx, int mode, int keep_pending) {
    LINK * l;
    LINK * m;
    char id[256];
    int i;

    strlcpy(id, ctx2id(ctx), sizeof(id));
    for (m = root.next; m != &root; m = m->next) {
        SymbolsCache * syms = root2syms(m);
        for (i = 0; i < HASH_SIZE; i++) {
            l = syms->link_sym[i].next;
            while (l != syms->link_sym + i) {
                SymInfoCache * c = syms2sym(l);
                l = l->next;
                if ((mode & (1 << c->update_policy)) != 0 && strcmp(id, c->owner_id) == 0) {
                    if (keep_pending) {
                        if ((!c->done_context || c->pending_get_context) &&
                            (!c->done_children || c->pending_get_children) &&
                            list_is_empty(&c->array_syms)) continue;
                    }
                    free_sym_info_cache(c);
                }
            }
            l = syms->link_find[i].next;
            while (l != syms->link_find + i) {
                FindSymCache * c = syms2find(l);
                l = l->next;
                if (c->pid == ctx->pid && (c->pending == NULL || !keep_pending))
                    free_find_sym_cache(c);
            }
            l = syms->link_list[i].next;
            while (l != syms->link_list + i) {
                ListSymCache * c = syms2list(l);
                l = l->next;
                if (c->pid == ctx->pid && (c->pending == NULL || !keep_pending))
                    free_list_sym_cache(c);
            }
        }
    }
}

static void event_context_created(Context * ctx, void * x) {
    flush_syms(ctx, (1 << UPDATE_ON_MEMORY_MAP_CHANGES) | (1 << UPDATE_ON_EXE_STATE_CHANGES), 0);
}

static void event_context_exited(Context * ctx, void * x) {
    flush_syms(ctx, (1 << UPDATE_ON_MEMORY_MAP_CHANGES) | (1 << UPDATE_ON_EXE_STATE_CHANGES), 0);
}

static void event_context_stopped(Context * ctx, void * x) {
    flush_syms(ctx, (1 << UPDATE_ON_EXE_STATE_CHANGES), 1);
}

static void event_context_started(Context * ctx, void * x) {
    flush_syms(ctx, (1 << UPDATE_ON_EXE_STATE_CHANGES), 1);
}

static void event_context_changed(Context * ctx, void * x) {
    flush_syms(ctx, (1 << UPDATE_ON_MEMORY_MAP_CHANGES) | (1 << UPDATE_ON_EXE_STATE_CHANGES), 1);
}

static void channel_close_listener(Channel * c) {
    LINK * l = root.next;
    while (l != &root) {
        SymbolsCache * s = root2syms(l);
        l = l->next;
        if (s->channel == c) free_symbols_cache(s);
    }
}

void ini_symbols_lib(void) {
    static ContextEventListener listener = {
        event_context_created,
        event_context_exited,
        event_context_stopped,
        event_context_started,
        event_context_changed
    };
    list_init(&root);
    add_context_event_listener(&listener, NULL);
    add_channel_close_listener(channel_close_listener);
}

#endif
