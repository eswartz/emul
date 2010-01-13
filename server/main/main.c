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
 * TCF Server main module.
 *
 * TCF Server is a value-add that provides StackTrace, Symbols, LineNumbers and Expressions services.
 */

#define CONFIG_MAIN
#include "config.h"

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <assert.h>
#include "asyncreq.h"
#include "events.h"
#include "trace.h"
#include "myalloc.h"
#include "errors.h"
#include "context-proxy.h"

static char * progname;
static Protocol * proto;
static ChannelServer * serv;
static TCFBroadcastGroup * bcg;
static TCFSuspendGroup * spg;

static void channel_redirected(Channel * host, Channel * target) {
    int i;
    int service_ln = 0;
    int service_mm = 0;
    for (i = 0; i < target->peer_service_cnt; i++) {
        protocol_get_service(host->protocol, target->peer_service_list[i]);
        if (strcmp(target->peer_service_list[i], "LineNumbers") == 0) service_ln = 1;
        if (strcmp(target->peer_service_list[i], "MemoryMap") == 0) service_mm = 1;
    }
    if (/*!service_ln &&*/ service_mm) {
        ini_line_numbers_service(host->protocol);
        create_context_proxy(host, target);
    }
}

static void channel_new_connection(ChannelServer * serv, Channel * c) {
    protocol_reference(proto);
    c->protocol = proto;
    c->redirected = channel_redirected;
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
    ini_services(proto, bcg, spg);

    ps = channel_peer_from_url(url);
    if (ps == NULL) {
        fprintf(stderr, "%s: invalid server URL (-s option value): %s\n", progname, url);
        exit(1);
    }
    peer_server_addprop(ps, loc_strdup("Name"), loc_strdup("TCF Proxy"));
    peer_server_addprop(ps, loc_strdup("Proxy"), loc_strdup(""));
    serv = channel_server(ps);
    if (serv == NULL) {
        fprintf(stderr, "%s: cannot create TCF server: %s\n", progname, errno_to_str(errno));
        exit(1);
    }
    serv->new_conn = channel_new_connection;

    discovery_start();

    run_event_loop();
    return 0;
}
