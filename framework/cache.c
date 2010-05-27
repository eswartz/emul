/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
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

#include <config.h>
#include <assert.h>
#include <framework/errors.h>
#include <framework/exceptions.h>
#include <framework/myalloc.h>
#include <framework/events.h>
#include <framework/trace.h>
#include <framework/cache.h>

typedef struct WaitingCacheClient {
    CacheClient * client;
    Channel * channel;
    void * args;
    size_t args_size;
    int args_copy;
#ifndef NDEBUG
    const char * file;
    int line;
#endif
} WaitingCacheClient;

static WaitingCacheClient current_client = {0, 0, 0, 0, 0};
static int client_exited = 0;
static int cache_miss_cnt = 0;
static WaitingCacheClient * wait_list_buf;
static unsigned wait_list_max;
static LINK cache_list;

static void run_cache_client(void) {
    Trap trap;

    cache_miss_cnt = 0;
    client_exited = 0;
    if (set_trap(&trap)) {
        current_client.client(current_client.args);
        clear_trap(&trap);
        assert(cache_miss_cnt == 0);
        assert(client_exited);
    }
    else if (get_error_code(trap.error) != ERR_CACHE_MISS || client_exited || cache_miss_cnt == 0) {
        trace(LOG_ALWAYS, "Unhandled exception in data cache client: %d %s", trap.error, errno_to_str(trap.error));
    }
    if (cache_miss_cnt == 0 && current_client.args_copy) loc_free(current_client.args);
    memset(&current_client, 0, sizeof(current_client));
    cache_miss_cnt = 0;
    client_exited = 0;
}

void cache_enter(CacheClient * client, Channel * channel, void * args, size_t args_size) {
    assert(is_dispatch_thread());
    assert(client != NULL);
    assert(channel != NULL);
    assert(!is_channel_closed(channel));
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

#ifdef NDEBUG
void cache_wait(AbstractCache * cache) {
#else
void cache_wait_dbg(const char * file, int line, AbstractCache * cache) {
#endif
    assert(is_dispatch_thread());
    assert(client_exited == 0);
    if (current_client.client != NULL && cache_miss_cnt == 0) {
        if (cache_list.next == NULL) list_init(&cache_list);
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
#ifndef NDEBUG
        current_client.file = file;
        current_client.line = line;
#endif
        if (cache->wait_list_cnt == 0) list_add_last(&cache->link, &cache_list);
        cache->wait_list_buf[cache->wait_list_cnt++] = current_client;
        channel_lock(current_client.channel);
    }
#ifndef NDEBUG
    else if (current_client.client == NULL) {
        trace(LOG_ALWAYS, "cache_wait(): illegal cache access at %s:%d", file, line);
    }
#endif
    cache_miss_cnt++;
    exception(ERR_CACHE_MISS);
}

void cache_notify(AbstractCache * cache) {
    unsigned i;
    unsigned cnt = cache->wait_list_cnt;

    assert(is_dispatch_thread());
    list_remove(&cache->link);
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
    assert(list_is_empty(&cache->link));
    loc_free(cache->wait_list_buf);
    memset(cache, 0, sizeof(*cache));
}
