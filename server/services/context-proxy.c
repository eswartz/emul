/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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
 * This module forwards handling of process/thread OS contexts to remote peer.
 */

#include "config.h"

#if ENABLE_DebugContext && ENABLE_ContextProxy

#include <errno.h>
#include <assert.h>
#include "stdio.h"
#include "context.h"
#include "myalloc.h"
#include "exceptions.h"
#include "protocol.h"
#include "json.h"
#include "cache.h"
#include "memorymap.h"
#include "context-proxy.h"

typedef struct ContextCache ContextCache;
typedef struct MemoryCache MemoryCache;
typedef struct PeerCache PeerCache;

struct ContextCache {
    char id[256];
    char parent_id[256];
    Context * ctx;
    PeerCache * peer;
    AbstractCache cache;

    /* Memory Map */
    ErrorReport * mem_regions_error;
    unsigned mem_regions_cnt;
    MemoryRegion * mem_regions;
    ReplyHandlerInfo * pending_get_map;

    /* Register definitions */
    ErrorReport * reg_error;
    unsigned reg_cnt;
    char ** reg_ids;
    char * reg_ids_str;
    RegisterDefinition * reg_defs;
    RegisterDefinition * pc_def;
    ReplyHandlerInfo * pending_get_regs;

    /* Run Control Properties */
    int has_state;
    int is_container;
    int can_suspend;
    long can_resume;
    long can_count;
    int can_terminate;
    
    /* Run Control State */
    int state_valid;
    int is_suspended;
    int pc_valid;
    int64_t suspend_pc;
    char suspend_reason[256];

    /* Memory */
    LINK mem_cache;
};

struct MemoryCache {
    LINK link;
    AbstractCache cache;
    int canceled;
    ErrorReport * error;
    ContextAddress addr;
    void * buf;
    size_t size;
    ReplyHandlerInfo * pending_command;
};

struct PeerCache {
    LINK link_all;
    Channel * host;
    Channel * target;
    ForwardingInputStream fwd;
    InputStream * fwd_inp;
};

#define peers2peer(A)    ((PeerCache *)((char *)(A) - offsetof(PeerCache, link_all)))
#define mems2mem(A)      ((MemoryCache *)((char *)(A) - offsetof(MemoryCache, link)))

static LINK peers;

static MemoryRegion * mem_buf = NULL;
static unsigned mem_buf_max = 0;
static unsigned mem_buf_pos = 0;

static unsigned * ids_buf = NULL;
static unsigned ids_buf_max = 0;
static unsigned ids_buf_pos = 0;

static char * str_buf = NULL;
static unsigned str_buf_max = 0;
static unsigned str_buf_pos = 0;

static const char RUN_CONTROL[] = "RunControl";

static char * map_to_local_file(char * file_name) {
    return file_name;
}

static void read_memory_region_property(InputStream * inp, char * name, void * args) {
    MemoryRegion * m = (MemoryRegion *)args;
    if (strcmp(name, "Addr") == 0) m->addr = (ContextAddress)json_read_int64(inp);
    else if (strcmp(name, "Size") == 0) m->size = json_read_ulong(inp);
    else if (strcmp(name, "Offs") == 0) m->file_offs = json_read_ulong(inp);
    else if (strcmp(name, "Flags") == 0) m->flags = json_read_ulong(inp);
    else if (strcmp(name, "FileName") == 0) m->file_name = json_read_alloc_string(inp);
    else loc_free(json_skip_object(inp));
}

static void read_memory_map_item(InputStream * inp, void * args) {
    MemoryRegion * m;
    if (mem_buf_pos >= mem_buf_max) {
        mem_buf_max = mem_buf_max == 0 ? 16 : mem_buf_max * 2;
        mem_buf = loc_realloc(mem_buf, sizeof(MemoryRegion) * mem_buf_max);
    }
    m = mem_buf + mem_buf_pos;
    memset(m, 0, sizeof(MemoryRegion));
    if (json_read_struct(inp, read_memory_region_property, m) && m->file_name != NULL) {
        struct stat buf;
        char * fnm = map_to_local_file(m->file_name);
        if (fnm != m->file_name) {
            loc_free(m->file_name);
            m->file_name = fnm == NULL ? NULL : loc_strdup(fnm);
        }
        if (fnm == NULL || stat(fnm, &buf) < 0) {
            loc_free(m->file_name);
        }
        else {
            m->dev = buf.st_dev;
            m->ino = buf.st_ino;
            mem_buf_pos++;
        }
    }
}

