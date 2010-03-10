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
    size_t args_size;
    int args_copy;
} WaitingCacheClient;

static WaitingCacheClient current_client = {0, 0, 0, 0, 0};
static int client_exited = 0;
static int cache_miss_cnt = 0;
static WaitingCacheClient * wait_list_buf;
static unsigned wait_list_max;

static void run_cache_client(void) {
    Trap trap;
    OutputStream * out = &current_client.channel->bcg->out;

    cache_miss_cnt = 0;
    client_exited = 0;
    if (set_trap(&trap)) {
        current_client.client(current_client.args);
        clear_trap(&trap);
        assert(cache_miss_cnt == 0);
        assert(client_exited);
        flush_stream(out);
    }
    else if (get_error_code(trap.error) != ERR_CACHE_MISS || client_exited) {
        trace(LOG_ALWAYS, "Unhandled exception in data cache client: %d %s", trap.error, errno_to_str(trap.error));
    }
    if (cache_miss_cnt == 0 && current_client.args_copy) loc_free(current_client.args);
    memset(&current_client, 0, sizeof(current_client));
}

void cache_enter(CacheClient * client, Channel * channel, void * args, size_t args_size) {
    assert(is_dispatch_thread());
    assert(client != NULL);
    assert(channel != NULL);
    assert(current_client.client == NULL);
    current_client.client = client;
    current_client.channel = channel;
    current_client.args = args;
    current_client.args_size = args_size;
    current_client.args_copy = 0;
    run_cache_client();
}

void cache_exit(void) {
    assert(is_dispatch_thread());
    assert(current_client.client != NULL);
    assert(!client_exited);
    if (cache_miss_cnt > 0) exception(ERR_CACHE_MISS);
    client_exited = 1;
}

void cache_wait(AbstractCache * cache) {
    assert(is_dispatch_thread());
    if (current_client.client != NULL && cache_miss_cnt == 0) {
        if (cache->wait_list_cnt >= cache->wait_list_max) {
            cache->wait_list_max += 8;
            cache->wait_list_buf = (WaitingCacheClient *)loc_realloc(cache->wait_list_buf, cache->wait_list_max * sizeof(WaitingCacheClient));
        }
        if (current_client.args != NULL && !current_client.args_copy) {
            void * mem = loc_alloc(current_client.args_size);
            memcpy(mem, current_client.args, current_client.args_size);
            current_client.args = mem;
            current_client.args_copy = 1;
        }
        cache->wait_list_buf[cache->wait_list_cnt++] = current_client;
        channel_lock(current_client.channel);
    }
    cache_miss_cnt++;
    exception(ERR_CACHE_MISS);
}

void cache_notify(AbstractCache * cache) {
    unsigned i;
    unsigned cnt = cache->wait_list_cnt;

    assert(is_dispatch_thread());
    cache->wait_list_cnt = 0;
    if (wait_list_max < cnt) {
        wait_list_max = cnt;
        wait_list_buf = (WaitingCacheClient *)loc_realloc(wait_list_buf, cnt * sizeof(WaitingCacheClient));
    }
    memcpy(wait_list_buf, cache->wait_list_buf, cnt * sizeof(WaitingCacheClient));
    for (i = 0; i < cnt; i++) {
        current_client = wait_list_buf[i];
        run_cache_client();
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
