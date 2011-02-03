/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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
 * Server initialization code.
 */

#ifndef D_server
#define D_server

#include <config.h>
#include <framework/protocol.h>

/*
 * Create and start TCF server listening on the port that is described by 'url'.
 * Return 0 on success, return -1 and set errno if error.
 */
extern int ini_server(const char * url, Protocol * proto, TCFBroadcastGroup * bcg);

#endif /* D_server */
