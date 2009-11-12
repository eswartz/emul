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
 * This module implements tunneling of TCF messages to another target on behalf of a client.
 * This service intended to be used when a client has no direct access to a target.
 */

#ifndef D_proxy
#define D_proxy

#include "channel.h"

extern void proxy_create(Channel * c1, Channel * c2);

#endif /* D_proxy */


