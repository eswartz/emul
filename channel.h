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
typedef struct ChannelCallbacks ChannelCallbacks;

struct Channel {
    InputStream inp;                    /* Input stream */
    OutputStream out;                   /* Output stream */
    TCFSuspendGroup * spg;              /* Suspend group */
    TCFBroadcastGroup * bcg;            /* Broadcast group */
    void * client_data;                 /* Client data */
    int peer_service_cnt;               /* Number of peer service names */
    char ** peer_service_list;          /* List of peer service names */
    LINK bclink;                        /* Broadcast list */
    LINK susplink;                      /* Suspend list */
    ChannelCallbacks *cb;               /* Client callback functions */
    int congestion_level;               /* Congestion level */

    void (*check_pending)(Channel *);   /* Check for pending messages */
    int (*message_count)(Channel *);    /* Return number of pending messages */
    void (*lock)(Channel *);            /* Lock channel from deletion */
    void (*unlock)(Channel *);          /* Unlock channel */
    int (*is_closed)(Channel *);        /* Return true if channel is closed */
    void (*close)(Channel *, int);      /* Closed channel */
};

struct ChannelCallbacks {
    void (*connecting)(Channel *);
    void (*connected)(Channel *);
    void (*receive)(Channel *);
    void (*disconnected)(Channel *);
};

typedef struct ChannelServer ChannelServer;
typedef struct ChannelServerCallbacks ChannelServerCallbacks;

struct ChannelServer {
    void * client_data;                 /* Client data */
    ChannelServerCallbacks * cb;        /* Call back handler */
    void (*close)(ChannelServer *);     /* Closed channel server */
};

struct ChannelServerCallbacks {
    void (*newConnection)(ChannelServer *, Channel *);
};

/*
 * Register channel close callback.
 * Service implementation can use the callback to deallocate resources
 * after a client disconnects.
 */
typedef void (*ChannelCloseListener)(Channel *);
extern void add_channel_close_listener(ChannelCloseListener listener);

/*
 * Start TCF channel server
 */
extern ChannelServer * channel_server(PeerServer * ps,
    ChannelServerCallbacks * cb, void * client_data);

/*
 * Connect to TCF channel server
 */
extern Channel * channel_connect(PeerServer * ps, ChannelCallbacks * cb,
    void * client_data, TCFSuspendGroup *, TCFBroadcastGroup *);

extern TCFSuspendGroup * suspend_group_alloc(void);
extern void suspend_group_free(TCFSuspendGroup *);

extern TCFBroadcastGroup * broadcast_group_alloc(void);
extern void broadcast_group_free(TCFBroadcastGroup *);

extern void stream_lock(Channel *);
extern void stream_unlock(Channel *);
extern int is_stream_closed(Channel *);
extern PeerServer * channel_peer_from_url(const char *);

extern void channels_suspend(TCFSuspendGroup * p);
extern int are_channels_suspended(TCFSuspendGroup * p);
extern void channels_resume(TCFSuspendGroup * p);
extern int channels_get_message_count(TCFSuspendGroup * p);

#endif
