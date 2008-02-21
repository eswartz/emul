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
 * Peer server management interface
 */

#ifndef D_peer
#define D_peer

#include <stdlib.h>
#include <time.h>

typedef struct PeerServer PeerServer;

struct PeerServer {
    char * id;
    int max;
    int ind;
    struct {
        char * name;
        char * value;
    } * list;
    unsigned int flags;
    time_t create_time;
    time_t stale_time;
    PeerServer * next;
};

enum {
    PS_FLAG_LOCAL = 1,
    PS_FLAG_PRIVATE = PS_FLAG_LOCAL*2,
    PS_FLAG_DISCOVERABLE = PS_FLAG_PRIVATE*2
};

/* Allocate peer server object */
extern PeerServer * peer_server_alloc(void);

/* Add properties to peer server object */
extern void peer_server_addprop(PeerServer * ps, char * name, char * value);

/* Add properties to peer server object */
extern char * peer_server_getprop(PeerServer * ps, char * name, char * default_value);

/* Free peer server object */
extern void peer_server_free(PeerServer * ps);

/* Add peer server information */
extern PeerServer * peer_server_add(PeerServer * ps, unsigned int stale_delta);

/* Remove peer server information */
extern void peer_server_remove(const char *id);

typedef int (*peer_server_iter_fnp)(PeerServer * ps, void * client_data);

/* Iterate over all peer servers */
extern int peer_server_iter(peer_server_iter_fnp fnp, void * client_data);

typedef void (*peer_server_listener)(PeerServer *ps, int changeType, void * client_data);

/* Peer server list change listener */
extern void peer_server_add_listener(peer_server_listener listener, void * client_data);

#endif
