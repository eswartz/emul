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
 * Server initialization code.
 */

#include "config.h"

#include <stdio.h>
#include "errors.h"
#include "myalloc.h"
#include "proxy.h"
#include "linenumbers.h"
#include "symbols.h"
#include "pathmap.h"
#include "context-proxy.h"
#include "server.h"

static Protocol * proto;
static TCFBroadcastGroup * bcg;

static void channel_new_connection(ChannelServer * serv, Channel * c) {
    protocol_reference(proto);
    c->protocol = proto;
    channel_set_broadcast_group(c, bcg);
    channel_start(c);
}

static void channel_redirection_listener(Channel * host, Channel * target) {
    if (target->state == ChannelStateStarted) {
        ini_line_numbers_service(target->protocol);
        ini_symbols_service(target->protocol);
    }
    if (target->state == ChannelStateConnected) {
        int i;
        int service_ln = 0;
        int service_mm = 0;
        int service_pm = 0;
        int service_sm = 0;
        for (i = 0; i < target->peer_service_cnt; i++) {
            char * nm = target->peer_service_list[i];
            if (strcmp(nm, "LineNumbers") == 0) service_ln = 1;
            if (strcmp(nm, "Symbols") == 0) service_sm = 1;
            if (strcmp(nm, "MemoryMap") == 0) service_mm = 1;
            if (strcmp(nm, "PathMap") == 0) service_pm = 1;
        }
        if (!service_pm) {
            ini_path_map_service(host->protocol);
        }
        if (service_mm) {
            if (!service_ln) ini_line_numbers_service(host->protocol);
            if (!service_sm) ini_symbols_service(host->protocol);
            create_context_proxy(host, target);
        }
    }
}

void ini_server(const char * url, Protocol * p, TCFBroadcastGroup * b) {
    ChannelServer * serv = NULL;
    PeerServer * ps = channel_peer_from_url(url);

    if (ps == NULL) {
        fprintf(stderr, "Invalid server URL (-s option value): %s\n", url);
        exit(1);
    }
    peer_server_addprop(ps, loc_strdup("Name"), loc_strdup("TCF Proxy"));
    peer_server_addprop(ps, loc_strdup("Proxy"), loc_strdup(""));
    proto = p;
    bcg = b;
    serv = channel_server(ps);
    if (serv == NULL) {
        fprintf(stderr, "Cannot create TCF server: %s\n", errno_to_str(errno));
        exit(1);
    }
    serv->new_conn = channel_new_connection;
    add_channel_redirection_listener(channel_redirection_listener);
}
