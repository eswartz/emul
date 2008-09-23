/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
 * Implements input and output stream over TCP/IP transport and UDP based auto discovery.
 */

#include "mdep.h"
#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include <signal.h>
#include <ctype.h>
#include "tcf.h"
#include "channel.h"
#include "channel_tcp.h"
#include "myalloc.h"
#include "events.h"
#include "exceptions.h"
#include "trace.h"
#include "link.h"
#include "json.h"

#define BCAST_MAGIC 0x8463e328

#define out2bcast(A)    ((TCFBroadcastGroup *)((char *)(A) - offsetof(TCFBroadcastGroup, out)))
#define bclink2channel(A) ((Channel *)((char *)(A) - offsetof(Channel, bclink)))
#define susplink2channel(A) ((Channel *)((char *)(A) - offsetof(Channel, susplink)))

static ChannelCloseListener close_listeners[16];
static int close_listeners_cnt = 0;

static void flush_all(OutputStream * out) {
    TCFBroadcastGroup * bcg = out2bcast(out);
    LINK * l = bcg->channels.next;

    assert(is_dispatch_thread());
    assert(bcg->magic == BCAST_MAGIC);
    while (l != &bcg->channels) {
        Channel * c = bclink2channel(l);
        c->out.flush(&c->out);
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
        c->out.write(&c->out, byte);
        l = l->next;
    }
}

void channels_suspend(TCFSuspendGroup * p) {
    assert(is_dispatch_thread());
    trace(LOG_PROTOCOL, "All channels suspended");
    p->suspended = 1;
}

int are_channels_suspended(TCFSuspendGroup * p) {
    assert(is_dispatch_thread());
    return p->suspended;
}

void channels_resume(TCFSuspendGroup * p) {
    LINK * l = p->channels.next;

    assert(is_dispatch_thread());
    assert(p->suspended);
    trace(LOG_PROTOCOL, "All channels resumed");
    p->suspended = 0;
    while (l != &p->channels) {
        Channel * c = susplink2channel(l);
        c->check_pending(c);
        l = l->next;
    }
}

int channels_get_message_count(TCFSuspendGroup * p) {
    int cnt = 0;
    LINK * l = p->channels.next;

    assert(is_dispatch_thread());
    while (l != &p->channels) {
        Channel * c = susplink2channel(l);
        cnt += c->message_count(c);
        l = l->next;
    }
    return cnt;
}

void add_channel_close_listener(ChannelCloseListener listener) {
    assert(close_listeners_cnt < sizeof(close_listeners) / sizeof(ChannelCloseListener));
    close_listeners[close_listeners_cnt++] = listener;
}

void notify_channel_closed(Channel * c) {
    int i;
    for (i = 0; i < close_listeners_cnt; i++) {
        close_listeners[i](c);
    }
}

TCFSuspendGroup * suspend_group_alloc(void) {
    TCFSuspendGroup * p = loc_alloc(sizeof(TCFSuspendGroup));

    list_init(&p->channels);
    p->suspended = 0;
    return p;
}

void suspend_group_free(TCFSuspendGroup * p) {
    LINK * l = p->channels.next;

    assert(is_dispatch_thread());
    while (l != &p->channels) {
        Channel * c = susplink2channel(l);
        assert(c->spg == p);
        c->spg = NULL;
        list_remove(&c->susplink);
        c->check_pending(c);
        l = l->next;
    }
    assert(list_is_empty(&p->channels));
    loc_free(p);
}

void channel_set_suspend_group(Channel * c, TCFSuspendGroup * spg) {
    if (c->spg != NULL) channel_clear_suspend_group(c);
    list_add_last(&c->susplink, &spg->channels);
    c->spg = spg;
}

void channel_clear_suspend_group(Channel * c) {
    if (c->spg == NULL) return;
    list_remove(&c->susplink);
    c->spg = NULL;
}

