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
 * Symbols service - proxy implementation, gets symbols information from host.
 */

/* TODO: need to cleanup symbols cache from data that not used for long time */

#include <config.h>

#if ENABLE_SymbolsProxy

#include <assert.h>
#include <stdio.h>
#include <framework/context.h>
#include <framework/cache.h>
#include <framework/json.h>
#include <framework/events.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <services/stacktrace.h>
#include <services/symbols.h>
#if ENABLE_RCBP_TEST
#  include <main/test.h>
#endif

#define HASH_SIZE 101

/* Symbols cahce, one per channel */
typedef struct SymbolsCache {
    Channel * channel;
    LINK link_root;
    LINK link_sym[HASH_SIZE];
    LINK link_find[HASH_SIZE];
    LINK link_list[HASH_SIZE];
    LINK link_frame[HASH_SIZE];
    int service_available;
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
    char * name;
    Context * update_owner;
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
    int64_t lower_bound;
    int64_t upper_bound;
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
    int update_policy;
    Context * ctx;
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
    int update_policy;
    Context * ctx;
    uint64_t ip;
    char ** list;
    unsigned list_size;
    unsigned list_max;
    int disposed;
} ListSymCache;

typedef struct StackFrameCache {
    LINK link_syms;
    AbstractCache cache;
    ReplyHandlerInfo * pending;
    ErrorReport * error;
    Context * ctx;
    uint64_t address;
    uint64_t size;

    StackTracingCommandSequence * fp;
    StackTracingCommandSequence ** regs;
    int regs_cnt;

    int disposed;
} StackFrameCache;

#define SYM_CACHE_MAGIC 0x38254865

#define root2syms(A) ((SymbolsCache *)((char *)(A) - offsetof(SymbolsCache, link_root)))
#define syms2sym(A)  ((SymInfoCache *)((char *)(A) - offsetof(SymInfoCache, link_syms)))
#define syms2find(A) ((FindSymCache *)((char *)(A) - offsetof(FindSymCache, link_syms)))
#define syms2list(A) ((ListSymCache *)((char *)(A) - offsetof(ListSymCache, link_syms)))
#define sym2arr(A)   ((ArraySymCache *)((char *)(A) - offsetof(ArraySymCache, link_sym)))
#define syms2frame(A)((StackFrameCache *)((char *)(A) - offsetof(StackFrameCache, link_syms)))

struct Symbol {
    unsigned magic;
    SymInfoCache * cache;
};

#include <services/symbols_alloc.h>

static LINK root;

static const char * SYMBOLS = "Symbols";

static unsigned hash_sym_id(const char * id) {
    int i;
    unsigned h = 0;
    for (i = 0; id[i]; i++) h += id[i];
    return h % HASH_SIZE;
}

static unsigned hash_find(Context * ctx, const char * name, uint64_t ip) {
    int i;
    unsigned h = 0;
    for (i = 0; name[i]; i++) h += name[i];
    return (h + ((uintptr_t)ctx >> 4) + (unsigned)ip) % HASH_SIZE;
}

static unsigned hash_list(Context * ctx, uint64_t ip) {
    return (((uintptr_t)ctx >> 4) + (unsigned)ip) % HASH_SIZE;
}

static unsigned hash_frame(Context * ctx) {
    return ((uintptr_t)ctx >> 4) % HASH_SIZE;
}

