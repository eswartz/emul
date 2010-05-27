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
 * Transport agnostic channel implementation.
 */

/* TODO: Somehow we should make it clear what needs to be done to add another transport layer.
 * Perhaps have a template or a readme file for it. */

#include <config.h>
#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include <ctype.h>
#include <framework/tcf.h>
#include <framework/channel.h>
#include <framework/channel_tcp.h>
#include <framework/myalloc.h>
#include <framework/events.h>
#include <framework/exceptions.h>
#include <framework/trace.h>
#include <framework/link.h>
#include <framework/json.h>

#define BCAST_MAGIC 0x1463e328

#define out2bcast(A)    ((TCFBroadcastGroup *)((char *)(A) - offsetof(TCFBroadcastGroup, out)))
#define bclink2channel(A) ((Channel *)((char *)(A) - offsetof(Channel, bclink)))
#define susplink2channel(A) ((Channel *)((char *)(A) - offsetof(Channel, susplink)))

static ChannelCloseListener close_listeners[16];
static int close_listeners_cnt = 0;

#define isBoardcastOkay(c) ((c)->state == ChannelStateConnected || \
                            (c)->state == ChannelStateRedirectSent || \
                            (c)->state == ChannelStateRedirectReceived)

static void flush_all(OutputStream * out) {
    TCFBroadcastGroup * bcg = out2bcast(out);
    LINK * l = bcg->channels.next;

    assert(is_dispatch_thread());
    assert(bcg->magic == BCAST_MAGIC);
    while (l != &bcg->channels) {
        Channel * c = bclink2channel(l);
        if (isBoardcastOkay(c)) flush_stream(&c->out);
        l = l->next;
    }
}

static void write_all(OutputStream * out, int byte) {
    TCFBroadcastGroup * bcg = out2bcast(out);
    LINK * l = bcg->channels.next;

    assert(is_dispatch_thread());
    assert(bcg->magic == BCAST_MAGIC);
    while (l != &bcg->channels) {
        Channel * c = bclink2channel(l);
        if (isBoardcastOkay(c)) c->out.write(&c->out, byte);
        l = l->next;
    }
}

static void write_block_all(OutputStream * out, const char * bytes, size_t size) {
    TCFBroadcastGroup * bcg = out2bcast(out);
    LINK * l = bcg->channels.next;

    assert(is_dispatch_thread());
    assert(bcg->magic == BCAST_MAGIC);
    while (l != &bcg->channels) {
        Channel * c = bclink2channel(l);
        if (isBoardcastOkay(c)) c->out.write_block(&c->out, bytes, size);
        l = l->next;
    }
}

static int splice_block_all(OutputStream * out, int fd, size_t size, off_t * offset) {
    char buffer[0x400];
    int rd = 0;

    assert(is_dispatch_thread());
    if (size > sizeof(buffer)) size = sizeof(buffer);
    if (offset != NULL) {
        rd = pread(fd, buffer, size, *offset);
        if (rd > 0) *offset += rd;
    }
    else {
        rd = read(fd, buffer, size);
    }
    if (rd > 0) write_block_all(out, buffer, rd);
    return rd;
}

void add_channel_close_listener(ChannelCloseListener listener) {
    assert(close_listeners_cnt < (int)(sizeof(close_listeners) / sizeof(ChannelCloseListener)));
    close_listeners[close_listeners_cnt++] = listener;
}

void notify_channel_closed(Channel * c) {
    int i;
    for (i = 0; i < close_listeners_cnt; i++) {
        close_listeners[i](c);
    }
}

TCFBroadcastGroup * broadcast_group_alloc(void) {
    TCFBroadcastGroup * p = (TCFBroadcastGroup*)loc_alloc_zero(sizeof(TCFBroadcastGroup));

    list_init(&p->channels);
    p->magic = BCAST_MAGIC;
    p->out.write = write_all;
    p->out.flush = flush_all;
    p->out.write_block = write_block_all;
    p->out.splice_block = splice_block_all;
    return p;
}

