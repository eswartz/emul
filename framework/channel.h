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
 * Transport agnostic TCF communication channel interface.
 */

#ifndef D_channel
#define D_channel

#include <framework/streams.h>
#include <framework/link.h>
#include <framework/peer.h>

typedef struct TCFBroadcastGroup TCFBroadcastGroup;
struct TCFBroadcastGroup {
    int magic;
    OutputStream out;                   /* Broadcast stream */
    LINK channels;                      /* Channels in group */
};

enum {
    ChannelStateStartWait,
    ChannelStateStarted,
    ChannelStateHelloSent,
    ChannelStateHelloReceived,
    ChannelStateConnected,
    ChannelStateRedirectSent,
    ChannelStateRedirectReceived,
    ChannelStateDisconnected
};

struct Protocol;
typedef struct Channel Channel;

struct Channel {
    InputStream inp;                    /* Input stream */
    OutputStream out;                   /* Output stream */
    TCFBroadcastGroup * bcg;            /* Broadcast group */
    void * client_data;                 /* Client data */
    struct Protocol * protocol;         /* Channel protocol */
    char * peer_name;                   /* A human readable remote peer name */
    int peer_service_cnt;               /* Number of remote peer service names */
    char ** peer_service_list;          /* List of remote peer service names */
    LINK bclink;                        /* Broadcast list */
    LINK susplink;                      /* Suspend list */
    int congestion_level;               /* Congestion level */
    int state;                          /* Current state */
    int disable_zero_copy;              /* Don't send ZeroCopy in Hello message even if we support it */

    /* Populated by channel implementation */
    void (*start_comm)(Channel *);      /* Start communication */
    void (*check_pending)(Channel *);   /* Check for pending messages */
    int (*message_count)(Channel *);    /* Return number of pending messages */
    void (*lock)(Channel *);            /* Lock channel from deletion */
    void (*unlock)(Channel *);          /* Unlock channel */
    int (*is_closed)(Channel *);        /* Return true if channel is closed */
    void (*close)(Channel *, int);      /* Close channel */

    /* Populated by channel client, NULL values mean default handling */
    void (*connecting)(Channel *);      /* Called when channel is ready for transmit */
    void (*connected)(Channel *);       /* Called when channel negotiation is complete */
    void (*receive)(Channel *);         /* Called when messages has been received */
    void (*disconnected)(Channel *);    /* Called when channel is disconnected */
};

typedef struct ChannelServer ChannelServer;

struct ChannelServer {
    void * client_data;                 /* Client data */
    void (*new_conn)(ChannelServer *, Channel *); /* New connection call back */
    void (*close)(ChannelServer *);     /* Close channel server */
};

/*
 * Register channel close callback.
 * Service implementation can use the callback to deallocate resources
 * after a client disconnects.
 */
typedef void (*ChannelCloseListener)(Channel *);
extern void add_channel_close_listener(ChannelCloseListener listener);

/*
 * Notify listeners about channel being closed.
 * The function is called from channel implementation code,
 * it is not intended to be called by clients.
 */
extern void notify_channel_closed(Channel *);

/*
 * Start TCF channel server.
 * On error returns NULL and sets errno.
 */
extern ChannelServer * channel_server(PeerServer *);

/*
 * Connect to TCF channel server.
 * On error returns NULL and sets errno.
 */
typedef void (*ChannelConnectCallBack)(void * /* callback_args */, int /* error */, Channel *);
extern void channel_connect(PeerServer * server, ChannelConnectCallBack callback, void * callback_args);

/*
 * Start communication of a newly created channel.
 */
extern void channel_start(Channel *);

/*
 * Close communication channel.
 */
extern void channel_close(Channel *);

/*
 * Allocate and return new "Broadcast Group" object.
 * Broadcast Group is collection of channels that participate together in broadcasting a message.
 */
extern TCFBroadcastGroup * broadcast_group_alloc(void);

/*
 * Remove channels from Broadcast Group and deallocate the group object.
 */
extern void broadcast_group_free(TCFBroadcastGroup *);

/*
 * Add a channel to a Broadcast Group.
 * If the channel is already in a group, it is removed from it first.
 */
extern void channel_set_broadcast_group(Channel *, TCFBroadcastGroup *);

/*
 * Remove channel from Suspend Group. Does nothing if the channel is not a member of a group.
 */
extern void channel_clear_broadcast_group(Channel *);

/*
 * Lock a channel. A closed channel will not be deallocated until it is unlocked.
 * Each call of this function incremnets the channel reference counter.
 */
extern void channel_lock(Channel *);

/*
 * Unlock a channel.
 * Each call of this function decremnets the channel reference counter.
 * If channel is closed and reference count is zero, then the channel object is deallocated.
 */
extern void channel_unlock(Channel *);

/*
 * Return 1 if channel is closed, otherwise return 0.
 */
extern int is_channel_closed(Channel *);

/* Depricated function names are kept for backward compatibility */
#define stream_lock(channel) channel_lock(channel)
#define stream_unlock(channel) channel_lock(channel)
#define is_stream_closed(channel) is_channel_closed(channel)

/*
 * Create and return PeerServer object with attribute values taken fron given URL.
 */
extern PeerServer * channel_peer_from_url(const char *);

#endif /* D_channel */
