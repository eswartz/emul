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
 * Implements discovery.
 */

#if _WRS_KERNEL
#  include <vxWorks.h>
#endif
#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include "mdep.h"
#include "tcf.h"
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

static ChannelCallbacks serverccb = {
    channel_server_connecting,
    channel_server_connected,
    channel_server_receive,
    channel_server_disconnected
};

/*
 * New incomming connection
 */
static void discovery_new_connection(ChannelServer * serv, Channel * c) {
    c->client_data = serv->client_data;
    c->cb = &serverccb;
    protocol_channel_opened(serv->client_data, c);
}

static ChannelServerCallbacks servercb = {
    discovery_new_connection
};

/*
 * Create a simple default discovery server if client did not provide one
 */
void discovery_default_master_notifier(void) {
    PeerServer * ps;
    Protocol * proto;
    ChannelServer *serv;

    trace(LOG_DISCOVERY, "discovery_default_master_notifier");
    ps = channel_peer_from_url(DEFAULT_DISCOVERY_URL);
    if (ps == NULL) {
        trace(LOG_ALWAYS, "invalid discovery server URL\n");
        return;
    }
    ps->flags |= PS_FLAG_PRIVATE;
    proto = protocol_alloc();
    serv = channel_server(ps, &servercb, proto);
    if (serv == NULL) {
        trace(LOG_ALWAYS, "cannot create TCF discovery server\n");
        protocol_free(proto);
        return;
    }
}
