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
 * This module handles process/thread OS contexts and their state machine.
 */

#include <config.h>

#include <assert.h>
#include <framework/context.h>
#include <framework/myalloc.h>

typedef struct Listener {
    ContextEventListener * func;
    void * args;
} Listener;

static Listener * listeners = NULL;
static unsigned listener_cnt = 0;
static unsigned listener_max = 0;

LINK context_root = { NULL, NULL };

char * pid2id(pid_t pid, pid_t parent) {
    static char s[64];
    char * p = s + sizeof(s);
    unsigned long n = (long)pid;
    *(--p) = 0;
    do {
        *(--p) = (char)(n % 10 + '0');
        n = n / 10;
    }
    while (n != 0);
    if (parent != 0) {
        n = (long)parent;
        *(--p) = '.';
        do {
            *(--p) = (char)(n % 10 + '0');
            n = n / 10;
        }
        while (n != 0);
    }
    *(--p) = 'P';
    return p;
}

pid_t id2pid(const char * id, pid_t * parent) {
    pid_t pid = 0;
    if (parent != NULL) *parent = 0;
    if (id == NULL) return 0;
    if (id[0] != 'P') return 0;
    if (id[1] == 0) return 0;
    pid = (pid_t)strtol(id + 1, (char **)&id, 10);
    if (id[0] == '.') {
        if (id[1] == 0) return 0;
        if (parent != NULL) *parent = pid;
        pid = (pid_t)strtol(id + 1, (char **)&id, 10);
    }
    if (id[0] != 0) return 0;
    return pid;
}

void add_context_event_listener(ContextEventListener * listener, void * client_data) {
    if (listener_cnt >= listener_max) {
        listener_max += 8;
        listeners = (Listener *)loc_realloc(listeners, listener_max * sizeof(Listener));
    }
    listeners[listener_cnt].func = listener;
    listeners[listener_cnt].args = client_data;
    listener_cnt++;
}

#if ENABLE_DebugContext

static size_t extension_size = 0;
static int context_created = 0;

size_t context_extension(size_t size) {
    size_t offs = 0;
    assert(!context_created);
    while (extension_size % sizeof(void *) != 0) extension_size++;
    offs = sizeof(Context) + extension_size;
    extension_size += size;
    return offs;
}

Context * create_context(const char * id) {
    Context * ctx = (Context *)loc_alloc_zero(sizeof(Context) + extension_size);

    strlcpy(ctx->id, id, sizeof(ctx->id));
    list_init(&ctx->children);
    context_created = 1;
    return ctx;
}

void context_lock(Context * ctx) {
    assert(ctx->ref_count > 0);
    ctx->ref_count++;
}

void context_unlock(Context * ctx) {
    assert(ctx->ref_count > 0);
    if (--(ctx->ref_count) == 0) {
        unsigned i;

        assert(ctx->exited);
        assert(list_is_empty(&ctx->children));
        if (ctx->parent != NULL) {
            list_remove(&ctx->cldl);
            context_unlock(ctx->parent);
            ctx->parent = NULL;
        }
        if (ctx->creator != NULL) {
            context_unlock(ctx->creator);
            ctx->creator = NULL;
        }

        assert(!ctx->event_notification);
        ctx->event_notification = 1;
        for (i = 0; i < listener_cnt; i++) {
            Listener * l = listeners + i;
            if (l->func->context_disposed == NULL) continue;
            l->func->context_disposed(ctx, l->args);
        }
        ctx->event_notification = 0;
        list_remove(&ctx->ctxl);
        loc_free(ctx);
    }
}

const char * context_state_name(Context * ctx) {
    if (ctx->exited) return "exited";
    if (ctx->stopped) return "stopped";
    return "running";
}

void send_context_created_event(Context * ctx) {
    unsigned i;
    assert(ctx->ref_count > 0);
    assert(!ctx->event_notification);
    ctx->event_notification = 1;
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->func->context_created == NULL) continue;
        l->func->context_created(ctx, l->args);
    }
    ctx->event_notification = 0;
}

void send_context_changed_event(Context * ctx) {
    unsigned i;
    assert(ctx->ref_count > 0);
    assert(!ctx->event_notification);
    ctx->event_notification = 1;
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->func->context_changed == NULL) continue;
        l->func->context_changed(ctx, l->args);
    }
    ctx->event_notification = 0;
}

void send_context_stopped_event(Context * ctx) {
    unsigned i;
    assert(ctx->ref_count > 0);
    assert(ctx->stopped != 0);
    assert(!ctx->event_notification);
    ctx->event_notification = 1;
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->func->context_stopped == NULL) continue;
        l->func->context_stopped(ctx, l->args);
        assert(ctx->stopped != 0);
    }
    ctx->event_notification = 0;
}

void send_context_started_event(Context * ctx) {
    unsigned i;
    assert(ctx->ref_count > 0);
    ctx->stopped = 0;
    ctx->stopped_by_bp = 0;
    ctx->stopped_by_exception = 0;
    ctx->event_notification++;
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->func->context_started == NULL) continue;
        l->func->context_started(ctx, l->args);
    }
    ctx->event_notification--;
}

void send_context_exited_event(Context * ctx) {
    unsigned i;
    assert(!ctx->event_notification);
    ctx->exiting = 0;
    ctx->pending_intercept = 0;
    ctx->exited = 1;
    ctx->event_notification = 1;
    for (i = 0; i < listener_cnt; i++) {
        Listener * l = listeners + i;
        if (l->func->context_exited == NULL) continue;
        l->func->context_exited(ctx, l->args);
    }
    ctx->event_notification = 0;
    context_unlock(ctx);
}

void ini_contexts(void) {
    list_init(&context_root);
    init_contexts_sys_dep();
}

#endif  /* if ENABLE_DebugContext */
