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
 * TCF Logger main module.
 *
 * TCF Logger is a simple TCF agent that des not provide any services itself,
 * instead it forward all TCF traffic to another agent.
 * Logger prints all messages it forwards.
 * It can be used as diagnostic and debugging tool.
 */

#include <config.h>

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <assert.h>
#include <framework/asyncreq.h>
#include <framework/events.h>
#include <framework/trace.h>
#include <framework/myalloc.h>
#include <framework/channel.h>
#include <framework/protocol.h>
#include <framework/proxy.h>
#include <framework/errors.h>
#include <services/discovery.h>

static const char * progname;
static const char * dest_url = "TCP::1534";

typedef struct ConnectInfo {
    PeerServer * ps;
    Channel * c1;
} ConnectInfo;

static void connect_done(void * args, int error, Channel * c2) {
    ConnectInfo * info = (ConnectInfo *)args;
    Channel * c1 = info->c1;

    if (!is_channel_closed(c1)) {
        assert(c1->state == ChannelStateRedirectReceived);
        if (error) {
            fprintf(stderr, "cannot connect to peer: %s\n", dest_url);
            channel_close(c1);
        }
        else {
            proxy_create(c1, c2);
        }
    }
    else if (!error) {
        channel_close(c2);
    }
    channel_unlock(c1);
    peer_server_free(info->ps);
    loc_free(info);
}

static void connect_dest(void * x) {
    Channel * c1 = (Channel *)x;
    PeerServer * ps = NULL;
    ConnectInfo * info = NULL;

    ps = channel_peer_from_url(dest_url);
    if (ps == NULL) {
        trace(LOG_ALWAYS, "cannot parse peer url: %s", dest_url);
        channel_close(c1);
        return;
    }
    channel_lock(c1);
    c1->state = ChannelStateRedirectReceived;
    info = (ConnectInfo *)loc_alloc_zero(sizeof(ConnectInfo));
    info->ps = ps;
    info->c1 = c1;
    channel_connect(ps, connect_done, info);
}

static void channel_server_connecting(Channel * c1) {
    trace(LOG_ALWAYS, "channel server connecting");

    assert(c1->state == ChannelStateStarted);
    c1->state = ChannelStateHelloSent;  /* Fake that we sent hello message. */
}

static void channel_server_connected(Channel * c1) {
    trace(LOG_ALWAYS, "channel server connected");

    assert(c1->state == ChannelStateConnected);

    /* Connect to destination on next dispatch since we are limited in
     * what we can do in a callback, e.g. cannot close channel. */
    post_event(connect_dest, c1);
}

static void channel_server_disconnected(Channel * c1) {
    trace(LOG_ALWAYS, "channel server disconnected");
    protocol_release(c1->protocol);
}

static void channel_new_connection(ChannelServer * serv, Channel * c) {
    c->protocol = protocol_alloc();
    c->connecting = channel_server_connecting;
    c->connected = channel_server_connected;
    c->disconnected = channel_server_disconnected;
    channel_start(c);
}

#if defined(_WRS_KERNEL)
int tcf_log(void) {
#else
int main(int argc, char ** argv) {
#endif
    int c;
    int ind;
    const char * log_name = "-";
    const char * url = "TCP:";
    PeerServer * ps;
    ChannelServer * serv;

    ini_mdep();
    ini_trace();
    ini_events_queue();
    ini_asyncreq();

    log_mode = LOG_TCFLOG;

#if defined(_WRS_KERNEL)

    progname = "tcf";
    open_log_file("-");

#else

    progname = argv[0];

    /* Parse arguments */
    for (ind = 1; ind < argc; ind++) {
        const char * s = argv[ind];
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
    if (ind < argc) {
        dest_url = argv[ind++];
    }

#endif

    ps = channel_peer_from_url(url);
    if (ps == NULL) {
        fprintf(stderr, "%s: invalid server URL (-s option value): %s\n", progname, url);
        exit(1);
    }
    peer_server_addprop(ps, loc_strdup("Name"), loc_strdup("TCF Protocol Logger"));
    peer_server_addprop(ps, loc_strdup("Proxy"), loc_strdup(""));
    serv = channel_server(ps);
    if (serv == NULL) {
        fprintf(stderr, "%s: cannot create TCF server: %s\n", progname, errno_to_str(errno));
        exit(1);
    }
    serv->new_conn = channel_new_connection;

    discovery_start();

    /* Process events - must run on the initial thread since ptrace()
     * returns ECHILD otherwise, thinking we are not the owner. */
    run_event_loop();
    return 0;
}
