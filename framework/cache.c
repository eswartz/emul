/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
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
 * Abstract asynchronous data cache support.
 */

#include "config.h"
#include <assert.h>
#include "errors.h"
#include "exceptions.h"
#include "myalloc.h"
#include "events.h"
#include "trace.h"
#include "cache.h"

static CacheClient * cache_client;
static void * client_args;

static WaitingCacheClient * wait_list_buf;
static unsigned wait_list_max;

static void cache_event(void * x) {
    AbstractCache * cache = (AbstractCache *)x;
    unsigned cnt = cache->wait_list_cnt;
    unsigned i;

    assert(cache->posted);
    cache->posted = 0;
    cache->wait_list_cnt = 0;
    if (wait_list_max < cnt) {
        wait_list_max = cnt;
        wait_list_buf = loc_realloc(wait_list_buf, cnt * sizeof(WaitingCacheClient));
    }
    memcpy(wait_list_buf, cache->wait_list_buf, cnt * sizeof(WaitingCacheClient));
    for (i = 0; i < cnt; i++) {
        cache_enter(wait_list_buf[i].client, wait_list_buf[i].args);
    }
}

extern void cache_enter(CacheClient * client, void * args) {
    Trap trap;

    assert(is_dispatch_thread());
    assert(cache_client == NULL);
    cache_client = client;
    client_args = args;
    if (set_trap(&trap)) {
        client(args);
        clear_trap(&trap);
    }
    else if (trap.error != ERR_CACHE_MISS || cache_client == NULL) {
        trace(LOG_ALWAYS, "Unhandled exception in data cache client: %d %s", trap.error, errno_to_str(trap.error));
    }
    cache_client = NULL;
    client_args = NULL;
}

extern void cache_exit(void) {
    assert(is_dispatch_thread());
    assert(cache_client != NULL);
    cache_client = NULL;
    client_args = NULL;
}

extern void cache_wait(AbstractCache * cache) {
    assert(is_dispatch_thread());
    if (cache_client != NULL) {
        if (cache->wait_list_cnt >= cache->wait_list_max) {
            cache->wait_list_max = cache->wait_list_max == 0 ? 8 : cache->wait_list_max + 8;
            cache->wait_list_buf = loc_realloc(cache->wait_list_buf, cache->wait_list_max * sizeof(WaitingCacheClient *));
        }
        cache->wait_list_buf[cache->wait_list_cnt].client = cache_client;
        cache->wait_list_buf[cache->wait_list_cnt].args = client_args;
        cache->wait_list_cnt++;
    }
    exception(ERR_CACHE_MISS);
}

extern void cache_notify(AbstractCache * cache) {
    assert(is_dispatch_thread());
    if (!cache->posted) {
        post_event(cache_event, cache);
        cache->posted = 1;
    }
}
