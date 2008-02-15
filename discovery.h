/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Discovery interface
 */

#ifndef D_discovery
#define D_discovery

#include "protocol.h"
#include "channel.h"
#include "link.h"

#define DEFAULT_DISCOVERY_URL   "TCP::1534"
#define DISCOVERY_TCF_PORT      "1534"

/*
 * Connect discovery client
 */
extern Channel * discovery_client(void);

/*
 * Add channel to include in discovery updates
 */
extern void discovery_channel_add(Channel *);

/*
 * Remove channel from discovery updates
 */
extern void discovery_channel_remove(Channel *);

/*
 * Start discovery of remote peers.  If no other master exist on the
 * local machine, then this instance will become master, otherwise a
 * client will attempt to connect to the existing master.  If the
 * existing master disappears, then a new attempt will be made to
 * become master or connect as a client.
 *
 * Returns true if this is instance is the discovery master.
 * Otherwise returns false and the callback is invoked if this
 * instance becomes the master at a later stage.
 */

typedef void (*DiscoveryMasterNotificationCB)(void);
extern int discovery_start(DiscoveryMasterNotificationCB);

#endif
