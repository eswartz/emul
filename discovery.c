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
 * Implements discovery.
 */

#include "mdep.h"
#include "config.h"

#if ENABLE_Discovery

#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include "tcf.h"
#include "discovery.h"
#include "discovery_udp.h"
#include "protocol.h"
#include "channel.h"
#include "myalloc.h"
#include "events.h"
#include "trace.h"
#include "exceptions.h"
#include "json.h"
#include "peer.h"

static const char * LOCATOR = "Locator";

#define REFRESH_TIME            10
#define STALE_TIME_DELTA        (REFRESH_TIME*3)

static int chan_max;
static int chan_ind;
static Channel ** chan_list;
static Channel * client_chan;
static int discovery_ismaster;
static int publish_peer_refresh_active;
static DiscoveryMasterNotificationCB master_notifier;
static void restart_discovery(void *);
static void generate_publish_peer_command(Channel * c, PeerServer * ps);

static void generate_peer_info(PeerServer * ps, OutputStream * out) {
    int i;

    write_stream(out, '{');
    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, ps->id);
    for (i = 0; i < ps->ind; i++) {
        write_stream(out, ',');
        json_write_string(out, ps->list[i].name);
        write_stream(out, ':');
        json_write_string(out, ps->list[i].value);
    }
    write_stream(out, '}');
}

static void remote_peer_change(PeerServer * ps, int changeType, OutputStream * out) {
    if ((ps->flags & PS_FLAG_DISCOVERABLE) == 0) return;
    trace(LOG_DISCOVERY, "discovery: remote_peer_change, id %s, type %d", ps->id, changeType);
    write_stringz(out, "E");
    write_stringz(out, LOCATOR);
    if (changeType >= 0) {
        if (changeType > 0) {
            write_stringz(out, "peerAdded");
        }
        else {
            write_stringz(out, "peerChanged");
        }
        generate_peer_info(ps, out);
    }
    else {
        write_stringz(out, "peerRemoved");
        json_write_string(out, ps->id);
    }
    write_stream(out, 0);
    write_stream(out, MARKER_EOM);
    flush_stream(out);
}

static int generate_peer_added_event(PeerServer * ps, void * x) {
    OutputStream * out = x;

    remote_peer_change(ps, 1, out);
    return 0;
}

static int republish_one_peer(PeerServer * ps, void * x) {
    generate_publish_peer_command(client_chan, ps);
    return 0;
}

static void republish_all_peers(void *x) {
    publish_peer_refresh_active = 0;
    if (client_chan != NULL) {
        peer_server_iter(republish_one_peer, NULL);
    }
}

static void publish_peer_error(InputStream * inp, char * name, void * x) {
    int * error = (int *)x;
    if (strcmp(name, "Code") == 0) {
        *error = json_read_long(inp);
    }
    else {
        loc_free(json_skip_object(inp));
    }
}

static void publish_peer_reply(Channel * c, void * client_data, int error) {
    time_t refresh_time;

    trace(LOG_DISCOVERY, "discovery: publish peer reply");
    if (error) {
        trace(LOG_DISCOVERY, "  error %d", error);
        return;
    }
    if (peek_stream(&c->inp) != 0) {
        json_read_struct(&c->inp, publish_peer_error, &error);
        if (error) trace(LOG_DISCOVERY, "  error %d", error);
    }
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    refresh_time = json_read_ulong(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    trace(LOG_DISCOVERY, "  refresh_time %d", refresh_time);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);
    if (publish_peer_refresh_active == 0) {
        publish_peer_refresh_active = 1;
        post_event_with_delay(republish_all_peers, NULL, (unsigned long)refresh_time*1000*1000);
    }
}

static void generate_publish_peer_command(Channel * c, PeerServer * ps) {
    if ((ps->flags & (PS_FLAG_LOCAL | PS_FLAG_PRIVATE | PS_FLAG_DISCOVERABLE)) != 
        (PS_FLAG_LOCAL | PS_FLAG_DISCOVERABLE)) {
        return;
    }
    trace(LOG_DISCOVERY, "discovery: publish peer command, id %s", ps->id);
    protocol_send_command(c->client_data, c, LOCATOR, "publishPeer", publish_peer_reply, NULL);
    generate_peer_info(ps, &c->out);
    write_stream(&c->out, 0);
    write_stream(&c->out, MARKER_EOM);
    flush_stream(&c->out);
}

/*
 * Add channel to include in discovery updates
 */
