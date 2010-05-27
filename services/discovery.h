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
 * Discovery interface
 */

#ifndef D_discovery
#define D_discovery

#include <config.h>
#include <framework/protocol.h>
#include <framework/channel.h>
#include <framework/link.h>

#define DISCOVERY_TCF_PORT      1534

/*
 * Start discovery of remote peers. f no other discovery master exist on the
 * local machine, then this instance will become master, otherwise a
 * agent will attempt to connect to the existing master.  If the
 * existing master disappears, then a new attempt will be made to
 * become master or connect as a client.
 */
extern void discovery_start(void);

#if SERVICE_Locator

extern void ini_locator_service(Protocol * p, TCFBroadcastGroup * bcg);

#endif /* SERVICE_Locator */

#endif /* D_discovery */
