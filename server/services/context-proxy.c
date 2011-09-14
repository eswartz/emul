/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * This module forwards handling of process/thread OS contexts to remote peer.
 */

#include <config.h>

#if ENABLE_DebugContext && ENABLE_ContextProxy

#include <errno.h>
#include <stdio.h>
#include <assert.h>
#include <sys/stat.h>
#include <framework/context.h>
#include <framework/myalloc.h>
#include <framework/trace.h>
#include <framework/exceptions.h>
#include <framework/protocol.h>
#include <framework/json.h>
#include <framework/cache.h>
#include <services/pathmap.h>
#include <services/memorymap.h>
#include <services/stacktrace.h>
#include <services/context-proxy.h>

typedef struct ContextCache ContextCache;
typedef struct MemoryCache MemoryCache;
typedef struct StackFrameCache StackFrameCache;
typedef struct PeerCache PeerCache;
typedef struct RegisterProps RegisterProps;

#define CTX_ID_HASH_SIZE 101

struct ContextCache {
    char id[256];
    char parent_id[256];
    char process_id[256];
    char creator_id[256];
    char * file;
    Context * ctx;
    PeerCache * peer;
    LINK id_hash_link;

    /* Memory Map */
    MemoryMap mmap;
    AbstractCache mmap_cache;
    ErrorReport * mmap_error;
    ReplyHandlerInfo * pending_get_mmap;
    int mmap_is_valid;

    /* Register definitions */
    AbstractCache regs_cache;
    ErrorReport * reg_error;
    unsigned reg_cnt;
    unsigned reg_size;
    char ** reg_ids;
    char * reg_ids_str;
    RegisterProps * reg_props;
    RegisterDefinition * reg_defs;
    RegisterDefinition * pc_def;
    int pending_regs_cnt;

    /* Run Control Properties */
    int has_state;
    int is_container;
    int can_suspend;
    long can_resume;
    long can_count;
    int can_terminate;

    /* Run Control State */
    int pc_valid;
    uint64_t suspend_pc;
    char * suspend_reason;
    char * signal_name;
    char ** bp_ids;

    /* Memory */
    LINK mem_cache_list;

    /* Stack trace */
    LINK stk_cache_list;
};

struct RegisterProps {
    RegisterDefinition def;
    char * id;
    char * role;
};

struct MemoryCache {
    LINK link_ctx;
    ContextCache * ctx;
    AbstractCache cache;
    ErrorReport * error;
    ContextAddress addr;
    void * buf;
    size_t size;
    ReplyHandlerInfo * pending;
    int disposed;
};

struct StackFrameCache {
    LINK link_ctx;
    ContextCache * ctx;
    AbstractCache cache;
    ErrorReport * error;
    int frame;
    ContextAddress ip;
    ContextAddress rp;
    StackFrame info;
    RegisterData reg_data;
    RegisterDefinition ** reg_defs;
    int reg_cnt;
    ReplyHandlerInfo * pending;
    int disposed;
};

struct PeerCache {
    LINK link_all;
    LINK context_id_hash[CTX_ID_HASH_SIZE];
    Channel * host;
    Channel * target;
    ForwardingInputStream bck_buf;
    ForwardingInputStream fwd_buf;
    InputStream * bck_inp;
    InputStream * fwd_inp;

    /* Initial Run Control context tree retrieval */
    int rc_done;
    int rc_pending_cnt;
    ErrorReport * rc_error;
    AbstractCache rc_cache;
};

#define peers2peer(A)    ((PeerCache *)((char *)(A) - offsetof(PeerCache, link_all)))
#define ctx2mem(A)       ((MemoryCache *)((char *)(A) - offsetof(MemoryCache, link_ctx)))
#define ctx2stk(A)       ((StackFrameCache *)((char *)(A) - offsetof(StackFrameCache, link_ctx)))
#define idhashl2ctx(A)   ((ContextCache *)((char *)(A) - offsetof(ContextCache, id_hash_link)))

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

static size_t context_extension_offset = 0;

#define EXT(ctx) ((ContextCache **)((char *)(ctx) + context_extension_offset))

static const char RUN_CONTROL[] = "RunControl";
static const char MEMORY_MAP[] = "MemoryMap";
static const char PATH_MAP[] = "PathMap";

static unsigned hash_ctx_id(const char * id) {
    int i;
    unsigned h = 0;
    for (i = 0; id[i]; i++) h += id[i];
    return h % CTX_ID_HASH_SIZE;
}

static ContextCache * find_context_cache(PeerCache * p, const char * id) {
    LINK * h = p->context_id_hash + hash_ctx_id(id);
    LINK * l = h->next;
    while (l != h) {
        ContextCache * c = idhashl2ctx(l);
        if (strcmp(c->id, id) == 0) return c;
        l = l->next;
    }
    return NULL;
}

static void set_context_links(ContextCache * c) {
    assert(c->peer->rc_done);
    if (c->parent_id[0]) {
        ContextCache * h = find_context_cache(c->peer, c->parent_id);
        if (h != NULL) {
            (c->ctx->parent = h->ctx)->ref_count++;
            list_add_last(&c->ctx->cldl, &h->ctx->children);
        }
        else {
            trace(LOG_ALWAYS, "Invalid parent ID: %s", c->parent_id);
        }
    }
    if (c->process_id[0]) {
        ContextCache * h = find_context_cache(c->peer, c->process_id);
        if (h != NULL) {
            c->ctx->mem = h->ctx;
        }
        else {
            trace(LOG_ALWAYS, "Invalid process ID: %s", c->process_id);
        }
    }
    if (c->creator_id[0]) {
        ContextCache * h = find_context_cache(c->peer, c->creator_id);
        if (h != NULL) {
            (c->ctx->creator = h->ctx)->ref_count++;
        }
        else {
            trace(LOG_ALWAYS, "Invalid creator ID: %s", c->creator_id);
        }
    }
}

static void add_context_cache(PeerCache * p, ContextCache * c) {
    LINK * h = p->context_id_hash + hash_ctx_id(c->id);
    c->peer = p;
    c->ctx = create_context(c->id);
    c->ctx->ref_count = 1;
    *EXT(c->ctx) = c;
    list_init(&c->mem_cache_list);
    list_init(&c->stk_cache_list);
    list_add_first(&c->id_hash_link, h);
    list_add_first(&c->ctx->ctxl, &context_root);
    if (p->rc_done) set_context_links(c);
    send_context_created_event(c->ctx);
}