static SymbolsCache * get_symbols_cache(void) {
    LINK * l = NULL;
    SymbolsCache * syms = NULL;
    Channel * c = cache_channel();
    if (c == NULL) {
        str_exception(ERR_OTHER, "get_symbols_cache(): illegal cache access");
    }
    for (l = root.next; l != &root; l = l->next) {
        SymbolsCache * x = root2syms(l);
        if (x->channel == c) {
            syms = x;
            break;
        }
    }
    if (syms == NULL) {
        int i = 0;
        syms = (SymbolsCache *)loc_alloc_zero(sizeof(SymbolsCache));
        syms->channel = c;
        list_add_first(&syms->link_root, &root);
        for (i = 0; i < HASH_SIZE; i++) {
            list_init(syms->link_sym + i);
            list_init(syms->link_find + i);
            list_init(syms->link_list + i);
            list_init(syms->link_frame + i);
        }
        channel_lock(c);
        for (i = 0; i < c->peer_service_cnt; i++) {
            if (strcmp(c->peer_service_list[i], SYMBOLS) == 0) syms->service_available = 1;
        }
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
    assert(c->magic == SYM_CACHE_MAGIC);
    list_remove(&c->link_syms);
    c->disposed = 1;
    if (c->pending_get_context == NULL && c->pending_get_children == NULL) {
        c->magic = 0;
        cache_dispose(&c->cache);
        loc_free(c->id);
        loc_free(c->type_id);
        loc_free(c->base_type_id);
        loc_free(c->index_type_id);
        loc_free(c->pointer_type_id);
        loc_free(c->name);
        loc_free(c->value);
        loc_free(c->children_ids);
        if (c->update_owner != NULL) context_unlock(c->update_owner);
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
        context_unlock(c->ctx);
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
        context_unlock(c->ctx);
        for (j = 0; j < c->list_size; j++) loc_free(c->list[j]);
        loc_free(c->list);
        loc_free(c);
    }
}

static void free_stack_frame_cache(StackFrameCache * c) {
    list_remove(&c->link_syms);
    c->disposed = 1;
    if (c->pending == NULL) {
        int i;
        cache_dispose(&c->cache);
        release_error_report(c->error);
        context_unlock(c->ctx);
        for (i = 0; i < c->regs_cnt; i++) loc_free(c->regs[i]);
        loc_free(c->regs);
        loc_free(c->fp);
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
        while (!list_is_empty(syms->link_frame + i)) {
            free_stack_frame_cache(syms2frame(syms->link_frame[i].next));
        }
    }
    channel_unlock(syms->channel);
    list_remove(&syms->link_root);
    loc_free(syms);
}

static Channel * get_channel(SymbolsCache * syms) {
    if (!syms->service_available) str_exception(ERR_SYM_NOT_FOUND, "Symbols service not available");
    return syms->channel;
}

static void read_context_data(InputStream * inp, const char * name, void * args) {
    char id[256];
    SymInfoCache * s = (SymInfoCache *)args;
    if (strcmp(name, "ID") == 0) { json_read_string(inp, id, sizeof(id)); assert(strcmp(id, s->id) == 0); }
    else if (strcmp(name, "OwnerID") == 0) { json_read_string(inp, id, sizeof(id)); s->update_owner = id2ctx(id); }
    else if (strcmp(name, "Name") == 0) s->name = json_read_alloc_string(inp);
    else if (strcmp(name, "UpdatePolicy") == 0) s->update_policy = json_read_long(inp);
    else if (strcmp(name, "Class") == 0) s->sym_class = json_read_long(inp);
    else if (strcmp(name, "TypeClass") == 0) s->type_class = json_read_long(inp);
    else if (strcmp(name, "TypeID") == 0) s->type_id = json_read_alloc_string(inp);
    else if (strcmp(name, "BaseTypeID") == 0) s->base_type_id = json_read_alloc_string(inp);
    else if (strcmp(name, "IndexTypeID") == 0) s->index_type_id = json_read_alloc_string(inp);
    else if (strcmp(name, "Size") == 0) { s->size = json_read_long(inp); s->has_size = 1; }
    else if (strcmp(name, "Length") == 0) { s->length = json_read_long(inp); s->has_length = 1; }
    else if (strcmp(name, "LowerBound") == 0) { s->lower_bound = json_read_int64(inp); s->has_lower_bound = 1; }
    else if (strcmp(name, "UpperBound") == 0) { s->upper_bound = json_read_int64(inp); s->has_upper_bound = 1; }
    else if (strcmp(name, "Offset") == 0) { s->offset = json_read_long(inp); s->has_offset = 1; }
    else if (strcmp(name, "Address") == 0) { s->address = (ContextAddress)json_read_uint64(inp); s->has_address = 1; }
    else if (strcmp(name, "Value") == 0) s->value = json_read_alloc_binary(inp, &s->value_size);
    else json_skip_object(inp);
}

static void validate_context(Channel * c, void * args, int error) {
    Trap trap;
    SymInfoCache * s = (SymInfoCache *)args;
    assert(s->pending_get_context != NULL);
    assert(s->error_get_context == NULL);
    assert(s->update_owner == NULL);
    assert(!s->done_context);
    if (set_trap(&trap)) {
        s->pending_get_context = NULL;
        s->done_context = 1;
        if (!error) {
            error = read_errno(&c->inp);
            json_read_struct(&c->inp, read_context_data, s);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
            if (!error && s->update_owner == NULL) error = ERR_INV_CONTEXT;
            if (!error && s->update_owner->exited) error = ERR_ALREADY_EXITED;
        }
        clear_trap(&trap);
        if (s->update_owner != NULL) context_lock(s->update_owner);
    }
    else {
        error = trap.error;
        s->update_owner = NULL;
    }
    s->error_get_context = get_error_report(error);
    cache_notify(&s->cache);
    if (s->disposed) free_sym_info_cache(s);
    if (trap.error) exception(trap.error);
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
        s->pending_get_context = protocol_send_command(c, SYMBOLS, "getContext", validate_context, s);
        json_write_string(&c->out, s->id);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        cache_wait(&s->cache);
    }
    clear_trap(&trap);
    return s;
}

