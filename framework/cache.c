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

typedef struct WaitingCacheClient {
    CacheClient * client;
    Channel * channel;
    void * args;
} WaitingCacheClient;

static WaitingCacheClient current_client = {0, 0, 0};
static WaitingCacheClient * wait_list_buf;
static unsigned wait_list_max;

void cache_enter(CacheClient * client, Channel * channel, void * args) {
    Trap trap;

    assert(is_dispatch_thread());
    assert(client != NULL);
    assert(channel != NULL);
    assert(current_client.client == NULL);
    current_client.client = client;
    current_client.channel = channel;
    current_client.args = args;
    if (set_trap(&trap)) {
        client(args);
        clear_trap(&trap);
    }
    else if (trap.error != ERR_CACHE_MISS || current_client.client == NULL) {
        trace(LOG_ALWAYS, "Unhandled exception in data cache client: %d %s", trap.error, errno_to_str(trap.error));
    }
    memset(&current_client, 0, sizeof(current_client));
}

void cache_exit(void) {
    assert(is_dispatch_thread());
    assert(current_client.client != NULL);
    memset(&current_client, 0, sizeof(current_client));
}

void cache_wait(AbstractCache * cache) {
    assert(is_dispatch_thread());
    if (current_client.client != NULL) {
        if (cache->wait_list_cnt >= cache->wait_list_max) {
            cache->wait_list_max = cache->wait_list_max == 0 ? 8 : cache->wait_list_max + 8;
            cache->wait_list_buf = loc_realloc(cache->wait_list_buf, cache->wait_list_max * sizeof(WaitingCacheClient));
        }
        cache->wait_list_buf[cache->wait_list_cnt++] = current_client;
        channel_lock(current_client.channel);
    }
    exception(ERR_CACHE_MISS);
}

void cache_notify(AbstractCache * cache) {
    unsigned i;
    unsigned cnt = cache->wait_list_cnt;

    assert(is_dispatch_thread());
    cache->wait_list_cnt = 0;
    if (wait_list_max < cnt) {
        wait_list_max = cnt;
        wait_list_buf = loc_realloc(wait_list_buf, cnt * sizeof(WaitingCacheClient));
    }
    memcpy(wait_list_buf, cache->wait_list_buf, cnt * sizeof(WaitingCacheClient));
    for (i = 0; i < cnt; i++) {
        cache_enter(wait_list_buf[i].client, wait_list_buf[i].channel, wait_list_buf[i].args);
        channel_unlock(wait_list_buf[i].channel);
    }
}

Channel * cache_channel(void) {
    return current_client.channel;
}

void cache_dispose(AbstractCache * cache) {
    assert(is_dispatch_thread());
    assert(cache->wait_list_cnt == 0);
    loc_free(cache->wait_list_buf);
    memset(cache, 0, sizeof(*cache));
}