void broadcast_group_free(TCFBroadcastGroup * p) {
    LINK * l = p->channels.next;

    assert(is_dispatch_thread());
    while (l != &p->channels) {
        Channel * c = bclink2channel(l);
        assert(c->bcg == p);
        l = l->next;
        c->bcg = NULL;
        list_remove(&c->bclink);
    }
    assert(list_is_empty(&p->channels));
    p->magic = 0;
    loc_free(p);
}

void channel_set_broadcast_group(Channel * c, TCFBroadcastGroup * bcg) {
    if (c->bcg != NULL) channel_clear_broadcast_group(c);
    list_add_last(&c->bclink, &bcg->channels);
    c->bcg = bcg;
}

void channel_clear_broadcast_group(Channel * c) {
    if (c->bcg == NULL) return;
    list_remove(&c->bclink);
    c->bcg = NULL;
}

void channel_lock(Channel * c) {
    c->lock(c);
}

void channel_unlock(Channel * c) {
    c->unlock(c);
}

int is_channel_closed(Channel * c) {
    return c->is_closed(c);
}

PeerServer * channel_peer_from_url(const char * url) {
    int i;
    const char * s;
    char transport[16];
    PeerServer * ps = peer_server_alloc();

    peer_server_addprop(ps, loc_strdup("Name"), loc_strdup("TCF Agent"));
    peer_server_addprop(ps, loc_strdup("OSName"), loc_strdup(get_os_name()));

    s = url;
    i = 0;
    while (*s && isalpha(*s) && i < (int)sizeof transport) transport[i++] = (char)toupper(*s++);
    if (*s == ':' && i < (int)sizeof transport) {
        s++;
        peer_server_addprop(ps, loc_strdup("TransportName"), loc_strndup(transport, i));
        url = s;
    }
    else {
        s = url;
    }
    while (*s && *s != ':' && *s != ';') s++;
    if (s != url) peer_server_addprop(ps, loc_strdup("Host"), loc_strndup(url, s - url));
    if (*s == ':') {
        s++;
        url = s;
        while (*s && *s != ';') s++;
        if (s != url) peer_server_addprop(ps, loc_strdup("Port"), loc_strndup(url, s - url));
    }

    while (*s == ';') {
        char * name;
        char * value;
        s++;
        url = s;
        while (*s && *s != '=') s++;
        if (*s != '=' || s == url) {
            s = url - 1;
            break;
        }
        name = loc_strndup(url, s - url);
        s++;
        url = s;
        while (*s && *s != ';') s++;
        value = loc_strndup(url, s - url);
        peer_server_addprop(ps, name, value);
    }
    if (*s != '\0') {
        peer_server_free(ps);
        return NULL;
    }
    return ps;
}

/*
 * Start TCF channel server
 */
ChannelServer * channel_server(PeerServer * ps) {
    const char * transportname = peer_server_getprop(ps, "TransportName", NULL);

    if (transportname == NULL || strcmp(transportname, "TCP") == 0 || strcmp(transportname, "SSL") == 0) {
        return channel_tcp_server(ps);
    }
    else {
        errno = ERR_INV_TRANSPORT;
        return NULL;
    }
}

/*
 * Connect to TCF channel server
 */
void channel_connect(PeerServer * ps, ChannelConnectCallBack callback, void * callback_args) {
    const char * transportname = peer_server_getprop(ps, "TransportName", NULL);

    if (transportname == NULL || strcmp(transportname, "TCP") == 0 || strcmp(transportname, "SSL") == 0) {
        channel_tcp_connect(ps, callback, callback_args);
    }
    else {
        callback(callback_args, ERR_INV_TRANSPORT, NULL);
    }
}

/*
 * Start communication of a newly created channel
 */
void channel_start(Channel * c) {
    trace(LOG_PROTOCOL, "Starting channel %#lx %s", c, c->peer_name);
    assert(c->protocol != NULL);
    assert(c->state == ChannelStateStartWait);
    c->state = ChannelStateStarted;
    c->start_comm(c);
}

/*
 * Close communication channel
 */
void channel_close(Channel * c) {
    trace(LOG_PROTOCOL, "Closing channel %#lx %s", c, c->peer_name);
    c->close(c, 0);
}