static void free_memory_cache(MemoryCache * m) {
    list_remove(&m->link_ctx);
    m->disposed = 1;
    if (m->pending == NULL) {
        release_error_report(m->error);
        cache_dispose(&m->cache);
        loc_free(m->buf);
        loc_free(m);
    }
}

static void free_stack_frame_cache(StackFrameCache * s) {
    list_remove(&s->link_ctx);
    s->disposed = 1;
    if (s->pending == NULL) {
        release_error_report(s->error);
        cache_dispose(&s->cache);
        loc_free(s->reg_data.data);
        loc_free(s->reg_data.mask);
        loc_free(s->reg_defs);
        loc_free(s);
    }
}

static void free_context_cache(ContextCache * c) {
    assert(c->pending_get_mmap == NULL);
    assert(c->pending_regs_cnt == 0);
    cache_dispose(&c->mmap_cache);
    cache_dispose(&c->regs_cache);
    release_error_report(c->mmap_error);
    release_error_report(c->reg_error);
    loc_free(c->file);
    context_clear_memory_map(&c->mmap);
    loc_free(c->mmap.regions);
    loc_free(c->reg_ids);
    loc_free(c->reg_ids_str);
    loc_free(c->reg_defs);
    loc_free(c->signal_name);
    loc_free(c->bp_ids);
    if (c->reg_props != NULL) {
        unsigned i;
        for (i = 0; i < c->reg_cnt; i++) {
            loc_free(c->reg_props[i].id);
            loc_free(c->reg_props[i].role);
        }
        loc_free(c->reg_props);
    }
    assert(list_is_empty(&c->id_hash_link));
    if (!list_is_empty(&c->mem_cache_list)) {
        LINK * l = c->mem_cache_list.next;
        while (l != &c->mem_cache_list) {
            MemoryCache * m = ctx2mem(c->mem_cache_list.next);
            l = l->next;
            free_memory_cache(m);
        }
    }
    if (!list_is_empty(&c->stk_cache_list)) {
        LINK * l = c->stk_cache_list.next;
        while (l != &c->stk_cache_list) {
            StackFrameCache * s = ctx2stk(c->stk_cache_list.next);
            l = l->next;
            free_stack_frame_cache(s);
        }
    }
    loc_free(c);
}

static void on_context_suspended(ContextCache * c) {
    LINK * l;

    if (c->peer->rc_done) {
        LINK * x = context_root.next;
        while (x != &context_root) {
            Context * ctx = ctxl2ctxp(x);
            if (ctx->mem == c->ctx->mem) {
                ContextCache * p = *EXT(ctx);
                l = p->mem_cache_list.next;
                while (l != &p->mem_cache_list) {
                    MemoryCache * m = ctx2mem(p->mem_cache_list.next);
                    l = l->next;
                    if (!m->pending) free_memory_cache(m);
                }
            }
            x = x->next;
        }
    }

    l = c->stk_cache_list.next;
    while (l != &c->stk_cache_list) {
        StackFrameCache * f = ctx2stk(c->stk_cache_list.next);
        l = l->next;
        free_stack_frame_cache(f);
    }
}

static void read_run_control_context_property(InputStream * inp, const char * name, void * args) {
    ContextCache * ctx = (ContextCache *)args;
    if (strcmp(name, "ID") == 0) json_read_string(inp, ctx->id, sizeof(ctx->id));
    else if (strcmp(name, "ParentID") == 0) json_read_string(inp, ctx->parent_id, sizeof(ctx->parent_id));
    else if (strcmp(name, "ProcessID") == 0) json_read_string(inp, ctx->process_id, sizeof(ctx->process_id));
    else if (strcmp(name, "CreatorID") == 0) json_read_string(inp, ctx->creator_id, sizeof(ctx->creator_id));
    else if (strcmp(name, "File") == 0) ctx->file = json_read_alloc_string(inp);
    else if (strcmp(name, "HasState") == 0) ctx->has_state = json_read_boolean(inp);
    else if (strcmp(name, "IsContainer") == 0) ctx->is_container = json_read_boolean(inp);
    else if (strcmp(name, "CanSuspend") == 0) ctx->can_suspend = json_read_boolean(inp);
    else if (strcmp(name, "CanResume") == 0) ctx->can_resume = json_read_long(inp);
    else if (strcmp(name, "CanCount") == 0) ctx->can_count = json_read_long(inp);
    else if (strcmp(name, "CanTerminate") == 0) ctx->can_terminate = json_read_boolean(inp);
    else json_skip_object(inp);
}

static void read_context_suspended_data(InputStream * inp, const char * name, void * args) {
    ContextCache * ctx = (ContextCache *)args;
    if (strcmp(name, "Signal") == 0 && ctx->ctx != NULL) ctx->ctx->signal = json_read_long(inp);
    else if (strcmp(name, "SignalName") == 0) ctx->signal_name = json_read_alloc_string(inp);
    else if (strcmp(name, "BPs") == 0) ctx->bp_ids = json_read_alloc_string_array(inp, NULL);
    else json_skip_object(inp);
}

static void clear_context_suspended_data(ContextCache * ctx) {
    loc_free(ctx->suspend_reason);
    loc_free(ctx->signal_name);
    loc_free(ctx->bp_ids);
    if (ctx->ctx != NULL) ctx->ctx->signal = 0;
    ctx->pc_valid = 0;
    ctx->suspend_pc = 0;
    ctx->suspend_reason = NULL;
    ctx->signal_name = NULL;
    ctx->bp_ids = NULL;
}

static void crear_memory_map_data(ContextCache * ctx) {
    context_clear_memory_map(&ctx->mmap);
    release_error_report(ctx->mmap_error);
    ctx->mmap_is_valid = 0;
    ctx->mmap_error = NULL;
}

static void read_context_added_item(InputStream * inp, void * args) {
    PeerCache * p = (PeerCache *)args;
    ContextCache * c = (ContextCache *)loc_alloc_zero(sizeof(ContextCache));

    json_read_struct(inp, read_run_control_context_property, c);

    if (find_context_cache(p, c->id) == NULL &&
        (c->parent_id[0] == 0 || find_context_cache(p, c->parent_id) != NULL)) {
        add_context_cache(p, c);
    }
    else {
        if (p->rc_done) trace(LOG_ALWAYS, "Invalid ID in 'context added' event: %s", c->id);
        free_context_cache(c);
    }
}