TCFBroadcastGroup * broadcast_group_alloc(void) {
    TCFBroadcastGroup * p = loc_alloc(sizeof(TCFBroadcastGroup));

    list_init(&p->channels);
    p->magic = BCAST_MAGIC;
    p->out.write = write_all;
    p->out.flush = flush_all;
    return p;
}

void broadcast_group_free(TCFBroadcastGroup * p) {
    LINK * l = p->channels.next;

    assert(is_dispatch_thread());
    while (l != &p->channels) {
        Channel * c = bclink2channel(l);
        assert(c->bcg == p);
        c->bcg = NULL;
        list_remove(&c->bclink);
        l = l->next;
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

void stream_lock(Channel * c) {
    c->lock(c);
}

void stream_unlock(Channel * c) {
    c->unlock(c);
}

int is_stream_closed(Channel * c) {
    return c->is_closed(c);
}

PeerServer * channel_peer_from_url(const char * url) {
    int c;
    int i;
    const char * s;
    char * name;
    char * value;
    char transport[16];
    PeerServer * ps;

    ps = peer_server_alloc();
    peer_server_addprop(ps, loc_strdup("Name"), loc_strdup("TCF Agent"));
    peer_server_addprop(ps, loc_strdup("OSName"), loc_strdup(get_os_name()));

    s = url;
    i = 0;
    while ((c = *s) != '\0' && c != ':' && isalpha(c) && i < sizeof transport) {
        transport[i++] = islower(c) ? toupper(c) : c;
        s++;
    }
    if (c == ':' && i < sizeof transport) {
        s++;
        transport[i++] = '\0';
        if (strcmp(transport, "TCP") != 0) {
            /* Assume implicit "TCP:" */
            s = url;
            value = "TCP";
        }
        else {
            value = transport;
            url = s;
        }
        peer_server_addprop(ps, loc_strdup("TransportName"), loc_strdup(value));
    }
    while ((c = *s) != '\0' && c != ':' && c != ';') s++;
    if (s != url) {
        peer_server_addprop(ps, loc_strdup("Host"), loc_strndup(url, s - url));
    }
    if (c == ':') {
        s++;
        url = s;
        while ((c = *s) != '\0' && c != ';') s++;
        if (s != url) {
            peer_server_addprop(ps, loc_strdup("Port"), loc_strndup(url, s - url));
        }
    }

    while (c == ';') {
        s++;
        url = s;
        while ((c = *s) != '\0' && c != '=') s++;
        if (c != '=' || s == url) {
            s = url - 1;
            c = *s;
            break;
        }
        name = loc_strndup(url, s - url);
        s++;
        url = s;
        while ((c = *s) != '\0' && c != ';') s++;
        if (c != ';') {
            loc_free(name);
            s = url - 1;
            c = *s;
            break;
        }
        value = loc_strndup(url, s - url);
        peer_server_addprop(ps, name, value);
    }
    if (c != '\0') {
        peer_server_free(ps);
        return NULL;
    }
    return ps;
}

/*
 * Start TCF channel server
 */
ChannelServer * channel_server(PeerServer * ps) {
    char * transportname = peer_server_getprop(ps, "TransportName", "");

    if (strcmp(transportname, "TCP") == 0) {
        return channel_tcp_server(ps);
    }
    else {
        return NULL;
    }
}

/*
 * Connect to TCF channel server
 */
Channel * channel_connect(PeerServer * ps) {
    char * transportname = peer_server_getprop(ps, "TransportName", "");

    if (strcmp(transportname, "TCP") == 0) {
        return channel_tcp_connect(ps);
    }
    else {
        return NULL;
    }
}

/*
 * Start communication of a newly created channel
 */
void channel_start(Channel * c) {
    trace(LOG_PROTOCOL, "Starting channel 0x%08x %s", c, c->peer_name);
    c->start_comm(c);
}

/*
 * Close communication channel
 */
void channel_close(Channel *c) {
    trace(LOG_PROTOCOL, "Closing channel 0x%08x %s", c, c->peer_name);
    c->close(c, 0);
}