void discovery_channel_add(Channel * c) {
    if (chan_ind == chan_max) {
        if (chan_list == NULL) {
            chan_max = 1;
            chan_list = loc_alloc(chan_max * sizeof *chan_list);
        }
        else {
            chan_max *= 2;
            chan_list = loc_realloc(chan_list, chan_max * sizeof *chan_list);
        }
        chan_list[chan_ind++] = c;
    }
    peer_server_iter(generate_peer_added_event, &c->out);
}

/*
 * Remove channel from discovery updates
 */
void discovery_channel_remove(Channel * c) {
    int i;

    for (i = 0; i < chan_ind; i++) {
        if (chan_list[i] == c) break;
    }
    if (i < chan_ind) {
        chan_ind--;
        for (; i < chan_ind; i++) {
            chan_list[i] = chan_list[i+1];
        }
    }
}

static void channel_client_connecting(Channel * c) {
    trace(LOG_DISCOVERY, "discovery: channel_client_connecting");

    send_hello_message(c->client_data, c);
    discovery_channel_add(c);
    flush_stream(&c->out);
}

static void channel_client_connected(Channel * c) {
    int i;

    trace(LOG_DISCOVERY, "discovery: channel_client_connected, peer services:");
    for (i = 0; i < c->peer_service_cnt; i++) {
        trace(LOG_DISCOVERY, "  %s", c->peer_service_list[i]);
    }
    if (publish_peer_refresh_active == 0) {
        publish_peer_refresh_active = 1;
        post_event(republish_all_peers, NULL);
    }
}

static void channel_client_receive(Channel * c) {
    handle_protocol_message(c->client_data, c);
}

static void channel_client_disconnected(Channel * c) {
    trace(LOG_DISCOVERY, "discovery: channel_client_disconnected");
    assert(client_chan == c);
    discovery_channel_remove(c);
    protocol_channel_closed(c->client_data, c);
    protocol_release(c->client_data);
    if (publish_peer_refresh_active) {
        publish_peer_refresh_active = 0;
        cancel_event(republish_all_peers, NULL, 0);
    }
    client_chan = NULL;
    post_event_with_delay(restart_discovery, NULL, 300*1000);
}

static void peer_list_changed(PeerServer * ps, int changeType, void * client_data) {
    int i;

    if (client_chan != NULL && changeType > 0) {
        generate_publish_peer_command(client_chan, ps);
    }
    for (i = 0; i < chan_ind; i++) {
        remote_peer_change(ps, changeType, &chan_list[i]->out);
    }
}

/*
 * Make local peers discoverable
 */
static void make_local_discoverable(void) {
    peer_server_add_listener(peer_list_changed, NULL);
}

/*
 * Connect discovery client
 */
static void discovery_client(void) {
    Protocol * proto;
    Channel * c;
    PeerServer * ps;
    
    ps = channel_peer_from_url(DEFAULT_DISCOVERY_URL);
    if (ps == NULL) {
        trace(LOG_ALWAYS, "invalid discovery server URL");
        return;
    }
    if (client_chan != NULL) {
        peer_server_free(ps);
        return;
    }
    proto = protocol_alloc();
    ini_locator_service(proto);
    c = channel_connect(ps);
    peer_server_free(ps);
    if (c == NULL) {
        trace(LOG_DISCOVERY, "cannot connect to TCF discovery");
        protocol_release(proto);
        return;
    }
    c->connecting = channel_client_connecting;
    c->connected = channel_client_connected;
    c->receive = channel_client_receive;
    c->disconnected = channel_client_disconnected;
    c->client_data = proto;
    protocol_channel_opened(proto, c);
    add_event_handler(c, LOCATOR, "peerAdded", event_locator_peer_added);
    add_event_handler(c, LOCATOR, "peerChanged", event_locator_peer_changed);
    add_event_handler(c, LOCATOR, "peerRemoved", event_locator_peer_removed);
    channel_start(c);
    client_chan = c;
}

static int start_discovery(void) {
    assert(is_dispatch_thread());
    assert(!discovery_ismaster);
    trace(LOG_DISCOVERY, "discovery start");
    if (master_notifier != NULL && discovery_udp_server(NULL) == 0) {
        discovery_ismaster = 1;
    }
    else {
        discovery_client();
        if (client_chan == NULL) {
            post_event_with_delay(restart_discovery, NULL, 300*1000);
        }
    }
    return discovery_ismaster;
}

static void restart_discovery(void * x) {
    if (start_discovery() && master_notifier != NULL) {
        master_notifier();
    }
}

void discovery_start(DiscoveryMasterNotificationCB mastercb) {
    master_notifier = mastercb;
    make_local_discoverable();
    restart_discovery(NULL);
}

#endif
