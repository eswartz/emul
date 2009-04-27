/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
 * TCP channel interface
 */

#ifndef D_channel_tcp
#define D_channel_tcp

/*
 * Start TCP channel listener.
 * On error returns NULL and sets errno.
 */
extern ChannelServer * channel_tcp_server(PeerServer *);

/*
 * Connect client side over TCP.
 * On error returns NULL and sets errno.
 */
extern Channel * channel_tcp_connect(PeerServer *);

#endif
