/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Implements peer server management.
 */

#if defined(_WRS_KERNEL)
#  include <vxWorks.h>
#endif
#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include "mdep.h"
#include "tcf.h"
#include "peer.h"
#include "myalloc.h"
#include "events.h"
#include "trace.h"

#define STALE_CHECK_TIME 20

typedef struct PeerServerList {
    PeerServer *root;
    int ind;
    int max;
    struct {
        peer_server_listener fnp;
        void *arg;
    } * list;
} PeerServerList;

static PeerServerList peer_server_list;
static int stale_timer_active = 0;

static void notify_listeners(PeerServerList * pi, PeerServer * ps, int changeType) {
    int i;

    trace(LOG_DISCOVERY, "peer server change, id=%s, type=%d", ps->id, changeType);
    for (i = 0; i < pi->ind; i++) {
        pi->list[i].fnp(ps, changeType, pi->list[i].arg);
    }
}

static int is_same(PeerServer *a, PeerServer *b) {
    int i;
    int j;

    if (a->ind != b->ind) {
        return 0;
    }
    for (i = 0; i < a->ind; i++) {
        for (j = 0; j < b->ind; j++) {
            if (strcmp(a->list[i].name, b->list[j].name) == 0) {
                break;
            }
        }
        if (j >= b->ind) {
            /* Name from a not found in b */
            return 0;
        }
        if (strcmp(a->list[i].value, b->list[j].value) != 0) {
            return 0;
        }
    }
    return 1;
}

static void clear_stale_peers(void *x) {
    PeerServerList *pi = &peer_server_list;
    PeerServer **sp = &pi->root;
    PeerServer *s;
    time_t timenow = time(NULL);
    int keep_timer = 0;

    assert(is_dispatch_thread());
    while ((s = *sp) != NULL) {
        if (s->create_time != s->stale_time && s->stale_time <= timenow) {
            /* Delete stale entry */
            *sp = s->next;
            notify_listeners(pi, s, -1);
            peer_server_free(s);
        }
        else {
            if (s->create_time != s->stale_time) {
                keep_timer = 1;
            }
            sp = &s->next;
        }
    }
    if (keep_timer) {
        post_event_with_delay(clear_stale_peers, NULL, STALE_CHECK_TIME*1000*1000);
    }
    else {
        stale_timer_active = 0;
    }
}

PeerServer * peer_server_alloc(void) {
    PeerServer * s = loc_alloc_zero(sizeof *s);

    s->max = 1;
    s->list = loc_alloc(s->max * sizeof *s->list);
    return s;
}

void peer_server_addprop(PeerServer * s, char * name, char * value) {
    int i;

    if (strcmp(name, "ID") == 0) {
        loc_free(name);
        s->id = value;
        return;
    }
    for (i = 0; i < s->ind; i++) {
        if (strcmp(s->list[i].name, name) == 0) {
            loc_free(name);
            loc_free(s->list[i].value);
            s->list[i].value = value;
            return;
        }
    }
    if (s->ind == s->max) {
        s->max *= 2;
        s->list = loc_realloc(s->list, s->max * sizeof *s->list);
    }
    s->list[s->ind].name = name;
    s->list[s->ind].value = value;
    s->ind++;
}

char *peer_server_getprop(PeerServer * s, char * name, char * default_value) {
    int i;

    for (i = 0; i < s->ind; i++) {
        if (strcmp(s->list[i].name, name) == 0) {
            return s->list[i].value;
        }
    }
    return default_value;
}

void peer_server_free(PeerServer * s) {
    while (s->ind > 0) {
        s->ind--;
        loc_free(s->list[s->ind].name);
        loc_free(s->list[s->ind].value);
    }
    loc_free(s->list);
    if (s->id) loc_free(s->id);
    loc_free(s);
}

PeerServer * peer_server_add(PeerServer * n, unsigned int stale_delta) {
    PeerServerList *pi = &peer_server_list;
    PeerServer ** sp = &pi->root;
    PeerServer * s;
    int type = 1;

    assert(is_dispatch_thread());
    while ((s = *sp) != NULL) {
        if (strcmp(s->id, n->id) == 0) {
            if (s->flags & PS_FLAG_LOCAL && !(n->flags & PS_FLAG_LOCAL)) {
                /* Never replace local entries with discovered ones */
                peer_server_free(n);
                return s;
            }
            if (is_same(s, n)) {
                s->create_time = time(NULL);
                s->stale_time = s->create_time + stale_delta;
                s->flags = n->flags;
                peer_server_free(n);
                return s;
            }
            *sp = s->next;
            peer_server_free(s);
            type = 0;
            break;
        }
        sp = &s->next;
    }
    n->create_time = time(NULL);
    n->stale_time = n->create_time + stale_delta;
    n->next = pi->root;
    pi->root = n;
    notify_listeners(pi, n, type);
    if (!stale_timer_active && stale_delta != 0) {
        stale_timer_active = 1;
        post_event_with_delay(clear_stale_peers, NULL, STALE_CHECK_TIME*1000*1000);
    }
    return n;
}

void peer_server_remove(const char *id) {
    PeerServerList *pi = &peer_server_list;
    PeerServer ** sp = &pi->root;
    PeerServer * s;

    assert(is_dispatch_thread());
    while ((s = *sp) != NULL) {
        if (strcmp(s->id, id) == 0) {
            *sp = s->next;
            notify_listeners(pi, s, -1);
            peer_server_free(s);
            break;
        }
        sp = &s->next;
    }
}

int peer_server_iter(peer_server_iter_fnp fnp, void * arg) {
    PeerServerList * pi = &peer_server_list;
    PeerServer * s = pi->root;
    int rval;

    assert(is_dispatch_thread());
    while (s != NULL) {
        rval = fnp(s, arg);
        if (rval != 0) {
            /* Abort iteration */
            return rval;
        }
        s = s->next;
    }
    return 0;
}

void peer_server_add_listener(peer_server_listener fnp, void * arg) {
    PeerServerList * pi = &peer_server_list;
    int i;

    for (i = 0; i < pi->ind; i++) {
        if (pi->list[i].fnp == fnp && pi->list[i].arg == arg) {
            /* Already in the list */
            return;
        }
    }
    if (pi->max == 0) {
        pi->max = 1;
        pi->list = loc_alloc(pi->max * sizeof *pi->list);
    }
    else if (pi->ind == pi->max) {
        pi->max *= 2;
        pi->list = loc_realloc(pi->list, pi->max * sizeof *pi->list);
    }
    pi->list[pi->ind].fnp = fnp;
    pi->list[pi->ind].arg = arg;
    pi->ind++;
}