static void validate_find(Channel * c, void * args, int error) {
    Trap trap;
    FindSymCache * f = (FindSymCache *)args;
    assert(f->pending != NULL);
    assert(f->error == NULL);
    if (set_trap(&trap)) {
        f->pending = NULL;
        if (!error) {
            error = read_errno(&c->inp);
            f->id = json_read_alloc_string(&c->inp);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    f->error = get_error_report(error);
    assert(f->error != NULL || f->id != NULL);
    cache_notify(&f->cache);
    if (f->disposed) free_find_sym_cache(f);
    if (trap.error) exception(trap.error);
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
        ctx = ctx->mem;
    }
    else {
        StackFrame * info = NULL;
        if (frame == STACK_TOP_FRAME && (frame = get_top_frame(ctx)) < 0) exception(errno);;
        if (get_frame_info(ctx, frame, &info) < 0) exception(errno);
        if (read_reg_value(info, get_PC_definition(ctx), &ip) < 0) exception(errno);
    }

    h = hash_find(ctx, name, ip);
    syms = get_symbols_cache();
    for (l = syms->link_find[h].next; l != syms->link_find + h; l = l->next) {
        FindSymCache * c = syms2find(l);
        if (c->ctx == ctx && c->ip == ip && strcmp(c->name, name) == 0) {
            f = c;
            break;
        }
    }

#if ENABLE_RCBP_TEST
    if (f == NULL && !syms->service_available) {
        void * address = NULL;
        int sym_class = 0;
        if (find_test_symbol(ctx, name, &address, &sym_class) >= 0) {
            char bf[256];
            f = (FindSymCache *)loc_alloc_zero(sizeof(FindSymCache));
            list_add_first(&f->link_syms, syms->link_find + h);
            context_lock(f->ctx = ctx);
            f->name = loc_strdup(name);
            f->ip = ip;
            f->update_policy = UPDATE_ON_MEMORY_MAP_CHANGES;
            snprintf(bf, sizeof(bf), "TEST.%X.%"PRIX64".%s", sym_class,
                    (uint64_t)(uintptr_t)address, ctx->mem->id);
            f->id = loc_strdup(bf);
        }
    }
#endif

    if (f == NULL) {
        Channel * c = get_channel(syms);
        f = (FindSymCache *)loc_alloc_zero(sizeof(FindSymCache));
        list_add_first(&f->link_syms, syms->link_find + h);
        context_lock(f->ctx = ctx);
        f->ip = ip;
        f->name = loc_strdup(name);
        f->update_policy = ip ? UPDATE_ON_EXE_STATE_CHANGES : UPDATE_ON_MEMORY_MAP_CHANGES;
        f->pending = protocol_send_command(c, SYMBOLS, "find", validate_find, f);
        if (frame != STACK_NO_FRAME) {
            json_write_string(&c->out, frame2id(ctx, frame));
        }
        else {
            json_write_string(&c->out, ctx->id);
        }
        write_stream(&c->out, 0);
        json_write_string(&c->out, name);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
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
    ListSymCache * f = (ListSymCache *)args;
    char * id = json_read_alloc_string(inp);
    if (f->list_size >= f->list_max) {
        f->list_max += 16;
        f->list = (char **)loc_realloc(f->list, f->list_max * sizeof(char *));
    }
    f->list[f->list_size++] = id;
}

static void validate_list(Channel * c, void * args, int error) {
    Trap trap;
    ListSymCache * f = (ListSymCache *)args;
    assert(f->pending != NULL);
    assert(f->error == NULL);
    if (set_trap(&trap)) {
        f->pending = NULL;
        if (!error) {
            error = read_errno(&c->inp);
            json_read_array(&c->inp, read_sym_list_item, f);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    f->error = get_error_report(error);
    cache_notify(&f->cache);
    if (f->disposed) free_list_sym_cache(f);
    if (trap.error) exception(trap.error);
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
        ctx = ctx->mem;
    }
    else {
        StackFrame * info = NULL;
        if (frame == STACK_TOP_FRAME && (frame = get_top_frame(ctx)) < 0) exception(errno);;
        if (get_frame_info(ctx, frame, &info) < 0) exception(errno);
        if (read_reg_value(info, get_PC_definition(ctx), &ip) < 0) exception(errno);
    }

    h = hash_list(ctx, ip);
    syms = get_symbols_cache();
    for (l = syms->link_list[h].next; l != syms->link_list + h; l = l->next) {
        ListSymCache * c = syms2list(l);
        if (c->ctx == ctx && c->ip == ip) {
            f = c;
            break;
        }
    }

    if (f == NULL) {
        Channel * c = get_channel(syms);
        f = (ListSymCache *)loc_alloc_zero(sizeof(ListSymCache));
        list_add_first(&f->link_syms, syms->link_list + h);
        context_lock(f->ctx = ctx);
        f->ip = ip;
        f->update_policy = ip ? UPDATE_ON_EXE_STATE_CHANGES : UPDATE_ON_MEMORY_MAP_CHANGES;
        f->pending = protocol_send_command(c, SYMBOLS, "list", validate_list, f);
        if (frame != STACK_NO_FRAME) {
            json_write_string(&c->out, frame2id(ctx, frame));
        }
        else {
            json_write_string(&c->out, ctx->id);
        }
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
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
    SymbolsCache * syms = get_symbols_cache();
    for (l = syms->link_sym[h].next; l != syms->link_sym + h; l = l->next) {
        SymInfoCache * x = syms2sym(l);
        if (strcmp(x->id, id) == 0) {
            s = x;
            break;
        }
    }
    if (s == NULL) {
        s = (SymInfoCache *)loc_alloc_zero(sizeof(SymInfoCache));
        s->magic = SYM_CACHE_MAGIC;
        s->id = loc_strdup(id);
        list_add_first(&s->link_syms, syms->link_sym + h);
        list_init(&s->array_syms);
#if ENABLE_RCBP_TEST
        if (strncmp(id, "TEST.", 5) == 0) {
            int sym_class = 0;
            uint64_t address = 0;
            char ctx_id[256];
            if (sscanf(id, "TEST.%X.%"SCNx64".%255s", &sym_class, &address, ctx_id) == 3) {
                s->done_context = 1;
                s->has_address = 1;
                s->address = (ContextAddress)address;
                s->sym_class = sym_class;
                s->update_policy = UPDATE_ON_MEMORY_MAP_CHANGES;
                s->update_owner = id2ctx(ctx_id);
                if (s->update_owner != NULL) context_lock(s->update_owner);
            }
        }
#endif
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
    if (c->update_owner == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *id = c->update_owner->id;
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
    if (c->has_length) {
        *length = c->length;
        return 0;
    }
    if (c->has_lower_bound && c->has_upper_bound) {
        *length = (ContextAddress)(c->has_upper_bound - c->has_lower_bound + 1);
        return 0;
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}

int get_symbol_lower_bound(const Symbol * sym, int64_t * lower_bound) {
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
    Trap trap;
    SymInfoCache * s = (SymInfoCache *)args;
    assert(s->pending_get_children != NULL);
    assert(s->error_get_children == NULL);
    assert(!s->done_children);
    if (set_trap(&trap)) {
        s->pending_get_children = NULL;
        s->done_children = 1;
        if (!error) {
            error = read_errno(&c->inp);
            s->children_ids = json_read_alloc_string_array(&c->inp, &s->children_count);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    s->error_get_children = get_error_report(error);
    cache_notify(&s->cache);
    if (s->disposed) free_sym_info_cache(s);
    if (trap.error) exception(trap.error);
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
        s->pending_get_children = protocol_send_command(c, SYMBOLS, "getChildren", validate_children, s);
        json_write_string(&c->out, s->id);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        cache_wait(&s->cache);
    }
    else if (s->children_count > 0) {
        int i, cnt = s->children_count;
        static Symbol ** buf = NULL;
        static int buf_len = 0;
        if (buf_len < cnt) {
            buf_len = cnt;
            buf = (Symbol **)loc_realloc(buf, cnt * sizeof(Symbol *));
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
    Trap trap;
    ArraySymCache * s = (ArraySymCache *)args;
    assert(s->pending != NULL);
    assert(s->error == NULL);
    assert(s->id == NULL);
    if (set_trap(&trap)) {
        s->pending = NULL;
        if (!error) {
            error = read_errno(&c->inp);
            s->id = json_read_alloc_string(&c->inp);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    s->error = get_error_report(error);
    cache_notify(&s->cache);
    if (s->disposed) free_arr_sym_cache(s);
    if (trap.error) exception(trap.error);
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
        a = (ArraySymCache *)loc_alloc_zero(sizeof(*a));
        list_add_first(&a->link_sym, &s->array_syms);
        a->length = length;
        a->pending = protocol_send_command(c, SYMBOLS, "getArrayType", validate_type_id, a);
        json_write_string(&c->out, s->id);
        write_stream(&c->out, 0);
        json_write_uint64(&c->out, length);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
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

static int trace_cmds_cnt = 0;
static int trace_cmds_max = 0;
static StackTracingCommand * trace_cmds = NULL;

static int trace_regs_cnt = 0;
static int trace_regs_max = 0;
static StackTracingCommandSequence ** trace_regs = NULL;

static int trace_error = 0;

ContextAddress is_plt_section(Context * ctx, ContextAddress addr) {
    /* TODO: is_plt_section() in symbols proxy */
    return 0;
}

static void read_stack_trace_command(InputStream * inp, void * args) {
    char id[256];
    Context * ctx = NULL;
    int frame = STACK_NO_FRAME;
    StackTracingCommand * cmd = NULL;
    if (trace_cmds_cnt >= trace_cmds_max) {
        trace_cmds_max += 16;
        trace_cmds = (StackTracingCommand *)loc_realloc(trace_cmds, trace_cmds_max * sizeof(StackTracingCommand));
    }
    cmd = trace_cmds + trace_cmds_cnt++;
    memset(cmd, 0, sizeof(*cmd));
    cmd->cmd = json_read_long(inp);
    switch (cmd->cmd) {
    case SFT_CMD_NUMBER:
        if (read_stream(inp) != ',') exception(ERR_JSON_SYNTAX);
        cmd->num = json_read_int64(inp);
        break;
    case SFT_CMD_REGISTER:
        if (read_stream(inp) != ',') exception(ERR_JSON_SYNTAX);
        json_read_string(inp, id, sizeof(id));
        if (id2register(id, &ctx, &frame, &cmd->reg) < 0) trace_error = errno;
        break;
    case SFT_CMD_DEREF:
        if (read_stream(inp) != ',') exception(ERR_JSON_SYNTAX);
        cmd->size = json_read_ulong(inp);
        if (read_stream(inp) != ',') exception(ERR_JSON_SYNTAX);
        cmd->big_endian = json_read_boolean(inp);
        break;
    }
}

static void read_stack_trace_register(InputStream * inp, const char * id, void * args) {
    if (trace_regs_cnt >= trace_regs_max) {
        trace_regs_max += 16;
        trace_regs = (StackTracingCommandSequence **)loc_realloc(trace_regs, trace_regs_max * sizeof(StackTracingCommandSequence *));
    }
    trace_cmds_cnt = 0;
    if (json_read_array(inp, read_stack_trace_command, NULL)) {
        Context * ctx = NULL;
        int frame = STACK_NO_FRAME;
        StackTracingCommandSequence * reg = (StackTracingCommandSequence *)loc_alloc(
            sizeof(StackTracingCommandSequence) + (trace_cmds_cnt - 1) * sizeof(StackTracingCommand));
        if (id2register(id, &ctx, &frame, &reg->reg) < 0) {
            trace_error = errno;
            loc_free(reg);
        }
        else {
            reg->cmds_cnt = trace_cmds_cnt;
            reg->cmds_max = trace_cmds_cnt;
            memcpy(reg->cmds, trace_cmds, trace_cmds_cnt * sizeof(StackTracingCommand));
            trace_regs[trace_regs_cnt++] = reg;
        }
    }
}

static void validate_frame(Channel * c, void * args, int error) {
    Trap trap;
    StackFrameCache * f = (StackFrameCache *)args;
    assert(f->pending != NULL);
    assert(f->error == NULL);
    if (set_trap(&trap)) {
        f->pending = NULL;
        if (!error) {
            uint64_t addr, size;
            trace_error = 0;
            error = read_errno(&c->inp);
            addr = json_read_uint64(&c->inp);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            size = json_read_uint64(&c->inp);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (!error && addr != 0 && size != 0) {
                f->address = addr;
                f->size = size;
            }
            trace_cmds_cnt = 0;
            if (json_read_array(&c->inp, read_stack_trace_command, NULL)) {
                f->fp = (StackTracingCommandSequence *)loc_alloc(sizeof(StackTracingCommandSequence) + (trace_cmds_cnt - 1) * sizeof(StackTracingCommand));
                f->fp->reg = NULL;
                f->fp->cmds_cnt = trace_cmds_cnt;
                f->fp->cmds_max = trace_cmds_cnt;
                memcpy(f->fp->cmds, trace_cmds, trace_cmds_cnt * sizeof(StackTracingCommand));
            }
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            trace_regs_cnt = 0;
            if (json_read_struct(&c->inp, read_stack_trace_register, NULL)) {
                f->regs_cnt = trace_regs_cnt;
                f->regs = (StackTracingCommandSequence **)loc_alloc(trace_regs_cnt * sizeof(StackTracingCommandSequence *));
                memcpy(f->regs, trace_regs, trace_regs_cnt * sizeof(StackTracingCommandSequence *));
            }
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
            if (!error && trace_error) error = trace_error;
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    if (get_error_code(error) != ERR_INV_COMMAND) f->error = get_error_report(error);
    cache_notify(&f->cache);
    if (f->disposed) free_stack_frame_cache(f);
    if (trap.error) exception(trap.error);
}

int get_next_stack_frame(StackFrame * frame, StackFrame * down) {
    Trap trap;
    unsigned h;
    LINK * l;
    uint64_t ip = 0;
    Context * ctx = frame->ctx;
    SymbolsCache * syms = NULL;
    StackFrameCache * f = NULL;

    if (!set_trap(&trap)) return -1;

    if (read_reg_value(frame, get_PC_definition(ctx), &ip) < 0) {
        if (frame->is_top_frame) exception(errno);
        clear_trap(&trap);
        return 0;
    }

    h = hash_frame(ctx->mem);
    syms = get_symbols_cache();
    for (l = syms->link_frame[h].next; l != syms->link_frame + h; l = l->next) {
        StackFrameCache * c = syms2frame(l);
        /* Here we assume that stack tracing info is valid for all threads in same memory space */
        if (c->ctx == ctx->mem) {
            if (c->pending != NULL) {
                cache_wait(&c->cache);
            }
            else if (c->address <= ip && c->address + c->size > ip) {
                f = c;
                break;
            }
        }
    }

    assert(f == NULL || f->pending == NULL);

    if (f == NULL && !syms->service_available) {
        /* nothing */
    }
    else if (f == NULL) {
        Channel * c = get_channel(syms);
        f = (StackFrameCache *)loc_alloc_zero(sizeof(StackFrameCache));
        list_add_first(&f->link_syms, syms->link_frame + h);
        context_lock(f->ctx = ctx->mem);
        f->address = ip;
        f->size = 1;
        f->pending = protocol_send_command(c, SYMBOLS, "findFrameInfo", validate_frame, f);
        json_write_string(&c->out, f->ctx->id);
        write_stream(&c->out, 0);
        json_write_uint64(&c->out, ip);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        cache_wait(&f->cache);
    }
    else if (f->error != NULL) {
        exception(set_error_report_errno(f->error));
    }
    else if (f->fp != NULL) {
        int i;
        frame->fp = (ContextAddress)evaluate_stack_trace_commands(ctx, frame, f->fp);
        for (i = 0; i < f->regs_cnt; i++) {
            uint64_t v = evaluate_stack_trace_commands(ctx, frame, f->regs[i]);
            if (write_reg_value(down, f->regs[i]->reg, v) < 0) exception(errno);
        }
    }

    clear_trap(&trap);
    return 0;
}

/*************************************************************************************************/

static void flush_syms(Context * ctx, int mode) {
    LINK * l;
    LINK * m;
    int i;

    for (m = root.next; m != &root; m = m->next) {
        SymbolsCache * syms = root2syms(m);
        for (i = 0; i < HASH_SIZE; i++) {
            l = syms->link_sym[i].next;
            while (l != syms->link_sym + i) {
                SymInfoCache * c = syms2sym(l);
                l = l->next;
                if (!c->done_context || c->error_get_context != NULL) {
                    free_sym_info_cache(c);
                }
                else if (c->update_policy == 0 || c->update_owner == NULL || c->update_owner->exited) {
                    free_sym_info_cache(c);
                }
                else if ((mode & (1 << c->update_policy)) && ctx == c->update_owner) {
                    free_sym_info_cache(c);
                }
            }
            l = syms->link_find[i].next;
            while (l != syms->link_find + i) {
                FindSymCache * c = syms2find(l);
                l = l->next;
                if ((mode & (1 << c->update_policy)) && c->ctx == ctx) {
                    free_find_sym_cache(c);
                }
            }
            l = syms->link_list[i].next;
            while (l != syms->link_list + i) {
                ListSymCache * c = syms2list(l);
                l = l->next;
                if ((mode & (1 << c->update_policy)) && c->ctx == ctx) {
                    free_list_sym_cache(c);
                }
            }
            if (mode & (1 << UPDATE_ON_MEMORY_MAP_CHANGES)) {
                l = syms->link_frame[i].next;
                while (l != syms->link_frame + i) {
                    StackFrameCache * c = syms2frame(l);
                    l = l->next;
                    if (c->ctx == ctx->mem) free_stack_frame_cache(c);
                }
            }
        }
    }
}

static void event_context_created(Context * ctx, void * x) {
    flush_syms(ctx, ~0);
}

static void event_context_exited(Context * ctx, void * x) {
    flush_syms(ctx, ~0);
}

static void event_context_stopped(Context * ctx, void * x) {
    flush_syms(ctx, (1 << UPDATE_ON_EXE_STATE_CHANGES));
}

static void event_context_started(Context * ctx, void * x) {
    flush_syms(ctx, (1 << UPDATE_ON_EXE_STATE_CHANGES));
}

static void event_context_changed(Context * ctx, void * x) {
    flush_syms(ctx, (1 << UPDATE_ON_MEMORY_MAP_CHANGES) | (1 << UPDATE_ON_EXE_STATE_CHANGES));
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
