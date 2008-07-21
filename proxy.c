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

#include "mdep.h"
#include "config.h"
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
    int instance;
};

static void proxy_connecting(Channel * c) {
    Proxy * proxy = c->client_data;

    assert(c == proxy->c);
    assert(proxy->state == ProxyStateInitial);
    proxy->state = ProxyStateConnecting;
    trace(LOG_PROXY, "proxy connecting");
    if (proxy[proxy->other].state == ProxyStateConnected) {
        send_hello_message(proxy->proto, c);
        flush_stream(&c->out);
    }
}

static void proxy_connected(Channel * c) {
    Proxy * proxy = c->client_data;
    int i;

    assert(c == proxy->c);
    assert(proxy->state == ProxyStateConnecting);
    proxy->state = ProxyStateConnected;
    trace(LOG_PROXY, "proxy connected, peer services:");
    for (i = 0; i < c->peer_service_cnt; i++) {
        trace(LOG_PROXY, "  %s", c->peer_service_list[i]);
        /* Include service names in other protocol hello message */
        protocol_get_service(proxy[proxy->other].proto, c->peer_service_list[i]);
    }
    if (proxy[proxy->other].state == ProxyStateConnecting ||
        proxy[proxy->other].state == ProxyStateConnected) {
        send_hello_message(proxy[proxy->other].proto, proxy[proxy->other].c);
        flush_stream(&proxy[proxy->other].c->out);
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
    trace(LOG_PROXY, "proxy disconnected");
    protocol_channel_closed(proxy->proto, c);
    if (proxy[proxy->other].state == ProxyStateDisconnected) {
        if (proxy->other == -1) proxy--;
        loc_free(proxy);
    }
    else {
        channel_close(proxy[proxy->other].c);
    }
}

static char logbuf[1024];

static void logchr(char ** pp, int c) {
    char * p = *pp;

    if (p + 2 < logbuf+sizeof logbuf) {
        *p++ = c;
    }
    *pp = p;
}

static void logstr(char ** pp, char * s) {
    char * p = *pp;
    int c;

    while ((c = *s++) != '\0') {
        if (p + 2 < logbuf+sizeof logbuf) {
            *p++ = c;
        }
    }
    *pp = p;
}

static void proxy_default_message_handler(Channel * c, char **argv, int argc) {
    Proxy * proxy = c->client_data;
    Channel * otherc = proxy[proxy->other].c;
    char * p;
    int i = 0;

    assert(c == proxy->c);
    assert(argc > 0 && strlen(argv[0]) == 1);
    if (proxy[proxy->other].state == ProxyStateDisconnected) return;
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

    p = logbuf;
    if (log_mode & LOG_TCFLOG) {
        logstr(&p, proxy->other > 0 ? "---> " : "<--- ");
        for (i = 0; i < argc; i++) {
            logstr(&p, argv[i]);
            logchr(&p, ' ');
        }
    }

    /* Copy body of message */
    do {
        i = read_stream(&c->inp);
        if (log_mode & LOG_TCFLOG) {
            if (i > ' ' && i < 127) {
                /* Printable ASCII  */
                logchr(&p, i);
            }
            else if (i == 0) {
                logstr(&p, " ");
            }
            else if (i > 0) {
                char buf[40];
                snprintf(buf, sizeof buf, "\\x%02x", i);
                logstr(&p, buf);
            }
            else if (i == MARKER_EOM) {
                logstr(&p, "<eom>");
            }
            else if (i == MARKER_EOS) {
                logstr(&p, "<eom>");
            }
            else {
                logstr(&p, "<?>");
            }
        }
        write_stream(&otherc->out, i);
    }
    while (i != MARKER_EOM && i != MARKER_EOS);
    flush_stream(&otherc->out);
    if (log_mode & LOG_TCFLOG) {
        *p = '\0';
        trace(LOG_TCFLOG, "%d: %s", proxy->instance, logbuf);
    }
}

void proxy_create(Channel * c1, Channel * c2) {
    Proxy * proxy = loc_alloc_zero(2*sizeof *proxy);
    static int instance;

    proxy[0].c = c1;
    proxy[0].proto = protocol_alloc();
    proxy[0].other = 1;
    proxy[0].state = ProxyStateConnecting;
    proxy[0].instance = instance;
    proxy[1].c = c2;
    proxy[1].proto = protocol_alloc();
    proxy[1].other = -1;
    proxy[1].state = ProxyStateInitial;
    proxy[1].instance = instance++;

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
