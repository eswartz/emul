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
 *
 * Usage example.
 * This example assumes that Context represents execution context on remote target,
 * and context data is kept in a cache that is updated on demand by sending asynchronous
 * data requests to a remote peer.
 *
 * The example shows how to implement cache client, in this case TCF command handle,
 * that handle cache misses by waiting until the cache is updated and the re-executing
 * cache client code.

    //--- Remote data provider ---

    static AbstractCache cache;

    Context * id2ctx(char * id) {
        if (!cache_valid) {
            // Send data request.
            ...
            // Interrupt client execution.
            // Client will be restarted by calling cache_notify(),
            // when data retrieval is done.
            cache_wait(&cache);
        }

        // Search cached data and return.
        ...
        return ctx;
    }

    //--- Data consumer: command handler ---

    static void cache_client(void * x) {
        // Get cached data.
        // This code can be interrupted by cache misses,
        // and then re-executed again when the cache is updated.
        // Make sure the code is re-entrant.

        CommandArgs * args = (CommandArgs *)x;
        Channel * c = args->channel;
        Context ctx = id2ctx(args->id);
        int result = context_has_state(ctx);

        // Done retreiving cached data.

        cache_exit();

        // Rest of the code does not need to be re-entrant.

        // Send command result message:

        write_stringz(&c->out, "R");
        write_stringz(&c->out, args->token);
        json_write_boolean(&c->out, result);
        write_stream(&c->out, 0);
        write_stream(&c->out, MARKER_EOM);

        // Done command handling.

        // Cleanup:

        channel_unlock(c);
        loc_free(args);
    }

    static void command_handler(char * token, Channel * c) {
        // Read command arguments

        CommandArgs * args = loc_alloc_zero(sizeof(CommandArgs));
        json_read_string(&c->inp, args->id, sizeof(args->id));
        if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
        if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
        strncpy(args->token, token, sizeof(args->token) - 1);

        // Lock the channel until command handling is done:

        channel_lock(args->channel = c);

        // Start cache client state machine:

        cache_enter(cache_client, args);
    }

    add_command_handler(proto, "Service Name", "Command Name", command_handler);


 * Only main thread is allowed to accesses caches.
 */

#ifndef D_cache
#define D_cache

typedef void CacheClient(void *);

typedef struct WaitingCacheClient {
    CacheClient * client;
    void * args;
} WaitingCacheClient;

typedef struct AbstractCache {
    WaitingCacheClient * wait_list_buf;
    unsigned wait_list_cnt;
    unsigned wait_list_max;
    int posted;
} AbstractCache;

extern void cache_enter(CacheClient * client, void * args);
extern void cache_exit(void);
extern void cache_wait(AbstractCache * cache);
extern void cache_notify(AbstractCache * cache);

#endif /* D_cache */
