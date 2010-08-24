/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
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
 * Named pipe channel interface
 */

#ifndef D_channel_pipe
#define D_channel_pipe

#include <framework/channel.h>

/*
 * Start named pipe channel listener.
 * On error returns NULL and sets errno.
 */
extern ChannelServer * channel_pipe_server(PeerServer * server);

/*
 * Connect client side over named pipe.
 * On error returns NULL and sets errno.
 */
extern void channel_pipe_connect(PeerServer * server, ChannelConnectCallBack callback, void * callback_args);

#endif /* D_channel_pipe */