static void read_context_changed_item(InputStream * inp, void * args) {
    PeerCache * p = (PeerCache *)args;
    ContextCache * c = NULL;
    ContextCache buf;
    memset(&buf, 0, sizeof(buf));
    json_read_struct(inp, read_run_control_context_property, &buf);
    c = find_context_cache(p, buf.id);
    if (c != NULL) {
        strcpy(c->parent_id, buf.parent_id);
        c->has_state = buf.has_state;
        c->is_container = buf.is_container;
        c->can_suspend = buf.can_suspend;
        c->can_resume = buf.can_resume;
        c->can_count = buf.can_count;
        c->can_terminate = buf.can_terminate;
        send_context_changed_event(c->ctx);
    }
    else if (p->rc_done) {
        trace(LOG_ALWAYS, "Invalid ID in 'context changed' event: %s", buf.id);
    }
}

static void read_context_removed_item(InputStream * inp, void * args) {
    PeerCache * p = (PeerCache *)args;
    ContextCache * c = NULL;
    char id[256];
    json_read_string(inp, id, sizeof(id));
    c = find_context_cache(p, id);
    if (c != NULL) {
        assert(*EXT(c->ctx) == c);
        send_context_exited_event(c->ctx);
    }
    else if (p->rc_done) {
        trace(LOG_ALWAYS, "Invalid ID in 'context removed' event: %s", id);
    }
}

static void read_container_suspended_item(InputStream * inp, void * args) {
    PeerCache * p = (PeerCache *)args;
    ContextCache * c = NULL;
    char id[256];
    json_read_string(inp, id, sizeof(id));
    c = find_context_cache(p, id);
    if (c != NULL) {
        assert(*EXT(c->ctx) == c);
        if (!c->ctx->stopped) {
            c->ctx->stopped = 1;
            on_context_suspended(c);
            send_context_stopped_event(c->ctx);
        }
    }
    else if (p->rc_done) {
        trace(LOG_ALWAYS, "Invalid ID in 'container suspended' event: %s", id);
    }
}

static void read_container_resumed_item(InputStream * inp, void * args) {
    PeerCache * p = (PeerCache *)args;
    ContextCache * c = NULL;
    char id[256];
    json_read_string(inp, id, sizeof(id));
    c = find_context_cache(p, id);
    if (c != NULL) {
        assert(*EXT(c->ctx) == c);
        if (c->ctx->stopped) {
            c->ctx->stopped = 0;
            clear_context_suspended_data(c);
            send_context_started_event(c->ctx);
        }
    }
    else if (p->rc_done) {
        trace(LOG_ALWAYS, "Invalid ID in 'container resumed' event: %s", id);
    }
}