static void read_ids_item(InputStream * inp, void * args) {
    int n;
    char id[256];
    if (ids_buf_pos >= ids_buf_max) {
        ids_buf_max = ids_buf_max == 0 ? 16 : ids_buf_max * 2;
        ids_buf = loc_realloc(ids_buf, sizeof(unsigned) * ids_buf_max);
    }
    n = json_read_string(inp, id, sizeof(id));
    if (n <= 0) return;
    n++;
    if (n > sizeof(id)) n = sizeof(id);
    if (str_buf_pos + n > str_buf_max) {
        str_buf_max = str_buf_max == 0 ? sizeof(id) : str_buf_max * 2;
        str_buf = loc_realloc(str_buf, str_buf_max);
    }
    memcpy(str_buf + str_buf_pos, id, n);
    ids_buf[ids_buf_pos++] = str_buf_pos;
    str_buf_pos += n;
}

static int validate_context_cache(Channel * c, void * args, int error) {
    ContextCache * cache = (ContextCache *)args;

    assert(cache->peer->target == c);
    if (cache->ctx->parent == NULL) {
        /* Get memory map */
        if (cache->pending_get_map != NULL) {
            assert(cache->mem_regions == NULL);
            cache->mem_regions_error = get_error_report(error);
            if (!error) {
                cache->mem_regions_error = get_error_report(read_errno(&c->inp));
                if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
                mem_buf_pos = 0;
                json_read_array(&c->inp, read_memory_map_item, NULL);
                cache->mem_regions_cnt = mem_buf_pos;
                cache->mem_regions = loc_alloc(sizeof(MemoryRegion) * mem_buf_pos);
                memcpy(cache->mem_regions, mem_buf, sizeof(MemoryRegion) * mem_buf_pos);
                if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
                if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
            }
        }
        else if (cache->mem_regions == NULL && cache->mem_regions_error == 0) {
            cache->pending_get_map = protocol_send_command(c, "MemoryMap", "get", validate_context_cache, args);
            write_stringz(&c->out, container_id(cache->ctx));
            write_stream(&c->out, MARKER_EOM);
            flush_stream(&c->out);
            return 0;
        }
    }
    else {
        /* Get register descriptions */
        if (cache->pending_get_regs != NULL) {
            assert(cache->reg_ids == NULL);
            assert(cache->reg_ids_str == NULL);
            cache->reg_error = get_error_report(error);
            if (!error) {
                unsigned i;
                cache->reg_error = get_error_report(read_errno(&c->inp));
                if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
                ids_buf_pos = 0;
                str_buf_pos = 0;
                json_read_array(&c->inp, read_ids_item, NULL);
                cache->reg_cnt = ids_buf_pos;
                cache->reg_ids = loc_alloc(sizeof(char *) * ids_buf_pos);
                cache->reg_ids_str = loc_alloc(str_buf_pos);
                memcpy(cache->reg_ids_str, str_buf, str_buf_pos);
                for (i = 0; i < cache->reg_cnt; i++) {
                    cache->reg_ids[i] = cache->reg_ids_str + ids_buf[i];
                }
                if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
                if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
            }
        }
        else if (cache->reg_ids == NULL && cache->reg_error == 0) {
            cache->pending_get_regs = protocol_send_command(c, "Registers", "getChildren", validate_context_cache, args);
            write_stringz(&c->out, thread_id(cache->ctx));
            write_stream(&c->out, MARKER_EOM);
            flush_stream(&c->out);
            return 0;
        }
    }

    return 1;
}

static void read_run_control_context_property(InputStream * inp, char * name, void * args) {
    ContextCache * ctx = (ContextCache *)args;
    if (strcmp(name, "ID") == 0) json_read_string(inp, ctx->id, sizeof(ctx->id));
    else if (strcmp(name, "ParentID") == 0) json_read_string(inp, ctx->parent_id, sizeof(ctx->parent_id));
    else if (strcmp(name, "HasState") == 0) ctx->has_state = json_read_boolean(inp);
    else if (strcmp(name, "IsContainer") == 0) ctx->is_container = json_read_boolean(inp);
    else if (strcmp(name, "CanSuspend") == 0) ctx->can_suspend = json_read_boolean(inp);
    else if (strcmp(name, "CanResume") == 0) ctx->can_resume = json_read_long(inp);
    else if (strcmp(name, "CanCount") == 0) ctx->can_count = json_read_long(inp);
    else if (strcmp(name, "CanTerminate") == 0) ctx->can_terminate = json_read_boolean(inp);
    else loc_free(json_skip_object(inp));
}

static void read_context_suspended_data(InputStream * inp, char * name, void * args) {
    ContextCache * ctx = (ContextCache *)args;
    loc_free(json_skip_object(inp));
}

static void read_context_added_item(InputStream * inp, void * args) {
    ContextCache ctx;
    memset(&ctx, 0, sizeof(ctx));
    json_read_struct(inp, read_run_control_context_property, &ctx);
    printf("added %s\n", ctx.id);
}

