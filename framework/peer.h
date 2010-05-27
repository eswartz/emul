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
 * Peer server management interface
 */

#ifndef D_peer
#define D_peer

#include <stdlib.h>
#include <time.h>

typedef struct PeerServer PeerServer;
typedef struct PeerServerList PeerServerList;

struct PeerServerList {
    const char * name;
    const char * value;
};

struct PeerServer {
    const char * id;
    int max;
    int ind;
    PeerServerList * list;
    unsigned int flags;
    time_t creation_time;
    time_t expiration_time;
    PeerServer * next;
};

enum {
    PS_FLAG_LOCAL = 1,
    PS_FLAG_PRIVATE = PS_FLAG_LOCAL * 2,
    PS_FLAG_DISCOVERABLE = PS_FLAG_PRIVATE * 2
};

/* Peer chane event types */
enum {
    PS_EVENT_ADDED,
    PS_EVENT_CHANGED,
    PS_EVENT_HEART_BEAT,
    PS_EVENT_REMOVED
};

/* Allocate peer server object */
extern PeerServer * peer_server_alloc(void);

/* Add properties to peer server object */
extern void peer_server_addprop(PeerServer * ps, const char * name, const char * value);

/* Add properties to peer server object */
extern const char * peer_server_getprop(PeerServer * ps, const char * name, const char * default_value);

/* Free peer server object */
extern void peer_server_free(PeerServer * ps);

/* Add peer server information */
extern PeerServer * peer_server_add(PeerServer * ps, unsigned int stale_delta);

/* Find peer server based on ID */
extern PeerServer * peer_server_find(const char * id);

/* Remove peer server information */
extern void peer_server_remove(const char * id);

typedef int (*peer_server_iter_fnp)(PeerServer * ps, void * client_data);

/* Iterate over all peer servers */
extern int peer_server_iter(peer_server_iter_fnp fnp, void * client_data);

typedef void (*peer_server_listener)(PeerServer * ps, int changeType, void * client_data);

/* Peer server list change listener */
extern void peer_server_add_listener(peer_server_listener listener, void * client_data);

#endif /* D_peer */
