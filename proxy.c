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
 * This module implements tunneling of TCF messages to another target on behalf of a client
 * This service intended to be used when a client has no direct access to a target.
 */

#include "config.h"
#include <assert.h>
#include <string.h>
#include "proxy.h"
#include "protocol.h"
#include "trace.h"
#include "errors.h"
#include "exceptions.h"
#include "myalloc.h"

typedef struct Proxy {
    Channel * c;
    Protocol * proto;
    int other;
    int instance;
} Proxy;

static void proxy_connecting(Channel * c) {
    Proxy * target = c->client_data;

    assert(c == target->c);
    assert(target->other == -1);
    assert(c->state == ChannelStateStarted);
    assert((target + target->other)->c->state == ChannelStateHelloReceived);

    trace(LOG_PROXY, "Proxy waiting Hello from target");

    send_hello_message(target->proto, target->c);
    flush_stream(&target->c->out);
}

static void proxy_connected(Channel * c) {
    Proxy * target = c->client_data;
    Proxy * host = target + target->other;
    int i;

    assert(target->c == c);
    if (target->other == 1) {
        /* We get here after sending hello to host */
        return;
    }
    assert(c->state == ChannelStateConnected);
    assert(host->c->state == ChannelStateHelloReceived);

    trace(LOG_PROXY, "Proxy connected, target services:");
    for (i = 0; i < target->c->peer_service_cnt; i++) {
        trace(LOG_PROXY, "    %s", target->c->peer_service_list[i]);
        protocol_get_service(host->proto, target->c->peer_service_list[i]);
    }

    send_hello_message(host->proto, host->c);
    flush_stream(&host->c->out);
}

static void proxy_receive(Channel * c) {
    Proxy * proxy = c->client_data;

    handle_protocol_message(proxy->proto, c);
}

static void proxy_disconnected(Channel * c) {
    Proxy * proxy = c->client_data;

    assert(c == proxy->c);
    if (proxy[proxy->other].c->state == ChannelStateDisconnected) {
        trace(LOG_PROXY, "Proxy disconnected");
        if (proxy->other == -1) proxy--;
        assert(proxy[0].c->spg == proxy[1].c->spg);
        suspend_group_free(proxy[0].c->spg);
        proxy[0].c->spg = proxy[1].c->spg = NULL;
        proxy[0].c->client_data = proxy[1].c->client_data = NULL;
        protocol_release(proxy[0].proto);
        protocol_release(proxy[1].proto);
        stream_unlock(proxy[0].c);
        stream_unlock(proxy[1].c);
        loc_free(proxy);
    }
    else {
        channel_close(proxy[proxy->other].c);
    }
}

static char logbuf[1024];

static void logchr(char ** pp, int c) {
    char * p = *pp;

    if (p + 2 < logbuf + sizeof logbuf) *p++ = (char)c;
    *pp = p;
}

static void logstr(char ** pp, char * s) {
    char * p = *pp;
    int c;

    while ((c = *s++) != '\0') {
        if (p + 2 < logbuf + sizeof logbuf) *p++ = (char)c;
    }
    *pp = p;
}

static void proxy_default_message_handler(Channel * c, char ** argv, int argc) {
    Proxy * proxy = c->client_data;
    Channel * otherc = proxy[proxy->other].c;
    char * p;
    int i = 0;

    assert(c == proxy->c);
    assert(argc > 0 && strlen(argv[0]) == 1);
    if (proxy[proxy->other].c->state == ChannelStateDisconnected) return;
    if (argv[0][0] == 'C') {
        write_stringz(&otherc->out, argv[0]);
        /* Prefix token with 'R'emote to distinguish from locally
         * generated commands */
        write_string(&otherc->out, "R");
        i = 1;
    }
    else if (argv[0][0] == 'R' || argv[0][0] == 'P' || argv[0][0] == 'N') {
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
    TCFSuspendGroup * spg = suspend_group_alloc();
    TCFBroadcastGroup * bcg = broadcast_group_alloc();
    Proxy * proxy = loc_alloc_zero(2 * sizeof *proxy);
    int i;

    static int instance;

    assert(c1->state == ChannelStateRedirectReceived);
    assert(c2->state == ChannelStateStartWait);

    stream_lock(c1);
    proxy[0].c = c1;
    proxy[0].proto = protocol_alloc();
    proxy[0].other = 1;
    proxy[0].instance = instance;

    stream_lock(c2);
    proxy[1].c = c2;
    proxy[1].proto = protocol_alloc();
    proxy[1].other = -1;
    proxy[1].instance = instance++;

    trace(LOG_PROXY, "Proxy created, host services:");
    for (i = 0; i < c1->peer_service_cnt; i++) {
        trace(LOG_PROXY, "    %s", c1->peer_service_list[i]);
        protocol_get_service(proxy[1].proto, c1->peer_service_list[i]);
    }
    c1->state = ChannelStateHelloReceived;
    notify_channel_closed(c1);
    protocol_release(c1->client_data);
    c1->client_data = NULL;

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

    channel_set_suspend_group(c1, spg);
    channel_set_suspend_group(c2, spg);
    channel_set_broadcast_group(c1, bcg);
    channel_set_broadcast_group(c2, bcg);
    channel_start(c2);
 }
