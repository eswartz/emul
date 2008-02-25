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
 * Agent main module.
 */

#define CONFIG_MAIN
#include "config.h"

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <assert.h>
#include <signal.h>
#include "mdep.h"
#include "events.h"
#include "trace.h"
#include "myalloc.h"
#include "expressions.h"
#include "cmdline.h"
#include "context.h"
#include "channel.h"
#include "protocol.h"
#include "discovery.h"

static char * progname;
static Protocol * proto;
static ChannelServer * serv;
static ChannelServer * serv2;
static TCFBroadcastGroup * bcg;
static TCFSuspendGroup * spg;

static void channel_server_connecting(Channel * c) {
    trace(LOG_ALWAYS, "channel server connecting");

    send_hello_message(c->client_data, c);
    discovery_channel_add(c);
    c->out.flush(&c->out);
}

static void channel_server_connected(Channel * c) {
    int i;

    trace(LOG_ALWAYS, "channel server connected, peer services:");
    for (i = 0; i < c->peer_service_cnt; i++) {
        trace(LOG_ALWAYS, "  %s", c->peer_service_list[i]);
    }
}

static void channel_server_receive(Channel * c) {
    handle_protocol_message(c->client_data, c);
}

static void channel_server_disconnected(Channel * c) {
    trace(LOG_ALWAYS, "channel server disconnected");
    discovery_channel_remove(c);
    protocol_channel_closed(c->client_data, c);
}

static ChannelCallbacks serverccb = {
    channel_server_connecting,
    channel_server_connected,
    channel_server_receive,
    channel_server_disconnected
};

static void channel_new_connection(ChannelServer * serv, Channel * c) {
    Protocol * proto = serv->client_data;

    c->client_data = proto;
    c->cb = &serverccb;
    c->spg = spg;
    c->bcg = bcg;
    protocol_channel_opened(proto, c);
}

static ChannelServerCallbacks servercb = {
    channel_new_connection
};

static void became_discovery_master(void) {
    PeerServer * ps = channel_peer_from_url(DEFAULT_DISCOVERY_URL);

    serv2 = channel_server(ps, &servercb, proto);
    if (serv2 == NULL) {
        trace(LOG_ALWAYS, "cannot create second TCF server\n");
    }
}

#if defined(_WRS_KERNEL)
int tcf(void) {
#else   
int main(int argc, char ** argv) {
#endif
    int c;
    int ind;
    int ismaster;
    int interactive = 0;
    char * s;
    char * log_name = 0;
    char * url = "TCP:";
    PeerServer * ps;

#ifndef WIN32
    signal(SIGPIPE, SIG_IGN);
#endif
    ini_mdep();
    ini_trace();
    ini_events_queue();
    ini_expression_library();

#if defined(_WRS_KERNEL)
    
    progname = "tcf";
    log_file = stdout;
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
            case 'i':
                interactive = 1;
                break;

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
    
    /* Create log file */
    if (log_name == 0) {
        log_file = NULL;
    }
    else if (strcmp(log_name, "-") == 0) {
        log_file = stderr;
    }
    else if ((log_file = fopen(log_name, "a")) == NULL) {
        fprintf(stderr, "%s: error: cannot create log file %s\n", progname, log_name);
        exit(1);
    }
    
#endif

    if (interactive) ini_cmdline_handler();

    bcg = broadcast_group_alloc();
    spg = suspend_group_alloc();
    proto = protocol_alloc();
    ini_services(proto, bcg, spg);
    ini_contexts();
    ismaster = discovery_start(became_discovery_master);

    ps = channel_peer_from_url(url);
    if (ps == NULL) {
        fprintf(stderr, "invalid server URL (-s option value): %s\n", url);
        exit(1);
    }
    if (ismaster) {
        if (!strcmp(peer_server_getprop(ps, "TransportName", ""), "TCP") &&
                peer_server_getprop(ps, "Port", NULL) == NULL) {
            peer_server_addprop(ps, loc_strdup("Port"), loc_strdup(DISCOVERY_TCF_PORT));
        }
        serv = channel_server(ps, &servercb, proto);
        // TODO: replace 'ps' with actual peer object created for the server
        if (strcmp(peer_server_getprop(ps, "TransportName", ""), "TCP") ||
                strcmp(peer_server_getprop(ps, "Port", ""), DISCOVERY_TCF_PORT)) {
            became_discovery_master();
        }
    }
    else {
        serv = channel_server(ps, &servercb, proto);
    }
    if (serv == NULL) {
        fprintf(stderr, "cannot create TCF server\n");
        exit(1);
    }

    /* Process events - must run on the initial thread since ptrace()
     * returns ECHILD otherwise, thinking we are not the owner. */
    run_event_loop();
    return 0;
}
