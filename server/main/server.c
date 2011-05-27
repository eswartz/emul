/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
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
#include <framework/proxy.h>
#include <services/linenumbers.h>
#include <services/symbols.h>
#include <services/pathmap.h>
#include <services/context-proxy.h>
#include <main/server.h>

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
        int forward_pm = 0;
        for (i = 0; i < target->peer_service_cnt; i++) {
            char * nm = target->peer_service_list[i];
            if (strcmp(nm, "LineNumbers") == 0) service_ln = 1;
            if (strcmp(nm, "Symbols") == 0) service_sm = 1;
            if (strcmp(nm, "MemoryMap") == 0) service_mm = 1;
            if (strcmp(nm, "PathMap") == 0) service_pm = 1;
        }
        if (!service_pm || !service_ln || !service_sm) {
            ini_path_map_service(host->protocol);
            if (service_pm) forward_pm = 1;
        }
        if (service_mm) {
            if (!service_ln) ini_line_numbers_service(host->protocol);
            if (!service_sm) ini_symbols_service(host->protocol);
            create_context_proxy(host, target, forward_pm);
        }
    }
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
    peer_server_addprop(ps, loc_strdup("Name"), loc_strdup("TCF Proxy"));
    peer_server_addprop(ps, loc_strdup("Proxy"), loc_strdup(""));
    serv = channel_server(ps);
    if (serv == NULL) exception(errno);
    serv->new_conn = channel_new_connection;

    clear_trap(&trap);
    add_channel_redirection_listener(channel_redirection_listener);
    return 0;
}
