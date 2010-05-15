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

#include <config.h>

#include <stdio.h>
#include <framework/errors.h>
#include <main/server.h>

static Protocol * proto;
static TCFBroadcastGroup * bcg;

static void channel_new_connection(ChannelServer * serv, Channel * c) {
    protocol_reference(proto);
    c->protocol = proto;
    channel_set_broadcast_group(c, bcg);
    channel_start(c);
}

void ini_server(const char * url, Protocol * p, TCFBroadcastGroup * b) {
    ChannelServer * serv = NULL;
    PeerServer * ps = channel_peer_from_url(url);

    if (ps == NULL) {
        fprintf(stderr, "Invalid server URL (-s option value): %s\n", url);
        exit(1);
    }
    proto = p;
    bcg = b;
    serv = channel_server(ps);
    if (serv == NULL) {
        fprintf(stderr, "Cannot create TCF server: %s\n", errno_to_str(errno));
        exit(1);
    }
    serv->new_conn = channel_new_connection;
}
