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

#ifndef D_channel_tcp
#define D_channel_tcp

/*
 * Start TCP channel listener
 */
extern ChannelServer * channel_tcp_server(PeerServer *);

/*
 * Connect client side over TCP
 */
extern Channel * channel_tcp_connect(PeerServer *);

#endif