static void read_context_changed_item(InputStream * inp, void * args) {
    ContextCache ctx;
    memset(&ctx, 0, sizeof(ctx));
    json_read_struct(inp, read_run_control_context_property, &ctx);
    printf("changed %s\n", ctx.id);
}

static void read_context_removed_item(InputStream * inp, void * args) {
    char id[256];
    json_read_string(inp, id, sizeof(id));
    printf("removed %s\n", id);
}

static void read_container_suspended_item(InputStream * inp, void * args) {
    PeerCache * p = (PeerCache *)args;
    char id[256];
    json_read_string(inp, id, sizeof(id));
    printf("suspended %s\n", id);
}

static void read_container_resumed_item(InputStream * inp, void * args) {
    PeerCache * p = (PeerCache *)args;
    char id[256];
    json_read_string(inp, id, sizeof(id));
    printf("resumed %s\n", id);
}

static void event_context_added(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextAdded");
    json_read_array(p->fwd_inp, read_context_added_item, NULL);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_context_changed(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextChanged");
    json_read_array(p->fwd_inp, read_context_changed_item, NULL);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_context_removed(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextRemoved");
    json_read_array(p->fwd_inp, read_context_removed_item, NULL);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_context_suspended(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    char id[256];
    char reason[256];
    int64_t pc;
    ContextCache ctx;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextSuspended");
    json_read_string(p->fwd_inp, id, sizeof(id));
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    pc = json_read_int64(p->fwd_inp);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(p->fwd_inp, reason, sizeof(reason));
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_struct(p->fwd_inp, read_context_suspended_data, &ctx);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_context_resumed(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    char id[256];
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextResumed");
    json_read_string(p->fwd_inp, id, sizeof(id));
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_container_suspended(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    char id[256];
    char reason[256];
    int64_t pc;
    ContextCache ctx;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "containerSuspended");
    json_read_string(p->fwd_inp, id, sizeof(id));
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    pc = json_read_int64(p->fwd_inp);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(p->fwd_inp, reason, sizeof(reason));
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_struct(p->fwd_inp, read_context_suspended_data, &ctx);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_array(p->fwd_inp, read_container_suspended_item, p);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_container_resumed(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "containerResumed");
    json_read_array(p->fwd_inp, read_container_resumed_item, p);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static ContextCache * alloc_context_cache(void) {
    ContextCache * c = loc_alloc_zero(sizeof(ContextCache));
    list_init(&c->mem_cache);
}

void create_context_proxy(Channel * host, Channel * target) {
    LINK * l;
    PeerCache * p;
    for (l = peers.next; l != &peers; l = l->next) {
        p = peers2peer(l);
        if (p->target == target) return;
    }
    p = loc_alloc_zero(sizeof(PeerCache));
    p->host = host;
    p->target = target;
    p->fwd_inp = create_forwarding_input_stream(&p->fwd, &target->inp, &host->out);
    list_add_first(&p->link_all, &peers);
    channel_lock(host);
    channel_lock(target);
    add_event_handler2(target, RUN_CONTROL, "contextAdded", event_context_added, p);
    add_event_handler2(target, RUN_CONTROL, "contextChanged", event_context_changed, p);
    add_event_handler2(target, RUN_CONTROL, "contextRemoved", event_context_removed, p);
    add_event_handler2(target, RUN_CONTROL, "contextSuspended", event_context_suspended, p);
    add_event_handler2(target, RUN_CONTROL, "contextResumed", event_context_resumed, p);
    add_event_handler2(target, RUN_CONTROL, "containerSuspended", event_container_suspended, p);
    add_event_handler2(target, RUN_CONTROL, "containerResumed", event_container_resumed, p);
}

void context_lock(Context * ctx) {
    assert(ctx->ref_count > 0);
    ctx->ref_count++;
}

void context_unlock(Context * ctx) {
    assert(ctx->ref_count > 0);
    if (--ctx->ref_count == 0) {
        ContextCache * c = ctx->proxy;
        assert(list_is_empty(&ctx->children));
        assert(ctx->parent == NULL);
        assert(c->pending_get_map == NULL);
        assert(c->pending_get_regs == NULL);
        list_remove(&ctx->ctxl);
        list_remove(&ctx->pidl);
        release_error_report(ctx->regs_error);
        loc_free(ctx->bp_ids);
        loc_free(ctx->regs);
        loc_free(ctx);
        loc_free(c->mem_regions);
        loc_free(c->reg_ids);
        loc_free(c->reg_ids_str);
        loc_free(c->reg_defs);
        while (!list_is_empty(&c->mem_cache)) {
            MemoryCache * m = mems2mem(c->mem_cache.next);
            list_remove(&m->link);
            if (m->pending_command != NULL) {
                m->canceled = 1;
            }
            else {
                loc_free(m->buf);
                loc_free(m);
            }
        }
        loc_free(c);
    }
}

Context * id2ctx(char * id) {
    return NULL;
}

int context_has_state(Context * ctx) {
    ContextCache * cache = (ContextCache *)ctx->proxy;
    if (!validate_context_cache(cache->peer->target, cache, 0)) cache_wait(&cache->cache);
    return cache->has_state;
}

static void validate_memory_cache(Channel * c, void * args, int error) {
    MemoryCache * m = (MemoryCache *)args;
    m->error = get_error_report(error);
    if (!error) {
        size_t pos = 0;
        JsonReadBinaryState state;
        json_read_binary_start(&state, &c->inp);
        for (;;) {
            int rd = json_read_binary_data(&state, (int8_t *)m->buf + pos, m->size - pos);
            if (rd == 0) break;
            pos += rd;
        }
        json_read_binary_end(&state);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        m->error = get_error_report(read_errno(&c->inp));


        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    }
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    errno = EINVAL;
    return -1;
}

int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    ContextCache * cache = (ContextCache *)ctx->proxy;
    Channel * c = cache->peer->target;
    MemoryCache * m = NULL;
    LINK * l = NULL;

    for (l = cache->mem_cache.next; l != &cache->mem_cache; l = l->next) {
        m = mems2mem(l);
        if (address >= m->addr && address + size <= m->addr + m->size) {
            if (m->pending_command != NULL) cache_wait(&m->cache);
            memcpy(buf, (int8_t *)m->buf + (address - m->addr), size);
            errno = set_error_report_errno(m->error);
            return !errno ? 0 : -1;
        }
    }

    m = loc_alloc_zero(sizeof(MemoryCache));
    m->addr = address;
    m->buf = loc_alloc(size);
    m->size = size;
    m->pending_command = protocol_send_command(c, "Memory", "get", validate_memory_cache, m);
    write_stringz(&c->out, container_id(cache->ctx));
    json_write_int64(&c->out, m->addr);
    write_stream(&c->out, 0);
    json_write_long(&c->out, 1);
    write_stream(&c->out, 0);
    json_write_long(&c->out, m->size);
    write_stream(&c->out, 0);
    json_write_long(&c->out, 0);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
    flush_stream(&c->out);
    cache_wait(&m->cache);
    return -1;
}

void memory_map_get_regions(Context * ctx, MemoryRegion ** regions, unsigned * cnt) {
    ContextCache * cache = (ContextCache *)ctx->proxy;
    if (!validate_context_cache(cache->peer->target, cache, 0)) cache_wait(&cache->cache);
    *regions = cache->mem_regions;
    *cnt = cache->mem_regions_cnt;
}

RegisterDefinition * get_reg_definitions(Context * ctx) {
    ContextCache * cache = (ContextCache *)ctx->proxy;
    if (!validate_context_cache(cache->peer->target, cache, 0)) cache_wait(&cache->cache);
    return cache->reg_defs;
}

RegisterDefinition * get_PC_definition(Context * ctx) {
    ContextCache * cache = (ContextCache *)ctx->proxy;
    if (!validate_context_cache(cache->peer->target, cache, 0)) cache_wait(&cache->cache);
    return cache->pc_def;
}

RegisterDefinition * get_reg_by_id(Context * ctx, unsigned id, unsigned munbering_convention) {
    RegisterDefinition * defs;
    ContextCache * cache = (ContextCache *)ctx->proxy;
    if (!validate_context_cache(cache->peer->target, cache, 0)) cache_wait(&cache->cache);
    defs = cache->reg_defs;
    while (defs != NULL && defs->name != NULL) {
        switch (munbering_convention) {
        case REGNUM_DWARF:
            if (defs->dwarf_id == id) return defs;
            break;
        case REGNUM_EH_FRAME:
            if (defs->eh_frame_id == id) return defs;
            break;
        }
        defs++;
    }
    return NULL;
}

static void channel_close_listener(Channel * c) {
    LINK * l = NULL;

    for (l = peers.next; l != &peers; l = l->next) {
        PeerCache * p = peers2peer(l);
        if (p->target == c) {
            channel_unlock(p->host);
            channel_unlock(p->target);
            list_remove(&p->link_all);
            loc_free(p);
            return;
        }
    }
}

void init_contexts_sys_dep(void) {
    list_init(&peers);
    add_channel_close_listener(channel_close_listener);
}

#endif /* ENABLE_DebugContext && ENABLE_ContextProxy */
