/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Implements discovery.
 */

#include "mdep.h"
#include "config.h"

#if ENABLE_Discovery

#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include "tcf.h"
#include "discovery_help.h"
#include "discovery.h"
#include "protocol.h"
#include "channel.h"
#include "myalloc.h"
#include "events.h"
#include "trace.h"
#include "exceptions.h"
#include "json.h"
#include "peer.h"

/*
 * Channel callback handlers
 */
static void channel_server_connecting(Channel *c) {
    trace(LOG_DISCOVERY, "discovery_help: channel_server_connecting");

    send_hello_message(c->client_data, c);
    discovery_channel_add(c);
    c->out.flush(&c->out);
}

static void channel_server_connected(Channel *c) {
    int i;

    trace(LOG_DISCOVERY, "discovery_help: channel_server_connected, peer services:");
    for (i = 0; i < c->peer_service_cnt; i++) {
        trace(LOG_DISCOVERY, "  %s", c->peer_service_list[i]);
    }
}

static void channel_server_receive(Channel *c) {
    handle_protocol_message(c->client_data, c);
}

static void channel_server_disconnected(Channel *c) {
    trace(LOG_DISCOVERY, "discovery_help: channel_server_disconnected");
    discovery_channel_remove(c);
    protocol_channel_closed(c->client_data, c);
}

/*
 * New incomming connection
 */
static void discovery_new_connection(ChannelServer * serv, Channel * c) {
    c->client_data = serv->client_data;
    c->connecting = channel_server_connecting;
    c->connected = channel_server_connected;
    c->receive = channel_server_receive;
    c->disconnected = channel_server_disconnected;
    channel_start(c);
    protocol_channel_opened(serv->client_data, c);
}

/*
 * Check if agent is already listening on discovery master port.
 */
static int check_master(PeerServer * ps, void * arg) {
    if (ps->flags & PS_FLAG_LOCAL) {
        char * transport_name = peer_server_getprop(ps, "TransportName", "");
        if (strcmp(transport_name, "TCP") == 0) {
            char * port = peer_server_getprop(ps, "Port", "0");
            if (atoi(port) == DISCOVERY_TCF_PORT) (*(int *)arg)++;
        }
    }
    return 0;
}

/*
 * Create a simple default discovery server if client did not provide one
 */
void create_default_discovery_master(void) {
    PeerServer * ps;
    Protocol * proto;
    ChannelServer * serv;
    int already_master = 0;

    peer_server_iter(check_master, &already_master);
    if (already_master) return;

    trace(LOG_DISCOVERY, "create_default_discovery_master");
    ps = channel_peer_from_url(DEFAULT_DISCOVERY_URL);
    if (ps == NULL) {
        trace(LOG_ALWAYS, "invalid discovery server URL");
        return;
    }
    peer_server_addprop(ps, loc_strdup("Name"), loc_strdup("TCF Discovery Master"));
    ps->flags |= PS_FLAG_PRIVATE;
    proto = protocol_alloc();
    ini_locator_service(proto);
    serv = channel_server(ps);
    if (serv == NULL) {
        trace(LOG_ALWAYS, "cannot create TCF discovery server");
        protocol_release(proto);
        peer_server_free(ps);
        return;
    }
    serv->new_conn = discovery_new_connection;
    serv->client_data = proto;
}

#endif
