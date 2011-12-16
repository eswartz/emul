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
 * This module forwards handling of process/thread OS contexts to remote peer.
 */

#ifndef D_context_proxy
#define D_context_proxy

#include <config.h>
#include <framework/channel.h>

struct RegisterData {
    uint8_t * data;
    uint8_t * mask;
};

extern void create_context_proxy(Channel * host, Channel * target, int forward_pm);

#endif /* D_context_proxy */
