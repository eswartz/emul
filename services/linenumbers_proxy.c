/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
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
 * TCF service line Numbers - proxy version.
 *
 * The service associates locations in the source files with the corresponding
 * machine instruction addresses in the executable object.
 */

#include <config.h>

#if ENABLE_LineNumbersProxy

#include <assert.h>
#include <stdio.h>
#include <framework/context.h>
#include <framework/cache.h>
#include <framework/json.h>
#include <framework/events.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <services/linenumbers.h>

#define HASH_SIZE 511

/* Line numbers cahce, one per channel */
typedef struct LineNumbersCache {
    unsigned magic;
    Channel * channel;
    LINK link_root;
    LINK link_addr[HASH_SIZE];
} LineNumbersCache;

/* Line number to address translation cache */
typedef struct LineAddressCache {
    unsigned magic;
    LINK link_cache;
    AbstractCache cache;
    Context * ctx;
    char * file;
    int line;
    int column;
    ReplyHandlerInfo * pending;
    ErrorReport * error;
    int areas_cnt;
    CodeArea * areas;
    int disposed;
} LineAddressCache;

#define LINE_NUMBERS_CACHE_MAGIC 0x19873654

#define root2cache(A) ((LineNumbersCache *)((char *)(A) - offsetof(LineNumbersCache, link_root)))
#define cache2addr(A) ((LineAddressCache *)((char *)(A) - offsetof(LineAddressCache, link_cache)))

static LINK root;

static int code_area_cnt = 0;
static int code_area_max = 0;
static CodeArea * code_area_buf = NULL;

static void free_line_address_cache(LineAddressCache * cache) {
    assert(cache->magic == LINE_NUMBERS_CACHE_MAGIC);
    list_remove(&cache->link_cache);
    cache->disposed = 1;
    if (cache->pending == NULL) {
        int i;
        cache->magic = 0;
        cache_dispose(&cache->cache);
        release_error_report(cache->error);
        context_unlock(cache->ctx);
        for (i = 0; i < cache->areas_cnt; i++) {
            CodeArea * area = cache->areas + i;
            loc_free(area->file);
            loc_free(area->directory);
        }
        loc_free(cache->areas);
        loc_free(cache->file);
        loc_free(cache);
    }
}

static void free_line_numbers_cache(LineNumbersCache * cache) {
    int i;
    assert(cache->magic == LINE_NUMBERS_CACHE_MAGIC);
    cache->magic = 0;
    for (i = 0; i < HASH_SIZE; i++) {
        while (!list_is_empty(cache->link_addr + i)) {
            free_line_address_cache(cache2addr(cache->link_addr[i].next));
        }
    }
    channel_unlock(cache->channel);
    list_remove(&cache->link_root);
    loc_free(cache);
}

static LineNumbersCache * get_line_numbers_cache(void) {
    LINK * l = NULL;
    LineNumbersCache * cache = NULL;
    Channel * c = cache_channel();
    if (c == NULL) exception(ERR_SYM_NOT_FOUND);
    for (l = root.next; l != &root; l = l->next) {
        LineNumbersCache * x = root2cache(l);
        if (x->channel == c) {
            cache = x;
            break;
        }
    }
    if (cache == NULL) {
        int i = 0;
        cache = (LineNumbersCache *)loc_alloc_zero(sizeof(LineNumbersCache));
        cache->magic = LINE_NUMBERS_CACHE_MAGIC;
        cache->channel = c;
        list_add_first(&cache->link_root, &root);
        for (i = 0; i < HASH_SIZE; i++) {
            list_init(cache->link_addr + i);
        }
        channel_lock(c);
    }
    return cache;
}

static unsigned hash_addr(Context * ctx, const char * file, int line, int column) {
    int i;
    unsigned h = 0;
    for (i = 0; file[i]; i++) h += file[i];
    return (h + ((uintptr_t)ctx >> 4) + (unsigned)line + (unsigned)column) % HASH_SIZE;
}

static void read_code_area_props(InputStream * inp, const char * name, void * args) {
    CodeArea * area = (CodeArea *)args;
    if (strcmp(name, "SLine") == 0) area->start_line = json_read_long(inp);
    else if (strcmp(name, "SCol") == 0) area->start_column = json_read_long(inp);
    else if (strcmp(name, "SAddr") == 0) area->start_address = (ContextAddress)json_read_uint64(inp);
    else if (strcmp(name, "ELine") == 0) area->end_line = json_read_long(inp);
    else if (strcmp(name, "ECol") == 0) area->end_column = json_read_long(inp);
    else if (strcmp(name, "EAddr") == 0) area->end_address = (ContextAddress)json_read_uint64(inp);
    else if (strcmp(name, "File") == 0) area->file = json_read_alloc_string(inp);
    else if (strcmp(name, "Dir") == 0) area->directory = json_read_alloc_string(inp);
    else if (strcmp(name, "ISA") == 0) area->isa = json_read_long(inp);
    else if (strcmp(name, "IsStmt") == 0) area->is_statement = json_read_boolean(inp);
    else if (strcmp(name, "BasicBlock") == 0) area->basic_block = json_read_boolean(inp);
    else if (strcmp(name, "PrologueEnd") == 0) area->prologue_end = json_read_boolean(inp);
    else if (strcmp(name, "EpilogueBegin") == 0) area->epilogue_begin = json_read_boolean(inp);
}

