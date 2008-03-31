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
    trace(LOG_DISCOVERY, "discovery_help: channel_server_disconnected\n");
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
 * Create a simple default discovery server if client did not provide one
 */
void discovery_default_master_notifier(void) {
    PeerServer * ps;
    Protocol * proto;
    ChannelServer * serv;

    trace(LOG_DISCOVERY, "discovery_default_master_notifier");
    ps = channel_peer_from_url(DEFAULT_DISCOVERY_URL);
    if (ps == NULL) {
        trace(LOG_ALWAYS, "invalid discovery server URL\n");
        return;
    }
    ps->flags |= PS_FLAG_PRIVATE;
    proto = protocol_alloc();
    ini_locator_service(proto);
    serv = channel_server(ps);
    if (serv == NULL) {
        trace(LOG_ALWAYS, "cannot create TCF discovery server\n");
        protocol_release(proto);
        return;
    }
    serv->new_conn = discovery_new_connection;
    serv->client_data = proto;
}
