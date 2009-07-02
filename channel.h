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
 * Transport agnostic channel interface
 */

#ifndef D_channel
#define D_channel

#include "streams.h"
#include "link.h"
#include "peer.h"

typedef struct TCFSuspendGroup TCFSuspendGroup;
struct TCFSuspendGroup {
    LINK channels;                      /* Channels in group */
    int suspended;                      /* Receive suspended when true */
};

typedef struct TCFBroadcastGroup TCFBroadcastGroup;
struct TCFBroadcastGroup {
    int magic;
    OutputStream out;                   /* Broadcast stream */
    LINK channels;                      /* Channels in group */
};

typedef struct Channel Channel;
struct Channel {
    InputStream inp;                    /* Input stream */
    OutputStream out;                   /* Output stream */
    TCFSuspendGroup * spg;              /* Suspend group */
    TCFBroadcastGroup * bcg;            /* Broadcast group */
    void * client_data;                 /* Client data */
    char * peer_name;                   /* A human readable remote peer name */
    int peer_service_cnt;               /* Number of remote peer service names */
    char ** peer_service_list;          /* List of remote peer service names */
    LINK bclink;                        /* Broadcast list */
    LINK susplink;                      /* Suspend list */
    int congestion_level;               /* Congestion level */
    int hello_received;                 /* "Hello" message has beed received - peer_service_list is valid */

    /* Populated by channel implementation */
    void (*start_comm)(Channel *);      /* Start communication */
    void (*check_pending)(Channel *);   /* Check for pending messages */
    int (*message_count)(Channel *);    /* Return number of pending messages */
    void (*lock)(Channel *);            /* Lock channel from deletion */
    void (*unlock)(Channel *);          /* Unlock channel */
    int (*is_closed)(Channel *);        /* Return true if channel is closed */
    void (*close)(Channel *, int);      /* Closed channel */

    /* Populated by channel client */
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
 * Start communication of a newly created channel
 */
extern void channel_start(Channel *);

/*
 * Close communication channel
 */
extern void channel_close(Channel *);

extern TCFSuspendGroup * suspend_group_alloc(void);
extern void suspend_group_free(TCFSuspendGroup *);
extern void channel_set_suspend_group(Channel *, TCFSuspendGroup *);
extern void channel_clear_suspend_group(Channel *);

extern TCFBroadcastGroup * broadcast_group_alloc(void);
extern void broadcast_group_free(TCFBroadcastGroup *);
extern void channel_set_broadcast_group(Channel *, TCFBroadcastGroup *);
extern void channel_clear_broadcast_group(Channel *);

extern void stream_lock(Channel *);
extern void stream_unlock(Channel *);
extern int is_stream_closed(Channel *);
extern PeerServer * channel_peer_from_url(const char *);

extern void channels_suspend(TCFSuspendGroup * p);
extern int are_channels_suspended(TCFSuspendGroup * p);
extern void channels_resume(TCFSuspendGroup * p);
extern int channels_get_message_count(TCFSuspendGroup * p);

#endif /* D_channel */
