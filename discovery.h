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
 * Discovery interface
 */

#ifndef D_discovery
#define D_discovery

#include "mdep.h"
#include "config.h"
#include "protocol.h"
#include "channel.h"
#include "link.h"

#define DEFAULT_DISCOVERY_URL   "TCP::1534"
#define DISCOVERY_TCF_PORT      1534
typedef void (*DiscoveryMasterNotificationCB)(void);

#if ENABLE_Discovery

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
 * agent will attempt to connect to the existing master.  If the
 * existing master disappears, then a new attempt will be made to
 * become master or connect as a client.
 *
 * Callback is invoked every time this agent instance becomes the master.
 */
extern void discovery_start(DiscoveryMasterNotificationCB);

#else

#define discovery_channel_add(channel)
#define discovery_channel_remove(channel)
#define discovery_start(call_back)

#endif

#endif