static void read_code_area_array(InputStream * inp, void * args) {
    CodeArea * area = NULL;
    if (code_area_cnt >= code_area_max) {
        code_area_max += 8;
        code_area_buf = (CodeArea *)loc_realloc(code_area_buf, sizeof(CodeArea) * code_area_max);
    }
    area = code_area_buf + code_area_cnt++;
    memset(area, 0, sizeof(CodeArea));
    json_read_struct(inp, read_code_area_props, area);
}

static void validate_map_to_memory(Channel * c, void * args, int error) {
    Trap trap;
    LineAddressCache * f = (LineAddressCache *)args;
    assert(f->magic == LINE_NUMBERS_CACHE_MAGIC);
    assert(f->pending != NULL);
    assert(f->error == NULL);
    if (set_trap(&trap)) {
        f->pending = NULL;
        if (!error) {
            error = read_errno(&c->inp);
            code_area_cnt = 0;
            json_read_array(&c->inp, read_code_area_array, NULL);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
            if (code_area_cnt > 0) {
                f->areas_cnt = code_area_cnt;
                f->areas = (CodeArea *)loc_alloc(sizeof(CodeArea) * code_area_cnt);
                memcpy(f->areas, code_area_buf, sizeof(CodeArea) * code_area_cnt);
            }
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    f->error = get_error_report(error);
    cache_notify(&f->cache);
    if (f->disposed) free_line_address_cache(f);
    if (trap.error) exception(trap.error);
}

int line_to_address(Context * ctx, char * file, int line, int column, LineNumbersCallBack * client, void * args) {
    LINK * l = NULL;
    LineNumbersCache * cache = NULL;
    LineAddressCache * f = NULL;
    unsigned h;
    Trap trap;

    if (!set_trap(&trap)) return -1;

    ctx = ctx->mem;
    h = hash_addr(ctx, file, line, column);
    cache = get_line_numbers_cache();
    assert(cache->magic == LINE_NUMBERS_CACHE_MAGIC);
    for (l = cache->link_addr[h].next; l != cache->link_addr + h; l = l->next) {
        LineAddressCache * c = cache2addr(l);
        if (c->ctx == ctx && c->line == line && c->column == column && strcmp(c->file, file) == 0) {
            assert(c->magic == LINE_NUMBERS_CACHE_MAGIC);
            f = c;
            break;
        }
    }

    if (f == NULL) {
        Channel * c = cache_channel();
        if (c == NULL) exception(ERR_UNSUPPORTED);
        f = (LineAddressCache *)loc_alloc_zero(sizeof(LineAddressCache));
        list_add_first(&f->link_cache, cache->link_addr + h);
        f->magic = LINE_NUMBERS_CACHE_MAGIC;
        context_lock(f->ctx = ctx);
        f->file = loc_strdup(file);
        f->line = line;
        f->column = column;
        f->pending = protocol_send_command(c, "LineNumbers", "mapToMemory", validate_map_to_memory, f);
        json_write_string(&c->out, ctx->id);
        write_stream(&c->out, 0);
        json_write_string(&c->out, file);
        write_stream(&c->out, 0);
        json_write_long(&c->out, line);
        write_stream(&c->out, 0);
        json_write_long(&c->out, column);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        cache_wait(&f->cache);
    }
    else if (f->pending != NULL) {
        cache_wait(&f->cache);
    }
    else if (f->error != NULL) {
        char msg[FILE_PATH_SIZE + 64];
        snprintf(msg, sizeof(msg), "Text position '%s:%d' not found", file, line);
        exception(set_errno(set_error_report_errno(f->error), msg));
    }
    else {
        int i;
        for (i = 0; i < f->areas_cnt; i++) {
            client(f->areas + i, args);
        }
    }
    clear_trap(&trap);
    return 0;
}

static void flush_cache(Context * ctx) {
    LINK * l;
    LINK * m;
    int i;

    for (m = root.next; m != &root; m = m->next) {
        LineNumbersCache * cache = root2cache(m);
        for (i = 0; i < HASH_SIZE; i++) {
            l = cache->link_addr[i].next;
            while (l != cache->link_addr + i) {
                LineAddressCache * c = cache2addr(l);
                l = l->next;
                if (c->ctx == ctx) free_line_address_cache(c);
            }
        }
    }
}

static void event_context_created(Context * ctx, void * x) {
    if (ctx == ctx->mem) flush_cache(ctx);
}

static void event_context_exited(Context * ctx, void * x) {
    if (ctx == ctx->mem) flush_cache(ctx);
}

static void event_context_changed(Context * ctx, void * x) {
    flush_cache(ctx->mem);
}

static void channel_close_listener(Channel * c) {
    LINK * l = root.next;
    while (l != &root) {
        LineNumbersCache * cache = root2cache(l);
        l = l->next;
        if (cache->channel == c) free_line_numbers_cache(cache);
    }
}

void ini_line_numbers_lib(void) {
    static ContextEventListener listener = {
        event_context_created,
        event_context_exited,
        NULL,
        NULL,
        event_context_changed
    };
    list_init(&root);
    add_context_event_listener(&listener, NULL);
    add_channel_close_listener(channel_close_listener);
}

#endif /* ENABLE_LineNumbersProxy */

