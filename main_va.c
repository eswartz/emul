/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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
 * TCF Value Add main module.
 *
 * This implementation of Value Add provides only few services,
 * but it supports Locator.redirect command, which can forward TCF traffic to other TCF agents.
 *
 * The code is intended to be an example of service proxing implementation.
 */

#include "config.h"

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <assert.h>
#include <signal.h>
#include "asyncreq.h"
#include "events.h"
#include "trace.h"
#include "myalloc.h"
#include "json.h"
#include "channel.h"
#include "protocol.h"
#include "proxy.h"
#include "discovery.h"
#include "errors.h"

static char * progname;
static Protocol * proto;
static ChannelServer * serv;
static TCFBroadcastGroup * bcg;
static TCFSuspendGroup * spg;

static void channel_server_connecting(Channel * c) {
    trace(LOG_PROTOCOL, "channel server connecting");

    send_hello_message(c->client_data, c);
    flush_stream(&c->out);
}

static void channel_server_connected(Channel * c) {
    int i;

    trace(LOG_PROTOCOL, "channel server connected, peer services:");
    for (i = 0; i < c->peer_service_cnt; i++) {
        trace(LOG_PROTOCOL, "  %s", c->peer_service_list[i]);
    }
}

static void channel_server_receive(Channel * c) {
    handle_protocol_message(c->client_data, c);
}

static void channel_server_disconnected(Channel * c) {
    trace(LOG_PROTOCOL, "channel server disconnected");
}

static void channel_new_connection(ChannelServer * serv, Channel * c) {
    protocol_reference(proto);
    c->client_data = proto;
    c->connecting = channel_server_connecting;
    c->connected = channel_server_connected;
    c->receive = channel_server_receive;
    c->disconnected = channel_server_disconnected;
    channel_set_suspend_group(c, spg);
    channel_set_broadcast_group(c, bcg);
    channel_start(c);
}

#if defined(_WRS_KERNEL)
int tcf_va(void) {
#else
int main(int argc, char ** argv) {
#endif
    int c;
    int ind;
    char * s;
    char * log_name = 0;
    char * url = "TCP:";
    PeerServer * ps;

#ifndef WIN32
    signal(SIGPIPE, SIG_IGN);
#endif
    ini_mdep();
    ini_trace();
    ini_asyncreq();
    ini_events_queue();

#if defined(_WRS_KERNEL)

    progname = "tcf";
    open_log_file("-");
    log_mode = 0;

#else

    progname = argv[0];

    /* Parse arguments */
    for (ind = 1; ind < argc; ind++) {
        s = argv[ind];
        if (*s != '-') {
            break;
        }
        s++;
        while ((c = *s++) != '\0') {
            switch (c) {
            case 'l':
            case 'L':
            case 's':
                if (*s == '\0') {
                    if (++ind >= argc) {
                        fprintf(stderr, "%s: error: no argument given to option '%c'\n", progname, c);
                        exit(1);
                    }
                    s = argv[ind];
                }
                switch (c) {
                case 'l':
                    log_mode = strtol(s, 0, 0);
                    break;

                case 'L':
                    log_name = s;
                    break;

                case 's':
                    url = s;
                    break;

                default:
                    fprintf(stderr, "%s: error: illegal option '%c'\n", progname, c);
                    exit(1);
                }
                s = "";
                break;

            default:
                fprintf(stderr, "%s: error: illegal option '%c'\n", progname, c);
                exit(1);
            }
        }
    }

    open_log_file(log_name);

#endif

    bcg = broadcast_group_alloc();
    spg = suspend_group_alloc();
    proto = protocol_alloc();
#if SERVICE_Locator
    ini_locator_service(proto, bcg);
#endif

    ps = channel_peer_from_url(url);
    if (ps == NULL) {
        fprintf(stderr, "invalid server URL (-s option value): %s\n", url);
        exit(1);
    }
    peer_server_addprop(ps, loc_strdup("Name"), loc_strdup("TCF Proxy"));
    peer_server_addprop(ps, loc_strdup("Proxy"), loc_strdup(""));
    serv = channel_server(ps);
    if (serv == NULL) {
        fprintf(stderr, "cannot create TCF server\n");
        exit(1);
    }
    serv->new_conn = channel_new_connection;

    discovery_start();

    /* Process events - must run on the initial thread since ptrace()
     * returns ECHILD otherwise, thinking we are not the owner. */
    run_event_loop();
    return 0;
}
