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
#include "trace.h"
#include "exceptions.h"
#include "protocol.h"
#include "json.h"
#include "cache.h"
#include "pathmap.h"
#include "memorymap.h"
#include "context-proxy.h"

typedef struct ContextCache ContextCache;
typedef struct MemoryCache MemoryCache;
typedef struct PeerCache PeerCache;

struct ContextCache {
    LINK link_peer;

    char id[256];
    char parent_id[256];
    Context * ctx;
    PeerCache * peer;

    /* Memory Map */
    AbstractCache mmap_cache;
    ErrorReport * mmap_error;
    unsigned mmap_size;
    MemoryRegion * mmap_regions;
    ReplyHandlerInfo * pending_get_mmap;

    /* Register definitions */
    AbstractCache regs_cache;
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
    int pc_valid;
    int64_t suspend_pc;
    char suspend_reason[256];

    /* Memory */
    LINK mem_cache;
};

struct MemoryCache {
    LINK link_ctx;
    Context * ctx;
    AbstractCache cache;
    ErrorReport * error;
    ContextAddress addr;
    void * buf;
    size_t size;
    ReplyHandlerInfo * pending_command;
};

struct PeerCache {
    LINK link_all;
    LINK ctx_cache;
    Channel * host;
    Channel * target;
    ForwardingInputStream fwd;
    InputStream * fwd_inp;

    /* Initial Run Control context tree retrieval */
    int rc_done;
    int rc_pending_cnt;
    ErrorReport * rc_error;
    AbstractCache rc_cache;
};

#define peers2peer(A)    ((PeerCache *)((char *)(A) - offsetof(PeerCache, link_all)))
#define peer2ctx(A)      ((ContextCache *)((char *)(A) - offsetof(ContextCache, link_peer)))
#define ctx2mem(A)       ((MemoryCache *)((char *)(A) - offsetof(MemoryCache, link_ctx)))

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

static ContextCache * find_context_cache(PeerCache * p, const char * id) {
    LINK * l;
    for (l = p->ctx_cache.next; l != &p->ctx_cache; l = l->next) {
        ContextCache * c = peer2ctx(l);
        if (strcmp(c->id, id) == 0) return c;
    }
    return NULL;
}

static void add_context_cache(PeerCache * p, ContextCache * c) {
    list_init(&c->mem_cache);
    list_add_first(&c->link_peer, &p->ctx_cache);
    c->peer = p;
    c->ctx = (Context *)loc_alloc_zero(sizeof(Context));
    list_init(&c->ctx->children);
    list_init(&c->ctx->cldl);
    c->ctx->pid = id2pid(c->id, NULL);
    c->ctx->mem = c->ctx->pid;
    c->ctx->proxy = c;
    c->ctx->ref_count = 1;
    if (c->parent_id[0]) {
        ContextCache * h = find_context_cache(p, c->parent_id);
        if (h != NULL) {
            c->ctx->parent = h->ctx;
            c->ctx->mem = h->ctx->mem;
            h->ctx->ref_count++;
            list_add_last(&c->ctx->cldl, &h->ctx->children);
        }
        else if (p->rc_done) {
            trace(LOG_ALWAYS, "Invalid parent ID in 'context added' event: %s", c->parent_id);
        }
    }
    send_context_created_event(c->ctx);
}

