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
 * This module handles process/thread OS contexts and their state machine.
 */

#include "config.h"

#include <stdlib.h>
#include <assert.h>
#include <errno.h>
#include <signal.h>
#include "context.h"
#include "events.h"
#include "errors.h"
#include "trace.h"
#include "myalloc.h"
#include "breakpoints.h"
#include "waitpid.h"

static ContextEventListener * event_listeners = NULL;

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

char * ctx2id(Context * ctx) {
    if (ctx->parent == NULL) return pid2id(ctx->pid, 0);
    assert(ctx->parent->parent == NULL);
    return pid2id(ctx->pid, ctx->parent->pid);
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
    listener->client_data = client_data;
    listener->next = event_listeners;
    event_listeners = listener;
}

#if ENABLE_DebugContext

#if !ENABLE_ContextProxy

#define CONTEXT_PID_HASH_SIZE 1024
#define CONTEXT_PID_HASH(PID) ((unsigned)(PID) % CONTEXT_PID_HASH_SIZE)
#define pidl2ctxp(A) ((Context *)((char *)(A) - offsetof(Context, pidl)))

LINK context_root = { NULL, NULL };

static LINK context_pid_root[CONTEXT_PID_HASH_SIZE];

void link_context(Context * ctx) {
    LINK * qhp = &context_pid_root[CONTEXT_PID_HASH(ctx->pid)];

    assert(ctx->pid != 0);
    assert(ctx->mem != 0);
    assert(context_find_from_pid(ctx->pid, ctx->parent != NULL) == NULL);
    list_remove(&ctx->ctxl);
    list_remove(&ctx->pidl);
    list_add_first(&ctx->ctxl, &context_root);
    list_add_first(&ctx->pidl, qhp);
    ctx->ref_count++;
}

Context * context_find_from_pid(pid_t pid, int thread) {
    LINK * qhp = &context_pid_root[CONTEXT_PID_HASH(pid)];
    LINK * qp = qhp->next;

    assert(is_dispatch_thread());
    if (qp == NULL) return NULL;
    while (qp != qhp) {
        Context * ctx = pidl2ctxp(qp);
        if (ctx->pid == pid && !ctx->exited &&
            (ctx->parent != NULL) == (thread != 0)) return ctx;
        qp = qp->next;
    }
    return NULL;
}

Context * id2ctx(const char * id) {
    pid_t parent = 0;
    pid_t pid = id2pid(id, &parent);
    if (pid == 0) return NULL;
    return context_find_from_pid(pid, parent != 0);
}

void context_lock(Context * ctx) {
    assert(ctx->ref_count > 0);
    ctx->ref_count++;
}

void context_unlock(Context * ctx) {
    assert(ctx->ref_count > 0);
    if (--(ctx->ref_count) == 0) {
        assert(list_is_empty(&ctx->children));
        assert(ctx->parent == NULL);
        list_remove(&ctx->ctxl);
        list_remove(&ctx->pidl);
        release_error_report(ctx->regs_error);
        loc_free(ctx->bp_ids);
        loc_free(ctx->regs);
        loc_free(ctx);
    }
}

Context * create_context(pid_t pid, size_t regs_size) {
    Context * ctx = (Context *)loc_alloc_zero(sizeof(Context));

    ctx->pid = pid;
    if ((ctx->regs_size = regs_size) > 0) {
        ctx->regs = (RegisterData *)loc_alloc_zero(regs_size);
    }
    list_init(&ctx->children);
    list_init(&ctx->ctxl);
    list_init(&ctx->pidl);
    list_init(&ctx->cldl);
    return ctx;
}

#endif /* !ENABLE_ContextProxy */

const char * context_state_name(Context * ctx) {
    if (ctx->exited) return "exited";
    if (ctx->intercepted) return "intercepted";
    if (ctx->stopped) return "stopped";
    return "running";
}

void send_context_created_event(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    assert(ctx->ref_count > 0);
    assert(!ctx->event_notification);
    ctx->event_notification = 1;
    while (listener != NULL) {
        if (listener->context_created != NULL) {
            listener->context_created(ctx, listener->client_data);
        }
        listener = listener->next;
    }
    ctx->event_notification = 0;
}

void send_context_changed_event(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    assert(ctx->ref_count > 0);
    assert(!ctx->event_notification);
    ctx->event_notification = 1;
    while (listener != NULL) {
        if (listener->context_changed != NULL) {
            listener->context_changed(ctx, listener->client_data);
        }
        listener = listener->next;
    }
    ctx->event_notification = 0;
}

void send_context_stopped_event(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    assert(ctx->ref_count > 0);
    assert(ctx->stopped != 0);
    assert(!ctx->event_notification);
    ctx->event_notification = 1;
#if !ENABLE_ContextProxy
    if (ctx->bp_ids != NULL) {
        loc_free(ctx->bp_ids);
        ctx->bp_ids = NULL;
    }
    if (ctx->stopped_by_bp) {
        evaluate_breakpoint_condition(ctx);
    }
#endif
    while (listener != NULL) {
        if (listener->context_stopped != NULL) {
            listener->context_stopped(ctx, listener->client_data);
        }
        listener = listener->next;
    }
    ctx->event_notification = 0;
}

void send_context_started_event(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    assert(ctx->ref_count > 0);
    ctx->stopped = 0;
#if !ENABLE_ContextProxy
    ctx->stopped_by_bp = 0;
    ctx->stopped_by_exception = 0;
#endif
    ctx->event_notification++;
    while (listener != NULL) {
        if (listener->context_started != NULL) {
            listener->context_started(ctx, listener->client_data);
        }
        listener = listener->next;
    }
    ctx->event_notification--;
}

void send_context_exited_event(Context * ctx) {
    ContextEventListener * listener = event_listeners;
    assert(!ctx->event_notification);
    ctx->event_notification = 1;
    while (listener != NULL) {
        if (listener->context_exited != NULL) {
            listener->context_exited(ctx, listener->client_data);
        }
        listener = listener->next;
    }
    ctx->event_notification = 0;
}

unsigned context_word_size(Context * ctx) {
    /* Place holder to support variable context word size */
    return sizeof(ContextAddress);
}

void ini_contexts(void) {
#if !ENABLE_ContextProxy
    int i;
    for (i = 0; i < CONTEXT_PID_HASH_SIZE; i++) {
        list_init(&context_pid_root[i]);
    }
    list_init(&context_root);
#endif
    init_contexts_sys_dep();
}

#endif  /* if ENABLE_DebugContext */