static void event_context_added(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextAdded");
    json_read_array(p->bck_inp, read_context_added_item, p);
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->bck_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_context_changed(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextChanged");
    json_read_array(p->bck_inp, read_context_changed_item, p);
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->bck_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_context_removed(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextRemoved");
    json_read_array(p->bck_inp, read_context_removed_item, p);
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->bck_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_context_suspended(Channel * ch, void * args) {
    PeerCache * p = (PeerCache *)args;
    ContextCache buf;
    ContextCache * c = &buf;

    assert(p->target == ch);
    memset(&buf, 0, sizeof(buf));
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextSuspended");
    json_read_string(p->bck_inp, c->id, sizeof(c->id));
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    c = find_context_cache(p, c->id);
    if (c == NULL) c = &buf;
    else clear_context_suspended_data(c);
    c->suspend_pc = json_read_uint64(p->bck_inp);
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    c->suspend_reason = json_read_alloc_string(p->bck_inp);
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_struct(p->bck_inp, read_context_suspended_data, c);
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->bck_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    if (c != &buf) {
        assert(*EXT(c->ctx) == c);
        c->pc_valid = 1;
        if (!c->ctx->stopped) {
            c->ctx->stopped = 1;
            on_context_suspended(c);
            send_context_stopped_event(c->ctx);
        }
    }
    else if (p->rc_done) {
        trace(LOG_ALWAYS, "Invalid ID in 'context suspended' event: %s", c->id);
    }
}

static void event_context_resumed(Channel * ch, void * args) {
    PeerCache * p = (PeerCache *)args;
    ContextCache * c = NULL;
    char id[256];

    assert(p->target == ch);
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextResumed");
    json_read_string(p->bck_inp, id, sizeof(id));
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->bck_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    c = find_context_cache(p, id);
    if (c != NULL) {
        assert(*EXT(c->ctx) == c);
        if (c->ctx->stopped) {
            c->ctx->stopped = 0;
            clear_context_suspended_data(c);
            send_context_started_event(c->ctx);
        }
    }
    else if (p->rc_done) {
        trace(LOG_ALWAYS, "Invalid ID in 'context resumed' event: %s", id);
    }
}

static void event_container_suspended(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    ContextCache ctx;

    memset(&ctx, 0, sizeof(ctx));
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "containerSuspended");
    json_read_string(p->bck_inp, ctx.id, sizeof(ctx.id));
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    ctx.suspend_pc = json_read_uint64(p->bck_inp);
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    ctx.suspend_reason = json_read_alloc_string(p->bck_inp);
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_struct(p->bck_inp, read_context_suspended_data, &ctx);
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    /* TODO: save suspend data in the cache */
    json_read_array(p->bck_inp, read_container_suspended_item, p);
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->bck_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_container_resumed(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "containerResumed");
    json_read_array(p->bck_inp, read_container_resumed_item, p);
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->bck_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_memory_map_changed(Channel * c, void * args) {
    char id[256];
    PeerCache * p = (PeerCache *)args;
    ContextCache * ctx = NULL;

    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, MEMORY_MAP);
    write_stringz(&p->host->out, "changed");
    json_read_string(p->bck_inp, id, sizeof(id));
    if (read_stream(p->bck_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->bck_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    ctx = find_context_cache(p, id);
    if (ctx != NULL) {
        assert(*EXT(ctx->ctx) == ctx);
        crear_memory_map_data(ctx);
        memory_map_event_mapping_changed(ctx->ctx);
    }
    else if (p->rc_done) {
        trace(LOG_ALWAYS, "Invalid ID in 'memory map changed' event: %s", id);
    }
}

static void command_path_map_set(char * token, Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    write_stringz(&p->target->out, "C");
    write_stream(&p->target->out, 'R');
    write_stringz(&p->target->out, token);
    write_stringz(&p->target->out, PATH_MAP);
    write_stringz(&p->target->out, "set");
    set_path_map(p->host, p->fwd_inp);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void validate_peer_cache_children(Channel * c, void * args, int error);

void create_context_proxy(Channel * host, Channel * target, int forward_pm) {
    int i;
    LINK * l;
    PeerCache * p;
    for (l = peers.next; l != &peers; l = l->next) {
        p = peers2peer(l);
        if (p->target == target) return;
    }
    p = (PeerCache *)loc_alloc_zero(sizeof(PeerCache));
    p->host = host;
    p->target = target;
    p->bck_inp = create_forwarding_input_stream(&p->bck_buf, &target->inp, &host->out);
    p->fwd_inp = create_forwarding_input_stream(&p->fwd_buf, &host->inp, &target->out);
    for (i = 0; i < CTX_ID_HASH_SIZE; i++) list_init(p->context_id_hash + i);
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
    add_event_handler2(target, MEMORY_MAP, "changed", event_memory_map_changed, p);
    if (forward_pm) add_command_handler2(host->protocol, PATH_MAP, "set", command_path_map_set, p);
    /* Retirve initial set of run control contexts */
    protocol_send_command(p->target, "RunControl", "getChildren", validate_peer_cache_children, p);
    write_stringz(&p->target->out, "null");
    write_stream(&p->target->out, MARKER_EOM);
    p->rc_pending_cnt++;
}

static void validate_peer_cache_context(Channel * c, void * args, int error);
static void validate_peer_cache_state(Channel * c, void * args, int error);

static void read_rc_children_item(InputStream * inp, void * args) {
    char id[256];
    PeerCache * p = (PeerCache *)args;

    json_read_string(inp, id, sizeof(id));

    if (find_context_cache(p, id) == NULL) {
        ContextCache * c = (ContextCache *)loc_alloc_zero(sizeof(ContextCache));
        strcpy(c->id, id);
        c->peer = p;
        protocol_send_command(p->target, "RunControl", "getContext", validate_peer_cache_context, c);
        json_write_string(&p->target->out, c->id);
        write_stream(&p->target->out, 0);
        write_stream(&p->target->out, MARKER_EOM);
        p->rc_pending_cnt++;
    }
}

static void set_rc_done(PeerCache * p) {
    if (p->rc_pending_cnt == 0) {
        int i;
        LINK * l;
        p->rc_done = 1;
        for (i = 0; i < CTX_ID_HASH_SIZE; i++) {
            LINK * h = p->context_id_hash + i;
            for (l = h->next; l != h; l = l->next) {
                set_context_links(idhashl2ctx(l));
            }
        }
        cache_notify(&p->rc_cache);
    }
}

static void set_rc_error(PeerCache * p, int error) {
    if (error == 0) return;
    if (get_error_code(error) == ERR_INV_CONTEXT) return;
    if (get_error_code(error) == ERR_ALREADY_EXITED) return;
    if (p->rc_error != NULL) return;
    p->rc_error = get_error_report(error);
}

static void validate_peer_cache_children(Channel * c, void * args, int error) {
    PeerCache * p = (PeerCache *)args;
    Trap trap;

    assert(p->target == c);
    assert(p->rc_pending_cnt > 0);
    if (set_trap(&trap)) {
        p->rc_pending_cnt--;
        if (!error) {
            error = read_errno(&c->inp);
            json_read_array(&c->inp, read_rc_children_item, p);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    set_rc_error(p, error);
    set_rc_done(p);
    if (trap.error) exception(trap.error);
}

static void validate_peer_cache_context(Channel * c, void * args, int error) {
    ContextCache * x = (ContextCache *)args;
    PeerCache * p = x->peer;
    Trap trap;

    assert(p->target == c);
    assert(p->rc_pending_cnt > 0);
    if (set_trap(&trap)) {
        p->rc_pending_cnt--;
        if (error) {
            set_rc_error(p, error);
            free_context_cache(x);
        }
        else {
            set_rc_error(p, error = read_errno(&c->inp));
            json_read_struct(&c->inp, read_run_control_context_property, x);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
            if (error || find_context_cache(p, x->id) != NULL) {
                free_context_cache(x);
            }
            else if (x->has_state) {
                protocol_send_command(p->target, "RunControl", "getState", validate_peer_cache_state, x);
                json_write_string(&p->target->out, x->id);
                write_stream(&p->target->out, 0);
                write_stream(&p->target->out, MARKER_EOM);
                p->rc_pending_cnt++;
            }
            else {
                add_context_cache(p, x);
                protocol_send_command(p->target, "RunControl", "getChildren", validate_peer_cache_children, p);
                json_write_string(&p->target->out, x->id);
                write_stream(&p->target->out, 0);
                write_stream(&p->target->out, MARKER_EOM);
                p->rc_pending_cnt++;
            }
        }
        clear_trap(&trap);
    }
    else {
        set_rc_error(p, trap.error);
        free_context_cache(x);
    }
    set_rc_done(p);
    if (trap.error) exception(trap.error);
}

static void validate_peer_cache_state(Channel * c, void * args, int error) {
    ContextCache * x = (ContextCache *)args;
    PeerCache * p = x->peer;
    Trap trap;

    assert(p->target == c);
    assert(p->rc_pending_cnt > 0);
    if (set_trap(&trap)) {
        p->rc_pending_cnt--;
        if (error) {
            set_rc_error(p, error);
            free_context_cache(x);
        }
        else {
            set_rc_error(p, error = read_errno(&c->inp));
            clear_context_suspended_data(x);
            x->pc_valid = json_read_boolean(&c->inp);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            x->suspend_pc = json_read_uint64(&c->inp);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            x->suspend_reason = json_read_alloc_string(&c->inp);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            json_read_struct(&c->inp, read_context_suspended_data, x);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

            if (error || find_context_cache(p, x->id) != NULL) {
                free_context_cache(x);
            }
            else {
                add_context_cache(p, x);
                x->ctx->stopped = x->pc_valid;
                if (x->pc_valid) {
                    on_context_suspended(x);
                    send_context_stopped_event(x->ctx);
                }
                protocol_send_command(p->target, "RunControl", "getChildren", validate_peer_cache_children, p);
                json_write_string(&p->target->out, x->id);
                write_stream(&p->target->out, 0);
                write_stream(&p->target->out, MARKER_EOM);
                p->rc_pending_cnt++;
            }
        }
        clear_trap(&trap);
    }
    else {
        set_rc_error(p, trap.error);
        free_context_cache(x);
    }
    set_rc_done(p);
    if (trap.error) exception(trap.error);
}

Context * id2ctx(const char * id) {
    LINK * l;
    Channel * c = cache_channel();
    assert(c != NULL);
    for (l = peers.next; l != &peers; l = l->next) {
        PeerCache * p = peers2peer(l);
        if (p->host == c || p->target == c) {
            if (p->rc_pending_cnt > 0) {
                cache_wait(&p->rc_cache);
            }
            else if (p->rc_error != NULL) {
                set_error_report_errno(p->rc_error);
            }
            else if (!p->rc_done) {
                cache_wait(&p->rc_cache);
            }
            else {
                ContextCache * h = find_context_cache(p, id);
                return h ? h->ctx : NULL;
            }
        }
    }
    return NULL;
}

int context_has_state(Context * ctx) {
    return (*EXT(ctx))->has_state;
}

Context * context_get_group(Context * ctx, int group) {
    switch (group) {
    case CONTEXT_GROUP_INTERCEPT:
        return ctx;
    }
    return ctx->mem;
}

static void validate_memory_cache(Channel * c, void * args, int error) {
    MemoryCache * m = (MemoryCache *)args;
    Context * ctx = m->ctx->ctx;
    Trap trap;

    assert(m->pending != NULL);
    assert(m->error == NULL);
    if (set_trap(&trap)) {
        m->pending = NULL;
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
            error = read_errno(&c->inp);
            while (read_stream(&c->inp) != 0) {}
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    m->error = get_error_report(error);
    cache_notify(&m->cache);
    if (m->disposed) free_memory_cache(m);
    context_unlock(ctx);
    if (trap.error) exception(trap.error);
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    errno = EINVAL;
    return -1;
}

int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    ContextCache * cache = *EXT(ctx);
    MemoryCache * m = NULL;
    Channel * c = NULL;
    LINK * l = NULL;
    Trap trap;

    if (!set_trap(&trap)) return -1;
    if (!cache->peer->rc_done) cache_wait(&cache->peer->rc_cache);

    for (l = cache->mem_cache_list.next; l != &cache->mem_cache_list; l = l->next) {
        m = ctx2mem(l);
        if (address >= m->addr && address + size <= m->addr + m->size) {
            if (m->pending != NULL) cache_wait(&m->cache);
            memcpy(buf, (int8_t *)m->buf + (address - m->addr), size);
            set_error_report_errno(m->error);
            clear_trap(&trap);
            return !errno ? 0 : -1;
        }
    }

    c = cache->peer->target;
    m = (MemoryCache *)loc_alloc_zero(sizeof(MemoryCache));
    list_add_first(&m->link_ctx, &cache->mem_cache_list);
    m->ctx = cache;
    m->addr = address;
    m->buf = loc_alloc(size);
    m->size = size;
    m->pending = protocol_send_command(c, "Memory", "get", validate_memory_cache, m);
    json_write_string(&c->out, cache->ctx->id);
    write_stream(&c->out, 0);
    json_write_int64(&c->out, m->addr);
    write_stream(&c->out, 0);
    json_write_long(&c->out, 1);
    write_stream(&c->out, 0);
    json_write_long(&c->out, m->size);
    write_stream(&c->out, 0);
    json_write_long(&c->out, 0);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
    context_lock(ctx);
    cache_wait(&m->cache);
    return -1;
}

int context_write_reg(Context * ctx, RegisterDefinition * def, unsigned offs, unsigned size, void * buf) {
    errno = EINVAL;
    return -1;
}

int context_read_reg(Context * ctx, RegisterDefinition * def, unsigned offs, unsigned size, void * buf) {
    errno = EINVAL;
    return -1;
}

static void read_memory_region_property(InputStream * inp, const char * name, void * args) {
    MemoryRegion * m = (MemoryRegion *)args;
    if (strcmp(name, "Addr") == 0) m->addr = (ContextAddress)json_read_uint64(inp);
    else if (strcmp(name, "Size") == 0) m->size = json_read_ulong(inp);
    else if (strcmp(name, "Offs") == 0) m->file_offs = json_read_ulong(inp);
    else if (strcmp(name, "BSS") == 0) m->bss = json_read_boolean(inp);
    else if (strcmp(name, "Flags") == 0) m->flags = json_read_ulong(inp);
    else if (strcmp(name, "FileName") == 0) m->file_name = json_read_alloc_string(inp);
    else if (strcmp(name, "SectionName") == 0) m->sect_name = json_read_alloc_string(inp);
    else if (strcmp(name, "ID")) m->id = json_read_alloc_string(inp);
    else {
        MemoryRegionAttribute * x = (MemoryRegionAttribute *)loc_alloc(sizeof(MemoryRegionAttribute));
        x->name = loc_strdup(name);
        x->value = json_read_object(inp);
        x->next = m->attrs;
        m->attrs = x;
    }
}

static void read_memory_map_item(InputStream * inp, void * args) {
    ContextCache * cache = (ContextCache *)args;
    MemoryRegion * m;
    if (mem_buf_pos >= mem_buf_max) {
        mem_buf_max = mem_buf_max == 0 ? 16 : mem_buf_max * 2;
        mem_buf = (MemoryRegion *)loc_realloc(mem_buf, sizeof(MemoryRegion) * mem_buf_max);
    }
    m = mem_buf + mem_buf_pos;
    memset(m, 0, sizeof(MemoryRegion));
    if (json_read_struct(inp, read_memory_region_property, m) && m->file_name != NULL) {
        struct stat buf;
        char * fnm = apply_path_map(cache->peer->host, cache->ctx, m->file_name, PATH_MAP_TO_LOCAL);
        if (fnm != m->file_name) {
            loc_free(m->file_name);
            m->file_name = loc_strdup(fnm);
        }
        if (m->file_name != NULL && stat(m->file_name, &buf) == 0) {
            m->dev = buf.st_dev;
            m->ino = buf.st_ino;
            mem_buf_pos++;
        }
        else if (m->file_name != NULL) {
            trace(LOG_ELF, "Object file not found: %s", m->file_name);
            loc_free(m->file_name);
        }
    }
}

static void validate_memory_map_cache(Channel * c, void * args, int error) {
    ContextCache * cache = (ContextCache *)args;
    Trap trap;

    assert(cache->mmap_is_valid == 0);
    assert(cache->pending_get_mmap != NULL);
    if (set_trap(&trap)) {
        cache->pending_get_mmap = NULL;
        if (!error) {
            error = read_errno(&c->inp);
            mem_buf_pos = 0;
            json_read_array(&c->inp, read_memory_map_item, cache);
            cache->mmap.region_cnt = mem_buf_pos;
            cache->mmap.region_max = mem_buf_pos;
            cache->mmap.regions = (MemoryRegion *)loc_alloc(sizeof(MemoryRegion) * mem_buf_pos);
            memcpy(cache->mmap.regions, mem_buf, sizeof(MemoryRegion) * mem_buf_pos);

            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    cache->mmap_is_valid = 1;
    cache->mmap_error = get_error_report(error);
    cache_notify(&cache->mmap_cache);
    context_unlock(cache->ctx);
    if (trap.error) exception(trap.error);
}

int context_get_memory_map(Context * ctx, MemoryMap * map) {
    ContextCache * cache = *EXT(ctx);
    Trap trap;

    if (!set_trap(&trap)) return -1;
    if (cache->peer != NULL && !cache->peer->rc_done) cache_wait(&cache->peer->rc_cache);

    cache = *EXT(ctx);
    assert(cache->ctx == ctx);
    if (cache->pending_get_mmap != NULL) cache_wait(&cache->mmap_cache);
    if (cache->mmap_is_valid == 0 && cache->peer != NULL) {
        Channel * c = cache->peer->target;
        cache->pending_get_mmap = protocol_send_command(c, "MemoryMap", "get", validate_memory_map_cache, cache);
        json_write_string(&c->out, cache->id);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        context_lock(ctx);
        cache_wait(&cache->mmap_cache);
    }
    if (map->region_max < cache->mmap.region_cnt) {
        map->region_max = cache->mmap.region_cnt;
        map->regions = (MemoryRegion *)loc_realloc(map->regions, sizeof(MemoryRegion) * map->region_max);
    }
    map->region_cnt = cache->mmap.region_cnt;
    if (map->region_cnt > 0) {
        memcpy(map->regions, cache->mmap.regions, sizeof(MemoryRegion) * map->region_cnt);
        memset(cache->mmap.regions, 0, sizeof(MemoryRegion) * map->region_cnt);
        cache->mmap.region_cnt = 0;
        cache->mmap_is_valid = 0;
    }
    clear_trap(&trap);
    if (cache->mmap_error != NULL) {
        set_error_report_errno(cache->mmap_error);
        return -1;
    }
    return 0;
}

static void read_ids_item(InputStream * inp, void * args) {
    int n;
    char id[256];
    if (ids_buf_pos >= ids_buf_max) {
        ids_buf_max = ids_buf_max == 0 ? 16 : ids_buf_max * 2;
        ids_buf = (unsigned *)loc_realloc(ids_buf, sizeof(unsigned) * ids_buf_max);
    }
    n = json_read_string(inp, id, sizeof(id));
    if (n <= 0) return;
    n++;
    if (n > (int)sizeof(id)) n = sizeof(id);
    if (str_buf_pos + n > str_buf_max) {
        str_buf_max = str_buf_max == 0 ? sizeof(id) : str_buf_max * 2;
        str_buf = (char *)loc_realloc(str_buf, str_buf_max);
    }
    memcpy(str_buf + str_buf_pos, id, n);
    ids_buf[ids_buf_pos++] = str_buf_pos;
    str_buf_pos += n;
}

static void read_register_property(InputStream * inp, const char * name, void * args) {
    RegisterProps * p = (RegisterProps *)args;
    if (strcmp(name, "ID") == 0) p->id = json_read_alloc_string(inp);
    else if (strcmp(name, "Role") == 0) p->role = json_read_alloc_string(inp);
    else if (strcmp(name, "Name") == 0) p->def.name = json_read_alloc_string(inp);
    else if (strcmp(name, "Size") == 0) p->def.size = (uint16_t)json_read_long(inp);
    else if (strcmp(name, "DwarfID") == 0) p->def.dwarf_id = (int16_t)json_read_long(inp);
    else if (strcmp(name, "EhFrameID") == 0) p->def.eh_frame_id = (int16_t)json_read_long(inp);
    else if (strcmp(name, "BigEndian") == 0) p->def.big_endian = (uint8_t)json_read_boolean(inp);
    else json_skip_object(inp);
}

static void validate_registers_cache(Channel * c, void * args, int error) {
    ContextCache * cache = (ContextCache *)args;
    Trap trap;

    if (cache->reg_ids == NULL) {
        /* Registers.getChildren reply */
        assert(cache->reg_ids_str == NULL);
        assert(cache->reg_error == NULL);
        assert(cache->pending_regs_cnt == 1);
        if (set_trap(&trap)) {
            cache->pending_regs_cnt--;
            if (!error) {
                unsigned i;
                error = read_errno(&c->inp);
                ids_buf_pos = 0;
                str_buf_pos = 0;
                json_read_array(&c->inp, read_ids_item, NULL);
                cache->reg_cnt = ids_buf_pos;
                cache->reg_ids = (char **)loc_alloc(sizeof(char *) * ids_buf_pos);
                cache->reg_ids_str = (char *)loc_alloc(str_buf_pos);
                memcpy(cache->reg_ids_str, str_buf, str_buf_pos);
                for (i = 0; i < cache->reg_cnt; i++) {
                    cache->reg_ids[i] = cache->reg_ids_str + ids_buf[i];
                }
                if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
                if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
                if (!error && cache->reg_cnt == 0) error = set_errno(
                    ERR_INV_CONTEXT, "Register definitions not available");
            }
            clear_trap(&trap);
        }
        else {
            error = trap.error;
        }
        cache->reg_error = get_error_report(error);
        cache_notify(&cache->regs_cache);
    }
    else {
        /* Registers.getContext reply */
        assert(cache->pending_regs_cnt > 0);
        if (set_trap(&trap)) {
            cache->pending_regs_cnt--;
            if (!error) {
                unsigned i;
                RegisterProps props;
                memset(&props, 0, sizeof(props));
                props.def.dwarf_id = -1;
                props.def.eh_frame_id = -1;
                error = read_errno(&c->inp);
                json_read_struct(&c->inp, read_register_property, &props);
                if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
                if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
                for (i = 0; i < cache->reg_cnt; i++) {
                    if (strcmp(props.id, cache->reg_ids[i]) == 0) {
                        cache->reg_props[i] = props;
                        cache->reg_defs[i] = props.def;
                        if (props.role != NULL && strcmp(props.role, "PC") == 0) {
                            cache->pc_def = cache->reg_defs + i;
                        }
                        break;
                    }
                }
            }
            clear_trap(&trap);
        }
        else {
            error = trap.error;
        }
        cache->reg_error = get_error_report(error);
        if (cache->pending_regs_cnt == 0) {
            unsigned i;
            unsigned offs = 0;
            for (i = 0; i < cache->reg_cnt; i++) {
                RegisterDefinition * r = cache->reg_defs + i;
                r->offset = offs;
                offs += r->size;
            }
            cache->reg_size = offs;
            cache_notify(&cache->regs_cache);
        }
    }
    context_unlock(cache->ctx);
    if (trap.error) exception(trap.error);
}

static void check_registers_cache(ContextCache * cache) {
    if (cache->pending_regs_cnt > 0) cache_wait(&cache->regs_cache);
    if (cache->reg_error != NULL) exception(set_error_report_errno(cache->reg_error));
    if (cache->reg_ids == NULL) {
        Channel * c = cache->peer->target;
        cache->pending_regs_cnt++;
        protocol_send_command(c, "Registers", "getChildren", validate_registers_cache, cache);
        json_write_string(&c->out, cache->ctx->id);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        context_lock(cache->ctx);
        cache_wait(&cache->regs_cache);
    }
    if (cache->reg_defs == NULL) {
        unsigned i;
        cache->reg_defs = (RegisterDefinition *)loc_alloc_zero(sizeof(RegisterDefinition) * (cache->reg_cnt + 1));
        cache->reg_props = (RegisterProps *)loc_alloc_zero(sizeof(RegisterProps) * cache->reg_cnt);
        for (i = 0; i < cache->reg_cnt; i++) {
            Channel * c = cache->peer->target;
            cache->pending_regs_cnt++;
            protocol_send_command(c, "Registers", "getContext", validate_registers_cache, cache);
            json_write_string(&c->out, cache->reg_ids[i]);
            write_stream(&c->out, 0);
            write_stream(&c->out, MARKER_EOM);
            context_lock(cache->ctx);
        }
        cache_wait(&cache->regs_cache);
    }
}

RegisterDefinition * get_reg_definitions(Context * ctx) {
    ContextCache * cache = *EXT(ctx);
    check_registers_cache(cache);
    return cache->reg_defs;
}

RegisterDefinition * get_PC_definition(Context * ctx) {
    ContextCache * cache = *EXT(ctx);
    check_registers_cache(cache);
    return cache->pc_def;
}

static void validate_reg_values_cache(Channel * c, void * args, int error) {
    StackFrameCache * s = (StackFrameCache *)args;
    Context * ctx = s->ctx->ctx;
    Trap trap;

    assert(s->pending != NULL);
    assert(s->error == NULL);
    if (set_trap(&trap)) {
        s->pending = NULL;
        if (!error) {
            int r = 0;
            int n = s->info.is_top_frame ? s->ctx->reg_cnt : s->reg_cnt;
            JsonReadBinaryState state;
            error = read_errno(&c->inp);
            json_read_binary_start(&state, &c->inp);
            for (r = 0; r < n; r++) {
                size_t pos = 0;
                RegisterDefinition * reg = s->info.is_top_frame ? s->ctx->reg_defs + r : s->reg_defs[r];
                uint8_t * data = s->reg_data.data + reg->offset;
                uint8_t * mask = s->reg_data.mask + reg->offset;
                while (pos < reg->size) {
                    size_t rd = json_read_binary_data(&state, data + pos, reg->size - pos);
                    memset(mask + pos, ~0, rd);
                    if (rd == 0) break;
                    pos += rd;
                }
            }
            json_read_binary_end(&state);
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
    if (s->disposed) free_stack_frame_cache(s);
    context_unlock(ctx);
    if (trap.error) exception(trap.error);
}

static void validate_reg_children_cache(Channel * c, void * args, int error) {
    StackFrameCache * s = (StackFrameCache *)args;
    Context * ctx = s->ctx->ctx;
    Trap trap;

    assert(s->pending != NULL);
    assert(s->error == NULL);
    if (set_trap(&trap)) {
        s->pending = NULL;
        if (!error) {
            ids_buf_pos = 0;
            str_buf_pos = 0;
            error = read_errno(&c->inp);
            json_read_array(&c->inp, read_ids_item, NULL);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
            if (!error && !s->disposed) {
                int n = 0;
                s->reg_cnt = ids_buf_pos;
                s->reg_defs = (RegisterDefinition **)loc_alloc_zero(sizeof(RegisterDefinition *) * s->reg_cnt);
                for (n = 0; n < s->reg_cnt; n++) {
                    unsigned r = 0;
                    char * id = str_buf + ids_buf[n];
                    if (*id++ != 'R') {
                        error = ERR_INV_CONTEXT;
                        break;
                    }
                    while (*id >= '0' && *id <= '9') {
                        r = r * 10 + (*id++ - '0');
                    }
                    if (r >= s->ctx->reg_cnt) {
                        error = ERR_INV_CONTEXT;
                        break;
                    }
                    s->reg_defs[n] = s->ctx->reg_defs + r;
                }
                if (!error) {
                    s->pending = protocol_send_command(c, "Registers", "getm", validate_reg_values_cache, s);
                    write_stream(&c->out, '[');
                    for (n = 0; n < s->reg_cnt; n++) {
                        RegisterDefinition * reg = s->reg_defs[n];
                        char * id = str_buf + ids_buf[n];
                        if (n > 0) write_stream(&c->out, ',');
                        write_stream(&c->out, '[');
                        json_write_string(&c->out, id);
                        write_stream(&c->out, ',');
                        json_write_long(&c->out, 0);
                        write_stream(&c->out, ',');
                        json_write_long(&c->out, reg->size);
                        write_stream(&c->out, ']');
                    }
                    write_stream(&c->out, ']');
                    write_stream(&c->out, 0);
                    write_stream(&c->out, MARKER_EOM);
                    clear_trap(&trap);
                    return;
                }
            }
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    s->error = get_error_report(error);
    cache_notify(&s->cache);
    if (s->disposed) free_stack_frame_cache(s);
    context_unlock(ctx);
    if (trap.error) exception(trap.error);
}

static void read_stack_frame_property(InputStream * inp, const char * name, void * args) {
    StackFrameCache * s = (StackFrameCache *)args;
    if (strcmp(name, "FP") == 0) s->info.fp = (ContextAddress)json_read_uint64(inp);
    else if (strcmp(name, "IP") == 0) s->ip = (ContextAddress)json_read_uint64(inp);
    else if (strcmp(name, "RP") == 0) s->rp = (ContextAddress)json_read_uint64(inp);
    else if (strcmp(name, "TopFrame") == 0) s->info.is_top_frame = json_read_boolean(inp);
    else json_skip_object(inp);
}

static void read_stack_frame(InputStream * inp, void * args) {
    json_read_struct(inp, read_stack_frame_property, args);
}

static void validate_stack_frame_cache(Channel * c, void * args, int error) {
    StackFrameCache * s = (StackFrameCache *)args;
    Context * ctx = s->ctx->ctx;
    Trap trap;

    assert(s->pending != NULL);
    assert(s->error == NULL);
    if (set_trap(&trap)) {
        s->pending = NULL;
        if (!error) {
            json_read_array(&c->inp, read_stack_frame, s);
            if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
            error = read_errno(&c->inp);
            if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
            if (!error && !s->disposed) {
                if (s->info.is_top_frame) {
                    RegisterDefinition * reg = s->ctx->reg_defs;
                    s->pending = protocol_send_command(c, "Registers", "getm", validate_reg_values_cache, s);
                    write_stream(&c->out, '[');
                    while (reg->name) {
                        write_stream(&c->out, '[');
                        json_write_string(&c->out, register2id(s->ctx->ctx, s->frame, reg));
                        write_stream(&c->out, ',');
                        json_write_long(&c->out, 0);
                        write_stream(&c->out, ',');
                        json_write_long(&c->out, reg->size);
                        write_stream(&c->out, ']');
                        if ((++reg)->name) write_stream(&c->out, ',');
                    }
                    write_stream(&c->out, ']');
                }
                else {
                    s->pending = protocol_send_command(c, "Registers", "getChildren", validate_reg_children_cache, s);
                    json_write_string(&c->out, frame2id(s->ctx->ctx, s->frame));
                }
                write_stream(&c->out, 0);
                write_stream(&c->out, MARKER_EOM);
                clear_trap(&trap);
                return;
            }
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    s->error = get_error_report(error);
    cache_notify(&s->cache);
    if (s->disposed) free_stack_frame_cache(s);
    context_unlock(ctx);
    if (trap.error) exception(trap.error);
}

int get_frame_info(Context * ctx, int frame, StackFrame ** info) {
    ContextCache * cache = *EXT(ctx);
    Channel * c = cache->peer->target;
    StackFrameCache * s = NULL;
    LINK * l = NULL;
    const char * id = NULL;
    Trap trap;

    if (!cache->has_state) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (cache->ctx->exited) {
        errno = ERR_ALREADY_EXITED;
        return -1;
    }

    if (!set_trap(&trap)) return -1;

    assert(frame >= 0);
    check_registers_cache(cache);
    for (l = cache->stk_cache_list.next; l != &cache->stk_cache_list; l = l->next) {
        s = ctx2stk(l);
        if (s->frame == frame) {
            assert(!s->disposed);
            if (s->pending != NULL) cache_wait(&s->cache);
            *info = &s->info;
            clear_trap(&trap);
            set_error_report_errno(s->error);
            return !errno ? 0 : -1;
        }
    }

    id = frame2id(cache->ctx, frame);
    if (id == NULL) {
        clear_trap(&trap);
        return -1;
    }

    s = (StackFrameCache *)loc_alloc_zero(sizeof(StackFrameCache));
    list_add_first(&s->link_ctx, &cache->stk_cache_list);
    s->ctx = cache;
    s->frame = frame;
    s->info.ctx = ctx;
    s->info.regs = &s->reg_data;
    s->reg_data.data = (uint8_t *)loc_alloc_zero(cache->reg_size);
    s->reg_data.mask = (uint8_t *)loc_alloc_zero(cache->reg_size);
    s->pending = protocol_send_command(c, "StackTrace", "getContext", validate_stack_frame_cache, s);
    write_stream(&c->out, '[');
    json_write_string(&c->out, id);
    write_stream(&c->out, ']');
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
    context_lock(ctx);
    cache_wait(&s->cache);
    return -1;
}

int get_top_frame(Context * ctx) {
    set_errno(ERR_UNSUPPORTED, "get_top_frame()");
    return STACK_TOP_FRAME;
}

unsigned context_word_size(Context * ctx) {
    RegisterDefinition * pc = get_PC_definition(ctx);
    return pc->size;
}

static void channel_close_listener(Channel * c) {
    LINK * l = NULL;

    for (l = peers.next; l != &peers; l = l->next) {
        PeerCache * p = peers2peer(l);
        if (p->target == c) {
            int i;
            assert(p->rc_pending_cnt == 0);
            for (i = 0; i < CTX_ID_HASH_SIZE; i++) {
                LINK * h = p->context_id_hash + i;
                while (!list_is_empty(h)) {
                    ContextCache * c = idhashl2ctx(h->next);
                    assert(*EXT(c->ctx) == c);
                    send_context_exited_event(c->ctx);
                }
            }
            channel_unlock(p->host);
            channel_unlock(p->target);
            cache_dispose(&p->rc_cache);
            release_error_report(p->rc_error);
            list_remove(&p->link_all);
            loc_free(p);
            return;
        }
    }
}

static void event_context_exited(Context * ctx, void * args) {
    ContextCache * c = *EXT(ctx);
    assert(!list_is_empty(&c->id_hash_link));
    list_remove(&c->id_hash_link);
}

static void event_context_disposed(Context * ctx, void * args) {
    ContextCache * c = *EXT(ctx);
    assert(c->ctx == ctx);
    c->ctx = NULL;
    free_context_cache(c);
}

void init_contexts_sys_dep(void) {
    static ContextEventListener listener = {
        NULL,
        event_context_exited,
        NULL,
        NULL,
        NULL,
        event_context_disposed
    };
    add_context_event_listener(&listener, NULL);
    add_channel_close_listener(channel_close_listener);
    context_extension_offset = context_extension(sizeof(ContextCache *));
    list_init(&peers);
}

#endif /* ENABLE_DebugContext && ENABLE_ContextProxy */
