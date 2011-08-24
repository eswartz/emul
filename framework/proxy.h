/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
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

#include <framework/channel.h>

/*
 * Register channel redirection callback.
 * When a channel is redirected, the callback is called two times:
 * 1. before sending Hello message to target channel.
 * 2. after receiving Hello message from target channel, but before sending Hello to host.
 */
typedef void (*ChannelRedirectionListener)(Channel * /* host */, Channel * /* target */);
extern void add_channel_redirection_listener(ChannelRedirectionListener listener);

extern void proxy_create(Channel * c1, Channel * c2);

/*
 * Retrieve host (upstream) channel for proxy connection.  Channel
 * argument can be either the host or the target channel.  Returns
 * NULL if not a proxy connection.
 */
extern Channel *proxy_get_host_channel(Channel * c);

/*
 * Retrieve target (downstream) channel for proxy connection.  Channel
 * argument can be either the host or the target channel.  Returns
 * NULL if not a proxy connection.
 */
extern Channel *proxy_get_target_channel(Channel * c);

#endif /* D_proxy */
