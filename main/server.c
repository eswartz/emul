/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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

#include <config.h>

#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <main/server.h>

static Protocol * proto;
static TCFBroadcastGroup * bcg;

static void channel_new_connection(ChannelServer * serv, Channel * c) {
    protocol_reference(proto);
    c->protocol = proto;
    channel_set_broadcast_group(c, bcg);
    channel_start(c);
}

int ini_server(const char * url, Protocol * p, TCFBroadcastGroup * b) {
    ChannelServer * serv = NULL;
    PeerServer * ps = NULL;
    Trap trap;

    if (!set_trap(&trap)) {
        bcg = NULL;
        proto = NULL;
        if (ps != NULL) peer_server_free(ps);
        errno = trap.error;
        return -1;
    }

    bcg = b;
    proto = p;
    ps = channel_peer_from_url(url);
    if (ps == NULL) str_exception(ERR_OTHER, "Invalid server URL");
    peer_server_addprop(ps, loc_strdup("ServerManagerID"), loc_strdup(get_service_manager_id(p)));
    serv = channel_server(ps);
    if (serv == NULL) exception(errno);
    serv->new_conn = channel_new_connection;

    clear_trap(&trap);
    return 0;
}
