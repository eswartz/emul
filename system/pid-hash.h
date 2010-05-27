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
 * This is utility module that contains context PID hash table implementation.
 * System specific debug context code can use this module to implement context lookup.
 */

/* TODO: consider splitting the code into two files: some clients might want to reuse PID hash table for context_find_from_pid(),
 * but use some other means to implement id2ctx() */

#define CONTEXT_PID_HASH_SIZE 1024
#define CONTEXT_PID_HASH(PID) ((unsigned)(PID) % CONTEXT_PID_HASH_SIZE)

static LINK context_pid_hash[CONTEXT_PID_HASH_SIZE];
static size_t pid_hash_link_offset = 0;

#define ctx2pidlink(ctx) ((LINK *)((char *)(ctx) + pid_hash_link_offset))
#define pidlink2ctx(lnk) ((Context *)((char *)(lnk) - pid_hash_link_offset))

static void link_context(Context * ctx) {
    LINK * h = context_pid_hash + CONTEXT_PID_HASH(EXT(ctx)->pid);

    assert(ctx->mem != NULL);
    assert(EXT(ctx)->pid != 0);
    assert(context_find_from_pid(EXT(ctx)->pid, ctx->parent != NULL) == NULL);
    list_add_first(&ctx->ctxl, &context_root);
    list_add_first(ctx2pidlink(ctx), h);
    ctx->ref_count++;
}

Context * context_find_from_pid(pid_t pid, int thread) {
    LINK * h = context_pid_hash + CONTEXT_PID_HASH(pid);
    LINK * l = h->next;

    assert(is_dispatch_thread());
    if (l == NULL) return NULL;
    while (l != h) {
        Context * ctx = pidlink2ctx(l);
        if (EXT(ctx)->pid == pid &&
            (ctx->parent != NULL) == (thread != 0)) return ctx;
        l = l->next;
    }
    return NULL;
}

Context * id2ctx(const char * id) {
    pid_t parent = 0;
    pid_t pid = id2pid(id, &parent);
    if (pid == 0) return NULL;
    return context_find_from_pid(pid, parent != 0);
}

static void pid_hash_context_exited(Context * ctx, void * args) {
    list_remove(ctx2pidlink(ctx));
}

static void ini_context_pid_hash(void) {
    int i;
    static ContextEventListener l = { NULL, pid_hash_context_exited };
    for (i = 0; i < CONTEXT_PID_HASH_SIZE; i++) list_init(context_pid_hash + i);
    pid_hash_link_offset = context_extension(sizeof(LINK));
    add_context_event_listener(&l, NULL);
}
