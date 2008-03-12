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
 * This module implements tunneling of TCF messages to another target on behalf of a client
 * This service intended to be used when a client has no direct access to a target.
 */

#include <assert.h>
#include <string.h>
#include "proxy.h"
#include "protocol.h"
#include "trace.h"
#include "errors.h"
#include "exceptions.h"
#include "myalloc.h"
#include "discovery.h"

enum {
    ProxyStateInitial,
    ProxyStateConnecting,
    ProxyStateConnected,
    ProxyStateDisconnected
};

typedef struct Proxy Proxy;
struct Proxy {
    Channel * c;
    Protocol * proto;
    int other;
    int state;
};

static void proxy_connecting(Channel * c) {
    Proxy * proxy = c->client_data;

    assert(c == proxy->c);
    assert(proxy->state == ProxyStateInitial);
    proxy->state = ProxyStateConnecting;
    trace(LOG_ALWAYS, "proxy connecting");
    if (proxy[proxy->other].state == ProxyStateConnected) {
        send_hello_message(proxy->proto, c);
        c->out.flush(&c->out);
    }
}

static void proxy_connected(Channel * c) {
    Proxy * proxy = c->client_data;
    int i;

    assert(c == proxy->c);
    assert(proxy->state == ProxyStateConnecting);
    proxy->state = ProxyStateConnected;
    trace(LOG_ALWAYS, "proxy connected, peer services:");
    for (i = 0; i < c->peer_service_cnt; i++) {
        trace(LOG_ALWAYS, "  %s", c->peer_service_list[i]);
        /* Include service names in other protocol hello message */
        protocol_get_service(proxy[proxy->other].proto, c->peer_service_list[i]);
    }
    if (proxy[proxy->other].state == ProxyStateConnecting ||
        proxy[proxy->other].state == ProxyStateConnected) {
        send_hello_message(proxy[proxy->other].proto, proxy[proxy->other].c);
        c->out.flush(&proxy[proxy->other].c->out);
    }
}

static void proxy_receive(Channel * c) {
    Proxy * proxy = c->client_data;

    handle_protocol_message(proxy->proto, c);
}

static void proxy_disconnected(Channel * c) {
    Proxy * proxy = c->client_data;

    assert(c == proxy->c);
    assert(proxy->state == ProxyStateConnecting || proxy->state == ProxyStateConnected);
    proxy->state = ProxyStateDisconnected;
    trace(LOG_ALWAYS, "proxy disconnected");
    protocol_channel_closed(proxy->proto, c);
    if (proxy[proxy->other].state == ProxyStateDisconnected) {
        if (proxy->other == -1) proxy--;
        loc_free(proxy);
    }
    else {
        channel_close(proxy[proxy->other].c);
    }
}

static void proxy_default_message_handler(Channel * c, char **argv, int argc) {
    Proxy * proxy = c->client_data;
    Channel * otherc = proxy[proxy->other].c;
    int i = 0;

    assert(c == proxy->c);
    assert(argc > 0 && strlen(argv[0]) == 1);
    if (argv[0][0] == 'C') {
        write_stringz(&otherc->out, argv[0]);
        /* Prefix token with 'R'emote to distinguish from locally
         * generated commands */
        write_string(&otherc->out, "R");
        i = 1;
    }
    else if (argv[0][0] == 'R') {
        if (argv[1][0] != 'R') {
            trace(LOG_ALWAYS, "Reply with unexpected token: %s", argv[1]);
            exception(ERR_PROTOCOL);
        }
        argv[1]++;
    }
    while (i < argc) {
        write_stringz(&otherc->out, argv[i]);
        i++;
    }

    /* Copy body of message */
    do {
        i = c->inp.read(&c->inp);
        otherc->out.write(&otherc->out, i);
    } while (i != MARKER_EOM);
    otherc->out.flush(&otherc->out);
}

void proxy_create(Channel * c1, Channel * c2) {
    Proxy * proxy = loc_alloc_zero(2*sizeof *proxy);

    proxy[0].c = c1;
    proxy[0].proto = protocol_alloc();
    proxy[0].other = 1;
    proxy[0].state = ProxyStateConnecting;
    proxy[1].c = c2;
    proxy[1].proto = protocol_alloc();
    proxy[1].other = -1;
    proxy[1].state = ProxyStateInitial;

    discovery_channel_remove(c1);
    c1->connecting = proxy_connecting;
    c1->connected = proxy_connected;
    c1->receive = proxy_receive;
    c1->disconnected = proxy_disconnected;
    c1->client_data = proxy;
    set_default_message_handler(proxy[0].proto, proxy_default_message_handler);

    c2->connecting = proxy_connecting;
    c2->connected = proxy_connected;
    c2->receive = proxy_receive;
    c2->disconnected = proxy_disconnected;
    c2->client_data = proxy + 1;
    set_default_message_handler(proxy[1].proto, proxy_default_message_handler);
 }