static void free_context_cache(ContextCache * c) {
    assert(c->pending_get_mmap == NULL);
    assert(c->pending_get_regs == NULL);
    cache_dispose(&c->mmap_cache);
    cache_dispose(&c->regs_cache);
    if (c->peer != NULL && c->link_peer.next != NULL) list_remove(&c->link_peer);
    release_error_report(c->mmap_error);
    release_error_report(c->reg_error);
    loc_free(c->mmap_regions);
    loc_free(c->reg_ids);
    loc_free(c->reg_ids_str);
    loc_free(c->reg_defs);
    while (!list_is_empty(&c->mem_cache)) {
        MemoryCache * m = ctx2mem(c->mem_cache.next);
        assert(m->pending_command == NULL);
        list_remove(&m->link_ctx);
        release_error_report(m->error);
        cache_dispose(&m->cache);
        loc_free(m->buf);
        loc_free(m);
    }
    loc_free(c);
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
    PeerCache * p = (PeerCache *)args;
    ContextCache * c = loc_alloc_zero(sizeof(ContextCache));

    json_read_struct(inp, read_run_control_context_property, c);

    if (find_context_cache(p, c->id) == NULL && (c->parent_id[0] == 0 || find_context_cache(p, c->parent_id) != NULL)) {
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
        if (c->mmap_regions != NULL || c->mmap_error != NULL) {
            release_error_report(c->mmap_error);
            loc_free(c->mmap_regions);
            c->mmap_error = NULL;
            c->mmap_regions = NULL;
            c->mmap_size = 0;
        }
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
        assert(c->ctx->proxy == c);
        c->ctx->exited = 1;
        send_context_exited_event(c->ctx);
        if (c->ctx->parent != NULL) {
            list_remove(&c->ctx->cldl);
            context_unlock(c->ctx->parent);
            c->ctx->parent = NULL;
        }
        context_unlock(c->ctx);
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
        assert(c->ctx->proxy == c);
        if (!c->ctx->stopped) {
            c->ctx->stopped = 1;
            c->ctx->intercepted = 1;
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
        assert(c->ctx->proxy == c);
        if (c->ctx->stopped) {
            c->ctx->stopped = 0;
            c->ctx->intercepted = 0;
            c->pc_valid = 0;
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
    json_read_array(p->fwd_inp, read_context_added_item, p);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_context_changed(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextChanged");
    json_read_array(p->fwd_inp, read_context_changed_item, p);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_context_removed(Channel * c, void * args) {
    PeerCache * p = (PeerCache *)args;
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextRemoved");
    json_read_array(p->fwd_inp, read_context_removed_item, p);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
}

static void event_context_suspended(Channel * ch, void * args) {
    PeerCache * p = (PeerCache *)args;
    ContextCache * c = NULL;
    char id[256];
    char reason[sizeof(c->suspend_reason)];
    int64_t pc;
    ContextCache buf;

    assert(p->target == ch);
    write_stringz(&p->host->out, "E");
    write_stringz(&p->host->out, RUN_CONTROL);
    write_stringz(&p->host->out, "contextSuspended");
    json_read_string(p->fwd_inp, id, sizeof(id));
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    pc = json_read_int64(p->fwd_inp);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_string(p->fwd_inp, reason, sizeof(reason));
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    json_read_struct(p->fwd_inp, read_context_suspended_data, &buf);
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    c = find_context_cache(p, id);
    if (c != NULL) {
        assert(c->ctx->proxy == c);
        c->pc_valid = 1;
        c->suspend_pc = pc;
        strcpy(c->suspend_reason, reason);
        if (!c->ctx->stopped) {
            c->ctx->stopped = 1;
            c->ctx->intercepted = 1;
            send_context_stopped_event(c->ctx);
        }
    }
    else if (p->rc_done) {
        trace(LOG_ALWAYS, "Invalid ID in 'context suspended' event: %s", id);
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
    json_read_string(p->fwd_inp, id, sizeof(id));
    if (read_stream(p->fwd_inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(p->fwd_inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    c = find_context_cache(p, id);
    if (c != NULL) {
        assert(c->ctx->proxy == c);
        if (c->ctx->stopped) {
            c->ctx->stopped = 0;
            c->ctx->intercepted = 0;
            c->pc_valid = 0;
            send_context_started_event(c->ctx);
        }
    }
    else if (p->rc_done) {
        trace(LOG_ALWAYS, "Invalid ID in 'context resumed' event: %s", id);
    }
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
    list_init(&p->ctx_cache);
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
    ctx->ref_count++;
}

void context_unlock(Context * ctx) {
    assert(ctx->ref_count > 0);
    if (--ctx->ref_count == 0) {
        ContextCache * c = ctx->proxy;
        assert(list_is_empty(&ctx->children));
        assert(ctx->parent == NULL);
        loc_free(ctx);
        c->ctx = NULL;
        free_context_cache(c);
    }
}

static void validate_peer_cache_context(Channel * c, void * args, int error);
static void validate_peer_cache_state(Channel * c, void * args, int error);

static void read_rc_children_item(InputStream * inp, void * args) {
    char id[256];
    PeerCache * p = (PeerCache *)args;

    json_read_string(inp, id, sizeof(id));

    if (find_context_cache(p, id) == NULL) {
        ContextCache * c = loc_alloc_zero(sizeof(ContextCache));
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
        p->rc_done = 1;
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

    assert(p->target == c);
    assert(p->rc_pending_cnt > 0);
    p->rc_pending_cnt--;
    if (error) {
        set_rc_error(p, error);
    }
    else {
        set_rc_error(p, read_errno(&c->inp));
        json_read_array(&c->inp, read_rc_children_item, p);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
        flush_stream(&p->target->out);
    }
    set_rc_done(p);
}

static void validate_peer_cache_context(Channel * c, void * args, int error) {
    ContextCache * x = args;
    PeerCache * p = x->peer;

    assert(p->target == c);
    assert(p->rc_pending_cnt > 0);
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
            flush_stream(&p->target->out);
            p->rc_pending_cnt++;
        }
        else {
            add_context_cache(p, x);
            protocol_send_command(p->target, "RunControl", "getChildren", validate_peer_cache_children, p);
            json_write_string(&p->target->out, x->id);
            write_stream(&p->target->out, 0);
            write_stream(&p->target->out, MARKER_EOM);
            flush_stream(&p->target->out);
            p->rc_pending_cnt++;
        }
    }
    set_rc_done(p);
}

static void validate_peer_cache_state(Channel * c, void * args, int error) {
    ContextCache * x = args;
    PeerCache * p = x->peer;

    assert(p->target == c);
    assert(p->rc_error == NULL);
    assert(p->rc_pending_cnt > 0);
    p->rc_pending_cnt--;
    if (error) {
        set_rc_error(p, error);
        free_context_cache(x);
    }
    else {
        set_rc_error(p, error = read_errno(&c->inp));
        x->pc_valid = json_read_boolean(&c->inp);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        x->suspend_pc = json_read_int64(&c->inp);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        json_read_string(&c->inp, x->suspend_reason, sizeof(x->suspend_reason));
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        json_read_struct(&c->inp, read_context_suspended_data, x);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

        if (error || find_context_cache(p, x->id) != NULL) {
            free_context_cache(x);
        }
        else {
            add_context_cache(p, x);
            x->ctx->stopped = x->ctx->intercepted = x->pc_valid;
            if (x->pc_valid) send_context_stopped_event(x->ctx);
            protocol_send_command(p->target, "RunControl", "getChildren", validate_peer_cache_children, p);
            json_write_string(&p->target->out, x->id);
            write_stream(&p->target->out, 0);
            write_stream(&p->target->out, MARKER_EOM);
            flush_stream(&p->target->out);
            p->rc_pending_cnt++;
        }
    }
    set_rc_done(p);
}

Context * id2ctx(const char * id) {
    LINK * l;
    Channel * c = cache_channel();
    for (l = peers.next; l != &peers; l = l->next) {
        PeerCache * p = peers2peer(l);
        if (p->host == c || p->target == c) {
            if (p->rc_pending_cnt > 0) {
                cache_wait(&p->rc_cache);
            }
            else if (p->rc_error != NULL) {
                errno = set_error_report_errno(p->rc_error);
            }
            else if (!p->rc_done) {
                protocol_send_command(p->target, "RunControl", "getChildren", validate_peer_cache_children, p);
                write_stringz(&p->target->out, "null");
                write_stream(&p->target->out, MARKER_EOM);
                flush_stream(&p->target->out);
                p->rc_pending_cnt++;
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
    ContextCache * cache = (ContextCache *)ctx->proxy;
    return cache->has_state;
}

static void validate_memory_cache(Channel * c, void * args, int error) {
    MemoryCache * m = (MemoryCache *)args;

    assert(m->pending_command != NULL);
    assert(m->error == NULL);
    m->pending_command = NULL;
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
        while (read_stream(&c->inp) != 0) {}
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    }
    cache_notify(&m->cache);
    context_unlock(m->ctx);
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
        m = ctx2mem(l);
        if (address >= m->addr && address + size <= m->addr + m->size) {
            if (m->pending_command != NULL) cache_wait(&m->cache);
            memcpy(buf, (int8_t *)m->buf + (address - m->addr), size);
            errno = set_error_report_errno(m->error);
            return !errno ? 0 : -1;
        }
    }

    m = loc_alloc_zero(sizeof(MemoryCache));
    m->ctx = ctx;
    m->addr = address;
    m->buf = loc_alloc(size);
    m->size = size;
    m->pending_command = protocol_send_command(c, "Memory", "get", validate_memory_cache, m);
    json_write_string(&c->out, ctx2id(cache->ctx));
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
    flush_stream(&c->out);
    context_lock(ctx);
    cache_wait(&m->cache);
    return -1;
}

static void read_memory_region_property(InputStream * inp, char * name, void * args) {
    MemoryRegion * m = (MemoryRegion *)args;
    if (strcmp(name, "Addr") == 0) m->addr = (ContextAddress)json_read_int64(inp);
    else if (strcmp(name, "Size") == 0) m->size = json_read_ulong(inp);
    else if (strcmp(name, "Offs") == 0) m->file_offs = json_read_ulong(inp);
    else if (strcmp(name, "Flags") == 0) m->flags = json_read_ulong(inp);
    else if (strcmp(name, "FileName") == 0) m->file_name = json_read_alloc_string(inp);
    else if (strcmp(name, "SectionName") == 0) m->sect_name = json_read_alloc_string(inp);
    else loc_free(json_skip_object(inp));
}

static void read_memory_map_item(InputStream * inp, void * args) {
    Channel * c = args;
    MemoryRegion * m;
    if (mem_buf_pos >= mem_buf_max) {
        mem_buf_max = mem_buf_max == 0 ? 16 : mem_buf_max * 2;
        mem_buf = loc_realloc(mem_buf, sizeof(MemoryRegion) * mem_buf_max);
    }
    m = mem_buf + mem_buf_pos;
    memset(m, 0, sizeof(MemoryRegion));
    if (json_read_struct(inp, read_memory_region_property, m) && m->file_name != NULL) {
        struct stat buf;
        char * fnm = path_map_to_local(c, m->file_name);
        if (fnm != NULL) {
            loc_free(m->file_name);
            m->file_name = loc_strdup(fnm);
        }
        if (m->file_name == NULL || stat(m->file_name, &buf) < 0) {
            loc_free(m->file_name);
        }
        else {
            m->dev = buf.st_dev;
            m->ino = buf.st_ino;
            mem_buf_pos++;
        }
    }
}

static void validate_memory_map_cache(Channel * c, void * args, int error) {
    ContextCache * cache = (ContextCache *)args;

    assert(cache->ctx->parent == NULL);
    assert(cache->mmap_regions == NULL);
    assert(cache->pending_get_mmap != NULL);
    cache->pending_get_mmap = NULL;
    cache->mmap_error = get_error_report(error);
    if (!error) {
        cache->mmap_error = get_error_report(read_errno(&c->inp));
        mem_buf_pos = 0;
        json_read_array(&c->inp, read_memory_map_item, cache->peer->host);
        cache->mmap_size = mem_buf_pos;
        cache->mmap_regions = loc_alloc(sizeof(MemoryRegion) * mem_buf_pos);
        memcpy(cache->mmap_regions, mem_buf, sizeof(MemoryRegion) * mem_buf_pos);
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    }
    cache_notify(&cache->mmap_cache);
    context_unlock(cache->ctx);
}

void memory_map_get_regions(Context * ctx, MemoryRegion ** regions, unsigned * cnt) {
    ContextCache * cache = NULL;
    while (ctx->parent != NULL && ctx->parent->mem == ctx->mem) ctx = ctx->parent;
    cache = (ContextCache *)ctx->proxy;
    assert(cache->ctx == ctx);
    if (cache->pending_get_mmap != NULL) cache_wait(&cache->mmap_cache);
    if (cache->mmap_regions == NULL && cache->mmap_error == NULL && cache->peer != NULL) {
        Channel * c = cache->peer->target;
        cache->pending_get_mmap = protocol_send_command(c, "MemoryMap", "get", validate_memory_map_cache, cache);
        json_write_string(&c->out, cache->id);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);
        flush_stream(&c->out);
        context_lock(ctx);
        cache_wait(&cache->mmap_cache);
    }
    *regions = cache->mmap_regions;
    *cnt = cache->mmap_size;
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

static int validate_registers_cache(Channel * c, void * args, int error) {
    ContextCache * cache = (ContextCache *)args;

    assert(cache->peer->target == c);
    if (cache->ctx->parent != NULL) {
        /* Get register descriptions */
        if (cache->pending_get_regs != NULL) {
            assert(cache->reg_ids == NULL);
            assert(cache->reg_ids_str == NULL);
            assert(cache->reg_error = NULL);
            cache->pending_get_regs = NULL;
            cache->reg_error = get_error_report(error);
            if (!error) {
                unsigned i;
                cache->reg_error = get_error_report(read_errno(&c->inp));
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
            context_unlock(cache->ctx);
        }
        else if (cache->reg_ids == NULL && cache->reg_error == 0) {
            cache->pending_get_regs = protocol_send_command(c, "Registers", "getChildren", validate_registers_cache, args);
            write_stringz(&c->out, ctx2id(cache->ctx));
            write_stream(&c->out, MARKER_EOM);
            flush_stream(&c->out);
            context_lock(cache->ctx);
            return 0;
        }

        /* TODO: read register defs and register values */
        if (cache->reg_error == NULL) cache->reg_error = get_error_report(ERR_UNSUPPORTED);
    }

    return 1;
}

RegisterDefinition * get_reg_definitions(Context * ctx) {
    ContextCache * cache = (ContextCache *)ctx->proxy;
    if (!validate_registers_cache(cache->peer->target, cache, 0)) cache_wait(&cache->regs_cache);
    return cache->reg_defs;
}

RegisterDefinition * get_PC_definition(Context * ctx) {
    ContextCache * cache = (ContextCache *)ctx->proxy;
    if (!validate_registers_cache(cache->peer->target, cache, 0)) cache_wait(&cache->regs_cache);
    return cache->pc_def;
}

RegisterDefinition * get_reg_by_id(Context * ctx, unsigned id, unsigned munbering_convention) {
    RegisterDefinition * defs;
    ContextCache * cache = (ContextCache *)ctx->proxy;
    if (!validate_registers_cache(cache->peer->target, cache, 0)) cache_wait(&cache->regs_cache);
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
            assert(p->rc_pending_cnt == 0);
            channel_unlock(p->host);
            channel_unlock(p->target);
            cache_dispose(&p->rc_cache);
            release_error_report(p->rc_error);
            list_remove(&p->link_all);
            while (!list_is_empty(&p->ctx_cache)) {
                ContextCache * c = peer2ctx(p->ctx_cache.next);
                c->peer = NULL;
                list_remove(&c->link_peer);
                if (c->ctx->parent != NULL) {
                    list_remove(&c->ctx->cldl);
                    context_unlock(c->ctx->parent);
                    c->ctx->parent = NULL;
                }
                context_unlock(c->ctx);
            }
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
